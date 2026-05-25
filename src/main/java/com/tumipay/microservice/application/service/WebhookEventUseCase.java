package com.tumipay.microservice.application.service;

import com.tumipay.microservice.application.dto.WebhookEventComposition;
import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookEventResult;
import com.tumipay.microservice.domain.port.input.IWebhookEventUseCase;
import com.tumipay.microservice.domain.service.contract.IDomainValidationService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventDomainService;
import com.tumipay.microservice.shared.enums.BaseErrorCodeEnum;
import com.tumipay.microservice.shared.exception.BusinessException;
import com.tumipay.microservice.shared.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * WebhookEventUseCase
 * <p>
 * Use case implementation for webhook event reception and acknowledgment.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Log4j2
@Service
@AllArgsConstructor
public class WebhookEventUseCase implements IWebhookEventUseCase {

    private final IDomainValidationService domainValidationService;
    private final IProviderWebhookEventDomainService webhookEventDomainService;

    @Override
    public Mono<WebhookEventResult> processWebhookEvent(WebhookEvent webhookEvent) {

        return validateWebhookEvent(webhookEvent)
            .flatMap(this::checkIdempotency)
            .flatMap(this::persistWebhookEvent)
            .flatMap(this::buildWebhookEventResult)
            .doOnSuccess(result -> log.info(
                "Webhook process finished provider_code={}, event_type={}, uuid={}",
                result.getAdapterProviderCode(),
                result.getEventType(),
                result.getUuid()
            ))
            .doOnError(error ->
                log.error("Error processing webhook event, error: {}", error.getMessage())
            );
    }

    private Mono<WebhookEventComposition> validateWebhookEvent(WebhookEvent webhookEvent) {

        return domainValidationService.validate("ProviderWebhookEvent", webhookEvent)
            .flatMap(domainValidationResult -> {

                if (domainValidationResult.isSuccess()) {
                    return Mono.just(WebhookEventComposition.builder()
                        .webhookEvent(webhookEvent)
                        .build()
                    );
                }

                return Mono.error(new ValidationException(
                    BaseErrorCodeEnum.VALIDATION_ERROR.getCode(),
                    domainValidationResult.getErrorMessage(),
                    domainValidationResult.getValidationErrors())
                );
            });
    }

    private Mono<WebhookEventComposition> checkIdempotency(WebhookEventComposition composition) {
        return webhookEventDomainService.validateIdempotency(composition.getWebhookEvent().getIdempotencyKey())
            .flatMap(validationResult -> {

                if (validationResult.isSuccess()) {
                    return Mono.just(composition);
                }

                return Mono.error(new BusinessException(
                    BaseErrorCodeEnum.DUPLICATE_WEBHOOK_EVENT.getCode(),
                    validationResult.getErrorMessage())
                );
            });
    }

    private Mono<WebhookEventComposition> persistWebhookEvent(WebhookEventComposition composition) {

        final WebhookEvent webhookEvent = composition.getWebhookEvent();
        final Instant now = Instant.now();

        final WebhookEvent providerWebhookEvent = WebhookEvent.builder()
            .uuid(UUID.randomUUID().toString())
            .adapterProviderCode(webhookEvent.getAdapterProviderCode())
            .eventType(webhookEvent.getEventType())
            .externalEventId(webhookEvent.getExternalEventId())
            .idempotencyKey(webhookEvent.getIdempotencyKey())
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED)
            .errorCode(null)
            .retryCount(0)
            .lastError(null)
            .eventRequest(webhookEvent.getEventRequest())
            .receivedAt(now)
            .processedAt(null)
            .createdAt(now)
            .nextRetryAt(now)
            .updatedAt(now)
            .build();

        return webhookEventDomainService.saveDomainEntity(providerWebhookEvent)
            .flatMap(domainOperationResult -> {

                if (domainOperationResult.isFailed()) {
                    return Mono.error(new BusinessException(
                        "PROVIDER_WEBHOOK_EVENT_EXCEPTION",
                        domainOperationResult.getErrorMessage())
                    );
                }

                composition.setWebhookEvent(domainOperationResult.getEntity());
                return Mono.just(composition);
            });
    }

    private Mono<WebhookEventResult> buildWebhookEventResult(WebhookEventComposition composition) {

        final WebhookEvent persistedWebhookEvent = composition.getWebhookEvent();
        final WebhookEventResult webhookEventResult = WebhookEventResult.builder()
            .uuid(persistedWebhookEvent.getUuid())
            .eventType(persistedWebhookEvent.getEventType())
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED) // Semantic ACK for HTTP caller; DB stores RECEIVED — receiver scheduler moves to PENDING
            .adapterProviderCode(persistedWebhookEvent.getAdapterProviderCode())
            .message("Webhook event received and queued for processing")
            .receivedAt(persistedWebhookEvent.getReceivedAt())
            .timestamp(Instant.now())
            .build();

        composition.setWebhookEventResult(webhookEventResult);
        return Mono.just(webhookEventResult);
    }
}

