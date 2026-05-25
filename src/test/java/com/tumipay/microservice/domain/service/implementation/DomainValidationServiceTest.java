package com.tumipay.microservice.domain.service.implementation;

import com.tumipay.microservice.domain.component.enums.OperationStatusEnum;
import com.tumipay.microservice.shared.dto.DomainValidationResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DomainValidationServiceTest
 * <p>
 * DomainValidationServiceTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 25/04/2026
 */
@ExtendWith(MockitoExtension.class)
class DomainValidationServiceTest {

    @Mock
    private Validator validator;

    private DomainValidationService domainValidationService;

    @BeforeEach
    void setUp() {
        domainValidationService = new DomainValidationService(validator);
    }

    @Test
    void shouldFailWhenDomainEntityIsNull() {
        StepVerifier.create(domainValidationService.validate("ProviderWebhookEvent", null))
            .assertNext(result -> {
                Assertions.assertEquals(OperationStatusEnum.FAILED, result.getStatus());
                Assertions.assertEquals("Validation error", result.getErrorMessage());
                Assertions.assertFalse(result.getValidationErrors().isEmpty());
                Assertions.assertEquals("ProviderWebhookEvent", result.getValidationErrors().getFirst().getField());
            })
            .verifyComplete();
    }

    @Test
    void shouldFailWhenConstraintViolationsExist() {
        Object payload = new Object();

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = (ConstraintViolation<Object>) org.mockito.Mockito.mock(ConstraintViolation.class);
        Path propertyPath = org.mockito.Mockito.mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("eventType");
        when(violation.getMessage()).thenReturn("must not be blank");
        when(validator.validate(any())).thenReturn(Set.of(violation));

        StepVerifier.create(domainValidationService.validate("ProviderWebhookEvent", payload))
            .assertNext(result -> {
                Assertions.assertEquals(OperationStatusEnum.FAILED, result.getStatus());
                Assertions.assertEquals("Validation failed", result.getErrorMessage());
                Assertions.assertEquals("eventType", result.getValidationErrors().getFirst().getField());
                Assertions.assertEquals("must not be blank", result.getValidationErrors().getFirst().getMessage());
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnSuccessWhenNoViolationsExist() {
        Object payload = new Object();
        when(validator.validate(any())).thenReturn(Collections.emptySet());

        StepVerifier.create(domainValidationService.validate("ProviderWebhookEvent", payload))
            .assertNext(DomainValidationResult::isSuccess)
            .verifyComplete();
    }
}
