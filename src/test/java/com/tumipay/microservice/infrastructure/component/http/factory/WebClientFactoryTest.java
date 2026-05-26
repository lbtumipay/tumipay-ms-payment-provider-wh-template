package com.tumipay.microservice.infrastructure.component.http.factory;

import com.tumipay.microservice.infrastructure.component.properties.WebClientProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebClientFactory Unit Tests")
class WebClientFactoryTest {

    @Test
    @DisplayName("createWebClient - should cache instances by provider key")
    void createWebClient_shouldCacheInstancesByProviderKey() {
        WebClientFactory factory = new WebClientFactory(buildProperties());

        WebClient first = factory.createWebClient("provider-a");
        WebClient second = factory.createWebClient("provider-a");
        WebClient third = factory.createWebClient("provider-b");

        assertSame(first, second);
        assertNotSame(first, third);
    }

    @Test
    @DisplayName("addDataToContext filter - should enrich reactor context")
    void addDataToContext_shouldEnrichReactorContext() {
        WebClientFactory factory = new WebClientFactory(buildProperties());

        ExchangeFilterFunction addDataToContext = (ExchangeFilterFunction) ReflectionTestUtils.getField(factory, "addDataToContext");
        assertNotNull(addDataToContext);

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("https://example.com/test")).build();
        ClientResponse expected = ClientResponse.create(HttpStatus.OK).body("ok").build();

        Mono<ClientResponse> result = addDataToContext.filter(
            request,
            ignored -> Mono.deferContextual(contextView -> {
                assertTrue(contextView.hasKey("startTime"));
                assertTrue(contextView.hasKey("integrationRequestId"));
                return Mono.just(expected);
            })
        );

        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete();
    }

    @Test
    @DisplayName("filterRequest - should pass request to next exchange function")
    void filterRequest_shouldPassRequestToNextExchangeFunction() {
        WebClientFactory factory = new WebClientFactory(buildProperties());

        ExchangeFilterFunction filterRequest = ReflectionTestUtils.invokeMethod(factory, "filterRequest");
        assertNotNull(filterRequest);

        ClientRequest request = ClientRequest.create(HttpMethod.POST, URI.create("https://example.com/submit"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer token")
            .header("x-trace-id", "trace-1")
            .body(BodyInserters.fromValue("{\"id\":1}"))
            .build();

        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();

        Mono<ClientResponse> result = filterRequest.filter(
            request,
            nextRequest -> {
                capturedRequest.set(nextRequest);
                return Mono.just(ClientResponse.create(HttpStatus.OK).body("done").build());
            }
        );

        StepVerifier.create(result)
            .expectNextMatches(response -> response.statusCode().value() == 200)
            .verifyComplete();

        assertNotNull(capturedRequest.get());
        assertEquals(HttpMethod.POST, capturedRequest.get().method());
        assertEquals("https://example.com/submit", capturedRequest.get().url().toString());
    }

    @Test
    @DisplayName("filterResponse - should rebuild response preserving headers and body")
    void filterResponse_shouldRebuildResponsePreservingHeadersAndBody() {
        WebClientFactory factory = new WebClientFactory(buildProperties());

        ExchangeFilterFunction filterResponse = ReflectionTestUtils.invokeMethod(factory, "filterResponse");
        assertNotNull(filterResponse);

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("https://example.com/result")).build();

        ClientResponse providerResponse = ClientResponse.create(HttpStatus.ACCEPTED)
            .header("x-provider", "sandbox")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body("provider-body")
            .build();

        Mono<String> result = filterResponse
            .filter(request, ignored -> Mono.just(providerResponse))
            .contextWrite(Context.of("startTime", Instant.now(), "integrationRequestId", "int-123"))
            .flatMap(response -> {
                assertEquals(202, response.statusCode().value());
                assertEquals("sandbox", response.headers().asHttpHeaders().getFirst("x-provider"));
                return response.bodyToMono(String.class);
            });

        StepVerifier.create(result)
            .expectNext("provider-body")
            .verifyComplete();
    }

    @Test
    @DisplayName("sanitizeHeaders - should mask sensitive headers")
    @SuppressWarnings("unchecked")
    void sanitizeHeaders_shouldMaskSensitiveHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer token");
        headers.add("x-api-key", "key-123");
        headers.add("x-custom", "value1");

        List<String> result = (List<String>) ReflectionTestUtils.invokeMethod(
            WebClientFactory.class,
            "sanitizeHeaders",
            headers
        );

        assertNotNull(result);
        assertTrue(result.contains("Authorization=***MASKED***"));
        assertTrue(result.contains("x-api-key=***MASKED***"));
        assertTrue(result.contains("x-custom=value1"));
    }

    @Test
    @DisplayName("sanitizeHeaderValue - should join values for non-sensitive headers")
    void sanitizeHeaderValue_shouldJoinValuesForNonSensitiveHeaders() {
        String result = (String) ReflectionTestUtils.invokeMethod(
            WebClientFactory.class,
            "sanitizeHeaderValue",
            "x-tags",
            List.of("A", "B", "C")
        );

        assertEquals("A,B,C", result);
    }

    @Test
    @DisplayName("logExternalRequestBody - should not fail for a valid data buffer")
    void logExternalRequestBody_shouldNotFailForValidDataBuffer() {
        DataBuffer buffer = new DefaultDataBufferFactory().wrap("{\"field\":\"value\"}".getBytes());

        ReflectionTestUtils.invokeMethod(WebClientFactory.class, "logExternalRequestBody", buffer);
    }

    private WebClientProperties buildProperties() {
        WebClientProperties properties = new WebClientProperties();

        properties.getPool().setMaxConnections(10);
        properties.getPool().setPendingAcquireTimeout(Duration.ofSeconds(1));
        properties.getPool().setMaxIdleTime(Duration.ofSeconds(5));
        properties.getPool().setMaxLifeTime(Duration.ofSeconds(30));
        properties.getPool().setEvictInBackground(Duration.ofSeconds(20));

        properties.getTimeout().setConnect(Duration.ofMillis(300));
        properties.getTimeout().setResponse(Duration.ofSeconds(1));

        properties.getTcp().setKeepAlive(true);

        return properties;
    }
}

