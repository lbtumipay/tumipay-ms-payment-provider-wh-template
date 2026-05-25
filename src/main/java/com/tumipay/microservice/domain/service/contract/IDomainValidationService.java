package com.tumipay.microservice.domain.service.contract;

import com.tumipay.microservice.shared.dto.DomainValidationResult;
import reactor.core.publisher.Mono;

/**
 * IDomainValidationService
 * <p>
 * IDomainValidationService interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 18/03/2026
 */
public interface IDomainValidationService {

    /**
     * Validates a given domain entity using Bean Validation (Jakarta Validation API)
     * and returns the result wrapped in a reactive {@link Mono}.
     *
     * <p>
     * This method performs a synchronous validation internally but defers execution
     * using {@link Mono#defer(java.util.function.Supplier)} to ensure proper reactive
     * behavior within a reactive pipeline.
     * </p>
     *
     * <p>
     * The validation process includes:
     * <ul>
     *     <li>Null check for the input entity</li>
     *     <li>Constraint validation using {@link jakarta.validation.Validator}</li>
     *     <li>Mapping of constraint violations into a standardized error structure</li>
     * </ul>
     * </p>
     *
     * <p>
     * The result is encapsulated in a {@link DomainValidationResult} object:
     * <ul>
     *     <li>{@code SUCCESS} if no validation errors are found</li>
     *     <li>{@code FAILED} if validation errors exist, including detailed error messages</li>
     * </ul>
     * </p>
     *
     * <p>
     * This method does not throw exceptions as part of the validation flow. Instead,
     * it returns a controlled domain result, making it suitable for functional and
     * reactive pipelines.
     * </p>
     *
     * @param <T>          the type of the domain entity to validate
     * @param domainEntityName the domain object to be validated
     * @param domainEntity the domain object to be validated
     * @return a {@link Mono} emitting a {@link DomainValidationResult} indicating
     * the outcome of the validation process
     */
    <T> Mono<DomainValidationResult> validate(String domainEntityName, T domainEntity);
}
