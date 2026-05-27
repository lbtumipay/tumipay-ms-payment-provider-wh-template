# Especificación Técnica: WebhookReceiverScheduler

> **Versión:** 1.0.0
> **Fecha:** 2026-04-16
> **Proyecto:** `tumipay-ms-payment-provider-template`
> **Paquete objetivo:** `com.tumipay.microservice.infrastructure.adapter.input.scheduler`
> **Autor:** TumiPay SAS — Engineering Standards Team

---

## 1. Propósito y Alcance

Este documento define el contrato técnico que debe seguir un **Agente de IA** para implementar el componente `WebhookReceiverScheduler`, el cual actúa como el primer eslabón del pipeline de procesamiento asíncrono de webhooks del proveedor de pagos.

### 1.1 Contexto del pipeline completo

El procesamiento de un webhook del proveedor sigue el siguiente ciclo de vida:

```
Proveedor de Pagos
      │
      ▼  HTTP POST
┌─────────────────────────────┐
│   ProviderWebhookController │  ← Recibe el evento del proveedor
│   (ya implementado)         │
└──────────┬──────────────────┘
           │
           ▼  Guarda como RECEIVED
┌─────────────────────────┐
│  tp_provider_webhook    │  ← Tabla BD
│  _event                 │
└──────────┬──────────────┘
           │
           ▼  Scheduler (NUEVO — este documento)
┌─────────────────────────┐
│  WebhookReceiverScheduler│  ← Lee RECEIVED → procesa → marca PENDING
│  (CREAR)                │
└──────────┬──────────────┘
           │
           ▼  Estado PENDING en BD
┌─────────────────────────┐
│  WebhookWorkerScheduler │  ← Lee PENDING → despacha al Gateway
│  (ya implementado)      │
└─────────────────────────┘
```

### 1.2 Archivos a crear

| Clase | Paquete | Estado |
|---|---|---|
| `WebhookReceiverConstant` | `infrastructure.component.constant` | CREAR |
| `WebhookReceiverProperties` | `infrastructure.component.properties` | CREAR |
| `IWebhookReceiverUseCase` | `domain.port.input` | CREAR |
| `IWebhookEventTypeClassifier` | `domain.service.contract` | CREAR |
| `WebhookEventTypeClassifier` | `application.service` | CREAR |
| `WebhookReceiverUseCase` | `application.service` | CREAR |
| `WebhookReceiverScheduler` | `infrastructure.adapter.input.scheduler` | CREAR |

### 1.3 Archivos a modificar

| Clase | Cambio requerido |
|---|---|
| `WebhookEventUseCase` | Cambiar `processingStatus` de `PENDING` a `RECEIVED` al guardar |
| `IProviderTransactionDomainService` | Agregar método `getByProviderTransactionId` |
| `ProviderTransactionDomainService` | Implementar `getByProviderTransactionId` |
| `IProviderWebhookEventDomainService` | Agregar método `findReceivedBatch` |
| `ProviderWebhookEventDomainService` | Implementar `findReceivedBatch` |
| `IWebhookWorkerRepositoryPort` | Agregar método `findReceivedBatch` |
| `WebhookWorkerRepositoryAdapter` | Implementar `findReceivedBatch` con query SQL |
| `application.yml` | Agregar configuración `scheduler-webhook-receiver` y `webhook-receiver` |

### 1.4 Premisa fundamental — La validación del evento es EXTENSIBLE

La lógica que interpreta el campo `pwe_event_request` (deserializado como `ProviderWebhookRequest`) y determina el nuevo estado de la transacción **es específica de cada Payment Provider**. Este documento define:

- ✅ **Dónde** y **cómo** desplegar cada componente.
- ✅ **Qué contratos** de dominio usar.
- ✅ **Qué estados** son finales y cuáles no.
- ✅ **La estructura del flujo reactivo** completo.

Este documento **NO define**:
- ❌ La lógica exacta para mapear `ProviderWebhookRequest.status` a `TransactionStatusEnum` (depende del proveedor).
- ❌ Los valores concretos que indica aprobación, rechazo o cancelación en el webhook del proveedor.

Esas secciones deberán marcarse con `// TODO` según las instrucciones de la sección 6.

---

## 2. Prerequisito — Modificar `WebhookEventUseCase`

> ⚠️ **CRÍTICO:** Antes de implementar el scheduler, el agente DEBE aplicar este cambio.

Actualmente `WebhookEventUseCase.persistWebhookEvent` guarda el evento con estado `PENDING`. Con el nuevo pipeline, el evento debe guardarse como `RECEIVED` para que el `WebhookReceiverScheduler` pueda reconocerlo.

### Cambio en `WebhookEventUseCase.java`

**Archivo:** `src/main/java/com/tumipay/microservice/application/service/WebhookEventUseCase.java`

**Línea a modificar** en el método `persistWebhookEvent`:

```java
// ANTES (línea actual):
.processingStatus(WebhookProcessingStatusEnum.PENDING)

// DESPUÉS:
.processingStatus(WebhookProcessingStatusEnum.RECEIVED)
```

> El comentario en `buildWebhookEventResult` que dice `// Semantic ACK for HTTP caller; DB stores PENDING`
> debe actualizarse a `// Semantic ACK for HTTP caller; DB stores RECEIVED — receiver scheduler moves to PENDING`.

---

## 3. Extensiones a Contratos Existentes

### 3.1 `IProviderTransactionDomainService` — Nuevo método

**Archivo:** `src/main/java/com/tumipay/microservice/domain/service/contract/IProviderTransactionDomainService.java`

Agregar el siguiente método a la interfaz:

```java
/**
 * Retrieves a provider transaction by the identifier assigned by the
 * external payment provider.
 *
 * @param providerTransactionId the provider's own transaction identifier,
 *                              as received in the webhook event payload.
 * @return {@link Mono} emitting a {@link DomainOperationResult} wrapping the
 *         found {@link ProviderTransaction}, or a failure result if not found.
 */
Mono<DomainOperationResult<ProviderTransaction>> getByProviderTransactionId(String providerTransactionId);
```

### 3.2 `ProviderTransactionDomainService` — Implementar nuevo método

**Archivo:** `src/main/java/com/tumipay/microservice/domain/service/implementation/ProviderTransactionDomainService.java`

Agregar la implementación del método usando el patrón existente:

```java
@Override
public Mono<DomainOperationResult<ProviderTransaction>> getByProviderTransactionId(String providerTransactionId) {

    if (!StringUtils.hasText(providerTransactionId)) {
        return monoDomainFailure("providerTransactionId is required and cannot be empty");
    }

    return providerTransactionRepositoryPort.findByProviderTransactionId(providerTransactionId)
        .flatMap(this::monoDomainSuccess)
        .switchIfEmpty(monoDomainFailure(
            "ProviderTransaction not found for providerTransactionId=" + providerTransactionId
        ))
        .onErrorResume(error -> {
            log.error("Error getting ProviderTransaction for providerTransactionId={}, error: {}",
                providerTransactionId, error.getMessage());
            return monoDomainFailure("Error getting ProviderTransaction: " + error.getMessage());
        })
        .transform(CommonLoggerUtils.withProcessLogging("getProviderTransactionByProviderTransactionId"));
}
```

> El método `providerTransactionRepositoryPort.findByProviderTransactionId` ya existe en
> `IProviderTransactionRepositoryPort` — no requiere cambios en el repositorio.

### 3.3 `IProviderWebhookEventDomainService` — Nuevo método

**Archivo:** `src/main/java/com/tumipay/microservice/domain/service/contract/IProviderWebhookEventDomainService.java`

Agregar el siguiente método a la interfaz:

```java
/**
 * Retrieves a batch of webhook events in RECEIVED status, ordered by
 * received_at ASC (oldest first), up to the given limit.
 *
 * @param batchSize maximum number of RECEIVED events to return.
 * @return a reactive {@link Flux} emitting the found {@link WebhookEvent} records.
 */
Flux<WebhookEvent> findReceivedBatch(int batchSize);
```

### 3.4 `ProviderWebhookEventDomainService` — Implementar nuevo método

**Archivo:** `src/main/java/com/tumipay/microservice/domain/service/implementation/ProviderWebhookEventDomainService.java`

Agregar la implementación delegando al nuevo método del repositorio:

```java
@Override
public Flux<WebhookEvent> findReceivedBatch(int batchSize) {

    if (batchSize <= 0) {
        return Flux.error(new IllegalArgumentException("batchSize must be greater than zero"));
    }

    return webhookWorkerRepositoryPort.findReceivedBatch(batchSize);
}
```

### 3.5 `IWebhookWorkerRepositoryPort` — Nuevo método

**Archivo:** `src/main/java/com/tumipay/microservice/domain/port/output/IWebhookWorkerRepositoryPort.java`

Agregar el siguiente método:

```java
/**
 * Retrieves a batch of webhook events with {@code RECEIVED} processing status,
 * ordered by {@code pwe_received_at ASC} (oldest-first).
 * <p>
 * Intended for use by the {@code WebhookReceiverScheduler} to find events
 * that arrived via HTTP but have not yet been pre-processed.
 *
 * @param batchSize maximum number of RECEIVED events to return.
 * @return a reactive {@link Flux} emitting the found {@link WebhookEvent} records.
 */
Flux<WebhookEvent> findReceivedBatch(int batchSize);
```

### 3.6 `WebhookWorkerRepositoryAdapter` — Implementar `findReceivedBatch`

**Archivo:** `src/main/java/com/tumipay/microservice/infrastructure/adapter/output/persistence/WebhookWorkerRepositoryAdapter.java`

El agente debe implementar `findReceivedBatch` usando R2DBC `DatabaseClient` con una query SQL que:
- Filtre por `pwe_processing_status = 'RECEIVED'`
- Ordene por `pwe_received_at ASC`
- Limite a `batchSize` registros
- Mapee a `WebhookEvent` usando el mapper de persistencia existente

```java
@Override
public Flux<WebhookEvent> findReceivedBatch(int batchSize) {

    // El agente debe usar el DatabaseClient o el R2dbcEntityTemplate
    // disponible en esta clase para ejecutar la siguiente query:
    //
    // SELECT * FROM tp_provider_webhook_event
    // WHERE pwe_processing_status = 'RECEIVED'
    // ORDER BY pwe_received_at ASC
    // LIMIT :batchSize
    //
    // Y mapear cada resultado al modelo de dominio WebhookEvent
    // usando el mapper de persistencia existente (IProviderWebhookEventPersistenceMapper).
    //
    // Seguir el patrón exacto que usa claimBatch en esta misma clase.
}
```

> ⚠️ **Nota de concurrencia:** A diferencia de `claimBatch`, este método NO usa `FOR UPDATE SKIP LOCKED`
> porque el `WebhookReceiverScheduler` usa un `AtomicBoolean` para prevenir ejecuciones solapadas
> en una instancia. Si en el futuro se requiere soporte multi-réplica, este método debe evolucionar
> a un patrón de claim atómico.

---

## 4. Nuevos Componentes — Detalle de implementación

### 4.1 `WebhookReceiverConstant`

**Archivo a crear:** `src/main/java/com/tumipay/microservice/infrastructure/component/constant/WebhookReceiverConstant.java`

```java
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
```

### 4.2 `WebhookReceiverProperties`

**Archivo a crear:** `src/main/java/com/tumipay/microservice/infrastructure/component/properties/WebhookReceiverProperties.java`

```java
package com.tumipay.microservice.infrastructure.component.properties;

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

    /** Habilita o deshabilita el scheduler del receiver. */
    private boolean enabled = true;

    /** Número máximo de eventos RECEIVED a procesar por ciclo. */
    @Min(1)
    private Integer batchSize = WebhookReceiverConstant.DEFAULT_BATCH_SIZE;
}
```

> El agente debe registrar este bean de propiedades en la clase principal de la aplicación
> o en una clase `@Configuration` usando `@EnableConfigurationProperties(WebhookReceiverProperties.class)`,
> siguiendo el patrón existente de `WebhookWorkerProperties`.

### 4.3 `IWebhookReceiverUseCase`

**Archivo a crear:** `src/main/java/com/tumipay/microservice/domain/port/input/IWebhookReceiverUseCase.java`

```java
package com.tumipay.microservice.domain.port.input;

import reactor.core.publisher.Mono;

/**
 * IWebhookReceiverUseCase
 * <p>
 * Input port for the Webhook Receiver. Defines the contract for pre-processing
 * a batch of RECEIVED webhook events before they are enqueued as PENDING for
 * the {@code WebhookWorkerScheduler}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-16
 */
public interface IWebhookReceiverUseCase {

    /**
     * Executes one pre-processing cycle over a batch of RECEIVED webhook events.
     * <p>
     * For each event:
     * <ul>
     *   <li>PAYOUT_TRANSACTION events: resolves the provider transaction and updates
     *       its status based on the webhook payload, then transitions the webhook to PENDING.</li>
     *   <li>PAYIN_TRANSACTION events: transitions the webhook directly to PENDING
     *       without modifying the transaction record.</li>
     * </ul>
     *
     * @param batchSize maximum number of RECEIVED events to process in this cycle.
     * @return a reactive {@link Mono} that completes when all events in the batch
     *         have been processed (successfully or with logged errors).
     */
    Mono<Void> processReceivedBatch(int batchSize);
}
```

### 4.4 `WebhookReceiverUseCase`

**Archivo a crear:** `src/main/java/com/tumipay/microservice/application/service/WebhookReceiverUseCase.java`

#### 4.4.1 Declaración de clase e inyecciones

```java
package com.tumipay.microservice.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumipay.microservice.domain.port.input.IWebhookReceiverUseCase;
import com.tumipay.microservice.domain.service.contract.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class WebhookReceiverUseCase implements IWebhookReceiverUseCase {

    private final IProviderWebhookEventDomainService webhookEventDomainService;
    private final IProviderTransactionDomainService providerTransactionDomainService;
    private final IProviderWebhookEventClassifierService webhookEventTypeClassifier;
    private final ObjectMapper objectMapper;     // Bean de Spring — inyectado por @RequiredArgsConstructor
```

> El bean `ObjectMapper` debe estar disponible en el contexto de Spring.
> Si no existe una declaración explícita en `@Configuration`, Spring Boot lo auto-registra.

#### 4.4.2 Implementación de `processReceivedBatch`

```java
    @Override
public Mono<Void> processReceivedBatch(int batchSize) {

    return webhookEventDomainService.findReceivedBatch(batchSize)
        .doOnNext(event -> log.debug(
            "Receiver processing event id={}, type={}, uuid={}",
            event.getId(), event.getEventType(), event.getUuid()
        ))
        .flatMap(this::processReceivedEvent)
        .then()
        .doOnSubscribe(s -> log.debug(
            "Webhook receiver batch started: batchSize={}", batchSize
        ))
        .doOnSuccess(unused -> log.debug(
            "Webhook receiver batch completed"
        ))
        .doOnError(error -> log.error(
            "Error in webhook receiver batch: {}", error.getMessage(), error
        ));
}
```

#### 4.4.3 Método `processReceivedEvent` — Clasificación y despacho por tipo

El método invoca primero a `IWebhookEventTypeClassifier` para determinar el tipo canónico del evento
evaluando el `pwe_event_request` crudo. El tipo clasificado se persiste en `eventType` al hacer la
transición a `PENDING`, garantizando que el `WebhookWorkerScheduler` siempre reciba un evento
correctamente tipificado conforme a `WebhookEventTypeEnum`.

```java
    private Mono<Void> processReceivedEvent(WebhookEvent event) {

    return Mono.defer(() -> {

        // 1. Clasificar el tipo de evento evaluando el payload crudo del proveedor
        final WebhookEventTypeEnum classifiedType =
            webhookEventTypeClassifier.classify(event.getEventRequest());

        log.debug("Classified webhook id={} as eventType={}",
            event.getId(), classifiedType);

        // 2. Despachar según el tipo clasificado
        if (isPayoutEvent(classifiedType)) {
            return processPayoutWebhookEvent(event, classifiedType);
        }

        if (isPayinEvent(classifiedType)) {
            return processPayinWebhookEvent(event, classifiedType);
        }

        // UNKNOWN_EVENT — transicionar a PENDING preservando la clasificación
        // para que el WebhookWorkerScheduler lo procese (o marque como FAILED)
        log.warn("Unclassified event [{}] for webhook id={}. " +
            "Saving as UNKNOWN_EVENT and transitioning to PENDING.",
            classifiedType, event.getId());
        return transitionWebhookToPending(event, WebhookEventTypeEnum.UNKNOWN_EVENT);
    })
    .onErrorResume(error -> {
        log.error("Error processing received webhook event id={}, uuid={}, error={}",
            event.getId(), event.getUuid(), error.getMessage(), error);
        return Mono.empty(); // No propagar — procesar el resto del batch
    });
}
```

#### 4.4.4 Determinación del tipo de evento

Los helpers reciben el `WebhookEventTypeEnum` ya clasificado — no trabajan con `String` crudo.
Esto garantiza type-safety y evita comparaciones de cadenas frágiles.

```java
    /**
 * Returns true if the classifiedType corresponds to a PAYOUT_TRANSACTION event.
 * Matches all WebhookEventTypeEnum values whose name starts with "PAYOUT_TRANSACTION".
 */
private boolean isPayoutEvent(WebhookEventTypeEnum classifiedType) {
    return classifiedType != null && classifiedType.name().startsWith("PAYOUT_TRANSACTION");
}

/**
 * Returns true if the classifiedType corresponds to a PAYIN_TRANSACTION event.
 * Matches all WebhookEventTypeEnum values whose name starts with "PAYIN_TRANSACTION".
 */
private boolean isPayinEvent(WebhookEventTypeEnum classifiedType) {
    return classifiedType != null && classifiedType.name().startsWith("PAYIN_TRANSACTION");
}
```

#### 4.4.5 Procesamiento de `PAYOUT_TRANSACTION`

Recibe el `classifiedType` ya determinado por `IWebhookEventTypeClassifier` y lo propaga a
`transitionWebhookToPending` para que sea persistido.

```java
    /**
 * Processes a PAYOUT_TRANSACTION webhook event:
 * 1. Deserializes the event payload (pwe_event_request) to ProviderWebhookRequest.
 * 2. Finds the ProviderTransaction by providerTransactionId.
 * 3. If the transaction is in a non-final state, updates its status.
 * 4. Transitions the webhook event from RECEIVED to PENDING with the classified eventType.
 */
private Mono<Void> processPayoutWebhookEvent(WebhookEvent event, WebhookEventTypeEnum classifiedType) {

    return Mono.defer(() -> {

        // 1. Deserializar el payload del evento
        final ProviderWebhookRequest webhookRequest = deserializeEventRequest(event);

        if (webhookRequest == null) {
            log.error("Cannot deserialize eventRequest for webhook id={}. Transitioning to PENDING anyway.",
                event.getId());
            return transitionWebhookToPending(event, classifiedType);
        }

        // 2. Buscar la transacción por el ID del proveedor
        final String providerTransactionId = webhookRequest.getTransactionId(); // provider_transaction_id

        return providerTransactionDomainService.getByProviderTransactionId(providerTransactionId)
            .flatMap(result -> {

                if (result.isFailed() || result.getEntity() == null) {
                    log.warn(
                        "Transaction not found for providerTransactionId={}, webhook id={}. " +
                            "Transitioning webhook to PENDING for worker to handle.",
                        providerTransactionId, event.getId()
                    );
                    // Transicionar el webhook a PENDING aunque no se encontró la transacción
                    return transitionWebhookToPending(event, classifiedType);
                }

                final ProviderTransaction transaction = result.getEntity();

                // 3. Actualizar estado de transacción solo si está en estado no final
                if (isNonFinalStatus(transaction.getStatus())) {

                    final TransactionStatusEnum newStatus = resolveTransactionStatus(webhookRequest);

                    if (newStatus == null) {
                        log.warn(
                            "Could not resolve new transaction status from webhook for " +
                                "providerTransactionId={}, webhook id={}. Transitioning to PENDING.",
                            providerTransactionId, event.getId()
                        );
                        return transitionWebhookToPending(event, classifiedType);
                    }

                    final ProviderTransaction updated = buildUpdatedTransaction(transaction, newStatus);

                    return providerTransactionDomainService.updateDomainEntity(updated)
                        .flatMap(updateResult -> {
                            if (updateResult.isFailed()) {
                                log.error(
                                    "Failed to update transaction status for providerTransactionId={}, " +
                                        "webhook id={}: {}",
                                    providerTransactionId, event.getId(), updateResult.getErrorMessage()
                                );
                            }
                            // Transicionar el webhook a PENDING independientemente del resultado
                            return transitionWebhookToPending(event, classifiedType);
                        });

                } else {
                    // La transacción ya está en estado final — no actualizar
                    log.info(
                        "Transaction for providerTransactionId={} is already in final state [{}]. " +
                            "Skipping status update. Transitioning webhook id={} to PENDING.",
                        providerTransactionId, transaction.getStatus(), event.getId()
                    );
                    return transitionWebhookToPending(event, classifiedType);
                }
            });
    });
}
```

#### 4.4.6 Procesamiento de `PAYIN_TRANSACTION`

```java
    /**
 * Processes a PAYIN_TRANSACTION webhook event.
 * No transaction status update is performed for PayIn events.
 * The webhook is transitioned directly from RECEIVED to PENDING with the
 * classified eventType so the WebhookWorkerScheduler dispatches it to
 * the TumiPay Payment Gateway with a properly typed event.
 */
private Mono<Void> processPayinWebhookEvent(WebhookEvent event, WebhookEventTypeEnum classifiedType) {

    log.debug("PAYIN_TRANSACTION webhook id={} type={} — no transaction update required. " +
        "Transitioning to PENDING.", event.getId(), classifiedType);

    return transitionWebhookToPending(event, classifiedType);
}
```

#### 4.4.7 Transición del webhook a PENDING

Persiste el `classifiedType` en el campo `eventType` del evento, reemplazando cualquier valor
que haya llegado del HTTP controller. Esto garantiza que el `WebhookWorkerScheduler` siempre
opere sobre eventos correctamente tipificados según `WebhookEventTypeEnum`.

```java
    /**
 * Updates the webhook event processing status from RECEIVED to PENDING and
 * persists the type resolved by IWebhookEventTypeClassifier.
 * Uses the existing updateDomainEntity contract.
 *
 * @param event          the original RECEIVED webhook event.
 * @param classifiedType the WebhookEventTypeEnum determined by the classifier
 *                       (may be UNKNOWN_EVENT if the payload could not be mapped).
 */
private Mono<Void> transitionWebhookToPending(WebhookEvent event, WebhookEventTypeEnum classifiedType) {

    final WebhookEvent pendingEvent = WebhookEvent.builder()
        .id(event.getId())
        .uuid(event.getUuid())
        .adapterProviderCode(event.getAdapterProviderCode())
        .eventType(classifiedType.name())           // ← Persiste el tipo clasificado
        .externalEventId(event.getExternalEventId())
        .idempotencyKey(event.getIdempotencyKey())
        .processingStatus(WebhookProcessingStatusEnum.PENDING)
        .errorCode(event.getErrorCode())
        .retryCount(event.getRetryCount() != null ? event.getRetryCount() : 0)
        .lastError(event.getLastError())
        .eventRequest(event.getEventRequest())
        .receivedAt(event.getReceivedAt())
        .createdAt(event.getCreatedAt())
        .nextRetryAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    return webhookEventDomainService.updateDomainEntity(pendingEvent)
        .flatMap(result -> {
            if (result.isFailed()) {
                log.error("Failed to transition webhook id={} to PENDING: {}",
                    event.getId(), result.getErrorMessage());
                return Mono.error(new RuntimeException(
                    WebhookReceiverConstant.ERROR_CODE_WEBHOOK_UPDATE_FAILED +
                        ": " + result.getErrorMessage()
                ));
            }
            log.info("Webhook id={} uuid={} classified as [{}] and transitioned to PENDING successfully.",
                event.getId(), event.getUuid(), classifiedType);
            return Mono.empty();
        })
        .then();
}
```

#### 4.4.8 Helpers — Deserialización y estados

```java
    /**
 * Deserializes the JSON string stored in WebhookEvent.eventRequest to ProviderWebhookRequest.
 *
 * @param event the webhook event containing the raw JSON payload.
 * @return a ProviderWebhookRequest instance, or null if deserialization fails.
 */
private ProviderWebhookRequest deserializeEventRequest(WebhookEvent event) {
    try {
        return objectMapper.readValue(event.getEventRequest(), ProviderWebhookRequest.class);
    } catch (Exception e) {
        log.error("Failed to deserialize eventRequest for webhook id={}, uuid={}: {}",
            event.getId(), event.getUuid(), e.getMessage());
        return null;
    }
}

/**
 * Determines whether a transaction status is non-final and can still be updated.
 * Non-final states: PENDING, ERROR.
 * Final states: APPROVED, REJECTED, EXPIRED, CANCELLED.
 */
private boolean isNonFinalStatus(TransactionStatusEnum status) {
    return status == TransactionStatusEnum.PENDING
        || status == TransactionStatusEnum.ERROR;
}

/**
 * Builds an updated ProviderTransaction with the new status.
 * Preserves all existing fields and only updates status and updatedAt.
 */
private ProviderTransaction buildUpdatedTransaction(ProviderTransaction existing, TransactionStatusEnum newStatus) {
    return ProviderTransaction.builder()
        .id(existing.getId())
        .uuid(existing.getUuid())
        .transactionId(existing.getTransactionId())
        .referenceId(existing.getReferenceId())
        .adapterProviderCode(existing.getAdapterProviderCode())
        .providerTransactionId(existing.getProviderTransactionId())
        .providerReferenceId(existing.getProviderReferenceId())
        .idempotencyKey(existing.getIdempotencyKey())
        .amount(existing.getAmount())
        .currency(existing.getCurrency())
        .transactionType(existing.getTransactionType())
        .status(newStatus)
        .paymentMethod(existing.getPaymentMethod())
        .errorCode(existing.getErrorCode())
        .errorMessage(existing.getErrorMessage())
        .providerProcessedAt(Instant.now())
        .metadata(existing.getMetadata())
        .createdAt(existing.getCreatedAt())
        .updatedAt(Instant.now())
        .build();
}
```

#### 4.4.9 `resolveTransactionStatus` — TODO de lógica de negocio

```java
    /**
 * Maps the ProviderWebhookRequest payload to a TumiPay TransactionStatusEnum.
 *
 * TODO: This method contains provider-specific business logic.
 *       The mapping between the provider's webhook status values and TumiPay's
 *       TransactionStatusEnum MUST be defined according to the specific Payment
 *       Provider's webhook documentation.
 *
 *       The current implementation is a TEMPLATE that returns null for all cases.
 *       The implementing developer MUST replace the body of this method with the
 *       actual mapping logic for the specific Payment Provider.
 *
 *       Example (illustrative — values are fictional):
 *       return switch (webhookRequest.getStatus().toUpperCase()) {
 *           case "APPROVED", "SUCCESS", "COMPLETED" -> TransactionStatusEnum.APPROVED;
 *           case "REJECTED", "FAILED", "DECLINED"   -> TransactionStatusEnum.REJECTED;
 *           case "CANCELLED", "VOIDED"              -> TransactionStatusEnum.CANCELLED;
 *           default -> null; // Unknown status — do not update
 *       };
 *
 * @param webhookRequest the deserialized webhook payload from the provider.
 * @return the mapped {@link TransactionStatusEnum}, or {@code null} if the mapping
 *         cannot be determined (the transaction will not be updated in this case).
 */
private TransactionStatusEnum resolveTransactionStatus(ProviderWebhookRequest webhookRequest) {
    // TODO: Implementar lógica de mapeo específica del Payment Provider.
    //       Ver documentación del proveedor para los valores de status en el webhook.
    //       Los estados válidos de destino son: APPROVED, REJECTED, CANCELLED.
    //       (PENDING, ERROR, EXPIRED no deben ser estados de destino desde un webhook).
    return null;
}
}
```

### 4.5 `WebhookReceiverScheduler`

**Archivo a crear:** `src/main/java/com/tumipay/microservice/infrastructure/adapter/input/scheduler/WebhookReceiverScheduler.java`

```java
package com.tumipay.microservice.infrastructure.adapter.input.scheduler;

import com.tumipay.microservice.domain.port.input.IWebhookReceiverUseCase;
import com.tumipay.microservice.infrastructure.component.constant.WebhookReceiverConstant;
import com.tumipay.microservice.shared.properties.WebhookReceiverProperties;
import com.tumipay.microservice.infrastructure.component.properties.WebhookReceiverSchedulerProperties;
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
 * and pre-processes them before they reach the {@link WebhookDispatchScheduler}.
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
                log.debug("Webhook receiver is disabled, skipping cycle");
                return Mono.empty();
            }

            if (!executionInProgress.compareAndSet(false, true)) {
                log.warn("Webhook receiver is already running, skipping this cycle");
                return Mono.empty();
            }

            final int batchSize = resolveBatchSize();

            return webhookReceiverUseCase.processReceivedBatch(batchSize)
                    .doOnSubscribe(s -> log.debug(
                            "Starting webhook receiver cycle: batchSize={}", batchSize
                    ))
                    .doOnSuccess(unused -> log.info(
                            "Webhook receiver cycle completed: batchSize={}", batchSize
                    ))
                    .doOnError(error -> log.error(
                            "Error in webhook receiver cycle", error
                    ))
                    .onErrorResume(error -> Mono.empty())
                    .doFinally(signalType -> executionInProgress.set(false));
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private int resolveBatchSize() {
        final Integer configured = webhookReceiverProperties.getBatchSize();
        return (configured != null && configured > 0)
                ? configured
                : WebhookReceiverConstant.DEFAULT_BATCH_SIZE;
    }
}
```

### 4.6 `IWebhookEventTypeClassifier`

**Archivo a crear:** `src/main/java/com/tumipay/microservice/domain/service/contract/IWebhookEventTypeClassifier.java`

> ⚠️ **CREAR ANTES QUE `WebhookReceiverUseCase`** — el use case inyecta esta dependencia.

```java
package com.tumipay.microservice.domain.service.contract;

import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;

/**
 * IWebhookEventTypeClassifier
 * <p>
 * Domain service contract responsible for evaluating the raw JSON payload
 * of a provider webhook event and mapping it to the corresponding
 * {@link WebhookEventTypeEnum} value.
 * <p>
 * This contract is provider-specific: each Payment Provider integration must
 * supply its own implementation, since the fields and values that identify an
 * event type differ across providers. If the payload cannot be mapped to a
 * known type, the implementation MUST return {@link WebhookEventTypeEnum#UNKNOWN_EVENT}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-16
 */
public interface IWebhookEventTypeClassifier {

    /**
     * Evaluates the raw JSON string stored in {@code pwe_event_request} and
     * determines the corresponding {@link WebhookEventTypeEnum}.
     *
     * <p>Implementations must never throw; any parsing or mapping failure must
     * result in {@link WebhookEventTypeEnum#UNKNOWN_EVENT} being returned.
     *
     * @param eventRequestJson the raw JSON payload received from the payment provider,
     *                         as stored in the {@code pwe_event_request} column.
     * @return the classified {@link WebhookEventTypeEnum}, or
     *         {@link WebhookEventTypeEnum#UNKNOWN_EVENT} if the type cannot be determined.
     *         Never {@code null}.
     */
    WebhookEventTypeEnum classify(String eventRequestJson);
}
```

### 4.7 `WebhookEventTypeClassifier`

**Archivo a crear:** `src/main/java/com/tumipay/microservice/application/service/WebhookEventTypeClassifier.java`

> El método `resolveEventType` contiene el `// TODO` de lógica de negocio del proveedor,
> siguiendo el mismo patrón que `resolveTransactionStatus` en el use case.

```java
package com.tumipay.microservice.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumipay.microservice.domain.component.enums.WebhookEventTypeEnum;
import com.tumipay.microservice.domain.service.contract.IProviderWebhookEventClassifierService;
import com.tumipay.microservice.domain.service.contract.IWebhookEventTypeClassifierService;
import com.tumipay.microservice.infrastructure.adapter.input.http.provider.request.ProviderWebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * WebhookEventTypeClassifier
 * <p>
 * Provider-specific implementation of {@link IProviderWebhookEventClassifierService}.
 * Deserializes the raw JSON payload from {@code pwe_event_request} and maps
 * it to the corresponding {@link WebhookEventTypeEnum} according to the
 * payment provider's webhook contract.
 * <p>
 * Returns {@link WebhookEventTypeEnum#UNKNOWN_EVENT} for any payload that
 * cannot be parsed or mapped, ensuring every event persisted in
 * {@code tp_provider_webhook_event} carries a valid, typed classification
 * before it is dispatched to the TumiPay Payment Gateway.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 2026-04-16
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class WebhookEventTypeClassifier implements IProviderWebhookEventClassifierService {

    private final ObjectMapper objectMapper;

    @Override
    public WebhookEventTypeEnum classify(String eventRequestJson) {

        if (eventRequestJson == null || eventRequestJson.isBlank()) {
            log.warn("Cannot classify event type: eventRequestJson is null or blank");
            return WebhookEventTypeEnum.UNKNOWN_EVENT;
        }

        try {
            final ProviderWebhookRequest request =
                    objectMapper.readValue(eventRequestJson, ProviderWebhookRequest.class);

            return resolveEventType(request);

        } catch (Exception e) {
            log.error("Failed to classify event type from payload: {}", e.getMessage());
            return WebhookEventTypeEnum.UNKNOWN_EVENT;
        }
    }

    /**
     * Maps a deserialized {@link ProviderWebhookRequest} to the corresponding
     * {@link WebhookEventTypeEnum}.
     *
     * TODO: This method contains provider-specific business logic.
     *       The mapping between the provider's webhook event fields/values and
     *       TumiPay's WebhookEventTypeEnum MUST be defined according to the specific
     *       Payment Provider's webhook documentation.
     *
     *       The current implementation is a TEMPLATE that returns UNKNOWN_EVENT for all cases.
     *       The implementing developer MUST replace the body of this method with the
     *       actual mapping logic for the specific Payment Provider.
     *
     *       Example (illustrative — fields and values are fictional):
     *       String type   = request.getEventType();   // e.g. "payout", "payin"
     *       String status = request.getStatus();       // e.g. "approved", "rejected"
     *       if ("payout".equalsIgnoreCase(type)) {
     *           return switch (status.toUpperCase()) {
     *               case "APPROVED"  -> WebhookEventTypeEnum.PAYOUT_TRANSACTION_APPROVED;
     *               case "REJECTED"  -> WebhookEventTypeEnum.PAYOUT_TRANSACTION_REJECTED;
     *               case "PENDING"   -> WebhookEventTypeEnum.PAYOUT_TRANSACTION_PENDING;
     *               case "EXPIRED"   -> WebhookEventTypeEnum.PAYOUT_TRANSACTION_EXPIRED;
     *               case "CANCELLED" -> WebhookEventTypeEnum.PAYOUT_TRANSACTION_CANCELLED;
     *               case "ERROR"     -> WebhookEventTypeEnum.PAYOUT_TRANSACTION_ERROR;
     *               default          -> WebhookEventTypeEnum.UNKNOWN_EVENT;
     *           };
     *       }
     *       if ("payin".equalsIgnoreCase(type)) { ... }
     *       return WebhookEventTypeEnum.UNKNOWN_EVENT;
     *
     * @param request the deserialized webhook payload from the provider.
     * @return the corresponding {@link WebhookEventTypeEnum}, never {@code null}.
     */
    private WebhookEventTypeEnum resolveEventType(ProviderWebhookRequest request) {
        // TODO: Implementar lógica de clasificación específica del Payment Provider.
        //       Ver documentación del proveedor para los campos y valores que identifican
        //       el tipo de evento (PAYIN vs PAYOUT, APPROVED vs REJECTED, etc.).
        //       Nunca retornar null — usar WebhookEventTypeEnum.UNKNOWN_EVENT como fallback.
        return WebhookEventTypeEnum.UNKNOWN_EVENT;
    }
}
```

---

## 5. Lógica de Negocio — Diagrama de Flujo

```
WebhookReceiverScheduler.executeWebhookReceiverCycle()
    │
    ▼
enabled? ──No──► skip cycle
    │ Yes
    ▼
executionInProgress? ──Yes──► skip cycle (warn log)
    │ No (set = true)
    ▼
WebhookReceiverUseCase.processReceivedBatch(batchSize)
    │
    ▼
findReceivedBatch(batchSize)  ← tp_provider_webhook_event WHERE status='RECEIVED'
    │
    ├─[For each WebhookEvent]────────────────────────────────────────────────┐
    │                                                                        │
    │  isPayoutEvent(eventType)?                                             │
    │    ├── Yes ──► processPayoutWebhookEvent(event)                       │
    │    │               │                                                   │
    │    │               ▼                                                   │
    │    │           deserialize eventRequest → ProviderWebhookRequest       │
    │    │               │                                                   │
    │    │               ▼                                                   │
    │    │           getByProviderTransactionId(webhookRequest.transactionId)│
    │    │               │                                                   │
    │    │               ├── Not found ──► log warn                         │
    │    │               │                transitionWebhookToPending()       │
    │    │               │                                                   │
    │    │               └── Found ──► isNonFinalStatus(transaction.status)?│
    │    │                               │                                   │
    │    │                               ├── Yes ──► resolveTransactionStatus│
    │    │                               │            (TODO: provider logic) │
    │    │                               │           updateDomainEntity()    │
    │    │                               │           transitionWebhookToPending│
    │    │                               │                                   │
    │    │                               └── No (final state) ──► log info  │
    │    │                                           transitionWebhookToPending│
    │    │                                                                   │
    │    ├── isPayinEvent? ──► processPayinWebhookEvent(event)              │
    │    │                         transitionWebhookToPending()              │
    │    │                                                                   │
    │    └── Unknown ──► log warn                                           │
    │                    transitionWebhookToPending()                        │
    │                                                                        │
    └────────────────────────────────────────────────────────────────────────┘
    │
    ▼
executionInProgress.set(false)
```

### 5.1 Estados finales vs no-finales de `TransactionStatusEnum`

| Estado | Tipo | Acción del receiver |
|---|---|---|
| `PENDING` | No final | Actualizar si el webhook indica nuevo estado |
| `ERROR` | No final | Actualizar si el webhook indica nuevo estado |
| `APPROVED` | **Final** | No actualizar — log info y continuar |
| `REJECTED` | **Final** | No actualizar — log info y continuar |
| `EXPIRED` | **Final** | No actualizar — log info y continuar |
| `CANCELLED` | **Final** | No actualizar — log info y continuar |

### 5.2 Estados de destino permitidos desde el webhook (solo PAYOUT)

El método `resolveTransactionStatus` SOLO debe retornar:

| Estado de destino | Cuándo aplicar |
|---|---|
| `APPROVED` | El proveedor confirma que el desembolso fue acreditado |
| `REJECTED` | El proveedor rechaza el desembolso |
| `CANCELLED` | El desembolso fue cancelado antes de procesarse |

> ⚠️ No se deben asignar los estados `PENDING`, `ERROR` ni `EXPIRED` desde un webhook del proveedor.

---

## 6. Configuración YAML

**Archivo:** `src/main/resources/application.yml`

Agregar las siguientes secciones en los lugares indicados:

### 6.1 En la sección `tumipay.schedulers` (junto a `scheduler-webhook-worker`)

```yaml
tumipay:
    schedulers:
        scheduler-authorization-refresh:
            enabled: true
            scheduler-initial-delay-ms: 10000
            scheduler-fixed-delay-ms: 300000
        scheduler-webhook-worker:
            enabled: true
            scheduler-initial-delay-ms: 15000
            scheduler-fixed-delay-ms: 30000
        scheduler-webhook-receiver:                    # ← AGREGAR
            enabled: true
            scheduler-initial-delay-ms: 5000
            scheduler-fixed-delay-ms: 15000
```

### 6.2 Nueva sección `tumipay.webhook-receiver` (junto a `tumipay.webhook-worker`)

```yaml
tumipay:
    webhook-receiver:
        enabled:    ${ENV_WEBHOOK_RECEIVER_ENABLED:true}
        batch-size: ${ENV_WEBHOOK_RECEIVER_BATCH_SIZE:20}
```

### 6.3 Variables de entorno asociadas

| Variable de entorno | Valor por defecto | Descripción |
|---|---|---|
| `ENV_WEBHOOK_RECEIVER_ENABLED` | `true` | Habilita/deshabilita el scheduler |
| `ENV_WEBHOOK_RECEIVER_BATCH_SIZE` | `20` | Máximo de eventos RECEIVED por ciclo |

---

## 7. Logging requerido

Usar `@Log4j2` (presente en el scheduler y en el use case). Patrones de log requeridos:

```java
// Scheduler — inicio de ciclo
log.debug("Starting webhook receiver cycle: batchSize={}", batchSize);

// Scheduler — fin de ciclo exitoso
log.info("Webhook receiver cycle completed: batchSize={}", batchSize);

// Scheduler — ciclo ya en progreso
log.warn("Webhook receiver is already running, skipping this cycle");

// Scheduler — deshabilitado
log.debug("Webhook receiver is disabled, skipping cycle");

// UseCase — evento siendo procesado
log.debug("Receiver processing event id={}, type={}, uuid={}",
          event.getId(), event.getEventType(), event.getUuid());

// UseCase — tipo desconocido
    log.warn("Unknown event type [{}] for webhook id={}. Transitioning to PENDING.",
             eventType, event.getId());

// UseCase — transacción no encontrada
    log.warn("Transaction not found for providerTransactionId={}, webhook id={}. " +
                 "Transitioning webhook to PENDING for worker to handle.",
             providerTransactionId, event.getId());

// UseCase — transacción en estado final
    log.info("Transaction for providerTransactionId={} is already in final state [{}]. " +
                 "Skipping status update. Transitioning webhook id={} to PENDING.",
             providerTransactionId, transaction.getStatus(), event.getId());

// UseCase — transición exitosa
    log.info("Webhook id={} uuid={} transitioned to PENDING successfully.",
             event.getId(), event.getUuid());

// UseCase — error procesando evento (no detiene el batch)
    log.error("Error processing received webhook event id={}, uuid={}, error={}",
              event.getId(), event.getUuid(), error.getMessage(), error);

// UseCase — error actualizando transacción (no detiene el batch)
    log.error("Failed to update transaction status for providerTransactionId={}, webhook id={}: {}",
              providerTransactionId, event.getId(), updateResult.getErrorMessage());

// UseCase — error transicionando webhook a PENDING
    log.error("Failed to transition webhook id={} to PENDING: {}",
              event.getId(), result.getErrorMessage());
```

> ⚠️ Nunca loguear el contenido completo de `eventRequest` en nivel INFO o superior,
> ya que puede contener datos sensibles del proveedor.

---

## 8. Criterios de Aceptación del Agente

### Prerequisito
- [ ] **P-01** `WebhookEventUseCase.persistWebhookEvent` guarda los eventos con `processingStatus = RECEIVED` en lugar de `PENDING`.

### Extensiones a contratos existentes
- [ ] **E-01** `IProviderTransactionDomainService` declara el método `getByProviderTransactionId(String)`.
- [ ] **E-02** `ProviderTransactionDomainService` implementa `getByProviderTransactionId` usando `providerTransactionRepositoryPort.findByProviderTransactionId`.
- [ ] **E-03** `IProviderWebhookEventDomainService` declara el método `findReceivedBatch(int)`.
- [ ] **E-04** `ProviderWebhookEventDomainService` implementa `findReceivedBatch` delegando a `webhookWorkerRepositoryPort.findReceivedBatch`.
- [ ] **E-05** `IWebhookWorkerRepositoryPort` declara el método `findReceivedBatch(int)`.
- [ ] **E-06** `WebhookWorkerRepositoryAdapter` implementa `findReceivedBatch` con query SQL a `tp_provider_webhook_event` filtrando por `pwe_processing_status = 'RECEIVED'`.

### Nuevos componentes
- [ ] **N-01** `WebhookReceiverConstant` existe con las constantes de timing de SpEL y los códigos de error.
- [ ] **N-02** `WebhookReceiverProperties` está anotada con `@ConfigurationProperties(prefix = "tumipay.webhook-receiver")` y registrada como bean.
- [ ] **N-03** `IWebhookReceiverUseCase` existe con el método `processReceivedBatch(int)`.
- [ ] **N-04** `WebhookReceiverUseCase` implementa `IWebhookReceiverUseCase`.
- [ ] **N-05** `WebhookReceiverScheduler` usa `@Scheduled` con SpEL apuntando a las constantes de `WebhookReceiverConstant`.
- [ ] **N-06** `WebhookReceiverScheduler` usa `AtomicBoolean` como re-entry guard.

### Lógica de negocio
- [ ] **B-01** Los eventos con `eventType` que inicia en `"PAYOUT_TRANSACTION"` ejecutan `processPayoutWebhookEvent`.
- [ ] **B-02** Los eventos con `eventType` que inicia en `"PAYIN_TRANSACTION"` ejecutan `processPayinWebhookEvent` (solo transición a PENDING, sin actualización de transacción).
- [ ] **B-03** Para PAYOUT: la deserialización de `eventRequest` usa `ObjectMapper` para obtener `ProviderWebhookRequest`.
- [ ] **B-04** Para PAYOUT: la búsqueda de transacción usa `IProviderTransactionDomainService.getByProviderTransactionId`.
- [ ] **B-05** Para PAYOUT: solo se actualiza el estado de transacción si su estado actual es `PENDING` o `ERROR` (no-final).
- [ ] **B-06** Para PAYOUT: si el estado es final (`APPROVED`, `REJECTED`, `EXPIRED`, `CANCELLED`), no se modifica la transacción.
- [ ] **B-07** El método `resolveTransactionStatus` contiene el comentario `// TODO` indicando que la lógica depende del Payment Provider específico y devuelve `null` en la implementación base.
- [ ] **B-08** En todos los casos, el webhook se transiciona de `RECEIVED` a `PENDING` (incluyendo casos de error en búsqueda de transacción).
- [ ] **B-09** Los errores durante el procesamiento de un evento individual no detienen el procesamiento del resto del batch (`onErrorResume` retorna `Mono.empty()`).

### Configuración
- [ ] **C-01** `application.yml` incluye la sección `tumipay.schedulers.scheduler-webhook-receiver`.
- [ ] **C-02** `application.yml` incluye la sección `tumipay.webhook-receiver` con variables de entorno.
- [ ] **C-03** El código compila sin errores con `mvn compile`.

---

## 9. Archivos de Referencia

| Archivo | Propósito |
|---|---|
| `.../adapter/input/scheduler/WebhookWorkerScheduler.java` | Patrón de scheduler — seguir exactamente |
| `.../adapter/input/scheduler/AuthCredentialScheduler.java` | Patrón de scheduler alternativo |
| `.../application/service/WebhookWorkerUseCase.java` | Patrón de use case con flatMap batch |
| `.../application/service/WebhookEventUseCase.java` | **MODIFICAR** — cambiar PENDING → RECEIVED |
| `.../domain/service/contract/IProviderTransactionDomainService.java` | **MODIFICAR** — agregar `getByProviderTransactionId` |
| `.../domain/service/implementation/ProviderTransactionDomainService.java` | **MODIFICAR** — implementar nuevo método |
| `.../domain/service/contract/IProviderWebhookEventDomainService.java` | **MODIFICAR** — agregar `findReceivedBatch` |
| `.../domain/service/implementation/ProviderWebhookEventDomainService.java` | **MODIFICAR** — implementar nuevo método |
| `.../domain/port/output/IWebhookWorkerRepositoryPort.java` | **MODIFICAR** — agregar `findReceivedBatch` |
| `.../persistence/WebhookWorkerRepositoryAdapter.java` | **MODIFICAR** — implementar `findReceivedBatch` |
| `.../http/request/ProviderWebhookRequest.java` | Contrato del payload del webhook — NO modificar |
| `.../component/constant/WebhookWorkerConstant.java` | Patrón de constantes — seguir exactamente |
| `.../component/properties/WebhookWorkerProperties.java` | Patrón de properties — seguir exactamente |
| `.../domain/component/enums/WebhookProcessingStatusEnum.java` | Estados del ciclo de vida del webhook |
| `.../domain/component/enums/TransactionStatusEnum.java` | Estados de transacción — finales y no finales |
| `.../domain/component/enums/WebhookEventTypeEnum.java` | Tipos de eventos — prefix PAYOUT_/PAYIN_ |
| `.../domain/model/webhook/WebhookEvent.java` | Modelo de dominio del evento webhook |
| `.../domain/model/provider/ProviderTransaction.java` | Modelo de dominio de la transacción |
| `src/main/resources/application.yml` | **ACTUALIZAR** — agregar secciones webhook-receiver |

---

*Este documento es la fuente de verdad para el agente. El campo `resolveTransactionStatus` debe contener
un `// TODO` bien documentado indicando que la lógica de mapeo es específica del Payment Provider.
Ante cualquier ambigüedad en la lógica de negocio, el agente debe dejar `// TODO` con comentario
descriptivo en lugar de asumir comportamiento.*
