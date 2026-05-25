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
     * Enum representing an error during HTTP integration with the provider.
     */
    HTTP_INTEGRATION_ERROR("HTTP_INTEGRATION_ERROR", "Error during HTTP integration with provider"),

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
     * Enum representing a resource not found error code.
     */
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource not found"),

    /**
     * Enum representing an account not found error code.
     * Returns HTTP 404 to the caller when the requested account does not exist at the provider.
     */
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "Account not found"),

    /**
     * Enum representing an invalid bank code error.
     */
    INVALID_BANK_CODE("INVALID_BANK_CODE", "Invalid bank code format"),

    /**
     * Enum representing an invalid beneficiary information error.
     */
    INVALID_BENEFICIARY("INVALID_BENEFICIARY", "Invalid beneficiary information"),

    /**
     * Enum representing an invalid document number error code.
     */
    INVALID_DOCUMENT_NUMBER("INVALID_DOCUMENT_NUMBER", "Invalid document number format"),

    /**
     * Enum representing a missing identifier error code when searching for a transaction.
     */
    MISSING_IDENTIFIER("MISSING_IDENTIFIER", "At least one identifier must be provided"),

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
     * Enum representing an unsupported payment method error code.
     */
    UNSUPPORTED_PAYMENT_METHOD("UNSUPPORTED_PAYMENT_METHOD", "The specified payment method is not supported"),

    /**
     * Enum representing an authorization error
     */
    PAYMENT_PROVIDER_AUTHORIZATION_ERROR("PAYMENT_PROVIDER_AUTHORIZATION_ERROR", "The specified payment provider is not implemented"),

    /**
     * Enum representing an unsupported payment provider error code.
     */
    PAYMENT_PROVIDER_NOT_IMPLEMENTED("PAYMENT_PROVIDER_NOT_IMPLEMENTED", "The specified payment provider is not implemented"),

    /**
     * Enum representing a credential retrieval failure error code.
     */
    PROVIDER_CONFIGURATION_FAILED("PROVIDER_CONFIGURATION_FAILED", "Failed to load adapter provider configuration"),

    /**
     * Enum representing a credential retrieval failure error code.
     */
    PROVIDER_CREDENTIAL_FAILED("PROVIDER_CREDENTIAL_FAILED", "Failed to retrieve credentials for the provider"),

    /**
     * Enum representing a provider integration business error code.
     */
    PROVIDER_BUSINESS_VALIDATION_ERROR("PROVIDER_BUSINESS_VALIDATION_ERROR", "A business error occurred during the provider integration"),

    /**
     * Enum representing a provider communication error code.
     */
    PROVIDER_COMMUNICATION_ERROR("PROVIDER_COMMUNICATION_ERROR", "An error occurred while communicating with the provider"),

    /**
     * Enum representing an error code for issues with the provider during transaction processing.
     */
    PROVIDER_TRANSACTION_ERROR("PROVIDER_TRANSACTION_ERROR", "An error occurred while processing the transaction with the provider"),

    /**
     * Enum representing an error code for issues with the provider during logging of the integration.
     */
    PROVIDER_INTEGRATION_LOG_ERROR("PROVIDER_INTEGRATION_LOG_ERROR", "An error occurred while logging the provider integration"),

    /**
     * Enum representing a generic internal server error code.
     */
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error");


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
