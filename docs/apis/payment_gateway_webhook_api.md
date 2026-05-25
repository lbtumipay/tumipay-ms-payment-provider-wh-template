# TumiPay Payment Gateway - Webhook Intake API

**Version:** 1.0.0  
**OpenAPI:** 3.0.3  
**Descripción:**  
Contrato HTTP REST estándar que debe implementar el Payment Gateway para recibir y procesar eventos webhook despachados por el componente `tumipay-ms-payment-provider-template`.

---

## 🌐 Servers

| Environment | URL                                     |
| ----------- | --------------------------------------- |
| Production  | https://api.tumipay.com/gateway         |
| Staging     | https://api.staging.tumipay.com/gateway |

---

## 🔐 Seguridad

### ApiKeyAuth

* **Tipo:** apiKey
* **Ubicación:** Header
* **Header:** `X-Api-Key`
* **Descripción:** API key para autenticar el origen del webhook.

---

## 📌 Endpoint

### POST `/v1/webhook/payment-event`

**Descripción:**  
Endpoint de recepción del webhook despachado por el adapter.  
Los códigos HTTP `200`, `202` y `409` se consideran acuses válidos para finalizar el procesamiento del evento en el worker.

---

### 🔑 Headers

| Header                  | Required | Description                        |
| ----------------------- | -------- | ---------------------------------- |
| X-Idempotency-Key       | ✅        | Clave idempotente del evento       |
| X-Adapter-Provider-Code | ✅        | Código del payment provider origen |
| X-Request-ID            | ❌        | Identificador de trazabilidad      |

---

## 📥 Request Body

### GatewayWebhookRequest

```json
{
  "event_type": "PAYIN_TRANSACTION_APPROVED",
  "adapter_provider_code": "TUMIPAY_EXAMPLE_PAYMENT_PROVIDER",
  "transaction_id": "TXN-2026-001",
  "reference_id": "REF-2026-001",
  "provider_transaction_id": "PROV-TX-9876543",
  "event_request": {
    "provider_event_id": "evt_abc123",
    "provider_event_type": "payment.approved",
    "provider_transaction_id": "PROV-TX-9876543",
    "provider_status": "APPROVED",
    "provider_data": {
      "amount": 150000,
      "currency": "COP",
      "payment_method": "PSE"
    }
  },
  "received_at": "2026-04-14T10:30:00Z",
  "event_uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 📌 Campos

| Campo                   | Tipo     | Required | Descripción                    |
| ----------------------- | -------- | -------- | ------------------------------ |
| event_type              | string   | ✅        | Tipo de evento                 |
| adapter_provider_code   | string   | ✅        | Código del provider            |
| transaction_id          | string   | ❌        | ID interno TumiPay             |
| reference_id            | string   | ❌        | ID de referencia               |
| provider_transaction_id | string   | ❌        | ID del proveedor               |
| event_request           | object   | ✅        | Payload original del proveedor |
| received_at             | datetime | ✅        | Timestamp de recepción         |
| event_uuid              | uuid     | ✅        | Identificador único del evento |

---

## 📊 Tipos de Evento

```text
PAYIN_TRANSACTION_APPROVED
PAYIN_TRANSACTION_REJECTED
PAYIN_TRANSACTION_PENDING
PAYIN_TRANSACTION_EXPIRED
PAYIN_TRANSACTION_ERROR
PAYIN_TRANSACTION_CANCELLED
PAYOUT_TRANSACTION_APPROVED
PAYOUT_TRANSACTION_REJECTED
PAYOUT_TRANSACTION_PENDING
PAYOUT_TRANSACTION_EXPIRED
PAYOUT_TRANSACTION_ERROR
PAYOUT_TRANSACTION_CANCELLED
```

---

## 📤 Responses

### ✅ 200 / 202 - ProcessCompleted

```json
{
  "code": "PROCESS_COMPLETED",
  "status": "SUCCESS",
  "message": "Operation completed successfully",
  "data": {
    "gateway_event_id": "gw-evt-550e8400-e29b-41d4-a716-446655440000",
    "event_id": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

---

### 🔁 409 - DuplicateEvent

```json
{
  "code": "DUPLICATE_EVENT",
  "status": "FAILED",
  "message": "Duplicate event detected for idempotency_key 9c143979-9212-4a8a-9d1e-cdb692843e07"
}
```

---

## ❌ Errores

### ErrorResponse

```json
{
  "code": "VALIDATION_ERROR",
  "status": "ERROR",
  "message": "Validation failed",
  "data": [
    {
      "field": "event_type",
      "message": "event_type cannot be null"
    }
  ]
}
```

---

### 📌 Códigos de Error

| Código HTTP | Descripción           |
| ----------- | --------------------- |
| 400         | Bad Request           |
| 401         | Unauthorized          |
| 403         | Forbidden             |
| 404         | Not Found             |
| 422         | Unprocessable Entity  |
| 500         | Internal Server Error |
| 503         | Service Unavailable   |

---

## 🔁 Status Types

```text
SUCCESS
FAILED
ERROR
```

---

## 🧠 Notas de Arquitectura

* Se utiliza **idempotencia obligatoria** para evitar reprocesamiento.
* El Gateway debe responder con `200`, `202` o `409` para detener retries del worker.
* El campo `event_request` permite flexibilidad para soportar múltiples providers.
* El diseño es compatible con procesamiento **asíncrono (Claim-Batch pattern)**.
* `event_uuid` permite trazabilidad completa entre Adapter → Gateway → Worker.

---

## 📌 Consideraciones Clave

* Validar headers obligatorios antes de procesar.
* Persistir `event_uuid` y `idempotency_key`.
* Manejar duplicados con respuesta `409`.
* Soportar alta concurrencia (múltiples workers).
* Mantener trazabilidad con `X-Request-ID`.

---
