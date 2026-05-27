package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.model.transaction.StandardTransactionResult;
import com.tumipay.microservice.domain.port.output.IAdapterTransactionRepositoryPort;
import com.tumipay.microservice.domain.port.output.IProviderTransactionRepositoryPort;
import com.tumipay.microservice.domain.service.contract.IProviderTransactionDomainService;
import com.tumipay.microservice.shared.dto.CommonValidationResult;
import com.tumipay.microservice.shared.dto.DomainOperationResult;
import com.tumipay.microservice.shared.enums.BaseOperationStatusEnum;
import com.tumipay.microservice.shared.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * IPaymentProviderDomainService
 * <p>
 * IPaymentProviderDomainService interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
@Log4j2
@Service
@AllArgsConstructor
public class ProviderTransactionDomainService implements IProviderTransactionDomainService {

    private final IProviderTransactionRepositoryPort providerTransactionRepositoryPort;
    private final IAdapterTransactionRepositoryPort adapterTransactionRepositoryPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<DomainOperationResult<ProviderTransaction>> getDomainEntityByUuId(String entityUuId) {

        if (CommonStringUtils.isBlank(entityUuId)) {
            return monoDomainFailure("entityUuId is required and cannot be empty");
        }

        if (!CommonUuidUtils.isValidId(entityUuId)) {
            return monoDomainFailure("entityUuId format is invalid");
        }

        return providerTransactionRepositoryPort.findByUuid(entityUuId)
            .flatMap(this::monoDomainSuccess)
            .switchIfEmpty(monoDomainFailure("ProviderTransaction not found for uuid=" + entityUuId))
            .onErrorResume(error -> {
                log.error("Error getting ProviderTransaction for uuid={}, error: {}", entityUuId, error.getMessage());
                return monoDomainFailure("Error getting ProviderTransaction: " + error.getMessage());
            })
            .transform(CommonLoggerUtils.withProcessLogging("getProviderTransactionByUuid"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<DomainOperationResult<ProviderTransaction>> saveDomainEntity(ProviderTransaction entity) {

        return validateCreate()
            .apply(entity)
            .flatMap(this::handleDomainValidationResult)
            .flatMap(validationResult -> providerTransactionRepositoryPort.save(entity))
            .flatMap(this::monoDomainSuccess)
            .onErrorResume(error -> {
                log.error("Error saving ProviderTransaction, error: {}", error.getMessage());
                return monoDomainFailure("Error saving ProviderTransaction: " + error.getMessage());
            })
            .transform(CommonLoggerUtils.withProcessLogging("saveProviderTransaction"));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<DomainOperationResult<ProviderTransaction>> updateDomainEntity(ProviderTransaction entity) {

        return validateUpdate()
            .apply(entity)
            .flatMap(this::handleDomainValidationResult)
            .flatMap(validationResult ->
                providerTransactionRepositoryPort.update(entity)
            )
            .flatMap(this::monoDomainSuccess)
            .onErrorResume(error -> {
                log.error("Error updating ProviderTransaction, error: {}", error.getMessage());
                return monoDomainFailure("Error updating ProviderTransaction: " + error.getMessage());
            })
            .transform(CommonLoggerUtils.withProcessLogging("updateProviderTransaction"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<DomainOperationResult<Void>> validateIdempotency(String idempotencyKey) {

        if (CommonStringUtils.isBlank(idempotencyKey)) {
            return Mono.just(DomainOperationResult.<Void>builder()
                .status(BaseOperationStatusEnum.FAILED)
                .errorMessage("idempotencyKey is required and cannot be empty")
                .build());
        }

        return providerTransactionRepositoryPort.findByIdempotencyKey(idempotencyKey)
            .flatMap(existingTransaction -> {
                log.warn("Duplicate idempotency detected for idempotencyKey={}", idempotencyKey);
                return Mono.just(DomainOperationResult.<Void>builder()
                    .status(BaseOperationStatusEnum.FAILED)
                    .errorMessage("Duplicate transaction detected for idempotency_key " + idempotencyKey)
                    .build());
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.debug("No duplicate found for idempotencyKey={}", idempotencyKey);
                return Mono.just(DomainOperationResult.<Void>builder()
                    .status(BaseOperationStatusEnum.SUCCESS)
                    .build());
            }))
            .onErrorResume(error -> {
                log.error("Error validating idempotency for idempotencyKey={}, error={}", idempotencyKey, error.getMessage(), error);
                return Mono.just(DomainOperationResult.<Void>builder()
                    .status(BaseOperationStatusEnum.FAILED)
                    .errorMessage("Error validating idempotency: " + error.getMessage())
                    .build());
            })
            .transform(CommonLoggerUtils.withProcessLogging("validateProviderTransactionIdempotency"));
    }

    @Override
    public Mono<StandardTransactionResult> findByTransactionId(String transactionId) {
        return adapterTransactionRepositoryPort.findByTransactionId(transactionId);
    }

    @Override
    public Mono<StandardTransactionResult> findByAdapterTransactionId(String adapterTransactionId) {
        return adapterTransactionRepositoryPort.findByAdapterTransactionId(adapterTransactionId);
    }

    @Override
    public Mono<StandardTransactionResult> findByProviderTransactionId(String providerTransactionId) {
        return adapterTransactionRepositoryPort.findByProviderTransactionId(providerTransactionId);
    }

    private Mono<CommonValidationResult> handleDomainValidationResult(CommonValidationResult result) {

        if (result.getStatus() == BaseOperationStatusEnum.FAILED) {
            log.error(CommonErrorUtils.toJson(result.getErrors()));
            return Mono.error(new IllegalArgumentException(result.getErrorMessage()));
        }

        return Mono.just(CommonValidationResult.builder()
            .status(BaseOperationStatusEnum.SUCCESS)
            .build()
        );
    }

    private Mono<DomainOperationResult<ProviderTransaction>> monoDomainSuccess(ProviderTransaction providerTransaction) {
        return Mono.just(DomainOperationResult.<ProviderTransaction>builder()
            .status(BaseOperationStatusEnum.SUCCESS)
            .entity(providerTransaction)
            .build()
        );
    }

    private Mono<DomainOperationResult<ProviderTransaction>> monoDomainFailure(String errorMessage) {
        return Mono.just(DomainOperationResult.<ProviderTransaction>builder()
            .status(BaseOperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .build()
        );
    }

    private Function<ProviderTransaction, Mono<CommonValidationResult>> validateCreate() {

        return transaction -> {

            final List<String> errors = new ArrayList<>();

            if (transaction == null) {
                errors.add("ProviderTransaction cannot be null");
                return Mono.just(buildFailure(errors));
            }

            CommonValidationUtils.validateText(transaction.getAdapterProviderCode(), "adapterProviderCode", errors);
            CommonValidationUtils.validateRequiredEnum(transaction.getTransactionType(), "transactionType", errors);
            CommonValidationUtils.validateText(transaction.getTransactionId(), "transactionId", errors);
            CommonValidationUtils.validateText(transaction.getReferenceId(), "transactionReferenceId", errors);
            CommonValidationUtils.validateText(transaction.getIdempotencyKey(), "idempotencyKey", errors);
            CommonValidationUtils.validateText(transaction.getCurrency(), "currency", errors);
            CommonValidationUtils.validateRequiredEnum(transaction.getPaymentMethod(), "paymentMethod", errors);
            CommonValidationUtils.validateRequiredEnum(transaction.getStatus(), "status", errors);

            if (transaction.getAmount() == null || transaction.getAmount().compareTo(0L) <= 0) {
                errors.add("The amount is required and must be greater than zero");
            }

            return Mono.just(
                errors.isEmpty()
                    ? CommonValidationResult.builder()
                      .status(BaseOperationStatusEnum.SUCCESS)
                      .build()
                    : buildFailure(errors)
            );
        };
    }

    private Function<ProviderTransaction, Mono<CommonValidationResult>> validateUpdate() {

        return transaction -> {

            final List<String> errors = new ArrayList<>();

            if (transaction == null) {
                errors.add("ProviderTransaction cannot be null");
                return Mono.just(buildFailure(errors));
            }

            CommonValidationUtils.validateText(transaction.getTransactionId(), "transactionId", errors);
            CommonValidationUtils.validateRequiredEnum(transaction.getStatus(), "status", errors);
            CommonValidationUtils.validateText(transaction.getProviderTransactionId(), "providerTransactionId", errors);

            return Mono.just(
                errors.isEmpty()
                    ? CommonValidationResult.builder()
                      .status(BaseOperationStatusEnum.SUCCESS)
                      .build()
                    : buildFailure(errors)
            );
        };
    }



    private CommonValidationResult buildFailure(List<String> errors) {
        return CommonValidationResult.builder()
            .status(BaseOperationStatusEnum.FAILED)
            .errorMessage("ProviderTransaction validation failed")
            .errors(errors)
            .build();
    }

    @Override
    public Mono<DomainOperationResult<ProviderTransaction>> getByProviderTransactionId(String providerTransactionId) {

        if (CommonStringUtils.isEmpty(providerTransactionId)) {
            return monoDomainFailure("providerTransactionId is required and cannot be empty");
        }

        return providerTransactionRepositoryPort.findByProviderTransactionId(providerTransactionId)
            .flatMap(this::monoDomainSuccess)
            .switchIfEmpty(monoDomainFailure(
                "ProviderTransaction not found for providerTransactionId=" + providerTransactionId
            ))
            .onErrorResume(error -> {
                log.error("Error getting ProviderTransaction for providerTransactionId={}, error: {}",
                    providerTransactionId, error.getMessage());
                return monoDomainFailure("Error getting ProviderTransaction: " + error.getMessage());
            })
            .transform(CommonLoggerUtils.withProcessLogging("getProviderTransactionByProviderTransactionId"));
    }
}
