package parser;

import java.util.ArrayList;
import java.util.List;

public class NodoArbol {
    private String valor;
    private List<NodoArbol> hijos;
    private int linea = -1;

    public NodoArbol(String valor) {
        this.valor = valor;
        this.hijos = new ArrayList<>();
    }

    public NodoArbol(String valor, int linea) {
        this.valor = valor;
        this.hijos = new ArrayList<>();
        this.linea = linea;
    }

    public void agregarHijo(NodoArbol hijo) {
        if (hijo != null) {
            this.hijos.add(hijo);
        }
    }

    public String getValor() { return valor; }
    public List<NodoArbol> getHijos() { return hijos; }
    public int getLinea() { return linea; }
}
