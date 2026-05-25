package com.tumipay.microservice.domain.port.output;

/**
 * IApiKeyValidatorPort
 * <p>
 * Domain port for internal API Key validation.
 * <p>
 * Decouples the validation logic from the infrastructure layer,
 * following hexagonal architecture principles.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
public interface IApiKeyValidatorPort {

    /**
     * Validates whether the provided API Key is legitimate.
     *
     * @param apiKey the value extracted from the {@code X-Api-Key} HTTP header.
     * @return {@code true} if the key matches the configured secret; {@code false} otherwise.
     */
    boolean isValid(String apiKey);
}

