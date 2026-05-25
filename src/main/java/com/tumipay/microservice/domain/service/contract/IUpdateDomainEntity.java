package com.tumipay.microservice.domain.service.contract;

import java.io.Serializable;

/**
 * IUpdateDomainEntity
 * <p>
 * Contract interface that defines a standardized operation for updating
 * existing domain entities.
 * <p>
 * This interface represents a generic abstraction for modifying the state
 * of a persisted domain entity, ensuring:
 * <ul>
 *     <li>Validation of domain invariants prior to applying updates</li>
 *     <li>Verification of entity existence before modification</li>
 *     <li>Return of the updated domain entity with its final persisted state</li>
 * </ul>
 * <p>
 * The concrete implementation of this contract is responsible for:
 * <ul>
 *     <li>Validating domain rules and business constraints</li>
 *     <li>Ensuring the target entity exists before update</li>
 *     <li>Managing transactional consistency</li>
 *     <li>Handling concurrency, versioning, or optimistic locking if applicable</li>
 * </ul>
 * <p>
 * Domain or validation errors should be represented using controlled
 * exceptions aligned with the application’s error handling strategy.
 * Technical failures may propagate as runtime exceptions when appropriate.
 * <p>
 * Both request and response types are constrained to {@link Serializable}
 * to support safe transmission, persistence, logging, and interoperability
 * across distributed systems.
 * <p>
 * This interface is intentionally technology-agnostic and does not impose
 * any restrictions on the underlying persistence or infrastructure layer.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT
 * STANDARDS AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND
 * COPYRIGHT LAWS.
 *
 * @param <E> Type of the domain entity to be updated.
 *            Must implement {@link Serializable}.
 *
 * @author TumiPay
 * @since 26/12/2025
 */
public interface IUpdateDomainEntity<E, R> {

    /**
     * Updates a domain entity and returns the updated persisted instance.
     * <p>
     * Requirements:
     * <ul>
     *     <li>{@code entity}: Must represent an existing domain entity.</li>
     *     <li>The entity must comply with all domain invariants prior to update.</li>
     * </ul>
     *
     * Behavior:
     * <ul>
     *     <li>The implementation must verify the existence of the entity before applying updates.</li>
     *     <li>Partial or full updates may be applied depending on business rules.</li>
     *     <li>If concurrency control is required, appropriate mechanisms (e.g. versioning, optimistic locking) must be enforced.</li>
     *     <li>If the operation succeeds, the returned entity reflects its final persisted state.</li>
     *     <li>If domain or validation rules are violated, a controlled domain exception may be thrown.</li>
     * </ul>
     *
     * Important:
     * <ul>
     *     <li>This contract provides clear semantic intent for update operations.</li>
     *     <li>It is intended to be used within application or domain services.</li>
     *     <li>No persistence or infrastructure-specific details should leak through this interface.</li>
     * </ul>
     *
     * @param entity domain entity containing the updated state.
     * @return the updated domain entity with its final persisted state.
     */
    R updateDomainEntity(E entity);
}
