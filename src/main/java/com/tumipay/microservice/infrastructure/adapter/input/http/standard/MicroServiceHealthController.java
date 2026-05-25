package com.tumipay.microservice.infrastructure.adapter.input.http.standard;

import com.tumipay.microservice.infrastructure.adapter.input.http.standard.response.MicroServiceInfoResponse;
import com.tumipay.microservice.infrastructure.component.constant.CommonLoggerConstants;
import com.tumipay.microservice.infrastructure.component.dto.BaseApiResponse;
import com.tumipay.microservice.infrastructure.component.properties.MicroServiceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringBootVersion;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

/**
 * MicroServiceController
 * <p>
 * REST controller that exposes microservice runtime metadata and health information.
 * Handles incoming HTTP GET requests to {@code /v1/microservice/info} and returns
 * a snapshot of the service identity and runtime environment, built from
 * {@link MicroServiceProperties} configuration and JVM/framework system properties.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/02/2026
 */
@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/actuator")
public class MicroServiceHealthController {

    private final MicroServiceProperties microserviceInfo;

    /**
     * Returns a runtime snapshot of the microservice identity and environment metadata
     * for the liveness probe endpoint.
     * <p>
     * {@code GET /actuator/health/liveness}
     * <p>
     * This probe is used by Kubernetes and container orchestration platforms to determine
     * if the service instance should remain running. It returns microservice metadata including
     * name, version, environment, Java version, Spring Boot version, and current timestamp.
     * <p>
     * The response is assembled from:
     * <ul>
     *   <li>{@link MicroServiceProperties}: name, description, version, and environment</li>
     *   <li>JVM system property: java.version</li>
     *   <li>Spring Boot framework version</li>
     *   <li>Current server timestamp</li>
     * </ul>
     * If an unexpected error occurs, the method recovers and returns an HTTP {@code 500 Internal Server Error} response.
     *
     * @return a {@link Mono} emitting a {@link ResponseEntity} with {@link BaseApiResponse}
     *         containing {@link MicroServiceInfoResponse}, or an HTTP 500 error if operation fails
     */
    @GetMapping(path = "/health/liveness", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BaseApiResponse<MicroServiceInfoResponse>>> getMicroserviceLiveness() {
        return getMicroserviceHealthInfo("getMicroserviceLiveness");
    }

    /**
     * Returns a runtime snapshot of the microservice identity and environment metadata
     * for the readiness probe endpoint.
     * <p>
     * {@code GET /actuator/health/readiness}
     * <p>
     * This probe is used by Kubernetes and container orchestration platforms to determine
     * if the service instance is ready to accept traffic. It returns microservice metadata including
     * name, version, environment, Java version, Spring Boot version, and current timestamp.
     * <p>
     * The response is assembled from:
     * <ul>
     *   <li>{@link MicroServiceProperties}: name, description, version, and environment</li>
     *   <li>JVM system property: java.version</li>
     *   <li>Spring Boot framework version</li>
     *   <li>Current server timestamp</li>
     * </ul>
     * If an unexpected error occurs, the method recovers and returns an HTTP {@code 500 Internal Server Error} response.
     *
     * @return a {@link Mono} emitting a {@link ResponseEntity} with {@link BaseApiResponse}
     *         containing {@link MicroServiceInfoResponse}, or an HTTP 500 error if operation fails
     */
    @GetMapping(path = "/health/readiness", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BaseApiResponse<MicroServiceInfoResponse>>> getMicroserviceReadiness() {
        return getMicroserviceHealthInfo("getMicroserviceReadiness");
    }

    /**
     * Returns a runtime snapshot of the microservice identity and environment metadata
     * for the readiness probe endpoint.
     * <p>
     * {@code GET /actuator/health}
     * <p>
     * This probe is used by Kubernetes and container orchestration platforms to determine
     * if the service instance is ready to accept traffic. It returns microservice metadata including
     * name, version, environment, Java version, Spring Boot version, and current timestamp.
     * <p>
     * The response is assembled from:
     * <ul>
     *   <li>{@link MicroServiceProperties}: name, description, version, and environment</li>
     *   <li>JVM system property: java.version</li>
     *   <li>Spring Boot framework version</li>
     *   <li>Current server timestamp</li>
     * </ul>
     * If an unexpected error occurs, the method recovers and returns an HTTP {@code 500 Internal Server Error} response.
     *
     * @return a {@link Mono} emitting a {@link ResponseEntity} with {@link BaseApiResponse}
     *         containing {@link MicroServiceInfoResponse}, or an HTTP 500 error if operation fails
     */
    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BaseApiResponse<MicroServiceInfoResponse>>> getMicroserviceHealth() {
        return getMicroserviceHealthInfo("getMicroserviceReadiness");
    }

    /**
     * Builds and returns microservice health information as a reactive Mono response.
     * <p>
     * This private method encapsulates the common logic for both liveness and readiness probes.
     * It assembles the microservice metadata from configuration and system properties, handles
     * errors gracefully, and logs both success and error outcomes.
     *
     * @param methodName the name of the calling method (used for logging purposes)
     * @return a {@link Mono} emitting a {@link ResponseEntity} with the microservice health info
     *         or an HTTP 500 error response if an exception occurs
     */
    private Mono<ResponseEntity<BaseApiResponse<MicroServiceInfoResponse>>> getMicroserviceHealthInfo(String methodName) {
        return Mono.fromSupplier(() -> ResponseEntity.ok(
                BaseApiResponse.success(
                    MicroServiceInfoResponse.builder()
                        .serviceName(microserviceInfo.getName())
                        .serviceDescription(microserviceInfo.getDescription())
                        .version(microserviceInfo.getVersion())
                        .environment(microserviceInfo.getEnvironment())
                        .javaVersion(System.getProperty("java.version"))
                        .springBootVersion(SpringBootVersion.getVersion())
                        .timestamp(LocalDateTime.now())
                        .build()
                )
            ))
            .onErrorResume(e -> {
                log.error("Error occurred while fetching service info, error: {}", e.getMessage());
                return Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseApiResponse.internalError(null))
                );
            })
            .doOnSuccess(success -> log.debug(CommonLoggerConstants.COMMON_SUCCESS_PROCESS_LOG_FORMAT, methodName, success))
            .doOnError(throwable -> log.error(CommonLoggerConstants.COMMON_ERROR_PROCESS_LOG_FORMAT, methodName, throwable.getMessage()));
    }
}
