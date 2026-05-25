package com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper;

import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import com.tumipay.microservice.domain.model.provider.ProviderIntegrationLog;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderIntegrationLogEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentAdapterIntegrationLogMapper.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("PaymentAdapterIntegrationLogMapper Unit Tests")
class PaymentAdapterIntegrationLogMapperTest {

    private PaymentAdapterIntegrationLogMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentAdapterIntegrationLogMapperImpl();
    }

    // ── toEntity ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toEntity should map all fields from ProviderIntegrationLog domain")
    void toEntityShouldMapAllFields() {

        Instant now = Instant.now();

        ProviderIntegrationLog domain = ProviderIntegrationLog.builder()
            .id(10L)
            .uuid("log-uuid-001")
            .transactionId("TX-001")
            .referenceId("REF-001")
            .providerTransactionId("PROV-TX-001")
            .providerReferenceId("PROV-REF-001")
            .idempotencyKey("IDEM-001")
            .adapterProviderCode("PROV_001")
            .transactionType(TransactionTypeEnum.PAYIN_TRANSACTION)
            .paymentMethod(PaymentMethodEnum.CARD)
            .providerEndpoint("/v1/payins")
            .httpMethod("POST")
            .requestPayload("{\"amount\":100}")
            .responsePayload("{\"status\":\"approved\"}")
            .httpStatusCode(200)
            .providerLatencyMs(350)
            .success(true)
            .errorCode(null)
            .errorMessage(null)
            .createdAt(now)
            .updatedAt(now)
            .build();

        ProviderIntegrationLogEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(10L,                                    entity.getId());
        assertEquals("log-uuid-001",                         entity.getUuid());
        assertEquals("TX-001",                               entity.getTransactionId());
        assertEquals("REF-001",                              entity.getReferenceId());
        assertEquals("PROV-TX-001",                          entity.getProviderTransactionId());
        assertEquals("PROV-REF-001",                         entity.getProviderReferenceId());
        assertEquals("IDEM-001",                             entity.getIdempotencyKey());
        assertEquals("PROV_001",                             entity.getAdapterProviderCode());
        assertEquals(TransactionTypeEnum.PAYIN_TRANSACTION.name(), entity.getTransactionType());
        assertEquals(PaymentMethodEnum.CARD.name(),          entity.getPaymentMethod());
        assertEquals("/v1/payins",                           entity.getProviderEndpoint());
        assertEquals("POST",                                 entity.getHttpMethod());
        assertEquals("{\"amount\":100}",                     entity.getRequestPayload());
        assertEquals("{\"status\":\"approved\"}",            entity.getResponsePayload());
        assertEquals(200,                                    entity.getHttpStatusCode());
        assertEquals(350,                                    entity.getProviderLatencyMs());
        assertTrue(entity.getSuccess());
        assertNull(entity.getErrorCode());
        assertEquals(now,                                    entity.getCreatedAt());
        assertEquals(now,                                    entity.getUpdatedAt());
    }

    @Test
    @DisplayName("toEntity should map null fields without throwing")
    void toEntityShouldMapNullFieldsWithoutThrowing() {
        ProviderIntegrationLog domain = ProviderIntegrationLog.builder().build();

        ProviderIntegrationLogEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertNull(entity.getTransactionId());
        assertNull(entity.getRequestPayload());
    }

    // ── toDomain ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toDomain should map all fields from ProviderIntegrationLogEntity")
    void toDomainShouldMapAllFields() {
        Instant now = Instant.now();

        ProviderIntegrationLogEntity entity = ProviderIntegrationLogEntity.builder()
            .id(20L)
            .uuid("log-uuid-002")
            .transactionId("TX-002")
            .referenceId("REF-002")
            .providerTransactionId("PROV-TX-002")
            .providerReferenceId("PROV-REF-002")
            .idempotencyKey("IDEM-002")
            .adapterProviderCode("PROV_002")
            .transactionType(TransactionTypeEnum.PAYOUT_TRANSACTION.name())
            .paymentMethod(PaymentMethodEnum.PSE.name())
            .providerEndpoint("/v1/payouts")
            .httpMethod("POST")
            .requestPayload("{\"amount\":500}")
            .responsePayload("{\"status\":\"pending\"}")
            .httpStatusCode(202)
            .providerLatencyMs(120)
            .success(false)
            .errorCode("INSUFFICIENT_FUNDS")
            .errorMessage("not enough balance")
            .createdAt(now)
            .updatedAt(now)
            .build();

        ProviderIntegrationLog domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(20L,                                         domain.getId());
        assertEquals("log-uuid-002",                              domain.getUuid());
        assertEquals("TX-002",                                    domain.getTransactionId());
        assertEquals("PROV_002",                                  domain.getAdapterProviderCode());
        assertEquals(TransactionTypeEnum.PAYOUT_TRANSACTION.name(), entity.getTransactionType());
        assertEquals("/v1/payouts",                               domain.getProviderEndpoint());
        assertEquals(202,                                         domain.getHttpStatusCode());
        assertEquals("INSUFFICIENT_FUNDS",                        domain.getErrorCode());
        assertEquals("not enough balance",                        domain.getErrorMessage());
        assertEquals(now,                                         domain.getCreatedAt());
    }

    @Test
    @DisplayName("toDomain should return null fields when entity fields are null")
    void toDomainShouldHandleNullFields() {
        ProviderIntegrationLogEntity entity = ProviderIntegrationLogEntity.builder().build();

        ProviderIntegrationLog domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertNull(domain.getTransactionId());
        assertNull(domain.getErrorCode());
    }

    // ── doToDomain (reactive) ──────────────────────────────────────────────────

    @Test
    @DisplayName("doToDomain should wrap toDomain result in a Mono")
    void doToDomainShouldWrapResultInMono() {
        ProviderIntegrationLogEntity entity = ProviderIntegrationLogEntity.builder()
            .uuid("log-uuid-003")
            .transactionId("TX-003")
            .adapterProviderCode("PROV_003")
            .build();

        StepVerifier.create(mapper.doToDomain(entity))
            .assertNext(domain -> {
                assertNotNull(domain);
                assertEquals("log-uuid-003", domain.getUuid());
                assertEquals("TX-003",       domain.getTransactionId());
                assertEquals("PROV_003",     domain.getAdapterProviderCode());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("doToDomain should emit exactly one element")
    void doToDomainShouldEmitExactlyOneElement() {
        ProviderIntegrationLogEntity entity = ProviderIntegrationLogEntity.builder()
            .uuid("log-uuid-004")
            .build();

        StepVerifier.create(mapper.doToDomain(entity))
            .expectNextCount(1)
            .verifyComplete();
    }
}

