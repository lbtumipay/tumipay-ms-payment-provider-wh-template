# 📄 Especificación Técnica V2 — API Key Interna (Arquitectura Hexagonal TumiPay)

> **Versión:** 2.0
> **Fecha:** 2026-04-19
> **Sustituye a:** `API_KEY_VALIDATOR_SPEC.md` (V1)

---

## 1. Objetivo

Implementar un mecanismo de seguridad basado en **API Key interna** para proteger endpoints específicos del microservicio, evitando su consumo desde fuera del entorno controlado.

La API Key será gestionada mediante **Secret Manager** y utilizada exclusivamente para **comunicación service-to-service (interna)**.

---

## 2. Alcance

- ✔ Protección selectiva de endpoints internos mediante anotación
- ✔ Validación de API Key vía header HTTP
- ✔ Alineación con arquitectura hexagonal TumiPay
- ✔ Uso de WebFlux (reactivo, no bloqueante)
- ✔ Patrón de propiedades `tumipay.*` (consistente con el proyecto)
- ✔ Uso de `BaseApiResponse` para respuestas de error estandarizadas
- ✔ Alineación con convención de variables de entorno `ENV_*`

---

## 3. No Alcance

- ❌ Autenticación de usuarios finales
- ❌ OAuth2 / JWT
- ❌ Gestión de múltiples clientes externos
- ❌ Persistencia de API Keys en base de datos

---

## 4. Header de Seguridad

```http
X-Api-Key: <internal-api-key>
```

---

## 5. Configuración

### 5.1 Variable de entorno

```bash
ENV_MS_INTERNAL_API_KEY=<secret-value>
```

### 5.2 YAML (`application.yml`)

La clave se configura bajo el prefijo `tumipay.*`, alineado con la convención del proyecto:

```yaml
tumipay:
  security:
    internal:
      api-key: ${ENV_MS_INTERNAL_API_KEY:}
```

> ⚠️ **V1 usaba** `security.internal.api-key`. En V2 se adopta `tumipay.security.internal.api-key`
> para ser consistente con el prefijo estándar del proyecto (`tumipay.microservice`, `tumipay.payment-gateway`, etc.).

---

## 6. Diseño Arquitectónico

### 6.1 Enfoque

Se implementa mediante:

| Componente | Rol |
|---|---|
| `@InternalApiSecured` | Anotación para marcar endpoints internos |
| `InternalApiSecurityFilter` | `WebFilter` reactivo para interceptar y validar |
| `IApiKeyValidatorPort` | Port en capa de dominio para desacoplar la validación |
| `InternalApiKeyValidatorAdapter` | Implementación de la validación con comparación segura |
| `InternalSecurityProperties` | Properties class con prefijo `tumipay.security.internal` |

### 6.2 Paquetes (alineados con estructura real del proyecto)

```
com.tumipay.microservice
├── domain
│   └── port
│       └── input
│           └── IApiKeyValidatorPort.java          ← Port de dominio
├── infrastructure
│   └── component
│       ├── annotation
│       │   └── InternalApiSecured.java            ← Anotación de marcado
│       ├── filter
│       │   └── InternalApiSecurityFilter.java     ← WebFilter reactivo
│       ├── properties
│       │   └── InternalSecurityProperties.java    ← ConfigurationProperties
│       └── (raíz component)
│           └── InternalApiKeyValidatorAdapter.java ← Implementación del port
```

> **Nota:** `IApiKeyValidatorPort` se ubica en `domain.port.input` ya que representa
> una capacidad de validación de seguridad consultada desde la capa de infraestructura.
> La implementación va en `infrastructure.component` (no en `infrastructure.adapter.input.http`)
> porque no es un adaptador HTTP externo, sino un componente interno de seguridad.

### 6.3 Flujo de ejecución

```
HTTP Request
    ↓
HttpLoggerFilter          (MDC / correlación — ya existente)
    ↓
InternalApiSecurityFilter (¿endpoint anotado con @InternalApiSecured?)
    ├── NO → chain.filter(exchange)
    └── SÍ → IApiKeyValidatorPort.isValid(apiKey)
                ├── válido   → chain.filter(exchange)
                └── inválido → 401 Unauthorized (BaseApiResponse)
    ↓
Controller (@InternalApiSecured)
```

---

## 7. Anotación

**Paquete:** `com.tumipay.microservice.infrastructure.component.annotation`

```java
package com.tumipay.microservice.infrastructure.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * InternalApiSecured
 * <p>
 * Marks a controller class or method as requiring internal API Key authentication.
 * <p>
 * When present, {@link InternalApiSecurityFilter} will enforce validation of the
 * {@code X-Api-Key} header before allowing the request to proceed.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalApiSecured {
}
```

---

## 8. Properties

**Paquete:** `com.tumipay.microservice.infrastructure.component.properties`

```java
package com.tumipay.microservice.infrastructure.component.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * InternalSecurityProperties
 * <p>
 * Configuration properties for internal API Key security.
 * <p>
 * Bound to {@code tumipay.security.internal} prefix, alineado con la convención
 * estándar del proyecto TumiPay.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@Data
@Validated
@ConfigurationProperties(prefix = "tumipay.security.internal")
public class InternalSecurityProperties {

    /**
     * Internal API Key used for service-to-service authentication.
     * <p>
     * MUST be provided via {@code ENV_MS_INTERNAL_API_KEY} environment variable.
     * Never hardcode this value.
     */
    private String apiKey;
}
```

> ⚠️ **V1 usaba** `InternalSecurityProperties` con prefijo `security.internal`.
> En V2 el prefijo es `tumipay.security.internal`.

---

## 9. Port

**Paquete:** `com.tumipay.microservice.domain.port.input`

```java
package com.tumipay.microservice.domain.port.input;

/**
 * IApiKeyValidatorPort
 * <p>
 * Domain port for internal API Key validation.
 * <p>
 * Decouples the validation logic from the infrastructure layer,
 * following hexagonal architecture principles.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
public interface IApiKeyValidatorPort {

    /**
     * Validates whether the provided API Key is legitimate.
     *
     * @param apiKey the value extracted from the {@code X-Api-Key} HTTP header.
     * @return {@code true} if the key matches the configured secret; {@code false} otherwise.
     */
    boolean isValid(String apiKey);
}
```

---

## 10. Adapter (Implementación del Port)

**Paquete:** `com.tumipay.microservice.infrastructure.component`

```java
package com.tumipay.microservice.infrastructure.component;

import com.tumipay.microservice.domain.port.input.IApiKeyValidatorPort;
import com.tumipay.microservice.infrastructure.component.properties.InternalSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * InternalApiKeyValidatorAdapter
 * <p>
 * Implementation of {@link IApiKeyValidatorPort} that validates the API Key
 * using a constant-time comparison to prevent timing attacks.
 * <p>
 * The expected key is sourced from {@link InternalSecurityProperties},
 * which binds to {@code tumipay.security.internal.api-key}.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@Component
@RequiredArgsConstructor
public class InternalApiKeyValidatorAdapter implements IApiKeyValidatorPort {

    private final InternalSecurityProperties properties;

    /**
     * {@inheritDoc}
     * <p>
     * Uses {@link MessageDigest#isEqual} for constant-time byte comparison,
     * mitigating timing-based side-channel attacks.
     */
    @Override
    public boolean isValid(String apiKey) {

        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
            apiKey.getBytes(StandardCharsets.UTF_8),
            properties.getApiKey().getBytes(StandardCharsets.UTF_8)
        );
    }
}
```

> **Diferencia con V1:** Se agrega validación explícita de que `properties.getApiKey()` no sea
> `null` ni vacío, evitando que un secret no configurado permita acceso con cualquier valor vacío.

---

## 11. WebFilter

**Paquete:** `com.tumipay.microservice.infrastructure.component.filter`

```java
package com.tumipay.microservice.infrastructure.component.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumipay.microservice.domain.port.input.IApiKeyValidatorPort;
import com.tumipay.microservice.infrastructure.component.annotation.InternalApiSecured;
import com.tumipay.microservice.infrastructure.component.dto.BaseApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * InternalApiSecurityFilter
 * <p>
 * Reactive WebFilter that enforces internal API Key authentication for endpoints
 * annotated with {@link InternalApiSecured}.
 * <p>
 * Integrates with the existing reactive filter chain alongside {@link HttpLoggerFilter}.
 * Returns a standardized {@link BaseApiResponse} error body on unauthorized access,
 * consistent with the TumiPay API response conventions.
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 19/04/2026
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class InternalApiSecurityFilter implements WebFilter {

    private static final String HEADER_API_KEY = "X-Api-Key";

    private final IApiKeyValidatorPort apiKeyValidator;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ObjectMapper objectMapper;

    /**
     * Intercepts incoming requests and validates the API Key for secured endpoints.
     *
     * @param exchange the current server exchange.
     * @param chain    the filter chain.
     * @return {@link Mono} that completes when processing finishes.
     */
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        return requestMappingHandlerMapping.getHandler(exchange)
            .flatMap(handler -> {

                if (!(handler instanceof HandlerMethod handlerMethod)) {
                    return chain.filter(exchange);
                }

                boolean secured =
                    handlerMethod.hasMethodAnnotation(InternalApiSecured.class) ||
                    handlerMethod.getBeanType().isAnnotationPresent(InternalApiSecured.class);

                if (!secured) {
                    return chain.filter(exchange);
                }

                String apiKey = exchange.getRequest().getHeaders().getFirst(HEADER_API_KEY);

                if (!apiKeyValidator.isValid(apiKey)) {
                    log.warn("Unauthorized internal API Key access attempt on path: {}",
                        exchange.getRequest().getPath().value());
                    return writeUnauthorizedResponse(exchange);
                }

                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange));
    }

    /**
     * Writes a standardized 401 Unauthorized response using {@link BaseApiResponse}.
     */
    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange) {

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return Mono.fromCallable(() -> {
                BaseApiResponse<Void> body = BaseApiResponse.<Void>builder()
                    .code("UNAUTHORIZED")
                    .message("Invalid or missing internal API Key.")
                    .data(null)
                    .build();
                return objectMapper.writeValueAsBytes(body);
            })
            .flatMap(bytes -> {
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                return exchange.getResponse().writeWith(Mono.just(buffer));
            });
    }
}
```

> **Diferencias con V1:**
> - Se usa `RequestMappingHandlerMapping.getHandler()` en vez de `exchange.getAttribute()`,
>   ya que en WebFlux el atributo `BEST_MATCHING_HANDLER_ATTRIBUTE` puede no estar disponible
>   en el momento de ejecución del filtro.
> - La respuesta de error usa `BaseApiResponse` (estándar TumiPay) en lugar de `setComplete()` vacío.
> - Se agrega logging del intento de acceso no autorizado (sin loggear el API Key).
> - Se anota con `@NonNull` de `org.jspecify` (patrón del proyecto).

---

## 12. Registro de la Properties Class

La clase `InternalSecurityProperties` debe registrarse en la clase principal o en una `@Configuration`:

```java
@SpringBootApplication
@EnableConfigurationProperties({
    // ... otras properties existentes ...
    InternalSecurityProperties.class
})
public class MicroserviceApplication { ... }
```

---

## 13. Uso en Controllers

```java
// Proteger todo el controlador
@InternalApiSecured
@RestController
@RequestMapping("/v1/internal/credentials")
public class CredentialRefreshController extends BaseController { ... }

// O proteger un método específico
@RestController
@RequestMapping("/v1/transactions")
public class TransactionController extends BaseController {

    @InternalApiSecured
    @GetMapping("/internal/report")
    public Mono<ResponseEntity<BaseApiResponse<ReportResponse>>> getInternalReport() { ... }
}
```

---

## 14. Buenas Prácticas

- ✔ No hardcodear API Keys — usar `ENV_MS_INTERNAL_API_KEY`
- ✔ Usar Secret Manager (AWS Secrets Manager, GCP Secret Manager, etc.)
- ✔ Comparación segura con `MessageDigest.isEqual` (tiempo constante)
- ✔ Nunca loggear el valor del API Key
- ✔ Validar que el secret esté configurado antes de comparar
- ✔ Permitir rotación de secrets sin redeploy (via Secret Manager rotation)
- ✔ Respuesta de error estandarizada con `BaseApiResponse`

---

## 15. Consideraciones de Seguridad

- Combinar con IP Whitelisting en entornos productivos (recomendado)
- Proteger únicamente endpoints de uso interno (scheduler callbacks, refresh de credenciales, etc.)
- El filtro no reemplaza HTTPS — debe desplegarse siempre sobre TLS
- Evitar exponer endpoints `@InternalApiSecured` en el gateway público

---

## 16. Auditoría

Registrar en log (sin almacenar payloads ni API Keys):

| Campo | Fuente |
|---|---|
| `requestId` | Header `X-Request-Id` (MDC via `HttpLoggerFilter`) |
| `merchantId` | Header `X-Merchant-Id` (MDC via `HttpLoggerFilter`) |
| `path` | `exchange.getRequest().getPath()` |
| `status` | `HttpStatus.UNAUTHORIZED` (401) |
| `timestamp` | `LocalDateTime.now()` |

---

## 17. Evolución

- Soporte para múltiples API Keys (multi-tenant interno)
- Cache en Redis para validación de alta frecuencia
- Migración a mTLS para comunicación inter-servicio en producción
- Integración con rotación automática de secrets

---

## 18. Cambios respecto a V1

| Aspecto | V1 | V2 |
|---|---|---|
| Prefijo YAML | `security.internal.api-key` | `tumipay.security.internal.api-key` |
| Variable de entorno | `ENV_MS_INTERNAL_API_KEY` | `ENV_MS_INTERNAL_API_KEY` (sin cambio) |
| Obtención del handler | `exchange.getAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE)` | `RequestMappingHandlerMapping.getHandler()` |
| Respuesta 401 | `setComplete()` (sin body) | `BaseApiResponse` con `code` y `message` |
| Validación de secret vacío | No considerada | Validación explícita en el adapter |
| Logging de acceso denegado | No contemplado | `log.warn(...)` sin exponer el key |
| Anotación `@NonNull` | No usada | `org.jspecify.annotations.NonNull` (patrón del proyecto) |
| Paquete de la anotación | No especificado | `infrastructure.component.annotation` |
| Paquete de properties | No especificado | `infrastructure.component.properties` |
| Paquete del adapter | No especificado | `infrastructure.component` |
| Paquete del filter | No especificado | `infrastructure.component.filter` |

---

## 19. Conclusión

La V2 mantiene el diseño ligero y desacoplado de V1, adaptándolo completamente a los
**patrones reales del proyecto `tumipay-ms-payment-provider-template`**:
convención de prefijos `tumipay.*`, uso de `BaseApiResponse`, patrón de variables de entorno `ENV_*`,
estructura de paquetes hexagonal y estilo de código (Lombok, Log4j2, jspecify `@NonNull`).

