package com.tumipay.microservice.domain.component.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebhookProcessingStatusEnum.
 * <p>
 * Tests enum values and basic functionality.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("WebhookProcessingStatusEnum Unit Tests")
class WebhookProcessingStatusEnumTest {

    @Test
    @DisplayName("WebhookProcessingStatusEnum should have RECEIVED value")
    void testReceived() {
        assertNotNull(WebhookProcessingStatusEnum.RECEIVED);
        assertEquals("RECEIVED", WebhookProcessingStatusEnum.RECEIVED.name());
    }

    @Test
    @DisplayName("WebhookProcessingStatusEnum should have VALIDATING value")
    void testValidating() {
        assertNotNull(WebhookProcessingStatusEnum.VALIDATING);
        assertEquals("VALIDATING", WebhookProcessingStatusEnum.VALIDATING.name());
    }

    @Test
    @DisplayName("WebhookProcessingStatusEnum should have PENDING value")
    void testPending() {
        assertNotNull(WebhookProcessingStatusEnum.PENDING);
        assertEquals("PENDING", WebhookProcessingStatusEnum.PENDING.name());
    }

    @Test
    @DisplayName("WebhookProcessingStatusEnum should have PROCESSING value")
    void testProcessing() {
        assertNotNull(WebhookProcessingStatusEnum.PROCESSING);
        assertEquals("PROCESSING", WebhookProcessingStatusEnum.PROCESSING.name());
    }

    @Test
    @DisplayName("WebhookProcessingStatusEnum should have PROCESSED value")
    void testProcessed() {
        assertNotNull(WebhookProcessingStatusEnum.PROCESSED);
        assertEquals("PROCESSED", WebhookProcessingStatusEnum.PROCESSED.name());
    }

    @Test
    @DisplayName("WebhookProcessingStatusEnum should have FAILED value")
    void testFailed() {
        assertNotNull(WebhookProcessingStatusEnum.FAILED);
        assertEquals("FAILED", WebhookProcessingStatusEnum.FAILED.name());
    }

    @Test
    @DisplayName("WebhookProcessingStatusEnum should have exactly 6 values")
    void testEnumSize() {
        assertEquals(6, WebhookProcessingStatusEnum.values().length);
    }

    @Test
    @DisplayName("All WebhookProcessingStatusEnum values should be accessible by name")
    void testAllValuesAccessible() {
        for (WebhookProcessingStatusEnum status : WebhookProcessingStatusEnum.values()) {
            assertEquals(status, WebhookProcessingStatusEnum.valueOf(status.name()));
        }
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid value")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> WebhookProcessingStatusEnum.valueOf("INVALID"));
    }
}

