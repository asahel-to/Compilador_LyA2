# Cambios del código intermedio (TAC)

## Qué se agregó

- Se creó la clase [src/parser/GeneradorCodigoIntermedio.java](src/parser/GeneradorCodigoIntermedio.java) para traducir el AST a instrucciones de tres direcciones.
- Se añadió una prueba de validación en [src/tests/PruebasCodigoIntermedio.java](src/tests/PruebasCodigoIntermedio.java).
- Se incorporó una pestaña nueva en la interfaz llamada "TAC" dentro de [src/ui/CompiladorUI.java](src/ui/CompiladorUI.java), para mostrar el código intermedio de forma visual.

## Qué genera ahora

El generador produce TAC para:

- `scan ... as ...` → `variable = call scan, ruta`
- `let` → asignación directa
- `log(...)` → `call log, ...`
- `done()` → `call done`
- `if` → salto condicional con etiquetas
- `for each` → inicialización, condición y avance del iterador
- condiciones con `==`, `||` y acceso a atributos como `item.ext`

## Cómo funciona internamente

El proceso sigue este orden:

1. El parser construye el árbol sintáctico (AST) a partir del código fuente.
2. El analizador semántico valida el programa.
3. El generador recorre el AST y emite instrucciones TAC.

La clase [src/parser/GeneradorCodigoIntermedio.java](src/parser/GeneradorCodigoIntermedio.java) trabaja con una estrategia muy simple:

- Usa un `StringBuilder` para acumular las instrucciones.
- Usa un contador para generar temporales como `t1`, `t2`, `t3`.
- Usa otro contador para crear etiquetas como `L1`, `L2`.
- Recorre cada nodo del árbol y emite una traducción según el tipo de instrucción.

## Ejemplo de traducción

### Asignación simple

Código fuente:

```sortscript
let docs = "/docs"
```

TAC generado:

```text
docs = "/docs"
```

### Llamada a función

Código fuente:

```sortscript
log("mensaje")
```

TAC generado:

```text
call log, "mensaje"
```

### Condición `if`

Código fuente:

```sortscript
if 1 == 1 {
    log("ok")
}
```

TAC generado:

```text
t1 = 1 == 1
if_false t1 goto L1
call log, "ok"
L1:
```

### Iteración `for each`

Código fuente:

```sortscript
for each item in files {
    move(item, "/dest")
}
```

TAC generado:

```text
t1 = call inicializar_iterador, files
L1:
t2 = call tiene_siguiente, t1
if_false t2 goto L2
item = call siguiente, t1
call move, item, "/dest"
goto L1
L2:
```

### Condición lógica con atributos

Código fuente:

```sortscript
for each item in files {
    if item.ext == ".pdf" || item.ext == ".docx" {
        log("ok")
    }
}
```

TAC generado:

```text
t3 = item.ext
t4 = ".pdf"
t5 = t3 == t4
t6 = item.ext
t7 = ".docx"
t8 = t6 == t7
t9 = t5 or t8
if_false t9 goto L3
call log, "ok"
L3:
```

## Cómo verlo en la interfaz

Después de analizar un programa desde la interfaz, se abre la pestaña "TAC" en la parte lateral y se muestra el resultado del generador de forma visual.

## Nota sobre `else`

El parser ya tiene una ruta para `else`, pero la parte de generación de TAC se validó en esta etapa con ejemplos de `if` sin `else`. El soporte para `else` puede ampliarse después si se desea una traducción más completa.

## Verificación

La implementación fue verificada ejecutando:

```bash
java -cp bin tests.PruebasCodigoIntermedio
```

Resultado:

- Compilación exitosa
- Prueba ejecutada correctamente con `Intermediate code test passed`
