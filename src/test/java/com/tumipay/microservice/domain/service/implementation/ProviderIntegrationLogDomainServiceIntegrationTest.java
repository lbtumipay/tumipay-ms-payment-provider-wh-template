package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.OperationStatusEnum;
import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import com.tumipay.microservice.domain.model.provider.ProviderIntegrationLog;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderIntegrationLogEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderIntegrationLogRepository;
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
 * ProviderIntegrationLogDomainServiceIntegrationTest
 * <p>
 * ProviderIntegrationLogDomainServiceIntegrationTest class.
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
class ProviderIntegrationLogDomainServiceIntegrationTest {

    @Autowired
    private ProviderIntegrationLogDomainService providerIntegrationLogDomainService;

    @Autowired
    private IProviderIntegrationLogRepository providerIntegrationLogRepository;

    @BeforeEach
    void cleanDatabase() {
        providerIntegrationLogRepository.deleteAll().block();
    }

    @Test
    void shouldPersistIntegrationLogInH2WhenSavingDomainEntity() {
        String transactionId = UUID.randomUUID().toString();
        String idempotencyKey = UUID.randomUUID().toString();

        ProviderIntegrationLog log = ProviderIntegrationLog.builder()
            .uuid(UUID.randomUUID().toString())
            .transactionId(transactionId)
            .referenceId("ref-" + transactionId.substring(0, 8))
            .providerTransactionId("PROV-001")
            .providerReferenceId("PROV-REF-001")
            .idempotencyKey(idempotencyKey)
            .adapterProviderCode("TP_PROVIDER")
            .transactionType(TransactionTypeEnum.PAYIN_TRANSACTION)
            .paymentMethod(PaymentMethodEnum.CARD)
            .providerEndpoint("/v1/payins")
            .httpMethod("POST")
            .requestPayload("{\"request\":\"payload\"}")
            .responsePayload("{\"status\":\"APPROVED\"}")
            .httpStatusCode(201)
            .providerLatencyMs(35)
            .success(Boolean.TRUE)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        StepVerifier.create(providerIntegrationLogDomainService.saveDomainEntity(log))
            .assertNext(result -> Assertions.assertEquals(OperationStatusEnum.SUCCESS, result.getStatus()))
            .verifyComplete();

        ProviderIntegrationLogEntity persisted = providerIntegrationLogRepository.findByIdempotencyKey(idempotencyKey).block();
        Assertions.assertNotNull(persisted);
        Assertions.assertEquals("POST", persisted.getHttpMethod());
        Assertions.assertEquals("/v1/payins", persisted.getProviderEndpoint());
    }

    @Test
    void shouldFindIntegrationLogByUuidFromH2() {
        String uuid = UUID.randomUUID().toString();

        providerIntegrationLogRepository.save(ProviderIntegrationLogEntity.builder()
            .uuid(uuid)
            .transactionId(UUID.randomUUID().toString())
            .referenceId("ref-find")
            .providerTransactionId("PROV-123")
            .providerReferenceId("PROV-REF-123")
            .transactionType("PAYIN_TRANSACTION")
            .paymentMethod("CARD")
            .idempotencyKey(UUID.randomUUID().toString())
            .adapterProviderCode("TP_PROVIDER")
            .httpMethod("POST")
            .providerEndpoint("/v1/payins")
            .requestPayload("{}")
            .responsePayload("{}")
            .httpStatusCode(200)
            .providerLatencyMs(10)
            .success(Boolean.TRUE)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build()).block();

        StepVerifier.create(providerIntegrationLogDomainService.getDomainEntityByUuId(uuid))
            .assertNext(result -> {
                Assertions.assertEquals(OperationStatusEnum.SUCCESS, result.getStatus());
                Assertions.assertEquals(uuid, result.getEntity().getUuid());
            })
            .verifyComplete();
    }
}
