package com.tumipay.microservice.infrastructure.component.http.dto;

import com.tumipay.microservice.infrastructure.component.http.config.ConfigHttpIntegration;
import com.tumipay.microservice.infrastructure.component.http.enums.HttpMethodEnum;
import lombok.*;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;

/**
 * ClientHttpRequest
 * <p>
 * ClientHttpRequest class.
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
public class ClientHttpRequest <T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Integration configuration.
     */
    private ConfigHttpIntegration configIntegration;

    /**
     * HTTP method.
     */
    private HttpMethodEnum method;

    /**
     * HTTP headers.
     */
    private HttpHeaders headers;

    /**
     * Query parameters.
     */
    private MultiValueMap<String, String> queryParams;

    /**
     * Request payload.
     */
    private T body;

    /**
     * Request timeout.
     */
    private Duration timeout;

    /**
     * Correlation identifier.
     */
    private String requestId;

    /**
     * Integration execution identifier.
     */
    private String integrationId;
}