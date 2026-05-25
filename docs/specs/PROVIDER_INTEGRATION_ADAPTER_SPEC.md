# Especificación Técnica: Integración con Payment Provider — Capa Output HTTP

> **Versión:** 1.0.0
> **Fecha:** 2026-04-08
> **Proyecto:** `tumipay-ms-payment-provider-template`
> **Paquete objetivo:** `com.tumipay.microservice.infrastructure.adapter.output.http`
> **Autor:** TumiPay SAS — Engineering Standards Team

---

## 1. Propósito y Alcance

Este documento define el contrato técnico que debe seguir un **Agente de IA** para reemplazar las implementaciones MOCK (`// TODO`) de los adaptadores de integración con el Payment Provider por llamadas HTTP reactivas reales utilizando `WebClient`.

### 1.1 Archivos objetivo

| Clase | Puerto que implementa | Estado actual |
|---|---|---|
| `ProviderTransactionIntegrationAdapter` | `IProviderTransactionIntegrationAdapterPort` | MOCK — todos los métodos retornan `Mono.just(...)` con datos ficticios |
| `ProviderSecurityIntegrationAdapter` | `IProviderSecurityIntegrationAdapterPort` | MOCK — genera tokens ficticios sin llamada HTTP real |

### 1.2 Premisa fundamental — Contratos del Proveedor son DESCONOCIDOS

Los contratos HTTP del Payment Provider externo (URLs, campos de Request, campos de Response) **no están definidos** en este documento porque son específicos de cada proveedor. El agente que implemente este código **debe recibir como input la documentación HTTP del proveedor** y mapear sus campos según las reglas descritas en las secciones siguientes.

Este documento define:
- ✅ **Dónde** colocar cada DTO de Request y Response.
- ✅ **Cómo** estructurar la cadena reactiva de cada operación.
- ✅ **Qué campos** de dominio están disponibles para el mapeo.
- ✅ **Qué campos** del resultado deben poblarse obligatoriamente.
- ✅ **Qué esquemas de seguridad** implementar y cómo.

Este documento **NO define**:
- ❌ Los nombres de campos específicos del proveedor externo.
- ❌ Las URLs exactas del proveedor (provienen de `PaymentProvidersProperties`).
- ❌ El formato de autenticación propietario del proveedor.

---

## 2. Inyecciones Disponibles

Ambas clases ya tienen declarados los siguientes beans por `@AllArgsConstructor`:

### 2.1 `ProviderTransactionIntegrationAdapter`

```java
private final PaymentProvidersProperties paymentProvidersProperties;
private final IProviderCredentialService providerCredentialService;
private final WebClientConfig webClientConfig;
```

### 2.2 `ProviderSecurityIntegrationAdapter`

```java
private final PaymentProvidersProperties paymentProvidersProperties;
private final WebClientConfig webClientConfig;
```

### 2.3 `PaymentProvidersProperties` — Propiedades clave

| Propiedad | Getter | Descripción |
|---|---|---|
| Código del proveedor | `.getCode()` | Identificador único del proveedor. Ej: `"TUMIPAY_EXAMPLE"` |
| Timeout general | `.getTimeout()` | Timeout en ms para llamadas HTTP |
| URL base de seguridad | `.getSecurity().getBaseUrl()` | Base URL del servidor de autenticación |
| Path generación de token | `.getSecurity().getEndpoints().getGenerateTokenPath()` | Ej: `/oauth/token` |
| Path refresh de token | `.getSecurity().getEndpoints().getRefreshTokenPath()` | Ej: `/oauth/refresh` |
| URL base de integración | `.getIntegration().getBaseUrl()` | Base URL de la API transaccional |
| Path PayIn | `.getIntegration().getEndpoints().getPayInTransactionPath()` | Ej: `/v1/payins` |
| Path PayOut | `.getIntegration().getEndpoints().getPayOutTransactionPath()` | Ej: `/v1/payouts` |
| Path consulta | `.getIntegration().getEndpoints().getGetTransactionPath()` | Ej: `/v1/transactions/{id}` |
| Tiempo expiración | `.getAuthorization().getExpirationTimeMs()` | TTL del token en ms |
| Umbral refresco | `.getAuthorization().getRefreshThresholdMs()` | Margen antes de expiración para refrescar |

### 2.4 `WebClient` — Estrategia de instanciación

El bean `WebClient` registrado en `WebClientConfig` usa la propiedad global `webclient.base-url`. Para las llamadas al proveedor **el agente DEBE crear instancias mutadas** que apunten a la URL correcta:

```java
// Para llamadas transaccionales (ProviderTransactionIntegrationAdapter)
WebClient transactionClient = webClientConfig.webClient(properties)
    .mutate()
    .baseUrl(paymentProvidersProperties.getIntegration().getBaseUrl())
    .build();

// Para llamadas de seguridad (ProviderSecurityIntegrationAdapter)
WebClient securityClient = webClientConfig.webClient(properties)
    .mutate()
    .baseUrl(paymentProvidersProperties.getSecurity().getBaseUrl())
    .build();
```

> ⚠️ **Regla obligatoria:** Nunca hardcodear URLs. Siempre usar `PaymentProvidersProperties`.

---

## 3. Estructura de Paquetes y DTOs

### 3.1 Mapa de paquetes

```
infrastructure.adapter.output.http/
│
├── ProviderTransactionIntegrationAdapter.java   ← MODIFICAR (reemplazar TODO)
├── ProviderSecurityIntegrationAdapter.java      ← MODIFICAR (reemplazar TODO)
│
├── request/                                     ← DTOs de REQUEST hacia el proveedor
│   ├── ProviderPayInRequest.java               ← EXPANDIR con campos reales del proveedor
│   ├── ProviderPayOutRequest.java              ← EXPANDIR con campos reales del proveedor
│   ├── ProviderTransactionRequest.java         ← EXPANDIR si aplica (query params)
│   │
│   └── security/                               ← CREAR sub-paquete para auth
│       ├── OAuthTokenRequest.java              ← CREAR si el esquema es OAUTH2 o JWE
│       ├── OAuthTokenRefreshRequest.java       ← CREAR si requiere refresh de token
│       └── [ProviderSpecificAuthRequest].java  ← CREAR si el esquema lo requiere
│
└── response/                                    ← DTOs de RESPONSE del proveedor
    ├── ProviderPayInResponse.java              ← EXPANDIR con campos reales del proveedor
    ├── ProviderPayOutResponse.java             ← EXPANDIR con campos reales del proveedor
    ├── ProviderTransactionStatusResponse.java  ← VER NOTA: clase normalizada TumiPay
    ├── ProviderTransactionDetailResponse.java  ← VER NOTA: clase normalizada TumiPay
    ├── ProviderTransactionResponse.java        ← EXPANDIR si aplica
    │
    └── security/                               ← CREAR sub-paquete para auth
        ├── OAuthTokenResponse.java             ← CREAR si el esquema es OAUTH2 o JWE
        └── [ProviderSpecificAuthResponse].java ← CREAR si el esquema lo requiere
```

> 📌 **NOTA sobre `ProviderTransactionStatusResponse` y `ProviderTransactionDetailResponse`:**
> Estas clases son el **modelo normalizado TumiPay** del resultado de consulta. Sus campos
> (`transactionId`, `providerTransactionId`, `operation`, `status`, `paymentMethod`, etc.)
> representan el contrato interno y **NO deben eliminarse**. El agente debe crear una clase
> RAW separada (ej. `ProviderRawTransactionStatusResponse`) para parsear la respuesta del
> proveedor y luego mapear sus campos al modelo normalizado TumiPay.

### 3.2 Reglas de estructura de DTOs

Todos los DTOs en `request/` y `response/` (incluyendo sub-paquetes) **deben seguir**:

```java
package com.tumipay.microservice.infrastructure.adapter.output.http.request; // o .response, .request.security, .response.security

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class ProviderXxxRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    // ↓ Campos definidos por el contrato HTTP del proveedor externo
    @JsonProperty("campo_del_proveedor")    // nombre exacto del JSON del proveedor
    private TipoJava campoJava;
}
```

### 3.3 Campos de dominio disponibles para mapeo en Request

El agente usa estos objetos de dominio recibidos como parámetros del método para construir el Request del proveedor:

#### Para `payInTransaction(PayInTransaction)`:

| Objeto | Campo | Tipo | Getter |
|---|---|---|---|
| `Transaction` | `transactionId` | `String` | `.getTransaction().getTransactionId()` |
| `Transaction` | `referenceId` | `String` | `.getTransaction().getReferenceId()` |
| `Transaction` | `amount.value` | `Integer` | `.getTransaction().getAmount().getValue()` |
| `Transaction` | `amount.currency` | `String` | `.getTransaction().getAmount().getCurrency()` |
| `Transaction` | `country` | `String` | `.getTransaction().getCountry()` |
| `Transaction` | `paymentMethod` | `PaymentMethodEnum` | `.getTransaction().getPaymentMethod()` |
| `Transaction` | `description` | `String` | `.getTransaction().getDescription()` |
| `Transaction` | `expirationMinutes` | `Integer` | `.getTransaction().getExpirationMinutes()` |
| `Customer` | `customerId` | `String` | `.getCustomer().getCustomerId()` |
| `Customer` | `document.documentType` | `String` | `.getCustomer().getDocument().getDocumentType()` |
| `Customer` | `document.documentNumber` | `String` | `.getCustomer().getDocument().getDocumentNumber()` |
| `Customer` | `firstName` | `String` | `.getCustomer().getFirstName()` |
| `Customer` | `lastName` | `String` | `.getCustomer().getLastName()` |
| `Customer` | `email` | `String` | `.getCustomer().getEmail()` |
| `Customer` | `phone` | `String` | `.getCustomer().getPhone()` |
| `Customer` | `personType` | `PersonTypeEnum` | `.getCustomer().getPersonType()` |
| `Merchant` | `merchantId` | `String` | `.getMerchant().getMerchantId()` |
| `Merchant` | `code` | `String` | `.getMerchant().getCode()` |
| `Merchant` | `name` | `String` | `.getMerchant().getName()` |
| `PayInTransaction` | `idempotencyKey` | `String` | `.getIdempotencyKey()` |
| `PayInTransaction` | `webhookUrl` | `String` | `.getWebhookUrl()` |
| `PayInTransaction` | `metadata` | `Map<String,Object>` | `.getMetadata()` |

#### Para `payOutTransaction(PayOutTransaction)`:

| Objeto | Campo | Tipo | Getter |
|---|---|---|---|
| `Transaction` | *(mismos campos que PayIn)* | — | `.getTransaction().*` |
| `Beneficiary` | `beneficiaryId` | `String` | `.getBeneficiary().getBeneficiaryId()` |
| `Beneficiary` | `document.documentType` | `String` | `.getBeneficiary().getDocument().getDocumentType()` |
| `Beneficiary` | `document.documentNumber` | `String` | `.getBeneficiary().getDocument().getDocumentNumber()` |
| `Beneficiary` | `firstName` | `String` | `.getBeneficiary().getFirstName()` |
| `Beneficiary` | `lastName` | `String` | `.getBeneficiary().getLastName()` |
| `Beneficiary` | `email` | `String` | `.getBeneficiary().getEmail()` |
| `Beneficiary` | `phone` | `String` | `.getBeneficiary().getPhone()` |
| `Beneficiary` | `bankAccount.accountNumber` | `String` | `.getBeneficiary().getBankAccount().getAccountNumber()` |
| `Beneficiary` | `bankAccount.accountType` | `String` | `.getBeneficiary().getBankAccount().getAccountType()` |
| `Beneficiary` | `bankAccount.bankCode` | `String` | `.getBeneficiary().getBankAccount().getBankCode()` |
| `PayOutTransaction` | `idempotencyKey` | `String` | `.getIdempotencyKey()` |
| `PayOutTransaction` | `webhookUrl` | `String` | `.getWebhookUrl()` |
| `PayOutTransaction` | `metadata` | `Map<String,Object>` | `.getMetadata()` |

#### Para `getTransactionStatus(String transactionId)` y `getTransactionDetail(String transactionId)`:

El único dato de entrada es el `transactionId` de TumiPay (tipo `String`). El agente debe decidir si este se envía como path variable o query param según la documentación del proveedor.

---

## 4. Implementación de `ProviderSecurityIntegrationAdapter`

### 4.1 Responsabilidad del método `generateAuthorization`

```java
Mono<AuthCredential> generateAuthorization(CredentialTypeEnum credentialType)
```

Este método debe generar o recuperar una credencial de autorización válida para consumir la API del proveedor. El comportamiento depende del `AuthSchemeEnum` que corresponda al `CredentialTypeEnum` recibido.

### 4.2 Resolución de esquema (ya existe en la clase — NO modificar)

```java
// Lógica ya implementada — el agente NO debe modificar este método
private AuthSchemeEnum resolveAuthScheme(CredentialTypeEnum credentialType) {
    return CredentialTypeEnum.API_KEY.equals(credentialType)
        ? AuthSchemeEnum.API_KEY
        : AuthSchemeEnum.OAUTH2;
}
```

> ⚠️ Si el proveedor requiere `JWE` o `BASIC`, el agente debe extender este método
> para cubrir los nuevos casos.

### 4.3 Implementación por `AuthSchemeEnum`

---

#### **Esquema: `OAUTH2`** (flujo client_credentials)

**DTOs a crear:**

`request/security/OAuthTokenRequest.java`:
```java
// Campos típicos — adaptar al contrato real del proveedor
@JsonProperty("grant_type")    private String grantType;      // "client_credentials"
@JsonProperty("client_id")     private String clientId;
@JsonProperty("client_secret") private String clientSecret;
@JsonProperty("scope")         private String scope;          // opcional
```

`response/security/OAuthTokenResponse.java`:
```java
// Campos típicos — adaptar al contrato real del proveedor
@JsonProperty("access_token")  private String accessToken;
@JsonProperty("token_type")    private String tokenType;      // "Bearer"
@JsonProperty("expires_in")    private Long expiresIn;        // segundos
@JsonProperty("scope")         private String scope;
```

**Cadena reactiva a implementar:**

```java
// 1. Construir el Request
final OAuthTokenRequest tokenRequest = OAuthTokenRequest.builder()
    .grantType("client_credentials")
    .clientId(/* del proveedor — viene de secreto/config */)
    .clientSecret(/* del proveedor — viene de secreto/config */)
    .scope(/* opcional */)
    .build();

// 2. Registrar tiempo de inicio
final Instant issuedAt = Instant.now();
final long expirationTimeMs = resolveExpirationTimeMs();

// 3. Llamada HTTP POST al endpoint de token
return securityWebClient
    .post()
    .uri(paymentProvidersProperties.getSecurity().getEndpoints().getGenerateTokenPath())
    .contentType(MediaType.APPLICATION_FORM_URLENCODED) // o APPLICATION_JSON según proveedor
    .bodyValue(tokenRequest)
    .retrieve()
    .onStatus(
        HttpStatusCode::isError,
        response -> response.bodyToMono(String.class)
            .flatMap(body -> Mono.error(
                new ProviderSecurityException("Token generation failed: " + body)
            ))
    )
    .bodyToMono(OAuthTokenResponse.class)
    // 4. Mapear a AuthCredential normalizado TumiPay
    .map(tokenResponse -> AuthCredential.builder()
        .adapterProviderCode(paymentProvidersProperties.getCode())
        .credentialType(CredentialTypeEnum.TOKEN)
        .value(tokenResponse.getAccessToken())
        .metadata(CredentialMetadata.builder()
            .scheme(AuthSchemeEnum.OAUTH2)
            .ttl(Duration.ofMillis(expirationTimeMs))
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusMillis(expirationTimeMs))
            .scope(tokenResponse.getScope())
            .build())
        .build()
    )
    .doOnError(e -> log.error("Error generating OAUTH2 token for provider {}: {}",
        paymentProvidersProperties.getCode(), e.getMessage()));
```

---

#### **Esquema: `API_KEY`**

**No requiere llamada HTTP.** La API Key proviene de configuración segura (variable de entorno o secreto).

**DTO:** No se necesita DTO de Request. El valor de la clave debe provenir de una propiedad configurada (ej. nueva propiedad en `PaymentProvidersProperties` o variable de entorno directa).

**Cadena reactiva:**

```java
final Instant issuedAt = Instant.now();
final long expirationTimeMs = resolveExpirationTimeMs();

// El valor de la API Key viene de configuración/secreto — NO hardcodear
final String apiKeyValue = paymentProvidersProperties./* nueva propiedad getApiKey() */;

return Mono.just(
    AuthCredential.builder()
        .adapterProviderCode(paymentProvidersProperties.getCode())
        .credentialType(CredentialTypeEnum.API_KEY)
        .value(apiKeyValue)
        .metadata(CredentialMetadata.builder()
            .scheme(AuthSchemeEnum.API_KEY)
            .ttl(Duration.ofMillis(expirationTimeMs))
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusMillis(expirationTimeMs))
            .scope("provider:api")
            .build())
        .build()
);
```

---

#### **Esquema: `JWE`** (JSON Web Encryption)

**DTOs a crear:** iguales a OAUTH2 pero con cabecera de firma adicional.
El proceso es similar a OAUTH2 con el agregado de firma/cifrado del request antes de enviarlo y descifrado/verificación del response.

**Cadena reactiva:** Igual a OAUTH2 pero incluir paso de firma antes del `.bodyValue(...)` y verificación de firma después del `.bodyToMono(...)`.

```java
// Antes de bodyValue: firmar/cifrar tokenRequest con clave privada del proveedor
// Después de bodyToMono: verificar firma del response con clave pública del proveedor
// Usar AuthSchemeEnum.JWE en el metadata
```

---

#### **Esquema: `BASIC`** (HTTP Basic Authentication)

**No requiere llamada HTTP.** Se construye el header directamente.

**Cadena reactiva:**

```java
final String username = /* de configuración */;
final String password = /* de secreto */;
final String encoded = Base64.getEncoder().encodeToString(
    (username + ":" + password).getBytes(StandardCharsets.UTF_8)
);

return Mono.just(
    AuthCredential.builder()
        .adapterProviderCode(paymentProvidersProperties.getCode())
        .credentialType(CredentialTypeEnum.TOKEN)
        .value(encoded)
        .metadata(CredentialMetadata.builder()
            .scheme(AuthSchemeEnum.BASIC)
            .ttl(Duration.ofMillis(resolveExpirationTimeMs()))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusMillis(resolveExpirationTimeMs()))
            .scope("provider:basic")
            .build())
        .build()
);
```

### 4.4 Validación obligatoria antes de retornar

En **todos** los esquemas, antes de retornar el `AuthCredential` se debe ejecutar la validación existente en `AuthCredentialValidation`:

```java
.flatMap(authCredential ->
    AuthCredentialValidation.validateAuthCredential().apply(authCredential)
        .flatMap(validationResult -> {
            if (!validationResult.isSuccess()) {
                return Mono.error(new ProviderSecurityException(
                    "Invalid credential generated: " + validationResult.getMessage()
                ));
            }
            return AuthCredentialValidation.validateCredentialMetadata()
                .apply(authCredential.getMetadata())
                .flatMap(metaResult -> {
                    if (!metaResult.isSuccess()) {
                        return Mono.error(new ProviderSecurityException(
                            "Invalid credential metadata: " + metaResult.getMessage()
                        ));
                    }
                    return Mono.just(authCredential);
                });
        })
)
```

---

## 5. Implementación de `ProviderTransactionIntegrationAdapter`

### 5.1 Patrón de obtención de credenciales (aplicar antes de CADA operación)

```java
private Mono<AuthCredential> resolveCredential(CredentialTypeEnum credentialType) {
    return providerCredentialService
        .getCredentialByProviderCode(paymentProvidersProperties.getCode())
        .flatMap(result -> {
            if (result.isSuccess() && result.getData() != null) {
                // Verificar si requiere refresco según umbral configurado
                long thresholdSeconds = paymentProvidersProperties.getAuthorization() != null
                    ? paymentProvidersProperties.getAuthorization().getRefreshThresholdMs() / 1000
                    : 300L;
                return providerCredentialService
                    .requiresRefresh(paymentProvidersProperties.getCode(), thresholdSeconds)
                    .flatMap(refreshResult -> {
                        if (Boolean.TRUE.equals(refreshResult.getData())) {
                            return refreshAndSaveCredential(credentialType);
                        }
                        return Mono.just(result.getData());
                    });
            }
            // No existe credential → generar y guardar
            return refreshAndSaveCredential(credentialType);
        });
}

private Mono<AuthCredential> refreshAndSaveCredential(CredentialTypeEnum credentialType) {
    return providerSecurityIntegrationAdapter        // ← inyectar esta dependencia
        .generateAuthorization(credentialType)
        .flatMap(credential -> providerCredentialService.saveCredential(credential))
        .map(DomainOperationResult::getData);
}
```

> **Nota:** Para que `ProviderTransactionIntegrationAdapter` llame a `ProviderSecurityIntegrationAdapter`,
> se debe **agregar `IProviderSecurityIntegrationAdapterPort` como dependencia inyectada** en la clase.
> El agente debe añadir:
> ```java
> private final IProviderSecurityIntegrationAdapterPort providerSecurityIntegrationAdapter;
> ```

### 5.2 Inyección de credencial en cabeceras HTTP

Según el `AuthSchemeEnum` de la credencial obtenida, el header HTTP varía:

| `AuthSchemeEnum` | Header a inyectar |
|---|---|
| `OAUTH2` | `Authorization: Bearer <value>` |
| `JWE` | `Authorization: Bearer <value>` |
| `API_KEY` | `X-Api-Key: <value>` *(o el nombre que defina el proveedor)* |
| `BASIC` | `Authorization: Basic <value>` |

```java
private WebClient buildAuthenticatedClient(AuthCredential credential) {
    WebClient base = transactionWebClient; // mutated con integration.baseUrl
    AuthSchemeEnum scheme = credential.getMetadata().getScheme();

    return switch (scheme) {
        case OAUTH2, JWE -> base.mutate()
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + credential.getValue())
            .build();
        case API_KEY -> base.mutate()
            .defaultHeader("X-Api-Key", credential.getValue()) // ajustar al header real
            .build();
        case BASIC -> base.mutate()
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credential.getValue())
            .build();
    };
}
```

### 5.3 Operación `payInTransaction`

**Objetivo:** Reemplazar el `Mono.just(...)` mock por una llamada HTTP POST real al proveedor.

**Cadena reactiva completa:**

```java
@Override
public Mono<PayInTransactionResult<?, ?>> payInTransaction(PayInTransaction payInTransaction) {

    return resolveCredential(CredentialTypeEnum.TOKEN) // o API_KEY según proveedor
        .flatMap(credential -> {

            final long startTime = System.currentTimeMillis();

            // 1. Mapear dominio → ProviderPayInRequest (campos definidos por el proveedor)
            final ProviderPayInRequest providerRequest = ProviderPayInRequest.builder()
                // ↓ TODO: el agente llena estos campos con los del proveedor real
                .transactionId(payInTransaction.getTransaction().getTransactionId())
                // ... resto de campos del proveedor
                .build();

            final String endpoint = paymentProvidersProperties.getIntegration()
                .getEndpoints().getPayInTransactionPath();

            // 2. Ejecutar llamada HTTP
            return buildAuthenticatedClient(credential)
                .post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(providerRequest)
                .retrieve()
                .onStatus(
                    HttpStatusCode::isError,
                    clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new WebClientResponseException(
                            clientResponse.statusCode().value(), body, null, null, null
                        )))
                )
                .bodyToMono(ProviderPayInResponse.class)
                // 3. Mapear ProviderPayInResponse → PayInTransactionResult
                .map(providerResponse -> PayInTransactionResult
                    .<ProviderPayInRequest, ProviderPayInResponse>builder()
                    .paymentAdapterCode(paymentProvidersProperties.getCode())
                    .providerTransactionId(providerResponse.getProviderTransactionId()) // campo del proveedor
                    .providerReferenceId(providerResponse.getProviderReferenceId())     // campo del proveedor
                    .httpMethod(HttpMethod.POST.name())
                    .httpStatusCode(HttpStatus.OK.value())
                    .providerEndpoint(endpoint)
                    .providerRequest(providerRequest)
                    .providerResponse(providerResponse)
                    .providerLatencyMs((int)(System.currentTimeMillis() - startTime))
                    .providerProcessedAt(Instant.now())
                    .status(OperationStatusEnum.SUCCESS.toString())
                    .message("PayIn transaction processed successfully")
                    .processedAt(Instant.now())
                    .build()
                )
                // 4. Manejo de error HTTP del proveedor
                .onErrorResume(WebClientResponseException.class, ex ->
                    Mono.just(PayInTransactionResult
                        .<ProviderPayInRequest, ProviderPayInResponse>builder()
                        .paymentAdapterCode(paymentProvidersProperties.getCode())
                        .httpMethod(HttpMethod.POST.name())
                        .httpStatusCode(ex.getStatusCode().value())
                        .providerEndpoint(endpoint)
                        .providerRequest(providerRequest)
                        .providerLatencyMs((int)(System.currentTimeMillis() - startTime))
                        .providerProcessedAt(Instant.now())
                        .status(OperationStatusEnum.FAILED.toString())
                        .errorCode("PROVIDER_HTTP_ERROR_" + ex.getStatusCode().value())
                        .errorMessage(ex.getResponseBodyAsString())
                        .processedAt(Instant.now())
                        .build()
                    )
                )
                .timeout(Duration.ofMillis(paymentProvidersProperties.getTimeout()));
        });
}
```

### 5.4 Operación `payOutTransaction`

**Idéntica a `payInTransaction`** con las siguientes diferencias:

1. El tipo genérico es `PayOutTransactionResult<ProviderPayOutRequest, ProviderPayOutResponse>`.
2. El endpoint es `paymentProvidersProperties.getIntegration().getEndpoints().getPayOutTransactionPath()`.
3. El objeto de dominio de entrada es `PayOutTransaction` (usa `beneficiary` en lugar de `customer`).
4. **Diferencia crítica:** El campo `synchronous` en `PayOutTransactionResult` debe poblarse:
   - `synchronous = true` → el proveedor retornó el resultado final en la misma respuesta HTTP.
   - `synchronous = false` → el proveedor aceptó la transacción pero el resultado llega por webhook.
   - El agente debe determinar este valor **según la documentación del proveedor** (generalmente viene en la respuesta del proveedor).

```java
.map(providerResponse -> PayOutTransactionResult
    .<ProviderPayOutRequest, ProviderPayOutResponse>builder()
    // ... mismos campos que PayIn ...
    .synchronous(/* true o false según respuesta del proveedor */)
    .build()
)
```

### 5.5 Operación `getTransactionStatus`

**Objetivo:** Consultar el estado actual de una transacción en el proveedor.

**Datos de entrada:** `String transactionId` (ID interno de TumiPay).

**Cadena reactiva:**

```java
@Override
public Mono<ProviderTransactionStatusResponse> getTransactionStatus(String transactionId) {

    return resolveCredential(CredentialTypeEnum.TOKEN)
        .flatMap(credential -> {

            // El path puede ser /v1/transactions/{transactionId} — adaptar al proveedor
            final String path = paymentProvidersProperties.getIntegration()
                .getEndpoints().getGetTransactionPath();

            return buildAuthenticatedClient(credential)
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path(path)
                    // Adaptar si es path variable o query param
                    .build(transactionId)
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(
                            new ProviderTransactionException("Status query failed: " + body)
                        ))
                )
                // 1. Parsear al DTO RAW del proveedor (crear ProviderRawTransactionStatusResponse)
                .bodyToMono(ProviderRawTransactionStatusResponse.class) // ← CREAR esta clase
                // 2. Mapear al modelo normalizado TumiPay
                .map(raw -> ProviderTransactionStatusResponse.builder()
                    .transactionId(transactionId)
                    .providerTransactionId(raw./* campo del proveedor */())
                    .operation(/* mapear desde raw a TransactionTypeEnum */)
                    .status(/* mapear desde raw a TransactionStatusEnum */)
                    .paymentMethod(/* mapear desde raw a PaymentMethodEnum */)
                    .metadata(/* mapear metadata adicional del proveedor */)
                    .build()
                )
                .timeout(Duration.ofMillis(paymentProvidersProperties.getTimeout()));
        });
}
```

> ⚠️ **Nota sobre `ProviderRawTransactionStatusResponse`:**
> Crear esta clase en `response/` para parsear la respuesta cruda del proveedor.
> El mapeo al modelo normalizado `ProviderTransactionStatusResponse` debe respetar los enums:
> - `TransactionStatusEnum`: `PENDING | APPROVED | REJECTED | EXPIRED | ERROR | CANCELLED`
> - `TransactionTypeEnum`: `PAYIN_TRANSACTION | PAYOUT_TRANSACTION`
> - `PaymentMethodEnum`: `PSE | CARD | CASH | QR | TRANSFIYA | BREB | BANK_TRANSFER | WALLET`

### 5.6 Operación `getTransactionDetail`

**Idéntica a `getTransactionStatus`** pero retorna `ProviderTransactionDetailResponse` que extiende `ProviderTransactionStatusResponse` con campos adicionales:

| Campo adicional | Descripción |
|---|---|
| `providerReferenceId` | ID de referencia del proveedor |
| `message` | Mensaje descriptivo del proveedor |
| `errorCode` | Código de error si aplica |
| `errorMessage` | Mensaje de error detallado |
| `providerRequest` | Payload enviado al proveedor (para auditoría) |
| `providerResponse` | Payload recibido del proveedor (para auditoría) |
| `providerRaw` | `Map<String,Object>` con la respuesta raw completa |

Similarmente, crear `ProviderRawTransactionDetailResponse` en `response/` para parsear el raw del proveedor.

---

## 6. Clases de Respuesta RAW a Crear

El agente debe crear las siguientes clases para separar el parseo del proveedor del modelo normalizado TumiPay:

| Clase a crear | Paquete | Propósito |
|---|---|---|
| `ProviderRawTransactionStatusResponse` | `response/` | Parsear respuesta cruda de `getTransactionStatus` |
| `ProviderRawTransactionDetailResponse` | `response/` | Parsear respuesta cruda de `getTransactionDetail` |
| `OAuthTokenRequest` | `request/security/` | Request para endpoint OAuth2 |
| `OAuthTokenRefreshRequest` | `request/security/` | Request para refresh de token OAuth2 |
| `OAuthTokenResponse` | `response/security/` | Response del endpoint OAuth2 |

Plantilla para las clases RAW:

```java
package com.tumipay.microservice.infrastructure.adapter.output.http.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumipay.microservice.infrastructure.adapter.output.http.provider.response.ProviderTransactionStatusResponse;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * ProviderRawTransactionStatusResponse
 *
 * RAW response DTO parsed directly from the payment provider's API response.
 * This class represents the provider-specific contract and must be mapped
 * to TumiPay's normalized model {@link ProviderTransactionStatusResponse}.
 *
 * ↓ REPLACE all fields with the actual provider's API response fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class ProviderRawTransactionStatusResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -8555744880770987231L;

    // ↓ TODO: reemplazar con los campos reales de la respuesta del proveedor
    @JsonProperty("provider_transaction_id")
    private String providerTransactionId;

    @JsonProperty("provider_status")          // nombre del campo en el JSON del proveedor
    private String providerStatus;            // valor crudo — mapear a TransactionStatusEnum

    @JsonProperty("provider_type")            // nombre del campo en el JSON del proveedor
    private String providerType;              // valor crudo — mapear a TransactionTypeEnum

    @JsonProperty("provider_payment_method")  // nombre del campo en el JSON del proveedor
    private String providerPaymentMethod;     // valor crudo — mapear a PaymentMethodEnum
}
```

---

## 7. Tabla de Mapeo de Enums

El agente debe crear métodos privados de mapeo para convertir los valores crudos del proveedor a los enums normalizados de TumiPay:

### 7.1 Mapeo de estado de transacción

```java
// En ProviderTransactionIntegrationAdapter o en una clase util separada
private TransactionStatusEnum mapProviderStatus(String rawStatus) {
    // ↓ TODO: reemplazar los strings con los valores reales del proveedor
    return switch (rawStatus.toUpperCase()) {
        case "PROVEEDOR_APROBADO", "APPROVED", "SUCCESS" -> TransactionStatusEnum.APPROVED;
        case "PROVEEDOR_RECHAZADO", "REJECTED", "FAILED"  -> TransactionStatusEnum.REJECTED;
        case "PROVEEDOR_PENDIENTE", "PENDING", "PROCESSING" -> TransactionStatusEnum.PENDING;
        case "PROVEEDOR_EXPIRADO", "EXPIRED", "TIMEOUT"  -> TransactionStatusEnum.EXPIRED;
        case "PROVEEDOR_CANCELADO", "CANCELLED"          -> TransactionStatusEnum.CANCELLED;
        default -> TransactionStatusEnum.ERROR;
    };
}
```

### 7.2 Mapeo de tipo de operación

```java
private TransactionTypeEnum mapProviderOperationType(String rawType) {
    // ↓ TODO: reemplazar con los valores reales del proveedor
    return switch (rawType.toUpperCase()) {
        case "PAYIN", "PAY_IN", "COBRO"  -> TransactionTypeEnum.PAYIN_TRANSACTION;
        case "PAYOUT", "PAY_OUT", "PAGO" -> TransactionTypeEnum.PAYOUT_TRANSACTION;
        default -> TransactionTypeEnum.PAYIN_TRANSACTION;
    };
}
```

### 7.3 Mapeo de método de pago

```java
private PaymentMethodEnum mapProviderPaymentMethod(String rawMethod) {
    // ↓ TODO: reemplazar con los valores reales del proveedor
    return switch (rawMethod.toUpperCase()) {
        case "CARD", "TARJETA"       -> PaymentMethodEnum.CARD;
        case "PSE"                   -> PaymentMethodEnum.PSE;
        case "CASH", "EFECTIVO"      -> PaymentMethodEnum.CASH;
        case "BANK_TRANSFER"         -> PaymentMethodEnum.BANK_TRANSFER;
        case "WALLET", "BILLETERA"   -> PaymentMethodEnum.WALLET;
        case "QR"                    -> PaymentMethodEnum.QR;
        case "TRANSFIYA"             -> PaymentMethodEnum.TRANSFIYA;
        case "BREB"                  -> PaymentMethodEnum.BREB;
        default -> PaymentMethodEnum.CARD;
    };
}
```

---

## 8. Manejo de Errores

### 8.1 Tipos de error a manejar

| Tipo | Origen | Acción |
|---|---|---|
| `WebClientResponseException` | HTTP 4xx/5xx del proveedor | Construir resultado con `status=FAILED`, `errorCode`, `errorMessage` |
| `TimeoutException` | Timeout en llamada HTTP | Lanzar `ProviderTransactionException` con código `PROVIDER_TIMEOUT` |
| `ProviderSecurityException` | Error al generar token | Propagar como error técnico |
| `WebClientRequestException` | Error de conexión | Lanzar con código `PROVIDER_CONNECTION_ERROR` |

### 8.2 Campos obligatorios en resultado de error

Cuando una operación falla, el `PayInTransactionResult` o `PayOutTransactionResult` **debe siempre** incluir:

```
status         = OperationStatusEnum.FAILED.toString()
httpStatusCode = código HTTP recibido (o 0 si no hay respuesta)
httpMethod     = método HTTP usado
providerEndpoint = URL intentada
providerRequest  = el Request que se envió (para trazabilidad)
providerLatencyMs = tiempo transcurrido hasta el error
processedAt    = Instant.now()
providerProcessedAt = Instant.now()
errorCode      = código de error descriptivo (ej. "PROVIDER_HTTP_ERROR_400")
errorMessage   = mensaje de error del proveedor
paymentAdapterCode = paymentProvidersProperties.getCode()
```

### 8.3 Timeout obligatorio

**Todos** los métodos que realizan llamadas HTTP deben terminar con:

```java
.timeout(Duration.ofMillis(paymentProvidersProperties.getTimeout()))
.onErrorMap(TimeoutException.class, ex ->
    new ProviderTransactionException(
        "PROVIDER_TIMEOUT",
        "Provider call timed out after " + paymentProvidersProperties.getTimeout() + "ms"
    )
)
```

---

## 9. Medición de Latencia

La latencia debe medirse en **todos** los métodos que realizan llamadas HTTP, incluyendo el camino de error:

```java
// CORRECTO — la latencia se calcula incluso en el onErrorResume
final long startTime = System.currentTimeMillis();

return webClient.post()...
    .map(response -> {
        long latency = System.currentTimeMillis() - startTime;
        return buildResult(..., (int) latency, ...);
    })
    .onErrorResume(WebClientResponseException.class, ex -> {
        long latency = System.currentTimeMillis() - startTime; // ← también aquí
        return Mono.just(buildErrorResult(..., (int) latency, ...));
    });
```

---

## 10. Logging Obligatorio

Usar `@Log4j2` (ya presente en ambas clases). Patrones de log requeridos:

```java
// Antes de llamada HTTP
log.info("Calling provider [{}] endpoint [{}] method [{}]",
    paymentProvidersProperties.getCode(), endpoint, httpMethod);

// En respuesta exitosa
log.info("Provider [{}] responded successfully in [{}ms] with status [{}]",
    paymentProvidersProperties.getCode(), latencyMs, httpStatusCode);

// En error
log.error("Provider [{}] call failed. Endpoint: [{}], Status: [{}], Error: [{}]",
    paymentProvidersProperties.getCode(), endpoint, httpStatusCode, errorMessage);

// En seguridad
log.info("Generating authorization for provider [{}] with scheme [{}]",
    paymentProvidersProperties.getCode(), authScheme);
```

> ⚠️ **Nunca** loguear el `value` de un `AuthCredential` (contiene tokens/claves secretas).

---

## 11. Configuración YAML — Variables de Entorno

El agente debe verificar que `application.yml` (y los perfiles `dev`, `staging`, `prod`) contenga estas variables de entorno para la integración:

```yaml
tumipay:
  payment-provider:
    code: ${ENV_PAYMENT_PROVIDER_CODE}
    name: ${ENV_PAYMENT_PROVIDER_NAME}
    timeout: ${ENV_PAYMENT_PROVIDER_TIMEOUT:30000}
    security:
      base-url: ${ENV_PAYMENT_PROVIDER_SECURITY_BASE_URL}
      endpoints:
        generate-token-path: ${ENV_PAYMENT_PROVIDER_TOKEN_PATH:/oauth/token}
        refresh-token-path: ${ENV_PAYMENT_PROVIDER_REFRESH_TOKEN_PATH:/oauth/refresh}
    integration:
      base-url: ${ENV_PAYMENT_PROVIDER_BASE_URL}
      endpoints:
        payin-transaction-path: ${ENV_PAYMENT_PROVIDER_PAYIN_PATH:/v1/payins}
        payout-transaction-path: ${ENV_PAYMENT_PROVIDER_PAYOUT_PATH:/v1/payouts}
        get-transaction-path: ${ENV_PAYMENT_PROVIDER_TRANSACTION_PATH:/v1/transactions/{id}}
    authorization:
      expiration-time-ms: ${ENV_PAYMENT_PROVIDER_TOKEN_EXPIRATION_MS:3600000}
      refresh-threshold-ms: ${ENV_PAYMENT_PROVIDER_TOKEN_REFRESH_THRESHOLD_MS:300000}
```

Si el esquema es `API_KEY`, el agente debe agregar una nueva propiedad:
```yaml
    security:
      api-key: ${ENV_PAYMENT_PROVIDER_API_KEY}  # ← NUEVA — valor secreto nunca en texto plano
```

---

## 12. Criterios de Aceptación del Agente

El agente habrá completado su tarea satisfactoriamente cuando:

### `ProviderSecurityIntegrationAdapter`
- [ ] **S-01** El método `generateAuthorization` realiza una llamada HTTP real al endpoint del proveedor cuando `credentialType = TOKEN`.
- [ ] **S-02** Para `credentialType = API_KEY` no realiza llamada HTTP; retorna credencial desde configuración.
- [ ] **S-03** Toda credencial generada pasa por `AuthCredentialValidation.validateAuthCredential()` y `validateCredentialMetadata()`.
- [ ] **S-04** El campo `value` del `AuthCredential` **nunca** aparece en logs.
- [ ] **S-05** Los DTOs `OAuthTokenRequest` y `OAuthTokenResponse` están creados en sus sub-paquetes correctos.
- [ ] **S-06** El timeout se aplica sobre la llamada HTTP de generación de token.

### `ProviderTransactionIntegrationAdapter`
- [ ] **T-01** Los 4 métodos (`payInTransaction`, `payOutTransaction`, `getTransactionStatus`, `getTransactionDetail`) reemplazaron los `Mono.just(...)` mock por cadenas reactivas con `WebClient`.
- [ ] **T-02** Cada operación obtiene/refresca credenciales antes de ejecutar la llamada HTTP.
- [ ] **T-03** `ProviderPayInRequest` y `ProviderPayOutRequest` tienen los campos reales del proveedor.
- [ ] **T-04** `ProviderPayInResponse` y `ProviderPayOutResponse` tienen los campos reales del proveedor.
- [ ] **T-05** `ProviderRawTransactionStatusResponse` y `ProviderRawTransactionDetailResponse` existen y son mapeadas al modelo normalizado TumiPay.
- [ ] **T-06** `providerLatencyMs` se calcula en el camino exitoso Y en el de error.
- [ ] **T-07** `PayOutTransactionResult.synchronous` se puebla correctamente.
- [ ] **T-08** Todos los campos obligatorios del resultado (sección 8.2) se populan tanto en éxito como en error.
- [ ] **T-09** `.timeout(Duration.ofMillis(paymentProvidersProperties.getTimeout()))` está presente en cada llamada HTTP.
- [ ] **T-10** Los métodos de mapeo de enums (`mapProviderStatus`, `mapProviderOperationType`, `mapProviderPaymentMethod`) están implementados.
- [ ] **T-11** El `httpMethod` siempre se establece con `HttpMethod.POST.name()` para PayIn/PayOut y `HttpMethod.GET.name()` para consultas.

### General
- [ ] **G-01** No hay URLs hardcodeadas; todas provienen de `PaymentProvidersProperties`.
- [ ] **G-02** No hay credenciales hardcodeadas; todas provienen de variables de entorno o secretos.
- [ ] **G-03** El código compila sin errores con `mvn compile`.
- [ ] **G-04** No se modificaron las interfaces `IProviderTransactionIntegrationAdapterPort` ni `IProviderSecurityIntegrationAdapterPort`.

---

## 13. Archivos de Referencia

| Archivo | Propósito |
|---|---|
| `.../output/http/ProviderTransactionIntegrationAdapter.java` | **Modificar** — reemplazar mocks |
| `.../output/http/ProviderSecurityIntegrationAdapter.java` | **Modificar** — reemplazar mocks |
| `.../output/http/request/ProviderPayInRequest.java` | **Expandir** con campos del proveedor |
| `.../output/http/request/ProviderPayOutRequest.java` | **Expandir** con campos del proveedor |
| `.../output/http/response/ProviderPayInResponse.java` | **Expandir** con campos del proveedor |
| `.../output/http/response/ProviderPayOutResponse.java` | **Expandir** con campos del proveedor |
| `.../output/http/response/ProviderTransactionStatusResponse.java` | **NO modificar** — modelo normalizado |
| `.../output/http/response/ProviderTransactionDetailResponse.java` | **NO modificar** — modelo normalizado |
| `.../output/http/validation/AuthCredentialValidation.java` | **NO modificar** — usar como está |
| `.../domain/port/output/IProviderTransactionIntegrationAdapterPort.java` | **NO modificar** — contrato de puerto |
| `.../domain/port/output/IProviderSecurityIntegrationAdapterPort.java` | **NO modificar** — contrato de puerto |
| `.../component/properties/PaymentProvidersProperties.java` | Referencia de propiedades disponibles |
| `.../component/config/WebClientConfig.java` | Referencia de configuración del cliente HTTP |
| `src/main/resources/application.yml` | **Actualizar** variables de entorno si aplica |

---

*Este documento es la fuente de verdad para el agente. Ante la ausencia de documentación del proveedor externo, el agente debe dejar los campos `// TODO` debidamente marcados con comentarios que especifiquen exactamente qué información del proveedor se requiere para completarlos.*

