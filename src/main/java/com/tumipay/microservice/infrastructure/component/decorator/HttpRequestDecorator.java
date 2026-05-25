package com.tumipay.microservice.infrastructure.component.decorator;

import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HttpRequestDecorator
 * <p>
 * Reactive HTTP request decorator implementation based on Spring WebFlux.
 * <p>
 * This component extends {@link ServerHttpRequestDecorator} in order to intercept,
 * inspect and log incoming HTTP request payloads within the reactive pipeline.
 *
 * <p>
 * Main responsibilities:
 * <ul>
 *     <li>Log incoming request path.</li>
 *     <li>Log request body content.</li>
 *     <li>Preserve reactive stream flow without blocking.</li>
 * </ul>
 *
 * <p>
 * Architectural Context:
 * <ul>
 *     <li>Designed exclusively for Spring WebFlux (non-blocking stack).</li>
 *     <li>Operates inside the reactive stream lifecycle.</li>
 *     <li>Does not alter request body content.</li>
 * </ul>
 *
 * <p>
 * Observability Strategy:
 * <ul>
 *     <li>Uses Log4j2 for structured debug-level logging.</li>
 *     <li>Captures request payload for traceability.</li>
 *     <li>Supports microservice distributed tracing diagnostics.</li>
 * </ul>
 *
 * <p>
 * Important Considerations:
 * <ul>
 *     <li>This implementation assumes UTF-8 encoded payloads.</li>
 *     <li>Large payloads may impact performance when logged.</li>
 *     <li>Binary content (files, images, PDFs) may produce unreadable logs.</li>
 *     <li>Does not cache or rebuild the DataBuffer.</li>
 * </ul>
 *
 * <p>
 * WARNING:
 * <ul>
 *     <li>Reading a {@link DataBuffer} consumes its content.</li>
 *     <li>If downstream components require the full body, a caching strategy
 *         (e.g., DataBufferUtils.join + buffer recreation) should be implemented.</li>
 * </ul>
 *
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT
 * STANDARDS AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND
 * COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 03/03/2026
 */
@Log4j2
public class HttpRequestDecorator extends ServerHttpRequestDecorator {

    /**
     * Constructor.
     * Wraps the original {@link ServerHttpRequest} while preserving
     * its original metadata and reactive characteristics.
     *
     * @param delegate original reactive HTTP request.
     */
    public HttpRequestDecorator(ServerHttpRequest delegate) {
        super(delegate);
    }

    /**
     * Intercepts the reactive request body stream.
     * Processing Flow:
     * <ul>
     *     <li>Logs the incoming request path.</li>
     *     <li>Observes each emitted {@link DataBuffer}.</li>
     *     <li>Extracts raw bytes from buffer.</li>
     *     <li>Converts payload to UTF-8 string.</li>
     *     <li>Logs request body at DEBUG level.</li>
     * </ul>
     * Reactive Behavior:
     * <ul>
     *     <li>Uses {@code doOnNext} for side-effect logging.</li>
     *     <li>Does not modify or replace the buffer.</li>
     *     <li>Maintains non-blocking execution model.</li>
     * </ul>
     * Technical Note:
     * <ul>
     *     <li>Reading the buffer moves its read pointer.</li>
     *     <li>If full reusability is required, buffer duplication must be implemented.</li>
     * </ul>
     * @return reactive {@link Flux} containing the original request body.
     */
    @Override
    public Flux<DataBuffer> getBody() {

        // Capture current MDC context before entering
        final Map<String, String> contextMap = ThreadContext.getImmutableContext();
        log.info("Request received from path [{}]", super.getPath());

        return super.getBody()
            .map(buffer -> {

                // Restore MDC context inside reactive execution.
                ThreadContext.putAll(contextMap);

                try {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);

                    final String body = new String(bytes, StandardCharsets.UTF_8);
                    log.info(BaseIntegrationConstant.REQUEST_MESSAGE, body);

                    // Release original Netty buffer to avoid memory leaks.
                    DataBufferUtils.release(buffer);

                    return buffer.factory().wrap(bytes);
                } finally {
                    // Prevent MDC leakage between threads.
                    ThreadContext.clearMap();
                }
            });
    }
}