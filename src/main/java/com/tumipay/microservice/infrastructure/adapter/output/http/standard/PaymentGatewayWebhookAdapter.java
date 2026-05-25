package com.tumipay.microservice.infrastructure.adapter.output.http.standard;

import com.tumipay.microservice.domain.model.gateway.GatewayWebhookResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.output.IPaymentGatewayWebhookAdapterPort;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.request.GatewayWebhookRequest;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.response.GatewayWebhookResponse;
import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import com.tumipay.microservice.infrastructure.component.http.config.ConfigHttpIntegration;
import com.tumipay.microservice.infrastructure.component.http.contract.IHttpClientExecutor;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpRequest;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpResponse;
import com.tumipay.microservice.infrastructure.component.http.enums.HttpMethodEnum;
import com.tumipay.microservice.infrastructure.component.properties.PaymentGatewayProperties;
import com.tumipay.microservice.shared.exception.BusinessException;
import com.tumipay.microservice.shared.exception.GatewayWebhookException;
import com.tumipay.microservice.shared.util.CommonJsonUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static final String INTEGRATION_CODE = "TUMIPAY_PAYMENT_GATEWAY_WEBHOOK";
    private static final String DUPLICATE_EVENT_CODE = "DUPLICATE_EVENT";
    private static final String DUPLICATE_EVENT_STATUS = "FAILED";
    private static final String DUPLICATE_EVENT_MESSAGE = "Duplicate event — already acknowledged by Gateway";
    private static final String GATEWAY_HTTP_ERROR_CODE = "GATEWAY_HTTP_ERROR";
    private static final String GATEWAY_TIMEOUT_CODE = "GATEWAY_TIMEOUT";
    private static final String EMPTY_BODY = "<empty body>";
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final Set<Integer> ACCEPTED_GATEWAY_STATUS_CODES = IntStream.rangeClosed(400, 599)
        .boxed()
        .collect(Collectors.toSet());

    private final PaymentGatewayProperties paymentGatewayProperties;
    private final IHttpClientExecutor httpClientExecutor;

    public PaymentGatewayWebhookAdapter(final PaymentGatewayProperties paymentGatewayProperties,
                                        final IHttpClientExecutor httpClientExecutor) {

        this.paymentGatewayProperties = Objects.requireNonNull(paymentGatewayProperties);
        this.httpClientExecutor = Objects.requireNonNull(httpClientExecutor);
        log.info("Initialized Payment Gateway webhook adapter with integrationCode=[{}]", INTEGRATION_CODE);
    }

    @Override
    public Mono<GatewayWebhookResult> dispatchWebhookEvent(final WebhookEvent webhookEvent) {
        return Mono.defer(() -> {

            final long startNanos = System.nanoTime();
            final GatewayWebhookRequest request = buildGatewayWebhookRequest(webhookEvent);
            final long timeoutMs = resolveTimeoutMillis();
            final String path = resolveWebhookEventPath();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (StringUtils.hasText(webhookEvent.getIdempotencyKey())) {
                headers.add(BaseIntegrationConstant.HEADER_IDEMPOTENCY_KEY, webhookEvent.getIdempotencyKey());
            }

            if (StringUtils.hasText(paymentGatewayProperties.getApiKey())) {
                headers.add(BaseIntegrationConstant.HEADER_API_KEY, paymentGatewayProperties.getApiKey());
            }

            if (StringUtils.hasText(webhookEvent.getAdapterProviderCode())) {
                headers.add(BaseIntegrationConstant.HEADER_ADAPTER_PROVIDER_CODE, webhookEvent.getAdapterProviderCode());
            }

            final ConfigHttpIntegration configHttpIntegration = ConfigHttpIntegration.builder()
                .integrationCode(INTEGRATION_CODE)
                .host(paymentGatewayProperties.getBaseUrl().trim())
                .integrationPath(path)
                .timeout(Duration.ofMillis(timeoutMs))
                .retryEnabled(Boolean.TRUE)
                .maxRetries(DEFAULT_MAX_RETRIES)
                .build();

            final ClientHttpRequest<GatewayWebhookRequest> httpRequest = ClientHttpRequest.<GatewayWebhookRequest>builder()
                .configIntegration(configHttpIntegration)
                .method(HttpMethodEnum.POST)
                .headers(headers)
                .body(request)
                .timeout(Duration.ofMillis(timeoutMs))
                .acceptedStatusCodes(ACCEPTED_GATEWAY_STATUS_CODES)
                .requestId(resolveRequestId(webhookEvent))
                .integrationId(resolveIntegrationId(webhookEvent))
                .build();

            log.info(
                "Dispatching webhook event to Gateway - eventType=[{}], uuid=[{}], adapterProviderCode=[{}]",
                webhookEvent.getEventType(),
                webhookEvent.getUuid(),
                webhookEvent.getAdapterProviderCode()
            );

            log.debug("Dispatching webhook request payload: {}",
                CommonJsonUtils.toJson(request)
            );

            return httpClientExecutor
                .execute(httpRequest, String.class)
                .flatMap(response -> handleGatewayResponse(response, webhookEvent))
                .onErrorMap(
                    TimeoutException.class,
                    ex -> new GatewayWebhookException(
                        GATEWAY_TIMEOUT_CODE,
                        "Gateway webhook dispatch timed out after [" + timeoutMs + "ms] for event uuid=[" + webhookEvent.getUuid() + "]"
                    )
                )
                .onErrorMap(
                    BusinessException.class,
                    ex -> new GatewayWebhookException(
                        GATEWAY_HTTP_ERROR_CODE,
                        "Gateway webhook dispatch failed for event uuid=[" + webhookEvent.getUuid() + "]: " + ex.getMessage()
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

    private String resolveWebhookEventPath() {
        return Optional.ofNullable(paymentGatewayProperties.getEndpoints())
            .map(PaymentGatewayProperties.GatewayEndpoints::getWebhookEventPath)
            .filter(StringUtils::hasText)
            .orElse("/v1/webhook/payment-event");
    }

    private String resolveRequestId(final WebhookEvent webhookEvent) {
        return Optional.ofNullable(webhookEvent.getIdempotencyKey())
            .filter(StringUtils::hasText)
            .orElseGet(() -> Optional.ofNullable(webhookEvent.getUuid())
                .filter(StringUtils::hasText)
                .orElseGet(() -> UUID.randomUUID().toString()));
    }

    private String resolveIntegrationId(final WebhookEvent webhookEvent) {
        return Optional.ofNullable(webhookEvent.getUuid())
            .filter(StringUtils::hasText)
            .orElseGet(() -> UUID.randomUUID().toString());
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

    private Mono<GatewayWebhookResult> handleGatewayResponse(final ClientHttpResponse<String> response,
                                                             final WebhookEvent webhookEvent) {
        final int statusCode = Optional.ofNullable(response.getStatusCode())
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value());
        final String rawBody = Optional.ofNullable(response.getRawBody())
            .filter(body -> !body.isBlank())
            .orElse(EMPTY_BODY);

        if (statusCode == HttpStatus.CONFLICT.value()) {
            final GatewayWebhookResponse duplicateResponse = deserializeGatewayWebhookResponse(response.getRawBody());
            return Mono.just(duplicateResponse != null ? convertToDto(duplicateResponse) : buildDuplicateResponse());
        }

        if (HttpStatusCode.valueOf(statusCode).is2xxSuccessful()) {
            final GatewayWebhookResponse gatewayResponse = deserializeGatewayWebhookResponse(response.getRawBody());

            if (gatewayResponse == null) {
                return Mono.error(new GatewayWebhookException(
                    GATEWAY_HTTP_ERROR_CODE,
                    "Gateway responded with unreadable body for HTTP [" + statusCode + "] dispatching event uuid=["
                        + webhookEvent.getUuid() + "]"
                ));
            }

            return Mono.just(convertToDto(gatewayResponse));
        }

        return Mono.error(buildGatewayHttpException(statusCode, rawBody, webhookEvent));
    }

    private GatewayWebhookResponse deserializeGatewayWebhookResponse(final String rawBody) {
        if (!StringUtils.hasText(rawBody)) {
            return null;
        }

        return CommonJsonUtils.fromJsonSafe(rawBody, GatewayWebhookResponse.class);
    }

    private GatewayWebhookException buildGatewayHttpException(final int statusCode,
                                                              final String body,
                                                              final WebhookEvent webhookEvent) {
        final String errorCode = statusCode >= 500
            ? "GATEWAY_SERVER_ERROR_" + statusCode
            : "GATEWAY_CLIENT_ERROR_" + statusCode;

        return new GatewayWebhookException(
            errorCode,
            "Gateway responded with HTTP [" + statusCode + "] dispatching event uuid=["
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