package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.output.IWebhookWorkerRepositoryPort;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.IProviderWebhookEventPersistenceMapper;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderWebhookEventR2dbcRepository;
import com.tumipay.microservice.shared.properties.WebhookDispatcherProperties;
import com.tumipay.microservice.shared.util.CommonInstantUtils;
import com.tumipay.microservice.shared.util.CommonIntegerUtils;
import com.tumipay.microservice.shared.util.CommonStringUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * WebhookWorkerRepositoryAdapter
 * <p>
 * Infrastructure adapter for the Webhook Worker Claim-Batch pattern.
 * Uses native SQL with {@code FOR UPDATE SKIP LOCKED} and {@code RETURNING *}
 * via {@link DatabaseClient} (Spring Data R2DBC does not support these constructs
 * in derived query methods).
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 10/04/2026
 */
@SuppressFBWarnings(
    value = {
        "VA_FORMAT_STRING_USES_NEWLINE"
    },
    justification = "[VA_FORMAT_STRING_USES_NEWLINE] False positive: SQL defined using Java Text Blocks (\"\"\") does not explicitly use \\n. " +
        "Line breaks are part of the text block syntax, and using %n is not applicable in SQL statements. " +
        "Replacing with %n would break SQL readability and is not required."
)
@Log4j2
@Component
@RequiredArgsConstructor
public class WebhookWorkerRepositoryAdapter implements IWebhookWorkerRepositoryPort {

    private final DatabaseClient databaseClient;
    private final IProviderWebhookEventPersistenceMapper webhookEventPersistenceMapper;
    private final WebhookDispatcherProperties webhookDispatcherProperties;
    private final IProviderWebhookEventR2dbcRepository providerWebhookEventR2dbcRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<WebhookEvent> claimBatch(String workerId, int batchSize) {

        final int effectiveBatchSize = batchSize > 0 ? batchSize : 1;

        final int workerTimeoutMinutes = CommonIntegerUtils.defaultIfNull(
            webhookDispatcherProperties.getWorkerTimeoutMinutes(), 5
        );

        final String sql = """
            WITH candidates AS (
                SELECT pwe_id
                FROM tp_provider_webhook_event
                WHERE (
                    pwe_processing_status = 'PENDING'
                    OR (
                        pwe_processing_status = 'PROCESSING'
                        AND (
                            pwe_claimed_at IS NULL
                            OR pwe_claimed_at < now() - INTERVAL '%d minutes'
                        )
                    )
                )
                  AND COALESCE(pwe_next_retry_at, pwe_created_at) <= now()
                ORDER BY pwe_created_at
                FOR UPDATE SKIP LOCKED
                LIMIT CAST(:batchSize AS INTEGER)
            )
            UPDATE tp_provider_webhook_event t
            SET pwe_processing_status = 'PROCESSING',
                pwe_claimed_by        = :workerId,
                pwe_claimed_at        = now(),
                pwe_updated_at        = now()
            FROM candidates c
            WHERE t.pwe_id = c.pwe_id
            RETURNING t.*
            """.formatted(workerTimeoutMinutes);

        return databaseClient.sql(sql)
            .bind("workerId", workerId)
            .bind("batchSize", effectiveBatchSize)
            .fetch()
            .all()
            .map(this::mapRowToEntity)
            .map(webhookEventPersistenceMapper::toDomain)
            .doOnSubscribe(sub ->
                log.debug("ClaimBatch started | workerId={} | batchSize={} | timeout={}min",
                    workerId,
                    effectiveBatchSize,
                    workerTimeoutMinutes
                ))
            .doOnNext(event ->
                log.trace("Claimed webhook event | id={} | workerId={}",
                    event.getId(),
                    workerId
                ))
            .doOnError(error ->
                log.error("Error claiming webhook batch for worker={}: {}",
                    workerId,
                    error.getMessage()
                )
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<WebhookEvent> markAsProcessed(Long id) {

        return providerWebhookEventR2dbcRepository.findById(id)
            .flatMap(entity -> {
                entity.setProcessingStatus(WebhookProcessingStatusEnum.PROCESSED.toString());
                entity.setProcessedAt(Instant.now());
                entity.setUpdatedAt(Instant.now());
                return providerWebhookEventR2dbcRepository.save(entity);
            })
            .map(webhookEventPersistenceMapper::toDomain)
            .doOnError(error ->
                log.error("Error marking webhook event as PROCESSED id={}: {}",
                    id,
                    error.getMessage()
                )
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<WebhookEvent> markForRetry(Long id, String errorCode, String lastError, int maxRetryCount) {

        final String finalErrorCode = CommonStringUtils.defaultIfNull(errorCode, "UNKNOWN_ERROR");
        final String finalLastError = CommonStringUtils.truncate(lastError, 500, "Unknown error");

        return providerWebhookEventR2dbcRepository.findById(id)
            .flatMap(entity -> {
                final boolean exhausted = entity.getRetryCount() != null
                    && entity.getRetryCount() >= maxRetryCount;

                entity.setProcessingStatus(exhausted
                    ? WebhookProcessingStatusEnum.FAILED.toString()
                    : WebhookProcessingStatusEnum.PENDING.toString()
                );
                entity.setRetryCount(CommonIntegerUtils.defaultIfNull(entity.getRetryCount(), 0) + 1);
                entity.setNextRetryAt(exhausted
                    ? entity.getNextRetryAt()
                    : Instant.now().plusSeconds(30)
                );
                entity.setErrorCode(finalErrorCode);
                entity.setLastError(finalLastError);
                entity.setClaimedBy(null);
                entity.setClaimedAt(null);
                entity.setUpdatedAt(Instant.now());
                return providerWebhookEventR2dbcRepository.save(entity);
            })
            .map(webhookEventPersistenceMapper::toDomain)
            .doOnError(error ->
                log.error("Error marking webhook event for retry id={}: {}",
                    id,
                    error.getMessage()
                )
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<WebhookEvent> markAsFailed(Long id, String errorCode, String lastError) {

        final String errorMessage = CommonStringUtils.truncate(lastError, 500, "Fatal error");
        final String finalErrorCode = CommonStringUtils.defaultIfNull(errorCode, "FATAL_ERROR");

        return providerWebhookEventR2dbcRepository.findById(id)
            .flatMap(entity -> {
                entity.setProcessingStatus(WebhookProcessingStatusEnum.FAILED.toString());
                entity.setErrorCode(finalErrorCode);
                entity.setLastError(errorMessage);
                entity.setClaimedBy(null);
                entity.setClaimedAt(null);
                entity.setUpdatedAt(Instant.now());
                return providerWebhookEventR2dbcRepository.save(entity);
            })
            .map(webhookEventPersistenceMapper::toDomain)
            .doOnError(error ->
                log.error("Error marking webhook event as FAILED id={}: {}",
                    id,
                    error.getMessage()
                )
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<WebhookEvent> findReceivedBatch(int batchSize) {

        final int effectiveBatchSize = batchSize > 0 ? batchSize : 1;

        final int validatingTimeoutMinutes = CommonIntegerUtils.defaultIfNull(
            webhookDispatcherProperties.getValidatingTimeoutMinutes(), 10
        );

        final String sql = """
            WITH candidates AS (
                SELECT pwe_id
                FROM tp_provider_webhook_event
                WHERE (
                    pwe_processing_status = 'RECEIVED'
                    OR (
                        pwe_processing_status = 'VALIDATING'
                        AND (
                            pwe_updated_at IS NULL
                            OR pwe_updated_at < now() - INTERVAL '%d minutes'
                        )
                    )
                )
                ORDER BY pwe_received_at ASC
                FOR UPDATE SKIP LOCKED
                LIMIT CAST(:batchSize AS INTEGER)
            )
            UPDATE tp_provider_webhook_event t
            SET pwe_processing_status = 'VALIDATING',
                pwe_updated_at        = now()
            FROM candidates c
            WHERE t.pwe_id = c.pwe_id
            RETURNING t.*
            """.formatted(validatingTimeoutMinutes);

        return databaseClient.sql(sql)
            .bind("batchSize", effectiveBatchSize)
            .fetch()
            .all()
            .map(this::mapRowToEntity)
            .map(webhookEventPersistenceMapper::toDomain)
            .doOnSubscribe(sub ->
                log.debug("FindReceivedBatch started | batchSize={} | validatingTimeout={}min",
                    effectiveBatchSize,
                    validatingTimeoutMinutes
                )
            )
            .doOnNext(event ->
                log.trace("Claimed webhook event for validation | id={}", event.getId())
            )
            .doOnError(error ->
                log.error("Error finding RECEIVED/VALIDATING webhook batch: {}",
                    error.getMessage()
                )
            );
    }

    private ProviderWebhookEventEntity mapRowToEntity(Map<String, Object> row) {
        return ProviderWebhookEventEntity.builder()
            .id(row.get("pwe_id") != null ? ((Number) row.get("pwe_id")).longValue() : null)
            .uuid(row.get("pwe_uuid") != null ? UUID.fromString(row.get("pwe_uuid").toString()) : null)
            .adapterProviderCode((String) row.get("pwe_adapter_provider_code"))
            .eventType((String) row.get("pwe_event_type"))
            .externalEventId((String) row.get("pwe_external_event_id"))
            .idempotencyKey((String) row.get("pwe_idempotency_key"))
            .processingStatus((String) row.get("pwe_processing_status"))
            .errorCode((String) row.get("pwe_error_code"))
            .retryCount(row.get("pwe_retry_count") != null ? ((Number) row.get("pwe_retry_count")).intValue() : 0)
            .lastError((String) row.get("pwe_last_error"))
            .eventRequest((String) row.get("pwe_event_request"))
            .claimedBy((String) row.get("pwe_claimed_by"))
            .claimedAt(CommonInstantUtils.toInstant(row.get("pwe_claimed_at")))
            .nextRetryAt(CommonInstantUtils.toInstant(row.get("pwe_next_retry_at")))
            .updatedAt(CommonInstantUtils.toInstant(row.get("pwe_updated_at")))
            .receivedAt(CommonInstantUtils.toInstant(row.get("pwe_received_at")))
            .processedAt(CommonInstantUtils.toInstant(row.get("pwe_processed_at")))
            .createdAt(CommonInstantUtils.toInstant(row.get("pwe_created_at")))
            .build();
    }
}
