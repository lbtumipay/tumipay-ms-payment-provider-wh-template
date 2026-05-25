package com.tumipay.microservice.infrastructure.component.constant;

import lombok.experimental.UtilityClass;

/**
 * CommonLoggerConstants
 * <p>
 * CommonLoggerConstants class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 12/03/2026
 */
@UtilityClass
public class CommonLoggerConstants {

    public static final String COMMON_SUCCESS_PROCESS_STRING_FORMAT = "Successfully process %s completed with result: %s";
    public static final String COMMON_ERROR_PROCESS_STRING_FORMAT = "Error process %s completed with result: %s";

    public static final String COMMON_SUCCESS_PROCESS_LOG_FORMAT = "Successfully process {} completed with result: {}";
    public static final String COMMON_ERROR_PROCESS_LOG_FORMAT = "Error process {} completed with result: {}";
}