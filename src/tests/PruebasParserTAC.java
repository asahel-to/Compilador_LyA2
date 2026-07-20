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

        System.out.println("Pruebas de parsing TAC aprobadas");
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
