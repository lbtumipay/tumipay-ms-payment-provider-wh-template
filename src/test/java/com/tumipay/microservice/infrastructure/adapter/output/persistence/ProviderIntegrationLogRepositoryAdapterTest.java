package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.model.provider.ProviderIntegrationLog;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderIntegrationLogEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.PaymentAdapterIntegrationLogMapper;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderIntegrationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProviderIntegrationLogRepositoryAdapterTest
 * <p>
 * ProviderIntegrationLogRepositoryAdapterTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderIntegrationLogRepositoryAdapter Unit Tests")
class ProviderIntegrationLogRepositoryAdapterTest {

    @Mock
    private IProviderIntegrationLogRepository repository;
    @Mock private PaymentAdapterIntegrationLogMapper mapper;
    private ProviderIntegrationLogRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProviderIntegrationLogRepositoryAdapter(repository, mapper);
    }


    @Test
    @DisplayName("save should map to entity, persist, and map back to domain")
    void saveShouldMapPersistAndMapBack() {

        ProviderIntegrationLog domain = domain("TX-001", "uuid-001");
        ProviderIntegrationLogEntity entity = entity("TX-001");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.save(domain))
            .assertNext(r -> assertEquals("TX-001", r.getTransactionId()))
            .verifyComplete();

        verify(mapper).toEntity(domain);
        verify(repository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("save should propagate repository error")
    void saveShouldPropagateRepositoryError() {

        ProviderIntegrationLog domain = domain("TX-ERR", "uuid-err");
        ProviderIntegrationLogEntity entity = entity("TX-ERR");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(new RuntimeException("save error")));

        StepVerifier.create(adapter.save(domain))
            .expectErrorMatches(e -> "save error".equals(e.getMessage()))
            .verify();
    }

    @Test
    @DisplayName("update should map to entity, persist, and map back to domain")
    void updateShouldMapPersistAndMapBack() {

        ProviderIntegrationLog domain = domain("TX-002", "uuid-002");
        ProviderIntegrationLogEntity entity = entity("TX-002");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.update(domain))
            .assertNext(r -> assertEquals("TX-002", r.getTransactionId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("update should propagate repository error")
    void updateShouldPropagateRepositoryError() {

        ProviderIntegrationLog domain = domain("TX-ERR2", "uuid-err2");
        ProviderIntegrationLogEntity entity = entity("TX-ERR2");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(new RuntimeException("update error")));

        StepVerifier.create(adapter.update(domain))
            .expectErrorMatches(e -> "update error".equals(e.getMessage()))
            .verify();
    }

    @Test
    @DisplayName("findByUuid should return mapped domain when entity found")
    void findByUuidShouldReturnMappedDomain() {

        ProviderIntegrationLogEntity entity = entity("TX-003");
        ProviderIntegrationLog domain = domain("TX-003", "uuid-003");

        when(repository.findByUuid("uuid-003")).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findByUuid("uuid-003"))
            .assertNext(r -> assertEquals("uuid-003", r.getUuid()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByUuid should return empty Mono when not found")
    void findByUuidShouldReturnEmptyWhenNotFound() {
        when(repository.findByUuid("missing")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByUuid("missing"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByUuid should propagate repository error")
    void findByUuidShouldPropagateError() {
        when(repository.findByUuid("err-uuid"))
            .thenReturn(Mono.error(new RuntimeException("uuid error")));

        StepVerifier.create(adapter.findByUuid("err-uuid"))
            .expectErrorMatches(e -> "uuid error".equals(e.getMessage()))
            .verify();
    }

    @Test
    @DisplayName("findByTransactionId should return mapped domain via doToDomain")
    void findByTransactionIdShouldReturnMappedDomain() {

        ProviderIntegrationLogEntity entity = entity("TX-004");
        ProviderIntegrationLog domain = domain("TX-004", "uuid-004");

        when(repository.findByTransactionId("TX-004")).thenReturn(Mono.just(entity));
        when(mapper.doToDomain(entity)).thenReturn(Mono.just(domain));

        StepVerifier.create(adapter.findByTransactionId("TX-004"))
            .assertNext(r -> assertEquals("TX-004", r.getTransactionId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByTransactionId should return empty Mono when not found")
    void findByTransactionIdShouldReturnEmptyWhenNotFound() {

        when(repository.findByTransactionId("TX-NONE")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByTransactionId("TX-NONE"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByTransactionId should propagate repository error")
    void findByTransactionIdShouldPropagateError() {

        when(repository.findByTransactionId("TX-ERR"))
            .thenReturn(Mono.error(new RuntimeException("tx error")));

        StepVerifier.create(adapter.findByTransactionId("TX-ERR"))
            .expectErrorMatches(e -> "tx error".equals(e.getMessage()))
            .verify();
    }

    @Test
    @DisplayName("findByIdempotencyKey should return mapped domain when entity found")
    void findByIdempotencyKeyShouldReturnMappedDomain() {

        ProviderIntegrationLogEntity entity = entity("TX-005");
        ProviderIntegrationLog domain = domain("TX-005", "uuid-005");

        when(repository.findByIdempotencyKey("IDEM-005")).thenReturn(Mono.just(entity));
        when(mapper.doToDomain(entity)).thenReturn(Mono.just(domain));

        StepVerifier.create(adapter.findByIdempotencyKey("IDEM-005"))
            .assertNext(r -> assertEquals("TX-005", r.getTransactionId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByIdempotencyKey should return empty Mono when not found")
    void findByIdempotencyKeyShouldReturnEmptyWhenNotFound() {
        when(repository.findByIdempotencyKey("IDEM-NONE")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByIdempotencyKey("IDEM-NONE"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findAllByAdapterProviderCode should return all mapped logs as Flux")
    void findAllByAdapterProviderCodeShouldReturnAllLogs() {

        ProviderIntegrationLogEntity e1 = entity("TX-006");
        ProviderIntegrationLogEntity e2 = entity("TX-007");
        ProviderIntegrationLog d1 = domain("TX-006", "uuid-006");
        ProviderIntegrationLog d2 = domain("TX-007", "uuid-007");

        when(repository.findByAdapterProviderCode("PROV_001")).thenReturn(Flux.just(e1, e2));
        when(mapper.toDomain(e1)).thenReturn(d1);
        when(mapper.toDomain(e2)).thenReturn(d2);

        StepVerifier.create(adapter.findAllByAdapterProviderCode("PROV_001"))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    @DisplayName("findAllByAdapterProviderCode should return empty Flux when no logs found")
    void findAllByAdapterProviderCodeShouldReturnEmptyFlux() {
        when(repository.findByAdapterProviderCode("PROV_NONE")).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findAllByAdapterProviderCode("PROV_NONE"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findAllByAdapterProviderCode should propagate repository error")
    void findAllByAdapterProviderCodeShouldPropagateError() {
        when(repository.findByAdapterProviderCode("PROV_ERR"))
            .thenReturn(Flux.error(new RuntimeException("flux error")));

        StepVerifier.create(adapter.findAllByAdapterProviderCode("PROV_ERR"))
            .expectErrorMatches(e -> "flux error".equals(e.getMessage()))
            .verify();
    }

    private ProviderIntegrationLog domain(String transactionId, String uuid) {
        return ProviderIntegrationLog.builder().transactionId(transactionId).uuid(uuid).build();
    }

    private ProviderIntegrationLogEntity entity(String transactionId) {
        return ProviderIntegrationLogEntity.builder().transactionId(transactionId).build();
    }
}
