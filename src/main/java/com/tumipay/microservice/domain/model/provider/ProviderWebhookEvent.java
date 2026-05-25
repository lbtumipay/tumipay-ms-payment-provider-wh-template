package com.tumipay.microservice.domain.model.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * MoneyMovementWebhookEvent
 * <p>
 * MoneyMovementWebhookEvent class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class ProviderWebhookEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    private String eventId;

    private String eventKey;

    private Map<String, Object> content;
}