package com.tumipay.microservice.infrastructure.adapter.output.security;

import com.tumipay.microservice.infrastructure.component.properties.InternalSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for InternalApiKeyValidatorAdapter.
 * <p>
 * Verifies all validation branches: null/blank incoming key, null/blank configured secret,
 * mismatched keys, and exact match (constant-time comparison).
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InternalApiKeyValidatorAdapter Unit Tests")
class InternalApiKeyValidatorAdapterTest {

    @Mock
    private InternalSecurityProperties properties;

    private InternalApiKeyValidatorAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InternalApiKeyValidatorAdapter(properties);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Incoming API Key is null or blank → always false
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When the incoming API Key is null or blank")
    class IncomingKeyNullOrBlank {

        @Test
        @DisplayName("Should return false when apiKey is null")
        void shouldReturnFalseWhenApiKeyIsNull() {
            assertFalse(adapter.isValid(null));
        }

        @ParameterizedTest(name = "blank value: \"{0}\"")
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should return false when apiKey is blank")
        void shouldReturnFalseWhenApiKeyIsBlank(String blank) {
            assertFalse(adapter.isValid(blank));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Configured secret is null or blank → always false
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When the configured secret is null or blank")
    class ConfiguredSecretNullOrBlank {

        @Test
        @DisplayName("Should return false when configured apiKey property is null")
        void shouldReturnFalseWhenConfiguredKeyIsNull() {
            when(properties.getApiKey()).thenReturn(null);
            assertFalse(adapter.isValid("some-incoming-key"));
        }

        @ParameterizedTest(name = "blank configured secret: \"{0}\"")
        @ValueSource(strings = {"", "   ", "\t"})
        @DisplayName("Should return false when configured apiKey property is blank")
        void shouldReturnFalseWhenConfiguredKeyIsBlank(String blank) {
            when(properties.getApiKey()).thenReturn(blank);
            assertFalse(adapter.isValid("some-incoming-key"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Keys do not match → false
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When the incoming key does not match the configured secret")
    class KeyMismatch {

        @Test
        @DisplayName("Should return false when keys are completely different")
        void shouldReturnFalseWhenKeysDiffer() {
            when(properties.getApiKey()).thenReturn("correct-secret");
            assertFalse(adapter.isValid("wrong-key"));
        }

        @Test
        @DisplayName("Should return false when keys differ only by case")
        void shouldReturnFalseWhenKeysDifferByCase() {
            when(properties.getApiKey()).thenReturn("MySecret");
            assertFalse(adapter.isValid("mysecret"));
        }

        @Test
        @DisplayName("Should return false when incoming key is a prefix of the configured secret")
        void shouldReturnFalseWhenIncomingKeyIsPrefix() {
            when(properties.getApiKey()).thenReturn("secret-full");
            assertFalse(adapter.isValid("secret"));
        }

        @Test
        @DisplayName("Should return false when incoming key is a suffix of the configured secret")
        void shouldReturnFalseWhenIncomingKeyIsSuffix() {
            when(properties.getApiKey()).thenReturn("prefix-secret");
            assertFalse(adapter.isValid("secret"));
        }

        @Test
        @DisplayName("Should return false when keys differ by trailing whitespace")
        void shouldReturnFalseWhenKeysDifferByTrailingWhitespace() {
            when(properties.getApiKey()).thenReturn("secret");
            assertFalse(adapter.isValid("secret "));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Keys match exactly → true
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When the incoming key matches the configured secret exactly")
    class KeyMatch {

        @Test
        @DisplayName("Should return true when keys match exactly")
        void shouldReturnTrueWhenKeysMatch() {
            when(properties.getApiKey()).thenReturn("correct-secret");
            assertTrue(adapter.isValid("correct-secret"));
        }

        @Test
        @DisplayName("Should return true for a long API key that matches")
        void shouldReturnTrueForLongMatchingKey() {
            String longKey = "a".repeat(256);
            when(properties.getApiKey()).thenReturn(longKey);
            assertTrue(adapter.isValid(longKey));
        }

        @Test
        @DisplayName("Should return true when key contains special characters and matches")
        void shouldReturnTrueForKeyWithSpecialCharacters() {
            String specialKey = "S3cr3t!@#$%^&*()-_=+[]{}|;':\",./<>?";
            when(properties.getApiKey()).thenReturn(specialKey);
            assertTrue(adapter.isValid(specialKey));
        }

        @Test
        @DisplayName("Should return true when key contains unicode characters and matches")
        void shouldReturnTrueForKeyWithUnicodeCharacters() {
            String unicodeKey = "clave-secreta-ñ-ü-ö-€";
            when(properties.getApiKey()).thenReturn(unicodeKey);
            assertTrue(adapter.isValid(unicodeKey));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Constant-time comparison (timing-attack resistance)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Constant-time comparison behavior")
    class ConstantTimeComparison {

        @Test
        @DisplayName("Should reject keys that match up to all but the last byte")
        void shouldRejectKeyThatDiffersOnlyInLastByte() {
            when(properties.getApiKey()).thenReturn("abcdefghij-X");
            assertFalse(adapter.isValid("abcdefghij-Y"));
        }

        @Test
        @DisplayName("Should reject keys that match up to all but the first byte")
        void shouldRejectKeyThatDiffersOnlyInFirstByte() {
            when(properties.getApiKey()).thenReturn("Xbcdefghij");
            assertFalse(adapter.isValid("Ybcdefghij"));
        }
    }
}

