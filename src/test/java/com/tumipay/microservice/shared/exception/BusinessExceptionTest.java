package com.tumipay.microservice.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BusinessExceptionTest
 * <p>
 * BusinessExceptionTest test class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 22/04/2026
 */
@DisplayName("BusinessException Unit Tests")
class BusinessExceptionTest {

    @Test
    @DisplayName("Should create exception with code and message")
    void shouldCreateExceptionWithCodeAndMessage() {
        BusinessException ex = new BusinessException("BIZ-001", "business rule violated");

        assertNotNull(ex);
        assertEquals("BIZ-001", ex.getCode());
        assertEquals("business rule violated", ex.getMessage());
    }

    @Test
    @DisplayName("Should extend RuntimeException")
    void shouldExtendRuntimeException() {
        BusinessException ex = new BusinessException("BIZ-002", "some error");

        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("Should propagate message to super constructor")
    void shouldPropagateMessageToSuperConstructor() {
        BusinessException ex = new BusinessException("BIZ-003", "super message");

        assertEquals("super message", ex.getMessage());
    }

    @Test
    @DisplayName("Should be throwable and catchable")
    void shouldBeThrowableAndCatchable() {
        try {
            throw new BusinessException("BIZ-004", "thrown error");
        } catch (BusinessException ex) {
            assertEquals("BIZ-004", ex.getCode());
            assertEquals("thrown error", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Same instance should be equal to itself")
    void sameInstanceShouldBeEqualToItself() {
        // @EqualsAndHashCode(callSuper = true) delegates to Throwable (identity-based)
        // so two different instances are never equal, but an instance is equal to itself
        BusinessException ex = new BusinessException("BIZ-005", "same error");

        assertEquals(ex, ex);
        assertEquals(ex.hashCode(), ex.hashCode());
    }

    @Test
    @DisplayName("Should support builder pattern")
    void shouldSupportBuilderPattern() {
        BusinessException ex = BusinessException.builder()
                .code("BIZ-006")
                .message("built error")
                .build();

        assertEquals("BIZ-006", ex.getCode());
        assertEquals("built error", ex.getMessage());
    }
}

