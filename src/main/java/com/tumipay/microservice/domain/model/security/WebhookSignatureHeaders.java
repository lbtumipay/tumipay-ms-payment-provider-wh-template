package com.tumipay.microservice.domain.model.security;

/**
 * Immutable representation of the security headers extracted from a Cobre signed webhook request.
 * <p>
 * Headers defined by Cobre official documentation:
 * <ul>
 *   <li>{@code event-timestamp} — ISO 8601 UTC timestamp of event creation</li>
 *   <li>{@code event-signature} — HMAC-SHA256 hex hash of the canonical payload</li>
 * </ul>
 *
 * @param timestamp the value of the {@code event-timestamp} header (ISO 8601 UTC, e.g. {@code 2025-02-03T22:20:24Z})
 * @param signature the value of the {@code event-signature} header (hex-encoded HMAC-SHA256, 64 chars)
 */
public record WebhookSignatureHeaders(
    String timestamp,
    String signature
) {

    /**
     * Returns {@code true} if both timestamp and signature are non-null and non-blank.
     */
    public boolean isComplete() {
        return timestamp != null && !timestamp.isBlank()
            && signature != null && !signature.isBlank();
    }
}
