package com.tumipay.microservice.infrastructure.component.filter;

import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import com.tumipay.microservice.infrastructure.component.decorator.HttpRequestDecorator;
import com.tumipay.microservice.infrastructure.component.decorator.HttpResponseDecorator;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for HttpLoggerFilter.
 * <p>
 * Verifies health-endpoint bypass, header extraction, MDC population,
 * exchange decoration and reactive chain delegation.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HttpLoggerFilter Unit Tests")
class HttpLoggerFilterTest {

    @Mock
    private WebFilterChain chain;

    private HttpLoggerFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HttpLoggerFilter();
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @AfterEach
    void tearDown() {
        ThreadContext.clearAll();
    }

    // ── Health endpoint bypass ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should bypass logging for actuator endpoints without decorating exchange")
    void shouldBypassLoggingForActuatorEndpoints() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/actuator/health").build()
        );

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        // Exchange passed to chain must NOT have a decorated request
        assertFalse(captor.getValue().getRequest() instanceof HttpRequestDecorator,
            "Health endpoints must bypass decoration");
    }

    @Test
    @DisplayName("Should bypass logging for swagger-ui endpoints")
    void shouldBypassLoggingForSwaggerUiEndpoints() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/swagger-ui/index.html").build()
        );

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    @DisplayName("Should bypass logging for v3 api-docs endpoint")
    void shouldBypassLoggingForV3ApiDocsEndpoint() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/v3/api-docs").build()
        );

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(any());
    }

    // ── Header extraction ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should use merchantId and requestId from request headers when present")
    void shouldUseMerchantIdAndRequestIdFromHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/tp/payment/adapter/payin/transaction")
                .header(BaseIntegrationConstant.HEADER_MERCHANT_ID, "MERCHANT-123")
                .header(BaseIntegrationConstant.HEADER_REQUEST_ID,  "REQ-456")
                .build()
        );

        filter.filter(exchange, chain).block();

        assertEquals("MERCHANT-123", ThreadContext.get(BaseIntegrationConstant.KEY_MERCHANT_ID));
        assertEquals("REQ-456",      ThreadContext.get(BaseIntegrationConstant.KEY_REQUEST_ID));
    }

    @Test
    @DisplayName("Should use fallback merchantId-not-found when X-Merchant-ID header is absent")
    void shouldUseFallbackMerchantIdWhenHeaderIsAbsent() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/tp/payment/adapter/payin/transaction").build()
        );

        filter.filter(exchange, chain).block();

        assertEquals("merchantId-not-found",
            ThreadContext.get(BaseIntegrationConstant.KEY_MERCHANT_ID));
    }

    @Test
    @DisplayName("Should generate TUMIPAY-prefixed requestId when X-Request-ID header is absent")
    void shouldGenerateTumiPayRequestIdWhenHeaderIsAbsent() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/tp/payment/adapter/payin/transaction").build()
        );

        filter.filter(exchange, chain).block();

        String requestId = ThreadContext.get(BaseIntegrationConstant.KEY_REQUEST_ID);
        assertNotNull(requestId);
        assertTrue(requestId.startsWith("TUMIPAY-"),
            "Auto-generated requestId must have TUMIPAY- prefix");
    }

    // ── ThreadContext (MDC) population ─────────────────────────────────────────

    @Test
    @DisplayName("Should populate operationId PAY_IN_TRANSACTION for payin path")
    void shouldPopulateOperationIdForPayinPath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/tp/payment/adapter/payin/transaction").build()
        );

        filter.filter(exchange, chain).block();

        assertEquals("PAY_IN_TRANSACTION",
            ThreadContext.get(BaseIntegrationConstant.KEY_OPERATION_ID));
    }

    @Test
    @DisplayName("Should populate all three MDC keys with provided header values")
    void shouldPopulateAllThreeMdcKeys() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/tp/payment/adapter/payin/transaction")
                .header(BaseIntegrationConstant.HEADER_MERCHANT_ID, "M-001")
                .header(BaseIntegrationConstant.HEADER_REQUEST_ID,  "R-001")
                .build()
        );

        filter.filter(exchange, chain).block();

        assertEquals("R-001",              ThreadContext.get(BaseIntegrationConstant.KEY_REQUEST_ID));
        assertEquals("M-001",              ThreadContext.get(BaseIntegrationConstant.KEY_MERCHANT_ID));
        assertEquals("PAY_IN_TRANSACTION", ThreadContext.get(BaseIntegrationConstant.KEY_OPERATION_ID));
    }

    // ── Exchange decoration ────────────────────────────────────────────────────

    @Test
    @DisplayName("Should decorate request and response for non-health endpoints")
    void shouldDecorateRequestAndResponseForNonHealthEndpoints() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/tp/payment/adapter/payin/transaction").build()
        );

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        ServerWebExchange captured = captor.getValue();
        assertInstanceOf(HttpRequestDecorator.class,  captured.getRequest(),
            "Request must be wrapped with HttpRequestDecorator");
        assertInstanceOf(HttpResponseDecorator.class, captured.getResponse(),
            "Response must be wrapped with HttpResponseDecorator");
    }

    // ── Reactive context propagation ───────────────────────────────────────────

    @Test
    @DisplayName("Should write requestId merchantId and operationId into reactive context")
    void shouldWriteCorrelationKeysIntoReactiveContext() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/tp/payment/adapter/payin/transaction")
                .header(BaseIntegrationConstant.HEADER_MERCHANT_ID, "M-CTX")
                .header(BaseIntegrationConstant.HEADER_REQUEST_ID,  "R-CTX")
                .build()
        );

        // Capture reactive context by subscribing with deferContextual nested within the chain
        when(chain.filter(any())).thenReturn(
            Mono.deferContextual(ctx -> {
                assertEquals("R-CTX",              ctx.getOrDefault(BaseIntegrationConstant.KEY_REQUEST_ID,  ""));
                assertEquals("M-CTX",              ctx.getOrDefault(BaseIntegrationConstant.KEY_MERCHANT_ID, ""));
                assertEquals("PAY_IN_TRANSACTION", ctx.getOrDefault(BaseIntegrationConstant.KEY_OPERATION_ID, ""));
                return Mono.empty();
            })
        );

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();
    }
}

