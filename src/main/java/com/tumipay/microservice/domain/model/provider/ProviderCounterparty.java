package com.tumipay.microservice.domain.model.provider;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * ProviderCounterparty
 * <p>
 * ProviderCounterparty class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 22/04/2026
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderCounterparty implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String uuid;
    private String cobreId;
    private String geo;
    private String type;
    private String documentNumber;
    private String accountNumber;
    private String bankCode;
    private String status;
}
