package com.tumipay.microservice.infrastructure.component.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * MicroServiceFlywayProperties
 * <p>
 * Configuration properties for managing database schema migrations using Flyway.
 * <p>
 * These settings control how migrations are executed, validated, and handled
 * across different environments (development, staging, production).
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/03/2026
 */
@Data
@Validated
@ConfigurationProperties(prefix = "spring.flyway")
public class MicroServiceFlywayProperties {

    /**
     * JDBC URL used by Flyway to connect to the database.
     * <p>
     * This is required because Flyway operates over JDBC, even when the application
     * uses reactive drivers (e.g., R2DBC) at runtime.
     */
    private String url;

    /**
     * Enables or disables Flyway migrations execution at application startup.
     * <p>
     * Typically enabled in all environments except specific test scenarios.
     */
    private boolean enabled;

    /**
     * Automatically baselines an existing database when migrations are applied.
     * <p>
     * Required when integrating Flyway into a pre-existing schema.
     */
    private boolean baselineOnMigrate;

    /**
     * Version to tag an existing schema when baselining.
     * <p>
     * Migrations below this version will be ignored.
     */
    private String baselineVersion;

    /**
     * Description assigned to the baseline entry.
     * <p>
     * Useful for audit and traceability purposes.
     */
    private String baselineDescription;

    /**
     * Allows execution of migrations out of version order.
     * <p>
     * Useful in distributed development environments, but should be used with caution.
     */
    private boolean outOfOrder;

    /**
     * Disables the Flyway clean operation.
     * <p>
     * Strongly recommended to keep this enabled (true) in production environments
     * to prevent accidental data loss.
     */
    private boolean cleanDisabled;

    /**
     * Validates applied migrations against available scripts during startup.
     * <p>
     * Ensures schema integrity and prevents drift between environments.
     */
    private boolean validateOnMigrate;

    /**
     * Ignores missing migrations that were applied previously but no longer exist locally.
     * <p>
     * Useful when migration scripts are removed intentionally.
     */
    private boolean ignoreMissingMigrations;

    /**
     * Ignores migrations that are marked as ignored by Flyway.
     */
    private boolean ignoreIgnoredMigrations;

    /**
     * Ignores pending migrations during validation.
     * <p>
     * Useful in controlled rollout scenarios.
     */
    private boolean ignorePendingMigrations;

    /**
     * Ignores future migrations that exist in the database but not locally.
     * <p>
     * Common in multi-version deployments or rollback scenarios.
     */
    private boolean ignoreFutureMigrations;

    /**
     * List of Flyway migration locations.
     * <p>
     * Example: classpath:db/migration
     */
    private String[] locations;
}