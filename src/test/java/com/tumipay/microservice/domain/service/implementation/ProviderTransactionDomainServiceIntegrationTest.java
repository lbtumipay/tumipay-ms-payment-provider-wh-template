package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderTransactionEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderTransactionRepository;
import com.tumipay.microservice.shared.enums.BaseOperationStatusEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * ProviderTransactionDomainServiceIntegrationTest2
 * <p>
 * ProviderTransactionDomainServiceIntegrationTest2 class.
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
class ProviderTransactionDomainServiceIntegrationTest {

    @Autowired
    private ProviderTransactionDomainService providerTransactionDomainService;

    @Autowired
    private IProviderTransactionRepository providerTransactionRepository;

    @BeforeEach
    void cleanDatabase() {
        providerTransactionRepository.deleteAll().block();
    }

    @Test
    void shouldPersistTransactionInH2WhenSavingDomainEntity() {
        String transactionId = UUID.randomUUID().toString();
        String uuid = UUID.randomUUID().toString();
        String idempotencyKey = UUID.randomUUID().toString();

        ProviderTransaction domainEntity = ProviderTransaction.builder()
            .uuid(uuid)
            .transactionId(transactionId)
            .referenceId("ref-" + transactionId.substring(0, 8))
            .transactionType(TransactionTypeEnum.PAYIN_TRANSACTION)
            .paymentMethod(PaymentMethodEnum.CARD)
            .adapterProviderCode("TP_PROVIDER")
            .idempotencyKey(idempotencyKey)
            .status(TransactionStatusEnum.PENDING)
            .amount(15000L)
            .currency("COP")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        StepVerifier.create(providerTransactionDomainService.saveDomainEntity(domainEntity))
            .assertNext(result -> Assertions.assertEquals(BaseOperationStatusEnum.SUCCESS, result.getStatus()))
            .verifyComplete();

        ProviderTransactionEntity persisted = providerTransactionRepository.findByIdempotencyKey(idempotencyKey).block();
        Assertions.assertNotNull(persisted);
        Assertions.assertEquals(transactionId, persisted.getTransactionId());
        Assertions.assertEquals("TP_PROVIDER", persisted.getAdapterProviderCode());
    }

    @Test
    void shouldFailIdempotencyValidationWhenDuplicateExistsInH2() {
        String idempotencyKey = UUID.randomUUID().toString();

        providerTransactionRepository.save(ProviderTransactionEntity.builder()
            .uuid(UUID.randomUUID().toString())
            .transactionId(UUID.randomUUID().toString())
            .referenceId("ref-dup")
            .transactionType("PAYIN_TRANSACTION")
            .paymentMethod("CARD")
            .adapterProviderCode("TP_PROVIDER")
            .idempotencyKey(idempotencyKey)
            .status("PENDING")
            .amount(5000L)
            .currency("COP")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build()).block();

        StepVerifier.create(providerTransactionDomainService.validateIdempotency(idempotencyKey))
            .assertNext(result -> {
                Assertions.assertEquals(BaseOperationStatusEnum.FAILED, result.getStatus());
                Assertions.assertTrue(result.getErrorMessage().contains("Duplicate transaction detected"));
            })
            .verifyComplete();
    }

    @Test
    void shouldFindTransactionByUuidFromH2() {
        String uuid = UUID.randomUUID().toString();

        providerTransactionRepository.save(ProviderTransactionEntity.builder()
            .uuid(uuid)
            .transactionId(UUID.randomUUID().toString())
            .referenceId("ref-find")
            .transactionType("PAYOUT_TRANSACTION")
            .paymentMethod("BANK_TRANSFER")
            .adapterProviderCode("TP_PROVIDER")
            .idempotencyKey(UUID.randomUUID().toString())
            .status("PENDING")
            .amount(9000L)
            .currency("COP")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build()).block();

        StepVerifier.create(providerTransactionDomainService.getDomainEntityByUuId(uuid))
            .assertNext(result -> {
                Assertions.assertEquals(BaseOperationStatusEnum.SUCCESS, result.getStatus());
                Assertions.assertEquals(uuid, result.getEntity().getUuid());
            })
            .verifyComplete();
    }
}
