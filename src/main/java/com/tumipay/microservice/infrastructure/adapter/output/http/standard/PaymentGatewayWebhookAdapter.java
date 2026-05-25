package com.tumipay.microservice.infrastructure.adapter.output.http.standard;

import com.tumipay.microservice.domain.model.gateway.GatewayWebhookResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.output.IPaymentGatewayWebhookAdapterPort;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.dto.PaymentGatewayComposition;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.mapper.IGatewayWebhookMapper;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.request.GatewayWebhookRequest;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.response.GatewayWebhookResponse;
import com.tumipay.microservice.infrastructure.component.constant.BaseIntegrationConstant;
import com.tumipay.microservice.infrastructure.component.http.config.ConfigHttpIntegration;
import com.tumipay.microservice.infrastructure.component.http.contract.IHttpClientExecutor;
import com.tumipay.microservice.infrastructure.component.http.dto.ClientHttpRequest;
import com.tumipay.microservice.infrastructure.component.http.enums.HttpMethodEnum;
import com.tumipay.microservice.infrastructure.component.properties.PaymentGatewayProperties;
import com.tumipay.microservice.shared.enums.BaseErrorCodeEnum;
import com.tumipay.microservice.shared.exception.BusinessException;
import com.tumipay.microservice.shared.exception.GatewayWebhookException;
import com.tumipay.microservice.shared.util.CommonDurationUtils;
import com.tumipay.microservice.shared.util.CommonJsonUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
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

    private static final String EMPTY_BODY = "<empty body>";
    private static final String DEFAULT_WEBHOOK_EVENT_PATH = "/v1/webhook/payment-event";
    private static final String INTEGRATION_CODE = "tumipay-payment-gateway-webhook-dispatch";
    private static final int DEFAULT_MAX_RETRIES = 3;

    private static final Set<Integer> ACCEPTED_GATEWAY_STATUS_CODES = IntStream.rangeClosed(400, 599)
        .boxed()
        .collect(Collectors.toSet());

    private final PaymentGatewayProperties paymentGatewayProperties;
    private final IHttpClientExecutor httpClientExecutor;
    private final IGatewayWebhookMapper gatewayWebhookMapper;

    public PaymentGatewayWebhookAdapter(final PaymentGatewayProperties paymentGatewayProperties,
                                        final IHttpClientExecutor httpClientExecutor,
                                        final IGatewayWebhookMapper gatewayWebhookMapper) {

        this.paymentGatewayProperties = Objects.requireNonNull(paymentGatewayProperties);
        this.httpClientExecutor = Objects.requireNonNull(httpClientExecutor);
        this.gatewayWebhookMapper = Objects.requireNonNull(gatewayWebhookMapper);
    }

    @Override
    public Mono<GatewayWebhookResult> dispatchWebhookEvent(final WebhookEvent webhookEvent) {

        return createComposition(webhookEvent)
            .flatMap(this::validateRequest)
            .flatMap(this::dispatchWebhookEvent)
            .doOnSuccess(saved ->
                log.debug("Process invoke dispatchWebhookEvent executed successfully")
            )
            .doOnError(error ->
                log.error("Error in invoke dispatchWebhookEvent process error: {}", error.getMessage())
            );
    }

    private Mono<PaymentGatewayComposition> createComposition(final WebhookEvent webhookEvent) {

        return Mono.fromSupplier(() -> PaymentGatewayComposition.builder()
            .webhookEvent(webhookEvent)
            .gatewayWebhookRequest(gatewayWebhookMapper.buildGatewayWebhookRequest(webhookEvent))
            .webhookEventPath(Optional.ofNullable(paymentGatewayProperties.getEndpoints())
                .map(PaymentGatewayProperties.GatewayEndpoints::getWebhookEventPath)
                .filter(StringUtils::hasText)
                .orElse(DEFAULT_WEBHOOK_EVENT_PATH))
            .requestId(Optional.ofNullable(webhookEvent.getIdempotencyKey())
                .filter(StringUtils::hasText)
                .or(() -> Optional.ofNullable(webhookEvent.getUuid()).filter(StringUtils::hasText))
                .orElseGet(() -> UUID.randomUUID().toString()))
            .integrationId(Optional.ofNullable(webhookEvent.getUuid())
                .filter(StringUtils::hasText)
                .orElseGet(() -> UUID.randomUUID().toString()))
            .timeout(CommonDurationUtils.resolveProviderTimeout(paymentGatewayProperties.getTimeout()))
            .startNanos(System.nanoTime())
            .build());
    }

    private Mono<PaymentGatewayComposition> validateRequest(PaymentGatewayComposition composition) {

        if (composition.getWebhookEvent() == null) {
            return Mono.error(new GatewayWebhookException(
                BaseErrorCodeEnum.TUMIPAY_PAYMENT_GATEWAY_ERROR.getCode(),
                "Invalid composition: webhook event is null"
            ));
        }

        if (composition.getGatewayWebhookRequest() == null) {
            return Mono.error(new GatewayWebhookException(
                BaseErrorCodeEnum.TUMIPAY_PAYMENT_GATEWAY_ERROR.getCode(),
                "Invalid composition: gateway webhook request is null for event uuid=[" + composition.getWebhookEvent().getUuid() + "]"
            ));
        }

        return Mono.just(composition);
    }

    private Mono<GatewayWebhookResult> dispatchWebhookEvent(PaymentGatewayComposition composition) {

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Optional.ofNullable(composition.getWebhookEvent().getIdempotencyKey())
            .filter(StringUtils::hasText)
            .ifPresent(value -> headers.add(BaseIntegrationConstant.HEADER_IDEMPOTENCY_KEY, value));

        Optional.ofNullable(paymentGatewayProperties.getApiKey())
            .filter(StringUtils::hasText)
            .ifPresent(value -> headers.add(BaseIntegrationConstant.HEADER_API_KEY, value));

        Optional.ofNullable(composition.getWebhookEvent().getAdapterProviderCode())
            .filter(StringUtils::hasText)
            .ifPresent(value -> headers.add(BaseIntegrationConstant.HEADER_ADAPTER_PROVIDER_CODE, value));

        final ConfigHttpIntegration configHttpIntegration = ConfigHttpIntegration.builder()
            .integrationCode(INTEGRATION_CODE)
            .host(paymentGatewayProperties.getBaseUrl().trim())
            .integrationPath(composition.getWebhookEventPath())
            .timeout(composition.getTimeout())
            .retryEnabled(Boolean.TRUE)
            .maxRetries(DEFAULT_MAX_RETRIES)
            .build();

        final ClientHttpRequest<GatewayWebhookRequest> httpRequest = ClientHttpRequest.<GatewayWebhookRequest>builder()
            .configIntegration(configHttpIntegration)
            .method(HttpMethodEnum.POST)
            .headers(headers)
            .body(composition.getGatewayWebhookRequest())
            .timeout(composition.getTimeout())
            .acceptedStatusCodes(ACCEPTED_GATEWAY_STATUS_CODES)
            .requestId(composition.getRequestId())
            .integrationId(composition.getIntegrationId())
            .build();

        log.info(
            "Dispatching webhook event to Gateway - eventType=[{}], uuid=[{}], adapterProviderCode=[{}]",
            composition.getWebhookEvent().getEventType(),
            composition.getWebhookEvent().getUuid(),
            composition.getWebhookEvent().getAdapterProviderCode()
        );

        log.debug(
            "Dispatching webhook request payload: {}",
            CommonJsonUtils.toJson(composition.getGatewayWebhookRequest())
        );

        return httpClientExecutor.execute(httpRequest, String.class)
            .flatMap(response -> {

                final int statusCode = Optional.ofNullable(response.getStatusCode())
                    .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value());

                final String rawBody = Optional.ofNullable(response.getRawBody())
                    .filter(StringUtils::hasText)
                    .orElse(EMPTY_BODY);

                if (statusCode == HttpStatus.CONFLICT.value()) {
                    final GatewayWebhookResponse duplicateResponse = StringUtils.hasText(response.getRawBody())
                        ? CommonJsonUtils.fromJsonSafe(response.getRawBody(), GatewayWebhookResponse.class)
                        : null;

                    return Mono.just(
                        duplicateResponse != null
                            ? gatewayWebhookMapper.convertToResult(duplicateResponse)
                            : gatewayWebhookMapper.buildDuplicateResponse()
                    );
                }

                if (HttpStatusCode.valueOf(statusCode).is2xxSuccessful()) {
                    final GatewayWebhookResponse gatewayWebhookResponse = StringUtils.hasText(response.getRawBody())
                        ? CommonJsonUtils.fromJsonSafe(response.getRawBody(), GatewayWebhookResponse.class)
                        : null;

                    if (gatewayWebhookResponse == null) {
                        return Mono.error(new GatewayWebhookException(
                            BaseErrorCodeEnum.TUMIPAY_PAYMENT_GATEWAY_ERROR.getCode(),
                            "Gateway responded with unreadable body for HTTP [" + statusCode + "] dispatching event uuid=[" + composition.getWebhookEvent().getUuid() + "]"
                        ));
                    }

                    return Mono.just(gatewayWebhookMapper.convertToResult(gatewayWebhookResponse));
                }

                final String errorCode = statusCode >= 500
                    ? "GATEWAY_SERVER_ERROR_" + statusCode
                    : "GATEWAY_CLIENT_ERROR_" + statusCode;

                return Mono.error(new GatewayWebhookException(
                    errorCode,
                    "Gateway responded with HTTP [" + statusCode + "] dispatching event uuid=[" + composition.getWebhookEvent().getUuid() + "]: " + rawBody
                ));
            })
            .onErrorMap(
                TimeoutException.class,
                ex -> new GatewayWebhookException(
                    BaseErrorCodeEnum.TUMIPAY_PAYMENT_GATEWAY_ERROR.getCode(),
                    "Gateway webhook dispatch timed out after [" + composition.getTimeoutMs() + "ms] for event uuid=[" + composition.getWebhookEvent().getUuid() + "]"
                )
            )
            .onErrorMap(
                BusinessException.class,
                ex -> new GatewayWebhookException(
                    BaseErrorCodeEnum.TUMIPAY_PAYMENT_GATEWAY_ERROR.getCode(),
                    "Gateway webhook dispatch failed for event uuid=[" + composition.getWebhookEvent().getUuid() + "]: " + ex.getMessage()
                )
            )
            .doOnNext(response -> {

                final long latencyMs = (System.nanoTime() - composition.getStartNanos()) / 1_000_000;

                log.info(
                    "[TUMIPAY_PAYMENT_GATEWAY_WEBHOOK] Payment Gateway process webhook event successfully - uuid=[{}], code=[{}], status=[{}], gatewayEventId=[{}], latency=[{}ms]",
                    composition.getWebhookEvent().getUuid(),
                    response.getCode(),
                    response.getStatus(),
                    response.getData() != null ? response.getData().getGatewayEventId() : null,
                    latencyMs
                );
            })
            .doOnError(error -> log.error(
                "Failed to dispatch webhook event to Gateway - uuid=[{}], eventType=[{}], error=[{}]",
                composition.getWebhookEvent().getUuid(),
                composition.getWebhookEvent().getEventType(),
                error.getMessage()
            ));
    }
}