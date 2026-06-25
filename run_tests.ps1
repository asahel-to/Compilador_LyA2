Write-Host "SortScript Tarea 3: compilacion + pruebas semanticas"
Write-Host "Generating sources list..."
Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName } | Out-File -Encoding utf8 sources.txt
if (-not (Test-Path bin)) { New-Item -ItemType Directory -Path bin | Out-Null }
Write-Host "Compiling..."
$srcs = Get-Content sources.txt
& javac -d bin -sourcepath src @srcs
if ($LASTEXITCODE -ne 0) { Write-Error "Compilation failed ($LASTEXITCODE)"; exit $LASTEXITCODE }
Write-Host "Running tests..."
& java -cp bin tests.SemanticTests
exit $LASTEXITCODE
