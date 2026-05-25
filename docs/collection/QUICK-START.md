# 🚀 Guía Rápida - Colección Postman TumiPay

## Inicio Rápido (5 minutos)

### Paso 1: Importar la Colección
```
1. Abre Postman
2. Click en "Import" (arriba a la izquierda)
3. Selecciona: TumiPay-Microservice-API.postman_collection.json
4. Click "Import"
```

### Paso 2: Importar Ambiente
```
1. Click en el ícono de Engranaje (Settings) - arriba a la derecha
2. Click en "Manage Environments"
3. Click en "Import"
4. Selecciona uno de estos archivos según tu ambiente:
   - TumiPay-Microservice-Dev-Environment.postman_environment.json (Desarrollo)
   - TumiPay-Microservice-Staging-Environment.postman_environment.json (Staging)
   - TumiPay-Microservice-Prod-Environment.postman_environment.json (Producción)
```

### Paso 3: Seleccionar Ambiente
```
1. Arriba a la derecha donde dice "No Environment"
2. Selecciona el ambiente que importaste (Dev, Staging o Prod)
```

### Paso 4: Hacer tu Primer Request
```
1. En la colección, expande "Service Information"
2. Click en "Get Microservice Info"
3. Click en el botón azul "Send"
4. ¡Verás la respuesta en la pestaña "Response"!
```

---

## 📊 Estructura de Respuesta

Todas las respuestas siguen este patrón estándar de TumiPay:

```json
{
  "code": "código de respuesta",
  "status": "SUCCESS o ERROR",
  "message": "texto del mensaje",
  "data": { /* objeto con los datos */ }
}
```

---

## 🔧 Configuración Manual (Si es necesario)

Si tu servidor está en un puerto o URL diferente:

1. Haz click en el ícono del Engranaje (Settings)
2. Selecciona tu ambiente
3. Busca la variable `baseUrl`
4. Cambia el valor a tu URL, por ejemplo:
   - Dev local: `localhost:8080`
   - Dev en red: `192.168.1.100:8080`
   - Server: `tu-dominio.com`

---

## ✅ Pruebas Automáticas

Cuando ejecutas un request, Postman corre automáticamente pruebas para validar:

- ✓ Código de estado correcto (200)
- ✓ Estructura BaseApiResponse válida
- ✓ Status = SUCCESS
- ✓ Campos esperados en los datos
- ✓ Tipos de datos válidos
- ✓ Variables se llenan automáticamente

Verifica la pestaña "Test Results" para ver los detalles.

---

## 💡 Tips Útiles

### Usar Variables
En cualquier URL o body, usa `{{nombreVariable}}` para referenciar variables:
```
{{baseUrl}}/api/v1/service/info
```

### Variables Dinámicas
Después de llamar a `/service/info`, se llenan automáticamente:
- `{{serviceName}}` - Nombre del servicio
- `{{serviceVersion}}` - Versión actual
- `{{serviceEnvironment}}` - Ambiente activo

### Copiar cURL
1. Click derecho en un request
2. "Copy as cURL"
3. Pega en tu terminal

### Ejecutar Colección Completa
1. Click en el ícono de carpeta de la colección
2. Click en "Run"
3. Verás todas las pruebas ejecutarse

---

## 🆘 Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `Cannot GET /api/v1/service/info` | Servidor no activo | Inicia el microservicio Java |
| `Connection refused` | URL incorrecta o puerto cerrado | Verifica `baseUrl` y firewall |
| `status: ERROR` | Error en servidor | Revisa los logs del servidor |
| `Variables no se llenan` | Scripts desactivados | Activa Scripts en Settings |

---

## 📝 Ejemplo cURL

Si prefieres usar línea de comandos:

```bash
curl -X GET \
  'http://localhost:8080/api/v1/service/info' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json'
```

---

## 🔐 Seguridad

⚠️ **Recuerda:**
- No commitees archivos con contraseñas reales
- En Producción, usa HTTPS (https://)
- Usa tokens/API keys cuando sea necesario
- Mantén las colecciones seguras en control de versiones

---

## 📚 Documentación Completa

Para más detalles, consulta: `README-POSTMAN.md`

---

## 🎯 Próximos Pasos

1. ✅ Prueba el endpoint `/service/info`
2. 📝 Explora las respuestas
3. 🧪 Ejecuta las pruebas automáticas
4. 🔄 Cambia entre ambientes
5. 🚀 Integra con tu flujo de trabajo

---

**¿Necesitas ayuda?** Revisa los comentarios en cada request o consulta la documentación completa.

¡Listo para empezar! 🎉




