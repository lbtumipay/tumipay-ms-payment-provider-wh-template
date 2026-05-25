package com.tumipay.microservice.infrastructure.component.decorator;

import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HttpResponseDecorator.
 * <p>
 * Verifies request-ID header injection, buffer rebuilding,
 * response body preservation and writeAndFlushWith delegation.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@DisplayName("HttpResponseDecorator Unit Tests")
class HttpResponseDecoratorTest {

    private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    @AfterEach
    void tearDown() {
        ThreadContext.clearAll();
    }

    // ── Constructor ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should construct wrapping the delegate and expose its headers")
    void shouldConstructWrappingTheDelegateAndExposeHeaders() {
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        assertNotNull(decorator);
        assertNotNull(decorator.getHeaders());
    }

    // ── writeWith — X-Request-ID injection ────────────────────────────────────

    @Test
    @DisplayName("writeWith should inject X-Request-ID header from ThreadContext")
    void writeWithShouldInjectRequestIdHeaderFromThreadContext() {
        ThreadContext.put(BaseIntegrationConstant.KEY_REQUEST_ID, "TUMIPAY-TEST-001");
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        DataBuffer buffer = bufferFactory.wrap("{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(decorator.writeWith(Mono.just(buffer)))
            .verifyComplete();

        assertEquals("TUMIPAY-TEST-001",
            decorator.getHeaders().getFirst(BaseIntegrationConstant.HEADER_REQUEST_ID));
    }

    @Test
    @DisplayName("writeWith should not add X-Request-ID header when ThreadContext has no requestId")
    void writeWithShouldNotAddRequestIdHeaderWhenThreadContextIsEmpty() {
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        DataBuffer buffer = bufferFactory.wrap("{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(decorator.writeWith(Mono.just(buffer)))
            .verifyComplete();

        assertNull(decorator.getHeaders().getFirst(BaseIntegrationConstant.HEADER_REQUEST_ID),
            "Header must be absent when ThreadContext contains no requestId");
    }

    // ── writeWith — buffer rebuild ─────────────────────────────────────────────

    @Test
    @DisplayName("writeWith should rebuild buffer and preserve response body content")
    void writeWithShouldRebuildBufferAndPreserveContent() {
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        String payload = "{\"result\":\"success\",\"code\":\"PROCESS_COMPLETED\"}";
        DataBuffer buffer = bufferFactory.wrap(payload.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(decorator.writeWith(Mono.just(buffer)))
            .verifyComplete();

        StepVerifier.create(mockResponse.getBodyAsString())
            .expectNext(payload)
            .verifyComplete();
    }

    @Test
    @DisplayName("writeWith should handle multiple sequential buffers")
    void writeWithShouldHandleMultipleSequentialBuffers() {
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        DataBuffer buf1 = bufferFactory.wrap("{\"part\":".getBytes(StandardCharsets.UTF_8));
        DataBuffer buf2 = bufferFactory.wrap("\"one\"}".getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(decorator.writeWith(Mono.just(buf1)))
            .verifyComplete();

        // Verify at least one write completed without error
        StepVerifier.create(mockResponse.getBodyAsString())
            .expectNextMatches(body -> body.contains("part"))
            .verifyComplete();
    }

    @Test
    @DisplayName("writeWith should inject requestId and preserve body simultaneously")
    void writeWithShouldInjectRequestIdAndPreserveBodySimultaneously() {
        ThreadContext.put(BaseIntegrationConstant.KEY_REQUEST_ID, "TUMIPAY-FULL-001");
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        String payload = "{\"code\":\"PROCESS_COMPLETED\"}";
        DataBuffer buffer = bufferFactory.wrap(payload.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(decorator.writeWith(Mono.just(buffer)))
            .verifyComplete();

        assertEquals("TUMIPAY-FULL-001",
            decorator.getHeaders().getFirst(BaseIntegrationConstant.HEADER_REQUEST_ID));

        StepVerifier.create(mockResponse.getBodyAsString())
            .expectNext(payload)
            .verifyComplete();
    }

    // ── writeAndFlushWith ──────────────────────────────────────────────────────

    @Test
    @DisplayName("writeAndFlushWith should delegate to super without error")
    void writeAndFlushWithShouldDelegateToSuperWithoutError() {
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        StepVerifier.create(decorator.writeAndFlushWith(Mono.empty()))
            .verifyComplete();
    }

    @Test
    @DisplayName("writeAndFlushWith should log status and complete when publisher has data")
    void writeAndFlushWithShouldCompleteWhenPublisherHasData() {
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        DataBuffer buffer = bufferFactory.wrap("{}".getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(decorator.writeAndFlushWith(Mono.just(Mono.just(buffer))))
            .verifyComplete();
    }

    // ── Edge cases ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("writeWith should handle empty response body without error")
    void writeWithShouldHandleEmptyBodyWithoutError() {
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        DataBuffer emptyBuffer = bufferFactory.wrap(new byte[0]);

        StepVerifier.create(decorator.writeWith(Mono.just(emptyBuffer)))
            .verifyComplete();

        StepVerifier.create(mockResponse.getBodyAsString())
            .expectNext("")
            .verifyComplete();
    }

    @Test
    @DisplayName("Should expose response headers from the delegate")
    void shouldExposeResponseHeadersFromDelegate() {
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        mockResponse.getHeaders().add("Content-Type", "application/json");

        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        assertEquals("application/json",
            decorator.getHeaders().getFirst("Content-Type"));
    }

    @Test
    @DisplayName("Should preserve HTTP status code set on the delegate")
    void shouldPreserveHttpStatusCodeSetOnDelegate() {
        MockServerHttpResponse mockResponse = new MockServerHttpResponse();
        mockResponse.setRawStatusCode(201);

        HttpResponseDecorator decorator = new HttpResponseDecorator(mockResponse);

        assertEquals(201, decorator.getStatusCode().value());
    }
}

