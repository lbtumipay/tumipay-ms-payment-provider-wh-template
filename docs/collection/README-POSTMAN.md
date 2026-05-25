# Colección Postman - TumiPay Microservice API

## Descripción General

Esta colección contiene todos los endpoints disponibles para el Microservicio TumiPay. Incluye ejemplos de solicitudes, respuestas esperadas y pruebas automatizadas.

**Versión de Colección:** 2.1.0  
**API Version:** v1  
**Última Actualización:** 2026-03-04  
**Autor:** TumiPay SAS

---

## Requisitos Previos

- **Postman** (v10.0 o superior)
- **Microservicio TumiPay** ejecutándose localmente o en un servidor accesible
- **Java 21** instalado en el servidor
- **Spring Boot 4.0.2**

---

## Instalación y Configuración

### 1. Importar la Colección

1. Abre Postman
2. Haz clic en **Import** (esquina superior izquierda)
3. Selecciona la opción **Upload Files**
4. Navega a `docs/collection/TumiPay-Microservice-API.postman_collection.json`
5. Haz clic en **Import**

### 2. Configurar Variables de Entorno

La colección utiliza variables para facilitar el cambio entre diferentes ambientes. Debes configurar:

#### Variables Principales:
| Variable | Valor por Defecto | Descripción |
|----------|------------------|-------------|
| `baseUrl` | `localhost:8080` | URL base del servicio |
| `serviceName` | *(vacío)* | Nombre del servicio (se llena automáticamente) |
| `serviceVersion` | *(vacío)* | Versión del servicio (se llena automáticamente) |
| `serviceEnvironment` | *(vacío)* | Ambiente del servicio (se llena automáticamente) |

#### Configurar por Ambiente:

**Desarrollo (Dev):**
```
baseUrl: localhost:8080
```

**Staging:**
```
baseUrl: staging-api.tumipay.co:8080
```

**Producción (Prod):**
```
baseUrl: api.tumipay.co:8080
```

---

## Estructura de la Colección

### 📦 Service Information
Grupo de endpoints relacionados con la información del servicio.

#### **GET /api/v1/service/info**
Obtiene información completa del microservicio usando la estructura estándar BaseApiResponse.

**URL:** `{{baseUrl}}/api/v1/service/info`

**Método:** `GET`

**Headers:**
- `Content-Type: application/json`
- `Accept: application/json`

**Estructura de Respuesta (BaseApiResponse):**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| code | string | Código de la respuesta |
| status | string | Estado (SUCCESS o ERROR) |
| message | string | Mensaje descriptivo |
| data | object | Objeto con los datos solicitados |
```json
{
  "code": "SUCCESS",
  "status": "SUCCESS",
  "message": null,
  "data": {
    "serviceName": "tumipay-microservice",
    "serviceDescription": "TumiPay Microservice Template",
    "version": "0.0.1",
    "environment": "dev",
    "javaVersion": "21.0.2",
    "springBootVersion": "4.0.2",
    "timestamp": "2026-03-04T12:00:00"
  }
}
```

**Response - 500 Internal Server Error:**
```json
{
  "code": "INTERNAL_ERROR",
  "status": "ERROR",
  "message": "Se produjo un error interno al procesar la solicitud",
  "data": null
}
```

---

## Pruebas Automatizadas

La colección incluye scripts de prueba que se ejecutan automáticamente después de cada solicitud.

### Pruebas en `/api/v1/service/info`:

1. ✅ Validar que el código de estado sea 200
2. ✅ Validar que la respuesta contenga la estructura BaseApiResponse
3. ✅ Validar que `status` sea `SUCCESS`
4. ✅ Validar que los datos contienen los campos del servicio
5. ✅ Validar los tipos de datos de los campos
6. ✅ Guardar automáticamente los valores en variables de entorno

---

## Ejemplos de Uso

### Ejemplo 1: Obtener Información del Servicio

```bash
# Usando cURL
curl -X GET \
  'http://localhost:8080/api/v1/service/info' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json'
```

### Ejemplo 2: Automatizar Múltiples Solicitudes

1. Abre la pestaña **Runner** en Postman
2. Selecciona la colección "TumiPay Microservice API"
3. Haz clic en **Run**
4. Los resultados se mostrarán en tiempo real

---

## Variables de Entorno Auto-Populadas

Después de ejecutar el endpoint `/api/v1/service/info`, las siguientes variables se llenarán automáticamente:

- `serviceName`: Nombre del servicio
- `serviceVersion`: Versión del servicio
- `serviceEnvironment`: Ambiente del servicio (dev/staging/prod)

Puedes usar estas variables en futuras solicitudes con la sintaxis `{{variableName}}`.

---

## Solución de Problemas

### Problema: "Cannot GET /api/v1/service/info"
**Solución:** 
- Verifica que el microservicio está ejecutándose
- Confirma que `baseUrl` es correcto
- Revisa que la URL no tenga espacios o caracteres especiales

### Problema: "Connection Refused"
**Solución:**
- Asegúrate de que el servidor está escuchando en el puerto correcto
- Verifica la configuración de firewall
- Comprueba que el host es accesible desde tu máquina

### Problema: "Internal Server Error (500)"
**Solución:**
- Revisa los logs del servidor
- Verifica que todas las propiedades de configuración están correctamente establecidas
- Intenta reiniciar el microservicio

---

## Buenas Prácticas

1. **Usar Variables:** Siempre usa `{{baseUrl}}` en lugar de URLs hardcodeadas
2. **Organización:** Mantén los endpoints agrupados lógicamente
3. **Documentación:** Describe cada endpoint con ejemplos y casos de uso
4. **Pruebas:** Ejecuta las pruebas después de cambios en la API
5. **Versionado:** Mantén múltiples versiones de colecciones si trabajas con diferentes versiones de API

---

## Exportación de Resultados

Para exportar los resultados de las pruebas:

1. En el **Runner**, después de ejecutar, haz clic en **Export Results**
2. Selecciona el formato (JSON, CSV, etc.)
3. Guarda el archivo en un directorio seguro

---

## Soporte y Contacto

Para reportar problemas o sugerencias sobre esta colección:
- **Proyecto:** TumiPay Microservice Template
- **Repositorio:** https://github.com/TumiPay/tumipay-microservice-template
- **Licencia:** Proprietary - TumiPay SAS

---

## Changelog

### v0.0.2 (2026-03-04)
- ✨ Actualización: Uso de estructura estándar BaseApiResponse
- ✨ Campos: code, status, message, data
- ✨ Pruebas automatizadas actualizadas

### v0.0.1 (2026-03-04)
- ✨ Creación inicial de la colección
- ✨ Endpoint: GET /api/v1/service/info
- ✨ Pruebas automatizadas
- ✨ Variables de entorno configurables

---

**Última Actualización:** 2026-03-04  
**Creado por:** TumiPay SAS  
**Estado:** Activo ✅





