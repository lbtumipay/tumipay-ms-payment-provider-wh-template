package com.tumipay.microservice.infrastructure.adapter.output.persistence.repository;

import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderIntegrationLogEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * PaymentAdapterIntegrationLogRepository
 * <p>
 * Reactive repository for PaymentAdapterIntegrationLogEntity.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
@Repository
public interface IProviderIntegrationLogRepository extends R2dbcRepository<ProviderIntegrationLogEntity, Long> {

    /**
     * Find an integration log by its UUID.
     *
     * @param uuid the UUID of the integration log
     * @return a Mono containing the integration log entity, or empty if not found
     */
    Mono<ProviderIntegrationLogEntity> findByUuid(String uuid);

    /**
     * Find integration logs by idempotency key.
     *
     * @param idempotencyKey the idempotency key
     * @return a Flux containing all integration log entities with the idempotency key
     */
    Mono<ProviderIntegrationLogEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find all integration logs for a specific transaction ID.
     *
     * @param transactionId the transaction ID
     * @return a Flux containing all integration log entities for the transaction
     */
    Mono<ProviderIntegrationLogEntity> findByTransactionId(String transactionId);

    /**
     * Find all integration logs for a specific adapter provider code.
     *
     * @param adapterProviderCode the adapter provider code
     * @return a Flux containing all integration log entities for the provider
     */
    Flux<ProviderIntegrationLogEntity> findByAdapterProviderCode(String adapterProviderCode);
}

