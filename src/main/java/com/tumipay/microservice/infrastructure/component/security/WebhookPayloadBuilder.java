package com.tumipay.microservice.infrastructure.component.security;

import org.springframework.stereotype.Component;

/**
 * Builds the canonical payload for Cobre webhook signature verification.
 * <p>
 * Canonical format (per Cobre documentation):
 * <pre>event-timestamp + "." + rawBody</pre>
 * Example: {@code 2025-02-03T22:20:24Z.{"id":"ev_abc","event_key":"accounts.balance.credit"}}
 */
@Component
public class WebhookPayloadBuilder {

    /**
     * Concatenates the event timestamp and raw body with a period separator.
     *
     * @param timestamp the exact value of the {@code event-timestamp} header
     * @param rawBody   the exact request body as received (no modification allowed)
     * @return the canonical payload string ready for HMAC computation
     */
    public String build(String timestamp, String rawBody) {
        return timestamp + "." + rawBody;
    }
}
