package com.tumipay.microservice.shared.util;

import lombok.experimental.UtilityClass;

import java.time.Duration;

/**
 * CommonDurationUtils
 * <p>
 * CommonDurationUtils class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 22/05/2026
 */
@UtilityClass
public class CommonDurationUtils {

    /**
     * Resolves a provider timeout value from an optional integer input.
     * <p>
     * If the input is non-null, it is interpreted as a timeout in milliseconds and converted to a {@link Duration}.
     * If the input is null, a default timeout of 30 seconds is returned.
     *
     * @param timeout the optional timeout value in milliseconds; may be null
     * @return a {@link Duration} representing the resolved timeout
     */
    public Duration resolveProviderTimeout(final Integer timeout) {
        return timeout != null ? Duration.ofMillis(timeout) : Duration.ofSeconds(30);
    }
}