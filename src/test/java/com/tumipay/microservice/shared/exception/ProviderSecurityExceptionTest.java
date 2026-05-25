package com.tumipay.microservice.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProviderSecurityExceptionTest
 * <p>
 * ProviderSecurityExceptionTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("ProviderSecurityException Unit Tests")
class ProviderSecurityExceptionTest {

    @Test
    @DisplayName("Should create exception with code and message")
    void shouldCreateExceptionWithCodeAndMessage() {
        ProviderSecurityException ex = new ProviderSecurityException("SEC-001", "authentication failed");

        assertNotNull(ex);
        assertEquals("SEC-001", ex.getCode());
        assertEquals("authentication failed", ex.getMessage());
    }

    @Test
    @DisplayName("Should extend RuntimeException")
    void shouldExtendRuntimeException() {
        ProviderSecurityException ex = new ProviderSecurityException("SEC-002", "some error");

        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("Should propagate message to super constructor")
    void shouldPropagateMessageToSuperConstructor() {
        ProviderSecurityException ex = new ProviderSecurityException("SEC-003", "super message");

        assertEquals("super message", ex.getMessage());
    }

    @Test
    @DisplayName("Should be throwable and catchable")
    void shouldBeThrowableAndCatchable() {
        try {
            throw new ProviderSecurityException("SEC-004", "token expired");
        } catch (ProviderSecurityException ex) {
            assertEquals("SEC-004", ex.getCode());
            assertEquals("token expired", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Same instance should be equal to itself")
    void sameInstanceShouldBeEqualToItself() {
        // @EqualsAndHashCode(callSuper = true) delegates to Throwable (identity-based)
        // so two different instances are never equal, but an instance is equal to itself
        ProviderSecurityException ex = new ProviderSecurityException("SEC-005", "same error");

        assertEquals(ex, ex);
        assertEquals(ex.hashCode(), ex.hashCode());
    }

    @Test
    @DisplayName("Should support builder pattern")
    void shouldSupportBuilderPattern() {
        ProviderSecurityException ex = ProviderSecurityException.builder()
            .code("SEC-006")
            .message("built security error")
            .build();

        assertEquals("SEC-006", ex.getCode());
        assertEquals("built security error", ex.getMessage());
    }
}