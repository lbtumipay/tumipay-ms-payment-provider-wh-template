package com.tumipay.microservice.infrastructure.component.http.factory;

import com.tumipay.microservice.infrastructure.component.properties.WebClientProperties;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.context.Context;
import reactor.util.context.ContextView;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebClientFactory
 * <p>
 * WebClientFactory class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/05/2026
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class WebClientFactory {

    private static final String MASKED_VALUE = "***MASKED***";
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
        HttpHeaders.AUTHORIZATION.toLowerCase(Locale.ROOT),
        HttpHeaders.COOKIE.toLowerCase(Locale.ROOT),
        HttpHeaders.SET_COOKIE.toLowerCase(Locale.ROOT),
        "x-api-key",
        "api-key"
    );

    private final WebClientProperties properties;
    private final ConcurrentHashMap<String, WebClient> webClientCache = new ConcurrentHashMap<>();

    public WebClient createWebClient(final String adapterProviderCode) {
        return webClientCache
            .computeIfAbsent(adapterProviderCode, this::create);
    }

    private WebClient create(final String adapterProviderCode) {

        final ConnectionProvider provider = ConnectionProvider.builder(adapterProviderCode)
            .maxConnections(properties.getPool().getMaxConnections())
            .pendingAcquireTimeout(properties.getPool().getPendingAcquireTimeout())
            .maxIdleTime(properties.getPool().getMaxIdleTime())
            .maxLifeTime(properties.getPool().getMaxLifeTime())
            .evictInBackground(properties.getPool().getEvictInBackground())
            .build();

        final HttpClient httpClient = HttpClient.create(provider)
            .compress(true)
            .followRedirect(true)
            .responseTimeout(properties.getTimeout().getResponse())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getTimeout().getConnect().toMillis())
            .keepAlive(properties.getTcp().isKeepAlive());

        return WebClient.builder()
            .filter(addDataToContext)
            .filter(filterRequest())
            .filter(filterResponse())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    private final ExchangeFilterFunction addDataToContext = (request, next) -> {
        final Instant startTime = Instant.now();
        final String integrationRequestId = UUID.randomUUID().toString();
        return next.exchange(request).contextWrite(Context.of("startTime", startTime, "integrationRequestId", integrationRequestId));
    };

    private ExchangeFilterFunction filterRequest() {

        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> Mono.deferContextual(contextView -> {
            logExternalRequest(clientRequest, contextView);

            return Mono.defer(() -> Mono.just(ClientRequest.from(clientRequest).body((outputMessage, context) ->
                clientRequest.body().insert(new ClientHttpRequestDecorator(outputMessage) {

                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        return super.writeWith(Mono.from(body).doOnNext(WebClientFactory::logExternalRequestBody));
                    }
                }, context)
            ).build()));
        }));
    }

    private ExchangeFilterFunction filterResponse() {

        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> Mono.deferContextual(contextView -> {
            final Instant startTime = contextView.getOrDefault("startTime", Instant.now());
            final String integrationRequestId = contextView.getOrDefault("integrationRequestId", "");

            return clientResponse.bodyToMono(String.class).defaultIfEmpty("No Body").flatMap(body -> {
                logExternalResponse(clientResponse, body, startTime, integrationRequestId);

                return Mono.just(ClientResponse.create(clientResponse.statusCode())
                    .headers(headers -> headers.addAll(clientResponse.headers().asHttpHeaders()))
                    .cookies(cookies -> cookies.addAll(clientResponse.cookies()))
                    .body(body)
                    .build());
            });
        }));
    }

    private void logExternalRequest(ClientRequest clientRequest, ContextView contextView) {

        final Instant startTime = contextView.getOrDefault("startTime", Instant.now());
        final String integrationRequestId = contextView.getOrDefault("integrationRequestId", "");
        final List<String> headers = sanitizeHeaders(clientRequest.headers());

        log.info("External Request: IntegrationRequestId={} Method={} URL={} StartTime={} Headers={}",
            integrationRequestId,
            clientRequest.method(),
            clientRequest.url(),
            startTime,
            headers
        );
    }

    private static void logExternalRequestBody(DataBuffer dataBuffer) {

        if (!log.isDebugEnabled()) {
            return;
        }

        final String requestBody = dataBuffer.toString(StandardCharsets.UTF_8).isBlank() ? "No Body" : dataBuffer.toString(StandardCharsets.UTF_8);

        log.debug("External Request Body: {}", requestBody);
    }

    private static void logExternalResponse(ClientResponse clientResponse, String body, Instant startTime, String integrationRequestId) {

        final Instant endTime = Instant.now();
        final Duration duration = Duration.between(startTime, endTime);

        log.info("External Response: IntegrationRequestId={} Status={} StartTime={} EndTime={} Duration={}ms",
            integrationRequestId,
            clientResponse.statusCode(),
            startTime,
            endTime,
            duration.toMillis()
        );

        if (log.isDebugEnabled()) {
            log.debug("External Response Body: {}", body);
        }
    }

    private static List<String> sanitizeHeaders(HttpHeaders headers) {

        return headers.headerSet()
            .stream()
            .map(header -> header.getKey() + "=" + sanitizeHeaderValue(header.getKey(), header.getValue()))
            .toList();
    }

    private static String sanitizeHeaderValue(String headerName, List<String> values) {

        if (SENSITIVE_HEADERS.contains(headerName.toLowerCase(Locale.ROOT))) {
            return MASKED_VALUE;
        }

        return String.join(",", values);
    }
}