package com.tumipay.microservice.shared.util;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ErrorFormatterUtils
 * <p>
 * ErrorFormatterUtils class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 12/03/2026
 */
@UtilityClass
public class CommonErrorUtils {

    private static final Gson GSON = new Gson();

    /**
     * Formats a list of error messages as a single inline string separated by {@code "; "}.
     * Returns {@code "error details not available"} if the list is null or empty.
     *
     * @param errors the list of error messages to format.
     * @return a single-line string with all errors joined by {@code "; "}.
     */
    public static String toInline(List<String> errors) {

        if (errors == null || errors.isEmpty()) {
            return "error details not available";
        }

        return String.join("; ", errors);
    }

    /**
     * Formats a list of error messages as a numbered multiline string.
     * Returns {@code "error details not available"} if the list is null or empty.
     *
     * @param errors the list of error messages to format.
     * @return a numbered multiline string with each error on its own line.
     */
    public static String toList(List<String> errors) {

        if (errors == null || errors.isEmpty()) {
            return "error details not available";
        }

        return IntStream.range(0, errors.size())
            .mapToObj(i -> (i + 1) + ". " + errors.get(i))
            .collect(Collectors.joining("\n"));
    }

    /**
     * Serializes a list of error messages to a JSON array string.
     * Returns {@code "[]"} if the list is null.
     *
     * @param errors the list of error messages to serialize.
     * @return a JSON array string representation of the errors.
     */
    public static String toJson(List<String> errors) {
        if (errors == null) {
            return "[]";
        }
        return GSON.toJson(errors);
    }
}