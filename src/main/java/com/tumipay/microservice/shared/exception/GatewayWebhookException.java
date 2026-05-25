package com.tumipay.microservice.shared.exception;

/**
 * GatewayWebhookException
 * <p>
 * Exception thrown when dispatching a normalized webhook event to the TumiPay Payment Gateway fails.
 * Carries an error code and descriptive message for retry/failure classification
 * in the Webhook Worker Claim-Batch pattern.
 * <p>
 * Error codes:
 * <ul>
 *   <li>{@code GATEWAY_TIMEOUT} — the dispatch call timed out.</li>
 *   <li>{@code GATEWAY_CLIENT_ERROR_{status}} — HTTP 4xx error returned by the Gateway.</li>
 *   <li>{@code GATEWAY_SERVER_ERROR_{status}} — HTTP 5xx error returned by the Gateway.</li>
 *   <li>{@code GATEWAY_HTTP_ERROR_{status}} — generic HTTP error.</li>
 * </ul>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 14/04/2026
 */
public class GatewayWebhookException extends RuntimeException {

    private final String code;

    public GatewayWebhookException(String code, String message) {
        super(message);
        this.code = code;
    }

    public GatewayWebhookException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

