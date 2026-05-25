package com.tumipay.microservice.infrastructure.component.enums;

import java.util.Locale;

/**
 * AllowCountryCodesEnum
 * <p>
 * AllowCountryCodesEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 21/04/2026
 */
public enum AllowCountryCodesEnum {

    CO("CO"),
    MX("MX");

    private final String code;

    AllowCountryCodesEnum(final String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    public String toLowerCase() {
        return toString().toLowerCase(Locale.ROOT);
    }

    public String toUpperCase() {
        return toString().toUpperCase(Locale.ROOT);
    }
}
