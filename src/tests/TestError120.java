package tests;

import lexico.*;
import parser.*;
import java.util.List;

public class TestError120 {
    public static void main(String[] args) {
        String codigo = 
            "scan \"C:/Users/Ana/Desktop\" as escritorio\n" +
            "let docs = \"C:/Users/Ana/Documentos\"\n" +
            "let media = \"C:/Users/Ana/Multimedia\"\n" +
            "let backups = \"C:/Users/Ana/Backups\"\n" +
            "let tamLimite = 10MB\n" +
            "log(\"Iniciando limpieza del escritorio...\")\n" +
            "for each item in escritorio {\n" +
            "    if item.ext == .pdf || item.ext == .docx || item.ext == .xlsx {\n" +
            "        move(item, docs)\n" +
            "    }\n" +
            "    if item.ext == .jpg || item.ext == .mp3 || item.ext == .mp4 {\n" +
            "        move(item, media)\n" +
            "    }\n" +
            "    if item.ext == .zip || item.ext == .rar {\n" +
            "        copy(item, backups)\n" +
            "    }\n" +
            "    if item.size > tamLimite && item.ext != .zip {\n" +
            "        log(\"Archivo grande en escritorio: \" + item.name)\n" +
            "    }\n" +
            "}\n" +
            "log(\"Limpieza completada\")\n" +
            "done()\n";

        System.out.println("=== TEST: Error 120 en Ejemplo 4 ===\n");

        Token[] raw = Tokenizador.tokenizador(codigo);
        AFD afd = SortScriptAFD.obtenerAFD();
        List<Token> tokens = afd.aceptar(raw);

        if (afd.getErrores().hayErrores()) {
            System.out.println("--- ERRORES LEXICOS ---");
            afd.getErrores().imprimirTabla();
            return;
        }
        System.out.println("LEXICO: OK");

        List<Token> parserTokens = new java.util.ArrayList<>();
        for (Token t : tokens) if (t.existeSimbolo()) parserTokens.add(t);

        ParserLL1 p = new ParserLL1(parserTokens);
        p.inicio();

        if (p.errores.hayErrores()) {
            System.out.println("--- ERRORES SINTACTICOS ---");
            p.errores.imprimirTabla();
            return;
        }
        System.out.println("SINTACTICO: OK");

        NodoArbol raiz = p.getRaiz();
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analizar(raiz);

        if (sem.errores.hayErrores()) {
            System.out.println("--- ERRORES SEMANTICOS ---");
            for (ErrorCompilacion e : sem.errores.getErrores()) {
                System.out.println("  Codigo " + e.getNumero() + " | Linea " + e.getLinea() + " | " + e.getDescripcion());
            }
        } else {
            System.out.println("SEMANTICO: OK (sin errores)");
        }
    }
}
