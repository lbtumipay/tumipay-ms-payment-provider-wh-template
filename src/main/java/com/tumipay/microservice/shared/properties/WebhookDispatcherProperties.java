package com.tumipay.microservice.shared.properties;

import com.tumipay.microservice.infrastructure.component.constant.WebhookWorkerConstant;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * WebhookWorkerProperties
 * <p>
 * Externalized configuration properties for the Webhook Worker scheduler.
 * Bound from the {@code tumipay.webhook-worker} prefix in application YAML.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 10/04/2026
 */
@Data
@Validated
@ConfigurationProperties(prefix = "tumipay.webhook-dispatcher")
public class WebhookDispatcherProperties {

    /**
     * Enables or disables the worker scheduler.
     */
    private boolean enabled = true;

    /**
     * Fixed worker ID. If null, it is auto-generated using hostname + PID.
     */
    private String workerId;

    /**
     * Maximum number of events to claim per cycle.
     */
    @Min(1)
    private Integer batchSize = WebhookWorkerConstant.DEFAULT_BATCH_SIZE;

    /**
     * Maximum number of retries before marking the event as FAILED.
     */
    @Min(0)
    private Integer maxRetryCount = WebhookWorkerConstant.DEFAULT_MAX_RETRY_COUNT;

    /**
     * Maximum number of events processed in parallel within the batch.
     */
    @Min(1)
    private Integer concurrency = WebhookWorkerConstant.DEFAULT_CONCURRENCY;

    /**
     * Seconds to wait between retries.
     */
    @Min(1)
    private Integer retryBackoffSeconds = WebhookWorkerConstant.RETRY_BACKOFF_SECONDS;

    /**
     * Minutes before considering a worker as hung (for recovery purposes).
     */
    @Min(1)
    private Integer workerTimeoutMinutes = WebhookWorkerConstant.WORKER_TIMEOUT_MINUTES;

    @Min(1)
    private Integer validatingTimeoutMinutes = WebhookWorkerConstant.WORKER_VALIDATING_TIMEOUT_MINUTES;
}

