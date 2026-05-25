package com.tumipay.microservice.domain.service.contract;

import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;

/**
 * IWebhookEventTypeClassifier
 * <p>
 * Domain service contract responsible for evaluating the raw JSON payload
 * of a provider webhook event and mapping it to the corresponding
 * {@link WebhookEventTypeEnum} value.
 * <p>
 * This contract is provider-specific: each Payment Provider integration must
 * supply its own implementation, since the fields and values that identify an
 * event type differ across providers. If the payload cannot be mapped to a
 * known type, the implementation MUST return {@link WebhookEventTypeEnum#UNKNOWN_EVENT}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-16
 */
public interface IProviderWebhookEventClassifierService {

    /**
     * Evaluates the raw JSON string stored in {@code pwe_event_request} and
     * determines the corresponding {@link WebhookClassifierResult}.
     *
     * <p>Implementations must never throw; any parsing or mapping failure must
     * result in {@link WebhookClassifierResult} being returned.
     *
     * @param eventRequest the raw JSON payload received from the payment provider,
     *                         as stored in the {@code pwe_event_request} column.
     * @return the classified {@link WebhookClassifierResult}, or
     *         {@link WebhookClassifierResult} if the type cannot be determined.
     *         Never {@code null}.
     */
    WebhookClassifierResult classifyWebhook(String eventRequest);
}

