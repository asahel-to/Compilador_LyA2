package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserTAC {

    public static class TripletaTAC {
        private final String indice;
        private final String operando1;
        private final String operador;
        private final String operando2;

        public TripletaTAC(String indice, String operando1, String operador, String operando2) {
            this.indice = indice;
            this.operando1 = operando1;
            this.operador = operador;
            this.operando2 = operando2;
        }

        public String getIndice() { return indice; }
        public String getOperando1() { return operando1; }
        public String getOperador() { return operador; }
        public String getOperando2() { return operando2; }
    }

    public static class CuadruploTAC {
        private final String op;
        private final String arg1;
        private final String arg2;
        private final String resultado;

        public CuadruploTAC(String op, String arg1, String arg2, String resultado) {
            this.op = op;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.resultado = resultado;
        }

        public String getOp() { return op; }
        public String getArg1() { return arg1; }
        public String getArg2() { return arg2; }
        public String getResultado() { return resultado; }
    }

    public static class ResultadoTAC {
        private final List<TripletaTAC> tripletas;
        private final List<CuadruploTAC> cuadruplos;

        public ResultadoTAC(List<TripletaTAC> tripletas, List<CuadruploTAC> cuadruplos) {
            this.tripletas = tripletas;
            this.cuadruplos = cuadruplos;
        }

        public List<TripletaTAC> getTripletas() { return tripletas; }
        public List<CuadruploTAC> getCuadruplos() { return cuadruplos; }
    }

    private static final Pattern P_IF_FALSE = Pattern.compile("^if_false\\s+(\\S+)\\s+goto\\s+(\\S+)$");
    private static final Pattern P_GOTO = Pattern.compile("^goto\\s+(\\S+)$");
    private static final Pattern P_LABEL = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*:$");
    private static final Pattern P_CALL = Pattern.compile("^(?:call\\s+)?([A-Za-z_][A-Za-z0-9_]*)(?:\\s*,\\s*(.*))?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_ASIGN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.+)$");
    private static final Pattern P_ATTR = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\.([A-Za-z_][A-Za-z0-9_]*)$");

    public ResultadoTAC parsear(List<String> lineas) {
        List<TripletaTAC> tripletas = new ArrayList<>();
        List<CuadruploTAC> cuadruplos = new ArrayList<>();
        Map<String, String> referenciaIndices = new HashMap<>();

        int instruccionNumero = 1;
        for (String linea : lineas) {
            String texto = linea == null ? "" : linea.trim();
            if (texto.isEmpty()) continue; // Skip empty lines

            InstruccionTAC instruccion = parseLinea(texto, instruccionNumero, referenciaIndices);
            if (instruccion == null) continue;

            if (instruccion.resultado != null && !instruccion.resultado.isEmpty() && esTemporal(instruccion.resultado)) {
                referenciaIndices.put(instruccion.resultado, String.valueOf(instruccionNumero));
            }

            tripletas.add(new TripletaTAC(
                String.valueOf(instruccionNumero),
                instruccion.arg1,
                instruccion.op,
                instruccion.arg2
            ));
            cuadruplos.add(new CuadruploTAC(
                instruccion.op,
                instruccion.arg1,
                instruccion.arg2,
                instruccion.resultado // Add the result to the quadruple
            ));
            instruccionNumero++; // Increment instruction number
        }

        return new ResultadoTAC(tripletas, cuadruplos);
    }

    private static class InstruccionTAC {
        private final String indice;
        private final String op;
        private final String arg1;
        private final String arg2;
        private final String resultado;

        private InstruccionTAC(String indice, String op, String arg1, String arg2, String resultado) {
            this.indice = indice;
            this.op = op;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.resultado = resultado; // Set the result
        }
    }

    private InstruccionTAC parseLinea(String texto, int numeroInstruccion, Map<String, String> referenciaIndices) {
        Matcher mLabel = P_LABEL.matcher(texto);
        if (mLabel.matches()) {
            return new InstruccionTAC(
                String.valueOf(numeroInstruccion),
                "LABEL",
                "",
                "",
                mLabel.group(1).trim()
            );
        }

        Matcher mIf = P_IF_FALSE.matcher(texto);
        if (mIf.matches()) {
            return new InstruccionTAC(
                String.valueOf(numeroInstruccion),
                "IF_FALSE",
                reemplazarReferenciaSiCorresponde(mIf.group(1), referenciaIndices),
                "",
                mIf.group(2)
            );
        }

        Matcher mGoto = P_GOTO.matcher(texto);
        if (mGoto.matches()) {
            return new InstruccionTAC(
                String.valueOf(numeroInstruccion),
                "GOTO",
                "",
                "",
                mGoto.group(1)
            );
        }

        if (texto.toLowerCase().startsWith("call ")) {
            Matcher mCall = P_CALL.matcher(texto.trim());
            if (mCall.matches()) {
                String nombre = mCall.group(1);
                String args = mCall.group(2) == null ? "" : mCall.group(2).trim();
                if (esValorSimple(args) && referenciaIndices.containsKey(args)) {
                    args = referenciaIndices.get(args);
                }
                return new InstruccionTAC(
                    String.valueOf(numeroInstruccion),
                    "CALL",
                    nombre,
                    args,
                    ""
                );
            }
            return null;
        }

        Matcher mAssign = P_ASIGN.matcher(texto);
        if (mAssign.matches()) {
            String destino = mAssign.group(1);
            String resto = mAssign.group(2).trim();

            if (resto.toLowerCase().startsWith("call ")) {
                Matcher mCall = P_CALL.matcher(resto.trim());
                if (mCall.matches()) {
                    String nombre = mCall.group(1);
                    String args = mCall.group(2) == null ? "" : mCall.group(2).trim();
                    if (esValorSimple(args) && referenciaIndices.containsKey(args)) {
                        args = referenciaIndices.get(args);
                    }
                    return new InstruccionTAC(destino, "CALL", nombre, args, destino);
                }
            }

            Matcher mAttr = P_ATTR.matcher(resto);
            if (mAttr.matches()) {
                return new InstruccionTAC(destino, "ATTR", mAttr.group(1), mAttr.group(2), destino);
            }

            if (esValorSimple(resto)) {
                resto = reemplazarReferenciaSiCorresponde(resto, referenciaIndices);
                return new InstruccionTAC(destino, "=", resto, "", destino);
            }

            String operador = detectarOperador(resto);
            if (!"=".equals(operador)) {
                int idx = buscarIndiceOperador(resto, operador);
                if (idx >= 0) {
                    String left = reemplazarReferenciaSiCorresponde(resto.substring(0, idx).trim(), referenciaIndices);
                    String right = reemplazarReferenciaSiCorresponde(resto.substring(idx + operador.length()).trim(), referenciaIndices);
                    return new InstruccionTAC(destino, operador, left, right, destino);
                }
            }

            return new InstruccionTAC(destino, "=", reemplazarReferenciaSiCorresponde(resto, referenciaIndices), "", destino);
        }

        return null;
    }

    private boolean esValorSimple(String texto) {
        if (texto == null) return false;
        String trimmed = texto.trim();
        if (trimmed.matches("^\".*\"$")) return true;
        if (trimmed.matches("^[A-Za-z_][A-Za-z0-9_]*$")) return true;
        if (trimmed.matches("^\\d+(?:\\.\\d+)?(?:KB|MB|GB)?$")) return true;
        return false;
    }

    private boolean esTemporal(String texto) {
        return texto != null && texto.matches("^t\\d+$");
    }

    private String reemplazarReferenciaSiCorresponde(String texto, Map<String, String> referencias) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.matches("^[A-Za-z_][A-Za-z0-9_]*$") && referencias.containsKey(limpio)) {
            return referencias.get(limpio);
        }
        return texto;
    }

    private String detectarOperador(String expresion) {
        if (expresion == null) return "=";
        String texto = expresion.trim();
        if (buscarIndiceOperador(texto, "==") >= 0) return "==";
        if (buscarIndiceOperador(texto, "!=") >= 0) return "!=";
        if (buscarIndiceOperador(texto, ">=") >= 0) return ">=";
        if (buscarIndiceOperador(texto, "<=") >= 0) return "<=";
        if (buscarIndiceOperador(texto, "or", true) >= 0) return "or";
        if (buscarIndiceOperador(texto, "and", true) >= 0) return "and";
        if (buscarIndiceOperador(texto, ">") >= 0) return ">";
        if (buscarIndiceOperador(texto, "<") >= 0) return "<";
        if (buscarIndiceOperador(texto, "+") >= 0) return "+";
        if (buscarIndiceOperador(texto, "-") >= 0) return "-";
        if (buscarIndiceOperador(texto, "*") >= 0) return "*";
        if (buscarIndiceOperador(texto, "/") >= 0) return "/";
        return "=";
    }

    private int buscarIndiceOperador(String texto, String operador) {
        return buscarIndiceOperador(texto, operador, "or".equals(operador) || "and".equals(operador));
    }

    private int buscarIndiceOperador(String texto, String operador, boolean palabra) {
        boolean dentro = false;
        for (int i = 0; i <= texto.length() - operador.length(); i++) {
            char c = texto.charAt(i);
            if (c == '"') {
                dentro = !dentro;
            }
            if (dentro) continue;
            if (texto.startsWith(operador, i)) {
                if (palabra) {
                    boolean inicioValido = i == 0 || !Character.isLetterOrDigit(texto.charAt(i - 1));
                    int fin = i + operador.length();
                    boolean finValido = fin >= texto.length() || !Character.isLetterOrDigit(texto.charAt(fin));
                    if (!inicioValido || !finValido) continue;
                }
                return i;
            }
        }
        return -1;
    }

    private String[] separarOperandos(String expresion, String operador) {
        String texto = expresion.trim();
        int idx = -1;
        if ("or".equals(operador) || "and".equals(operador)) {
            idx = texto.indexOf(operador);
        } else {
            idx = texto.indexOf(operador);
        }
        if (idx < 0) return new String[0];
        String left = texto.substring(0, idx).trim();
        String right = texto.substring(idx + operador.length()).trim();
        return new String[]{left, right};
    }
}
