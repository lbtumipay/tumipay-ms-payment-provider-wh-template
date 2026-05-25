package com.tumipay.microservice.domain.component.enums;

/**
 * AuthSchemeEnum
 * <p>
 * AuthSchemeEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 9/03/2026
 */
public enum AuthSchemeEnum {

    /**
     * OAuth 2.0 authentication scheme
     */
    OAUTH2,

    /**
     * API Key authentication scheme
     */
    API_KEY,

    /**
     * JSON Web Encryption authentication scheme
     */
    JWE,

    /**
     * Basic authentication scheme (username:password)
     */
    BASIC
}

