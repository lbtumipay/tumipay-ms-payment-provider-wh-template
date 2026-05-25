package com.tumipay.microservice.shared.dto;

import com.tumipay.microservice.shared.enums.BaseOperationStatusEnum;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * CommonValidationResult
 * <p>
 * CommonValidationResult class.
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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString()
public class CommonValidationResult implements Serializable {

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
     * Factory method for successful operations.
     *
     * @return successful {@link CommonValidationResult}
     */
    public static CommonValidationResult success() {
        return CommonValidationResult.builder()
            .status(BaseOperationStatusEnum.SUCCESS)
            .build();
    }

    /**
     * Factory method for failed operations.
     *
     * @param errorMessage high-level error message
     * @return failed {@link CommonValidationResult}
     */
    public static CommonValidationResult failure(String errorMessage) {
        return CommonValidationResult.builder()
            .status(BaseOperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .build();
    }

    /**
     * Factory method for failed operations.
     *
     * @param errorMessage high-level error message
     * @param errors       detailed error list
     * @return failed {@link CommonValidationResult}
     */
    public static <E extends Serializable> CommonValidationResult failure(String errorMessage, List<String> errors) {
        return CommonValidationResult.builder()
            .status(BaseOperationStatusEnum.FAILED)
            .errorMessage(errorMessage)
            .errors(errors)
            .build();
    }

    /**
     * Indicates whether the operation was successful.
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
