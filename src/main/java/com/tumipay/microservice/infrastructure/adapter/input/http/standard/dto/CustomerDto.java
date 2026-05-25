package com.tumipay.microservice.infrastructure.adapter.input.http.standard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumipay.microservice.domain.component.enums.PersonTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Customer
 * <p>
 * Customer class.
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
public class CustomerDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    @NotNull(message = "customer_id cannot be null")
    @JsonProperty("customer_id")
    private String customerId;

    @NotNull(message = "document cannot be null")
    @JsonProperty("document")
    private DocumentDto document;

    @NotNull(message = "first_name cannot be null")
    @JsonProperty("first_name")
    private String firstName;

    @NotNull(message = "last_name cannot be null")
    @JsonProperty("last_name")
    private String lastName;

    @NotNull(message = "email cannot be null")
    @JsonProperty("email")
    private String email;

    @NotNull(message = "phone cannot be null")
    @JsonProperty("phone")
    private String phone;

    @NotNull(message = "person_type cannot be null")
    @JsonProperty("person_type")
    private PersonTypeEnum personType;
}