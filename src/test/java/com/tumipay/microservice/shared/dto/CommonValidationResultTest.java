package com.tumipay.microservice.shared.dto;

import com.tumipay.microservice.shared.enums.BaseOperationStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonValidationResultTest
 * <p>
 * CommonValidationResultTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("CommonValidationResult Unit Tests")
class CommonValidationResultTest {

    @Test
    @DisplayName("success - should return SUCCESS status")
    void success_shouldReturnSuccessStatus() {
        CommonValidationResult result = CommonValidationResult.success();

        assertNotNull(result);
        assertEquals(BaseOperationStatusEnum.SUCCESS, result.getStatus());
    }

    @Test
    @DisplayName("success - isSuccess should return true")
    void success_isSuccess_shouldReturnTrue() {
        assertTrue(CommonValidationResult.success().isSuccess());
    }

    @Test
    @DisplayName("success - isFailed should return false")
    void success_isFailed_shouldReturnFalse() {
        assertFalse(CommonValidationResult.success().isFailed());
    }

    @Test
    @DisplayName("success - errorMessage should be null")
    void success_errorMessage_shouldBeNull() {
        assertNull(CommonValidationResult.success().getErrorMessage());
    }

    @Test
    @DisplayName("success - errors list should be empty by default")
    void success_errors_shouldBeEmptyByDefault() {
        List<String> errors = CommonValidationResult.success().getErrors();

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("failure(message) - should return FAILED status")
    void failure_message_shouldReturnFailedStatus() {
        CommonValidationResult result = CommonValidationResult.failure("something went wrong");

        assertEquals(BaseOperationStatusEnum.FAILED, result.getStatus());
    }

    @Test
    @DisplayName("failure(message) - isFailed should return true")
    void failure_message_isFailed_shouldReturnTrue() {
        assertTrue(CommonValidationResult.failure("error").isFailed());
    }

    @Test
    @DisplayName("failure(message) - isSuccess should return false")
    void failure_message_isSuccess_shouldReturnFalse() {
        assertFalse(CommonValidationResult.failure("error").isSuccess());
    }

    @Test
    @DisplayName("failure(message) - errorMessage should match provided value")
    void failure_message_errorMessage_shouldMatchProvided() {
        String msg = "validation error";

        assertEquals(msg, CommonValidationResult.failure(msg).getErrorMessage());
    }

    @Test
    @DisplayName("failure(message) - errors list should be empty by default")
    void failure_message_errors_shouldBeEmptyByDefault() {
        List<String> errors = CommonValidationResult.failure("error").getErrors();

        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("failure(message, errors) - should return FAILED with detail list")
    void failure_messageAndErrors_shouldReturnFailedStatusWithDetailList() {
        List<String> errorList = List.of(
            "field amount required",
            "field currency invalid"
        );

        CommonValidationResult result =
            CommonValidationResult.failure("multi-error", errorList);

        assertEquals(BaseOperationStatusEnum.FAILED, result.getStatus());
        assertEquals("multi-error", result.getErrorMessage());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().contains("field amount required"));
        assertNotNull(result.getValidationErrors());
        assertTrue(result.getValidationErrors().isEmpty());
    }

    @Test
    @DisplayName("failure(errorCode, errorMessage) - should return FAILED status and set error code")
    void failure_errorCodeAndMessage_shouldReturnFailedStatusWithErrorCode() {
        CommonValidationResult result = CommonValidationResult.failure("ERR-001", "validation error");

        assertEquals(BaseOperationStatusEnum.FAILED, result.getStatus());
        assertEquals("ERR-001", result.getErrorCode());
        assertEquals("validation error", result.getErrorMessage());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
        assertNotNull(result.getValidationErrors());
        assertTrue(result.getValidationErrors().isEmpty());
    }

    @Test
    @DisplayName("failure(message, errors) - isFailed should return true")
    void failure_messageAndErrors_isFailed_shouldReturnTrue() {
        assertTrue(
            CommonValidationResult.failure("e", List.of("detail")).isFailed()
        );
    }

    @Test
    @DisplayName("failure(message, errors) - isSuccess should return false")
    void failure_messageAndErrors_isSuccess_shouldReturnFalse() {
        assertFalse(
            CommonValidationResult.failure("e", List.of("detail")).isSuccess()
        );
    }

    @Test
    @DisplayName("failure(message, errors) - should work with empty errors list")
    void failure_messageAndErrors_shouldWorkWithEmptyErrorsList() {
        CommonValidationResult result =
            CommonValidationResult.failure(
                "no-details",
                Collections.emptyList()
            );

        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("builder - should construct instance with explicit values")
    void builder_shouldConstructInstanceWithExplicitValues() {
        List<String> errors = List.of("e1", "e2");

        CommonValidationResult result = CommonValidationResult.builder()
            .status(BaseOperationStatusEnum.FAILED)
            .errorMessage("builder error")
            .errors(errors)
            .build();

        assertEquals(BaseOperationStatusEnum.FAILED, result.getStatus());
        assertEquals("builder error", result.getErrorMessage());
        assertEquals(errors, result.getErrors());
    }

    @Test
    @DisplayName("noArgsConstructor - should create instance with null status")
    void noArgsConstructor_shouldCreateInstanceWithNullStatus() {
        CommonValidationResult result = new CommonValidationResult();

        assertNotNull(result);
        assertNull(result.getStatus());
        assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("isSuccess - should return false when status is FAILED")
    void isSuccess_shouldReturnFalseWhenStatusIsFailed() {
        assertFalse(
            CommonValidationResult.builder()
                .status(BaseOperationStatusEnum.FAILED)
                .build()
                .isSuccess()
        );
    }

    @Test
    @DisplayName("isFailed - should return false when status is SUCCESS")
    void isFailed_shouldReturnFalseWhenStatusIsSuccess() {
        assertFalse(
            CommonValidationResult.builder()
                .status(BaseOperationStatusEnum.SUCCESS)
                .build()
                .isFailed()
        );
    }

    @Test
    @DisplayName("isSuccess/isFailed - should return false when status is null")
    void statusChecks_shouldReturnFalseWhenStatusIsNull() {
        CommonValidationResult result = new CommonValidationResult();

        assertFalse(result.isSuccess());
        assertFalse(result.isFailed());
    }

    @Test
    @DisplayName("equals - two success instances should be equal")
    void equals_sameData_shouldBeEqual() {
        CommonValidationResult r1 = CommonValidationResult.success();
        CommonValidationResult r2 = CommonValidationResult.success();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    @DisplayName("equals - success and failure should not be equal")
    void equals_differentStatus_shouldNotBeEqual() {
        assertNotEquals(
            CommonValidationResult.success(),
            CommonValidationResult.failure("error")
        );
    }

    @Test
    @DisplayName("toString - should include status value")
    void toString_shouldIncludeStatus() {
        assertTrue(
            CommonValidationResult.success()
                .toString()
                .contains("SUCCESS")
        );
    }

    @Test
    @DisplayName("should implement Serializable")
    void shouldImplementSerializable() {
        assertInstanceOf(
            java.io.Serializable.class,
            CommonValidationResult.success()
        );
    }
}
