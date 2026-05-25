package com.tumipay.microservice.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ErrorFormatterUtilsTest
 * <p>
 * ErrorFormatterUtilsTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 23/04/2026
 */
@DisplayName("ErrorFormatterUtils Unit Tests")
class CommonErrorUtilsTest {

    @Test
    @DisplayName("Should return fallback message for null or empty errors")
    void shouldReturnFallbackMessageForNullOrEmptyErrors() {
        assertEquals("error details not available", CommonErrorUtils.toInline(null));
        assertEquals("error details not available", CommonErrorUtils.toInline(List.of()));
        assertEquals("error details not available", CommonErrorUtils.toList(null));
        assertEquals("error details not available", CommonErrorUtils.toList(List.of()));
    }

    @Test
    @DisplayName("Should format errors as inline and numbered list")
    void shouldFormatErrorsAsInlineAndNumberedList() {
        List<String> errors = List.of("missing field", "invalid amount");

        assertEquals("missing field; invalid amount", CommonErrorUtils.toInline(errors));
        assertEquals("1. missing field\n2. invalid amount", CommonErrorUtils.toList(errors));
    }

    @Test
    @DisplayName("Should format errors as JSON")
    void shouldFormatErrorsAsJson() {
        assertEquals("[]", CommonErrorUtils.toJson(null));
        assertEquals("[\"missing field\",\"invalid amount\"]", CommonErrorUtils.toJson(List.of("missing field", "invalid amount")));
    }
}