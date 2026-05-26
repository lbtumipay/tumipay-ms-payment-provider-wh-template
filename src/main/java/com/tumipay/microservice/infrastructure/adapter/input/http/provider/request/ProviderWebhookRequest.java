package com.tumipay.microservice.infrastructure.adapter.input.http.provider.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * ProviderWebhookRequest
 * <p>
 * Generic request contract for provider webhook payload.
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
public class ProviderWebhookRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7823649870234561234L;

    @JsonProperty("id")
    private String eventId;

    @JsonProperty("event_key")
    private String eventKey;

    @JsonProperty("content")
    private Object content;

    @JsonProperty("created_at")
    private String createdAt;
}
