package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.port.output.IAdapterTransactionRepositoryPort;
import com.tumipay.microservice.domain.port.output.IProviderTransactionRepositoryPort;
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
 * ProviderTransactionDomainServiceTest2
 * <p>
 * ProviderTransactionDomainServiceTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderTransactionDomainService Unit Tests")
class ProviderTransactionDomainServiceTest {
    @Mock
    private IProviderTransactionRepositoryPort repositoryPort;

    @Mock
    private IAdapterTransactionRepositoryPort adapterTransactionRepositoryPort;

    private ProviderTransactionDomainService service;

    @BeforeEach
    void setUp() {
        service = new ProviderTransactionDomainService(repositoryPort, adapterTransactionRepositoryPort);
    }

    private ProviderTransaction validCreateEntity() {
        return ProviderTransaction.builder()
            .adapterProviderCode("TP_PROVIDER")
            .transactionType(TransactionTypeEnum.PAYIN_TRANSACTION)
            .transactionId(UUID.randomUUID().toString())
            .referenceId("ref-001")
            .idempotencyKey(UUID.randomUUID().toString())
            .currency("COP")
            .paymentMethod(PaymentMethodEnum.CARD)
            .status(TransactionStatusEnum.PENDING)
            .amount(15000L)
            .createdAt(Instant.now())
            .build();
    }

    private ProviderTransaction validUpdateEntity() {
        return ProviderTransaction.builder()
            .transactionId(UUID.randomUUID().toString())
            .status(TransactionStatusEnum.APPROVED)
            .providerTransactionId("PROV-001")
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
        String uuid = UUID.randomUUID().toString();
        ProviderTransaction entity = validCreateEntity();
        entity.setUuid(uuid);
        when(repositoryPort.findByUuid(uuid)).thenReturn(Mono.just(entity));
        StepVerifier.create(service.getDomainEntityByUuId(uuid))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void getDomainEntityByUuId_validUuidNotFound_shouldReturnFailure() {
        String uuid = UUID.randomUUID().toString();
        when(repositoryPort.findByUuid(uuid)).thenReturn(Mono.empty());
        StepVerifier.create(service.getDomainEntityByUuId(uuid))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("ProviderTransaction not found for uuid=" + uuid, r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void getDomainEntityByUuId_repoError_shouldReturnFailure() {
        String uuid = UUID.randomUUID().toString();
        when(repositoryPort.findByUuid(uuid)).thenReturn(Mono.error(new RuntimeException("db error")));
        StepVerifier.create(service.getDomainEntityByUuId(uuid))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("Error getting ProviderTransaction: db error", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void saveDomainEntity_nullEntity_shouldReturnFailure() {
        StepVerifier.create(service.saveDomainEntity(null))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void saveDomainEntity_missingRequiredFields_shouldReturnFailure() {
        ProviderTransaction invalid = ProviderTransaction.builder()
            .adapterProviderCode("TP_PROVIDER")
            .build(); // missing most required fields
        StepVerifier.create(service.saveDomainEntity(invalid))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void saveDomainEntity_validEntity_shouldReturnSuccess() {
        ProviderTransaction entity = validCreateEntity();
        when(repositoryPort.save(any())).thenReturn(Mono.just(entity));
        StepVerifier.create(service.saveDomainEntity(entity))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void saveDomainEntity_repoError_shouldReturnFailure() {
        ProviderTransaction entity = validCreateEntity();
        when(repositoryPort.save(any())).thenReturn(Mono.error(new RuntimeException("save error")));
        StepVerifier.create(service.saveDomainEntity(entity))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("Error saving ProviderTransaction: save error", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void saveDomainEntity_invalidAmount_shouldReturnFailure() {
        ProviderTransaction entity = validCreateEntity();
        entity.setAmount(0L);
        StepVerifier.create(service.saveDomainEntity(entity))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void updateDomainEntity_nullEntity_shouldReturnFailure() {
        StepVerifier.create(service.updateDomainEntity(null))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void updateDomainEntity_missingRequiredUpdateFields_shouldReturnFailure() {
        ProviderTransaction invalid = ProviderTransaction.builder()
            .transactionId(UUID.randomUUID().toString())
            .build(); // missing status and providerTransactionId
        StepVerifier.create(service.updateDomainEntity(invalid))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void updateDomainEntity_validEntity_shouldReturnSuccess() {
        ProviderTransaction entity = validUpdateEntity();
        when(repositoryPort.update(any())).thenReturn(Mono.just(entity));
        StepVerifier.create(service.updateDomainEntity(entity))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void updateDomainEntity_repoError_shouldReturnFailure() {
        ProviderTransaction entity = validUpdateEntity();
        when(repositoryPort.update(any())).thenReturn(Mono.error(new RuntimeException("update error")));
        StepVerifier.create(service.updateDomainEntity(entity))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("Error updating ProviderTransaction: update error", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void validateIdempotency_blankKey_shouldReturnFailure() {
        StepVerifier.create(service.validateIdempotency(""))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("idempotencyKey is required and cannot be empty", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void validateIdempotency_nonUuidFormatWithoutDuplicate_shouldReturnSuccess() {
        String idempotencyKey = "not-a-uuid";
        when(repositoryPort.findByIdempotencyKey(idempotencyKey)).thenReturn(Mono.empty());

        StepVerifier.create(service.validateIdempotency(idempotencyKey))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus());
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
}