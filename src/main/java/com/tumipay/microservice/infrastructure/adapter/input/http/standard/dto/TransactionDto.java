package com.tumipay.microservice.infrastructure.adapter.input.http.standard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Transaction
 * <p>
 * Transaction class.
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
public class TransactionDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    @NotBlank(message = "transaction_id cannot be blank")
    @JsonProperty("transaction_id")
    private String transactionId;

    @NotBlank(message = "reference_id cannot be blank")
    @JsonProperty("reference_id")
    private String referenceId;

    @NotNull(message = "amount cannot be blank")
    @JsonProperty("amount")
    private AmountDto amount;

    @NotBlank(message = "country cannot be blank")
    @JsonProperty("country")
    private String country;

    @NotNull(message = "payment_method cannot be null")
    @JsonProperty("payment_method")
    private PaymentMethodEnum paymentMethod;

    @NotBlank(message = "description cannot be blank")
    @JsonProperty("description")
    private String description;

    @JsonProperty("expiration_minutes")
    @NotNull(message = "expiration_minutes cannot be null")
    @Positive(message = "expiration_minutes must be greater than 0")
    private Integer expirationMinutes;
}