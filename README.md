# TumiPay Microservice Template

Plantilla de Microservicio con arquitectura hexagonal.

---

## Resumen

Este repositorio contiene una plantilla de microservicio (TumiPay) con arquitectura hexagonal, preparada para ejecutar con Spring Boot WebFlux, R2DBC (PostgreSQL), Redis y observabilidad básica (Actuator / OpenAPI).

---
## 📦 Requisitos Previos

- Java 21+
- Maven 3.9+
- PostgreSQL 16+ (o usar Docker)
- Redis 7+ (o usar Docker)

---

## 🚀 Ejecución

### 1. Levantar dependencias con Docker

```bash
# Desde el directorio raíz del proyecto
cd ..
docker compose up -d postgres redis zipkin
```

> Nota: los servicios `postgres`, `redis` y `zipkin` están definidos en el docker compose utilizado por el equipo; ajusta los nombres si tu `docker-compose.yml` local difiere.

### 2. Compilar el proyecto

```bash
mvn clean install -DskipTests
```

### 3. Ejecutar la aplicación (perfil dev)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación estará disponible en: http://localhost:8000

---

## ✅ Verificar

```bash
# Health check

curl http://localhost:8000/tp/payment/adapter/v1/microservice/info

---

## 🐳 Ejecución con Docker

```bash
# Desde el directorio raíz del proyecto
cd ..
docker compose up -d --build app
```

---

## 🔌 Puerto

- Puerto por defecto: 8000

---

## 📁 Estructura del proyecto (resumen)

- `src/main/java` - Código fuente
  - `com.tumipay.microservice` - paquete raíz
    - `application` - casos de uso / servicios de aplicación
    - `domain` - modelos, servicios de dominio y puertos
    - `infrastructure` - adaptadores (input/output), componentes, propiedades
      - `adapter.input.http.controller` - controladores HTTP
      - `component` - utilidades compartidas, handlers, filtros
- `src/main/resources` - configuraciones por perfil y recursos (YAML, logging, DB migrations)
- `src/test` - pruebas unitarias e integración
- `db/migration` - migraciones Flyway (V1__create_transactions_table.sql)

---

## 🏛 Arquitectura

Este proyecto sigue una aproximación hexagonal (Ports & Adapters):

- Core/Domain: contiene la lógica de negocio y modelos inmutables.
- Application: orquesta casos de uso y aplica reglas de negocio.
- Infrastructure (Adapters): implementa persistencia (R2DBC/Postgres), cache (Redis), y adaptadores de entrada (HTTP controllers) y salida.

Beneficios:
- Separación clara entre lógica de negocio y detalles técnicos.
- Testabilidad y facilidad para cambiar adaptadores (por ejemplo, reemplazar Postgres por otro DB).

---

## 🛣 Endpoints detectados

El proyecto contiene controladores REST documentados con OpenAPI. Endpoints descubiertos automáticamente en el código:

- GET /api/v1/service/info
  - Descripción: Devuelve metadatos del microservicio (nombre, versión, entorno, versiones de Java y Spring Boot, timestamp).
  - Ejemplo:

```bash
curl -s http://localhost:8080/api/v1/service/info | jq
```

- Actuator endpoints disponibles bajo `/actuator` (por ejemplo `/actuator/health`, `/actuator/info`) si están habilitados en el perfil.
- Swagger UI: `/swagger-ui.html` (documentación OpenAPI generada por springdoc)

Si agregas más controladores, actualiza esta sección; el proyecto ya incluye anotaciones OpenAPI en las interfaces de los controladores.

---

## ⚙️ Configuración y perfiles

Archivos de configuración principales en `src/main/resources`:

- `application.yml` - configuración base
- `application-dev.yml` - perfil de desarrollo
- `application-staging.yml` - perfil de staging
- `application-prod.yml` - perfil de producción
- `application-test.yml` (en `src/test/resources`) - pruebas

Variables importantes a revisar:
- `spring.r2dbc.url`, `spring.r2dbc.username`, `spring.r2dbc.password` (conexión a Postgres)
- `spring.redis.*` (conexión a Redis)
- `spring.profiles.active` (perfil activo durante ejecución)

---

## 🧪 Testing

Para ejecutar las pruebas unitarias y de integración:

```bash
mvn test
```

El proyecto incluye configuración de Jacoco y reglas de cobertura (80% por paquete). Ajusta o deshabilita temporalmente la verificación si necesitas compilar localmente sin cumplir cobertura.

---

## 📦 Packaging / CI

El empaquetado por defecto es `jar`. El plugin de `spring-boot-maven-plugin` está configurado en `pom.xml`.

---

## ✅ Buenas prácticas y notas

- Usa los perfiles (`dev`, `staging`, `prod`) para diferenciar configuraciones.
- Las excepciones se manejan centralizadamente en `infrastructure/component/handler/BaseGlobalExceptionHandler` que devuelve respuestas estándar (`BaseApiResponse`).
- Documenta nuevos endpoints con OpenAPI (`@Operation`, `@ApiResponses`) siguiendo el estilo usado en `IMicroServiceController`.

---

## ✉️ Contacto y licencia

- Equipo responsable: TumiPay Architecture and Engineering Team (admin.it@tumipay.co)
- Licencia: Propietary - TumiPay (ver `pom.xml` para detalles)

---

Si quieres, puedo:
- Extraer automáticamente más endpoints y generar una sección de ejemplos de petición/respuesta más detallada.
- Añadir una guía de despliegue en Kubernetes o un `docker-compose.yml` de ejemplo.
