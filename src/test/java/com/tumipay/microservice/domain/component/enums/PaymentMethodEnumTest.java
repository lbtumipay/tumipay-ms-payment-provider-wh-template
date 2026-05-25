package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentMethodEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("PaymentMethodEnum Unit Tests")
class PaymentMethodEnumTest {

    @Test
    @DisplayName("PaymentMethodEnum should have PSE value")
    void testPse() {
        assertNotNull(PaymentMethodEnum.PSE);
    }

    @Test
    @DisplayName("PaymentMethodEnum should have CARD value")
    void testCard() {
        assertNotNull(PaymentMethodEnum.CARD);
    }

    @Test
    @DisplayName("PaymentMethodEnum should have CASH value")
    void testCash() {
        assertNotNull(PaymentMethodEnum.CASH);
    }

    @Test
    @DisplayName("PaymentMethodEnum should have QR value")
    void testQr() {
        assertNotNull(PaymentMethodEnum.QR);
    }

    @Test
    @DisplayName("PaymentMethodEnum should have TRANSFIYA value")
    void testTransfiya() {
        assertNotNull(PaymentMethodEnum.TRANSFIYA);
    }

    @Test
    @DisplayName("PaymentMethodEnum should have BREB value")
    void testBreb() {
        assertNotNull(PaymentMethodEnum.BREB);
    }

    @Test
    @DisplayName("PaymentMethodEnum should have BANK_TRANSFER value")
    void testBankTransfer() {
        assertNotNull(PaymentMethodEnum.BANK_TRANSFER);
    }

    @Test
    @DisplayName("PaymentMethodEnum should have WALLET value")
    void testWallet() {
        assertNotNull(PaymentMethodEnum.WALLET);
    }

    @Test
    @DisplayName("PaymentMethodEnum should have exactly 8 values")
    void testEnumSize() {
        assertEquals(8, PaymentMethodEnum.values().length);
    }

    @Test
    @DisplayName("All PaymentMethodEnum values should be accessible by name")
    void testAllValuesAccessible() {
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            assertEquals(method, PaymentMethodEnum.valueOf(method.name()));
        }
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> PaymentMethodEnum.valueOf("INVALID"));
    }
}

