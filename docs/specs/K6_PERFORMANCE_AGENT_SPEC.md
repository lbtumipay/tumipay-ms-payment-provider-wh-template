# Especificación Técnica: Agente de IA para Pruebas de Performance con k6

> **Versión:** 1.0.0  
> **Fecha:** 2026-04-19  
> **Proyecto:** `tumipay-ms-payment-provider-template`  
> **Autor:** TumiPay SAS — Engineering Standards Team

---

## 1. Objetivo

Este documento define el contrato técnico que debe seguir un **Agente de IA** para inspeccionar los controladores REST del microservicio y generar de forma autónoma scripts de pruebas de performance con **k6** ubicados en `automation/performance/k6/`.

El agente debe ser capaz de:

1. Leer los controladores del paquete fuente definido.
2. Extraer todos los endpoints disponibles con sus contratos HTTP completos.
3. Generar scripts k6 individuales por controlador y un script orquestador global.
4. Garantizar que cada endpoint tenga cobertura de performance para los escenarios mínimos requeridos.
5. Parametrizar los scripts con variables de entorno y datos de prueba representativos.

---

## 2. Alcance

### 2.1 Paquetes fuente a inspeccionar

| Paquete | Descripción |
|---|---|
| `com.tumipay.microservice.infrastructure.adapter.input.http.standard` | Controladores estándar de operaciones de negocio |

### 2.2 Archivos k6 objetivo

| Archivo | Controlador asociado | Tipo |
|---|---|---|
| `automation/performance/k6/payin-test.js` | `PayInTransactionController` | Script de escenario |
| `automation/performance/k6/payout-test.js` | `PayOutTransactionController` | Script de escenario |
| `automation/performance/k6/transaction-query-test.js` | `TransactionController` | Script de escenario |
| `automation/performance/k6/microservice-info-test.js` | `MicroServiceController` | Script de escenario |
| `automation/performance/k6/config/options.js` | — | Configuración global de opciones k6 |
| `automation/performance/k6/config/thresholds.js` | — | Umbrales de performance globales |
| `automation/performance/k6/data/payin-payload.json` | `PayInTransactionController` | Dataset de request bodies |
| `automation/performance/k6/data/payout-payload.json` | `PayOutTransactionController` | Dataset de request bodies |
| `automation/performance/k6/utils/helpers.js` | — | Utilidades compartidas (headers, UUID, etc.) |
| `automation/performance/k6/main.js` | Todos | Orquestador: importa y ejecuta todos los escenarios |

---

## 3. Inventario de Controladores y Contratos HTTP

El agente **debe** derivar esta información leyendo directamente el código fuente. A continuación se documenta el estado actual como referencia.

### 3.1 `MicroServiceController`

```
@RequestMapping("/v1/microservice")
GET /info → Mono<ResponseEntity<BaseApiResponse<MicroServiceInfoResponse>>>
Headers requeridos: ninguno
Headers opcionales: ninguno
Autenticación: ninguna (@InternalApiSecured NO aplicado)
Respuesta OK: HTTP 200 + BaseApiResponse<MicroServiceInfoResponse>
```

**Campos de respuesta `MicroServiceInfoResponse`:**

| Campo JSON | Tipo | Descripción |
|---|---|---|
| `service_name` | string | Nombre del microservicio |
| `service_description` | string | Descripción del microservicio |
| `version` | string | Versión del servicio |
| `environment` | string | Entorno de despliegue |
| `java_version` | string | Versión de Java en ejecución |
| `spring_boot_version` | string | Versión de Spring Boot |
| `timestamp` | string (ISO-8601) | Timestamp del servidor al momento de la consulta |

---

### 3.2 `PayInTransactionController`

```
@RequestMapping("/v1/payin")
POST /transaction → Mono<ResponseEntity<BaseApiResponse<PayInTransactionResponse>>>
Headers requeridos: Content-Type: application/json
Headers opcionales: X-Idempotency-Key (valor: "X-Idempotency-Key")
Autenticación: @InternalApiSecured → X-Api-Key requerido
Body: PayInTransactionRequest (@Valid)
Respuesta OK: HTTP 200 + BaseApiResponse<PayInTransactionResponse>
```

**Estructura del body `PayInTransactionRequest`:**

```json
{
  "transaction": {
    "transaction_id": "string (NotBlank)",
    "reference_id": "string (NotBlank)",
    "amount": {
      "value": "integer",
      "currency": "string"
    },
    "country": "string (NotBlank)",
    "payment_method": "PaymentMethodEnum (NotNull)",
    "description": "string (NotBlank)",
    "expiration_minutes": "integer (NotNull, Positive)"
  },
  "customer": {
    "customer_id": "string (NotNull)",
    "document": {
      "document_type": "string (NotNull)",
      "document_number": "string (NotNull)"
    },
    "first_name": "string (NotNull)",
    "last_name": "string (NotNull)",
    "email": "string (NotNull)",
    "phone": "string (NotNull)",
    "person_type": "PersonTypeEnum (NotNull)"
  },
  "merchant": {
    "merchant_id": "string",
    "code": "string",
    "name": "string",
    "document": {
      "document_type": "string",
      "document_number": "string"
    }
  },
  "webhook_url": "string (NotNull, URL válida)",
  "metadata": {}
}
```

**Campos de respuesta `PayInTransactionResponse`:**

| Campo JSON | Tipo |
|---|---|
| `adapter_transaction_id` | string |
| `payment_adapter_code` | string |
| `transaction_id` | string |
| `reference_id` | string |
| `idempotency_key` | string |
| `provider_transaction_id` | string |
| `provider_reference_id` | string |
| `operation_status` | string |
| `transaction_status` | string |
| `message` | string |
| `provider_request` | object |
| `provider_response` | object |
| `metadata` | object |
| `errors` | array |
| `created_at` | Instant |
| `updated_at` | Instant |
| `processed_at` | Instant |

---

### 3.3 `PayOutTransactionController`

```
@RequestMapping("/v1/payout")
POST /transaction → Mono<ResponseEntity<BaseApiResponse<PayOutTransactionResponse>>>
Headers requeridos: Content-Type: application/json
Headers opcionales: X-Idempotency-Key (valor: "X-Idempotency-Key")
Autenticación: @InternalApiSecured → X-Api-Key requerido
Body: PayOutTransactionRequest
Respuesta OK: HTTP 200 + BaseApiResponse<PayOutTransactionResponse>
```

**Estructura del body `PayOutTransactionRequest`:**

```json
{
  "transaction": {
    "transaction_id": "string (NotNull)",
    "reference_id": "string",
    "amount": {
      "value": "integer",
      "currency": "string"
    },
    "country": "string",
    "payment_method": "PaymentMethodEnum",
    "description": "string",
    "expiration_minutes": "integer"
  },
  "beneficiary": {
    "beneficiary_id": "string",
    "document": {
      "document_type": "string",
      "document_number": "string"
    },
    "first_name": "string",
    "last_name": "string",
    "email": "string",
    "phone": "string",
    "bank_account": {
      "bank_code": "string",
      "bank_name": "string",
      "account_type": "AccountTypeEnum",
      "account_number": "string"
    }
  },
  "webhook_url": "string (NotBlank)",
  "metadata": {}
}
```

---

### 3.4 `TransactionController`

```
@RequestMapping("/v1/transactions")
GET / → Mono<ResponseEntity<BaseApiResponse<AdapterTransactionResponse>>>
Headers requeridos: ninguno
Autenticación: @InternalApiSecured → X-Api-Key requerido
Query params (todos opcionales):
  - transaction_id
  - adapter_transaction_id
  - provider_transaction_id
Respuesta OK: HTTP 200 + BaseApiResponse<AdapterTransactionResponse>
```

---

## 4. Contratos de Respuesta

### 4.1 `BaseApiResponse<T>` (todos los controladores estándar)

```json
{
  "code": "string",
  "status": "string",
  "message": "string",
  "data": {}
}
```

| Escenario | `code` | `status` | HTTP |
|---|---|---|---|
| Operación exitosa | `PROCESS_COMPLETED` | `SUCCESS` | 200 |
| Error de validación Bean | `VALIDATION_ERROR` | `ERROR` | 400 |
| Error de negocio | *(código de negocio)* | `FAILED` | 400/409/422 |
| Error interno | `INTERNAL_SERVER_ERROR` | `ERROR` | 500 |

### 4.2 Headers estándar (fuente: `BaseIntegrationConstant`)

| Constante | Valor HTTP | Usado en |
|---|---|---|
| `HEADER_REQUEST_ID` | `X-Request-ID` | Todos (trazabilidad) |
| `HEADER_CORRELATION_ID` | `X-Correlation-ID` | Todos (trazabilidad) |
| `HEADER_MERCHANT_ID` | `X-Merchant-ID` | PayIn, PayOut |
| `HEADER_IDEMPOTENCY_KEY` | `X-Idempotency-Key` | PayIn, PayOut |
| `HEADER_API_KEY` | `X-Api-Key` | Todos (@InternalApiSecured) |

---

## 5. Instrucciones para el Agente

### 5.1 Proceso de lectura de fuentes

El agente debe leer en este orden:

1. Todos los archivos `.java` en el paquete `standard`.
2. Para cada controlador, extraer:
   - `@RequestMapping` base path.
   - Cada método HTTP (`@GetMapping`, `@PostMapping`, etc.) y su sub-path.
   - Headers consumidos (`@RequestHeader`), incluyendo si son `required`.
   - Query params (`@RequestParam`), incluyendo si son `required`.
   - Tipo del body (`@RequestBody`) y sus validaciones (`@Valid`, `@NotNull`, `@NotBlank`, `@Pattern`).
   - Tipo de respuesta y estructura.
3. Leer `BaseIntegrationConstant.java` para obtener los **valores exactos** de los headers.
4. Leer los DTOs de request/response (en `http/request/` y `http/response/`) para mapear el schema completo del payload.
5. Leer los modelos de dominio (en `domain/model/`) para derivar los campos anidados.

### 5.2 Reglas de generación de scripts k6

#### Regla G-01: URL base
Todas las URLs deben construirse usando la variable de entorno `__ENV.BASE_URL`:
```javascript
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const url = `${BASE_URL}/tp/payment/adapter/v1/payin/transaction`;
```

#### Regla G-02: Headers obligatorios
Cada request debe incluir:
```javascript
const headers = {
  'Content-Type': 'application/json',
  'X-Request-ID': uuidv4(),
  'X-Api-Key': __ENV.API_KEY || 'test-api-key',
};
```
Para PayIn y PayOut, también incluir:
```javascript
headers['X-Idempotency-Key'] = uuidv4();
headers['X-Merchant-ID'] = __ENV.MERCHANT_ID || 'test-merchant';
```

#### Regla G-03: Generación de UUIDs
Siempre usar una función helper local para generar UUIDs en k6 (k6 no soporta `crypto.randomUUID()` de Node.js):
```javascript
// En utils/helpers.js
export function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0;
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
}
```

#### Regla G-04: Verificaciones mínimas por response
```javascript
// Para BaseApiResponse exitoso:
check(response, {
  'HTTP 200': (r) => r.status === 200,
  'code PROCESS_COMPLETED': (r) => JSON.parse(r.body).code === 'PROCESS_COMPLETED',
  'status SUCCESS': (r) => JSON.parse(r.body).status === 'SUCCESS',
  'data presente': (r) => JSON.parse(r.body).data !== null,
});

// Para error de validación:
check(response, {
  'HTTP 400': (r) => r.status === 400,
  'code VALIDATION_ERROR': (r) => JSON.parse(r.body).code === 'VALIDATION_ERROR',
});
```

#### Regla G-05: Thresholds mínimos
Cada script debe definir o importar los thresholds desde `config/thresholds.js`:
```javascript
export const thresholds = {
  http_req_duration: ['p(95)<2000', 'p(99)<5000'],
  http_req_failed: ['rate<0.01'],
  http_reqs: ['rate>10'],
};
```

#### Regla G-06: Estructura de opciones k6
```javascript
// config/options.js
export const smokeOptions = {
  vus: 1,
  duration: '30s',
};

export const loadOptions = {
  stages: [
    { duration: '1m', target: 10 },
    { duration: '3m', target: 10 },
    { duration: '1m', target: 0 },
  ],
};

export const stressOptions = {
  stages: [
    { duration: '2m', target: 50 },
    { duration: '5m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 0 },
  ],
};

export const spikeOptions = {
  stages: [
    { duration: '10s', target: 100 },
    { duration: '1m', target: 100 },
    { duration: '10s', target: 0 },
  ],
};
```

#### Regla G-07: Datos de prueba
Los payloads de prueba deben residir en archivos JSON dentro de `data/`. El agente debe generar al menos **3 registros distintos** por cada endpoint POST, variando `transaction_id`, `reference_id` y montos.

#### Regla G-08: Métricas personalizadas
Cada script debe declarar métricas personalizadas para el endpoint:
```javascript
import { Trend, Rate, Counter } from 'k6/metrics';

const payinDuration = new Trend('payin_transaction_duration');
const payinErrors = new Rate('payin_transaction_errors');
const payinRequests = new Counter('payin_transaction_requests');
```

#### Regla G-09: Manejo de errores y logging
En caso de fallo de check, loguear la respuesta completa:
```javascript
if (!checkResult) {
  console.error(`[ERROR] ${url} → status=${response.status} body=${response.body}`);
}
```

#### Regla G-10: Modularidad
Cada script de escenario debe poder ejecutarse de forma **independiente** mediante:
```bash
k6 run automation/performance/k6/payin-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-key \
  -e TEST_TYPE=smoke
```
Y también importarse desde `main.js` como escenario independiente.

---

## 6. Escenarios de Performance Requeridos por Controlador

### 6.1 `payin-test.js` — `PayInTransactionController`

| # | Nombre del escenario k6 | Tipo de carga | Descripción |
|---|---|---|---|
| S-01 | `payin_smoke` | Smoke (1 VU / 30s) | Verificar que el endpoint responde correctamente bajo carga mínima. |
| S-02 | `payin_load` | Load (10 VUs / 5m rampa) | Simular carga sostenida representativa de producción. |
| S-03 | `payin_stress` | Stress (hasta 100 VUs) | Determinar el punto de quiebre del endpoint bajo carga extrema. |
| S-04 | `payin_spike` | Spike (100 VUs / 10s) | Simular pico súbito de tráfico para detectar degradación. |

**Pasos del flujo k6 (`default function`):**

1. Seleccionar aleatoriamente un payload del dataset `data/payin-payload.json`.
2. Sustituir `transaction_id` con un UUID fresco (garantizar unicidad por iteración).
3. Construir headers con `X-Idempotency-Key` único y `X-Api-Key`.
4. Ejecutar `http.post(url, JSON.stringify(payload), { headers })`.
5. Registrar métricas personalizadas (`payinDuration`, `payinErrors`, `payinRequests`).
6. Ejecutar checks de respuesta (ver Regla G-04).
7. `sleep(1)` entre iteraciones.

**Checks requeridos:**
```javascript
check(response, {
  'payin HTTP 200': (r) => r.status === 200,
  'payin code PROCESS_COMPLETED': (r) => JSON.parse(r.body).code === 'PROCESS_COMPLETED',
  'payin status SUCCESS': (r) => JSON.parse(r.body).status === 'SUCCESS',
  'payin adapter_transaction_id presente': (r) => !!JSON.parse(r.body).data?.adapter_transaction_id,
  'payin response time < 2s': (r) => r.timings.duration < 2000,
});
```

**Dataset mínimo `data/payin-payload.json`:**
```json
[
  {
    "transaction": {
      "transaction_id": "txn-perf-001",
      "reference_id": "ref-perf-001",
      "amount": { "value": 10000, "currency": "COP" },
      "country": "CO",
      "payment_method": "CARD",
      "description": "K6 Performance Test - PayIn 01",
      "expiration_minutes": 30
    },
    "customer": {
      "customer_id": "cust-perf-001",
      "document": { "document_type": "CC", "document_number": "123456789" },
      "first_name": "Juan",
      "last_name": "Perez",
      "email": "juan.perez@test.com",
      "phone": "3001234567",
      "person_type": "INDIVIDUAL"
    },
    "merchant": {
      "merchant_id": "m-perf-001",
      "code": "TUMIPAY",
      "name": "TumiPay Store",
      "document": { "document_type": "NIT", "document_number": "900123456" }
    },
    "webhook_url": "https://example.com/webhook/payin",
    "metadata": { "test_run": "k6-performance", "order_id": "ORD-PERF-001" }
  },
  {
    "transaction": {
      "transaction_id": "txn-perf-002",
      "reference_id": "ref-perf-002",
      "amount": { "value": 50000, "currency": "COP" },
      "country": "CO",
      "payment_method": "PSE",
      "description": "K6 Performance Test - PayIn 02",
      "expiration_minutes": 60
    },
    "customer": {
      "customer_id": "cust-perf-002",
      "document": { "document_type": "CE", "document_number": "987654321" },
      "first_name": "Maria",
      "last_name": "Lopez",
      "email": "maria.lopez@test.com",
      "phone": "3109876543",
      "person_type": "INDIVIDUAL"
    },
    "merchant": {
      "merchant_id": "m-perf-002",
      "code": "TUMIPAY",
      "name": "TumiPay Store",
      "document": { "document_type": "NIT", "document_number": "900123456" }
    },
    "webhook_url": "https://example.com/webhook/payin",
    "metadata": { "test_run": "k6-performance", "order_id": "ORD-PERF-002" }
  },
  {
    "transaction": {
      "transaction_id": "txn-perf-003",
      "reference_id": "ref-perf-003",
      "amount": { "value": 150000, "currency": "COP" },
      "country": "CO",
      "payment_method": "CASH",
      "description": "K6 Performance Test - PayIn 03",
      "expiration_minutes": 120
    },
    "customer": {
      "customer_id": "cust-perf-003",
      "document": { "document_type": "CC", "document_number": "111222333" },
      "first_name": "Carlos",
      "last_name": "Garcia",
      "email": "carlos.garcia@test.com",
      "phone": "3201112222",
      "person_type": "LEGAL"
    },
    "merchant": {
      "merchant_id": "m-perf-003",
      "code": "TUMIPAY",
      "name": "TumiPay Store",
      "document": { "document_type": "NIT", "document_number": "900123456" }
    },
    "webhook_url": "https://example.com/webhook/payin",
    "metadata": { "test_run": "k6-performance", "order_id": "ORD-PERF-003" }
  }
]
```

---

### 6.2 `payout-test.js` — `PayOutTransactionController`

| # | Nombre del escenario k6 | Tipo de carga | Descripción |
|---|---|---|---|
| S-01 | `payout_smoke` | Smoke (1 VU / 30s) | Verificar que el endpoint PayOut responde correctamente bajo carga mínima. |
| S-02 | `payout_load` | Load (10 VUs / 5m rampa) | Simular carga sostenida representativa. |
| S-03 | `payout_stress` | Stress (hasta 100 VUs) | Determinar el punto de quiebre del endpoint PayOut. |
| S-04 | `payout_spike` | Spike (100 VUs / 10s) | Simular pico súbito de tráfico. |

**Checks requeridos:**
```javascript
check(response, {
  'payout HTTP 200': (r) => r.status === 200,
  'payout code PROCESS_COMPLETED': (r) => JSON.parse(r.body).code === 'PROCESS_COMPLETED',
  'payout status SUCCESS': (r) => JSON.parse(r.body).status === 'SUCCESS',
  'payout adapter_transaction_id presente': (r) => !!JSON.parse(r.body).data?.adapter_transaction_id,
  'payout response time < 2s': (r) => r.timings.duration < 2000,
});
```

**Dataset mínimo `data/payout-payload.json`:**
```json
[
  {
    "transaction": {
      "transaction_id": "pout-perf-001",
      "reference_id": "ref-pout-001",
      "amount": { "value": 20000, "currency": "COP" },
      "country": "CO",
      "payment_method": "BANK_TRANSFER",
      "description": "K6 Performance Test - PayOut 01",
      "expiration_minutes": 30
    },
    "beneficiary": {
      "beneficiary_id": "ben-perf-001",
      "document": { "document_type": "CC", "document_number": "444555666" },
      "first_name": "Ana",
      "last_name": "Martinez",
      "email": "ana.martinez@test.com",
      "phone": "3154445555",
      "bank_account": {
        "bank_code": "001",
        "bank_name": "Banco de Bogotá",
        "account_type": "SAVINGS",
        "account_number": "123456789012"
      }
    },
    "webhook_url": "https://example.com/webhook/payout",
    "metadata": { "test_run": "k6-performance", "order_id": "POUT-PERF-001" }
  },
  {
    "transaction": {
      "transaction_id": "pout-perf-002",
      "reference_id": "ref-pout-002",
      "amount": { "value": 75000, "currency": "COP" },
      "country": "CO",
      "payment_method": "BANK_TRANSFER",
      "description": "K6 Performance Test - PayOut 02",
      "expiration_minutes": 60
    },
    "beneficiary": {
      "beneficiary_id": "ben-perf-002",
      "document": { "document_type": "NIT", "document_number": "900111222" },
      "first_name": "Pedro",
      "last_name": "Ramirez",
      "email": "pedro.ramirez@test.com",
      "phone": "3006667777",
      "bank_account": {
        "bank_code": "007",
        "bank_name": "Bancolombia",
        "account_type": "CHECKING",
        "account_number": "987654321098"
      }
    },
    "webhook_url": "https://example.com/webhook/payout",
    "metadata": { "test_run": "k6-performance", "order_id": "POUT-PERF-002" }
  },
  {
    "transaction": {
      "transaction_id": "pout-perf-003",
      "reference_id": "ref-pout-003",
      "amount": { "value": 200000, "currency": "COP" },
      "country": "CO",
      "payment_method": "BANK_TRANSFER",
      "description": "K6 Performance Test - PayOut 03",
      "expiration_minutes": 90
    },
    "beneficiary": {
      "beneficiary_id": "ben-perf-003",
      "document": { "document_type": "CE", "document_number": "777888999" },
      "first_name": "Laura",
      "last_name": "Torres",
      "email": "laura.torres@test.com",
      "phone": "3177778888",
      "bank_account": {
        "bank_code": "040",
        "bank_name": "Davivienda",
        "account_type": "SAVINGS",
        "account_number": "456789012345"
      }
    },
    "webhook_url": "https://example.com/webhook/payout",
    "metadata": { "test_run": "k6-performance", "order_id": "POUT-PERF-003" }
  }
]
```

---

### 6.3 `transaction-query-test.js` — `TransactionController`

| # | Nombre del escenario k6 | Tipo de carga | Descripción |
|---|---|---|---|
| S-01 | `query_smoke` | Smoke (1 VU / 30s) | Verificar la consulta de transacciones bajo carga mínima. |
| S-02 | `query_load` | Load (20 VUs / 5m rampa) | Carga sostenida: las consultas suelen ser más frecuentes que las transacciones. |
| S-03 | `query_stress` | Stress (hasta 150 VUs) | Determinar el punto de quiebre del endpoint de consulta. |

**Pasos del flujo k6:**

1. Alternar entre los tres query params (`transaction_id`, `adapter_transaction_id`, `provider_transaction_id`) usando módulo de iteración.
2. Obtener el ID desde `__ENV` (pre-cargado de una ejecución previa de PayIn) o usar un valor fijo de prueba.
3. Ejecutar `http.get(url, { headers })`.
4. Ejecutar checks.
5. `sleep(0.5)` entre iteraciones (consultas son más rápidas).

**Checks requeridos:**
```javascript
check(response, {
  'query HTTP 200 o 4xx': (r) => r.status === 200 || (r.status >= 400 && r.status < 500),
  'query response time < 1s': (r) => r.timings.duration < 1000,
  'query body válido': (r) => {
    try { JSON.parse(r.body); return true; } catch { return false; }
  },
});
```

---

### 6.4 `microservice-info-test.js` — `MicroServiceController`

| # | Nombre del escenario k6 | Tipo de carga | Descripción |
|---|---|---|---|
| S-01 | `info_smoke` | Smoke (1 VU / 30s) | Verificar disponibilidad del endpoint de info. |
| S-02 | `info_load` | Load (50 VUs / 3m) | Carga alta: este endpoint es usado por health checks frecuentes. |

**Checks requeridos:**
```javascript
check(response, {
  'info HTTP 200': (r) => r.status === 200,
  'info code PROCESS_COMPLETED': (r) => JSON.parse(r.body).code === 'PROCESS_COMPLETED',
  'info service_name presente': (r) => !!JSON.parse(r.body).data?.service_name,
  'info response time < 500ms': (r) => r.timings.duration < 500,
});
```

---

## 7. Estructura de Archivos Generados

El agente debe generar exactamente la siguiente estructura de archivos:

```
automation/performance/k6/
├── main.js                        ← Orquestador principal
├── payin-test.js                  ← Escenarios PayIn
├── payout-test.js                 ← Escenarios PayOut
├── transaction-query-test.js      ← Escenarios consulta de transacciones
├── microservice-info-test.js      ← Escenarios info del microservicio
├── config/
│   ├── options.js                 ← Definiciones de smoke/load/stress/spike
│   └── thresholds.js              ← Umbrales globales de performance
├── data/
│   ├── payin-payload.json         ← Dataset PayIn (mínimo 3 registros)
│   └── payout-payload.json        ← Dataset PayOut (mínimo 3 registros)
└── utils/
    └── helpers.js                 ← uuidv4(), buildHeaders(), parseBody()
```

---

## 8. Template de Script de Escenario

Cada archivo de escenario (`*-test.js`) debe seguir esta estructura:

```javascript
/**
 * [NombreControlador] - k6 Performance Test
 *
 * Controlador: [NombreControlador]
 * Endpoint: [MÉTODO] [PATH]
 * Autor: TumiPay SAS — Engineering Standards Team
 * Fecha: [FECHA]
 *
 * Uso:
 *   k6 run automation/performance/k6/[nombre]-test.js \
 *     -e BASE_URL=http://localhost:8080 \
 *     -e API_KEY=my-key \
 *     -e TEST_TYPE=smoke
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';
import { smokeOptions, loadOptions, stressOptions, spikeOptions } from './config/options.js';
import { thresholds } from './config/thresholds.js';
import { uuidv4, buildHeaders, parseBody } from './utils/helpers.js';

// ── Métricas personalizadas ──────────────────────────────────────────────────
const [nombre]Duration  = new Trend('[nombre]_duration');
const [nombre]Errors    = new Rate('[nombre]_errors');
const [nombre]Requests  = new Counter('[nombre]_requests');

// ── Configuración de opciones por tipo de prueba ─────────────────────────────
const TEST_TYPE = __ENV.TEST_TYPE || 'smoke';
const optionsMap = {
  smoke:  { ...smokeOptions,  thresholds },
  load:   { ...loadOptions,   thresholds },
  stress: { ...stressOptions, thresholds },
  spike:  { ...spikeOptions,  thresholds },
};
export const options = optionsMap[TEST_TYPE] || optionsMap.smoke;

// ── Datos de prueba ───────────────────────────────────────────────────────────
const payloads = JSON.parse(open('./data/[nombre]-payload.json'));

// ── Función principal ─────────────────────────────────────────────────────────
export default function () {
  const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
  const url = `${BASE_URL}/tp/payment/adapter/v1/[path]/transaction`;

  // Seleccionar payload aleatorio y sustituir transaction_id
  const payload = JSON.parse(JSON.stringify(payloads[Math.floor(Math.random() * payloads.length)]));
  payload.transaction.transaction_id = uuidv4();

  const headers = buildHeaders({
    idempotency: true,
    merchantId: __ENV.MERCHANT_ID || 'test-merchant',
    apiKey: __ENV.API_KEY || 'test-api-key',
  });

  const startTime = Date.now();
  const response = http.post(url, JSON.stringify(payload), { headers });
  [nombre]Duration.add(Date.now() - startTime);
  [nombre]Requests.add(1);

  const passed = check(response, {
    '[nombre] HTTP 200': (r) => r.status === 200,
    '[nombre] code PROCESS_COMPLETED': (r) => parseBody(r).code === 'PROCESS_COMPLETED',
    '[nombre] status SUCCESS': (r) => parseBody(r).status === 'SUCCESS',
    '[nombre] data presente': (r) => !!parseBody(r).data,
    '[nombre] response time < 2s': (r) => r.timings.duration < 2000,
  });

  [nombre]Errors.add(!passed);

  if (!passed) {
    console.error(`[ERROR] ${url} → status=${response.status} body=${response.body}`);
  }

  sleep(1);
}
```

---

## 9. Template `main.js` (Orquestador)

```javascript
/**
 * main.js — Orquestador de pruebas de performance k6
 *
 * Ejecuta todos los escenarios de performance del microservicio
 * tumipay-ms-payment-provider-template en paralelo usando
 * la API de escenarios de k6.
 *
 * Uso:
 *   k6 run automation/performance/k6/main.js \
 *     -e BASE_URL=http://localhost:8080 \
 *     -e API_KEY=my-key \
 *     -e TEST_TYPE=load
 */

import { thresholds } from './config/thresholds.js';
import { loadOptions } from './config/options.js';

export { default as payinTest }       from './payin-test.js';
export { default as payoutTest }      from './payout-test.js';
export { default as queryTest }       from './transaction-query-test.js';
export { default as infoTest }        from './microservice-info-test.js';

export const options = {
  thresholds,
  scenarios: {
    payin: {
      executor: 'ramping-vus',
      exec: 'payinTest',
      stages: loadOptions.stages,
      startVUs: 0,
    },
    payout: {
      executor: 'ramping-vus',
      exec: 'payoutTest',
      stages: loadOptions.stages,
      startVUs: 0,
      startTime: '30s',
    },
    query: {
      executor: 'ramping-vus',
      exec: 'queryTest',
      stages: loadOptions.stages,
      startVUs: 0,
      startTime: '60s',
    },
    info: {
      executor: 'constant-vus',
      exec: 'infoTest',
      vus: 5,
      duration: '5m',
      startTime: '0s',
    },
  },
};
```

---

## 10. Template `utils/helpers.js`

```javascript
/**
 * helpers.js — Utilidades compartidas para scripts k6
 */

/**
 * Genera un UUID v4 compatible con el entorno de k6.
 * @returns {string} UUID en formato xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
 */
export function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0;
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
}

/**
 * Construye los headers estándar de TumiPay.
 * @param {object} opts Opciones de configuración de headers
 * @param {boolean} [opts.idempotency=false] Si true, agrega X-Idempotency-Key
 * @param {string}  [opts.merchantId]        Valor de X-Merchant-ID
 * @param {string}  [opts.apiKey]            Valor de X-Api-Key
 * @returns {object} Headers HTTP listos para usar en k6
 */
export function buildHeaders({ idempotency = false, merchantId, apiKey } = {}) {
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Request-ID': uuidv4(),
    'X-Correlation-ID': uuidv4(),
  };

  if (apiKey) {
    headers['X-Api-Key'] = apiKey;
  }
  if (merchantId) {
    headers['X-Merchant-ID'] = merchantId;
  }
  if (idempotency) {
    headers['X-Idempotency-Key'] = uuidv4();
  }

  return headers;
}

/**
 * Parsea el body de una respuesta k6 de forma segura.
 * @param {object} response Objeto response de k6
 * @returns {object} JSON parseado o {} en caso de error
 */
export function parseBody(response) {
  try {
    return JSON.parse(response.body);
  } catch {
    return {};
  }
}
```

---

## 11. Variables de Entorno Requeridas

El agente debe documentar las siguientes variables de entorno en el README del directorio `automation/performance/k6/`:

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `BASE_URL` | URL base del microservicio | `http://localhost:8080` |
| `API_KEY` | API Key para autenticación (`X-Api-Key`) | `test-api-key` |
| `MERCHANT_ID` | ID del merchant (`X-Merchant-ID`) | `test-merchant` |
| `TEST_TYPE` | Tipo de prueba: `smoke`, `load`, `stress`, `spike` | `smoke` |
| `TRANSACTION_ID` | ID de transacción pre-existente para pruebas de consulta | `""` |
| `ADAPTER_TRANSACTION_ID` | ID de transacción del adaptador para consulta | `""` |
| `PROVIDER_TRANSACTION_ID` | ID de transacción del proveedor para consulta | `""` |

---

## 12. Thresholds Globales (config/thresholds.js)

```javascript
/**
 * thresholds.js — Umbrales de performance globales para TumiPay
 *
 * Referencia:
 *   - p95 < 2000ms : El 95% de las requests deben responder en menos de 2 segundos.
 *   - p99 < 5000ms : El 99% de las requests deben responder en menos de 5 segundos.
 *   - error rate < 1% : La tasa de error no debe superar el 1%.
 */
export const thresholds = {
  // Métricas HTTP globales
  http_req_duration: ['p(95)<2000', 'p(99)<5000'],
  http_req_failed:   ['rate<0.01'],

  // Métricas por escenario PayIn
  payin_duration:    ['p(95)<2000'],
  payin_errors:      ['rate<0.01'],

  // Métricas por escenario PayOut
  payout_duration:   ['p(95)<2000'],
  payout_errors:     ['rate<0.01'],

  // Métricas por escenario consulta (más rápido por ser GET)
  query_duration:    ['p(95)<1000'],
  query_errors:      ['rate<0.01'],

  // Métricas por escenario info (muy rápido)
  info_duration:     ['p(95)<500'],
  info_errors:       ['rate<0.005'],
};
```

---

## 13. README del Directorio k6

El agente debe crear o actualizar `automation/performance/k6/README.md` con:

- Descripción general del suite de performance.
- Requisitos previos (k6 instalado: `brew install k6` / `choco install k6`).
- Tabla de comandos de ejecución por escenario y tipo de prueba.
- Ejemplo de salida esperada.
- Instrucciones para interpretar los resultados (thresholds pasados/fallados).

**Ejemplo de comandos que debe incluir el README:**

```bash
# Prueba de humo - PayIn
k6 run automation/performance/k6/payin-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=smoke

# Prueba de carga - PayOut
k6 run automation/performance/k6/payout-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=load

# Prueba de estrés - Consulta de Transacciones
k6 run automation/performance/k6/transaction-query-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=stress

# Ejecución completa de todos los escenarios
k6 run automation/performance/k6/main.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=load

# Ejecución con salida en JSON para reporte
k6 run automation/performance/k6/main.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=load \
  --out json=automation/performance/k6/results/result.json
```

---

## 14. Criterios de Aceptación del Agente

El agente habrá completado su tarea satisfactoriamente cuando:

- [ ] **C-01** Todos los archivos listados en la Sección 7 existen en `automation/performance/k6/`.
- [ ] **C-02** Cada script de escenario puede ejecutarse de forma independiente con `k6 run`.
- [ ] **C-03** `main.js` importa correctamente todos los escenarios y los orquesta en paralelo.
- [ ] **C-04** `utils/helpers.js` implementa `uuidv4()`, `buildHeaders()` y `parseBody()`.
- [ ] **C-05** `config/options.js` define `smokeOptions`, `loadOptions`, `stressOptions` y `spikeOptions`.
- [ ] **C-06** `config/thresholds.js` define umbrales para métricas globales y por escenario.
- [ ] **C-07** Los datasets en `data/` tienen al menos 3 registros válidos por endpoint POST.
- [ ] **C-08** Cada script registra métricas personalizadas con `Trend`, `Rate` y `Counter`.
- [ ] **C-09** Los headers `X-Api-Key`, `X-Request-ID`, `X-Idempotency-Key` y `X-Merchant-ID` usan los **valores exactos** de `BaseIntegrationConstant` (sin valores hardcoded distintos a las constantes).
- [ ] **C-10** Todos los scripts son JavaScript ES6+ válido y compatible con k6 (sin dependencias de Node.js).
- [ ] **C-11** Existe un archivo `automation/performance/k6/README.md` con instrucciones completas.
- [ ] **C-12** Las URLs de todos los endpoints siguen el patrón `${BASE_URL}/tp/payment/adapter/v1/<path>`.
- [ ] **C-13** El campo `transaction_id` se sustituye con `uuidv4()` en cada iteración para garantizar unicidad.
- [ ] **C-14** Ningún script usa `import` de módulos de Node.js incompatibles con k6 (e.g., `crypto`, `fs`).

---

## 15. Archivos de Referencia del Proyecto

| Archivo | Propósito |
|---|---|
| `src/main/java/.../http/standard/MicroServiceController.java` | Controlador info del servicio |
| `src/main/java/.../http/standard/PayInTransactionController.java` | Controlador Pay-In |
| `src/main/java/.../http/standard/PayOutTransactionController.java` | Controlador Pay-Out |
| `src/main/java/.../http/standard/TransactionController.java` | Controlador consulta de transacciones |
| `src/main/java/.../http/request/PayInTransactionRequest.java` | DTO request Pay-In |
| `src/main/java/.../http/request/PayOutTransactionRequest.java` | DTO request Pay-Out |
| `src/main/java/.../http/response/PayInTransactionResponse.java` | DTO response Pay-In |
| `src/main/java/.../http/response/PayOutTransactionResponse.java` | DTO response Pay-Out |
| `src/main/java/.../http/response/AdapterTransactionResponse.java` | DTO response consulta |
| `src/main/java/.../http/response/MicroServiceInfoResponse.java` | DTO response info servicio |
| `src/main/java/.../component/constant/BaseIntegrationConstant.java` | Constantes de headers HTTP |
| `src/main/java/.../domain/model/Transaction.java` | Modelo de dominio Transaction |
| `src/main/java/.../domain/model/Customer.java` | Modelo de dominio Customer |
| `src/main/java/.../domain/model/Merchant.java` | Modelo de dominio Merchant |
| `src/main/java/.../domain/model/Beneficiary.java` | Modelo de dominio Beneficiary |
| `src/main/java/.../domain/model/Amount.java` | Modelo de dominio Amount |
| `src/main/java/.../domain/model/Document.java` | Modelo de dominio Document |
| `src/main/java/.../domain/model/BankAccount.java` | Modelo de dominio BankAccount |
| `automation/performance/k6/` | Directorio destino de los scripts k6 |

---

*Este documento es la fuente de verdad para el agente. Ante cualquier ambigüedad, el agente debe priorizar los valores literales encontrados en `BaseIntegrationConstant.java` y los contratos definidos en los DTOs de request/response sobre cualquier otra fuente.*

