package com.tumipay.microservice.shared.validation;

import com.tumipay.microservice.shared.dto.CommonValidationResult;
import reactor.core.publisher.Mono;

/**
 * ICommonValidationContract
 * <p>
 * ICommonValidationContract interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 24/05/2026
 */
public interface ICommonValidationContract<T> {

    /**
     * Applies validation rules over the provided object.
     *
     * @param target object to validate
     * @return validation result
     */
    Mono<CommonValidationResult> validate(T target);
}
