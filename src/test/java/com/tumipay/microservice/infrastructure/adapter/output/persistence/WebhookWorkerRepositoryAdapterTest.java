package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.IProviderWebhookEventPersistenceMapper;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.ProviderWebhookEventMapperComponent;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderWebhookEventR2dbcRepository;
import com.tumipay.microservice.shared.properties.WebhookDispatcherProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WebhookWorkerRepositoryAdapterTest
 * <p>
 * Unit tests for {@link WebhookWorkerRepositoryAdapter}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookWorkerRepositoryAdapter Unit Tests")
class WebhookWorkerRepositoryAdapterTest {

    @Mock private DatabaseClient databaseClient;
    @Mock private ProviderWebhookEventMapperComponent providerWebhookEventMapperComponent;
    @Mock private IProviderWebhookEventPersistenceMapper webhookEventPersistenceMapper;
    @Mock private WebhookDispatcherProperties webhookDispatcherProperties;
    @Mock private IProviderWebhookEventR2dbcRepository webhookEventRepository;

    @Mock private DatabaseClient.GenericExecuteSpec executeSpec;
    @SuppressWarnings("rawtypes")
    @Mock private FetchSpec fetchSpec;

    private WebhookWorkerRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WebhookWorkerRepositoryAdapter(
            webhookEventRepository,
            providerWebhookEventMapperComponent,
            webhookEventPersistenceMapper,
            webhookDispatcherProperties,
            databaseClient
        );
    }

    // ── claimBatch ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("claimBatch should return mapped domain events")
    @SuppressWarnings("unchecked")
    void claimBatchShouldReturnMappedEvents() {
        Map<String, Object> row = buildRow(1L);
        WebhookEvent domain = buildDomain(1L);

        when(webhookDispatcherProperties.getWorkerTimeoutMinutes()).thenReturn(5);
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(row));
        when(providerWebhookEventMapperComponent.mapRowToEntity(row)).thenReturn(buildEntity(1L));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.claimBatch("worker-1", 10))
            .assertNext(e -> assertEquals(1L, e.getId()))
            .verifyComplete();

        verify(databaseClient).sql(anyString());
        verify(executeSpec, atLeast(2)).bind(anyString(), any());
        verify(providerWebhookEventMapperComponent).mapRowToEntity(row);
        verify(webhookEventPersistenceMapper).toDomain(any(ProviderWebhookEventEntity.class));
    }

    @Test
    @DisplayName("claimBatch should use default timeout when workerTimeoutMinutes is null")
    @SuppressWarnings("unchecked")
    void claimBatchShouldUseDefaultTimeoutWhenNull() {
        when(webhookDispatcherProperties.getWorkerTimeoutMinutes()).thenReturn(null);
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.all()).thenReturn(Flux.empty());

        StepVerifier.create(adapter.claimBatch("worker-1", 10))
            .verifyComplete();

        verify(databaseClient).sql(anyString());
    }

    @Test
    @DisplayName("claimBatch should normalize batchSize to 1 when zero or negative")
    @SuppressWarnings("unchecked")
    void claimBatchShouldNormalizeBatchSizeToOneWhenNonPositive() {
        when(webhookDispatcherProperties.getWorkerTimeoutMinutes()).thenReturn(5);
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.all()).thenReturn(Flux.empty());

        StepVerifier.create(adapter.claimBatch("worker-1", 0))
            .verifyComplete();

        // Verify bind("batchSize", 1) was called — batchSize 0 → normalized to 1
        verify(executeSpec).bind("batchSize", 1);
    }

    @Test
    @DisplayName("claimBatch should propagate database error")
    @SuppressWarnings("unchecked")
    void claimBatchShouldPropagateError() {
        when(webhookDispatcherProperties.getWorkerTimeoutMinutes()).thenReturn(5);
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.all()).thenReturn(Flux.error(new RuntimeException("db error")));

        StepVerifier.create(adapter.claimBatch("worker-1", 5))
            .expectErrorMatches(e -> "db error".equals(e.getMessage()))
            .verify();
    }

    @Test
    @DisplayName("claimBatch should map row with Instant claimedAt")
    @SuppressWarnings("unchecked")
    void claimBatchShouldMapRowWithInstantClaimedAt() {
        Map<String, Object> row = buildRow(2L);
        row.put("pwe_claimed_at", Instant.now());
        row.put("pwe_next_retry_at", LocalDateTime.now());
        row.put("pwe_updated_at", OffsetDateTime.now());

        WebhookEvent domain = buildDomain(2L);

        when(webhookDispatcherProperties.getWorkerTimeoutMinutes()).thenReturn(5);
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(row));
        when(providerWebhookEventMapperComponent.mapRowToEntity(row)).thenReturn(buildEntity(2L));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.claimBatch("worker-1", 5))
            .assertNext(e -> assertEquals(2L, e.getId()))
            .verifyComplete();
    }

    // ── markAsProcessed ───────────────────────────────────────────────────────

    @Test
    @DisplayName("markAsProcessed should return mapped domain event")
    void markAsProcessedShouldReturnMappedDomain() {
        ProviderWebhookEventEntity entity = buildEntity(10L);
        entity.setProcessingStatus("PROCESSING");
        WebhookEvent domain = buildDomain(10L);

        when(webhookEventRepository.findById(10L)).thenReturn(Mono.just(entity));
        when(webhookEventRepository.save(any(ProviderWebhookEventEntity.class))).thenReturn(Mono.just(entity));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.markAsProcessed(10L))
            .assertNext(e -> assertEquals(10L, e.getId()))
            .verifyComplete();

        verify(webhookEventRepository).findById(10L);
        verify(webhookEventRepository).save(any(ProviderWebhookEventEntity.class));
    }

    @Test
    @DisplayName("markAsProcessed should return empty when row not found")
    void markAsProcessedShouldReturnEmptyWhenNotFound() {
        when(webhookEventRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.markAsProcessed(99L))
            .verifyComplete();
    }

    @Test
    @DisplayName("markAsProcessed should propagate database error")
    void markAsProcessedShouldPropagateError() {
        when(webhookEventRepository.findById(10L)).thenReturn(Mono.error(new RuntimeException("db error")));

        StepVerifier.create(adapter.markAsProcessed(10L))
            .expectErrorMatches(e -> "db error".equals(e.getMessage()))
            .verify();
    }

    // ── markForRetry ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("markForRetry should set PENDING and increment retryCount when not exhausted")
    void markForRetryShouldReturnMappedDomain() {
        ProviderWebhookEventEntity entity = buildEntity(20L);
        entity.setRetryCount(1); // 1 < maxRetryCount(3) → PENDING
        WebhookEvent domain = buildDomain(20L);

        when(webhookEventRepository.findById(20L)).thenReturn(Mono.just(entity));
        when(webhookEventRepository.save(any(ProviderWebhookEventEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.markForRetry(20L, "ERR_001", "Some error", 3))
            .assertNext(e -> assertEquals(20L, e.getId()))
            .verifyComplete();

        verify(webhookEventRepository).save(argThat(e ->
            "PENDING".equals(e.getProcessingStatus())
            && e.getRetryCount() == 2
            && "ERR_001".equals(e.getErrorCode())
            && "Some error".equals(e.getLastError())
        ));
    }

    @Test
    @DisplayName("markForRetry should set FAILED when retryCount is exhausted")
    void markForRetryShouldSetFailedWhenExhausted() {
        ProviderWebhookEventEntity entity = buildEntity(20L);
        entity.setRetryCount(3); // 3 >= maxRetryCount(3) → FAILED
        WebhookEvent domain = buildDomain(20L);

        when(webhookEventRepository.findById(20L)).thenReturn(Mono.just(entity));
        when(webhookEventRepository.save(any(ProviderWebhookEventEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.markForRetry(20L, "ERR_001", "Some error", 3))
            .assertNext(e -> assertEquals(20L, e.getId()))
            .verifyComplete();

        verify(webhookEventRepository).save(argThat(e ->
            "FAILED".equals(e.getProcessingStatus())
        ));
    }

    @Test
    @DisplayName("markForRetry should use fallback values when errorCode and lastError are null")
    void markForRetryShouldUseFallbackValuesWhenNulls() {
        ProviderWebhookEventEntity entity = buildEntity(21L);
        entity.setRetryCount(0);
        WebhookEvent domain = buildDomain(21L);

        when(webhookEventRepository.findById(21L)).thenReturn(Mono.just(entity));
        when(webhookEventRepository.save(any(ProviderWebhookEventEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.markForRetry(21L, null, null, 3))
            .assertNext(e -> assertEquals(21L, e.getId()))
            .verifyComplete();

        verify(webhookEventRepository).save(argThat(e ->
            "UNKNOWN_ERROR".equals(e.getErrorCode()) && "Unknown error".equals(e.getLastError())
        ));
    }

    @Test
    @DisplayName("markForRetry should truncate lastError when longer than 500 chars")
    void markForRetryShouldTruncateLastError() {
        String longError = "x".repeat(600);
        ProviderWebhookEventEntity entity = buildEntity(22L);
        entity.setRetryCount(0);
        WebhookEvent domain = buildDomain(22L);

        when(webhookEventRepository.findById(22L)).thenReturn(Mono.just(entity));
        when(webhookEventRepository.save(any(ProviderWebhookEventEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.markForRetry(22L, "ERR", longError, 3))
            .assertNext(e -> assertEquals(22L, e.getId()))
            .verifyComplete();

        verify(webhookEventRepository).save(argThat(e ->
            e.getLastError() != null && e.getLastError().length() == 500
        ));
    }

    @Test
    @DisplayName("markForRetry should propagate database error")
    void markForRetryShouldPropagateError() {
        when(webhookEventRepository.findById(20L)).thenReturn(Mono.error(new RuntimeException("retry db error")));

        StepVerifier.create(adapter.markForRetry(20L, "ERR", "error", 3))
            .expectErrorMatches(e -> "retry db error".equals(e.getMessage()))
            .verify();
    }

    // ── markAsFailed ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("markAsFailed should return mapped domain event")
    void markAsFailedShouldReturnMappedDomain() {
        ProviderWebhookEventEntity entity = buildEntity(30L);
        WebhookEvent domain = buildDomain(30L);

        when(webhookEventRepository.findById(30L)).thenReturn(Mono.just(entity));
        when(webhookEventRepository.save(any(ProviderWebhookEventEntity.class))).thenReturn(Mono.just(entity));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.markAsFailed(30L, "FATAL_001", "Fatal error occurred"))
            .assertNext(e -> assertEquals(30L, e.getId()))
            .verifyComplete();

        verify(webhookEventRepository).findById(30L);
        verify(webhookEventRepository).save(any(ProviderWebhookEventEntity.class));
    }

    @Test
    @DisplayName("markAsFailed should use fallback values when errorCode and lastError are null")
    void markAsFailedShouldUseFallbackValuesWhenNulls() {
        ProviderWebhookEventEntity entity = buildEntity(31L);
        WebhookEvent domain = buildDomain(31L);

        when(webhookEventRepository.findById(31L)).thenReturn(Mono.just(entity));
        when(webhookEventRepository.save(any(ProviderWebhookEventEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.markAsFailed(31L, null, null))
            .assertNext(e -> assertEquals(31L, e.getId()))
            .verifyComplete();

        verify(webhookEventRepository).save(argThat(e ->
            "FATAL_ERROR".equals(e.getErrorCode()) && "Fatal error".equals(e.getLastError())
        ));
    }

    @Test
    @DisplayName("markAsFailed should truncate lastError when longer than 500 chars")
    void markAsFailedShouldTruncateLastError() {
        String longError = "e".repeat(600);
        ProviderWebhookEventEntity entity = buildEntity(32L);
        WebhookEvent domain = buildDomain(32L);

        when(webhookEventRepository.findById(32L)).thenReturn(Mono.just(entity));
        when(webhookEventRepository.save(any(ProviderWebhookEventEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.markAsFailed(32L, "ERR", longError))
            .assertNext(e -> assertEquals(32L, e.getId()))
            .verifyComplete();

        verify(webhookEventRepository).save(argThat(e ->
            e.getLastError() != null && e.getLastError().length() == 500
        ));
    }

    @Test
    @DisplayName("markAsFailed should propagate database error")
    void markAsFailedShouldPropagateError() {
        when(webhookEventRepository.findById(30L)).thenReturn(Mono.error(new RuntimeException("failed db error")));

        StepVerifier.create(adapter.markAsFailed(30L, "ERR", "error"))
            .expectErrorMatches(e -> "failed db error".equals(e.getMessage()))
            .verify();
    }

    // ── findReceivedBatch ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findReceivedBatch should return mapped domain events with VALIDATING status")
    @SuppressWarnings("unchecked")
    void findReceivedBatchShouldReturnMappedEvents() {
        Map<String, Object> row = buildRow(50L);
        WebhookEvent domain = buildDomain(50L);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(row));
        when(providerWebhookEventMapperComponent.mapRowToEntity(row)).thenReturn(buildEntity(50L));
        when(webhookEventPersistenceMapper.toDomain(any(ProviderWebhookEventEntity.class))).thenReturn(domain);

        StepVerifier.create(adapter.findReceivedBatch(5))
            .assertNext(e -> assertEquals(50L, e.getId()))
            .verifyComplete();

        verify(databaseClient).sql(anyString());
        verify(executeSpec).bind("batchSize", 5);
    }

    @Test
    @DisplayName("findReceivedBatch should normalize batchSize to 1 when zero or negative")
    @SuppressWarnings("unchecked")
    void findReceivedBatchShouldNormalizeBatchSize() {
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.all()).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findReceivedBatch(0))
            .verifyComplete();

        verify(executeSpec).bind("batchSize", 1);
    }

    @Test
    @DisplayName("findReceivedBatch should return empty when no RECEIVED events")
    @SuppressWarnings("unchecked")
    void findReceivedBatchShouldReturnEmptyWhenNone() {
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.all()).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findReceivedBatch(10))
            .verifyComplete();
    }

    @Test
    @DisplayName("findReceivedBatch should propagate database error")
    @SuppressWarnings("unchecked")
    void findReceivedBatchShouldPropagateError() {
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.all()).thenReturn(Flux.error(new RuntimeException("db error")));

        StepVerifier.create(adapter.findReceivedBatch(5))
            .expectErrorMatches(e -> "db error".equals(e.getMessage()))
            .verify();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Map<String, Object> buildRow(Long id) {
        Map<String, Object> row = new HashMap<>();
        row.put("pwe_id", id);
        row.put("pwe_uuid", UUID.randomUUID().toString());
        row.put("pwe_adapter_provider_code", "TP_PROVIDER");
        row.put("pwe_event_type", "PAYMENT_APPROVED");
        row.put("pwe_external_event_id", "EXT-" + id);
        row.put("pwe_idempotency_key", "IDEM-" + id);
        row.put("pwe_processing_status", "PROCESSING");
        row.put("pwe_error_code", null);
        row.put("pwe_retry_count", 0);
        row.put("pwe_last_error", null);
        row.put("pwe_event_request", "{}");
        row.put("pwe_claimed_by", null);
        row.put("pwe_claimed_at", null);
        row.put("pwe_next_retry_at", null);
        row.put("pwe_updated_at", null);
        row.put("pwe_received_at", null);
        row.put("pwe_processed_at", null);
        row.put("pwe_created_at", Instant.now());
        return row;
    }

    private ProviderWebhookEventEntity buildEntity(Long id) {
        return ProviderWebhookEventEntity.builder()
            .id(id)
            .uuid(UUID.randomUUID())
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYMENT_APPROVED")
            .processingStatus("PROCESSING")
            .retryCount(0)
            .build();
    }

    private WebhookEvent buildDomain(Long id) {
        return WebhookEvent.builder()
            .id(id)
            .adapterProviderCode("TP_PROVIDER")
            .eventType("PAYMENT_APPROVED")
            .eventRequest("{}")
            .build();
    }
}

