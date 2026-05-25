package com.tumipay.microservice.infrastructure.adapter.output.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * CobreCounterpartyEntity
 * <p>
 * CobreCounterpartyEntity class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString(callSuper = false)
@Table("tp_cobre_counterparty")
public class CobreCounterpartyEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column("cc_id")
    private Long id;

    @Column("cc_uuid")
    private String uuid;

    @Column("cc_cobre_id")
    private String cobreId;

    @Column("cc_geo")
    private String geo;

    @Column("cc_type")
    private String type;

    @Column("cc_alias")
    private String alias;

    @Column("cc_bank_code")
    private String bankCode;

    @Column("cc_account_number")
    private String accountNumber;

    @Column("cc_document_number")
    private String documentNumber;

    @Column("cc_status")
    private String status;

    @Column("cc_error_code")
    private String errorCode;

    @Column("cc_error_message")
    private String errorMessage;

    @Column("cc_metadata")
    private String metadata;

    @Column("cc_created_at")
    private Instant createdAt;

    @Column("cc_updated_at")
    private Instant updatedAt;
}