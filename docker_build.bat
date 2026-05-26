@echo off
title TumiPay - Docker Build Java Service

echo =====================================
echo     TumiPay Docker Build Utility
echo =====================================
echo.

REM Obtener nombre del directorio actual (nombre del microservicio)
for %%i in (.) do set SERVICE_NAME=%%~ni

REM Definir version (puedes cambiarla si necesitas versionado dinámico)
set VERSION=1.0.0

echo Building service: %SERVICE_NAME%
echo Version: %VERSION%
echo.

REM Build Docker image usando el Dockerfile del proyecto actual
docker build -t %SERVICE_NAME%:%VERSION% .
REM docker build --no-cache --progress=plain -t %SERVICE_NAME%:%VERSION% .

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Docker build failed
    exit /b %ERRORLEVEL%
)

echo.
echo =====================================
echo Build completed successfully
echo Image: %SERVICE_NAME%:%VERSION%
echo =====================================

pause