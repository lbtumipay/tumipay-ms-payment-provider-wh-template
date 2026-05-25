package com.tumipay.microservice.application.dto;

import com.tumipay.microservice.domain.model.gateway.GatewayWebhookResult;
import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.model.provider.ProviderWebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * WebhookDispatchComposition
 * <p>
 * WebhookDispatchComposition class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/05/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class WebhookDispatchComposition implements Serializable {

    @Serial
    private static final long serialVersionUID = -3122974880770123456L;

    /**
     * UUID of the webhook event record in the adapter's {@code tp_provider_webhook_event} table.
     */
    private String workerId;

    /**
     * The original RECEIVED webhook event being processed.
     */
    private WebhookEvent webhookEvent;

    /**
     * Result produced by the classifier after inspecting the raw event payload.
     */
    private WebhookClassifierResult webhookClassifierResult;

    /**
     * Deserialized provider webhook request extracted from {@code webhookEvent.eventRequest}.
     */
    private ProviderWebhookEvent providerWebhookEvent;

    /**
     * Provider transaction looked up by the providerTransactionId contained in the webhook.
     */
    private ProviderTransaction providerTransaction;

    /**
     * Result of dispatching the webhook event to the TumiPay Payment Gateway.
     */
    private GatewayWebhookResult gatewayWebhookResult;
}