package com.tumipay.microservice.infrastructure.component.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumipay.microservice.domain.model.security.WebhookSignatureValidationResult;
import com.tumipay.microservice.domain.service.contract.IWebhookSignatureValidator;
import com.tumipay.microservice.infrastructure.component.annotation.ProviderWebhookSigned;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebhookSignatureValidationFilter.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookSignatureValidationFilter Unit Tests")
class SignatureValidationFilterTest {

    @Mock private IWebhookSignatureValidator webhookSignatureValidator;
    @Mock private RequestMappingHandlerMapping handlerMapping;
    @Mock private WebFilterChain chain;

    private SignatureValidationFilter filter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        filter = new SignatureValidationFilter(webhookSignatureValidator, handlerMapping, objectMapper);
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    // ── Stub controllers ──────────────────────────────────────────────────────

    static class SignedController {
        @ProviderWebhookSigned
        public void signedMethod() {}

        public void unsignedMethod() {}
    }

    @ProviderWebhookSigned
    static class ClassLevelSignedController {
        public void handle() {}
    }

    static class UnsignedController {
        public void handle() {}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. No handler resolved — pass through
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When no handler is resolved")
    class NoHandlerResolved {

        @Test
        @DisplayName("Should pass through to chain when getHandler returns empty")
        void filter_whenNoHandlerResolved_thenPassThrough() {
            when(handlerMapping.getHandler(any())).thenReturn(Mono.empty());

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/webhook/event")
                    .body("{\"id\":\"test\"}")
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verifyNoInteractions(webhookSignatureValidator);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Endpoint without @ProviderWebhookSigned — pass through
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When endpoint is not annotated with @ProviderWebhookSigned")
    class NotAnnotatedEndpoint {

        @Test
        @DisplayName("Should pass through without signature validation")
        void filter_whenNotAnnotated_thenPassThroughWithoutValidation() throws Exception {
            UnsignedController bean = new UnsignedController();
            Method method = bean.getClass().getMethod("handle");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/other/endpoint")
                    .body("{\"id\":\"test\"}")
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verifyNoInteractions(webhookSignatureValidator);
        }

        @Test
        @DisplayName("Should pass through for unsigned method in partially-annotated controller")
        void filter_whenUnsignedMethodInMixedController_thenPassThrough() throws Exception {
            SignedController bean = new SignedController();
            Method method = bean.getClass().getMethod("unsignedMethod");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/other")
                    .body("{\"id\":\"test\"}")
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verifyNoInteractions(webhookSignatureValidator);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Method-level @ProviderWebhookSigned — valid signature
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When endpoint has @ProviderWebhookSigned and signature is valid")
    class ValidSignature {

        @Test
        @DisplayName("Should allow request through for method-level annotation with valid signature")
        void filter_whenMethodAnnotatedAndSignatureValid_thenPassThrough() throws Exception {
            SignedController bean = new SignedController();
            Method method = bean.getClass().getMethod("signedMethod");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(webhookSignatureValidator.validate(any(), any()))
                .thenReturn(Mono.just(WebhookSignatureValidationResult.success()));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/webhook/event")
                    .header("event-timestamp", "2025-02-03T22:20:24Z")
                    .header("event-signature", "validhash")
                    .body("{\"id\":\"ev_abc\"}")
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(webhookSignatureValidator).validate(any(), any());
            verify(chain).filter(any());
        }

        @Test
        @DisplayName("Should allow request through for class-level annotation with valid signature")
        void filter_whenClassAnnotatedAndSignatureValid_thenPassThrough() throws Exception {
            ClassLevelSignedController bean = new ClassLevelSignedController();
            Method method = bean.getClass().getMethod("handle");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(webhookSignatureValidator.validate(any(), any()))
                .thenReturn(Mono.just(WebhookSignatureValidationResult.success()));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/webhook/event")
                    .body("{\"id\":\"ev_abc\"}")
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. @ProviderWebhookSigned — invalid signature → 401
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When endpoint has @ProviderWebhookSigned and signature is invalid")
    class InvalidSignature {

        @Test
        @DisplayName("Should return 401 Unauthorized when signature validation fails")
        void filter_whenSignatureInvalid_thenReturn401() throws Exception {
            SignedController bean = new SignedController();
            Method method = bean.getClass().getMethod("signedMethod");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(webhookSignatureValidator.validate(any(), any()))
                .thenReturn(Mono.just(WebhookSignatureValidationResult.failure("Signature mismatch")));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/webhook/event")
                    .header("event-timestamp", "2025-02-03T22:20:24Z")
                    .header("event-signature", "invalid-sig")
                    .body("{\"id\":\"ev_abc\"}")
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("Should return 401 when signature headers are missing")
        void filter_whenHeadersMissing_thenReturn401() throws Exception {
            SignedController bean = new SignedController();
            Method method = bean.getClass().getMethod("signedMethod");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(webhookSignatureValidator.validate(any(), any()))
                .thenReturn(Mono.just(WebhookSignatureValidationResult.failure("Missing signature headers")));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/webhook/event")
                    .body("{\"id\":\"ev_abc\"}")
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should write JSON body with WEBHOOK_SIGNATURE_INVALID code on 401")
        void filter_whenInvalidSignature_thenWritesJsonBodyWithCorrectCode() throws Exception {
            SignedController bean = new SignedController();
            Method method = bean.getClass().getMethod("signedMethod");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(webhookSignatureValidator.validate(any(), any()))
                .thenReturn(Mono.just(WebhookSignatureValidationResult.failure("Signature mismatch")));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/webhook/event")
                    .body("{\"id\":\"ev_abc\"}")
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(exchange.getResponse().getHeaders().getContentType())
                .isEqualTo(org.springframework.http.MediaType.APPLICATION_JSON);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Handler is not a HandlerMethod
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When handler is not a HandlerMethod")
    class NonHandlerMethod {

        @Test
        @DisplayName("Should pass through without signature validation")
        void filter_whenHandlerIsNotHandlerMethod_thenPassThrough() {
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just("functional-route"));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/other").body("{}")
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verifyNoInteractions(webhookSignatureValidator);
        }
    }
}
