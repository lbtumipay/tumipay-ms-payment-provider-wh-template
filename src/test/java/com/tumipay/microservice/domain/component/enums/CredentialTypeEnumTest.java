package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CredentialTypeEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("CredentialTypeEnum Unit Tests")
class CredentialTypeEnumTest {

    @Test
    @DisplayName("CredentialTypeEnum should have TOKEN value")
    void testToken() {
        assertNotNull(CredentialTypeEnum.TOKEN);
        assertEquals("TOKEN", CredentialTypeEnum.TOKEN.name());
    }

    @Test
    @DisplayName("CredentialTypeEnum should have API_KEY value")
    void testApiKey() {
        assertNotNull(CredentialTypeEnum.API_KEY);
        assertEquals("API_KEY", CredentialTypeEnum.API_KEY.name());
    }

    @Test
    @DisplayName("CredentialTypeEnum should have exactly 2 values")
    void testEnumSize() {
        assertEquals(2, CredentialTypeEnum.values().length);
    }

    @Test
    @DisplayName("All CredentialTypeEnum values should be accessible by name")
    void testAllValuesAccessible() {
        for (CredentialTypeEnum type : CredentialTypeEnum.values()) {
            assertEquals(type, CredentialTypeEnum.valueOf(type.name()));
        }
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> CredentialTypeEnum.valueOf("INVALID"));
    }
}

