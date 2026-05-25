package com.tumipay.microservice.domain.port.input;

import reactor.core.publisher.Mono;

/**
 * IWebhookWorkerUseCase
 * <p>
 * Input port for the Webhook Receiver. Defines the contract for pre-processing
 * a batch of RECEIVED webhook events before they are enqueued as PENDING for
 * the {@code WebhookWorkerScheduler}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/04/2026
 */
public interface IWebhookReceiverUseCase {

    /**
     * Executes one pre-processing cycle over a batch of RECEIVED webhook events.
     * <p>
     * For each event:
     * <ul>
     *   <li>PAYOUT_TRANSACTION events: resolves the provider transaction and updates
     *       its status based on the webhook payload, then transitions the webhook to PENDING.</li>
     *   <li>PAYIN_TRANSACTION events: transitions the webhook directly to PENDING
     *       without modifying the transaction record.</li>
     * </ul>
     *
     * @param batchSize maximum number of RECEIVED events to process in this cycle.
     * @return a reactive {@link Mono} that completes when all events in the batch
     *         have been processed (successfully or with logged errors).
     */
    Mono<Void> processReceivedBatch(int batchSize);
}

