package com.tumipay.microservice.infrastructure.component.enums;

import com.tumipay.microservice.infrastructure.component.constant.BaseResponseConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BaseResponseEnum.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("BaseResponseEnum Unit Tests")
class BaseResponseEnumTest {

    // ── Enum catalogue ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should contain exactly two constants")
    void shouldContainExactlyTwoConstants() {
        assertEquals(2, BaseResponseEnum.values().length);
    }

    @Test
    @DisplayName("Every constant should have non-null code status and message")
    void everyConstantShouldHaveNonNullFields() {
        for (BaseResponseEnum value : BaseResponseEnum.values()) {
            assertNotNull(value.getCode(),    value.name() + " must have a non-null code");
            assertNotNull(value.getStatus(),  value.name() + " must have a non-null status");
            assertNotNull(value.getMessage(), value.name() + " must have a non-null message");
        }
    }

    @ParameterizedTest
    @DisplayName("Should expose correct code status and message per constant")
    @CsvSource({
        "SUCCESS_RESPONSE,       PROCESS_COMPLETED,   SUCCESS, Operation completed successfully",
        "INTERNAL_ERROR_RESPONSE, INTERNAL_SERVER_ERROR, ERROR, Internal Server Error"
    })
    void shouldExposeCorrectFieldsPerConstant(
            String name, String expectedCode, String expectedStatus, String expectedMessage) {
        BaseResponseEnum constant = BaseResponseEnum.valueOf(name.trim());

        assertEquals(expectedCode.trim(),    constant.getCode());
        assertEquals(expectedStatus.trim(),  constant.getStatus());
        assertEquals(expectedMessage.trim(), constant.getMessage());
    }

    // ── Alignment with BaseResponseConstant ────────────────────────────────────

    @Test
    @DisplayName("SUCCESS_RESPONSE should align with BaseResponseConstant values")
    void successResponseShouldAlignWithBaseResponseConstant() {
        BaseResponseEnum success = BaseResponseEnum.SUCCESS_RESPONSE;

        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_CODE,    success.getCode());
        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_STATUS,  success.getStatus());
        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_MESSAGE, success.getMessage());
    }

    @Test
    @DisplayName("INTERNAL_ERROR_RESPONSE should align with BaseResponseConstant values")
    void internalErrorResponseShouldAlignWithBaseResponseConstant() {
        BaseResponseEnum error = BaseResponseEnum.INTERNAL_ERROR_RESPONSE;

        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_CODE,    error.getCode());
        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_STATUS,  error.getStatus());
        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_MESSAGE, error.getMessage());
    }

    // ── toString ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toString should return the code value")
    void toStringShouldReturnCodeValue() {
        assertEquals("PROCESS_COMPLETED",   BaseResponseEnum.SUCCESS_RESPONSE.toString());
        assertEquals("INTERNAL_SERVER_ERROR", BaseResponseEnum.INTERNAL_ERROR_RESPONSE.toString());
    }

    // ── getResponseByCode ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getResponseByCode should return matching enum for a valid code")
    void getResponseByCodeShouldReturnMatchingEnumForValidCode() {
        assertEquals(BaseResponseEnum.SUCCESS_RESPONSE,
            BaseResponseEnum.getResponseByCode("PROCESS_COMPLETED"));
        assertEquals(BaseResponseEnum.INTERNAL_ERROR_RESPONSE,
            BaseResponseEnum.getResponseByCode("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("getResponseByCode lookup should be case-insensitive")
    void getResponseByCodeShouldBeCaseInsensitive() {
        assertEquals(BaseResponseEnum.SUCCESS_RESPONSE,
            BaseResponseEnum.getResponseByCode("process_completed"));
        assertEquals(BaseResponseEnum.SUCCESS_RESPONSE,
            BaseResponseEnum.getResponseByCode("Process_Completed"));
    }

    @Test
    @DisplayName("getResponseByCode should return null for an unknown code")
    void getResponseByCodeShouldReturnNullForUnknownCode() {
        assertNull(BaseResponseEnum.getResponseByCode("UNKNOWN_CODE"));
    }

    @Test
    @DisplayName("getResponseByCode should return null for an empty string")
    void getResponseByCodeShouldReturnNullForEmptyString() {
        assertNull(BaseResponseEnum.getResponseByCode(""));
    }

    @Test
    @DisplayName("getResponseByCode should throw NullPointerException when code is null")
    void getResponseByCodeShouldThrowNullPointerExceptionWhenCodeIsNull() {
        assertThrows(NullPointerException.class,
            () -> BaseResponseEnum.getResponseByCode(null));
    }

    // ── exists ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("exists should return true for all declared codes")
    void existsShouldReturnTrueForAllDeclaredCodes() {
        assertTrue(BaseResponseEnum.exists("PROCESS_COMPLETED"));
        assertTrue(BaseResponseEnum.exists("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("exists lookup should be case-insensitive")
    void existsShouldBeCaseInsensitive() {
        assertTrue(BaseResponseEnum.exists("process_completed"));
        assertTrue(BaseResponseEnum.exists("Process_Completed"));
    }

    @Test
    @DisplayName("exists should return false for unknown code")
    void existsShouldReturnFalseForUnknownCode() {
        assertFalse(BaseResponseEnum.exists("DOES_NOT_EXIST"));
    }

    @Test
    @DisplayName("exists should return false for empty string")
    void existsShouldReturnFalseForEmptyString() {
        assertFalse(BaseResponseEnum.exists(""));
    }

    @Test
    @DisplayName("exists should throw NullPointerException when code is null")
    void existsShouldThrowNullPointerExceptionWhenCodeIsNull() {
        assertThrows(NullPointerException.class,
            () -> BaseResponseEnum.exists(null));
    }

    // ── Code uniqueness ────────────────────────────────────────────────────────

    @Test
    @DisplayName("All declared constants should have unique codes")
    void allConstantsShouldHaveUniqueCodes() {
        long distinctCodes = java.util.Arrays.stream(BaseResponseEnum.values())
            .map(BaseResponseEnum::getCode)
            .distinct()
            .count();

        assertEquals(BaseResponseEnum.values().length, distinctCodes,
            "Every constant must have a unique code");
    }
}

