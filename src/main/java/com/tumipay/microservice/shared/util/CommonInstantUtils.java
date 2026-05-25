package com.tumipay.microservice.shared.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * CommonInstantUtils
 * <p>
 * CommonInstantUtils class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/04/2026
 */
@UtilityClass
public class CommonInstantUtils {

    /**
     * Converts a value of unknown temporal type to {@link Instant}.
     * <p>
     * Supported input types:
     * <ul>
     *     <li>{@code null} → {@code null}</li>
     *     <li>{@link Instant} → returned as-is</li>
     *     <li>{@link LocalDateTime} → converted assuming UTC offset</li>
     *     <li>{@link OffsetDateTime} → converted via {@link OffsetDateTime#toInstant()}</li>
     *     <li>Any other type → {@code null}</li>
     * </ul>
     * <p>
     * Example:
     * <pre>
     * toInstant(null)                        = null
     * toInstant(Instant.now())               = Instant (as-is)
     * toInstant(LocalDateTime.now())         = Instant (UTC)
     * toInstant(OffsetDateTime.now())        = Instant
     * toInstant("unsupported")              = null
     * </pre>
     *
     * @param value the temporal value to convert; may be {@code null} or any supported type
     * @return the equivalent {@link Instant}, or {@code null} if not convertible
     */
    public static Instant toInstant(Object value) {
        return switch (value) {
            case null -> null;
            case Instant instant -> instant;
            case LocalDateTime ldt -> ldt.toInstant(ZoneOffset.UTC);
            case OffsetDateTime odt -> odt.toInstant();
            default -> null;
        };
    }
}