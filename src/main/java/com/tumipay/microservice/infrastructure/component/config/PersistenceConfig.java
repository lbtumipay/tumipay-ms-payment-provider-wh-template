package com.tumipay.microservice.infrastructure.component.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * PersistenceConfig
 * <p>
 * PersistenceConfig class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 23/03/2026
 */
@Configuration
@EnableR2dbcRepositories(
    basePackages = "com.tumipay.microservice.infrastructure.adapter.output.persistence.repository"
)
@EntityScan(
    basePackages = "com.tumipay.microservice.infrastructure.adapter.output.persistence.entity"
)
public class PersistenceConfig {

}