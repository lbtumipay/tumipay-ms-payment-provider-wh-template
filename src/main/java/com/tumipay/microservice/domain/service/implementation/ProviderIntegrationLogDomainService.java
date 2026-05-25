package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.model.provider.ProviderIntegrationLog;
import com.tumipay.microservice.domain.port.output.IProviderIntegrationLogRepositoryPort;
import com.tumipay.microservice.domain.service.contract.IProviderIntegrationLogDomainService;
import com.tumipay.microservice.shared.dto.CommonValidationResult;
import com.tumipay.microservice.domain.component.enums.OperationStatusEnum;
import com.tumipay.microservice.shared.dto.DomainOperationResult;
import com.tumipay.microservice.shared.enums.BaseOperationStatusEnum;
import com.tumipay.microservice.shared.util.CommonErrorUtils;
import com.tumipay.microservice.shared.util.CommonLoggerUtils;
import com.tumipay.microservice.shared.util.CommonUuidUtils;
import com.tumipay.microservice.shared.util.CommonValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * ProviderIntegrationLogDomainService
 * <p>
 * ProviderIntegrationLogDomainService class.
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
public class ProviderIntegrationLogDomainService implements IProviderIntegrationLogDomainService {

    private final IProviderIntegrationLogRepositoryPort providerIntegrationLogRepositoryPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<DomainOperationResult<ProviderIntegrationLog>> getDomainEntityByUuId(String entityUuId) {

        if (!StringUtils.hasText(entityUuId)) {
            return monoDomainFailure("entityUuId is required and cannot be empty");
        }

        if (!CommonUuidUtils.isValidId(entityUuId)) {
            return monoDomainFailure("entityUuId format is invalid");
        }

        return providerIntegrationLogRepositoryPort.findByUuid(entityUuId)
            .flatMap(this::monoDomainSuccess)
            .switchIfEmpty(monoDomainFailure("ProviderIntegrationLog not found for uuid=" + entityUuId))
            .onErrorResume(error -> {
                log.error("Error getting ProviderIntegrationLog for uuid={}, error: {}", entityUuId, error.getMessage());
                return monoDomainFailure("Error getting ProviderIntegrationLog: " + error.getMessage());
            })
            .transform(CommonLoggerUtils.withProcessLogging("getProviderIntegrationLogByUuid"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<DomainOperationResult<ProviderIntegrationLog>> saveDomainEntity(ProviderIntegrationLog entity) {

        return validateCreate()
            .apply(entity)
            .flatMap(this::handleDomainValidationResult)
            .flatMap(validateEntity ->
                providerIntegrationLogRepositoryPort.save(entity)
            )
            .flatMap(this::monoDomainSuccess)
            .transform(CommonLoggerUtils.withProcessLogging("saveProviderIntegrationLog"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<DomainOperationResult<ProviderIntegrationLog>> updateDomainEntity(ProviderIntegrationLog entity) {

        return validateUpdate()
            .apply(entity)
            .flatMap(this::handleDomainValidationResult)
            .flatMap(validateEntity ->
                providerIntegrationLogRepositoryPort.update(entity)
            )
            .flatMap(this::monoDomainSuccess)
            .transform(CommonLoggerUtils.withProcessLogging("updateProviderIntegrationLog"));
    }

    private Mono<CommonValidationResult> handleDomainValidationResult(CommonValidationResult result) {

        if (result.getStatus() == BaseOperationStatusEnum.FAILED) {
            log.error("Error in updateDomainEntity validation, validations errors {},", CommonErrorUtils.toJson(result.getErrors()));
            return Mono.error(new IllegalArgumentException(result.getErrorMessage()));
        }

        return Mono.just(CommonValidationResult.builder()
            .status(BaseOperationStatusEnum.SUCCESS)
            .build()
        );
    }

    private Mono<DomainOperationResult<ProviderIntegrationLog>> monoDomainSuccess(ProviderIntegrationLog providerIntegrationLog) {
        return Mono.just(DomainOperationResult.<ProviderIntegrationLog>builder()
            .status(OperationStatusEnum.SUCCESS)
            .entity(providerIntegrationLog)
            .build()
        );
    }

    private Mono<DomainOperationResult<ProviderIntegrationLog>> monoDomainFailure(String errorMessage) {
        return Mono.just(DomainOperationResult.<ProviderIntegrationLog>builder()
            .status(OperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .build()
        );
    }

    private Function<ProviderIntegrationLog, Mono<CommonValidationResult>> validateCreate() {

        return log -> {

            final List<String> errors = new ArrayList<>();

            if (log == null) {
                errors.add("ProviderIntegrationLog cannot be null");
                return Mono.just(buildFailure(errors));
            }

            CommonValidationUtils.validateText(log.getAdapterProviderCode(), "adapter_provider_code", errors);
            CommonValidationUtils.validateText(log.getTransactionType().toString(), "transaction_type", errors);
            CommonValidationUtils.validateText(log.getTransactionId(), "transaction_id", errors);
            CommonValidationUtils.validateText(log.getReferenceId(), "reference_id", errors);
            CommonValidationUtils.validateText(log.getIdempotencyKey(), "idempotency_key", errors);

            return Mono.just(
                errors.isEmpty()
                    ? CommonValidationResult.builder()
                      .status(BaseOperationStatusEnum.SUCCESS)
                      .build()
                    : buildFailure(errors)
            );
        };
    }

    private Function<ProviderIntegrationLog, Mono<CommonValidationResult>> validateUpdate() {

        return log -> {

            final List<String> errors = new ArrayList<>();

            if (log == null) {
                errors.add("ProviderIntegrationLog cannot be null");
                return Mono.just(buildFailure(errors));
            }

            if (log.getRequestPayload() == null) {
                errors.add("The requestPayload is required and cannot be null");
            }

            if (log.getResponsePayload() == null) {
                errors.add("The requestPayload is required and cannot be null");
            }

            CommonValidationUtils.validateText(log.getHttpMethod(), "httpMethod", errors);
            CommonValidationUtils.validateText(log.getProviderEndpoint(), "providerEndpoint", errors);

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
            .errorMessage("ProviderIntegrationLog validation failed")
            .errors(errors)
            .build();
    }
}