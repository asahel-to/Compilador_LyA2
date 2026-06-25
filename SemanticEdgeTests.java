import lexico.*;
import parser.*;
import java.util.List;

public class SemanticEdgeTests {
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
        if (r[0] == expectedLex && r[1] == expectedSyn && r[2] == expectedSem) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + label + " => lex=" + r[0] + " sin=" + r[1] + " sem=" + r[2]
                + " (esperado lex=" + expectedLex + " sin=" + expectedSyn + " sem=" + expectedSem + ")");
        }
    }

    public static void main(String[] args) {
        // === PROGRAMAS VÁLIDOS ===
        check("programa completo valido",
            "scan \"C:\\Users\" as col\nfor each item in col {\nlet y = item.size\nlog(y)\n}\ndone()\n", 0, 0, 0);

        check("programa minimo",
            "scan \"C:\\Users\" as col\ndone()\n", 0, 0, 0);

        // === ERRORES SEMÁNTICOS: variable no declarada ===
        check("variable no declarada",
            "scan \"C:\\Users\" as col\nlet y = z\ndone()\n", 0, 0, 1);

        check("for source no declarada",
            "scan \"C:\\Users\" as col\nfor each item in zzz {}\ndone()\n", 0, 0, 1);

        check("variable en expresion no declarada",
            "scan \"C:\\Users\" as col\nlet y = 5\nlet z = y + www\ndone()\n", 0, 0, 1);

        // === ERRORES SEMÁNTICOS: redeclaracion ===
        check("redeclaracion let",
            "scan \"C:\\Users\" as col\nlet y = 5\nlet y = 10\ndone()\n", 0, 0, 1);

        // === ERRORES SEMÁNTICOS: alcance (scope) ===
        check("for iterator no accesible fuera",
            "scan \"C:\\Users\" as col\nfor each item in col {\nlet z = 5\n}\nlet w = item\ndone()\n", 0, 0, 1);

        check("variable en if no accesible fuera",
            "scan \"C:\\Users\" as col\nif col.size > 0 {\nlet z = 5\n}\nlet w = z\ndone()\n", 0, 0, 1);

        check("variable externa accesible dentro de for",
            "scan \"C:\\Users\" as col\nlet zz = 5\nfor each item in col {\nlet w = zz\n}\ndone()\n", 0, 0, 0);

        check("nested if scope",
            "scan \"C:\\Users\" as col\nif col.size > 0 {\nlet val1 = 1\nif col.size > 100MB {\nlet val2 = val1\n}\n}\ndone()\n", 0, 0, 0);

        // === ERRORES SEMÁNTICOS: atributos en tipos incorrectos ===
        check("atributo en numero",
            "scan \"C:\\Users\" as col\nlet y = 5\nlet z = y.size\ndone()\n", 0, 0, 1);

        check("atributo en string",
            "scan \"C:\\Users\" as col\nlet y = \"hello\"\nlet z = y.size\ndone()\n", 0, 0, 1);

        check("atributo en ruta valida",
            "scan \"C:\\Users\" as col\nlet y = \"C:\\Users\\file.txt\"\nlet z = y.ext\ndone()\n", 0, 0, 0);

        // === ERRORES SEMÁNTICOS: funciones ===
        check("func move demasiados args",
            "scan \"C:\\Users\" as col\nmove(\"C:\\source\", \"C:\\dest\", col)\ndone()\n", 0, 0, 1);

        check("func log 0 args",
            "scan \"C:\\Users\" as col\nlog()\ndone()\n", 0, 0, 1);

        check("func log 3 args",
            "scan \"C:\\Users\" as col\nlog(\"a\", \"b\", \"c\")\ndone()\n", 0, 0, 1);

        check("func move tipo incorrecto primer arg",
            "scan \"C:\\Users\" as col\nmove(123, \"C:\\dest\")\ndone()\n", 0, 0, 1);

        check("func move tipo correcto",
            "scan \"C:\\Users\" as col\nmove(\"C:\\source\", \"C:\\dest\")\ndone()\n", 0, 0, 0);

        check("func log string argumento valido",
            "scan \"C:\\Users\" as col\nlog(\"hello\")\ndone()\n", 0, 0, 0);

        check("func copy con ruta correcta",
            "scan \"C:\\Users\" as col\ncopy(\"C:\\source\", \"C:\\backup\")\ndone()\n", 0, 0, 0);

        check("func delete concat path",
            "scan \"C:\\Users\" as col\nfor each item in col {\ndelete(\"C:\\temp\" + item.name)\n}\ndone()\n", 0, 0, 0);

        // === PANICO: DESCONOCIDO produce error lex+sint pero puede cascada sem ===
        check("DESCONOCIDO en argumento func",
            "scan \"C:\\Users\" as col\nmove(col, eac)\ndone()\n", 1, 1, 0);

        // === PROGRAMA COMPLETO COMPLEJO ===
        check("programa completo complejo",
            "scan \"C:\\Users\" as col\nlet total = 0\nfor each item in col {\nif item.size > 1MB {\nlet total = total + 1\nlog(\"big: \" + item.name)\n} else {\nlog(\"small: \" + item.name)\n}\n}\nlet msg = \"processed files\"\nlog(msg)\ndone()\n", 0, 0, 0);

        System.out.println("\n=== RESULTADOS SEMANTICOS ===");
        System.out.println("Pasadas: " + passed + " Falladas: " + failed);
        if (failed > 0) System.exit(1);
    }
}
