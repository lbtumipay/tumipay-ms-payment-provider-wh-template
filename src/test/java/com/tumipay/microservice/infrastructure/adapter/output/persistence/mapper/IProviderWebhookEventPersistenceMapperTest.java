package com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IProviderWebhookEventPersistenceMapper.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("IProviderWebhookEventPersistenceMapper Unit Tests")
class IProviderWebhookEventPersistenceMapperTest {

    private IProviderWebhookEventPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IProviderWebhookEventPersistenceMapperImpl();
    }

    // ── toEntity ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toEntity should map all fields from WebhookEvent domain to entity")
    void toEntityShouldMapAllFieldsFromDomain() {
        String uuidStr   = "550e8400-e29b-41d4-a716-446655440001";
        Instant now      = Instant.now();

        WebhookEvent domain = WebhookEvent.builder()
            .id(1L)
            .uuid(uuidStr)
            .adapterProviderCode("PROV_001")
            .eventType("PAYMENT_APPROVED")
            .externalEventId("EVT-EXT-001")
            .idempotencyKey("IDEM-001")
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED)
            .errorCode("ERR-001")
            .retryCount(0)
            .lastError(null)
            .eventRequest("{\"amount\":100}")
            .receivedAt(now)
            .createdAt(now)
            .processedAt(now)
            .build();

        ProviderWebhookEventEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(1L,                                    entity.getId());
        assertEquals(UUID.fromString(uuidStr),              entity.getUuid());
        assertEquals("PROV_001",                            entity.getAdapterProviderCode());
        assertEquals("PAYMENT_APPROVED",                    entity.getEventType());
        assertEquals("EVT-EXT-001",                         entity.getExternalEventId());
        assertEquals("IDEM-001",                            entity.getIdempotencyKey());
        assertEquals(WebhookProcessingStatusEnum.RECEIVED.name(), entity.getProcessingStatus());
        assertEquals("ERR-001",                             entity.getErrorCode());
        assertEquals(0,                                     entity.getRetryCount());
        assertEquals("{\"amount\":100}",                    entity.getEventRequest());
        assertEquals(now,                                   entity.getReceivedAt());
        assertEquals(now,                                   entity.getCreatedAt());
    }

    @Test
    @DisplayName("toEntity should convert invalid UUID string to null")
    void toEntityShouldConvertInvalidUuidToNull() {
        WebhookEvent domain = WebhookEvent.builder()
            .uuid("not-a-valid-uuid")
            .adapterProviderCode("PROV")
            .eventType("TYPE")
            .eventRequest("{}")
            .build();

        ProviderWebhookEventEntity entity = mapper.toEntity(domain);

        assertNull(entity.getUuid());
    }

    @Test
    @DisplayName("toEntity should convert null processingStatus to null string")
    void toEntityShouldConvertNullProcessingStatusToNull() {
        WebhookEvent domain = WebhookEvent.builder()
            .uuid(UUID.randomUUID().toString())
            .adapterProviderCode("PROV")
            .eventType("TYPE")
            .eventRequest("{}")
            .processingStatus(null)
            .build();

        ProviderWebhookEventEntity entity = mapper.toEntity(domain);

        assertNull(entity.getProcessingStatus());
    }

    // ── toDomain ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toDomain should map all fields from entity to WebhookEvent domain")
    void toDomainShouldMapAllFieldsFromEntity() {
        UUID uuid   = UUID.randomUUID();
        Instant now = Instant.now();

        ProviderWebhookEventEntity entity = ProviderWebhookEventEntity.builder()
            .id(2L)
            .uuid(uuid)
            .adapterProviderCode("PROV_002")
            .eventType("PAYMENT_DECLINED")
            .externalEventId("EVT-EXT-002")
            .idempotencyKey("IDEM-002")
            .processingStatus(WebhookProcessingStatusEnum.FAILED.name())
            .errorCode("ERR-002")
            .retryCount(3)
            .lastError("timeout")
            .eventRequest("{\"ref\":\"abc\"}")
            .receivedAt(now)
            .createdAt(now)
            .processedAt(now)
            .build();

        WebhookEvent domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(2L,                                 domain.getId());
        assertEquals(uuid.toString(),                    domain.getUuid());
        assertEquals("PROV_002",                         domain.getAdapterProviderCode());
        assertEquals("PAYMENT_DECLINED",                 domain.getEventType());
        assertEquals("EVT-EXT-002",                      domain.getExternalEventId());
        assertEquals("IDEM-002",                         domain.getIdempotencyKey());
        assertEquals(WebhookProcessingStatusEnum.FAILED, domain.getProcessingStatus());
        assertEquals("ERR-002",                          domain.getErrorCode());
        assertEquals(3,                                  domain.getRetryCount());
        assertEquals("timeout",                          domain.getLastError());
        assertEquals("{\"ref\":\"abc\"}",                domain.getEventRequest());
        assertEquals(now,                                domain.getReceivedAt());
    }

    @Test
    @DisplayName("toDomain should return null uuid string when entity uuid is null")
    void toDomainShouldReturnNullUuidStringWhenEntityUuidIsNull() {
        ProviderWebhookEventEntity entity = ProviderWebhookEventEntity.builder()
            .uuid(null)
            .adapterProviderCode("PROV")
            .eventType("TYPE")
            .eventRequest("{}")
            .build();

        WebhookEvent domain = mapper.toDomain(entity);

        assertNull(domain.getUuid());
    }

    // ── toUuid (default) ───────────────────────────────────────────────────────

    @Test
    @DisplayName("toUuid should parse a valid UUID string")
    void toUuidShouldParseValidUuidString() {
        String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
        UUID result = mapper.toUuid(uuidStr);

        assertNotNull(result);
        assertEquals(UUID.fromString(uuidStr), result);
    }

    @Test
    @DisplayName("toUuid should return null for null input")
    void toUuidShouldReturnNullForNullInput() {
        assertNull(mapper.toUuid(null));
    }

    @ParameterizedTest
    @DisplayName("toUuid should return null for invalid UUID strings")
    @ValueSource(strings = {"", "  ", "not-a-uuid", "12345"})
    void toUuidShouldReturnNullForInvalidStrings(String invalid) {
        assertNull(mapper.toUuid(invalid));
    }

    // ── toStringUuid (default) ─────────────────────────────────────────────────

    @Test
    @DisplayName("toStringUuid should convert UUID to string representation")
    void toStringUuidShouldConvertUuidToString() {
        UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        assertEquals("550e8400-e29b-41d4-a716-446655440000", mapper.toStringUuid(uuid));
    }

    @Test
    @DisplayName("toStringUuid should return null when UUID is null")
    void toStringUuidShouldReturnNullWhenUuidIsNull() {
        assertNull(mapper.toStringUuid(null));
    }

    // ── toProcessingStatus (default) ──────────────────────────────────────────

    @Test
    @DisplayName("toProcessingStatus should return enum name for non-null value")
    void toProcessingStatusShouldReturnEnumName() {
        assertEquals("RECEIVED",   mapper.toProcessingStatus(WebhookProcessingStatusEnum.RECEIVED));
        assertEquals("PROCESSING", mapper.toProcessingStatus(WebhookProcessingStatusEnum.PROCESSING));
        assertEquals("PROCESSED",  mapper.toProcessingStatus(WebhookProcessingStatusEnum.PROCESSED));
        assertEquals("FAILED",     mapper.toProcessingStatus(WebhookProcessingStatusEnum.FAILED));
    }

    @Test
    @DisplayName("toProcessingStatus should return null when value is null")
    void toProcessingStatusShouldReturnNullWhenValueIsNull() {
        assertNull(mapper.toProcessingStatus(null));
    }

    // ── toProcessingStatusEnum (default) ──────────────────────────────────────

    @Test
    @DisplayName("toProcessingStatusEnum should parse all valid status names")
    void toProcessingStatusEnumShouldParseAllValidNames() {
        assertEquals(WebhookProcessingStatusEnum.RECEIVED,   mapper.toProcessingStatusEnum("RECEIVED"));
        assertEquals(WebhookProcessingStatusEnum.PROCESSING, mapper.toProcessingStatusEnum("PROCESSING"));
        assertEquals(WebhookProcessingStatusEnum.PROCESSED,  mapper.toProcessingStatusEnum("PROCESSED"));
        assertEquals(WebhookProcessingStatusEnum.FAILED,     mapper.toProcessingStatusEnum("FAILED"));
    }

    @Test
    @DisplayName("toProcessingStatusEnum should return null for null input")
    void toProcessingStatusEnumShouldReturnNullForNullInput() {
        assertNull(mapper.toProcessingStatusEnum(null));
    }

    @ParameterizedTest
    @DisplayName("toProcessingStatusEnum should return null for blank input")
    @ValueSource(strings = {"", "  "})
    void toProcessingStatusEnumShouldReturnNullForBlankInput(String blank) {
        assertNull(mapper.toProcessingStatusEnum(blank));
    }

    @Test
    @DisplayName("toProcessingStatusEnum should return FAILED for unknown status")
    void toProcessingStatusEnumShouldReturnFailedForUnknownStatus() {
        assertEquals(WebhookProcessingStatusEnum.FAILED, mapper.toProcessingStatusEnum("UNKNOWN_STATUS"));
        assertEquals(WebhookProcessingStatusEnum.FAILED, mapper.toProcessingStatusEnum("CANCELLED"));
    }
}

