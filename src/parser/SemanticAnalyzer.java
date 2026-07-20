package parser;

import java.util.List;

public class SemanticAnalyzer {
    public Errores errores = new Errores();
    private SymbolTable symbols = new SymbolTable();

    public void analizar(NodoArbol raiz) {
        if (raiz == null) return;
        visitProgram(raiz);
    }

    private void visitProgram(NodoArbol nodo) {
        // program structure: [scanStmt, stmts..., done, (, ), EOF]
        for (NodoArbol h : nodo.getHijos()) visit(h);
    }

    private void visit(NodoArbol nodo) {
        if (nodo == null) return;
        String v = nodo.getValor();
        switch (v) {
            case "scanStmt":
                handleScan(nodo);
                break;
            case "letStmt":
                handleLet(nodo);
                break;
            case "forStmt":
                handleFor(nodo);
                break;
            case "funcCall":
                handleFuncCall(nodo);
                break;
            case "ifStmt":
                handleIf(nodo);
                break;
            default:
                // for expression-like nodes, detect identifier usage
                if ("termino".equals(v) || "terminoRuta".equals(v) || "expresion".equals(v)
                    || "expresionRuta".equals(v) || "argumentos".equals(v) || "argumentosRuta".equals(v)) {
                    checkIdentifierUsage(nodo);
                    checkAttributeAccessInTerm(nodo);
                } else {
                    // traverse children
                    for (NodoArbol h : nodo.getHijos()) visit(h);
                }
        }
    }

    private void checkAttributeAccessInTerm(NodoArbol nodo) {
        if (nodo == null) return;
        if (!"termino".equals(nodo.getValor()) && !"terminoRuta".equals(nodo.getValor())) return;
        List<NodoArbol> ch = nodo.getHijos();
        if (ch.size() < 2) return;
        NodoArbol acceso = ch.get(1);
        if (acceso == null || !"accesoAtributo".equals(acceso.getValor())) return;
        if (acceso.getHijos().size() < 2) return;
        NodoArbol attrLeaf = acceso.getHijos().get(1);
        String attrName = attrLeaf.getValor();
        NodoArbol base = ch.get(0);
        String baseVal = base.getValor();
        int linea = attrLeaf.getLinea();
        String low = attrName == null ? "" : attrName.toLowerCase();
        if (!(low.equals("ext") || low.equals("size") || low.equals("name") || low.equals("date"))) {
            errores.agregarError(TablaErrores.ERROR_ATRIBUTO_TIPO, linea, "Atributo inválido: '" + attrName + "'.");
            return;
        }
        if (baseVal != null && baseVal.matches("\\d+(KB|MB|GB)")) {
            errores.agregarError(TablaErrores.ERROR_ATRIBUTO_TIPO, base.getLinea(),
                "No se puede acceder al atributo '" + attrName + "' en un literal numérico.");
            return;
        }
        if (esIdentificadorLexico(baseVal)) {
            if (symbols.isDeclared(baseVal)) {
                String t = symbols.getType(baseVal);
                if (t != null && ("number".equals(t) || "string".equals(t))) {
                    errores.agregarError(TablaErrores.ERROR_ATRIBUTO_TIPO, base.getLinea(),
                        "No se puede acceder al atributo '" + attrName + "' en un valor de tipo '" + t + "' ('" + baseVal + "').");
                }
            }
        }
    }

    private void handleScan(NodoArbol nodo) {
        // children: 'scan', ruta, 'as', identificador
        List<NodoArbol> ch = nodo.getHijos();
        if (ch.size() >= 4) {
            NodoArbol idNode = ch.get(3);
            String name = idNode.getValor();
            int linea = idNode.getLinea();
            if (!symbols.declare(name, linea, "collection")) {
                errores.agregarError(TablaErrores.ERROR_YA_DECLARADO, linea,
                    "Identificador '" + name + "' ya declarado en este alcance.");
            }
        }
    }

    private void handleLet(NodoArbol nodo) {
        // children: 'let', identificador, '=', expresion
        List<NodoArbol> ch = nodo.getHijos();
        if (ch.size() >= 2) {
            NodoArbol idNode = ch.get(1);
            String name = idNode.getValor();
            int linea = idNode.getLinea();
            // detect simple type from expression (inspect first leaf)
            String inferred = "unknown";
            if (ch.size() >= 4) {
                NodoArbol expr = ch.get(3);
                String fv = firstLeafValue(expr);
                if (fv != null && fv.startsWith("\"") && fv.endsWith("\"")) {
                    String inner = fv.substring(1, fv.length()-1);
                    if (inner.startsWith("/") || inner.startsWith(".") || inner.contains("/") || inner.contains("\\")) inferred = "path";
                    else inferred = "string";
                } else if (fv != null && fv.matches("\\d+(KB|MB|GB)")) inferred = "number";
                else if (fv != null && fv.matches("\\d+")) inferred = "int";
                else if (fv != null && (fv.startsWith(".") || fv.startsWith("/") || fv.contains("/") || fv.contains("\\"))) inferred = "path";
                else if (fv != null && esIdentificadorLexico(fv)) {
                    String t = symbols.getType(fv);
                    inferred = t == null ? "unknown" : t;
                }
            }
            if (!symbols.declare(name, linea, inferred)) {
                errores.agregarError(TablaErrores.ERROR_YA_DECLARADO, linea,
                    "Identificador '" + name + "' ya declarado en este alcance.");
            }
            // check expression usage
            if (ch.size() >= 4) visit(ch.get(3));
        }
    }

    private String firstLeafValue(NodoArbol nodo) {
        if (nodo == null) return null;
        NodoArbol cur = nodo;
        while (cur != null && !cur.getHijos().isEmpty()) {
            cur = cur.getHijos().get(0);
        }
        return cur == null ? null : cur.getValor();
    }

    private NodoArbol firstLeaf(NodoArbol nodo) {
        if (nodo == null) return null;
        NodoArbol cur = nodo;
        while (cur != null && !cur.getHijos().isEmpty()) {
            cur = cur.getHijos().get(0);
        }
        return cur;
    }

    private void handleFor(NodoArbol nodo) {
        // children: for, each, iterator(id), in, source(id), '{', bloque, '}'
        List<NodoArbol> ch = nodo.getHijos();
        // require at least 7 children to ensure source was parsed (index 4)
        if (ch.size() >= 7) {
            NodoArbol source = ch.get(4);
            String sourceName = source.getValor();
            if (!symbols.isDeclared(sourceName)) {
                errores.agregarError(TablaErrores.ERROR_NO_DECLARADO,
                    source.getLinea(), "Identificador '" + sourceName + "' no declarado antes de su uso en 'in'.");
            } else {
                String sourceType = symbols.getType(sourceName);
                if (sourceType != null && !"collection".equals(sourceType)) {
                    errores.agregarError(TablaErrores.ERROR_ARGUMENTOS_TIPO,
                        source.getLinea(), "Fuente del 'for each' debe ser tipo 'collection', pero '" + sourceName + "' es tipo '" + sourceType + "'.");
                }
            }
            // iterator must be declared with let before for each
            NodoArbol iterator = ch.get(2);
            String itName = iterator.getValor();
            if (!symbols.isDeclared(itName)) {
                errores.agregarError(TablaErrores.ERROR_NO_DECLARADO, iterator.getLinea(),
                    "Identificador '" + itName + "' no declarado antes de su uso en 'for each'.");
            }
            // create inner scope for block
            symbols.enterScope();
            // visit block (6)
            visit(ch.get(6));
            symbols.exitScope();
        } else {
            symbols.enterScope();
            for (NodoArbol h : ch) visit(h);
            symbols.exitScope();
        }
    }

    private void handleIf(NodoArbol nodo) {
        List<NodoArbol> ch = nodo.getHijos();
        if (ch.size() >= 6) {
            visit(ch.get(1));
            symbols.enterScope();
            if (ch.get(3) != null) visit(ch.get(3));
            symbols.exitScope();
            handleElseOpt(ch.get(5));
        } else {
            symbols.enterScope();
            for (NodoArbol h : ch) visit(h);
            symbols.exitScope();
        }
    }

    private void handleElseOpt(NodoArbol nodo) {
        if (nodo == null) return;
        List<NodoArbol> ch = nodo.getHijos();
        if (ch.size() < 2) return;
        NodoArbol second = ch.get(1);
        if ("ifStmt".equals(second.getValor())) {
            handleIf(second);
        } else {
            symbols.enterScope();
            if (ch.size() >= 3 && ch.get(2) != null) visit(ch.get(2));
            symbols.exitScope();
        }
    }

    private void handleFuncCall(NodoArbol nodo) {
        List<NodoArbol> ch = nodo.getHijos();
        // require at least 4 children: name + '(' + args + ')'
        if (ch.size() >= 4) {
            NodoArbol funcNameNode = ch.get(0);
            String funcName = funcNameNode.getValor();
            String funcKey = funcName.toLowerCase();
            NodoArbol argsNode = null;
            for (NodoArbol c : ch) if (c != null && ("argumentos".equals(c.getValor()) || "argumentosRuta".equals(c.getValor()))) argsNode = c;

            int argCount = countArguments(argsNode);

            if (symbols.hasSignature(funcKey)) {
                FunctionSignature sig = symbols.getSignature(funcKey);
                if (argCount < sig.minArgs || argCount > sig.maxArgs) {
                    errores.agregarError(TablaErrores.ERROR_ARGUMENTOS_CANTIDAD, funcNameNode.getLinea(),
                        "Función '" + funcName + "' espera entre " + sig.minArgs + " y " + sig.maxArgs + " argumento(s), recibidos: " + argCount);
                } else if (sig.argTypes != null) {
                    List<NodoArbol> argExprs = collectArgumentExpressions(argsNode);
                    for (int i = 0; i < Math.min(argExprs.size(), sig.argTypes.length); i++) {
                        NodoArbol expr = argExprs.get(i);
                        String inferred = inferTypeOfExpression(expr);
                        String expected = sig.argTypes[i];
                        if (inferred != null && expected != null && !inferred.equals("unknown") && !inferred.equals(expected)) {
                            int linea = expr.getLinea();
                            if (linea <= 0) { NodoArbol leaf = firstLeaf(expr); if (leaf != null) linea = leaf.getLinea(); }
                            errores.agregarError(TablaErrores.ERROR_ARGUMENTOS_TIPO, linea > 0 ? linea : 1,
                                "Función '" + funcName + "' argumento " + (i+1) + ": se esperaba tipo '" + expected + "', encontrado '" + inferred + "'.");
                        }
                    }
                }
            }

            for (NodoArbol c : ch) visit(c);
        } else {
            // incomplete parse — still check identifiers in children
            for (NodoArbol c : ch) visit(c);
        }
    }

    private List<NodoArbol> collectArgumentExpressions(NodoArbol args) {
        java.util.ArrayList<NodoArbol> list = new java.util.ArrayList<>();
        if (args == null) return list;
        // first expresion
        if (!args.getHijos().isEmpty()) {
            list.add(args.getHijos().get(0));
        }
        // restoArgs chain
        NodoArbol resto = null;
        if (args.getHijos().size() >= 2) resto = args.getHijos().get(1);
        while (resto != null) {
            // restoArgs -> COMA, expresion, restoArgs
            if (!resto.getHijos().isEmpty()) list.add(resto.getHijos().get(1));
            if (resto.getHijos().size() >= 3) resto = resto.getHijos().get(2);
            else resto = null;
        }
        return list;
    }

    private String inferTypeOfExpression(NodoArbol expr) {
        if (expr == null) return "unknown";
        String fv = firstLeafValue(expr);
        if (fv == null) return "unknown";
        // if quoted string, inspect inner content to detect path-like literal
        if (fv.startsWith("\"") && fv.endsWith("\"")) {
            String inner = fv.substring(1, fv.length()-1);
            if (inner.startsWith("/") || inner.startsWith(".") || inner.contains("/") || inner.contains("\\")) return "path";
            return "string";
        }
        if (fv.matches("\\d+(KB|MB|GB)")) return "number";
        if (fv.matches("\\d+")) return "int";
        if (fv.startsWith(".") || fv.startsWith("/") || fv.contains("/") || fv.contains("\\")) return "path";
        if (esIdentificadorLexico(fv)) {
            String t = symbols.getType(fv);
            return t == null ? "unknown" : t;
        }
        return "unknown";
    }

    private int countArguments(NodoArbol args) {
        if (args == null) return 0;
        // argumentos -> expresion (first) + restoArgs
        if ("argumentos".equals(args.getValor()) || "argumentosRuta".equals(args.getValor())) {
            int count = 0;
            // count first expresion if present
            if (!args.getHijos().isEmpty()) count++;
            // count restoArgs recursively
            NodoArbol resto = null;
            if (args.getHijos().size() >= 2) resto = args.getHijos().get(1);
            while (resto != null) {
                // restoArgs -> COMA, expresion, restoArgs
                if (!resto.getHijos().isEmpty()) count++; // expresion
                if (resto.getHijos().size() >= 3) resto = resto.getHijos().get(2);
                else resto = null;
            }
            return count;
        }
        return 0;
    }

    // detect identifier usage in nodes
    private void checkIdentifierUsage(NodoArbol nodo) {
        if (nodo == null) return;
        if (nodo.getHijos().isEmpty()) return;
        // if first child is a leaf and looks like an identifier
        NodoArbol first = nodo.getHijos().get(0);
        if (first.getHijos().isEmpty() && first.getLinea() > 0) {
            String val = first.getValor();
            if (esIdentificadorLexico(val)) {
                if (!symbols.isDeclared(val)) {
                    errores.agregarError(TablaErrores.ERROR_NO_DECLARADO, first.getLinea(),
                        "Identificador '" + val + "' no declarado antes de su uso.");
                }
            }
        }
        // continue deeper and also check attribute access in child terms
        for (NodoArbol h : nodo.getHijos()) {
            checkAttributeAccessInTerm(h);
            checkIdentifierUsage(h);
        }
    }

    private boolean esIdentificadorLexico(String s) {
        if (s == null) return false;
        // string literal
        if (s.startsWith("\"") && s.endsWith("\"")) return false;
        // numeric literal
        if (s.matches("\\d+(KB|MB|GB)?")) return false;
        // path or extension starting with dot or slash
        if (s.startsWith(".") || s.startsWith("/") || s.contains("/") || s.contains("\\")) return false;
        // attributes or keywords (lowercase names like ext,size,name,date)
        String low = s.toLowerCase();
        if (low.equals("ext") || low.equals("size") || low.equals("name") || low.equals("date")) return false;
        // if contains non identifier chars
        return s.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }
}
