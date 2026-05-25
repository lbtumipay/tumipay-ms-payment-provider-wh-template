package com.tumipay.microservice.infrastructure.component.constant;

import lombok.experimental.UtilityClass;

/**
 * PaymentProviderConstant
 * <p>
 * PaymentProviderConstant class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 28/03/2026
 */
@UtilityClass
public class PaymentProviderConstant {

    public static final String SCHEDULER_AUTHORIZATION_REFRESH_FIXED_DELAY_MS =
        "${tumipay.schedulers.scheduler-authorization-refresh.scheduler-fixed-delay-ms:300000}";

    public static final String SCHEDULER_AUTHORIZATION_REFRESH_INITIAL_DELAY_MS =
        "${tumipay.schedulers.scheduler-authorization-refresh.scheduler-initial-delay-ms:10000}";

    public static final long DEFAULT_THRESHOLD_SECONDS = 0L;
}