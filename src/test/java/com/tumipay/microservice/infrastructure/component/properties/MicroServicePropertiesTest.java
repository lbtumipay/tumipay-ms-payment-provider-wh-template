package com.tumipay.microservice.infrastructure.component.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MicroServicePropertiesTest
 * <p>
 * MicroServicePropertiesTest test class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("MicroServiceProperties Unit Tests")
class MicroServicePropertiesTest {

    @Test
    @DisplayName("Should expose expected configuration prefix")
    void shouldExposeExpectedConfigurationPrefix() {
        ConfigurationProperties annotation = MicroServiceProperties.class
            .getAnnotation(ConfigurationProperties.class);

        assertNotNull(annotation);
        assertEquals("tumipay.microservice", annotation.prefix());
    }

    @Test
    @DisplayName("Should set and get all microservice fields")
    void shouldSetAndGetAllMicroserviceFields() {

        MicroServiceProperties properties = new MicroServiceProperties();

        properties.setName("payment-adapter");
        properties.setDescription("TumiPay Payment Adapter");
        properties.setVersion("1.0.0");
        properties.setEnvironment("staging");
        properties.setStartupLogEnabled(true);
        properties.setStartupLogLevel("INFO");
        properties.setSpringVersion("6.2.x");
        properties.setLogsPath("/logs");
        properties.setPort(8000);
        properties.setContextPath("/tp/payment/adapter");

        assertEquals("payment-adapter", properties.getName());
        assertEquals("TumiPay Payment Adapter", properties.getDescription());
        assertEquals("1.0.0", properties.getVersion());
        assertEquals("staging", properties.getEnvironment());
        assertTrue(properties.isStartupLogEnabled());
        assertEquals("INFO", properties.getStartupLogLevel());
        assertEquals("6.2.x", properties.getSpringVersion());
        assertEquals("/logs", properties.getLogsPath());
        assertEquals(8000, properties.getPort());
        assertEquals("/tp/payment/adapter", properties.getContextPath());
    }
}