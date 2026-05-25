package com.tumipay.microservice.shared.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * CommonValidationUtils
 * <p>
 * CommonValidationUtils class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 9/04/2026
 */
@UtilityClass
public class CommonValidationUtils {

    /**
     * Validates that the given text value is not blank (not null, empty, or whitespace-only).
     * If invalid, adds a descriptive error message to the provided errors list.
     *
     * @param value  the text value to validate.
     * @param field  the name of the field being validated, used in the error message.
     * @param errors the list to collect validation error messages.
     */
    public void validateText(String value, String field, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            errors.add("The " + field + " is required and cannot be empty");
        }
    }

    /**
     * Validates that the given enum value is not null.
     * If null, adds a descriptive error message to the provided errors list.
     *
     * @param value  the enum value to validate.
     * @param field  the name of the field being validated, used in the error message.
     * @param errors the list to collect validation error messages.
     */
    public void validateRequiredEnum(Enum<?> value, String field, List<String> errors) {
        if (value == null) {
            errors.add("The " + field + " is required and cannot be null");
        }
    }

    /**
     * Validates that the given text value is not blank and represents a valid UUID format.
     * Adds descriptive error messages to the provided errors list for each violation found.
     *
     * @param value  the string value expected to be a valid UUID.
     * @param field  the name of the field being validated, used in the error messages.
     * @param errors the list to collect validation error messages.
     */
    public void validateUuidText(String value, String field, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            errors.add("The " + field + " is required and cannot be empty");
            return;
        }

        if (!CommonUuidUtils.isValidId(value)) {
            errors.add("The " + field + " format is invalid");
        }
    }
}