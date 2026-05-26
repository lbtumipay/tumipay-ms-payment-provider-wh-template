package com.tumipay.microservice.infrastructure.component.http.util;

import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpRequest;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpResponse;
import com.tumipay.microservice.shared.enums.BaseErrorCodeEnum;
import com.tumipay.microservice.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HttpCommonHelper Unit Tests")
class HttpCommonHelperTest {

    @Test
    @DisplayName("toClientHttpResponse - should map successful response using Class")
    void toClientHttpResponse_shouldMapSuccessfulResponseUsingClass() {
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
            .header("x-provider", "sandbox")
            .body("ok")
            .build();

        ClientHttpRequest<String> request = ClientHttpRequest.<String>builder()
            .requestId("req-1")
            .integrationId("int-1")
            .build();

        Mono<ClientHttpResponse<String>> result = HttpCommonHelper.toClientHttpResponse(
            clientResponse,
            String.class,
            null,
            Instant.now(),
            request
        );

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals("ok", response.getBody());
                assertEquals("ok", response.getRawBody());
                assertEquals("req-1", response.getRequestId());
                assertEquals("int-1", response.getIntegrationId());
                assertTrue(response.getSuccess());
                assertTrue(response.getHeaders().firstValue("x-provider").isPresent());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("toClientHttpResponse - should map successful response using ParameterizedTypeReference")
    void toClientHttpResponse_shouldMapSuccessfulResponseUsingTypeReference() {
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body("[\"one\",\"two\"]")
            .build();

        ClientHttpRequest<String> request = ClientHttpRequest.<String>builder()
            .requestId("req-2")
            .integrationId("int-2")
            .build();

        Mono<ClientHttpResponse<List<String>>> result = HttpCommonHelper.toClientHttpResponse(
            clientResponse,
            null,
            new ParameterizedTypeReference<>() {
            },
            Instant.now(),
            request
        );

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(List.of("one", "two"), response.getBody());
                assertEquals("[one, two]", response.getRawBody());
                assertTrue(response.getSuccess());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("toClientHttpResponse - should return error when response status is 4xx/5xx")
    void toClientHttpResponse_shouldReturnErrorFor4xxOr5xx() {
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.NOT_FOUND).build();

        ClientHttpRequest<String> request = ClientHttpRequest.<String>builder()
            .requestId("req-3")
            .integrationId("int-3")
            .build();

        Mono<ClientHttpResponse<String>> result = HttpCommonHelper.toClientHttpResponse(
            clientResponse,
            String.class,
            null,
            Instant.now(),
            request
        );

        StepVerifier.create(result)
            .expectErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof BusinessException);
                BusinessException businessException = (BusinessException) throwable;
                assertEquals(BaseErrorCodeEnum.RESOURCE_NOT_FOUND.getCode(), businessException.getCode());
            })
            .verify();
    }

    @Test
    @DisplayName("doOnSuccessIntegration and doOnErrorIntegration - should not throw")
    void doOnCallbacks_shouldNotThrow() {
        ClientHttpRequest<String> request = ClientHttpRequest.<String>builder()
            .integrationId("int-4")
            .build();

        assertDoesNotThrow(() -> HttpCommonHelper.doOnSuccessIntegration(request, "ok"));
        assertDoesNotThrow(() -> HttpCommonHelper.doOnErrorIntegration(request, new RuntimeException("boom")));
    }
}

