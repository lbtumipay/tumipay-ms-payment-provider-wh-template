package com.tumipay.microservice.domain.service.contract;

/**
 * ISaveDomainEntity
 * <p>
 * Contract interface that defines a standardized operation for persisting
 * domain entities.
 * <p>
 * This interface represents a generic abstraction for saving domain-level
 * entities, ensuring:
 * <ul>
 *     <li>Validation of domain invariants prior to persistence</li>
 *     <li>Execution of the persistence operation within appropriate transactional boundaries</li>
 *     <li>Return of the persisted entity with adjusted or generated values
 *         (e.g. identifiers, timestamps)</li>
 * </ul>
 * <p>
 * The concrete implementation of this contract is responsible for:
 * <ul>
 *     <li>Validating domain rules and business constraints</li>
 *     <li>Handling transactional consistency</li>
 *     <li>Ensuring the returned entity reflects its final persisted state</li>
 * </ul>
 * <p>
 * Domain or validation errors should be represented using controlled
 * exceptions aligned with the application’s error handling strategy.
 * Technical failures may propagate as runtime exceptions when appropriate.
 * <p>
 * This interface is technology-agnostic and does not impose any constraints
 * regarding databases, ORMs, frameworks, or storage strategies.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT
 * STANDARDS AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND
 * COPYRIGHT LAWS.
 *
 * @param <E> Type of the domain entity to be persisted.
 *
 * @author TumiPay
 * @since 26/12/2025
 */
public interface ISaveDomainEntity<E, R> {

    /**
     * Persists a domain entity and returns the persisted instance.
     * <p>
     * Requirements:
     * <ul>
     *     <li>{@code entity}: Must be a valid domain entity instance.</li>
     *     <li>The entity must comply with all domain invariants prior to invocation.</li>
     * </ul>
     *
     * Behavior:
     * <ul>
     *     <li>Domain and validation rules may be evaluated before persistence.</li>
     *     <li>If the operation succeeds, the returned entity reflects its final persisted state.</li>
     *     <li>If domain or validation rules are violated, a controlled domain exception may be thrown.</li>
     *     <li>Technical failures may propagate as runtime exceptions.</li>
     * </ul>
     *
     * Important:
     * <ul>
     *     <li>This contract provides a clear semantic for write operations.</li>
     *     <li>It is intended to be used within application or domain services.</li>
     *     <li>No persistence or infrastructure-specific details should leak through this interface.</li>
     * </ul>
     *
     * @param entity domain entity to be persisted.
     * @return the persisted domain entity with its final state.
     */
    R saveDomainEntity(E entity);
}
