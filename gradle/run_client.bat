@echo off
title Indwave - Client
echo ========================================
echo Starting Indwave Client...
echo ========================================
call gradlew.bat runClient
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ CLIENT CRASHED or FAILED TO START.
) else (
    echo.
    echo Client stopped.
)
echo.
pause
