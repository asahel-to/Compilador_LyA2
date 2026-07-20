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

El parser usa un enfoque de **dos pasadas**:

#### Primera pasada: parseo sin reemplazo
- `parseLineaRaw(String texto)` analiza cada línea TAC y devuelve un `InstruccionTAC` con los nombres originales de las variables y temporales (por ejemplo, `t1`, `t2`, `a`, `b`).
- Se construyen dos mapas de referencia:
  - `referenciaNumerica`: `"t1"` → `"1"` (índice numérico de la instrucción)
  - `referenciaTemporal`: `"t1"` → `"t1"` (formato `t<n>` para cuádruplos)

#### Segunda pasada: generación de tripletas y cuádruplos
- Se itera sobre la lista de instrucciones parseadas y se generan **tripletas y cuádruplos de forma independiente**:
  - **Tripletas**: los operandos que son temporales (como `t1`) se reemplazan por su **índice numérico** de instrucción (por ejemplo, `"1"`).
  - **Cuádruplos**: los operandos que son temporales se reemplazan por su nombre temporal con formato `t<n>` (por ejemplo, `"t1"`).

### Flujo del parseo

```
Línea TAC original:     t2 = t1 * c
                            ↓ parseLineaRaw
InstruccionTAC:         op="*", arg1="t1", arg2="c", resultado="t2"
                            ↓ segunda pasada
Tripleta:               índice=2, op="*", arg1="1", arg2="c"
Cuádruplo:              op="*", arg1="t1", arg2="c", resultado="t2"
```

### Correciones importantes implementadas

1. `esValorSimple(...)` detecta primero literales completos:
   - cadenas entre comillas
   - identificadores válidos
   - números con sufijos como `KB`, `MB`, `GB`

   Esto evita que los literales como `"C:/Users/Ana/Documentos"` se dividan por `/` o `:`.

2. `if_false t9 goto Label3` se parsea con una expresión regular completa:
   - `^if_false\s+(\S+)\s+goto\s+(\S+)$`

   De esta forma no se trata `goto` como una instrucción separada.

3. Las tripletas usan el mismo orden de columnas:
   - `Índice`, `Op`, `Arg1`, `Arg2`

4. Los cuádruplos usan el formato:
   - `Op`, `Arg1`, `Arg2`, `Resultado`

5. Los operandos temporales se representan de forma diferente según la vista:
   - En **tripletas**: se usan **índices numéricos** para referenciar resultados anteriores (por ejemplo, `"1"` en vez de `"t1"`).
   - En **cuádruplos**: se usan **nombres temporales** con formato `t<n>` (por ejemplo, `"t1"`, `"t2"`).

## 3. Tripletas vs Cuádruplos

### Tripletas

En esta interfaz se muestra cada instrucción con columnas:
- `Índice`
- `Op`
- `Arg1`
- `Arg2`

Los operandos que son resultados de instrucciones anteriores se muestran como su **índice numérico**.

Ejemplo con:
```
t1 = a + b
t2 = t1 * c
x = t2
```

| Índice | Op  | Arg1 | Arg2 |
|--------|-----|------|------|
| 1      | +   | a    | b    |
| 2      | *   | 1    | c    |
| 3      | =   | 2    |      |

### Cuádruplos

Aquí se representa la misma instrucción con columnas:
- `Op`
- `Arg1`
- `Arg2`
- `Resultado`

Los operandos que son resultados de instrucciones anteriores se muestran como **temporales** (`t<n>`).

Ejemplo con las mismas instrucciones:

| Op  | Arg1 | Arg2 | Resultado |
|-----|------|------|-----------|
| +   | a    | b    | t1        |
| *   | t1   | c    | t2        |
| =   | t2   |      | x         |

## 4. Componentes de UI

### `src/ui/CompiladorUI.java`

- Genera TAC en la pestaña `TAC` tras el análisis.
- Envía ese texto a los botones `Tripletas` y `Cuádruplos`.
- Las ventanas usan directamente el contenido actual de `txtCodigoIntermedio`.

### `src/ui/VentanaTripletasLyA2.java`

- Muestra una tabla de tripletas con colores oscuros forzados (renderer explícito).
- Carga TAC inicial desde `tacInicial`.
- Invoca `ParserTAC.parsear(...)` una sola vez.

### `src/ui/VentanaCuadruplosLyA2.java`

- Muestra una tabla de cuadruplos con colores oscuros forzados (renderer explícito).
- También invoca `ParserTAC.parsear(...)` una sola vez.

## 5. Recomendación de uso

1. Ejecutar el análisis en la UI principal.
2. Ir a la pestaña `TAC`.
3. Abrir `Tripletas` o `Cuádruplos`.
4. Si es necesario, usar `Cargar desde TAC` para refrescar.

## 6. Beneficios de esta implementación

- El parser es único y determinista con enfoque de dos pasadas.
- Tripletas y cuádruplos generan sus referencias de forma independiente.
- Las tripletas usan índices numéricos para referenciar resultados anteriores (más compacto).
- Los cuádruplos usan nombres temporales `t<n>` para mayor legibilidad.
- Mantiene los literales de cadena intactos.
- Hace que las instrucciones `call` sin retorno sean consistentes.
- Simplifica la depuración y el mantenimiento.
