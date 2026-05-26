package com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper;

import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProviderWebhookEventMapperComponent.
 */
@DisplayName("ProviderWebhookEventMapperComponent Unit Tests")
class ProviderWebhookEventMapperComponentTest {

    private final ProviderWebhookEventMapperComponent mapper = new ProviderWebhookEventMapperComponent();

    @Test
    @DisplayName("mapRowToEntity should map complete row with temporal conversions")
    void mapRowToEntityShouldMapCompleteRow() {
        UUID uuid = UUID.randomUUID();
        Instant claimedAt = Instant.now();
        LocalDateTime nextRetryAt = LocalDateTime.of(2026, 5, 25, 10, 30, 0);
        OffsetDateTime updatedAt = OffsetDateTime.of(2026, 5, 25, 11, 45, 0, 0, ZoneOffset.ofHours(-5));
        Instant receivedAt = Instant.now().minusSeconds(60);
        Instant processedAt = Instant.now().minusSeconds(30);
        Instant createdAt = Instant.now().minusSeconds(120);

        Map<String, Object> row = new HashMap<>();
        row.put("pwe_id", 10);
        row.put("pwe_uuid", uuid.toString());
        row.put("pwe_adapter_provider_code", "TP_PROVIDER");
        row.put("pwe_event_type", "PAYMENT_APPROVED");
        row.put("pwe_external_event_id", "EVT-10");
        row.put("pwe_idempotency_key", "IDEM-10");
        row.put("pwe_processing_status", "PROCESSING");
        row.put("pwe_error_code", "ERR-10");
        row.put("pwe_retry_count", 2L);
        row.put("pwe_last_error", "none");
        row.put("pwe_event_request", "{}");
        row.put("pwe_claimed_by", "worker-1");
        row.put("pwe_claimed_at", claimedAt);
        row.put("pwe_next_retry_at", nextRetryAt);
        row.put("pwe_updated_at", updatedAt);
        row.put("pwe_received_at", receivedAt);
        row.put("pwe_processed_at", processedAt);
        row.put("pwe_created_at", createdAt);

        ProviderWebhookEventEntity entity = mapper.mapRowToEntity(row);

        assertNotNull(entity);
        assertEquals(10L, entity.getId());
        assertEquals(uuid, entity.getUuid());
        assertEquals("TP_PROVIDER", entity.getAdapterProviderCode());
        assertEquals("PAYMENT_APPROVED", entity.getEventType());
        assertEquals("EVT-10", entity.getExternalEventId());
        assertEquals("IDEM-10", entity.getIdempotencyKey());
        assertEquals("PROCESSING", entity.getProcessingStatus());
        assertEquals("ERR-10", entity.getErrorCode());
        assertEquals(2, entity.getRetryCount());
        assertEquals("none", entity.getLastError());
        assertEquals("{}", entity.getEventRequest());
        assertEquals("worker-1", entity.getClaimedBy());
        assertEquals(claimedAt, entity.getClaimedAt());
        assertEquals(nextRetryAt.toInstant(ZoneOffset.UTC), entity.getNextRetryAt());
        assertEquals(updatedAt.toInstant(), entity.getUpdatedAt());
        assertEquals(receivedAt, entity.getReceivedAt());
        assertEquals(processedAt, entity.getProcessedAt());
        assertEquals(createdAt, entity.getCreatedAt());
    }

    @Test
    @DisplayName("mapRowToEntity should use defaults when id uuid and retryCount are null")
    void mapRowToEntityShouldUseDefaultsWhenNulls() {
        Map<String, Object> row = new HashMap<>();
        row.put("pwe_id", null);
        row.put("pwe_uuid", null);
        row.put("pwe_retry_count", null);
        row.put("pwe_created_at", null);

        ProviderWebhookEventEntity entity = mapper.mapRowToEntity(row);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getUuid());
        assertEquals(0, entity.getRetryCount());
        assertNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("mapRowToEntity should set null for unsupported temporal value types")
    void mapRowToEntityShouldSetNullForUnsupportedTemporalTypes() {
        Map<String, Object> row = new HashMap<>();
        row.put("pwe_claimed_at", "not-a-temporal");
        row.put("pwe_next_retry_at", 12345);
        row.put("pwe_updated_at", true);
        row.put("pwe_received_at", "2026-05-25");
        row.put("pwe_processed_at", new Object());
        row.put("pwe_created_at", "created");

        ProviderWebhookEventEntity entity = mapper.mapRowToEntity(row);

        assertNull(entity.getClaimedAt());
        assertNull(entity.getNextRetryAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getReceivedAt());
        assertNull(entity.getProcessedAt());
        assertNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("mapRowToEntity should throw IllegalArgumentException when uuid format is invalid")
    void mapRowToEntityShouldThrowForInvalidUuid() {
        Map<String, Object> row = new HashMap<>();
        row.put("pwe_uuid", "invalid-uuid");

        assertThrows(IllegalArgumentException.class, () -> mapper.mapRowToEntity(row));
    }
}

