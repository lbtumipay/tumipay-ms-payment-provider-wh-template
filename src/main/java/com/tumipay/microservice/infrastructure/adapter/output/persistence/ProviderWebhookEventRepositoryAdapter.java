package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.output.IProviderWebhookEventRepositoryPort;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.IProviderWebhookEventPersistenceMapper;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderWebhookEventR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * ProviderWebhookEventRepositoryAdapter
 * <p>
 * Adapter implementation for provider webhook event persistence.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 5/04/2026
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class ProviderWebhookEventRepositoryAdapter implements IProviderWebhookEventRepositoryPort {

    private final IProviderWebhookEventR2dbcRepository providerWebhookEventR2dbcRepository;
    private final IProviderWebhookEventPersistenceMapper providerWebhookEventPersistenceMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<WebhookEvent> save(WebhookEvent webhookEvent) {
        log.debug("Saving provider webhook event uuid={}", webhookEvent.getUuid());
        return Mono.just(webhookEvent)
            .map(providerWebhookEventPersistenceMapper::toEntity)
            .flatMap(providerWebhookEventR2dbcRepository::save)
            .map(providerWebhookEventPersistenceMapper::toDomain)
            .doOnSuccess(saved -> log.info("Provider webhook event saved successfully uuid={}", saved.getUuid()))
            .doOnError(error -> log.error("Error saving provider webhook event: {}", error.getMessage(), error));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<WebhookEvent> update(WebhookEvent webhookEvent) {
        log.debug("Updating provider webhook event uuid={}", webhookEvent.getUuid());
        return Mono.just(webhookEvent)
            .map(providerWebhookEventPersistenceMapper::toEntity)
            .flatMap(providerWebhookEventR2dbcRepository::save)
            .map(providerWebhookEventPersistenceMapper::toDomain)
            .doOnSuccess(updated ->
                log.info("Provider webhook event updated successfully uuid={}", updated.getUuid())
            )
            .doOnError(error ->
                log.error("Error updating provider webhook event: {}", error.getMessage(), error)
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<WebhookEvent> findByUuid(UUID uuid) {
        log.debug("Finding provider webhook event by uuid={}", uuid);
        return providerWebhookEventR2dbcRepository.findByUuid(uuid)
            .map(providerWebhookEventPersistenceMapper::toDomain)
            .doOnError(error ->
                log.error("Error finding provider webhook event by uuid: {}", error.getMessage(), error)
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<WebhookEvent> findByIdempotencyKey(String idempotencyKey) {
        log.debug("Finding provider webhook event by idempotencyKey={}", idempotencyKey);
        return providerWebhookEventR2dbcRepository.findByIdempotencyKey(idempotencyKey)
            .map(providerWebhookEventPersistenceMapper::toDomain)
            .doOnError(error ->
                log.error("Error finding provider webhook event by idempotencyKey: {}", error.getMessage(), error)
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<WebhookEvent> findByExternalEventId(String externalEventId) {
        log.debug("Finding provider webhook event by externalEventId={}", externalEventId);
        return providerWebhookEventR2dbcRepository.findByExternalEventId(externalEventId)
            .map(providerWebhookEventPersistenceMapper::toDomain)
            .doOnError(error ->
                log.error("Error finding provider webhook event by externalEventId: {}", error.getMessage(), error)
            );
    }
}

