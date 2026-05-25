package com.tumipay.microservice.infrastructure.adapter.input.http.provider;

import com.tumipay.microservice.domain.port.input.IWebhookEventUseCase;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.mapper.IWebhookEventHttpMapper;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.request.ProviderWebhookRequest;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.response.ProviderWebhookResponse;
import com.tumipay.microservice.infrastructure.component.annotation.ProviderWebhookSigned;
import com.tumipay.microservice.infrastructure.component.util.ReactiveControllerUtils;
import com.tumipay.microservice.shared.exception.BusinessException;
import com.tumipay.microservice.shared.properties.PaymentProvidersProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * WebhookController
 * <p>
 * REST controller for provider webhook intake.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/webhook")
public class ProviderWebhookController {

    private final PaymentProvidersProperties paymentProvidersProperties;
    private final IWebhookEventHttpMapper webhookEventHttpMapper;
    private final IWebhookEventUseCase webhookEventUseCase;

    @ProviderWebhookSigned
    @PostMapping(
        path = "/event",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<ProviderWebhookResponse>> receiveWebhookEvent(
        @RequestBody final ProviderWebhookRequest providerWebhookRequest) {

        return webhookEventHttpMapper.mapToDomain(
                providerWebhookRequest,
                paymentProvidersProperties.getCode()
            )
            .flatMap(webhookEventUseCase::processWebhookEvent)
            .flatMap(webhookEventHttpMapper::mapToResponse)
            .flatMap(this::mapToWebhookResponse)
            .doOnSuccess(ReactiveControllerUtils.logOnSuccess("receiveWebhookEvent"))
            .doOnError(ReactiveControllerUtils.logOnError("receiveWebhookEvent"))
            .onErrorResume(this::handleOnDuplicateWebhookError);
    }

    public <T> Mono<ResponseEntity<ProviderWebhookResponse>> mapToWebhookResponse(final ProviderWebhookResponse response) {
        return Mono.just(ResponseEntity.ok(
            response
        ));
    }

    private Mono<ResponseEntity<ProviderWebhookResponse>> handleOnDuplicateWebhookError(Throwable throwable) {

        if (throwable instanceof BusinessException be && "DUPLICATE_WEBHOOK_EVENT".equals(be.getCode())) {
            return Mono.just(
                ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ProviderWebhookResponse.builder()
                        .code("DUPLICATE_WEBHOOK_EVENT")
                        .message("Message with the same idempotency key already received and processed")
                        .build()
                    )
            );
        }

        return Mono.error(throwable);
    }
}

