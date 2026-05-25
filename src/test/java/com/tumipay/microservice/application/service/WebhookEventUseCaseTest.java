package com.tumipay.microservice.application.service;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.service.contract.IDomainValidationService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventDomainService;
import com.tumipay.microservice.shared.dto.DomainOperationResult;
import com.tumipay.microservice.shared.dto.DomainValidationResult;
import com.tumipay.microservice.shared.exception.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * WebhookEventUseCaseTest
 * <p>
 * WebhookEventUseCaseTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@ExtendWith(MockitoExtension.class)
class WebhookEventUseCaseTest {

    @Mock
    private IDomainValidationService domainValidationService;

    @Mock
    private IProviderWebhookEventDomainService webhookEventDomainService;

    private WebhookEventUseCase webhookEventUseCase;

    @BeforeEach
    void setUp() {
        webhookEventUseCase = new WebhookEventUseCase(domainValidationService, webhookEventDomainService);
    }

    @Test
    void shouldFailWhenWebhookIdempotencyFails() {
        WebhookEvent webhookEvent = buildWebhookEvent();

        when(domainValidationService.validate(eq("ProviderWebhookEvent"), eq(webhookEvent)))
            .thenReturn(Mono.just(DomainValidationResult.success()));
        when(webhookEventDomainService.validateIdempotency("wh-001"))
            .thenReturn(Mono.just(DomainValidationResult.failure("duplicate webhook")));

        StepVerifier.create(webhookEventUseCase.processWebhookEvent(webhookEvent))
            .expectErrorSatisfies(error -> {
                BusinessException businessException = (BusinessException) error;
                Assertions.assertEquals("DUPLICATE_WEBHOOK_EVENT", businessException.getCode());
                Assertions.assertEquals("duplicate webhook", businessException.getMessage());
            })
            .verify();
    }

    @Test
    void shouldProcessWebhookSuccessfully() {
        WebhookEvent webhookEvent = buildWebhookEvent();
        WebhookEvent persistedEvent = WebhookEvent.builder()
            .uuid("wh-uuid-001")
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYIN_STATUS")
            .idempotencyKey("wh-001")
            .eventRequest("{\"status\":\"PENDING\"}")
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED)
            .receivedAt(Instant.parse("2026-04-06T13:00:00Z"))
            .createdAt(Instant.parse("2026-04-06T13:00:00Z"))
            .build();

        when(domainValidationService.validate(eq("ProviderWebhookEvent"), eq(webhookEvent)))
            .thenReturn(Mono.just(DomainValidationResult.success()));
        when(webhookEventDomainService.validateIdempotency("wh-001"))
            .thenReturn(Mono.just(DomainValidationResult.success()));
        when(webhookEventDomainService.saveDomainEntity(any(WebhookEvent.class)))
            .thenReturn(Mono.just(DomainOperationResult.success(persistedEvent)));

        StepVerifier.create(webhookEventUseCase.processWebhookEvent(webhookEvent))
            .assertNext(result -> {
                Assertions.assertEquals("wh-uuid-001", result.getUuid());
                Assertions.assertEquals("PAYIN_STATUS", result.getEventType());
                Assertions.assertEquals("TP_PROVIDER", result.getAdapterProviderCode());
                Assertions.assertEquals("Webhook event received and queued for processing", result.getMessage());
            })
            .verifyComplete();
    }

    private WebhookEvent buildWebhookEvent() {
        return WebhookEvent.builder()
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYIN_STATUS")
            .externalEventId("external-001")
            .idempotencyKey("wh-001")
            .eventRequest("{\"status\":\"PENDING\"}")
            .build();
    }
}