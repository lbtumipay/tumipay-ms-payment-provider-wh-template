package com.tumipay.microservice.domain.model.webhook;

import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * WebhookClassifierResult
 * <p>
 * WebhookClassifierResult class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 17/04/2026
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class WebhookClassifierResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    private String providerTransactionId;
    private WebhookEventTypeEnum classifiedType;
    private TransactionStatusEnum transactionStatus;
}