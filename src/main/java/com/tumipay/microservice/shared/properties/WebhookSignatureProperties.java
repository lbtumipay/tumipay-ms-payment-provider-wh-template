package com.tumipay.microservice.shared.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;

/**
 * WebhookSignatureProperties
 * <p>
 * Configuration for Cobre signed webhook validation.
 * <p>
 * Enables HMAC-SHA256 signature verification on incoming webhook events
 * to prevent request forgery and replay attacks.
 * <p>
 * It is a key component for adapter-based integrations following TumiPay standards.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 13/02/2026
 */
@Data
@ConfigurationProperties(prefix = "tumipay.webhook-signature")
public class WebhookSignatureProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Enables or disables signature validation.
     * <p>
     * Set to {@code false} only in local development environments.
     * MUST be {@code true} in production.
     */
    private boolean enabled = true;

    /**
     * Maximum allowed difference (in seconds) between the event-timestamp header
     * and the current server time. Prevents replay attacks.
     * <p>
     * Default: 300 seconds (5 minutes), as recommended by Cobre documentation.
     */
    private int toleranceSeconds = 300;

    /**
     * Name of the HTTP header containing the HMAC-SHA256 signature.
     * <p>
     * Official Cobre header: {@code event-signature}.
     */
    private String signatureHeader;

    /**
     * Name of the HTTP header containing the event timestamp.
     * <p>
     * Official Cobre header: {@code event-timestamp} (ISO 8601 UTC format).
     */
    private String timestampHeader;

    /**
     * Secret key used to generate and verify the HMAC-SHA256 signature.
     * <p>
     * Corresponds to the {@code event_signature_key} configured in the Cobre subscription.
     * MUST be provided via environment variable - NEVER hardcoded.
     * <p>
     * Example: {@code ${ENV_COBRE_WEBHOOK_SIGNATURE_KEY}}
     */
    private String secret;
}

