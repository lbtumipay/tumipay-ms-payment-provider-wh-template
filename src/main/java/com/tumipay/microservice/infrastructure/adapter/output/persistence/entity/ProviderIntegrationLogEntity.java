package com.tumipay.microservice.infrastructure.adapter.output.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * ProviderIntegrationLogEntity
 * <p>
 * Entity representing the tp_payment_adapter_integration_log table.
 * Stores all HTTP interactions between the Adapter and the Payment Provider
 * including request and response payloads.
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
@EqualsAndHashCode
@ToString(callSuper = false)
@Table("tp_provider_integration_log")
public class ProviderIntegrationLogEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /**
     * Primary key identifier of the request log record.
     */
    @Id
    @Column("pil_id")
    private Long id;

    /**
     * Universally unique identifier of the request log entry.
     */
    @Column("pil_uuid")
    private String uuid;

    /**
     * Unique identifier of the transaction associated with the provider request.
     */
    @Column("pil_transaction_id")
    private String transactionId;

    /**
     * Reference ID for the transaction, used for correlating related transactions in TumiPay Gateway.
     */
    @Column("pil_reference_id")
    private String referenceId;

    /**
     * Identifier assigned to the transaction by the payment provider.
     */
    @Column("pil_provider_transaction_id")
    private String providerTransactionId;

    /**
     * Reference ID provided by the payment provider for reconciliation purposes.
     */
    @Column("pil_provider_reference_id")
    private String providerReferenceId;

    /**
     * Type of transaction (PAYIN_TRANSACTION or PAYOUT_TRANSACTION) associated with the provider request.
     */
    @Column("pil_transaction_type")
    private String transactionType;

    /**
     * Payment method used in the transaction (CARD, PSE, TRANSFER, etc.).
     */
    @Column("pil_payment_method")
    private String paymentMethod;

    /**
     * Idempotency key received from the Gateway used to prevent duplicate requests.
     */
    @Column("pil_idempotency_key")
    private String idempotencyKey;

    /**
     * Code identifying the payment provider associated with the request.
     */
    @Column("pil_adapter_provider_code")
    private String adapterProviderCode;

    /**
     * HTTP method used when invoking the provider endpoint (POST, GET, etc.).
     */
    @Column("pil_http_method")
    private String httpMethod;

    /**
     * Provider API endpoint invoked by the adapter.
     */
    @Column("pil_provider_endpoint")
    private String providerEndpoint;

    /**
     * JSON payload sent to the payment provider.
     */
    @Column("pil_request_payload")
    private String requestPayload;

    /**
     * JSON payload received from the payment provider as response.
     */
    @Column("pil_response_payload")
    private String responsePayload;

    /**
     * HTTP status code returned by the payment provider.
     */
    @Column("pil_http_status")
    private Integer httpStatusCode;

    /**
     * Time in milliseconds that the provider request took to complete.
     */
    @Column("pil_provider_latency_ms")
    private Integer providerLatencyMs;

    /**
     * Indicates whether the provider request was successfully processed.
     */
    @Column("pil_success")
    private Boolean success;

    /**
     * Error code returned by the provider in case of failure.
     */
    @Column("pil_error_code")
    private String errorCode;

    /**
     * Error message returned by the provider in case of failure.
     */
    @Column("pil_error_message")
    private String errorMessage;

    /**
     * Timestamp indicating when the request log was created.
     */
    @Column("pil_created_at")
    private Instant createdAt;

    /**
     * Timestamp indicating when the request log was last updated.
     */
    @Column("pil_updated_at")
    private Instant updatedAt;
}

