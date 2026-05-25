package com.tumipay.microservice.shared.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MicroServiceCommonPropertiesTest2
 * <p>
 * MicroServiceCommonPropertiesTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 23/04/2026
 */
@DisplayName("MicroServiceCommonProperties Unit Tests")
class MicroServiceCommonPropertiesTest {

    @Test
    @DisplayName("Should expose expected configuration prefix")
    void shouldExposeExpectedConfigurationPrefix() {

        ConfigurationProperties annotation = MicroServiceCommonProperties.class
            .getAnnotation(ConfigurationProperties.class);

        assertNotNull(annotation);
        assertEquals("tumipay.common-configuration", annotation.prefix());
    }

    @Test
    @DisplayName("Should set and get amount fields")
    void shouldSetAndGetAmountFields() {

        MicroServiceCommonProperties properties = new MicroServiceCommonProperties();

        properties.setAmountScale(2);
        properties.setAmountRoundingMode("HALF_UP");

        assertEquals(2, properties.getAmountScale());
        assertEquals("HALF_UP", properties.getAmountRoundingMode());
    }

    @Test
    @DisplayName("Should keep null defaults when values are not assigned")
    void shouldKeepNullDefaultsWhenValuesAreNotAssigned() {

        MicroServiceCommonProperties properties = new MicroServiceCommonProperties();

        assertNull(properties.getAmountScale());
        assertNull(properties.getAmountRoundingMode());
    }
}