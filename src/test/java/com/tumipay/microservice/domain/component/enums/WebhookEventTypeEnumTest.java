package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebhookEventTypeEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("WebhookEventTypeEnum Unit Tests")
class WebhookEventTypeEnumTest {

    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYIN_TRANSACTION_APPROVED value")
    void testPayinTransactionApproved() {
        assertNotNull(WebhookEventTypeEnum.PAYIN_TRANSACTION_APPROVED);
    }

    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYIN_TRANSACTION_REJECTED value")
    void testPayinTransactionRejected() {
        assertNotNull(WebhookEventTypeEnum.PAYIN_TRANSACTION_REJECTED);
    }


    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYIN_TRANSACTION_ERROR value")
    void testPayinTransactionError() {
        assertNotNull(WebhookEventTypeEnum.PAYIN_TRANSACTION_ERROR);
    }

    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYIN_TRANSACTION_CANCELLED value")
    void testPayinTransactionCancelled() {
        assertNotNull(WebhookEventTypeEnum.PAYIN_TRANSACTION_CANCELLED);
    }

    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYOUT_TRANSACTION_APPROVED value")
    void testPayoutTransactionApproved() {
        assertNotNull(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED);
    }

    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYOUT_TRANSACTION_REJECTED value")
    void testPayoutTransactionRejected() {
        assertNotNull(WebhookEventTypeEnum.PAYOUT_TRANSACTION_REJECTED);
    }

    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYOUT_TRANSACTION_PENDING value")
    void testPayoutTransactionPending() {
        assertNotNull(WebhookEventTypeEnum.PAYOUT_TRANSACTION_PENDING);
    }

    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYOUT_TRANSACTION_EXPIRED value")
    void testPayoutTransactionExpired() {
        assertNotNull(WebhookEventTypeEnum.PAYOUT_TRANSACTION_EXPIRED);
    }

    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYOUT_TRANSACTION_ERROR value")
    void testPayoutTransactionError() {
        assertNotNull(WebhookEventTypeEnum.PAYOUT_TRANSACTION_ERROR);
    }

    @Test
    @DisplayName("WebhookEventTypeEnum should have PAYOUT_TRANSACTION_CANCELLED value")
    void testPayoutTransactionCancelled() {
        assertNotNull(WebhookEventTypeEnum.PAYOUT_TRANSACTION_CANCELLED);
    }

    @Test
    @DisplayName("WebhookEventTypeEnum should have UNKNOWN_EVENT value")
    void testUnknownEvent() {
        assertNotNull(WebhookEventTypeEnum.UNKNOWN_EVENT);
    }

    @Test
    @DisplayName("All WebhookEventTypeEnum values should be accessible by name")
    void testAllValuesAccessible() {
        for (WebhookEventTypeEnum type : WebhookEventTypeEnum.values()) {
            assertEquals(type, WebhookEventTypeEnum.valueOf(type.name()));
        }
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> WebhookEventTypeEnum.valueOf("INVALID"));
    }
}

