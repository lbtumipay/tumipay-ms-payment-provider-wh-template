package com.tumipay.microservice.domain.model.security;

/**
 * Immutable result of a webhook signature validation operation.
 * <p>
 * Encapsulates whether the validation passed and, if not, the reason for rejection.
 *
 * @param valid  {@code true} if the signature is valid and the request can be processed
 * @param reason human-readable rejection reason when {@code valid} is {@code false}; {@code null} on success
 */
public record WebhookSignatureValidationResult(
    boolean valid,
    String reason
) {

    /** Factory method for a successful validation. */
    public static WebhookSignatureValidationResult success() {
        return new WebhookSignatureValidationResult(true, null);
    }

    /** Factory method for a failed validation with a descriptive reason. */
    public static WebhookSignatureValidationResult failure(String reason) {
        return new WebhookSignatureValidationResult(false, reason);
    }
}
