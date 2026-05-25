package com.tumipay.microservice.domain.component.enums;

import com.tumipay.microservice.shared.enums.BaseOperationStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OperationStatusEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("OperationStatusEnum Unit Tests")
class OperationStatusEnumTest {

    @Test
    @DisplayName("OperationStatusEnum should have SUCCESS value")
    void testSuccess() {
        assertNotNull(BaseOperationStatusEnum.SUCCESS);
        assertEquals("SUCCESS", BaseOperationStatusEnum.SUCCESS.name());
    }

    @Test
    @DisplayName("OperationStatusEnum should have FAILED value")
    void testFailed() {
        assertNotNull(BaseOperationStatusEnum.FAILED);
        assertEquals("FAILED", BaseOperationStatusEnum.FAILED.name());
    }

    @Test
    @DisplayName("OperationStatusEnum should have exactly 2 values")
    void testEnumSize() {
        assertEquals(2, BaseOperationStatusEnum.values().length);
    }

    @Test
    @DisplayName("All OperationStatusEnum values should be accessible by name")
    void testAllValuesAccessible() {
        for (BaseOperationStatusEnum status : BaseOperationStatusEnum.values()) {
            assertEquals(status, BaseOperationStatusEnum.valueOf(status.name()));
        }
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> BaseOperationStatusEnum.valueOf("INVALID"));
    }
}

