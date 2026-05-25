package com.tumipay.microservice.infrastructure.component.constant;

import lombok.experimental.UtilityClass;

/**
 * WebhookWorkerConstant
 * <p>
 * Constants for the Webhook Worker scheduler and Claim-Batch pattern configuration.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 10/04/2026
 */
@UtilityClass
public class WebhookWorkerConstant {

    // Scheduler timing constants (SpEL expressions — igual que PaymentProviderConstant)
    public static final String SCHEDULER_WEBHOOK_WORKER_FIXED_DELAY_MS =
        "${tumipay.schedulers.scheduler-webhook-dispatcher.scheduler-fixed-delay-ms:30000}";

    public static final String SCHEDULER_WEBHOOK_WORKER_INITIAL_DELAY_MS =
        "${tumipay.schedulers.scheduler-webhook-dispatcher.scheduler-initial-delay-ms:15000}";

    // Worker defaults
    public static final int DEFAULT_BATCH_SIZE       = 10;
    public static final int DEFAULT_MAX_RETRY_COUNT  = 5;
    public static final int DEFAULT_CONCURRENCY      = 10;

    // Retry backoff interval in seconds (default value, can be overridden by properties)
    public static final int RETRY_BACKOFF_SECONDS    = 30;

    // Timeout to detect hung-up workers (minutes)
    public static final int WORKER_TIMEOUT_MINUTES   = 5;

    // Timeout to detect stuck transaction validation workers (minutes)
    public static final int WORKER_VALIDATING_TIMEOUT_MINUTES   = 10;

    // Error codes
    public static final String ERROR_CODE_PROCESSING_FAILED = "WEBHOOK_PROCESSING_FAILED";
    public static final String ERROR_CODE_TIMEOUT           = "WEBHOOK_WORKER_TIMEOUT";
    public static final String ERROR_CODE_UNKNOWN           = "UNKNOWN_ERROR";
}

