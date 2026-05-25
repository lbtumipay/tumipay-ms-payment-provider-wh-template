package com.tumipay.microservice.infrastructure.component.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for WebhookTimestampValidator.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 */
@DisplayName("WebhookTimestampValidator Unit Tests")
class WebhookTimestampValidatorTest {

    private static final int TOLERANCE = 300;

    private WebhookTimestampValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WebhookTimestampValidator();
    }

    @Nested
    @DisplayName("isValid() — within tolerance")
    class WithinTolerance {

        @Test
        @DisplayName("Should return true for timestamp 10 seconds in the past")
        void isValid_whenTimestamp10sInPast_thenTrue() {
            String timestamp = Instant.now().minusSeconds(10).toString();

            assertThat(validator.isValid(timestamp, TOLERANCE)).isTrue();
        }

        @Test
        @DisplayName("Should return true for timestamp 10 seconds in the future")
        void isValid_whenTimestamp10sInFuture_thenTrue() {
            String timestamp = Instant.now().plusSeconds(10).toString();

            assertThat(validator.isValid(timestamp, TOLERANCE)).isTrue();
        }

        @Test
        @DisplayName("Should return true for timestamp exactly at tolerance boundary (300s)")
        void isValid_whenTimestampAtBoundary_thenTrue() {
            String timestamp = Instant.now().minusSeconds(300).toString();

            assertThat(validator.isValid(timestamp, TOLERANCE)).isTrue();
        }
    }

    @Nested
    @DisplayName("isValid() — outside tolerance")
    class OutsideTolerance {

        @Test
        @DisplayName("Should return false for timestamp 301 seconds in the past")
        void isValid_whenTimestamp301sInPast_thenFalse() {
            String timestamp = Instant.now().minusSeconds(301).toString();

            assertThat(validator.isValid(timestamp, TOLERANCE)).isFalse();
        }

        @Test
        @DisplayName("Should return false for timestamp 301 seconds in the future")
        void isValid_whenTimestamp301sInFuture_thenFalse() {
            String timestamp = Instant.now().plusSeconds(301).toString();

            assertThat(validator.isValid(timestamp, TOLERANCE)).isFalse();
        }
    }

    @Nested
    @DisplayName("isValid() — invalid input")
    class InvalidInput {

        @Test
        @DisplayName("Should return false for malformed timestamp string")
        void isValid_whenTimestampMalformed_thenFalse() {
            assertThat(validator.isValid("not-a-timestamp", TOLERANCE)).isFalse();
        }

        @Test
        @DisplayName("Should return false for null timestamp without throwing exception")
        void isValid_whenTimestampIsNull_thenFalse() {
            assertThat(validator.isValid(null, TOLERANCE)).isFalse();
        }
    }
}
