package com.tumipay.microservice.domain.service.contract;

import java.io.Serializable;

/**
 * IGetDomainEntityByUuId
 * <p>
 * Contract interface that defines a standardized operation for retrieving
 * a domain entity using a Universally Unique Identifier (UUID).
 * <p>
 * This interface provides a generic abstraction for querying domain entities
 * by a globally unique identifier, ensuring:
 * <ul>
 *     <li>Consistent access to domain entities across distributed systems</li>
 *     <li>Clear separation between domain logic and data access concerns</li>
 *     <li>Safe entity retrieval in multi-tenant and event-driven environments</li>
 * </ul>
 * <p>
 * The concrete implementation of this contract is responsible for:
 * <ul>
 *     <li>Validating the UUID format and integrity</li>
 *     <li>Ensuring global uniqueness and existence of the entity</li>
 *     <li>Applying access control or tenancy rules when required</li>
 * </ul>
 * <p>
 * The domain entity type is constrained to {@link Serializable} to support
 * safe transport, logging, and interoperability across distributed platforms.
 * <p>
 * This contract is intentionally technology-agnostic and does not impose
 * any restrictions on persistence, indexing, or UUID generation strategies.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT
 * STANDARDS AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND
 * COPYRIGHT LAWS.
 *
 * @param <E> Type of the domain entity to be retrieved.
 *            Must implement {@link Serializable}.
 * @param <U> Type of the UUID used to locate the entity.
 *
 * @author TumiPay
 * @since 26/12/2025
 */
public interface IGetDomainEntityByUuId<I, R> {

    /**
     * Retrieves a domain entity by its universally unique identifier (UUID).
     * <p>
     * Requirements:
     * <ul>
     *     <li>{@code entityUuId}: Must represent a valid and non-null UUID.</li>
     *     <li>The UUID must uniquely identify a domain entity across the system.</li>
     * </ul>
     *
     * Behavior:
     * <ul>
     *     <li>The implementation validates the UUID before querying.</li>
     *     <li>If the entity does not exist, the implementation may return {@code null} or throw a domain-specific exception.</li>
     *     <li>Read operations must not mutate domain state.</li>
     * </ul>
     *
     * Response details:
     * <ul>
     *     <li>Returns an instance of {@code E} representing the requested domain entity.</li>
     * </ul>
     *
     * Important:
     * <ul>
     *     <li>This contract represents a read-only operation based on UUIDs.</li>
     *     <li>UUIDs are intended for global uniqueness across bounded contexts.</li>
     *     <li>Implementations may leverage indexed UUID fields or caches transparently.</li>
     * </ul>
     *
     * @param entityUuId universally unique identifier of the domain entity.
     * @return domain entity associated with the given UUID.
     */
    R getDomainEntityByUuId(I entityUuId);
}
