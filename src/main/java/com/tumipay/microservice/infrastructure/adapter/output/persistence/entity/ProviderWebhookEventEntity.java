package com.tumipay.microservice.infrastructure.adapter.output.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * ProviderWebhookEventEntity
 * <p>
 * Entity representing the tp_provider_webhook_event table.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString(callSuper = false)
@Table("tp_provider_webhook_event")
public class ProviderWebhookEventEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    @Id
    @Column("pwe_id")
    private Long id;

    @Column("pwe_uuid")
    private UUID uuid;

    @Column("pwe_adapter_provider_code")
    private String adapterProviderCode;

    @Column("pwe_event_type")
    private String eventType;

    @Column("pwe_transaction_id")
    private String transactionId;

    @Column("pwe_reference_id")
    private String referenceId;

    @Column("pwe_provider_transaction_id")
    private String providerTransactionId;

    @Column("pwe_external_event_id")
    private String externalEventId;

    @Column("pwe_idempotency_key")
    private String idempotencyKey;

    @Column("pwe_processing_status")
    private String processingStatus;

    @Column("pwe_error_code")
    private String errorCode;

    @Column("pwe_retry_count")
    private Integer retryCount;

    @Column("pwe_last_error")
    private String lastError;

    @Column("pwe_event_request")
    private String eventRequest;

    @Column("pwe_received_at")
    private Instant receivedAt;

    @Column("pwe_processed_at")
    private Instant processedAt;

    @Column("pwe_created_at")
    private Instant createdAt;

    @Column("pwe_claimed_by")
    private String claimedBy;

    @Column("pwe_claimed_at")
    private Instant claimedAt;

    @Column("pwe_next_retry_at")
    private Instant nextRetryAt;

    @Column("pwe_updated_at")
    private Instant updatedAt;
}

