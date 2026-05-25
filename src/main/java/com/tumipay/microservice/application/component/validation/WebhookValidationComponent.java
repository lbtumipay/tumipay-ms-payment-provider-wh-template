package com.tumipay.microservice.application.component.validation;

import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.shared.dto.CommonValidationResult;
import com.tumipay.microservice.shared.validation.ICommonValidationContract;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * WebhookValidationComponent
 * <p>
 * WebhookValidationComponent class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 25/05/2026
 */
@Component
public class WebhookValidationComponent implements ICommonValidationContract<WebhookEvent> {

    @Override
    public Mono<CommonValidationResult> validate(final WebhookEvent target) {
        return Mono.just(CommonValidationResult.success());
    }
}