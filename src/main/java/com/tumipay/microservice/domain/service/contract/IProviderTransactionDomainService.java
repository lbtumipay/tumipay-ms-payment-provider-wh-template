package com.tumipay.microservice.domain.service.contract;

import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.model.transaction.StandardTransactionResult;
import com.tumipay.microservice.shared.dto.DomainOperationResult;
import com.tumipay.microservice.shared.dto.DomainValidationResult;
import reactor.core.publisher.Mono;

/**
 * IPaymentProviderDomainService
 * <p>
 * IPaymentProviderDomainService interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
public interface IProviderTransactionDomainService
    extends
    ISaveDomainEntity<ProviderTransaction, Mono<DomainOperationResult<ProviderTransaction>>>,
    IUpdateDomainEntity<ProviderTransaction, Mono<DomainOperationResult<ProviderTransaction>>>,
    IGetDomainEntityByUuId<String, Mono<DomainOperationResult<ProviderTransaction>>> {

    /**
     * Validates the idempotency of a transaction using the provided transaction identifier.
     * If a record with the same ID already exists, a validation failure result is returned,
     * preventing duplicate processing of the same transaction.
     *
     * @param transactionId unique identifier of the transaction to validate.
     * @return a {@link Mono} emitting a {@link DomainValidationResult} indicating
     *         whether the transaction is new (valid) or already registered (invalid).
     */
    Mono<DomainValidationResult> validateIdempotency(String transactionId);

    /**
     * Retrieves a standardized transaction result by the internal TumiPay transaction identifier.
     *
     * @param transactionId the internal unique identifier assigned to the transaction.
     * @return a {@link Mono} emitting a {@link StandardTransactionResult} with the transaction
     *         details, or an empty result if no matching record is found.
     */
    Mono<StandardTransactionResult> findByTransactionId(String transactionId);

    /**
     * Retrieves a standardized transaction result by the adapter-level transaction identifier.
     * This identifier is assigned by the payment adapter layer when the transaction is created.
     *
     * @param adapterTransactionId the unique identifier assigned by the adapter.
     * @return a {@link Mono} emitting a {@link StandardTransactionResult} with the transaction
     *         details, or an empty result if no matching record is found.
     */
    Mono<StandardTransactionResult> findByAdapterTransactionId(String adapterTransactionId);

    /**
     * Retrieves a standardized transaction result by the reference identifier assigned by the
     * external payment provider (e.g., as returned in a payment response or webhook).
     *
     * @param providerTransactionId the provider's reference identifier for the transaction.
     * @return a {@link Mono} emitting a {@link StandardTransactionResult} with the transaction
     *         details, or an empty result if no matching record is found.
     */
    Mono<StandardTransactionResult> findByProviderTransactionId(String providerTransactionId);

    /**
     * Retrieves a provider transaction by the identifier assigned by the
     * external payment provider.
     *
     * @param providerTransactionId the provider's own transaction identifier,
     *                              as received in the webhook event payload.
     * @return {@link Mono} emitting a {@link DomainOperationResult} wrapping the
     *         found {@link ProviderTransaction}, or a failure result if not found.
     */
    Mono<DomainOperationResult<ProviderTransaction>> getByProviderTransactionId(String providerTransactionId);
}
