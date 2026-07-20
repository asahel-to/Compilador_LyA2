package tests;

import java.util.List;
import parser.GeneradorTresDirecciones;

public class PruebasTresDirecciones {
    public static void main(String[] args) {
        try {
            GeneradorTresDirecciones gen = new GeneradorTresDirecciones();
            GeneradorTresDirecciones.ResultadoTresDirecciones resultado = gen.generar("a+b*c");

            if (resultado == null || resultado.getTripletas().size() != 2) {
                throw new RuntimeException("Se esperaba generar 2 tripletas para a+b*c");
            }

            if (!resultado.getTripletas().get(0).getResultado().equals("T1") ||
                !resultado.getTripletas().get(0).getOperador().equals("*")) {
                throw new RuntimeException("La primera tripleta no se generó con la forma esperada");
            }

            if (!resultado.getCuadruplos().get(0).getResultado().equals("T1") ||
                !resultado.getCuadruplos().get(1).getResultado().equals("T2")) {
                throw new RuntimeException("Los cuádruplos no se generaron con los resultados esperados");
            }

            System.out.println("Pruebas de tres direcciones aprobadas");
            System.out.println(resultado.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}
