package com.tumipay.microservice.domain.port.output;

import com.tumipay.microservice.domain.model.gateway.GatewayWebhookResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import reactor.core.publisher.Mono;

/**
 * IPaymentGatewayWebhookAdapterPort
 * <p>
 * Output port for dispatching normalized webhook events to the TumiPay Payment Gateway.
 * Defines the reactive contract that infrastructure adapters must implement to forward
 * provider webhook events using the standard TumiPay Gateway webhook HTTP contract.
 * <p>
 * All Payment Provider adapters share this same dispatch contract. What varies per provider
 * is the {@code event_request} payload embedded inside the request body, which corresponds
 * to the raw event received from the external Payment Provider and stored in the
 * {@code pwe_event_request} column of the {@code tp_provider_webhook_event} table.
 * <p>
 * The implementation must:
 * <ul>
 *   <li>Forward the event via HTTP POST to the Gateway webhook endpoint.</li>
 *   <li>Include the {@code X-Idempotency-Key} header from {@link WebhookEvent#getIdempotencyKey()}.</li>
 *   <li>Include the {@code X-Api-Key} header for Gateway authentication.</li>
 *   <li>Include the {@code X-Adapter-Provider-Code} header identifying the provider.</li>
 *   <li>Treat HTTP 200, 202 and 409 (duplicate) as successful acknowledgements.</li>
 *   <li>Raise a {@code GatewayWebhookException} for unrecoverable 4xx/5xx errors.</li>
 * </ul>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 14/04/2026
 */
public interface IPaymentGatewayWebhookAdapterPort {

    /**
     * Dispatches a normalized webhook event to the TumiPay Payment Gateway.
     * <p>
     * Builds the standard Gateway webhook request from the {@link WebhookEvent} domain model,
     * posts it to the configured gateway webhook endpoint, and returns the Gateway acknowledgement.
     *
     * @param webhookEvent the domain model of the webhook event to dispatch,
     *                     populated from the {@code tp_provider_webhook_event} table record.
     * @return {@link Mono} emitting the {@link GatewayWebhookResult} with the Gateway's
     *         acknowledgement ({@code RECEIVED}, {@code ACCEPTED} or {@code DUPLICATE}),
     *         or an error signal if the dispatch fails with an unrecoverable error.
     */
    Mono<GatewayWebhookResult> dispatchWebhookEvent(WebhookEvent webhookEvent);
}

