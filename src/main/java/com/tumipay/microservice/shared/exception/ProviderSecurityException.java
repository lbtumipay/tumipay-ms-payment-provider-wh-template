package com.tumipay.microservice.shared.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ProviderSecurityException
 * <p>
 * ProviderSecurityException class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/03/2026
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProviderSecurityException extends RuntimeException {

    private final String code;
    private final String message;


    public ProviderSecurityException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
