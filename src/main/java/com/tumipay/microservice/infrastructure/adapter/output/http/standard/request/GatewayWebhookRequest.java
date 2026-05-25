package com.tumipay.microservice.infrastructure.adapter.output.http.standard.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * GatewayWebhookRequest
 * <p>
 * Standard request DTO for dispatching normalized webhook events to the TumiPay Payment Gateway.
 * All Payment Provider adapters use this same contract to forward events; what varies per
 * provider is the {@code event_request} payload embedded in the body.
 * <p>
 * Sent via HTTP POST to the endpoint configured in
 * {@code tumipay.payment-provider.gateway.endpoints.webhook-event-path}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 14/04/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayWebhookRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /**
     * UUID of the webhook event record in the adapter's {@code tp_provider_webhook_event} table.
     */
    @JsonProperty("event_id")
    private String eventId;

    /**
     * Normalized event type. Values from {@code WebhookEventTypeEnum}.
     * Examples: PAYIN_TRANSACTION_APPROVED, PAYOUT_TRANSACTION_REJECTED.
     */
    @JsonProperty("event_type")
    private String eventType;

    /**
     * Unique code of the Payment Provider adapter that originated this event.
     * Sourced from {@code paymentProvidersProperties.getCode()}.
     */
    @JsonProperty("adapter_provider_code")
    private String adapterProviderCode;

    /**
     * TumiPay internal transaction ID. May be null if not available in the provider event payload.
     */
    @JsonProperty("transaction_id")
    private String transactionId;

    /**
     * TumiPay reference ID. May be null if not available in the provider event payload.
     */
    @JsonProperty("reference_id")
    private String referenceId;

    /**
     * ID assigned by the Payment Provider for this transaction.
     */
    @JsonProperty("provider_transaction_id")
    private String providerTransactionId;

    /**
     * Full event payload received from the Payment Provider.
     * Sourced from {@code WebhookEvent.eventRequest} (column {@code pwe_event_request}).
     * Sent as a native JSON Object (not as an escaped String) to ensure clean JSON embedding.
     */
    @JsonProperty("event_request")
    private Object eventRequest;

    /**
     * Timestamp when the event was originally received from the Payment Provider.
     */
    @JsonProperty("received_at")
    private Instant receivedAt;
}

