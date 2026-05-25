package com.tumipay.microservice.infrastructure.adapter.input.scheduler;

import com.tumipay.microservice.domain.port.input.IWebhookReceiverUseCase;
import com.tumipay.microservice.infrastructure.component.constant.WebhookReceiverConstant;
import com.tumipay.microservice.infrastructure.component.properties.WebhookReceiverProperties;
import com.tumipay.microservice.shared.util.CommonIntegerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebhookReceiverScheduler
 * <p>
 * Scheduled component that drives the first stage of the webhook processing pipeline.
 * Polls the {@code tp_provider_webhook_event} table for events in {@code RECEIVED} status
 * and pre-processes them before they reach the {@link WebhookWorkerScheduler}.
 * <p>
 * For {@code PAYOUT_TRANSACTION} events: looks up the provider transaction, updates its
 * status based on the webhook payload, then transitions the webhook to {@code PENDING}.
 * For {@code PAYIN_TRANSACTION} events: transitions the webhook directly to {@code PENDING}.
 * <p>
 * Uses an {@link AtomicBoolean} re-entry guard to prevent overlapping executions
 * when a cycle takes longer than the configured fixed delay.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-16
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class WebhookReceiverScheduler {

    private final IWebhookReceiverUseCase webhookReceiverUseCase;
    private final WebhookReceiverProperties webhookReceiverProperties;
    private final AtomicBoolean executionInProgress = new AtomicBoolean(false);

    /**
     * Triggers the webhook receiver pre-processing cycle on a fixed delay.
     * Delegates to {@link #executeWebhookReceiverFlow()} and subscribes reactively.
     */
    @Scheduled(
        fixedDelayString = WebhookReceiverConstant.SCHEDULER_WEBHOOK_RECEIVER_FIXED_DELAY_MS,
        initialDelayString = WebhookReceiverConstant.SCHEDULER_WEBHOOK_RECEIVER_INITIAL_DELAY_MS
    )
    public void executeWebhookReceiverCycle() {
        executeWebhookReceiverFlow().subscribe();
    }

    /**
     * Reactive flow for one webhook receiver cycle. Package-visible to allow
     * direct testing without triggering the scheduler.
     *
     * @return a {@link Mono} that completes when the cycle finishes (or is skipped).
     */
    Mono<Void> executeWebhookReceiverFlow() {

        return Mono.defer(() -> {

            if (!webhookReceiverProperties.isEnabled()) {
                log.info("Webhook receiver is disabled, skipping cycle");
                return Mono.empty();
            }

            if (!executionInProgress.compareAndSet(false, true)) {
                log.warn("Webhook receiver is already running, skipping this cycle");
                return Mono.empty();
            }

            final int batchSize = CommonIntegerUtils.defaultIfNull(
                webhookReceiverProperties.getBatchSize(),
                WebhookReceiverConstant.DEFAULT_BATCH_SIZE
            );

            return webhookReceiverUseCase.processReceivedBatch(batchSize)
                .doOnSubscribe(subscription -> log.debug(
                    "Starting webhook receiver cycle: batchSize={}", batchSize
                ))
                .doOnSuccess(unused -> log.debug(
                    "Webhook receiver cycle completed: batchSize={}", batchSize
                ))
                .doOnError(error -> log.error(
                    "Error in webhook receiver cycle", error
                ))
                .onErrorResume(error -> Mono.empty())
                .doFinally(signalType -> executionInProgress.set(false));
        });
    }
}

