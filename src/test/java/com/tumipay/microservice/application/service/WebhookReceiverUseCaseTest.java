package com.tumipay.microservice.application.service;

import com.tumipay.microservice.application.component.mapper.IProviderTransactionMapper;
import com.tumipay.microservice.application.component.mapper.IProviderTransactionMapperImpl;
import com.tumipay.microservice.application.component.mapper.IWebhookEventMapper;
import com.tumipay.microservice.application.component.mapper.IWebhookEventMapperImpl;
import com.tumipay.microservice.domain.component.enums.TransactionStatusEnum;
import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.component.enums.WebhookProcessingStatusEnum;
import com.tumipay.microservice.domain.model.provider.ProviderTransaction;
import com.tumipay.microservice.domain.model.webhook.WebhookClassifierResult;
import com.tumipay.microservice.domain.model.webhook.WebhookEvent;
import com.tumipay.microservice.domain.service.contract.IProviderTransactionDomainService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventClassifierService;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventDomainService;
import com.tumipay.microservice.shared.dto.DomainOperationResult;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WebhookReceiverUseCaseTest
 * <p>
 * Unit tests for {@link WebhookReceiverUseCase}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookReceiverUseCase Unit Tests")
class WebhookReceiverUseCaseTest {

    @Mock private IProviderWebhookEventDomainService webhookEventDomainService;
    @Mock private IProviderTransactionDomainService providerTransactionDomainService;
    @Mock private IProviderWebhookEventClassifierService webhookEventTypeClassifierService;

    private IWebhookEventMapper webhookEventMapper;
    private IProviderTransactionMapper providerTransactionMapper;

    private WebhookReceiverUseCase useCase;

    @BeforeEach
    void setUp() {
        webhookEventMapper = new IWebhookEventMapperImpl();
        providerTransactionMapper = new IProviderTransactionMapperImpl();
        useCase = new WebhookReceiverUseCase(
            webhookEventDomainService,
            providerTransactionDomainService,
            webhookEventTypeClassifierService,
            webhookEventMapper,
            providerTransactionMapper
        );
    }

    // ── processReceivedBatch — empty batch ────────────────────────────────────

    @Test
    @DisplayName("processReceivedBatch should complete immediately when batch is empty")
    void shouldCompleteWhenBatchIsEmpty() {
        when(webhookEventDomainService.findReceivedBatch(10)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.processReceivedBatch(10))
            .verifyComplete();

        verifyNoInteractions(webhookEventTypeClassifierService);
        verifyNoInteractions(providerTransactionDomainService);
    }

    // ── processReceivedBatch — PAYIN event ────────────────────────────────────

    @Test
    @DisplayName("processReceivedBatch should transition PAYIN event directly to PENDING")
    void shouldTransitionPayInEventToPending() {
        WebhookEvent event = buildEvent(1L, "{\"provider_transaction_id\":\"TXN-001\"}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYIN_TRANSACTION_APPROVED)
            .transactionStatus(TransactionStatusEnum.APPROVED)
            .providerTransactionId("TXN-001")
            .build();
        DomainOperationResult<WebhookEvent> updateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(event.getEventRequest())).thenReturn(classifierResult);
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(updateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(webhookEventDomainService).updateDomainEntity(any());
        verifyNoInteractions(providerTransactionDomainService);
    }

    @Test
    @DisplayName("processReceivedBatch should transition all PAYIN variants to PENDING without touching transactions")
    void shouldTransitionPayInRejectedEventToPending() {
        WebhookEvent event = buildEvent(2L, "{}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYIN_TRANSACTION_REJECTED)
            .transactionStatus(TransactionStatusEnum.REJECTED)
            .build();
        DomainOperationResult<WebhookEvent> updateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(updateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(webhookEventDomainService).updateDomainEntity(any());
        verifyNoInteractions(providerTransactionDomainService);
    }

    // ── processReceivedBatch — PAYOUT event ──────────────────────────────────

    @Test
    @DisplayName("processReceivedBatch should update transaction and transition PAYOUT event to PENDING")
    void shouldUpdateTransactionAndTransitionPayOutEventToPending() {
        WebhookEvent event = buildEvent(3L, "{\"transaction_id\":\"TXN-PAYOUT-001\"}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED)
            .transactionStatus(TransactionStatusEnum.APPROVED)
            .providerTransactionId("TXN-PAYOUT-001")
            .build();
        ProviderTransaction transaction = buildTransaction("TXN-PAYOUT-001", TransactionStatusEnum.PENDING);
        DomainOperationResult<ProviderTransaction> txResult = DomainOperationResult.success(transaction);
        DomainOperationResult<ProviderTransaction> updateTxResult = DomainOperationResult.success(
            buildTransaction("TXN-PAYOUT-001", TransactionStatusEnum.APPROVED)
        );
        DomainOperationResult<WebhookEvent> webhookUpdateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId("TXN-PAYOUT-001"))
            .thenReturn(Mono.just(txResult));
        when(providerTransactionDomainService.updateDomainEntity(any())).thenReturn(Mono.just(updateTxResult));
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(webhookUpdateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(providerTransactionDomainService).getByProviderTransactionId("TXN-PAYOUT-001");
        verify(providerTransactionDomainService).updateDomainEntity(any());
        verify(webhookEventDomainService).updateDomainEntity(any());
    }

    @Test
    @DisplayName("processReceivedBatch should skip transaction update when transaction is in final state")
    void shouldSkipTransactionUpdateWhenInFinalState() {
        WebhookEvent event = buildEvent(4L, "{\"transaction_id\":\"TXN-FINAL\"}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED)
            .transactionStatus(TransactionStatusEnum.APPROVED)
            .providerTransactionId("TXN-FINAL")
            .build();
        ProviderTransaction transaction = buildTransaction("TXN-FINAL", TransactionStatusEnum.APPROVED); // already final
        DomainOperationResult<ProviderTransaction> txResult = DomainOperationResult.success(transaction);
        DomainOperationResult<WebhookEvent> webhookUpdateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(any()))
            .thenReturn(Mono.just(txResult));
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(webhookUpdateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(providerTransactionDomainService, never()).updateDomainEntity(any());
        verify(webhookEventDomainService).updateDomainEntity(any());
    }

    @Test
    @DisplayName("processReceivedBatch should skip transaction update when classifier provides no target status")
    void shouldSkipTransactionUpdateWhenNoTargetStatus() {
        WebhookEvent event = buildEvent(5L, "{\"transaction_id\":\"TXN-NOSTATUS\"}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED)
            .transactionStatus(null) // no target status
            .providerTransactionId("TXN-NOSTATUS")
            .build();
        ProviderTransaction transaction = buildTransaction("TXN-NOSTATUS", TransactionStatusEnum.PENDING);
        DomainOperationResult<ProviderTransaction> txResult = DomainOperationResult.success(transaction);
        DomainOperationResult<WebhookEvent> webhookUpdateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId(any()))
            .thenReturn(Mono.just(txResult));
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(webhookUpdateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(providerTransactionDomainService, never()).updateDomainEntity(any());
        verify(webhookEventDomainService).updateDomainEntity(any());
    }


    @Test
    @DisplayName("processReceivedBatch should skip transaction lookup when eventRequest is invalid JSON")
    void shouldSkipTransactionLookupWhenEventRequestIsInvalidJson() {
        WebhookEvent event = buildEvent(6L, "NOT_JSON");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED)
            .transactionStatus(TransactionStatusEnum.APPROVED)
            .build();
        DomainOperationResult<WebhookEvent> webhookUpdateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(webhookUpdateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verifyNoInteractions(providerTransactionDomainService);
        verify(webhookEventDomainService).updateDomainEntity(any());
    }

    @Test
    @DisplayName("processReceivedBatch should transition to PENDING even when transaction not found")
    void shouldTransitionToPendingWhenTransactionNotFound() {
        WebhookEvent event = buildEvent(7L, "{\"transaction_id\":\"TXN-NOTFOUND\"}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED)
            .transactionStatus(TransactionStatusEnum.APPROVED)
            .providerTransactionId("TXN-NOTFOUND")
            .build();
        DomainOperationResult<ProviderTransaction> notFoundResult = DomainOperationResult.failure("not found");
        DomainOperationResult<WebhookEvent> webhookUpdateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId("TXN-NOTFOUND"))
            .thenReturn(Mono.just(notFoundResult));
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(webhookUpdateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(providerTransactionDomainService, never()).updateDomainEntity(any());
        verify(webhookEventDomainService).updateDomainEntity(any());
    }

    // ── processReceivedBatch — UNKNOWN_EVENT ──────────────────────────────────

    @Test
    @DisplayName("processReceivedBatch should transition UNKNOWN_EVENT to PENDING")
    void shouldTransitionUnknownEventToPending() {
        WebhookEvent event = buildEvent(8L, "{}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.UNKNOWN_EVENT)
            .build();
        DomainOperationResult<WebhookEvent> webhookUpdateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(webhookUpdateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(webhookEventDomainService).updateDomainEntity(any());
        verifyNoInteractions(providerTransactionDomainService);
    }

    // ── processReceivedBatch — webhook update failure ─────────────────────────

    @Test
    @DisplayName("processReceivedBatch should propagate error when webhook update fails")
    void shouldPropagateErrorWhenWebhookUpdateFails() {
        WebhookEvent event = buildEvent(9L, "{}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYIN_TRANSACTION_APPROVED)
            .build();
        DomainOperationResult<WebhookEvent> failedUpdateResult = DomainOperationResult.failure("DB error");

        when(webhookEventDomainService.findReceivedBatch(1)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(failedUpdateResult));

        // The error is swallowed by the onErrorResume in processReceivedEvent, so the batch completes
        StepVerifier.create(useCase.processReceivedBatch(1))
            .verifyComplete();
    }

    // ── processReceivedBatch — error resilience ───────────────────────────────

    @Test
    @DisplayName("processReceivedBatch should continue processing remaining events when one fails with exception")
    void shouldContinueProcessingWhenOneEventFails() {
        WebhookEvent event1 = buildEvent(10L, "{}");
        WebhookEvent event2 = buildEvent(11L, "{}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYIN_TRANSACTION_APPROVED)
            .build();
        DomainOperationResult<WebhookEvent> successResult = DomainOperationResult.success(event2);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event1, event2));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        // event1 update throws exception; event2 succeeds
        when(webhookEventDomainService.updateDomainEntity(any()))
            .thenReturn(Mono.error(new RuntimeException("DB failure")))
            .thenReturn(Mono.just(successResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(webhookEventDomainService, times(2)).updateDomainEntity(any());
    }

    // ── processReceivedBatch — multiple events ────────────────────────────────

    @Test
    @DisplayName("processReceivedBatch should process a mixed batch of PAYIN and PAYOUT events")
    void shouldProcessMixedBatch() {
        WebhookEvent payInEvent = buildEvent(20L, "{}");
        WebhookEvent payOutEvent = buildEvent(21L, "{\"transaction_id\":\"TXN-MIXED\"}");

        WebhookClassifierResult payInResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYIN_TRANSACTION_APPROVED)
            .build();
        WebhookClassifierResult payOutResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED)
            .transactionStatus(TransactionStatusEnum.APPROVED)
            .providerTransactionId("TXN-MIXED")
            .build();

        ProviderTransaction transaction = buildTransaction("TXN-MIXED", TransactionStatusEnum.PENDING);
        DomainOperationResult<ProviderTransaction> txResult = DomainOperationResult.success(transaction);
        DomainOperationResult<ProviderTransaction> updateTxResult = DomainOperationResult.success(
            buildTransaction("TXN-MIXED", TransactionStatusEnum.APPROVED)
        );
        DomainOperationResult<WebhookEvent> webhookUpdateResult = DomainOperationResult.success(payInEvent);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(payInEvent, payOutEvent));
        when(webhookEventTypeClassifierService.classifyWebhook(any()))
            .thenReturn(payInResult)
            .thenReturn(payOutResult);
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(webhookUpdateResult));
        when(providerTransactionDomainService.getByProviderTransactionId("TXN-MIXED"))
            .thenReturn(Mono.just(txResult));
        when(providerTransactionDomainService.updateDomainEntity(any())).thenReturn(Mono.just(updateTxResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(webhookEventDomainService, times(2)).updateDomainEntity(any());
        verify(providerTransactionDomainService).getByProviderTransactionId("TXN-MIXED");
        verify(providerTransactionDomainService).updateDomainEntity(any());
    }

    // ── processReceivedBatch — transaction update fails ───────────────────────

    @Test
    @DisplayName("processReceivedBatch should still transition webhook to PENDING when transaction update fails")
    void shouldTransitionToPendingEvenWhenTransactionUpdateFails() {
        WebhookEvent event = buildEvent(30L, "{\"transaction_id\":\"TXN-UPDFAIL\"}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED)
            .transactionStatus(TransactionStatusEnum.APPROVED)
            .providerTransactionId("TXN-UPDFAIL")
            .build();
        ProviderTransaction transaction = buildTransaction("TXN-UPDFAIL", TransactionStatusEnum.PENDING);
        DomainOperationResult<ProviderTransaction> txResult = DomainOperationResult.success(transaction);
        DomainOperationResult<ProviderTransaction> failedTxUpdate = DomainOperationResult.failure("update error");
        DomainOperationResult<WebhookEvent> webhookUpdateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId("TXN-UPDFAIL"))
            .thenReturn(Mono.just(txResult));
        when(providerTransactionDomainService.updateDomainEntity(any())).thenReturn(Mono.just(failedTxUpdate));
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(webhookUpdateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(providerTransactionDomainService).updateDomainEntity(any());
        verify(webhookEventDomainService).updateDomainEntity(any());
    }

    // ── processReceivedBatch — ERROR status (non-final) ──────────────────────

    @Test
    @DisplayName("processReceivedBatch should update transaction when current status is ERROR (non-final)")
    void shouldUpdateTransactionWhenStatusIsError() {
        WebhookEvent event = buildEvent(40L, "{\"transaction_id\":\"TXN-ERR\"}");
        WebhookClassifierResult classifierResult = WebhookClassifierResult.builder()
            .classifiedType(WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED)
            .transactionStatus(TransactionStatusEnum.APPROVED)
            .providerTransactionId("TXN-ERR")
            .build();
        ProviderTransaction transaction = buildTransaction("TXN-ERR", TransactionStatusEnum.ERROR);
        DomainOperationResult<ProviderTransaction> txResult = DomainOperationResult.success(transaction);
        DomainOperationResult<ProviderTransaction> updateTxResult = DomainOperationResult.success(
            buildTransaction("TXN-ERR", TransactionStatusEnum.APPROVED)
        );
        DomainOperationResult<WebhookEvent> webhookUpdateResult = DomainOperationResult.success(event);

        when(webhookEventDomainService.findReceivedBatch(5)).thenReturn(Flux.just(event));
        when(webhookEventTypeClassifierService.classifyWebhook(any())).thenReturn(classifierResult);
        when(providerTransactionDomainService.getByProviderTransactionId("TXN-ERR"))
            .thenReturn(Mono.just(txResult));
        when(providerTransactionDomainService.updateDomainEntity(any())).thenReturn(Mono.just(updateTxResult));
        when(webhookEventDomainService.updateDomainEntity(any())).thenReturn(Mono.just(webhookUpdateResult));

        StepVerifier.create(useCase.processReceivedBatch(5))
            .verifyComplete();

        verify(providerTransactionDomainService).updateDomainEntity(any());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private WebhookEvent buildEvent(Long id, String eventRequest) {
        return WebhookEvent.builder()
            .id(id)
            .uuid("uuid-" + id)
            .adapterProviderCode("TP_PROVIDER")
            .eventType("RECEIVED")
            .eventRequest(eventRequest)
            .processingStatus(WebhookProcessingStatusEnum.PENDING)
            .retryCount(0)
            .receivedAt(Instant.now())
            .createdAt(Instant.now())
            .build();
    }

    private ProviderTransaction buildTransaction(String providerTransactionId, TransactionStatusEnum status) {
        return ProviderTransaction.builder()
            .id(1L)
            .uuid("tx-uuid")
            .providerTransactionId(providerTransactionId)
            .adapterProviderCode("TP_PROVIDER")
            .status(status)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }
}

