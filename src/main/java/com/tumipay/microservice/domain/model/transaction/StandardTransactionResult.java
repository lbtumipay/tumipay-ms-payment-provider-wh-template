package com.tumipay.microservice.domain.model.transaction;

import lombok.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * StandardTransactionResult
 * <p>
 * StandardTransactionResult class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 31/03/2026
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class StandardTransactionResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    private String adapterTransactionId;
    private String idempotencyKey;
    private String transactionId;
    private String referenceId;
    private String providerTransactionId;
    private String providerReferenceId;
    private String adapterProviderCode;
    private String transactionType;
    private String paymentMethod;
    private String providerEndpoint;
    private String httpMethod;
    private Map<String, Object> providerRequest;
    private Map<String, Object> providerResponse;
    private Integer httpStatusCode;
    private Boolean success;
    private String errorCode;
    private String errorMessage;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant processedAt;
}