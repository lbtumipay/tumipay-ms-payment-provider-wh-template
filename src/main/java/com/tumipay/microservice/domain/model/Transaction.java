package com.tumipay.microservice.domain.model;

import com.tumipay.microservice.domain.component.enums.PaymentMethodEnum;
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
public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    private String transactionId;
    private String referenceId;
    private Amount amount;
    private String country;
    private PaymentMethodEnum paymentMethod;
    private String description;
    private Integer expirationMinutes;
}