package com.tumipay.microservice.shared.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidationErrorTest
 * <p>
 * Unit tests for {@link ValidationError}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 07/04/2026
 */
@DisplayName("ValidationError Unit Tests")
class ValidationErrorTest {

    // ────────────────────────────────────────────────────────────────────────────
    // Builder
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("builder - should build instance with all fields")
    void builder_shouldBuildInstanceWithAllFields() {
        ValidationError error = ValidationError.builder()
            .field("amount")
            .message("must be positive")
            .build();

        assertNotNull(error);
        assertEquals("amount", error.getField());
        assertEquals("must be positive", error.getMessage());
    }

    @Test
    @DisplayName("builder - should build instance with null fields")
    void builder_shouldBuildInstanceWithNullFields() {
        ValidationError error = ValidationError.builder().build();

        assertNotNull(error);
        assertNull(error.getField());
        assertNull(error.getMessage());
    }

    // ────────────────────────────────────────────────────────────────────────────
    // No-Args Constructor
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("noArgsConstructor - should create empty instance")
    void noArgsConstructor_shouldCreateEmptyInstance() {
        ValidationError error = new ValidationError();

        assertNotNull(error);
        assertNull(error.getField());
        assertNull(error.getMessage());
    }

    // ────────────────────────────────────────────────────────────────────────────
    // All-Args Constructor
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("allArgsConstructor - should set all fields")
    void allArgsConstructor_shouldSetAllFields() {
        ValidationError error = new ValidationError("currency", "must not be blank");

        assertEquals("currency", error.getField());
        assertEquals("must not be blank", error.getMessage());
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Setters (@Data)
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setters - should update fields after construction")
    void setters_shouldUpdateFieldsAfterConstruction() {
        ValidationError error = new ValidationError();
        error.setField("email");
        error.setMessage("invalid format");

        assertEquals("email", error.getField());
        assertEquals("invalid format", error.getMessage());
    }

    // ────────────────────────────────────────────────────────────────────────────
    // equals & hashCode (@Data)
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("equals - two instances with same data should be equal")
    void equals_sameData_shouldBeEqual() {
        ValidationError e1 = ValidationError.builder().field("name").message("required").build();
        ValidationError e2 = ValidationError.builder().field("name").message("required").build();

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    @DisplayName("equals - two instances with different data should not be equal")
    void equals_differentData_shouldNotBeEqual() {
        ValidationError e1 = ValidationError.builder().field("name").message("required").build();
        ValidationError e2 = ValidationError.builder().field("age").message("must be positive").build();

        assertNotEquals(e1, e2);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // toString (@Data)
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toString - should include field and message values")
    void toString_shouldIncludeFieldValues() {
        ValidationError error = ValidationError.builder().field("phone").message("invalid").build();
        String result = error.toString();

        assertTrue(result.contains("phone"));
        assertTrue(result.contains("invalid"));
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Serializable
    // ────────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should implement Serializable")
    void shouldImplementSerializable() {
        ValidationError error = ValidationError.builder().field("code").message("not null").build();
        assertInstanceOf(java.io.Serializable.class, error);
    }
}

