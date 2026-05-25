package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionTypeEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("TransactionTypeEnum Unit Tests")
class TransactionTypeEnumTest {

    @Test
    @DisplayName("TransactionTypeEnum should have PAYIN_TRANSACTION value")
    void testPayinTransaction() {
        assertNotNull(TransactionTypeEnum.PAYIN_TRANSACTION);
        assertEquals("PAYIN_TRANSACTION", TransactionTypeEnum.PAYIN_TRANSACTION.name());
    }

    @Test
    @DisplayName("TransactionTypeEnum should have PAYOUT_TRANSACTION value")
    void testPayoutTransaction() {
        assertNotNull(TransactionTypeEnum.PAYOUT_TRANSACTION);
        assertEquals("PAYOUT_TRANSACTION", TransactionTypeEnum.PAYOUT_TRANSACTION.name());
    }

    @Test
    @DisplayName("TransactionTypeEnum should have exactly 2 values")
    void testEnumSize() {
        assertEquals(2, TransactionTypeEnum.values().length);
    }

    @Test
    @DisplayName("All TransactionTypeEnum values should be accessible by name")
    void testAllValuesAccessible() {
        for (TransactionTypeEnum type : TransactionTypeEnum.values()) {
            assertEquals(type, TransactionTypeEnum.valueOf(type.name()));
        }
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> TransactionTypeEnum.valueOf("INVALID"));
    }
}

