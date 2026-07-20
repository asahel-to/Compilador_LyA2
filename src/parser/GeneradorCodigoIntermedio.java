package parser;

import java.util.ArrayList;
import java.util.List;

public class GeneradorCodigoIntermedio {
    private final StringBuilder codigo = new StringBuilder();
    private int tempCounter = 1;
    private int labelCounter = 1;

    public String generar(NodoArbol raiz) {
        codigo.setLength(0);
        if (raiz == null) return "";
        for (NodoArbol hijo : raiz.getHijos()) {
            generarNodo(hijo);
        }
        emit("call done");
        return codigo.toString().trim();
    }

    private void generarNodo(NodoArbol nodo) {
        if (nodo == null) return;
        switch (nodo.getValor()) {
            case "scanStmt":
                generarScan(nodo);
                break;
            case "letStmt":
                generarLet(nodo);
                break;
            case "funcCall":
                generarFuncion(nodo);
                break;
            case "ifStmt":
                generarIf(nodo);
                break;
            case "forStmt":
                generarFor(nodo);
                break;
            default:
                for (NodoArbol hijo : nodo.getHijos()) {
                    generarNodo(hijo);
                }
        }
    }

    private void generarScan(NodoArbol nodo) {
        List<NodoArbol> hijos = nodo.getHijos();
        if (hijos.size() >= 4) {
            String id = hijos.get(3).getValor();
            String ruta = hijos.get(1).getValor();
            emit(id + " = call scan, " + ruta);
        }
    }

    private void generarLet(NodoArbol nodo) {
        List<NodoArbol> hijos = nodo.getHijos();
        if (hijos.size() >= 4) {
            String nombre = hijos.get(1).getValor();
            String expr = traducirExpresion(hijos.get(3));
            emit(nombre + " = " + expr);
        }
    }

    private void generarFuncion(NodoArbol nodo) {
        List<NodoArbol> hijos = nodo.getHijos();
        if (hijos.size() < 4) return;
        String nombre = hijos.get(0).getValor();
        StringBuilder args = new StringBuilder();
        NodoArbol argsNode = null;
        for (NodoArbol h : hijos) {
            if (h != null && ("argumentos".equals(h.getValor()) || "argumentosRuta".equals(h.getValor()))) {
                argsNode = h;
                break;
            }
        }
        if (argsNode != null) {
            args.append(extraerArgumentos(argsNode));
        }
        emit("call " + nombre + (args.length() > 0 ? ", " + args : ""));
    }

    private String extraerArgumentos(NodoArbol nodo) {
        if (nodo == null) return "";
        StringBuilder sb = new StringBuilder();
        if (!nodo.getHijos().isEmpty()) {
            sb.append(traducirExpresion(nodo.getHijos().get(0)));
        }
        NodoArbol resto = nodo.getHijos().size() >= 2 ? nodo.getHijos().get(1) : null;
        while (resto != null) {
            if (resto.getHijos().size() >= 2) {
                sb.append(", ").append(traducirExpresion(resto.getHijos().get(1)));
            }
            resto = resto.getHijos().size() >= 3 ? resto.getHijos().get(2) : null;
        }
        return sb.toString();
    }

    private void generarIf(NodoArbol nodo) {
        List<NodoArbol> hijos = nodo.getHijos();
        if (hijos.size() < 5) return;
        String falseLabel = nuevaEtiqueta("Label");
        String endLabel = null;
        String condTemp = generarCondicion(hijos.get(1));
        emit("if_false " + condTemp + " goto " + falseLabel);
        if (hijos.size() > 3 && hijos.get(3) != null) {
            generarNodo(hijos.get(3));
        }
        if (hijos.size() > 5 && hijos.get(5) != null) {
            endLabel = nuevaEtiqueta("Label");
            emit("goto " + endLabel);
            emit(falseLabel + ":");
            generarElse(hijos.get(5), endLabel);
            emit(endLabel + ":");
        } else {
            emit(falseLabel + ":");
        }
    }

    private void generarElse(NodoArbol nodo, String endLabel) {
        if (nodo == null) return;
        if (nodo.getHijos().isEmpty()) return;
        NodoArbol segundo = nodo.getHijos().get(1);
        if (segundo != null && "ifStmt".equals(segundo.getValor())) {
            String falseLabel = nuevaEtiqueta("Label");
            String condTemp = generarCondicion(segundo.getHijos().get(1));
            emit("if_false " + condTemp + " goto " + falseLabel);
            if (segundo.getHijos().size() > 3 && segundo.getHijos().get(3) != null) {
                generarNodo(segundo.getHijos().get(3));
            }
            emit("goto " + endLabel);
            emit(falseLabel + ":");
            if (segundo.getHijos().size() > 5 && segundo.getHijos().get(5) != null) {
                generarElse(segundo.getHijos().get(5), endLabel);
            }
        } else if (nodo.getHijos().size() >= 3) {
            generarNodo(nodo.getHijos().get(2));
        }
    }

    private String generarCondicion(NodoArbol nodo) {
        if (nodo == null) return "";
        if ("condicionOr".equals(nodo.getValor())) {
            List<String> terminos = new ArrayList<>();
            recolectarCondiciones(nodo, "or", terminos);
            return combinarCondiciones(terminos, "or");
        }
        if ("condicionAnd".equals(nodo.getValor())) {
            List<String> terminos = new ArrayList<>();
            recolectarCondiciones(nodo, "and", terminos);
            return combinarCondiciones(terminos, "and");
        }
        if ("condicionSimple".equals(nodo.getValor())) {
            List<NodoArbol> hijos = nodo.getHijos();
            if (hijos.size() >= 3) {
                String left = resolverValorCondicion(hijos.get(0));
                String op = obtenerOperador(hijos.get(1));
                String right = resolverValorCondicion(hijos.get(2));
                String temp = nuevaTemp();
                emit(temp + " = " + left + " " + op + " " + right);
                return temp;
            }
        }
        return traducirExpresion(nodo);
    }

    private String resolverValorCondicion(NodoArbol nodo) {
        if (nodo == null) return "";
        String valor = traducirExpresion(nodo);
        if (valor.contains(".")) {
            String temp = nuevaTemp();
            emit(temp + " = " + valor);
            return temp;
        }
        return valor;
    }

    private void recolectarCondiciones(NodoArbol nodo, String operador, List<String> terminos) {
        if (nodo == null) return;
        if ("condicionOr".equals(nodo.getValor()) && "or".equals(operador)) {
            recolectarCondiciones(nodo.getHijos().get(0), operador, terminos);
            NodoArbol resto = nodo.getHijos().size() > 1 ? nodo.getHijos().get(1) : null;
            while (resto != null) {
                if (resto.getHijos().size() >= 2) {
                    terminoDesdeCondicion(resto.getHijos().get(1), terminos);
                }
                resto = resto.getHijos().size() >= 3 ? resto.getHijos().get(2) : null;
            }
            return;
        }
        if ("condicionAnd".equals(nodo.getValor()) && "and".equals(operador)) {
            recolectarCondiciones(nodo.getHijos().get(0), operador, terminos);
            NodoArbol resto = nodo.getHijos().size() > 1 ? nodo.getHijos().get(1) : null;
            while (resto != null) {
                if (resto.getHijos().size() >= 2) {
                    terminoDesdeCondicion(resto.getHijos().get(1), terminos);
                }
                resto = resto.getHijos().size() >= 3 ? resto.getHijos().get(2) : null;
            }
            return;
        }
        terminoDesdeCondicion(nodo, terminos);
    }

    private void terminoDesdeCondicion(NodoArbol nodo, List<String> terminos) {
        if (nodo == null) return;
        if ("condicionSimple".equals(nodo.getValor())) {
            terminos.add(generarCondicion(nodo));
        } else if (nodo.getHijos() != null) {
            for (NodoArbol hijo : nodo.getHijos()) {
                terminoDesdeCondicion(hijo, terminos);
            }
        }
    }

    private String combinarCondiciones(List<String> terminos, String operador) {
        if (terminos.isEmpty()) return "";
        String acumulado = terminos.get(0);
        for (int i = 1; i < terminos.size(); i++) {
            String temp = nuevaTemp();
            emit(temp + " = " + acumulado + " " + operador + " " + terminos.get(i));
            acumulado = temp;
        }
        return acumulado;
    }

    private String traducirExpresion(NodoArbol nodo) {
        if (nodo == null) return "";
        String valor = nodo.getValor();
        if ("expresion".equals(valor)) {
            if (nodo.getHijos().isEmpty()) return "";
            String acumulado = traducirExpresion(nodo.getHijos().get(0));
            NodoArbol resto = nodo.getHijos().size() > 1 ? nodo.getHijos().get(1) : null;
            while (resto != null && !resto.getHijos().isEmpty()) {
                if (resto.getHijos().size() >= 2) {
                    String rhs = traducirExpresion(resto.getHijos().get(1));
                    String temp = nuevaTemp();
                    emit(temp + " = " + acumulado + " + " + rhs);
                    acumulado = temp;
                }
                resto = resto.getHijos().size() >= 3 ? resto.getHijos().get(2) : null;
            }
            return acumulado;
        }
        if ("termino".equals(valor) || "terminoRuta".equals(valor)) {
            List<NodoArbol> hijos = nodo.getHijos();
            if (hijos.isEmpty()) return "";
            String base = traducirExpresion(hijos.get(0));
            if (hijos.size() > 1) {
                NodoArbol acceso = hijos.get(1);
                if ("accesoAtributo".equals(acceso.getValor()) && !acceso.getHijos().isEmpty()) {
                    String attr = acceso.getHijos().get(acceso.getHijos().size() - 1).getValor();
                    if (attr != null) return base + "." + attr;
                }
            }
            return base;
        }
        if ("literal".equals(valor)) {
            if (nodo.getHijos().isEmpty()) return normalizarLiteral(nodo.getValor());
            return normalizarLiteral(nodo.getHijos().get(0).getValor());
        }
        if (nodo.getHijos().isEmpty()) return normalizarLiteral(nodo.getValor());
        StringBuilder sb = new StringBuilder();
        for (NodoArbol hijo : nodo.getHijos()) {
            String texto = traducirExpresion(hijo);
            if (texto == null || texto.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(texto);
        }
        return sb.toString();
    }

    private String normalizarLiteral(String valor) {
        if (valor == null) return "";
        String texto = valor.trim();
        if (texto.isEmpty()) return "\"\"";
        if ((texto.startsWith("\"") && texto.endsWith("\"")) || (texto.startsWith("'") && texto.endsWith("'"))) {
            return texto;
        }
        if (texto.startsWith(".") && texto.matches("\\.[A-Za-z0-9]+")) {
            return "\"" + texto + "\"";
        }
        return texto;
    }

    private String obtenerOperador(NodoArbol nodo) {
        if (nodo == null || nodo.getHijos().isEmpty()) return "==";
        return nodo.getHijos().get(0).getValor();
    }

    private void generarFor(NodoArbol nodo) {
        List<NodoArbol> hijos = nodo.getHijos();
        if (hijos.size() < 8) return;
        String iterador = hijos.get(2).getValor();
        String fuente = hijos.get(4).getValor();
        String inicio = nuevaEtiqueta("Label");
        String fin = nuevaEtiqueta("Label");
        String temp = nuevaTemp();
        emit(temp + " = call inicializar_iterador, " + fuente);
        emit(inicio + ":");
        String cond = nuevaTemp();
        emit(cond + " = call tiene_siguiente, " + temp);
        emit("if_false " + cond + " goto " + fin);
        emit(iterador + " = call siguiente, " + temp);
        if (hijos.get(6) != null) {
            generarNodo(hijos.get(6));
        }
        emit("goto " + inicio);
        emit(fin + ":");
    }

    private void emit(String linea) {
        codigo.append(linea).append("\n");
    }

    private String nuevaTemp() {
        return "t" + tempCounter++;
    }

    private String nuevaEtiqueta(String prefijo) {
        return prefijo + labelCounter++;
    }
}
