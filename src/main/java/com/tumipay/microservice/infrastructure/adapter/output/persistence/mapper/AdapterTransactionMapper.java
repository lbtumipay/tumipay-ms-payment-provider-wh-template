package com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper;

import com.tumipay.microservice.domain.model.transaction.StandardTransactionResult;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderIntegrationLogEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderTransactionEntity;
import com.tumipay.microservice.shared.mapper.ICommonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * AdapterTransactionMapper
 * <p>
 * AdapterTransactionMapper interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 31/03/2026
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdapterTransactionMapper extends ICommonMapper {

    @Mapping(target = "adapterTransactionId", source = "providerTransactionEntity.uuid")
    @Mapping(target = "transactionId", source = "providerTransactionEntity.transactionId")
    @Mapping(target = "referenceId", source = "providerTransactionEntity.referenceId")
    @Mapping(target = "idempotencyKey", source = "providerTransactionEntity.idempotencyKey")
    @Mapping(target = "providerTransactionId", source = "providerTransactionEntity.providerTransactionId")
    @Mapping(target = "providerReferenceId", source = "providerTransactionEntity.providerReferenceId")
    @Mapping(target = "adapterProviderCode", source = "providerTransactionEntity.adapterProviderCode")
    @Mapping(target = "transactionType", source = "providerTransactionEntity.transactionType")
    @Mapping(target = "paymentMethod", source = "providerTransactionEntity.paymentMethod")
    @Mapping(target = "providerEndpoint", source = "providerIntegrationLogEntity.providerEndpoint")
    @Mapping(target = "httpMethod", source = "providerIntegrationLogEntity.httpMethod")
    @Mapping(target = "providerRequest", source = "providerIntegrationLogEntity.requestPayload", qualifiedByName = "mapMetadata")
    @Mapping(target = "providerResponse", source = "providerIntegrationLogEntity.responsePayload", qualifiedByName = "mapMetadata")
    @Mapping(target = "httpStatusCode", source = "providerIntegrationLogEntity.httpStatusCode")
    @Mapping(target = "success", source = "providerIntegrationLogEntity.success")
    @Mapping(target = "errorCode", source = "providerTransactionEntity.errorCode")
    @Mapping(target = "errorMessage", source = "providerTransactionEntity.errorMessage")
    @Mapping(target = "metadata", source = "providerTransactionEntity.metadata", qualifiedByName = "mapMetadata")
    @Mapping(target = "createdAt", source = "providerTransactionEntity.createdAt")
    @Mapping(target = "updatedAt", source = "providerTransactionEntity.updatedAt")
    @Mapping(target = "processedAt", source = "providerTransactionEntity.providerProcessedAt")
    StandardTransactionResult toDomain(ProviderIntegrationLogEntity providerIntegrationLogEntity, ProviderTransactionEntity providerTransactionEntity);
}