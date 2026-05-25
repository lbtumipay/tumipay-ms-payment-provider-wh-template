package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.OperationStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.output.IProviderWebhookEventRepositoryPort;
import com.tumipay.microservice.domain.port.output.IWebhookWorkerRepositoryPort;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventDomainService;
import com.tumipay.microservice.shared.dto.CommonValidationResult;
import com.tumipay.microservice.shared.dto.DomainOperationResult;
import com.tumipay.microservice.shared.enums.BaseOperationStatusEnum;
import com.tumipay.microservice.shared.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * ProviderWebhookEventDomainService
 * <p>
 * Domain service for webhook event persistence and validation.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Log4j2
@Service
@AllArgsConstructor
public class ProviderWebhookEventDomainService implements IProviderWebhookEventDomainService {

    private final IProviderWebhookEventRepositoryPort providerWebhookEventRepositoryPort;
    private final IWebhookWorkerRepositoryPort webhookWorkerRepositoryPort;

    @Override
    public Mono<DomainOperationResult<WebhookEvent>> getDomainEntityByUuId(String entityUuId) {

        if (CommonStringUtils.isEmpty(entityUuId)) {
            return monoDomainFailure("entityUuId is required and cannot be empty");
        }

        if (!CommonUuidUtils.isValidId(entityUuId)) {
            return monoDomainFailure("entityUuId format is invalid");
        }

        return providerWebhookEventRepositoryPort.findByUuid(UUID.fromString(entityUuId))
            .flatMap(this::monoDomainSuccess)
            .switchIfEmpty(monoDomainFailure("ProviderWebhookEvent not found for uuid=" + entityUuId))
            .onErrorResume(error -> {
                log.error("Error getting ProviderWebhookEvent for uuid={}, error: {}", entityUuId, error.getMessage());
                return monoDomainFailure("Error getting ProviderWebhookEvent: " + error.getMessage());
            })
            .transform(CommonLoggerUtils.withProcessLogging("getProviderWebhookEventByUuid"));
    }

    @Override
    public Mono<DomainOperationResult<WebhookEvent>> saveDomainEntity(WebhookEvent entity) {

        return validateCreate()
            .apply(entity)
            .flatMap(this::handleDomainValidationResult)
            .flatMap(validationResult -> providerWebhookEventRepositoryPort.save(entity))
            .flatMap(this::monoDomainSuccess)
            .onErrorResume(error -> {
                log.error("Error saving ProviderWebhookEvent, error: {}", error.getMessage());
                return monoDomainFailure("Error saving ProviderWebhookEvent: " + error.getMessage());
            })
            .transform(CommonLoggerUtils.withProcessLogging("saveProviderWebhookEvent"));
    }

    @Override
    public Mono<DomainOperationResult<WebhookEvent>> updateDomainEntity(WebhookEvent entity) {

        return validateUpdate()
            .apply(entity)
            .flatMap(this::handleDomainValidationResult)
            .flatMap(validationResult -> providerWebhookEventRepositoryPort.update(entity))
            .flatMap(this::monoDomainSuccess)
            .onErrorResume(error -> {
                log.error("Error updating ProviderWebhookEvent, error: {}", error.getMessage());
                return monoDomainFailure("Error updating ProviderWebhookEvent: " + error.getMessage());
            })
            .transform(CommonLoggerUtils.withProcessLogging("updateProviderWebhookEvent"));
    }

    @Override
    public Mono<DomainOperationResult<Void>> validateIdempotency(String idempotencyKey) {

        if (CommonStringUtils.isBlank(idempotencyKey)) {
            return Mono.just(DomainOperationResult.<Void>builder()
                .status(OperationStatusEnum.FAILED)
                .errorMessage("idempotencyKey is required and cannot be empty")
                .build());
        }

        return providerWebhookEventRepositoryPort.findByIdempotencyKey(idempotencyKey)
            .flatMap(existingWebhookEvent -> {
                log.warn("Duplicate webhook idempotency detected for idempotencyKey={}", idempotencyKey);
                return Mono.just(DomainOperationResult.<Void>builder()
                    .status(OperationStatusEnum.FAILED)
                    .errorMessage("Duplicate webhook event detected for idempotency_key " + idempotencyKey)
                    .build());
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.debug("No duplicate webhook event found for idempotencyKey={}", idempotencyKey);
                return Mono.just(DomainOperationResult.<Void>builder()
                    .status(OperationStatusEnum.SUCCESS)
                    .build());
            }))
            .onErrorResume(error -> {
                log.error(
                    "Error validating webhook idempotency for idempotencyKey={}, error={}",
                    idempotencyKey,
                    error.getMessage()
                );
                return Mono.just(DomainOperationResult.<Void>builder()
                    .status(OperationStatusEnum.FAILED)
                    .errorMessage("Error validating idempotency: " + error.getMessage())
                    .build());
            })
            .transform(CommonLoggerUtils.withProcessLogging("validateProviderWebhookEventIdempotency"));
    }

    @Override
    public Flux<WebhookEvent> claimBatch(String workerId, int batchSize) {

        if (CommonStringUtils.isBlank(workerId)) {
            return Flux.error(new IllegalArgumentException("workerId is required and cannot be empty"));
        }

        if (batchSize <= 0) {
            return Flux.error(new IllegalArgumentException("batchSize must be greater than zero"));
        }

        return webhookWorkerRepositoryPort.claimBatch(workerId, batchSize);
    }

    @Override
    public Mono<WebhookEvent> markAsProcessed(Long id) {

        if (id == null || id <= 0) {
            return Mono.error(new IllegalArgumentException("id is required and must be greater than zero"));
        }

        return webhookWorkerRepositoryPort.markAsProcessed(id)
            .transform(CommonLoggerUtils.withProcessLogging("markWebhookEventAsProcessed"));
    }

    @Override
    public Mono<WebhookEvent> markForRetry(Long id, String errorCode, String lastError, int maxRetryCount) {

        if (id == null || id <= 0) {
            return Mono.error(new IllegalArgumentException("id is required and must be greater than zero"));
        }

        if (CommonStringUtils.isEmpty(errorCode)) {
            return Mono.error(new IllegalArgumentException("errorCode is required and cannot be empty"));
        }

        if (maxRetryCount < 0) {
            return Mono.error(new IllegalArgumentException("maxRetryCount cannot be negative"));
        }

        return webhookWorkerRepositoryPort.markForRetry(id, errorCode, lastError, maxRetryCount)
            .transform(CommonLoggerUtils.withProcessLogging("markWebhookEventForRetry"));
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

    private Mono<DomainOperationResult<WebhookEvent>> monoDomainSuccess(WebhookEvent webhookEvent) {
        return Mono.just(DomainOperationResult.<WebhookEvent>builder()
            .status(OperationStatusEnum.SUCCESS)
            .entity(webhookEvent)
            .build()
        );
    }

    private Mono<DomainOperationResult<WebhookEvent>> monoDomainFailure(String errorMessage) {
        return Mono.just(DomainOperationResult.<WebhookEvent>builder()
            .status(OperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .build()
        );
    }

    private Function<WebhookEvent, Mono<CommonValidationResult>> validateCreate() {

        return webhookEvent -> {

            final List<String> errors = new ArrayList<>();

            if (webhookEvent == null) {
                errors.add("ProviderWebhookEvent cannot be null");
                return Mono.just(buildFailure(errors));
            }

            CommonValidationUtils.validateText(webhookEvent.getAdapterProviderCode(), "adapterProviderCode", errors);
            CommonValidationUtils.validateText(webhookEvent.getEventType(), "eventType", errors);
            CommonValidationUtils.validateRequiredEnum(webhookEvent.getProcessingStatus(), "processingStatus", errors);
            CommonValidationUtils.validateText(webhookEvent.getEventRequest(), "eventRequest", errors);

            return Mono.just(
                errors.isEmpty()
                    ? CommonValidationResult.builder()
                      .status(BaseOperationStatusEnum.SUCCESS)
                      .build()
                    : buildFailure(errors)
            );
        };
    }

    private Function<WebhookEvent, Mono<CommonValidationResult>> validateUpdate() {

        return webhookEvent -> {

            final List<String> errors = new ArrayList<>();

            if (webhookEvent == null) {
                errors.add("ProviderWebhookEvent cannot be null");
                return Mono.just(buildFailure(errors));
            }

            CommonValidationUtils.validateUuidText(webhookEvent.getUuid(), "uuid", errors);
            CommonValidationUtils.validateRequiredEnum(webhookEvent.getProcessingStatus(), "processingStatus", errors);

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
            .errorMessage("ProviderWebhookEvent validation failed")
            .errors(errors)
            .build();
    }

    @Override
    public Flux<WebhookEvent> findReceivedBatch(int batchSize) {

        if (batchSize <= 0) {
            return Flux.error(new IllegalArgumentException("batchSize must be greater than zero"));
        }

        return webhookWorkerRepositoryPort.findReceivedBatch(batchSize);
    }
}

