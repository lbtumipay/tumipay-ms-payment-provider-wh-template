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
@RequestMapping("/v1/microservice")
public class MicroServiceController {

    private final MicroServiceProperties microserviceInfo;

    /**
     * Returns a runtime snapshot of the microservice identity and environment metadata.
     * <p>
     * {@code GET /v1/microservice/info}
     * <p>
     * The response is assembled from the following sources:
     * <ul>
     *   <li>{@link MicroServiceProperties}: {@code name}, {@code description}, {@code version} and {@code environment}.</li>
     *   <li>JVM system property {@code java.version}.</li>
     *   <li>Spring Boot framework version via {@code SpringBootVersion.getVersion()}.</li>
     *   <li>Current server timestamp ({@code LocalDateTime.now()}).</li>
     * </ul>
     * If an unexpected error occurs during assembly, the method recovers and returns
     * an HTTP {@code 500 Internal Server Error} response.
     *
     * @return {@link Mono} emitting a {@link ResponseEntity} wrapping a
     *         {@link BaseApiResponse} with the {@link MicroServiceInfoResponse},
     *         or an HTTP {@code 500} error response body if the operation fails.
     */
    @GetMapping(path = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BaseApiResponse<MicroServiceInfoResponse>>> getMicroserviceInfo() {

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
            .doOnSuccess(success -> log.debug(CommonLoggerConstants.COMMON_SUCCESS_PROCESS_LOG_FORMAT, "getMicroserviceInfo", success))
            .doOnError(throwable -> log.error(CommonLoggerConstants.COMMON_ERROR_PROCESS_LOG_FORMAT, "createEventData", throwable.getMessage()));
    }
}
