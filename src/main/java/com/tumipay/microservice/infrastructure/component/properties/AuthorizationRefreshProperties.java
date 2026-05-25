package com.tumipay.microservice.infrastructure.component.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * AuthorizationRefreshProperties
 * <p>
 * AuthorizationRefreshProperties class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 18/04/2026
 */
@Data
@Validated
@ConfigurationProperties(prefix = "tumipay.authorization-refresh")
public class AuthorizationRefreshProperties {

    /**
     * Enables or disables the worker scheduler.
     */
    private boolean enabled = true;
}