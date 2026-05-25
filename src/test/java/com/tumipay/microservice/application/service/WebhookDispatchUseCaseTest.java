package com.tumipay.microservice.application.service;

import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.gateway.GatewayWebhookResult;
import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.port.output.IPaymentGatewayWebhookAdapterPort;
import com.tumipay.microservice.domain.service.contract.IProviderTransactionDomainService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventClassifierService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventDomainService;
import com.tumipay.microservice.shared.dto.DomainOperationResult;
import com.tumipay.microservice.shared.enums.BaseErrorCodeEnum;
import com.tumipay.microservice.shared.exception.BusinessException;
import com.tumipay.microservice.shared.exception.GatewayWebhookException;
import com.tumipay.microservice.shared.properties.WebhookDispatcherProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WebhookDispatchUseCaseTest
 * <p>
 * Unit tests for the {@link WebhookDispatchUseCase} application service.
 * Validates the full poll-and-process cycle: batch claiming, event classification,
 * provider transaction lookup, gateway dispatch, acknowledgement and retry/failure handling.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 18/05/2026
 */
@ExtendWith(MockitoExtension.class)
class WebhookDispatchUseCaseTest {

    // -------------------------------------------------------------------------
    // Test constants
    // -------------------------------------------------------------------------

    private static final String WORKER_ID = "worker-001";
    private static final int BATCH_SIZE = 5;
    private static final Long EVENT_ID = 1L;
    private static final String EVENT_UUID = "uuid-wh-001";
    private static final String PROVIDER_TX_ID = "prov-tx-001";
    private static final String TRANSACTION_ID = "tx-001";
    private static final String REFERENCE_ID = "ref-001";
    private static final String EVENT_REQUEST = "{\"status\":\"COMPLETED\",\"id\":\"prov-tx-001\"}";
    private static final String EVENT_TYPE = "PAYIN_STATUS";
    private static final int MAX_RETRY_COUNT = 3;

    // -------------------------------------------------------------------------
    // Mocks
    // -------------------------------------------------------------------------

    @Mock
    private IProviderWebhookEventDomainService providerWebhookEventDomainService;

    @Mock
    private WebhookDispatcherProperties webhookDispatcherProperties;

    @Mock
    private IPaymentGatewayWebhookAdapterPort paymentGatewayWebhookAdapterPort;

    @Mock
    private IProviderTransactionDomainService providerTransactionDomainService;

    @Mock
    private IProviderWebhookEventClassifierService webhookEventTypeClassifierService;

    // -------------------------------------------------------------------------
    // Subject under test
    // -------------------------------------------------------------------------

    private WebhookDispatchUseCase webhookDispatchUseCase;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        webhookDispatchUseCase = new WebhookDispatchUseCase(
            providerWebhookEventDomainService,
            webhookDispatcherProperties,
            paymentGatewayWebhookAdapterPort,
            providerTransactionDomainService,
            webhookEventTypeClassifierService
        );
    }

    // =========================================================================
    // pollAndProcess – happy path
    // =========================================================================

    @Test
    @DisplayName("pollAndProcess: should complete successfully when a batch event is processed end-to-end")
    void pollAndProcess_shouldCompleteSuccessfully_whenFullPipelineSucceeds() {

        WebhookEvent event = buildWebhookEvent();
        WebhookClassifierResult classifierResult = buildClassifierResult();
        ProviderTransaction providerTransaction = buildProviderTransaction();
        GatewayWebhookResult gatewayResult = buildSuccessGatewayResult();
        WebhookEvent processedEvent = buildProcessedWebhookEvent();

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(EVENT_REQUEST))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(PROVIDER_TX_ID))
            .thenReturn(Mono.just(DomainOperationResult.success(providerTransaction)));
        when(paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(any(WebhookEvent.class)))
            .thenReturn(Mono.just(gatewayResult));
        when(providerWebhookEventDomainService.markAsProcessed(EVENT_ID))
            .thenReturn(Mono.just(processedEvent));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).claimBatch(WORKER_ID, BATCH_SIZE);
        verify(webhookEventTypeClassifierService).classifyWebhook(EVENT_REQUEST);
        verify(providerTransactionDomainService).getByProviderTransactionId(PROVIDER_TX_ID);
        verify(paymentGatewayWebhookAdapterPort).dispatchWebhookEvent(any(WebhookEvent.class));
        verify(providerWebhookEventDomainService).markAsProcessed(EVENT_ID);
    }

    @Test
    @DisplayName("pollAndProcess: should complete immediately when the claimed batch is empty")
    void pollAndProcess_shouldCompleteImmediately_whenBatchIsEmpty() {

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.empty());

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(paymentGatewayWebhookAdapterPort, never()).dispatchWebhookEvent(any());
        verify(providerWebhookEventDomainService, never()).markAsProcessed(any());
    }

    @Test
    @DisplayName("pollAndProcess: should process multiple events in the batch concurrently")
    void pollAndProcess_shouldProcessMultipleEvents_whenBatchContainsMultipleEvents() {

        WebhookEvent event1 = buildWebhookEventWithId(1L, "uuid-001");
        WebhookEvent event2 = buildWebhookEventWithId(2L, "uuid-002");
        WebhookClassifierResult classifierResult = buildClassifierResult();
        ProviderTransaction providerTransaction = buildProviderTransaction();
        GatewayWebhookResult gatewayResult = buildSuccessGatewayResult();

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event1, event2));
        when(webhookEventTypeClassifierService.classifyWebhook(anyString()))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(anyString()))
            .thenReturn(Mono.just(DomainOperationResult.success(providerTransaction)));
        when(paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(any(WebhookEvent.class)))
            .thenReturn(Mono.just(gatewayResult));
        when(providerWebhookEventDomainService.markAsProcessed(1L))
            .thenReturn(Mono.just(buildProcessedWebhookEventWithId(1L)));
        when(providerWebhookEventDomainService.markAsProcessed(2L))
            .thenReturn(Mono.just(buildProcessedWebhookEventWithId(2L)));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).markAsProcessed(1L);
        verify(providerWebhookEventDomainService).markAsProcessed(2L);
    }

    // =========================================================================
    // pollAndProcess – gateway dispatch scenarios
    // =========================================================================

    @Test
    @DisplayName("pollAndProcess: should mark event for retry when gateway returns FAILED status")
    void pollAndProcess_shouldMarkForRetry_whenGatewayReturnsFailed() {

        WebhookEvent event = buildWebhookEvent();
        WebhookClassifierResult classifierResult = buildClassifierResult();
        ProviderTransaction providerTransaction = buildProviderTransaction();
        GatewayWebhookResult failedGatewayResult = GatewayWebhookResult.builder()
            .code("GATEWAY_ERROR")
            .status("FAILED")
            .message("Gateway rejected the event")
            .build();

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(webhookDispatcherProperties.getMaxRetryCount()).thenReturn(MAX_RETRY_COUNT);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(EVENT_REQUEST))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(PROVIDER_TX_ID))
            .thenReturn(Mono.just(DomainOperationResult.success(providerTransaction)));
        when(paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(any(WebhookEvent.class)))
            .thenReturn(Mono.just(failedGatewayResult));
        when(providerWebhookEventDomainService.markForRetry(eq(EVENT_ID), eq("GATEWAY_ERROR"), anyString(), eq(MAX_RETRY_COUNT)))
            .thenReturn(Mono.just(buildRetryScheduledWebhookEvent()));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).markForRetry(eq(EVENT_ID), eq("GATEWAY_ERROR"), anyString(), eq(MAX_RETRY_COUNT));
        verify(providerWebhookEventDomainService, never()).markAsProcessed(any());
    }

    @Test
    @DisplayName("pollAndProcess: should mark event for retry when GatewayWebhookException is thrown")
    void pollAndProcess_shouldMarkForRetry_whenGatewayWebhookExceptionThrown() {

        WebhookEvent event = buildWebhookEvent();
        WebhookClassifierResult classifierResult = buildClassifierResult();
        ProviderTransaction providerTransaction = buildProviderTransaction();
        String gatewayErrorCode = "GATEWAY_SERVER_ERROR_500";

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(webhookDispatcherProperties.getMaxRetryCount()).thenReturn(MAX_RETRY_COUNT);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(EVENT_REQUEST))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(PROVIDER_TX_ID))
            .thenReturn(Mono.just(DomainOperationResult.success(providerTransaction)));
        when(paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(any(WebhookEvent.class)))
            .thenReturn(Mono.error(new GatewayWebhookException(gatewayErrorCode, "Internal server error from gateway")));
        when(providerWebhookEventDomainService.markForRetry(eq(EVENT_ID), eq(gatewayErrorCode), anyString(), eq(MAX_RETRY_COUNT)))
            .thenReturn(Mono.just(buildRetryScheduledWebhookEvent()));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).markForRetry(eq(EVENT_ID), eq(gatewayErrorCode), anyString(), eq(MAX_RETRY_COUNT));
        verify(providerWebhookEventDomainService, never()).markAsProcessed(any());
    }

    // =========================================================================
    // pollAndProcess – provider transaction lookup scenarios
    // =========================================================================

    @Test
    @DisplayName("pollAndProcess: should mark event for retry when provider transaction is not found")
    void pollAndProcess_shouldMarkForRetry_whenProviderTransactionNotFound() {

        WebhookEvent event = buildWebhookEvent();
        WebhookClassifierResult classifierResult = buildClassifierResult();

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(webhookDispatcherProperties.getMaxRetryCount()).thenReturn(MAX_RETRY_COUNT);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(EVENT_REQUEST))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(PROVIDER_TX_ID))
            .thenReturn(Mono.just(DomainOperationResult.failure("Provider transaction not found")));
        when(providerWebhookEventDomainService.markForRetry(
            eq(EVENT_ID),
            eq(BaseErrorCodeEnum.TRANSACTION_NOT_FOUND.getCode()),
            anyString(),
            eq(MAX_RETRY_COUNT)
        )).thenReturn(Mono.just(buildRetryScheduledWebhookEvent()));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).markForRetry(
            eq(EVENT_ID),
            eq(BaseErrorCodeEnum.TRANSACTION_NOT_FOUND.getCode()),
            anyString(),
            eq(MAX_RETRY_COUNT)
        );
        verify(paymentGatewayWebhookAdapterPort, never()).dispatchWebhookEvent(any());
        verify(providerWebhookEventDomainService, never()).markAsProcessed(any());
    }

    // =========================================================================
    // pollAndProcess – generic / unexpected error scenarios
    // =========================================================================

    @Test
    @DisplayName("pollAndProcess: should mark event for retry with WEBHOOK_PROCESSING_ERROR on unexpected exception")
    void pollAndProcess_shouldMarkForRetry_withWebhookProcessingError_onUnexpectedException() {

        WebhookEvent event = buildWebhookEvent();
        WebhookClassifierResult classifierResult = buildClassifierResult();
        ProviderTransaction providerTransaction = buildProviderTransaction();

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(webhookDispatcherProperties.getMaxRetryCount()).thenReturn(MAX_RETRY_COUNT);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(EVENT_REQUEST))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(PROVIDER_TX_ID))
            .thenReturn(Mono.just(DomainOperationResult.success(providerTransaction)));
        when(paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(any(WebhookEvent.class)))
            .thenReturn(Mono.error(new RuntimeException("Unexpected connection error")));
        when(providerWebhookEventDomainService.markForRetry(
            eq(EVENT_ID),
            eq(BaseErrorCodeEnum.WEBHOOK_PROCESSING_ERROR.getCode()),
            anyString(),
            eq(MAX_RETRY_COUNT)
        )).thenReturn(Mono.just(buildRetryScheduledWebhookEvent()));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).markForRetry(
            eq(EVENT_ID),
            eq(BaseErrorCodeEnum.WEBHOOK_PROCESSING_ERROR.getCode()),
            anyString(),
            eq(MAX_RETRY_COUNT)
        );
        verify(providerWebhookEventDomainService, never()).markAsProcessed(any());
    }

    @Test
    @DisplayName("pollAndProcess: should mark event for retry with BusinessException error code")
    void pollAndProcess_shouldMarkForRetry_withBusinessExceptionCode_onBusinessError() {

        WebhookEvent event = buildWebhookEvent();
        WebhookClassifierResult classifierResult = buildClassifierResult();
        ProviderTransaction providerTransaction = buildProviderTransaction();
        String businessErrorCode = "CUSTOM_BUSINESS_ERROR";

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(webhookDispatcherProperties.getMaxRetryCount()).thenReturn(MAX_RETRY_COUNT);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(EVENT_REQUEST))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(PROVIDER_TX_ID))
            .thenReturn(Mono.just(DomainOperationResult.success(providerTransaction)));
        when(paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(any(WebhookEvent.class)))
            .thenReturn(Mono.error(new BusinessException(businessErrorCode, "Custom business rule violation")));
        when(providerWebhookEventDomainService.markForRetry(
            eq(EVENT_ID), eq(businessErrorCode), anyString(), eq(MAX_RETRY_COUNT)
        )).thenReturn(Mono.just(buildRetryScheduledWebhookEvent()));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).markForRetry(
            eq(EVENT_ID), eq(businessErrorCode), anyString(), eq(MAX_RETRY_COUNT)
        );
    }

    // =========================================================================
    // pollAndProcess – FAILED (max retries exhausted) scenario
    // =========================================================================

    @Test
    @DisplayName("pollAndProcess: should log permanent failure when event reaches max retry count")
    void pollAndProcess_shouldLogPermanentFailure_whenMaxRetriesExhausted() {

        WebhookEvent event = buildWebhookEvent();
        event.setRetryCount(MAX_RETRY_COUNT);

        WebhookClassifierResult classifierResult = buildClassifierResult();
        ProviderTransaction providerTransaction = buildProviderTransaction();

        WebhookEvent failedEvent = buildWebhookEventWithStatus(WebhookProcessingStatusEnum.FAILED);

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(webhookDispatcherProperties.getMaxRetryCount()).thenReturn(MAX_RETRY_COUNT);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(EVENT_REQUEST))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(PROVIDER_TX_ID))
            .thenReturn(Mono.just(DomainOperationResult.success(providerTransaction)));
        when(paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(any(WebhookEvent.class)))
            .thenReturn(Mono.error(new RuntimeException("Persistent failure")));
        when(providerWebhookEventDomainService.markForRetry(
            eq(EVENT_ID),
            eq(BaseErrorCodeEnum.WEBHOOK_PROCESSING_ERROR.getCode()),
            anyString(),
            eq(MAX_RETRY_COUNT)
        )).thenReturn(Mono.just(failedEvent));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).markForRetry(eq(EVENT_ID), anyString(), anyString(), eq(MAX_RETRY_COUNT));
    }

    // =========================================================================
    // pollAndProcess – concurrency resolution
    // =========================================================================

    @Test
    @DisplayName("pollAndProcess: should use default concurrency of 10 when concurrency is null")
    void pollAndProcess_shouldUseDefaultConcurrency_whenConcurrencyIsNull() {

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(null);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.empty());

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();
    }

    @Test
    @DisplayName("pollAndProcess: should use default concurrency of 10 when concurrency is zero")
    void pollAndProcess_shouldUseDefaultConcurrency_whenConcurrencyIsZero() {

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(0);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.empty());

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();
    }

    @Test
    @DisplayName("pollAndProcess: should use custom concurrency when valid positive value is configured")
    void pollAndProcess_shouldUseCustomConcurrency_whenValidConcurrencyConfigured() {

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(3);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.empty());

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();
    }

    // =========================================================================
    // pollAndProcess – gateway with data in result
    // =========================================================================

    @Test
    @DisplayName("pollAndProcess: should complete successfully when gateway result includes data")
    void pollAndProcess_shouldComplete_whenGatewayResultIncludesDataBlock() {

        WebhookEvent event = buildWebhookEvent();
        WebhookClassifierResult classifierResult = buildClassifierResult();
        ProviderTransaction providerTransaction = buildProviderTransaction();
        GatewayWebhookResult gatewayResult = GatewayWebhookResult.builder()
            .code("PROCESS_COMPLETED")
            .status("SUCCESS")
            .message("Event processed")
            .data(GatewayWebhookResult.GatewayWebhookData.builder()
                .gatewayEventId("gw-event-99")
                .eventId(EVENT_UUID)
                .build())
            .build();
        WebhookEvent processedEvent = buildProcessedWebhookEvent();

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(EVENT_REQUEST))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(PROVIDER_TX_ID))
            .thenReturn(Mono.just(DomainOperationResult.success(providerTransaction)));
        when(paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(any(WebhookEvent.class)))
            .thenReturn(Mono.just(gatewayResult));
        when(providerWebhookEventDomainService.markAsProcessed(EVENT_ID))
            .thenReturn(Mono.just(processedEvent));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).markAsProcessed(EVENT_ID);
    }

    // =========================================================================
    // pollAndProcess – gateway result with null status (treated as success)
    // =========================================================================

    @Test
    @DisplayName("pollAndProcess: should mark as processed when gateway result status is null")
    void pollAndProcess_shouldMarkAsProcessed_whenGatewayStatusIsNull() {

        WebhookEvent event = buildWebhookEvent();
        WebhookClassifierResult classifierResult = buildClassifierResult();
        ProviderTransaction providerTransaction = buildProviderTransaction();
        GatewayWebhookResult nullStatusResult = GatewayWebhookResult.builder()
            .code("PROCESS_COMPLETED")
            .status(null)
            .build();
        WebhookEvent processedEvent = buildProcessedWebhookEvent();

        when(webhookDispatcherProperties.getConcurrency()).thenReturn(5);
        when(providerWebhookEventDomainService.claimBatch(WORKER_ID, BATCH_SIZE))
            .thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(EVENT_REQUEST))
            .thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(PROVIDER_TX_ID))
            .thenReturn(Mono.just(DomainOperationResult.success(providerTransaction)));
        when(paymentGatewayWebhookAdapterPort.dispatchWebhookEvent(any(WebhookEvent.class)))
            .thenReturn(Mono.just(nullStatusResult));
        when(providerWebhookEventDomainService.markAsProcessed(EVENT_ID))
            .thenReturn(Mono.just(processedEvent));

        StepVerifier.create(webhookDispatchUseCase.pollAndProcess(WORKER_ID, BATCH_SIZE))
            .verifyComplete();

        verify(providerWebhookEventDomainService).markAsProcessed(EVENT_ID);
        verify(providerWebhookEventDomainService, never()).markForRetry(anyLong(), anyString(), anyString(), anyInt());
    }

    // =========================================================================
    // Private builder helpers
    // =========================================================================

    private WebhookEvent buildWebhookEvent() {
        return WebhookEvent.builder()
            .id(EVENT_ID)
            .uuid(EVENT_UUID)
            .adapterProviderCode("TP_PROVIDER")
            .eventType(EVENT_TYPE)
            .idempotencyKey("idem-key-001")
            .eventRequest(EVENT_REQUEST)
            .processingStatus(WebhookProcessingStatusEnum.PENDING)
            .retryCount(0)
            .receivedAt(Instant.parse("2026-05-18T10:00:00Z"))
            .createdAt(Instant.parse("2026-05-18T10:00:00Z"))
            .build();
    }

    private WebhookEvent buildWebhookEventWithId(Long id, String uuid) {
        return WebhookEvent.builder()
            .id(id)
            .uuid(uuid)
            .adapterProviderCode("TP_PROVIDER")
            .eventType(EVENT_TYPE)
            .idempotencyKey("idem-key-" + id)
            .eventRequest(EVENT_REQUEST)
            .processingStatus(WebhookProcessingStatusEnum.PENDING)
            .retryCount(0)
            .receivedAt(Instant.parse("2026-05-18T10:00:00Z"))
            .createdAt(Instant.parse("2026-05-18T10:00:00Z"))
            .build();
    }

    private WebhookEvent buildProcessedWebhookEvent() {
        return WebhookEvent.builder()
            .id(EVENT_ID)
            .uuid(EVENT_UUID)
            .processingStatus(WebhookProcessingStatusEnum.PROCESSED)
            .processedAt(Instant.parse("2026-05-18T10:01:00Z"))
            .build();
    }

    private WebhookEvent buildProcessedWebhookEventWithId(Long id) {
        return WebhookEvent.builder()
            .id(id)
            .processingStatus(WebhookProcessingStatusEnum.PROCESSED)
            .processedAt(Instant.parse("2026-05-18T10:01:00Z"))
            .build();
    }

    private WebhookEvent buildRetryScheduledWebhookEvent() {
        return WebhookEvent.builder()
            .id(EVENT_ID)
            .uuid(EVENT_UUID)
            .processingStatus(WebhookProcessingStatusEnum.PENDING)
            .retryCount(1)
            .nextRetryAt(Instant.parse("2026-05-18T10:05:00Z"))
            .build();
    }

    private WebhookEvent buildWebhookEventWithStatus(WebhookProcessingStatusEnum status) {
        return WebhookEvent.builder()
            .id(EVENT_ID)
            .uuid(EVENT_UUID)
            .processingStatus(status)
            .retryCount(MAX_RETRY_COUNT)
            .build();
    }

    private WebhookClassifierResult buildClassifierResult() {
        return WebhookClassifierResult.builder()
            .providerTransactionId(PROVIDER_TX_ID)
            .build();
    }

    private ProviderTransaction buildProviderTransaction() {
        return ProviderTransaction.builder()
            .id(10L)
            .uuid("prov-uuid-001")
            .transactionId(TRANSACTION_ID)
            .referenceId(REFERENCE_ID)
            .providerTransactionId(PROVIDER_TX_ID)
            .build();
    }

    private GatewayWebhookResult buildSuccessGatewayResult() {
        return GatewayWebhookResult.builder()
            .code("PROCESS_COMPLETED")
            .status("SUCCESS")
            .message("Webhook event processed successfully")
            .data(GatewayWebhookResult.GatewayWebhookData.builder()
                .gatewayEventId("gw-event-001")
                .eventId(EVENT_UUID)
                .build())
            .build();
    }
}

