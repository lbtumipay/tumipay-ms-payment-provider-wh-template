package com.tumipay.microservice.infrastructure.component.properties;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * WebClientProperties
 * <p>
 * Configuration properties for WebClient and underlying HTTP client behavior.
 * <p>
 * These settings control connection pooling, timeouts, and TCP-level options,
 * allowing fine-tuned performance and resource management when interacting
 * with external services.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 3/03/2026
 */
@Data
@ToString
@ConfigurationProperties(prefix = "webclient")
public class WebClientProperties {

    /**
     * Connection pool configuration.
     */
    private Pool pool = new Pool();

    /**
     * Timeout configuration for HTTP operations.
     */
    private Timeout timeout = new Timeout();

    /**
     * TCP-level configuration.
     */
    private Tcp tcp = new Tcp();

    // ===================== POOL =====================

    /**
     * Controls connection pooling behavior.
     * <p>
     * Important for performance and resource optimization under high concurrency.
     */
    @Data
    @ToString
    public static class Pool {

        /**
         * Maximum number of active connections in the pool.
         */
        private int maxConnections;

        /**
         * Maximum time to wait when acquiring a connection from the pool.
         * <p>
         * If exceeded, the request fails with a timeout.
         */
        private Duration pendingAcquireTimeout;

        /**
         * Maximum idle time for a connection before being closed.
         */
        private Duration maxIdleTime;

        /**
         * Maximum lifetime of a connection regardless of activity.
         * <p>
         * Helps prevent stale or long-lived connections.
         */
        private Duration maxLifeTime;

        /**
         * Interval for background eviction of idle/expired connections.
         */
        private Duration evictInBackground;
    }

    // ===================== TIMEOUT =====================

    /**
     * Defines timeout behavior for HTTP requests.
     */
    @Data
    @ToString
    public static class Timeout {

        /**
         * Timeout for establishing a TCP connection.
         */
        private Duration connect;

        /**
         * Maximum time to wait for a response after the request is sent.
         */
        private Duration response;
    }

    // ===================== TCP =====================

    /**
     * TCP-level socket configuration.
     */
    @Data
    @ToString
    public static class Tcp {

        /**
         * Enables TCP keep-alive.
         * <p>
         * Keeps connections open and reusable, improving performance
         * for frequent calls to the same host.
         */
        private boolean keepAlive;
    }
}