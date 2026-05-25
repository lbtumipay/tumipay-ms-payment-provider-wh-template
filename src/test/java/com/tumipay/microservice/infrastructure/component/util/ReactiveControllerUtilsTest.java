package com.tumipay.microservice.infrastructure.component.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ReactiveControllerUtilsTest
 * <p>
 * ReactiveControllerUtilsTest class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 25/04/2026
 */
@DisplayName("ReactiveControllerUtils Unit Tests")
class ReactiveControllerUtilsTest {

    @Test
    @DisplayName("logOnSuccess should return a non-null consumer")
    void logOnSuccessShouldReturnNonNullConsumer() {
        Consumer<String> consumer = ReactiveControllerUtils.logOnSuccess("OPERATION");

        assertNotNull(consumer);
        consumer.accept("some-response");   // no exception expected
    }

    @Test
    @DisplayName("logOnError should return a non-null consumer")
    void logOnErrorShouldReturnNonNullConsumer() {
        Consumer<Throwable> consumer = ReactiveControllerUtils.logOnError("OPERATION");

        assertNotNull(consumer);
        consumer.accept(new RuntimeException("err"));   // no exception expected
    }
}
