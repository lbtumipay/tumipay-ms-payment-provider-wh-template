package com.tumipay.microservice.infrastructure.adapter.output.persistence.repository;

import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.ProviderTransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * IProviderTransactionRepository
 * <p>
 * Reactive repository for PaymentAdapterTransactionEntity.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
@Repository
public interface IProviderTransactionRepository extends R2dbcRepository<ProviderTransactionEntity, Long> {

    /**
     * Find a payment adapter transaction by its UUID.
     *
     * @param uuid the UUID of the transaction
     * @return a Mono containing the transaction entity, or empty if not found
     */
    Mono<ProviderTransactionEntity> findByUuid(String uuid);

    /**
     * Find a payment adapter transaction by its transaction ID.
     *
     * @param transactionId the transaction ID from the Gateway
     * @return a Mono containing the transaction entity, or empty if not found
     */
    Mono<ProviderTransactionEntity> findByTransactionId(String transactionId);

    /**
     * Find a payment adapter transaction by provider transaction ID.
     *
     * @param providerTransactionId the provider transaction ID
     * @return a Mono containing the transaction entity, or empty if not found
     */
    Mono<ProviderTransactionEntity> findByProviderTransactionId(String providerTransactionId);


    /**
     * Find a payment adapter transaction by its transaction ID.
     *
     * @param idempotencyKey the idempotency key of the transaction
     * @return a Mono containing the transaction entity, or empty if not found
     */
    Mono<ProviderTransactionEntity> findByIdempotencyKey(String idempotencyKey);
}
