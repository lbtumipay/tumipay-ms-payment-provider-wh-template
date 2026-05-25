package com.tumipay.microservice.infrastructure.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BanksCoCodeEnumTest
 * <p>
 * Unit tests for BanksCoCodeEnum.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/04/2026
 */
@DisplayName("BanksCoCodeEnum Tests")
class BanksCoCodeEnumTest {

    // ── Values and toString ──────────────────────────────────────────────────

    @Test
    @DisplayName("All enum constants should have non-null and non-empty code")
    void allConstantsShouldHaveNonEmptyCode() {
        for (BanksCoCodeEnum bank : BanksCoCodeEnum.values()) {
            assertNotNull(bank.getCode(), "Code should not be null for: " + bank.name());
            assertFalse(bank.getCode().isBlank(), "Code should not be blank for: " + bank.name());
        }
    }

    @Test
    @DisplayName("toString should return the code")
    void toStringShouldReturnCode() {
        assertEquals("BANCOLOMBIA", BanksCoCodeEnum.BANCOLOMBIA.toString());
        assertEquals("NEQUI", BanksCoCodeEnum.NEQUI.toString());
        assertEquals("MOVII", BanksCoCodeEnum.MOVII.toString());
    }

    // ── getBankByCode ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBankByCode should return correct enum for exact code")
    void getBankByCodeShouldReturnCorrectEnum() {
        assertEquals(BanksCoCodeEnum.BANCOLOMBIA, BanksCoCodeEnum.getBankByCode("BANCOLOMBIA"));
        assertEquals(BanksCoCodeEnum.DAVIVIENDA, BanksCoCodeEnum.getBankByCode("DAVIVIENDA"));
        assertEquals(BanksCoCodeEnum.LULO_BANK, BanksCoCodeEnum.getBankByCode("LULO_BANK"));
    }

    @Test
    @DisplayName("getBankByCode should be case-insensitive")
    void getBankByCodeShouldBeCaseInsensitive() {
        assertEquals(BanksCoCodeEnum.BANCOLOMBIA, BanksCoCodeEnum.getBankByCode("bancolombia"));
        assertEquals(BanksCoCodeEnum.NEQUI, BanksCoCodeEnum.getBankByCode("nequi"));
    }

    @Test
    @DisplayName("getBankByCode should return null for unknown code")
    void getBankByCodeShouldReturnNullForUnknownCode() {
        assertNull(BanksCoCodeEnum.getBankByCode("UNKNOWN_BANK"));
    }

    @Test
    @DisplayName("getBankByCode should return null for empty string")
    void getBankByCodeShouldReturnNullForEmptyString() {
        assertNull(BanksCoCodeEnum.getBankByCode(""));
    }

    @Test
    @DisplayName("getBankByCode should return null for null")
    void getBankByCodeShouldReturnNullForNull() {
        assertNull(BanksCoCodeEnum.getBankByCode(null));
    }

    // ── exists ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("exists should return true for known code")
    void existsShouldReturnTrueForKnownCode() {
        assertTrue(BanksCoCodeEnum.exists("BANCOLOMBIA"));
        assertTrue(BanksCoCodeEnum.exists("DAVIPLATA"));
    }

    @Test
    @DisplayName("exists should be case-insensitive")
    void existsShouldBeCaseInsensitive() {
        assertTrue(BanksCoCodeEnum.exists("bancolombia"));
        assertTrue(BanksCoCodeEnum.exists("Davivienda"));
    }

    @Test
    @DisplayName("exists should return false for unknown code")
    void existsShouldReturnFalseForUnknownCode() {
        assertFalse(BanksCoCodeEnum.exists("UNKNOWN_BANK"));
    }

    @Test
    @DisplayName("exists should return false for empty string")
    void existsShouldReturnFalseForEmptyString() {
        assertFalse(BanksCoCodeEnum.exists(""));
    }

    @Test
    @DisplayName("exists should return false for null")
    void existsShouldReturnFalseForNull() {
        assertFalse(BanksCoCodeEnum.exists(null));
    }

    // ── Completeness ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("All values should be findable by their own code")
    void allValuesShouldBeFoundByTheirCode() {
        for (BanksCoCodeEnum bank : BanksCoCodeEnum.values()) {
            assertTrue(BanksCoCodeEnum.exists(bank.getCode()),
                "exists() failed for: " + bank.name());
            assertEquals(bank, BanksCoCodeEnum.getBankByCode(bank.getCode()),
                "getBankByCode() failed for: " + bank.name());
        }
    }
}

