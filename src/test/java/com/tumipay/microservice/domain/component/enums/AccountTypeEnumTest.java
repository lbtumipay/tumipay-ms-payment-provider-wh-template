package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountTypeEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("AccountTypeEnum Unit Tests")
class AccountTypeEnumTest {

    @Test
    @DisplayName("AccountTypeEnum should have SAVINGS value")
    void testSavingsValue() {
        assertNotNull(AccountTypeEnum.SAVINGS);
        assertEquals("SAVINGS", AccountTypeEnum.SAVINGS.name());
    }

    @Test
    @DisplayName("AccountTypeEnum should have CHECKING value")
    void testCheckingValue() {
        assertNotNull(AccountTypeEnum.CHECKING);
        assertEquals("CHECKING", AccountTypeEnum.CHECKING.name());
    }

    @Test
    @DisplayName("AccountTypeEnum should have exactly 2 values")
    void testEnumSize() {
        assertEquals(2, AccountTypeEnum.values().length);
    }

    @Test
    @DisplayName("AccountTypeEnum values should be accessible by ordinal")
    void testOrdinal() {
        AccountTypeEnum[] values = AccountTypeEnum.values();
        assertNotNull(values[0]);
        assertNotNull(values[1]);
    }

    @Test
    @DisplayName("AccountTypeEnum should be comparable")
    void testComparable() {
        assertTrue(AccountTypeEnum.SAVINGS.ordinal() < AccountTypeEnum.CHECKING.ordinal());
    }

    @Test
    @DisplayName("valueOf should retrieve SAVINGS")
    void testValueOf_Savings() {
        assertEquals(AccountTypeEnum.SAVINGS, AccountTypeEnum.valueOf("SAVINGS"));
    }

    @Test
    @DisplayName("valueOf should retrieve CHECKING")
    void testValueOf_Checking() {
        assertEquals(AccountTypeEnum.CHECKING, AccountTypeEnum.valueOf("CHECKING"));
    }
}

