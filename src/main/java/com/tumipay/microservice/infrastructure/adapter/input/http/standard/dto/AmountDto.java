package com.tumipay.microservice.infrastructure.adapter.input.http.standard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Amount
 * <p>
 * Amount class.
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
public class AmountDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    @JsonProperty("value")
    private Integer value;

    @JsonProperty("currency")
    private String currency;
}