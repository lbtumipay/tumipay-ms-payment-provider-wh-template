package com.tumipay.microservice.domain.model.provider;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * ProviderTransactionDetailDto
 * <p>
 * Extended transaction detail contract for troubleshooting and audit purposes.
 * Extends {@link ProviderTransactionStatusResult} with diagnostic information:
 * error codes, error messages, raw request/response payloads, and provider raw data.
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
@EqualsAndHashCode(callSuper = true)
public class ProviderTransactionDetailResult extends ProviderTransactionStatusResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /** Provider-assigned reference identifier for reconciliation. */
    private String providerReferenceId;

    /** Human-readable message describing the provider's response. */
    private String message;

    /** Provider-specific error code, if any. */
    private String errorCode;

    /** Provider-specific error message, if any. */
    private String errorMessage;

    /** Raw request payload sent to the provider (serialized). */
    private Object providerRequest;

    /** Raw response payload received from the provider (serialized). */
    private Object providerResponse;

    /** Additional provider-specific diagnostic data. */
    private Map<String, Object> providerRaw;
}

