package com.tumipay.microservice.infrastructure.component.security;

import com.tumipay.microservice.domain.model.security.WebhookSignatureHeaders;
import com.tumipay.microservice.domain.model.security.WebhookSignatureValidationResult;
import com.tumipay.microservice.domain.service.contract.IWebhookSignatureValidator;
import com.tumipay.microservice.shared.properties.WebhookSignatureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Infrastructure implementation of {@link IWebhookSignatureValidator} for Cobre signed webhooks.
 * <p>
 * Validation flow:
 * <ol>
 *   <li>Extract {@code event-timestamp} and {@code event-signature} headers</li>
 *   <li>Reject if either header is missing or blank</li>
 *   <li>Reject if signature validation is disabled (enabled=false in config)</li>
 *   <li>Validate timestamp is within tolerance window (default 300s)</li>
 *   <li>Build canonical payload: {@code event-timestamp + "." + rawBody}</li>
 *   <li>Generate HMAC-SHA256 of canonical payload using configured secret</li>
 *   <li>Perform timing-safe comparison of generated vs. received signature</li>
 * </ol>
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class ProviderWebhookSignatureValidator implements IWebhookSignatureValidator {

    private final WebhookSignatureProperties webhookSignatureProperties;
    private final WebhookPayloadBuilder payloadBuilder;
    private final HmacSha256SignatureGenerator signatureGenerator;
    private final WebhookTimestampValidator timestampValidator;
    private final TimingSafeSignatureComparator signatureComparator;

    @Override
    public Mono<WebhookSignatureValidationResult> validate(String rawBody, HttpHeaders headers) {
        return Mono.fromCallable(() -> doValidate(rawBody, headers))
            .onErrorResume(ex -> {
                log.error("[WEBHOOK-SIGNATURE] Unexpected error during signature validation", ex);
                return Mono.just(WebhookSignatureValidationResult.failure("Internal validation error"));
            });
    }

    private WebhookSignatureValidationResult doValidate(String rawBody, HttpHeaders headers) {

        WebhookSignatureProperties config = webhookSignatureProperties;

        if (config == null || !config.isEnabled()) {
            log.warn("[WEBHOOK-SIGNATURE] Signature validation is disabled — skipping");
            return WebhookSignatureValidationResult.success();
        }

        WebhookSignatureHeaders signatureHeaders = extractHeaders(headers, config);

        if (!signatureHeaders.isComplete()) {
            log.warn("[WEBHOOK-SIGNATURE] Missing required headers: event-timestamp or event-signature");
            return WebhookSignatureValidationResult.failure("Missing signature headers");
        }

        if (!timestampValidator.isValid(signatureHeaders.timestamp(), config.getToleranceSeconds())) {
            log.warn("[WEBHOOK-SIGNATURE] Timestamp validation failed: {}", signatureHeaders.timestamp());
            return WebhookSignatureValidationResult.failure("Timestamp expired or invalid");
        }

        String canonicalPayload = payloadBuilder.build(signatureHeaders.timestamp(), rawBody);
        String generatedSignature = signatureGenerator.generate(canonicalPayload, config.getSecret());

        if (generatedSignature == null) {
            return WebhookSignatureValidationResult.failure("Signature generation failed");
        }

        boolean matches = signatureComparator.matches(generatedSignature, signatureHeaders.signature());

        if (!matches) {
            log.warn("[WEBHOOK-SIGNATURE] Signature mismatch for timestamp: {}", signatureHeaders.timestamp());
            return WebhookSignatureValidationResult.failure("Signature mismatch");
        }

        log.debug("[WEBHOOK-SIGNATURE] Signature valid for timestamp: {}", signatureHeaders.timestamp());
        return WebhookSignatureValidationResult.success();
    }

    private WebhookSignatureHeaders extractHeaders(
        HttpHeaders headers,
        WebhookSignatureProperties config
    ) {
        String timestamp = headers.getFirst(config.getTimestampHeader());
        String signature = headers.getFirst(config.getSignatureHeader());
        return new WebhookSignatureHeaders(timestamp, signature);
    }
}
