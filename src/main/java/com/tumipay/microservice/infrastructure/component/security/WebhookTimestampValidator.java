package com.tumipay.microservice.infrastructure.component.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Validates that the {@code event-timestamp} header of a Cobre webhook is within
 * the allowed tolerance window to prevent replay attacks.
 * <p>
 * Cobre timestamp format: ISO 8601 UTC (e.g. {@code 2025-02-03T22:20:24Z}).
 * Default tolerance: 300 seconds (configurable via {@code tumipay.payment-provider.webhook-signature.tolerance-seconds}).
 */
@Log4j2
@Component
public class WebhookTimestampValidator {

    /**
     * Returns {@code true} if the given timestamp string is within the tolerance window.
     * <p>
     * Validation logic:
     * <pre>Math.abs(Instant.now().getEpochSecond() - requestEpochSeconds) &lt;= toleranceSeconds</pre>
     *
     * @param timestamp        ISO 8601 UTC timestamp from the {@code event-timestamp} header
     * @param toleranceSeconds maximum allowed difference in seconds (typically 300)
     * @return {@code true} if timestamp is valid and within tolerance; {@code false} otherwise
     */
    public boolean isValid(String timestamp, int toleranceSeconds) {
        if (timestamp == null) {
            log.warn("[WEBHOOK-SIGNATURE] event-timestamp header is null");
            return false;
        }
        try {
            Instant requestInstant = Instant.parse(timestamp);
            long requestEpochSeconds = requestInstant.getEpochSecond();
            long nowEpochSeconds = Instant.now().getEpochSecond();
            return Math.abs(nowEpochSeconds - requestEpochSeconds) <= toleranceSeconds;
        } catch (DateTimeParseException e) {
            log.warn("[WEBHOOK-SIGNATURE] Unparseable event-timestamp header: {}", timestamp);
            return false;
        }
    }
}
