package com.tumipay.microservice.domain.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * FindAdapterTransactionQuery
 * <p>
 * FindAdapterTransactionQuery class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 31/03/2026
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindStandardTransactionQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String adapterTransactionId;
    private String transactionId;
    private String providerTransactionId;
}