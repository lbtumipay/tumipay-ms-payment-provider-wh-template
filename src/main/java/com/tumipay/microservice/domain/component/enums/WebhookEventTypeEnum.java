package com.tumipay.microservice.domain.component.enums;

/**
 * WebhookEventTypeEnum
 * <p>
 * WebhookEventTypeEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
public enum WebhookEventTypeEnum {

    /**
     * Enum representing a successfully approved pay-in transaction webhook event.
     */
    PAYIN_TRANSACTION_APPROVED,

    /**
     * Enum representing a rejected pay-in transaction webhook event.
     */
    PAYIN_TRANSACTION_REJECTED,

    /**
     * Enum representing a technical error in a pay-in transaction webhook event.
     */
    PAYIN_TRANSACTION_ERROR,

    /**
     * Enum representing a cancelled pay-in transaction webhook event.
     */
    PAYIN_TRANSACTION_CANCELLED,

    /**
     * Enum representing a successfully approved payout transaction webhook event.
     */
    PAYOUT_TRANSACTION_APPROVED,

    /**
     * Enum representing a rejected payout transaction webhook event.
     */
    PAYOUT_TRANSACTION_REJECTED,

    /**
     * Enum representing a pending payout transaction webhook event.
     */
    PAYOUT_TRANSACTION_PENDING,

    /**
     * Enum representing an expired payout transaction webhook event.
     */
    PAYOUT_TRANSACTION_EXPIRED,

    /**
     * Enum representing a technical error in a payout transaction webhook event.
     */
    PAYOUT_TRANSACTION_ERROR,

    /**
     * Enum representing a cancelled payout transaction webhook event.
     */
    PAYOUT_TRANSACTION_CANCELLED,

    /**
     * Enum representing an unknown or unrecognized webhook event type.
     */
    UNKNOWN_EVENT
}
