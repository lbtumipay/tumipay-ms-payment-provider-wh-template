package com.tumipay.microservice.infrastructure.adapter.input.scheduler;

import com.tumipay.microservice.domain.port.input.IWebhookWorkerUseCase;
import com.tumipay.microservice.shared.properties.WebhookDispatcherProperties;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * WebhookWorkerSchedulerTest
 * <p>
 * Unit tests for {@link WebhookWorkerScheduler}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 16/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookWorkerScheduler Tests")
class WebhookWorkerSchedulerTest {

    @Mock
    private IWebhookWorkerUseCase webhookWorkerUseCase;

    @Mock
    private WebhookDispatcherProperties webhookDispatcherProperties;

    private WebhookWorkerScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new WebhookWorkerScheduler(webhookWorkerUseCase, webhookDispatcherProperties);
    }

    // ── Disabled ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should skip cycle when webhook worker is disabled")
    void shouldSkipWhenWorkerIsDisabled() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(false);

        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        verifyNoInteractions(webhookWorkerUseCase);
    }

    // ── Re-entry guard ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should skip concurrent cycle when execution is already in progress")
    void shouldSkipConcurrentExecution() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(true);
        when(webhookDispatcherProperties.getWorkerId()).thenReturn("worker-1");
        when(webhookDispatcherProperties.getBatchSize()).thenReturn(10);

        // First call blocks indefinitely so the lock is held
        when(webhookWorkerUseCase.pollAndProcess(anyString(), anyInt()))
            .thenReturn(Mono.never());

        // Subscribe without blocking to keep the lock held
        scheduler.executeWebhookWorkerFlow().subscribe();

        // Second call should be skipped (lock is held)
        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        verify(webhookWorkerUseCase, times(1)).pollAndProcess(anyString(), anyInt());
    }

    // ── Worker ID resolution ──────────────────────────────────────────────────

    @Test
    @DisplayName("Should use configured workerId when provided")
    void shouldUseConfiguredWorkerId() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(true);
        when(webhookDispatcherProperties.getWorkerId()).thenReturn("my-worker-id");
        when(webhookDispatcherProperties.getBatchSize()).thenReturn(5);
        when(webhookWorkerUseCase.pollAndProcess(anyString(), anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        ArgumentCaptor<String> workerIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(webhookWorkerUseCase).pollAndProcess(workerIdCaptor.capture(), anyInt());
        assertEquals("my-worker-id", workerIdCaptor.getValue());
    }

    @Test
    @DisplayName("Should auto-generate workerId when not configured")
    void shouldAutoGenerateWorkerIdWhenNotConfigured() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(true);
        when(webhookDispatcherProperties.getWorkerId()).thenReturn(null);
        when(webhookDispatcherProperties.getBatchSize()).thenReturn(5);
        when(webhookWorkerUseCase.pollAndProcess(anyString(), anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        ArgumentCaptor<String> workerIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(webhookWorkerUseCase).pollAndProcess(workerIdCaptor.capture(), anyInt());
        assertTrue(workerIdCaptor.getValue() != null && !workerIdCaptor.getValue().isBlank(),
            "Auto-generated workerId should not be blank");
    }

    @Test
    @DisplayName("Should auto-generate workerId when configured workerId is blank")
    void shouldAutoGenerateWorkerIdWhenBlank() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(true);
        when(webhookDispatcherProperties.getWorkerId()).thenReturn("   ");
        when(webhookDispatcherProperties.getBatchSize()).thenReturn(5);
        when(webhookWorkerUseCase.pollAndProcess(anyString(), anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        ArgumentCaptor<String> workerIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(webhookWorkerUseCase).pollAndProcess(workerIdCaptor.capture(), anyInt());
        assertTrue(workerIdCaptor.getValue() != null && !workerIdCaptor.getValue().isBlank());
    }

    // ── Batch size resolution ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should use configured batchSize when valid")
    void shouldUseConfiguredBatchSize() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(true);
        when(webhookDispatcherProperties.getWorkerId()).thenReturn("worker-1");
        when(webhookDispatcherProperties.getBatchSize()).thenReturn(25);
        when(webhookWorkerUseCase.pollAndProcess(anyString(), anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        ArgumentCaptor<Integer> batchCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(webhookWorkerUseCase).pollAndProcess(anyString(), batchCaptor.capture());
        assertEquals(25, batchCaptor.getValue());
    }

    @Test
    @DisplayName("Should use default batchSize when configured value is null")
    void shouldUseDefaultBatchSizeWhenNull() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(true);
        when(webhookDispatcherProperties.getWorkerId()).thenReturn("worker-1");
        when(webhookDispatcherProperties.getBatchSize()).thenReturn(null);
        when(webhookWorkerUseCase.pollAndProcess(anyString(), anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        ArgumentCaptor<Integer> batchCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(webhookWorkerUseCase).pollAndProcess(anyString(), batchCaptor.capture());
        assertEquals(10, batchCaptor.getValue()); // DEFAULT_BATCH_SIZE = 10
    }

    @Test
    @DisplayName("Should use default batchSize when configured value is zero or negative")
    void shouldUseDefaultBatchSizeWhenNonPositive() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(true);
        when(webhookDispatcherProperties.getWorkerId()).thenReturn("worker-1");
        when(webhookDispatcherProperties.getBatchSize()).thenReturn(0);
        when(webhookWorkerUseCase.pollAndProcess(anyString(), anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        ArgumentCaptor<Integer> batchCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(webhookWorkerUseCase).pollAndProcess(anyString(), batchCaptor.capture());
        assertEquals(10, batchCaptor.getValue()); // DEFAULT_BATCH_SIZE = 10
    }

    // ── Error handling & lock release ─────────────────────────────────────────

    @Test
    @DisplayName("Should release execution lock after error and allow next execution")
    void shouldReleaseLockAfterErrorAndAllowNextExecution() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(true);
        when(webhookDispatcherProperties.getWorkerId()).thenReturn("worker-1");
        when(webhookDispatcherProperties.getBatchSize()).thenReturn(10);

        when(webhookWorkerUseCase.pollAndProcess(anyString(), anyInt()))
            .thenReturn(Mono.error(new RuntimeException("processing error")))
            .thenReturn(Mono.empty());

        // First call: error should be swallowed via onErrorResume
        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        // Second call: lock must have been released
        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        verify(webhookWorkerUseCase, times(2)).pollAndProcess(anyString(), anyInt());
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should complete successfully when pollAndProcess returns empty")
    void shouldCompleteSuccessfullyOnNormalFlow() {
        when(webhookDispatcherProperties.isEnabled()).thenReturn(true);
        when(webhookDispatcherProperties.getWorkerId()).thenReturn("worker-abc");
        when(webhookDispatcherProperties.getBatchSize()).thenReturn(10);
        when(webhookWorkerUseCase.pollAndProcess("worker-abc", 10)).thenReturn(Mono.empty());

        StepVerifier.create(scheduler.executeWebhookWorkerFlow())
            .verifyComplete();

        verify(webhookWorkerUseCase).pollAndProcess("worker-abc", 10);
    }
}

