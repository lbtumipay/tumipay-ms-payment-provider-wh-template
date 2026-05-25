package com.tumipay.microservice.infrastructure.component.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for TimingSafeSignatureComparator.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 */
@DisplayName("TimingSafeSignatureComparator Unit Tests")
class TimingSafeSignatureComparatorTest {

    private TimingSafeSignatureComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new TimingSafeSignatureComparator();
    }

    @Nested
    @DisplayName("matches(String, String)")
    class Matches {

        @Test
        @DisplayName("Should return true for two identical strings")
        void matches_whenIdenticalStrings_thenTrue() {
            String sig = "1ff93b74902d1f94c38d0cf384a6b44d294b4557b3bfa8cb79c6dce9ba467215";

            assertThat(comparator.matches(sig, sig)).isTrue();
        }

        @Test
        @DisplayName("Should return false for two different strings")
        void matches_whenDifferentStrings_thenFalse() {
            String generated = "1ff93b74902d1f94c38d0cf384a6b44d294b4557b3bfa8cb79c6dce9ba467215";
            String received  = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

            assertThat(comparator.matches(generated, received)).isFalse();
        }

        @Test
        @DisplayName("Should return false when generated is null")
        void matches_whenGeneratedIsNull_thenFalse() {
            assertThat(comparator.matches(null, "some-sig")).isFalse();
        }

        @Test
        @DisplayName("Should return false when received is null")
        void matches_whenReceivedIsNull_thenFalse() {
            assertThat(comparator.matches("some-sig", null)).isFalse();
        }

        @Test
        @DisplayName("Should return false when both arguments are null")
        void matches_whenBothAreNull_thenFalse() {
            assertThat(comparator.matches(null, null)).isFalse();
        }

        @Test
        @DisplayName("Should return false for strings with different lengths without throwing exception")
        void matches_whenDifferentLengths_thenFalseWithoutException() {
            String short1 = "abc";
            String long1  = "abcdef";

            assertThatCode(() -> comparator.matches(short1, long1))
                .doesNotThrowAnyException();

            assertThat(comparator.matches(short1, long1)).isFalse();
        }
    }

    @Nested
    @DisplayName("matchesBytes(byte[], String)")
    class MatchesBytes {

        @Test
        @DisplayName("Should return true when bytes match the hex representation")
        void matchesBytes_whenBytesMatchHex_thenTrue() {
            HmacSha256SignatureGenerator generator = new HmacSha256SignatureGenerator();
            String payload = "2025-02-03T22:20:24Z.{\"id\":\"test\"}";
            String secret = "test-secret";

            byte[] bytes = generator.generateBytes(payload, secret);
            String hex = generator.generate(payload, secret);

            assertThat(comparator.matchesBytes(bytes, hex)).isTrue();
        }

        @Test
        @DisplayName("Should return false when generatedBytes is null")
        void matchesBytes_whenGeneratedBytesIsNull_thenFalse() {
            assertThat(comparator.matchesBytes(null, "some-hex")).isFalse();
        }

        @Test
        @DisplayName("Should return false when receivedHex is null")
        void matchesBytes_whenReceivedHexIsNull_thenFalse() {
            assertThat(comparator.matchesBytes(new byte[]{1, 2, 3}, null)).isFalse();
        }
    }
}
