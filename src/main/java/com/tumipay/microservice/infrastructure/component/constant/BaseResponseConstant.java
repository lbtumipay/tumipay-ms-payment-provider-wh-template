package com.tumipay.microservice.infrastructure.component.constant;

import lombok.experimental.UtilityClass;

/**
 * BaseResponseConstants
 * <p>
 * BaseResponseConstants class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 26/12/2025
 */
@UtilityClass
public class BaseResponseConstant {

    public static final String SUCCESS_RESPONSE_CODE = "PROCESS_COMPLETED";
    public static final String SUCCESS_RESPONSE_STATUS = "SUCCESS";
    public static final String SUCCESS_RESPONSE_MESSAGE = "Operation completed successfully";


    public static final String INTERNAL_ERROR_RESPONSE_CODE = "INTERNAL_SERVER_ERROR";
    public static final String INTERNAL_ERROR_RESPONSE_STATUS = "ERROR";
    public static final String INTERNAL_ERROR_RESPONSE_MESSAGE = "Internal Server Error";

    public static final String FAILED_RESPONSE_STATUS = "FAILED";
}
