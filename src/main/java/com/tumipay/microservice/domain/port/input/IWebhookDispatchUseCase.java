package com.tumipay.microservice.domain.port.input;

import reactor.core.publisher.Mono;
/**
 * IWebhookDispatchUseCase
 * <p>
 * Input port for the Webhook Worker. Defines the contract for executing a full
 * poll-and-process cycle using the Claim-Batch pattern over the
 * tp_provider_webhook_event table.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 10/04/2026
 */
public interface IWebhookDispatchUseCase {

    /**
     * Executes a full poll-and-process cycle for the webhook worker.
     *
     * @param workerId  unique identifier of the worker instance executing this cycle.
     * @param batchSize maximum number of events to claim and process in this cycle.
     * @return a reactive Mono that completes when all claimed events
     *         have been processed (successfully or with error handling).
     */
    Mono<Void> pollAndProcess(String workerId, int batchSize);
}