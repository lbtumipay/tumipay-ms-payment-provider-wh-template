# ⚙️ Guía de Instalación — macOS

> **Proyecto:** `tumipay-ms-payment-provider-template`  
> **Autor:** TumiPay SAS — Engineering Standards Team  
> **Fecha:** 2026-04-19

Guía paso a paso para instalar todas las dependencias necesarias y ejecutar la suite de pruebas de performance con **k6** en **macOS** (Intel y Apple Silicon M1/M2/M3/M4).

---

## 📋 Requisitos del sistema

| Requisito | Versión mínima |
|-----------|---------------|
| macOS | 12 Monterey o superior (recomendado 13 Ventura+) |
| Terminal | Terminal.app, iTerm2 o similar |
| Arquitectura | Intel x86_64 / Apple Silicon ARM64 (ambas soportadas) |
| Memoria RAM | 4 GB mínimo (8 GB recomendado para pruebas stress) |
| Disco libre | 500 MB |

---

## 🛠️ Opción 1 — Instalación con Homebrew (recomendada)

Homebrew es el gestor de paquetes estándar de macOS.

### Paso 1 — Instalar Homebrew

Abre una **Terminal** y ejecuta:

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

> Si ya tienes Homebrew, actualízalo:
> ```bash
> brew update && brew upgrade
> ```

Verifica la instalación:

```bash
brew --version
```

#### Apple Silicon (M1/M2/M3/M4) — configuración adicional

En chips Apple Silicon, Homebrew se instala en `/opt/homebrew`. Agrega esto a tu shell si aún no está configurado:

```bash
# Para zsh (shell por defecto en macOS 10.15+)
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
eval "$(/opt/homebrew/bin/brew shellenv)"
```

### Paso 2 — Instalar k6

```bash
brew install k6
```

Homebrew detecta automáticamente la arquitectura (Intel o Apple Silicon) e instala el binario correcto.

### Paso 3 — Verificar instalación de k6

```bash
k6 version
```

Salida esperada:
```
k6 v0.55.x (go1.xx.x, darwin/arm64)   # Apple Silicon
k6 v0.55.x (go1.xx.x, darwin/amd64)   # Intel
```

---

## 🛠️ Opción 2 — Instalación manual (sin gestor de paquetes)

### Paso 1 — Descargar el binario

1. Ve a [https://github.com/grafana/k6/releases/latest](https://github.com/grafana/k6/releases/latest)
2. Descarga el archivo correspondiente a tu arquitectura:
   - **Apple Silicon (M1/M2/M3/M4):** `k6-vX.XX.X-macos-arm64.zip`
   - **Intel:** `k6-vX.XX.X-macos-amd64.zip`

### Paso 2 — Instalar el binario

```bash
# Descomprimir (ajusta el nombre del archivo según la versión descargada)
unzip k6-v*.zip

# Mover a directorio del sistema
sudo mv k6 /usr/local/bin/

# Dar permisos de ejecución
sudo chmod +x /usr/local/bin/k6
```

### Paso 3 — Permitir ejecución en macOS (Gatekeeper)

Si macOS bloquea la ejecución con el mensaje _"k6 no se puede abrir porque es de un desarrollador no identificado"_:

```bash
sudo xattr -d com.apple.quarantine /usr/local/bin/k6
```

### Paso 4 — Verificar instalación

```bash
k6 version
```

---

## 📁 Paso 4 — Clonar / preparar el proyecto

Si aún no tienes el proyecto, clónalo:

```bash
git clone https://github.com/tumipay/tumipay-ms-payment-provider-template.git
cd tumipay-ms-payment-provider-template
```

---

## 📂 Paso 5 — Crear los directorios de resultados

Los scripts guardan los reportes JSON y CSV en subdirectorios de `results/`. Créalos antes de la primera ejecución:

```bash
mkdir -p automation/performance/k6/results/{payin,payout,query,info,main}
```

---

## ▶️ Paso 6 — Ejecutar las pruebas

> ⚠️ Ejecuta todos los comandos desde la **raíz del proyecto** para que las rutas relativas funcionen correctamente.

### Prueba de humo — PayIn (verificación rápida)

```bash
k6 run automation/performance/k6/payin-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=smoke
```

### Prueba de carga — PayOut

```bash
k6 run automation/performance/k6/payout-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=load
```

### Prueba de estrés — Consulta de transacciones

```bash
k6 run automation/performance/k6/transaction-query-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=stress \
  -e TRANSACTION_ID=txn-perf-001
```

### Prueba de info del microservicio

```bash
k6 run automation/performance/k6/microservice-info-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e TEST_TYPE=load
```

### Ejecución completa — Todos los escenarios en paralelo

```bash
k6 run automation/performance/k6/main.js \
  -e BASE_URL=http://localhost:8080 \
  -e API_KEY=my-api-key \
  -e TEST_TYPE=load
```

---

## 📄 Paso 7 — Verificar los reportes generados

Después de cada ejecución, los reportes se guardan automáticamente:

```bash
# Listar reportes generados para PayIn
ls -lh automation/performance/k6/results/payin/

# Ver el contenido del último CSV generado
ls -t automation/performance/k6/results/payin/*.csv | head -1 | xargs cat
```

---

## 🔧 Solución de problemas frecuentes

| Problema | Causa probable | Solución |
|----------|---------------|----------|
| `command not found: k6` | k6 no está en el PATH | Cierra y vuelve a abrir la terminal, o ejecuta `source ~/.zprofile` |
| `"k6" no puede abrirse` (Gatekeeper) | macOS bloqueó el binario no firmado | Ejecuta `sudo xattr -d com.apple.quarantine /usr/local/bin/k6` |
| `ECONNREFUSED` en la prueba | El microservicio no está corriendo | Inicia el microservicio con `./mvnw spring-boot:run` o Docker |
| `Error: open ./data/payin-payload.json` | El comando no se ejecuta desde la raíz del proyecto | Navega a la raíz: `cd ~/.../<proyecto>` |
| `cannot create output file` | El directorio `results/` no existe | Ejecuta el **Paso 5** para crear los directorios |
| Homebrew lento en Apple Silicon | Rosetta 2 interfiere | Asegúrate de usar la terminal nativa (no bajo Rosetta): `arch -arm64 brew install k6` |

---

## 🍎 Nota sobre Apple Silicon (M1 / M2 / M3 / M4)

k6 tiene soporte nativo para ARM64 desde la versión `v0.37.0`. Al instalar con Homebrew, el binario ARM64 se selecciona automáticamente.

Si necesitas verificar la arquitectura del binario instalado:

```bash
file $(which k6)
# Salida esperada en Apple Silicon:
# /opt/homebrew/bin/k6: Mach-O 64-bit executable arm64
```

---

## 🔗 Referencias

- [Documentación oficial de k6](https://k6.io/docs/)
- [Releases de k6 en GitHub](https://github.com/grafana/k6/releases)
- [Homebrew — fórmula k6](https://formulae.brew.sh/formula/k6)
- [README principal de la suite k6](K6_README.md)

