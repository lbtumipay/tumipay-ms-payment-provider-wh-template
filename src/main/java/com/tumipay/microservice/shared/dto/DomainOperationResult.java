package com.tumipay.microservice.shared.dto;

import com.tumipay.microservice.shared.enums.BaseOperationStatusEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * DomainOperationResult
 * <p>
 * DomainOperationResult class.
 * <p>
 *  Generic wrapper that represents the outcome of a domain operation.
 * This class encapsulates:
 * <ul>
 *     <li>Execution status</li>
 *     <li>Optional error message</li>
 *     <li>Optional list of validation or domain errors</li>
 *     <li>Resulting domain entity</li>
 * </ul>
 * @param <E> Type of the domain entity involved in the operation.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 27/12/2025
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = false)
public class DomainOperationResult<E> implements Serializable{

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /**
     * Execution status of the operation.
     */
    private BaseOperationStatusEnum status;

    /**
     * High-level error message describing the failure.
     */
    private String errorMessage;

    /**
     * List of domain or validation error messages.
     */
    @Builder.Default
    private List<String> errors = Collections.emptyList();

    /**
     * Resulting domain entity after the operation.
     */
    private E entity;

    /**
     * Factory method for successful operations.
     * @param entity resulting domain entity
     * @param <E> entity type
     * @return successful {@link DomainOperationResult}
     */
    public static <E extends Serializable> DomainOperationResult<E> success(E entity) {
        return DomainOperationResult.<E>builder()
            .status(BaseOperationStatusEnum.SUCCESS)
            .entity(entity)
            .build();
    }

    /**
     * Factory method for failed operations.
     * @param errorMessage high-level error message
     * @param <E> entity type
     * @return failed {@link DomainOperationResult}
     */
    public static <E extends Serializable> DomainOperationResult<E> failure(
        String errorMessage
    ) {
        return DomainOperationResult.<E>builder()
            .status(BaseOperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .build();
    }

    /**
     * Factory method for failed operations.
     * @param errorMessage high-level error message
     * @param errors detailed error list
     * @param <E> entity type
     * @return failed {@link DomainOperationResult}
     */
    public static <E extends Serializable> DomainOperationResult<E> failure(
        String errorMessage,
        List<String> errors
    ) {
        return DomainOperationResult.<E>builder()
            .status(BaseOperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .errors(errors)
            .build();
    }

    /**
     * Indicates whether the operation was successful.
     *
     * @return {@code true} if status is SUCCESS
     */
    public boolean isSuccess() {
        return BaseOperationStatusEnum.SUCCESS.equals(this.status);
    }

    /**
     * Indicates whether the operation was failed.
     * @return {@code true} if status is FAILED
     */
    public boolean isFailed() {
        return BaseOperationStatusEnum.FAILED.equals(this.status);
    }
}
