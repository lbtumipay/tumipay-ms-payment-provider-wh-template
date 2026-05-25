package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthSchemeEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("AuthSchemeEnum Unit Tests")
class AuthSchemeEnumTest {

    @Test
    @DisplayName("AuthSchemeEnum should have OAUTH2 value")
    void testOAuth2() {
        assertNotNull(AuthSchemeEnum.OAUTH2);
        assertEquals("OAUTH2", AuthSchemeEnum.OAUTH2.name());
    }

    @Test
    @DisplayName("AuthSchemeEnum should have API_KEY value")
    void testApiKey() {
        assertNotNull(AuthSchemeEnum.API_KEY);
        assertEquals("API_KEY", AuthSchemeEnum.API_KEY.name());
    }

    @Test
    @DisplayName("AuthSchemeEnum should have JWE value")
    void testJwe() {
        assertNotNull(AuthSchemeEnum.JWE);
        assertEquals("JWE", AuthSchemeEnum.JWE.name());
    }

    @Test
    @DisplayName("AuthSchemeEnum should have BASIC value")
    void testBasic() {
        assertNotNull(AuthSchemeEnum.BASIC);
        assertEquals("BASIC", AuthSchemeEnum.BASIC.name());
    }

    @Test
    @DisplayName("AuthSchemeEnum should have exactly 4 values")
    void testEnumSize() {
        assertEquals(4, AuthSchemeEnum.values().length);
    }

    @Test
    @DisplayName("All AuthSchemeEnum values should be accessible by name")
    void testAllValuesAccessible() {
        for (AuthSchemeEnum scheme : AuthSchemeEnum.values()) {
            assertEquals(scheme, AuthSchemeEnum.valueOf(scheme.name()));
        }
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> AuthSchemeEnum.valueOf("INVALID"));
    }
}

