package com.tumipay.microservice.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GatewayWebhookExceptionTest
 * <p>
 * Unit tests for {@link GatewayWebhookException}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-20
 */
@DisplayName("GatewayWebhookException Unit Tests")
class GatewayWebhookExceptionTest {

    @Test
    @DisplayName("Constructor(code, message) — should set code and message")
    void constructor_shouldSetCodeAndMessage() {
        GatewayWebhookException ex = new GatewayWebhookException("GATEWAY_TIMEOUT", "Request timed out");

        assertEquals("GATEWAY_TIMEOUT", ex.getCode());
        assertEquals("Request timed out", ex.getMessage());
    }

    @Test
    @DisplayName("Constructor(code, message, cause) — should set code, message and cause")
    void constructor_shouldSetCodeMessageAndCause() {
        RuntimeException cause = new RuntimeException("network error");
        GatewayWebhookException ex = new GatewayWebhookException("GATEWAY_SERVER_ERROR_500", "Internal server error", cause);

        assertEquals("GATEWAY_SERVER_ERROR_500", ex.getCode());
        assertEquals("Internal server error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("getCode — should return the code provided at construction")
    void getCode_shouldReturnProvidedCode() {
        GatewayWebhookException ex = new GatewayWebhookException("GATEWAY_CLIENT_ERROR_400", "Bad request");

        assertEquals("GATEWAY_CLIENT_ERROR_400", ex.getCode());
    }

    @Test
    @DisplayName("Should be an instance of RuntimeException")
    void shouldExtendRuntimeException() {
        GatewayWebhookException ex = new GatewayWebhookException("GATEWAY_HTTP_ERROR_503", "Service unavailable");

        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("Constructor(code, message) — cause should be null when not provided")
    void constructor_causeIsNullWhenNotProvided() {
        GatewayWebhookException ex = new GatewayWebhookException("GATEWAY_TIMEOUT", "timed out");

        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("Constructor(code, message, cause) — null cause is accepted")
    void constructor_nullCauseIsAccepted() {
        GatewayWebhookException ex = new GatewayWebhookException("GATEWAY_TIMEOUT", "timed out", null);

        assertEquals("GATEWAY_TIMEOUT", ex.getCode());
        assertNull(ex.getCause());
    }
}
