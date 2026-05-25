package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProviderWebhookEventTypeEnumTest2
 * <p>
 * ProviderWebhookEventTypeEnumTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("ProviderWebhookEventTypeEnum Unit Tests")
class ProviderWebhookEventTypeEnumTest {

    @Test
    @DisplayName("All constants should expose non-empty code and description")
    void allConstantsShouldExposeNonEmptyCodeAndDescription() {
        for (ProviderWebhookEventTypeEnum type : ProviderWebhookEventTypeEnum.values()) {
            assertNotNull(type.getCode(), "Code should not be null for: " + type.name());
            assertFalse(type.getCode().isBlank(), "Code should not be blank for: " + type.name());
            assertNotNull(type.getDescription(), "Description should not be null for: " + type.name());
            assertFalse(type.getDescription().isBlank(), "Description should not be blank for: " + type.name());
        }
    }

    @Test
    @DisplayName("toString should return code")
    void toStringShouldReturnCode() {
        assertEquals("money_movements", ProviderWebhookEventTypeEnum.MONEY_MOVEMENTS.toString());
        assertEquals("UNKNOWN_EVENT", ProviderWebhookEventTypeEnum.UNKNOWN_EVENT.toString());
    }

    @Test
    @DisplayName("getProviderWebhookEventTypeByCode should resolve UNKNOWN_EVENT")
    void getProviderWebhookEventTypeByCodeShouldResolveUnknownEvent() {
        assertEquals(
            ProviderWebhookEventTypeEnum.UNKNOWN_EVENT,
            ProviderWebhookEventTypeEnum.getProviderWebhookEventTypeByCode("UNKNOWN_EVENT")
        );
    }

    @Test
    @DisplayName("getProviderWebhookEventTypeByCode should return null for regular provider event code with current implementation")
    void getProviderWebhookEventTypeByCodeShouldReturnNullForRegularEventCode() {
        assertNull(ProviderWebhookEventTypeEnum.getProviderWebhookEventTypeByCode("money_movements"));
    }

    @Test
    @DisplayName("getProviderWebhookEventTypeByCode should return null for empty string and unknown code")
    void getProviderWebhookEventTypeByCodeShouldReturnNullForEmptyOrUnknownCode() {
        assertNull(ProviderWebhookEventTypeEnum.getProviderWebhookEventTypeByCode(""));
        assertNull(ProviderWebhookEventTypeEnum.getProviderWebhookEventTypeByCode("NOT_FOUND"));
    }

    @Test
    @DisplayName("getProviderWebhookEventTypeByCode should throw NullPointerException for null code")
    void getProviderWebhookEventTypeByCodeShouldThrowForNullCode() {
        assertThrows(
            NullPointerException.class,
            () -> ProviderWebhookEventTypeEnum.getProviderWebhookEventTypeByCode(null)
        );
    }

    @Test
    @DisplayName("exists should only return true for UNKNOWN_EVENT with current implementation")
    void existsShouldReflectCurrentLookupImplementation() {
        assertTrue(ProviderWebhookEventTypeEnum.exists("UNKNOWN_EVENT"));
        assertFalse(ProviderWebhookEventTypeEnum.exists("money_movements"));
    }

    @Test
    @DisplayName("exists should return false for empty string and unknown code")
    void existsShouldReturnFalseForEmptyOrUnknownCode() {
        assertFalse(ProviderWebhookEventTypeEnum.exists(""));
        assertFalse(ProviderWebhookEventTypeEnum.exists("NOT_FOUND"));
    }

    @Test
    @DisplayName("exists should throw NullPointerException for null code")
    void existsShouldThrowForNullCode() {
        assertThrows(NullPointerException.class, () -> ProviderWebhookEventTypeEnum.exists(null));
    }
}
