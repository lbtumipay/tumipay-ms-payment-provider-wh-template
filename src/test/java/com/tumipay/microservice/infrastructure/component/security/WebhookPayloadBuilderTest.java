package com.tumipay.microservice.infrastructure.component.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for WebhookPayloadBuilder.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 */
@DisplayName("WebhookPayloadBuilder Unit Tests")
class WebhookPayloadBuilderTest {

    private WebhookPayloadBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new WebhookPayloadBuilder();
    }

    @Nested
    @DisplayName("build()")
    class Build {

        @Test
        @DisplayName("Should concatenate timestamp and rawBody with period separator")
        void build_whenTimestampAndBody_thenConcatenatesCorrectly() {
            String timestamp = "2025-02-03T22:20:24Z";
            String rawBody = "{\"id\":\"ev_abc\"}";

            String result = builder.build(timestamp, rawBody);

            assertThat(result).isEqualTo("2025-02-03T22:20:24Z.{\"id\":\"ev_abc\"}");
        }

        @Test
        @DisplayName("Should produce canonical form with empty rawBody")
        void build_whenEmptyRawBody_thenTrailingDot() {
            String timestamp = "2025-02-03T22:20:24Z";
            String rawBody = "";

            String result = builder.build(timestamp, rawBody);

            assertThat(result).isEqualTo("2025-02-03T22:20:24Z.");
        }
    }
}
