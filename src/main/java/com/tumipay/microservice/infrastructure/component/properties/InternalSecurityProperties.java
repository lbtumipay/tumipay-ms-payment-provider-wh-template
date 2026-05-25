package com.tumipay.microservice.infrastructure.component.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * InternalSecurityProperties
 * <p>
 * Configuration properties for internal API Key security.
 * <p>
 * Bound to {@code tumipay.security.internal} prefix, alineado con la convención
 * estándar del proyecto TumiPay.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@Data
@Validated
@ConfigurationProperties(prefix = "tumipay.security.internal")
public class InternalSecurityProperties {

    /**
     * Internal API Key used for service-to-service authentication.
     * <p>
     * MUST be provided via {@code ENV_MS_INTERNAL_API_KEY} environment variable.
     * Never hardcode this value.
     */
    private String apiKey;
}

