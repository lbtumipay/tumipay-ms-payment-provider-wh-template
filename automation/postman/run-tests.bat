@echo off
setlocal

set "SCRIPT_DIR=%~dp0"

node "%SCRIPT_DIR%scripts\run-tests.js" %*

REM Capture exit code from Node process
set EXIT_CODE=%ERRORLEVEL%

REM Propagate exit code to the caller / CI pipeline:
REM   0 = all tests passed  (pipeline step: SUCCESS)
REM   1 = one or more tests failed (pipeline step: FAILURE)
exit /b %EXIT_CODE%
