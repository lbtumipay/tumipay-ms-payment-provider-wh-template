package com.tumipay.microservice.domain.model.webhook;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * WebhookEventResult
 * <p>
 * Webhook event acknowledgment result.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class WebhookEventResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    private String uuid;
    private String eventType;
    private WebhookProcessingStatusEnum processingStatus;
    private String adapterProviderCode;
    private String message;
    private Instant receivedAt;
    private Instant timestamp;
}

