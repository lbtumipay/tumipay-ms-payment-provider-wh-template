# ⚙️ Guía de Instalación — Windows

> **Proyecto:** `tumipay-ms-payment-provider-template`  
> **Autor:** TumiPay SAS — Engineering Standards Team  
> **Fecha:** 2026-04-19

Guía paso a paso para instalar todas las dependencias necesarias y ejecutar la suite de pruebas de performance con **k6** en **Windows 10 / Windows 11**.

---

## 📋 Requisitos del sistema

| Requisito | Versión mínima |
|-----------|---------------|
| Windows | 10 (64-bit) / Windows 11 |
| PowerShell | 5.1 o superior (incluido en Windows) |
| Memoria RAM | 4 GB mínimo (8 GB recomendado para pruebas stress) |
| Disco libre | 500 MB |

---
## 🛠️ Opción 1 — Instalación con winget (Windows Package Manager)

`winget` viene preinstalado en Windows 10 (21H1+) y Windows 11.

### Paso 1 — Instalar k6

Abre **PowerShell** y ejecuta:

```powershell
winget install k6 --source winget
```

### Paso 2 — Verificar instalación

```powershell
k6 version
```

---

## 🛠️ Opción 2 — Instalación manual (sin gestor de paquetes)

### Paso 1 — Descargar el binario

1. Ve a [https://github.com/grafana/k6/releases/latest](https://github.com/grafana/k6/releases/latest)
2. Descarga el archivo `k6-vX.XX.X-windows-amd64.zip`
3. Extrae el contenido en una carpeta, por ejemplo: `C:\tools\k6\`

### Paso 2 — Agregar k6 al PATH del sistema

1. Abre **Panel de Control** → **Sistema** → **Configuración avanzada del sistema**
2. Haz clic en **Variables de entorno**
3. En **Variables del sistema**, selecciona `Path` → **Editar**
4. Haz clic en **Nuevo** y agrega la ruta: `C:\tools\k6`
5. Acepta todos los diálogos con **Aceptar**
6. Cierra y vuelve a abrir PowerShell

### Paso 3 — Verificar instalación

```powershell
k6 version
```

---

## 📁 Paso 4 — Clonar / preparar el proyecto

Si aún no tienes el proyecto, clónalo:

```powershell
git clone https://github.com/tumipay/tumipay-ms-payment-provider-template.git
cd tumipay-ms-payment-provider-template
```

---

## 📂 Paso 5 — Crear los directorios de resultados

Los scripts guardan los reportes JSON y CSV en subdirectorios de `results/`. Créalos antes de la primera ejecución:

```powershell
New-Item -ItemType Directory -Force -Path "automation\performance\k6\results\payin"
New-Item -ItemType Directory -Force -Path "automation\performance\k6\results\payout"
New-Item -ItemType Directory -Force -Path "automation\performance\k6\results\query"
New-Item -ItemType Directory -Force -Path "automation\performance\k6\results\info"
New-Item -ItemType Directory -Force -Path "automation\performance\k6\results\main"
```

---

## ▶️ Paso 6 — Ejecutar las pruebas

> ⚠️ Ejecuta todos los comandos desde la **raíz del proyecto** para que las rutas relativas funcionen correctamente.

### Prueba de humo — PayIn (verificación rápida)

```powershell
k6 run automation/performance/k6/payin-test.js `
  -e BASE_URL=http://localhost:8080 `
  -e API_KEY=my-api-key `
  -e TEST_TYPE=smoke
```

### Prueba de carga — PayOut

```powershell
k6 run automation/performance/k6/payout-test.js `
  -e BASE_URL=http://localhost:8080 `
  -e API_KEY=my-api-key `
  -e TEST_TYPE=load
```

### Prueba de estrés — Consulta de transacciones

```powershell
k6 run automation/performance/k6/transaction-query-test.js `
  -e BASE_URL=http://localhost:8080 `
  -e API_KEY=my-api-key `
  -e TEST_TYPE=stress `
  -e TRANSACTION_ID=txn-perf-001
```

### Prueba de info del microservicio

```powershell
k6 run automation/performance/k6/microservice-info-test.js `
  -e BASE_URL=http://localhost:8080 `
  -e TEST_TYPE=load
```

### Ejecución completa — Todos los escenarios en paralelo

```powershell
k6 run automation/performance/k6/main.js `
  -e BASE_URL=http://localhost:8080 `
  -e API_KEY=my-api-key `
  -e TEST_TYPE=load
```

---

## 📄 Paso 7 — Verificar los reportes generados

Después de cada ejecución, los reportes se guardan automáticamente:

```powershell
# Listar reportes generados para PayIn
Get-ChildItem automation\performance\k6\results\payin\

# Ver el contenido del último CSV generado
Get-ChildItem automation\performance\k6\results\payin\ -Filter "*.csv" |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1 |
  Get-Content
```

---

## 🔧 Solución de problemas frecuentes

| Problema | Causa probable | Solución |
|----------|---------------|----------|
| `k6 : El término 'k6' no se reconoce` | k6 no está en el PATH | Cierra y vuelve a abrir PowerShell, o reinicia el equipo |
| `ECONNREFUSED` en la prueba | El microservicio no está corriendo | Inicia el microservicio con `./mvnw spring-boot:run` o Docker |
| `Error: open ./data/payin-payload.json` | El comando no se ejecuta desde la raíz del proyecto | Navega a la raíz: `cd C:\...\tumipay-ms-payment-provider-template` |
| `cannot create output file` | El directorio `results/` no existe | Ejecuta el **Paso 5** para crear los directorios |
| Permisos denegados en Chocolatey | PowerShell no está como Administrador | Ejecuta PowerShell con "Ejecutar como administrador" |

---

## 🔗 Referencias

- [Documentación oficial de k6](https://k6.io/docs/)
- [Releases de k6 en GitHub](https://github.com/grafana/k6/releases)
- [Chocolatey — k6 package](https://community.chocolatey.org/packages/k6)
- [README principal de la suite k6](K6_README.md)

