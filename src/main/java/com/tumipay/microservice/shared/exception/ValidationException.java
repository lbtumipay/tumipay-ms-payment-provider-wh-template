package com.tumipay.microservice.shared.exception;

import com.tumipay.microservice.shared.dto.ValidationError;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * ValidationException
 * <p>
 * ValidationException class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 17/02/2026
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends RuntimeException {

    private final String code;
    private final String message;
    private final List<ValidationError> errors;

    public ValidationException(final String code, final String message, final List<ValidationError> errors) {
        super(message);
        this.code = code;
        this.errors = errors;
        this.message = message;
    }
}
