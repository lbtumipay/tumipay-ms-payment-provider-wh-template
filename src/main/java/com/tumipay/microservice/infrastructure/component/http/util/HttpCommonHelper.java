package com.tumipay.microservice.infrastructure.component.http.util;

import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpRequest;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpResponse;
import com.tumipay.microservice.shared.util.CommonJsonUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpExceptionHelper
 * <p>
 * HttpExceptionHelper class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 21/05/2026
 */
@Log4j2
@UtilityClass
public class HttpCommonHelper {

    public  <T, R> Mono<ClientHttpResponse<R>> toClientHttpResponse(
        final ClientResponse clientResponse,
        final Class<R> responseClass,
        final ParameterizedTypeReference<R> responseType,
        final Instant startTime,
        final ClientHttpRequest<T> request
    ) {

        final HttpStatusCode statusCode = clientResponse.statusCode();

        if (isAcceptedErrorStatus(statusCode, request)) {
            final HttpHeaders responseHeaders = clientResponse.headers().asHttpHeaders();
            return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(rawBody -> buildClientHttpResponse(
                    statusCode,
                    responseHeaders,
                    deserializeAcceptedBody(rawBody, responseClass, responseType),
                    rawBody,
                    startTime,
                    request,
                    false
                ));
        }

        if (statusCode.is4xxClientError() || statusCode.is5xxServerError()) {
            return HttpExceptionHelper.getProcessException(clientResponse).flatMap(Mono::error);
        }

        final Mono<R> bodyMono = responseType != null
            ? clientResponse.bodyToMono(responseType)
            : clientResponse.bodyToMono(responseClass);

        final HttpHeaders springHeaders = clientResponse.headers().asHttpHeaders();

        return bodyMono.map(body -> buildClientHttpResponse(
            statusCode,
            springHeaders,
            body,
            body == null ? null : body.toString(),
            startTime,
            request,
            statusCode.is2xxSuccessful()
        ));
    }

    private static <T> boolean isAcceptedErrorStatus(final HttpStatusCode statusCode,
                                                     final ClientHttpRequest<T> request) {
        return (statusCode.is4xxClientError() || statusCode.is5xxServerError())
            && request.getAcceptedStatusCodes() != null
            && request.getAcceptedStatusCodes().contains(statusCode.value());
    }

    private static <R> R deserializeAcceptedBody(final String rawBody,
                                                 final Class<R> responseClass,
                                                 final ParameterizedTypeReference<R> responseType) {
        if (rawBody == null || rawBody.isBlank()) {
            return null;
        }

        if (responseType != null) {
            return CommonJsonUtils.fromJson(rawBody, responseType.getType());
        }

        if (responseClass == null || responseClass == Void.class) {
            return null;
        }

        if (String.class.equals(responseClass)) {
            return responseClass.cast(rawBody);
        }

        return CommonJsonUtils.fromJson(rawBody, responseClass);
    }

    private static <T, R> ClientHttpResponse<R> buildClientHttpResponse(final HttpStatusCode statusCode,
                                                                        final HttpHeaders springHeaders,
                                                                        final R body,
                                                                        final String rawBody,
                                                                        final Instant startTime,
                                                                        final ClientHttpRequest<T> request,
                                                                        final boolean success) {
        final Map<String, List<String>> responseHeaders = new LinkedHashMap<>();
        springHeaders.forEach(responseHeaders::put);

        return ClientHttpResponse.<R>builder()
            .statusCode(statusCode.value())
            .headers(java.net.http.HttpHeaders.of(responseHeaders, (name, value) -> true))
            .body(body)
            .rawBody(rawBody)
            .duration(Duration.between(startTime, Instant.now()))
            .requestId(request.getRequestId())
            .integrationId(request.getIntegrationId())
            .success(success)
            .build();
    }

    public static <T, R> void doOnSuccessIntegration(final ClientHttpRequest<T> request, R success) {
        log.debug("success integration process by integration code {}, success {}",
            request.getIntegrationId(),
            success
        );
    }

    public static <T> void doOnErrorIntegration(final ClientHttpRequest<T> request, Throwable t) {

        log.debug("error integration process by integration code {}, error {}",
            request.getIntegrationId(),
            t.getMessage()
        );
    }
}