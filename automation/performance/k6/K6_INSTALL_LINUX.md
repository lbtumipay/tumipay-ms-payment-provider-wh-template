# ⚙️ Guía de Instalación — Linux

> **Proyecto:** `tumipay-ms-payment-provider-template`  
> **Autor:** TumiPay SAS — Engineering Standards Team  
> **Fecha:** 2026-04-23

Guía paso a paso para instalar todas las dependencias necesarias y ejecutar la suite de pruebas de performance con **k6** en **Linux** (Ubuntu/Debian, Fedora/RHEL/CentOS y derivados).

---

## 📋 Requisitos del sistema

| Requisito | Versión mínima |
|-----------|---------------|
| Linux | Distribución de 64 bits |
| Shell | Bash o compatible |
| Memoria RAM | 4 GB mínimo (8 GB recomendado para pruebas stress) |
| Disco libre | 500 MB |

---
## 🛠️ Opción 1 — Instalación con repositorio oficial (recomendada)

### A) Debian / Ubuntu y derivados

### Paso 1 — Agregar repositorio de k6

```bash
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://dl.k6.io/key.gpg | sudo gpg --dearmor -o /etc/apt/keyrings/k6-archive-keyring.gpg
echo "deb [signed-by=/etc/apt/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list > /dev/null
