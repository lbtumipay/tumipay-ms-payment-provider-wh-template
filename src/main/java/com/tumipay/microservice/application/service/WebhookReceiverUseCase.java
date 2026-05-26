package com.tumipay.microservice.application.service;

import com.tumipay.microservice.application.component.mapper.IProviderTransactionMapper;
import com.tumipay.microservice.application.component.mapper.IWebhookEventMapper;
import com.tumipay.microservice.application.dto.WebhookReceiverComposition;
import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.model.provider.ProviderWebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.input.IWebhookReceiverUseCase;
import com.tumipay.microservice.domain.service.contract.IProviderTransactionDomainService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventClassifierService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventDomainService;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.request.ProviderWebhookRequest;
import com.tumipay.microservice.infrastructure.component.constant.WebhookReceiverConstant;
import com.tumipay.microservice.shared.exception.BusinessException;
import com.tumipay.microservice.shared.util.CommonJsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * WebhookReceiverUseCase
 * <p>
 * Application service that implements the pre-processing pipeline for RECEIVED webhook events.
 * Reads a batch of RECEIVED events, classifies them by type, and transitions each to PENDING
 * so the {@code WebhookWorkerScheduler} can dispatch them to the TumiPay Payment Gateway.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-16
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class WebhookReceiverUseCase implements IWebhookReceiverUseCase {

    private final IProviderWebhookEventDomainService webhookEventDomainService;
    private final IProviderTransactionDomainService providerTransactionDomainService;
    private final IProviderWebhookEventClassifierService webhookEventTypeClassifierService;
    private final IWebhookEventMapper webhookEventMapper;
    private final IProviderTransactionMapper providerTransactionMapper;

    @Override
    public Mono<Void> processReceivedBatch(int batchSize) {

        return webhookEventDomainService.findReceivedBatch(batchSize)
            .flatMap(this::processReceivedEvent)
            .then()
            .doOnSubscribe(s -> log.debug(
                "Webhook receiver batch started: batchSize={}", batchSize
            ))
            .doOnSuccess(unused -> log.debug(
                "Webhook receiver batch completed"
            ))
            .doOnError(error -> log.error(
                "Error in webhook receiver batch: {}", error.getMessage(), error
            ));
    }

    private Mono<Void> processReceivedEvent(WebhookEvent event) {

        return Mono.defer(() -> {

                // 1. Classify the event type by evaluating the raw provider payload
                final WebhookClassifierResult webhookClassifierResult = webhookEventTypeClassifierService.classifyWebhook(
                    event.getEventRequest()
                );

                // For PAYOUT_TRANSACTION events, we attempt to update the transaction status according to the webhook payload before transitioning to PENDING.
                if (isPayOutEvent(webhookClassifierResult.getClassifiedType())) {
                    return processPayoutWebhookEvent(
                        event,
                        webhookClassifierResult
                    );
                }

                // For PAYIN_TRANSACTION events, we assume no transaction status update is required and transition directly to PENDING
                if (isPayInEvent(webhookClassifierResult.getClassifiedType())) {
                    return processPayInWebhookEvent(
                        event,
                        webhookClassifierResult
                    );
                }

                // UNKNOWN_EVENT — transition to PENDING preserving the classification
                log.warn("Unclassified event [{}] for webhook id={}. Saving as UNKNOWN_EVENT and transitioning to PENDING.",
                    webhookClassifierResult,
                    event.getId()
                );

                return transitionWebhookToPending(
                    event,
                    WebhookEventTypeEnum.UNKNOWN_EVENT
                );
            })
            .onErrorResume(error -> {
                log.error("Error processing received webhook event id={}, uuid={}, error={}",
                    event.getId(), event.getUuid(), error.getMessage(), error);
                return Mono.empty();
            });
    }

    /**
     * Returns true if the classifiedType corresponds to a PAYOUT_TRANSACTION event.
     * Matches all WebhookEventTypeEnum values whose name starts with "PAYOUT_TRANSACTION".
     */
    private boolean isPayOutEvent(WebhookEventTypeEnum classifiedType) {
        return classifiedType != null && classifiedType.name().startsWith("PAYOUT_TRANSACTION");
    }

    /**
     * Returns true if the classifiedType corresponds to a PAYIN_TRANSACTION event.
     * Matches all WebhookEventTypeEnum values whose name starts with "PAYIN_TRANSACTION".
     */
    private boolean isPayInEvent(WebhookEventTypeEnum classifiedType) {
        return classifiedType != null && classifiedType.name().startsWith("PAYIN_TRANSACTION");
    }

    /**
     * Processes a PAYOUT_TRANSACTION webhook webhookEvent using a composition-based pipeline:
     * 1. Builds the initial {@link WebhookReceiverComposition}.
     * 2. Deserializes the raw webhookEvent payload to {@link ProviderWebhookRequest}.
     * 3. Looks up the {@link ProviderTransaction} by providerTransactionId.
     * 4. Updates the transaction status when it is in a non-final state.
     * 5. Transitions the webhook webhookEvent from RECEIVED to PENDING.
     */
    private Mono<Void> processPayoutWebhookEvent(final WebhookEvent webhookEvent, final WebhookClassifierResult webhookClassifierResult) {

        return Mono.just(WebhookReceiverComposition.builder()
                .webhookEvent(webhookEvent)
                .webhookClassifierResult(webhookClassifierResult)
                .build()
            )
            .flatMap(this::deserializeWebhookRequest)
            .flatMap(this::resolveProviderTransaction)
            .flatMap(this::updateTransactionStatusIfRequired)
            .flatMap(this::transitionCompositionWebhookToPending)
            .doOnSuccess(v -> log.debug("processPayoutWebhookEvent pipeline completed for webhook id={}", webhookEvent.getId()))
            .doOnError(error -> log.error("Error in processPayoutWebhookEvent pipeline for webhook id={}: {}", webhookEvent.getId(), error.getMessage()));
    }

    /**
     * Step 1 – Deserializes {@code webhookEvent.eventRequest} to {@link ProviderWebhookRequest}
     * and stores it in the composition.
     * <p>
     * If deserialization fails the composition is returned with a {@code null}
     * {@code providerWebhookRequest}; downstream steps must handle that case gracefully.
     */
    private Mono<WebhookReceiverComposition> deserializeWebhookRequest(WebhookReceiverComposition composition) {

        return Mono.just(composition.getWebhookEvent())
            .flatMap(webhookEvent -> {

                final ProviderWebhookEvent providerWebhookEvent = deserializeEventRequest(webhookEvent);
                composition.setProviderWebhookEvent(providerWebhookEvent);
                return Mono.just(composition);
            });
    }

    /**
     * Step 2 – Looks up the {@link ProviderTransaction} by the providerTransactionId obtained
     * from the deserialized request and stores it in the composition.
     * <p>
     * If the request could not be deserialized (step 2) or the transaction is not found,
     * the composition is returned with a {@code null} {@code providerTransaction};
     * the update step (step 4) will then be a no-op.
     */
    private Mono<WebhookReceiverComposition> resolveProviderTransaction(WebhookReceiverComposition composition) {

        final ProviderWebhookEvent providerWebhookEvent = composition.getProviderWebhookEvent();

        if (providerWebhookEvent == null) {

            // Deserialization failed — skip lookup
            log.warn("Cannot resolve provider transaction because deserialization of webhook eventRequest failed for webhook id={}. Skipping transaction lookup); webhook will still transition to PENDING.",
                composition.getWebhookEvent().getId()
            );
            return Mono.just(composition);
        }

        final WebhookClassifierResult webhookClassifierResult = composition.getWebhookClassifierResult();
        final String providerTransactionId = webhookClassifierResult.getProviderTransactionId();

        return providerTransactionDomainService.getByProviderTransactionId(providerTransactionId)
            .flatMap(result -> {

                if (result.isFailed() || result.getEntity() == null) {
                    log.warn("Transaction not found for providerTransactionId={}, webhook id={}. Skipping status update; webhook will still transition to PENDING.",
                        providerTransactionId,
                        composition.getWebhookEvent().getId()
                    );
                    // providerTransaction remains null → step 4 is a no-op
                    return Mono.just(composition);
                }

                composition.setProviderTransaction(result.getEntity());
                return Mono.just(composition);
            });
    }

    /**
     * Step 3 – Updates the transaction status when the resolved transaction is in a non-final
     * state and the classifier provided a target {@link TransactionStatusEnum}.
     * <p>
     * The step is skipped (no-op) when:
     * <ul>
     *   <li>No {@code providerTransaction} was resolved (step 3 found nothing).</li>
     *   <li>The transaction is already in a final state.</li>
     *   <li>The classifier could not resolve a target status.</li>
     * </ul>
     */
    private Mono<WebhookReceiverComposition> updateTransactionStatusIfRequired(WebhookReceiverComposition composition) {

        final ProviderTransaction transaction = composition.getProviderTransaction();

        if (transaction == null) {
            // Nothing to update
            return Mono.just(composition);
        }

        if (!isNonFinalStatus(transaction.getStatus())) {
            log.info("Transaction for providerTransactionId={} is already in final state [{}]. Skipping status update. Webhook id={} will transition to PENDING.",
                transaction.getProviderTransactionId(),
                transaction.getStatus(),
                composition.getWebhookEvent().getId()
            );
            return Mono.just(composition);
        }

        final TransactionStatusEnum newStatus = composition.getWebhookClassifierResult().getTransactionStatus();

        if (newStatus == null) {
            log.warn("Could not resolve new transaction status from webhook for providerTransactionId={}, webhook id={}. Skipping update; transitioning to PENDING.",
                transaction.getProviderTransactionId(),
                composition.getWebhookEvent().getId()
            );
            return Mono.just(composition);
        }

        final ProviderTransaction updated = providerTransactionMapper.mapUpdateFromWebhookStatus(
            transaction,
            newStatus
        );

        return providerTransactionDomainService.updateDomainEntity(updated)
            .flatMap(updateResult -> {

                if (updateResult.isFailed()) {
                    log.error("Failed to update transaction status for providerTransactionId={}, webhook id={}: {}",
                        transaction.getProviderTransactionId(),
                        composition.getWebhookEvent().getId(),
                        updateResult.getErrorMessage()
                    );
                } else {
                    composition.setProviderTransaction(updateResult.getEntity());
                }

                return Mono.just(composition);
            });
    }

    /**
     * Step 4 – Transitions the webhook event from RECEIVED to PENDING using the classified
     * event type stored in the composition.
     */
    private Mono<Void> transitionCompositionWebhookToPending(WebhookReceiverComposition composition) {

        final WebhookEvent webhookEvent = composition.getWebhookEvent();
        final WebhookEventTypeEnum webhookEventType = composition.getWebhookClassifierResult().getClassifiedType();
        final ProviderTransaction providerTransaction = composition.getProviderTransaction();

        if(providerTransaction != null) {
            webhookEvent.setProviderTransactionId(providerTransaction.getProviderTransactionId());
            webhookEvent.setTransactionId(providerTransaction.getTransactionId());
            webhookEvent.setReferenceId(providerTransaction.getReferenceId());
        }

        return transitionWebhookToPending(
            webhookEvent,
            webhookEventType
        );
    }

    /**
     * Processes a PAYIN_TRANSACTION webhook event.
     * No transaction status update is performed for PayIn events.
     * The webhook is transitioned directly from RECEIVED to PENDING with the
     * classified eventType so the WebhookWorkerScheduler dispatches it to
     * the TumiPay Payment Gateway with a properly typed event.
     */
    private Mono<Void> processPayInWebhookEvent(WebhookEvent event, WebhookClassifierResult webhookClassifierResult) {

        log.debug("PAYIN_TRANSACTION webhook id={} type={} — no transaction update required. Transitioning to PENDING.",
            event.getId(),
            webhookClassifierResult.getClassifiedType()
        );

        return Mono.empty();
    }


    /**
     * Updates the webhook webhookEvent processing status from RECEIVED to PENDING and
     * persists the type resolved by IWebhookEventTypeClassifier.
     *
     * @param webhookEvent          the original RECEIVED webhook webhookEvent.
     * @param webhookEventType the WebhookEventTypeEnum determined by the classifier.
     */
    private Mono<Void> transitionWebhookToPending(final WebhookEvent webhookEvent, final WebhookEventTypeEnum webhookEventType) {

        final WebhookEvent pendingEvent = webhookEventMapper.mapToPending(
            webhookEvent,
            webhookEventType
        );

        return webhookEventDomainService.updateDomainEntity(pendingEvent)
            .flatMap(result -> {
                if (result.isFailed()) {

                    log.error("Failed to transition webhook id={} to PENDING: {}",
                        webhookEvent.getId(),
                        result.getErrorMessage()
                    );

                    return Mono.error(new BusinessException(
                        WebhookReceiverConstant.ERROR_CODE_WEBHOOK_UPDATE_FAILED,
                        result.getErrorMessage())
                    );
                }

                log.info("Webhook id={} uuid={} classified as [{}] and transitioned to PENDING successfully.",
                    webhookEvent.getId(),
                    webhookEvent.getUuid(),
                    webhookEventType
                );
                return Mono.empty();
            })
            .then();
    }

    /**
     * Deserializes the JSON string stored in WebhookEvent.eventRequest to ProviderMoneyMovementWebhookRequest.
     *
     * @param event the webhook event containing the raw JSON payload.
     * @return a ProviderMoneyMovementWebhookRequest instance, or null if deserialization fails.
     */
    private ProviderWebhookEvent deserializeEventRequest(WebhookEvent event) {

        try {

            return CommonJsonUtils.fromJson(
                event.getEventRequest(),
                ProviderWebhookEvent.class
            );
        } catch (Exception e) {

            log.error("Failed to deserialize eventRequest for webhook id={}, uuid={}: {}",
                event.getId(),
                event.getUuid(),
                e.getMessage()
            );
            return null;
        }
    }

    /**
     * Determines whether a transaction status is non-final and can still be updated.
     * Non-final states: PENDING, ERROR.
     * Final states: APPROVED, REJECTED, EXPIRED, CANCELLED.
     */
    private boolean isNonFinalStatus(TransactionStatusEnum status) {
        return status == TransactionStatusEnum.PENDING
            || status == TransactionStatusEnum.ERROR;
    }
}

