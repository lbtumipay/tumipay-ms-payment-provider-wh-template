package com.tumipay.microservice.infrastructure.adapter.input.http.provider;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookEventResult;
import com.tumipay.microservice.domain.port.input.IWebhookEventUseCase;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.mapper.IWebhookEventHttpMapper;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.request.ProviderWebhookRequest;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.response.ProviderWebhookResponse;
import com.tumipay.microservice.shared.exception.BusinessException;
import com.tumipay.microservice.shared.properties.PaymentProvidersProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebhookController.
 * <p>
 * Covers: successful processing, duplicate event (409), non-duplicate business
 * exception re-propagation, technical errors, and the mapToWebhookResponse helper.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookController Unit Tests")
class ProviderWebhookControllerTest {

    @Mock private PaymentProvidersProperties paymentProvidersProperties;
    @Mock private IWebhookEventHttpMapper    webhookEventHttpMapper;
    @Mock private IWebhookEventUseCase       webhookEventUseCase;

    private ProviderWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new ProviderWebhookController(
            paymentProvidersProperties,
            webhookEventHttpMapper,
            webhookEventUseCase
        );
        lenient().when(paymentProvidersProperties.getCode()).thenReturn("PROV_001");
    }

    // ── receiveWebhookEvent — happy path ───────────────────────────────────────

    @Test
    @DisplayName("receiveWebhookEvent should return 200 OK with response when event is processed")
    void receiveWebhookEventShouldReturn200WhenProcessedSuccessfully() {

        WebhookEvent domain = webhookEvent();
        WebhookEventResult result = processedResult();
        ProviderWebhookResponse response = response("PROCESSED", "Accepted");

        when(webhookEventHttpMapper.mapToDomain(
            any(ProviderWebhookRequest.class),
            eq("PROV_001")
        )).thenReturn(Mono.just(domain));

        when(webhookEventUseCase.processWebhookEvent(domain)).thenReturn(Mono.just(result));
        when(webhookEventHttpMapper.mapToResponse(result)).thenReturn(Mono.just(response));

        StepVerifier.create(controller.receiveWebhookEvent(request()))
            .assertNext(entity -> {
                assertEquals(HttpStatus.OK, entity.getStatusCode());
                assertNotNull(entity.getBody());
                assertEquals("PROCESSED",  entity.getBody().getCode());
                assertEquals("Accepted",   entity.getBody().getMessage());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("receiveWebhookEvent should pass provider code from properties to mapper")
    void receiveWebhookEventShouldPassProviderCodeFromPropertiesToMapper() {

        WebhookEvent domain = webhookEvent();
        WebhookEventResult result = processedResult();
        ProviderWebhookResponse response = response("PROCESSED", "Accepted");

        when(webhookEventHttpMapper.mapToDomain(any(), eq("PROV_001")))
            .thenReturn(Mono.just(domain));
        when(webhookEventUseCase.processWebhookEvent(domain)).thenReturn(Mono.just(result));
        when(webhookEventHttpMapper.mapToResponse(result)).thenReturn(Mono.just(response));

        StepVerifier.create(controller.receiveWebhookEvent(request()))
            .assertNext(entity -> assertEquals(HttpStatus.OK, entity.getStatusCode()))
            .verifyComplete();

        verify(paymentProvidersProperties).getCode();
    }

    @Test
    @DisplayName("receiveWebhookEvent should pass request and provider code to mapper")
    void receiveWebhookEventShouldPassRequestAndProviderCodeToMapper() {

        WebhookEvent domain = webhookEvent();
        WebhookEventResult result = processedResult();
        ProviderWebhookResponse response = response("PROCESSED", "Accepted");

        when(webhookEventHttpMapper.mapToDomain(
            any(ProviderWebhookRequest.class),
            eq("PROV_001")
        )).thenReturn(Mono.just(domain));
        when(webhookEventUseCase.processWebhookEvent(domain)).thenReturn(Mono.just(result));
        when(webhookEventHttpMapper.mapToResponse(result)).thenReturn(Mono.just(response));

        StepVerifier.create(controller.receiveWebhookEvent(request()))
            .assertNext(entity -> assertEquals(HttpStatus.OK, entity.getStatusCode()))
            .verifyComplete();

        ArgumentCaptor<ProviderWebhookRequest> captor = ArgumentCaptor.forClass(ProviderWebhookRequest.class);
        verify(webhookEventHttpMapper).mapToDomain(
            captor.capture(), eq("PROV_001")
        );
        assertNotNull(captor.getValue());
    }

    // ── receiveWebhookEvent — duplicate webhook (409) ──────────────────────────

    @Test
    @DisplayName("receiveWebhookEvent should return 409 CONFLICT for DUPLICATE_WEBHOOK_EVENT")
    void receiveWebhookEventShouldReturn409ForDuplicateWebhookEvent() {

        when(webhookEventHttpMapper.mapToDomain(any(), any()))
            .thenReturn(Mono.error(
                new BusinessException("DUPLICATE_WEBHOOK_EVENT", "event already processed")
            ));

        StepVerifier.create(controller.receiveWebhookEvent(request()))
            .assertNext(entity -> {
                assertEquals(HttpStatus.CONFLICT, entity.getStatusCode());
                assertNotNull(entity.getBody());
                assertEquals("DUPLICATE_WEBHOOK_EVENT", entity.getBody().getCode());
                assertEquals(
                    "Message with the same idempotency key already received and processed",
                    entity.getBody().getMessage()
                );
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("receiveWebhookEvent should return 409 when use case throws DUPLICATE_WEBHOOK_EVENT")
    void receiveWebhookEventShouldReturn409WhenUseCaseThrowsDuplicate() {
        WebhookEvent domain = webhookEvent();

        when(webhookEventHttpMapper.mapToDomain(any(), any()))
            .thenReturn(Mono.just(domain));
        when(webhookEventUseCase.processWebhookEvent(domain))
            .thenReturn(Mono.error(
                new BusinessException("DUPLICATE_WEBHOOK_EVENT", "duplicate key")
            ));

        StepVerifier.create(controller.receiveWebhookEvent(request()))
            .assertNext(entity -> assertEquals(HttpStatus.CONFLICT, entity.getStatusCode()))
            .verifyComplete();
    }

    // ── receiveWebhookEvent — error propagation ────────────────────────────────

    @Test
    @DisplayName("receiveWebhookEvent should propagate non-duplicate BusinessException")
    void receiveWebhookEventShouldPropagateNonDuplicateBusinessException() {
        when(webhookEventHttpMapper.mapToDomain(any(), any()))
            .thenReturn(Mono.error(
                new BusinessException("OTHER_BIZ_ERROR", "some business error")
            ));

        StepVerifier.create(controller.receiveWebhookEvent(request()))
            .expectErrorMatches(e -> e instanceof BusinessException
                && "OTHER_BIZ_ERROR".equals(((BusinessException) e).getCode()))
            .verify();
    }

    @Test
    @DisplayName("receiveWebhookEvent should propagate technical exception from mapper")
    void receiveWebhookEventShouldPropagateTechnicalExceptionFromMapper() {
        when(webhookEventHttpMapper.mapToDomain(any(), any()))
            .thenReturn(Mono.error(new RuntimeException("mapper failure")));

        StepVerifier.create(controller.receiveWebhookEvent(request()))
            .expectErrorMatches(e -> e instanceof RuntimeException
                && "mapper failure".equals(e.getMessage()))
            .verify();
    }

    @Test
    @DisplayName("receiveWebhookEvent should propagate technical exception from use case")
    void receiveWebhookEventShouldPropagateTechnicalExceptionFromUseCase() {
        WebhookEvent domain = webhookEvent();

        when(webhookEventHttpMapper.mapToDomain(any(), any()))
            .thenReturn(Mono.just(domain));
        when(webhookEventUseCase.processWebhookEvent(domain))
            .thenReturn(Mono.error(new RuntimeException("use case failure")));

        StepVerifier.create(controller.receiveWebhookEvent(request()))
            .expectErrorMatches(e -> "use case failure".equals(e.getMessage()))
            .verify();
    }

    @Test
    @DisplayName("receiveWebhookEvent should propagate technical exception from response mapper")
    void receiveWebhookEventShouldPropagateTechnicalExceptionFromResponseMapper() {
        WebhookEvent domain = webhookEvent();
        WebhookEventResult result = processedResult();

        when(webhookEventHttpMapper.mapToDomain(any(), any()))
            .thenReturn(Mono.just(domain));
        when(webhookEventUseCase.processWebhookEvent(domain)).thenReturn(Mono.just(result));
        when(webhookEventHttpMapper.mapToResponse(result))
            .thenReturn(Mono.error(new RuntimeException("response mapper failure")));

        StepVerifier.create(controller.receiveWebhookEvent(request()))
            .expectErrorMatches(e -> "response mapper failure".equals(e.getMessage()))
            .verify();
    }

    // ── mapToWebhookResponse ───────────────────────────────────────────────────

    @Test
    @DisplayName("mapToWebhookResponse should wrap response in ResponseEntity with 200 OK")
    void mapToWebhookResponseShouldWrapResponseInOkEntity() {
        ProviderWebhookResponse response = response("PROCESSED", "Accepted");

        StepVerifier.create(controller.mapToWebhookResponse(response))
            .assertNext(entity -> {
                assertEquals(HttpStatus.OK, entity.getStatusCode());
                assertEquals(response, entity.getBody());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("mapToWebhookResponse should emit exactly one element")
    void mapToWebhookResponseShouldEmitExactlyOneElement() {
        StepVerifier.create(controller.mapToWebhookResponse(response("CODE", "MSG")))
            .expectNextCount(1)
            .verifyComplete();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private ProviderWebhookRequest request() {
        return ProviderWebhookRequest.builder()
            .eventId("EVT-EXT-001")
            .eventKey("money_movements.status.completed")
            .content(Map.of(
                "id", "TX-001",
                "status", "completed"
            ))
            .build();
    }

    private WebhookEvent webhookEvent() {
        return WebhookEvent.builder()
            .uuid("webhook-uuid-001")
            .adapterProviderCode("PROV_001")
            .eventType("PAYMENT_APPROVED")
            .idempotencyKey("PROV_001_EVT-EXT-001")
            .eventRequest("{\"provider_event_type\":\"PAYMENT_APPROVED\"}")
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED)
            .build();
    }

    private WebhookEventResult processedResult() {
        return WebhookEventResult.builder()
            .uuid("webhook-uuid-001")
            .adapterProviderCode("PROV_001")
            .eventType("PAYMENT_APPROVED")
            .processingStatus(WebhookProcessingStatusEnum.PROCESSED)
            .message("OK")
            .build();
    }

    private ProviderWebhookResponse response(String code, String message) {
        return ProviderWebhookResponse.builder()
            .code(code)
            .message(message)
            .build();
    }
}

