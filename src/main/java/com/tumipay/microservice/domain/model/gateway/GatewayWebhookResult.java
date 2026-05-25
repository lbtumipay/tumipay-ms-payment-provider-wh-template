package com.tumipay.microservice.domain.model.gateway;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * GatewayWebhookResponseDto
 * <p>
 * Neutral domain contract for responses from the TumiPay Payment Gateway
 * after dispatching a normalized webhook event.
 * <p>
 * Possible outcomes:
 * <ul>
 *   <li>HTTP 200/202 — code: PROCESS_COMPLETED, status: SUCCESS</li>
 *   <li>HTTP 409       — code: DUPLICATE_EVENT,   status: FAILED</li>
 * </ul>
 * <p>
 * No Jackson annotations. Pure domain value object.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 30/04/2026
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayWebhookResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /**
     * Response code returned by the Gateway.
     * Possible values: PROCESS_COMPLETED, DUPLICATE_EVENT, etc.
     */
    private String code;

    /**
     * Processing status returned by the Gateway.
     * Possible values: SUCCESS, FAILED, ERROR, etc.
     */
    private String status;

    /**
     * Human-readable message describing the outcome of the Gateway's processing.
     */
    private String message;

    /**
     * Optional data present on successful responses (HTTP 200/202).
     * Contains the Gateway's internal event identifiers for correlation and traceability.
     * Null on HTTP 409 (duplicate event) responses.
     */
    private GatewayWebhookData data;

    /**
     * Nested data structure for successful Gateway responses.
     * Carries the Gateway's internal event identifiers.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayWebhookData implements Serializable {

        @Serial
        private static final long serialVersionUID = 3987654321098765432L;

        /** Internal identifier assigned by the Payment Gateway for this event. */
        private String gatewayEventId;

        /** Echo of the event_uuid sent in the request. Used for correlation. */
        private String eventId;
    }
}

