package com.tumipay.microservice.infrastructure.component.util;

import com.tumipay.microservice.infrastructure.component.constant.CommonLoggerConstants;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;

/**
 * ReactiveControllerUtils
 * <p>
 * ReactiveControllerUtils class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/03/2026
 */
@UtilityClass
@Log4j2
public class ReactiveControllerUtils {

    private static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";

    /**
     * Returns a success logger to be used in doOnSuccess.
     */
    public static <T> Consumer<T> logOnSuccess(String operation) {
        return response -> log.debug(
            CommonLoggerConstants.COMMON_SUCCESS_PROCESS_LOG_FORMAT,
            operation,
            response
        );
    }

    /**
     * Returns an error logger to be used in doOnError.
     */
    public static Consumer<Throwable> logOnError(String operation) {
        return throwable -> log.error(
            CommonLoggerConstants.COMMON_ERROR_PROCESS_LOG_FORMAT,
            operation,
            throwable.getMessage()
        );
    }
}