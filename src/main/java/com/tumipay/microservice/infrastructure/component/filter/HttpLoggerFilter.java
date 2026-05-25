package com.tumipay.microservice.infrastructure.component.filter;

import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import com.tumipay.microservice.infrastructure.component.decorator.HttpRequestDecorator;
import com.tumipay.microservice.infrastructure.component.decorator.HttpResponseDecorator;
import com.tumipay.microservice.infrastructure.component.util.HttpWebFilterUtils;
import com.tumipay.microservice.shared.util.CommonStringUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * HttpLoggerFilter
 * <p>
 * Reactive logging interceptor for HTTP traffic in Spring WebFlux-based microservices.
 * <p>
 * This component is responsible for:
 * <ul>
 *     <li>Extracting correlation headers (merchantId and requestId).</li>
 *     <li>Generating a requestId if not present.</li>
 *     <li>Injecting contextual metadata into the Log4j2 {@link ThreadContext} (MDC).</li>
 *     <li>Decorating the {@link org.springframework.http.server.reactive.ServerHttpRequest}
 *         and {@link org.springframework.http.server.reactive.ServerHttpResponse}
 *         to enable request/response body logging.</li>
 *     <li>Skipping logging logic for health-check endpoints.</li>
 * </ul>
 *
 * <p><b>Architecture Context</b></p>
 * This interceptor operates at the WebFilter layer of Spring WebFlux,
 * meaning it participates in the reactive filter chain before the request
 * reaches the controller layer.
 *
 * <p><b>Observability Strategy</b></p>
 * The interceptor guarantees traceability across distributed systems by:
 * <ul>
 *     <li>Standardizing correlation identifiers.</li>
 *     <li>Ensuring MDC propagation for structured logging.</li>
 *     <li>Allowing downstream components to log using the same contextual identifiers.</li>
 * </ul>
 *
 * <p><b>Health Endpoint Optimization</b></p>
 * Requests identified as common health endpoints are excluded from
 * decoration and logging to reduce log noise and improve performance.
 *
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS
 * AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 03/03/2026
 */
@Log4j2
@Component
public class HttpLoggerFilter implements WebFilter {

    /**
     * Filters incoming HTTP requests in a reactive WebFlux pipeline.
     * <p>
     * Execution flow:
     * <ol>
     *     <li>Validates whether the request targets a health endpoint.</li>
     *     <li>Extracts {@code merchantId} and {@code requestId} from headers.</li>
     *     <li>If missing, applies fallback strategy:
     *          <ul>
     *              <li>{@code merchantId}: defaults to "merchantId-not-found".</li>
     *              <li>{@code requestId}: auto-generated unique identifier.</li>
     *          </ul>
     *     </li>
     *     <li>Injects identifiers into Log4j2 {@link ThreadContext} for MDC logging.</li>
     *     <li>Decorates request and response to enable payload interception.</li>
     *     <li>Continues the reactive filter chain.</li>
     * </ol>
     *
     * <p><b>Headers Processed</b></p>
     * <ul>
     *     <li>{@link BaseIntegrationConstant#HEADER_MERCHANT_ID}</li>
     *     <li>{@link BaseIntegrationConstant#HEADER_REQUEST_ID}</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * Although WebFlux is reactive and non-blocking, the MDC context is set
     * at the beginning of the filter to ensure log correlation consistency
     * across the execution flow.
     *
     * <p><b>Important Considerations</b></p>
     * <ul>
     *     <li>No blocking operations are introduced.</li>
     *     <li>Request/response bodies are wrapped but not consumed at this stage.</li>
     *     <li>Health endpoints bypass logging logic.</li>
     * </ul>
     *
     * @param serverWebExchange the current HTTP exchange containing request and response objects.
     * @param webFilterChain    the reactive filter chain used to delegate processing.
     * @return {@link Mono<Void>} that completes when request processing finishes.
     */
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange serverWebExchange, @NonNull WebFilterChain webFilterChain) {

        // Skip logging for health endpoints
        if (Boolean.TRUE.equals(HttpWebFilterUtils.validateCommonHealthEndpoints.apply(serverWebExchange))) {
            return webFilterChain.filter(serverWebExchange.mutate().build());
        }

        // Extract merchantId header with fallback
        final var merchantId = CommonStringUtils.defaultIfEmpty(
            serverWebExchange
                .getRequest()
                .getHeaders()
                .getFirst(BaseIntegrationConstant.HEADER_MERCHANT_ID),
            "merchantId-not-found"
        );

        // Extract or generate requestId
        final var requestId = CommonStringUtils.defaultIfEmpty(
            serverWebExchange
                .getRequest()
                .getHeaders()
                .getFirst(BaseIntegrationConstant.HEADER_REQUEST_ID),
            HttpWebFilterUtils.generateRequestId()
        );

        // Extract or generate operationId
        final var operationId = HttpWebFilterUtils.getOperationIdFromPath(
            serverWebExchange
        );

        // Inject correlation identifiers into MDC
        ThreadContext.put(BaseIntegrationConstant.KEY_REQUEST_ID, requestId);
        ThreadContext.put(BaseIntegrationConstant.KEY_MERCHANT_ID, merchantId);
        ThreadContext.put(BaseIntegrationConstant.KEY_OPERATION_ID, operationId);

        log.trace("merchantId [{}]", merchantId);
        log.trace("requestId [{}]", requestId);

        // Decorate request and response for logging purposes
        return webFilterChain
            .filter(
                serverWebExchange.mutate()
                    .request(new HttpRequestDecorator(serverWebExchange.getRequest()))
                    .response(new HttpResponseDecorator(serverWebExchange.getResponse()))
                    .build()
            )
            .contextWrite(context -> context.put(BaseIntegrationConstant.KEY_OPERATION_ID, operationId))
            .contextWrite(context -> context.put(BaseIntegrationConstant.KEY_MERCHANT_ID, merchantId))
            .contextWrite(context -> context.put(BaseIntegrationConstant.KEY_REQUEST_ID, requestId));
    }
}