package com.tumipay.microservice.infrastructure.component.constant;

import lombok.experimental.UtilityClass;

/**
 * WebhookReceiverConstant
 * <p>
 * Constants for the Webhook Receiver scheduler configuration.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-16
 */
@UtilityClass
public class WebhookReceiverConstant {

    // Scheduler timing constants (SpEL expressions)
    public static final String SCHEDULER_WEBHOOK_RECEIVER_FIXED_DELAY_MS =
        "${tumipay.schedulers.scheduler-webhook-receiver.scheduler-fixed-delay-ms:15000}";

    public static final String SCHEDULER_WEBHOOK_RECEIVER_INITIAL_DELAY_MS =
        "${tumipay.schedulers.scheduler-webhook-receiver.scheduler-initial-delay-ms:5000}";

    // Worker defaults
    public static final int DEFAULT_BATCH_SIZE = 20;

    // Error codes
    public static final String ERROR_CODE_TRANSACTION_NOT_FOUND    = "RECEIVER_TRANSACTION_NOT_FOUND";
    public static final String ERROR_CODE_DESERIALIZATION_FAILED   = "RECEIVER_DESERIALIZATION_FAILED";
    public static final String ERROR_CODE_TRANSACTION_UPDATE_FAILED = "RECEIVER_TRANSACTION_UPDATE_FAILED";
    public static final String ERROR_CODE_WEBHOOK_UPDATE_FAILED    = "RECEIVER_WEBHOOK_UPDATE_FAILED";
    public static final String ERROR_CODE_PROCESSING_FAILED        = "RECEIVER_PROCESSING_FAILED";
}

