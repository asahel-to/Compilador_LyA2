package parser;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class SymbolInfo {
    int line;
    String type; // "number", "string", "path", "collection", "unknown"
    SymbolInfo(int line, String type) { this.line = line; this.type = type; }
}

class FunctionSignature {
    String name;
    String[] argTypes; // null means flexible types
    int minArgs;
    int maxArgs;
    String returnType; // e.g. "void" or type name
    FunctionSignature(String name, String[] argTypes, int minArgs, int maxArgs, String returnType) {
        this.name = name; this.argTypes = argTypes; this.minArgs = minArgs; this.maxArgs = maxArgs; this.returnType = returnType;
    }
}

public class SymbolTable {
    private Deque<Map<String, SymbolInfo>> scopes = new ArrayDeque<>();
    private Map<String, FunctionSignature> signatures = new HashMap<>();
    private Set<String> knownTypes = new HashSet<>();

    public SymbolTable() {
        enterScope();
        initBuiltins();
    }

    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    public void exitScope() {
        if (!scopes.isEmpty()) scopes.pop();
    }

    public boolean declare(String name, int linea) {
        return declare(name, linea, "unknown");
    }

    public boolean declare(String name, int linea, String type) {
        Map<String,SymbolInfo> top = scopes.peek();
        if (top.containsKey(name)) return false; // redeclaracion en mismo scope
        top.put(name, new SymbolInfo(linea, type));
        return true;
    }

    public boolean isDeclared(String name) {
        for (Map<String,SymbolInfo> s : scopes) {
            if (s.containsKey(name)) return true;
        }
        return false;
    }

    public void registerSignature(FunctionSignature sig) {
        signatures.put(sig.name, sig);
    }

    public FunctionSignature getSignature(String name) {
        return signatures.get(name);
    }

    public boolean hasSignature(String name) { return signatures.containsKey(name); }

    private void initBuiltins() {
        // known simple types
        Collections.addAll(knownTypes, "path", "number", "string", "collection", "void", "unknown", "int");

        // move(src, dest) : void
        registerSignature(new FunctionSignature("move", new String[]{"int","path"}, 2, 2, "void"));
        // copy(src, dest) : void
        registerSignature(new FunctionSignature("copy", new String[]{"int","path"}, 2, 2, "void"));
        // rename(src, dest) : void
        registerSignature(new FunctionSignature("rename", new String[]{"int","path"}, 2, 2, "void"));
        // delete(target) : void
        registerSignature(new FunctionSignature("delete", new String[]{"int"}, 1, 1, "void"));
        // log(x [, level]) : void  (flexible types)
        registerSignature(new FunctionSignature("log", null, 1, 2, "void"));
    }

    public Integer getDeclarationLine(String name) {
        for (Map<String,SymbolInfo> s : scopes) {
            if (s.containsKey(name)) return s.get(name).line;
        }
        return null;
    }

    public String getType(String name) {
        for (Map<String,SymbolInfo> s : scopes) {
            if (s.containsKey(name)) return s.get(name).type;
        }
        return null;
    }
    public void setType(String name, String type) {
        for (Map<String,SymbolInfo> s : scopes) {
            if (s.containsKey(name)) { s.get(name).type = type; return; }
        }
    }
}
