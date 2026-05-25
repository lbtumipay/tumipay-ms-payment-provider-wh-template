package com.tumipay.microservice.infrastructure.component.decorator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HttpRequestDecorator.
 * <p>
 * Verifies that the decorator wraps the delegate correctly and
 * preserves body content after interception.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("HttpRequestDecorator Unit Tests")
class HttpRequestDecoratorTest {

    @Test
    @DisplayName("Should wrap delegate and expose its path")
    void shouldWrapDelegateAndExposeItsPath() {
        MockServerHttpRequest mockRequest = MockServerHttpRequest
            .post("/tp/payment/adapter/payin/transaction")
            .build();

        HttpRequestDecorator decorator = new HttpRequestDecorator(mockRequest);

        assertNotNull(decorator);
        assertEquals("/tp/payment/adapter/payin/transaction", decorator.getPath().value());
    }

    @Test
    @DisplayName("getBody should preserve body content after decoration")
    void getBodyShouldPreserveBodyContentAfterDecoration() {
        String body = "{\"amount\":100,\"currency\":\"COP\"}";
        MockServerHttpRequest mockRequest = MockServerHttpRequest
            .post("/tp/test")
            .body(body);

        HttpRequestDecorator decorator = new HttpRequestDecorator(mockRequest);

        StepVerifier.create(
            decorator.getBody()
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
        )
            .expectNext(body)
            .verifyComplete();
    }

    @Test
    @DisplayName("getBody should return empty Flux when request has no body")
    void getBodyShouldReturnEmptyFluxWhenRequestHasNoBody() {
        MockServerHttpRequest mockRequest = MockServerHttpRequest
            .get("/tp/test")
            .build();

        HttpRequestDecorator decorator = new HttpRequestDecorator(mockRequest);

        StepVerifier.create(decorator.getBody())
            .verifyComplete();
    }

    @Test
    @DisplayName("getBody should handle bodies with special characters correctly")
    void getBodyShouldHandleBodiesWithSpecialCharacters() {
        String body = "{\"description\":\"Pago ñoño €100\"}";
        MockServerHttpRequest mockRequest = MockServerHttpRequest
            .post("/tp/test")
            .body(body);

        HttpRequestDecorator decorator = new HttpRequestDecorator(mockRequest);

        StepVerifier.create(
            decorator.getBody()
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
        )
            .expectNext(body)
            .verifyComplete();
    }

    // ── HTTP metadata delegation ───────────────────────────────────────────────

    @Test
    @DisplayName("Should preserve the HTTP method from the delegate")
    void shouldPreserveHttpMethodFromDelegate() {
        MockServerHttpRequest mockRequest = MockServerHttpRequest
            .post("/tp/test")
            .build();

        HttpRequestDecorator decorator = new HttpRequestDecorator(mockRequest);

        assertEquals(HttpMethod.POST, decorator.getMethod());
    }

    @Test
    @DisplayName("Should preserve request headers from the delegate")
    void shouldPreserveRequestHeadersFromDelegate() {
        MockServerHttpRequest mockRequest = MockServerHttpRequest
            .post("/tp/test")
            .header("X-Merchant-ID", "MERCHANT-001")
            .header("X-Request-ID",  "REQ-001")
            .build();

        HttpRequestDecorator decorator = new HttpRequestDecorator(mockRequest);

        assertEquals("MERCHANT-001", decorator.getHeaders().getFirst("X-Merchant-ID"));
        assertEquals("REQ-001",      decorator.getHeaders().getFirst("X-Request-ID"));
    }

    @Test
    @DisplayName("Should expose the full URI of the delegate")
    void shouldExposeFullUriFromDelegate() {
        MockServerHttpRequest mockRequest = MockServerHttpRequest
            .get("/tp/payment/adapter/payin/transaction")
            .build();

        HttpRequestDecorator decorator = new HttpRequestDecorator(mockRequest);

        assertNotNull(decorator.getURI());
        assertTrue(decorator.getURI().toString().contains("/tp/payment/adapter/payin/transaction"));
    }

    @Test
    @DisplayName("getBody should emit exactly one DataBuffer item per single-chunk body")
    void getBodyShouldEmitExactlyOneBufferItemForSingleChunkBody() {
        MockServerHttpRequest mockRequest = MockServerHttpRequest
            .post("/tp/test")
            .body("{\"key\":\"value\"}");

        HttpRequestDecorator decorator = new HttpRequestDecorator(mockRequest);

        StepVerifier.create(decorator.getBody())
            .expectNextCount(1)
            .verifyComplete();
    }
}
