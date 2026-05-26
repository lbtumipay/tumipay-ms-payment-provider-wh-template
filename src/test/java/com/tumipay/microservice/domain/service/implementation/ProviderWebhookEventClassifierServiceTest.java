package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.component.mapper.WebhookEventMapperComponent;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * WebhookEventTypeClassifierTest
 * <p>
 * Unit tests for {@link ProviderWebhookEventClassifierService} with Cobre provider-specific logic.
 * Tests use the serialized {@code ProviderMoneyMovementWebhookRequest} JSON format
 * (as stored in {@code pwe_event_request}) adapted from Cobre's webhook payload.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-20
 */
@DisplayName("WebhookEventTypeClassifier Tests")
class ProviderWebhookEventClassifierServiceTest {

    private ProviderWebhookEventClassifierService classifier;

    @BeforeEach
    void setUp() {
        classifier = new ProviderWebhookEventClassifierService(new WebhookEventMapperComponent());
    }

    // ── Null / blank input ────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return UNKNOWN_EVENT when eventRequestJson is null")
    void shouldReturnUnknownEventWhenNull() {
        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, classifier.classifyWebhook(null).getClassifiedType());
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT when eventRequestJson is blank")
    void shouldReturnUnknownEventWhenBlank() {
        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, classifier.classifyWebhook("   ").getClassifiedType());
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT when eventRequestJson is empty string")
    void shouldReturnUnknownEventWhenEmpty() {
        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, classifier.classifyWebhook("").getClassifiedType());
    }

    // ── Invalid JSON ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return UNKNOWN_EVENT when eventRequestJson is invalid JSON")
    void shouldReturnUnknownEventOnInvalidJson() {
        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, classifier.classifyWebhook("not-a-json{{{").getClassifiedType());
    }

    // ── PAYOUT_TRANSACTION — Cobre money_movements events ────────────────────

    @Test
    @DisplayName("Should classify money_movements.status.completed as PAYOUT_TRANSACTION_APPROVED")
    void shouldClassifyCompletedAsPayoutApproved() {
        WebhookClassifierResult result = classifier.classifyWebhook(cobreJson("money_movements.status.completed", "completed", "mm_001"));
        assertEquals(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.APPROVED, result.getTransactionStatus());
        assertEquals("mm_001", result.getProviderTransactionId());
    }

    @Test
    @DisplayName("Should classify money_movements.status.failed as PAYOUT_TRANSACTION_REJECTED")
    void shouldClassifyFailedAsPayoutRejected() {
        WebhookClassifierResult result = classifier.classifyWebhook(cobreJson("money_movements.status.failed", "failed", "mm_002"));
        assertEquals(WebhookEventTypeEnum.PAYOUT_TRANSACTION_ERROR, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.REJECTED, result.getTransactionStatus());
    }

    @Test
    @DisplayName("Should classify money_movements.status.cancelled as PAYOUT_TRANSACTION_CANCELLED")
    void shouldClassifyCancelledAsPayoutCancelled() {
        WebhookClassifierResult result = classifier.classifyWebhook(cobreJson("money_movements.status.cancelled", "cancelled", "mm_003"));
        assertEquals(WebhookEventTypeEnum.PAYOUT_TRANSACTION_CANCELLED, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.CANCELLED, result.getTransactionStatus());
    }

    @Test
    @DisplayName("Should classify money_movements.status.processing as PAYOUT_TRANSACTION_PENDING and return PENDING transactionStatus")
    void shouldClassifyProcessingAsPayoutPendingWithNullStatus() {
        WebhookClassifierResult result = classifier.classifyWebhook(cobreJson("money_movements.status.processing", "processing", "mm_004"));
        assertEquals(WebhookEventTypeEnum.PAYOUT_TRANSACTION_PENDING, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.PENDING, result.getTransactionStatus());
    }

    // ── UNKNOWN_EVENT — unrecognized inputs ──────────────────────────────────

    @Test
    @DisplayName("Should return UNKNOWN_EVENT for valid JSON with empty event_type")
    void shouldReturnUnknownEventForEmptyJsonObject() {
        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, classifier.classifyWebhook("{}").getClassifiedType());
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT for unknown event_key prefix")
    void shouldReturnUnknownEventForUnknownEventKeyPrefix() {
        String json = cobreJson("unknown_event_type", "completed", "TXN-001");
        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, classifier.classifyWebhook(json).getClassifiedType());
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT for money_movements event with unknown status")
    void shouldReturnUnknownEventForMoneyMovementsWithUnknownStatus() {
        String json = cobreJson("money_movements.status.unknown_state", "unknown_state", "mm_999");
        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, classifier.classifyWebhook(json).getClassifiedType());
    }

    @Test
    @DisplayName("Should classify completed event as APPROVED even when content.status is missing")
    void shouldClassifyCompletedWhenStatusNodeIsMissing() {
        String json = "{\"event_id\":\"evt_888\",\"event_key\":\"money_movements.status.completed\",\"content\":{\"id\":\"mm_888\"}}";
        WebhookClassifierResult result = classifier.classifyWebhook(json);

        assertEquals(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.APPROVED, result.getTransactionStatus());
        assertEquals("mm_888", result.getProviderTransactionId());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds the JSON as it would be stored in pwe_event_request after the
     * IWebhookEventHttpMapper adapter converts the Cobre payload to ProviderMoneyMovementWebhookRequest.
     * JsonMapperUtils uses Gson with LOWER_CASE_WITH_UNDERSCORES policy.
     */
    private String cobreJson(String eventKey, String state, String transactionId) {
        return String.format(
            "{\"event_id\":\"evt_%s\",\"event_key\":\"%s\",\"content\":{\"id\":\"%s\",\"external_id\":\"ext_%s\",\"status\":{\"state\":\"%s\",\"code\":\"%s\",\"description\":\"state %s\"}}}",
            transactionId, eventKey, transactionId, transactionId, state, state, state
        );
    }
}
