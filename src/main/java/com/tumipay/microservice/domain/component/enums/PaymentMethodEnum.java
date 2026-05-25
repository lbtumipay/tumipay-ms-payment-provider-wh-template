package com.tumipay.microservice.domain.component.enums;

/**
 * PaymentMethodEnum
 * <p>
 * PaymentMethodEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
public enum PaymentMethodEnum {


    /**
     * Enum representing the PSE (Electronic Services Payment) payment method.
     */
    PSE,

    /**
     * Enum representing a card payment method.
     */
    CARD,

    /**
     * Enum representing a cash payment method.
     */
    CASH,

    /**
     * Enum representing a QR code payment method.
     */
    QR,

    /**
     * Enum representing the Transfiya payment method.
     */
    TRANSFIYA,

    /**
     * Enum representing the BREB payment method.
     */
    BREB,

    /**
     * Enum representing a bank transfer payment method.
     */
    BANK_TRANSFER,

    /**
     * Enum representing a digital wallet payment method.
     */
    WALLET
}
