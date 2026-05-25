package com.tumipay.microservice.shared.properties;

import com.tumipay.microservice.infrastructure.component.constant.WebhookReceiverConstant;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * WebhookReceiverProperties
 * <p>
 * Externalized configuration properties for the Webhook Receiver scheduler.
 * Bound from the {@code tumipay.webhook-receiver} prefix in application YAML.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-16
 */
@Data
@Validated
@ConfigurationProperties(prefix = "tumipay.webhook-receiver")
public class WebhookReceiverProperties {

    /**
     * Enables or disables the receiver scheduler.
     */
    private boolean enabled = true;

    /**
     * Maximum number of RECEIVED events to process per cycle.
     */
    @Min(1)
    private Integer batchSize = WebhookReceiverConstant.DEFAULT_BATCH_SIZE;
}

