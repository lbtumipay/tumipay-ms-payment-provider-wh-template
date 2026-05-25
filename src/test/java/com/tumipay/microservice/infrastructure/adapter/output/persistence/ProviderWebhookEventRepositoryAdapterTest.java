package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.IProviderWebhookEventPersistenceMapper;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderWebhookEventR2dbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProviderWebhookEventRepositoryAdapter.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderWebhookEventRepositoryAdapter Unit Tests")
class ProviderWebhookEventRepositoryAdapterTest {

    @Mock private IProviderWebhookEventR2dbcRepository  repository;
    @Mock private IProviderWebhookEventPersistenceMapper mapper;

    private ProviderWebhookEventRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProviderWebhookEventRepositoryAdapter(repository, mapper);
    }

    // ── save ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save should map to entity, persist, and map back to domain")
    void saveShouldMapPersistAndMapBack() {
        WebhookEvent domain = event("evt-uuid-001", "EVT-EXT-001");
        ProviderWebhookEventEntity entity = entity("evt-uuid-001");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.save(domain))
            .assertNext(r -> assertEquals("evt-uuid-001", r.getUuid()))
            .verifyComplete();

        verify(mapper).toEntity(domain);
        verify(repository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("save should propagate repository error")
    void saveShouldPropagateRepositoryError() {
        WebhookEvent domain = event("evt-err", "EVT-ERR");
        ProviderWebhookEventEntity entity = entity("evt-err");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(new RuntimeException("save error")));

        StepVerifier.create(adapter.save(domain))
            .expectErrorMatches(e -> "save error".equals(e.getMessage()))
            .verify();
    }

    // ── update ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update should map to entity, persist, and map back to domain")
    void updateShouldMapPersistAndMapBack() {
        WebhookEvent domain = event("evt-uuid-002", "EVT-EXT-002");
        ProviderWebhookEventEntity entity = entity("evt-uuid-002");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.update(domain))
            .assertNext(r -> assertEquals("evt-uuid-002", r.getUuid()))
            .verifyComplete();
    }

    @Test
    @DisplayName("update should propagate repository error")
    void updateShouldPropagateRepositoryError() {
        WebhookEvent domain = event("evt-err2", "EVT-ERR2");
        ProviderWebhookEventEntity entity = entity("evt-err2");

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(new RuntimeException("update error")));

        StepVerifier.create(adapter.update(domain))
            .expectErrorMatches(e -> "update error".equals(e.getMessage()))
            .verify();
    }

    // ── findByUuid ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByUuid should return mapped domain when entity found")
    void findByUuidShouldReturnMappedDomain() {
        UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        ProviderWebhookEventEntity entity = entity(uuid.toString());
        WebhookEvent domain = event(uuid.toString(), "EVT-EXT-003");

        when(repository.findByUuid(uuid)).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findByUuid(uuid))
            .assertNext(r -> assertEquals(uuid.toString(), r.getUuid()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByUuid should return empty Mono when not found")
    void findByUuidShouldReturnEmptyWhenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByUuid(uuid))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByUuid should propagate repository error")
    void findByUuidShouldPropagateError() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid))
            .thenReturn(Mono.error(new RuntimeException("uuid error")));

        StepVerifier.create(adapter.findByUuid(uuid))
            .expectErrorMatches(e -> "uuid error".equals(e.getMessage()))
            .verify();
    }

    // ── findByIdempotencyKey ───────────────────────────────────────────────────

    @Test
    @DisplayName("findByIdempotencyKey should return mapped domain when entity found")
    void findByIdempotencyKeyShouldReturnMappedDomain() {
        ProviderWebhookEventEntity entity = entity("evt-uuid-004");
        WebhookEvent domain = event("evt-uuid-004", "EVT-EXT-004");

        when(repository.findByIdempotencyKey("IDEM-004")).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findByIdempotencyKey("IDEM-004"))
            .assertNext(r -> assertEquals("evt-uuid-004", r.getUuid()))
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
    @DisplayName("findByIdempotencyKey should propagate repository error")
    void findByIdempotencyKeyShouldPropagateError() {
        when(repository.findByIdempotencyKey("IDEM-ERR"))
            .thenReturn(Mono.error(new RuntimeException("idem error")));

        StepVerifier.create(adapter.findByIdempotencyKey("IDEM-ERR"))
            .expectErrorMatches(e -> "idem error".equals(e.getMessage()))
            .verify();
    }

    // ── findByExternalEventId ──────────────────────────────────────────────────

    @Test
    @DisplayName("findByExternalEventId should return mapped domain when entity found")
    void findByExternalEventIdShouldReturnMappedDomain() {
        ProviderWebhookEventEntity entity = entity("evt-uuid-005");
        WebhookEvent domain = event("evt-uuid-005", "EVT-EXT-005");

        when(repository.findByExternalEventId("EVT-EXT-005")).thenReturn(Mono.just(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findByExternalEventId("EVT-EXT-005"))
            .assertNext(r -> assertEquals("EVT-EXT-005", r.getExternalEventId()))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByExternalEventId should return empty Mono when not found")
    void findByExternalEventIdShouldReturnEmptyWhenNotFound() {
        when(repository.findByExternalEventId("EVT-NONE")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByExternalEventId("EVT-NONE"))
            .verifyComplete();
    }

    @Test
    @DisplayName("findByExternalEventId should propagate repository error")
    void findByExternalEventIdShouldPropagateError() {
        when(repository.findByExternalEventId("EVT-ERR"))
            .thenReturn(Mono.error(new RuntimeException("ext error")));

        StepVerifier.create(adapter.findByExternalEventId("EVT-ERR"))
            .expectErrorMatches(e -> "ext error".equals(e.getMessage()))
            .verify();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private WebhookEvent event(String uuid, String externalEventId) {
        return WebhookEvent.builder()
            .uuid(uuid)
            .externalEventId(externalEventId)
            .adapterProviderCode("PROV_001")
            .eventType("PAYMENT_APPROVED")
            .eventRequest("{}")
            .processingStatus(WebhookProcessingStatusEnum.RECEIVED)
            .build();
    }

    private ProviderWebhookEventEntity entity(String uuidStr) {
        return ProviderWebhookEventEntity.builder()
            .uuid(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
            .adapterProviderCode("PROV_001")
            .eventType("PAYMENT_APPROVED")
            .build();
    }
}

