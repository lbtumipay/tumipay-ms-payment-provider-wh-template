package com.tumipay.microservice.infrastructure.adapter.input.scheduler;

import com.tumipay.microservice.domain.port.input.IWebhookReceiverUseCase;
import com.tumipay.microservice.infrastructure.component.properties.WebhookReceiverProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * WebhookReceiverSchedulerTest
 * <p>
 * Unit tests for {@link WebhookReceiverScheduler}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookReceiverScheduler Tests")
class WebhookReceiverSchedulerTest {

    @Mock
    private IWebhookReceiverUseCase webhookReceiverUseCase;

    @Mock
    private WebhookReceiverProperties webhookReceiverProperties;

    private WebhookReceiverScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new WebhookReceiverScheduler(webhookReceiverUseCase, webhookReceiverProperties);
    }

    // ── Disabled ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should skip cycle when webhook receiver is disabled")
    void shouldSkipWhenReceiverIsDisabled() {
        when(webhookReceiverProperties.isEnabled()).thenReturn(false);

        StepVerifier.create(scheduler.executeWebhookReceiverFlow())
            .verifyComplete();

        verifyNoInteractions(webhookReceiverUseCase);
    }

    // ── Re-entry guard ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should skip concurrent cycle when execution is already in progress")
    void shouldSkipConcurrentExecution() {
        when(webhookReceiverProperties.isEnabled()).thenReturn(true);
        when(webhookReceiverProperties.getBatchSize()).thenReturn(20);

        // First call blocks indefinitely so the lock is held
        when(webhookReceiverUseCase.processReceivedBatch(anyInt()))
            .thenReturn(Mono.never());

        // Subscribe without blocking to keep the lock held
        scheduler.executeWebhookReceiverFlow().subscribe();

        // Second call should be skipped (lock is held)
        StepVerifier.create(scheduler.executeWebhookReceiverFlow())
            .verifyComplete();

        verify(webhookReceiverUseCase, times(1)).processReceivedBatch(anyInt());
    }

    // ── Batch size resolution ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should use configured batchSize when valid")
    void shouldUseConfiguredBatchSize() {
        when(webhookReceiverProperties.isEnabled()).thenReturn(true);
        when(webhookReceiverProperties.getBatchSize()).thenReturn(30);
        when(webhookReceiverUseCase.processReceivedBatch(anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookReceiverFlow())
            .verifyComplete();

        ArgumentCaptor<Integer> batchCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(webhookReceiverUseCase).processReceivedBatch(batchCaptor.capture());
        assertEquals(30, batchCaptor.getValue());
    }

    @Test
    @DisplayName("Should use default batchSize when configured value is null")
    void shouldUseDefaultBatchSizeWhenNull() {
        when(webhookReceiverProperties.isEnabled()).thenReturn(true);
        when(webhookReceiverProperties.getBatchSize()).thenReturn(null);
        when(webhookReceiverUseCase.processReceivedBatch(anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookReceiverFlow())
            .verifyComplete();

        ArgumentCaptor<Integer> batchCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(webhookReceiverUseCase).processReceivedBatch(batchCaptor.capture());
        // DEFAULT_BATCH_SIZE = 20
        assertEquals(20, batchCaptor.getValue());
    }

    // ── Error handling & lock release ─────────────────────────────────────────

    @Test
    @DisplayName("Should release execution lock after error and allow next execution")
    void shouldReleaseLockAfterErrorAndAllowNextExecution() {
        when(webhookReceiverProperties.isEnabled()).thenReturn(true);
        when(webhookReceiverProperties.getBatchSize()).thenReturn(20);

        when(webhookReceiverUseCase.processReceivedBatch(anyInt()))
            .thenReturn(Mono.error(new RuntimeException("processing error")))
            .thenReturn(Mono.empty());

        // First call: error should be swallowed via onErrorResume
        StepVerifier.create(scheduler.executeWebhookReceiverFlow())
            .verifyComplete();

        // Second call: lock must have been released
        StepVerifier.create(scheduler.executeWebhookReceiverFlow())
            .verifyComplete();

        verify(webhookReceiverUseCase, times(2)).processReceivedBatch(anyInt());
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should complete successfully when processReceivedBatch returns empty")
    void shouldCompleteSuccessfullyOnNormalFlow() {
        when(webhookReceiverProperties.isEnabled()).thenReturn(true);
        when(webhookReceiverProperties.getBatchSize()).thenReturn(20);
        when(webhookReceiverUseCase.processReceivedBatch(20)).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookReceiverFlow())
            .verifyComplete();

        verify(webhookReceiverUseCase).processReceivedBatch(20);
    }
}

