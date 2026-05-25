package com.tumipay.microservice.shared.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.io.Serial;
import java.io.Serializable;

/**
 * PaymentProvidersProperties
 * <p>
 * Configuration properties for external payment provider integration.
 * <p>
 * This configuration defines connectivity, security, and operational parameters
 * required to interact with a specific payment provider.
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
@Validated
@ConfigurationProperties(prefix = "tumipay.payment-provider")
public class PaymentProvidersProperties {

    /**
     * Unique identifier of the payment provider.
     * <p>
     * Used for routing, logging, and multi-provider support.
     */
    @NotBlank
    private String code;

    /**
     * Human-readable name of the payment provider.
     */
    @NotBlank
    private String name;

    /**
     * Cobre source account id ({@code acc_…}) used as {@code source_id} for payout money movements.
     */
    private String payoutSourceAccountId;

    /**
     * Default timeout (in milliseconds) for provider API calls.
     * <p>
     * Example: 30000 = 30 seconds.
     * <p>
     * Ensures consistent timeout behavior across all integration operations.
     */
    private Integer timeout;
    private Security security;
    private Integration integration;
    private Authorization authorization;
    private Gateway gateway;

    // ===================== SECURITY =====================

    /**
     * Security configuration for authentication and token management.
     */
    @Data
    public static class Security implements Serializable {

        @Serial
        private static final long serialVersionUID = 7818090795385371573L;

        /**
         * Base URL for authentication endpoints (e.g., OAuth server).
         */
        @NotBlank
        private String baseUrl;

        /**
         * Provider API user identifier for authentication requests.
         * Must be supplied via environment variable — never hardcoded in production.
         */
        private String userId;

        /**
         * Provider API secret for authentication requests.
         * Must be supplied via environment variable — never hardcoded in production.
         */
        private String secret;

        /**
         * Security-related endpoints.
         */
        private SecurityEndpoints endpoints;
    }

    @Data
    public static class SecurityEndpoints implements Serializable {

        @Serial
        private static final long serialVersionUID = 7818090795385371573L;

        /**
         * Endpoint path for generating access tokens.
         * <p>
         * Example: /oauth/token
         */
        private String generateTokenPath;

        /**
         * Endpoint path for refreshing access tokens.
         * <p>
         * Example: /oauth/refresh
         */
        private String refreshTokenPath;
    }

    // ===================== INTEGRATION =====================

    /**
     * Integration configuration for transactional APIs.
     */
    @Data
    public static class Integration implements Serializable {

        @Serial
        private static final long serialVersionUID = 7818090795385371573L;

        /**
         * Base URL for transactional endpoints.
         */
        @NotBlank
        private String baseUrl;

        /**
         * Transaction-related endpoints.
         */
        private IntegrationEndpoints endpoints;
    }

    @Data
    public static class IntegrationEndpoints implements Serializable {

        @Serial
        private static final long serialVersionUID = 7818090795385371573L;

        /**
         * Endpoint path for PayIn transactions.
         * <p>
         * Example: /v1/payins
         */
        private String payInTransactionPath;

        /**
         * Endpoint path for PayOut transactions.
         * <p>
         * Example: /v1/payouts
         */
        private String payOutTransactionPath;

        /**
         * Endpoint path for querying transactions.
         * <p>
         * Example: /v1/transactions/{id}
         */
        private String getTransactionPath;

        /**
         * Cobre counterparty creation path (e.g. /v1/counterparties).
         */
        private String counterpartyPath;
    }

    // ===================== AUTHORIZATION =====================

    /**
     * Authorization lifecycle configuration.
     * <p>
     * Controls token expiration and refresh strategy.
     */
    @Data
    public static class Authorization implements Serializable {

        @Serial
        private static final long serialVersionUID = 7818090795385371573L;

        /**
         * Token expiration time in milliseconds.
         * <p>
         * Used to determine token validity duration.
         */
        private Long expirationTimeMs;

        /**
         * Time threshold (in milliseconds) before expiration to trigger token refresh.
         * <p>
         * Helps prevent token expiration during active operations.
         */
        private Long refreshThresholdMs;
    }

    // ===================== GATEWAY =====================

    /**
     * Configuration for dispatching normalized webhook events to the TumiPay Payment Gateway.
     * <p>
     * Defines connectivity parameters required to forward provider webhook events
     * using the standard TumiPay Gateway webhook contract.
     */
    @Data
    public static class Gateway implements Serializable {

        @Serial
        private static final long serialVersionUID = 7818090795385371573L;

        /**
         * Base URL of the TumiPay Payment Gateway.
         * <p>
         * Example: https://api.tumipay.com/gateway
         */
        private String baseUrl;

        /**
         * API Key for authenticating outbound webhook dispatch requests to the Payment Gateway.
         * <p>
         * MUST be provided via environment variable — never hardcoded.
         */
        private String apiKey;

        /**
         * Gateway-specific endpoint paths.
         */
        private GatewayEndpoints endpoints;
    }

    /**
     * Gateway endpoint paths configuration.
     */
    @Data
    public static class GatewayEndpoints implements Serializable {

        @Serial
        private static final long serialVersionUID = 7818090795385371573L;

        /**
         * Endpoint path for dispatching normalized webhook events.
         * <p>
         * Example: /v1/webhook/payment-event
         */
        private String webhookEventPath;
    }
}