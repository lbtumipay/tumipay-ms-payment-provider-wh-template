package com.tumipay.microservice.infrastructure.adapter.output.http.standard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.response.GatewayWebhookResponse;
import com.tumipay.microservice.infrastructure.component.properties.PaymentGatewayProperties;
import com.tumipay.microservice.shared.exception.GatewayWebhookException;
import com.tumipay.microservice.shared.properties.WebClientProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * PaymentGatewayWebhookAdapterTest
 * <p>
 * Unit tests for {@link PaymentGatewayWebhookAdapter}.
 * Verifies that the adapter correctly maps Gateway HTTP responses
 * to the {@link GatewayWebhookResponse} contract defined in the API spec.
 * <p>
 * HTTP scenarios covered:
 * <ul>
 *   <li>200 OK  → {@code code=PROCESS_COMPLETED}, {@code status=SUCCESS}, {@code data} present.</li>
 *   <li>202 Accepted → same as 200.</li>
 *   <li>409 Conflict with body → {@code code=DUPLICATE_EVENT}, {@code status=FAILED}.</li>
 *   <li>409 Conflict with empty body → synthetic {@code DUPLICATE_EVENT} response.</li>
 *   <li>400 Bad Request → {@link GatewayWebhookException} with CLIENT_ERROR code.</li>
 *   <li>500 Internal Server Error → {@link GatewayWebhookException} with SERVER_ERROR code.</li>
 * </ul>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 14/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentGatewayWebhookAdapter Unit Tests")
class PaymentGatewayWebhookAdapterTest {

    // ─── Constants ────────────────────────────────────────────────────────────

    private static final String GATEWAY_BASE_URL = "http://localhost:9090";
    private static final String WEBHOOK_PATH     = "/v1/webhook/payment-event";
    private static final String GATEWAY_API_KEY  = "test-api-key-secret";
    private static final String PROVIDER_CODE    = "TUMIPAY_TEST_PROVIDER";
    private static final String EVENT_UUID       = "550e8400-e29b-41d4-a716-446655440000";
    private static final String IDEMPOTENCY_KEY  = "idem-key-001";

    // ─── Mocks ────────────────────────────────────────────────────────────────

    @Mock
    private PaymentGatewayProperties paymentGatewayProperties;

    @Mock
    private PaymentGatewayProperties.GatewayEndpoints gatewayEndpoints;


    @Mock
    private WebClientProperties webClientProperties;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules();          // registers JavaTimeModule for Instant serialization

    private PaymentGatewayWebhookAdapter adapter;

    // ─── Helpers ─────────────────────────────────────────────────────────────

    @BeforeEach
    void mockGatewayProperties() {
        when(paymentGatewayProperties.getBaseUrl()).thenReturn(GATEWAY_BASE_URL);
        when(paymentGatewayProperties.getApiKey()).thenReturn(GATEWAY_API_KEY);
        when(paymentGatewayProperties.getEndpoints()).thenReturn(gatewayEndpoints);
        when(gatewayEndpoints.getWebhookEventPath()).thenReturn(WEBHOOK_PATH);
        when(paymentGatewayProperties.getTimeout()).thenReturn(5_000);
    }

    /**
     * Builds the adapter wiring a {@code WebClient} backed by a stub {@code ExchangeFunction}
     * that returns the given {@code responseBody} and HTTP {@code status}.
     */
    private PaymentGatewayWebhookAdapter adapterWith(String responseBody, HttpStatus status) {
        final WebClient stubWebClient = WebClient.builder()
            .exchangeFunction(request -> Mono.just(
                ClientResponse.create(status)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(responseBody)
                    .build()
            ))
            .build();

        return new PaymentGatewayWebhookAdapter(
            paymentGatewayProperties,
            stubWebClient,
            webClientProperties
        );
    }

    private WebhookEvent buildWebhookEvent() {
        return WebhookEvent.builder()
            .uuid(EVENT_UUID)
            .adapterProviderCode(PROVIDER_CODE)
            .eventType("PAYIN_TRANSACTION_APPROVED")
            .idempotencyKey(IDEMPOTENCY_KEY)
            .eventRequest("{\"status\":\"APPROVED\",\"amount\":150000}")
            .receivedAt(Instant.parse("2026-04-14T10:30:00Z"))
            .build();
    }

    // ─── HTTP 200 - ProcessCompleted ─────────────────────────────────────────

    @Test
    @DisplayName("HTTP 200 — code should be PROCESS_COMPLETED and status SUCCESS")
    void dispatchWebhookEvent_http200_codeAndStatusAreCorrect() throws Exception {
        GatewayWebhookResponse payload = GatewayWebhookResponse.builder()
            .code("PROCESS_COMPLETED")
            .status("SUCCESS")
            .message("Operation completed successfully")
            .data(GatewayWebhookResponse.GatewayWebhookResponseData.builder()
                .gatewayEventId("gw-evt-" + EVENT_UUID)
                .eventId(EVENT_UUID)
                .build())
            .build();

        adapter = adapterWith(objectMapper.writeValueAsString(payload), HttpStatus.OK);

        StepVerifier.create(adapter.dispatchWebhookEvent(buildWebhookEvent()))
            .assertNext(response -> {
                assertEquals("PROCESS_COMPLETED", response.getCode());
                assertEquals("SUCCESS", response.getStatus());
                assertEquals("Operation completed successfully", response.getMessage());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("HTTP 200 — data.gatewayEventId and data.eventId should be mapped correctly")
    void dispatchWebhookEvent_http200_dataIsMappedCorrectly() throws Exception {
        GatewayWebhookResponse payload = GatewayWebhookResponse.builder()
            .code("PROCESS_COMPLETED")
            .status("SUCCESS")
            .message("Operation completed successfully")
            .data(GatewayWebhookResponse.GatewayWebhookResponseData.builder()
                .gatewayEventId("gw-evt-" + EVENT_UUID)
                .eventId(EVENT_UUID)
                .build())
            .build();

        adapter = adapterWith(objectMapper.writeValueAsString(payload), HttpStatus.OK);

        StepVerifier.create(adapter.dispatchWebhookEvent(buildWebhookEvent()))
            .assertNext(response -> {
                assertNotNull(response.getData());
                assertEquals("gw-evt-" + EVENT_UUID, response.getData().getGatewayEventId());
                assertEquals(EVENT_UUID, response.getData().getEventId());
            })
            .verifyComplete();
    }

    // ─── HTTP 202 - Accepted ─────────────────────────────────────────────────

    @Test
    @DisplayName("HTTP 202 — code should be PROCESS_COMPLETED and status SUCCESS")
    void dispatchWebhookEvent_http202_codeAndStatusAreCorrect() throws Exception {
        GatewayWebhookResponse payload = GatewayWebhookResponse.builder()
            .code("PROCESS_COMPLETED")
            .status("SUCCESS")
            .message("Operation accepted")
            .build();

        adapter = adapterWith(objectMapper.writeValueAsString(payload), HttpStatus.ACCEPTED);

        StepVerifier.create(adapter.dispatchWebhookEvent(buildWebhookEvent()))
            .assertNext(response -> {
                assertEquals("PROCESS_COMPLETED", response.getCode());
                assertEquals("SUCCESS", response.getStatus());
            })
            .verifyComplete();
    }

    // ─── HTTP 409 - DuplicateEvent ────────────────────────────────────────────

    @Test
    @DisplayName("HTTP 409 with body — code should be DUPLICATE_EVENT and status FAILED")
    void dispatchWebhookEvent_http409WithBody_codeIsDuplicateEvent() throws Exception {
        GatewayWebhookResponse payload = GatewayWebhookResponse.builder()
            .code("DUPLICATE_EVENT")
            .status("FAILED")
            .message("Duplicate event detected for idempotency_key " + IDEMPOTENCY_KEY)
            .build();

        adapter = adapterWith(objectMapper.writeValueAsString(payload), HttpStatus.CONFLICT);

        StepVerifier.create(adapter.dispatchWebhookEvent(buildWebhookEvent()))
            .assertNext(response -> {
                assertEquals("DUPLICATE_EVENT", response.getCode());
                assertEquals("FAILED", response.getStatus());
                assertNull(response.getData());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("HTTP 409 with empty body — synthetic response should have DUPLICATE_EVENT code")
    void dispatchWebhookEvent_http409EmptyBody_syntheticResponseHasDuplicateEventCode() {
        adapter = adapterWith("", HttpStatus.CONFLICT);

        StepVerifier.create(adapter.dispatchWebhookEvent(buildWebhookEvent()))
            .assertNext(response -> {
                assertEquals("DUPLICATE_EVENT", response.getCode());
                assertEquals("FAILED", response.getStatus());
                assertNull(response.getData());
                assertNotNull(response.getMessage());
            })
            .verifyComplete();
    }

    // ─── HTTP 4xx / 5xx — Error handling ─────────────────────────────────────

    @Test
    @DisplayName("HTTP 400 — should throw GatewayWebhookException with GATEWAY_CLIENT_ERROR_400 code")
    void dispatchWebhookEvent_http400_throwsGatewayClientError() {
        adapter = adapterWith("{\"code\":\"VALIDATION_ERROR\",\"status\":\"ERROR\"}", HttpStatus.BAD_REQUEST);

        StepVerifier.create(adapter.dispatchWebhookEvent(buildWebhookEvent()))
            .expectErrorSatisfies(error -> {
                assertInstanceOf(GatewayWebhookException.class, error);
                assertEquals("GATEWAY_CLIENT_ERROR_400", ((GatewayWebhookException) error).getCode());
            })
            .verify();
    }

    @Test
    @DisplayName("HTTP 401 — should throw GatewayWebhookException with GATEWAY_CLIENT_ERROR_401 code")
    void dispatchWebhookEvent_http401_throwsGatewayClientError() {
        adapter = adapterWith("{\"code\":\"UNAUTHORIZED\",\"status\":\"ERROR\"}", HttpStatus.UNAUTHORIZED);

        StepVerifier.create(adapter.dispatchWebhookEvent(buildWebhookEvent()))
            .expectErrorSatisfies(error -> {
                assertInstanceOf(GatewayWebhookException.class, error);
                assertEquals("GATEWAY_CLIENT_ERROR_401", ((GatewayWebhookException) error).getCode());
            })
            .verify();
    }

    @Test
    @DisplayName("HTTP 500 — should throw GatewayWebhookException with GATEWAY_SERVER_ERROR_500 code")
    void dispatchWebhookEvent_http500_throwsGatewayServerError() {
        adapter = adapterWith("{\"code\":\"INTERNAL_ERROR\",\"status\":\"ERROR\"}", HttpStatus.INTERNAL_SERVER_ERROR);

        StepVerifier.create(adapter.dispatchWebhookEvent(buildWebhookEvent()))
            .expectErrorSatisfies(error -> {
                assertInstanceOf(GatewayWebhookException.class, error);
                assertEquals("GATEWAY_SERVER_ERROR_500", ((GatewayWebhookException) error).getCode());
            })
            .verify();
    }

    @Test
    @DisplayName("HTTP 503 — should throw GatewayWebhookException with GATEWAY_SERVER_ERROR_503 code")
    void dispatchWebhookEvent_http503_throwsGatewayServerError() {
        adapter = adapterWith("{\"code\":\"SERVICE_UNAVAILABLE\",\"status\":\"ERROR\"}", HttpStatus.SERVICE_UNAVAILABLE);

        StepVerifier.create(adapter.dispatchWebhookEvent(buildWebhookEvent()))
            .expectErrorSatisfies(error -> {
                assertInstanceOf(GatewayWebhookException.class, error);
                assertEquals("GATEWAY_SERVER_ERROR_503", ((GatewayWebhookException) error).getCode());
            })
            .verify();
    }
}

