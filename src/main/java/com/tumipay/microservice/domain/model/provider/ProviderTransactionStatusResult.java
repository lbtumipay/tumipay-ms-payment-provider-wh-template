package com.tumipay.microservice.domain.model.provider;

import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.TransactionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * ProviderTransactionStatusDto
 * <p>
 * Neutral domain contract for provider transaction status queries.
 * Abstracts away HTTP/infrastructure details and provides a standard
 * contract for all Payment Provider adapters to implement.
 * <p>
 * No Jackson annotations. Pure domain value object.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 30/04/2026
 */
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderTransactionStatusResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /** Whether the provider operation succeeded. */
    private boolean ok;

    /** TumiPay transaction identifier. */
    private String transactionId;

    /** Provider-specific transaction identifier. */
    private String providerTransactionId;

    /** Transaction operation type (PAYIN_TRANSACTION, PAYOUT_TRANSACTION, etc.). */
    private TransactionTypeEnum operation;

    /** Current transaction status from provider (PENDING, APPROVED, REJECTED, etc.). */
    private TransactionStatusEnum status;

    /** Payment method used (BANK_TRANSFER, BREB, etc.). */
    private PaymentMethodEnum paymentMethod;

    /** Optional metadata or payment-specific data from provider. */
    private Map<String, Object> metadata;

    /** Timestamp of the response. */
    private Instant timestamp;
}



