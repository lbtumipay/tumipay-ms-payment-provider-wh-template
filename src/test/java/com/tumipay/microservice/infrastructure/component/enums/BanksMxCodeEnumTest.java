package com.tumipay.microservice.infrastructure.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BanksMxCodeEnumTest
 * <p>
 * Unit tests for BanksMxCodeEnum.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/04/2026
 */
@DisplayName("BanksMxCodeEnum Tests")
class BanksMxCodeEnumTest {

    // ── Values and toString ──────────────────────────────────────────────────

    @Test
    @DisplayName("All enum constants should have non-null and non-empty code")
    void allConstantsShouldHaveNonEmptyCode() {
        for (BanksMxCodeEnum bank : BanksMxCodeEnum.values()) {
            assertNotNull(bank.getCode(), "Code should not be null for: " + bank.name());
            assertFalse(bank.getCode().isBlank(), "Code should not be blank for: " + bank.name());
        }
    }

    @Test
    @DisplayName("toString should return the code")
    void toStringShouldReturnCode() {
        assertEquals("BANAMEX", BanksMxCodeEnum.BANAMEX.toString());
        assertEquals("BBVA_MEXICO", BanksMxCodeEnum.BBVA_MEXICO.toString());
        assertEquals("STP", BanksMxCodeEnum.STP.toString());
    }

    // ── getBankByCode ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBankByCode should return correct enum for exact code")
    void getBankByCodeShouldReturnCorrectEnum() {
        assertEquals(BanksMxCodeEnum.BANAMEX, BanksMxCodeEnum.getBankByCode("BANAMEX"));
        assertEquals(BanksMxCodeEnum.BANORTE, BanksMxCodeEnum.getBankByCode("BANORTE"));
        assertEquals(BanksMxCodeEnum.MERCADO_PAGO, BanksMxCodeEnum.getBankByCode("MERCADO_PAGO"));
    }

    @Test
    @DisplayName("getBankByCode should be case-insensitive")
    void getBankByCodeShouldBeCaseInsensitive() {
        assertEquals(BanksMxCodeEnum.BANAMEX, BanksMxCodeEnum.getBankByCode("banamex"));
        assertEquals(BanksMxCodeEnum.BBVA_MEXICO, BanksMxCodeEnum.getBankByCode("bbva_mexico"));
    }

    @Test
    @DisplayName("getBankByCode should return null for unknown code")
    void getBankByCodeShouldReturnNullForUnknownCode() {
        assertNull(BanksMxCodeEnum.getBankByCode("UNKNOWN_BANK"));
    }

    @Test
    @DisplayName("getBankByCode should return null for empty string")
    void getBankByCodeShouldReturnNullForEmptyString() {
        assertNull(BanksMxCodeEnum.getBankByCode(""));
    }

    // ── exists ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("exists should return true for known code")
    void existsShouldReturnTrueForKnownCode() {
        assertTrue(BanksMxCodeEnum.exists("BANAMEX"));
        assertTrue(BanksMxCodeEnum.exists("STP"));
    }

    @Test
    @DisplayName("exists should be case-insensitive")
    void existsShouldBeCaseInsensitive() {
        assertTrue(BanksMxCodeEnum.exists("banamex"));
        assertTrue(BanksMxCodeEnum.exists("Banorte"));
    }

    @Test
    @DisplayName("exists should return false for unknown code")
    void existsShouldReturnFalseForUnknownCode() {
        assertFalse(BanksMxCodeEnum.exists("UNKNOWN_BANK"));
    }

    @Test
    @DisplayName("exists should return false for empty string")
    void existsShouldReturnFalseForEmptyString() {
        assertFalse(BanksMxCodeEnum.exists(""));
    }

    @Test
    @DisplayName("exists should return false for null")
    void existsShouldReturnFalseForNull() {
        assertFalse(BanksMxCodeEnum.exists(null));
    }

    // ── Completeness ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("All values should be findable by their own code")
    void allValuesShouldBeFoundByTheirCode() {
        for (BanksMxCodeEnum bank : BanksMxCodeEnum.values()) {
            assertTrue(BanksMxCodeEnum.exists(bank.getCode()),
                "exists() failed for: " + bank.name());
            assertEquals(bank, BanksMxCodeEnum.getBankByCode(bank.getCode()),
                "getBankByCode() failed for: " + bank.name());
        }
    }
}

