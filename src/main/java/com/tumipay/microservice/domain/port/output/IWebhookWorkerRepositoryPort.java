package com.tumipay.microservice.domain.port.output;

import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * IWebhookWorkerRepositoryPort
 * <p>
 * Output port for the Webhook Worker Claim-Batch pattern.
 * Defines the contract for atomically claiming and updating webhook events
 * stored in the {@code tp_provider_webhook_event} table.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 10/04/2026
 */
public interface IWebhookWorkerRepositoryPort {

    /**
     * Atomically claims a batch of PENDING (or abandoned PROCESSING) webhook events
     * using {@code FOR UPDATE SKIP LOCKED}. Safe for multi-replica deployments.
     *
     * @param workerId  unique identifier of the worker instance claiming the batch.
     * @param batchSize maximum number of events to claim in this cycle.
     * @return a reactive {@link Flux} emitting the claimed {@link WebhookEvent} records.
     */
    Flux<WebhookEvent> claimBatch(String workerId, int batchSize);

    /**
     * Marks a webhook event as successfully processed.
     * Sets {@code pwe_processing_status = PROCESSED} and clears the claim fields.
     *
     * @param id the internal identifier of the webhook event.
     * @return a reactive {@link Mono} emitting the updated {@link WebhookEvent}.
     */
    Mono<WebhookEvent> markAsProcessed(Long id);

    /**
     * Schedules a webhook event for retry or marks it as FAILED if retries are exhausted.
     * <ul>
     *   <li>If {@code retry_count < maxRetryCount}: sets status to {@code PENDING}
     *       with {@code next_retry_at = now() + 30s}.</li>
     *   <li>If {@code retry_count >= maxRetryCount}: sets status to {@code FAILED}.</li>
     * </ul>
     *
     * @param id            the internal identifier of the webhook event.
     * @param errorCode     the error code to persist.
     * @param lastError     the error message to persist (truncated to 500 chars).
     * @param maxRetryCount the maximum number of allowed retries.
     * @return a reactive {@link Mono} emitting the updated {@link WebhookEvent}.
     */
    Mono<WebhookEvent> markForRetry(Long id, String errorCode, String lastError, int maxRetryCount);

    /**
     * Directly marks a webhook event as FAILED without incrementing retry count.
     * Used for non-recoverable errors.
     *
     * @param id        the internal identifier of the webhook event.
     * @param errorCode the error code to persist.
     * @param lastError the error message to persist (truncated to 500 chars).
     * @return a reactive {@link Mono} emitting the updated {@link WebhookEvent}.
     */
    Mono<WebhookEvent> markAsFailed(Long id, String errorCode, String lastError);

    /**
     * Retrieves a batch of webhook events with {@code RECEIVED} processing status,
     * ordered by {@code pwe_received_at ASC} (oldest-first).
     * <p>
     * Intended for use by the {@code WebhookReceiverScheduler} to find events
     * that arrived via HTTP but have not yet been pre-processed.
     *
     * @param batchSize maximum number of RECEIVED events to return.
     * @return a reactive {@link Flux} emitting the found {@link WebhookEvent} records.
     */
    Flux<WebhookEvent> findReceivedBatch(int batchSize);
}

