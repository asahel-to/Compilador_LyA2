@echo off
title SortScript Compiler
setlocal enabledelayedexpansion

set JAVA_HOME=C:\Program Files\Javaa
set PATH=%JAVA_HOME%\bin;%PATH%
set SRC_DIR=src
set BIN_DIR=bin
set ROOT_DIR=%~dp0

if not exist "%ROOT_DIR%\%BIN_DIR%" mkdir "%ROOT_DIR%\%BIN_DIR%"

echo ====================================
echo  SortScript - Compilador
echo ====================================
echo.

echo [1/2] Compilando fuentes...
set SOURCES=
for /R "%ROOT_DIR%\%SRC_DIR%" %%f in (*.java) do call set SOURCES=%%SOURCES%% "%%f"
javac -d "%ROOT_DIR%\%BIN_DIR%" -sourcepath "%ROOT_DIR%\%SRC_DIR%" %SOURCES%

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] La compilacion fallo. Revise los errores arriba.
    pause
    exit /b 1
)

echo Compilacion exitosa.
echo.

echo [2/2] Iniciando interfaz grafica...
start "" java -cp "%ROOT_DIR%\%BIN_DIR%" ui.CompiladorUI

echo Interfaz lanzada en ventana separada.
echo.
pause
