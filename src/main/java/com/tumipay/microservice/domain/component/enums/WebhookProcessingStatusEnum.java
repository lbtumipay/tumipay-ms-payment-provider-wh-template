package com.tumipay.microservice.domain.component.enums;

/**
 * WebhookProcessingStatusEnum
 * <p>
 * WebhookProcessingStatusEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
public enum WebhookProcessingStatusEnum {

    /**
     * Enum representing a webhook event received via HTTP (response to caller).
     */
    RECEIVED,

    /**
     * Enum representing a webhook event currently being validated by a worker instance.
     * Used to lock the row and prevent other instances from picking it up concurrently.
     */
    VALIDATING,

    /**
     * Enum representing a webhook event queued and ready to be processed by a worker.
     */
    PENDING,

    /**
     * Enum representing a webhook event currently being processed by a worker.
     */
    PROCESSING,

    /**
     * Enum representing a successfully processed webhook event.
     */
    PROCESSED,

    /**
     * Enum representing a webhook event that failed after exhausting all retries.
     */
    FAILED
}
