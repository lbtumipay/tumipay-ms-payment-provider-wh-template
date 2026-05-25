package com.tumipay.microservice.infrastructure.component.http.executor;

import com.tumipay.microservice.infrastructure.component.http.config.ConfigHttpIntegration;
import com.tumipay.microservice.infrastructure.component.http.contract.IHttpClientExecutor;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpRequest;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpResponse;
import com.tumipay.microservice.infrastructure.component.http.enums.HttpMethodEnum;
import com.tumipay.microservice.infrastructure.component.http.factory.WebClientFactory;
import com.tumipay.microservice.infrastructure.component.http.util.HttpCommonHelper;
import com.tumipay.microservice.infrastructure.component.http.util.HttpExceptionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
//import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
//import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import java.time.Instant;

/**
 * ReactiveHttpClientExecutor
 * <p>
 * Reactive HTTP execution engine implementation.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS
 * AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/05/2026
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class ReactiveHttpClientExecutor implements IHttpClientExecutor {

    private final WebClientFactory webClientFactory;
    //private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Override
    public <T, R> Mono<ClientHttpResponse<R>> execute(final ClientHttpRequest<T> request, final Class<R> responseClass) {

        final HttpMethodEnum method = request.getMethod();
        //final ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create(getCircuitBreakerId(request));

        if (method == HttpMethodEnum.POST) {
            //return circuitBreaker.run(executePost(request, responseClass, null), Mono::error);
            return executePost(request, responseClass, null);
        }

        if (method == HttpMethodEnum.GET) {
            //return circuitBreaker.run(executeGet(request, responseClass, null), Mono::error);
            return executeGet(request, responseClass, null);
        }

        if (method == HttpMethodEnum.PUT) {
            //return circuitBreaker.run(executePut(request, responseClass, null), Mono::error);
            return executePut(request, responseClass, null);
        }

        if (method == HttpMethodEnum.PATCH) {
            //return circuitBreaker.run(executePath(request, responseClass, null), Mono::error);
            return executePath(request, responseClass, null);
        }

        if (method == HttpMethodEnum.DELETE) {
            //return circuitBreaker.run(executePath(request, responseClass, null), Mono::error);
            return executeDelete(request, responseClass, null);
        }

        return Mono.error(HttpExceptionHelper.getUnsupportedHttpMethodException(method));
    }

    @Override
    public <T, R> Mono<ClientHttpResponse<R>> execute(final ClientHttpRequest<T> request, final ParameterizedTypeReference<R> responseType) {

        final HttpMethodEnum method = request.getMethod();
        //final ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create(getCircuitBreakerId(request));

        if (method == HttpMethodEnum.POST) {
            //return circuitBreaker.run(executePost(request, null, responseType), Mono::error);
            return executePost(request, null, responseType);
        }

        if (method == HttpMethodEnum.GET) {
            //return circuitBreaker.run(executeGet(request, null, responseType), Mono::error);
            return executeGet(request, null, responseType);
        }

        if (method == HttpMethodEnum.PUT) {
            //return circuitBreaker.run(executePut(request, null, responseType), Mono::error);
            return executePut(request, null, responseType);
        }

        if (method == HttpMethodEnum.PATCH) {
            //return circuitBreaker.run(executePath(request, null, responseType), Mono::error);
            return executePath(request, null, responseType);
        }

        if (method == HttpMethodEnum.DELETE) {
            //return circuitBreaker.run(executePath(request, null, responseType), Mono::error);
            return executeDelete(request, null, responseType);
        }

        return Mono.error(HttpExceptionHelper.getUnsupportedHttpMethodException(method));
    }

    /*
    private <T> String getCircuitBreakerId(final ClientHttpRequest<T> request) {

        if (request.getConfigIntegration() == null || request.getConfigIntegration().getIntegrationCode() == null) {
            return "default-http-integration";
        }

        return request.getConfigIntegration().getIntegrationCode();
    }*/

    private <T, R> Mono<ClientHttpResponse<R>> executePost(final ClientHttpRequest<T> request, final Class<R> responseClass, final ParameterizedTypeReference<R> responseType) {

        final ConfigHttpIntegration configHttpIntegration = request.getConfigIntegration();
        final WebClient webClient = webClientFactory.createWebClient(
            configHttpIntegration.getIntegrationCode()
        );

        final String uri = UriComponentsBuilder
            .fromUriString(configHttpIntegration.getHost())
            .path(configHttpIntegration.getIntegrationPath())
            .queryParams(request.getQueryParams())
            .build()
            .toUriString();

        final Instant startTime = Instant.now();

        return webClient
            .post()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .headers(headers -> {

                if (configHttpIntegration.getDefaultHeaders() != null) {
                    headers.setAll(configHttpIntegration.getDefaultHeaders());
                }

                if (request.getHeaders() != null) {
                    headers.addAll(request.getHeaders());
                }
            })
            .accept(MediaType.ALL)
            .body(BodyInserters.fromValue(request.getBody()))
            .exchangeToMono(response ->
                HttpCommonHelper.toClientHttpResponse(response, responseClass, responseType, startTime, request)
            )
            .timeout(configHttpIntegration.getTimeout())
            .switchIfEmpty(Mono.error(HttpExceptionHelper.getNotResourceException()))
            .doOnSuccess(success -> HttpCommonHelper.doOnSuccessIntegration(request, success))
            .doOnError(throwable -> HttpCommonHelper.doOnErrorIntegration(request, throwable));
    }

    private <T, R> Mono<ClientHttpResponse<R>> executePut(final ClientHttpRequest<T> request, final Class<R> responseClass, final ParameterizedTypeReference<R> responseType) {

        final ConfigHttpIntegration configHttpIntegration = request.getConfigIntegration();
        final WebClient webClient = webClientFactory.createWebClient(
            configHttpIntegration.getIntegrationCode()
        );

        final String uri = UriComponentsBuilder
            .fromUriString(configHttpIntegration.getHost())
            .path(configHttpIntegration.getIntegrationPath())
            .queryParams(request.getQueryParams())
            .build()
            .toUriString();

        final Instant startTime = Instant.now();

        return webClient
            .put()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .headers(headers -> {

                if (configHttpIntegration.getDefaultHeaders() != null) {
                    headers.setAll(configHttpIntegration.getDefaultHeaders());
                }

                if (request.getHeaders() != null) {
                    headers.addAll(request.getHeaders());
                }
            })
            .accept(MediaType.ALL)
            .body(BodyInserters.fromValue(request.getBody()))
            .exchangeToMono(response ->
                HttpCommonHelper.toClientHttpResponse(response, responseClass, responseType, startTime, request)
            )
            .timeout(configHttpIntegration.getTimeout())
            .switchIfEmpty(Mono.error(HttpExceptionHelper.getNotResourceException()))
            .doOnSuccess(success -> HttpCommonHelper.doOnSuccessIntegration(request, success))
            .doOnError(throwable -> HttpCommonHelper.doOnErrorIntegration(request, throwable));
    }

    private <T, R> Mono<ClientHttpResponse<R>> executePath(final ClientHttpRequest<T> request, final Class<R> responseClass, final ParameterizedTypeReference<R> responseType) {

        final ConfigHttpIntegration configHttpIntegration = request.getConfigIntegration();
        final WebClient webClient = webClientFactory.createWebClient(
            configHttpIntegration.getIntegrationCode()
        );

        final String uri = UriComponentsBuilder
            .fromUriString(configHttpIntegration.getHost())
            .path(configHttpIntegration.getIntegrationPath())
            .queryParams(request.getQueryParams())
            .build()
            .toUriString();

        final Instant startTime = Instant.now();

        return webClient
            .patch()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .headers(headers -> {

                if (configHttpIntegration.getDefaultHeaders() != null) {
                    headers.setAll(configHttpIntegration.getDefaultHeaders());
                }

                if (request.getHeaders() != null) {
                    headers.addAll(request.getHeaders());
                }
            })
            .accept(MediaType.ALL)
            .body(BodyInserters.fromValue(request.getBody()))
            .exchangeToMono(response ->
                HttpCommonHelper.toClientHttpResponse(response, responseClass, responseType, startTime, request)
            )
            .timeout(configHttpIntegration.getTimeout())
            .switchIfEmpty(Mono.error(HttpExceptionHelper.getNotResourceException()))
            .doOnSuccess(success -> HttpCommonHelper.doOnSuccessIntegration(request, success))
            .doOnError(throwable -> HttpCommonHelper.doOnErrorIntegration(request, throwable));
    }

    private <T, R> Mono<ClientHttpResponse<R>> executeDelete(final ClientHttpRequest<T> request, final Class<R> responseClass, final ParameterizedTypeReference<R> responseType) {

        final ConfigHttpIntegration configHttpIntegration = request.getConfigIntegration();
        final WebClient webClient = webClientFactory.createWebClient(
            configHttpIntegration.getIntegrationCode()
        );

        final String uri = UriComponentsBuilder
            .fromUriString(configHttpIntegration.getHost())
            .path(configHttpIntegration.getIntegrationPath())
            .queryParams(request.getQueryParams())
            .build()
            .toUriString();

        final Instant startTime = Instant.now();

        return webClient
            .delete()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .headers(headers -> {

                if (configHttpIntegration.getDefaultHeaders() != null) {
                    headers.setAll(configHttpIntegration.getDefaultHeaders());
                }

                if (request.getHeaders() != null) {
                    headers.addAll(request.getHeaders());
                }
            })
            .accept(MediaType.ALL)
            .exchangeToMono(response ->
                HttpCommonHelper.toClientHttpResponse(response, responseClass, responseType, startTime, request)
            )
            .timeout(configHttpIntegration.getTimeout())
            .switchIfEmpty(Mono.error(HttpExceptionHelper.getNotResourceException()))
            .doOnSuccess(success -> HttpCommonHelper.doOnSuccessIntegration(request, success))
            .doOnError(throwable -> HttpCommonHelper.doOnErrorIntegration(request, throwable));
    }

    private <T, R> Mono<ClientHttpResponse<R>> executeGet(final ClientHttpRequest<T> request, final Class<R> responseClass, final ParameterizedTypeReference<R> responseType) {

        final ConfigHttpIntegration configHttpIntegration = request.getConfigIntegration();
        final WebClient webClient = webClientFactory.createWebClient(
            configHttpIntegration.getIntegrationCode()
        );

        final String uri = UriComponentsBuilder
            .fromUriString(configHttpIntegration.getHost())
            .path(configHttpIntegration.getIntegrationPath())
            .queryParams(request.getQueryParams())
            .build()
            .toUriString();

        final Instant startTime = Instant.now();

        return webClient
            .get()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .headers(headers -> {

                if (configHttpIntegration.getDefaultHeaders() != null) {
                    headers.setAll(configHttpIntegration.getDefaultHeaders());
                }

                if (request.getHeaders() != null) {
                    headers.addAll(request.getHeaders());
                }
            })
            .accept(MediaType.ALL)
            .exchangeToMono(response ->
                HttpCommonHelper.toClientHttpResponse(response, responseClass, responseType, startTime, request)
            )
            .timeout(configHttpIntegration.getTimeout())
            .switchIfEmpty(Mono.error(HttpExceptionHelper.getNotResourceException()))
            .doOnSuccess(success -> HttpCommonHelper.doOnSuccessIntegration(request, success))
            .doOnError(throwable -> HttpCommonHelper.doOnErrorIntegration(request, throwable));
    }
}