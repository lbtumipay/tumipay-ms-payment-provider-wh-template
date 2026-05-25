package com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import com.tumipay.microservice.shared.util.CommonUuidUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

/**
 * IProviderWebhookEventPersistenceMapper
 * <p>
 * Mapper for webhook event persistence conversion.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IProviderWebhookEventPersistenceMapper {

    @Mapping(target = "uuid", expression = "java(toUuid(domain.getUuid()))")
    @Mapping(target = "processingStatus", expression = "java(toProcessingStatus(domain.getProcessingStatus()))")
    ProviderWebhookEventEntity toEntity(WebhookEvent domain);

    @Mapping(target = "uuid", expression = "java(toStringUuid(entity.getUuid()))")
    @Mapping(target = "processingStatus", expression = "java(toProcessingStatusEnum(entity.getProcessingStatus()))")
    WebhookEvent toDomain(ProviderWebhookEventEntity entity);

    default UUID toUuid(String value) {
        if (!CommonUuidUtils.isValidId(value)) {
            return null;
        }
        return UUID.fromString(value);
    }

    default String toStringUuid(UUID value) {
        return value != null ? value.toString() : null;
    }

    default String toProcessingStatus(WebhookProcessingStatusEnum value) {
        return value != null ? value.name() : null;
    }

    default WebhookProcessingStatusEnum toProcessingStatusEnum(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return WebhookProcessingStatusEnum.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return WebhookProcessingStatusEnum.FAILED;
        }
    }
}

