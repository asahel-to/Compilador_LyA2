package tests;

import java.util.Arrays;
import java.util.List;
import parser.ParserTAC;

public class PruebasParserTAC {
    public static void main(String[] args) {
        verificarCaso("docs = \"C:/Users/Ana/Documentos\"", "=", "\"C:/Users/Ana/Documentos\"", "", "docs");
        verificarCaso("if_false t9 goto Label3", "IF_FALSE", "t9", "", "Label3");
        verificarCaso("tamLimite = 50MB", "=", "50MB", "", "tamLimite");
        verificarCaso("call log, \"Iniciando limpieza del escritorio...\"", "CALL", "log", "\"Iniciando limpieza del escritorio...\"", "");

        verificarTripletasVsCuadruplos();

        System.out.println("Pruebas de parsing TAC aprobadas");
    }

    private static void verificarTripletasVsCuadruplos() {
        ParserTAC parser = new ParserTAC();
        java.util.List<String> lineas = java.util.Arrays.asList(
            "t1 = a + b",
            "t2 = t1 * c",
            "x = t2"
        );
        ParserTAC.ResultadoTAC resultado = parser.parsear(lineas);

        if (resultado.getTripletas().size() != 3) {
            throw new RuntimeException("Se esperaban 3 tripletas, se obtuvieron " + resultado.getTripletas().size());
        }
        if (resultado.getCuadruplos().size() != 3) {
            throw new RuntimeException("Se esperaban 3 cuadruplos, se obtuvieron " + resultado.getCuadruplos().size());
        }

        // Tripleta 1: (1, a, +, b) - sin referencias
        ParserTAC.TripletaTAC t1 = resultado.getTripletas().get(0);
        if (!t1.getIndice().equals("1")) throw new RuntimeException("Indice tripleta 1 incorrecto");
        if (!t1.getOperando1().equals("a")) throw new RuntimeException("Arg1 tripleta 1 incorrecto");
        if (!t1.getOperador().equals("+")) throw new RuntimeException("Op tripleta 1 incorrecto");
        if (!t1.getOperando2().equals("b")) throw new RuntimeException("Arg2 tripleta 1 incorrecto");

        // Tripleta 2: (2, 1, *, c) - t1 referenciado como indice 1
        ParserTAC.TripletaTAC t2 = resultado.getTripletas().get(1);
        if (!t2.getOperando1().equals("1")) {
            throw new RuntimeException("Tripleta 2 arg1 debe ser '1' (indice), pero fue '" + t2.getOperando1() + "'");
        }

        // Tripleta 3: (3, 2, =, ) - t2 referenciado como indice 2
        ParserTAC.TripletaTAC t3 = resultado.getTripletas().get(2);
        if (!t3.getOperando1().equals("2")) {
            throw new RuntimeException("Tripleta 3 arg1 debe ser '2' (indice), pero fue '" + t3.getOperando1() + "'");
        }

        // Cuadruplo 1: (+, a, b, t1) - sin referencias
        ParserTAC.CuadruploTAC c1 = resultado.getCuadruplos().get(0);
        if (!c1.getArg1().equals("a")) throw new RuntimeException("Arg1 cuadruplo 1 incorrecto");
        if (!c1.getResultado().equals("t1")) throw new RuntimeException("Resultado cuadruplo 1 incorrecto");

        // Cuadruplo 2: (*, t1, c, t2) - referencia como t<n>, no como indice
        ParserTAC.CuadruploTAC c2 = resultado.getCuadruplos().get(1);
        if (!c2.getArg1().equals("t1")) {
            throw new RuntimeException("Cuadruplo 2 arg1 debe ser 't1', pero fue '" + c2.getArg1() + "'");
        }

        // Cuadruplo 3: (=, t2, , x) - referencia como t<n>
        ParserTAC.CuadruploTAC c3 = resultado.getCuadruplos().get(2);
        if (!c3.getArg1().equals("t2")) {
            throw new RuntimeException("Cuadruplo 3 arg1 debe ser 't2', pero fue '" + c3.getArg1() + "'");
        }
    }

    private static void verificarCaso(String linea, String esperadoOp, String esperadoArg1, String esperadoArg2, String esperadoResultado) {
        List<String> tac = Arrays.asList(linea);
        ParserTAC.ResultadoTAC resultado = new ParserTAC().parsear(tac);
        if (resultado.getTripletas().size() != 1 || resultado.getCuadruplos().size() != 1) {
            throw new RuntimeException("La línea no produjo exactamente una instrucción: " + linea);
        }

        ParserTAC.TripletaTAC tripleta = resultado.getTripletas().get(0);
        ParserTAC.CuadruploTAC cuadruplo = resultado.getCuadruplos().get(0);

        if (!tripleta.getOperador().equals(esperadoOp)) {
            throw new RuntimeException("Op incorrecto en tripleta para '" + linea + "': esperado='" + esperadoOp + "' obtenido='" + tripleta.getOperador() + "'");
        }
        if (!tripleta.getOperando1().equals(esperadoArg1)) {
            throw new RuntimeException("Arg1 incorrecto en tripleta para '" + linea + "': esperado='" + esperadoArg1 + "' obtenido='" + tripleta.getOperando1() + "'");
        }
        if (!tripleta.getOperando2().equals(esperadoArg2)) {
            throw new RuntimeException("Arg2 incorrecto en tripleta para '" + linea + "': esperado='" + esperadoArg2 + "' obtenido='" + tripleta.getOperando2() + "'");
        }

        if (!cuadruplo.getOp().equals(esperadoOp)) {
            throw new RuntimeException("Op incorrecto en cuadruplo para '" + linea + "': esperado='" + esperadoOp + "' obtenido='" + cuadruplo.getOp() + "'");
        }
        if (!cuadruplo.getArg1().equals(esperadoArg1)) {
            throw new RuntimeException("Arg1 incorrecto en cuadruplo para '" + linea + "': esperado='" + esperadoArg1 + "' obtenido='" + cuadruplo.getArg1() + "'");
        }
        if (!cuadruplo.getArg2().equals(esperadoArg2)) {
            throw new RuntimeException("Arg2 incorrecto en cuadruplo para '" + linea + "': esperado='" + esperadoArg2 + "' obtenido='" + cuadruplo.getArg2() + "'");
        }
        if (!cuadruplo.getResultado().equals(esperadoResultado)) {
            throw new RuntimeException("Resultado incorrecto en cuadruplo para '" + linea + "': esperado='" + esperadoResultado + "' obtenido='" + cuadruplo.getResultado() + "'");
        }
    }
}
