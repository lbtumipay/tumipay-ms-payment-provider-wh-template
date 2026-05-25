package com.tumipay.microservice.infrastructure.adapter.output.persistence;

import com.tumipay.microservice.domain.model.provider.ProviderIntegrationLog;
import com.tumipay.microservice.domain.port.output.IProviderIntegrationLogRepositoryPort;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.mapper.PaymentAdapterIntegrationLogMapper;
import com.tumipay.microservice.infrastructure.adapter.output.persistence.repository.IProviderIntegrationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * PaymentProviderIntegrationLogRepositoryAdapter
 * <p>
 * Adapter implementation for payment provider integration log persistence operations.
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
public class ProviderIntegrationLogRepositoryAdapter implements IProviderIntegrationLogRepositoryPort {

    private final IProviderIntegrationLogRepository providerIntegrationLogRepository;
    private final PaymentAdapterIntegrationLogMapper paymentAdapterIntegrationLogMapper;

    /**
     * {@inheritDoc}
     */
    public Mono<ProviderIntegrationLog> save(ProviderIntegrationLog integrationLog) {
        log.debug("Saving payment provider integration log for transactionId: {}", integrationLog.getTransactionId());
        return Mono.just(integrationLog)
            .map(paymentAdapterIntegrationLogMapper::toEntity)
            .flatMap(providerIntegrationLogRepository::save)
            .map(paymentAdapterIntegrationLogMapper::toDomain)
            .doOnSuccess(saved ->
                log.debug("Operation save success, Payment provider integration log saved successfully, {}", saved)
            )
            .doOnError(error ->
                log.error("Error saving payment provider integration log: {}", error.getMessage())
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ProviderIntegrationLog> update(ProviderIntegrationLog integrationLog) {
        log.debug("Updating payment provider integration log for transactionId: {}", integrationLog.getId());
        return Mono.just(integrationLog)
            .map(paymentAdapterIntegrationLogMapper::toEntity)
            .flatMap(providerIntegrationLogRepository::save)
            .map(paymentAdapterIntegrationLogMapper::toDomain)
            .doOnSuccess(updated ->
                log.debug("Operation update success, Payment provider integration log updated successfully, {}", updated)
            )
            .doOnError(error ->
                log.error("Error update payment provider integration log: {}", error.getMessage())
            );
    }

    /**
     * {@inheritDoc}
     */
    public Mono<ProviderIntegrationLog> findByUuid(String uuid) {
        log.debug("Finding payment provider integration log by UUID: {}", uuid);
        return providerIntegrationLogRepository.findByUuid(uuid)
            .map(paymentAdapterIntegrationLogMapper::toDomain)
            .doOnSuccess(found ->
                log.debug("Operation findByUuid success, Payment provider integration log found: {}", found)
            )
            .doOnError(error ->
                log.error("Error finding payment provider integration log by UUID: {}", error.getMessage())
            );
    }

    /**
     * {@inheritDoc}
     */
    public Mono<ProviderIntegrationLog> findByTransactionId(String transactionId) {
        log.debug("Finding payment provider integration logs by transactionId: {}", transactionId);
        return providerIntegrationLogRepository.findByTransactionId(transactionId)
            .flatMap(paymentAdapterIntegrationLogMapper::doToDomain)
            .doOnSuccess(success ->
                log.debug("Operation findByTransactionId success, Payment provider integration logs retrieved for transactionId: {}, success: {}", transactionId, success)
            )
            .doOnError(error ->
                log.error("Error finding payment provider integration logs by transactionId: {}", error.getMessage())
            );
    }

    /**
     * {@inheritDoc}
     */
    public Mono<ProviderIntegrationLog> findByIdempotencyKey(String idempotencyKey) {
        log.debug("Finding payment provider integration logs by idempotencyKey: {}", idempotencyKey);
        return providerIntegrationLogRepository.findByIdempotencyKey(idempotencyKey)
            .flatMap(paymentAdapterIntegrationLogMapper::doToDomain)
            .doOnSuccess(success ->
                log.debug("Operation findByIdempotencyKey success, Payment provider integration logs retrieved for idempotencyKey: {}, success: {}", idempotencyKey, success)
            )
            .doOnError(error ->
                log.error("Error finding payment provider integration logs by idempotencyKey: {}", error.getMessage())
            );
    }

    /**
     * {@inheritDoc}
     */
    public Flux<ProviderIntegrationLog> findAllByAdapterProviderCode(String adapterProviderCode) {
        log.debug("Finding payment provider integration logs by adapterProviderCode: {}", adapterProviderCode);
        return providerIntegrationLogRepository.findByAdapterProviderCode(adapterProviderCode)
            .map(paymentAdapterIntegrationLogMapper::toDomain)
            .doOnComplete(() ->
                log.debug("Operation findAllByAdapterProviderCode success, Payment provider integration logs retrieved for adapterProviderCode: {}", adapterProviderCode)
            )
            .doOnError(error ->
                log.error("Error finding payment provider integration logs by adapterProviderCode: {}", error.getMessage())
            );
    }
}

