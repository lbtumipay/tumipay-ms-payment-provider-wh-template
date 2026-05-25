package com.tumipay.microservice.application.component.mapper;

import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * IProviderTransactionMapper
 * <p>
 * IProviderTransactionMapper class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IProviderTransactionMapper extends IBaseApplicationMapper {

    @Mapping(target = "id",                   source = "existing.id")
    @Mapping(target = "uuid",                 source = "existing.uuid")
    @Mapping(target = "transactionId",        source = "existing.transactionId")
    @Mapping(target = "referenceId",          source = "existing.referenceId")
    @Mapping(target = "adapterProviderCode",  source = "existing.adapterProviderCode")
    @Mapping(target = "providerTransactionId",source = "existing.providerTransactionId")
    @Mapping(target = "providerReferenceId",  source = "existing.providerReferenceId")
    @Mapping(target = "idempotencyKey",       source = "existing.idempotencyKey")
    @Mapping(target = "amount",               source = "existing.amount")
    @Mapping(target = "currency",             source = "existing.currency")
    @Mapping(target = "transactionType",      source = "existing.transactionType")
    @Mapping(target = "status",               source = "newStatus")
    @Mapping(target = "paymentMethod",        source = "existing.paymentMethod")
    @Mapping(target = "errorCode",            source = "existing.errorCode")
    @Mapping(target = "errorMessage",         source = "existing.errorMessage")
    @Mapping(target = "metadata",             source = "existing.metadata")
    @Mapping(target = "createdAt",            source = "existing.createdAt")
    @Mapping(target = "providerProcessedAt",  expression = "java(createInstant(\"providerProcessedAt\"))")
    @Mapping(target = "updatedAt",            expression = "java(createInstant(\"updatedAt\"))")
    ProviderTransaction mapUpdateFromWebhookStatus(ProviderTransaction existing, TransactionStatusEnum newStatus);
}