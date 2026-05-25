package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProviderWebhookEventStatusEnumTest
 * <p>
 * ProviderWebhookEventStatusEnumTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("ProviderWebhookEventStatusEnum Unit Tests")
class ProviderWebhookEventStatusEnumTest {

    @Test
    @DisplayName("All constants should expose non-empty code and description")
    void allConstantsShouldExposeNonEmptyCodeAndDescription() {
        for (ProviderWebhookEventStatusEnum status : ProviderWebhookEventStatusEnum.values()) {
            assertNotNull(status.getCode(), "Code should not be null for: " + status.name());
            assertFalse(status.getCode().isBlank(), "Code should not be blank for: " + status.name());
            assertNotNull(status.getDescription(), "Description should not be null for: " + status.name());
            assertFalse(status.getDescription().isBlank(), "Description should not be blank for: " + status.name());
        }
    }

    @Test
    @DisplayName("toString should return code")
    void toStringShouldReturnCode() {
        assertEquals("money_movements.status.initiated", ProviderWebhookEventStatusEnum.MONEY_MOVEMENTS_STATUS_INITIATED.toString());
        assertEquals("UNKNOWN_STATUS", ProviderWebhookEventStatusEnum.UNKNOWN_STATUS.toString());
    }

    @Test
    @DisplayName("getProviderWebhookEventStatusByCode should resolve UNKNOWN_STATUS")
    void getProviderWebhookEventStatusByCodeShouldResolveUnknownStatus() {
        assertEquals(
            ProviderWebhookEventStatusEnum.UNKNOWN_STATUS,
            ProviderWebhookEventStatusEnum.getProviderWebhookEventStatusByCode("UNKNOWN_STATUS")
        );
    }

    @Test
    @DisplayName("getProviderWebhookEventStatusByCode should return null for regular provider status code with current implementation")
    void getProviderWebhookEventStatusByCodeShouldReturnNullForRegularStatusCode() {
        assertNull(
            ProviderWebhookEventStatusEnum.getProviderWebhookEventStatusByCode("money_movements.status.completed")
        );
    }

    @Test
    @DisplayName("getProviderWebhookEventStatusByCode should return null for empty string and unknown code")
    void getProviderWebhookEventStatusByCodeShouldReturnNullForEmptyOrUnknownCode() {
        assertNull(ProviderWebhookEventStatusEnum.getProviderWebhookEventStatusByCode(""));
        assertNull(ProviderWebhookEventStatusEnum.getProviderWebhookEventStatusByCode("NOT_FOUND"));
    }

    @Test
    @DisplayName("getProviderWebhookEventStatusByCode should throw NullPointerException for null code")
    void getProviderWebhookEventStatusByCodeShouldThrowForNullCode() {
        assertThrows(
            NullPointerException.class,
            () -> ProviderWebhookEventStatusEnum.getProviderWebhookEventStatusByCode(null)
        );
    }

    @Test
    @DisplayName("exists should only return true for UNKNOWN_STATUS with current implementation")
    void existsShouldReflectCurrentLookupImplementation() {
        assertTrue(ProviderWebhookEventStatusEnum.exists("UNKNOWN_STATUS"));
        assertFalse(ProviderWebhookEventStatusEnum.exists("money_movements.status.processing"));
    }

    @Test
    @DisplayName("exists should return false for empty string and unknown code")
    void existsShouldReturnFalseForEmptyOrUnknownCode() {
        assertFalse(ProviderWebhookEventStatusEnum.exists(""));
        assertFalse(ProviderWebhookEventStatusEnum.exists("NOT_FOUND"));
    }

    @Test
    @DisplayName("exists should throw NullPointerException for null code")
    void existsShouldThrowForNullCode() {
        assertThrows(NullPointerException.class, () -> ProviderWebhookEventStatusEnum.exists(null));
    }
}