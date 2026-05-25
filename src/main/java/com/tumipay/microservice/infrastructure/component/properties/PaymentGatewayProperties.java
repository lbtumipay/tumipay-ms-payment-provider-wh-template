package com.tumipay.microservice.infrastructure.component.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;
import java.io.Serializable;

/**
 * PaymentGatewayProperties
 * <p>
 * Configuration properties for TumiPay Payment Gateway integration.
 * <p>
 * This configuration defines connectivity, security, and operational parameters
 * required to interact with a specific TumiPay Payment Gateway.
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
@ConfigurationProperties(prefix = "tumipay.payment-gateway")
public class PaymentGatewayProperties {

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
     * Default timeout (in milliseconds) for TumiPay Payment Gateway API calls.
     * <p>
     * Example: 30000 = 30 seconds.
     * <p>
     * Ensures consistent timeout behavior across all integration operations.
     */
    private Integer timeout;

    /**
     * Gateway-specific endpoint paths.
     */
    private GatewayEndpoints endpoints;


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