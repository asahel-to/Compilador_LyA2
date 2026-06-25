package tests;

import java.util.List;
import lexico.AFD;
import lexico.SortScriptAFD;
import lexico.Token;
import lexico.Tokenizador;
import parser.ParserLL1;
import parser.SemanticAnalyzer;
import parser.NodoArbol;
import parser.Errores;
import parser.ErrorCompilacion;

public class ValidationTests {

    static int passed = 0, failed = 0;

    public static void main(String[] args) {
        System.out.println("=== VALIDACION COMPLETA POR CAPAS ===\n");

        layer1Lexical();
        layer2Syntax();
        layer3Semantic();
        layer4Integration();

        System.out.println("\n=== RESULTADO GLOBAL ===");
        System.out.println("Pasadas: " + passed + " Falladas: " + failed);
        if (failed > 0) System.exit(2);
    }

    // ========================================================================
    // CAPA 1: ANALISIS LEXICO
    // ========================================================================
    private static void layer1Lexical() {
        System.out.println("--- CAPA 1: LEXICO ---");

        // 1.1 Caracteres invalidos producen error lexico
        checkLexError("1.1a", "scan @", true,
            "Caracter '@' deberia producir error lexico");
        checkLexError("1.1b", "scan #", true,
            "Caracter '#' deberia producir error lexico");
        checkLexError("1.1c", "scan $", true,
            "Caracter '$' deberia producir error lexico");

        // 1.2 Tokens validos NO producen error lexico
        checkLexError("1.2a", "scan \"/path\" as x\ndone()\n", false,
            "Programa valido sin errores lexicos");
        checkLexError("1.2b", "scan \"/path\" as tmp\nfor each x in tmp {}\ndone()\n", false,
            "Extension word 'tmp' como identificador");
        checkLexError("1.2c", "scan \"/path\" as x\nlet y = 123\ndone()\n", false,
            "Numero entero valido");
        checkLexError("1.2d", "scan \"/path\" as x\nlet y = 100MB\ndone()\n", false,
            "Tamanio valido (100MB)");
        checkLexError("1.2e", "scan \"/path\" as x\nlet y = .pdf\ndone()\n", false,
            "Extension valida (.pdf)");

        // 1.3 Comillas dobles sin cerrar — el tokenizador no produce error,
        //     pero verifica que no cause excepcion
        checkLexNoCrash("1.3a", "scan \"/path\" as x\nlog(\"hello)\ndone()\n",
            "String sin cerrar no debe causar excepcion");

        // 1.4 Comentario de bloque sin cerrar — no debe causar excepcion
        checkLexNoCrash("1.4a", "scan \"/path\" as x\n/* comentario\nlog(\"ok\")\ndone()\n",
            "Comentario sin cerrar no debe causar excepcion");

        System.out.println();
    }

    // ========================================================================
    // CAPA 2: ANALISIS SINTACTICO
    // ========================================================================
    private static void layer2Syntax() {
        System.out.println("--- CAPA 2: SINTACTICO ---");

        // 2.1 Programa valido minimo -> 0 errores
        checkSynErrors("2.1a", "scan \"/path\" as x\ndone()\n", 0,
            "Programa minimo valido");

        // 2.2 Falta scan al inicio
        checkSynErrors("2.2a", "for each x in x {}\ndone()\n", 1,
            "Falta 'scan' al inicio");

        // 2.3 Scan mal formado
        checkSynErrors("2.3a", "scan x\ndone()\n", 1,
            "Scan sin ruta (panico detiene cascada)");
        checkSynErrors("2.3b", "scan \"/path\" x\ndone()\n", 1,
            "Scan sin 'as'");

        // 2.4 For mal formado
        checkSynErrors("2.4a", "scan \"/path\" as x\nfor y in x {}\ndone()\n", 1,
            "For sin 'each'");
        checkSynErrors("2.4b", "scan \"/path\" as x\nfor each in x {}\ndone()\n", 1,
            "For sin iterador");
        checkSynErrors("2.4c", "scan \"/path\" as x\nfor each y x {}\ndone()\n", 1,
            "For sin 'in'");
        checkSynErrors("2.4d", "scan \"/path\" as x\nfor each x in {}\ndone()\n", 1,
            "For sin fuente");

        // 2.5 If mal formado
        checkSynErrors("2.5a", "scan \"/path\" as x\nfor each x in x {\nif {\n}\n}\ndone()\n", 1,
            "If sin condicion (panico corta cascada)");

        // 2.6 Let mal formado
        checkSynErrors("2.6a", "scan \"/path\" as x\nlet = 1\ndone()\n", 1,
            "Let sin identificador (panico corta cascada)");
        checkSynErrors("2.6b", "scan \"/path\" as x\nlet x\ndone()\n", 1,
            "Let sin expresion (panico corta cascada)");

        // 2.7 Funcion mal formada
        checkSynErrors("2.7a", "scan \"/path\" as x\nmove\ndone()\n", 1,
            "Funcion sin parentesis");
        checkSynErrors("2.7b", "scan \"/path\" as x\nmove(\"/a\"\ndone()\n", 1,
            "Funcion sin parentesis cierre");

        // 2.8 Bloques mal cerrados
        checkSynErrors("2.8a", "scan \"/path\" as x\nfor each x in x {\ndone()\n", 1,
            "For sin cerrar (falta })");

        // 2.9 Sentencia desconocida
        checkSynErrors("2.9a", "scan \"/path\" as x\nxyz 123\ndone()\n", 1,
            "Palabra desconocida como sentencia");

        System.out.println();
    }

    // ========================================================================
    // CAPA 3: ANALISIS SEMANTICO
    // ========================================================================
    private static void layer3Semantic() {
        System.out.println("--- CAPA 3: SEMANTICO ---");

        // 3.1 Variables: declaracion y uso
        checkSemErrors("3.1a", "scan \"/path\" as files\nlet x = 1\ndone()\n", 0,
            "Declaracion y uso correcto");
        checkSemErrors("3.1b", "scan \"/path\" as files\nlet x = y\ndone()\n", 1,
            "Uso de variable no declarada");
        checkSemErrors("3.1c", "scan \"/path\" as files\nlet files = 1\ndone()\n", 1,
            "Redeclaracion de variable");

        // 3.2 Ambitos (scopes)
        checkSemErrors("3.2a", "scan \"/path\" as files\nfor each x in files {\nlet inner = 1\n}\nlet x = inner\ndone()\n", 1,
            "Variable de for no accesible fuera");
        checkSemErrors("3.2b", "scan \"/path\" as files\nif 1 == 1 {\nlet inner = 1\n}\nlet x = inner\ndone()\n", 1,
            "Variable de if no accesible fuera");
        checkSemErrors("3.2c", "scan \"/path\" as files\nif 1 == 1 {\nlet z = 1\n} else {\nlet b = 2\n}\nlet x = b\ndone()\n", 1,
            "Variable de else no accesible fuera");
        checkSemErrors("3.2d", "scan \"/path\" as files\nlet outer = 1\nfor each item in files {\nlet x = outer\n}\ndone()\n", 0,
            "Variable externa accesible dentro de for");

        // 3.3 Funciones: move (2 args path,path)
        checkSemErrors("3.3a", "scan \"/path\" as files\nfor each x in files { move(x, \"/dest\") }\ndone()\n", 0,
            "move: argumentos correctos");
        checkSemErrors("3.3b", "scan \"/path\" as files\nmove(files, \"/dest\", \"/x\")\ndone()\n", 1,
            "move: demasiados argumentos");
        checkSemErrors("3.3c", "scan \"/path\" as files\nmove(123, \"/dest\")\ndone()\n", 1,
            "move: tipo incorrecto primer arg");

        // 3.4 Funciones: copy (2 args path,path) — igual que move
        checkSemErrors("3.4a", "scan \"/path\" as files\nfor each x in files { copy(x, \"/dest\") }\ndone()\n", 0,
            "copy: argumentos correctos");
        checkSemErrors("3.4b", "scan \"/path\" as files\ncopy(123, \"/dest\")\ndone()\n", 1,
            "copy: tipo incorrecto primer arg");

        // 3.5 Funciones: delete (1 arg path)
        checkSemErrors("3.5a", "scan \"/path\" as files\nfor each x in files { delete(x) }\ndone()\n", 0,
            "delete: argumento correcto");
        checkSemErrors("3.5b", "scan \"/path\" as files\ndelete(123)\ndone()\n", 1,
            "delete: tipo incorrecto arg");
        checkSemErrors("3.5c", "scan \"/path\" as files\ndelete(files, \"/x\")\ndone()\n", 1,
            "delete: demasiados argumentos");

        // 3.6 Funciones: rename (2 args path,path)
        checkSemErrors("3.6a", "scan \"/path\" as files\nfor each x in files { rename(x, \"/new\") }\ndone()\n", 0,
            "rename: argumentos correctos");
        checkSemErrors("3.6b", "scan \"/path\" as files\nrename(99, \"/new\")\ndone()\n", 1,
            "rename: tipo incorrecto primer arg");

        // 3.7 Funciones: log (1-2 args any)
        checkSemErrors("3.7a", "scan \"/path\" as files\nlog(\"msg\")\ndone()\n", 0,
            "log: 1 argumento string");
        checkSemErrors("3.7b", "scan \"/path\" as files\nlog(42)\ndone()\n", 0,
            "log: 1 argumento numero");
        checkSemErrors("3.7c", "scan \"/path\" as files\nlog(\"msg\", \"INFO\")\ndone()\n", 0,
            "log: 2 argumentos");
        checkSemErrors("3.7d", "scan \"/path\" as files\nlog()\ndone()\n", 1,
            "log: 0 argumentos (min 1)");
        checkSemErrors("3.7e", "scan \"/path\" as files\nlog(1, 2, 3)\ndone()\n", 1,
            "log: 3 argumentos (max 2)");

        // 3.8 Atributos de archivo
        checkSemErrors("3.8a", "scan \"/path\" as files\nfor each x in files { let z = x.ext }\ndone()\n", 0,
            "Atributo valido (ext)");
        checkSemErrors("3.8b", "scan \"/path\" as files\nlet k = 123\nlet x = k.ext\ndone()\n", 1,
            "Atributo sobre numero");
        checkSemErrors("3.8c", "scan \"/path\" as files\nlet x = y.size\ndone()\n", 1,
            "Atributo sobre no declarado");

        // 3.9 For con fuente no declarada
        checkSemErrors("3.9a", "scan \"/path\" as files\nfor each x in noExiste {}\ndone()\n", 1,
            "Fuente de for no declarada");

        // 3.10 Case insensitive en funciones
        checkSemErrors("3.10a", "scan \"/path\" as files\nfor each x in files { MOVE(x, \"/dest\") }\ndone()\n", 0,
            "MOVE en mayusculas funciona");
        checkSemErrors("3.10b", "scan \"/path\" as files\nfor each x in files { DELETE(x) }\ndone()\n", 0,
            "DELETE en mayusculas funciona");

        System.out.println();
    }

    // ========================================================================
    // CAPA 4: INTEGRACION — programas completos
    // ========================================================================
    private static void layer4Integration() {
        System.out.println("--- CAPA 4: INTEGRACION ---");

        // 4.1 Programa con todas las caracteristicas
        checkFullPipeline("4.1a",
            "scan \"C:/Users/Test\" as carpeta\n" +
            "let limite = 50MB\n" +
            "log(\"Iniciando...\")\n" +
            "for each arch in carpeta {\n" +
            "    if arch.ext == .pdf {\n" +
            "        move(arch, \"C:/Docs\")\n" +
            "    } else if arch.ext == .jpg {\n" +
            "        copy(arch, \"C:/Imagenes\")\n" +
            "    } else {\n" +
            "        log(\"Sin clasificar: \" + arch.name)\n" +
            "    }\n" +
            "    if arch.size > limite {\n" +
            "        log(\"Archivo grande: \" + arch.name)\n" +
            "    }\n" +
            "}\n" +
            "log(\"Fin\")\n" +
            "done()\n",
            "Programa completo debe pasar todas las capas sin errores");

        // 4.2 Programa con solo scan (minimo)
        checkFullPipeline("4.2a",
            "scan \"/path\" as x\ndone()\n",
            "Programa minimo sin errores");

        // 4.3 Programa con operadores logicos
        checkFullPipeline("4.3a",
            "scan \"/path\" as x\n" +
            "for each x in x {\n" +
            "    if x.ext == .pdf || x.ext == .docx {\n" +
            "        if x.size > 10MB && x.size < 100MB {\n" +
            "            move(x, \"/dest\")\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "done()\n",
            "Operadores logicos OR y AND anidados");

        // 4.4 Concatenacion de cadenas
        checkFullPipeline("4.4a",
            "scan \"/path\" as x\n" +
            "for each x in x {\n" +
            "    log(\"Archivo: \" + x.name)\n" +
            "}\n" +
            "done()\n",
            "Concatenacion de cadenas con +");

        // 4.5 Variables reutilizadas en distintos ambitos
        checkFullPipeline("4.5a",
            "scan \"/path\" as x\n" +
            "let msg = \"inicio\"\n" +
            "log(msg)\n" +
            "for each x in x {\n" +
            "    let msgLocal = x.name\n" +
            "    log(msgLocal)\n" +
            "}\n" +
            "log(msg)\n" +
            "done()\n",
            "Variable externa persiste fuera del for");

        System.out.println();
    }

    // ========================================================================
    // METODOS AUXILIARES
    // ========================================================================

    // Verifica que la capa lexica detecta (o no) errores
    private static void checkLexError(String id, String code, boolean expectError, String desc) {
        System.out.print("  " + id + ": " + desc + "... ");
        try {
            Token[] raw = Tokenizador.tokenizador(code);
            AFD afd = SortScriptAFD.obtenerAFD();
            afd.aceptar(raw);
            boolean hasLexErrors = afd.getErrores().hayErrores();
            if (hasLexErrors == expectError) {
                System.out.println("OK");
                passed++;
            } else {
                System.out.println("FALLO: se esperaban errores=" + expectError
                    + " pero se encontraron=" + hasLexErrors);
                if (hasLexErrors) afd.getErrores().imprimirTabla();
                failed++;
            }
        } catch (Exception e) {
            System.out.println("EXCEPCION: " + e.getMessage());
            failed++;
        }
    }

    // Verifica que el analisis lexico no cause excepcion
    private static void checkLexNoCrash(String id, String code, String desc) {
        System.out.print("  " + id + ": " + desc + "... ");
        try {
            Token[] raw = Tokenizador.tokenizador(code);
            AFD afd = SortScriptAFD.obtenerAFD();
            afd.aceptar(raw);
            System.out.println("OK (sin excepcion)");
            passed++;
        } catch (Exception e) {
            System.out.println("EXCEPCION: " + e.getMessage());
            failed++;
        }
    }

    // Verifica la cantidad de errores sintacticos
    private static void checkSynErrors(String id, String code, int expected, String desc) {
        System.out.print("  " + id + ": " + desc + "... ");
        try {
            Token[] raw = Tokenizador.tokenizador(code);
            AFD afd = SortScriptAFD.obtenerAFD();
            List<Token> tokens = afd.aceptar(raw);
            if (afd.getErrores().hayErrores()) {
                System.out.println("FALLO: errores lexicos inesperados");
                afd.getErrores().imprimirTabla();
                failed++;
                return;
            }

            java.util.ArrayList<Token> parserTokens = new java.util.ArrayList<>();
            for (Token t : tokens) if (t.existeSimbolo()) parserTokens.add(t);

            ParserLL1 p = new ParserLL1(parserTokens);
            p.inicio();
            int synCount = p.errores.getErrores().size();

            if (synCount == expected) {
                System.out.println("OK (" + synCount + " errores)");
                passed++;
            } else {
                System.out.println("FALLO: esperados=" + expected + " encontrados=" + synCount);
                if (synCount > 0) p.errores.imprimirTabla();
                failed++;
            }
        } catch (Exception e) {
            System.out.println("EXCEPCION: " + e.getMessage());
            failed++;
        }
    }

    // Verifica la cantidad de errores semanticos (sin errores lex/syn)
    private static void checkSemErrors(String id, String code, int expected, String desc) {
        System.out.print("  " + id + ": " + desc + "... ");
        try {
            Token[] raw = Tokenizador.tokenizador(code);
            AFD afd = SortScriptAFD.obtenerAFD();
            List<Token> tokens = afd.aceptar(raw);
            if (afd.getErrores().hayErrores()) {
                System.out.println("FALLO: errores lexicos inesperados");
                afd.getErrores().imprimirTabla();
                failed++;
                return;
            }

            java.util.ArrayList<Token> parserTokens = new java.util.ArrayList<>();
            for (Token t : tokens) if (t.existeSimbolo()) parserTokens.add(t);

            ParserLL1 p = new ParserLL1(parserTokens);
            p.inicio();
            if (p.errores.hayErrores()) {
                System.out.println("FALLO: errores sintacticos inesperados");
                p.errores.imprimirTabla();
                failed++;
                return;
            }

            NodoArbol raiz = p.getRaiz();
            SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.analizar(raiz);
            int semCount = sem.errores.getErrores().size();

            if (semCount == expected) {
                System.out.println("OK (" + semCount + " errores)");
                passed++;
            } else {
                System.out.println("FALLO: esperados=" + expected + " encontrados=" + semCount);
                if (semCount > 0) sem.errores.imprimirTabla();
                failed++;
            }
        } catch (Exception e) {
            System.out.println("EXCEPCION: " + e.getMessage());
            failed++;
        }
    }

    // Verifica que el pipeline completo produce 0 errores en todas las capas
    private static void checkFullPipeline(String id, String code, String desc) {
        System.out.print("  " + id + ": " + desc + "... ");
        try {
            Token[] raw = Tokenizador.tokenizador(code);
            AFD afd = SortScriptAFD.obtenerAFD();
            List<Token> tokens = afd.aceptar(raw);
            if (afd.getErrores().hayErrores()) {
                System.out.println("FALLO (lexico)");
                afd.getErrores().imprimirTabla();
                failed++;
                return;
            }

            java.util.ArrayList<Token> parserTokens = new java.util.ArrayList<>();
            for (Token t : tokens) if (t.existeSimbolo()) parserTokens.add(t);

            ParserLL1 p = new ParserLL1(parserTokens);
            p.inicio();
            if (p.errores.hayErrores()) {
                System.out.println("FALLO (sintactico)");
                p.errores.imprimirTabla();
                failed++;
                return;
            }

            NodoArbol raiz = p.getRaiz();
            if (raiz == null) {
                System.out.println("FALLO: arbol nulo");
                failed++;
                return;
            }
            if (!"programa".equals(raiz.getValor())) {
                System.out.println("FALLO: raiz del arbol es '" + raiz.getValor() + "', esperado 'programa'");
                failed++;
                return;
            }

            SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.analizar(raiz);
            int semCount = sem.errores.getErrores().size();
            if (semCount > 0) {
                System.out.println("FALLO (semantico): " + semCount + " errores");
                sem.errores.imprimirTabla();
                failed++;
                return;
            }

            System.out.println("OK (0 errores en lex/syn/sem, raiz=programa)");
            passed++;
        } catch (Exception e) {
            System.out.println("EXCEPCION: " + e.getMessage());
            failed++;
        }
    }
}
