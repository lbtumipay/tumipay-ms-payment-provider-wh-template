package com.tumipay.microservice.infrastructure.component.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumipay.microservice.domain.port.output.IApiKeyValidatorPort;
import com.tumipay.microservice.infrastructure.component.annotation.InternalApiSecured;
import com.tumipay.microservice.infrastructure.component.dto.BaseApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * InternalApiSecurityFilter
 * <p>
 * Reactive WebFilter that enforces internal API Key authentication for endpoints
 * annotated with {@link InternalApiSecured}.
 * <p>
 * Integrates with the existing reactive filter chain alongside {@link HttpLoggerFilter}.
 * Returns a standardized {@link BaseApiResponse} error body on unauthorized access,
 * consistent with the TumiPay API response conventions.
 * <p>
 * Execution flow:
 * <ol>
 *     <li>Resolves the handler for the current request via {@link RequestMappingHandlerMapping}.</li>
 *     <li>Checks whether the resolved handler is annotated with {@link InternalApiSecured}
 *         at method or class level.</li>
 *     <li>If not secured, the filter chain continues without modification.</li>
 *     <li>If secured, extracts the {@code X-Api-Key} header and delegates validation
 *         to {@link IApiKeyValidatorPort}.</li>
 *     <li>On failure, returns a {@code 401 Unauthorized} response with a {@link BaseApiResponse} body.</li>
 * </ol>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@Log4j2
@Component
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class InternalApiSecurityFilter implements WebFilter {

    private static final String HEADER_API_KEY = "X-Api-Key";

    private final IApiKeyValidatorPort apiKeyValidator;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ObjectMapper objectMapper;

    /**
     * Intercepts incoming requests and validates the API Key for secured endpoints.
     *
     * @param exchange the current server exchange.
     * @param chain    the filter chain.
     * @return {@link Mono} that completes when processing finishes.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // Use thenReturn(Boolean) to ensure switchIfEmpty is not triggered after flatMap
        // completes, since Mono<Void> never emits elements and would cause a double invocation.
        return requestMappingHandlerMapping.getHandler(exchange)
            .flatMap(handler -> {

                if (!(handler instanceof HandlerMethod handlerMethod)) {
                    return chain.filter(exchange).thenReturn(Boolean.TRUE);
                }

                boolean secured =
                    handlerMethod.hasMethodAnnotation(InternalApiSecured.class) ||
                    handlerMethod.getBeanType().isAnnotationPresent(InternalApiSecured.class);

                if (!secured) {
                    return chain.filter(exchange).thenReturn(Boolean.TRUE);
                }

                String apiKey = exchange.getRequest().getHeaders().getFirst(HEADER_API_KEY);

                if (!apiKeyValidator.isValid(apiKey)) {
                    log.warn("Unauthorized internal API Key access attempt on path: {}",
                        exchange.getRequest().getPath().value());
                    return writeUnauthorizedResponse(exchange).thenReturn(Boolean.FALSE);
                }

                return chain.filter(exchange).thenReturn(Boolean.TRUE);
            })
            .switchIfEmpty(Mono.defer(() -> chain.filter(exchange).thenReturn(Boolean.FALSE)))
            .then();
    }

    /**
     * Writes a standardized 401 Unauthorized response using {@link BaseApiResponse}.
     *
     * @param exchange the current server exchange.
     * @return {@link Mono} that completes after writing the response.
     */
    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange) {

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return Mono.fromCallable(() -> {
                BaseApiResponse<Void> body = BaseApiResponse.<Void>builder()
                    .code("UNAUTHORIZED")
                    .status("FAILURE")
                    .message("Invalid or missing internal API Key.")
                    .data(null)
                    .build();
                return objectMapper.writeValueAsBytes(body);
            })
            .flatMap(bytes -> {
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                return exchange.getResponse().writeWith(Mono.just(buffer));
            });
    }
}

