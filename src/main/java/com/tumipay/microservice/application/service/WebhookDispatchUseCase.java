package com.tumipay.microservice.application.service;

import com.tumipay.microservice.application.dto.WebhookDispatchComposition;
import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.input.IWebhookWorkerUseCase;
import com.tumipay.microservice.domain.port.output.IPaymentGatewayWebhookAdapterPort;
import com.tumipay.microservice.domain.service.contract.IProviderTransactionDomainService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventClassifierService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventDomainService;
import com.tumipay.microservice.shared.enums.BaseErrorCodeEnum;
import com.tumipay.microservice.shared.exception.BusinessException;
import com.tumipay.microservice.shared.exception.GatewayWebhookException;
import com.tumipay.microservice.shared.properties.WebhookDispatcherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * WebhookDispatchUseCase
 * <p>
 * Application service that implements the Webhook Worker Claim-Batch pattern.
 * Orchestrates the poll-and-process cycle: claims a batch of PENDING events,
 * processes each one concurrently, and acknowledges or schedules retries.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 10/04/2026
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class WebhookDispatchUseCase implements IWebhookWorkerUseCase {

    private final IProviderWebhookEventDomainService providerWebhookEventDomainService;
    private final WebhookDispatcherProperties webhookDispatcherProperties;
    private final IPaymentGatewayWebhookAdapterPort paymentGatewayWebhookAdapterPort;
    private final IProviderTransactionDomainService providerTransactionDomainService;
    private final IProviderWebhookEventClassifierService webhookEventTypeClassifierService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> pollAndProcess(String workerId, int batchSize) {

        return providerWebhookEventDomainService.claimBatch(workerId, batchSize)
            .flatMap(webhookEvent -> this.processEvent(webhookEvent, workerId), resolveMaxConcurrency())
            .then()
            .doOnSubscribe(s -> log.debug(
                "Webhook worker poll cycle started: workerId={}, batchSize={}", workerId, batchSize
            ))
            .doOnSuccess(unused -> log.debug(
                "Webhook worker poll cycle completed: workerId={}", workerId
            ))
            .doOnError(error -> log.error(
                "Error in webhook worker poll cycle: workerId={}, error={}", workerId, error.getMessage(), error
            ));
    }

    // -------------------------------------------------------------------------
    // Internal processing
    // -------------------------------------------------------------------------

    private Mono<Void> processEvent(WebhookEvent event, final String workerId) {

        return Mono.defer(() -> {

            return composeDispatch(event, workerId)
                .flatMap(this::loadClassifierWebhook)
                .flatMap(this::loadProviderTransaction)
                .flatMap(this::prepareEventToDispatch)
                .flatMap(this::dispatchWebhookEventNew)
                .flatMap(this::markAsProcessed)
                .then()
                .onErrorResume(error -> handleProcessingError(event, error));
        });
    }

    private Mono<WebhookDispatchComposition> composeDispatch(final WebhookEvent event, final String workerId) {

        return Mono.just(WebhookDispatchComposition.builder()
            .workerId(workerId)
            .webhookEvent(event)
            .build()
        );
    }

    private Mono<WebhookDispatchComposition> loadClassifierWebhook(WebhookDispatchComposition composition) {

        return Mono.just(composition.getWebhookEvent())
            .flatMap(webhookEvent -> {

                final WebhookClassifierResult webhookClassifierResult = webhookEventTypeClassifierService.classifyWebhook(
                    composition.getWebhookEvent().getEventRequest()
                );

                composition.setWebhookClassifierResult(
                    webhookClassifierResult
                );

                return Mono.just(composition);
            });
    }

    private Mono<WebhookDispatchComposition> loadProviderTransaction(WebhookDispatchComposition composition) {

        return Mono.just(composition.getWebhookClassifierResult())
            .flatMap(webhookClassifierResult -> providerTransactionDomainService.getByProviderTransactionId(
                webhookClassifierResult.getProviderTransactionId()
            ))
            .flatMap(domainOperationResult -> {

                if (domainOperationResult.isSuccess()) {
                    // If the provider transaction is found, set it in the composition for downstream processing
                    composition.setProviderTransaction(domainOperationResult.getEntity());
                } else {

                    log.warn("Provider transaction not found for providerTransactionId={}",
                        composition.getWebhookClassifierResult().getProviderTransactionId());

                    return Mono.error(new BusinessException(
                        BaseErrorCodeEnum.TRANSACTION_NOT_FOUND.getCode(),
                        "Provider transaction not found for providerTransactionId for webhook event id = " + composition.getWebhookEvent().getId()
                    ));
                }

                return Mono.just(composition);
            });
    }

    private Mono<WebhookDispatchComposition> prepareEventToDispatch(WebhookDispatchComposition composition) {

        return Mono.just(composition).flatMap(currentComposition -> {
            final WebhookEvent webhookEvent = currentComposition.getWebhookEvent();
            final ProviderTransaction providerTransaction = currentComposition.getProviderTransaction();
            webhookEvent.setProviderTransactionId(providerTransaction.getProviderTransactionId());
            webhookEvent.setTransactionId(providerTransaction.getTransactionId());
            webhookEvent.setReferenceId(providerTransaction.getReferenceId());
            currentComposition.setWebhookEvent(webhookEvent);
            return Mono.just(currentComposition);
        });
    }


    /**
     * Dispatches a webhook event to the payment gateway and handles its response.
     *
     * @param composition the {@code WebhookDispatchComposition} containing the webhook event to be dispatched.
     * @return a {@code Mono<WebhookDispatchComposition>} that emits the original {@code WebhookDispatchComposition}
     * or an error if the gateway rejects the webhook event.
     */
    private Mono<WebhookDispatchComposition> dispatchWebhookEventNew(final WebhookDispatchComposition composition) {

        final WebhookEvent webhookEvent = composition.getWebhookEvent();

        log.info("Processing webhook event id={}, type={}, uuid={}",
            webhookEvent.getId(),
            webhookEvent.getEventType(),
            webhookEvent.getUuid()
        );

        return paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(webhookEvent)
            .flatMap(gatewayWebhookResult -> {

                if (gatewayWebhookResult.getStatus() != null && "FAILED".equalsIgnoreCase(gatewayWebhookResult.getStatus())) {
                    return Mono.error(new BusinessException(
                        gatewayWebhookResult.getCode(),
                        "Gateway rejected the webhook event with code: " + gatewayWebhookResult.getCode()
                    ));
                }

                log.info(
                    "Webhook event dispatched to Gateway - uuid=[{}], eventType=[{}], code=[{}], status=[{}], gatewayEventId=[{}]",
                    webhookEvent.getUuid(), webhookEvent.getEventType(),
                    gatewayWebhookResult.getCode(), gatewayWebhookResult.getStatus(),
                    gatewayWebhookResult.getData() != null ? gatewayWebhookResult.getData().getGatewayEventId() : null
                );

                composition.setGatewayWebhookResult(
                    gatewayWebhookResult
                );
                return Mono.just(composition);
            });
    }

    private Mono<WebhookDispatchComposition> markAsProcessed(final WebhookDispatchComposition composition) {

        final WebhookEvent webhookEvent = composition.getWebhookEvent();

        return providerWebhookEventDomainService.markAsProcessed(webhookEvent.getId())
            .doOnSuccess(updated -> log.debug(
                "Webhook event PROCESSED id={}, uuid={}", webhookEvent.getId(), webhookEvent.getUuid()
            ))
            .thenReturn(composition);
    }

    private Mono<Void> handleProcessingError(WebhookEvent event, Throwable error) {

        final int maxRetryCount = webhookDispatcherProperties.getMaxRetryCount();
        final String errorCode = resolveErrorCode(error);
        final String lastError = error.getMessage();

        log.error("Error processing webhook event id={}, uuid={}, retryCount={}/{}, error={}",
            event.getId(), event.getUuid(), event.getRetryCount(), maxRetryCount, error.getMessage());

        return providerWebhookEventDomainService.markForRetry(event.getId(), errorCode, lastError, maxRetryCount)
            .doOnSuccess(updated -> {
                final boolean isFailed = updated.getProcessingStatus() != null
                    && WebhookProcessingStatusEnum.FAILED == updated.getProcessingStatus();
                if (isFailed) {
                    log.error("Webhook event FAILED permanently id={}, uuid={} after {} retries",
                        event.getId(), event.getUuid(), updated.getRetryCount());
                } else {
                    log.info("Webhook event scheduled for RETRY id={}, nextRetryAt={}",
                        event.getId(), updated.getNextRetryAt());
                }
            })
            .then();
    }

    private String resolveErrorCode(Throwable error) {
        if (error instanceof BusinessException be) {
            return be.getCode();
        }
        if (error instanceof GatewayWebhookException gwe) {
            return gwe.getCode();
        }

        return BaseErrorCodeEnum.WEBHOOK_PROCESSING_ERROR.getCode();
    }

    private int resolveMaxConcurrency() {
        Integer concurrency = webhookDispatcherProperties.getConcurrency();
        return (concurrency != null && concurrency > 0) ? concurrency : 10;
    }
}

