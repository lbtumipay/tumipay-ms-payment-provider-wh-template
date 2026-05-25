package com.tumipay.microservice.domain.service.contract;

import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.shared.dto.DomainOperationResult;
import com.tumipay.microservice.shared.dto.DomainValidationResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * IProviderWebhookEventDomainService
 * <p>
 * Provider webhook event domain service contract.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
public interface IProviderWebhookEventDomainService
    extends
    ISaveDomainEntity<WebhookEvent, Mono<DomainOperationResult<WebhookEvent>>>,
    IUpdateDomainEntity<WebhookEvent, Mono<DomainOperationResult<WebhookEvent>>>,
    IGetDomainEntityByUuId<String, Mono<DomainOperationResult<WebhookEvent>>> {

    /**
     * Validates the idempotency of a webhook event using the provided idempotency key.
     * If a record with the same key already exists, a validation failure result is returned.
     *
     * @param idempotencyKey unique key used to identify duplicate webhook events.
     * @return a {@link Mono} emitting a {@link DomainValidationResult} indicating
     *         whether the event is new (valid) or already processed (invalid).
     */
    Mono<DomainValidationResult> validateIdempotency(String idempotencyKey);

    /**
     * Atomically claims a batch of pending webhook events and assigns them to the given worker.
     * Only events in a claimable state (e.g., RECEIVED or PENDING_RETRY) are selected.
     *
     * @param workerId  identifier of the worker instance claiming the batch.
     * @param batchSize maximum number of events to claim in a single operation.
     * @return a {@link Flux} emitting the claimed {@link WebhookEvent} records.
     */
    Flux<WebhookEvent> claimBatch(String workerId, int batchSize);

    /**
     * Marks the webhook event identified by the given ID as successfully processed.
     * Updates the event status to PROCESSED and sets the processed timestamp.
     *
     * @param id internal identifier of the webhook event.
     * @return a {@link Mono} emitting the updated {@link WebhookEvent}.
     */
    Mono<WebhookEvent> markAsProcessed(Long id);

    /**
     * Marks the webhook event identified by the given ID for retry after a processing failure.
     * Increments the retry counter and stores the error details. If the retry count has reached
     * the maximum allowed attempts, the event may be transitioned to a FAILED state instead.
     *
     * @param id            internal identifier of the webhook event.
     * @param errorCode     short error code describing the failure reason.
     * @param lastError     detailed error message or stack trace from the last processing attempt.
     * @param maxRetryCount maximum number of retry attempts allowed for the event.
     * @return a {@link Mono} emitting the updated {@link WebhookEvent}.
     */
    Mono<WebhookEvent> markForRetry(Long id, String errorCode, String lastError, int maxRetryCount);

    /**
     * Retrieves a batch of webhook events in RECEIVED status, ordered by
     * received_at ASC (oldest first), up to the given limit.
     *
     * @param batchSize maximum number of RECEIVED events to return.
     * @return a reactive {@link Flux} emitting the found {@link WebhookEvent} records.
     */
    Flux<WebhookEvent> findReceivedBatch(int batchSize);
}

