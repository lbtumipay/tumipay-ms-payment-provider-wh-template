package com.tumipay.microservice.infrastructure.adapter.output.http.standard;

import com.tumipay.microservice.domain.model.gateway.GatewayWebhookResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.output.IPaymentGatewayWebhookAdapterPort;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.request.GatewayWebhookRequest;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.response.GatewayWebhookResponse;
import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import com.tumipay.microservice.infrastructure.component.properties.PaymentGatewayProperties;
import com.tumipay.microservice.shared.exception.GatewayWebhookException;
import com.tumipay.microservice.shared.properties.WebClientProperties;
import com.tumipay.microservice.shared.util.CommonJsonUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * PaymentGatewayWebhookAdapter
 * <p>
 * Output HTTP adapter that dispatches normalized webhook events to the TumiPay Payment Gateway.
 * Implements the standard Gateway webhook dispatch contract defined in
 * {@link IPaymentGatewayWebhookAdapterPort}.
 * <p>
 * Used by {@code WebhookWorkerUseCase} to forward provider events already persisted in the
 * {@code tp_provider_webhook_event} table after they are claimed by the Claim-Batch worker.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 14/04/2026
 */
@Log4j2
@Component
public class PaymentGatewayWebhookAdapter implements IPaymentGatewayWebhookAdapterPort {

    private static final String DUPLICATE_EVENT_CODE = "DUPLICATE_EVENT";
    private static final String DUPLICATE_EVENT_STATUS = "FAILED";
    private static final String DUPLICATE_EVENT_MESSAGE = "Duplicate event — already acknowledged by Gateway";
    private static final String GATEWAY_TIMEOUT_CODE = "GATEWAY_TIMEOUT";
    private static final String EMPTY_BODY = "<empty body>";

    private final PaymentGatewayProperties paymentGatewayProperties;
    private final WebClient webClient;

    public PaymentGatewayWebhookAdapter(final PaymentGatewayProperties paymentGatewayProperties,
                                        final WebClient webClient,
                                        final WebClientProperties webClientProperties) {

        this.paymentGatewayProperties = Objects.requireNonNull(paymentGatewayProperties);
        this.webClient = webClient
            .mutate()
            .baseUrl(paymentGatewayProperties.getBaseUrl())
            .build();

        log.info("Initialized gatewayClient {}", this.webClient);
    }


    @Override
    public Mono<GatewayWebhookResult> dispatchWebhookEvent(final WebhookEvent webhookEvent) {
        return Mono.defer(() -> {
            final long startNanos = System.nanoTime();
            final GatewayWebhookRequest request = buildGatewayWebhookRequest(webhookEvent);
            final long timeoutMs = resolveTimeoutMillis();

            final String baseUrl = paymentGatewayProperties.getBaseUrl();
            final String path = Optional.ofNullable(paymentGatewayProperties.getEndpoints())
                .map(PaymentGatewayProperties.GatewayEndpoints::getWebhookEventPath)
                .filter(StringUtils::hasText)
                .orElse("/v1/webhook/payment-event");

            final URI uri = UriComponentsBuilder
                .fromUriString(baseUrl.trim())
                .path(path)
                .build()
                .toUri();

            log.info(
                "Dispatching webhook event to Gateway - eventType=[{}], uuid=[{}], adapterProviderCode=[{}]",
                webhookEvent.getEventType(),
                webhookEvent.getUuid(),
                webhookEvent.getAdapterProviderCode()
            );

            log.debug("Dispatching webhook request payload: {}",
                CommonJsonUtils.toJson(request)
            );

            return webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header(BaseIntegrationConstant.HEADER_IDEMPOTENCY_KEY, webhookEvent.getIdempotencyKey())
                .header(BaseIntegrationConstant.HEADER_API_KEY, paymentGatewayProperties.getApiKey())
                .header(BaseIntegrationConstant.HEADER_ADAPTER_PROVIDER_CODE, webhookEvent.getAdapterProviderCode())
                .bodyValue(request)
                .exchangeToMono(response -> handleGatewayResponse(response.statusCode(), response, webhookEvent))
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorMap(
                    TimeoutException.class,
                    ex -> new GatewayWebhookException(
                        GATEWAY_TIMEOUT_CODE,
                        "Gateway webhook dispatch timed out after [" + timeoutMs + "ms] for event uuid=["
                            + webhookEvent.getUuid() + "]",
                        ex
                    )
                )
                .doOnNext(response -> logSuccess(webhookEvent, response, startNanos))
                .doOnError(error -> log.error(
                    "Failed to dispatch webhook event to Gateway - uuid=[{}], eventType=[{}], error=[{}]",
                    webhookEvent.getUuid(),
                    webhookEvent.getEventType(),
                    error.getMessage()
                ));
        });
    }

    private GatewayWebhookRequest buildGatewayWebhookRequest(final WebhookEvent webhookEvent) {
        return GatewayWebhookRequest.builder()
            .eventId(webhookEvent.getUuid())
            .eventType(webhookEvent.getEventType())
            .adapterProviderCode(webhookEvent.getAdapterProviderCode())
            .transactionId(webhookEvent.getTransactionId())
            .referenceId(webhookEvent.getReferenceId())
            .providerTransactionId(webhookEvent.getProviderTransactionId())
            .eventRequest(deserializeEventRequest(webhookEvent.getEventRequest()))
            .receivedAt(webhookEvent.getReceivedAt())
            .build();
    }

    private Object deserializeEventRequest(final String eventRequestJson) {

        if (eventRequestJson == null || eventRequestJson.isBlank()) {
            return null;
        }

        try {
            return CommonJsonUtils.fromJson (eventRequestJson, Object.class);
        } catch (Exception ex) {
            log.warn(
                "Could not deserialize eventRequest as JSON object — uuid payload will be sent as raw string. cause=[{}]",
                ex.getMessage()
            );
            return eventRequestJson;
        }
    }

    private Mono<GatewayWebhookResult> handleGatewayResponse(final HttpStatusCode status,
                                                             final ClientResponse response,
                                                             final WebhookEvent webhookEvent) {

        if (status == HttpStatus.CONFLICT) {
            return response.bodyToMono(GatewayWebhookResponse.class)
                .map(this::convertToDto)
                .onErrorResume(DecodingException.class, ex -> Mono.empty())
                .switchIfEmpty(Mono.fromSupplier(this::buildDuplicateResponse));
        }

        if (status.is2xxSuccessful()) {
            return response.bodyToMono(GatewayWebhookResponse.class)
                .map(this::convertToDto);
        }

        return response.bodyToMono(String.class)
            .defaultIfEmpty(EMPTY_BODY)
            .flatMap(body -> Mono.error(buildGatewayHttpException(status, body, webhookEvent)));
    }

    private GatewayWebhookException buildGatewayHttpException(final HttpStatusCode status,
                                                              final String body,
                                                              final WebhookEvent webhookEvent) {
        final String errorCode = status.is5xxServerError()
            ? "GATEWAY_SERVER_ERROR_" + status.value()
            : "GATEWAY_CLIENT_ERROR_" + status.value();

        return new GatewayWebhookException(
            errorCode,
            "Gateway responded with HTTP [" + status.value() + "] dispatching event uuid=["
                + webhookEvent.getUuid() + "]: " + body
        );
    }

    private GatewayWebhookResult buildDuplicateResponse() {
        return GatewayWebhookResult.builder()
            .code(DUPLICATE_EVENT_CODE)
            .status(DUPLICATE_EVENT_STATUS)
            .message(DUPLICATE_EVENT_MESSAGE)
            .build();
    }

    private long resolveTimeoutMillis() {
        return paymentGatewayProperties.getTimeout();
    }

    private void logSuccess(final WebhookEvent webhookEvent,
                            final GatewayWebhookResult response,
                            final long startNanos) {
        final long latencyMs = (System.nanoTime() - startNanos) / 1_000_000;

        log.info(
            "[TUMIPAY_PAYMENT_GATEWAY_WEBHOOK] Payment Gateway process webhook event successfully - uuid=[{}], code=[{}], status=[{}], gatewayEventId=[{}], latency=[{}ms]",
            webhookEvent.getUuid(),
            response.getCode(),
            response.getStatus(),
            response.getData() != null ? response.getData().getGatewayEventId() : null,
            latencyMs
        );
    }

    /**
     * Converts GatewayWebhookResponse (HTTP DTO) to GatewayWebhookResponseDto (domain contract).
     */
    private GatewayWebhookResult convertToDto(final GatewayWebhookResponse response) {
        if (response == null) {
            return null;
        }

        GatewayWebhookResult.GatewayWebhookData dataDto = null;
        if (response.getData() != null) {
            dataDto = GatewayWebhookResult.GatewayWebhookData.builder()
                .gatewayEventId(response.getData().getGatewayEventId())
                .eventId(response.getData().getEventId())
                .build();
        }

        return GatewayWebhookResult.builder()
            .code(response.getCode())
            .status(response.getStatus())
            .message(response.getMessage())
            .data(dataDto)
            .build();
    }
}