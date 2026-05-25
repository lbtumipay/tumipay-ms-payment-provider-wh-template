package com.tumipay.microservice.infrastructure.component.http.config;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.Map;

/**
 * ConfigHttpIntegration
 * <p>
 * ConfigHttpIntegration class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/05/2026
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigHttpIntegration implements Serializable {

    @Serial
    private static final long serialVersionUID = 8678448860743774637L;

    /**
     * Integration identifier.
     */
    private String integrationCode;

    /**
     * Base host.
     */
    private String host;

    /**
     * Base integration path.
     */
    private String integrationPath;

    /**
     * Default timeout.
     */
    private Duration timeout;

    /**
     * Default headers.
     */
    private Map<String, String> defaultHeaders;

    /**
     * Enable retries.
     */
    private Boolean retryEnabled;

    /**
     * Max retries.
     */
    private Integer maxRetries;

    /**
     * Enable request logging.
     */
    private Boolean logRequest;

    /**
     * Enable response logging.
     */
    private Boolean logResponse;

    /**
     * Encode payload to Base64 before logging.
     */
    private Boolean encodeBodyToBase64;
}