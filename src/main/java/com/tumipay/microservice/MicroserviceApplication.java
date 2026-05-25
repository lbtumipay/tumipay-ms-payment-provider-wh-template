package com.tumipay.microservice;

import com.tumipay.microservice.infrastructure.component.config.MicroserviceBaseApplication;
import com.tumipay.microservice.infrastructure.component.properties.MicroServiceProperties;
import com.tumipay.microservice.infrastructure.component.util.ContextUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MicroserviceApplication
 * <p>
 * MicroserviceApplication main bootstrap class.
 * <p>
 * Entry point of the microservice built on Spring Boot framework.
 * This class is responsible for:
 * <ul>
 *     <li>Bootstrapping the Spring Application Context.</li>
 *     <li>Enabling reactive database repositories (R2DBC).</li>
 *     <li>Registering externalized configuration properties.</li>
 *     <li>Delegating common initialization behavior to {@link MicroserviceBaseApplication}.</li>
 * </ul>
 * <p>
 * Architectural Responsibilities:
 * <ul>
 *     <li>Acts as the primary configuration class annotated with {@code @SpringBootApplication}.</li>
 *     <li>Enables reactive persistence support via {@code @EnableR2dbcRepositories}.</li>
 *     <li>Binds application configuration properties defined in {@link MicroServiceProperties}.</li>
 *     <li>Ensures centralized and standardized initialization inherited from the base application layer.</li>
 * </ul>
 * <p>
 * Reactive Stack Considerations:
 * <ul>
 *     <li>Uses R2DBC (Reactive Relational Database Connectivity).</li>
 *     <li>Designed for non-blocking I/O and high-concurrency environments.</li>
 *     <li>Suitable for event-driven and reactive microservices architectures.</li>
 * </ul>
 * <p>
 * Initialization Flow:
 * <ol>
 *     <li>The {@code main} method invokes {@link SpringApplication#run(Class, String...)}.</li>
 *     <li>Spring Boot performs component scanning and auto-configuration.</li>
 *     <li>Configuration properties are bound to {@link MicroServiceProperties}.</li>
 *     <li>Base initialization logic is executed via {@link MicroserviceBaseApplication} constructor.</li>
 * </ol>
 * <p>
 * Security and Governance:
 * <ul>
 *     <li>Follows TumiPay microservice architectural standards.</li>
 *     <li>Centralizes environment-driven configuration management.</li>
 *     <li>Ensures consistent bootstrapping across the microservice ecosystem.</li>
 * </ul>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS
 * AND PROCEDURE AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 11/02/2026
 */
@SpringBootApplication(
    scanBasePackages = "com.tumipay.microservice"
)
@EnableScheduling
@ConfigurationPropertiesScan({
    "com.tumipay.microservice.infrastructure.component.properties",
    "com.tumipay.microservice.shared.properties"
})
public class MicroserviceApplication extends MicroserviceBaseApplication {

    /**
     * Constructs the MicroserviceApplication.
     * <p>
     * Delegates initialization logic to {@link MicroserviceBaseApplication},
     * ensuring standardized configuration and shared microservice behavior.
     * @param microServiceProperties configuration properties bound from
     *                               application.yml or application.properties.
     */
    public MicroserviceApplication(MicroServiceProperties microServiceProperties) {
        super(microServiceProperties);
    }

    /**
     * Main entry point of the microservice.
     * <p>
     * Triggers the Spring Boot auto-configuration mechanism and
     * starts the embedded application server.
     * @param args runtime arguments passed during application startup.
     */
    public static void main(String[] args) {
        ContextUtils.enableAutomaticContextPropagation();
        SpringApplication.run(MicroserviceApplication.class, args);
    }
}