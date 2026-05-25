package com.tumipay.microservice.infrastructure.adapter.output.http.standard.mapper;

import com.tumipay.microservice.domain.model.gateway.GatewayWebhookResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.request.GatewayWebhookRequest;
import com.tumipay.microservice.infrastructure.adapter.output.http.standard.response.GatewayWebhookResponse;
import com.tumipay.microservice.shared.util.CommonJsonUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

/**
 * IGatewayWebhookMapper
 *
 * MapStruct mapper for converting webhook domain models to HTTP request DTOs.
 * Handles transformation of normalized WebhookEvent entities to GatewayWebhookRequest
 * payloads for dispatching to the TumiPay Payment Gateway.
 *
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 25/05/2026
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IGatewayWebhookMapper {

    /**
     * Converts a WebhookEvent to a GatewayWebhookRequest.
     *
     * Maps all required fields from the domain model to the HTTP request DTO,
     * including special handling for the eventRequest field which requires
     * JSON deserialization from String to Object.
     *
     * @param webhookEvent the WebhookEvent to convert
     * @return the converted GatewayWebhookRequest
     */
    @Mapping(source = "uuid", target = "eventId")
    @Mapping(source = "eventRequest", target = "eventRequest", qualifiedByName = "deserializeEventRequest")
    GatewayWebhookRequest buildGatewayWebhookRequest(WebhookEvent webhookEvent);

    /**
     * Converts GatewayWebhookResponse (HTTP DTO) to GatewayWebhookResult (domain contract).
     * All field names match between source and target, including the nested data object.
     *
     * @param response the HTTP response DTO from the Gateway
     * @return the domain result model, or null if response is null
     */
    GatewayWebhookResult convertToResult(GatewayWebhookResponse response);

    /**
     * Builds a duplicate webhook response result.
     *
     * Used when the Gateway returns HTTP 409 Conflict status code,
     * indicating that the event was already acknowledged.
     * Returns a standardized GatewayWebhookResult with duplicate event metadata.
     *
     * @return a GatewayWebhookResult indicating a duplicate event
     */
    default GatewayWebhookResult buildDuplicateResponse() {
        return GatewayWebhookResult.builder()
            .code("DUPLICATE_EVENT")
            .status("FAILED")
            .message("Duplicate event — already acknowledged by Gateway")
            .build();
    }

    /**
     * Deserializes the eventRequest JSON string to an Object.
     *
     * Handles null/blank strings and JSON parsing errors gracefully.
     * If deserialization fails, returns the raw string as fallback.
     *
     * @param eventRequestJson the raw JSON string from the webhook event
     * @return the deserialized Object, or the raw string if deserialization fails, or null if blank
     */
    @Named("deserializeEventRequest")
    static Object deserializeEventRequest(String eventRequestJson) {
        if (eventRequestJson == null || eventRequestJson.isBlank()) {
            return null;
        }

        try {
            return CommonJsonUtils.fromJson(eventRequestJson, Object.class);
        } catch (Exception ex) {
            return eventRequestJson;
        }
    }
}
