package com.tumipay.microservice.infrastructure.adapter.output.security;

import com.tumipay.microservice.domain.port.output.IApiKeyValidatorPort;
import com.tumipay.microservice.infrastructure.component.properties.InternalSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * InternalApiKeyValidatorAdapter
 * <p>
 * Implementation of {@link IApiKeyValidatorPort} that validates the API Key
 * using a constant-time comparison to prevent timing attacks.
 * <p>
 * The expected key is sourced from {@link InternalSecurityProperties},
 * which binds to {@code tumipay.security.internal.api-key}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@Component
@RequiredArgsConstructor
public class InternalApiKeyValidatorAdapter implements IApiKeyValidatorPort {

    private final InternalSecurityProperties properties;

    /**
     * {@inheritDoc}
     * <p>
     * Uses {@link MessageDigest#isEqual} for constant-time byte comparison,
     * mitigating timing-based side-channel attacks.
     * <p>
     * Additionally validates that the configured secret is not null or blank,
     * preventing unintentional access when the secret is not properly set.
     */
    @Override
    public boolean isValid(String apiKey) {

        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
            apiKey.getBytes(StandardCharsets.UTF_8),
            properties.getApiKey().getBytes(StandardCharsets.UTF_8)
        );
    }
}

