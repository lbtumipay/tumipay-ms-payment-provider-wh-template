# Docker Setup Guide - TumiPay Microservice Template

Este documento describe cómo usar los Dockerfiles incluidos en el proyecto para construir y ejecutar la aplicación.

## Archivos Docker Disponibles

### 1. **Dockerfile** (Multi-stage para desarrollo)
Incluye dos etapas de runtime:
- **runtime-native**: Imagen nativa compilada con GraalVM
- **runtime-jvm**: Imagen tradicional con JVM

### 2. **Dockerfile.prod** (Optimizado para producción)
Incluye configuraciones de seguridad y optimización para ambientes productivos.

### 3. **docker-compose.yml**
Orquestación de servicios incluyendo:
- PostgreSQL (base de datos)
- Microservicio (versión nativa)
- Microservicio (versión JVM)

## Requisitos

- Docker Engine 20.10+
- Docker Compose 2.0+
- GraalVM 21 (para compilación nativa)
- Maven 3.8+

## Construcción Local

### Opción 1: Compilación Nativa (GraalVM)

```bash
# Construir imagen nativa
docker build \
  --target runtime-native \
  -t tumipay-microservice:latest-native \
  -f Dockerfile .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  tumipay-microservice:latest-native
```

**Ventajas:**
- Inicio ultra rápido (~100-500ms)
- Consumo mínimo de memoria
- Mejor rendimiento en contenedores

**Desventajas:**
- Tiempo de compilación más largo
- Requiere GraalVM en el builder

### Opción 2: Compilación JVM Tradicional

```bash
# Construir imagen JVM
docker build \
  --target runtime-jvm \
  -t tumipay-microservice:latest-jvm \
  -f Dockerfile .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  tumipay-microservice:latest-jvm
```

**Ventajas:**
- Compilación más rápida
- Mayor compatibilidad
- Mejor soporte de debugging

**Desventajas:**
- Inicio lento (~5-10 segundos)
- Mayor consumo de memoria

## Docker Compose

### Levantar todos los servicios

```bash
# Copiar archivo de configuración de ejemplo
cp .env.example .env

# Editar .env según sea necesario
nano .env

# Iniciar servicios
docker-compose up -d
```

### Verificar estado de servicios

```bash
# Ver logs
docker-compose logs -f

# Ver logs específicos
docker-compose logs -f microservice-native
docker-compose logs -f postgres
```

### Detener servicios

```bash
docker-compose down
```

### Eliminar volúmenes (borrar datos)

```bash
docker-compose down -v
```

## Acceso a la Aplicación

### Microservicio Nativo
- URL: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- OpenAPI: `http://localhost:8080/swagger-ui.html`

### Microservicio JVM
- URL: `http://localhost:8081`
- Health: `http://localhost:8081/actuator/health`
- OpenAPI: `http://localhost:8081/swagger-ui.html`

### PostgreSQL
- Host: `localhost:5432`
- Usuario: `tumipay` (por defecto)
- Contraseña: `tumipay123` (por defecto)
- Base de datos: `tumipay_db` (por defecto)

## Configuración de Variables de Entorno

Edita el archivo `.env` para personalizar:

```env
# Database
DB_USER=tumipay
DB_PASSWORD=tumipay123
DB_NAME=tumipay_db

# Spring Profile
SPRING_PROFILE=dev

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_TUMIPAY=DEBUG
```

## Producción

Para desplegar en producción, usa `Dockerfile.prod`:

```bash
# Compilar imagen de producción (nativa)
docker build \
  -t tumipay-microservice:latest \
  -f Dockerfile.prod .

# Ejecutar con restricciones de recursos
docker run \
  -d \
  --name tumipay-prod \
  --memory="512m" \
  --memory-swap="512m" \
  --cpus="1" \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  tumipay-microservice:latest
```

### Características de Seguridad en Producción

- Usuario `nonroot` para ejecutar la aplicación
- Imagen base `distroless` (minimiza superficie de ataque)
- HTTPS recomendado (configurar en ingress)
- Health checks configurados
- Optimizaciones de JVM para rendimiento

## Monitoreo

### Verificar salud de la aplicación

```bash
curl http://localhost:8080/actuator/health
```

### Ver métricas

```bash
curl http://localhost:8080/actuator/metrics
```

## Troubleshooting

### La aplicación no inicia

```bash
# Ver logs del contenedor
docker logs <container_id>

# Ver logs completos de docker-compose
docker-compose logs -f
```

### Problema con la base de datos

```bash
# Conectar al contenedor de PostgreSQL
docker-compose exec postgres psql -U tumipay -d tumipay_db

# Ver estado del volumen
docker volume inspect tumipay-microservice-template_postgres_data
```

### Limpiar recursos

```bash
# Eliminar contenedores detenidos
docker container prune

# Eliminar imágenes no utilizadas
docker image prune

# Eliminar volúmenes no utilizados
docker volume prune

# Limpieza completa (cuidado)
docker system prune -a --volumes
```

## Mejores Prácticas

1. **Siempre usa tags versionados**: `tumipay-microservice:v1.0.0`
2. **Escanea imágenes en busca de vulnerabilidades**: `docker scout cves`
3. **Usa registros privados** para imágenes en producción
4. **Implementa CI/CD** para automatizar construcción y despliegue
5. **Monitorea recursos** en contenedores
6. **Mantén actualizadas** las imágenes base

## Referencias

- [GraalVM Native Image](https://www.graalvm.org/native-image/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

