package com.tumipay.microservice.application.dto;

import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.model.provider.ProviderWebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * WebhookReceiverComposition
 * <p>
 * Composition object that carries all intermediate state through the
 * {@code processPayoutWebhookEvent} pipeline inside {@code WebhookReceiverUseCase}.
 * Each step in the pipeline reads from and writes to this object, keeping
 * individual step methods focused and easy to test in isolation.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class WebhookReceiverComposition implements Serializable {

    @Serial
    private static final long serialVersionUID = -3122974880770123456L;

    /** The original RECEIVED webhook event being processed. */
    private WebhookEvent webhookEvent;

    /** Result produced by the classifier after inspecting the raw event payload. */
    private WebhookClassifierResult webhookClassifierResult;

    /** Deserialized provider webhook request extracted from {@code webhookEvent.eventRequest}. */
    private ProviderWebhookEvent providerWebhookEvent;

    /** Provider transaction looked up by the providerTransactionId contained in the webhook. */
    private ProviderTransaction providerTransaction;
}

