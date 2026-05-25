package com.tumipay.microservice.domain.component.enums;

/**
 * TransactionStatusEnum
 * <p>
 * TransactionStatusEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
public enum TransactionStatusEnum {

    /**
     * Enum representing a pending transaction status.
     */
    PENDING,

    /**
     * Enum representing an approved transaction status.
     */
    APPROVED,

    /**
     * Enum representing a rejected transaction status.
     */
    REJECTED,

    /**
     * Enum representing an expired transaction status.
     */
    EXPIRED,

    /**
     * Enum representing a transaction status with a technical error.
     */
    ERROR,

    /**
     * Enum representing a cancelled transaction status.
     */
    CANCELLED
}
