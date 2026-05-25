# 🚀 TumiPay — Suite de Performance k6

> **Proyecto:** `tumipay-ms-payment-provider-template`  
> **Autor:** TumiPay SAS — Engineering Standards Team  
> **Fecha:** 2026-04-19

Suite completa de pruebas de performance con [k6](https://k6.io/) para el microservicio de pagos TumiPay. Cubre todos los controladores REST: PayIn, PayOut, consulta de transacciones e info del microservicio.

---

## 📁 Estructura

```
automation/performance/k6/
├── main.js                        ← Orquestador: ejecuta todos los escenarios en paralelo
├── payin-test.js                  ← Escenarios PayInTransactionController
├── payout-test.js                 ← Escenarios PayOutTransactionController
├── transaction-query-test.js      ← Escenarios TransactionController (GET)
├── microservice-info-test.js      ← Escenarios MicroServiceController (GET)
├── config/
│   ├── options.js                 ← Definiciones smoke / load / stress / spike
│   └── thresholds.js              ← Umbrales globales de performance
├── data/
│   ├── payin-payload.json         ← Dataset PayIn (3 registros)
│   └── payout-payload.json        ← Dataset PayOut (3 registros)
├── utils/
│   ├── helpers.js                 ← uuidv4(), buildHeaders(), parseBody()
│   └── summary.js                 ← buildSummaryOutputs(), generateCsvSummary()
└── results/                       ← 📂 Generado automáticamente al ejecutar
    ├── payin/
    │   ├── payin-summary-<timestamp>.json
    │   └── payin-summary-<timestamp>.csv
    ├── payout/
    ├── query/
    ├── info/
    └── main/
```

---

## ⚙️ Instalación

Consulta la guía detallada según tu sistema operativo:

| Sistema operativo                | Guía de instalación                            |
|----------------------------------|------------------------------------------------|
| 🪟 Windows 10 / 11               | [K6_INSTALL_WINDOWS.md](K6_INSTALL_WINDOWS.md) |
| 🍎 macOS (Intel + Apple Silicon) | [K6_INSTALL_MACOS.md](K6_INSTALL_MACOS.md)    |
| Guía para Entender el Reporte K6 | [K6_REPORT_GUIDE.md](K6_REPORT_GUIDE.md)    |

### Instalación rápida

```bash
# macOS
brew install k6

# Windows (PowerShell como Administrador)
choco install k6 -y

# Windows (winget)
winget install k6 --source winget
```

Verifica la instalación:
```bash
k6 version
```

---

## 🔑 Variables de entorno

| Variable | Descripción | Valor por defecto       |
|---|---|-------------------------|
| `BASE_URL` | URL base del microservicio | `http://localhost:8000` |
| `API_KEY` | API Key para autenticación (`X-Api-Key`) | `test-api-key`          |
| `MERCHANT_ID` | ID del merchant (`X-Merchant-ID`) | `test-merchant`         |
| `TEST_TYPE` | Tipo de prueba: `smoke`, `load`, `stress`, `spike` | `smoke`                 |
| `TRANSACTION_ID` | ID de transacción pre-existente para pruebas de consulta | `""`                    |
| `ADAPTER_TRANSACTION_ID` | ID de transacción del adaptador para consulta | `""`                    |
| `PROVIDER_TRANSACTION_ID` | ID de transacción del proveedor para consulta | `""`                    |

---

## 📄 Reportes automáticos (JSON + CSV)

Al finalizar **cada ejecución**, k6 genera automáticamente dos archivos en `results/<script>/`:

| Archivo | Formato | Contenido |
|---------|---------|-----------|
| `<label>-summary-<timestamp>.json` | JSON | Resumen completo: métricas, checks, thresholds, grupos |
| `<label>-summary-<timestamp>.csv`  | CSV  | Tabla de métricas (avg, min, med, max, p90, p95, p99, count, rate) |

### Ejemplo de estructura CSV generada

```csv
script,metric,type,avg_ms,min_ms,med_ms,max_ms,p90_ms,p95_ms,p99_ms,count,rate,passes,fails
payin,http_req_duration,trend,312.450,201.000,298.000,1200.000,450.000,620.000,980.000,,,
payin,http_req_failed,rate,,,,,,,,30,0.000,,
payin,payin_transaction_duration,trend,314.100,203.000,300.000,1200.000,451.000,622.000,982.000,,,
payin,checks,rate,,,,,,,,,1.000,150,0
```

> Los archivos se crean en la ruta relativa desde donde se invoca `k6 run`.  
> Asegúrate de ejecutar los comandos **desde la raíz del proyecto**.

---

## ▶️ Comandos de ejecución

### Pruebas individuales por controlador

```bash
# ── PayIn ──────────────────────────────────────────────────────────────────
# Smoke (1 VU / 30s)
k6 run automation/performance/k6/payin-test.js \
  -e BASE_URL=http://localhost:8000 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=smoke

# Load (10 VUs / 5m rampa)
k6 run automation/performance/k6/payin-test.js \
  -e BASE_URL=http://localhost:8000 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=load

# Stress (hasta 100 VUs)
k6 run automation/performance/k6/payin-test.js \
  -e BASE_URL=http://localhost:8000 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=stress

# Spike (100 VUs súbitos)
k6 run automation/performance/k6/payin-test.js \
  -e BASE_URL=http://localhost:8000 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=spike

# ── PayOut ─────────────────────────────────────────────────────────────────
k6 run automation/performance/k6/payout-test.js \
  -e BASE_URL=http://localhost:8000 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=load

# ── Consulta de Transacciones ──────────────────────────────────────────────
k6 run automation/performance/k6/transaction-query-test.js \
  -e BASE_URL=http://localhost:8000 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=stress \
  -e TRANSACTION_ID=txn-perf-001

# ── Info del Microservicio ─────────────────────────────────────────────────
k6 run automation/performance/k6/microservice-info-test.js \
  -e BASE_URL=http://localhost:8000 \
  -e TEST_TYPE=load
```

### Ejecución completa (orquestador)

```bash
# Todos los escenarios en paralelo
k6 run automation/performance/k6/main.js \
  -e BASE_URL=http://localhost:8000 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=load

# Con salida JSON para reporte
k6 run automation/performance/k6/main.js \
  -e BASE_URL=http://localhost:8000 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=load \
  --out json=automation/performance/k6/results/result.json
```

---

## 📊 Tipos de prueba

| Tipo | VUs | Duración | Propósito |
|------|-----|----------|-----------|
| `smoke` | 1 | 30s | Verificar que el endpoint responde correctamente bajo carga mínima |
| `load` | hasta 10 | 5m (rampa 1m↑ + 3m sostenida + 1m↓) | Simular carga sostenida representativa de producción |
| `stress` | hasta 100 | ~16m | Determinar el punto de quiebre del sistema |
| `spike` | 100 (súbito) | 80s | Detectar degradación ante picos de tráfico |

---

## ✅ Umbrales de aceptación (Thresholds)

| Métrica | Umbral |
|---------|--------|
| `http_req_duration` p(95) | < 2000 ms |
| `http_req_duration` p(99) | < 5000 ms |
| `http_req_failed` | < 1% |
| `payin_transaction_duration` p(95) | < 2000 ms |
| `payout_transaction_duration` p(95) | < 2000 ms |
| `query_transaction_duration` p(95) | < 1000 ms |
| `info_microservice_duration` p(95) | < 500 ms |

---

## 📈 Ejemplo de salida esperada

```
✓ payin HTTP 200
✓ payin code PROCESS_COMPLETED
✓ payin status SUCCESS
✓ payin adapter_transaction_id presente
✓ payin response time < 2s

checks.........................: 100.00% ✓ 150  ✗ 0
data_received..................: 45 kB   1.5 kB/s
data_sent......................: 38 kB   1.3 kB/s
http_req_duration..............: avg=312ms  min=201ms  med=298ms  max=1.2s   p(90)=450ms  p(95)=620ms
http_req_failed................: 0.00%   ✓ 0    ✗ 30
payin_transaction_duration.....: avg=314ms  min=203ms  med=300ms  max=1.2s   p(90)=451ms  p(95)=622ms
payin_transaction_errors.......: 0.00%   ✓ 0    ✗ 30
payin_transaction_requests.....: 30      1/s
```

---

## 🔍 Interpretación de resultados

- **`checks` al 100%**: Todos los assertions pasaron → el endpoint cumple el contrato definido.
- **`http_req_failed` = 0%**: No hubo errores de red ni respuestas 4xx/5xx inesperadas.
- **Thresholds en verde (✓)**: El sistema cumple los SLA de performance definidos.
- **Thresholds en rojo (✗)**: El sistema está degradado; revisar logs del microservicio y recursos del servidor.

> **Tip:** Si un threshold falla en `stress`, es esperado: indica el punto de saturación. El objetivo es que `smoke` y `load` siempre pasen.

---

## 🗂️ Headers utilizados

| Header | Valor | Endpoints |
|--------|-------|-----------|
| `X-Api-Key` | `$API_KEY` | PayIn, PayOut, Transactions |
| `X-Request-ID` | UUID v4 generado por iteración | Todos |
| `X-Correlation-ID` | UUID v4 generado por iteración | Todos |
| `X-Idempotency-Key` | UUID v4 generado por iteración | PayIn, PayOut |
| `X-Merchant-ID` | `$MERCHANT_ID` | PayIn, PayOut |
| `Content-Type` | `application/json` | POST endpoints |

