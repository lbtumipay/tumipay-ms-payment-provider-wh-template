package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.model.transaction.StandardTransactionResult;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderIntegrationLogEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderTransactionEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.AdapterTransactionMapper;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderIntegrationLogRepository;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdapterTransactionRepositoryAdapter.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdapterTransactionRepositoryAdapter Unit Tests")
class AdapterTransactionRepositoryAdapterTest {

    @Mock private IProviderTransactionRepository    providerTransactionRepository;
    @Mock private IProviderIntegrationLogRepository providerIntegrationLogRepository;
    @Mock private AdapterTransactionMapper          adapterTransactionMapper;

    private AdapterTransactionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AdapterTransactionRepositoryAdapter(
            providerIntegrationLogRepository,
            providerTransactionRepository,
            adapterTransactionMapper
        );
    }

    // ── findByAdapterTransactionId ─────────────────────────────────────────────

    @Test
    @DisplayName("findByAdapterTransactionId should return mapped result when both entities found")
    void findByAdapterTransactionIdShouldReturnMappedResult() {
        ProviderTransactionEntity    txEntity  = txEntity("TX-001", "uuid-001");
        ProviderIntegrationLogEntity logEntity = logEntity("TX-001");
        StandardTransactionResult    result    = result("uuid-001", "TX-001");

        when(providerTransactionRepository.findByUuid("uuid-001"))
            .thenReturn(Mono.just(txEntity));
        when(providerIntegrationLogRepository.findByTransactionId("TX-001"))
            .thenReturn(Mono.just(logEntity));
        when(adapterTransactionMapper.toDomain(logEntity, txEntity))
            .thenReturn(result);

        StepVerifier.create(adapter.findByAdapterTransactionId("uuid-001"))
            .assertNext(r -> assertEquals("TX-001", r.getTransactionId()))
            .verifyComplete();

        verify(providerTransactionRepository).findByUuid("uuid-001");
        verify(providerIntegrationLogRepository).findByTransactionId("TX-001");
    }

    @Test
    @DisplayName("findByAdapterTransactionId should return empty Mono when transaction not found")
    void findByAdapterTransactionIdShouldReturnEmptyWhenTxNotFound() {
        when(providerTransactionRepository.findByUuid("missing-uuid"))
            .thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByAdapterTransactionId("missing-uuid"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByAdapterTransactionId should propagate error from transaction repository")
    void findByAdapterTransactionIdShouldPropagateRepositoryError() {
        when(providerTransactionRepository.findByUuid("uuid-err"))
            .thenReturn(Mono.error(new RuntimeException("db error")));

        StepVerifier.create(adapter.findByAdapterTransactionId("uuid-err"))
            .expectErrorMatches(e -> "db error".equals(e.getMessage()))
            .verify();
    }

    // ── findByTransactionId ────────────────────────────────────────────────────

    @Test
    @DisplayName("findByTransactionId should zip both repositories and return mapped result")
    void findByTransactionIdShouldZipBothRepositoriesAndReturnMappedResult() {
        ProviderTransactionEntity    txEntity  = txEntity("TX-002", "uuid-002");
        ProviderIntegrationLogEntity logEntity = logEntity("TX-002");
        StandardTransactionResult    result    = result("uuid-002", "TX-002");

        when(providerTransactionRepository.findByTransactionId("TX-002"))
            .thenReturn(Mono.just(txEntity));
        when(providerIntegrationLogRepository.findByTransactionId("TX-002"))
            .thenReturn(Mono.just(logEntity));
        when(adapterTransactionMapper.toDomain(logEntity, txEntity))
            .thenReturn(result);

        StepVerifier.create(adapter.findByTransactionId("TX-002"))
            .assertNext(r -> assertEquals("TX-002", r.getTransactionId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByTransactionId should return empty Mono when transaction not found")
    void findByTransactionIdShouldReturnEmptyWhenTxNotFound() {
        when(providerTransactionRepository.findByTransactionId("TX-NONE"))
            .thenReturn(Mono.empty());
        when(providerIntegrationLogRepository.findByTransactionId("TX-NONE"))
            .thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByTransactionId("TX-NONE"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByTransactionId should propagate error from transaction repository")
    void findByTransactionIdShouldPropagateError() {
        when(providerTransactionRepository.findByTransactionId("TX-ERR"))
            .thenReturn(Mono.error(new RuntimeException("tx error")));
        when(providerIntegrationLogRepository.findByTransactionId("TX-ERR"))
            .thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByTransactionId("TX-ERR"))
            .expectErrorMatches(e -> "tx error".equals(e.getMessage()))
            .verify();
    }

    // ── findByProviderTransactionId ────────────────────────────────────────────

    @Test
    @DisplayName("findByProviderTransactionId should return mapped result when entities found")
    void findByProviderTransactionIdShouldReturnMappedResult() {
        ProviderTransactionEntity    txEntity  = txEntity("TX-003", "uuid-003");
        ProviderIntegrationLogEntity logEntity = logEntity("TX-003");
        StandardTransactionResult    result    = result("uuid-003", "TX-003");

        when(providerTransactionRepository.findByProviderTransactionId("PROV-TX-003"))
            .thenReturn(Mono.just(txEntity));
        when(providerIntegrationLogRepository.findByTransactionId("TX-003"))
            .thenReturn(Mono.just(logEntity));
        when(adapterTransactionMapper.toDomain(logEntity, txEntity))
            .thenReturn(result);

        StepVerifier.create(adapter.findByProviderTransactionId("PROV-TX-003"))
            .assertNext(r -> assertEquals("TX-003", r.getTransactionId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByProviderTransactionId should return empty Mono when not found")
    void findByProviderTransactionIdShouldReturnEmptyWhenNotFound() {
        when(providerTransactionRepository.findByProviderTransactionId("PROV-NONE"))
            .thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByProviderTransactionId("PROV-NONE"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByProviderTransactionId should propagate error")
    void findByProviderTransactionIdShouldPropagateError() {
        when(providerTransactionRepository.findByProviderTransactionId("PROV-ERR"))
            .thenReturn(Mono.error(new RuntimeException("prov error")));

        StepVerifier.create(adapter.findByProviderTransactionId("PROV-ERR"))
            .expectErrorMatches(e -> "prov error".equals(e.getMessage()))
            .verify();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private ProviderTransactionEntity txEntity(String transactionId, String uuid) {
        return ProviderTransactionEntity.builder()
            .uuid(uuid)
            .transactionId(transactionId)
            .build();
    }

    private ProviderIntegrationLogEntity logEntity(String transactionId) {
        return ProviderIntegrationLogEntity.builder()
            .transactionId(transactionId)
            .build();
    }

    private StandardTransactionResult result(String adapterTransactionId, String transactionId) {
        return StandardTransactionResult.builder()
            .adapterTransactionId(adapterTransactionId)
            .transactionId(transactionId)
            .build();
    }
}

