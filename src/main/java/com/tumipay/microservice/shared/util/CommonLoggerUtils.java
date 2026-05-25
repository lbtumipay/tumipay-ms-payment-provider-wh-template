package com.tumipay.microservice.shared.util;

import com.tumipay.microservice.infrastructure.component.constant.CommonLoggerConstants;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * CommonLoggerUtils
 * <p>
 * CommonLoggerUtils class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 25/03/2026
 */
@Log4j2
@UtilityClass
public class CommonLoggerUtils {

    /**
     * Returns a {@link Function} that wraps a {@link Mono} with structured process logging.
     * Logs a debug message on success and an error message on failure using the provided operation name.
     *
     * @param operationName the name of the operation to include in the log messages.
     * @param <T>           the type of the mono element.
     * @return a function that adds {@code doOnSuccess} and {@code doOnError} logging to the given mono.
     */
    public static <T> Function<Mono<T>, Mono<T>> withProcessLogging(String operationName) {
        return mono -> mono
            .doOnSuccess(success ->
                log.debug(CommonLoggerConstants.COMMON_SUCCESS_PROCESS_LOG_FORMAT, operationName, success)
            )
            .doOnError(error ->
                log.error(CommonLoggerConstants.COMMON_ERROR_PROCESS_LOG_FORMAT, operationName, error.getMessage())
            );
    }
}
