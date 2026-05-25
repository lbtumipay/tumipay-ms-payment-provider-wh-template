package com.tumipay.microservice.infrastructure.component.http.dto;

import lombok.*;
import java.io.Serial;
import java.io.Serializable;
import java.net.http.HttpHeaders;
import java.time.Duration;

/**
 * ClientHttpResponse
 * <p>
 * ClientHttpResponse class.
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
public class ClientHttpResponse<R> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * HTTP status code.
     */
    private Integer statusCode;

    /**
     * Response headers.
     */
    private HttpHeaders headers;

    /**
     * Response payload.
     */
    private R body;

    /**
     * Raw response body.
     */
    private String rawBody;

    /**
     * Execution duration.
     */
    private Duration duration;

    /**
     * Correlation identifier.
     */
    private String requestId;

    /**
     * Integration execution identifier.
     */
    private String integrationId;

    /**
     * Indicates successful execution.
     */
    private Boolean success;
}