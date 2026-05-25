package com.tumipay.microservice.infrastructure.adapter.output.http.standard.dto;

import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.request.GatewayWebhookRequest;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;

/**
 * PaymentGatewayComposition
 * <p>
 * PaymentGatewayComposition class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 25/05/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class PaymentGatewayComposition implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    private WebhookEvent webhookEvent;
    private GatewayWebhookRequest gatewayWebhookRequest;
    private String webhookEventPath;
    private String requestId;
    private String integrationId;
    private Long timeoutMs;
    private Duration timeout;
    private Long startNanos;
}