package com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper;

import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderTransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentAdapterTransactionMapper.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("PaymentAdapterTransactionMapper Unit Tests")
class PaymentAdapterTransactionMapperTest {

    private PaymentAdapterTransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentAdapterTransactionMapperImpl();
    }

    // ── toEntity ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toEntity should map all fields from ProviderTransaction domain")
    void toEntityShouldMapAllFieldsFromDomain() {
        Instant now = Instant.now();

        ProviderTransaction domain = ProviderTransaction.builder()
            .id(1L)
            .uuid("tx-uuid-001")
            .transactionId("TX-001")
            .referenceId("REF-001")
            .adapterProviderCode("PROV_001")
            .providerTransactionId("PROV-TX-001")
            .providerReferenceId("PROV-REF-001")
            .idempotencyKey("IDEM-001")
            .amount(10000)
            .currency("COP")
            .transactionType(TransactionTypeEnum.PAYIN_TRANSACTION)
            .status(TransactionStatusEnum.APPROVED)
            .paymentMethod(PaymentMethodEnum.CARD)
            .errorCode(null)
            .errorMessage(null)
            .providerProcessedAt(now)
            .metadata("{\"bank\":\"BANCOLOMBIA\"}")
            .createdAt(now)
            .updatedAt(now)
            .build();

        ProviderTransactionEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(1L,                                          entity.getId());
        assertEquals("tx-uuid-001",                               entity.getUuid());
        assertEquals("TX-001",                                    entity.getTransactionId());
        assertEquals("REF-001",                                   entity.getReferenceId());
        assertEquals("PROV_001",                                  entity.getAdapterProviderCode());
        assertEquals("PROV-TX-001",                               entity.getProviderTransactionId());
        assertEquals("PROV-REF-001",                              entity.getProviderReferenceId());
        assertEquals("IDEM-001",                                  entity.getIdempotencyKey());
        assertEquals(BigDecimal.valueOf(10000),                    entity.getAmount());
        assertEquals("COP",                                       entity.getCurrency());
        assertEquals(TransactionTypeEnum.PAYIN_TRANSACTION.name(), entity.getTransactionType());
        assertEquals(TransactionStatusEnum.APPROVED.name(),        entity.getStatus());
        assertEquals(PaymentMethodEnum.CARD.name(),                entity.getPaymentMethod());
        assertNull(entity.getErrorCode());
        assertEquals("{\"bank\":\"BANCOLOMBIA\"}",                 entity.getMetadata());
        assertEquals(now,                                         entity.getProviderProcessedAt());
        assertEquals(now,                                         entity.getCreatedAt());
        assertEquals(now,                                         entity.getUpdatedAt());
    }

    @Test
    @DisplayName("toEntity should map error fields when transaction fails")
    void toEntityShouldMapErrorFields() {
        ProviderTransaction domain = ProviderTransaction.builder()
            .uuid("tx-uuid-002")
            .transactionId("TX-002")
            .status(TransactionStatusEnum.ERROR)
            .errorCode("CARD_DECLINED")
            .errorMessage("insufficient funds")
            .build();

        ProviderTransactionEntity entity = mapper.toEntity(domain);

        assertEquals(TransactionStatusEnum.ERROR.name(), entity.getStatus());
        assertEquals("CARD_DECLINED",                    entity.getErrorCode());
        assertEquals("insufficient funds",               entity.getErrorMessage());
    }

    @Test
    @DisplayName("toEntity should handle null fields without throwing")
    void toEntityShouldHandleNullFieldsWithoutThrowing() {
        ProviderTransaction domain = ProviderTransaction.builder().build();

        ProviderTransactionEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertNull(entity.getTransactionId());
        assertNull(entity.getCurrency());
    }

    // ── toDomain ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toDomain should map all fields from ProviderTransactionEntity")
    void toDomainShouldMapAllFieldsFromEntity() {
        Instant now = Instant.now();

        ProviderTransactionEntity entity = ProviderTransactionEntity.builder()
            .id(2L)
            .uuid("tx-uuid-003")
            .transactionId("TX-003")
            .referenceId("REF-003")
            .adapterProviderCode("PROV_002")
            .providerTransactionId("PROV-TX-003")
            .providerReferenceId("PROV-REF-003")
            .idempotencyKey("IDEM-003")
            .amount(new BigDecimal("50000.00"))
            .currency("USD")
            .transactionType(TransactionTypeEnum.PAYOUT_TRANSACTION.name())
            .status(TransactionStatusEnum.PENDING.name())
            .paymentMethod(PaymentMethodEnum.BANK_TRANSFER.name())
            .errorCode(null)
            .errorMessage(null)
            .providerProcessedAt(now)
            .metadata("{\"channel\":\"web\"}")
            .createdAt(now)
            .updatedAt(now)
            .build();

        ProviderTransaction domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(2L,                                             domain.getId());
        assertEquals("tx-uuid-003",                                  domain.getUuid());
        assertEquals("TX-003",                                       domain.getTransactionId());
        assertEquals("PROV_002",                                     domain.getAdapterProviderCode());
        assertEquals(TransactionTypeEnum.PAYOUT_TRANSACTION.name(),  entity.getTransactionType());
        assertEquals("USD",                                          domain.getCurrency());
        assertEquals("{\"channel\":\"web\"}",                        domain.getMetadata());
        assertEquals(now,                                            domain.getCreatedAt());
    }

    @Test
    @DisplayName("toDomain should handle null fields without throwing")
    void toDomainShouldHandleNullFieldsWithoutThrowing() {
        ProviderTransactionEntity entity = ProviderTransactionEntity.builder().build();

        ProviderTransaction domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertNull(domain.getTransactionId());
        assertNull(domain.getCurrency());
    }

    @Test
    @DisplayName("toDomain round-trip should preserve key fields")
    void toDomainRoundTripShouldPreserveKeyFields() {
        ProviderTransaction original = ProviderTransaction.builder()
            .uuid("rt-uuid-001")
            .transactionId("RT-TX-001")
            .adapterProviderCode("RT_PROV")
            .currency("EUR")
            .metadata("{\"key\":\"value\"}")
            .build();

        ProviderTransaction roundTrip = mapper.toDomain(mapper.toEntity(original));

        assertEquals(original.getUuid(),               roundTrip.getUuid());
        assertEquals(original.getTransactionId(),      roundTrip.getTransactionId());
        assertEquals(original.getAdapterProviderCode(), roundTrip.getAdapterProviderCode());
        assertEquals(original.getCurrency(),           roundTrip.getCurrency());
        assertEquals(original.getMetadata(),           roundTrip.getMetadata());
    }
}

