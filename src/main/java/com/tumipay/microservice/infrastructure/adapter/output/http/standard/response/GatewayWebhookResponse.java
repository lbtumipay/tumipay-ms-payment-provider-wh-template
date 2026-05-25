package com.tumipay.microservice.infrastructure.adapter.output.http.standard.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * GatewayWebhookResponse
 * <p>
 * Standard response DTO from the TumiPay Payment Gateway after receiving a webhook dispatch.
 * The Payment Gateway must return this contract for HTTP 200, 202 and 409 responses.
 * <p>
 * Response structure:
 * <ul>
 *   <li>HTTP 200 / 202 — {@code code}: {@code PROCESS_COMPLETED}, {@code status}: {@code SUCCESS}</li>
 *   <li>HTTP 409       — {@code code}: {@code DUPLICATE_EVENT},   {@code status}: {@code FAILED}</li>
 * </ul>
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
public class GatewayWebhookResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /**
     * Response code returned by the Gateway.
     * Possible values: {@code PROCESS_COMPLETED}, {@code DUPLICATE_EVENT}.
     */
    @JsonProperty("code")
    private String code;

    /**
     * Processing status returned by the Gateway.
     * Possible values: {@code SUCCESS}, {@code FAILED}, {@code ERROR}.
     */
    @JsonProperty("status")
    private String status;

    /**
     * Human-readable message describing the outcome of the Gateway's processing.
     */
    @JsonProperty("message")
    private String message;

    /**
     * Optional payload present on HTTP 200 / 202 responses.
     * Contains the Gateway's internal event identifiers for correlation and traceability.
     * {@code null} on HTTP 409 (duplicate event) responses.
     */
    @JsonProperty("data")
    private GatewayWebhookResponseData data;

    // -------------------------------------------------------------------------
    // Nested DTO
    // -------------------------------------------------------------------------

    /**
     * GatewayWebhookResponseData
     * <p>
     * Payload embedded in successful Gateway responses (HTTP 200 / 202).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayWebhookResponseData implements Serializable {

        @Serial
        private static final long serialVersionUID = 3987654321098765432L;

        /**
         * Internal identifier assigned by the Payment Gateway for this event.
         */
        @JsonProperty("gateway_event_id")
        private String gatewayEventId;

        /**
         * Echo of the {@code event_uuid} sent in the request. Used for correlation.
         */
        @JsonProperty("event_id")
        private String eventId;
    }
}
