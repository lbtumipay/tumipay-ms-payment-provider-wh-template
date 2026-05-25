package com.tumipay.microservice.domain.model.webhook;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * WebhookEvent
 * <p>
 * Domain model representing a provider webhook event received by the platform.
 * This entity contains all required information for webhook lifecycle management,
 * including processing status, retry handling, claim-batch worker coordination,
 * and auditing timestamps.
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
@ToString(callSuper = false)
public class WebhookEvent implements Serializable {

    /**
     * Serialization identifier for the WebhookEvent domain model.
     */
    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /**
     * Internal database identifier of the webhook event.
     */
    private Long id;

    /**
     * Globally unique identifier of the webhook event.
     */
    private String uuid;

    /**
     * Adapter provider code associated with the webhook event.
     */
    private String adapterProviderCode;

    /**
     * TumiPay internal transaction ID. May be null if not available in the provider event payload.
     */
    private String transactionId;

    /**
     * TumiPay reference ID. May be null if not available in the provider event payload.
     */
    private String referenceId;

    /**
     * ID assigned by the Payment Provider for this transaction.
     */
    private String providerTransactionId;

    /**
     * Standardized event type received from the provider.
     */
    private String eventType;

    /**
     * External provider event identifier.
     */
    private String externalEventId;

    /**
     * Idempotency key used to prevent duplicate processing.
     */
    private String idempotencyKey;

    /**
     * Current processing status of the webhook event.
     */
    private WebhookProcessingStatusEnum processingStatus;

    /**
     * Error code generated during webhook processing.
     */
    private String errorCode;

    /**
     * Number of processing retry attempts executed for the webhook event.
     */
    private Integer retryCount;

    /**
     * Last processing error message associated with the webhook event.
     */
    private String lastError;

    /**
     * Raw webhook request payload received from the provider.
     */
    private String eventRequest;

    /**
     * Timestamp indicating when the webhook was received by the platform.
     */
    private Instant receivedAt;

    /**
     * Timestamp indicating when the webhook event record was created.
     */
    private Instant createdAt;

    /**
     * Timestamp indicating when the webhook event was successfully processed.
     */
    private Instant processedAt;

    /**
     * Identifier of the worker instance that claimed the webhook event
     * for processing using the Claim-Batch pattern.
     */
    private String claimedBy;

    /**
     * Timestamp indicating when the webhook event was claimed by a worker.
     */
    private Instant claimedAt;

    /**
     * Timestamp indicating when the webhook event becomes eligible
     * for retry or reprocessing.
     */
    private Instant nextRetryAt;

    /**
     * Timestamp of the last update performed on the webhook event record.
     */
    private Instant updatedAt;
}