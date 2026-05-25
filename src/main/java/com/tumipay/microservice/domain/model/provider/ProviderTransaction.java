package com.tumipay.microservice.domain.model.provider;

import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * ProviderTransaction
 * <p>
 * ProviderTransaction class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class ProviderTransaction implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    private Long id;
    private String uuid;
    private String transactionId;
    private String referenceId;
    private String adapterProviderCode;
    private String providerTransactionId;
    private String providerReferenceId;
    private String idempotencyKey;
    private Integer amount;
    private String currency;
    private TransactionTypeEnum transactionType;
    private TransactionStatusEnum status;
    private PaymentMethodEnum paymentMethod;
    private String errorCode;
    private String errorMessage;
    private Instant providerProcessedAt;
    private String metadata;
    private Instant createdAt;
    private Instant updatedAt;
}