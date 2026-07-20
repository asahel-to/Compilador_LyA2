package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GeneradorTresDirecciones {

    public static class Tripleta {
        private final int indice;
        private final String resultado;
        private final String operando1;
        private final String operador;
        private final String operando2;

        public Tripleta(int indice, String resultado, String operando1, String operador, String operando2) {
            this.indice = indice;
            this.resultado = resultado;
            this.operando1 = operando1;
            this.operador = operador;
            this.operando2 = operando2;
        }

        public int getIndice() { return indice; }
        public String getResultado() { return resultado; }
        public String getOperando1() { return operando1; }
        public String getOperador() { return operador; }
        public String getOperando2() { return operando2; }

        @Override
        public String toString() {
            return resultado + " = " + operando1 + " " + operador + " " + operando2;
        }
    }

    public static class Cuadruplo {
        private final String op;
        private final String arg1;
        private final String arg2;
        private final String resultado;

        public Cuadruplo(String op, String arg1, String arg2, String resultado) {
            this.op = op;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.resultado = resultado;
        }

        public String getOp() { return op; }
        public String getArg1() { return arg1; }
        public String getArg2() { return arg2; }
        public String getResultado() { return resultado; }

        @Override
        public String toString() {
            return op + ", " + arg1 + ", " + arg2 + ", " + resultado;
        }
    }

    public static class ResultadoTresDirecciones {
        private final List<Tripleta> tripletas;
        private final List<Cuadruplo> cuadruplos;

        public ResultadoTresDirecciones(List<Tripleta> tripletas, List<Cuadruplo> cuadruplos) {
            this.tripletas = tripletas;
            this.cuadruplos = cuadruplos;
        }

        public List<Tripleta> getTripletas() { return tripletas; }
        public List<Cuadruplo> getCuadruplos() { return cuadruplos; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Tripletas:\n");
            for (Tripleta t : tripletas) sb.append(t).append("\n");
            sb.append("\nCuádruplos:\n");
            for (Cuadruplo c : cuadruplos) sb.append(c).append("\n");
            return sb.toString();
        }
    }

    public ResultadoTresDirecciones generar(String expresion) {
        if (expresion == null || expresion.trim().isEmpty()) {
            return new ResultadoTresDirecciones(new ArrayList<>(), new ArrayList<>());
        }

        String expr = expresion.trim();
        List<Tripleta> tripletas = new ArrayList<>();
        List<Cuadruplo> cuadruplos = new ArrayList<>();
        int contador = 1;

        Stack<String> valores = new Stack<>();
        Stack<String> operadores = new Stack<>();

        for (int i = 0; i < expr.length(); i++) {
            char ch = expr.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '_') {
                int j = i;
                while (j < expr.length() && (Character.isLetterOrDigit(expr.charAt(j)) || expr.charAt(j) == '_')) {
                    j++;
                }
                String token = expr.substring(i, j);
                valores.push(token);
                i = j - 1;
            } else if (ch == '(') {
                operadores.push("(");
            } else if (ch == ')') {
                while (!operadores.isEmpty() && !"(".equals(operadores.peek())) {
                    aplicarOperador(valores, operadores, tripletas, cuadruplos, contador);
                    contador++;
                }
                if (!operadores.isEmpty()) operadores.pop();
            } else if (esOperador(ch)) {
                while (!operadores.isEmpty() && precedencia(operadores.peek()) >= precedencia(String.valueOf(ch))) {
                    aplicarOperador(valores, operadores, tripletas, cuadruplos, contador);
                    contador++;
                }
                operadores.push(String.valueOf(ch));
            }
        }

        while (!operadores.isEmpty()) {
            aplicarOperador(valores, operadores, tripletas, cuadruplos, contador);
            contador++;
        }

        if (valores.size() != 1) {
            throw new IllegalArgumentException("Expresión inválida: " + expresion);
        }

        return new ResultadoTresDirecciones(tripletas, cuadruplos);
    }

    private void aplicarOperador(Stack<String> valores, Stack<String> operadores, List<Tripleta> tripletas, List<Cuadruplo> cuadruplos, int contador) {
        if (operadores.isEmpty()) return;
        String op = operadores.pop();
        if ("(".equals(op)) return;
        if (valores.size() < 2) throw new IllegalArgumentException("Expresión inválida");

        String right = valores.pop();
        String left = valores.pop();
        String resultado = "T" + contador;

        tripletas.add(new Tripleta(contador, resultado, left, op, right));
        cuadruplos.add(new Cuadruplo(op, left, right, resultado));

        valores.push(resultado);
    }

    private boolean esOperador(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int precedencia(String op) {
        switch (op) {
            case "*":
            case "/":
                return 2;
            case "+":
            case "-":
                return 1;
            default:
                return 0;
        }
    }
}
