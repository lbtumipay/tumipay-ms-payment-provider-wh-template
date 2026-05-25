package com.tumipay.microservice.application.service;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderWebhookEventR2dbcRepository;
import com.tumipay.microservice.shared.exception.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

/**
 * WebhookEventUseCaseIntegrationTest
 * <p>
 * WebhookEventUseCaseIntegrationTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("it")
class WebhookEventUseCaseIntegrationTest {

    @Autowired
    private WebhookEventUseCase webhookEventUseCase;

    @Autowired
    private IProviderWebhookEventR2dbcRepository providerWebhookEventRepository;

    @BeforeEach
    void cleanDatabase() {
        providerWebhookEventRepository.deleteAll().block();
    }

    @Test
    void shouldPersistWebhookEventUsingRealDatabase() {
        String idempotencyKey = UUID.randomUUID().toString();
        WebhookEvent request = buildWebhookEvent(idempotencyKey);

        StepVerifier.create(webhookEventUseCase.processWebhookEvent(request))
            .assertNext(result -> {
                Assertions.assertEquals("PAYIN_STATUS", result.getEventType());
                Assertions.assertEquals("TP_PROVIDER", result.getAdapterProviderCode());
                Assertions.assertEquals(WebhookProcessingStatusEnum.RECEIVED, result.getProcessingStatus());
            })
            .verifyComplete();

        ProviderWebhookEventEntity persisted = providerWebhookEventRepository.findByIdempotencyKey(idempotencyKey).block();
        Assertions.assertNotNull(persisted);
        Assertions.assertEquals("PAYIN_STATUS", persisted.getEventType());
    }

    @Test
    void shouldRejectDuplicateWebhookEventByIdempotency() {
        String idempotencyKey = UUID.randomUUID().toString();
        providerWebhookEventRepository.save(ProviderWebhookEventEntity.builder()
            .uuid(UUID.randomUUID())
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYIN_STATUS")
            .externalEventId("ext-001")
            .idempotencyKey(idempotencyKey)
            .processingStatus("RECEIVED")
            .retryCount(0)
            .eventRequest("{\"status\":\"PENDING\"}")
            .receivedAt(Instant.now())
            .createdAt(Instant.now())
            .build()).block();

        WebhookEvent request = buildWebhookEvent(idempotencyKey);

        StepVerifier.create(webhookEventUseCase.processWebhookEvent(request))
            .expectErrorSatisfies(error -> {
                BusinessException businessException = (BusinessException) error;
                org.junit.jupiter.api.Assertions.assertEquals("DUPLICATE_WEBHOOK_EVENT", businessException.getCode());
            })
            .verify();
    }

    private WebhookEvent buildWebhookEvent(String idempotencyKey) {
        return WebhookEvent.builder()
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYIN_STATUS")
            .externalEventId("ext-" + idempotencyKey.substring(0, 8))
            .idempotencyKey(idempotencyKey)
            .eventRequest("{\"status\":\"PENDING\"}")
            .build();
    }
}