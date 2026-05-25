package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.model.transaction.StandardTransactionResult;
import com.tumipay.microservice.domain.port.output.IAdapterTransactionRepositoryPort;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.AdapterTransactionMapper;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderIntegrationLogRepository;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * AdapterTransactionRepositoryAdapter
 * <p>
 * AdapterTransactionRepositoryAdapter class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 31/03/2026
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class AdapterTransactionRepositoryAdapter implements IAdapterTransactionRepositoryPort {

    private final IProviderIntegrationLogRepository providerIntegrationLogRepository;
    private final IProviderTransactionRepository providerTransactionRepository;
    private final AdapterTransactionMapper adapterTransactionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<StandardTransactionResult> findByAdapterTransactionId(String adapterTransactionId) {
        log.debug("Finding adapter transaction result by adapterTransactionId: {}", adapterTransactionId);
        return providerTransactionRepository.findByUuid(adapterTransactionId)
            .flatMap(providerTransaction ->
                providerIntegrationLogRepository.findByTransactionId(providerTransaction.getTransactionId())
                    .map(log -> adapterTransactionMapper.toDomain(log, providerTransaction))
            )
            .doOnSuccess(result ->
                log.debug("Operation findByAdapterTransactionId success, AdapterTransactionResult found: {}", result)
            )
            .doOnError(error ->
                log.error("Error finding adapter transaction result by adapterTransactionId: {}", error.getMessage(), error)
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<StandardTransactionResult> findByTransactionId(String transactionId) {
        log.debug("Finding adapter transaction result by transactionId: {}", transactionId);
        return Mono.zip(
                providerTransactionRepository.findByTransactionId(transactionId),
                providerIntegrationLogRepository.findByTransactionId(transactionId)
            )
            .map(tuple ->
                adapterTransactionMapper.toDomain(tuple.getT2(), tuple.getT1())
            )
            .doOnSuccess(result ->
                log.debug("Operation findByTransactionId success, AdapterTransactionResult found: {}", result)
            )
            .doOnError(error ->
                log.error("Error finding adapter transaction result by transactionId: {}", error.getMessage(), error)
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<StandardTransactionResult> findByProviderTransactionId(String providerTransactionId) {
        log.debug("Finding adapter transaction result by providerTransactionId: {}", providerTransactionId);
        return providerTransactionRepository.findByProviderTransactionId(providerTransactionId)
            .flatMap(providerTransaction ->
                providerIntegrationLogRepository.findByTransactionId(providerTransaction.getTransactionId())
                    .map(log -> adapterTransactionMapper.toDomain(log, providerTransaction))
            )
            .doOnSuccess(result ->
                log.debug("Operation findByProviderTransactionId success, AdapterTransactionResult found: {}", result)
            )
            .doOnError(error ->
                log.error("Error finding adapter transaction result by providerTransactionId: {}", error.getMessage(), error)
            );
    }
}