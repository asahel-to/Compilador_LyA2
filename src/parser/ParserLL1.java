package parser;

import java.util.List;
import java.util.Set;
import lexico.TipoToken;
import lexico.Token;

public class ParserLL1 {
    public Errores errores = new Errores();
    private List<Token> tokens;
    private int pos = 0, lineaFinal = -2;
    private boolean panicoUsado = false, lFinal = false;
    private NodoArbol raizArbol;

    private static final Set<TipoToken> TOKENS_SEGUROS = Set.of(
        TipoToken.KW_DONE, TipoToken.LLAVE_CIERRA, TipoToken.KW_ELSE,
        TipoToken.KW_SCAN, TipoToken.KW_FOR, TipoToken.KW_IF,
        TipoToken.KW_LET, TipoToken.FUNC_MOVE, TipoToken.FUNC_COPY,
        TipoToken.FUNC_DELETE, TipoToken.FUNC_RENAME, TipoToken.FUNC_LOG,
        TipoToken.EOF
    );

    public ParserLL1(List<Token> tokens) {
        this.tokens = tokens;
    }

    public NodoArbol getRaiz() { return raizArbol; }

    private void panico() { panico(TOKENS_SEGUROS); }

    private void panico(Set<TipoToken> tokensSeguros) {
        panicoUsado = true;
        if (!tokensSeguros.contains(tokenActual().getTipo())) {
            while (tokenActual().getTipo() != TipoToken.EOF && !tokensSeguros.contains(tokenActual().getTipo()))
                pos++;
        }
    }

    private Token tokenActual() {
        if (pos >= tokens.size()) {
            lineaFinal = tokens.isEmpty() ? 1 : tokens.get(tokens.size() - 1).getLinea();
            lFinal = true;
            return new Token("", -1, TipoToken.EOF, "", true);
        }
        return tokens.get(pos);
    }

    private Token tokenAnterior() { return tokens.get(pos - 1); }

    private NodoArbol match(TipoToken esperado, String msg) {
        if (panicoUsado) return null;
        if (tokenActual().getTipo() == esperado) {
            NodoArbol hoja = new NodoArbol(tokenActual().getLexema(), tokenActual().getLinea());
            pos++;
            return hoja;
        } else {
            errores.agregarError(TablaErrores.ERROR_SINTACTICO_GENERAL,
                lFinal ? lineaFinal : tokenActual().getLinea(), msg);
            panico();
            return null;
        }
    }

    public void inicio() {
        raizArbol = new NodoArbol("programa");

        if (tokenActual().getTipo() != TipoToken.KW_SCAN) {
            errores.agregarError(TablaErrores.ERROR_SCAN_INICIO,
                tokenActual().getLinea(),
                "Se espera 'scan' al inicio del programa.");
            panico(Set.of(TipoToken.EOF));
            return;
        }

        raizArbol.agregarHijo(scanStmt());

        while (tokenActual().getTipo() != TipoToken.EOF &&
               tokenActual().getTipo() != TipoToken.KW_DONE &&
               tokenActual().getTipo() != TipoToken.LLAVE_CIERRA &&
               tokenActual().getTipo() != TipoToken.KW_ELSE) {
            raizArbol.agregarHijo(sentencia());
            panicoUsado = false;
        }

        if (!errores.hayErrores()) {
            match(TipoToken.KW_DONE, "Se espera 'done' al final del programa.");
            match(TipoToken.PAR_ABRE, "Se espera '(' despu\u00e9s de 'done'.");
            match(TipoToken.PAR_CIERRA, "Se espera ')' despu\u00e9s de done().");
            match(TipoToken.EOF, "Algo sali\u00f3 mal al final del programa.");
        } else {
            while (tokenActual().getTipo() != TipoToken.EOF) pos++;
        }
    }

    private NodoArbol bloque() {
        NodoArbol nodo = new NodoArbol("bloque");
        while (tokenActual().getTipo() != TipoToken.EOF &&
               tokenActual().getTipo() != TipoToken.KW_DONE &&
               tokenActual().getTipo() != TipoToken.LLAVE_CIERRA &&
               tokenActual().getTipo() != TipoToken.KW_ELSE) {
            nodo.agregarHijo(sentencia());
            panicoUsado = false;
        }
        return nodo;
    }

    private NodoArbol sentencia() {
        if (panicoUsado) return null;
        switch (tokenActual().getTipo()) {
            case KW_FOR:    return forStmt();
            case KW_IF:     return ifStmt();
            case KW_LET:    return letStmt();
            case FUNC_MOVE:
            case FUNC_COPY:
            case FUNC_DELETE:
            case FUNC_RENAME:
            case FUNC_LOG:  return funcCall();
            default:
                if (tokenActual().getTipo() != TipoToken.DESCONOCIDO) {
                    errores.agregarError(TablaErrores.ERROR_INSTRUCCION_INVALIDA,
                        lFinal ? lineaFinal : tokenActual().getLinea(),
                        "Se espera una acci\u00f3n como scan, for, if, let o una funci\u00f3n en vez de: " + tokenActual().getLexema());
                }
                pos++; // consume the offending token to avoid infinite loop
                panico();
                return null;
        }
    }

    private NodoArbol scanStmt() {
        NodoArbol nodo = new NodoArbol("scanStmt");
        nodo.agregarHijo(match(TipoToken.KW_SCAN, "Se espera 'scan'."));
        nodo.agregarHijo(match(TipoToken.LIT_RUTA, "Se espera una ruta despu\u00e9s de 'scan'."));
        nodo.agregarHijo(match(TipoToken.KW_AS, "Se espera 'as' despu\u00e9s de la ruta."));
        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera un identificador despu\u00e9s de 'as'."));
        return nodo;
    }

    private NodoArbol forStmt() {
        NodoArbol nodo = new NodoArbol("forStmt");
        nodo.agregarHijo(match(TipoToken.KW_FOR, "Se espera 'for'."));
        nodo.agregarHijo(match(TipoToken.KW_EACH, "Se espera 'each' despu\u00e9s de 'for'."));
        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera un identificador despu\u00e9s de 'for each'."));
        nodo.agregarHijo(match(TipoToken.KW_IN, "Se espera 'in'."));
        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera un identificador despu\u00e9s de 'in'."));
        nodo.agregarHijo(match(TipoToken.LLAVE_ABRE, "Se espera '{' para abrir el bloque del for."));
        nodo.agregarHijo(bloque());
        nodo.agregarHijo(match(TipoToken.LLAVE_CIERRA, "Se espera '}' para cerrar el bloque del for."));
        return nodo;
    }

    private NodoArbol ifStmt() {
        NodoArbol nodo = new NodoArbol("ifStmt");
        nodo.agregarHijo(match(TipoToken.KW_IF, "Se espera 'if'."));
        nodo.agregarHijo(condicion());
        nodo.agregarHijo(match(TipoToken.LLAVE_ABRE, "Se espera '{' para abrir el bloque del if."));
        nodo.agregarHijo(bloque());
        nodo.agregarHijo(match(TipoToken.LLAVE_CIERRA, "Se espera '}' para cerrar el bloque del if."));
        nodo.agregarHijo(elseOpt());
        return nodo;
    }

    private NodoArbol elseOpt() {
        if (tokenActual().getTipo() == TipoToken.KW_ELSE) {
            NodoArbol nodo = new NodoArbol("elseOpt");
            nodo.agregarHijo(match(TipoToken.KW_ELSE, "Se espera 'else'."));
            if (tokenActual().getTipo() == TipoToken.KW_IF) {
                nodo.agregarHijo(ifStmt());
            } else {
                nodo.agregarHijo(match(TipoToken.LLAVE_ABRE, "Se espera '{' para abrir el bloque del else."));
                nodo.agregarHijo(bloque());
                nodo.agregarHijo(match(TipoToken.LLAVE_CIERRA, "Se espera '}' para cerrar el bloque del else."));
            }
            return nodo;
        }
        return null;
    }

    private NodoArbol letStmt() {
        NodoArbol nodo = new NodoArbol("letStmt");
        nodo.agregarHijo(match(TipoToken.KW_LET, "Se espera 'let'."));
        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera un identificador despu\u00e9s de 'let'."));
        nodo.agregarHijo(match(TipoToken.OP_ASIG, "Se espera '=' despu\u00e9s del identificador."));
        nodo.agregarHijo(expresion());
        return nodo;
    }

    private NodoArbol funcCall() {
        NodoArbol nodo = new NodoArbol("funcCall");
        TipoToken tipo = tokenActual().getTipo();
        boolean esLog = false;
        String nombre = "";
        switch (tipo) {
            case FUNC_MOVE:   nombre = "move"; break;
            case FUNC_COPY:   nombre = "copy"; break;
            case FUNC_DELETE: nombre = "delete"; break;
            case FUNC_RENAME: nombre = "rename"; break;
            case FUNC_LOG:    nombre = "log"; esLog = true; break;
            default:
                errores.agregarError(TablaErrores.ERROR_FUNCION_INVALIDA_SIN,
                    tokenActual().getLinea(), "Funci\u00f3n no reconocida.");
                panico();
                return nodo;
        }
        nodo.agregarHijo(match(tipo, "Se espera '" + nombre + "'."));
        nodo.agregarHijo(match(TipoToken.PAR_ABRE, "Se espera '(' despu\u00e9s de '" + nombre + "'."));
        if (esLog) {
            nodo.agregarHijo(argumentos());
        } else {
            nodo.agregarHijo(argumentosRuta());
        }
        nodo.agregarHijo(match(TipoToken.PAR_CIERRA, "Se espera ')' para cerrar la funci\u00f3n '" + nombre + "'."));
        return nodo;
    }

    private NodoArbol argumentos() {
        NodoArbol nodo = new NodoArbol("argumentos");
        if (tokenActual().getTipo() != TipoToken.PAR_CIERRA) {
            nodo.agregarHijo(expresion());
            nodo.agregarHijo(restoArgs());
        }
        return nodo;
    }

    private NodoArbol restoArgs() {
        if (tokenActual().getTipo() == TipoToken.COMA) {
            NodoArbol nodo = new NodoArbol("restoArgs");
            nodo.agregarHijo(match(TipoToken.COMA, "Se espera ',' para separar argumentos."));
            nodo.agregarHijo(expresion());
            nodo.agregarHijo(restoArgs());
            return nodo;
        }
        return null;
    }

    private NodoArbol expresion() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("expresion");
        nodo.agregarHijo(termino());
        nodo.agregarHijo(restoExpresion());
        return nodo;
    }

    private NodoArbol restoExpresion() {
        if (tokenActual().getTipo() == TipoToken.OP_SUMA) {
            NodoArbol nodo = new NodoArbol("restoExpresion");
            nodo.agregarHijo(match(TipoToken.OP_SUMA, "Se espera '+' para la expresi\u00f3n."));
            nodo.agregarHijo(termino());
            nodo.agregarHijo(restoExpresion());
            return nodo;
        }
        return null;
    }

    private NodoArbol termino() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("termino");
        switch (tokenActual().getTipo()) {
            case IDENTIFICADOR:
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera un identificador."));
                nodo.agregarHijo(accesoAtributo());
                break;
            case LIT_RUTA:
            case LIT_EXT:
            case LIT_TAMANIO:
            case LIT_ENTERO:
            case LIT_CADENA:
                nodo.agregarHijo(literal());
                break;
            case PAR_ABRE:
                nodo.agregarHijo(match(TipoToken.PAR_ABRE, "Se espera '(' para la expresi\u00f3n."));
                nodo.agregarHijo(expresion());
                nodo.agregarHijo(match(TipoToken.PAR_CIERRA, "Se espera ')' para cerrar la expresi\u00f3n."));
                break;
            default:
                errores.agregarError(TablaErrores.ERROR_EXPRESION_INVALIDA,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba un literal, identificador o '(' en la expresi\u00f3n.");
                panico();
        }
        return nodo;
    }

    private NodoArbol accesoAtributo() {
        if (tokenActual().getTipo() == TipoToken.OP_PUNTO) {
            NodoArbol nodo = new NodoArbol("accesoAtributo");
            nodo.agregarHijo(match(TipoToken.OP_PUNTO, "Se espera '.' para acceder al atributo."));
            TipoToken attr = tokenActual().getTipo();
            switch (attr) {
                case ATTR_EXT:
                case ATTR_SIZE:
                case ATTR_NAME:
                case ATTR_DATE:
                    nodo.agregarHijo(match(attr, "Atributo no v\u00e1lido. Use ext, size, name o date."));
                    break;
                default:
                    errores.agregarError(TablaErrores.ERROR_ATRIBUTO_INVALIDO,
                        tokenActual().getLinea(),
                        "Se esperaba un atributo (ext, size, name, date) despu\u00e9s de '.'.");
                    panico();
            }
            return nodo;
        }
        return null;
    }

    private NodoArbol literal() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("literal");
        TipoToken t = tokenActual().getTipo();
        switch (t) {
            case LIT_RUTA:
                nodo.agregarHijo(match(TipoToken.LIT_RUTA, "Se esperaba una ruta."));
                break;
            case LIT_EXT:
                nodo.agregarHijo(match(TipoToken.LIT_EXT, "Se esperaba una extensi\u00f3n."));
                break;
            case LIT_TAMANIO:
                nodo.agregarHijo(match(TipoToken.LIT_TAMANIO, "Se esperaba un tama\u00f1o."));
                break;
            case LIT_ENTERO:
                nodo.agregarHijo(match(TipoToken.LIT_ENTERO, "Se esperaba un n\u00famero entero."));
                break;
            case LIT_CADENA:
                nodo.agregarHijo(match(TipoToken.LIT_CADENA, "Se esperaba una cadena."));
                break;
            default:
                errores.agregarError(TablaErrores.ERROR_EXPRESION_INVALIDA,
                    tokenActual().getLinea(), "Se esperaba un literal v\u00e1lido.");
                panico();
        }
        return nodo;
    }

    private NodoArbol literalRuta() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("literal");
        TipoToken t = tokenActual().getTipo();
        switch (t) {
            case LIT_RUTA:
                nodo.agregarHijo(match(TipoToken.LIT_RUTA, "Se esperaba una ruta."));
                break;
            case LIT_EXT:
                nodo.agregarHijo(match(TipoToken.LIT_EXT, "Se esperaba una extensi\u00f3n."));
                break;
            case LIT_TAMANIO:
                nodo.agregarHijo(match(TipoToken.LIT_TAMANIO, "Se esperaba un tama\u00f1o."));
                break;
            case LIT_ENTERO:
                nodo.agregarHijo(match(TipoToken.LIT_ENTERO, "Se esperaba un n\u00famero entero."));
                break;
            default:
                errores.agregarError(TablaErrores.ERROR_EXPRESION_INVALIDA,
                    tokenActual().getLinea(), "Se esperaba un literal v\u00e1lido (ruta, extensi\u00f3n, tama\u00f1o o n\u00famero).");
                panico();
        }
        return nodo;
    }

    private NodoArbol terminoRuta() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("termino");
        switch (tokenActual().getTipo()) {
            case IDENTIFICADOR:
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera un identificador."));
                nodo.agregarHijo(accesoAtributo());
                break;
            case LIT_RUTA:
            case LIT_EXT:
            case LIT_TAMANIO:
            case LIT_ENTERO:
                nodo.agregarHijo(literalRuta());
                break;
            case PAR_ABRE:
                nodo.agregarHijo(match(TipoToken.PAR_ABRE, "Se espera '(' para la expresi\u00f3n."));
                nodo.agregarHijo(expresionRuta());
                nodo.agregarHijo(match(TipoToken.PAR_CIERRA, "Se espera ')' para cerrar la expresi\u00f3n."));
                break;
            default:
                errores.agregarError(TablaErrores.ERROR_EXPRESION_INVALIDA,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba un literal, identificador o '(' en la expresi\u00f3n.");
                panico();
        }
        return nodo;
    }

    private NodoArbol expresionRuta() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("expresion");
        nodo.agregarHijo(terminoRuta());
        nodo.agregarHijo(restoExpresionRuta());
        return nodo;
    }

    private NodoArbol restoExpresionRuta() {
        if (tokenActual().getTipo() == TipoToken.OP_SUMA) {
            NodoArbol nodo = new NodoArbol("restoExpresion");
            nodo.agregarHijo(match(TipoToken.OP_SUMA, "Se espera '+' para la expresi\u00f3n."));
            nodo.agregarHijo(terminoRuta());
            nodo.agregarHijo(restoExpresionRuta());
            return nodo;
        }
        return null;
    }

    private NodoArbol argumentosRuta() {
        NodoArbol nodo = new NodoArbol("argumentos");
        if (tokenActual().getTipo() != TipoToken.PAR_CIERRA) {
            nodo.agregarHijo(expresionRuta());
            nodo.agregarHijo(restoArgsRuta());
        }
        return nodo;
    }

    private NodoArbol restoArgsRuta() {
        if (tokenActual().getTipo() == TipoToken.COMA) {
            NodoArbol nodo = new NodoArbol("restoArgs");
            nodo.agregarHijo(match(TipoToken.COMA, "Se espera ',' para separar argumentos."));
            nodo.agregarHijo(expresionRuta());
            nodo.agregarHijo(restoArgsRuta());
            return nodo;
        }
        return null;
    }

    private NodoArbol condicion() {
        return condicionOr();
    }

    private NodoArbol condicionOr() {
        NodoArbol nodo = new NodoArbol("condicionOr");
        nodo.agregarHijo(condicionAnd());
        nodo.agregarHijo(restoCondOr());
        return nodo;
    }

    private NodoArbol restoCondOr() {
        if (tokenActual().getTipo() == TipoToken.OP_OR) {
            NodoArbol nodo = new NodoArbol("restoCondOr");
            nodo.agregarHijo(match(TipoToken.OP_OR, "Se espera '||'."));
            nodo.agregarHijo(condicionAnd());
            nodo.agregarHijo(restoCondOr());
            return nodo;
        }
        return null;
    }

    private NodoArbol condicionAnd() {
        NodoArbol nodo = new NodoArbol("condicionAnd");
        nodo.agregarHijo(condicionSimple());
        nodo.agregarHijo(restoCondAnd());
        return nodo;
    }

    private NodoArbol restoCondAnd() {
        if (tokenActual().getTipo() == TipoToken.OP_AND) {
            NodoArbol nodo = new NodoArbol("restoCondAnd");
            nodo.agregarHijo(match(TipoToken.OP_AND, "Se espera '&&'."));
            nodo.agregarHijo(condicionSimple());
            nodo.agregarHijo(restoCondAnd());
            return nodo;
        }
        return null;
    }

    private NodoArbol condicionSimple() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("condicionSimple");
        if (tokenActual().getTipo() == TipoToken.OP_NOT) {
            nodo.agregarHijo(match(TipoToken.OP_NOT, "Se espera '!'."));
            nodo.agregarHijo(condicionSimple());
        } else if (tokenActual().getTipo() == TipoToken.PAR_ABRE) {
            nodo.agregarHijo(match(TipoToken.PAR_ABRE, "Se espera '(' para la condici\u00f3n."));
            nodo.agregarHijo(condicion());
            nodo.agregarHijo(match(TipoToken.PAR_CIERRA, "Se espera ')' para cerrar la condici\u00f3n."));
        } else {
            nodo.agregarHijo(expresion());
            nodo.agregarHijo(opComparacion());
            nodo.agregarHijo(expresion());
        }
        return nodo;
    }

    private NodoArbol opComparacion() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("opComparacion");
        TipoToken t = tokenActual().getTipo();
        switch (t) {
            case OP_IGUAL:
            case OP_DISTINTO:
            case OP_MAYOR:
            case OP_MENOR:
            case OP_MAYOR_EQ:
            case OP_MENOR_EQ:
                nodo.agregarHijo(match(t, "Se esperaba un operador de comparaci\u00f3n."));
                break;
            default:
                errores.agregarError(TablaErrores.ERROR_EXPRESION_INVALIDA,
                    tokenActual().getLinea(),
                    "Se esperaba un operador de comparaci\u00f3n (==, !=, >, <, >=, <=).");
                panico();
        }
        return nodo;
    }
}
