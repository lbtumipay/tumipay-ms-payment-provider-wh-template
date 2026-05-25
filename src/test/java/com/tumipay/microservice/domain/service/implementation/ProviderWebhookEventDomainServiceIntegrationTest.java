package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.OperationStatusEnum;
import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderWebhookEventR2dbcRepository;
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
 * ProviderWebhookEventDomainServiceIntegrationTest2
 * <p>
 * ProviderWebhookEventDomainServiceIntegrationTest2 class.
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
class ProviderWebhookEventDomainServiceIntegrationTest {

    @Autowired
    private ProviderWebhookEventDomainService providerWebhookEventDomainService;

    @Autowired
    private IProviderWebhookEventR2dbcRepository providerWebhookEventRepository;

    @BeforeEach
    void cleanDatabase() {
        providerWebhookEventRepository.deleteAll().block();
    }

    @Test
    void shouldPersistWebhookEventInH2WhenSavingDomainEntity() {
        String uuid = UUID.randomUUID().toString();
        String idempotencyKey = UUID.randomUUID().toString();

        WebhookEvent event = WebhookEvent.builder()
            .uuid(uuid)
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYIN_STATUS")
            .externalEventId("ext-001")
            .idempotencyKey(idempotencyKey)
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED)
            .retryCount(0)
            .eventRequest("{\"status\":\"PENDING\"}")
            .receivedAt(Instant.now())
            .createdAt(Instant.now())
            .build();

        StepVerifier.create(providerWebhookEventDomainService.saveDomainEntity(event))
            .assertNext(result -> Assertions.assertEquals(OperationStatusEnum.SUCCESS, result.getStatus()))
            .verifyComplete();

        ProviderWebhookEventEntity persisted = providerWebhookEventRepository.findByIdempotencyKey(idempotencyKey).block();
        Assertions.assertNotNull(persisted);
        Assertions.assertEquals("PAYIN_STATUS", persisted.getEventType());
    }

    @Test
    void shouldFailIdempotencyValidationWhenDuplicateWebhookExistsInH2() {
        String idempotencyKey = UUID.randomUUID().toString();

        providerWebhookEventRepository.save(ProviderWebhookEventEntity.builder()
            .uuid(UUID.randomUUID())
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYIN_STATUS")
            .externalEventId("ext-dup")
            .idempotencyKey(idempotencyKey)
            .processingStatus("RECEIVED")
            .retryCount(0)
            .eventRequest("{\"status\":\"PENDING\"}")
            .receivedAt(Instant.now())
            .createdAt(Instant.now())
            .build()).block();

        StepVerifier.create(providerWebhookEventDomainService.validateIdempotency(idempotencyKey))
            .assertNext(result -> {
                Assertions.assertEquals(OperationStatusEnum.FAILED, result.getStatus());
                Assertions.assertTrue(result.getErrorMessage().contains("Duplicate webhook event detected"));
            })
            .verifyComplete();
    }

    @Test
    void shouldFindWebhookByUuidFromH2() {
        UUID uuid = UUID.randomUUID();

        providerWebhookEventRepository.save(ProviderWebhookEventEntity.builder()
            .uuid(uuid)
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYOUT_STATUS")
            .externalEventId("ext-find")
            .idempotencyKey(UUID.randomUUID().toString())
            .processingStatus("RECEIVED")
            .retryCount(0)
            .eventRequest("{\"status\":\"PENDING\"}")
            .receivedAt(Instant.now())
            .createdAt(Instant.now())
            .build()).block();

        StepVerifier.create(providerWebhookEventDomainService.getDomainEntityByUuId(uuid.toString()))
            .assertNext(result -> {
                Assertions.assertEquals(OperationStatusEnum.SUCCESS, result.getStatus());
                Assertions.assertEquals(uuid.toString(), result.getEntity().getUuid());
            })
            .verifyComplete();
    }
}
