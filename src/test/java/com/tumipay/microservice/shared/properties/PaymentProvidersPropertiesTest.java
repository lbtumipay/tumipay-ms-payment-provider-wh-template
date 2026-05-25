package com.tumipay.microservice.shared.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * PaymentProvidersPropertiesTest
 * <p>
 * PaymentProvidersPropertiesTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("PaymentProvidersProperties Unit Tests")
class PaymentProvidersPropertiesTest {

    @Test
    @DisplayName("Should expose expected configuration prefix")
    void shouldExposeExpectedConfigurationPrefix() {
        ConfigurationProperties annotation = PaymentProvidersProperties.class
            .getAnnotation(ConfigurationProperties.class);

        assertNotNull(annotation);
        assertEquals("tumipay.payment-provider", annotation.prefix());
    }

    @Test
    @DisplayName("Should set and get nested payment provider fields")
    void shouldSetAndGetNestedPaymentProviderFields() {
        PaymentProvidersProperties properties = new PaymentProvidersProperties();
        PaymentProvidersProperties.Security security = new PaymentProvidersProperties.Security();
        PaymentProvidersProperties.SecurityEndpoints securityEndpoints = new PaymentProvidersProperties.SecurityEndpoints();
        PaymentProvidersProperties.Integration integration = new PaymentProvidersProperties.Integration();
        PaymentProvidersProperties.IntegrationEndpoints integrationEndpoints = new PaymentProvidersProperties.IntegrationEndpoints();
        PaymentProvidersProperties.Authorization authorization = new PaymentProvidersProperties.Authorization();

        securityEndpoints.setGenerateTokenPath("/oauth/token");
        securityEndpoints.setRefreshTokenPath("/oauth/refresh");
        security.setBaseUrl("https://auth.provider.example.com");
        security.setUserId("cli_test_user");
        security.setSecret("test_secret_value");
        security.setEndpoints(securityEndpoints);

        integrationEndpoints.setPayInTransactionPath("/v1/payins");
        integrationEndpoints.setPayOutTransactionPath("/v1/payouts");
        integrationEndpoints.setGetTransactionPath("/v1/transactions/{id}");
        integration.setBaseUrl("https://api.provider.example.com");
        integration.setEndpoints(integrationEndpoints);

        authorization.setExpirationTimeMs(1800000L);
        authorization.setRefreshThresholdMs(600000L);

        properties.setCode("TUMIPAY_PROVIDER");
        properties.setName("TumiPay Provider");
        properties.setTimeout(30000);
        properties.setPayoutSourceAccountId("acc_source_001");
        properties.setSecurity(security);
        properties.setIntegration(integration);
        properties.setAuthorization(authorization);

        assertEquals("TUMIPAY_PROVIDER", properties.getCode());
        assertEquals("TumiPay Provider", properties.getName());
        assertEquals(30000, properties.getTimeout());
        assertEquals("acc_source_001", properties.getPayoutSourceAccountId());
        assertEquals("https://auth.provider.example.com", properties.getSecurity().getBaseUrl());
        assertEquals("cli_test_user", properties.getSecurity().getUserId());
        assertEquals("test_secret_value", properties.getSecurity().getSecret());
        assertEquals("/oauth/token", properties.getSecurity().getEndpoints().getGenerateTokenPath());
        assertEquals("/oauth/refresh", properties.getSecurity().getEndpoints().getRefreshTokenPath());
        assertEquals("https://api.provider.example.com", properties.getIntegration().getBaseUrl());
        assertEquals("/v1/payins", properties.getIntegration().getEndpoints().getPayInTransactionPath());
        assertEquals("/v1/payouts", properties.getIntegration().getEndpoints().getPayOutTransactionPath());
        assertEquals("/v1/transactions/{id}", properties.getIntegration().getEndpoints().getGetTransactionPath());
        assertEquals(1800000L, properties.getAuthorization().getExpirationTimeMs());
        assertEquals(600000L, properties.getAuthorization().getRefreshThresholdMs());
    }
}
