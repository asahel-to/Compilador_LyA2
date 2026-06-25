import lexico.*;
import parser.*;
import java.util.List;

public class ComprehensiveErrorTests {
    static int passed = 0, failed = 0;

    static int[] analyze(String code) {
        Token[] raw = Tokenizador.tokenizador(code);
        AFD afd = SortScriptAFD.obtenerAFD();
        List<Token> tokens = afd.aceptar(raw);
        ParserLL1 parser = new ParserLL1(tokens);
        parser.inicio();
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analizar(parser.getRaiz());
        int syn = parser.errores.hayErrores() ? parser.errores.getErrores().size() : 0;
        int semE = sem.errores.hayErrores() ? sem.errores.getErrores().size() : 0;
        int lex = afd.getErrores().hayErrores() ? afd.getErrores().getErrores().size() : 0;
        return new int[]{lex, syn, semE};
    }

    static void check(String label, String code, int expectedLex, int expectedSyn, int expectedSem) {
        int[] r = analyze(code);
        boolean ok = r[0] == expectedLex && r[1] == expectedSyn && r[2] == expectedSem;
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + label + " => lex=" + r[0] + " sin=" + r[1] + " sem=" + r[2]
                + " (esperado lex=" + expectedLex + " sin=" + expectedSyn + " sem=" + expectedSem + ")");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== PRUEBAS SISTEMATICAS DE ERRORES ===\n");

        // ====================================================================
        // CAPA 1: ERRORES LEXICOS
        // ====================================================================
        System.out.println("--- CAPA 1: LEXICO ---");

        String C = "scan \"C:\\temp\" as col\n";

        // 1.1 Simbolo no en alfabeto
        check("1.1a @ no en alfabeto",  C + "@\ndone()\n", 1, 0, 0);
        check("1.1b # no en alfabeto",  C + "#\ndone()\n", 1, 0, 0);
        check("1.1c $ no en alfabeto",  C + "$\ndone()\n", 1, 0, 0);
        check("1.1d ~ no en alfabeto",  C + "~\ndone()\n", 1, 0, 0);
        check("1.1e ^ no en alfabeto",  C + "^\ndone()\n", 1, 0, 0);
        check("1.1f ` no en alfabeto",  C + "`\ndone()\n", 1, 0, 0);
        check("1.1g ? no en alfabeto",  C + "?\ndone()\n", 1, 0, 0);
        check("1.1h % no en alfabeto",  C + "%\ndone()\n", 1, 0, 0);
        check("1.1i & solo (no &&)",    C + "&\ndone()\n", 1, 0, 0);
        check("1.1j | solo (no ||)",    C + "|\ndone()\n", 1, 0, 0);

        // 1.2 Palabra reservada incompleta
        check("1.2a 'eac' (each incompleto)", C + "for each item in col {}\neac\ndone()\n", 1, 0, 0);
        check("1.2b 'sc' (scan-like incompleto)", C + "sc\ndone()\n", 1, 0, 0);
        check("1.2c 'fo' (for incompleto)", C + "fo\ndone()\n", 1, 0, 0);
        check("1.2d 'siz' (size incompleto)", C + "siz\ndone()\n", 1, 0, 0);
        check("1.2e 'nam' (name incompleto)", C + "nam\ndone()\n", 1, 0, 0);
        check("1.2f 'mo' (move incompleto)", C + "mo\ndone()\n", 1, 0, 0);
        check("1.2g 'del' (delete incompleto)", C + "del\ndone()\n", 1, 0, 0);
        check("1.2h 'ea' (each prefix)", C + "ea\ndone()\n", 1, 0, 0);

        // 1.3 Token no reconocido
        check("1.3a numero con letras '5abc'",  C + "5abc\ndone()\n", 1, 0, 0);
        check("1.3b guion '-' no es operador en SortScript", C + "-\ndone()\n", 1, 0, 0);
        check("1.3c '*' no es operador", C + "*\ndone()\n", 1, 0, 0);
        check("1.3d ';' no es valido", C + ";\ndone()\n", 1, 0, 0);

        // 1.4 Tokens que parecen literales pero son invalidos -> DESCONOCIDO
        check("1.4a '.abcdefgh' extension muy larga", C + ".abcdefgh\ndone()\n", 1, 0, 0);
        check("1.4b '5XYZ' tamano con unidad invalida", C + "5XYZ\ndone()\n", 1, 0, 0);
        check("1.4c scan ruta termina en backslash", C.substring(0, C.length()-1) + "\"\ndone()\n", 1, 1, 0);
        check("1.4d scan ruta solo raiz C:\\", "scan \"C:\\\" as col\ndone()\n", 0, 0, 0);

        // 1.5 Identificadores normales (NO deben dar error lexico)
        check("1.5a 'wh' es identificador valido", C + "let x = wh\ndone()\n", 0, 0, 1); // no declarado -> sem
        check("1.5b 'abcMB' es identificador valido", C + "let x = abcMB\ndone()\n", 0, 0, 1); // no declarado -> sem
        check("1.5c 'tmp' es identificador valido", C + "let x = tmp\ndone()\n", 0, 0, 1); // no declarado -> sem

        // ====================================================================
        // CAPA 2: ERRORES SINTACTICOS
        // ====================================================================
        System.out.println("\n--- CAPA 2: SINTACTICO ---");

        // 2.1 Falta scan
        check("2.1a programa sin scan", "done()\n", 0, 1, 0);

        // 2.2 scan mal formado
        check("2.2a scan sin ruta", "scan as col\ndone()\n", 0, 1, 0);
        check("2.2b scan sin as", "scan \"C:\\Users\" col\ndone()\n", 0, 1, 0);
        check("2.2c scan sin identificador", "scan \"C:\\Users\" as\ndone()\n", 0, 1, 0);

        // 2.3 for mal formado
        check("2.3a for sin each", C + "for item in col {}\ndone()\n", 0, 1, 0);
        check("2.3b for sin iterador", C + "for each in col {}\ndone()\n", 0, 1, 0);
        check("2.3c for sin in", C + "for each item col {}\ndone()\n", 0, 1, 0);
        check("2.3d for sin fuente (in seguido de {)", C + "for each item in {}\ndone()\n", 0, 1, 0);
        check("2.3e for sin } cierre", C + "for each item in col {\ndone()\n", 0, 1, 0);

        // 2.4 if mal formado
        check("2.4a if sin condicion", C + "if {}\ndone()\n", 0, 1, 0);
        check("2.4b if sin {", C + "if col.size > 0 }\ndone()\n", 0, 1, 0);
        check("2.4c if sin }", C + "if col.size > 0 {\ndone()\n", 0, 1, 0);
        check("2.4d if con condicion incompleta", C + "if col.size > {}\ndone()\n", 0, 1, 0);

        // 2.5 let mal formado
        check("2.5a let sin id", C + "let = 5\ndone()\n", 0, 1, 0);
        check("2.5b let sin =", C + "let x 5\ndone()\n", 0, 1, 0);
        check("2.5c let sin expresion", C + "let x =\ndone()\n", 0, 1, 0);

        // 2.6 Funciones mal formadas
        check("2.6a move sin parentesis", C + "move \"C:\\src\" \"C:\\dst\"\ndone()\n", 0, 1, 0);
        check("2.6b move sin )", C + "move(\"C:\\src\", \"C:\\dst\"\ndone()\n", 0, 1, 0);
        check("2.6c delete sin argumento", C + "delete()\ndone()\n", 0, 0, 1);
        check("2.6d log con trailing comma", C + "log(\"a\",)\ndone()\n", 0, 1, 0);
        check("2.6e done sin parentesis", "scan \"C:\\temp\" as col\ndone\n", 0, 1, 0);
        check("2.6f done sin )", "scan \"C:\\temp\" as col\ndone(\n", 0, 1, 0);

        // 2.7 Atributos mal formados
        check("2.7a atributo invalido .xyz", C + "for each item in col {\nlet x = item.xyz\n}\ndone()\n", 0, 1, 0);
        check("2.7b punto sin atributo", C + "for each item in col {\nlet x = item.\n}\ndone()\n", 0, 1, 0);

        // 2.8 Token desconocido o identificador como sentencia
        check("2.8a 'foox' como sentencia (ID)", C + "foox\ndone()\n", 0, 1, 0);
        check("2.8b 'x123' como sentencia (ID)", C + "x123\ndone()\n", 0, 1, 0);
        check("2.8c 'scan' repetido como sentencia", C + "scan \"C:\\temp\" as other\ndone()\n", 0, 1, 0);

        // 2.9 Llaves desbalanceadas
        check("2.9a } extra al final", C + "for each item in col {}\n}\ndone()\n", 0, 1, 0);

        // ====================================================================
        // CAPA 3: ERRORES SEMANTICOS
        // ====================================================================
        System.out.println("\n--- CAPA 3: SEMANTICO ---");

        // 3.1 Variable no declarada
        check("3.1a var no declarada en let", C + "let x = z\ndone()\n", 0, 0, 1);
        check("3.1b var no declarada en if", C + "if col.size > z {\nlet x = 1\n}\ndone()\n", 0, 0, 1);
        check("3.1c var no declarada en for source", C + "for each item in zzz {}\ndone()\n", 0, 0, 1);
        check("3.1d var no declarada en funcion", C + "log(zzz)\ndone()\n", 0, 0, 1);
        check("3.1e var no declarada en lado izquierdo de comparacion", C + "if x > col.size {}\ndone()\n", 0, 0, 1);
        check("3.1f var no declarada en suma", C + "let z = 1\nlet b = z + zzz\ndone()\n", 0, 0, 1);

        // 3.2 Redeclaracion
        check("3.2a redeclaracion let", C + "let x = 5\nlet x = 10\ndone()\n", 0, 0, 1);
        check("3.2b redeclaracion let con misma variable de scan", C + "let col = 5\ndone()\n", 0, 0, 1);
        check("3.2c redeclaracion for iterator con let previo", C + "let item = 5\nfor each item in col {}\ndone()\n", 0, 0, 0);

        // 3.3 Alcance (scope)
        check("3.3a iterator fuera del for", C + "for each item in col {}\nlog(item)\ndone()\n", 0, 0, 1);
        check("3.3b variable de if fuera", C + "if col.size > 0 {\nlet z = 1\n}\nlog(z)\ndone()\n", 0, 0, 1);
        check("3.3c variable de else fuera", C + "if col.size > 0 {\n} else {\nlet z = 1\n}\nlog(z)\ndone()\n", 0, 0, 1);
        check("3.3d variable de bloque for fuera", C + "for each item in col {\nlet z = 1\n}\nlog(z)\ndone()\n", 0, 0, 1);
        check("3.3e variable externa accesible dentro de for", C + "let zz = 5\nfor each item in col {\nlet z = zz\n}\ndone()\n", 0, 0, 0);

        // 3.4 Cantidad de argumentos
        check("3.4a move demasiados args", C + "move(\"C:\\src\", \"C:\\dst\", col)\ndone()\n", 0, 0, 1);
        check("3.4b move muy pocos args", C + "move(\"C:\\src\")\ndone()\n", 0, 0, 1);
        check("3.4c copy demasiados args", C + "copy(\"C:\\src\", \"C:\\dst\", col)\ndone()\n", 0, 0, 1);
        check("3.4d delete demasiados args", C + "delete(\"C:\\src\", \"C:\\dst\")\ndone()\n", 0, 0, 1);
        check("3.4e log 0 args", C + "log()\ndone()\n", 0, 0, 1);
        check("3.4f log 3 args", C + "log(\"a\", \"b\", \"c\")\ndone()\n", 0, 0, 1);
        check("3.4g rename demasiados args", C + "rename(\"C:\\src\", \"C:\\dst\", col)\ndone()\n", 0, 0, 1);

        // 3.5 Tipo de argumentos
        check("3.5a move arg1 numero", C + "move(123, \"C:\\dst\")\ndone()\n", 0, 0, 1);
        check("3.5b move arg2 numero", C + "move(\"C:\\src\", 456)\ndone()\n", 0, 0, 1);
        check("3.5c copy arg1 string sin /", C + "copy(\"hola\", \"C:\\dst\")\ndone()\n", 0, 1, 0);
        check("3.5d delete arg numero", C + "delete(789)\ndone()\n", 0, 0, 1);
        check("3.5e rename arg1 numero", C + "rename(123, \"C:\\dst\")\ndone()\n", 0, 0, 1);

        // 3.6 Atributos en tipo incorrecto
        check("3.6a atributo en numero", C + "let x = 5\nlet y = x.size\ndone()\n", 0, 0, 1);
        check("3.6b atributo en string", C + "let x = \"hello\"\nlet y = x.size\ndone()\n", 0, 0, 1);
        check("3.6c atributo valido en path", C + "let x = \"C:\\temp\\file.txt\"\nlet y = x.ext\ndone()\n", 0, 0, 0);
        check("3.6d atributo valido en iterator", C + "for each item in col {\nlet x = item.size\n}\ndone()\n", 0, 0, 0);

        // 3.7 *** GAP FIXED: for source debe ser collection ***
        check("3.7a for source tipo number", C + "let x = 5\nfor each item in x {}\ndone()\n", 0, 0, 1);
        check("3.7b for source tipo string", C + "let x = \"hola\"\nfor each item in x {}\ndone()\n", 0, 0, 1);

        // 3.8 Iterator puede ocultar variable externa (shadowing permitido)
        check("3.8a for iterator shadows outer variable", C + "let item = 5\nfor each item in col {}\ndone()\n", 0, 0, 0);

        // 3.9 Programa valido
        check("3.9a programa valido completo",
            "scan \"C:\\Users\" as col\nfor each item in col {\nlet x = item.size\nif x > 100KB {\nlog(\"big: \" + item.name)\n} else {\nlog(\"small: \" + item.name)\n}\n}\nlet msg = \"procesado\"\nlog(msg)\ndone()\n", 0, 0, 0);

        System.out.println("\n=== RESULTADOS ===");
        System.out.println("Pasadas: " + passed + " Falladas: " + failed);
        if (failed > 0) System.exit(1);
        System.out.println("TODAS LAS PRUEBAS PASARON.");
    }
}
