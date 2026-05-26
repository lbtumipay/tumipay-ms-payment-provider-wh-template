package com.tumipay.microservice.domain.component.mapper;

import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for WebhookEventMapperComponent.
 */
@DisplayName("WebhookEventMapperComponent Unit Tests")
class WebhookEventMapperComponentTest {

    private WebhookEventMapperComponent mapper;

    @BeforeEach
    void setUp() {
        mapper = new WebhookEventMapperComponent();
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT when payload is null")
    void shouldReturnUnknownEventWhenPayloadIsNull() {
        WebhookClassifierResult result = mapper.mapToClassifierResult(null);

        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, result.getClassifiedType());
        assertNull(result.getTransactionStatus());
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT when payload is invalid JSON")
    void shouldReturnUnknownEventWhenPayloadIsInvalidJson() {
        WebhookClassifierResult result = mapper.mapToClassifierResult("not-a-json");

        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, result.getClassifiedType());
        assertNull(result.getTransactionStatus());
    }

    @Test
    @DisplayName("Should classify completed event as PAYOUT_TRANSACTION_APPROVED")
    void shouldClassifyCompletedEventAsApproved() {
        String json = "{\"event_id\":\"evt_001\",\"event_key\":\"money_movements.status.completed\",\"content\":{\"id\":\"mm_001\"}}";

        WebhookClassifierResult result = mapper.mapToClassifierResult(json);

        assertEquals(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.APPROVED, result.getTransactionStatus());
        assertEquals("mm_001", result.getProviderTransactionId());
    }

    @Test
    @DisplayName("Should classify rejected event as PAYOUT_TRANSACTION_REJECTED")
    void shouldClassifyRejectedEventAsRejected() {
        String json = "{\"event_id\":\"evt_002\",\"event_key\":\"money_movements.status.rejected\",\"content\":{\"id\":\"mm_002\"}}";

        WebhookClassifierResult result = mapper.mapToClassifierResult(json);

        assertEquals(WebhookEventTypeEnum.PAYOUT_TRANSACTION_REJECTED, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.REJECTED, result.getTransactionStatus());
        assertEquals("mm_002", result.getProviderTransactionId());
    }

    @Test
    @DisplayName("Should classify initiated event as PAYOUT_TRANSACTION_PENDING")
    void shouldClassifyInitiatedEventAsPending() {
        String json = "{\"event_id\":\"evt_003\",\"event_key\":\"money_movements.status.initiated\",\"content\":{\"id\":123}}";

        WebhookClassifierResult result = mapper.mapToClassifierResult(json);

        assertEquals(WebhookEventTypeEnum.PAYOUT_TRANSACTION_PENDING, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.PENDING, result.getTransactionStatus());

        assertEquals("123.0", result.getProviderTransactionId());
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT and ERROR when event key prefix is not supported")
    void shouldReturnUnknownEventForUnsupportedEventPrefix() {
        String json = "{\"event_id\":\"evt_004\",\"event_key\":\"payment.status.completed\",\"content\":{\"id\":\"mm_004\"}}";

        WebhookClassifierResult result = mapper.mapToClassifierResult(json);

        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.ERROR, result.getTransactionStatus());
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT and ERROR when money_movements status is unknown")
    void shouldReturnUnknownEventForUnknownMoneyMovementsStatus() {
        String json = "{\"event_id\":\"evt_005\",\"event_key\":\"money_movements.status.unknown\",\"content\":{\"id\":\"mm_005\"}}";

        WebhookClassifierResult result = mapper.mapToClassifierResult(json);

        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.ERROR, result.getTransactionStatus());
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT and ERROR when content is empty")
    void shouldReturnUnknownEventWhenContentIsEmpty() {
        String json = "{\"event_id\":\"evt_006\",\"event_key\":\"money_movements.status.completed\",\"content\":{}}";

        WebhookClassifierResult result = mapper.mapToClassifierResult(json);

        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.ERROR, result.getTransactionStatus());
    }

    @Test
    @DisplayName("Should return UNKNOWN_EVENT and ERROR when content is null")
    void shouldReturnUnknownEventWhenContentIsNull() {
        String json = "{\"event_id\":\"evt_007\",\"event_key\":\"money_movements.status.completed\",\"content\":null}";

        WebhookClassifierResult result = mapper.mapToClassifierResult(json);

        assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT, result.getClassifiedType());
        assertEquals(TransactionStatusEnum.ERROR, result.getTransactionStatus());
    }
}

