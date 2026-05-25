package com.tumipay.microservice.shared.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for BaseOperationStatusEnum.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("BaseOperationStatusEnum Unit Tests")
class BaseOperationStatusEnumTest {

    @Test
    @DisplayName("Should contain exactly two constants: SUCCESS and FAILED")
    void shouldContainExactlyTwoConstants() {
        assertEquals(2, BaseOperationStatusEnum.values().length);
    }

    @Test
    @DisplayName("Should declare SUCCESS constant")
    void shouldDeclareSuccessConstant() {
        BaseOperationStatusEnum success = BaseOperationStatusEnum.SUCCESS;

        assertNotNull(success);
        assertEquals("SUCCESS", success.name());
    }

    @Test
    @DisplayName("Should declare FAILED constant")
    void shouldDeclareFailedConstant() {
        BaseOperationStatusEnum failed = BaseOperationStatusEnum.FAILED;

        assertNotNull(failed);
        assertEquals("FAILED", failed.name());
    }

    @Test
    @DisplayName("valueOf should resolve SUCCESS and FAILED by name")
    void valueOfShouldResolveByName() {
        assertEquals(BaseOperationStatusEnum.SUCCESS, BaseOperationStatusEnum.valueOf("SUCCESS"));
        assertEquals(BaseOperationStatusEnum.FAILED,  BaseOperationStatusEnum.valueOf("FAILED"));
    }

    @Test
    @DisplayName("SUCCESS and FAILED should have different ordinal positions")
    void successAndFailedShouldHaveDifferentOrdinals() {
        assertEquals(0, BaseOperationStatusEnum.SUCCESS.ordinal());
        assertEquals(1, BaseOperationStatusEnum.FAILED.ordinal());
    }

    @Test
    @DisplayName("toString should return the constant name")
    void toStringShouldReturnConstantName() {
        assertEquals("SUCCESS", BaseOperationStatusEnum.SUCCESS.toString());
        assertEquals("FAILED",  BaseOperationStatusEnum.FAILED.toString());
    }
}

