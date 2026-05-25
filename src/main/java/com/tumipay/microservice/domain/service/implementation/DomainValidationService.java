package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.service.contract.IDomainValidationService;
import com.tumipay.microservice.shared.dto.DomainValidationResult;
import com.tumipay.microservice.shared.dto.ValidationError;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * DomainValidationService
 * <p>
 * DomainValidationService class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 18/03/2026
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class DomainValidationService implements IDomainValidationService {

    private final Validator validator;

    /**
     * {@inheritDoc}
     */
    public <T> Mono<DomainValidationResult> validate(String domainEntityName, T domainEntity) {

        return Mono.defer(() -> {

            if (domainEntity == null) {
                return Mono.just(
                    DomainValidationResult.failures(
                        "Validation error",
                        List.of(
                            ValidationError.builder()
                                .field(domainEntityName)
                                .message("Object to validate is null")
                                .build()
                        )
                    )
                );
            }

            var violations = validator.validate(domainEntity);

            if (!violations.isEmpty()) {

                var validationErrors = violations.stream()
                    .map(v -> ValidationError.builder()
                        .field(v.getPropertyPath().toString())
                        .message(v.getMessage())
                        .build()
                    )
                    .toList();

                return Mono.just(
                    DomainValidationResult.failures(
                        "Validation failed",
                        validationErrors
                    )
                );
            }

            return Mono.just(DomainValidationResult.success());
        });
    }
}