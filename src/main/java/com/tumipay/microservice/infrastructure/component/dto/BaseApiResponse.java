package com.tumipay.microservice.infrastructure.component.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tumipay.microservice.infrastructure.component.constant.BaseResponseConstant;
import com.tumipay.microservice.infrastructure.component.enums.BaseResponseEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * BaseApiResponse
 * <p>
 * Generic API response wrapper used as the standard response contract
 * for all HTTP-based integrations.
 * <p>
 * This class defines a consistent envelope structure that includes:
 * <ul>
 *     <li>Response code</li>
 *     <li>Execution status</li>
 *     <li>Human-readable message</li>
 *     <li>Optional response payload</li>
 * </ul>
 * <p>
 * JSON field names are explicitly defined to guarantee contract stability
 * and interoperability across clients and SDKs.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT
 * STANDARDS AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND
 * COPYRIGHT LAWS.
 *
 * @param <T> Type of the response payload.
 *
 * @author TumiPay
 * @since 03/12/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
@JsonPropertyOrder({
    "code",
    "status",
    "message",
    "data"
})
public class BaseApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /**
     * Application-specific response code.
     */
    @JsonProperty("code")
    private String code;

    /**
     * High-level execution status (e.g. SUCCESS, FAILED).
     */
    @JsonProperty("status")
    private String status;

    /**
     * Human-readable message describing the response outcome.
     */
    @JsonProperty("message")
    private String message;

    /**
     * Optional response payload.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("data")
    private T data;

    /**
     * Builds a successful response without a payload.
     * @return empty successful {@link BaseApiResponse}
     */
    public static BaseApiResponse<?> getEmptySuccessfullyResponse() {

        final BaseResponseEnum successResponse = BaseResponseEnum.SUCCESS_RESPONSE;
        return BaseApiResponse.builder()
            .code(successResponse.getCode())
            .status(successResponse.getStatus())
            .message(successResponse.getMessage())
            .build();
    }

    /**
     * Builds a successful response with payload.
     * @param code business-specific response code
     * @param message business-specific response message
     * @param <T>  payload type
     * @return successful {@link BaseApiResponse} with data
     */
    public static <T> BaseApiResponse<T> businessError(String code, String message) {
        return BaseApiResponse.<T>builder()
            .code(code)
            .status(BaseResponseConstant.FAILED_RESPONSE_STATUS)
            .message(message)
            .build();
    }

    /**
     * Builds a successful response with payload.
     * @param code business-specific response code
     * @param message business-specific response message
     * @param <T>  payload type
     * @return successful {@link BaseApiResponse} with data
     */
    public static <T> BaseApiResponse<T> businessError(String code, String message, T data) {
        return BaseApiResponse.<T>builder()
            .code(code)
            .status(BaseResponseConstant.FAILED_RESPONSE_STATUS)
            .message(message)
            .data(data)
            .build();
    }

    /**
     * Builds a successful response with payload.
     * @param data response payload
     * @param <T>  payload type
     * @return successful {@link BaseApiResponse} with data
     */
    public static <T> BaseApiResponse<T> success(T data) {
        return buildResponse(BaseResponseEnum.SUCCESS_RESPONSE, data);
    }

    /**
     * Builds an internal error response without a payload.
     * @return error {@link BaseApiResponse}
     */
    public static BaseApiResponse<?> emptySuccess() {
        return buildResponse(BaseResponseEnum.SUCCESS_RESPONSE, null);
    }

    /**
     * Builds an internal error response with payload.
     * @param data error payload
     * @param <T>  payload type
     * @return error {@link BaseApiResponse} with data
     */
    public static <T> BaseApiResponse<T> internalError(T data) {
        return buildResponse(BaseResponseEnum.INTERNAL_ERROR_RESPONSE, data);
    }

    /**
     * Builds an internal error response without a payload.
     * @return error {@link BaseApiResponse}
     */
    public static <T> BaseApiResponse<T> emptyInternalError() {
        return buildResponse(BaseResponseEnum.INTERNAL_ERROR_RESPONSE, null);
    }

    /**
     * Internal builder method that centralizes response creation logic.
     * @return error {@link BaseApiResponse}
     */
    private static <T> BaseApiResponse<T> buildResponse(
        BaseResponseEnum responseEnum,
        T data
    ) {
        return BaseApiResponse.<T>builder()
            .code(responseEnum.getCode())
            .status(responseEnum.getStatus())
            .message(responseEnum.getMessage())
            .data(data)
            .build();
    }
}
