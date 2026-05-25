# TumiPay Payment Gateway - PayOut Adapter API

**Version:** 1.0.0
**OpenAPI:** 3.0.3
**Descripción:**
Contrato HTTP REST estándar expuesto por el adapter `tumipay-ms-payment-provider-template` para iniciar y procesar operaciones de pago saliente (Pay-Out / Disbursement). 

---

## 🌐 Servers

| Environment | URL                                     |
| ----------- | --------------------------------------- |
| Production  | https://adapter.tumipay.com/tp/payment/adapter/[adapter_code]         |
| Staging     | https://apiadapter.staging.tumipay.com/tp/payment/adapter/[adapter_code] |
| Local       | http://localhost:8000/tp/payment/adapter/[adapter_code]                   |

---

## 🔐 Seguridad

### ApiKeyAuth

* **Tipo:** apiKey
* **Header:** `X-Api-Key`
* **Descripción:** API key para autenticar al cliente del adapter.

---

## 📌 Endpoint

### POST `/v1/payout/transaction`

**Descripción:**
Endpoint principal del adapter para crear y procesar una transacción Pay-Out contra el proveedor externo. Permite el desembolso de fondos hacia un beneficiario.

---

## 🔄 Flujo funcional

1. Validación del payload
2. Validación del beneficiario y cuenta destino
3. Integración con proveedor externo
4. Persistencia de la transacción
5. Respuesta normalizada (sincrónica o asincrónica)

---

## 🔑 Headers

| Header            | Required | Description              |
| ----------------- | -------- | ------------------------ |
| X-Idempotency-Key | ❌        | Prevención de duplicados |
| X-Request-ID      | ❌        | Trazabilidad             |

---

## 📥 Request Body

### PayOutTransactionRequest

```json id="k5a9xp"
{
  "transaction": {
    "transaction_id": "TXN-POUT-2026-001",
    "reference_id": "REF-POUT-2026-001",
    "amount": {
      "value": 500000,
      "currency": "COP"
    },
    "country": "CO",
    "payment_method": "BANK_TRANSFER",
    "description": "Pago a proveedor - Factura 2026-04",
    "expiration_minutes": 60
  },
  "beneficiary": {
    "beneficiary_id": "BEN-001",
    "document": {
      "document_type": "CC",
      "document_number": "9876543210"
    },
    "first_name": "Maria",
    "last_name": "Lopez",
    "email": "maria.lopez@example.com",
    "phone": "+573109876543",
    "bank_account": {
      "bank_code": "BANCOLOMBIA",
      "bank_name": "Bancolombia",
      "account_type": "SAVINGS",
      "account_number": "12345678901"
    }
  },
  "metadata": {
    "payment_id": "PAY-2026-999",
    "channel": "API"
  }
}
```

---

## 📊 Estructura del Request

### Transaction

| Campo              | Tipo   | Required |
| ------------------ | ------ | -------- |
| transaction_id     | string | ✅        |
| reference_id       | string | ✅        |
| amount             | object | ✅        |
| country            | string | ✅        |
| payment_method     | enum   | ✅        |
| description        | string | ✅        |
| expiration_minutes | int    | ✅        |

---

### Beneficiary

| Campo          | Tipo   | Required |
| -------------- | ------ | -------- |
| beneficiary_id | string | ✅        |
| document       | object | ❌        |
| first_name     | string | ❌        |
| last_name      | string | ❌        |
| email          | string | ❌        |
| phone          | string | ❌        |
| bank_account   | object | ❌        |

---

### BankAccount

| Campo          | Tipo   |
| -------------- | ------ |
| bank_code      | string |
| bank_name      | string |
| account_type   | enum   |
| account_number | string |

---

## 📤 Response

### PayOutTransactionApiResponse

```json id="qz3e0y"
{
    "code": "PROCESS_COMPLETED",
    "status": "SUCCESS",
    "message": "Operation completed successfully",
    "data": {
        "adapter_transaction_id": "b80b2fc8-00fd-43b7-95a7-d11752924d36",
        "transaction_id": "b28f9215-9cdf-424c-8837-db108733d99a",
        "reference_id": "ref-001",
        "idempotency_key": "9c143979-9212-4a8a-9d1e-cdb692843e63",
        "payment_adapter_code": "TUMIPAY_EXAMPLE_PAYMENT_PROVIDER",
        "provider_transaction_id": "57f212c9-0ecc-4af2-b529-8ddf17f09031",
        "provider_reference_id": "d306e980-b321-4eb6-adba-9ab42de54112",
        "synchronous": false,
        "operation_status": "SUCCESS",
        "transaction_status": "PENDING",
        "message": "Mocked pay-in transaction successful",
        "provider_request": {
            "transactionId": "b28f9215-9cdf-424c-8837-db108733d99a"
        },
        "provider_response": {
            "example_provider_transaction_id": "57f212c9-0ecc-4af2-b529-8ddf17f09031",
            "example_provider_reference_id": "d306e980-b321-4eb6-adba-9ab42de54112"
        },
        "metadata": {
            "metadata_amount": {
                "value": 500000,
                "currency": "COP"
            }
        },
        "created_at": "2026-04-14T18:08:30.435632800Z",
        "updated_at": "2026-04-14T18:08:30.514969500Z",
        "processed_at": "2026-04-14T18:08:30.493009300Z"
    }
}
```

---

## 📊 Campos Clave de Respuesta

| Campo                     | Descripción         |
| ------------------------- | ------------------- |
| adapter_transaction_id    | ID interno          |
| payment_adapter_code      | Provider            |
| synchronous               | Indica sync/async   |
| status                    | Estado actual       |
| provider_transaction_id   | ID proveedor        |
| provider_request/response | Auditoría           |
| processed_at              | Fecha procesamiento |

---

## ⚙️ ENUMS

### PaymentMethodEnum
Los PaymentMethod pueden variar de acuerdo con el País y los métodos de pago soportados por el Payment Provider. 

```text id="hvqz6x"
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

### AccountTypeEnum

```text id="y5b7cj"
SAVINGS
CHECKING
```

---

## ❌ Errores

### ErrorResponse

```json id="n3s0mf"
{
  "code": "ADAPTER_VALIDATION_ERROR",
  "message": "Invalid PayOut transaction payload",
  "timestamp": "2026-04-14T11:00:01Z",
  "errors": [
    "beneficiary cannot be null",
    "webhook_url cannot be blank"
  ]
}
```

---

## 📌 Códigos HTTP

| Código | Descripción          |
| ------ | -------------------- |
| 200    | OK                   |
| 400    | Bad Request          |
| 401    | Unauthorized         |
| 403    | Forbidden            |
| 404    | Not Found            |
| 409    | Conflict             |
| 422    | Unprocessable Entity |
| 500    | Internal Error       |
| 503    | Service Unavailable  |

---

## 🧠 Notas de Arquitectura

* Soporta procesamiento **sincrónico y asincrónico**
* Resultado final puede llegar vía **webhook**
* Idempotencia recomendada
* Auditoría completa (`provider_request`, `provider_response`)
* Diseñado para múltiples providers
* Compatible con modelo **event-driven**

---

## 🚀 Consideraciones Clave

* Validar `account_number`
* Validar `bank_code`
* Validar formato E.164 en `phone`
* Validar consistencia `country` vs banco
* Manejar duplicados con idempotency key
* Persistir estado inicial `PENDING`
* Usar `synchronous` para definir estrategia de respuesta

---
