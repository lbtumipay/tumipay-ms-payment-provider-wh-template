package com.tumipay.microservice.infrastructure.component.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MicroServiceFlywayPropertiesTest
 * <p>
 * MicroServiceFlywayPropertiesTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 23/04/2026
 */
@DisplayName("MicroServiceFlywayProperties Unit Tests")
class MicroServiceFlywayPropertiesTest {

    @Test
    @DisplayName("Should expose expected configuration prefix")
    void shouldExposeExpectedConfigurationPrefix() {
        ConfigurationProperties annotation = MicroServiceFlywayProperties.class
            .getAnnotation(ConfigurationProperties.class);

        assertNotNull(annotation);
        assertEquals("spring.flyway", annotation.prefix());
    }

    @Test
    @DisplayName("Should set and get flyway fields")
    void shouldSetAndGetFlywayFields() {

        MicroServiceFlywayProperties properties = new MicroServiceFlywayProperties();

        properties.setUrl("jdbc:postgresql://localhost:5432/postgres");
        properties.setEnabled(true);
        properties.setBaselineOnMigrate(true);
        properties.setBaselineVersion("1");
        properties.setBaselineDescription("baseline");
        properties.setOutOfOrder(false);
        properties.setCleanDisabled(true);
        properties.setValidateOnMigrate(true);
        properties.setIgnoreMissingMigrations(false);
        properties.setIgnoreIgnoredMigrations(false);
        properties.setIgnorePendingMigrations(false);
        properties.setIgnoreFutureMigrations(true);
        properties.setLocations(new String[]{"classpath:db/migration"});

        assertEquals("jdbc:postgresql://localhost:5432/postgres", properties.getUrl());
        assertTrue(properties.isEnabled());
        assertTrue(properties.isBaselineOnMigrate());
        assertEquals("1", properties.getBaselineVersion());
        assertEquals("baseline", properties.getBaselineDescription());
        assertFalse(properties.isOutOfOrder());
        assertTrue(properties.isCleanDisabled());
        assertTrue(properties.isValidateOnMigrate());
        assertFalse(properties.isIgnoreMissingMigrations());
        assertFalse(properties.isIgnoreIgnoredMigrations());
        assertFalse(properties.isIgnorePendingMigrations());
        assertTrue(properties.isIgnoreFutureMigrations());
        assertArrayEquals(new String[]{"classpath:db/migration"}, properties.getLocations());
    }
}