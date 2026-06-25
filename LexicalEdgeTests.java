import lexico.*;
public class LexicalEdgeTests {
    static int passed = 0, failed = 0;

    static void check(String label, String lexema, TipoToken esperado) {
        TipoToken actual = AFD.determinarTipoLexema(lexema);
        if (actual == esperado) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + label + " (\"" + lexema + "\") => " + actual + " (esperado " + esperado + ")");
        }
    }

    public static void main(String[] args) {
        // === OPERADORES (deben ser exactos) ===
        check("OP_IGUAL", "==", TipoToken.OP_IGUAL);
        check("OP_DISTINTO", "!=", TipoToken.OP_DISTINTO);
        check("OP_MAYOR_EQ", ">=", TipoToken.OP_MAYOR_EQ);
        check("OP_MENOR_EQ", "<=", TipoToken.OP_MENOR_EQ);
        check("OP_MAYOR", ">", TipoToken.OP_MAYOR);
        check("OP_MENOR", "<", TipoToken.OP_MENOR);
        check("OP_AND", "&&", TipoToken.OP_AND);
        check("OP_OR", "||", TipoToken.OP_OR);
        check("OP_NOT", "!", TipoToken.OP_NOT);
        check("OP_ASIG", "=", TipoToken.OP_ASIG);
        check("OP_SUMA", "+", TipoToken.OP_SUMA);
        check("OP_PUNTO", ".", TipoToken.OP_PUNTO);
        check("LLAVE_ABRE", "{", TipoToken.LLAVE_ABRE);
        check("LLAVE_CIERRA", "}", TipoToken.LLAVE_CIERRA);
        check("PAR_ABRE", "(", TipoToken.PAR_ABRE);
        check("PAR_CIERRA", ")", TipoToken.PAR_CIERRA);
        check("COMA", ",", TipoToken.COMA);

        // === CADENAS / RUTAS ===
        // Rutas válidas (deben ser LIT_RUTA)
        check("ruta abs windows", "\"C:\\Users\\file.txt\"", TipoToken.LIT_RUTA);
        check("ruta abs unix", "\"/home/user/file.txt\"", TipoToken.LIT_RUTA);
        check("ruta relativa", "\"folder/file.txt\"", TipoToken.LIT_RUTA);
        check("ruta relativa backslash", "\"folder\\file.txt\"", TipoToken.LIT_RUTA);
        check("ruta solo drive con contenido", "\"C:\\Users\"", TipoToken.LIT_RUTA);
        check("ruta punto", "\"./file.txt\"", TipoToken.LIT_RUTA);
        check("ruta punto doble", "\"../file.txt\"", TipoToken.LIT_RUTA);

        // Rutas inválidas (DEBEN SER DESCONOCIDO)
        check("ruta termina /", "\"/path/\"", TipoToken.DESCONOCIDO);
        check("ruta termina \\", "\"C:\\Users\\\"", TipoToken.DESCONOCIDO);
        check("ruta termina solo /", "\"/\"", TipoToken.DESCONOCIDO);
        check("ruta termina solo \\", "\"C:\\\"", TipoToken.LIT_RUTA);
        check("ruta doble //", "\"//\"", TipoToken.DESCONOCIDO);
        check("ruta doble \\\\", "\"\\\\\"", TipoToken.DESCONOCIDO);
        check("ruta solo drive C:", "\"C:\"", TipoToken.DESCONOCIDO);

        // Cadenas literales normales (deben ser LIT_CADENA)
        check("string simple", "\"hola\"", TipoToken.LIT_CADENA);
        check("string con espacios", "\"hello world\"", TipoToken.LIT_CADENA);
        check("string un caracter", "\"a\"", TipoToken.LIT_CADENA);
        check("string dos caracteres", "\"ab\"", TipoToken.LIT_CADENA);
        check("string vacio", "\"\"", TipoToken.LIT_CADENA);
        check("string numerico", "\"123\"", TipoToken.LIT_CADENA);
        check("string url", "\"https://example.com\"", TipoToken.LIT_CADENA);
        check("string con punto", "\"version 1.2\"", TipoToken.LIT_CADENA);
        check("string nombre archivo sin ruta", "\"file.txt\"", TipoToken.LIT_CADENA);
        check("string comment-like con asterisco", "\"/* not a comment */\"", TipoToken.LIT_CADENA);
        check("string wildcard en contenido", "\"folder/*.txt\"", TipoToken.LIT_CADENA);
        check("string con comodines", "\"[backup]\"", TipoToken.LIT_CADENA);
        check("string con pipe", "\"a|b\"", TipoToken.LIT_CADENA);
        check("string ruta con guion", "\"backup-2023/file.txt\"", TipoToken.LIT_RUTA);
        check("string ruta con espacios", "\"C:\\My Files\\doc.txt\"", TipoToken.LIT_RUTA);

        // Cadenas inválidas (no cumplen formato)
        check("string sin cerrar", "\"abc", TipoToken.DESCONOCIDO);
        check("string solo comilla apertura", "\"", TipoToken.DESCONOCIDO);

        // === EXTENSIONES ===
        check("ext pdf", ".pdf", TipoToken.LIT_EXT);
        check("ext jpg", ".jpg", TipoToken.LIT_EXT);
        check("ext txt", ".txt", TipoToken.LIT_EXT);
        check("ext raro pero valido", ".sqlite", TipoToken.LIT_EXT);
        check("ext invalida", ".xyz", TipoToken.DESCONOCIDO);
        check("ext muy larga", ".abcdefgh", TipoToken.DESCONOCIDO);

        // === TAMAÑOS ===
        check("tam KB", "100KB", TipoToken.LIT_TAMANIO);
        check("tam MB", "500MB", TipoToken.LIT_TAMANIO);
        check("tam GB", "1GB", TipoToken.LIT_TAMANIO);
        check("tam 0", "0KB", TipoToken.LIT_TAMANIO);
        check("tam lowercase kb", "100kb", TipoToken.LIT_TAMANIO);
        check("tam lowercase mb", "500mb", TipoToken.LIT_TAMANIO);
        check("tam lowercase gb", "1gb", TipoToken.LIT_TAMANIO);
        check("tam mixed Kb", "100Kb", TipoToken.LIT_TAMANIO);
        check("tam unidad sola", "KB", TipoToken.IDENTIFICADOR);
        check("tam unidad invalida", "100K", TipoToken.DESCONOCIDO);

        // === ENTEROS ===
        check("entero simple", "123", TipoToken.LIT_ENTERO);
        check("entero cero", "0", TipoToken.LIT_ENTERO);
        check("entero leading zeros", "00123", TipoToken.LIT_ENTERO);

        // === IDENTIFICADORES ===
        check("id simple", "variable", TipoToken.IDENTIFICADOR);
        check("id con nums", "var123", TipoToken.IDENTIFICADOR);
        check("id underscore start", "_tmp", TipoToken.IDENTIFICADOR);
        check("id underscore medio", "my_var", TipoToken.IDENTIFICADOR);

        // === DESCONOCIDO ===
        check("desconocido num start", "1abc", TipoToken.DESCONOCIDO);
        check("desconocido guion", "my-var", TipoToken.DESCONOCIDO);
        check("desconocido arroba", "@", TipoToken.DESCONOCIDO);
        check("desconocido hashtag", "#", TipoToken.DESCONOCIDO);
        check("desconocido dollar", "$", TipoToken.DESCONOCIDO);
        check("desconocido pipe singular", "|", TipoToken.DESCONOCIDO);
        check("desconocido asterisco", "*", TipoToken.DESCONOCIDO);
        check("desconocido interrogacion", "?", TipoToken.DESCONOCIDO);

        System.out.println("\n=== RESULTADOS ===");
        System.out.println("Pasadas: " + passed + " Falladas: " + failed);
        if (failed > 0) System.exit(1);
    }
}
