package com.tumipay.microservice.domain.model;

import com.tumipay.microservice.domain.component.enums.AccountTypeEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * BankAccount
 * <p>
 * BankAccount class.
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
public class BankAccount implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;
    private String bankCode;
    private String bankName;
    private AccountTypeEnum accountType;
    private String accountNumber;
}