# TumiPay Payment Gateway - PayIn Adapter API

**Version:** 1.0.0
**OpenAPI:** 3.0.3
**Descripci車n:**
Contrato HTTP REST est芍ndar expuesto por el adapter `tumipay-ms-payment-provider-template` para iniciar y procesar operaciones de pago entrante (Pay-In). 

---

## ?? Servers

| Environment | URL                                     |
| ----------- | --------------------------------------- |
| Production  | https://adapter.tumipay.com/tp/payment/adapter/[adapter_code]         |
| Staging     | https://apiadapter.staging.tumipay.com/tp/payment/adapter/[adapter_code] |
| Local       | http://localhost:8000/tp/payment/adapter/[adapter_code]                   |


---

## ?? Seguridad

### ApiKeyAuth

* **Tipo:** apiKey
* **Header:** `X-Api-Key`
* **Descripci車n:** API key para autenticar al cliente del adapter.

---

## ?? Endpoint

### POST `/v1/payin/transaction`

**Descripci車n:**
Endpoint principal del adapter para crear y procesar una transacci車n Pay-In contra el proveedor de pago externo.

### ?? Flujo funcional

1. Validaci車n del payload
2. Validaci車n de merchant y cliente
3. Integraci車n con proveedor externo
4. Persistencia de la transacci車n
5. Respuesta normalizada (`BaseApiResponse`)

---

## ?? Headers

| Header            | Required | Description          |
| ----------------- | -------- | -------------------- |
| X-Idempotency-Key | ?        | Previene duplicados  |
| X-Request-ID      | ?        | Trazabilidad         |

---

## ?? Request Body

### PayInTransactionRequest

```json
{
  "transaction": {
    "transaction_id": "12ca003d-7110-4000-84f9-18fcdf54f4b2",
    "reference_id": "906072ff-b185-4397-a7ad-3dedbd8e5d0d",
    "amount": {
      "value": 150000,
      "currency": "COP"
    },
    "country": "CO",
    "payment_method": "PSE",
    "description": "Pago de servicio mensual",
    "expiration_minutes": 30
  },
  "customer": {
    "customer_id": "CUST-001",
    "document": {
      "document_type": "CC",
      "document_number": "1234567890"
    },
    "first_name": "Juan",
    "last_name": "Perez",
    "email": "juan.perez@example.com",
    "phone": "+573001234567",
    "person_type": "INDIVIDUAL"
  },
  "merchant": {
    "merchant_id": "MERCH-001",
    "code": "MERCH_CODE_001",
    "name": "Mi Comercio S.A.S",
    "document": {
      "document_type": "NIT",
      "document_number": "900123456-1"
    }
  },
  "webhook_url": "https://api.gateway.com/v1/webhook/payment-event",
  "metadata": {
    "order_id": "ORD-2026-001",
    "channel": "WEB"
  }
}
```

---

## ?? Estructura del Request

### Transaction

| Campo              | Tipo   | Required | Descripci車n        |
| ------------------ | ------ | -------- | ------------------ |
| transaction_id     | string | ?        | ID 迆nico           |
| reference_id       | string | ?        | Referencia negocio |
| amount             | object | ?        | Monto              |
| country            | string | ?        | ISO pa赤s           |
| payment_method     | enum   | ?        | M谷todo de pago     |
| description        | string | ?        | Descripci車n        |
| expiration_minutes | int    | ?        | Expiraci車n         |

---

### Customer

| Campo       | Tipo   | Required |
| ----------- | ------ | -------- |
| customer_id | string | ?        |
| document    | object | ?        |
| first_name  | string | ?        |
| last_name   | string | ?        |
| email       | string | ?        |
| phone       | string | ?        |
| person_type | enum   | ?        |

---

### Merchant

| Campo       | Tipo   | Required |
| ----------- | ------ | -------- |
| merchant_id | string | ?        |
| code        | string | ?        |
| name        | string | ?        |
| document    | object | ?        |

---

## ?? Response

### PayInTransactionApiResponse

```json
{
    "code": "PROCESS_COMPLETED",
    "status": "SUCCESS",
    "message": "Operation completed successfully",
    "data": {
        "adapter_transaction_id": "edee47e9-ad00-4857-b2fd-c7c2de81c887",
        "payment_adapter_code": "TUMIPAY_EXAMPLE_PAYMENT_PROVIDER",
        "transaction_id": "12ca003d-7110-4000-84f9-18fcdf54f4b2",
        "reference_id": "906072ff-b185-4397-a7ad-3dedbd8e5d0d",
        "idempotency_key": "9c143979-9212-4a8a-9d1e-cdb692843e07",
        "provider_transaction_id": "00f08a67-d546-4ea7-966a-8a1ead13c423",
        "provider_reference_id": "1b7b31d7-7ee5-45c5-ae10-d2ece255c0e8",
        "operation_status": "SUCCESS",
        "transaction_status": "APPROVED",
        "message": "Mocked pay-in transaction successful",
        "provider_request": {
            "transactionId": "12ca003d-7110-4000-84f9-18fcdf54f4b2"
        },
        "provider_response": {
            "example_provider_transaction_id": "00f08a67-d546-4ea7-966a-8a1ead13c423",
            "example_provider_reference_id": "1b7b31d7-7ee5-45c5-ae10-d2ece255c0e8"
        },
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

## ?? Campos de Respuesta

| Campo                   | Descripci車n        |
| ----------------------- | ------------------ |
| adapter_transaction_id  | ID interno adapter |
| payment_adapter_code    | Provider           |
| transaction_id          | Echo               |
| reference_id            | Echo               |
| idempotency_key         | Control duplicados |
| provider_transaction_id | ID proveedor       |
| operation_status        | Estado del Proceso |
| transaction_status      | Estado de la transacci車n |
| provider_request        | Request externo    |
| provider_response       | Response externo   |
| created_at              | Creaci車n           |
| processed_at            | Procesamiento      |

---

## ?? ENUMS

### PaymentMethodEnum
Los PaymentMethod pueden variar de acuerdo con el Pa赤s y los m谷todos de pago soportados por el Payment Provider. 

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

### PersonTypeEnum

```text
INDIVIDUAL
COMPANY
```

---

## ? Errores

### ErrorResponse

```json
{
  "code": "ADAPTER_VALIDATION_ERROR",
  "message": "Invalid PayIn transaction payload",
  "timestamp": "2026-04-14T10:30:01Z",
  "errors": [
    "transaction cannot be null",
    "webhook_url cannot be null"
  ]
}
```

---

## ?? C車digos HTTP

| C車digo | Descripci車n             |
| ------ | ----------------------- |
| 200    | OK                      |
| 400    | Bad Request             |
| 401    | Unauthorized            |
| 403    | Forbidden               |
| 404    | Not Found               |
| 409    | Conflict (idempotencia) |
| 422    | Unprocessable Entity    |
| 500    | Internal Error          |
| 503    | Service Unavailable     |

---

## ?? Notas de Arquitectura

* Soporta **idempotencia opcional** (pero recomendada).
* Respuesta estandarizada (`BaseApiResponse`).
* Incluye trazabilidad completa (`request_id`, `correlation_id`).
* Dise?ado para integraciones multi-provider.
* `provider_request` y `provider_response` permiten auditor赤a completa.
* Webhook desacoplado mediante `webhook_url`.

---

## ?? Consideraciones Clave

* Validar `amount.value > 0`
* Validar formato E.164 en `phone`
* Validar URL del webhook
* Manejar duplicados con idempotency key
* Persistir estado inicial como `PENDING`
* Preparar integraci車n asincr車nica v赤a webhook

---
