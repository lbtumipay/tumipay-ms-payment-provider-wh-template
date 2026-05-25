package com.tumipay.microservice.infrastructure.adapter.output.persistence.repository;

import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderWebhookEventEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * IProviderWebhookEventR2dbcRepository
 * <p>
 * Reactive repository for provider webhook events.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Repository
public interface IProviderWebhookEventR2dbcRepository extends ReactiveCrudRepository<ProviderWebhookEventEntity, Long> {

    Mono<ProviderWebhookEventEntity> findByUuid(UUID uuid);

    Mono<ProviderWebhookEventEntity> findByIdempotencyKey(String idempotencyKey);

    Mono<ProviderWebhookEventEntity> findByExternalEventId(String externalEventId);
}

