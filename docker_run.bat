@echo off
title TumiPay - Docker Run Java Service

echo =====================================
echo        TumiPay Docker Runner
echo =====================================
echo.

REM =====================================
REM Service Information
REM =====================================

for %%i in (.) do set SERVICE_NAME=%%~ni
set VERSION=1.0.0

REM =====================================
REM Environment File
REM =====================================

set ENV_FILE=.env

REM Validar que exista el archivo .env
IF NOT EXIST %ENV_FILE% (
    echo.
    echo ERROR: Environment file not found: %ENV_FILE%
    echo.
    pause
    exit /b 1
)

echo Running service: %SERVICE_NAME%
echo Image: %SERVICE_NAME%:%VERSION%
echo Environment File: %ENV_FILE%
echo.

REM =====================================
REM Docker Run
REM =====================================

docker run --rm ^
    --name %SERVICE_NAME% ^
    --env-file %ENV_FILE% ^
    -p 8000:8000 ^
    %SERVICE_NAME%:%VERSION%

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Docker run failed
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo =====================================
echo Service stopped
echo =====================================

pause