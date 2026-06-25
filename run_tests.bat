@echo off
setlocal
echo SortScript Tarea 3: compilacion + pruebas semanticas
echo Generating sources list (quoted paths)...
powershell -NoProfile -Command "Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { '"' + $_.FullName + '"' } | Out-File -Encoding utf8 sources.txt"
if not exist bin mkdir bin
echo Compiling (using PowerShell to handle spaces)...
powershell -NoProfile -Command " $files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }; & javac -d bin -sourcepath src $files "
if %errorlevel% neq 0 (
  echo Compilation failed.
  exit /b %errorlevel%
)
echo Running tests...
java -cp bin tests.SemanticTests
exit /b %errorlevel%
