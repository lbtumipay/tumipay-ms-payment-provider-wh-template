package com.tumipay.microservice.domain.model.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * ProviderIntegrationLog
 * <p>
 * ProviderIntegrationLog class.
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
public class ProviderIntegrationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    @JsonIgnore
    private Long id;
    private String uuid;
    private String transactionId;
    private String referenceId;
    private String providerTransactionId;
    private String providerReferenceId;
    private String idempotencyKey;
    private String adapterProviderCode;
    private TransactionTypeEnum transactionType;
    private PaymentMethodEnum paymentMethod;
    private String providerEndpoint;
    private String httpMethod;
    private String requestPayload;
    private String responsePayload;
    private Integer httpStatusCode;
    private int providerLatencyMs;
    private Boolean success;
    private String errorCode;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}