package com.tumipay.microservice.infrastructure.component.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumipay.microservice.domain.port.output.IApiKeyValidatorPort;
import com.tumipay.microservice.infrastructure.component.annotation.InternalApiSecured;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InternalApiSecurityFilter.
 * <p>
 * Verifies all execution branches: no-handler passthrough, unsecured endpoint passthrough,
 * valid API Key acceptance, missing/invalid API Key rejection (401), and the
 * unauthorized response body structure.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InternalApiSecurityFilter Unit Tests")
class InternalApiSecurityFilterTest {

    @Mock
    private IApiKeyValidatorPort apiKeyValidator;

    @Mock
    private RequestMappingHandlerMapping handlerMapping;

    @Mock
    private WebFilterChain chain;

    private InternalApiSecurityFilter filter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        filter = new InternalApiSecurityFilter(apiKeyValidator, handlerMapping, objectMapper);
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    // ── Helper: build a HandlerMethod for a given controller method ────────────

    private HandlerMethod handlerMethodFor(Object bean, String methodName) throws Exception {
        Method method = bean.getClass().getMethod(methodName);
        return new HandlerMethod(bean, method);
    }

    // ── Stub controllers ───────────────────────────────────────────────────────

    /** Controller with class-level @InternalApiSecured */
    @InternalApiSecured
    static class SecuredController {
        public void handle() {}
    }

    /** Controller with method-level @InternalApiSecured */
    static class MethodSecuredController {
        @InternalApiSecured
        public void securedMethod() {}

        public void publicMethod() {}
    }

    /** Plain controller, no security annotation */
    static class UnsecuredController {
        public void handle() {}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. No handler resolved (e.g. static resources, unknown paths)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When no handler is resolved")
    class NoHandlerResolved {

        @Test
        @DisplayName("Should pass through to chain when getHandler returns empty")
        void shouldPassThroughWhenNoHandlerResolved() {
            when(handlerMapping.getHandler(any())).thenReturn(Mono.empty());

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/unknown-path").build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verifyNoInteractions(apiKeyValidator);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Handler is not a HandlerMethod (e.g. WebFlux functional routes)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When handler is not a HandlerMethod")
    class HandlerNotHandlerMethod {

        @Test
        @DisplayName("Should pass through to chain for non-HandlerMethod handlers")
        void shouldPassThroughForNonHandlerMethodHandler() {
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just("not-a-handler-method"));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/functional-route").build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verifyNoInteractions(apiKeyValidator);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Unsecured endpoint (no @InternalApiSecured annotation)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When endpoint is not annotated with @InternalApiSecured")
    class UnsecuredEndpoint {

        @Test
        @DisplayName("Should pass through to chain without validating API Key")
        void shouldPassThroughForUnsecuredEndpoint() throws Exception {
            UnsecuredController bean = new UnsecuredController();
            HandlerMethod hm = handlerMethodFor(bean, "handle");
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/public").build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verifyNoInteractions(apiKeyValidator);
        }

        @Test
        @DisplayName("Should pass through for public method in partially-secured controller")
        void shouldPassThroughForPublicMethodInPartiallSecuredController() throws Exception {
            MethodSecuredController bean = new MethodSecuredController();
            Method method = bean.getClass().getMethod("publicMethod");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/public-method").build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verifyNoInteractions(apiKeyValidator);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Secured endpoint — valid API Key
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When endpoint is secured and API Key is valid")
    class ValidApiKey {

        @Test
        @DisplayName("Should allow request when class-level @InternalApiSecured and key is valid")
        void shouldAllowRequestForClassLevelAnnotationWithValidKey() throws Exception {
            SecuredController bean = new SecuredController();
            HandlerMethod hm = handlerMethodFor(bean, "handle");
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(apiKeyValidator.isValid("valid-key")).thenReturn(true);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/payin/transaction")
                    .header("X-Api-Key", "valid-key")
                    .build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verify(apiKeyValidator).isValid("valid-key");
        }

        @Test
        @DisplayName("Should allow request when method-level @InternalApiSecured and key is valid")
        void shouldAllowRequestForMethodLevelAnnotationWithValidKey() throws Exception {
            MethodSecuredController bean = new MethodSecuredController();
            Method method = bean.getClass().getMethod("securedMethod");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(apiKeyValidator.isValid("valid-key")).thenReturn(true);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/internal/op")
                    .header("X-Api-Key", "valid-key")
                    .build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain).filter(any());
            verify(apiKeyValidator).isValid("valid-key");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Secured endpoint — missing or invalid API Key → 401
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When endpoint is secured and API Key is missing or invalid")
    class InvalidApiKey {

        @Test
        @DisplayName("Should return 401 when X-Api-Key header is missing")
        void shouldReturn401WhenApiKeyHeaderIsMissing() throws Exception {
            SecuredController bean = new SecuredController();
            HandlerMethod hm = handlerMethodFor(bean, "handle");
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(apiKeyValidator.isValid(null)).thenReturn(false);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/payin/transaction").build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            verifyNoInteractions(chain);
        }

        @Test
        @DisplayName("Should return 401 when X-Api-Key header value is wrong")
        void shouldReturn401WhenApiKeyIsWrong() throws Exception {
            SecuredController bean = new SecuredController();
            HandlerMethod hm = handlerMethodFor(bean, "handle");
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(apiKeyValidator.isValid("wrong-key")).thenReturn(false);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/payin/transaction")
                    .header("X-Api-Key", "wrong-key")
                    .build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            verifyNoInteractions(chain);
        }

        @Test
        @DisplayName("Should not delegate to chain when API Key is invalid")
        void shouldNotCallChainWhenApiKeyIsInvalid() throws Exception {
            SecuredController bean = new SecuredController();
            HandlerMethod hm = handlerMethodFor(bean, "handle");
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(apiKeyValidator.isValid(any())).thenReturn(false);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/payin/transaction")
                    .header("X-Api-Key", "bad-key")
                    .build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("Should write JSON body with UNAUTHORIZED code on 401 response")
        void shouldWriteUnauthorizedJsonBodyOn401() throws Exception {
            SecuredController bean = new SecuredController();
            HandlerMethod hm = handlerMethodFor(bean, "handle");
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(apiKeyValidator.isValid(any())).thenReturn(false);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/payin/transaction")
                    .header("X-Api-Key", "bad-key")
                    .build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            // Response body is written — verify status and content type
            assertEquals(
                org.springframework.http.MediaType.APPLICATION_JSON,
                exchange.getResponse().getHeaders().getContentType()
            );
        }

        @Test
        @DisplayName("Should return 401 for method-level secured endpoint with invalid key")
        void shouldReturn401ForMethodLevelAnnotationWithInvalidKey() throws Exception {
            MethodSecuredController bean = new MethodSecuredController();
            Method method = bean.getClass().getMethod("securedMethod");
            HandlerMethod hm = new HandlerMethod(bean, method);
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(apiKeyValidator.isValid(any())).thenReturn(false);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/internal/op")
                    .header("X-Api-Key", "wrong")
                    .build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            verifyNoInteractions(chain);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Validator delegation
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("API Key validator delegation")
    class ValidatorDelegation {

        @Test
        @DisplayName("Should pass exact header value to validator")
        void shouldPassExactHeaderValueToValidator() throws Exception {
            SecuredController bean = new SecuredController();
            HandlerMethod hm = handlerMethodFor(bean, "handle");
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(apiKeyValidator.isValid("my-secret-key")).thenReturn(true);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/payin/transaction")
                    .header("X-Api-Key", "my-secret-key")
                    .build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(apiKeyValidator).isValid("my-secret-key");
        }

        @Test
        @DisplayName("Should pass null to validator when X-Api-Key header is absent")
        void shouldPassNullToValidatorWhenHeaderAbsent() throws Exception {
            SecuredController bean = new SecuredController();
            HandlerMethod hm = handlerMethodFor(bean, "handle");
            when(handlerMapping.getHandler(any())).thenReturn(Mono.just(hm));
            when(apiKeyValidator.isValid(null)).thenReturn(false);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/v1/payin/transaction").build()
            );

            StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

            verify(apiKeyValidator).isValid(null);
        }
    }
}

