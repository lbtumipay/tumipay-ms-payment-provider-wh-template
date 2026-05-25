package com.tumipay.microservice.application.dto;

import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookEventResult;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * WebhookEventComposition
 * <p>
 * Composition object used during webhook processing pipeline.
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
public class WebhookEventComposition implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    private WebhookEvent webhookEvent;

    private WebhookEventResult webhookEventResult;
}

