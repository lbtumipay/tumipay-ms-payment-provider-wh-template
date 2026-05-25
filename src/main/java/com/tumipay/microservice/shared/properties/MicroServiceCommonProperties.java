package com.tumipay.microservice.shared.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MicroServiceCommonProperties
 * <p>
 * MicroServiceCommonProperties class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 13/02/2026
 */
@Data
@ConfigurationProperties(prefix = "tumipay.common-configuration")
public class MicroServiceCommonProperties {

    /**
     * Defines the default scale (number of decimal places) for monetary amounts.
     * <p>
     * This setting ensures that all financial calculations and representations
     * adhere to a consistent level of precision across the microservice.
     * <p>
     * For example, a value of 2 would mean that all monetary amounts are rounded
     * to two decimal places, which is common for currencies like USD and EUR.
     */
    private Integer amountScale;

    /**
     * Specifies the rounding mode used for monetary amount calculations.
     * <p>
     * Common values include HALF_UP, HALF_DOWN, HALF_EVEN, CEILING, and FLOOR,
     * following the standard {@link java.math.RoundingMode} strategies.
     * <p>
     * This setting defines how decimal values are rounded during financial operations,
     * ensuring consistency, accuracy, and deterministic behavior across the platform.
     */
    private String amountRoundingMode;
}