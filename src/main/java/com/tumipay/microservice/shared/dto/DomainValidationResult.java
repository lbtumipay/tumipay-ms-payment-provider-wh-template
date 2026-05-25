package com.tumipay.microservice.shared.dto;

import com.tumipay.microservice.domain.component.enums.OperationStatusEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * DomainValidationResult
 * <p>
 * DomainValidationResult class.
 * <p>
 * Generic wrapper that represents the outcome of a domain operation.
 * This class encapsulates:
 * <ul>
 *     <li>Execution status</li>
 *     <li>Optional error message</li>
 *     <li>Optional list of validation or domain errors</li>
 *     <li>Resulting domain entity</li>
 * </ul>
 *
 *            <p>
 *            THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 *            AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 * @author TumiPay SAS.
 * @since 27/12/2025
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString()
public class DomainValidationResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    /**
     * Execution status of the operation.
     */
    private OperationStatusEnum status;

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
     * List of domain or validation error messages.
     */
    @Builder.Default
    private final List<ValidationError> validationErrors = Collections.emptyList();;


    /**
     * Factory method for successful operations.
     *
     * @return successful {@link DomainValidationResult}
     */
    public static DomainValidationResult success() {
        return DomainValidationResult.builder()
            .status(OperationStatusEnum.SUCCESS)
            .build();
    }

    /**
     * Factory method for failed operations.
     *
     * @param errorMessage high-level error message
     * @return failed {@link DomainValidationResult}
     */
    public static DomainValidationResult failure(String errorMessage) {
        return DomainValidationResult.builder()
            .status(OperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .build();
    }

    /**
     * Factory method for failed operations.
     *
     * @param errorMessage high-level error message
     * @param errors       detailed error list
     * @return failed {@link DomainValidationResult}
     */
    public static <E extends Serializable> DomainValidationResult failure(String errorMessage, List<String> errors) {
        return DomainValidationResult.builder()
            .status(OperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .errors(errors)
            .build();
    }

    /**
     * Factory method for failed operations.
     *
     * @param errorMessage high-level error message
     * @param errors       detailed error list
     * @return failed {@link DomainValidationResult}
     */
    public static <E extends Serializable> DomainValidationResult failures(String errorMessage, List<ValidationError> validationErrors) {
        return DomainValidationResult.builder()
            .status(OperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .validationErrors(validationErrors)
            .build();
    }

    /**
     * Indicates whether the operation was successful.
     * @return {@code true} if status is SUCCESS
     */
    public boolean isSuccess() {
        return OperationStatusEnum.SUCCESS.equals(this.status);
    }

    /**
     * Indicates whether the operation was failed.
     * @return {@code true} if status is FAILED
     */
    public boolean isFailed() {
        return OperationStatusEnum.FAILED.equals(this.status);
    }
}
