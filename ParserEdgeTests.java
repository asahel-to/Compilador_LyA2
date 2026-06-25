import lexico.*;
import parser.*;
import java.util.List;

public class ParserEdgeTests {
    static int passed = 0, failed = 0;

    static int parseAndCountErrors(String code) {
        Token[] raw = Tokenizador.tokenizador(code);
        AFD afd = SortScriptAFD.obtenerAFD();
        List<Token> tokens = afd.aceptar(raw);
        ParserLL1 parser = new ParserLL1(tokens);
        parser.inicio();
        return parser.errores.hayErrores() ? parser.errores.getErrores().size() : 0;
    }

    static String result(int actual, int expected, int lexErrors) {
        return "lex=" + lexErrors + " sin=" + actual + " (esperado=" + expected + ")";
    }

    static void check(String label, String code, int expectedSyn) {
        Token[] raw = Tokenizador.tokenizador(code);
        AFD afd = SortScriptAFD.obtenerAFD();
        List<Token> tokens = afd.aceptar(raw);
        ParserLL1 parser = new ParserLL1(tokens);
        parser.inicio();
        int actual = parser.errores.hayErrores() ? parser.errores.getErrores().size() : 0;
        int lexErrors = afd.getErrores().hayErrores() ? afd.getErrores().getErrores().size() : 0;
        if (actual == expectedSyn) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + label + " => " + result(actual, expectedSyn, lexErrors));
        }
    }

    public static void main(String[] args) {
        // === PROGRAMAS VÁLIDOS ===
        check("programa minimo valido",
            "scan \"C:\\Users\" as x\ndone()\n", 0);

        check("programa con for y let",
            "scan \"C:\\Users\" as x\nfor each item in x {\nlet y = 5\n}\ndone()\n", 0);

        check("programa con if else",
            "scan \"C:\\Users\" as x\nif x.size > 100MB {\nlog(\"big\")\n} else {\nlog(\"small\")\n}\ndone()\n", 0);

        check("programa con else if",
            "scan \"C:\\Users\" as x\nif x.size > 100MB {\nlog(\"big\")\n} else if x.size > 50MB {\nlog(\"medium\")\n} else {\nlog(\"small\")\n}\ndone()\n", 0);

        check("programa con expresion parentesis",
            "scan \"C:\\Users\" as x\nlet y = (5 + 3)\ndone()\n", 0);

        check("programa con condicion NOT",
            "scan \"C:\\Users\" as x\nif !(x.size > 0) {\nlog(\"empty\")\n}\ndone()\n", 0);

        check("programa con AND/OR",
            "scan \"C:\\Users\" as x\nif x.size > 0 && x.size < 100MB || x.ext == \".txt\" {\nlog(\"ok\")\n}\ndone()\n", 0);

        check("programa vacio (solo scan+done)",
            "scan \"C:\\Users\" as x\ndone()\n", 0);

        check("programa con atributo",
            "scan \"C:\\Users\" as x\nlog(x.name)\ndone()\n", 0);

        check("programa con multiple args log",
            "scan \"C:\\Users\" as x\nlog(\"a\", \"b\")\ndone()\n", 0);

        check("programa con argumento solo en funcion archivo",
            "scan \"C:\\Users\" as x\ndelete(x)\ndone()\n", 0);

        // === ERRORES SINTÁCTICOS ===
        check("falta scan al inicio",
            "for each f in x {}\ndone()\n", 1);

        check("scan sin ruta",
            "scan as x\ndone()\n", 1);

        check("scan sin as",
            "scan \"C:\\Users\" x\ndone()\n", 1);

        check("for sin each",
            "scan \"C:\\Users\" as x\nfor f in x {}\ndone()\n", 1);

        check("for sin in",
            "scan \"C:\\Users\" as x\nfor each f x {}\ndone()\n", 1);

        check("let sin id",
            "scan \"C:\\Users\" as x\nlet = 5\ndone()\n", 1);

        check("let sin expresion",
            "scan \"C:\\Users\" as x\nlet y =\ndone()\n", 1);

        check("func sin parentesis",
            "scan \"C:\\Users\" as x\nlog \"hello\"\ndone()\n", 1);

        check("if sin condicion",
            "scan \"C:\\Users\" as x\nif {\nlog(\"x\")\n}\ndone()\n", 1);

        check("else sin bloque",
            "scan \"C:\\Users\" as x\nif x.size > 0 {\nlog(\"a\")\n} else\ndone()\n", 1);

        check("for sin cerrar",
            "scan \"C:\\Users\" as x\nfor each f in x {\ndone()\n", 1);

        check("for con iterador DESCONOCIDO",
            "scan \"C:\\Users\" as x\nfor each eac in x {}\ndone()\n", 1);

        check("let con DESCONOCIDO en expresion",
            "scan \"C:\\Users\" as x\nlet y = eac\ndone()\n", 1);

        check("func con DESCONOCIDO argumento",
            "scan \"C:\\Users\" as x\nlog(eac)\ndone()\n", 1);

        check("parentesis desbalanceados",
            "scan \"C:\\Users\" as x\nif (x.size > 0 {\nlog(\"a\")\n}\ndone()\n", 1);

        check("punto sin atributo",
            "scan \"C:\\Users\" as x\nlet y = x.\ndone()\n", 1);

        check("atributo invalido despues de punto",
            "scan \"C:\\Users\" as x\nlet y = x.invalid\ndone()\n", 1);

        check("operador sin operandos",
            "scan \"C:\\Users\" as x\nif  > 0 {\nlog(\"a\")\n}\ndone()\n", 1);

        check("condicion sin operador",
            "scan \"C:\\Users\" as x\nif x.size 0 {\nlog(\"a\")\n}\ndone()\n", 1);

        check("done faltante",
            "scan \"C:\\Users\" as x\n", 1);

        System.out.println("\n=== RESULTADOS PARSER ===");
        System.out.println("Pasadas: " + passed + " Falladas: " + failed);
        if (failed > 0) System.exit(1);
    }
}
