package com.tumipay.microservice.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonInstantUtilsTest
 * <p>
 * Unit tests for {@link CommonInstantUtils}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-20
 */
@DisplayName("CommonInstantUtils Unit Tests")
class CommonInstantUtilsTest {

    @Test
    @DisplayName("toInstant(null) — should return null")
    void toInstant_null_shouldReturnNull() {
        assertNull(CommonInstantUtils.toInstant(null));
    }

    @Test
    @DisplayName("toInstant(Instant) — should return the same Instant instance")
    void toInstant_instant_shouldReturnSameInstance() {
        Instant now = Instant.now();
        assertSame(now, CommonInstantUtils.toInstant(now));
    }

    @Test
    @DisplayName("toInstant(LocalDateTime) — should convert assuming UTC offset")
    void toInstant_localDateTime_shouldConvertWithUtcOffset() {
        LocalDateTime ldt = LocalDateTime.of(2026, 4, 20, 12, 0, 0);
        Instant expected = ldt.toInstant(ZoneOffset.UTC);

        assertEquals(expected, CommonInstantUtils.toInstant(ldt));
    }

    @Test
    @DisplayName("toInstant(OffsetDateTime) — should convert via toInstant()")
    void toInstant_offsetDateTime_shouldConvertCorrectly() {
        OffsetDateTime odt = OffsetDateTime.of(2026, 4, 20, 12, 0, 0, 0, ZoneOffset.ofHours(-5));
        Instant expected = odt.toInstant();

        assertEquals(expected, CommonInstantUtils.toInstant(odt));
    }

    @Test
    @DisplayName("toInstant(String) — unsupported type should return null")
    void toInstant_unsupportedType_shouldReturnNull() {
        assertNull(CommonInstantUtils.toInstant("2026-04-20T12:00:00Z"));
    }

    @Test
    @DisplayName("toInstant(Integer) — unsupported type should return null")
    void toInstant_integer_shouldReturnNull() {
        assertNull(CommonInstantUtils.toInstant(12345));
    }
}
