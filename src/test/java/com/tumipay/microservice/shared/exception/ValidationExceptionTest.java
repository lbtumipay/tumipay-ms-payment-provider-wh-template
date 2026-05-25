package com.tumipay.microservice.shared.exception;

import com.tumipay.microservice.shared.dto.ValidationError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidationExceptionTest
 * <p>
 * ValidationExceptionTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("ValidationException Unit Tests")
class ValidationExceptionTest {

    @Test
    @DisplayName("Should create exception with code message and errors")
    void shouldCreateExceptionWithCodeMessageAndErrors() {

        List<ValidationError> errors = List.of(
            ValidationError.builder().field("amount").message("must be positive").build(),
            ValidationError.builder().field("currency").message("must not be blank").build()
        );

        ValidationException ex = new ValidationException("VAL-001", "validation failed", errors);

        assertNotNull(ex);
        assertEquals("VAL-001", ex.getCode());
        assertEquals("validation failed", ex.getMessage());
        assertEquals(2, ex.getErrors().size());
        assertEquals("amount", ex.getErrors().get(0).getField());
        assertEquals("currency", ex.getErrors().get(1).getField());
    }

    @Test
    @DisplayName("Should extend RuntimeException")
    void shouldExtendRuntimeException() {
        ValidationException ex = new ValidationException("VAL-002", "error", null);

        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("Should propagate message to super constructor")
    void shouldPropagateMessageToSuperConstructor() {
        ValidationException ex = new ValidationException("VAL-003", "super message", null);

        assertEquals("super message", ex.getMessage());
    }

    @Test
    @DisplayName("Should allow null errors list")
    void shouldAllowNullErrorsList() {
        ValidationException ex = new ValidationException("VAL-004", "no detail", null);

        assertNull(ex.getErrors());
    }

    @Test
    @DisplayName("Should allow empty errors list")
    void shouldAllowEmptyErrorsList() {
        ValidationException ex = new ValidationException("VAL-005", "empty errors", List.of());

        assertNotNull(ex.getErrors());
        assertTrue(ex.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should be throwable and catchable")
    void shouldBeThrowableAndCatchable() {
        List<ValidationError> errors = List.of(
            ValidationError.builder().field("email").message("invalid format").build()
        );

        try {
            throw new ValidationException("VAL-006", "request invalid", errors);
        } catch (ValidationException ex) {
            assertEquals("VAL-006", ex.getCode());
            assertEquals("request invalid", ex.getMessage());
            assertEquals(1, ex.getErrors().size());
            assertEquals("email", ex.getErrors().get(0).getField());
        }
    }

    @Test
    @DisplayName("Should support builder pattern")
    void shouldSupportBuilderPattern() {
        ValidationError error = ValidationError.builder()
            .field("transactionId")
            .message("must not be null")
            .build();

        ValidationException ex = ValidationException.builder()
            .code("VAL-007")
            .message("built validation error")
            .errors(List.of(error))
            .build();

        assertEquals("VAL-007", ex.getCode());
        assertEquals("built validation error", ex.getMessage());
        assertEquals(1, ex.getErrors().size());
        assertEquals("transactionId", ex.getErrors().get(0).getField());
    }
}
