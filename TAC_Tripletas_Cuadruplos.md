# Código Intermedio (TAC), Tripletas y Cuádruplos

## 1. Qué es el código intermedio (TAC)

TAC significa "Three-Address Code" (código de tres direcciones). Es una representación intermedia que facilita el análisis y la optimización del compilador antes de generar código final.

Cada instrucción TAC se representa como una operación con hasta tres operandos:
- `resultado = arg1 op arg2`
- `if_false arg1 goto Label`
- `goto Label`
- `call func, args`
- `Label:`

En este proyecto, el compilador genera TAC desde el AST de SortScript y luego permite visualizarlo como:
- tripletas
- cuádruplos

## 2. Arquitectura del parser TAC

### `src/parser/ParserTAC.java`

El parser centraliza el análisis de cada línea TAC en una sola función:
- `parseLinea(String texto, int numeroInstruccion)`

Esa función devuelve un objeto interno:
- `InstruccionTAC { indice, op, arg1, arg2, resultado }`

A partir de esa lista única de instrucciones, se construyen las dos vistas:
- `TripletaTAC`: usada para las tripletas
- `CuadruploTAC`: usada para los cuadruplos

### Correciones importantes implementadas

1. `esValorSimple(...)` detecta primero literales completos:
   - cadenas entre comillas
   - identificadores válidos
   - números con sufijos como `KB`, `MB`, `GB`

   Esto evita que los literales como `"C:/Users/Ana/Documentos"` se dividan por `/` o `:`.

2. `if_false t9 goto Label3` se parsea con una expresión regular completa:
   - `^if_false\s+(\S+)\s+goto\s+(\S+)$`

   De esta forma no se trata `goto` como una instrucción separada.

3. Las tripletas usan ahora el mismo orden de columnas que los cuadruplos:
   - `Índice`, `Op`, `Arg1`, `Arg2`

   Esto unifica la forma en que se representa cualquier instrucción, incluyendo asignaciones simples.

4. El parser devuelve una sola lista de instrucciones estructuradas. Las tablas de tripletas y cuadruplos consumen esa lista en lugar de parsear de forma independiente.

## 3. Tripletas vs Cuádruplos

### Tripletas

En esta interfaz se muestra cada instrucción con columnas:
- `Índice`
- `Op`
- `Arg1`
- `Arg2`

Ejemplo para `tamLimite = 50MB`:

- Índice: `1`
- Op: `=` 
- Arg1: `50MB`
- Arg2: ``

### Cuádruplos

Aquí se representa la misma instrucción con columnas:
- `Op`
- `Arg1`
- `Arg2`
- `Resultado`

Ejemplo para `tamLimite = 50MB`:

- Op: `=` 
- Arg1: `50MB`
- Arg2: ``
- Resultado: `tamLimite`

## 4. Componentes de UI

### `src/ui/CompiladorUI.java`

- Genera TAC en la pestaña `TAC` tras el análisis.
- Envía ese texto a los botones `Tripletas` y `Cuádruplos`.
- Las ventanas usan directamente el contenido actual de `txtCodigoIntermedio`.

### `src/ui/VentanaTripletasLyA2.java`

- Muestra una tabla de tripletas.
- Carga TAC inicial desde `tacInicial`.
- Invoca `ParserTAC.parsear(...)` una sola vez.

### `src/ui/VentanaCuadruplosLyA2.java`

- Muestra una tabla de cuadruplos.
- También invoca `ParserTAC.parsear(...)` una sola vez.

## 5. Recomendación de uso

1. Ejecutar el análisis en la UI principal.
2. Ir a la pestaña `TAC`.
3. Abrir `Tripletas` o `Cuádruplos`.
4. Si es necesario, usar `Cargar desde TAC` para refrescar.

## 6. Beneficios de esta implementación

- El parser es único y determinista.
- Evita discrepancias entre tripletas y cuadruplos.
- Mantiene los literales de cadena intactos.
- Hace que las instrucciones `call` sin retorno sean consistentes.
- Simplifica la depuración y el mantenimiento.
