package tests;

import java.util.List;
import lexico.AFD;
import lexico.SortScriptAFD;
import lexico.Token;
import lexico.Tokenizador;
import parser.GeneradorCodigoIntermedio;
import parser.ParserLL1;
import parser.SemanticAnalyzer;

public class PruebasCodigoIntermedio {
    public static void main(String[] args) {
        String codigo = "scan \"/home\" as files\n"
                + "let docs = \"/docs\"\n"
                + "log(\"mensaje\")\n"
                + "done()\n";

        try {
            String tac = generarTac(codigo);
            if (!tac.contains("docs = \"/docs\"") || !tac.contains("call log, \"mensaje\"") || !tac.contains("call done")) {
                throw new RuntimeException("No se generó el código intermedio esperado:\n" + tac);
            }

            String codigoIf = "scan \"/home\" as files\n"
                    + "if 1 == 1 {\n"
                    + "    log(\"ok\")\n"
                    + "}\n"
                    + "done()\n";
            String tacIf = generarTac(codigoIf);
            if (!tacIf.contains("if_false") || !tacIf.contains("goto Label") || !tacIf.contains("call log, \"ok\"")) {
                throw new RuntimeException("No se generó el TAC del if esperado:\n" + tacIf);
            }

            String codigoFor = "scan \"/home\" as files\n"
                    + "for each item in files {\n"
                    + "    move(item, \"/dest\")\n"
                    + "}\n"
                    + "done()\n";
            String tacFor = generarTac(codigoFor);
            if (!tacFor.contains("call inicializar_iterador") || !tacFor.contains("call tiene_siguiente") || !tacFor.contains("call siguiente")) {
                throw new RuntimeException("No se generó el TAC del for esperado:\n" + tacFor);
            }

            String codigoLogico = "scan \"/home\" as files\n"
                    + "for each item in files {\n"
                    + "    if item.ext == \".pdf\" || item.ext == \".docx\" {\n"
                    + "        log(\"ok\")\n"
                    + "    }\n"
                    + "}\n"
                    + "done()\n";
            String tacLogico = generarTac(codigoLogico);
            if (!tacLogico.contains("= item.ext") || !tacLogico.contains("==") || !tacLogico.contains("or")) {
                throw new RuntimeException("No se generó el TAC lógico esperado:\n" + tacLogico);
            }

            String codigoConcatenacion = "scan \"/home\" as files\n"
                    + "for each item in files {\n"
                    + "    log(\"Archivo grande: \" + item.name + \" (\" + item.size + \")\")\n"
                    + "}\n"
                    + "done()\n";
            String tacConcatenacion = generarTac(codigoConcatenacion);
            if (!tacConcatenacion.contains("= \"Archivo grande: \" + item.name") && !tacConcatenacion.contains("= \"Archivo grande: \"") ) {
                throw new RuntimeException("No se generó el TAC de concatenación esperado:\n" + tacConcatenacion);
            }
            if (!tacConcatenacion.contains("call log")) {
                throw new RuntimeException("No se generó el call log para la concatenación:\n" + tacConcatenacion);
            }

            String codigoElseIf = "scan \"/home\" as files\n"
                    + "if 1 == 1 {\n"
                    + "    log(\"uno\")\n"
                    + "} else if 2 == 2 {\n"
                    + "    log(\"dos\")\n"
                    + "} else {\n"
                    + "    log(\"tres\")\n"
                    + "}\n"
                    + "done()\n";
            String tacElseIf = generarTac(codigoElseIf);
            if (!tacElseIf.contains("if_false") || !tacElseIf.contains("goto Label") || !tacElseIf.contains("call log, \"dos\"") || !tacElseIf.contains("call log, \"tres\"")) {
                throw new RuntimeException("No se generó el TAC de else/else if esperado:\n" + tacElseIf);
            }

            System.out.println("Intermediate code test passed");
            System.out.println(tac);
            System.out.println("--- IF TAC ---");
            System.out.println(tacIf);
            System.out.println("--- FOR TAC ---");
            System.out.println(tacFor);
            System.out.println("--- LOGIC TAC ---");
            System.out.println(tacLogico);
            System.out.println("--- ELSE IF TAC ---");
            System.out.println(tacElseIf);
            System.out.println("--- CONCAT TAC ---");
            System.out.println(tacConcatenacion);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static String generarTac(String codigo) throws Exception {
        Token[] raw = Tokenizador.tokenizador(codigo);
        AFD afd = SortScriptAFD.obtenerAFD();
        List<Token> tokens = afd.aceptar(raw);
        if (afd.getErrores().hayErrores()) {
            throw new RuntimeException("Erro léxico inesperado");
        }

        List<Token> parserTokens = new java.util.ArrayList<>();
        for (Token t : tokens) if (t.existeSimbolo()) parserTokens.add(t);

        ParserLL1 parser = new ParserLL1(parserTokens);
        parser.inicio();
        if (parser.errores.hayErrores()) {
            throw new RuntimeException("Erro sintáctico inesperado");
        }

        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analizar(parser.getRaiz());
        if (!sem.errores.getErrores().isEmpty()) {
            throw new RuntimeException("Erro semántico inesperado");
        }

        return new GeneradorCodigoIntermedio().generar(parser.getRaiz());
    }
}
