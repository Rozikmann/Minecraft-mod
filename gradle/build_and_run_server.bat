@echo off
title Indwave - Build & Run Server
echo ========================================
echo 1. Building Indwave Mod...
echo ========================================
call gradlew.bat build
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ BUILD FAILED! Server will not start.
    echo Check the errors above.
    pause
    exit /b
)

echo.
echo ========================================
echo ✅ BUILD SUCCESSFUL! Starting Server...
echo ========================================
call gradlew.bat runServer

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ SERVER CRASHED or STOPPED WITH ERROR.
) else (
    echo.
    echo Server stopped normally.
)
echo.
pause