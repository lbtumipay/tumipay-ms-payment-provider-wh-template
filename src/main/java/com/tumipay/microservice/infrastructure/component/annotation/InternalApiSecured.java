package com.tumipay.microservice.infrastructure.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * InternalApiSecured
 * <p>
 * Marks a controller class or method as requiring internal API Key authentication.
 * <p>
 * When present, the {@code InternalApiSecurityFilter}
 * will enforce validation of the {@code X-Api-Key} header before allowing the request to proceed.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalApiSecured {
}

