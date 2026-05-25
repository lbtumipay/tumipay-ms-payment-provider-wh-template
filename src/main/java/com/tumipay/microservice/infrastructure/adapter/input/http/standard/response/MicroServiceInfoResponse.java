package com.tumipay.microservice.infrastructure.adapter.input.http.standard.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MicroServiceInfoResponse
 * <p>
 * MicroServiceInfoResponse class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/02/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class MicroServiceInfoResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("service_description")
    private String serviceDescription;

    private String version;

    private String environment;

    @JsonProperty("java_version")
    private String javaVersion;

    @JsonProperty("spring_boot_version")
    private String springBootVersion;

    private LocalDateTime timestamp;
}