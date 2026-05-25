package com.tumipay.microservice.infrastructure.component.config;

import com.tumipay.microservice.infrastructure.component.properties.MicroServiceFlywayProperties;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.r2dbc.autoconfigure.R2dbcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FlywayDataSourceConfig
 * <p>
 * FlywayDataSourceConfig class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/03/2026
 */
@Configuration
@EnableConfigurationProperties({
    R2dbcProperties.class,
    MicroServiceFlywayProperties.class
})
public class FlywayDataSourceConfig {

    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Flyway flyway(MicroServiceFlywayProperties flywayProperties, R2dbcProperties r2dbcProperties) {

        final String[] configuredLocations = flywayProperties.getLocations();
        final String[] locations = (configuredLocations == null || configuredLocations.length == 0)
            ? new String[] { "classpath:db/migration" }
            : configuredLocations.clone();

        return Flyway.configure()
            .dataSource(
                flywayProperties.getUrl(),
                r2dbcProperties.getUsername(),
                r2dbcProperties.getPassword()
            )
            .locations(locations)
            .baselineOnMigrate(true)
            .load();
    }
}