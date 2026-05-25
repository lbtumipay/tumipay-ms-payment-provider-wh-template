package com.tumipay.microservice.infrastructure.component.dto;

import com.tumipay.microservice.infrastructure.component.constant.BaseResponseConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseApiResponseTest
 * <p>
 * Unit tests for {@link BaseApiResponse}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 07/04/2026
 */
@DisplayName("BaseApiResponse Unit Tests")
class BaseApiResponseTest {

    @Test
    @DisplayName("getEmptySuccessfullyResponse - should return SUCCESS response with no data")
    void getEmptySuccessfullyResponse_shouldReturnSuccessWithNoData() {
        BaseApiResponse<?> response = BaseApiResponse.getEmptySuccessfullyResponse();

        assertNotNull(response);
        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_CODE, response.getCode());
        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_STATUS, response.getStatus());
        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_MESSAGE, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("success - should wrap data with SUCCESS envelope")
    void success_shouldWrapDataWithSuccessEnvelope() {
        String payload = "my-payload";
        BaseApiResponse<String> response = BaseApiResponse.success(payload);

        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_CODE, response.getCode());
        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_STATUS, response.getStatus());
        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_MESSAGE, response.getMessage());
        assertEquals(payload, response.getData());
    }

    @Test
    @DisplayName("success - should work with null payload")
    void success_shouldWorkWithNullPayload() {
        BaseApiResponse<String> response = BaseApiResponse.success(null);

        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_CODE, response.getCode());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("emptySuccess - should return SUCCESS response with null data")
    void emptySuccess_shouldReturnSuccessWithNullData() {
        BaseApiResponse<?> response = BaseApiResponse.emptySuccess();

        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_CODE, response.getCode());
        assertEquals(BaseResponseConstant.SUCCESS_RESPONSE_STATUS, response.getStatus());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("internalError - should return INTERNAL_SERVER_ERROR envelope with data")
    void internalError_shouldReturnInternalErrorEnvelopeWithData() {
        String errorDetail = "db connection lost";
        BaseApiResponse<String> response = BaseApiResponse.internalError(errorDetail);

        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_CODE, response.getCode());
        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_STATUS, response.getStatus());
        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_MESSAGE, response.getMessage());
        assertEquals(errorDetail, response.getData());
    }

    @Test
    @DisplayName("internalError - should work with null payload")
    void internalError_shouldWorkWithNullPayload() {
        BaseApiResponse<String> response = BaseApiResponse.internalError(null);

        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_CODE, response.getCode());
        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_STATUS, response.getStatus());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("emptyInternalError - should return INTERNAL_SERVER_ERROR envelope with null data")
    void emptyInternalError_shouldReturnInternalErrorWithNullData() {
        BaseApiResponse<String> response = BaseApiResponse.emptyInternalError();

        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_CODE, response.getCode());
        assertEquals(BaseResponseConstant.INTERNAL_ERROR_RESPONSE_STATUS, response.getStatus());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("businessError(code, message) - should return FAILED status without data")
    void businessError_codeAndMessage_shouldReturnFailedStatusWithNoData() {
        BaseApiResponse<Void> response = BaseApiResponse.businessError("BIZ-001", "business rule violated");

        assertEquals("BIZ-001", response.getCode());
        assertEquals(BaseResponseConstant.FAILED_RESPONSE_STATUS, response.getStatus());
        assertEquals("business rule violated", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("businessError(code, message, data) - should return FAILED status with data")
    void businessError_codeMessageAndData_shouldReturnFailedStatusWithData() {
        String payload = "extra-context";
        BaseApiResponse<String> response = BaseApiResponse.businessError("BIZ-002", "limit exceeded", payload);

        assertEquals("BIZ-002", response.getCode());
        assertEquals(BaseResponseConstant.FAILED_RESPONSE_STATUS, response.getStatus());
        assertEquals("limit exceeded", response.getMessage());
        assertEquals(payload, response.getData());
    }

    @Test
    @DisplayName("builder - should construct instance with explicit values")
    void builder_shouldConstructInstanceWithExplicitValues() {
        BaseApiResponse<Integer> response = BaseApiResponse.<Integer>builder()
            .code("CUSTOM-CODE")
            .status("CUSTOM-STATUS")
            .message("custom message")
            .data(42)
            .build();

        assertEquals("CUSTOM-CODE", response.getCode());
        assertEquals("CUSTOM-STATUS", response.getStatus());
        assertEquals("custom message", response.getMessage());
        assertEquals(42, response.getData());
    }

    @Test
    @DisplayName("noArgsConstructor + setters - should allow setting all fields")
    void noArgsConstructor_shouldAllowSettingAllFields() {
        BaseApiResponse<String> response = new BaseApiResponse<>();
        response.setCode("C1");
        response.setStatus("S1");
        response.setMessage("M1");
        response.setData("D1");

        assertEquals("C1", response.getCode());
        assertEquals("S1", response.getStatus());
        assertEquals("M1", response.getMessage());
        assertEquals("D1", response.getData());
    }

    @Test
    @DisplayName("allArgsConstructor - should set all fields")
    void allArgsConstructor_shouldSetAllFields() {
        BaseApiResponse<String> response = new BaseApiResponse<>("CODE", "STATUS", "MESSAGE", "DATA");

        assertEquals("CODE", response.getCode());
        assertEquals("STATUS", response.getStatus());
        assertEquals("MESSAGE", response.getMessage());
        assertEquals("DATA", response.getData());
    }

    @Test
    @DisplayName("equals - two instances with same fields should be equal")
    void equals_sameFields_shouldBeEqual() {
        BaseApiResponse<String> r1 = BaseApiResponse.<String>builder()
            .code("C").status("S").message("M").data("D").build();
        BaseApiResponse<String> r2 = BaseApiResponse.<String>builder()
            .code("C").status("S").message("M").data("D").build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    @DisplayName("equals - two instances with different codes should not be equal")
    void equals_differentCode_shouldNotBeEqual() {
        BaseApiResponse<String> r1 = BaseApiResponse.<String>builder().code("C1").build();
        BaseApiResponse<String> r2 = BaseApiResponse.<String>builder().code("C2").build();

        assertNotEquals(r1, r2);
    }

    @Test
    @DisplayName("toString - should include code and status")
    void toString_shouldIncludeRelevantFields() {
        BaseApiResponse<String> response = BaseApiResponse.<String>builder()
            .code("TST")
            .status("SUCCESS")
            .build();

        String result = response.toString();
        assertTrue(result.contains("TST"));
        assertTrue(result.contains("SUCCESS"));
    }

    @Test
    @DisplayName("should implement Serializable")
    void shouldImplementSerializable() {
        assertInstanceOf(java.io.Serializable.class, BaseApiResponse.emptySuccess());
    }
}

