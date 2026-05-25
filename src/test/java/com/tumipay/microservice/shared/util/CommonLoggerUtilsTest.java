package com.tumipay.microservice.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CommonLoggerUtilsTest
 * <p>
 * CommonLoggerUtilsTest test class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@DisplayName("CommonLoggerUtils Unit Tests")
class CommonLoggerUtilsTest {

    @Test
    @DisplayName("withProcessLogging should return a non-null transformer")
    void withProcessLoggingShouldReturnNonNullTransformer() {
        Function<Mono<String>, Mono<String>> transformer =
            CommonLoggerUtils.withProcessLogging("TEST_OPERATION");

        assertNotNull(transformer);
    }

    @Test
    @DisplayName("withProcessLogging should propagate value on success")
    void withProcessLoggingShouldPropagateValueOnSuccess() {
        Function<Mono<String>, Mono<String>> transformer =
            CommonLoggerUtils.withProcessLogging("TEST_OPERATION");

        Mono<String> result = transformer.apply(Mono.just("ok"));

        StepVerifier.create(result)
            .expectNext("ok")
            .verifyComplete();
    }

    @Test
    @DisplayName("withProcessLogging should propagate error without swallowing")
    void withProcessLoggingShouldPropagateErrorWithoutSwallowing() {
        Function<Mono<String>, Mono<String>> transformer =
            CommonLoggerUtils.withProcessLogging("FAILED_OPERATION");

        Mono<String> result = transformer.apply(
            Mono.error(new RuntimeException("processing error"))
        );

        StepVerifier.create(result)
            .expectErrorMatches(e -> e instanceof RuntimeException
                && "processing error".equals(e.getMessage()))
            .verify();
    }
}

