# SortScript Compilador

Compilador para el lenguaje de programación **SortScript**, un DSL para la organización y manipulación automatizada de archivos.

## Estructura del Proyecto

```
src/
  lexico/   # Análisis léxico (AFD, Token, TipoToken, SortScriptAFD, Tokenizador)
  parser/   # Análisis sintáctico y semántico (ParserLL1, NodoArbol, SemanticAnalyzer, SymbolTable)
  ui/       # Interfaz gráfica (CompiladorUI, PanelArbol, VentanaArbol, TextLineNumber)
```

## Requisitos

- Java 11 o superior (el código usa API introducidas en Java 9 como `Set.of`).
- `javac` y `java` disponibles en PATH.
- FlatLaf es opcional para tema visual avanzado.

## Compilar y ejecutar

Para compilar toda la aplicación y abrir la interfaz:

```bash
javac -d bin -sourcepath src src/lexico/*.java src/parser/*.java src/ui/*.java
java -cp bin ui.CompiladorUI
```

## Validación automática

La forma recomendada de verificar la implementación es ejecutar la suite de pruebas semánticas:

- `run_tests.bat` para Windows CMD.
- `run_tests.ps1` para PowerShell.

Ambos scripts compilan el proyecto y ejecutan `tests.SemanticTests`.

## Estado del proyecto — 9 tareas completadas (32 tests)

| Tarea | Descripción | Estado |
|-------|-------------|--------|
| 1 | Gramática definitiva del lenguaje | ✅ |
| 2 | Separación de análisis léxico / sintáctico / semántico | ✅ |
| 3 | Tabla de símbolos (let, for each, alcance) | ✅ |
| 4 | Validación de funciones integradas (arity, tipos) | ✅ |
| 5 | Modo pánico y recuperación de errores | ✅ |
| 6 | Manejo de comentarios y literales (strings, URLs) | ✅ |
| 7 | Estructura del árbol sintáctico (sin nodos wrapper) | ✅ |
| 8 | Interfaz gráfica mejorada (barra de estado, errores coloreados) | ✅ |
| 9 | Ajuste de funciones integradas (código muerto eliminado) | ✅ |

El compilador soporta: `scan`, `for each`, `if`/`else`, `let`, 5 funciones de archivo (`move`, `copy`, `rename`, `delete`, `log`), operadores de comparación y lógicos, atributos de archivo (`.ext`, `.size`, `.name`, `.date`), concatenación de cadenas, comentarios de línea y bloque, y recuperación ante errores sintácticos.

Ver el plan detallado en [`PLAN_IMPLEMENTACION_SORTSCRIPT.md`](PLAN_IMPLEMENTACION_SORTSCRIPT.md) y el tutorial completo en [`Tutorial SortScript.md`](Tutorial%20SortScript.md).
