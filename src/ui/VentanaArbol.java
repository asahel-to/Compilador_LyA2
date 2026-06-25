package ui;

import javax.swing.*;
import java.awt.*;
import parser.NodoArbol;

public class VentanaArbol extends JFrame {
    public VentanaArbol(NodoArbol raiz) {
        setTitle("\u00c1rbol de Derivaci\u00f3n Sint\u00e1ctico - SortScript");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        PanelArbol panelDibujo = new PanelArbol(raiz);
        JScrollPane scroll = new JScrollPane(panelDibujo);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        add(scroll);
    }
}
