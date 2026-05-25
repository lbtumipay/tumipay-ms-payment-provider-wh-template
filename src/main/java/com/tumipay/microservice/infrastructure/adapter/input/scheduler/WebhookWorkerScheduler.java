package com.tumipay.microservice.infrastructure.adapter.input.scheduler;

import com.tumipay.microservice.domain.port.input.IWebhookWorkerUseCase;
import com.tumipay.microservice.infrastructure.component.constant.WebhookWorkerConstant;
import com.tumipay.microservice.infrastructure.component.properties.WebhookDispatcherProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebhookWorkerScheduler
 * <p>
 * Scheduled component that drives the Webhook Worker Claim-Batch cycle.
 * Follows the same re-entry guard pattern as {@link AuthCredentialScheduler}:
 * an {@link AtomicBoolean} prevents overlapping executions when a cycle takes
 * longer than the configured fixed delay.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 10/04/2026
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class WebhookWorkerScheduler {

    private final IWebhookWorkerUseCase webhookWorkerUseCase;
    private final WebhookDispatcherProperties webhookDispatcherProperties;
    private final AtomicBoolean executionInProgress = new AtomicBoolean(false);

    /**
     * Triggers the webhook worker poll-and-process cycle on a fixed delay.
     * Delegates to {@link #executeWebhookWorkerFlow()} and subscribes reactively.
     */
    @Scheduled(
        fixedDelayString = WebhookWorkerConstant.SCHEDULER_WEBHOOK_WORKER_FIXED_DELAY_MS,
        initialDelayString = WebhookWorkerConstant.SCHEDULER_WEBHOOK_WORKER_INITIAL_DELAY_MS
    )
    public void executeWebhookWorkerCycle() {
        executeWebhookWorkerFlow().subscribe();
    }

    /**
     * Reactive flow for one webhook worker cycle. Package-visible to allow
     * direct testing without triggering the scheduler.
     *
     * @return a {@link Mono} that completes when the cycle finishes (or is skipped).
     */
    Mono<Void> executeWebhookWorkerFlow() {

        return Mono.defer(() -> {

            if (!webhookDispatcherProperties.isEnabled()) {
                log.info("Webhook worker is disabled, skipping cycle");
                return Mono.empty();
            }

            if (!executionInProgress.compareAndSet(false, true)) {
                log.warn("Webhook worker is already running, skipping this cycle");
                return Mono.empty();
            }

            final String workerId = resolveWorkerId();
            final int batchSize = resolveBatchSize();

            return webhookWorkerUseCase.pollAndProcess(workerId, batchSize)
                .doOnSubscribe(s -> log.debug(
                    "Starting webhook worker cycle: workerId={}, batchSize={}", workerId, batchSize
                ))
                .doOnSuccess(unused -> log.debug(
                    "Webhook worker cycle completed: workerId={}", workerId
                ))
                .doOnError(error -> log.error(
                    "Error in webhook worker cycle: workerId={}", workerId, error
                ))
                .onErrorResume(error -> Mono.empty())
                .doFinally(signalType -> executionInProgress.set(false));
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String resolveWorkerId() {

        final String workerConfiguredId = webhookDispatcherProperties.getWorkerId();

        if (workerConfiguredId != null && !workerConfiguredId.isBlank()) {
            return workerConfiguredId;
        }

        try {
            final String hostname = InetAddress.getLocalHost().getHostName();
            return String.format("%s-%s", hostname, ProcessHandle.current().pid());
        } catch (java.net.UnknownHostException e) {
            return "worker-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    private int resolveBatchSize() {
        final Integer configured = webhookDispatcherProperties.getBatchSize();
        return (configured != null && configured > 0) ? configured : WebhookWorkerConstant.DEFAULT_BATCH_SIZE;
    }
}

