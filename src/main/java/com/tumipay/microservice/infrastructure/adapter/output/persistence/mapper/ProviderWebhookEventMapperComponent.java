package com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper;

import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import com.tumipay.microservice.shared.util.CommonInstantUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * IProviderWebhookEventPersistenceMapper
 * <p>
 * Mapper for webhook event persistence conversion.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Component
public class ProviderWebhookEventMapperComponent {

    public ProviderWebhookEventEntity mapRowToEntity(Map<String, Object> row) {
        return ProviderWebhookEventEntity.builder()
            .id(row.get("pwe_id") != null ? ((Number) row.get("pwe_id")).longValue() : null)
            .uuid(row.get("pwe_uuid") != null ? UUID.fromString(row.get("pwe_uuid").toString()) : null)
            .adapterProviderCode((String) row.get("pwe_adapter_provider_code"))
            .eventType((String) row.get("pwe_event_type"))
            .externalEventId((String) row.get("pwe_external_event_id"))
            .idempotencyKey((String) row.get("pwe_idempotency_key"))
            .processingStatus((String) row.get("pwe_processing_status"))
            .errorCode((String) row.get("pwe_error_code"))
            .retryCount(row.get("pwe_retry_count") != null ? ((Number) row.get("pwe_retry_count")).intValue() : 0)
            .lastError((String) row.get("pwe_last_error"))
            .eventRequest((String) row.get("pwe_event_request"))
            .claimedBy((String) row.get("pwe_claimed_by"))
            .claimedAt(CommonInstantUtils.toInstant(row.get("pwe_claimed_at")))
            .nextRetryAt(CommonInstantUtils.toInstant(row.get("pwe_next_retry_at")))
            .updatedAt(CommonInstantUtils.toInstant(row.get("pwe_updated_at")))
            .receivedAt(CommonInstantUtils.toInstant(row.get("pwe_received_at")))
            .processedAt(CommonInstantUtils.toInstant(row.get("pwe_processed_at")))
            .createdAt(CommonInstantUtils.toInstant(row.get("pwe_created_at")))
            .build();
    }
}

