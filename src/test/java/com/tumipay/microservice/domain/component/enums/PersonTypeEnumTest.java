package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PersonTypeEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("PersonTypeEnum Unit Tests")
class PersonTypeEnumTest {

    @Test
    @DisplayName("PersonTypeEnum should have INDIVIDUAL value")
    void testIndividual() {
        assertNotNull(PersonTypeEnum.INDIVIDUAL);
        assertEquals("INDIVIDUAL", PersonTypeEnum.INDIVIDUAL.name());
    }

    @Test
    @DisplayName("PersonTypeEnum should have COMPANY value")
    void testCompany() {
        assertNotNull(PersonTypeEnum.COMPANY);
        assertEquals("COMPANY", PersonTypeEnum.COMPANY.name());
    }

    @Test
    @DisplayName("PersonTypeEnum should have exactly 2 values")
    void testEnumSize() {
        assertEquals(2, PersonTypeEnum.values().length);
    }

    @Test
    @DisplayName("All PersonTypeEnum values should be accessible by name")
    void testAllValuesAccessible() {
        for (PersonTypeEnum type : PersonTypeEnum.values()) {
            assertEquals(type, PersonTypeEnum.valueOf(type.name()));
        }
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> PersonTypeEnum.valueOf("INVALID"));
    }
}

