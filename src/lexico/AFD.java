package lexico;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AFD {

    // ─────────────────────────────────────────────────────────────
    // Lista de extensiones de archivo que SortScript reconoce como
    // tokens válidos de tipo LIT_EXT. Ej: ".pdf", ".mp3", ".java"
    // ─────────────────────────────────────────────────────────────
    private static final Set<String> EXTENSIONES_VALIDAS = Set.of(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "csv", "html", "htm", "xml", "json", "md", "rtf", "odt",
        "jpg", "jpeg", "png", "gif", "bmp", "svg", "tif", "tiff", "ico", "webp",
        "psd", "ai", "eps",
        "mp3", "wav", "flac", "ogg", "wma", "aac", "m4a",
        "mp4", "avi", "mkv", "mov", "wmv", "flv", "mpg", "mpeg", "webm", "3gp",
        "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso",
        "exe", "msi", "dll", "bat", "cmd", "sh", "app", "deb", "rpm",
        "java", "py", "js", "ts", "c", "cpp", "h", "hpp", "cs", "rb",
        "php", "sql", "swift", "kt", "go", "rs", "scala",
        "lnk", "url", "tmp", "log", "bak", "dat", "db", "sqlite", "dmg"
    );

    // ─────────────────────────────────────────────────────────────
    // Constantes para identificar a qué categoría pertenece
    // un estado del autómata. Se usan para generar mensajes
    // de error más descriptivos (ej. "keyword incompleta").
    // ─────────────────────────────────────────────────────────────
    private static final int CAT_KW = 0; // Keyword   → ej. sort, let, if
    private static final int CAT_FN = 1; // Function  → ej. move(), copy()
    private static final int CAT_AT = 2; // Attribute → ej. .name, .size

    // ─────────────────────────────────────────────────────────────
    // Los 5 componentes clásicos de un Autómata Finito Determinista
    // ─────────────────────────────────────────────────────────────
    private Set<String> estados;           // Todos los estados posibles del AFD
    private Set<Character> alfabeto;       // Caracteres que el AFD puede procesar
    private Map<String, Map<Character, String>> transiciones; // Tabla de transiciones:
                                           // dado un estado + carácter → siguiente estado
    private String estadoInicial;          // Estado desde donde arranca cada análisis
    private Set<String> estadosAceptacion; // Estados que indican un token válido y completo

    // ─────────────────────────────────────────────────────────────
    // Agrupaciones de estados por categoría.
    // Permiten saber si un estado pertenece a keywords,
    // funciones o atributos para dar errores precisos.
    // ─────────────────────────────────────────────────────────────
    private Set<String> kwStates;   // Estados correspondientes a palabras clave
    private Set<String> funcStates; // Estados correspondientes a funciones
    private Set<String> attrStates; // Estados correspondientes a atributos

    // Objeto que acumula todos los errores léxicos encontrados durante el análisis
    private parser.Errores erroresLexicos = new parser.Errores();


    // ─────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // Recibe todos los componentes del AFD ya construidos
    // desde afuera y los asigna a los atributos internos.
    // ─────────────────────────────────────────────────────────────
    public AFD(Set<String> estados, Set<Character> alfabeto,
               Map<String, Map<Character, String>> transiciones,
               String estadoInicial, Set<String> estadosAceptacion,
               Set<String> kwStates, Set<String> funcStates, Set<String> attrStates) {
        this.estados = estados;
        this.alfabeto = alfabeto;
        this.transiciones = transiciones;
        this.estadoInicial = estadoInicial;
        this.estadosAceptacion = estadosAceptacion;
        this.kwStates = kwStates;
        this.funcStates = funcStates;
        this.attrStates = attrStates;
    }

    // Getter para obtener los errores léxicos acumulados tras el análisis
    public parser.Errores getErrores() { return erroresLexicos; }


    // ─────────────────────────────────────────────────────────────
    // MÉTODO PRINCIPAL: aceptar()
    // Recibe tokens "crudos" (solo lexema + línea) y devuelve
    // la misma lista con cada token ya clasificado con su TipoToken.
    // ─────────────────────────────────────────────────────────────
    public List<Token> aceptar(Token[] tokens) {
        erroresLexicos = new parser.Errores(); // Reinicia el registro de errores
        List<Token> resultados = new ArrayList<>();

        for (Token tk : tokens) {
            String lexemaOriginal = tk.getLexema();
            String lexemaUpper = lexemaOriginal.toUpperCase(); // Ignora mayúsculas/minúsculas
            String estadoActual = estadoInicial;               // Reinicia el autómata para cada token
            boolean error = false;
            boolean pasoPorAceptacion = false;       // ¿Alguna vez llegamos a un estado de aceptación?
            String ultimoEstadoAceptacion = null;    // Último estado de aceptación que tocamos

            // ─────────────────────────────────────────────────────
            // Recorre el token carácter por carácter simulando el AFD
            // ─────────────────────────────────────────────────────
            for (char simbolo : lexemaUpper.toCharArray()) {

                // ¿El carácter no existe en el alfabeto del lenguaje?
                // → Error inmediato, no tiene sentido seguir procesando
                if (!alfabeto.contains(simbolo)) {
                    resultados.add(new Token(lexemaOriginal, tk.getLinea(),
                        TipoToken.DESCONOCIDO, estadoActual, false));
                    erroresLexicos.agregarError(parser.TablaErrores.ERROR_SIMBOLO_ALFABETO, tk.getLinea(),
                        "Error Léxico: Símbolo '" + simbolo + "' no definido en el alfabeto.");
                    error = true;
                    break;
                }

                // Obtiene las transiciones disponibles desde el estado actual
                Map<Character, String> transicionesEstado = transiciones.get(estadoActual);

                // ¿No hay ninguna transición para este carácter desde el estado actual?
                // → El autómata se "atascó", el token no puede continuar
                if (transicionesEstado == null || !transicionesEstado.containsKey(simbolo)) {

                    // Intenta clasificar el token por su forma (operador, literal, etc.)
                    TipoToken tipoDetectado = determinarTipoLexema(lexemaOriginal);

                    // Si antes pasamos por un estado de aceptación pero luego falló
                    // → el token empezó bien pero tiene caracteres de más (MAL FORMADO)
                    if (pasoPorAceptacion && tipoDetectado == TipoToken.DESCONOCIDO) {
                        int cat = determinarCategoria(ultimoEstadoAceptacion);
                        int codigo = cat >= 0 ? codigoMalformado(cat) : codigoNoReconocido(lexemaOriginal);
                        String catNombre = cat == CAT_KW ? "palabra clave" :
                                           cat == CAT_FN ? "función" :
                                           cat == CAT_AT ? "atributo" : "";
                        erroresLexicos.agregarError(codigo, tk.getLinea(),
                            "Error Léxico: " + catNombre + " mal formada '" + lexemaOriginal + "'.");

                    // Si nunca pasó por aceptación y tampoco es un literal conocido
                    // → el token simplemente NO EXISTE en el lenguaje
                    } else if (tipoDetectado == TipoToken.DESCONOCIDO) {
                        int codigo = codigoNoReconocido(lexemaOriginal);
                        erroresLexicos.agregarError(codigo, tk.getLinea(),
                            "Error Léxico: Token no reconocido '" + lexemaOriginal + "'.");
                    }

                    // Agrega el token con lo que se pudo detectar (o DESCONOCIDO)
                    resultados.add(new Token(lexemaOriginal, tk.getLinea(),
                        tipoDetectado, estadoActual, tipoDetectado != TipoToken.DESCONOCIDO));
                    error = true;
                    break;
                }

                // Transición válida: avanza al siguiente estado
                estadoActual = transicionesEstado.get(simbolo);

                // Si el nuevo estado es de aceptación, lo memoriza
                // (útil para detectar tokens mal formados más adelante)
                if (estadosAceptacion.contains(estadoActual)) {
                    pasoPorAceptacion = true;
                    ultimoEstadoAceptacion = estadoActual;
                }
            }

            // ─────────────────────────────────────────────────────
            // Si se procesaron todos los caracteres sin ningún error:
            // → Determina el tipo final según el estado en que terminó
            // ─────────────────────────────────────────────────────
            if (!error) {
                TipoToken tipo = tipoPR(estadoActual);

                if (tipo == TipoToken.DESCONOCIDO) {
                    // Caso especial: una sola letra suelta es un IDENTIFICADOR válido
                    if (lexemaOriginal.length() == 1 && Character.isLetter(lexemaOriginal.charAt(0))) {
                        tipo = TipoToken.IDENTIFICADOR;
                    } else {
                        // Terminó en estado intermedio → token INCOMPLETO
                        // Ej: escribió "sor" en lugar de "sort"
                        int cat = determinarCategoria(estadoActual);
                        int codigo = cat >= 0 ? codigoIncompleto(cat) : parser.TablaErrores.ERROR_TOKEN_NO_RECONOCIDO;
                        String catNombre = cat == CAT_KW ? "palabra clave" :
                                           cat == CAT_FN ? "función" :
                                           cat == CAT_AT ? "atributo" : "reservada";
                        erroresLexicos.agregarError(codigo, tk.getLinea(),
                            "Error Léxico: " + catNombre + " incompleta '" + lexemaOriginal + "'.");
                    }
                }

                // Agrega el token clasificado a la lista final
                resultados.add(new Token(lexemaOriginal, tk.getLinea(),
                    tipo, estadoActual, tipo != TipoToken.DESCONOCIDO));
            }
        }
        return resultados; // Lista completa de tokens clasificados
    }


    // ─────────────────────────────────────────────────────────────
    // Determina a qué categoría pertenece un estado del autómata.
    // Se usa para dar mensajes de error más descriptivos.
    // Devuelve: CAT_KW, CAT_FN, CAT_AT, o -1 si no pertenece a ninguna.
    // ─────────────────────────────────────────────────────────────
    private int determinarCategoria(String estado) {
        if (kwStates.contains(estado))   return CAT_KW;
        if (funcStates.contains(estado)) return CAT_FN;
        if (attrStates.contains(estado)) return CAT_AT;
        return -1; // No pertenece a ninguna categoría conocida
    }

    // ─────────────────────────────────────────────────────────────
    // Devuelve el código de error correcto cuando un token está
    // INCOMPLETO (le faltan caracteres al final).
    // Ej: "sor" en lugar de "sort"
    // ─────────────────────────────────────────────────────────────
    private int codigoIncompleto(int cat) {
        switch (cat) {
            case CAT_KW: return parser.TablaErrores.ERROR_KW_INCOMPLETA;
            case CAT_FN: return parser.TablaErrores.ERROR_FN_INCOMPLETA;
            case CAT_AT: return parser.TablaErrores.ERROR_ATTR_INCOMPLETO;
            default:     return parser.TablaErrores.ERROR_TOKEN_NO_RECONOCIDO;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Devuelve el código de error correcto cuando un token está
    // MAL FORMADO (empezó bien pero tiene caracteres incorrectos).
    // Ej: "so3t" en lugar de "sort"
    // ─────────────────────────────────────────────────────────────
    private int codigoMalformado(int cat) {
        switch (cat) {
            case CAT_KW: return parser.TablaErrores.ERROR_KW_MALFORMADA;
            case CAT_FN: return parser.TablaErrores.ERROR_FN_MALFORMADA;
            case CAT_AT: return parser.TablaErrores.ERROR_ATTR_MALFORMADO;
            default:     return parser.TablaErrores.ERROR_TOKEN_NO_RECONOCIDO;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Devuelve el código de error cuando el token no se reconoce
    // en absoluto. Distingue si empieza con número (literal mal
    // formado) o con letra (token desconocido).
    // ─────────────────────────────────────────────────────────────
    private int codigoNoReconocido(String lexema) {
        if (lexema.matches("^[0-9].*")) return parser.TablaErrores.ERROR_LITERAL_MALFORMADO;
        if (lexema.matches("^[a-zA-Z_].*")) return parser.TablaErrores.ERROR_TOKEN_NO_RECONOCIDO;
        return parser.TablaErrores.ERROR_TOKEN_NO_RECONOCIDO;
    }


    // ─────────────────────────────────────────────────────────────
    // Traduce el estado final del AFD al TipoToken correspondiente.
    // Prueba los tres prefijos posibles: KW_, FUNC_, ATTR_
    // Ej: estado "SORT" → busca "KW_SORT" en el enum → ✅ encontrado
    // Si ningún prefijo coincide → devuelve DESCONOCIDO
    // ─────────────────────────────────────────────────────────────
    public TipoToken tipoPR(String estadoActual) {
        try {
            return TipoToken.valueOf("KW_" + estadoActual);   // ¿Es una keyword?
        } catch (IllegalArgumentException e) {
            try {
                return TipoToken.valueOf("FUNC_" + estadoActual); // ¿Es una función?
            } catch (IllegalArgumentException e2) {
                try {
                    return TipoToken.valueOf("ATTR_" + estadoActual); // ¿Es un atributo?
                } catch (IllegalArgumentException e3) {
                    return TipoToken.DESCONOCIDO; // No coincide con ninguna categoría
                }
            }
        }
    }

    // Patrón de caracteres válidos dentro de una ruta de archivo
    private static final String CHARS_RUTA = "[a-zA-Z0-9_ .:\\-/\\\\]";

    // ─────────────────────────────────────────────────────────────
    // Clasifica tokens especiales SIN usar el autómata,
    // basándose únicamente en la forma/estructura del lexema.
    // Cubre: operadores, cadenas, rutas, extensiones, números, identificadores.
    // ─────────────────────────────────────────────────────────────
    public static TipoToken determinarTipoLexema(String lexema) {
        if (lexema == null || lexema.isEmpty()) return TipoToken.DESCONOCIDO;

        // ── Operadores y símbolos: comparación directa ──────────
        switch (lexema) {
            case "==": return TipoToken.OP_IGUAL;      // Igual a
            case "!=": return TipoToken.OP_DISTINTO;   // Distinto de
            case ">=": return TipoToken.OP_MAYOR_EQ;   // Mayor o igual
            case "<=": return TipoToken.OP_MENOR_EQ;   // Menor o igual
            case ">":  return TipoToken.OP_MAYOR;      // Mayor que
            case "<":  return TipoToken.OP_MENOR;      // Menor que
            case "&&": return TipoToken.OP_AND;        // AND lógico
            case "||": return TipoToken.OP_OR;         // OR lógico
            case "!":  return TipoToken.OP_NOT;        // Negación
            case "=":  return TipoToken.OP_ASIG;       // Asignación
            case "+":  return TipoToken.OP_SUMA;       // Suma / concatenación
            case ".":  return TipoToken.OP_PUNTO;      // Acceso a atributo
            case "{":  return TipoToken.LLAVE_ABRE;    // Apertura de bloque
            case "}":  return TipoToken.LLAVE_CIERRA;  // Cierre de bloque
            case "(":  return TipoToken.PAR_ABRE;      // Apertura de paréntesis
            case ")":  return TipoToken.PAR_CIERRA;    // Cierre de paréntesis
            case ",":  return TipoToken.COMA;          // Separador de argumentos
        }

        // ── Cadenas de texto y rutas de archivo ─────────────────
        // Todo lo que empiece y termine con comillas dobles
        if (lexema.startsWith("\"") && lexema.endsWith("\"") && lexema.length() >= 2) {
            String inner = lexema.substring(1, lexema.length() - 1); // Quita las comillas

            // Cadena vacía "" → es válida como LIT_CADENA
            if (inner.isEmpty()) return TipoToken.LIT_CADENA;

            boolean tieneSeparador = inner.contains("/") || inner.contains("\\");
            boolean soloCharsRuta  = inner.matches("^" + CHARS_RUTA + "+$");

            // Caso "C:\" o "C:/" → raíz de unidad → LIT_RUTA válida
            if (inner.matches("^[a-zA-Z]:[\\\\/]$")) {
                return TipoToken.LIT_RUTA;
            }

            // Caso "C:" solo (sin separador) → inválido
            if (inner.matches("^[a-zA-Z]:$")) {
                return TipoToken.DESCONOCIDO;
            }

            // Si tiene separadores y solo caracteres válidos de ruta:
            if (tieneSeparador && soloCharsRuta) {

                // Si parece una URL (http://, ftp://, etc.) → es LIT_CADENA, no ruta
                if (inner.matches("^[a-zA-Z][a-zA-Z0-9]+://.*$")) {
                    return TipoToken.LIT_CADENA;
                }

                // Rutas válidas: "C:\carpeta\archivo", "/home/user", "carpeta/archivo"
                // Condición: no debe terminar en \ o / (ruta incompleta)
                if ((inner.matches("^[a-zA-Z]:[\\\\/].+$") && !inner.endsWith("\\") && !inner.endsWith("/")) ||
                    (inner.matches("^[/\\\\].+$")           && !inner.endsWith("\\") && !inner.endsWith("/")) ||
                    (inner.matches(".+[/\\\\].+$")           && !inner.endsWith("\\") && !inner.endsWith("/"))) {
                    return TipoToken.LIT_RUTA;
                }

                // Tiene separadores pero no cumple el formato → inválido
                return TipoToken.DESCONOCIDO;
            }

            // Sin separadores y entre comillas → texto normal
            return TipoToken.LIT_CADENA;
        }

        // ── Extensiones de archivo ───────────────────────────────
        // Empieza con punto y está en la lista de extensiones válidas
        // Ej: ".pdf", ".mp3", ".java" → LIT_EXT
        if (lexema.startsWith(".") && lexema.length() >= 2 && lexema.length() <= 7) {
            if (EXTENSIONES_VALIDAS.contains(lexema.substring(1).toLowerCase())) {
                return TipoToken.LIT_EXT;
            }
        }

        // ── Tamaños de archivo ───────────────────────────────────
        // Número seguido de KB, MB o GB (sin distinguir mayúsculas)
        // Ej: "512MB", "2GB", "1024kb" → LIT_TAMANIO
        if (lexema.matches("(?i)^[0-9]+(KB|MB|GB)$")) return TipoToken.LIT_TAMANIO;

        // ── Números enteros ──────────────────────────────────────
        // Solo dígitos, sin letras ni puntos
        // Ej: "42", "1024" → LIT_ENTERO
        if (lexema.matches("^[0-9]+$")) return TipoToken.LIT_ENTERO;

        // ── Identificadores ──────────────────────────────────────
        // Empieza con letra o guión bajo, seguido de letras/dígitos/guión bajo
        // Ej: "miVariable", "_contador", "archivo2" → IDENTIFICADOR
        if (lexema.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return TipoToken.IDENTIFICADOR;
        }

        // Si no encajó en ninguna categoría → token desconocido
        return TipoToken.DESCONOCIDO;
    }
}