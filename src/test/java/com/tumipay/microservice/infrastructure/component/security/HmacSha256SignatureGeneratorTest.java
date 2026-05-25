package com.tumipay.microservice.infrastructure.component.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HmacSha256SignatureGenerator.
 * <p>
 * Verifies HMAC-SHA256 algorithm: correct format (64-char lowercase hex),
 * determinism, null/empty key handling and byte-array output.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 */
@DisplayName("HmacSha256SignatureGenerator Unit Tests")
class HmacSha256SignatureGeneratorTest {

    private HmacSha256SignatureGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new HmacSha256SignatureGenerator();
    }

    @Nested
    @DisplayName("generate()")
    class Generate {

        @Test
        @DisplayName("Should produce a lowercase hex string of exactly 64 characters for valid input")
        void generate_withValidInput_thenReturns64CharLowercaseHex() {
            String payload = "2025-02-03T22:20:24Z.{\"id\":\"ev_abc\",\"event_key\":\"accounts.balance.credit\"}";
            String secret = "cobre is super secure";

            String result = generator.generate(payload, secret);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(64);
            assertThat(result).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("Should produce the same hash for the same input (determinism)")
        void generate_sameInput_thenDeterministicOutput() {
            String payload = "2025-02-03T22:20:24Z.{\"id\":\"ev_abc\"}";
            String secret = "test-secret";

            String result1 = generator.generate(payload, secret);
            String result2 = generator.generate(payload, secret);

            assertThat(result1).isNotNull().isEqualTo(result2);
        }

        @Test
        @DisplayName("Should produce different hashes for different secrets")
        void generate_differentSecrets_thenDifferentHashes() {
            String payload = "2025-02-03T22:20:24Z.{\"id\":\"ev_abc\"}";

            String result1 = generator.generate(payload, "secret-one");
            String result2 = generator.generate(payload, "secret-two");

            assertThat(result1).isNotNull().isNotEqualTo(result2);
        }

        @Test
        @DisplayName("Should return null when secretKey is null without throwing exception")
        void generate_whenSecretKeyIsNull_thenReturnsNull() {
            String result = generator.generate("any.payload", null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when secretKey is empty string")
        void generate_whenSecretKeyIsEmpty_thenReturnsNull() {
            String result = generator.generate("any.payload", "");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("generateBytes()")
    class GenerateBytes {

        @Test
        @DisplayName("Should return 32-byte array for valid input (SHA256 = 256 bits = 32 bytes)")
        void generateBytes_withValidInput_thenReturns32Bytes() {
            String payload = "2025-01-01T00:00:00Z.{\"id\":\"test\"}";
            String secret = "test-secret";

            byte[] result = generator.generateBytes(payload, secret);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("Should return empty array when secretKey is null without throwing exception")
        void generateBytes_whenSecretKeyIsNull_thenReturnsEmptyArray() {
            byte[] result = generator.generateBytes("any.payload", null);

            assertThat(result).isNotNull().isEmpty();
        }
    }
}
