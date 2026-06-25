package ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import parser.NodoArbol;

public class PanelArbol extends JPanel {
    private NodoArbol raiz;
    private final int RADIO = 20;
    private final int DIAMETRO = RADIO * 2;
    private final int V_GAP = 70;
    private final int H_GAP = 20;
    private Map<NodoArbol, Point> posiciones = new HashMap<>();
    private Map<NodoArbol, Integer> anchosSubarbol = new HashMap<>();

    public PanelArbol(NodoArbol raiz) {
        this.raiz = raiz;
        this.setBackground(new Color(30, 30, 30));
        calcularPosiciones();
    }

    private void calcularPosiciones() {
        posiciones.clear();
        anchosSubarbol.clear();
        if (raiz != null) {
            calcularAncho(raiz);
            calcularCoordenadas(raiz, H_GAP, H_GAP + RADIO);
            int anchoTotal = anchosSubarbol.get(raiz) + H_GAP * 2;
            int profundidad = obtenerProfundidad(raiz);
            int altoTotal = (profundidad * V_GAP) + DIAMETRO + V_GAP;
            setPreferredSize(new Dimension(anchoTotal, altoTotal));
        }
    }

    private int calcularAncho(NodoArbol nodo) {
        if (nodo.getHijos().isEmpty()) {
            int ancho = DIAMETRO + H_GAP;
            anchosSubarbol.put(nodo, ancho);
            return ancho;
        }
        int anchoTotal = 0;
        for (NodoArbol hijo : nodo.getHijos())
            anchoTotal += calcularAncho(hijo);
        anchosSubarbol.put(nodo, anchoTotal);
        return anchoTotal;
    }

    private void calcularCoordenadas(NodoArbol nodo, int xInfo, int y) {
        int xCentro;
        if (nodo.getHijos().isEmpty()) {
            xCentro = xInfo + (DIAMETRO / 2);
        } else {
            int xInicioHijos = xInfo;
            for (NodoArbol hijo : nodo.getHijos()) {
                calcularCoordenadas(hijo, xInicioHijos, y + V_GAP);
                xInicioHijos += anchosSubarbol.get(hijo);
            }
            Point posPrimerHijo = posiciones.get(nodo.getHijos().get(0));
            Point posUltimoHijo = posiciones.get(nodo.getHijos().get(nodo.getHijos().size() - 1));
            xCentro = (posPrimerHijo.x + posUltimoHijo.x) / 2;
        }
        posiciones.put(nodo, new Point(xCentro, y));
    }

    private int obtenerProfundidad(NodoArbol nodo) {
        if (nodo.getHijos().isEmpty()) return 1;
        int maxProf = 0;
        for (NodoArbol hijo : nodo.getHijos())
            maxProf = Math.max(maxProf, obtenerProfundidad(hijo));
        return 1 + maxProf;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (raiz == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        dibujarLineas(g2, raiz);
        dibujarNodos(g2, raiz);
    }

    private void dibujarLineas(Graphics2D g, NodoArbol nodo) {
        Point pPadre = posiciones.get(nodo);
        g.setColor(Color.GRAY);
        for (NodoArbol hijo : nodo.getHijos()) {
            Point pHijo = posiciones.get(hijo);
            g.drawLine(pPadre.x, pPadre.y + RADIO, pHijo.x, pHijo.y - RADIO);
            dibujarLineas(g, hijo);
        }
    }

    private void dibujarNodos(Graphics2D g, NodoArbol nodo) {
        Point p = posiciones.get(nodo);
        if (nodo.getHijos().isEmpty())
            g.setColor(new Color(100, 200, 100));
        else
            g.setColor(new Color(100, 150, 255));
        g.fillOval(p.x - RADIO, p.y - RADIO, DIAMETRO, DIAMETRO);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2));
        g.drawOval(p.x - RADIO, p.y - RADIO, DIAMETRO, DIAMETRO);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String texto = nodo.getValor();
        if (fm.stringWidth(texto) > DIAMETRO - 5)
            g.drawString(texto, p.x - fm.stringWidth(texto) / 2, p.y - RADIO - 5);
        else
            g.drawString(texto, p.x - fm.stringWidth(texto) / 2, p.y + fm.getAscent() / 2 - 2);
        for (NodoArbol hijo : nodo.getHijos())
            dibujarNodos(g, hijo);
    }
}
