package com.tumipay.microservice.infrastructure.component.properties;

import com.tumipay.microservice.shared.properties.WebClientProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebClientPropertiesTest
 * <p>
 * WebClientPropertiesTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 23/04/2026
 */
@DisplayName("WebClientProperties Unit Tests")
class WebClientPropertiesTest {

    @Test
    @DisplayName("Should expose expected configuration prefix")
    void shouldExposeExpectedConfigurationPrefix() {
        ConfigurationProperties annotation = WebClientProperties.class
            .getAnnotation(ConfigurationProperties.class);

        assertNotNull(annotation);
        assertEquals("webclient", annotation.prefix());
    }

    @Test
    @DisplayName("Should initialize nested sections by default")
    void shouldInitializeNestedSectionsByDefault() {
        WebClientProperties properties = new WebClientProperties();

        assertNotNull(properties.getPool());
        assertNotNull(properties.getTimeout());
        assertNotNull(properties.getTcp());
    }

    @Test
    @DisplayName("Should set and get nested webclient fields")
    void shouldSetAndGetNestedWebclientFields() {
        WebClientProperties properties = new WebClientProperties();
        WebClientProperties.Pool pool = new WebClientProperties.Pool();
        WebClientProperties.Timeout timeout = new WebClientProperties.Timeout();
        WebClientProperties.Tcp tcp = new WebClientProperties.Tcp();

        pool.setMaxConnections(100);
        pool.setPendingAcquireTimeout(Duration.ofSeconds(30));
        pool.setMaxIdleTime(Duration.ofSeconds(20));
        pool.setMaxLifeTime(Duration.ofMinutes(5));
        pool.setEvictInBackground(Duration.ofSeconds(30));

        timeout.setConnect(Duration.ofSeconds(5));
        timeout.setResponse(Duration.ofSeconds(30));

        tcp.setKeepAlive(true);
        properties.setPool(pool);
        properties.setTimeout(timeout);
        properties.setTcp(tcp);

        assertEquals(100, properties.getPool().getMaxConnections());
        assertEquals(Duration.ofSeconds(30), properties.getPool().getPendingAcquireTimeout());
        assertEquals(Duration.ofSeconds(20), properties.getPool().getMaxIdleTime());
        assertEquals(Duration.ofMinutes(5), properties.getPool().getMaxLifeTime());
        assertEquals(Duration.ofSeconds(30), properties.getPool().getEvictInBackground());
        assertEquals(Duration.ofSeconds(5), properties.getTimeout().getConnect());
        assertEquals(Duration.ofSeconds(30), properties.getTimeout().getResponse());
        assertTrue(properties.getTcp().isKeepAlive());
    }
}