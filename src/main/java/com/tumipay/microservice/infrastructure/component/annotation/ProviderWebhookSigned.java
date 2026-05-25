package com.tumipay.microservice.infrastructure.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method or class as requiring Cobre signed webhook validation.
 * <p>
 * When present, {@code WebhookSignatureValidationFilter} intercepts the request,
 * captures the raw body, validates the HMAC-SHA256 signature from the
 * {@code event-signature} header against the {@code event-timestamp} header,
 * and rejects the request with 401 Unauthorized if validation fails.
 * <p>
 * Usage:
 * <pre>{@code
 * @ProviderWebhookSigned
 * @PostMapping("/event")
 * public Mono<ResponseEntity<?>> receiveWebhookEvent(...) { ... }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProviderWebhookSigned {
}
