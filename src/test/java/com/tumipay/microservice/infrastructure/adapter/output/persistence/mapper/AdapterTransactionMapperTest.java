package com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper;

import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import com.tumipay.microservice.domain.model.transaction.StandardTransactionResult;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderIntegrationLogEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderTransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AdapterTransactionMapper.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("AdapterTransactionMapper Unit Tests")
class AdapterTransactionMapperTest {

    private AdapterTransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AdapterTransactionMapperImpl();
    }

    // ── toDomain ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toDomain should map fields from both entities into StandardTransactionResult")
    void toDomainShouldMapFieldsFromBothEntities() {
        Instant now = Instant.now();

        ProviderTransactionEntity txEntity = ProviderTransactionEntity.builder()
            .uuid("tx-uuid-001")
            .transactionId("TX-001")
            .referenceId("REF-001")
            .idempotencyKey("IDEM-001")
            .providerTransactionId("PROV-TX-001")
            .providerReferenceId("PROV-REF-001")
            .adapterProviderCode("PROV_001")
            .transactionType(TransactionTypeEnum.PAYIN_TRANSACTION.name())
            .paymentMethod(PaymentMethodEnum.CARD.name())
            .errorCode("ERR-001")
            .errorMessage("card declined")
            .metadata("{\"bank\":\"BANCOLOMBIA\"}")
            .createdAt(now)
            .updatedAt(now)
            .providerProcessedAt(now)
            .build();

        ProviderIntegrationLogEntity logEntity = ProviderIntegrationLogEntity.builder()
            .providerEndpoint("/v1/payins")
            .httpMethod("POST")
            .requestPayload("{\"amount\":100}")
            .responsePayload("{\"status\":\"failed\"}")
            .httpStatusCode(400)
            .success(false)
            .build();

        StandardTransactionResult result = mapper.toDomain(logEntity, txEntity);

        assertNotNull(result);

        // Fields from ProviderTransactionEntity
        assertEquals("tx-uuid-001",  result.getAdapterTransactionId());
        assertEquals("TX-001",       result.getTransactionId());
        assertEquals("REF-001",      result.getReferenceId());
        assertEquals("IDEM-001",     result.getIdempotencyKey());
        assertEquals("PROV-TX-001",  result.getProviderTransactionId());
        assertEquals("PROV-REF-001", result.getProviderReferenceId());
        assertEquals("PROV_001",     result.getAdapterProviderCode());
        assertEquals(TransactionTypeEnum.PAYIN_TRANSACTION.name(), result.getTransactionType());
        assertEquals(PaymentMethodEnum.CARD.name(),                result.getPaymentMethod());
        assertEquals("ERR-001",      result.getErrorCode());
        assertEquals("card declined", result.getErrorMessage());
        assertEquals(now,            result.getCreatedAt());
        assertEquals(now,            result.getUpdatedAt());
        assertEquals(now,            result.getProcessedAt());

        // Fields from ProviderIntegrationLogEntity
        assertEquals("/v1/payins",   result.getProviderEndpoint());
        assertEquals("POST",         result.getHttpMethod());
        assertEquals(400,            result.getHttpStatusCode());
    }

    @Test
    @DisplayName("toDomain should deserialize requestPayload and responsePayload as Maps")
    void toDomainShouldDeserializePayloadsAsMaps() {
        ProviderTransactionEntity txEntity = ProviderTransactionEntity.builder()
            .uuid("tx-uuid-002")
            .build();

        ProviderIntegrationLogEntity logEntity = ProviderIntegrationLogEntity.builder()
            .requestPayload("{\"amount\":200,\"currency\":\"USD\"}")
            .responsePayload("{\"transactionId\":\"PROV-ABC\"}")
            .build();

        StandardTransactionResult result = mapper.toDomain(logEntity, txEntity);

        assertNotNull(result.getProviderRequest());
        assertNotNull(result.getProviderResponse());
        assertEquals(200.0, ((Number) result.getProviderRequest().get("amount")).doubleValue());
        assertEquals("USD", result.getProviderRequest().get("currency"));
        assertEquals("PROV-ABC", result.getProviderResponse().get("transactionId"));
    }

    @Test
    @DisplayName("toDomain should deserialize metadata JSON as Map")
    void toDomainShouldDeserializeMetadataAsMap() {
        ProviderTransactionEntity txEntity = ProviderTransactionEntity.builder()
            .uuid("tx-uuid-003")
            .metadata("{\"channel\":\"mobile\",\"version\":2}")
            .build();

        ProviderIntegrationLogEntity logEntity = ProviderIntegrationLogEntity.builder().build();

        StandardTransactionResult result = mapper.toDomain(logEntity, txEntity);

        assertNotNull(result.getMetadata());
        assertEquals("mobile", result.getMetadata().get("channel"));
        assertEquals(2.0, ((Number) result.getMetadata().get("version")).doubleValue());
    }

    @Test
    @DisplayName("toDomain should return null Maps when payloads are null")
    void toDomainShouldReturnNullMapsWhenPayloadsAreNull() {
        ProviderTransactionEntity txEntity = ProviderTransactionEntity.builder()
            .uuid("tx-uuid-004")
            .build();

        ProviderIntegrationLogEntity logEntity = ProviderIntegrationLogEntity.builder()
            .requestPayload(null)
            .responsePayload(null)
            .build();

        StandardTransactionResult result = mapper.toDomain(logEntity, txEntity);

        assertNull(result.getProviderRequest());
        assertNull(result.getProviderResponse());
    }

    @Test
    @DisplayName("toDomain should handle null transaction entity fields without throwing")
    void toDomainShouldHandleNullTransactionEntityFields() {
        ProviderTransactionEntity txEntity = ProviderTransactionEntity.builder().build();
        ProviderIntegrationLogEntity logEntity = ProviderIntegrationLogEntity.builder().build();

        StandardTransactionResult result = mapper.toDomain(logEntity, txEntity);

        assertNotNull(result);
        assertNull(result.getTransactionId());
        assertNull(result.getErrorCode());
    }

    @Test
    @DisplayName("toDomain success flag should be true when integration log marks success")
    void toDomainSuccessFlagShouldReflectIntegrationLog() {
        ProviderTransactionEntity txEntity = ProviderTransactionEntity.builder()
            .uuid("tx-uuid-005")
            .build();

        ProviderIntegrationLogEntity logEntity = ProviderIntegrationLogEntity.builder()
            .success(true)
            .httpStatusCode(200)
            .build();

        StandardTransactionResult result = mapper.toDomain(logEntity, txEntity);

        assertTrue(result.getSuccess());
        assertEquals(200, result.getHttpStatusCode());
    }
}

