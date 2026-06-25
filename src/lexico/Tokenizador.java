package lexico;

import java.util.ArrayList;
import java.util.List;

public class Tokenizador {

    public static String eliminarComentarios(String entrada) {
        StringBuilder salida = new StringBuilder();
        int i = 0, n = entrada.length();
        while (i < n) {
            if (entrada.charAt(i) == '"') {
                salida.append('"'); i++;
                while (i < n && entrada.charAt(i) != '"') {
                    salida.append(entrada.charAt(i)); i++;
                }
                if (i < n) { salida.append('"'); i++; }
                continue;
            }
            if (i + 1 < n && entrada.charAt(i) == '/' && entrada.charAt(i + 1) == '/') {
                i += 2;
                while (i < n && entrada.charAt(i) != '\n') i++;
                if (i < n && entrada.charAt(i) == '\n') { salida.append('\n'); i++; }
            } else if (i + 1 < n && entrada.charAt(i) == '/' && entrada.charAt(i + 1) == '*') {
                i += 2;
                boolean cerrado = false;
                while (i < n) {
                    if (i + 1 < n && entrada.charAt(i) == '*' && entrada.charAt(i + 1) == '/') {
                        i += 2; cerrado = true; break;
                    }
                    if (entrada.charAt(i) == '\n') salida.append('\n');
                    i++;
                }
                if (!cerrado) {
                    while (i < n) {
                        if (entrada.charAt(i) == '\n') salida.append('\n');
                        i++;
                    }
                    return salida.toString();
                }
            } else {
                salida.append(entrada.charAt(i));
                i++;
            }
        }
        return salida.toString();
    }

    public static String[] tokenizarLinea(String entrada) {
        List<String> tokens = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enCadena = false;
        boolean enRuta = false;

        for (int i = 0; i < entrada.length(); i++) {
            char c = entrada.charAt(i);

            if (c == '"') {
                actual.append(c);
                if (enCadena || enRuta) {
                    enCadena = false;
                    enRuta = false;
                    tokens.add(actual.toString());
                    actual.setLength(0);
                } else {
                    enCadena = true;
                }
                continue;
            }

            if (enCadena || enRuta) {
                actual.append(c);
                continue;
            }

            if (Character.isWhitespace(c)) {
                if (actual.length() > 0) {
                    tokens.add(actual.toString());
                    actual.setLength(0);
                }
                continue;
            }

            if (i + 1 < entrada.length()) {
                String doble = "" + c + entrada.charAt(i + 1);
                if (doble.equals("==") || doble.equals("!=") || doble.equals("<=") || doble.equals(">=")
                    || doble.equals("&&") || doble.equals("||")) {
                    if (actual.length() > 0) {
                        tokens.add(actual.toString());
                        actual.setLength(0);
                    }
                    tokens.add(doble);
                    i++;
                    continue;
                }
            }

            if ("+-=<>!.,(){}[]".indexOf(c) != -1) {
                if (c == '.' && actual.length() == 0) {
                    actual.append(c);
                    continue;
                }
                if (actual.length() > 0) {
                    tokens.add(actual.toString());
                    actual.setLength(0);
                }
                tokens.add(String.valueOf(c));
                continue;
            }

            actual.append(c);
        }

        if (actual.length() > 0) {
            tokens.add(actual.toString());
        }

        return tokens.toArray(new String[0]);
    }

    public static Token[] tokenizador(String entrada) {
        entrada = eliminarComentarios(entrada);
        String[] lineas = entrada.split("\n", -1);
        Token[] tokensTemp = new Token[10000];
        int contador = 0;
        int numLinea = 1;

        for (String linea : lineas) {
            String[] toks = tokenizarLinea(linea);
            for (String t : toks) {
                tokensTemp[contador++] = new Token(t, numLinea);
            }
            numLinea++;
        }

        Token[] resultado = new Token[contador];
        System.arraycopy(tokensTemp, 0, resultado, 0, contador);
        return resultado;
    }
}
