package com.tumipay.microservice.infrastructure.component.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumipay.microservice.domain.service.contract.IWebhookSignatureValidator;
import com.tumipay.microservice.infrastructure.component.annotation.ProviderWebhookSigned;
import com.tumipay.microservice.infrastructure.component.dto.BaseApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * WebhookSignatureValidationFilter
 * <p>
 * Reactive WebFilter that validates the HMAC-SHA256 signature of Cobre signed webhook
 * requests on endpoints annotated with {@link ProviderWebhookSigned}.
 * <p>
 * Critical constraint: In Spring WebFlux, the request body can only be consumed once.
 * This filter captures the raw body BEFORE Spring deserializes it into the controller's
 * {@code @RequestBody}, validates the signature, then re-injects the original bytes back
 * into the exchange so the controller receives its DTO normally.
 * <p>
 * Execution flow:
 * <ol>
 *   <li>Resolve the handler for the current request via {@link RequestMappingHandlerMapping}</li>
 *   <li>Check for {@link ProviderWebhookSigned} at method or class level</li>
 *   <li>If not annotated, continue filter chain without body capture</li>
 *   <li>Capture the full raw body via {@link DataBufferUtils#join}</li>
 *   <li>Delegate validation to {@link IWebhookSignatureValidator}</li>
 *   <li>On failure, return 401 Unauthorized with {@link BaseApiResponse}</li>
 *   <li>On success, mutate the exchange to re-inject the captured body bytes</li>
 *   <li>Continue the filter chain with the mutated exchange</li>
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
public class SignatureValidationFilter implements WebFilter {

    private final IWebhookSignatureValidator webhookSignatureValidator;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        return requestMappingHandlerMapping.getHandler(exchange)
            .flatMap(handler -> {

                if (!(handler instanceof HandlerMethod handlerMethod)) {
                    return chain.filter(exchange).thenReturn(Boolean.TRUE);
                }

                boolean requiresSignatureValidation =
                    handlerMethod.hasMethodAnnotation(ProviderWebhookSigned.class) ||
                    handlerMethod.getBeanType().isAnnotationPresent(ProviderWebhookSigned.class);

                if (!requiresSignatureValidation) {
                    return chain.filter(exchange).thenReturn(Boolean.TRUE);
                }

                return captureAndValidate(exchange, chain);
            })
            .switchIfEmpty(Mono.defer(() -> chain.filter(exchange).thenReturn(Boolean.FALSE)))
            .then();
    }

    /**
     * Captures the raw body, validates the signature, and either rejects or continues
     * the chain with the body re-injected.
     */
    private Mono<Boolean> captureAndValidate(ServerWebExchange exchange, WebFilterChain chain) {

        return DataBufferUtils.join(exchange.getRequest().getBody())
            .flatMap(dataBuffer -> {
                byte[] rawBodyBytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(rawBodyBytes);
                DataBufferUtils.release(dataBuffer);

                String rawBody = new String(rawBodyBytes, StandardCharsets.UTF_8);

                return webhookSignatureValidator.validate(rawBody, exchange.getRequest().getHeaders())
                    .flatMap(result -> {

                        if (!result.valid()) {
                            log.warn(
                                "[WEBHOOK-SIGNATURE] Request rejected on path {}: {}",
                                exchange.getRequest().getPath().value(),
                                result.reason()
                            );
                            return writeUnauthorizedResponse(exchange, result.reason())
                                .thenReturn(Boolean.FALSE);
                        }

                        ServerWebExchange mutatedExchange = injectBody(exchange, rawBodyBytes);
                        return chain.filter(mutatedExchange).thenReturn(Boolean.TRUE);
                    });
            })
            .switchIfEmpty(
                Mono.defer(() ->
                    webhookSignatureValidator.validate("", exchange.getRequest().getHeaders())
                        .flatMap(result -> {
                            if (!result.valid()) {
                                return writeUnauthorizedResponse(exchange, result.reason())
                                    .thenReturn(Boolean.FALSE);
                            }
                            return chain.filter(exchange).thenReturn(Boolean.TRUE);
                        })
                )
            );
    }

    /**
     * Mutates the exchange to replace the request body with the captured raw bytes,
     * allowing the controller's {@code @RequestBody} to deserialize them normally.
     * <p>
     * Uses {@link ServerHttpRequestDecorator} to override {@code getBody()},
     * since the body stream can only be consumed once in WebFlux.
     */
    private ServerWebExchange injectBody(ServerWebExchange exchange, byte[] rawBodyBytes) {
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(rawBodyBytes);
        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.just(dataBuffer);
            }
        };
        return exchange.mutate().request(mutatedRequest).build();
    }

    /**
     * Writes a standardized 401 Unauthorized response with the rejection reason.
     */
    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, String reason) {

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return Mono.fromCallable(() -> {
                BaseApiResponse<Void> body = BaseApiResponse.<Void>builder()
                    .code("WEBHOOK_SIGNATURE_INVALID")
                    .status("FAILURE")
                    .message(reason != null ? reason : "Invalid webhook signature.")
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
