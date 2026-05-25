package com.tumipay.microservice.infrastructure.adapter.input.http.standard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Document
 * <p>
 * Document class.
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
public class DocumentDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    @NotNull(message = "document_type cannot be null")
    @JsonProperty("document_type")
    private String documentType;

    @NotNull(message = "document_number cannot be null")
    @JsonProperty("document_number")
    private String documentNumber;
}