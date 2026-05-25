package com.tumipay.microservice.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CommonIntegerUtilsTest2
 * <p>
 * CommonIntegerUtilsTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("CommonIntegerUtils Unit Tests")
class CommonIntegerUtilsTest {

    @Test
    @DisplayName("Should return true for positive numeric values")
    void shouldReturnTrueForPositiveNumericValues() {
        assertTrue(CommonIntegerUtils.isPositive(1));
        assertTrue(CommonIntegerUtils.isPositive(10L));
    }

    @Test
    @DisplayName("Should return false for null zero and negative values")
    void shouldReturnFalseForNullZeroAndNegativeValues() {
        assertFalse(CommonIntegerUtils.isPositive(null));
        assertFalse(CommonIntegerUtils.isPositive(0));
        assertFalse(CommonIntegerUtils.isPositive(-1));
    }
}