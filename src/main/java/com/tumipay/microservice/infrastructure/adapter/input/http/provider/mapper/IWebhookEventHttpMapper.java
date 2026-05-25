package com.tumipay.microservice.infrastructure.adapter.input.http.provider.mapper;

import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.model.webhook.WebhookEventResult;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.request.ProviderWebhookRequest;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.response.ProviderWebhookResponse;
import com.tumipay.microservice.shared.util.CommonJsonUtils;
import com.tumipay.microservice.shared.util.CommonStringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * IWebhookEventHttpMapper
 * <p>
 * HTTP mapper for webhook domain and response contracts.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IWebhookEventHttpMapper {


    default Mono<WebhookEvent> mapToDomain(ProviderWebhookRequest providerWebhookRequest, String adapterProviderCode) {

        final String resolvedExternalEventId = resolveExternalEventId(providerWebhookRequest);
        final String resolvedIdempotencyKey = resolveIdempotencyKey(
            adapterProviderCode,
            resolvedExternalEventId
        );
        final String resolvedEventType = WebhookEventTypeEnum.UNKNOWN_EVENT.toString();

        return Mono.just(WebhookEvent.builder()
            .adapterProviderCode(adapterProviderCode)
            .eventType(resolvedEventType)
            .externalEventId(resolvedExternalEventId)
            .idempotencyKey(resolvedIdempotencyKey)
            .eventRequest(CommonJsonUtils.toJson(providerWebhookRequest))
            .build());
    }

    default Mono<ProviderWebhookResponse> mapToResponse(WebhookEventResult result) {

        if (result.getProcessingStatus() == null) {
            return Mono.just(ProviderWebhookResponse.builder()
                .code("ERROR")
                .message("Error processing webhook event")
                .build());
        }

        if (WebhookProcessingStatusEnum.FAILED.equals(result.getProcessingStatus())) {
            return Mono.just(ProviderWebhookResponse.builder()
                .code("FAILED")
                .message("Failed to process webhook event: " + result.getMessage())
                .build());
        }

        return Mono.just(ProviderWebhookResponse.builder()
            .code("PROCESSED")
            .message("Accepted")
            .build());
    }

    private String resolveExternalEventId(final ProviderWebhookRequest providerWebhookRequest) {

        if (providerWebhookRequest == null) {
            return UUID.randomUUID().toString();
        }

        if(CommonStringUtils.isEmpty(providerWebhookRequest.getEventId())) {
            return UUID.randomUUID().toString();
        }

        return providerWebhookRequest.getEventId();
    }

    private String resolveIdempotencyKey(String adapterProviderCode, String externalEventId) {

        if (CommonStringUtils.isNotEmpty(externalEventId)) {
            return adapterProviderCode + "_" + externalEventId;
        }

        return UUID.randomUUID().toString();
    }
}

