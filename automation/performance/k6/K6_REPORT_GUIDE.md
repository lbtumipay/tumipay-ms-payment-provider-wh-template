# 📊 Guía para Interpretar Reportes de k6 (Performance Testing)

## 📅 Contexto

Este documento explica cómo interpretar los resultados generados por **k6** en pruebas de performance, incluyendo:

- Validación funcional (checks)
- Métricas HTTP
- Distribución de latencia
- Breakdown interno de requests
- Diagnóstico arquitectónico

---

# 🧠 1. Resumen de un Reporte k6

Un reporte típico contiene:

- ✔ Checks (validaciones funcionales)
- 📊 Métricas de red y backend
- ⏱️ Tiempos de respuesta
- 🔁 Throughput
- ❌ Errores

---

# ✅ 2. Checks (Validación Funcional)

Ejemplo:

```
checks.........................: 100.00% ✓ 150 ✗ 0
```

## 📌 Qué significa

- Se ejecutaron 150 validaciones
- 150 pasaron, 0 fallaron

## 🧠 Interpretación

Los checks validan:

- HTTP status (200)
- Contrato de respuesta (code, status)
- Campos obligatorios
- SLA de tiempo

---

# 📊 3. Métricas de Latencia

Ejemplo:

```
http_req_duration: 
avg=25.79ms  
min=15.45ms  
med=26.8ms  
max=32.12ms  
p(90)=30.12ms  
p(95)=31.35ms
```

## 🔍 Definiciones

- **avg**: Promedio
- **min**: Tiempo mínimo
- **med**: Mediana (valor típico)
- **max**: Tiempo máximo
- **p(90)**: 90% de requests son más rápidas que este valor
- **p(95)**: 95% de requests son más rápidas que este valor

---

# 🧩 4. Breakdown Interno de una Request

```
blocked → connecting → tls → sending → waiting → receiving
```

### 🔹 http_req_blocked
Tiempo en cola antes de enviar la request

### 🔹 http_req_connecting
Tiempo en abrir conexión TCP

### 🔹 http_req_tls_handshaking
Handshake TLS (HTTPS)

### 🔹 http_req_sending
Tiempo enviando request

### 🔹 http_req_waiting (CRÍTICO)
Tiempo del backend (lógica de negocio)

### 🔹 http_req_receiving
Tiempo recibiendo respuesta

### 🔹 http_req_duration
Tiempo total

---

# 🎯 5. Diagnóstico Rápido

| Métrica alta | Problema |
|-------------|--------|
| blocked | saturación |
| connecting | red |
| tls | SSL |
| waiting | backend |
| receiving | payload grande |

---

# 🔁 6. Throughput

- **http_reqs**: total requests
- **rate**: requests por segundo

---

# 🔄 7. Iterations

- Ejecuciones del script
- Incluye lógica + sleep

---

# ❌ 8. Errores

- **http_req_failed**: % de fallos

---

# 🚨 9. Thresholds

Reglas como:

- error rate < 1%
- p95 < 200ms

---

# 🎯 10. Conclusión

Un reporte de k6 permite entender:

- Performance
- Estabilidad
- Calidad funcional

> 🔥 El objetivo no es solo rapidez, sino consistencia y corrección
