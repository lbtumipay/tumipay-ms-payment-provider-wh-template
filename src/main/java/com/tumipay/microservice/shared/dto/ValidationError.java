package com.tumipay.microservice.shared.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * ValidationError
 * <p>
 * ValidationError class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 17/02/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class ValidationError implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    private String field;
    private String message;
}
