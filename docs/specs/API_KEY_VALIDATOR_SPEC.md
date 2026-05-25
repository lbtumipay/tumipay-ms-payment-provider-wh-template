# 📄 Especificación Técnica — API Key Interna (Hexagonal Architecture)

## 1. Objetivo

Implementar un mecanismo de seguridad basado en **API Key interna** para proteger endpoints específicos del componente, evitando su consumo desde fuera del entorno controlado.

La API Key será gestionada mediante un **Secret Manager** y utilizada exclusivamente para **comunicación service-to-service (interna)**.

---

## 2. Alcance

- ✔ Protección selectiva de endpoints internos
- ✔ Validación de API Key vía header HTTP
- ✔ Integración con arquitectura hexagonal
- ✔ Uso de WebFlux (reactivo)
- ✔ Seguridad ligera sin frameworks pesados

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

La API Key se define como variable de entorno proveniente de Secret Manager:

```yaml
security:
  internal:
    api-key: ${ENV_MS_INTERNAL_API_KEY}
```

---

## 6. Diseño Arquitectónico

### 6.1 Enfoque

Se implementa mediante:

- Anotación para marcar endpoints internos
- WebFilter para validación
- Port para desacoplar lógica

---

### 6.2 Flujo

```
Request
  ↓
InternalApiSecurityFilter
  ↓
IApiKeyValidatorPort
  ↓
Controller
```

---

## 7. Anotación

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalApiSecured {
}
```

---

## 8. WebFilter

```java
@Component
@RequiredArgsConstructor
public class InternalApiSecurityFilter implements WebFilter {

    private final IApiKeyValidatorPort apiKeyValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        Object handler = exchange.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return chain.filter(exchange);
        }

        boolean secured =
            handlerMethod.hasMethodAnnotation(InternalApiSecured.class) ||
            handlerMethod.getBeanType().isAnnotationPresent(InternalApiSecured.class);

        if (!secured) {
            return chain.filter(exchange);
        }

        String apiKey = exchange.getRequest()
            .getHeaders()
            .getFirst("X-Api-Key");

        if (!apiKeyValidator.isValid(apiKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
```

---

## 9. Port

```java
public interface IApiKeyValidatorPort {
    boolean isValid(String apiKey);
}
```

---

## 10. Adapter

```java
@Component
@RequiredArgsConstructor
public class InternalApiKeyValidatorAdapter implements IApiKeyValidatorPort {

    private final InternalSecurityProperties properties;

    @Override
    public boolean isValid(String apiKey) {

        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
            apiKey.getBytes(StandardCharsets.UTF_8),
            properties.getApiKey().getBytes(StandardCharsets.UTF_8)
        );
    }
}
```

---

## 11. Buenas Prácticas

- ✔ No hardcodear API Keys
- ✔ Usar Secret Manager
- ✔ Comparación segura (`MessageDigest.isEqual`)
- ✔ No loggear API Keys
- ✔ Permitir rotación de secrets

---

## 12. Consideraciones de Seguridad

- Combinar con IP Whitelisting (recomendado)
- Proteger endpoints sensibles únicamente
- Validar headers obligatorios adicionales si aplica

---

## 13. Auditoría (Opcional)

Registrar metadata:

- endpoint
- status
- request_id

Sin almacenar payloads ni API Keys.

---

## 14. Evolución

- Soporte para múltiples servicios internos
- Integración con Redis (cache)
- Migración a mTLS o JWT interno

---

## 15. Conclusión

La solución proporciona una capa de seguridad ligera, desacoplada y alineada con arquitectura hexagonal, ideal para comunicación interna entre servicios.
