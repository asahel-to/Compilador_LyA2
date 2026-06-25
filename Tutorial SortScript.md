# Tutorial Completo de SortScript
## Lenguaje de Programación para Organización de Archivos

> **Versión:** 1.0
> **Compilador:** SortScript Compiler (Análisis Léxico + Sintáctico + Semántico)
> **Tokens:** 34 | **Producciones:** 18

---

## Índice

1. [¿Qué es SortScript?](#1-qué-es-sortscript)
2. [Estructura de un Programa](#2-estructura-de-un-programa)
3. [Comentarios](#3-comentarios)
4. [scan — Escanear Directorios](#4-scan--escanear-directorios)
5. [let — Variables](#5-let--variables)
6. [for each in — Bucles](#6-for-each-in--bucles)
7. [Atributos de Archivo](#7-atributos-de-archivo)
8. [if / else — Condicionales](#8-if--else--condicionales)
9. [Operadores](#9-operadores)
10. [Funciones Integradas](#10-funciones-integradas)
11. [Semántica y validación](#11-semántica-y-validación)
12. [Concatenación con +](#12-concatenación-con-)
13. [done() — Fin del Programa](#13-done--fin-del-programa)
14. [Programa Completo Paso a Paso](#14-programa-completo-paso-a-paso)
15. [Errores Comunes](#15-errores-comunes)
16. [Ejemplos Adicionales](#16-ejemplos-adicionales)
17. [Bug Conocido y Solución: Tokenización de Extensiones](#17-bug-conocido-y-solución-tokenización-de-extensiones)
18. [Ejercicios para Practicar](#18-ejercicios-para-practicar)
19. [Referencia Rápida](#19-referencia-rápida)

---

## 1. ¿Qué es SortScript?

SortScript es un **lenguaje de programación de propósito específico (DSL)** diseñado para automatizar la organización de archivos en un sistema de cómputo. Su sintaxis está inspirada en lenguajes como Python (legibilidad) y C (estructura de bloques), pero adaptada exclusivamente para trabajar con operaciones de archivos.

**¿Qué puedes hacer con SortScript?**
- Escanear carpetas completas
- Mover, copiar, eliminar y renombrar archivos
- Clasificar archivos por extensión, tamaño, nombre o fecha
- Mostrar mensajes informativos
- Automatizar tareas repetitivas de limpieza y organización

---

## 2. Estructura de un Programa

Todo programa SortScript tiene esta estructura obligatoria:

```
scan "ruta/de/carpeta" as nombreVariable

[ instrucciones ... ]

done()
```

### Regla #1: `done()` es obligatorio
El compilador espera encontrar `done()` al final. Si falta, se produce un **error sintáctico**.

### Regla #2: No hay punto y coma
A diferencia de C, Java o JavaScript, SortScript no usa `;`. Las sentencias terminan con un salto de línea.

### Regla #3: Todo programa empieza con `scan`
La primera instrucción real debe ser un `scan` para definir qué carpeta se va a procesar. Esta regla es obligatoria y única al inicio del programa.

### Flujo general

```
         ┌─────────────────────────────┐
         │   scan "ruta" as variable   │  ← 1. Definir carpeta
         └─────────────┬───────────────┘
                       │
         ┌─────────────▼───────────────┐
         │   for each archivo in var { │  ← 2. Iterar archivos (opcional)
         │       if condicion {        │  ← 3. Evaluar condiciones
         │           move/delete()     │  ← 4. Ejecutar acciones
         │       }                     │
         │   }                         │
         └─────────────┬───────────────┘
                       │
         ┌─────────────▼───────────────┐
         │   log("mensaje")            │  ← 5. Mostrar información
         │   done()                    │  ← 6. Finalizar
         └─────────────────────────────┘
```

---

## 3. Comentarios

SortScript soporta dos tipos de comentarios:

### Comentario de una línea (`//`)

```
// Esto es un comentario que ocupa una sola linea
scan "C:/Users/Ana" as carpeta  // los comentarios pueden ir al final
```

Todo lo que está después de `//` hasta el final de la línea es ignorado.

### Comentario de bloque (`/* */`)

```
/* Esto es un comentario
   que puede ocupar
   varias lineas */
scan "C:/Users/Ana" as carpeta
```

### ¿Cómo los maneja el compilador?

El `Tokenizador.java` elimina los comentarios **antes** de tokenizar. Su método `eliminarComentarios()`:
- Detecta `//` y salta hasta el siguiente `\n`
- Detecta `/*` y salta hasta encontrar `*/`
- Si el bloque `/* */` no se cierra, el tokenizador lo maneja como error y salta al final del archivo

---

## 4. `scan` — Escanear Directorios

### Sintaxis
```
scan "RUTA" as IDENTIFICADOR
```

### Ejemplo
```
scan "C:/Users/Ana/Downloads" as descargas
```

### Desglose parte por parte

| Parte | Token | Explicación |
|-------|-------|-------------|
| `scan` | `KW_SCAN` | Palabra reservada. Indica al compilador: "voy a escanear una carpeta" |
| `"C:/Users/Ana/Downloads"` | `LIT_RUTA` | Literal de ruta. Siempre va entre comillas dobles. Usa barras `/` (no backslashes `\`) |
| `as` | `KW_AS` | Palabra reservada. Significa "asignar a" |
| `descargas` | `IDENTIFICADOR` | Nombre que eliges para referirte a la carpeta escaneada |

### En lenguaje natural

> "Escanea la carpeta que está en `C:/Users/Ana/Downloads` y asígnale el nombre `descargas` para poder referirme a ella después."

### Reglas
- La ruta **siempre** va entre comillas dobles
- Después de `scan` debe ir una ruta (no una variable ni otro valor)
- El identificador después de `as` debe ser un nombre válido (letras, dígitos, sin espacios)
- Cada programa debe tener **al menos un** `scan`
- No puedes escanear en variables que ya existen

### ¿Qué hace realmente el compilador?

El **AFD** reconoce en orden:
1. `scan` → estado de aceptación para `KW_SCAN`
2. `"C:/Users/Ana/Downloads"` → estado de aceptación para `LIT_RUTA`
3. `as` → estado de aceptación para `KW_AS`
4. `descargas` → estado de aceptación para `IDENTIFICADOR`

El **Parser LL1** construye el nodo:
```
scanStmt
├── scan (KW_SCAN)
├── "C:/Users/Ana/Downloads" (LIT_RUTA)
├── as (KW_AS)
└── descargas (IDENTIFICADOR)
```

---

## 5. `let` — Variables

### Sintaxis
```
let IDENTIFICADOR = VALOR
```

### Ejemplos
```
let rutaDocumentos = "C:/Users/Ana/Documentos"
let tamañoMaximo = 100MB
let contador = 0
let mensaje = "Procesando archivos..."
let carpetaDestino = rutaDocumentos   // copiar otra variable
```

### Desglose parte por parte

| Parte | Token | Explicación |
|-------|-------|-------------|
| `let` | `KW_LET` | Palabra reservada. "Declara una variable llamada..." |
| `rutaDocumentos` | `IDENTIFICADOR` | Nombre de la variable (debe ser único en el ámbito) |
| `=` | `OP_ASIG` | Operador de asignación |
| `"C:/Users/Ana/Documentos"` | (literal) | Valor que se guarda en la variable |

### Tipos de valores que puedes asignar

| Tipo | Formato | Ejemplos | Token |
|------|---------|----------|-------|
| **Ruta** | `"texto"` con `/` | `"C:/Users/Ana/Docs"` | `LIT_RUTA` |
| **Extensión** | `.` + letras | `.pdf`, `.jpg` | `LIT_EXT` |
| **Tamaño** | número + KB/MB/GB | `100MB`, `1GB`, `500KB` | `LIT_TAMANIO` |
| **Entero** | solo dígitos | `42`, `0`, `100` | `LIT_ENTERO` |
| **Cadena** | `"texto"` | `"Hola mundo"` | `LIT_CADENA` |
| **Identificador** | nombre de variable | `rutaDocumentos` | `IDENTIFICADOR` |

### ¿Para qué sirven las variables?

1. **Reutilizar rutas largas** — escribes la ruta una vez y la usas muchas veces
2. **Dar nombres significativos** — `rutaDocumentos` es más claro que `"C:/Users/Ana/Documentos"`
3. **Centralizar cambios** — si cambia la ruta, solo cambias el `let`
4. **Guardar resultados intermedios** — aunque SortScript no tiene operaciones transformadoras aún

### Ejemplo con y sin variables

**Sin variables (repetitivo):**
```
for each f in carpeta {
    if f.ext == .pdf {
        move(f, "C:/Users/Ana/Documentos")
    }
    if f.ext == .jpg {
        move(f, "C:/Users/Ana/Imagenes")
    }
}
```

**Con variables (más limpio):**
```
let docs = "C:/Users/Ana/Documentos"
let img = "C:/Users/Ana/Imagenes"

for each f in carpeta {
    if f.ext == .pdf { move(f, docs) }
    if f.ext == .jpg { move(f, img) }
}
```

---

## 6. `for each in` — Bucles

### Sintaxis
```
for each IDENTIFICADOR in IDENTIFICADOR {
    [ instrucciones ... ]
}
```

### Ejemplo
```
for each archivo in descargas {
    move(archivo, "C:/Destino")
}
```

### Desglose parte por parte

| Parte | Token | Explicación |
|-------|-------|-------------|
| `for` | `KW_FOR` | Palabra reservada. Inicia el bucle |
| `each` | `KW_EACH` | Palabra reservada. "Cada uno de..." |
| `archivo` | `IDENTIFICADOR` | Variable temporal que representa "el archivo actual en cada vuelta" |
| `in` | `KW_IN` | Palabra reservada. "Dentro de..." |
| `descargas` | `IDENTIFICADOR` | Variable que contiene la carpeta (la del `scan`) |
| `{ }` | `LLAVE_ABRE` / `LLAVE_CIERRA` | Delimitan el bloque de código que se repite |

### En lenguaje natural

> "Para cada archivo (al que llamaré `archivo`) que está dentro de la carpeta `descargas`, ejecuta las instrucciones entre las llaves."

### ¿Cómo funciona conceptualmente?

```
Carpeta "descargas" contiene:
  [0] reporte.pdf
  [1] foto.jpg
  [2] cancion.mp3

Vuelta 1: archivo = "reporte.pdf"  → ejecuta bloque
Vuelta 2: archivo = "foto.jpg"     → ejecuta bloque
Vuelta 3: archivo = "cancion.mp3"  → ejecuta bloque
Fin
```

### Reglas importantes

1. **Las llaves `{ }` son obligatorias** — incluso si el bloque tiene una sola línea
2. **La variable del `for each`** (`archivo` en el ejemplo) solo existe dentro del bloque
3. **La variable del `in`** (`descargas`) debe haber sido declarada antes con `scan`
4. **No hay `break` ni `continue`** — el bucle siempre recorre todos los elementos

### Árbol sintáctico que construye el parser

```
forStmt
├── for (KW_FOR)
├── each (KW_EACH)
├── archivo (IDENTIFICADOR)
├── in (KW_IN)
├── descargas (IDENTIFICADOR)
├── { (LLAVE_ABRE)
├── bloque
│   ├── (move...)
│   └── ...
└── } (LLAVE_CIERRA)
```

---

## 7. Atributos de Archivo

Una vez que tienes un archivo dentro del `for each`, puedes acceder a sus propiedades usando la sintaxis de **punto**:

```
variableDelArchivo . nombreDelAtributo
```

### Los 4 atributos disponibles

| Código | Token | Significado | Ejemplo de valor |
|--------|-------|-------------|------------------|
| `archivo.ext` | `ATTR_EXT` | Extensión del archivo | `.pdf` |
| `archivo.size` | `ATTR_SIZE` | Tamaño del archivo | `100MB` |
| `archivo.name` | `ATTR_NAME` | Nombre completo del archivo | `"reporte.pdf"` |
| `archivo.date` | `ATTR_DATE` | Fecha de modificación | — |

### Ejemplos de uso

```
// Clasificar por extension
if archivo.ext == .pdf { ... }

// Filtrar por tamaño
if archivo.size > 50MB { ... }

// Mostrar nombre
log("Procesando: " + archivo.name)

// Combinar condiciones
if archivo.size > 100MB && archivo.ext == .mp4 { ... }
```

### ¿Cómo se representa esto en el árbol sintáctico?

Cuando escribes `archivo.ext`, el parser construye:
```
termino
├── archivo (IDENTIFICADOR)
└── accesoAtributo
    └── . (OP_PUNTO)
    └── ext (ATTR_EXT)
```

### Reglas
- El punto `.` es **obligatorio** entre el identificador y el atributo
- Los únicos atributos válidos son: `ext`, `size`, `name`, `date`
- No se puede usar un atributo sin un identificador antes (ej. `if ext == .pdf` no funciona)
- Los atributos son de solo lectura — no puedes asignarles valor

---

## 8. `if` / `else` — Condicionales

### Sintaxis
```
if CONDICION {
    [ instrucciones si se cumple ]
}

if CONDICION {
    [ instrucciones si se cumple ]
} else {
    [ instrucciones si NO se cumple ]
}
```

### Ejemplos

**If simple:**
```
if archivo.ext == .pdf {
    move(archivo, "C:/Documentos")
}
```

**If con operador lógico:**
```
if archivo.ext == .jpg || archivo.ext == .png {
    copy(archivo, "C:/Imagenes")
}
```

**If-else:**
```
if archivo.size > 100MB {
    log("Archivo grande: " + archivo.name)
} else {
    move(archivo, "C:/Normal")
}
```

**If con operador AND y NOT:**
```
if archivo.size > 10MB && archivo.ext != .zip {
    log("Archivo grande no comprimido")
}
```

### Desglose del `if`

| Parte | Token | Explicación |
|-------|-------|-------------|
| `if` | `KW_IF` | Palabra reservada. "Si se cumple que..." |
| `archivo.ext == .pdf` | (condición) | Expresión que se evalúa como verdadera o falsa |
| `{ ... }` | `LLAVE_ABRE` / `LLAVE_CIERRA` | Bloque que se ejecuta si la condición es verdadera |
| `else` | `KW_ELSE` | (Opcional) "En caso contrario..." |
| `{ ... }` | `LLAVE_ABRE` / `LLAVE_CIERRA` | Bloque que se ejecuta si la condición es falsa |

### Estructura de una condición en el árbol sintáctico

```
ifStmt
├── if (KW_IF)
├── condicion
│   ├── expresion (archivo.ext)
│   ├── opComparacion (==)
│   └── expresion (.pdf)
├── { (LLAVE_ABRE)
├── bloque
│   └── funcCall (move...)
├── } (LLAVE_CIERRA)
└── elseOpt
    ├── else (KW_ELSE)
    ├── { (LLAVE_ABRE)
    ├── bloque
    └── } (LLAVE_CIERRA)
```

### Reglas
- Las llaves `{ }` son **obligatorias** en el `if` y en el `else`
- El `else` es opcional
- Las condiciones usan **operadores de comparación** (`==`, `!=`, `>`, `<`, `>=`, `<=`)
- Se pueden combinar condiciones con `&&` (AND) y `||` (OR)
- No se pueden anidar paréntesis en condiciones (el parser actual soporta una condición a la vez)

---

## 9. Operadores

### Operadores de Comparación

| Operador | Significado | Ejemplo | ¿Verdadero cuando? |
|----------|-------------|---------|-------------------|
| `==` | Igual a | `ext == .pdf` | La extensión es `.pdf` |
| `!=` | Diferente de | `ext != .exe` | La extensión NO es `.exe` |
| `>` | Mayor que | `size > 100MB` | El tamaño es mayor a 100MB |
| `<` | Menor que | `size < 10MB` | El tamaño es menor a 10MB |
| `>=` | Mayor o igual | `size >= 50MB` | El tamaño es 50MB o más |
| `<=` | Menor o igual | `size <= 1GB` | El tamaño es 1GB o menos |

### Operadores Lógicos

| Operador | Significado | Ejemplo | Verdadero cuando... |
|----------|-------------|---------|---------------------|
| `&&` | AND (y) | `ext == .jpg && size > 5MB` | Ambas condiciones se cumplen |
| `||` | OR (o) | `ext == .jpg || ext == .png` | Al menos una se cumple |

### Operador de Asignación

| Operador | Significado | Ejemplo |
|----------|-------------|---------|
| `=` | Asignar valor | `let x = 100MB` |

### Operador de Acceso

| Operador | Significado | Ejemplo |
|----------|-------------|---------|
| `.` | Acceder a atributo | `archivo.ext` |

### Operador de Concatenación

| Operador | Significado | Ejemplo |
|----------|-------------|---------|
| `+` | Concatenar cadenas | `"Hola " + archivo.name` |

### Operador de Negación (no implementado en parser actual)

El token `!` existe en el AFD pero el parser LL1 actual **no** incluye la producción para expresiones con `!`. Esto significa que `!(condicion)` daría error sintáctico.

### Precedencia (de mayor a menor)

No hay precedencia explícita porque el parser LL1 actual solo maneja una condición a la vez. En una expresión como:
```
expresion opComparacion expresion
```

Cada `expresion` puede contener:
- Un identificador (con o sin atributo)
- Un literal (ruta, extensión, tamaño, entero, cadena)
- Una sub-expresión entre paréntesis `( )`
- Concatenación con `+` (asociativa a la izquierda)

---

## 10. Funciones Integradas

SortScript tiene 5 funciones integradas para manipular archivos y mostrar información.

### 10.1 `move(archivo, destino)`

**Sintaxis:** `move(archivo, rutaODestino)`

**Descripción:** Mueve un archivo de su ubicación actual a una nueva carpeta.

**Argumentos:**
| # | Tipo | Ejemplo |
|---|------|---------|
| 1 | Identificador de archivo (del `for each`) | `archivo` |
| 2 | Ruta (literal) o variable con ruta | `"C:/Destino"` o `rutaDocs` |

**Ejemplos:**
```
move(archivo, "C:/Users/Ana/Documentos")
move(archivo, rutaDocumentos)       // usando variable
move(archivo, "C:/" + archivo.name) // concatenando
```

### 10.2 `copy(archivo, destino)`

**Sintaxis:** `copy(archivo, rutaODestino)`

**Descripción:** Copia el archivo a otra ubicación. El original permanece en su lugar.

**Ejemplos:**
```
copy(archivo, "C:/Users/Ana/Backups")
copy(foto, rutaImagenes)
```

### 10.3 `delete(archivo)`

**Sintaxis:** `delete(archivo)`

**Descripción:** Elimina el archivo permanentemente.

**Ejemplos:**
```
delete(archivo)
delete(item)
```

### 10.4 `rename(archivo, nuevoNombre)`

**Sintaxis:** `rename(archivo, nuevoNombre)`

**Descripción:** Cambia el nombre del archivo.

**Ejemplos:**
```
rename(archivo, "documento_final.pdf")
rename(item, "backup_" + item.name)
```

### 10.5 `log(mensaje)`

**Sintaxis:** `log(mensaje)`

**Descripción:** Muestra un mensaje en la consola. No afecta archivos.

**Ejemplos:**
```
log("Iniciando organizacion...")
log("Archivo procesado: " + archivo.name)
log("Total: " + contador)
```

### Reglas comunes para todas las funciones

1. **Los paréntesis `( )` son obligatorios**
2. **Los argumentos se separan con `,`**
3. **Pueden recibir:**
   - Literales (rutas, cadenas, números)
   - Identificadores (variables o archivos del `for each`)
   - Expresiones concatenadas con `+`
4. **Deben ir dentro de un bloque** (`for`, `if`, o al nivel principal después del `scan`)

### Árbol sintáctico de una llamada

```
funcCall
├── move (FUNC_MOVE)
├── ( (PAR_ABRE)
├── argumentos
│   ├── expresion (archivo)
│   │   └── termino → archivo (IDENTIFICADOR)
│   └── restoArgs
│       ├── , (COMA)
│       └── expresion ("C:/Destino")
│           └── termino → "C:/Destino" (LIT_RUTA)
└── ) (PAR_CIERRA)
```

---

## 11. Semántica y validación

SortScript ya no solo se valida por sintaxis: el compilador comprueba también que el programa tenga sentido.

### Qué valida el compilador

- Variables declaradas con `let` y `scan`.
- Uso de identificadores dentro del ámbito correcto.
- Redeclaraciones en el mismo bloque.
- Firmas de funciones integradas (`move`, `copy`, `rename`, `delete`, `log`).
- Aridad correcta y tipos compatibles.
- Acceso a atributos válidos (`.ext`, `.size`, `.name`, `.date`).

### Qué verás en la interfaz

- Pestañas separadas para errores léxicos, sintácticos y semánticos.
- Resaltado de líneas con error semántico dentro del editor.
- Salto automático a la línea del error al hacer clic en la tabla de errores semánticos.

### Cómo interpretar los errores

- Si una variable no existe, la fase semántica lo marca.
- Si una función recibe demasiados o muy pocos argumentos, se reporta error.
- Si una ruta o identificador no coincide con el tipo esperado, también se reporta error.

## 12. Concatenación con `+`

### Sintaxis
```
expresion + expresion
```

### ¿Qué se puede concatenar?

La concatenación en SortScript une dos valores como texto:

```
log("Nombre: " + archivo.name)       // cadena + atributo
log(archivo.name + " - " + archivo.ext) // atributo + cadena + atributo
log("Ruta completa: C:/Docs/" + archivo.name)
```

### ¿Qué NO se puede hacer?

No hay suma aritmética. `100MB + 50MB` no es válido como expresión aritmética. El `+` solo concatena.

### Árbol sintáctico

```
expresion
├── termino ("Nombre: ")
└── restoExpresion
    ├── + (OP_SUMA)
    ├── termino (archivo.name)
    └── restoExpresion
        └── (vacio / ε)
```

---

## 13. `done()` — Fin del Programa

### Sintaxis
```
done()
```

### ¿Por qué es obligatorio?

El parser LL1 está diseñado para esperar `done()` como marcador de fin de programa. Esto permite:
1. **Validar que el programa termina correctamente** — no hay código truncado
2. **Proporcionar un punto de anclaje** para la gramática LL1
3. **Diferenciar el final intencional** de un archivo incompleto

### Desglose

| Parte | Token | Explicación |
|-------|-------|-------------|
| `done` | `KW_DONE` | Palabra reservada. "El programa ha terminado" |
| `(` | `PAR_ABRE` | Paréntesis de apertura |
| `)` | `PAR_CIERRA` | Paréntesis de cierre |

### En la gramática

```
programa → scanStmt { sentencia } done ( ) EOF
```

El parser espera:
1. Un `scan` inicial obligatorio
2. Cero o más sentencias (for, if, let, funciones)
3. La palabra `done`
4. Un paréntesis de apertura `(`
5. Un paréntesis de cierre `)`
6. El fin de archivo `EOF`

Si falta cualquiera de estos, se produce un error sintáctico.

---

## 14. Programa Completo Paso a Paso

### Programa: Organización del Escritorio

```
 1: // SortScript - Organizacion del Escritorio
 2: // =========================================
 3: 
 4: scan "C:/Users/Ana/Desktop" as escritorio
 5: 
 6: let docs = "C:/Users/Ana/Documentos"
 7: let media = "C:/Users/Ana/Multimedia"
 8: let backups = "C:/Users/Ana/Backups"
 9: let tamLimite = 10MB
10: 
11: log("Iniciando limpieza del escritorio...")
12: 
13: for each item in escritorio {
14: 
15:     if item.ext == .pdf || item.ext == .docx || item.ext == .xlsx {
16:         move(item, docs)
17:     }
18: 
19:     if item.ext == .jpg || item.ext == .mp3 || item.ext == .mp4 {
20:         move(item, media)
21:     }
22: 
23:     if item.ext == .zip || item.ext == .rar {
24:         copy(item, backups)
25:     }
26: 
27:     if item.size > tamLimite && item.ext != .zip {
28:         log("Archivo grande en escritorio: " + item.name)
29:     }
30: }
31: 
32: log("Limpieza completada")
33: done()
```

### Explicación línea por línea

#### Líneas 1-2: Comentarios
```
// SortScript - Organizacion del Escritorio
// =========================================
```
El tokenizador las elimina antes de analizar. Son solo para el programador.

#### Línea 4: Escanear el escritorio
```
scan "C:/Users/Ana/Desktop" as escritorio
```
- **`scan`**: keyword que inicia el programa
- **`"C:/Users/Ana/Desktop"`**: literal de ruta — la carpeta a escanear
- **`as`**: keyword que asigna la carpeta a una variable
- **`escritorio`**: identificador — la variable que contendrá la lista de archivos

#### Líneas 6-9: Declarar variables
```
let docs = "C:/Users/Ana/Documentos"
let media = "C:/Users/Ana/Multimedia"
let backups = "C:/Users/Ana/Backups"
let tamLimite = 10MB
```
- **`let`**: keyword para declarar variable
- **`docs`, `media`, `backups`**: variables que guardan rutas de destino
- **`tamLimite`**: variable que guarda el tamaño máximo (10MB)
- Usar variables evita repetir las rutas largas dentro del bucle

#### Línea 11: Mensaje de inicio
```
log("Iniciando limpieza del escritorio...")
```
- Llama a la función `log` con una cadena
- El mensaje aparecerá en la consola cuando el programa se ejecute

#### Línea 13: Inicio del bucle
```
for each item in escritorio {
```
- **`for each`**: inicia la iteración sobre los archivos
- **`item`**: nombre temporal para cada archivo en cada vuelta
- **`in escritorio`**: la variable que contiene la carpeta escaneada
- **`{`**: abre el bloque que se repite para cada archivo

#### Líneas 15-17: Clasificar documentos
```
    if item.ext == .pdf || item.ext == .docx || item.ext == .xlsx {
        move(item, docs)
    }
```
- **Condición**: pregunta si la extensión del archivo actual es `.pdf` **O** `.docx` **O** `.xlsx`
- **`item.ext`**: acceso al atributo `ext` del archivo actual
- **`==`**: operador de igualdad
- **`.pdf`**: literal de extensión
- **`||`**: operador OR — si cualquiera de las tres condiciones es verdadera
- **Acción**: si se cumple, mueve el archivo a la carpeta `docs`
- **`move(item, docs)`**: función move con el archivo y la variable `docs`

#### Líneas 19-21: Clasificar multimedia
```
    if item.ext == .jpg || item.ext == .mp3 || item.ext == .mp4 {
        move(item, media)
    }
```
- Misma estructura que el anterior
- Verifica si la extensión es `.jpg`, `.mp3` o `.mp4`
- Si se cumple, mueve a la carpeta `media`

#### Líneas 23-25: Respaldar comprimidos
```
    if item.ext == .zip || item.ext == .rar {
        copy(item, backups)
    }
```
- Verifica si la extensión es `.zip` o `.rar`
- Usa `copy` en lugar de `move` — el archivo original permanece en el escritorio
- Se copia a la carpeta `backups`

#### Líneas 27-29: Reportar archivos grandes
```
    if item.size > tamLimite && item.ext != .zip {
        log("Archivo grande en escritorio: " + item.name)
    }
```
- **`item.size > tamLimite`**: ¿el tamaño del archivo es mayor a 10MB?
- **`&&`**: operador AND — ambas condiciones deben cumplirse
- **`item.ext != .zip`**: ¿la extensión NO es `.zip`?
- Si las dos se cumplen, muestra un mensaje con el nombre del archivo
- **`"Archivo grande en escritorio: " + item.name`**: concatenación de cadena literal con el atributo `name`

#### Línea 30: Cierre del bucle
```
}
```
- Marca el fin del bloque del `for each`
- Si hay más archivos, vuelve a la línea 13 con el siguiente

#### Línea 32: Mensaje de finalización
```
log("Limpieza completada")
```
- Mensaje informativo después de procesar todos los archivos

#### Línea 33: Fin del programa
```
done()
```
- **`done`**: keyword que marca el final
- **`()`**: paréntesis obligatorios
- Si falta, el parser produce: *"Se espera 'done' al final del programa."*

### Árbol sintáctico completo

```
programa
├── scanStmt
│   ├── scan (KW_SCAN)
│   ├── "C:/Users/Ana/Desktop" (LIT_RUTA)
│   ├── as (KW_AS)
│   └── escritorio (IDENTIFICADOR)
├── letStmt
│   ├── let (KW_LET)
│   ├── docs (IDENTIFICADOR)
│   ├── = (OP_ASIG)
│   └── expresion → "C:/Users/Ana/Documentos" (LIT_RUTA)
├── letStmt
│   ├── let (KW_LET)
│   ├── media (IDENTIFICADOR)
│   ├── = (OP_ASIG)
│   └── expresion → "C:/Users/Ana/Multimedia" (LIT_RUTA)
├── letStmt  ... (backups y tamLimite similares)
├── funcCall (log)
├── forStmt
│   ├── for (KW_FOR)
│   ├── each (KW_EACH)
│   ├── item (IDENTIFICADOR)
│   ├── in (KW_IN)
│   ├── escritorio (IDENTIFICADOR)
│   ├── { (LLAVE_ABRE)
│   ├── bloque
│   │   ├── ifStmt  ... (pdf/docx/xlsx)
│   │   ├── ifStmt  ... (jpg/mp3/mp4)
│   │   ├── ifStmt  ... (zip/rar)
│   │   └── ifStmt  ... (size > 10MB)
│   └── } (LLAVE_CIERRA)
└── funcCall (log)
```

---

## 15. Errores Comunes

### 14.1 Error: Falta `done()` al final

**Código incorrecto:**
```
scan "C:/Users/Ana" as carpeta
for each f in carpeta {
    log("Hola")
}
// Falta done()
```

**Error del compilador:**
```
Error sintactico | Linea 4 | Se espera 'done' al final del programa.
```

### 14.2 Error: Olvidar las llaves `{ }`

**Código incorrecto:**
```
for each f in carpeta
    move(f, "C:/Destino")
```

**Error del compilador:**
```
Error sintactico | Linea 2 | Se espera '{' para abrir el bloque del for.
```

### 14.3 Error: Extensión sin punto

**Código incorrecto:**
```
if archivo.ext == "pdf"
if archivo.ext == pdf
```

**Errores del compilador:**
```
Error en expresion | Linea 1 | Se esperaba un literal ... o '(' en la expresion.
```
(Las extensiones deben tener el punto adelante: `.pdf`)

### 14.4 Error: Ruta sin comillas

**Código incorrecto:**
```
scan C:/Users/Ana as carpeta
```

**Error del compilador:**
```
Error sintactico | Linea 1 | Se espera una ruta después de 'scan'.
```
(Las rutas siempre van entre comillas dobles: `"C:/Users/Ana"`)

### 14.5 Error: Punto y coma al final

**Código incorrecto:**
```
scan "C:/Users/Ana" as carpeta;
move(f, "C:/Destino");
```

**Error del compilador:**
```
Error inicial | Linea 1 | Se espera una accion como scan, for, if, let...
o una funcion en vez de: ;
```

### 14.6 Error: Identificador inválido después de `as`

**Código incorrecto:**
```
scan "C:/Users/Ana" as 123nombre
```

**Error del compilador:**
```
Error en expresion | Linea 1 | Se esperaba un literal, identificador o '(' en la expresion.
```
(Los identificadores no pueden empezar con dígitos)

### 14.7 Error: Olvidar cerrar un bloque

**Código incorrecto:**
```
for each f in carpeta {
    if f.ext == .pdf {
        move(f, "C:/Docs")
    // Falta } del if
// Falta } del for
```

**Error del compilador:**
El parser llegará a `done()` o EOF mientras sigue dentro del `bloque` del `for`, y reportará:
```
Error sintactico | Linea ... | Se espera '}' para cerrar el bloque...
```

### 14.8 Error: `done()` sin paréntesis

**Código incorrecto:**
```
done
```

**Error del compilador:**
```
Error sintactico | Linea X | Se espera '(' después de 'done'.
```

### 14.9 Error: Función sin paréntesis

**Código incorrecto:**
```
move archivo, "C:/Destino"
```

**Error del compilador:**
```
Error inicial | Linea X | Se espera una accion como scan, for, if, let o
una funcion en vez de: archivo
```
(El parser no reconoce `archivo` como instrucción válida al inicio)

### 14.10 Error: Operador de comparación incorrecto

**Código incorrecto:**
```
if archivo.ext = .pdf    // = no es comparación, es asignación
if archivo.ext === .pdf   // === no existe en SortScript
```

**Error del compilador:**
```
Error sintactico | Linea X | Se espera un operador de comparación (==, !=, >, <, >=, <=)
```

### 14.11 Error: Sin `scan` al inicio

**Código incorrecto:**
```
for each f en carpeta { ... }
done()
```

**Error del compilador:**
```
Error inicial | Linea 1 | Se espera una accion como scan, for, if, let
o una funcion en vez de: for
```
Aunque `for` es válido, sin un `scan` previo la variable `carpeta` no existe.

---

## 16. Ejemplos Adicionales

### Ejemplo 1: Clasificador de Descargas

```
scan "C:/Users/Ana/Downloads" as downloads

let imagenes = "C:/Users/Ana/Imagenes"
let documentos = "C:/Users/Ana/Documentos"
let musica = "C:/Users/Ana/Musica"
let videos = "C:/Users/Ana/Videos"
let programas = "C:/Users/Ana/Programas"
let otros = "C:/Users/Ana/Otros"

for each archivo in downloads {
    if archivo.ext == .jpg || archivo.ext == .png || archivo.ext == .gif {
        move(archivo, imagenes)
    }

    if archivo.ext == .pdf || archivo.ext == .docx || archivo.ext == .txt {
        move(archivo, documentos)
    }

    if archivo.ext == .mp3 || archivo.ext == .wav || archivo.ext == .flac {
        move(archivo, musica)
    }

    if archivo.ext == .mp4 || archivo.ext == .avi || archivo.ext == .mkv {
        move(archivo, videos)
    }

    if archivo.ext == .exe || archivo.ext == .msi {
        move(archivo, programas)
    }

    if archivo.ext != .jpg && archivo.ext != .png && archivo.ext != .gif &&
       archivo.ext != .pdf && archivo.ext != .docx && archivo.ext != .txt &&
       archivo.ext != .mp3 && archivo.ext != .wav && archivo.ext != .flac &&
       archivo.ext != .mp4 && archivo.ext != .avi && archivo.ext != .mkv &&
       archivo.ext != .exe && archivo.ext != .msi {
        move(archivo, otros)
    }
}

log("Descargas organizadas exitosamente")
done()
```

### Ejemplo 2: Limpiador de Archivos Temporales

```
scan "C:/Users/Ana/AppData/Local/Temp" as temp

let tamMaximo = 50MB
let contador = 0

log("Limpiando archivos temporales...")

for each archivo in temp {
    if archivo.size > tamMaximo {
        log("Eliminando: " + archivo.name + " (" + archivo.size + ")")
        delete(archivo)
    }
}

log("Limpieza completada")
done()
```

### Ejemplo 3: Copia de Seguridad Selectiva

```
scan "C:/Users/Ana/Documentos" as docs

let backup = "D:/Backups/Documentos"
let maxAuto = 500MB

log("Iniciando backup de documentos...")

for each doc in docs {
    if doc.ext == .pdf || doc.ext == .docx {
        if doc.size < maxAuto {
            copy(doc, backup)
            log("Respaldado: " + doc.name)
        } else {
            log("Archivo muy grande para backup automatico: " + doc.name)
        }
    }
}

log("Backup finalizado")
done()
```

---

## 17. Bug Conocido y Solución: Tokenización de Extensiones

### Problema (Ya Corregido)

El tokenizador de SortScript tenía un bug donde el punto `.` era tratado como delimitador en todos los casos, lo que rompía los literales de extensión como `.pdf`, `.jpg`, `.txt`.

**Causa:** En `Tokenizador.java`, el método `tokenizarLinea()` tenía el punto `.` en la lista de caracteres delimitadores:

```java
if ("+-=<>!.,(){}[]".indexOf(c) != -1) {
```

Esto causaba que `.pdf` se partiera en dos tokens:
- `.` → `OP_PUNTO`
- `pdf` → `IDENTIFICADOR`

Pero el parser esperaba recibir `LIT_EXT` (un solo token) después de `==` en condiciones como `if f.ext == .pdf`. Al recibir `OP_PUNTO`, el parser caía en el caso `default` de `termino()` y producía:

```
Error en expresion | Linea X | Se esperaba un literal, identificador o '(' en la expresion.
```

### Solución Aplicada

Se modificó `Tokenizador.java` (líneas 91-94) para que el `.` solo actúe como delimitador cuando hay contenido previo acumulado (como en `file.ext`), pero cuando aparece al inicio de un token (como en `.pdf`), se acumule para formar el literal completo:

```java
if (c == '.' && actual.length() == 0) {
    actual.append(c);
    continue;
}
```

### Resultado

| Antes (roto) | Después (correcto) |
|---|---|
| `.pdf` → `[OP_PUNTO]` + `[IDENTIFICADOR]` | `.pdf` → `[LIT_EXT]` |
| Error sintáctico en línea del `if` | Sin errores |

### Archivo Modificado

- `src/lexico/Tokenizador.java` — Se añadieron 3 líneas (91-94)

Si descargas una nueva versión del proyecto, el bug ya está corregido. Si tienes una copia local, recompila con `compilar_y_ejecutar.bat`.

---

## 18. Ejercicios para Practicar

### Ejercicio 1: Básico
Escribe un programa SortScript que:
1. Escanee la carpeta `C:/Users/Ana/Música`
2. Mueva todos los archivos `.mp3` a `C:/Users/Ana/Música/MP3`
3. Mueva todos los archivos `.flac` a `C:/Users/Ana/Música/FLAC`
4. Muestre "Procesamiento completo" al final

### Ejercicio 2: Condicionales compuestos
Escribe un programa SortScript que:
1. Escanee `C:/Users/Ana/Descargas`
2. Elimine archivos `.exe` que pesen más de `100MB`
3. Copie archivos `.zip` a `D:/Backups`
4. Muestre en consola el nombre de cada archivo `.pdf` que encuentre

### Ejercicio 3: Variables y organización
Escribe un programa SortScript que:
1. Escanee `C:/Users/Ana/Escritorio`
2. Use variables para definir 3 rutas de destino
3. Clasifique archivos según su extensión en las 3 carpetas
4. Reporte archivos de más de `200MB` sin eliminarlos

### Ejercicio 4: Depuración (encontrar errores)
El siguiente código tiene 5 errores. Encuéntralos:

```
scan C:/Users/Ana/test as prueba

for each item in prueba {
    if item.ext = .txt {
        move(item "C:/Destino")
    }
    if item.tamaño > 100MB {
        log(Archivo grande)
    }
}

log("Fin")
```

Errores:
1. _________________________
2. _________________________
3. _________________________
4. _________________________
5. _________________________

---

## 19. Referencia Rápida

### Palabras Reservadas (9)
```
scan    as      for     each    in
if      else    let     done
```

### Funciones Integradas (5)
```
move(archivo, destino)     → Mueve un archivo
copy(archivo, destino)     → Copia un archivo
delete(archivo)            → Elimina un archivo
rename(archivo, nombre)    → Renombra un archivo
log(mensaje)               → Muestra mensaje en consola
```

### Atributos de Archivo (4)
```
archivo.ext     → Extension (.pdf, .jpg)
archivo.size    → Tamano (100MB, 1GB)
archivo.name    → Nombre del archivo
archivo.date    → Fecha de modificacion
```

### Operadores
```
Comparacion:  ==  !=  >  <  >=  <=
Logicos:      &&  ||
Asignacion:   =
Acceso:       .
Concatenacion: +
```

### Literales
```
Ruta:       "C:/Users/Ana/Carpeta"
Extension:  .pdf  .jpg  .txt
Tamano:     100MB  1GB  512KB
Entero:     42  0  100
Cadena:     "Hola mundo"
```

### Gramática LL1 (17 producciones)
```
programa       → scanStmt { sentencia } done ( ) EOF
sentencia      → forStmt | ifStmt | letStmt | funcCall
scanStmt       → scan LIT_RUTA as IDENTIFICADOR
forStmt        → for each IDENTIFICADOR in IDENTIFICADOR { { sentencia } }
ifStmt         → if condicion { { sentencia } } elseOpt
elseOpt        → else { { sentencia } } | ε
letStmt        → let IDENTIFICADOR = expresion
funcCall       → FUNC ( argumentos )
argumentos     → expresion restoArgs | ε
restoArgs      → , expresion restoArgs | ε
condicion      → expresion opComparacion expresion
expresion      → termino restoExpresion
restoExpresion → + termino restoExpresion | ε
termino        → IDENTIFICADOR accesoAtributo | literal | ( expresion )
accesoAtributo → . ATTR | ε
literal        → LIT_RUTA | LIT_EXT | LIT_TAMANIO | LIT_ENTERO | LIT_CADENA
opComparacion  → == | != | > | < | >= | <=
```

### Reglas de oro
1. Todo programa debe comenzar con `scan`
2. Todo programa termina con `done()`
3. Las llaves `{ }` son obligatorias en `for` e `if`
4. Sin punto y coma (`;`) — nunca
5. Las extensiones llevan punto adelante: `.pdf`
6. Las rutas y cadenas van entre comillas dobles: `"ruta"`
7. Los argumentos de funciones van entre `( )` separados por `,`
8. El `+` solo concatena (no suma)
9. Comentarios con `//` y `/* */`

---

> **Documento generado para el proyecto SortScript Compiler**
> Compilador de Lenguajes y Autómatas
