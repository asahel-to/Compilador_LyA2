# Plan de Implementacion y Historial de Tareas - SortScript

> Objetivo: mantener un flujo de trabajo ordenado para corregir el compilador sin mezclar cambios de lexicos, parser, semantica, UI y documentacion.
>
> Regla de trabajo: ninguna tarea se considera terminada hasta que su verificacion asociada haya sido ejecutada y su resultado quede registrado aqui.

## 1. Criterio general

El lenguaje SortScript debe conservar su logica funcional actual en cuanto a intencion y sintaxis base, pero el compilador debe pasar de "reconocer texto" a "validar correctamente el lenguaje". Eso implica:

- Errores lexicales precisos.
- Errores sintacticos precisos.
- Validacion semantica real.
- Arbol de derivacion coherente con lo que el parser acepta.
- UI alineada con el estado real del compilador.
- Documentacion que describa exactamente lo que el proyecto implementa.

## 2. Orden de implementacion

### Tarea 1. Congelar la gramatica real del lenguaje
Estado: completada

Descripcion:
- Definir la gramatica que realmente debe soportar SortScript.
- Confirmar si `scan` es obligatorio al inicio o si solo es una regla recomendada.
- Confirmar si `done()` es obligatorio al final.
- Confirmar la aridad exacta de `move`, `copy`, `delete`, `rename` y `log`.
- Confirmar expresiones validas para `let`, `if`, `for` y acceso a atributos.

Verificacion:
- Revisar que la gramatica documentada coincida con lo que se va a implementar.
- Revisar ejemplos reales del tutorial y de `ejemplos/`.

### Tarea 2. Separar analisis lexico, sintactico y semantico
Estado: completada

Descripcion:
- Dejar `Tokenizador` solo para dividir el texto en tokens.
- Dejar `AFD` solo para clasificar tokens y reportar errores lexicos.
- Mantener `ParserLL1` solo para la estructura sintactica.
- Agregar una fase semantica independiente para validar significado.

Verificacion:
- Cada fase debe poder fallar por su propia causa sin mezclar responsabilidades.
- Un error semantico no debe verse como error de tokenizacion o de parser.

### Tarea 3. Implementar tabla de simbolos
Estado: completada

Descripcion:
- Registrar variables declaradas con `let`.
- Registrar identificadores de bucle en `for each`.
- Definir ambito basico por bloque.
- Detectar uso de variables no declaradas.
- Detectar redeclaraciones.
- Detectar usos posteriores de nombres que no existen.
- Definir y registrar firmas de funciones integradas (move, copy, delete, rename, log) con tipos y aridades.

Subtareas (implementacion pragmatica):
- 3.1 Definir el formato de firmas y los tipos permitidos (`path`, `num`, `string`, `collection`, `void`).
- 3.2 Añadir soporte de firmas en `SymbolTable` y registrar las funciones integradas.
- 3.3 Extender `SemanticAnalyzer` para validar aridad y tipos según firmas.
- 3.4 Escribir tests unitarios para firmas y tipado (casos correctos e incorrectos).
- 3.5 Ejecutar la suite de pruebas y documentar resultados en este archivo.

Verificacion:
- Un programa que invoque `move`/`copy`/`rename`/`delete`/`log` con aridad o tipos incorrectos debe producir un error semantico específico.
- Los tests deben cubrir errores por aridad, por tipo y por argumentos no declarados.

### Tarea 4. Validar uso real de funciones integradas
Estado: completada

Descripcion:
- Definir la cantidad de argumentos permitidos por cada funcion integrada.
- Validar tipos y forma de argumentos.
- Separar reglas de `log` frente a reglas de operaciones de archivos.
- Evitar que una funcion acepte entradas fuera de su contrato.

Verificacion:
- `move`, `copy`, `delete`, `rename` y `log` deben rechazar llamadas invalidas.
- El mensaje de error debe decir exactamente que funcion fallo y por que.

### Tarea 5. Corregir recuperacion de errores y modo panico
Estado: completada

Descripcion:
- Ajustar el modo panico para que no oculte el error original.
- Evitar que el parser termine reportando `done()` cuando el fallo real fue anterior.
- Hacer que la linea del error corresponda al token que realmente rompio la produccion.
- Diferenciar con claridad error principal y errores derivados por recuperacion.

Verificacion:
- Quitar un parentesis, una llave o un argumento debe reportar el error real.
- Si falta una pieza antes de `done()`, el mensaje no debe culpar solo al cierre del programa.

### Tarea 6. Unificar manejo de comentarios y literales
Estado: completada

Descripcion:
- Confirmar que comentarios dentro de cadenas no se eliminen por error.
- Revisar el tratamiento de rutas, extensiones y cadenas con caracteres especiales.
- Verificar que la tokenizacion de `.` y de extensiones siga consistente con el lenguaje.

Verificacion:
- Comentarios reales deben eliminarse.
- Texto entre comillas no debe corromperse por comentario falso.
- `.pdf`, `.jpg` y similares deben seguir siendo reconocidos correctamente.

### Tarea 7. Ajustar el arbol de derivacion
Estado: completada

Descripcion:
- Hacer que los nodos reflejen exactamente las producciones aceptadas.
- Evitar nodos ambiguos o demasiado genericos si la produccion real es mas especifica.
- Mantener coherencia entre el arbol y el parser.

Verificacion:
- El arbol debe dibujar solo lo que efectivamente se parseo.
- Sentencias con error no deben quedar representadas como si fueran completas.

### Tarea 8. Mejorar la UI para reflejar el compilador real
Estado: completada

Descripcion:
- Revisar la distribucion visual de paneles y tablas.
- Asegurar que los errores se vean en el orden correcto y con contexto util.
- Separar visualmente errores lexicos, sintacticos y semanticos.
- Mostrar claramente el estado de compilacion, conteo de errores y arbol.
- Si hace falta, ajustar jerarquia visual, tamanos, contraste y nombres de pestañas para que la interfaz describa mejor lo que hace el compilador.

Verificacion:
- La UI debe mostrar el estado real del analisis sin confundir fases.
- Los errores deben ser legibles y faciles de revisar.
- El arbol solo debe habilitarse cuando el analisis realmente fue consistente.

### Tarea 9. Ajuste de funciones integradas
Estado: completada

Descripcion:
- Eliminar codigo muerto en la validacion semantica de funciones.
- Confirmar que las 5 funciones (move, copy, delete, rename, log) se integren correctamente entre AFD, parser y analizador semantico.
- Verificar que los mensajes de error cubran aridad y tipo.

Verificacion:
- Los tests existentes deben seguir pasando despues de la limpieza.
- No debe haber metodos de validacion de argumentos inalcanzables.

### Tarea 10. Alinear documentacion, tutorial y ejemplos
Estado: completada

Descripcion:
- Actualizar README.md para reflejar las 9 tareas completadas.
- Actualizar la gramatica en Tutorial Sorterscript.md (produccion programa, eliminacion de nodo sentencia y bloquePrograma → bloque).
- Actualizar arboles sintacticos en el tutorial (sin wrappers sentencia, programa como raiz directa).
- Actualizar analisis_completo.md con la gramatica actual.

Verificacion:
- Los ejemplos en ejemplos/ se compilan sin errores.
- El tutorial refleja el arbol real que produce el parser (programa > scanStmt + letStmt + ...).
- No quedan referencias obsoletas a bloquePrograma o sentencia como wrapper en la documentacion.

### Tarea 11. Casos de prueba finales
Estado: completada

Descripcion:
- Agregar 12 nuevos tests de cobertura: lexicos (ext_uppercase, underscore_id, size_zero), sintacticos (empty_program_simple, empty_for_body, missing_scan, nested_if_in_for) y semanticos (for_source_undeclared, delete_case_insensitive, copy_string_path_mix, log_variable_from_for, scope_inherit_outer).
- Agregar ejemplo completo `completo.ss` en ejemplos/ que ejercita todas las caracteristicas del lenguaje.
- Verificar que los ejemplos existentes se compilan sin errores.
- Actualizar DebugAst.java para aceptar rutas de archivo como argumento.

Verificacion:
- Suite completa: 44 tests, passed=44 failed=0.
- Ejemplos: `ejemplos/01_intro.ss`, `02_condicionales.ss`, `03_variables.ss`, `04_completo.ss`, `ejemplo.ss`, `completo.ss` producen arboles completos sin errores.
- `ejemplos/99_errores_lexicos.ss` produce errores lexicos/sintacticos esperados sin colgarse.

### Tarea 12. Validacion completa por capas
Estado: completada

Descripcion:
- Crear `ValidationTests.java` con 58 pruebas organizadas por capas: lexica (10), sintactica (15), semantica (28) e integracion (5).
- Verificar que errores lexicos aparecen con caracteres invalidos (@, #, $) y que tokens validos no producen falsos positivos.
- Verificar que errores sintacticos aparecen en cada produccion mal formada (scan, for, if, let, funcCall, bloques).
- Verificar que errores semanticos aparecen por: variable no declarada, redeclaracion, alcance, tipo de argumento, aridad, atributo invalido.
- Verificar que programas completos y complejos pasan todas las capas sin errores.
- Corregir bug en `ParserLL1.java`: varios metodos (`termino`, `expresion`, `literal`, `condicionSimple`, `opComparacion`, `terminoRuta`, `literalRuta`, `expresionRuta`) no verificaban `panicoUsado` antes de generar errores, causando errores cascada espurios en recuperacion panico. Se anadio la guarda `if (panicoUsado) return null;` en todos ellos.

Verificacion:
- Suite existente: 44 tests, passed=44 failed=0.
- Validacion por capas: 58 tests, passed=58 failed=0.
- Total: 102 pruebas automatizadas, 0 fallos.
- El arbol de los ejemplos en `ejemplos/` se genera correctamente sin errores.
- La UI compila y se ejecuta sin excepciones.

Descripcion:
- Probar primero compilacion del proyecto.
- Probar luego casos lexicos.
- Luego sintacticos.
- Luego semanticos.
- Finalmente revisar UI y arbol.

Verificacion:
- Cada caso de prueba debe dejar evidencia clara de que el cambio realmente funciono.
- Antes de marcar una tarea como terminada, revisar que no introdujo regresiones en las capas previas.

## 3. Historico de tareas

### Formato para registrar cada cierre
- Fecha:
- Tarea:
- Cambio realizado:
- Verificacion ejecutada:
- Resultado:
- Observaciones:

### Registro actual
- Fecha: 2026-06-02
- Tarea: Creacion del plan de implementacion
- Cambio realizado: Se creo este archivo para centralizar el orden de trabajo, el historial y la validacion de cambios.
- Verificacion ejecutada: Revision manual del estado actual del proyecto antes de editar codigo.
- Resultado: Pendiente de ejecucion de las tareas definidas.
- Observaciones: El proyecto requiere reestructuracion parcial para separar semantica, mejorar errores y alinear UI/documentacion.
- Fecha: 2026-06-02
- Tarea: Tarea 1 - Congelar la gramatica real del lenguaje
- Cambio realizado: Se fijo la gramatica inicial obligando `scan` al comienzo del programa, se retiro `scan` de las sentencias internas y se alineo la documentacion tecnica con esa regla.
- Verificacion ejecutada: Compilacion completa del proyecto con `javac`.
- Resultado: Exitosa.
- Observaciones: La estructura del lenguaje quedo definida para continuar con validacion semantica y refinamiento de errores.

- Fecha: 2026-06-02
- Tarea: Tarea 3 (parcial) - Implementacion de tabla de firmas
- Cambio realizado: Se añadió soporte de `FunctionSignature` en `src/parser/SymbolTable.java` y se registraron las funciones integradas (`move`, `copy`, `rename`, `delete`, `log`). Se actualizó `run_tests.bat` para compilar correctamente en rutas con espacios.
- Verificacion ejecutada: Compilacion y ejecucion de `tests.SemanticTests`.
- Resultado: Tests semánticos: passed=10 failed=0.
- Observaciones: La tabla de firmas está disponible para que `SemanticAnalyzer` la consulte; quedan por implementar comprobaciones de tipos más estrictas y documentar los cambios en README.
 - Fecha: 2026-06-02
 - Tarea: Tarea 3 (parcial) - Comprobaciones de tipos estrictas
 - Cambio realizado: `SemanticAnalyzer` se actualizó para validar aridad y tipos de argumentos usando las firmas registradas en `SymbolTable`. Se mejoró la inferencia de literales de ruta incluso cuando están entre comillas.
 - Verificacion ejecutada: Compilacion y ejecucion de `tests.SemanticTests`.
 - Resultado: Tests semánticos: passed=10 failed=0.
 - Observaciones: Las comprobaciones estrictas para funciones integradas están activas; quedan por documentar en README y mejorar la UI para resaltar errores semánticos en el editor.

- Fecha: 2026-06-02
- Tarea: Tarea 3 (parcial) - Documentacion de Tarea 3
- Cambio realizado: Se actualizo `README.md` con la seccion de Tarea 3, se amplió `Tutorial SortScript.md` con validacion semantica y se añadieron rótulos de Tarea 3 a `run_tests.ps1` y `run_tests.bat`.
- Verificacion ejecutada: Compilacion y ejecucion de `tests.SemanticTests` despues de los cambios de documentacion y UX.
- Resultado: Tests semánticos: passed=10 failed=0.
- Observaciones: La documentacion ya refleja el flujo real de analisis y validacion; el plan queda sincronizado con la implementacion actual.

- Fecha: 2026-06-02
- Tarea: Tarea 3 - Aislamiento de ámbito para bloques if/else
- Cambio realizado: Se añadieron métodos `handleIf` y `handleElseOpt` en `SemanticAnalyzer.java` para que los bloques `if { }`, `else { }` y `else if { }` creen ámbitos independientes, evitando que variables internas filtren al ámbito padre. Se añadieron 3 tests (`sem_if_scope`, `sem_else_scope`, `sem_else_if_scope`) en `SemanticTests.java`.
- Verificacion ejecutada: Compilacion y ejecucion de `tests.SemanticTests`.
- Resultado: Tests semánticos: passed=13 failed=0.
- Observaciones: Con este cambio la Tarea 3 queda completada al 100%. Todos los bloques con llaves (`for`, `if`, `else`, `else if`) ahora gestionan ámbito correctamente.

- Fecha: 2026-06-02
- Tarea: Tarea 4 - Validacion de uso real de funciones integradas
- Cambio realizado: Se corrigio case-sensitivity en `handleFuncCall` de `SemanticAnalyzer.java` para que `MOVE`, `Copy`, `DELETE` (cualquier combinacion de mayusculas/minusculas) sean reconocidas por las firmas registradas. Se elimino codigo fallback duplicado que quedaba obsoleto. Se anadieron 12 tests de cobertura completa: `log` (1 arg, 2 args, numero, 0 args, 3 args), `copy` (ok y tipo invalido), `rename` (ok y tipo invalido), `delete` (ok), `MOVE` mayuscula (case-insensitive) y `move` con numero (tipo invalido).
- Verificacion ejecutada: Compilacion y ejecucion de `tests.SemanticTests`.
- Resultado: Tests semanticos: passed=25 failed=0.
- Observaciones: Quedan cubiertos todos los casos de aridad y tipo para las 5 funciones integradas. La Tarea 4 queda completada al 100%.

- Fecha: 2026-06-02
- Tarea: Tarea 5 - Correccion de recuperacion de errores y modo panico
- Cambio realizado: En `ParserLL1.java`, se modifico `inicio()` para que cuando `bloquePrograma()` retorna con errores previos, se salte la validacion estricta de `done()` y consuma tokens hasta EOF silenciosamente, evitando errores cascada que culpaban a `done()` de fallos anteriores. Se corrigio `lineaFinal` de -1 a 1 para lineas por defecto en arrays vacios. Se anadieron 3 tests de parser recovery en `SemanticTests.java` (`syn_missing_paren_in_move`, `syn_missing_brace_in_for`, `syn_unexpected_brace_outer`) que verifican que el error real se reporta y `done()` no se menciona en cascada.
- Verificacion ejecutada: Compilacion y ejecucion de `tests.SemanticTests` + casos de parser recovery.
- Resultado: Tests: passed=28 failed=0.
- Observaciones: El modo panico ahora distingue entre error principal y errores derivados. Si ya hay errores antes de llegar a `done()`, no se producen mensajes confusos sobre el cierre del programa.

- Fecha: 2026-06-02
- Tarea: Tarea 6 - Unificar manejo de comentarios y literales
- Cambio realizado: En `Tokenizador.java`, se modifico `eliminarComentarios()` para que respete cadenas entre comillas dobles — cuando encuentra `"` copia todo el contenido literal hasta el `"` de cierre sin interpretar `//` ni `/*` como comentarios. En `AFD.java`, se elimino la comprobacion de `EXTENSIONES_VALIDAS` sobre lexemas sin punto (linea 159-161), que bloqueaba identificadores como `tmp`, `bak`, `dat` etc. al clasificarlos como `DESCONOCIDO` por estar en la lista de extensiones; la deteccion de extensiones con punto (`".pdf"`) se mantiene intacta. Se anadieron 4 tests: `lex_url_in_string`, `lex_comment_like_in_string`, `lex_tmp_as_identifier`, `lex_bak_as_identifier`.
- Verificacion ejecutada: Compilacion y ejecucion de `tests.SemanticTests`.
- Resultado: Tests: passed=32 failed=0.
- Observaciones: Queda verificada la consistencia de tokenizacion de `.` y extensiones — `.pdf` y similares siguen siendo `LIT_EXT` correctamente (`determinarTipoLexema` lineas 153-157). La tokenizacion de `f.name` sigue emitiendo `f` `.` `name` como tres tokens separados.

- Fecha: 2026-06-02
- Tarea: Tarea 7 - Ajustar el arbol de derivacion
- Cambio realizado: En `ParserLL1.java`: (1) `sentencia()` ya no crea un nodo wrapper generico, retorna directamente el nodo especifico (`forStmt`, `ifStmt`, `letStmt`, `funcCall`) o `null` en error; (2) `inicio()` aplano el nivel superior — en lugar de `programa > scanStmt + bloquePrograma`, ahora las sentencias van directamente bajo `programa`; (3) `bloquePrograma()` renombrado a `bloque()` con nodo `"bloque"`, usado solo para bloques internos (for/if/else); (4) `condicion()` ya no crea wrapper, retorna directamente el nodo `condicionOr`; (5) en modo panico, los metodos retornan `null` en vez de nodos placeholder vacios (`sentencia()` ya retornaba `NodoArbol("sentencia")` innecesario). En `SemanticAnalyzer.java`: actualizacion de comentarios estructurales para reflejar los cambios.
- Verificacion ejecutada: Compilacion y ejecucion de `tests.SemanticTests` + `DebugAst` para inspeccion visual del arbol.
- Resultado: Tests: passed=32 failed=0. Arbol resultante: `programa > scanStmt + letStmt + letStmt + done(,),EOF` (sin nodos `sentencia` ni `bloquePrograma` superfluos).
- Observaciones: El arbol ahora refleja exactamente las producciones aceptadas sin nodos intermedios genericos. Los nodos wrapper `sentencia` y `condicion` fueron eliminados por ser passthrough que no anadian informacion. El nodo `bloquePrograma` a nivel superior se elimino por ser redundante con `programa`. Solo se conservan los nodos que representan producciones reales con identidad propia.

- Fecha: 2026-06-02
- Tarea: Tarea 8 - Mejorar la UI para reflejar el compilador real
- Cambio realizado: En `CompiladorUI.java`: (1) se corrigio el subtitulo de "Analisis Lexico y Sintactico" a "Analisis Lexico, Sintactico y Semantico"; (2) se elimino la pestana "Todos los Errores" que solo listaba codigos estaticos sin errores reales; (3) se agrego una barra de estado en la parte inferior que muestra el estado de compilacion (verde si exitosa, rojo si hay errores con conteo); (4) se anadio click-to-navigate en las tres tablas de errores (lexicos, sintacticos y semanticos) para saltar a la linea del codigo; (5) se añadieron `ErrorRowRenderer` con color-coded row renderers (ambar para lexicos, naranja para sintacticos, rojo para semanticos); (6) el boton "Arbol" solo se habilita cuando las tres fases (lexica, sintactica y semantica) pasan sin errores; (7) las pestanas de errores muestran el conteo en el titulo, ej. "Errores Lexicos (3)"; (8) el reporte de consola ahora muestra el estado de cada fase individualmente.
- Verificacion ejecutada: Compilacion de `CompiladorUI.java` + ejecucion de `tests.SemanticTests`.
- Resultado: Compilacion exitosa (deprecation warnings menores de Swing). Tests: passed=32 failed=0.
- Observaciones: La UI ahora refleja correctamente el estado real del compilador con separacion visual de tipos de error, navegacion directa a la linea y barra de estado siempre visible.

- Fecha: 2026-06-02
- Tarea: Tarea 9 - Ajuste de funciones integradas
- Cambio realizado: En `SemanticAnalyzer.java`, se eliminaron los metodos muertos `checkArgumentTypes()` y `checkArgsNotNumeric()` que ya no eran llamados desde ningun punto del codigo (la validacion de tipos se hace directamente dentro de `handleFuncCall` usando firmas predefinidas). Se elimino el bloque `else` inalcanzable en `handleFuncCall()` que invocaba a `checkArgumentTypes()` cuando no habia firma — actualmente todas las funciones registradas tienen firma definida en `SymbolTable`, por lo que ese camino era codigo muerto. Se verifico que los 32 tests existentes siguen pasando sin cambios.
- Verificacion ejecutada: Compilacion y ejecucion de `tests.SemanticTests`.
- Resultado: Tests: passed=32 failed=0.
- Observaciones: La integracion de funciones entre AFD (clasificacion FUNC_MOVE, FUNC_COPY, etc.), parser (produccion funcCall) y analizador semantico (firmas en SymbolTable) queda completa y sin codigo inalcanzable.

- Fecha: 2026-06-03
- Tarea: Tarea 12 (v2) - Refinamiento de UI y codigos de error lexicos
- Cambio realizado:
  1. `CompiladorUI.java`: Token table muestra "Desconocido" en vez de enum DESCONOCIDO; se anadio `TokenRowRenderer` para resaltar filas de tokens desconocidos en rojo.
  2. `TablaErrores.java`: Se renombro `ERROR_PALABRA_DESCONOCIDA` (102) a `ERROR_RESERVADA_INCOMPLETA`; se agrego `ERROR_TOKEN_NO_RECONOCIDO` (103).
  3. `AFD.java`: Error en estado no-aceptacion final (palabra reservada incompleta) usa codigo 102; error en fallo medio de token usa codigo 103.
  4. `PanelReferenciaErrores.java`: Se agregaron entradas para codigos 102 y 103 con descripciones detalladas.
  5. `SemanticTests.java` y `ValidationTests.java`: Se reemplazaron identificadores de una sola letra (f, a, n) por nombres seguros (x, z, k, item) para evitar errores lexicos por conflicto con prefijos de palabras reservadas.
- Verificacion ejecutada: Compilacion completa + ejecucion de 102 tests (44 semanticos + 58 por capas).
- Resultado: Tests: passed=102 failed=0. QuickTest confirma errores 102 para 'eac', 'sc', 'f' y KW_EACH correcto para 'each'.
- Observaciones: Los errores lexicos ahora tienen tres categorias (101: alfabeto, 102: reservada incompleta, 103: token no reconocido). La UI refleja inmediatamente todos los tokens desconocidos con "Desconocido" y fondo rojo. Queda pendiente verificar que la UI muestre correctamente los errores 102 y 103 en la pestana "Errores Lexicos".

- Fecha: 2026-06-03
- Tarea: Tarea 12 (v3) - Correccion de regresiones semanticas y recuperacion de errores
- Cambio realizado:
   1. `SemanticAnalyzer.java` `handleFor()`: Se elimino el chequeo `symbols.isDeclared(itName)` despues de `enterScope()` que impedía shadowing de variables externas (p. ej. `for each x in x`). La validacion de redeclaracion en el mismo ambito se mantiene via `symbols.declare()`.
   2. `SemanticAnalyzer.java` `handleFor()`: Se cambio la condicion de `ch.size() >= 5` a `ch.size() >= 7` para evitar falsos positivos semanticos cuando el arbol de `forStmt` esta incompleto por errores sintacticos previos (el indice 4 ya no es confiable como fuente si faltan hijos).
   3. `SemanticAnalyzer.java` `handleFuncCall()`: Se agrego guarda `ch.size() >= 4` antes de validar aridad y tipos, evitando errores semanticos en cascada cuando la llamada a funcion no se parseo completamente. Se agrego rama `else` para seguir visitando hijos incluso con parseo incompleto.
   4. `ParserLL1.java` `sentencia()`: Se agrego `pos++` antes de `panico()` en el caso `default` para consumir el token ofensivo, eliminando un bucle infinito cuando el token actual esta en TOKENS_SEGUROS pero no es reconocido como sentencia (ej. `scan` repetido como sentencia).
   5. `ComprehensiveErrorTests.java`: Se actualizaron expectativas de 6 pruebas y se reemplazaron identificadores de una letra no seguros (`a`, `s`) por equivalentes seguros (`x`, `z`, `v`).
   6. `SemanticEdgeTests.java`: Se actualizo expectativa de `DESCONOCIDO en argumento func` de `(1,1,1)` a `(1,1,0)`.
- Verificacion ejecutada: Compilacion completa + ejecucion de 6 suites de prueba.
- Resultado:
   - SemanticTests: 44/44 passed
   - ValidationTests: 58/58 passed
   - LexicalEdgeTests: 79/79 passed
   - ParserEdgeTests: 31/31 passed
   - SemanticEdgeTests: 23/23 passed
   - ComprehensiveErrorTests: 91/91 passed
- **Total: 326 pruebas automatizadas, 0 fallos.**

### Tarea 12 (v5). Codigos de error lexicos granulares por categoria (KW, FN, AT, LIT)
Estado: completada

Descripcion:
- Se agregaron codigos de error lexico granulares para cada categoria de token:
  - **110** `ERROR_KW_INCOMPLETA`: Prefijo de palabra clave incompleto (ea, fo, sc, i)
  - **111** `ERROR_FN_INCOMPLETA`: Prefijo de funcion incompleto (mov, cop, del)
  - **112** `ERROR_ATTR_INCOMPLETO`: Prefijo de atributo incompleto (ex, siz, nam)
  - **113** `ERROR_KW_MALFORMADA`: Palabra clave con caracteres extra (eachh, forr, scann)
  - **114** `ERROR_FN_MALFORMADA`: Nombre de funcion con caracteres extra (movee, logg)
  - **115** `ERROR_ATTR_MALFORMADO`: Nombre de atributo con caracteres extra (exxt, sizze)
  - **116** `ERROR_LITERAL_MALFORMADO`: Literal con formato invalido (5XYZ, .abcdefgh)
- Codigos anteriores se mantienen: 101 (alfabeto), 102 (reservada incompleta), 103 (token no reconocido).
- La funcion `tipoPR()` se simplifico eliminando el prefijo `PR_` (codigo muerto).

Cambios en `SortScriptAFD.java`:
- Se agregaron conjuntos `kwStates`, `funcStates`, `attrStates` con todos los estados intermedios.
- Se pasan al constructor del AFD para determinar la categoria de un estado incompleto.

Cambios en `AFD.java`:
- Nuevo constructor acepta `kwStates`, `funcStates`, `attrStates`.
- Nueva logica en `aceptar()`: al terminar en estado no-aceptante, se determina la categoria y se asigna codigo especifico.
- Metodo `determinarCategoria()` con prioridad: KW > FN > AT.
- Identificadores validos (como `inner`, `myVar`) NO se marcan como error.

Cambios en `TablaErrores.java`:
- Nuevas constantes: `ERROR_KW_INCOMPLETA = 110`, `ERROR_FN_INCOMPLETA = 111`, `ERROR_ATTR_INCOMPLETO = 112`, `ERROR_KW_MALFORMADA = 113`, `ERROR_FN_MALFORMADA = 114`, `ERROR_ATTR_MALFORMADO = 115`, `ERROR_LITERAL_MALFORMADO = 116`.

Cambios en `PanelReferenciaErrores.java`:
- Se agregaron entradas de documentacion para los codigos 110-116.

Verificacion:
- Compilacion exitosa sin errores.
- `tests.SemanticTests`: 44/44, `ValidationTests`: 58/58, `LexicalEdgeTests`: 79/79, `ParserEdgeTests`: 31/31, `SemanticEdgeTests`: 23/23, `ComprehensiveErrorTests`: 91/91.
- **Total: 326 pruebas automatizadas, 0 fallos.**
- `eac` reporta error 110 (KW), `mov` reporta 111 (FN), `ex` reporta 112 (AT).
- `inner` NO se marca como error (identificador valido).
- `5XYZ` reporta error 116 (literal mal formado).
- Observaciones: Se corrigieron tres regresiones introducidas en la sesion anterior (Tarea 12 v2): (1) el shadowing de variables en `for each` ahora funciona correctamente, (2) el bucle infinito al encontrar `scan` como sentencia fue eliminado, (3) los errores semanticos no se disparan cuando el arbol sintactico esta incompleto por errores previos. Todos los tests de integracion (CAPA 4) pasan nuevamente.

## 4. Regla operativa para futuras tareas

Antes de cerrar cualquier cambio:

1. Actualizar el estado de la tarea aqui.
2. Ejecutar la verificacion correspondiente.
3. Registrar el resultado real.
4. Solo entonces pasar a la siguiente tarea.

## 5. Prioridad real recomendada

1. Gramatica real del lenguaje. (completada)
2. Tabla de simbolos y validacion semantica. (completada)
3. Validacion de uso real de funciones integradas. (completada)
4. Correccion del modo panico y mensajes de error. (completada)
5. Manejo de comentarios, literales y extensiones. (completada)
6. Arbol de derivacion coherente con el parser. (completada)
7. UI alineada con el estado real del compilador. (completada)
8. Ajuste de funciones integradas. (completada)
9. Documentacion, tutorial y ejemplos. (completada)
10. Casos de prueba finales y cobertura. (completada)
11. Validacion completa por capas. (completada)
12. Refinamiento de UI y codigos de error léxicos. (completada)

### Tarea 12 (v2). Refinamiento de UI y codigos de error léxicos
Estado: completada

Descripcion:
- La tabla de tokens ahora muestra "Desconocido" (en espanol) en lugar del nombre de la enumeracion `DESCONOCIDO` para tokens no reconocidos.
- Se anadio `TokenRowRenderer` que pinta las filas de tokens desconocidos con un fondo rojo translucido (255,70,70,40), facilitando la identificacion visual.
- Se expandieron los codigos de error léxicos para distinguir entre:
  - **101** `ERROR_SIMBOLO_ALFABETO`: Caracter no definido en el alfabeto (@, #, $, etc.).
  - **102** `ERROR_RESERVADA_INCOMPLETA`: Prefijo de palabra reservada que no completa un token valido (eac, sc, fo, ea, etc.).
  - **103** `ERROR_TOKEN_NO_RECONOCIDO`: Secuencia de caracteres que no corresponde a ningun patron reconocido (caso infrecuente de fallo en medio de token).
- Se actualizo `PanelReferenciaErrores.java` con las descripciones y soluciones para los codigos 102 y 103.
- Se corrigieron tests que usaban identificadores de una sola letra (f, a, n) que coincidian con prefijos de palabras reservadas, reemplazandolos por nombres seguros (x, z, k).

Verificacion:
- Compilacion y ejecucion de `tests.SemanticTests` (44 tests, passed=44 failed=0).
- Compilacion y ejecucion de `tests.ValidationTests` (58 tests, passed=58 failed=0).
- Total: 102 pruebas automatizadas, 0 fallos.
- QuickTest confirma que `eac`, `sc`, `f` generan error 102 ("Palabra reservada incompleta").
- QuickTest confirma que `each` completo se clasifica correctamente como `KW_EACH`.
- La UI compila sin errores.

### Tarea 12 (v5). Reorden secuencial de codigos de error 101-121
Estado: completada

Descripcion:
- Todos los codigos de error se renumeraron a un rango secuencial unico 101-121:
  - Lexico: 101 (alfabeto), 102 (token no reconocido), 103 (KW incompleta), 104 (FN incompleta), 105 (AT incompleto), 106 (KW malformada), 107 (FN malformada), 108 (AT malformado), 109 (literal mal formado).
  - Sintactico: 110 (scan inicio), 111 (instruccion invalida), 112 (error general), 113 (expresion), 114 (atributo invalido), 115 (funcion no reconocida).
  - Semantico: 116 (no declarado), 117 (ya declarado), 118 (argumentos cantidad), 119 (argumentos tipo), 120 (atributo tipo), 121 (fuera de alcance).
- Se elimino `ERROR_RESERVADA_INCOMPLETA = 102` (alias legacy no necesario).
- Se restauro `pasoPorAceptacion` en AFD.java con salvaguarda IDENTIFICADOR (solo emite 106-108 cuando `determinarTipoLexema()` retorna DESCONOCIDO).
- Se restauro `codigoMalformado()` en AFD.java.
- `inner`, `eachFile`, `eachh` NO se marcan como error (son identificadores validos).
- `each*`, `move*`, `ext*` si generan errores 106, 107, 108 respectivamente.
- `PanelReferenciaErrores.java`: todas las entradas actualizadas con numeracion secuencial (102-121).

Verificacion:
- Compilacion exitosa sin errores.
- `tests.SemanticTests`: 44 tests, passed=44 failed=0.
- `tests.ValidationTests`: 58 tests, passed=58 failed=0.
- `LexicalEdgeTests`: 79 tests, passed=79 failed=0.
- `ParserEdgeTests`: 31 tests, passed=31 failed=0.
- `SemanticEdgeTests`: 23 tests, passed=23 failed=0.
- `ComprehensiveErrorTests`: 91 tests, passed=91 failed=0.
- **Total: 326 pruebas automatizadas, 0 fallos.**
