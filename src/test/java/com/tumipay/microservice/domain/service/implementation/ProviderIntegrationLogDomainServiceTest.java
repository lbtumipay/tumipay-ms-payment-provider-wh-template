package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import com.tumipay.microservice.domain.model.provider.ProviderIntegrationLog;
import com.tumipay.microservice.domain.port.output.IProviderIntegrationLogRepositoryPort;
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
 * ProviderIntegrationLogDomainServiceTest2
 * <p>
 * ProviderIntegrationLogDomainServiceTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderIntegrationLogDomainService Unit Tests")
class ProviderIntegrationLogDomainServiceTest {

    @Mock
    private IProviderIntegrationLogRepositoryPort repositoryPort;
    private ProviderIntegrationLogDomainService service;

    @BeforeEach
    void setUp() {
        service = new ProviderIntegrationLogDomainService(repositoryPort);
    }

    private ProviderIntegrationLog validCreateEntity() {
        return ProviderIntegrationLog.builder()
            .adapterProviderCode("TP_PROVIDER")
            .transactionType(TransactionTypeEnum.PAYIN_TRANSACTION)
            .transactionId(UUID.randomUUID().toString())
            .referenceId("ref-001")
            .idempotencyKey(UUID.randomUUID().toString())
            .createdAt(Instant.now())
            .build();
    }

    private ProviderIntegrationLog validUpdateEntity() {
        return ProviderIntegrationLog.builder()
            .requestPayload("{\"k\":\"v\"}")
            .responsePayload("{\"status\":\"OK\"}")
            .httpMethod("POST")
            .providerEndpoint("/v1/payins")
            .build();
    }

    @Test
    void getDomainEntityByUuId_blankUuid_shouldReturnFailure() {
        StepVerifier.create(service.getDomainEntityByUuId("  "))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("entityUuId is required and cannot be empty", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void getDomainEntityByUuId_invalidUuidFormat_shouldReturnFailure() {
        StepVerifier.create(service.getDomainEntityByUuId("not-valid"))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("entityUuId format is invalid", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void getDomainEntityByUuId_validUuidFound_shouldReturnSuccess() {
        String uuid = UUID.randomUUID().toString();
        ProviderIntegrationLog entity = validCreateEntity();
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
                assertEquals("ProviderIntegrationLog not found for uuid=" + uuid, r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void getDomainEntityByUuId_repoError_shouldReturnFailure() {
        String uuid = UUID.randomUUID().toString();
        when(repositoryPort.findByUuid(uuid)).thenReturn(Mono.error(new RuntimeException("db error")));
        StepVerifier.create(service.getDomainEntityByUuId(uuid))
            .assertNext(r -> {
                assertEquals(BaseOperationStatusEnum.FAILED, r.getStatus());
                assertEquals("Error getting ProviderIntegrationLog: db error", r.getErrorMessage());
            }).verifyComplete();
    }

    @Test
    void saveDomainEntity_nullEntity_shouldReturnFailure() {
        StepVerifier.create(service.saveDomainEntity(null))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    void saveDomainEntity_missingAdapterProviderCode_shouldReturnFailure() {
        ProviderIntegrationLog invalid = ProviderIntegrationLog.builder()
            .transactionType(TransactionTypeEnum.PAYIN_TRANSACTION)
            .transactionId(UUID.randomUUID().toString())
            .referenceId("ref-001")
            .idempotencyKey(UUID.randomUUID().toString())
            .build(); // missing adapterProviderCode
        StepVerifier.create(service.saveDomainEntity(invalid))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    void saveDomainEntity_validEntity_shouldReturnSuccess() {
        ProviderIntegrationLog entity = validCreateEntity();
        when(repositoryPort.save(any())).thenReturn(Mono.just(entity));
        StepVerifier.create(service.saveDomainEntity(entity))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus()))
            .verifyComplete();
    }

    @Test
    void updateDomainEntity_nullEntity_shouldReturnFailure() {
        StepVerifier.create(service.updateDomainEntity(null))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    void updateDomainEntity_missingRequestPayload_shouldReturnFailure() {
        ProviderIntegrationLog invalid = ProviderIntegrationLog.builder()
            .responsePayload("{}")
            .httpMethod("POST")
            .providerEndpoint("/v1/payins")
            .build(); // missing requestPayload
        StepVerifier.create(service.updateDomainEntity(invalid))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    void updateDomainEntity_missingHttpMethod_shouldReturnFailure() {
        ProviderIntegrationLog invalid = ProviderIntegrationLog.builder()
            .requestPayload("{}")
            .responsePayload("{}")
            .providerEndpoint("/v1/payins")
            .build(); // missing httpMethod
        StepVerifier.create(service.updateDomainEntity(invalid))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    void updateDomainEntity_validEntity_shouldReturnSuccess() {
        ProviderIntegrationLog entity = validUpdateEntity();
        when(repositoryPort.update(any())).thenReturn(Mono.just(entity));
        StepVerifier.create(service.updateDomainEntity(entity))
            .assertNext(r -> assertEquals(BaseOperationStatusEnum.SUCCESS, r.getStatus()))
            .verifyComplete();
    }
}