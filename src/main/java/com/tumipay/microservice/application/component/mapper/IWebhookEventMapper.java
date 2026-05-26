package com.tumipay.microservice.application.component.mapper;

import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * IWebhookEventMapper
 * <p>
 * MapStruct mapper for {@link WebhookEvent} domain model transitions.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 18/04/2026
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IWebhookEventMapper extends IBaseApplicationMapper {

    /**
     * Maps an existing {@link WebhookEvent} to a new instance with PENDING status,
     * setting the classified event type, nextRetryAt and updatedAt to {@code Instant.now()}.
     *
     * @param event          the original RECEIVED webhook event.
     * @param classifiedType the {@link WebhookEventTypeEnum} determined by the classifier.
     * @return a new {@link WebhookEvent} ready to be persisted as PENDING.
     */
    @Mapping(target = "id",                 source = "event.id")
    @Mapping(target = "uuid",               source = "event.uuid")
    @Mapping(target = "adapterProviderCode",source = "event.adapterProviderCode")
    @Mapping(target = "eventType",          expression = "java(resolveEventType(classifiedType))")
    @Mapping(target = "transactionId",     source = "event.transactionId")
    @Mapping(target = "referenceId",       source = "event.referenceId")
    @Mapping(target = "providerTransactionId", source = "event.providerTransactionId")
    @Mapping(target = "externalEventId",    source = "event.externalEventId")
    @Mapping(target = "idempotencyKey",     source = "event.idempotencyKey")
    @Mapping(target = "processingStatus",   expression = "java(com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum.PENDING)")
    @Mapping(target = "errorCode",          source = "event.errorCode")
    @Mapping(target = "retryCount",         expression = "java(resolveRetryCount(event))")
    @Mapping(target = "lastError",          source = "event.lastError")
    @Mapping(target = "eventRequest",       source = "event.eventRequest")
    @Mapping(target = "receivedAt",         source = "event.receivedAt")
    @Mapping(target = "createdAt",          source = "event.createdAt")
    @Mapping(target = "processedAt",        source = "event.processedAt")
    @Mapping(target = "claimedBy",          source = "event.claimedBy")
    @Mapping(target = "claimedAt",          source = "event.claimedAt")
    @Mapping(target = "nextRetryAt",        expression = "java(createInstant(\"nextRetryAt\"))")
    @Mapping(target = "updatedAt",          expression = "java(createInstant(\"updatedAt\"))")
    WebhookEvent mapToPending(WebhookEvent event, WebhookEventTypeEnum classifiedType);

    default String resolveEventType(WebhookEventTypeEnum classifiedType) {
        return classifiedType != null
            ? classifiedType.name()
            : WebhookEventTypeEnum.UNKNOWN_EVENT.name();
    }

    default Integer resolveRetryCount(WebhookEvent event) {
        return event != null && event.getRetryCount() != null
            ? event.getRetryCount()
            : 0;
    }
}
