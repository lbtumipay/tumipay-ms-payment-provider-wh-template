package com.tumipay.microservice.infrastructure.component.http.executor;

import com.tumipay.microservice.infrastructure.component.http.config.ConfigHttpIntegration;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpRequest;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpResponse;
import com.tumipay.microservice.infrastructure.component.http.enums.HttpMethodEnum;
import com.tumipay.microservice.infrastructure.component.http.factory.WebClientFactory;
import com.tumipay.microservice.infrastructure.component.properties.WebClientProperties;
import com.tumipay.microservice.shared.enums.BaseErrorCodeEnum;
import com.tumipay.microservice.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReactiveHttpClientExecutor Unit Tests")
class ReactiveHttpClientExecutorTest {

    @ParameterizedTest
    @EnumSource(HttpMethodEnum.class)
    @DisplayName("execute(request, Class) - should execute all supported HTTP methods")
    void executeWithClass_shouldExecuteAllSupportedMethods(HttpMethodEnum method) {
        AtomicReference<ClientRequest> outboundRequest = new AtomicReference<>();

        WebClient webClient = WebClient.builder()
            .exchangeFunction(request -> {
                outboundRequest.set(request);
                return Mono.just(
                    ClientResponse.create(HttpStatus.OK)
                        .body("done")
                        .build()
                );
            })
            .build();

        ReactiveHttpClientExecutor executor = new ReactiveHttpClientExecutor(new StubWebClientFactory(webClient));
        ClientHttpRequest<Map<String, Object>> request = buildRequest(method, Duration.ofMillis(150));

        Mono<ClientHttpResponse<String>> result = executor.execute(request, String.class);

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals("done", response.getBody());
                assertEquals("done", response.getRawBody());
                assertTrue(response.getSuccess());
                assertEquals("req-123", response.getRequestId());
                assertEquals("integration-run-1", response.getIntegrationId());
            })
            .verifyComplete();

        assertNotNull(outboundRequest.get());
        assertEquals("https://example.com/payment/tx?ref=abc", outboundRequest.get().url().toString());
        assertEquals(method.name(), outboundRequest.get().method().name());
    }

    @Test
    @DisplayName("execute(request, ParameterizedTypeReference) - should deserialize typed response")
    void executeWithTypeReference_shouldDeserializeTypedResponse() {
        WebClient webClient = WebClient.builder()
            .exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("[\"A\",\"B\"]")
                    .build()
            ))
            .build();

        ReactiveHttpClientExecutor executor = new ReactiveHttpClientExecutor(new StubWebClientFactory(webClient));
        ClientHttpRequest<Map<String, Object>> request = buildRequest(HttpMethodEnum.GET, Duration.ofSeconds(2));

        Mono<ClientHttpResponse<List<String>>> result = executor.execute(
            request,
            new ParameterizedTypeReference<>() {
            }
        );

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(List.of("A", "B"), response.getBody());
                assertTrue(response.getSuccess());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("execute - should fail for unsupported HTTP method")
    void execute_shouldFailForUnsupportedMethod() {
        ReactiveHttpClientExecutor executor = new ReactiveHttpClientExecutor(new StubWebClientFactory(WebClient.builder().build()));
        ClientHttpRequest<Map<String, Object>> request = buildRequest(null, Duration.ofMillis(150));

        StepVerifier.create(executor.execute(request, String.class))
            .expectErrorSatisfies(throwable -> {
                BusinessException exception = assertInstanceOf(BusinessException.class, throwable);
                assertEquals(BaseErrorCodeEnum.HTTP_INTEGRATION_ERROR.getCode(), exception.getCode());
                assertTrue(exception.getMessage().contains("Unsupported HTTP method"));
            })
            .verify();
    }

    @Test
    @DisplayName("execute - should map NOT_FOUND as business exception")
    void execute_shouldMapNotFoundAsBusinessException() {
        WebClient webClient = WebClient.builder()
            .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build()))
            .build();

        ReactiveHttpClientExecutor executor = new ReactiveHttpClientExecutor(new StubWebClientFactory(webClient));
        ClientHttpRequest<Map<String, Object>> request = buildRequest(HttpMethodEnum.GET, Duration.ofMillis(150));

        StepVerifier.create(executor.execute(request, String.class))
            .expectErrorSatisfies(throwable -> {
                BusinessException exception = assertInstanceOf(BusinessException.class, throwable);
                assertEquals(BaseErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(), exception.getCode());
            })
            .verify();
    }

    @Test
    @DisplayName("execute - should fail with not-resource exception when response mono is empty")
    void execute_shouldFailWhenResponseMonoIsEmpty() {
        WebClient webClient = WebClient.builder()
            .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK).build()))
            .build();

        ReactiveHttpClientExecutor executor = new ReactiveHttpClientExecutor(new StubWebClientFactory(webClient));
        ClientHttpRequest<Map<String, Object>> request = buildRequest(HttpMethodEnum.GET, Duration.ofMillis(150));

        StepVerifier.create(executor.execute(request, String.class))
            .expectErrorSatisfies(throwable -> {
                BusinessException exception = assertInstanceOf(BusinessException.class, throwable);
                assertEquals(BaseErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(), exception.getCode());
            })
            .verify();
    }

    @Test
    @DisplayName("execute - should fail with timeout when provider does not answer")
    void execute_shouldFailWithTimeoutWhenProviderDoesNotAnswer() {
        WebClient webClient = WebClient.builder()
            .exchangeFunction(request -> Mono.never())
            .build();

        ReactiveHttpClientExecutor executor = new ReactiveHttpClientExecutor(new StubWebClientFactory(webClient));
        ClientHttpRequest<Map<String, Object>> request = buildRequest(HttpMethodEnum.GET, Duration.ofMillis(25));

        StepVerifier.create(executor.execute(request, String.class))
            .expectError(TimeoutException.class)
            .verify(Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("execute - should return accepted error response when status is configured")
    void execute_shouldReturnAcceptedErrorResponseWhenStatusIsConfigured() {
        WebClient webClient = WebClient.builder()
            .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.CONFLICT).build()))
            .build();

        ReactiveHttpClientExecutor executor = new ReactiveHttpClientExecutor(new StubWebClientFactory(webClient));
        ClientHttpRequest<Map<String, Object>> request = buildRequest(HttpMethodEnum.POST, Duration.ofMillis(150));
        request.setAcceptedStatusCodes(Set.of(HttpStatus.CONFLICT.value()));

        StepVerifier.create(executor.execute(request, String.class))
            .assertNext(response -> {
                assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode());
                assertEquals("", response.getRawBody());
                assertFalse(response.getSuccess());
                assertEquals("req-123", response.getRequestId());
                assertEquals("integration-run-1", response.getIntegrationId());
            })
            .verifyComplete();
    }

    private ClientHttpRequest<Map<String, Object>> buildRequest(HttpMethodEnum method, Duration timeout) {
        ConfigHttpIntegration config = ConfigHttpIntegration.builder()
            .integrationCode("provider-x")
            .host("https://example.com")
            .integrationPath("/payment/tx")
            .timeout(timeout)
            .defaultHeaders(Map.of("x-default", "default-value"))
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-custom", "custom-value");

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("ref", "abc");

        return ClientHttpRequest.<Map<String, Object>>builder()
            .configIntegration(config)
            .method(method)
            .headers(headers)
            .queryParams(queryParams)
            .body(Map.of("amount", 100))
            .requestId("req-123")
            .integrationId("integration-run-1")
            .build();
    }

    private static class StubWebClientFactory extends WebClientFactory {

        private final WebClient webClient;

        StubWebClientFactory(WebClient webClient) {
            super(buildProperties());
            this.webClient = webClient;
        }

        @Override
        public WebClient createWebClient(String adapterProviderCode) {
            return webClient;
        }

        private static WebClientProperties buildProperties() {
            WebClientProperties properties = new WebClientProperties();
            properties.getPool().setMaxConnections(1);
            properties.getPool().setPendingAcquireTimeout(Duration.ofSeconds(1));
            properties.getPool().setMaxIdleTime(Duration.ofSeconds(1));
            properties.getPool().setMaxLifeTime(Duration.ofSeconds(1));
            properties.getPool().setEvictInBackground(Duration.ofSeconds(1));
            properties.getTimeout().setConnect(Duration.ofSeconds(1));
            properties.getTimeout().setResponse(Duration.ofSeconds(1));
            properties.getTcp().setKeepAlive(true);
            return properties;
        }
    }
}

