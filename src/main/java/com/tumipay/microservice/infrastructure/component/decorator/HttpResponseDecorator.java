package com.tumipay.microservice.infrastructure.component.decorator;

import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * HttpResponseDecorator
 * <p>
 * Reactive HTTP response decorator implementation based on Spring WebFlux.
 * <p>
 * This component extends {@link ServerHttpResponseDecorator} in order to intercept,
 * enhance and observe outgoing HTTP responses within the reactive pipeline.
 * <p>
 * Main responsibilities:
 * <ul>
 *     <li>Inject correlation/request ID header into the response.</li>
 *     <li>Log HTTP response headers.</li>
 *     <li>Log HTTP response body content.</li>
 *     <li>Preserve reactive stream integrity.</li>
 * </ul>
 *
 * <p>
 * Architectural Context:
 * <ul>
 *     <li>Designed for non-blocking environments (Spring WebFlux).</li>
 *     <li>Operates within the reactive write lifecycle.</li>
 *     <li>Ensures proper {@link DataBuffer} memory management.</li>
 * </ul>
 * <p>
 * Observability Strategy:
 * <ul>
 *     <li>Uses Log4j2 for structured logging.</li>
 *     <li>Extracts correlation ID from {@link ThreadContext}.</li>
 *     <li>Enables full request-response tracing.</li>
 * </ul>
 * <p>
 * Memory Safety:
 * <ul>
 *     <li>Consumes original {@link DataBuffer} safely.</li>
 *     <li>Explicitly releases buffers using {@link DataBufferUtils#release(DataBuffer)}.</li>
 *     <li>Rebuilds a new immutable buffer for downstream propagation.</li>
 * </ul>
 * <p>
 * IMPORTANT:
 * <ul>
 *     <li>This decorator must only be used in reactive (WebFlux) stacks.</li>
 *     <li>Not compatible with Spring MVC (Servlet-based stack).</li>
 *     <li>Logging response bodies may impact performance for large payloads.</li>
 * </ul>
 *
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT
 * STANDARDS AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND
 * COPYRIGHT LAWS.
 *
 * @author TumiPay SAS
 * @since 03/03/2026
 */
@Log4j2
public class HttpResponseDecorator extends ServerHttpResponseDecorator {

    /**
     * Factory used to rebuild {@link DataBuffer} instances
     * after response body inspection.
     */
    private final DataBufferFactory bufferFactory;

    /**
     * Constructor.
     * Wraps the original {@link ServerHttpResponse} instance
     * while preserving its underlying configuration.
     *
     * @param delegate the original reactive HTTP response.
     */
    public HttpResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
        this.bufferFactory = delegate.bufferFactory();
    }

    /**
     * Intercepts the reactive response body write operation.
     * Processing flow:
     * <ul>
     *     <li>Add a correlation header (if available).</li>
     *     <li>Transform the outgoing {@link DataBuffer} stream.</li>
     *     <li>Log headers and response body content.</li>
     *     <li>Rebuild a new buffer for downstream continuation.</li>
     * </ul>
     * Reactive Behavior:
     * <ul>
     *     <li>Non-blocking transformation using {@link Flux#from(Publisher)}.</li>
     *     <li>Applies mapping function to each emitted {@link DataBuffer}.</li>
     * </ul>
     * @param body reactive publisher containing response data buffers.
     * @return completion signal for the write operation.
     */
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

        addRequestIdHeader();

        if (body == null) {
            return super.writeWith(Flux.empty());
        }

        return super.writeWith(
            Flux.from(body)
                .map(this::logAndRebuildBuffer)
        );
    }

    /**
     * Logs response headers and body content.
     * Technical Details:
     * <ul>
     *     <li>Reads raw bytes from the incoming {@link DataBuffer}.</li>
     *     <li>Converts content to UTF-8 String.</li>
     *     <li>Logs headers (DEBUG level).</li>
     *     <li>Logs body content (INFO level).</li>
     *     <li>Releases original buffer to avoid memory leaks.</li>
     *     <li>Wraps content into a new buffer instance.</li>
     * </ul>
     * Memory Management:
     * <ul>
     *     <li>Mandatory call to {@link DataBufferUtils#release(DataBuffer)}.</li>
     *     <li>Prevents Netty memory retention issues.</li>
     * </ul>
     * @param dataBuffer original emitted buffer.
     * @return rebuilt buffer ready for downstream writing.
     */
    private DataBuffer logAndRebuildBuffer(DataBuffer dataBuffer) {

        final byte[] content = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(content);

        final String responseBody = new String(content, StandardCharsets.UTF_8);

        log.debug(BaseIntegrationConstant.REQUEST_HEADERS_MESSAGE, getHeaders());
        log.info(BaseIntegrationConstant.RESPONSE_MESSAGE, responseBody);

        DataBufferUtils.release(dataBuffer);

        return bufferFactory.wrap(content);
    }

    /**
     * Injects correlation/request ID header into the HTTP response.
     * Behavior:
     * <ul>
     *     <li>Extracts request ID from {@link ThreadContext}.</li>
     *     <li>If present, adds it as response header.</li>
     *     <li>Does not overwrite existing header values.</li>
     * </ul>
     * This enables:
     * <ul>
     *     <li>Distributed tracing</li>
     *     <li>Cross-service correlation</li>
     *     <li>Observability compliance</li>
     * </ul>
     */
    private void addRequestIdHeader() {

        String requestId = ThreadContext.get(BaseIntegrationConstant.KEY_REQUEST_ID);

        if (requestId != null) {
            getHeaders().add(BaseIntegrationConstant.HEADER_REQUEST_ID, requestId);
        }
    }

    /**
     * Intercepts write-and-flush operations.
     * This method is typically invoked when streaming responses
     * or Server-Sent Events (SSE) are used.
     * Current Behavior:
     * <ul>
     *     <li>Logs HTTP status code at DEBUG level.</li>
     *     <li>Delegates directly to parent implementation.</li>
     * </ul>
     * @param body nested reactive publishers containing response buffers.
     * @return completion signal for the streaming write operation.
     */
    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {

        log.debug(
            BaseIntegrationConstant.HTTP_WRITE_AND_FLUSH_WITH_FORMAT,
            getStatusCode()
        );

        return super.writeAndFlushWith(body);
    }
}