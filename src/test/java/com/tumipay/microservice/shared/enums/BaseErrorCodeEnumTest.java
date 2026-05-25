package com.tumipay.microservice.shared.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BaseErrorCodeEnum.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("BaseErrorCodeEnum Unit Tests")
class BaseErrorCodeEnumTest {

    // ── Enum catalogue ─────────────────────────────────────────────────────────


    @Test
    @DisplayName("Every constant should have non-null code and message")
    void everyConstantShouldHaveNonNullCodeAndMessage() {
        for (BaseErrorCodeEnum value : BaseErrorCodeEnum.values()) {
            assertNotNull(value.getCode(),    value.name() + " must have a non-null code");
            assertNotNull(value.getMessage(), value.name() + " must have a non-null message");
        }
    }

    @ParameterizedTest
    @DisplayName("Should expose correct code and message per constant")
    @CsvSource({
        "VALIDATION_ERROR,       VALIDATION_ERROR, Validation error",
        "TRANSACTION_NOT_FOUND,  TRANSACTION_NOT_FOUND, Transaction not found",
        "DUPLICATE_TRANSACTION,  DUPLICATE_TRANSACTION, Transaction with this client_transaction_id already exists",
        "INVALID_CURRENCY_CODE,  INVALID_CURRENCY_CODE, Invalid currency code format",
        "INVALID_COUNTRY_CODE,   INVALID_COUNTRY_CODE, Invalid country code format",
        "INVALID_EMAIL,          INVALID_EMAIL, Invalid email format",
        "INVALID_AMOUNT,         INVALID_AMOUNT, Amount must be greater than zero",
        "DATABASE_ERROR,         DATABASE_ERROR, Database operation failed",
        "CACHE_ERROR,            CACHE_ERROR, Cache operation failed",
        "DUPLICATE_WEBHOOK_EVENT, DUPLICATE_WEBHOOK_EVENT, A webhook event with this idempotency key has already been processed",
        "WEBHOOK_PROCESSING_ERROR, WEBHOOK_PROCESSING_ERROR, An error occurred while processing the webhook event",
        "INTERNAL_ERROR,         INTERNAL_ERROR, Internal server error"
    })
    void shouldExposeCorrectCodeAndMessagePerConstant(String name, String expectedCode, String expectedMessage) {
        BaseErrorCodeEnum constant = BaseErrorCodeEnum.valueOf(name.trim());

        assertEquals(expectedCode.trim(),    constant.getCode());
        assertEquals(expectedMessage.trim(), constant.getMessage());
    }

    // ── toString ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toString should return the code value")
    void toStringShouldReturnCodeValue() {
        assertEquals("VALIDATION_ERROR", BaseErrorCodeEnum.VALIDATION_ERROR.toString());
        assertEquals("INTERNAL_ERROR", BaseErrorCodeEnum.INTERNAL_ERROR.toString());
    }

    // ── getResponseByCode ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getResponseByCode should return matching enum for a valid code")
    void getResponseByCodeShouldReturnMatchingEnumForValidCode() {
        assertEquals(BaseErrorCodeEnum.VALIDATION_ERROR,      BaseErrorCodeEnum.getResponseByCode("VALIDATION_ERROR"));
        assertEquals(BaseErrorCodeEnum.TRANSACTION_NOT_FOUND, BaseErrorCodeEnum.getResponseByCode("TRANSACTION_NOT_FOUND"));
        assertEquals(BaseErrorCodeEnum.WEBHOOK_PROCESSING_ERROR, BaseErrorCodeEnum.getResponseByCode("WEBHOOK_PROCESSING_ERROR"));
        assertEquals(BaseErrorCodeEnum.INTERNAL_ERROR,        BaseErrorCodeEnum.getResponseByCode("INTERNAL_ERROR"));
    }

    @Test
    @DisplayName("getResponseByCode should return null for an unknown code")
    void getResponseByCodeShouldReturnNullForUnknownCode() {
        assertNull(BaseErrorCodeEnum.getResponseByCode("999999"));
        assertNull(BaseErrorCodeEnum.getResponseByCode("XYZ"));
    }

    @Test
    @DisplayName("getResponseByCode should return null for an empty string")
    void getResponseByCodeShouldReturnNullForEmptyString() {
        assertNull(BaseErrorCodeEnum.getResponseByCode(""));
    }

    @Test
    @DisplayName("getResponseByCode should throw NullPointerException when code is null")
    void getResponseByCodeShouldThrowNullPointerExceptionWhenCodeIsNull() {
        assertThrows(NullPointerException.class,
            () -> BaseErrorCodeEnum.getResponseByCode(null));
    }

    // ── exists ─────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @DisplayName("exists should return true for all declared codes")
    @ValueSource(strings = {
        "VALIDATION_ERROR",
        "TRANSACTION_NOT_FOUND",
        "DUPLICATE_TRANSACTION",
        "INVALID_CURRENCY_CODE",
        "INVALID_COUNTRY_CODE",
        "INVALID_EMAIL",
        "INVALID_AMOUNT",
        "DATABASE_ERROR",
        "CACHE_ERROR",
        "DUPLICATE_WEBHOOK_EVENT",
        "WEBHOOK_PROCESSING_ERROR",
        "INTERNAL_ERROR"
    })
    void existsShouldReturnTrueForAllDeclaredCodes(String code) {
        assertTrue(BaseErrorCodeEnum.exists(code));
    }

    @Test
    @DisplayName("exists should return false for an unknown code")
    void existsShouldReturnFalseForUnknownCode() {
        assertFalse(BaseErrorCodeEnum.exists("UNKNOWN_CODE"));
        assertFalse(BaseErrorCodeEnum.exists("ZZZ"));
    }

    @Test
    @DisplayName("exists should return false for an empty string")
    void existsShouldReturnFalseForEmptyString() {
        assertFalse(BaseErrorCodeEnum.exists(""));
    }

    @Test
    @DisplayName("exists should throw NullPointerException when code is null")
    void existsShouldThrowNullPointerExceptionWhenCodeIsNull() {
        assertThrows(NullPointerException.class,
            () -> BaseErrorCodeEnum.exists(null));
    }

    // ── Code uniqueness ────────────────────────────────────────────────────────

    @Test
    @DisplayName("All declared constants should have unique codes")
    void allConstantsShouldHaveUniqueCodes() {
        long distinctCodes = java.util.Arrays.stream(BaseErrorCodeEnum.values())
            .map(BaseErrorCodeEnum::getCode)
            .distinct()
            .count();

        assertEquals(BaseErrorCodeEnum.values().length, distinctCodes,
            "Every constant must have a unique code");
    }
}

