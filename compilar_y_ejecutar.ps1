# SortScript - Compilador Launcher
# PowerShell script - Right-click "Run with PowerShell" or run from terminal

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$SRC = Join-Path $ROOT "src"
$BIN = Join-Path $ROOT "bin"
$JAVA_HOME = "C:\Program Files\Java"
$env:JAVA_HOME = $JAVA_HOME
$env:Path = "$JAVA_HOME\bin;$env:Path"

Write-Host "=================================" -ForegroundColor DarkYellow
Write-Host " SortScript - Compilador" -ForegroundColor Yellow
Write-Host "=================================" -ForegroundColor DarkYellow
Write-Host ""

if (-not (Test-Path $BIN)) {
    New-Item -ItemType Directory -Path $BIN -Force | Out-Null
}

Write-Host "[1/2] Compilando fuentes..." -ForegroundColor Cyan
$srcDirs = @("lexico", "parser", "ui")
$sources = $srcDirs | ForEach-Object { Join-Path $SRC $_ "*.java" }
javac -d $BIN -sourcepath $SRC $sources
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilacion fallida. Revise los errores." -ForegroundColor Red
    pause
    exit 1
}
Write-Host "Compilacion exitosa." -ForegroundColor Green
Write-Host ""

Write-Host "[2/2] Iniciando interfaz grafica..." -ForegroundColor Cyan
Start-Process -FilePath "java.exe" -ArgumentList "-cp", "`"$BIN`"", "ui.CompiladorUI"
Write-Host "Interfaz lanzada en ventana separada." -ForegroundColor Green
Write-Host ""

pause
