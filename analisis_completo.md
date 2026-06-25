28/5/26, 11:43 p.m.

> **ANÁLISIS** **COMPLETO** **DEL** **COMPILADOR** **SORTSCRIPT**
>
> **1.** **VISIÓN** **GENERAL** **DEL** **PROYECTO**
>
> **SortScript** **Compilador** es un compilador para un lenguaje DSL
> (Domain-Specific Language) llamado **SortScript**, diseñado para la
> organización y manipulación automatizada de archivos. Está escrito en
> Java y usa Swing para la interfaz gráfica.
>
> **Estructura** **del** **proyecto**
>
> SortScript---Compilador/ ├── src/
>
> │ ├── lexico/ \# Analizador léxico
>
> │ │ ├── AFD.java
>
> │ │ ├── SortScriptAFD.java │ │ ├── TipoToken.java
>
> │ │ ├── Token.java
>
> │ │ └── Tokenizador.java

\# Autómata Finito Determinista

\# Construcción del AFD específico \# Enumeración de tipos de token \#
Clase Token

\# Tokenizador (pre-procesamiento)

> │ ├── parser/ \# Analizador sintáctico
>
> │ │ ├── ParserLL1.java │ │ ├── NodoArbol.java
>
> │ │ ├── Errores.java

\# Parser descendente recursivo LL(1) \# Nodo para árbol de derivación

\# Gestor de errores

> │ │ ├── ErrorCompilacion.java \# Entidad de error
>
> │ │ └── TablaErrores.java \# Catálogo de mensajes de error │ └── ui/
> \# Interfaz gráfica
>
> │ ├── CompiladorUI.java │ ├── PanelArbol.java │ ├── VentanaArbol.java
>
> │ └── TextLineNumber.java

\# Ventana principal

\# Dibujo del árbol sintáctico \# Ventana del árbol

\# Numeración de líneas en editor

> ├── ejemplos/
>
> ├── bin/

\# Scripts de ejemplo en SortScript

\# Clases compiladas

> └── compilar_y_ejecutar.{bat,ps1} \# Scripts de compilación
>
> **Componentes** **externos** **requeridos**
>
> Java 8 o superior
>
> FlatLaf (opcional, para tema visual)

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
1/19

28/5/26, 11:43 p.m.

> **2.** **¿QUÉ** **HACE** **SORTSCRIPT** **COMO** **LENGUAJE?**
>
> SortScript es un DSL para **automatizar** **la** **organización**
> **de** **archivos** con una sintaxis similar a pseudocódigo. Permite:
>
> **Escanear** carpetas ( scan "ruta" as variable) **Iterar** archivos (
> for each archivo in carpeta { })
>
> **Condicionales** sobre atributos de archivos ( if archivo.ext ==
> .pdf) **Operaciones**: move(), copy(), delete(), rename(), log()
> **Variables** ( let nombre = valor)
>
> **Expresiones** con suma (+) y acceso a atributos ( .ext, .size,
> .name, .date) **Comentarios** // línea y /\* bloque \*/
>
> **Ejemplo** **representativo**
>
> scan "C:/Users/Ana/Downloads" as downloads
>
> for each file in downloads { if file.ext == .pdf {
>
> move(file, "C:/Users/Ana/Documents") }
>
> if file.size \> 100MB {
>
> log("Archivo grande: " + file.name) }
>
> } done()
>
> **3.** **ARQUITECTURA:** **SECUENCIA** **DEL** **COMPILADOR**
>
> El compilador sigue el modelo clásico de dos fases:
>
> Código Fuente │
>
> ▼ ┌──────────────────────┐
>
> │ 1. PRE-PROCESAMIENTO │ Tokenizador.java │ - Elimina comentarios │
>
> │ - Divide en tokens │

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
2/19

28/5/26, 11:43 p.m.

> │ - Conserva líneas │ └──────────┬───────────┘
>
> ▼ ┌──────────────────────┐
>
> │ 2. ANÁLISIS LÉXICO │ AFD.java + SortScriptAFD.java │ - Valida
> alfabeto │
>
> │ - Recorre AFD │ │ - Clasifica tokens │ │ - Detecta errores │
> └──────────┬───────────┘
>
> ▼ ┌──────────────────────┐ │ 3. FILTRO │ │ - Tokens no válidos │ │ se
> excluyen │ │ - Sólo tokens │ │ válidos al parser │
> └──────────┬───────────┘
>
> ▼ ┌──────────────────────┐
>
> │ 4. ANÁLISIS │ ParserLL1.java │ SINTÁCTICO │
>
> │ - Parser LL(1) │ │ - Árbol derivación │ │ - Recuperación de │ │
> errores (pánico) │ └──────────┬───────────┘
>
> ▼ ┌──────────────────────┐
>
> │ 5. INTERFAZ GRÁFICA │ CompiladorUI.java │ - Tabla de tokens │
>
> │ - Tabla de errores │ │ - Árbol sintáctico │ │ - Consola/reporte │
> └──────────────────────┘
>
> **4.** **ANÁLISIS** **DETALLADO** **POR** **CAPA**
>
> **4.1** **CAPA** **LÉXICA** **(** **src/lexico/)**
>
> **4.1.1** **TipoToken.java**

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
3/19

28/5/26, 11:43 p.m.

> **¿Qué** **es?** Enumeración que define todos los tipos de tokens del
> lenguaje.
>
> **Clasificación:**
>
> \| Prioridad \| Grupo \| Tokens \| \|-----------\|-------\|--------\|
>
> \| 1 \| Keywords \| SCAN, AS, FOR, EACH, IN, IF, ELSE, LET, DONE \| \|
> 2 \| Funciones \| MOVE, COPY, DELETE, RENAME, LOG \|
>
> \| 2 \| Atributos \| EXT, SIZE, NAME, DATE \|
>
> \| 3 \| Literales \| LIT_RUTA, LIT_EXT, LIT_TAMANIO, LIT_ENTERO,
> LIT_CADENA \|
>
> \| 4 \| Operadores \| OP_IGUAL, OP_DISTINTO, OP_MAYOR_EQ, OP_MENOR_EQ,
> OP_MAYOR, OP_MENOR, OP_AND, OP_OR, OP_NOT, OP_ASIG, OP_SUMA, OP_PUNTO
> \|
>
> \| 4 \| Delimitadores \| LLAVE_ABRE, LLAVE_CIERRA, PAR_ABRE,
> PAR_CIERRA, COMA \| \| - \| Especiales \| IDENTIFICADOR, EOF, ERROR,
> DESCONOCIDO \|
>
> **¿Por** **qué** **así?** La prioridad refleja el orden en que se
> deben reconocer los tokens. Las keywords tienen prioridad 1 porque
> deben matchear antes que identificadores (si algo es "scan", debe ser
> KW_SCAN, no IDENTIFICADOR).
>
> **4.1.2** **Token.java**
>
> **¿Qué** **es?** Clase que representa un token individual con: -
> lexema: el texto original
>
> \- linea: número de línea
>
> \- tipo: TipoToken asociado
>
> \- estadoFinal: estado del AFD donde terminó
>
> \- existeSimbolo: booleano que indica si el token fue reconocido
> válidamente
>
> **¿Por** **qué** **existeSimbolo?** Es una bandera crítica en el
> flujo: los tokens con existeSimbolo=false son excluidos del análisis
> sintáctico. Esto separa errores léxicos de sintácticos.
>
> **4.1.3** **Tokenizador.java**
>
> **¿Qué** **hace?** Tres funciones:
>
> 1\. **eliminarComentarios(String)**: Pre-procesamiento que elimina: 2.
> Comentarios de línea ( //)
>
> 3\. Comentarios de bloque ( /\* \*/)
>
> 4\. Importante: preserva saltos de línea para no alterar la numeración
>
> 5\. **tokenizarLinea(String)**: Divide una línea en tokens:

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
4/19

28/5/26, 11:43 p.m.

> 6\. Respeta cadenas entre comillas dobles como una unidad 7. Detecta
> operadores dobles ( ==, !=, \<=, \>=, &&, \|\|)
>
> 8\. Separa operadores/delimitadores individuales
>
> 9\. Divide por espacios en blanco
>
> 10\. **tokenizador(String)**: Orquestador:
>
> 11\. Llama eliminarComentarios 12. Divide por \n
>
> 13\. Tokeniza cada línea
>
> 14\. Asigna número de línea a cada token 15. Retorna arreglo de tokens
>
> **¿Por** **qué** **un** **array** **de** **tamaño** **fijo**
> **10000?** Es una decisión de diseño cuestionable. Se usó un array
> fijo por simplicidad pero podría desbordarse. Una lista dinámica sería
> más robusta.
>
> **¿Por** **qué** **se** **separa** **por** **líneas?** Para poder
> rastrear el número de línea de cada token, fundamental para reportar
> errores.
>
> **Detalle** **clave**: Una extensión como .pdf se tokeniza como un
> solo token porque el elif (c == '.' && actual.length() == 0) captura
> el punto seguido de letras como una unidad. Esto significa que
>
> file.ext se divide como \[file, ., ext\] y .pdf como \[.pdf\].
>
> **4.1.4** **AFD.java** **-** **El** **Autómata** **Finito**
> **Determinista**
>
> **¿Qué** **es?** Implementación de un AFD (Automata Finito
> Determinista) parametrizable.
>
> **Componentes** **del** **AFD:**
>
> \- estados: Set de nombres de estado (strings) - alfabeto: Set de
> caracteres válidos
>
> \- transiciones: Map\<String, Map\<Character, String\>\> - desde un
> estado + carácter → nuevo estado - estadoInicial: String ("INICIO" en
> nuestro caso)
>
> \- estadosAceptacion: Set de estados finales
>
> **Método** **aceptar(Token\[\])**:
>
> Recorre cada token y para cada carácter del lexema (en mayúsculas): 1.
> Verifica si el carácter está en el alfabeto → si no, ERROR
>
> 2\. Verifica si hay transición desde el estado actual → si no, llama a
> determinarTipoLexema() como fallback

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
5/19

28/5/26, 11:43 p.m.

> 3\. Transiciona al siguiente estado
>
> 4\. Al final, si no hubo error, llama a tipoPR(estadoFinal) para
> determinar el tipo
>
> **Método** **tipoPR()**: Intenta convertir el estado final
> directamente a un TipoToken con prefijos PR\_, KW\_, FUNC\_, ATTR\_, y
> si falla, usa el fallback determinarTipoLexema().
>
> **Método** **determinarTipoLexema()** (estático): Sistema de respaldo
> basado en expresiones regulares que detecta:
>
> \- Operadores ( ==, !=, etc.)
>
> \- Delimitadores ( {, }, (, ), ,)
>
> \- Literales: rutas ( "..."), extensiones ( .ext), tamaños ( 100MB),
> enteros, cadenas - Identificadores ( \[a-zA-Z\_\]\[a-zA-Z0-9\_\]\*)
>
> **¿Cuál** **es** **el** **problema** **principal** **del** **AFD**
> **actual?**
>
> El AFD construido por SortScriptAFD.obtenerAFD() solo reconoce
> **palabras** **clave,** **funciones** **y** **atributos** escritas
> letra por letra. NO reconoce:
>
> \- Operadores ( ==, !=, \>, \<, +, etc.) - Delimitadores ( {, }, (, ),
> ,)
>
> \- Literales (rutas, números, extensiones) - Identificadores
>
> \- Espacios en blanco
>
> Esto significa que el AFD real solo cubre ~18 palabras. **Todo**
> **lo** **demás** **se** **resuelve** **mediante**
> **determinarTipoLexema()** **con** **regex**, que es un sistema de
> respaldo externo al autómata.
>
> **¿Por** **qué** **se** **diseñó** **así?** Porque construir un AFD
> que reconozca patrones complejos como rutas ( "...") o tamaños (
> 100MB) es más complejo. La mezcla AFD + regex fue un compromiso entre
> la teoría de autómatas y la practicidad.
>
> **4.1.5** **SortScriptAFD.java**
>
> **¿Qué** **hace?** Construye el AFD específico de SortScript con:
>
> \- **Alfabeto**: letras A-Z, a-z, dígitos 0-9, y símbolos especiales (
> +-=\<\>!.,(){}\[\]/\*" \_\t\n\r)
>
> \- **Transiciones**: Para cada keyword ( SCAN, AS, FOR, etc.), función
> ( MOVE, COPY, etc.) y atributo ( EXT, SIZE, etc.), agrega transiciones
> letra por letra desde el estado "INICIO"
>
> **Ejemplo** **de** **cómo** **se** **construye** **"SCAN":**
>
> INICIO --'S'--\> "S" --'C'--\> "SC" --'A'--\> "SCA" --'N'--\> "SCAN"
> (aceptación)

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
6/19

28/5/26, 11:43 p.m.

> **Estados** **clave**: INICIO, ERROR, ACEPTA + un estado por cada
> prefijo de palabra clave.
>
> **4.2** **CAPA** **SINTÁCTICA** **(** **src/parser/)**
>
> **4.2.1** **ParserLL1.java** **-** **Analizador** **Sintáctico**
> **LL(1)**
>
> **¿Qué** **es?** Un parser descendente recursivo predictivo que
> implementa una gramática LL(1).
>
> **Arquitectura** **del** **parser**:
>
> Cada método coincide con una producción de la gramática:
>
> programa → scanStmt { sentencia } done ( ) EOF
>
> sentencia → forStmt \| ifStmt \| letStmt \| funcCall
> scanStmt → SCAN LIT_RUTA AS IDENTIFICADOR
>
> forStmt → FOR EACH IDENTIFICADOR IN IDENTIFICADOR { { sentencia } }
> ifStmt → IF condicion { { sentencia } } elseOpt
>
> elseOpt → ELSE { { sentencia } } \| ε letStmt → LET IDENTIFICADOR =
> expresion funcCall → FUNC ( argumentos ) argumentos → expresion
> restoArgs \| ε restoArgs → , expresion restoArgs \| ε expresion →
> termino restoExpresion
>
> restoExpresion → + termino restoExpresion \| ε
>
> termino → IDENTIFICADOR accesoAtributo \| literal \| ( expresion )
> accesoAtributo → . ATTR \| ε
>
> literal → LIT_RUTA \| LIT_EXT \| LIT_TAMANIO \| LIT_ENTERO \|
> LIT_CADENA condicion → expresion opComparacion expresion
>
> opComparacion → == \| != \| \> \| \< \| \>= \| \<=
>
> **Mecanismo** **de** **match()**:
>
> \- Si el token actual coincide con el esperado, avanza y crea un nodo
> hoja
>
> \- Si no coincide, registra error sintáctico y activa **recuperación**
> **de** **errores** **por** **pánico**
>
> **Recuperación** **de** **errores** **(Panic** **Mode)**:
>
> \- TOKENS_SEGUROS: conjunto de tokens seguros donde reiniciar
> (KW_DONE, LLAVE_CIERRA, KW_ELSE, KW_SCAN, KW_FOR, KW_IF, KW_LET,
> funciones, EOF)
>
> \- Método panico(): salta tokens hasta encontrar uno seguro
>
> \- La bandera panicoUsado evita que se generen errores en cascada

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
7/19

28/5/26, 11:43 p.m.

> **Árbol** **de** **derivación**:
>
> \- NodoArbol: estructura de árbol N-ario
>
> \- Cada nodo tiene un valor (string) y una lista de hijos
>
> \- raizActual en CompiladorUI se usa para dibujar el árbol
>
> **4.2.2** **NodoArbol.java**
>
> Estructura simple de árbol con:
>
> \- valor: texto del nodo (nombre de producción o lexema) - hijos:
> lista de nodos hijo
>
> **4.2.3** **Errores.java** **y** **ErrorCompilacion.java**
>
> Sistema de gestión de errores:
>
> \- Cada error tiene: código numérico, línea, descripción
>
> \- Errores mantiene una lista y consulta TablaErrores para el mensaje
> base
>
> **4.2.4** **TablaErrores.java**
>
> Catálogo de errores con mensajes predefinidos: \| Código \| Constante
> \| Mensaje \|
>
> \|--------\|-----------\|---------\|
>
> \| 1 \| ERROR_INICIAL \| Error de inicio de instrucción \| \| 101 \|
> ERROR_SINTACTICO \| Error sintáctico \|
>
> \| 103 \| ERROR_EXPRESION \| Error en expresión \|
>
> \| 203 \| ERROR_ARGUMENTO_VACIO \| Argumento inválido \| \| 210 \|
> ERROR_TIPO_DATOS \| Error de tipos \|
>
> \| 211 \| ERROR_FUNCION_INVALIDA \| Función no reconocida \|
>
> \| 500 \| ERROR_ACCION_INVALIDA \| Acción u operación no válida \|
>
> **4.3** **CAPA** **DE** **INTERFAZ** **(** **src/ui/)**
>
> **4.3.1** **CompiladorUI.java**
>
> **¿Qué** **es?** Ventana principal del compilador con diseño oscuro
> personalizado.
>
> **Componentes:**
>
> \- **Barra** **superior**: Logo "SORTSCRIPT", botones (Cargar Ejemplo,
> Limpiar, ANALIZAR, Árbol, tema) - **Panel** **izquierdo**: Editor de
> código ( JTextPane) con:

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
8/19

28/5/26, 11:43 p.m.

> \- Sintaxis coloreada en tiempo real (keywords, funciones, números,
> cadenas, comentarios) - Numeración de líneas
>
> \- Filtro de documento que repinta en cada cambio - **Panel**
> **derecho**: JTabbedPane con 4 pestañas:
>
> \- **Tokens**: Tabla con lexema, tipo, línea
>
> \- **Errores** **Léxicos**: Tabla con código, línea, descripción
>
> \- **Errores** **Sintácticos**: Tabla con código, línea, descripción -
> **Consola**: Reporte de compilación
>
> **Flujo** **de** **ejecutarAnalisis()**: 1. Obtener texto del editor
>
> 2\. Limpiar tablas anteriores
>
> 3\. Llamar Tokenizador.tokenizador() → tokens crudos 4. Obtener AFD
> con SortScriptAFD.obtenerAFD()
>
> 5\. Llamar AFD.aceptar() → tokens clasificados + errores léxicos
>
> 6\. Filtrar tokens: excluir los de líneas con errores léxicos (
> lineasConError) 7. Pasar tokens limpios al ParserLL1 → árbol
> sintáctico
>
> 8\. Mostrar resultados en pestañas
>
> **Filtro** **de** **tokens**:
>
> for (Token tk : tokensAnalizados) {
>
> if (!lineasConError.contains(tk.getLinea())) tokensParser.add(tk); }
>
> ¿Por qué? Si una línea tiene un error léxico, todos los tokens de esa
> línea se excluyen del análisis sintáctico para evitar errores en
> cascada. Es una decisión discutible porque si la línea tiene tokens
> válidos junto al error, también se pierden.
>
> **Resaltado** **de** **sintaxis**: Usa 5 expresiones regulares y
> StyledDocument.setCharacterAttributes().
>
> **4.3.2** **PanelArbol.java**
>
> **¿Qué** **es?** Componente JPanel que dibuja el árbol sintáctico
> usando Graphics2D.
>
> **Algoritmo** **de** **dibujo**:
>
> 1\. calcularAncho(): Recursivo, calcula el ancho que necesita el
> subárbol de cada nodo 2. calcularCoordenadas(): Posiciona cada nodo
> centrado sobre sus hijos
>
> 3\. paintComponent(): Dibuja líneas (conexiones) y nodos (círculos) -
> Nodos hoja: verde
>
> \- Nodos internos: azul

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
9/19

28/5/26, 11:43 p.m.

> **4.3.3** **VentanaArbol.java**
>
> Ventana simple que contiene un PanelArbol dentro de un JScrollPane.
>
> **4.3.4** **TextLineNumber.java**
>
> Componente reutilizable de la comunidad Java que pinta números de
> línea al lado del editor.
>
> **5.** **AUTÓMATAS** **FINITOS** **QUE** **SE** **DEBIERON** **USAR**
>
> El AFD actual tiene un problema fundamental: **no** **reconoce**
> **la** **mayoría** **de** **los** **tokens**. Lo que debería haber
> sido un AFD completo terminó siendo un híbrido AFD + regex. A
> continuación presento los autómatas que se debieron diseñar para un
> análisis léxico completo y correcto.
>
> **5.1** **AFD** **COMPLETO** **PARA** **SORTSCRIPT** **(Propuesta**
> **de** **diseño)**
>
> La estrategia correcta es diseñar **múltiples** **sub-autómatas** que
> convergen en un AFD principal, o mejor aún, usar un único AFD con
> estados bien definidos para cada categoría.
>
> **5.1.1** **AFD** **para** **Keywords** **(Palabras** **Reservadas)**
>
> S C A N
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4(SCAN) \[aceptación\] A S
>
> INICIO ───► q1 ───► q2(AS) \[aceptación\] F O R
>
> INICIO ───► q1 ───► q2 ───► q3(FOR) \[aceptación\] E A C H
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4(EACH) \[aceptación\] I N
>
> INICIO ───► q1 ───► q2(IN) \[aceptación\] I F
>
> INICIO ───► q1 ───► q2(IF) \[aceptación\]
>
> E L S E
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4(ELSE) \[aceptación\] L E T
>
> INICIO ───► q1 ───► q2 ───► q3(LET) \[aceptación\] D O N E
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4(DONE) \[aceptación\]

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
10/19

28/5/26, 11:43 p.m.

> **5.1.2** **AFD** **para** **Funciones** **Integradas**
>
> M O V E
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4(MOVE) \[aceptación\] C O P Y
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4(COPY) \[aceptación\]
>
> D E L E T E
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4 ───► q5 ───► q6(DELETE)
> \[aceptación\] R E N A M E
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4 ───► q5 ───► q6(RENAME)
> \[aceptación\] L O G
>
> INICIO ───► q1 ───► q2 ───► q3(LOG) \[aceptación\]
>
> **5.1.3** **AFD** **para** **Atributos**
>
> E X T
>
> INICIO ───► q1 ───► q2 ───► q3(EXT) \[aceptación\] S I Z E
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4(SIZE) \[aceptación\] N A M E
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4(NAME) \[aceptación\] D A T E
>
> INICIO ───► q1 ───► q2 ───► q3 ───► q4(DATE) \[aceptación\]
>
> **5.1.4** **AFD** **para** **Identificadores**
>
> letra/dígito ┌──────────────┐
>
> │ ▼
>
> INICIO ───► q_letra ───► q_id \[aceptación\] │
>
> └──► (sin transición si no es letra ni '\_')
>
> Alfabeto: \[a-zA-Z\_\]\[a-zA-Z0-9\_\]\*
>
> Estado inicial → q_letra (solo si el primer char es letra o \_)
> Transiciones:
>
> q_letra ─letra/dígito/\_─► q_id
>
> q_id ─letra/dígito/\_─► q_id (bucle)
>
> **Importante**: Este AFD debe tener **menor** **prioridad** que el de
> keywords. Es decir, si "scan" podría matchear como identificador, el
> autómata debe reconocerlo primero como keyword. Esto se logra probando
> keywords antes que identificadores.

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
11/19

28/5/26, 11:43 p.m.

> **5.1.5** **AFD** **para** **Literales**
>
> **5.1.5.1** **Ruta** **(** **"...")**
>
> cualquier char excepto " ┌──────────────┐
>
> │ ▼
>
> INICIO ───► q_comilla ───► q_ruta \[aceptación\] (") (si siguiente ")
>
> Estados: INICIO → q_comilla → q_ruta (aceptación) Transiciones:
>
> INICIO ──"──► q_comilla
>
> q_comilla ──(char !=")──► q_comilla (bucle) q_comilla ──"──► q_ruta
> (aceptación)
>
> **5.1.5.2** **Extensión** **(** **.ext)**
>
> letra/dígito(1-6) ┌──────────────┐
>
> │ ▼
>
> INICIO ───► q_punto ───► q_ext ───► q_ext2 \[aceptación\] (.)
> (letra/díg) (letra/díg) opcional
>
> Estados: INICIO → q_punto → q_ext → q_ext2 (aceptación) Transiciones:
>
> INICIO ──.──► q_punto (pero solo si NO estamos en un token donde . es
> operador) q_punto ──\[a-zA-Z0-9\]──► q_ext
>
> q_ext ──\[a-zA-Z0-9\]──► q_ext (hasta 5 más) q_ext ──(fin)──► q_ext2
> (aceptación)
>
> **Nota**: El . presenta una ambigüedad: puede ser operador de acceso (
> file.ext) o inicio de extensión ( .pdf). Se resuelve por contexto: si
> el token completo empieza con . y no hay nada antes, es extensión; si
> . aparece después de un identificador, es operador.
>
> **5.1.5.3** **Tamaño** **(núm** **+** **KB/MB/GB)**
>
> dígitos K/M/G B
>
> INICIO ───► q_num ───► q_num2 ───► q_unidad ───► q_tamano
> \[aceptación\] (dígito) (dígitos) (K\|M\|G) (B)
>
> Transiciones:
>
> INICIO ──\[0-9\]──► q_num
>
> q_num ──\[0-9\]──► q_num (bucle)

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
12/19

28/5/26, 11:43 p.m.

> q_num ──\[KMG\]──► q_unidad
>
> q_unidad ──B──► q_tamano (aceptación)
>
> **5.1.5.4** **Entero**
>
> dígitos ┌──────────────┐ │ ▼
>
> INICIO ───► q_num ───► q_entero \[aceptación\] (dígito)
>
> Transiciones:
>
> INICIO ──\[0-9\]──► q_num
>
> q_num ──\[0-9\]──► q_num (bucle)
>
> q_num ──(fin sin K/M/G)──► q_entero (aceptación)
>
> **5.1.5.5** **Cadena**
>
> cualquier char excepto " ┌──────────────┐
>
> │ ▼
>
> INICIO ───► q_comilla ───► q_cadena \[aceptación\]
>
> (") (si siguiente ") pero sin más restricciones
>
> **5.1.6** **AFD** **para** **Operadores**
>
> Cada operador es una secuencia fija de 1 o 2 caracteres:
>
> INICIO ──=──► q_asig \[OP_ASIG, aceptación\]
>
> INICIO ──=──► q_igual1 ──=──► q_igual \[OP_IGUAL, aceptación\] INICIO
> ──!──► q_dist1 ──=──► q_dist \[OP_DISTINTO, aceptación\] INICIO
> ──\>──► q_mayor \[OP_MAYOR, aceptación\]
>
> INICIO ──\>──► q_mayoreq1 ──=──► q_mayoreq \[OP_MAYOR_EQ, aceptación\]
> INICIO ──\<──► q_menor \[OP_MENOR, aceptación\]
>
> INICIO ──\<──► q_menoreq1 ──=──► q_menoreq \[OP_MENOR_EQ, aceptación\]
> INICIO ──&──► q_and1 ──&──► q_and \[OP_AND, aceptación\]
>
> INICIO ──\|──► q_or1 ──\|──► q_or \[OP_OR, aceptación\] INICIO ──+──►
> q_suma \[OP_SUMA, aceptación\]
>
> INICIO ──.──► q_punto \[OP_PUNTO, aceptación\] (solo cuando no va
> seguido de letras formando extensión)
>
> **¿Por** **qué** **=** **puede** **ser** **OP_ASIG** **u**
> **OP_IGUAL?** Esto se resuelve porque cuando el tokenizador encuentra
> ==, emite un solo token == de tipo OP_IGUAL. El token = solo se emite
> cuando es un =

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
13/19

28/5/26, 11:43 p.m.

> solitario. El AFD para operadores necesita manejar el "lookahead" de 1
> carácter para distinguir = de ==.
>
> **5.1.7** **AFD** **para** **Delimitadores**
>
> INICIO ──{──► q_llave_abre \[LLAVE_ABRE, aceptación\] INICIO ──}──►
> q_llave_cierra \[LLAVE_CIERRA, aceptación\] INICIO ──(──► q_par_abre
> \[PAR_ABRE, aceptación\]
>
> INICIO ──)──► q_par_cierra \[PAR_CIERRA, aceptación\] INICIO ──,──►
> q_coma \[COMA, aceptación\]
>
> **5.2** **DIAGRAMA** **DEL** **AFD** **COMPLETO** **(unificado)**
>
> ┌──────────────────────────────────────────────┐ │ PALABRAS CLAVE │
>
> │ S→C→A→N(SCAN)
>
> │ E→A→C→H(EACH)

A→S(AS)

I→N(IN)

F→O→R(FOR) │

I→F(IF) │

> │ E→L→S→E(ELSE) L→E→T(LET) D→O→N→E(DONE) │
> └──────────────────────┬───────────────────────┘
>
> │ ┌──────────────────────┐
> ┌─────────────────────────┴──────────────────────┐ │ DELIMITADORES │ │
> INICIO │ │ { → LLAVE_ABRE │ │ │ │ } → LLAVE_CIERRA │ │ ┌─── letra/\_
> ──────────────────────────┐ │ │ ( → PAR_ABRE │ │ │ │ │ │ ) →
> PAR_CIERRA │ │ ▼ ▼ │ │ , → COMA │ │ q_letra
> ─────────────────────────────► q_id │
>
> └──────────────────────┘
>
> ┌──────────────────────┐

│ │ (ID) │ │ │ dígitos │

│ ▼ │

> │ OPERADORES │ │ q_num ──\[0-9\]──► q_num │
>
> │ = → OP_ASIG │ │ │ │ == → OP_IGUAL │ │ │ │ != → OP_DISTINTO │ │ │
>
> │ \> → OP_MAYOR │ │ │

└──(fin)──► q_entero (LIT_ENTERO) │ └──\[K\|M\|G\]──► q_unidad ──B──► │

> q_tam (LIT_TAM) │
>
> │
>
> │ \>= → OP_MAYOR_EQ │ │ \< → OP_MENOR │ │ \<= → OP_MENOR_EQ │ │ && →
> OP_AND │
>
> │ \|\| → OP_OR │

│ └─── " ──► q_comilla ──"──► │ │ ┌──(bucle chars)──┘ │ │ ├──► q_ruta
(si coincide patrón) │ │ └──► q_cadena (si no) │

│ │

> │ ! → OP_NOT │ + → OP_SUMA
>
> │ . → OP_PUNTO

│ └────────────────────────────────────────────────┘ │

│

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
14/19

28/5/26, 11:43 p.m.

> │ (contextual) │ └──────────────────────┘
>
> **5.3** **PROPUESTA** **DE** **IMPLEMENTACIÓN** **DEL** **AFD**
> **COMPLETO**
>
> Para implementar correctamente este AFD unificado, se necesitaría:
>
> public class SortScriptAFDCompleto {
>
> public static AFD obtenerAFDCompleto() { // 1. Definir alfabeto
> completo
>
> Set\<Character\> alfabeto = new HashSet\<\>();
>
> for (char c = 'A'; c \<= 'Z'; c++) alfabeto.add(c); for (char c = 'a';
> c \<= 'z'; c++) alfabeto.add(c); for (char c = '0'; c \<= '9'; c++)
> alfabeto.add(c);
>
> "={}()\<\>!&\|+-.,;:\\' \t\n\r\_/".chars().forEach(c -\>
> alfabeto.add((char)c));
>
> // 2. Definir estados
>
> Set\<String\> estados = new HashSet\<\>(); estados.add("INICIO");
>
> // ... agregar todos los estados necesarios
>
> // 3. Definir transiciones (ejemplo para keywords)
>
> Map\<String, Map\<Character, String\>\> transiciones = new
> HashMap\<\>();
>
> // Keywords: cada letra es un estado String\[\]\[\] palabras = {
>
> {"SCAN", "KW_SCAN"}, {"AS", "KW_AS"}, {"FOR", "KW_FOR"}, {"EACH",
> "KW_EACH"}, {"IN", "KW_IN"}, {"IF", "KW_IF"}, {"ELSE", "KW_ELSE"},
> {"LET", "KW_LET"}, {"DONE", "KW_DONE"}, {"MOVE", "FUNC_MOVE"},
> {"COPY", "FUNC_COPY"},
>
> {"DELETE", "FUNC_DELETE"}, {"RENAME", "FUNC_RENAME"}, {"LOG",
> "FUNC_LOG"},
>
> {"EXT", "ATTR_EXT"}, {"SIZE", "ATTR_SIZE"}, {"NAME", "ATTR_NAME"},
> {"DATE", "ATTR_DATE"}
>
> };
>
> for (String\[\] palabra : palabras) { String nombre = palabra\[0\];
> String tipo = palabra\[1\]; String prevEstado = "INICIO";
>
> for (int i = 0; i \< nombre.length(); i++) { char c =
> nombre.charAt(i);
>
> String estadoActual = nombre.substring(0, i + 1) + "\_" + tipo;
> estados.add(estadoActual);

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
15/19

28/5/26, 11:43 p.m.

> transiciones.computeIfAbsent(prevEstado, k -\> new HashMap\<\>())
> .put(c, estadoActual);
>
> prevEstado = estadoActual; }
>
> estadosAceptacion.add(prevEstado); }
>
> // 4. Operadores de 2 caracteres (prioridad sobre 1 caracter)
> String\[\]\[\] operadores = {
>
> {"==", "OP_IGUAL"}, {"!=", "OP_DISTINTO"}, {"\>=", "OP_MAYOR_EQ"},
> {"\<=", "OP_MENOR_EQ"}, {"&&", "OP_AND"}, {"\|\|", "OP_OR"}
>
> };
>
> // Similar lógica de transiciones...
>
> // 5. Operadores de 1 carácter agregarTransicion(transiciones,
> "INICIO", '=', "OP_ASIG"); agregarTransicion(transiciones, "INICIO",
> '\>', "OP_MAYOR"); agregarTransicion(transiciones, "INICIO", '\<',
> "OP_MENOR"); agregarTransicion(transiciones, "INICIO", '!', "OP_NOT");
> agregarTransicion(transiciones, "INICIO", '+', "OP_SUMA");
> agregarTransicion(transiciones, "INICIO", '.', "OP_PUNTO");
>
> // 6. Delimitadores
>
> agregarTransicion(transiciones, "INICIO", '{', "LLAVE_ABRE");
> agregarTransicion(transiciones, "INICIO", '}', "LLAVE_CIERRA");
> agregarTransicion(transiciones, "INICIO", '(', "PAR_ABRE");
> agregarTransicion(transiciones, "INICIO", ')', "PAR_CIERRA");
> agregarTransicion(transiciones, "INICIO", ',', "COMA");
>
> // 7. Identificadores y literales requieren manejo de bucles //
> (tienen transiciones que se repiten en el mismo estado)
>
> return new AFD(estados, alfabeto, transiciones, "INICIO",
> estadosAceptacion); }
>
> }
>
> **5.4** **AUTÓMATA** **PARA** **EL** **PARSER** **LL(1)**
>
> El parser LL(1) implementa una gramática que puede representarse como
> un autómata de pila (PDA - Pushdown Automaton). A diferencia del AFD
> (que solo reconoce tokens individuales), el PDA reconoce la estructura
> jerárquica del programa.
>
> **Gramática** **en** **formato** **BNF:**

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
16/19

28/5/26, 11:43 p.m.

> \<programa\> \<bloque\> \<sentencia\> \<scan\> \<for\>
>
> \<if\> \<let\> \<funcCall\>
>
> \<argumentos\> \<expresion\> \<termino\> \<literal\> \<condicion\>
>
> \<opComp\>

::= \<bloque\> "done" "(" ")" EOF ::= \<sentencia\>\*

::= \<scan\> \| \<for\> \| \<if\> \| \<let\> \| \<funcCall\> ::= "scan"
LIT_RUTA "as" IDENTIFICADOR

::= "for" "each" IDENTIFICADOR "in" IDENTIFICADOR "{" \<bloque\> "}" ::=
"if" \<condicion\> "{" \<bloque\> "}" \[ "else" "{" \<bloque\> "}" \]
::= "let" IDENTIFICADOR "=" \<expresion\>

::= FUNC "(" \<argumentos\> ")"

::= \<expresion\> ( "," \<expresion\> )\* \| ε ::= \<termino\> ( "+"
\<termino\> )\*

::= IDENTIFICADOR \[ "." ATTR \] \| \<literal\> \| "(" \<expresion\> ")"
::= LIT_RUTA \| LIT_EXT \| LIT_TAMANIO \| LIT_ENTERO \| LIT_CADENA ::=
\<expresion\> \<opComp\> \<expresion\>

::= "==" \| "!=" \| "\>" \| "\<" \| "\>=" \| "\<="

> **Tabla** **de** **parsing** **LL(1)** **simplificada:**

||
||
||
||
||
||
||
||

> **6.** **CORRECCIONES** **Y** **MEJORAS** **RECOMENDADAS**
>
> **6.1** **Problemas** **Detectados**

||
||
||
||
||
||
||
||
||

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
17/19

28/5/26, 11:43 p.m.

||
||
||
||

> **6.2** **Mejoras** **Sugeridas**
>
> 1\. **AFD** **completo**: Implementar el AFD propuesto en la sección
> 5.3
>
> 2\. **Análisis** **semántico**: Agregar tabla de símbolos para
> verificar declaraciones y tipos
>
> 3\. **Generación** **de** **código**: Traducir SortScript a comandos
> reales del sistema operativo (Java NIO)
>
> 4\. **Lista** **dinámica**: Reemplazar Token\[10000\] por
> List\<Token\>
>
> 5\. **Filtrado** **selectivo**: Excluir solo los tokens específicos
> que fallaron, no toda la línea 6. **Pruebas** **unitarias**: Agregar
> tests para cada fase
>
> **7.** **RESUMEN** **DEL** **FLUJO** **COMPLETO**
>
> 1\. USUARIO ESCRIBE CÓDIGO │
>
> 2\. PRESIONA "ANALIZAR" │
>
> 3\. Tokenizador.tokenizador() │
>
> 4\. AFD.aceptar(tokens) │
>
> 5\. Filtro de tokens │
>
> 6\. ParserLL1.inicio() │
>
> 7\. Muestra resultados │
>
> 8\. Usuario puede ver árbol

Editor con resaltado

btnAnalizar

Elimina comentarios, genera tokens crudos

Clasifica cada token, detecta errores léxicos

Excluye tokens inválidos del análisis

Construye árbol sintáctico, detecta errores

Tablas: Tokens, Errores léxicos, Errores sintácticos

VentanaArbol con PanelArbol

> **Estado** **actual**: El compilador llega hasta el análisis
> sintáctico y construye el árbol de derivación, pero **no** **ejecuta**
> **las** **acciones** (no mueve/copia/elimina archivos realmente). Es
> un analizador, no un compilador completo.
>
> **8.** **CONCLUSIÓN**

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
18/19

28/5/26, 11:43 p.m.

> SortScript Compilador es un proyecto educativo que implementa
> correctamente las fases de análisis léxico y sintáctico de un
> compilador, con una interfaz gráfica atractiva y funcional. La
> principal debilidad es que el AFD es incompleto (solo reconoce
> palabras clave), delegando el resto del reconocimiento a un sistema de
> respaldo basado en regex. Un rediseño del AFD para cubrir todos los
> tipos de token mejoraría la solidez teórica y práctica del compilador.
>
> *Documento* *generado* *el* *28* *de* *mayo* *de* *2026*

file:///C:/Users/asahe/OneDrive/Documentos/COMPILADOR/SortScript---Compilador/analisis_completo.html
19/19
