package com.tumipay.microservice.infrastructure.component.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MicroServiceProperties
 * <p>
 * Core configuration properties that define metadata, runtime behavior,
 * and operational settings of the microservice.
 * <p>
 * These properties are typically used for identification, logging,
 * environment configuration, and deployment standardization.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 13/02/2026
 */
@Data
@ConfigurationProperties(prefix = "tumipay.microservice")
public class MicroServiceProperties {

    // === Service metadata ===

    /**
     * Logical name of the microservice.
     * <p>
     * Used for identification across logs, monitoring systems, and integrations.
     */
    private String name;

    /**
     * Functional description of the microservice.
     * <p>
     * Provides context about the business capability implemented.
     */
    private String description;

    /**
     * Version of the microservice.
     * <p>
     * Typically aligned with release/versioning strategy (e.g., semantic versioning).
     */
    private String version;

    /**
     * Deployment environment (e.g., dev, qa, staging, prod).
     * <p>
     * Used to adapt behavior and configuration dynamically per environment.
     */
    private String environment;

    // === Logging & observability ===

    /**
     * Enables or disables startup logging.
     * <p>
     * When enabled, logs key configuration and environment details during application startup.
     */
    private boolean startupLogEnabled;

    /**
     * Log level used during startup logging (e.g., INFO, DEBUG).
     * <p>
     * Controls verbosity of initialization logs.
     */
    private String startupLogLevel;

    /**
     * Version of the Spring framework used by the microservice.
     * <p>
     * Useful for diagnostics, observability, and compatibility tracking.
     */
    private String springVersion;

    /**
     * Base path for storing application logs.
     * <p>
     * Can be used for file-based logging or external log aggregation.
     */
    private String logsPath;

    // === Runtime configuration ===

    /**
     * Port where the microservice runs.
     * <p>
     * Typically aligned with server.port but exposed here for centralized configuration.
     */
    private Integer port;

    /**
     * Base context path of the microservice.
     * <p>
     * Defines the root path for all exposed endpoints (e.g., /tp/payment/adapter).
     */
    private String contextPath;
}