package com.tumipay.microservice.shared.enums;

import lombok.Getter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * BaseErrorCodeEnum
 * <p>
 * BaseErrorCodeEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 27/12/2025
 */
@Getter
public enum BaseErrorCodeEnum {

    /**
     * Enum representing a validation error code.
     */
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation error"),

    /**
     * Enum representing a transaction not found error code.
     */
    TRANSACTION_NOT_FOUND("TRANSACTION_NOT_FOUND", "Transaction not found"),

    /**
     * Enum representing a duplicate transaction error code.
     */
    DUPLICATE_TRANSACTION("DUPLICATE_TRANSACTION", "Transaction with this client_transaction_id already exists"),

    /**
     * Enum representing an invalid currency code error.
     */
    INVALID_CURRENCY_CODE("INVALID_CURRENCY_CODE", "Invalid currency code format"),

    /**
     * Enum representing an invalid country code error.
     */
    INVALID_COUNTRY_CODE("INVALID_COUNTRY_CODE", "Invalid country code format"),

    /**
     * Enum representing an invalid email format error.
     */
    INVALID_EMAIL("INVALID_EMAIL", "Invalid email format"),

    /**
     * Enum representing an invalid amount error.
     */
    INVALID_AMOUNT("INVALID_AMOUNT", "Amount must be greater than zero"),

    /**
     * Enum representing a database operation error code.
     */
    DATABASE_ERROR("DATABASE_ERROR", "Database operation failed"),

    /**
     * Enum representing a cache operation error code.
     */
    CACHE_ERROR("CACHE_ERROR", "Cache operation failed"),

    /**
     * Enum representing a duplicate webhook event error code.
     */
    DUPLICATE_WEBHOOK_EVENT("DUPLICATE_WEBHOOK_EVENT", "A webhook event with this idempotency key has already been processed"),

    /**
     * Enum representing a webhook processing error code.
     */
    WEBHOOK_PROCESSING_ERROR("WEBHOOK_PROCESSING_ERROR", "An error occurred while processing the webhook event"),

    /**
     * Enum representing a generic internal server error code.
     */
    INTERNAL_ERROR("999", "Internal server error");

    private final String code;
    private final String message;

    private static final Map<String, BaseErrorCodeEnum> baseErrorCodeMap = new HashMap<>();

    static {
        for (final BaseErrorCodeEnum documentTypeEnum : EnumSet.allOf(BaseErrorCodeEnum.class)) {
            baseErrorCodeMap.put(documentTypeEnum.getCode(), documentTypeEnum);
        }
    }

    BaseErrorCodeEnum(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return code;
    }

    public static BaseErrorCodeEnum getResponseByCode(final String code) {
        return !code.isEmpty() ? baseErrorCodeMap.get(code.toUpperCase(Locale.ROOT)) : null;
    }

    public static boolean exists(final String code) {
        return !code.isEmpty() && (baseErrorCodeMap.containsKey(code.toUpperCase(Locale.ROOT)));
    }
}
