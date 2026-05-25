package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.port.output.IProviderTransactionRepositoryPort;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.PaymentAdapterTransactionMapper;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * PaymentProviderTransactionRepositoryAdapter
 * <p>
 * Adapter implementation for payment provider transaction persistence operations.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class ProviderTransactionRepositoryAdapter implements IProviderTransactionRepositoryPort {

    private final IProviderTransactionRepository providerTransactionRepository;
    private final PaymentAdapterTransactionMapper adapterTransactionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ProviderTransaction> save(ProviderTransaction transaction) {
        log.debug("Saving payment provider transaction with transactionId: {}", transaction.getTransactionId());
        return Mono.just(transaction)
            .map(adapterTransactionMapper::toEntity)
            .flatMap(providerTransactionRepository::save)
            .map(adapterTransactionMapper::toDomain)
            .doOnSuccess(saved ->
                log.debug("Operation save success, Payment provider transaction saved successfully: {}", saved)
            )
            .doOnError(error ->
                log.error("Error saving payment provider transaction: {}", error.getMessage())
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ProviderTransaction> update(ProviderTransaction transaction) {
        log.debug("Updating payment provider transaction with transactionId: {}", transaction.getTransactionId());
        return Mono.just(transaction)
            .map(adapterTransactionMapper::toEntity)
            .flatMap(providerTransactionRepository::save)
            .map(adapterTransactionMapper::toDomain)
            .doOnSuccess(updated ->
                log.debug("Operation update success, Payment provider transaction updated successfully: {}", updated)
            )
            .doOnError(error ->
                log.error("Error updating payment provider transaction: {}", error.getMessage())
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ProviderTransaction> findByUuid(String uuid) {
        log.debug("Finding payment provider transaction by UUID: {}", uuid);
        return providerTransactionRepository.findByUuid(uuid)
            .map(adapterTransactionMapper::toDomain)
            .doOnSuccess(found ->
                log.debug("Operation findByUuid success, Payment provider transaction found: {}", found)
            )
            .doOnError(error ->
                log.error("Error finding payment provider transaction by UUID: {}", error.getMessage())
            );
    }

    @Override
    public Mono<ProviderTransaction> findByIdempotencyKey(String idempotencyKey) {
        log.debug("Finding payment provider transaction by idempotencyKey: {}", idempotencyKey);
        return providerTransactionRepository.findByIdempotencyKey(idempotencyKey)
            .map(adapterTransactionMapper::toDomain)
            .doOnSuccess(found ->
                log.debug("Operation findByIdempotencyKey success, Payment provider transaction found: {}", found)
            )
            .doOnError(error ->
                log.error("Error finding payment provider transaction by idempotencyKey: {}, error: {}", idempotencyKey, error.getMessage())
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ProviderTransaction> findByTransactionId(String transactionId) {
        log.debug("Finding payment provider transaction by transactionId: {}", transactionId);
        return providerTransactionRepository.findByTransactionId(transactionId)
            .map(adapterTransactionMapper::toDomain)
            .doOnSuccess(found ->
                log.debug("Operation findByTransactionId success, Payment provider transaction found: {}", found)
            )
            .doOnError(error ->
                log.error("Error finding payment provider transaction by transactionId: {}", error.getMessage(), error)
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ProviderTransaction> findByProviderTransactionId(String providerTransactionId) {
        log.debug("Finding payment provider transaction by providerTransactionId: {}", providerTransactionId);
        return providerTransactionRepository.findByProviderTransactionId(providerTransactionId)
            .map(adapterTransactionMapper::toDomain)
            .doOnSuccess(found ->
                log.debug("Operation findByProviderTransactionId success, Payment provider transaction found: {}", found)
            )
            .doOnError(error ->
                log.error("Error finding payment provider transaction by providerTransactionId: {}", error.getMessage(), error)
            );
    }
}
