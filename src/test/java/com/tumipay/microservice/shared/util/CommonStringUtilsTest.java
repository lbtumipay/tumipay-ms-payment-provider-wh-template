package com.tumipay.microservice.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonStringUtilsTest
 * <p>
 * CommonStringUtilsTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 23/04/2026
 */
@DisplayName("CommonStringUtils Unit Tests")
class CommonStringUtilsTest {

    @Test
    @DisplayName("Should validate blank and not blank values")
    void shouldValidateBlankAndNotBlankValues() {
        assertTrue(CommonStringUtils.isBlank(null));
        assertTrue(CommonStringUtils.isBlank("   "));
        assertFalse(CommonStringUtils.isBlank("abc"));

        assertFalse(CommonStringUtils.isNotBlank(null));
        assertFalse(CommonStringUtils.isNotBlank("  \t"));
        assertTrue(CommonStringUtils.isNotBlank("abc"));
    }

    @Test
    @DisplayName("Should resolve default value only for empty values")
    void shouldResolveDefaultValueOnlyForEmptyValues() {
        assertEquals("default", CommonStringUtils.defaultIfEmpty(null, "default"));
        assertEquals("default", CommonStringUtils.defaultIfEmpty("", "default"));
        assertEquals(" ", CommonStringUtils.defaultIfEmpty(" ", "default"));
        assertEquals("value", CommonStringUtils.defaultIfEmpty("value", "default"));
    }

    @Test
    @DisplayName("Should validate empty values")
    void shouldValidateEmptyValues() {
        assertTrue(CommonStringUtils.isEmpty(null));
        assertTrue(CommonStringUtils.isEmpty(""));
        assertFalse(CommonStringUtils.isEmpty(" "));
        assertFalse(CommonStringUtils.isEmpty("abc"));
    }
}