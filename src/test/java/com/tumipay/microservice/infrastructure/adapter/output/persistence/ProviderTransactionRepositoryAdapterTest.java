package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderTransactionEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.PaymentAdapterTransactionMapper;
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
 * Unit tests for ProviderTransactionRepositoryAdapter.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderTransactionRepositoryAdapter Unit Tests")
class ProviderTransactionRepositoryAdapterTest {

    @Mock private IProviderTransactionRepository providerTransactionRepository;
    @Mock private PaymentAdapterTransactionMapper mapper;

    private ProviderTransactionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProviderTransactionRepositoryAdapter(providerTransactionRepository, mapper);
    }

    // ── save ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save should map to entity, persist, and map back to domain")
    void saveShouldMapPersistAndMapBack() {
        ProviderTransaction domain = domain("TX-001", "uuid-001");
        ProviderTransactionEntity entity = entity("TX-001", "uuid-001");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(providerTransactionRepository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.save(domain))
            .assertNext(r -> assertEquals("TX-001", r.getTransactionId()))
            .verifyComplete();

        verify(mapper).toEntity(domain);
        verify(providerTransactionRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("save should propagate repository error")
    void saveShouldPropagateRepositoryError() {
        ProviderTransaction domain = domain("TX-ERR", "uuid-err");
        ProviderTransactionEntity entity = entity("TX-ERR", "uuid-err");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(providerTransactionRepository.save(entity))
            .thenReturn(Mono.error(new RuntimeException("save error")));

        StepVerifier.create(adapter.save(domain))
            .expectErrorMatches(e -> "save error".equals(e.getMessage()))
            .verify();
    }

    // ── update ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update should map to entity, persist, and map back to domain")
    void updateShouldMapPersistAndMapBack() {
        ProviderTransaction domain = domain("TX-002", "uuid-002");
        ProviderTransactionEntity entity = entity("TX-002", "uuid-002");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(providerTransactionRepository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.update(domain))
            .assertNext(r -> assertEquals("TX-002", r.getTransactionId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("update should propagate repository error")
    void updateShouldPropagateRepositoryError() {
        ProviderTransaction domain = domain("TX-ERR2", "uuid-err2");
        ProviderTransactionEntity entity = entity("TX-ERR2", "uuid-err2");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(providerTransactionRepository.save(entity))
            .thenReturn(Mono.error(new RuntimeException("update error")));

        StepVerifier.create(adapter.update(domain))
            .expectErrorMatches(e -> "update error".equals(e.getMessage()))
            .verify();
    }

    // ── findByUuid ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByUuid should return mapped domain when entity found")
    void findByUuidShouldReturnMappedDomain() {
        ProviderTransactionEntity entity = entity("TX-003", "uuid-003");
        ProviderTransaction domain = domain("TX-003", "uuid-003");

        when(providerTransactionRepository.findByUuid("uuid-003")).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findByUuid("uuid-003"))
            .assertNext(r -> assertEquals("uuid-003", r.getUuid()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByUuid should return empty Mono when not found")
    void findByUuidShouldReturnEmptyWhenNotFound() {
        when(providerTransactionRepository.findByUuid("missing")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByUuid("missing"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByUuid should propagate repository error")
    void findByUuidShouldPropagateError() {
        when(providerTransactionRepository.findByUuid("err-uuid"))
            .thenReturn(Mono.error(new RuntimeException("uuid error")));

        StepVerifier.create(adapter.findByUuid("err-uuid"))
            .expectErrorMatches(e -> "uuid error".equals(e.getMessage()))
            .verify();
    }

    // ── findByIdempotencyKey ───────────────────────────────────────────────────

    @Test
    @DisplayName("findByIdempotencyKey should return mapped domain when entity found")
    void findByIdempotencyKeyShouldReturnMappedDomain() {
        ProviderTransactionEntity entity = entity("TX-004", "uuid-004");
        ProviderTransaction domain = domain("TX-004", "uuid-004");

        when(providerTransactionRepository.findByIdempotencyKey("IDEM-004"))
            .thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findByIdempotencyKey("IDEM-004"))
            .assertNext(r -> assertEquals("TX-004", r.getTransactionId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByIdempotencyKey should return empty Mono when not found")
    void findByIdempotencyKeyShouldReturnEmptyWhenNotFound() {
        when(providerTransactionRepository.findByIdempotencyKey("IDEM-NONE"))
            .thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByIdempotencyKey("IDEM-NONE"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByIdempotencyKey should propagate error")
    void findByIdempotencyKeyShouldPropagateError() {
        when(providerTransactionRepository.findByIdempotencyKey("IDEM-ERR"))
            .thenReturn(Mono.error(new RuntimeException("idem error")));

        StepVerifier.create(adapter.findByIdempotencyKey("IDEM-ERR"))
            .expectErrorMatches(e -> "idem error".equals(e.getMessage()))
            .verify();
    }

    // ── findByTransactionId ────────────────────────────────────────────────────

    @Test
    @DisplayName("findByTransactionId should return mapped domain when entity found")
    void findByTransactionIdShouldReturnMappedDomain() {
        ProviderTransactionEntity entity = entity("TX-005", "uuid-005");
        ProviderTransaction domain = domain("TX-005", "uuid-005");

        when(providerTransactionRepository.findByTransactionId("TX-005"))
            .thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findByTransactionId("TX-005"))
            .assertNext(r -> assertEquals("TX-005", r.getTransactionId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByTransactionId should return empty Mono when not found")
    void findByTransactionIdShouldReturnEmptyWhenNotFound() {
        when(providerTransactionRepository.findByTransactionId("TX-NONE"))
            .thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByTransactionId("TX-NONE"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByTransactionId should propagate error")
    void findByTransactionIdShouldPropagateError() {
        when(providerTransactionRepository.findByTransactionId("TX-ERR"))
            .thenReturn(Mono.error(new RuntimeException("tx error")));

        StepVerifier.create(adapter.findByTransactionId("TX-ERR"))
            .expectErrorMatches(e -> "tx error".equals(e.getMessage()))
            .verify();
    }

    // ── findByProviderTransactionId ────────────────────────────────────────────

    @Test
    @DisplayName("findByProviderTransactionId should return mapped domain when entity found")
    void findByProviderTransactionIdShouldReturnMappedDomain() {
        ProviderTransactionEntity entity = entity("TX-006", "uuid-006");
        ProviderTransaction domain = domain("TX-006", "uuid-006");

        when(providerTransactionRepository.findByProviderTransactionId("PROV-TX-006"))
            .thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findByProviderTransactionId("PROV-TX-006"))
            .assertNext(r -> assertEquals("TX-006", r.getTransactionId()))
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

    private ProviderTransaction domain(String transactionId, String uuid) {
        return ProviderTransaction.builder().transactionId(transactionId).uuid(uuid).build();
    }

    private ProviderTransactionEntity entity(String transactionId, String uuid) {
        return ProviderTransactionEntity.builder().transactionId(transactionId).uuid(uuid).build();
    }
}

