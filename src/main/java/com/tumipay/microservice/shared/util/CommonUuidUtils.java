package com.tumipay.microservice.shared.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * CommonUuidUtil
 * <p>
 * Utility class that centralizes validation logic related to {@link UUID}
 * identifiers across the microservice ecosystem.
 * <p>
 * This component provides standardized validation mechanisms to ensure that
 * UUID-based identifiers:
 * <ul>
 *     <li>Are not null when handled as {@link UUID} objects.</li>
 *     <li>Are syntactically valid when received as {@link String} values.</li>
 * </ul>
 *
 * <p>
 * The purpose of this utility is to:
 * <ul>
 *     <li>Promote defensive programming practices.</li>
 *     <li>Avoid repetitive validation logic across services, controllers, and mappers.</li>
 *     <li>Prevent {@link IllegalArgumentException} propagation caused by malformed UUID strings.</li>
 * </ul>
 *
 * <p>
 * Design considerations:
 * <ul>
 *     <li>Implemented as a {@code @UtilityClass} (Lombok), making the class final,
 *     with a private constructor and static methods only.</li>
 *     <li>Stateless and thread-safe.</li>
 *     <li>No side effects.</li>
 * </ul>
 *
 * <p>
 * Expected UUID format:
 * <pre>
 *     xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
 * </pre>
 * Example:
 * <pre>
 *     550e8400-e29b-41d4-a716-446655440000
 * </pre>
 *
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS
 * AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/02/2026
 */
@UtilityClass
public class CommonUuidUtils {

    /**
     * Validates that a {@link UUID} object reference is not null.
     * <p>
     * This method does not validate version or variant metadata of the UUID.
     * It strictly verifies object presence.
     *
     * <p>
     * Recommended usage:
     * <ul>
     *     <li>Domain validations</li>
     *     <li>Service layer input guards</li>
     *     <li>Repository parameter validation</li>
     * </ul>
     *
     * @param value UUID instance to validate.
     * @return {@code true} if the UUID reference is not null;
     *         {@code false} otherwise.
     */
    public static boolean isValidId(UUID value) {
        return value != null;
    }

    /**
     * Validates whether a {@link String} represents a syntactically valid UUID.
     * <p>
     * Validation rules:
     * <ul>
     *     <li>Must not be null.</li>
     *     <li>Must not be blank.</li>
     *     <li>Must conform to the standard UUID textual representation.</li>
     * </ul>
     * <p>
     * Internally delegates validation to {@link UUID#fromString(String)}.
     * If parsing fails, the method safely captures the {@link IllegalArgumentException}
     * and returns {@code false}.
     * <p>
     * This method guarantees:
     * <ul>
     *     <li>No exception propagation.</li>
     *     <li>Deterministic boolean output.</li>
     *     <li>Safe validation for REST inputs and external payloads.</li>
     * </ul>
     * @param value String value expected to contain a UUID.
     * @return {@code true} if the string is a valid UUID format;
     *         {@code false} otherwise.
     */
    public static boolean isValidId(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
