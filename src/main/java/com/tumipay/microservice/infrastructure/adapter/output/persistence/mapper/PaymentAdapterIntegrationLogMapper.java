package com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper;

import com.tumipay.microservice.domain.model.provider.ProviderIntegrationLog;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderIntegrationLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import reactor.core.publisher.Mono;

/**
 * PaymentAdapterIntegrationLogMapper
 * <p>
 * Mapper interface for converting between PaymentProviderLog domain model
 * and PaymentAdapterIntegrationLogEntity.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentAdapterIntegrationLogMapper {

    /**
     * Converts a PaymentProviderLog domain model to a PaymentAdapterIntegrationLogEntity.
     *
     * @param domain the domain model
     * @return the entity
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "referenceId", source = "referenceId")
    @Mapping(target = "providerTransactionId", source = "providerTransactionId")
    @Mapping(target = "providerReferenceId", source = "providerReferenceId")
    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    @Mapping(target = "providerLatencyMs", source = "providerLatencyMs")
    @Mapping(target = "adapterProviderCode", source = "adapterProviderCode")
    @Mapping(target = "transactionType", source = "transactionType")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "providerEndpoint", source = "providerEndpoint")
    @Mapping(target = "httpMethod", source = "httpMethod")
    @Mapping(target = "requestPayload", source = "requestPayload")
    @Mapping(target = "responsePayload", source = "responsePayload")
    @Mapping(target = "httpStatusCode", source = "httpStatusCode")
    @Mapping(target = "success", source = "success")
    @Mapping(target = "errorCode", source = "errorCode")
    @Mapping(target = "errorMessage", source = "errorMessage")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ProviderIntegrationLogEntity toEntity(ProviderIntegrationLog domain);


    /**
     * Converts a PaymentAdapterIntegrationLogEntity to a PaymentProviderLog domain model.
     *
     * @param entity the entity
     * @return the domain model
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "referenceId", source = "referenceId")
    @Mapping(target = "providerTransactionId", source = "providerTransactionId")
    @Mapping(target = "providerReferenceId", source = "providerReferenceId")
    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    @Mapping(target = "providerLatencyMs", source = "providerLatencyMs")
    @Mapping(target = "adapterProviderCode", source = "adapterProviderCode")
    @Mapping(target = "transactionType", source = "transactionType")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "providerEndpoint", source = "providerEndpoint")
    @Mapping(target = "httpMethod", source = "httpMethod")
    @Mapping(target = "requestPayload", source = "requestPayload")
    @Mapping(target = "responsePayload", source = "responsePayload")
    @Mapping(target = "httpStatusCode", source = "httpStatusCode")
    @Mapping(target = "success", source = "success")
    @Mapping(target = "errorCode", source = "errorCode")
    @Mapping(target = "errorMessage", source = "errorMessage")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ProviderIntegrationLog toDomain(ProviderIntegrationLogEntity entity);

    default Mono<ProviderIntegrationLog> doToDomain(ProviderIntegrationLogEntity entity){
        return Mono.just(entity)
                .map(this::toDomain);
    }
}