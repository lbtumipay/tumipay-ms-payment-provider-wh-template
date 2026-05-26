package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.component.mapper.WebhookEventMapperComponent;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventClassifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * WebhookEventTypeClassifier
 * <p>
 * Provider-specific implementation of {@link IProviderWebhookEventClassifierService}.
 * Deserializes the raw JSON payload from {@code pwe_event_request} and maps
 * it to the corresponding {@link WebhookEventTypeEnum} according to the
 * payment provider's webhook contract.
 * <p>
 * Returns {@link WebhookEventTypeEnum#UNKNOWN_EVENT} for any payload that
 * cannot be parsed or mapped, ensuring every event persisted in
 * {@code tp_provider_webhook_event} carries a valid, typed classification
 * before it is dispatched to the TumiPay Payment Gateway.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-16
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ProviderWebhookEventClassifierService implements IProviderWebhookEventClassifierService {

    private final WebhookEventMapperComponent webhookEventMapperComponent;

    @Override
    public WebhookClassifierResult classifyWebhook(String eventRequest) {

        try {

            return webhookEventMapperComponent.mapToClassifierResult(eventRequest);
        } catch (Exception e) {
            log.error("Failed to classify event webhook type from payload: {}", e.getMessage());

            return WebhookClassifierResult.builder()
                .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
                .build();
        }
    }
}
