package com.tumipay.microservice.infrastructure.adapter.input.http.provider.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * ProviderWebhookResponse
 * <p>
 * HTTP acknowledgment response for webhook reception.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class ProviderWebhookResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;
}

