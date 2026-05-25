package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.ProviderWebhookEventTypeEnum;
import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.model.provider.ProviderWebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventClassifierService;
import com.tumipay.microservice.shared.util.CommonJsonUtils;
import com.tumipay.microservice.shared.util.CommonStringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Map;

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

    @Override
    public WebhookClassifierResult classifyWebhook(String eventRequest) {

        if (eventRequest == null || eventRequest.isBlank()) {
            log.warn("Cannot classify event webhook type: eventRequestJson is null or blank");

            return WebhookClassifierResult.builder()
                .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
                .build();
        }

        try {

            final ProviderWebhookEvent providerWebhookEvent = CommonJsonUtils.fromJson(
                eventRequest,
                ProviderWebhookEvent.class
            );

            final String providerWebhookEventType = extractEventType(providerWebhookEvent.getEventKey());

            if (ProviderWebhookEventTypeEnum.MONEY_MOVEMENTS.getCode().equals(providerWebhookEventType)) {
                return getPayOutClassifierResult(providerWebhookEvent);
            }

            return WebhookClassifierResult.builder()
                .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
                .transactionStatus(TransactionStatusEnum.ERROR)
                .build();
        } catch (Exception e) {
            log.error("Failed to classify event webhook type from payload: {}", e.getMessage());

            return WebhookClassifierResult.builder()
                .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
                .build();
        }
    }

    private WebhookClassifierResult getPayOutClassifierResult(ProviderWebhookEvent providerWebhookEvent) {

        try {

            if (providerWebhookEvent == null || providerWebhookEvent.getContent() == null) {
                return WebhookClassifierResult.builder()
                    .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
                    .transactionStatus(TransactionStatusEnum.ERROR)
                    .build();
            }

            final Map<String, Object> content = providerWebhookEvent.getContent();

            if (content.isEmpty() || CommonStringUtils.isEmpty(providerWebhookEvent.getEventId())) {
                return WebhookClassifierResult.builder()
                    .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
                    .transactionStatus(TransactionStatusEnum.ERROR)
                    .build();
            }

            if (providerWebhookEvent.getEventKey() == null || CommonStringUtils.isEmpty(providerWebhookEvent.getEventKey())) {
                return WebhookClassifierResult.builder()
                    .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
                    .transactionStatus(TransactionStatusEnum.ERROR)
                    .build();
            }

            final String providerTransactionId = String.valueOf(content.get("id"));

            switch (providerWebhookEvent.getEventKey()) {
                case "money_movements.status.initiated",
                     "money_movements.status.pending_approval",
                     "money_movements.status.processing" -> {
                    return WebhookClassifierResult.builder()
                        .providerTransactionId(providerTransactionId)
                        .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_PENDING)
                        .transactionStatus(TransactionStatusEnum.PENDING)
                        .build();
                }
                case "money_movements.status.completed" -> {
                    return WebhookClassifierResult.builder()
                        .providerTransactionId(providerTransactionId)
                        .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED)
                        .transactionStatus(TransactionStatusEnum.APPROVED)
                        .build();
                }
                case "money_movements.status.rejected" -> {
                    return WebhookClassifierResult.builder()
                        .providerTransactionId(providerTransactionId)
                        .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_REJECTED)
                        .transactionStatus(TransactionStatusEnum.REJECTED)
                        .build();
                }
                case "money_movements.status.failed" -> {
                    return WebhookClassifierResult.builder()
                        .providerTransactionId(providerTransactionId)
                        .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_ERROR)
                        .transactionStatus(TransactionStatusEnum.REJECTED)
                        .build();
                }
                case "money_movements.status.cancelled" -> {
                    return WebhookClassifierResult.builder()
                        .providerTransactionId(providerTransactionId)
                        .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_CANCELLED)
                        .transactionStatus(TransactionStatusEnum.CANCELLED)
                        .build();
                }
            }

            return WebhookClassifierResult.builder()
                .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
                .transactionStatus(TransactionStatusEnum.ERROR)
                .build();
        } catch (Exception e) {

            log.error("Failed to classify PAYOUT event type from payload: {}", e.getMessage());

            return WebhookClassifierResult.builder()
                .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
                .transactionStatus(TransactionStatusEnum.ERROR)
                .build();
        }
    }

    /**
     * Extracts the main event type from the provider's event key.
     * For example, an event key like "money_movements.created" would return "money_movements".
     * Examples:
     * money_movements.status.initiated
     * money_movements.status.processing
     * money_movements.status.completed
     * money_movements.status.rejected
     * money_movements.status.failed
     * money_movements.status.pending_approval
     * money_movements.status.canceled
     * money_movements.status.returned
     *
     * @param eventKey the raw event key from the provider's webhook payload.
     * @return the extracted main event type, or the original string if it does not contain a dot.
     */
    private String extractEventType(String eventKey) {

        if (eventKey == null || eventKey.isBlank()) {
            return null;
        }

        final int firstDotIndex = eventKey.indexOf('.');

        // Does not have the expected format → return the entire string
        if (firstDotIndex == -1) {
            return eventKey;
        }

        return eventKey.substring(0, firstDotIndex);
    }
}
