# TumiPay – Integration Tests (Newman)

Pruebas de integración para el microservicio `tumipay-ms-payment-provider-template`, ejecutadas con [Newman](https://learning.postman.com/docs/collections/using-newman-cli/command-line-integration-with-newman/) sobre colecciones Postman.

---

## Requisitos previos

| Herramienta | Versión mínima | Verificar |
|---|---|---|
| Node.js | 18+ | `node --version` |
| npm | 9+ | `npm --version` |
| Microservicio corriendo | — | `http://localhost:8000` |

### Instalar dependencias

```bash
cd automation/postman
npm install
```

Esto instala:
- **newman** `^6.1.1` – runner de colecciones Postman en CLI
- **newman-reporter-htmlextra** `^1.23.1` – reportes HTML enriquecidos

---

## Estructura del directorio

```
automation/postman/
├── collections/
│   ├── payin_collection.json       # Tests de PayIn (creación de transacción)
│   ├── payout_collection.json      # Tests de PayOut (desembolso)
│   ├── query_collection.json       # Tests de consulta de transacciones
│   └── webhook_collection.json     # Tests de eventos webhook
├── enviroments/
│   ├── local_environment.json      # Variables para entorno local
│   └── dev_environment.json        # Variables para entorno dev
├── reports/                        # Reportes generados (git-ignored)
├── scripts/
│   └── run-tests.js                # Runner secuencial con encadenamiento de variables
├── package.json
└── README.md
```

---

## Ejecución

Todos los comandos deben ejecutarse desde `automation/postman/`.

### Suite completa (modo local)

Corre las 4 colecciones en orden secuencial y genera un **reporte HTML por colección** en `reports/`.

```bash
npm test
# equivalente a:
npm run test:local
```

Reportes generados en `reports/`:
```
reports/
├── report-local-payin-tests.html
├── report-local-payout-tests.html
├── report-local-query-tests.html
└── report-local-webhook-tests.html
```

### Suite completa (entorno dev)

```bash
npm run test:dev
```

Reportes generados en `reports/`:
```
reports/
├── report-dev-payin-tests.html
├── report-dev-payout-tests.html
├── report-dev-query-tests.html
└── report-dev-webhook-tests.html
```

### Modo CI – pipeline

Activa `bail` (detiene al primer fallo por colección) y genera un reporte `.json` por colección en `reports/`.

```bash
# Contra entorno local
npm run test:ci

# Contra entorno dev
npm run test:ci:dev
```

Reportes generados en `reports/`:
```
reports/
├── ci-report-payin-tests.json
├── ci-report-payout-tests.json
├── ci-report-query-tests.json
└── ci-report-webhook-tests.json
```

### Modo verbose – debug local

Corre toda la suite y genera un reporte HTML detallado por colección en `reports/`.

```bash
npm run test:verbose
```

Reportes generados en `reports/`:
```
reports/
├── verbose-report-payin-tests.html
├── verbose-report-payout-tests.html
├── verbose-report-query-tests.html
└── verbose-report-webhook-tests.html
```

### Colecciones individuales con reporte HTML

Útil para depurar una sola colección sin correr la suite entera.

```bash
# PayIn
npm run test:payin

# PayOut
npm run test:payout

# Webhook
npm run test:webhook

# Variantes en dev
npm run test:payin:dev
npm run test:payout:dev
npm run test:webhook:dev
```

---

## Variables de entorno

Las variables se definen en `enviroments/local_environment.json` y `enviroments/dev_environment.json`.

| Variable | Descripción | Quién la setea |
|---|---|---|
| `baseUrl` | URL base del microservicio | Manual en el `.json` |
| `merchant_id` | ID del comercio de prueba | Manual en el `.json` |
| `apiKey` | API Key para autenticación | Manual en el `.json` |
| `transaction_id` | ID de la transacción creada | `payin_collection` → `01_happy_path` |
| `adapter_transaction_id` | ID interno del adaptador | `payin_collection` → `01_happy_path` |
| `provider_transaction_id` | ID del proveedor externo | `payin_collection` → `01_happy_path` |
| `request_id` | UUID por request (auto) | Generado en prerequest script |
| `idempotency_key` | Clave de idempotencia (auto) | Generado en prerequest script |

> **Nota:** El runner secuencial (`run-tests.js`) persiste el environment entre colecciones usando un archivo temporal. Las colecciones de **Query** y **Webhook** consumen los IDs generados por **PayIn** automáticamente.

---

## Orden de ejecución de la suite

```
1. PayIn Tests    →  crea transacción y persiste IDs en environment
2. PayOut Tests   →  crea transacción de desembolso
3. Query Tests    →  consulta usando los IDs generados por PayIn
4. Webhook Tests  →  envía eventos usando provider_transaction_id de PayIn
```

---

## Integración en CI/CD

### Exit codes (códigos de salida del proceso)

El runner propaga un **exit code estándar de Unix/Linux** al proceso padre (pipeline) cuando termina:

| Exit code | Significado | Comportamiento en el pipeline |
|---|---|---|
| `0` | ✅ Todas las colecciones pasaron | Step marcado como **SUCCESS** |
| `1` | ❌ Una o más colecciones fallaron | Step marcado como **FAILURE** |

Este código es emitido por el proceso Node (`process.exit(0/1)` en `scripts/run-tests.js`) y propagado explícitamente tanto por `run-tests.sh` (`exit $EXIT_CODE`) como por `run-tests.bat` (`exit /b %EXIT_CODE%`).

> **¿Por qué importa?** Los sistemas CI como GitLab CI, GitHub Actions, Jenkins o Bitbucket Pipelines
> leen el exit code del último comando para decidir si el step fue exitoso o fallido.
> Sin un exit code no-cero, el pipeline no sabrá que las pruebas fallaron.

---

### Ejemplos de integración por plataforma

```yaml
# GitHub Actions
- name: Run integration tests
  working-directory: automation/postman
  run: |
    npm install
    npm run test:ci
  # Si test:ci retorna exit 1, este step falla y el workflow se detiene
```

```yaml
# GitLab CI
integration-tests:
  stage: test
  script:
    - cd automation/postman
    - npm install
    - npm run test:ci      # exit 1 → job FAILED; exit 0 → job PASSED
  artifacts:
    paths:
      - automation/postman/reports/
    when: always            # guardar reportes incluso si el job falla
```

```groovy
// Jenkins (Declarative Pipeline)
stage('Integration Tests') {
    steps {
        dir('automation/postman') {
            sh 'npm install'
            sh 'npm run test:ci'   // exit 1 lanza una excepción y marca el stage como FAILED
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'automation/postman/reports/**', allowEmptyArchive: true
        }
    }
}
```

```yaml
# Bitbucket Pipelines
- step:
    name: Integration Tests
    script:
      - cd automation/postman
      - npm install
      - npm run test:ci
    artifacts:
      - automation/postman/reports/**
```

El comando `test:ci` retorna **exit code 1** si alguna colección falla, lo que marca el pipeline como fallido.

---

## Referencia rápida de comandos

| Comando | Entorno | Reporte generado | Bail |
|---|---|---|---|
| `npm test` | local | `report-local-*.html` | No |
| `npm run test:local` | local | `report-local-*.html` | No |
| `npm run test:dev` | dev | `report-dev-*.html` | No |
| `npm run test:ci` | local | `ci-report-*.json` | **Sí** |
| `npm run test:ci:dev` | dev | `ci-report-*.json` | **Sí** |
| `npm run test:verbose` | local | `verbose-report-*.html` | No |
| `npm run test:payin` | local | `integration-test-report-payin.html` | No |
| `npm run test:payout` | local | `integration-test-report-payout.html` | No |
| `npm run test:webhook` | local | `integration-test-report-webhook.html` | No |

---

## Solución de problemas

| Error | Causa probable | Solución |
|---|---|---|
| `connect ECONNREFUSED localhost:8000` | El microservicio no está corriendo | Iniciar la aplicación antes de ejecutar |
| `Error: Cannot find module './collections/...'` | Ruta incorrecta o archivo faltante | Verificar que los `.json` existen en `collections/` |
| `ReferenceError: crypto is not defined` | Versión antigua de Newman | Ejecutar `npm install` para actualizar dependencias |
| Reporte HTML no generado | Falta `newman-reporter-htmlextra` o comando sin `--html` | Ejecutar `npm install` y usar `test:local` o `test:verbose` |
| Query tests fallan con IDs vacíos | PayIn falló y no persistió las variables | Correr primero `npm run test:payin` y verificar |

