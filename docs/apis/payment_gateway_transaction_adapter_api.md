# TumiPay Payment Gateway - Transaction Query Adapter API

**Version:** 1.0.0
**OpenAPI:** 3.0.3
**Descripción:**
Contrato HTTP REST estándar expuesto por el adapter `tumipay-ms-payment-provider-template` para consultar el estado y el detalle de una transacción previamente procesada (Pay-In o Pay-Out). Permite recuperar el registro por cualquiera de sus tres identificadores disponibles.

---

## 🌐 Servers

| Environment | URL                                                                       |
| ----------- | ------------------------------------------------------------------------- |
| Production  | https://adapter.tumipay.com/tp/payment/adapter/[adapter_code]             |
| Staging     | https://apiadapter.staging.tumipay.com/tp/payment/adapter/[adapter_code]  |
| Local       | http://localhost:8000/tp/payment/adapter/[adapter_code]                   |

---

## 🔐 Seguridad

### ApiKeyAuth

* **Tipo:** apiKey
* **Header:** `X-Api-Key`
* **Descripción:** API key para autenticar al cliente del adapter.

---

## 📌 Endpoint

### GET `/v1/transactions`

**Descripción:**
Endpoint de consulta que permite recuperar el detalle completo de una transacción previamente procesada por el adapter.
Se debe proveer **al menos uno** de los tres query parameters disponibles. El caso de uso evalúa los identificadores en orden de prioridad interna; si ninguno resuelve a un registro existente, se retorna un error `404`.

---

## 🔄 Flujo funcional

1. Recepción de al menos uno de los identificadores via query params
2. Construcción del query object `FindStandardTransactionQuery`
3. Delegación al caso de uso `ITransactionQueryUseCase`
4. Búsqueda por prioridad: `transaction_id` → `adapter_transaction_id` → `provider_transaction_id`
5. Mapeo del resultado al DTO `AdapterTransactionResponse`
6. Respuesta normalizada (`BaseApiResponse`)

---

## 🔑 Headers

| Header       | Required | Description                       |
| ------------ | -------- | --------------------------------- |
| X-Api-Key    | ✅        | Autenticación del cliente         |
| X-Request-ID | ❌        | Identificador de trazabilidad     |

---

## 📥 Query Parameters

| Parámetro                | Tipo   | Required | Descripción                                                        |
| ------------------------ | ------ | -------- | ------------------------------------------------------------------ |
| `transaction_id`         | string | ❌        | Identificador de transacción asignado por TumiPay (sistema origen) |
| `adapter_transaction_id` | string | ❌        | Identificador interno de la transacción dentro del adapter         |
| `provider_transaction_id`| string | ❌        | Identificador de la transacción asignado por el proveedor externo  |

> ⚠️ **Al menos uno de los tres parámetros debe ser proporcionado.** Si ninguno resuelve un registro existente, la respuesta será `404 Not Found`.

---

## 🔍 Ejemplos de Request

### Consulta por `transaction_id`
```http
GET /v1/transactions?transaction_id=12ca003d-7110-4000-84f9-18fcdf54f4b2
X-Api-Key: your-api-key
```

### Consulta por `adapter_transaction_id`
```http
GET /v1/transactions?adapter_transaction_id=edee47e9-ad00-4857-b2fd-c7c2de81c887
X-Api-Key: your-api-key
```

### Consulta por `provider_transaction_id`
```http
GET /v1/transactions?provider_transaction_id=00f08a67-d546-4ea7-966a-8a1ead13c423
X-Api-Key: your-api-key
```

---

## 📤 Response

### ✅ 200 - TransactionFound

```json
{
  "code": "PROCESS_COMPLETED",
  "status": "SUCCESS",
  "message": "Operation completed successfully",
  "data": {
    "adapter_transaction_id": "edee47e9-ad00-4857-b2fd-c7c2de81c887",
    "adapter_provider_code": "TUMIPAY_EXAMPLE_PAYMENT_PROVIDER",
    "transaction_id": "12ca003d-7110-4000-84f9-18fcdf54f4b2",
    "reference_id": "906072ff-b185-4397-a7ad-3dedbd8e5d0d",
    "idempotency_key": "9c143979-9212-4a8a-9d1e-cdb692843e07",
    "provider_transaction_id": "00f08a67-d546-4ea7-966a-8a1ead13c423",
    "provider_reference_id": "1b7b31d7-7ee5-45c5-ae10-d2ece255c0e8",
    "transaction_type": "PAYIN",
    "payment_method": "PSE",
    "http_method": "POST",
    "provider_endpoint": "https://provider.example.com/v1/payment",
    "provider_request": {
      "transactionId": "12ca003d-7110-4000-84f9-18fcdf54f4b2"
    },
    "provider_response": {
      "example_provider_transaction_id": "00f08a67-d546-4ea7-966a-8a1ead13c423",
      "example_provider_reference_id": "1b7b31d7-7ee5-45c5-ae10-d2ece255c0e8"
    },
    "http_status_Code": 200,
    "success": true,
    "error_code": null,
    "error_message": null,
    "metadata": {
      "metadata_amount": {
        "value": 150000,
        "currency": "COP"
      }
    },
    "created_at": "2026-04-14T18:08:44.638945500Z",
    "updated_at": "2026-04-14T18:08:44.668199100Z",
    "processed_at": "2026-04-14T18:08:44.658655700Z"
  }
}
```

---

## 📊 Campos de Respuesta - `AdapterTransactionResponse`

| Campo                    | Tipo     | Nullable | Descripción                                              |
| ------------------------ | -------- | -------- | -------------------------------------------------------- |
| `adapter_transaction_id` | string   | ❌        | ID interno de la transacción en el adapter               |
| `adapter_provider_code`  | string   | ❌        | Código del payment provider asociado al adapter          |
| `transaction_id`         | string   | ✅        | ID de la transacción en el sistema TumiPay (origen)      |
| `reference_id`           | string   | ✅        | Referencia del negocio                                   |
| `idempotency_key`        | string   | ✅        | Clave de idempotencia usada en el procesamiento original |
| `provider_transaction_id`| string   | ✅        | ID de la transacción asignado por el proveedor externo   |
| `provider_reference_id`  | string   | ✅        | Referencia asignada por el proveedor externo             |
| `transaction_type`       | enum     | ✅        | Tipo de transacción (`PAYIN`, `PAYOUT`)                  |
| `payment_method`         | enum     | ✅        | Método de pago utilizado                                 |
| `http_method`            | string   | ✅        | Método HTTP utilizado al llamar al proveedor             |
| `provider_endpoint`      | string   | ✅        | URL del endpoint del proveedor invocado                  |
| `provider_request`       | object   | ✅        | Payload enviado al proveedor (auditoría)                 |
| `provider_response`      | object   | ✅        | Respuesta recibida del proveedor (auditoría)             |
| `http_status_Code`       | integer  | ✅        | Código HTTP retornado por el proveedor                   |
| `success`                | boolean  | ✅        | Indica si el procesamiento fue exitoso                   |
| `error_code`             | string   | ✅        | Código de error del proveedor (si aplica)                |
| `error_message`          | string   | ✅        | Mensaje de error del proveedor (si aplica)               |
| `metadata`               | object   | ✅        | Metadatos adicionales de la transacción                  |
| `created_at`             | datetime | ❌        | Timestamp de creación del registro                       |
| `updated_at`             | datetime | ❌        | Timestamp de última actualización del registro           |
| `processed_at`           | datetime | ✅        | Timestamp de procesamiento efectivo                      |

---

## ⚙️ ENUMS

### TransactionTypeEnum

```text
PAYIN
PAYOUT
```

### PaymentMethodEnum

```text
PSE
CARD
CASH
QR
TRANSFIYA
BREB
BANK_TRANSFER
WALLET
```

---

## ❌ Errores

### 404 - Transaction Not Found

```json
{
  "code": "TRANSACTION_NOT_FOUND",
  "status": "ERROR",
  "message": "Transaction not found",
  "timestamp": "2026-04-14T18:10:00Z"
}
```

### 400 - Bad Request (sin parámetros)

```json
{
  "code": "ADAPTER_VALIDATION_ERROR",
  "status": "ERROR",
  "message": "At least one query parameter must be provided",
  "timestamp": "2026-04-14T18:10:00Z",
  "errors": [
    "transaction_id, adapter_transaction_id or provider_transaction_id is required"
  ]
}
```

### ErrorResponse genérico

```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "status": "ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2026-04-14T18:10:00Z"
}
```

---

## 📌 Códigos HTTP

| Código | Descripción           |
| ------ | --------------------- |
| 200    | OK                    |
| 400    | Bad Request           |
| 401    | Unauthorized          |
| 403    | Forbidden             |
| 404    | Not Found             |
| 422    | Unprocessable Entity  |
| 500    | Internal Server Error |
| 503    | Service Unavailable   |

---

## 🧠 Notas de Arquitectura

* El controlador es **reactivo** (`Mono<ResponseEntity<...>>`) y sigue el modelo de programación reactiva con **Project Reactor**.
* La búsqueda de la transacción sigue un **orden de prioridad** definido en el caso de uso: `transaction_id` tiene precedencia sobre `adapter_transaction_id`, y este a su vez sobre `provider_transaction_id`.
* La respuesta está envuelta en el objeto `BaseApiResponse<AdapterTransactionResponse>` para mantener una estructura normalizada y consistente con todos los demás endpoints del adapter.
* Los campos `error_code`, `error_message` y `metadata` se omiten del JSON de respuesta si están vacíos (`@JsonInclude(NON_EMPTY)`).
* Diseñado para operar en entornos **multi-provider**: el campo `adapter_provider_code` identifica de forma unívoca el proveedor de pago que procesó la transacción.
* Incluye auditoría completa del ciclo de vida con `provider_request`, `provider_response` y `http_status_Code`.

---

## 🚀 Consideraciones de Consumo

* Utilizar `transaction_id` como identificador principal cuando se disponga, ya que corresponde al ID del sistema TumiPay y es el de mayor prioridad.
* En flujos donde aún no se cuente con el `transaction_id` (por ejemplo, reconciliación desde el proveedor), usar `provider_transaction_id`.
* Este endpoint es de **solo lectura** (GET) y no genera ningún efecto colateral sobre la transacción.
* Recomendado para auditoría, reconciliación y soporte de transacciones.

