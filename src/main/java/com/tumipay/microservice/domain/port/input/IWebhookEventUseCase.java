package com.tumipay.microservice.domain.port.input;

import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookEventResult;
import reactor.core.publisher.Mono;

/**
 * IWebhookEventUseCase
 * <p>
 * Webhook event input use case contract.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
public interface IWebhookEventUseCase {

    /**
     * Receives and processes a webhook event from the Payment Provider.
     *
     * @param webhookEvent normalized webhook event domain model
     * @return webhook event acknowledgment result
     */
    Mono<WebhookEventResult> processWebhookEvent(WebhookEvent webhookEvent);
}

