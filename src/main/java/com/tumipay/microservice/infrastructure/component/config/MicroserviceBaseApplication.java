package com.tumipay.microservice.infrastructure.component.config;

import com.tumipay.microservice.infrastructure.component.properties.MicroServiceProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * MicroserviceBaseApplication
 * <p>
 * MicroserviceBaseApplication class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 11/02/2026
 */
@Log4j2
@Configuration
@EnableConfigurationProperties({MicroServiceProperties.class})
@EnableAutoConfiguration
public abstract class MicroserviceBaseApplication implements ApplicationListener<ContextRefreshedEvent> {

    private final MicroServiceProperties microServiceProperties;

    public MicroserviceBaseApplication(final MicroServiceProperties microServiceProperties) {
        this.microServiceProperties = microServiceProperties;
    }

    public void onApplicationEvent(final ContextRefreshedEvent event) {

        log.info("**************************************************************************************");
        log.info("Microservices Name: {}", this.microServiceProperties.getName());
        log.info("Port: {}", this.microServiceProperties.getPort());
        log.info("Version: {}", this.microServiceProperties.getVersion());
        log.info("Context: {}", this.microServiceProperties.getContextPath());
        log.info("Logs Path: {}", this.microServiceProperties.getLogsPath());
        log.info("Spring Version: {}", this.microServiceProperties.getSpringVersion());
        log.info("Microservice Successfully Loaded");
        log.info("**************************************************************************************");
    }
}