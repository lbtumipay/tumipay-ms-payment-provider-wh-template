package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.output.IProviderWebhookEventRepositoryPort;
import com.tumipay.microservice.domain.port.output.IWebhookWorkerRepositoryPort;
import com.tumipay.microservice.shared.enums.BaseOperationStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ProviderWebhookEventDomainServiceTest2
 * <p>
 * ProviderWebhookEventDomainServiceTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 23/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderWebhookEventDomainService Unit Tests")
class ProviderWebhookEventDomainServiceTest {

    @Mock
    private IProviderWebhookEventRepositoryPort repositoryPort;

    @Mock
    private IWebhookWorkerRepositoryPort webhookWorkerRepositoryPort;

    private ProviderWebhookEventDomainService service;
    @BeforeEach
    void setUp() {
        service = new ProviderWebhookEventDomainService(repositoryPort, webhookWorkerRepositoryPort);
    }
    private WebhookEvent validCreateEntity() {
        return WebhookEvent.builder()
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYIN_STATUS")
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED)
            .eventRequest("{\"status\":\"PENDING\"}")
            .idempotencyKey(UUID.randomUUID().toString())
            .receivedAt(Instant.now())
            .createdAt(Instant.now())
            .build();
    }
    private WebhookEvent validUpdateEntity() {
        return WebhookEvent.builder()
            .uuid(UUID.randomUUID().toString())
            .processingStatus(WebhookProcessingStatusEnum.PROCESSED)
            .build();
    }

    @Test
    void getDomainEntityByUuId_blankUuid_shouldReturnFailure() {
        StepVerifier.create(service.getDomainEntityByUuId(""))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("entityUuId is required and cannot be empty", r.getErrorMessage());
            }).verifyComplete();
    }
    @Test
    void getDomainEntityByUuId_invalidUuidFormat_shouldReturnFailure() {
        StepVerifier.create(service.getDomainEntityByUuId("not-a-valid-uuid"))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("entityUuId format is invalid", r.getErrorMessage());
            }).verifyComplete();
    }
    @Test
    void getDomainEntityByUuId_validUuidFound_shouldReturnSuccess() {
        UUID uuid = UUID.randomUUID();
        WebhookEvent entity = validCreateEntity();
        entity.setUuid(uuid.toString());
        when(repositoryPort.findByUuid(uuid)).thenReturn(Mono.just(entity));
        StepVerifier.create(service.getDomainEntityByUuId(uuid.toString()))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus()))
            .verifyComplete();
    }
    @Test
    void getDomainEntityByUuId_validUuidNotFound_shouldReturnFailure() {
        UUID uuid = UUID.randomUUID();
        when(repositoryPort.findByUuid(uuid)).thenReturn(Mono.empty());
        StepVerifier.create(service.getDomainEntityByUuId(uuid.toString()))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("ProviderWebhookEvent not found for uuid=" + uuid, r.getErrorMessage());
            }).verifyComplete();
    }
    @Test
    void getDomainEntityByUuId_repoError_shouldReturnFailure() {
        UUID uuid = UUID.randomUUID();
        when(repositoryPort.findByUuid(uuid)).thenReturn(Mono.error(new RuntimeException("db error")));
        StepVerifier.create(service.getDomainEntityByUuId(uuid.toString()))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("Error getting ProviderWebhookEvent: db error", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void saveDomainEntity_nullEntity_shouldReturnFailure() {
        StepVerifier.create(service.saveDomainEntity(null))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }
    @Test
    void saveDomainEntity_missingAdapterProviderCode_shouldReturnFailure() {
        WebhookEvent invalid = WebhookEvent.builder()
            .eventType("PAYIN_STATUS")
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED)
            .eventRequest("{\"status\":\"PENDING\"}")
            .build(); // missing adapterProviderCode
        StepVerifier.create(service.saveDomainEntity(invalid))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }
    @Test
    void saveDomainEntity_missingProcessingStatus_shouldReturnFailure() {
        WebhookEvent invalid = WebhookEvent.builder()
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYIN_STATUS")
            .eventRequest("{\"status\":\"PENDING\"}")
            .build(); // missing processingStatus
        StepVerifier.create(service.saveDomainEntity(invalid))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }
    @Test
    void saveDomainEntity_validEntity_shouldReturnSuccess() {
        WebhookEvent entity = validCreateEntity();
        when(repositoryPort.save(any())).thenReturn(Mono.just(entity));
        StepVerifier.create(service.saveDomainEntity(entity))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus()))
            .verifyComplete();
    }
    @Test
    void saveDomainEntity_repoError_shouldReturnFailure() {
        WebhookEvent entity = validCreateEntity();
        when(repositoryPort.save(any())).thenReturn(Mono.error(new RuntimeException("save error")));
        StepVerifier.create(service.saveDomainEntity(entity))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("Error saving ProviderWebhookEvent: save error", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void updateDomainEntity_nullEntity_shouldReturnFailure() {
        StepVerifier.create(service.updateDomainEntity(null))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void updateDomainEntity_invalidUuidField_shouldReturnFailure() {
        WebhookEvent invalid = WebhookEvent.builder()
            .uuid("not-a-valid-uuid")
            .processingStatus(WebhookProcessingStatusEnum.PROCESSED)
            .build();
        StepVerifier.create(service.updateDomainEntity(invalid))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void updateDomainEntity_missingProcessingStatus_shouldReturnFailure() {
        WebhookEvent invalid = WebhookEvent.builder()
            .uuid(UUID.randomUUID().toString())
            .build(); // missing processingStatus
        StepVerifier.create(service.updateDomainEntity(invalid))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void updateDomainEntity_validEntity_shouldReturnSuccess() {
        WebhookEvent entity = validUpdateEntity();
        when(repositoryPort.update(any())).thenReturn(Mono.just(entity));
        StepVerifier.create(service.updateDomainEntity(entity))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void updateDomainEntity_repoError_shouldReturnFailure() {
        WebhookEvent entity = validUpdateEntity();
        when(repositoryPort.update(any())).thenReturn(Mono.error(new RuntimeException("update error")));
        StepVerifier.create(service.updateDomainEntity(entity))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("Error updating ProviderWebhookEvent: update error", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void validateIdempotency_blankKey_shouldReturnFailure() {
        StepVerifier.create(service.validateIdempotency("  "))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("idempotencyKey is required and cannot be empty", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void validateIdempotency_noDuplicate_shouldReturnSuccess() {
        String key = UUID.randomUUID().toString();
        when(repositoryPort.findByIdempotencyKey(key)).thenReturn(Mono.empty());
        StepVerifier.create(service.validateIdempotency(key))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void validateIdempotency_repoError_shouldReturnFailure() {
        String key = UUID.randomUUID().toString();
        when(repositoryPort.findByIdempotencyKey(key))
            .thenReturn(Mono.error(new RuntimeException("idempotency error")));
        StepVerifier.create(service.validateIdempotency(key))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("Error validating idempotency: idempotency error", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void claimBatch_blankWorkerId_shouldReturnError() {
        StepVerifier.create(service.claimBatch(" ", 10))
            .expectErrorMatches(error -> error instanceof IllegalArgumentException
                && "workerId is required and cannot be empty".equals(error.getMessage()))
            .verify();
    }

    @Test
    void markAsProcessed_invalidId_shouldReturnError() {
        StepVerifier.create(service.markAsProcessed(0L))
            .expectErrorMatches(error -> error instanceof IllegalArgumentException
                && "id is required and must be greater than zero".equals(error.getMessage()))
            .verify();
    }

    @Test
    void markForRetry_validInput_shouldReturnUpdatedEvent() {
        WebhookEvent updated = validCreateEntity();
        updated.setId(99L);
        when(webhookWorkerRepositoryPort.markForRetry(99L, "ERR_CODE", "boom", 3))
            .thenReturn(Mono.just(updated));

        StepVerifier.create(service.markForRetry(99L, "ERR_CODE", "boom", 3))
            .assertNext(result -> assertEquals(99L, result.getId()))
            .verifyComplete();
    }
}