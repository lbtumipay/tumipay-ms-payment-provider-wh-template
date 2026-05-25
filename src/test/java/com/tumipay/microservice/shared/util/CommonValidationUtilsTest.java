package com.tumipay.microservice.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CommonValidationUtilsTest
 * <p>
 * Unit tests for {@link CommonValidationUtils}, covering all public validation
 * methods: text presence, required enum, and UUID format.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 9/04/2026
 */
@DisplayName("CommonValidationUtils Unit Tests")
class CommonValidationUtilsTest {

    // -------------------------------------------------------------------------
    // validateText
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should add error when text value is null")
    void shouldAddErrorWhenTextIsNull() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateText(null, "username", errors);

        assertEquals(1, errors.size());
        assertEquals("The username is required and cannot be empty", errors.get(0));
    }

    @Test
    @DisplayName("Should add error when text value is empty")
    void shouldAddErrorWhenTextIsEmpty() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateText("", "username", errors);

        assertEquals(1, errors.size());
        assertEquals("The username is required and cannot be empty", errors.get(0));
    }

    @Test
    @DisplayName("Should add error when text value contains only whitespace")
    void shouldAddErrorWhenTextIsBlank() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateText("   ", "username", errors);

        assertEquals(1, errors.size());
        assertEquals("The username is required and cannot be empty", errors.get(0));
    }

    @Test
    @DisplayName("Should not add error when text value is valid")
    void shouldNotAddErrorWhenTextIsValid() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateText("john_doe", "username", errors);

        assertTrue(errors.isEmpty());
    }

    // -------------------------------------------------------------------------
    // validateRequiredEnum
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should add error when enum value is null")
    void shouldAddErrorWhenEnumIsNull() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateRequiredEnum(null, "status", errors);

        assertEquals(1, errors.size());
        assertEquals("The status is required and cannot be null", errors.get(0));
    }

    @Test
    @DisplayName("Should not add error when enum value is present")
    void shouldNotAddErrorWhenEnumIsPresent() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateRequiredEnum(SampleEnum.ACTIVE, "status", errors);

        assertTrue(errors.isEmpty());
    }

    // -------------------------------------------------------------------------
    // validateUuidText
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should add error when UUID text is null")
    void shouldAddErrorWhenUuidTextIsNull() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateUuidText(null, "transactionId", errors);

        assertEquals(1, errors.size());
        assertEquals("The transactionId is required and cannot be empty", errors.get(0));
    }

    @Test
    @DisplayName("Should add error when UUID text is empty")
    void shouldAddErrorWhenUuidTextIsEmpty() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateUuidText("", "transactionId", errors);

        assertEquals(1, errors.size());
        assertEquals("The transactionId is required and cannot be empty", errors.get(0));
    }

    @Test
    @DisplayName("Should add error when UUID text is blank")
    void shouldAddErrorWhenUuidTextIsBlank() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateUuidText("   ", "transactionId", errors);

        assertEquals(1, errors.size());
        assertEquals("The transactionId is required and cannot be empty", errors.get(0));
    }

    @Test
    @DisplayName("Should add format error when UUID text is not a valid UUID")
    void shouldAddFormatErrorWhenUuidTextIsInvalid() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateUuidText("not-a-uuid", "transactionId", errors);

        assertEquals(1, errors.size());
        assertEquals("The transactionId format is invalid", errors.get(0));
    }

    @Test
    @DisplayName("Should not add error when UUID text is a valid UUID")
    void shouldNotAddErrorWhenUuidTextIsValid() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateUuidText("550e8400-e29b-41d4-a716-446655440000", "transactionId", errors);

        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Should accumulate multiple errors across consecutive validations")
    void shouldAccumulateMultipleErrors() {
        List<String> errors = new ArrayList<>();

        CommonValidationUtils.validateText(null, "name", errors);
        CommonValidationUtils.validateRequiredEnum(null, "type", errors);
        CommonValidationUtils.validateUuidText("bad-id", "referenceId", errors);

        assertEquals(3, errors.size());
    }

    // -------------------------------------------------------------------------
    // Helper enum for testing
    // -------------------------------------------------------------------------

    private enum SampleEnum {
        ACTIVE, INACTIVE
    }
}

