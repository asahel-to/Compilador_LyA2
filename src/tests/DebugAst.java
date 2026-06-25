package tests;

import java.util.List;
import lexico.AFD;
import lexico.SortScriptAFD;
import lexico.Token;
import lexico.Tokenizador;
import parser.NodoArbol;
import parser.ParserLL1;

public class DebugAst {
    public static void main(String[] args) {
        String code = "scan \"/home\" as files\nlet n = 123\nlet x = n.ext\ndone()\n";
        if (args.length > 0) {
            try {
                code = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(args[0])));
            } catch (Exception e) {
                System.err.println("Warning: could not read " + args[0] + ", using default.");
            }
        }
        try {
            Token[] raw = Tokenizador.tokenizador(code);
            AFD afd = SortScriptAFD.obtenerAFD();
            List<Token> tokens = afd.aceptar(raw);
            java.util.ArrayList<Token> parserTokens = new java.util.ArrayList<>();
            for (Token t : tokens) if (t.existeSimbolo()) parserTokens.add(t);
            ParserLL1 p = new ParserLL1(parserTokens);
            p.inicio();
            NodoArbol r = p.getRaiz();
            printNode(r, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void printNode(NodoArbol n, int indent) {
        if (n == null) return;
        for (int i=0;i<indent;i++) System.out.print("  ");
        System.out.println(n.getValor() + " (line=" + n.getLinea() + ")");
        for (NodoArbol c : n.getHijos()) printNode(c, indent+1);
    }
}
