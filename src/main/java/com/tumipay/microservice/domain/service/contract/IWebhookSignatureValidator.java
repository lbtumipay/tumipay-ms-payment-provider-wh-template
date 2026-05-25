package com.tumipay.microservice.domain.service.contract;

import com.tumipay.microservice.domain.model.security.WebhookSignatureValidationResult;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

/**
 * Port defining the contract for validating Cobre signed webhook requests.
 * <p>
 * Implementations must:
 * <ol>
 *   <li>Extract {@code event-timestamp} and {@code event-signature} from headers</li>
 *   <li>Validate the timestamp is within the configured tolerance (default 300s)</li>
 *   <li>Build the canonical payload: {@code event-timestamp + "." + rawBody}</li>
 *   <li>Generate HMAC-SHA256 of the canonical payload using the configured secret</li>
 *   <li>Compare the generated signature against the received one using timing-safe comparison</li>
 * </ol>
 */
public interface IWebhookSignatureValidator {

    /**
     * Validates the HMAC-SHA256 signature of a webhook request.
     *
     * @param rawBody the exact request body as received, without any modification
     * @param headers the full HTTP headers of the request
     * @return a {@link Mono} emitting the validation result — never empty, never errors
     */
    Mono<WebhookSignatureValidationResult> validate(String rawBody, HttpHeaders headers);
}
