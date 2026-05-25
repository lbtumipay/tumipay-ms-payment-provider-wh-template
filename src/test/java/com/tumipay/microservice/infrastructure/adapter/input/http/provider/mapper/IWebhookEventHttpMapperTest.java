package com.tumipay.microservice.infrastructure.adapter.input.http.provider.mapper;

import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEventResult;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.request.ProviderWebhookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IWebhookEventHttpMapperTest2
 * <p>
 * IWebhookEventHttpMapperTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 25/04/2026
 */
class IWebhookEventHttpMapperTest {

    private IWebhookEventHttpMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IWebhookEventHttpMapper() {
        };
    }

    @Test
    void shouldMapToDomainUsingRequestEventIdWhenPresent() {
        ProviderWebhookRequest request = cobreRequest("money_movements.status.completed", "mm_12345", "completed");
        request.setEventId("evt-123");

        StepVerifier.create(mapper.mapToDomain(request, "TP_PROVIDER"))
            .assertNext(domain -> {
                assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT.toString(), domain.getEventType());
                assertEquals("evt-123", domain.getExternalEventId());
                assertEquals("TP_PROVIDER_evt-123", domain.getIdempotencyKey());
                assertNotNull(domain.getEventRequest());
            })
            .verifyComplete();
    }

    @Test
    void shouldSetUnknownEventTypeEvenWhenEventKeyIsPresent() {
        ProviderWebhookRequest request = cobreRequest("money_movements.status.completed", "mm_12345", "completed");

        StepVerifier.create(mapper.mapToDomain(request, "TP_PROVIDER"))
            .assertNext(domain -> assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT.toString(), domain.getEventType()))
            .verifyComplete();
    }

    @Test
    void shouldAdaptContentIdToTransactionIdInSerializedEventRequest() {
        ProviderWebhookRequest request = cobreRequest("money_movements.status.completed", "mm_12345", "completed");

        StepVerifier.create(mapper.mapToDomain(request, "TP_PROVIDER"))
            .assertNext(domain -> {
                assertNotNull(domain.getEventRequest());
                assertTrue(domain.getEventRequest().contains("mm_12345"));
            })
            .verifyComplete();
    }

    @Test
    void shouldGenerateUuidAsExternalEventIdWhenEventIdIsMissing() {
        ProviderWebhookRequest request = cobreRequest("money_movements.status.processing", "mm_999", "processing");

        StepVerifier.create(mapper.mapToDomain(request, "TP_PROVIDER"))
            .assertNext(domain -> {
                assertNotNull(domain.getExternalEventId());
                assertFalse(domain.getExternalEventId().isBlank());
                assertDoesNotThrow(() -> java.util.UUID.fromString(domain.getExternalEventId()));
                assertTrue(domain.getIdempotencyKey().startsWith("TP_PROVIDER_"));
            })
            .verifyComplete();
    }

    @Test
    void shouldResolveUnknownEventWhenEventKeyIsBlank() {
        ProviderWebhookRequest request = ProviderWebhookRequest.builder()
            .eventKey("")
            .build();

        StepVerifier.create(mapper.mapToDomain(request, "TP_PROVIDER"))
            .assertNext(domain -> assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT.toString(), domain.getEventType()))
            .verifyComplete();
    }

    @Test
    void shouldGenerateExternalEventIdAndIdempotencyWhenRequestIsNull() {

        StepVerifier.create(mapper.mapToDomain(null, "TP_PROVIDER"))
            .assertNext(domain -> {
                assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT.toString(), domain.getEventType());
                assertNotNull(domain.getExternalEventId());
                assertFalse(domain.getExternalEventId().isBlank());
                assertDoesNotThrow(() -> java.util.UUID.fromString(domain.getExternalEventId()));
                assertEquals("TP_PROVIDER_" + domain.getExternalEventId(), domain.getIdempotencyKey());
            })
            .verifyComplete();
    }

    @Test
    void shouldGenerateUuidAsExternalEventIdWhenEventIdIsBlank() {
        ProviderWebhookRequest request = ProviderWebhookRequest.builder()
            .eventId("")
            .eventKey("money_movements.status.completed")
            .build();

        StepVerifier.create(mapper.mapToDomain(request, "TP_PROVIDER"))
            .assertNext(domain -> {
                assertEquals(WebhookEventTypeEnum.UNKNOWN_EVENT.toString(), domain.getEventType());
                assertNotNull(domain.getExternalEventId());
                assertFalse(domain.getExternalEventId().isBlank());
                assertDoesNotThrow(() -> java.util.UUID.fromString(domain.getExternalEventId()));
                assertNotNull(domain.getIdempotencyKey());
                assertEquals("TP_PROVIDER_" + domain.getExternalEventId(), domain.getIdempotencyKey());
            })
            .verifyComplete();
    }

    @Test
    void shouldMapErrorResponseWhenProcessingStatusIsNull() {
        WebhookEventResult result = WebhookEventResult.builder().processingStatus(null).build();

        StepVerifier.create(mapper.mapToResponse(result))
            .assertNext(response -> {
                assertEquals("ERROR", response.getCode());
                assertEquals("Error processing webhook event", response.getMessage());
            })
            .verifyComplete();
    }

    @Test
    void shouldMapFailedResponseWhenProcessingStatusIsFailed() {
        WebhookEventResult result = WebhookEventResult.builder()
            .processingStatus(WebhookProcessingStatusEnum.FAILED)
            .message("provider rejected")
            .build();

        StepVerifier.create(mapper.mapToResponse(result))
            .assertNext(response -> {
                assertEquals("FAILED", response.getCode());
                assertEquals("Failed to process webhook event: provider rejected", response.getMessage());
            })
            .verifyComplete();
    }

    @Test
    void shouldMapProcessedResponseWhenStatusIsNotFailed() {
        WebhookEventResult result = WebhookEventResult.builder()
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED)
            .build();

        StepVerifier.create(mapper.mapToResponse(result))
            .assertNext(response -> {
                assertEquals("PROCESSED", response.getCode());
                assertEquals("Accepted", response.getMessage());
            })
            .verifyComplete();
    }

    private ProviderWebhookRequest cobreRequest(String eventKey, String transactionId, String status) {
        Map<String, Object> content = new HashMap<>();
        content.put("id", transactionId);
        content.put("status", status);

        return ProviderWebhookRequest.builder()
            .eventKey(eventKey)
            .content(content)
            .build();
    }
}