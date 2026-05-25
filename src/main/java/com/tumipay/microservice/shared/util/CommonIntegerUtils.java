package com.tumipay.microservice.shared.util;

import lombok.experimental.UtilityClass;

/**
 * CommonIntegerUtil
 * <p>
 * Centralized utility component responsible for validating numeric identifiers
 * across the microservice architecture.
 *
 * <p>
 * This utility abstracts and standardizes validation logic for identifiers
 * represented as {@link Number} types (e.g., {@link Integer}, {@link Long}),
 * ensuring consistent enforcement of positive numeric constraints.
 *
 * <p>
 * Validation rule enforced:
 * <pre>
 *     value != null AND value > 0
 * </pre>
 *
 * <p>
 * Architectural objectives:
 * <ul>
 *     <li>Guarantee consistent validation semantics across Controllers, Services, and Repositories.</li>
 *     <li>Prevent invalid persistence operations using zero or negative identifiers.</li>
 *     <li>Promote defensive programming aligned with domain-driven validation strategies.</li>
 *     <li>Reduce duplication of numeric guard clauses throughout the codebase.</li>
 * </ul>
 *
 * <p>
 * Design characteristics:
 * <ul>
 *     <li>Annotated with {@code @UtilityClass} (Lombok), implicitly making the class final.</li>
 *     <li>Private constructor generated automatically.</li>
 *     <li>Contains only static methods.</li>
 *     <li>Stateless and inherently thread-safe.</li>
 *     <li>No side effects.</li>
 * </ul>
 *
 * <p>
 * The validation logic assumes compatibility with common database identity strategies,
 * such as auto-increment columns and sequence-generated primary keys, where
 * valid identifiers must be strictly positive.
 *
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS
 * AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/02/2026
 */
@UtilityClass
public class CommonIntegerUtils {

    /**
     * Validates whether a numeric identifier is strictly positive.
     *
     * <p>
     * Validation criteria:
     * <ul>
     *     <li>The value must not be {@code null}.</li>
     *     <li>The numeric value must be strictly greater than zero.</li>
     * </ul>
     *
     * <p>
     * Supported numeric types include:
     * <ul>
     *     <li>{@link Integer}</li>
     *     <li>{@link Long}</li>
     *     <li>Any subclass of {@link Number}</li>
     * </ul>
     *
     * <p>
     * Typical usage scenarios:
     * <ul>
     *     <li>Primary key validation prior to database queries.</li>
     *     <li>Path variable validation in REST controllers.</li>
     *     <li>Service-layer input guards.</li>
     *     <li>Precondition validation before invoking repository methods.</li>
     * </ul>
     *
     * <p>
     * Example:
     * <pre>
     *     isPositive(10)     → true
     *     isPositive(0)      → false
     *     isPositive(-5)     → false
     *     isPositive(null)   → false
     * </pre>
     *
     * <p>
     * Implementation detail:
     * <ul>
     *     <li>Uses {@code longValue()} to normalize numeric comparison.</li>
     *     <li>No exception is thrown; validation is non-intrusive and side-effect free.</li>
     * </ul>
     *
     * @param value Numeric identifier to validate.
     * @return {@code true} if the value is non-null and strictly greater than zero;
     *         {@code false} otherwise.
     */
    public static boolean isPositive(Number value) {
        return value != null && value.longValue() > 0;
    }

    /**
     * Returns {@code value} if it is not {@code null}; otherwise returns {@code defaultValue}.
     * <p>
     * Examples:
     * <pre>
     * defaultIfNull(null, 5)  = 5
     * defaultIfNull(0, 5)     = 0
     * defaultIfNull(10, 5)    = 10
     * </pre>
     *
     * @param value        the {@link Integer} to evaluate; may be {@code null}
     * @param defaultValue the fallback primitive value to return when {@code value} is {@code null}
     * @return {@code value} if not {@code null}; {@code defaultValue} otherwise
     */
    public static int defaultIfNull(final Integer value, final int defaultValue) {
        return value != null ? value : defaultValue;
    }
}
