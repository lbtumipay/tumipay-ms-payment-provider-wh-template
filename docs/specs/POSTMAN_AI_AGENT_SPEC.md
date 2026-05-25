# Especificación Técnica: Agente de IA para Ajuste de Pruebas Automatizadas Postman

> **Versión:** 1.0.0  
> **Fecha:** 2026-04-08  
> **Proyecto:** `tumipay-ms-payment-provider-template`  
> **Autor:** TumiPay SAS — Engineering Standards Team

---

## 1. Objetivo

Este documento define el contrato técnico que debe seguir un **Agente de IA** para inspeccionar los controladores REST del microservicio y ajustar de forma autónoma las colecciones de pruebas automatizadas de Postman ubicadas en `test/postman/collections/`.

El agente debe ser capaz de:

1. Leer los controladores de los paquetes fuente definidos.
2. Detectar discrepancias entre el código y las colecciones existentes.
3. Corregir las colecciones existentes y crear las faltantes.
4. Garantizar que cada endpoint tenga cobertura de pruebas para los escenarios mínimos requeridos.

---

## 2. Alcance

### 2.1 Paquetes fuente a inspeccionar

| Paquete | Descripción |
|---|---|
| `com.tumipay.microservice.infrastructure.adapter.input.http.standard` | Controladores estándar de operaciones de negocio |
| `com.tumipay.microservice.infrastructure.adapter.input.http.provider` | Controladores de integración con el proveedor de pago (webhooks) |

### 2.2 Archivos de colecciones objetivo

| Archivo | Controlador asociado |
|---|---|
| `test/postman/collections/payin_collection.json` | `PayInTransactionController` |
| `test/postman/collections/payout_collection.json` | `PayOutTransactionController` |
| `test/postman/collections/query_collection.json` | `TransactionController` |
| `test/postman/collections/webhook_collection.json` | `WebhookController` |
| `test/postman/collections/microservice_collection.json` | `MicroServiceController` (**FALTANTE — debe crearse**) |

---

## 3. Inventario Actual de Controladores

El agente **debe** derivar esta información leyendo directamente el código fuente. A continuación se documenta el estado actual como referencia.

### 3.1 Paquete `standard`

#### `MicroServiceController`
```
@RequestMapping("/v1/microservice")
GET /info → Mono<ResponseEntity<BaseApiResponse<MicroServiceInfoResponse>>>
Headers requeridos: ninguno
Headers opcionales: ninguno
Respuesta OK: HTTP 200 + BaseApiResponse<MicroServiceInfoResponse>
```

#### `PayInTransactionController`
```
@RequestMapping("/v1/payin")
POST /transaction → Mono<ResponseEntity<BaseApiResponse<PayInTransactionResponse>>>
Headers requeridos: Content-Type: application/json
Headers opcionales: X-Idempotency-Key (BaseIntegrationConstant.HEADER_IDEMPOTENCY_KEY)
Body: PayInTransactionRequest (@Valid, @NotNull en campos: transaction, customer, merchant, webhook_url)
Respuesta OK: HTTP 200 + BaseApiResponse<PayInTransactionResponse>
```

#### `PayOutTransactionController`
```
@RequestMapping("/v1/payout")
POST /transaction → Mono<ResponseEntity<BaseApiResponse<PayOutTransactionResponse>>>
Headers requeridos: Content-Type: application/json
Headers opcionales: X-Idempotency-Key (BaseIntegrationConstant.HEADER_IDEMPOTENCY_KEY)
Body: PayOutTransactionRequest (@NotNull en: transaction, beneficiary; @NotBlank en: webhook_url)
Respuesta OK: HTTP 200 + BaseApiResponse<PayOutTransactionResponse>
```

#### `TransactionController`
```
@RequestMapping("/v1/transactions")
GET / → Mono<ResponseEntity<BaseApiResponse<AdapterTransactionResponse>>>
Headers requeridos: ninguno
Query params (todos opcionales):
  - transaction_id
  - adapter_transaction_id
  - provider_transaction_id
Respuesta OK: HTTP 200 + BaseApiResponse<AdapterTransactionResponse>
```

### 3.2 Paquete `provider`

#### `WebhookController`
```
@RequestMapping("/v1/webhook")
POST /event → Mono<ResponseEntity<WebhookEventResponse>>
Headers requeridos: Content-Type: application/json
Headers opcionales:
  - X-Api-Key        (BaseIntegrationConstant.HEADER_API_KEY)
  - X-Idempotency-Key (BaseIntegrationConstant.HEADER_IDEMPOTENCY_KEY)
  - X-Event-Id       (BaseIntegrationConstant.HEADER_EVENT_ID)   ← valor = "X-Event-Id"
  - X-Event-Type     (BaseIntegrationConstant.HEADER_EVENT_TYPE) ← valor = "X-Event-Type"
Body: WebhookEventRequest (todos los campos opcionales)
Respuesta OK: HTTP 200 + WebhookEventResponse  ← NO usa BaseApiResponse
Respuesta duplicado: HTTP 409 + WebhookEventResponse { code: "DUPLICATE_WEBHOOK_EVENT", message: "..." }
```

---

## 4. Contratos de Respuesta

### 4.1 `BaseApiResponse<T>` (controladores estándar)

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

### 4.2 `WebhookEventResponse` (controlador provider)

```json
{
  "code": "string",
  "message": "string"
}
```

| Escenario | `code` | HTTP |
|---|---|---|
| Evento procesado | *(definido por implementación)* | 200 |
| Evento duplicado | `DUPLICATE_WEBHOOK_EVENT` | 409 |

### 4.3 Cabeceras estándar (fuente: `BaseIntegrationConstant`)

| Nombre lógico | Valor del header HTTP | Usado en |
|---|---|---|
| `HEADER_REQUEST_ID` | `X-Request-ID` | Todos (trazabilidad) |
| `HEADER_IDEMPOTENCY_KEY` | `X-Idempotency-Key` | PayIn, PayOut, Webhook |
| `HEADER_MERCHANT_ID` | `X-Merchant-ID` | PayIn, PayOut |
| `HEADER_API_KEY` | `X-Api-Key` | Webhook |
| `HEADER_EVENT_ID` | `X-Event-Id` | Webhook |
| `HEADER_EVENT_TYPE` | `X-Event-Type` | Webhook |

---

## 5. Discrepancias Detectadas (Estado Actual)

El agente **debe detectar y corregir** las siguientes discrepancias entre el código fuente y las colecciones existentes.

### 5.1 `webhook_collection.json` — Headers incorrectos

| Elemento | Valor actual (incorrecto) | Valor correcto |
|---|---|---|
| Header evento ID | `X-Provider-Event-Id` | `X-Event-Id` |
| Header tipo evento | `X-Provider-Event-Type` | `X-Event-Type` |

**Fuente de verdad:** `BaseIntegrationConstant.HEADER_EVENT_ID = "X-Event-Id"` y `HEADER_EVENT_TYPE = "X-Event-Type"`.

### 5.2 `webhook_collection.json` — Test `03_idempotency_duplicate` incorrecto

| Elemento | Valor actual (incorrecto) | Valor correcto |
|---|---|---|
| Código de error duplicado | `DUPLICATE_TRANSACTION` | `DUPLICATE_WEBHOOK_EVENT` |
| Status del body | `FAILED` | Campo `status` **no existe** en `WebhookEventResponse` |
| HTTP esperado | No verificado | `409` |

**Fuente de verdad:** `WebhookController.handleOnDuplicateWebhookError()` retorna HTTP 409 con `WebhookEventResponse { code: "DUPLICATE_WEBHOOK_EVENT" }`.

### 5.3 Colección `microservice_collection.json` — FALTANTE

No existe colección para `GET /v1/microservice/info`. **El agente debe crearla.**

---

## 6. Instrucciones para el Agente

### 6.1 Proceso de lectura de fuentes

El agente debe leer en este orden:

1. Todos los archivos `.java` en los paquetes `standard` y `provider`.
2. Para cada controlador, extraer:
   - `@RequestMapping` base path.
   - Cada método HTTP (`@GetMapping`, `@PostMapping`, etc.) y su path.
   - Headers consumidos (`@RequestHeader`), incluyendo si son `required`.
   - Query params (`@RequestParam`), incluyendo si son `required`.
   - Tipo del body (`@RequestBody`) y sus validaciones (`@Valid`, `@NotNull`, `@NotBlank`, `@Pattern`).
   - Tipo de respuesta (si es `BaseApiResponse<T>` o directamente `WebhookEventResponse`).
   - Lógica `onErrorResume` para determinar códigos de error esperados.
3. Leer `BaseIntegrationConstant.java` para obtener los **valores exactos** de los headers.
4. Leer `BaseResponseConstant.java` para obtener los **códigos y status** estándar.

### 6.2 Reglas de validación de colecciones

Para cada endpoint detectado, el agente debe verificar:

#### Regla V-01: Nombre del header
El valor del campo `key` en el array `header` de la request Postman debe coincidir **exactamente** con la constante definida en `BaseIntegrationConstant`. Nunca usar valores literales que no estén respaldados por las constantes.

#### Regla V-02: URL base
Todas las URLs deben usar la variable `{{baseUrl}}` como prefijo. El path debe ser `/tp/payment/adapter` + el path del controlador.
- Ejemplo: `{{baseUrl}}/tp/payment/adapter/v1/payin/transaction`

#### Regla V-03: Estructura de aserciones de respuesta exitosa
Para respuestas envueltas en `BaseApiResponse`:
```javascript
pm.test('Status HTTP 200', () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.test('Código de respuesta correcto', () => pm.expect(json.code).to.eql('PROCESS_COMPLETED'));
pm.test('Status SUCCESS', () => pm.expect(json.status).to.eql('SUCCESS'));
pm.test('Data presente', () => pm.expect(json.data).to.be.an('object'));
```

#### Regla V-04: Estructura de aserciones para `WebhookEventResponse`
El cuerpo de respuesta **no** contiene `status`, solo `code` y `message`:
```javascript
pm.test('HTTP aceptado', () => pm.expect([200, 202]).to.include(pm.response.code));
const json = pm.response.json();
pm.test('code presente', () => pm.expect(json).to.have.property('code'));
pm.test('message presente', () => pm.expect(json).to.have.property('message'));
```

#### Regla V-05: Código de duplicado en Webhook
El único código de error de duplicado permitido para el webhook es `DUPLICATE_WEBHOOK_EVENT` (HTTP 409). No usar `DUPLICATE_TRANSACTION`.

#### Regla V-06: Campos de idempotencia
En el prerequest script de cualquier test que use `X-Idempotency-Key`, siempre generar un GUID fresco usando `pm.variables.replaceIn('{{$guid}}')`.

#### Regla V-07: Escenarios mínimos requeridos por tipo de endpoint

| Tipo de endpoint | Escenarios mínimos |
|---|---|
| POST con body + idempotencia | happy_path, validation_error_missing_required_field, idempotency_first_call, idempotency_duplicate |
| GET con query params | by_transaction_id, by_adapter_transaction_id, by_provider_transaction_id, not_found |
| POST webhook | valid_event, invalid_event_empty_body, idempotency_duplicate |
| GET info | happy_path, response_fields_validation |

### 6.3 Acciones de ajuste permitidas

| Acción | Descripción |
|---|---|
| **CORREGIR** | Modificar un item existente (header, URL, aserción) sin cambiar el nombre ni la posición. |
| **AGREGAR** | Añadir un nuevo item de test al final de la colección si un escenario no existe. |
| **CREAR** | Generar un archivo `.json` nuevo si no existe colección para un controlador. |
| **NO ELIMINAR** | El agente no debe eliminar ningún item existente sin aprobación explícita del usuario. |

---

## 7. Escenarios de Test Requeridos por Colección

### 7.1 `payin_collection.json`

| # | Nombre | Método | Path | Escenario |
|---|---|---|---|---|
| 01 | `01_happy_path` | POST | `/v1/payin/transaction` | Body completo y válido, headers de idempotencia frescos. |
| 02 | `02_validation_error_missing_required_field` | POST | `/v1/payin/transaction` | Body con `transaction` vacío, sin `customer` ni `merchant`. Espera HTTP 400, `code=VALIDATION_ERROR`, `status=ERROR`. |
| 03 | `03_idempotency_first_call` | POST | `/v1/payin/transaction` | Primera llamada con una clave de idempotencia guardada en env (`payin_idem_key`). Espera HTTP 200. |
| 04 | `04_idempotency_duplicate` | POST | `/v1/payin/transaction` | Segunda llamada con la misma `payin_idem_key`. Espera comportamiento de idempotencia según implementación. |

**Body de referencia mínimo (`01_happy_path`):**
```json
{
  "transaction": {
    "transaction_id": "{{transaction_id}}",
    "reference_id": "ref-001",
    "amount": { "value": 10000, "currency": "COP" },
    "country": "CO",
    "payment_method": "CARD",
    "description": "Test pago",
    "expiration_minutes": 30
  },
  "customer": {
    "customer_id": "cust-001",
    "document": { "document_type": "CC", "document_number": "123456789" },
    "first_name": "Juan", "last_name": "Perez",
    "email": "juan@test.com", "phone": "3001234567", "person_type": "INDIVIDUAL"
  },
  "merchant": {
    "merchant_id": "m-001", "code": "TUMIPAY", "name": "Store",
    "document": { "type": "NIT", "number": "900123456" }
  },
  "webhook_url": "https://example.com/webhook",
  "metadata": { "order_id": "ORD-001" }
}
```

**Headers requeridos:**
- `Content-Type: application/json`
- `X-Idempotency-Key: {{idempotency_key}}`

**Aserciones `01_happy_path`:**
```javascript
pm.test('Status 200', () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.test('code PROCESS_COMPLETED', () => pm.expect(json.code).to.eql('PROCESS_COMPLETED'));
pm.test('status SUCCESS', () => pm.expect(json.status).to.eql('SUCCESS'));
pm.test('adapter_transaction_id presente', () => pm.expect(json.data).to.have.property('adapter_transaction_id'));
pm.test('provider_transaction_id presente', () => pm.expect(json.data).to.have.property('provider_transaction_id'));
pm.environment.set('adapter_transaction_id', json.data.adapter_transaction_id);
pm.environment.set('provider_transaction_id', json.data.provider_transaction_id);
pm.environment.set('transaction_id', json.data.transaction_id);
```

---

### 7.2 `payout_collection.json`

| # | Nombre | Método | Path | Escenario |
|---|---|---|---|---|
| 01 | `01_happy_path` | POST | `/v1/payout/transaction` | Body completo con `transaction`, `beneficiary`, `webhook_url`. |
| 02 | `02_validation_error_missing_beneficiary` | POST | `/v1/payout/transaction` | Body sin `beneficiary`. Espera HTTP 400, `code=VALIDATION_ERROR`. |
| 03 | `03_idempotency_first_call` | POST | `/v1/payout/transaction` | Primera llamada con `payout_idem_key`. |
| 04 | `04_idempotency_duplicate` | POST | `/v1/payout/transaction` | Segunda llamada con la misma `payout_idem_key`. |

**Headers requeridos:**
- `Content-Type: application/json`
- `X-Idempotency-Key: {{idempotency_key}}`

---

### 7.3 `query_collection.json`

| # | Nombre | Método | Path | Escenario |
|---|---|---|---|---|
| 01 | `01_by_transaction_id` | GET | `/v1/transactions?transaction_id={{transaction_id}}` | Buscar por `transaction_id` guardado en env. Espera HTTP 200 y match del ID. |
| 02 | `02_by_adapter_transaction_id` | GET | `/v1/transactions?adapter_transaction_id={{adapter_transaction_id}}` | Buscar por `adapter_transaction_id`. |
| 03 | `03_by_provider_transaction_id` | GET | `/v1/transactions?provider_transaction_id={{provider_transaction_id}}` | Buscar por `provider_transaction_id`. |
| 04 | `04_not_found` | GET | `/v1/transactions?transaction_id=NON_EXISTENT_ID` | Buscar un ID inexistente. Espera HTTP 4xx o 5xx. |

**Aserciones mínimas `01_by_transaction_id`:**
```javascript
pm.test('Status 200', () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.test('code PROCESS_COMPLETED', () => pm.expect(json.code).to.eql('PROCESS_COMPLETED'));
pm.test('status SUCCESS', () => pm.expect(json.status).to.eql('SUCCESS'));
pm.test('transaction_id coincide', () => pm.expect(json.data.transaction_id).to.eql(pm.environment.get('transaction_id')));
```

---

### 7.4 `webhook_collection.json`

| # | Nombre | Método | Path | Escenario |
|---|---|---|---|---|
| 01 | `01_valid_event` | POST | `/v1/webhook/event` | Payload completo con todos los campos del proveedor. |
| 02 | `02_invalid_event_empty_body` | POST | `/v1/webhook/event` | Body vacío `{}`. Espera HTTP 200/202 (webhook acepta campos opcionales). |
| 03 | `03_idempotency_duplicate` | POST | `/v1/webhook/event` | Segunda llamada con la misma `X-Idempotency-Key`. Espera HTTP 409, `code=DUPLICATE_WEBHOOK_EVENT`. |

**Headers requeridos `01_valid_event`:**
```
Content-Type: application/json
X-Api-Key: {{apiKey}}
X-Idempotency-Key: {{idempotency_key}}
X-Event-Id: {{providerEventId}}
X-Event-Type: {{providerEventType}}
```

**Body de referencia `01_valid_event`:**
```json
{
  "provider_event_id": "{{providerEventId}}",
  "provider_event_type": "TRANSACTION_STATUS_UPDATE",
  "provider_transaction_id": "{{provider_transaction_id}}",
  "provider_status": "SUCCESS",
  "provider_data": { "example": "ok" }
}
```

**Aserciones `01_valid_event`:**
```javascript
pm.test('HTTP aceptado', () => pm.expect([200, 202]).to.include(pm.response.code));
const json = pm.response.json();
pm.test('code presente', () => pm.expect(json).to.have.property('code'));
pm.test('message presente', () => pm.expect(json).to.have.property('message'));
// NO verificar json.status — WebhookEventResponse no tiene campo status
```

**Aserciones `03_idempotency_duplicate`:**
```javascript
pm.test('HTTP 409 Conflict', () => pm.response.to.have.status(409));
const json = pm.response.json();
pm.test('code DUPLICATE_WEBHOOK_EVENT', () => pm.expect(json.code).to.eql('DUPLICATE_WEBHOOK_EVENT'));
// NO verificar json.status — WebhookEventResponse no tiene campo status
```

---

### 7.5 `microservice_collection.json` (**NUEVA**)

| # | Nombre | Método | Path | Escenario |
|---|---|---|---|---|
| 01 | `01_happy_path` | GET | `/v1/microservice/info` | Sin headers especiales. Espera HTTP 200 con datos del servicio. |
| 02 | `02_response_fields_validation` | GET | `/v1/microservice/info` | Verificar que todos los campos de `MicroServiceInfoResponse` están presentes. |

**Aserciones `01_happy_path`:**
```javascript
pm.test('Status 200', () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.test('code PROCESS_COMPLETED', () => pm.expect(json.code).to.eql('PROCESS_COMPLETED'));
pm.test('status SUCCESS', () => pm.expect(json.status).to.eql('SUCCESS'));
```

**Aserciones `02_response_fields_validation`:**
```javascript
const json = pm.response.json();
const data = json.data;
pm.test('service_name presente', () => pm.expect(data).to.have.property('service_name'));
pm.test('service_description presente', () => pm.expect(data).to.have.property('service_description'));
pm.test('version presente', () => pm.expect(data).to.have.property('version'));
pm.test('environment presente', () => pm.expect(data).to.have.property('environment'));
pm.test('java_version presente', () => pm.expect(data).to.have.property('java_version'));
pm.test('spring_boot_version presente', () => pm.expect(data).to.have.property('spring_boot_version'));
pm.test('timestamp presente', () => pm.expect(data).to.have.property('timestamp'));
```

**Estructura del item Postman para `01_happy_path`:**
```json
{
  "name": "01_happy_path",
  "request": {
    "method": "GET",
    "header": [
      { "key": "Accept", "value": "application/json" }
    ],
    "url": "{{baseUrl}}/tp/payment/adapter/v1/microservice/info"
  },
  "event": [
    {
      "listen": "prerequest",
      "script": {
        "type": "text/javascript",
        "exec": [
          "pm.variables.set('request_id', pm.variables.replaceIn('{{$guid}}'));"
        ]
      }
    },
    {
      "listen": "test",
      "script": {
        "type": "text/javascript",
        "exec": [
          "pm.test('Status 200', () => pm.response.to.have.status(200));",
          "const json = pm.response.json();",
          "pm.test('code PROCESS_COMPLETED', () => pm.expect(json.code).to.eql('PROCESS_COMPLETED'));",
          "pm.test('status SUCCESS', () => pm.expect(json.status).to.eql('SUCCESS'));",
          "pm.test('data presente', () => pm.expect(json.data).to.be.an('object'));"
        ]
      }
    }
  ]
}
```

---

## 8. Variables de Entorno Requeridas

El agente debe verificar que el archivo de entorno (`local_environment.json`, `dev_environment.json`) contenga las siguientes variables. Si alguna falta, **debe agregarla**:

| Variable | Tipo | Descripción | Valor por defecto |
|---|---|---|---|
| `baseUrl` | string | URL base del servicio | `http://localhost:8000` |
| `merchant_id` | string | ID de merchant para pruebas | `example-merchant-id` |
| `apiKey` | string | API Key para autenticación de webhook | `your-api-key` |
| `request_id` | string | Generado por prerequest script | `""` |
| `idempotency_key` | string | Generado por prerequest script | `""` |
| `transaction_id` | string | Persistido entre tests | `""` |
| `adapter_transaction_id` | string | Persistido desde respuesta PayIn/PayOut | `""` |
| `provider_transaction_id` | string | Persistido desde respuesta PayIn | `""` |
| `payin_idem_key` | string | Clave fija para test idempotencia PayIn | `""` |
| `payout_idem_key` | string | Clave fija para test idempotencia PayOut | `""` |
| `fixed_idempotency` | string | Clave fija para test duplicado Webhook | `""` |
| `providerEventId` | string | ID de evento del proveedor | `""` |
| `providerEventType` | string | Tipo de evento del proveedor | `""` |

---

## 9. Estructura JSON de Referencia para Colecciones Nuevas

Cualquier colección creada debe seguir el schema `v2.1.0` de Postman:

```json
{
  "info": {
    "name": "TumiPay - <Nombre> Tests",
    "_postman_id": "<UUID generado>",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": []
}
```

---

## 10. Criterios de Aceptación del Agente

El agente habrá completado su tarea satisfactoriamente cuando:

- [ ] **C-01** Todas las colecciones existentes tienen los nombres de headers alineados con `BaseIntegrationConstant`.
- [ ] **C-02** `webhook_collection.json` usa `X-Event-Id` y `X-Event-Type` (no `X-Provider-*`).
- [ ] **C-03** `webhook_collection.json` test `03_idempotency_duplicate` verifica HTTP 409 y `code=DUPLICATE_WEBHOOK_EVENT`.
- [ ] **C-04** Ninguna colección de webhook verifica el campo `status` en el body (no existe en `WebhookEventResponse`).
- [ ] **C-05** El archivo `microservice_collection.json` existe con al menos 2 items de test.
- [ ] **C-06** Todos los archivos de entorno contienen las 13 variables listadas en la Sección 8.
- [ ] **C-07** No se eliminó ningún item de test existente.
- [ ] **C-08** Todos los archivos JSON son válidos (parseables sin errores).
- [ ] **C-09** Las URLs de todas las colecciones siguen el patrón `{{baseUrl}}/tp/payment/adapter/v1/<path>`.
- [ ] **C-10** Los prerequest scripts que generan IDs usan exclusivamente `pm.variables.replaceIn('{{$guid}}')`.

---

## 11. Archivos de Referencia del Proyecto

| Archivo | Propósito |
|---|---|
| `src/main/java/.../http/standard/MicroServiceController.java` | Controlador info del servicio |
| `src/main/java/.../http/standard/PayInTransactionController.java` | Controlador Pay-In |
| `src/main/java/.../http/standard/PayOutTransactionController.java` | Controlador Pay-Out |
| `src/main/java/.../http/standard/TransactionController.java` | Controlador consulta de transacciones |
| `src/main/java/.../http/provider/WebhookController.java` | Controlador webhook del proveedor |
| `src/main/java/.../http/request/PayInTransactionRequest.java` | DTO request Pay-In |
| `src/main/java/.../http/request/PayOutTransactionRequest.java` | DTO request Pay-Out |
| `src/main/java/.../http/request/WebhookEventRequest.java` | DTO request Webhook |
| `src/main/java/.../http/response/PayInTransactionResponse.java` | DTO response Pay-In |
| `src/main/java/.../http/response/PayOutTransactionResponse.java` | DTO response Pay-Out |
| `src/main/java/.../http/response/WebhookEventResponse.java` | DTO response Webhook |
| `src/main/java/.../http/response/AdapterTransactionResponse.java` | DTO response consulta |
| `src/main/java/.../http/response/MicroServiceInfoResponse.java` | DTO response info servicio |
| `src/main/java/.../component/constant/BaseIntegrationConstant.java` | Constantes de headers HTTP |
| `src/main/java/.../component/constant/BaseResponseConstant.java` | Constantes de códigos de respuesta |
| `src/main/java/.../component/dto/BaseApiResponse.java` | Wrapper estándar de respuestas API |
| `test/postman/collections/` | Directorio de colecciones Postman |
| `test/postman/enviroments/` | Directorio de ambientes Postman |
| `test/postman/scripts/run-tests.js` | Script ejecutor de Newman |

---

*Este documento es la fuente de verdad para el agente. Ante cualquier ambigüedad, el agente debe priorizar los valores literales encontrados en `BaseIntegrationConstant.java` y `BaseResponseConstant.java` sobre cualquier otra fuente.*

