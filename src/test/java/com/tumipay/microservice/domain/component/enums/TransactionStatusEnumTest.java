package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionStatusEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("TransactionStatusEnum Unit Tests")
class TransactionStatusEnumTest {

    @Test
    @DisplayName("TransactionStatusEnum should have PENDING value")
    void testPending() {
        assertNotNull(TransactionStatusEnum.PENDING);
    }

    @Test
    @DisplayName("TransactionStatusEnum should have APPROVED value")
    void testApproved() {
        assertNotNull(TransactionStatusEnum.APPROVED);
    }

    @Test
    @DisplayName("TransactionStatusEnum should have REJECTED value")
    void testRejected() {
        assertNotNull(TransactionStatusEnum.REJECTED);
    }

    @Test
    @DisplayName("TransactionStatusEnum should have EXPIRED value")
    void testExpired() {
        assertNotNull(TransactionStatusEnum.EXPIRED);
    }

    @Test
    @DisplayName("TransactionStatusEnum should have ERROR value")
    void testError() {
        assertNotNull(TransactionStatusEnum.ERROR);
    }

    @Test
    @DisplayName("TransactionStatusEnum should have CANCELLED value")
    void testCancelled() {
        assertNotNull(TransactionStatusEnum.CANCELLED);
    }

    @Test
    @DisplayName("TransactionStatusEnum should have exactly 6 values")
    void testEnumSize() {
        assertEquals(6, TransactionStatusEnum.values().length);
    }

    @Test
    @DisplayName("All TransactionStatusEnum values should be accessible by name")
    void testAllValuesAccessible() {
        for (TransactionStatusEnum status : TransactionStatusEnum.values()) {
            assertEquals(status, TransactionStatusEnum.valueOf(status.name()));
        }
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> TransactionStatusEnum.valueOf("INVALID"));
    }
}

