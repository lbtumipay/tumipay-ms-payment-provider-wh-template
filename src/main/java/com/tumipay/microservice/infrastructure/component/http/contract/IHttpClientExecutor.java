package com.tumipay.microservice.infrastructure.component.http.contract;

import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpRequest;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpResponse;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

/**
 * IHttpClientExecutor
 * <p>
 * IHttpClientExecutor interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/05/2026
 */
public interface IHttpClientExecutor {

    /**
     * Executes an outbound HTTP request using a concrete response class.
     *
     * @param request       outbound HTTP request
     * @param responseClass response payload class
     * @return standardized HTTP response
     */
    <T, R> Mono<ClientHttpResponse<R>> execute(ClientHttpRequest<T> request, Class<R> responseClass);

    /**
     * Executes an outbound HTTP request using a generic response type.
     *
     * @param request      outbound HTTP request
     * @param responseType generic response type reference
     * @return standardized HTTP response
     */
    <T, R> Mono<ClientHttpResponse<R>> execute(ClientHttpRequest<T> request, ParameterizedTypeReference<R> responseType);
}
