package ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.text.*;
import lexico.*;
import parser.*;

public class CompiladorUI extends JFrame {

    // Colores del tema oscuro personalizado
    private static final Color FONDO_PRINCIPAL = new Color(18, 18, 24);
    private static final Color FONDO_SECUNDARIO = new Color(26, 26, 36);
    private static final Color FONDO_TERCIARIO = new Color(34, 34, 46);
    private static final Color COLOR_BORDE = new Color(48, 48, 64);
    private static final Color TEXTO_PRINCIPAL = new Color(220, 220, 235);
    private static final Color TEXTO_SECUNDARIO = new Color(140, 140, 165);
    private static final Color ACENTO_AMBER = new Color(255, 184, 76);
    private static final Color ACENTO_CORAL = new Color(255, 107, 107);
    private static final Color ACENTO_TEAL = new Color(77, 216, 200);
    private static final Color ACENTO_INDIGO = new Color(130, 120, 255);
    private static final Color EXITO = new Color(77, 216, 130);
    private static final Color ERROR_TXT = new Color(255, 120, 120);
    private static final Color FILA_PAR = new Color(26, 26, 38);
    private static final Color FILA_IMPAR = new Color(30, 30, 44);

    private JTextPane txtEntrada;
    private JTextArea txtConsola;
    private JTable tblTokens;
    private JTable tblErroresLexicos;
    private JTable tblErroresSintacticos;
    private JTable tblErroresSemanticos;
    private DefaultTableModel modeloTablaTokens;
    private DefaultTableModel modeloErroresLexicos;
    private DefaultTableModel modeloErroresSintacticos;
    private DefaultTableModel modeloErroresSemanticos;
    private JTabbedPane panelLateral;
    private NodoArbol raizActual = null;
    private JButton btnAnalizar;
    private JButton btnVerArbol;
    private JButton btnLimpiar;
    private JButton btnEjemplo;
    private JToggleButton btnTema;
    private JLabel lblStatus;

    private Highlighter.HighlightPainter semHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(255,120,120,80));
    private java.util.List<Object> semanticHighlights = new java.util.ArrayList<>();

    private SimpleAttributeSet estiloNormal = new SimpleAttributeSet();
    private SimpleAttributeSet estiloKeyword = new SimpleAttributeSet();
    private SimpleAttributeSet estiloFunc = new SimpleAttributeSet();
    private SimpleAttributeSet estiloCadena = new SimpleAttributeSet();
    private SimpleAttributeSet estiloNumero = new SimpleAttributeSet();
    private SimpleAttributeSet estiloComentario = new SimpleAttributeSet();

    public CompiladorUI() {
        setTitle("SortScript Compiler v1.0");
        setSize(1200, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setUndecorated(false);
        getContentPane().setBackground(FONDO_PRINCIPAL);

        iniciarEstilos();
        construirUI();

        setVisible(true);
    }

    private void iniciarEstilos() {
        StyleConstants.setBold(estiloKeyword, true);
        StyleConstants.setItalic(estiloComentario, true);
        StyleConstants.setForeground(estiloNormal, TEXTO_PRINCIPAL);
        StyleConstants.setForeground(estiloKeyword, new Color(200, 150, 255));
        StyleConstants.setForeground(estiloFunc, ACENTO_TEAL);
        StyleConstants.setForeground(estiloCadena, new Color(255, 200, 120));
        StyleConstants.setForeground(estiloNumero, new Color(130, 200, 255));
        StyleConstants.setForeground(estiloComentario, new Color(100, 100, 130));
    }

    private void construirUI() {
        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(FONDO_SECUNDARIO);
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE));
        barra.setPreferredSize(new Dimension(0, 28));

        lblStatus = new JLabel("  Listo");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(TEXTO_SECUNDARIO);
        barra.add(lblStatus, BorderLayout.WEST);

        return barra;
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(FONDO_SECUNDARIO);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE));
        barra.setPreferredSize(new Dimension(0, 52));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setBackground(FONDO_SECUNDARIO);
        izq.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        JLabel logo = new JLabel("SORTSCRIPT");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logo.setForeground(ACENTO_AMBER);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        izq.add(logo);

        JLabel sep1 = new JLabel("|");
        sep1.setForeground(COLOR_BORDE);
        sep1.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        sep1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 14));
        izq.add(sep1);

        JLabel subt = new JLabel("Compilador  \u2022  An\u00e1lisis L\u00e9xico, Sint\u00e1ctico y Sem\u00e1ntico");
        subt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subt.setForeground(TEXTO_SECUNDARIO);
        izq.add(subt);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        der.setBackground(FONDO_SECUNDARIO);
        der.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        btnEjemplo = crearBoton("Cargar Ejemplo", new Color(100, 100, 140), new Color(130, 130, 180));
        btnEjemplo.addActionListener(e -> cargarEjemplo());
        der.add(btnEjemplo);

        btnLimpiar = crearBoton("Limpiar", new Color(80, 60, 60), new Color(180, 100, 100));
        btnLimpiar.addActionListener(e -> limpiarInterfaz());
        der.add(btnLimpiar);

        btnAnalizar = new JButton("ANALIZAR");
        btnAnalizar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.setBackground(ACENTO_AMBER);
        btnAnalizar.setOpaque(true);
        btnAnalizar.setBorderPainted(false);
        btnAnalizar.setFocusPainted(false);
        btnAnalizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAnalizar.setPreferredSize(new Dimension(110, 30));
        btnAnalizar.addActionListener(e -> ejecutarAnalisis());
        der.add(btnAnalizar);

        btnVerArbol = crearBoton("\u00c1rbol", new Color(60, 80, 60), ACENTO_TEAL);
        btnVerArbol.setEnabled(false);
        btnVerArbol.addActionListener(e -> {
            if (raizActual != null) {
                VentanaArbol va = new VentanaArbol(raizActual);
                va.getContentPane().setBackground(FONDO_PRINCIPAL);
                va.setVisible(true);
            }
        });
        der.add(btnVerArbol);

        btnTema = new JToggleButton("\u263E");
        btnTema.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnTema.setForeground(TEXTO_SECUNDARIO);
        btnTema.setBackground(FONDO_TERCIARIO);
        btnTema.setOpaque(true);
        btnTema.setBorderPainted(false);
        btnTema.setFocusPainted(false);
        btnTema.setPreferredSize(new Dimension(36, 30));
        btnTema.addActionListener(e -> {});
        der.add(btnTema);

        barra.add(izq, BorderLayout.WEST);
        barra.add(der, BorderLayout.EAST);
        return barra;
    }

    private JButton crearBoton(String texto, Color fondo, Color hover) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(TEXTO_PRINCIPAL);
        btn.setBackground(fondo);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(texto.length() * 9 + 20, 30));
        return btn;
    }

    private JPanel crearPanelCentral() {
        JPanel central = new JPanel(new GridBagLayout());
        central.setBackground(FONDO_PRINCIPAL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 10, 8, 10);

        // Panel izquierdo: Editor
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.62;
        gbc.weighty = 1.0;
        central.add(crearPanelEditor(), gbc);

        // Panel derecho: Resultados
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.38;
        gbc.weighty = 1.0;
        central.add(crearPanelResultados(), gbc);

        return central;
    }

    private JPanel crearPanelEditor() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(FONDO_SECUNDARIO);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JLabel lblTitulo = new JLabel("  C\u00f3digo Fuente  .ss");
        lblTitulo.setOpaque(true);
        lblTitulo.setBackground(FONDO_TERCIARIO);
        lblTitulo.setForeground(TEXTO_SECUNDARIO);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitulo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        panel.add(lblTitulo, BorderLayout.NORTH);

        txtEntrada = new JTextPane();
        txtEntrada.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
        txtEntrada.setBackground(FONDO_PRINCIPAL);
        txtEntrada.setForeground(TEXTO_PRINCIPAL);
        txtEntrada.setCaretColor(TEXTO_PRINCIPAL);
        txtEntrada.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ((AbstractDocument) txtEntrada.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet attr) throws BadLocationException {
                clearSemanticHighlights();
                super.insertString(fb, off, str, attr);
                pintarCodigo();
            }
            @Override
            public void replace(FilterBypass fb, int off, int len, String text, AttributeSet attrs) throws BadLocationException {
                clearSemanticHighlights();
                super.replace(fb, off, len, text, attrs);
                pintarCodigo();
            }
            @Override
            public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
                clearSemanticHighlights();
                super.remove(fb, off, len);
                pintarCodigo();
            }
        });

        JScrollPane scroll = new JScrollPane(txtEntrada);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FONDO_PRINCIPAL);
        try {
            TextLineNumber tln = new TextLineNumber(txtEntrada);
            tln.setBackground(FONDO_TERCIARIO);
            tln.setForeground(TEXTO_SECUNDARIO);
            tln.setCurrentLineForeground(ACENTO_AMBER);
            scroll.setRowHeaderView(tln);
        } catch (Exception e) {}
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelResultados() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(FONDO_SECUNDARIO);
        panel.setBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1));

        JLabel lblTitulo = new JLabel("  Resultados");
        lblTitulo.setOpaque(true);
        lblTitulo.setBackground(FONDO_TERCIARIO);
        lblTitulo.setForeground(TEXTO_SECUNDARIO);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitulo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        panel.add(lblTitulo, BorderLayout.NORTH);

        panelLateral = new JTabbedPane();
        panelLateral.setBackground(FONDO_TERCIARIO);
        panelLateral.setForeground(TEXTO_PRINCIPAL);
        panelLateral.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        // Pestaña: Tokens
        String[] colTokens = {"Lexema", "Tipo", "L\u00ednea"};
        modeloTablaTokens = new DefaultTableModel(colTokens, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int col) { return String.class; }
        };
        tblTokens = new JTable(modeloTablaTokens);
        configurarTabla(tblTokens);
        tblTokens.setDefaultRenderer(Object.class, new TokenRowRenderer());
        panelLateral.addTab("Tokens", new JScrollPane(tblTokens));

        // Pestaña: Errores
        String[] colsErr = {"C\u00f3digo", "L\u00ednea", "Descripci\u00f3n"};
        modeloErroresLexicos = new DefaultTableModel(colsErr, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblErroresLexicos = new JTable(modeloErroresLexicos);
        configurarTabla(tblErroresLexicos);
        configurarColsError(tblErroresLexicos);
        panelLateral.addTab("Errores L\u00e9xicos", new JScrollPane(tblErroresLexicos));

        modeloErroresSintacticos = new DefaultTableModel(colsErr, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblErroresSintacticos = new JTable(modeloErroresSintacticos);
        configurarTabla(tblErroresSintacticos);
        configurarColsError(tblErroresSintacticos);
        panelLateral.addTab("Errores Sint\u00e1cticos", new JScrollPane(tblErroresSintacticos));

        modeloErroresSemanticos = new DefaultTableModel(colsErr, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblErroresSemanticos = new JTable(modeloErroresSemanticos);
        configurarTabla(tblErroresSemanticos);
        configurarColsError(tblErroresSemanticos);
        JScrollPane spSem = new JScrollPane(tblErroresSemanticos);
        panelLateral.addTab("Errores Sem\u00e1nticos", spSem);

        // click-to-navigate en todas las tablas de errores
        MouseAdapter navegador = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable tbl = (JTable) e.getSource();
                int row = tbl.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    Object lineObj = tbl.getModel().getValueAt(row, 1);
                    if (lineObj != null) {
                        try {
                            int linea = Integer.parseInt(lineObj.toString());
                            goToLine(linea);
                        } catch (NumberFormatException ex) {}
                    }
                }
            }
        };
        tblErroresLexicos.addMouseListener(navegador);
        tblErroresSintacticos.addMouseListener(navegador);
        tblErroresSemanticos.addMouseListener(navegador);

        // color-coded row renderers for error tables
        tblErroresLexicos.setDefaultRenderer(Object.class, new ErrorRowRenderer(new Color(255, 200, 80, 30)));
        tblErroresSintacticos.setDefaultRenderer(Object.class, new ErrorRowRenderer(new Color(255, 140, 50, 30)));
        tblErroresSemanticos.setDefaultRenderer(Object.class, new ErrorRowRenderer(new Color(255, 80, 80, 30)));

        // Pestaña: Consola
        txtConsola = new JTextArea();
        txtConsola.setEditable(false);
        txtConsola.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        txtConsola.setBackground(FONDO_PRINCIPAL);
        txtConsola.setForeground(TEXTO_PRINCIPAL);
        txtConsola.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panelLateral.addTab("Consola", new JScrollPane(txtConsola));

        // Pestaña: Referencia de Errores
        panelLateral.addTab("Ref. Errores", new PanelReferenciaErrores());

        panel.add(panelLateral, BorderLayout.CENTER);

        return panel;
    }

    private void configurarTabla(JTable tabla) {
        tabla.setBackground(FONDO_PRINCIPAL);
        tabla.setForeground(TEXTO_PRINCIPAL);
        tabla.setGridColor(COLOR_BORDE);
        tabla.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        tabla.setRowHeight(26);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setSelectionBackground(new Color(60, 60, 90));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setIntercellSpacing(new Dimension(8, 0));
        tabla.setFillsViewportHeight(true);

        JTableHeader hdr = tabla.getTableHeader();
        hdr.setBackground(FONDO_TERCIARIO);
        hdr.setForeground(ACENTO_AMBER);
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 10));
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE));
    }

    private void configurarColsError(JTable tabla) {
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        TableColumnModel cm = tabla.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);
        cm.getColumn(0).setMaxWidth(70);
        cm.getColumn(1).setPreferredWidth(40);
        cm.getColumn(1).setMaxWidth(60);
    }

    private void pintarCodigo() {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = txtEntrada.getStyledDocument();
                String texto = doc.getText(0, doc.getLength());
                doc.setCharacterAttributes(0, texto.length(), estiloNormal, true);

                String kw = "(?i)\\b(scan|as|for|each|in|if|else|let|done)\\b";
                String func = "(?i)\\b(move|copy|delete|rename|log|ext|size|name|date)\\b";
                String cads = "\"[^\"]*\"";
                String nums = "\\b\\d+(KB|MB|GB)?\\b";
                String cmts = "//.*|/\\*[\\s\\S]*?\\*/";

                Pattern pk = Pattern.compile(kw);
                Matcher mk = pk.matcher(texto);
                while (mk.find()) doc.setCharacterAttributes(mk.start(), mk.end() - mk.start(), estiloKeyword, false);

                Pattern pf = Pattern.compile(func);
                Matcher mf = pf.matcher(texto);
                while (mf.find()) doc.setCharacterAttributes(mf.start(), mf.end() - mf.start(), estiloFunc, false);

                Pattern pn = Pattern.compile(nums);
                Matcher mn = pn.matcher(texto);
                while (mn.find()) doc.setCharacterAttributes(mn.start(), mn.end() - mn.start(), estiloNumero, false);

                Pattern pc = Pattern.compile(cads);
                Matcher mc = pc.matcher(texto);
                while (mc.find()) doc.setCharacterAttributes(mc.start(), mc.end() - mc.start(), estiloCadena, false);

                Pattern pcm = Pattern.compile(cmts);
                Matcher mcm = pcm.matcher(texto);
                while (mcm.find()) doc.setCharacterAttributes(mcm.start(), mcm.end() - mcm.start(), estiloComentario, false);
            } catch (BadLocationException e) {}
        });
    }

    private void clearSemanticHighlights() {
        Highlighter h = txtEntrada.getHighlighter();
        for (Object tag : semanticHighlights) {
            try { h.removeHighlight(tag); } catch (Exception ex) {}
        }
        semanticHighlights.clear();
    }

    private void highlightSemanticErrors(java.util.List<parser.ErrorCompilacion> errores) {
        clearSemanticHighlights();
        if (errores == null || errores.isEmpty()) return;
        Highlighter h = txtEntrada.getHighlighter();
        Document doc = txtEntrada.getDocument();
        Element root = doc.getDefaultRootElement();
        for (parser.ErrorCompilacion err : errores) {
            int linea = err.getLinea();
            if (linea <= 0) continue;
            int idx = Math.max(0, linea - 1);
            if (idx >= root.getElementCount()) continue;
            Element lineElem = root.getElement(idx);
            int start = lineElem.getStartOffset();
            int end = Math.min(lineElem.getEndOffset(), doc.getLength());
            try {
                Object tag = h.addHighlight(start, end, semHighlightPainter);
                semanticHighlights.add(tag);
            } catch (BadLocationException ex) {}
        }
        if (tblErroresSemanticos != null && modeloErroresSemanticos.getRowCount() > 0) {
            tblErroresSemanticos.setRowSelectionInterval(0, 0);
        }
    }

    private void goToLine(int linea) {
        if (linea <= 0) return;
        Document doc = txtEntrada.getDocument();
        Element root = doc.getDefaultRootElement();
        int idx = Math.max(0, linea - 1);
        if (idx >= root.getElementCount()) return;
        Element lineElem = root.getElement(idx);
        int start = lineElem.getStartOffset();
        txtEntrada.requestFocusInWindow();
        txtEntrada.setCaretPosition(start);
        try {
            Rectangle r = txtEntrada.modelToView(start);
            if (r != null) txtEntrada.scrollRectToVisible(r);
        } catch (BadLocationException ex) {}
    }

    private void ejecutarAnalisis() {
        String codigo = txtEntrada.getText();
        if (codigo.trim().isEmpty()) return;

        modeloTablaTokens.setRowCount(0);
        modeloErroresLexicos.setRowCount(0);
        modeloErroresSintacticos.setRowCount(0);
        modeloErroresSemanticos.setRowCount(0);
        // reset tab titles
        panelLateral.setTitleAt(1, "Errores L\u00e9xicos");
        panelLateral.setTitleAt(2, "Errores Sint\u00e1cticos");
        panelLateral.setTitleAt(3, "Errores Sem\u00e1nticos");
        txtConsola.setText("");
        raizActual = null;
        btnVerArbol.setEnabled(false);
        lblStatus.setText("  Analizando...");
        lblStatus.setForeground(TEXTO_SECUNDARIO);
        clearSemanticHighlights();

        int contLex = 0, contSin = 0, contSem = 0;

        try {
            Token[] tokensCrudos = Tokenizador.tokenizador(codigo);
            AFD afd = SortScriptAFD.obtenerAFD();
            List<Token> tokensAnalizados = afd.aceptar(tokensCrudos);

            for (Token tk : tokensAnalizados) {
                String tipoStr = tk.getTipo() == TipoToken.DESCONOCIDO ? "Desconocido" : tk.getTipo().toString();
                modeloTablaTokens.addRow(new Object[]{tk.getLexema(), tipoStr, tk.getLinea()});
            }

            int selectedTab = 0;

            Errores errLex = afd.getErrores();
            if (errLex.hayErrores()) {
                contLex = errLex.getErrores().size();
                for (ErrorCompilacion err : errLex.getErrores())
                    modeloErroresLexicos.addRow(new Object[]{err.getNumero(), err.getLinea(), err.getDescripcion()});
                panelLateral.setTitleAt(1, "Errores L\u00e9xicos (" + contLex + ")");
                selectedTab = 1;
            }

            List<Token> tokensParser = new ArrayList<>();
            for (Token tk : tokensAnalizados) {
                if (tk.existeSimbolo()) tokensParser.add(tk);
            }

            if (!tokensParser.isEmpty()) {
                ParserLL1 parser = new ParserLL1(tokensParser);
                parser.inicio();
                if (parser.errores.hayErrores()) {
                    contSin = parser.errores.getErrores().size();
                    for (ErrorCompilacion err : parser.errores.getErrores())
                        modeloErroresSintacticos.addRow(new Object[]{err.getNumero(), err.getLinea(), err.getDescripcion()});
                    panelLateral.setTitleAt(2, "Errores Sint\u00e1cticos (" + contSin + ")");
                    if (selectedTab == 0) selectedTab = 2;
                }
                raizActual = parser.getRaiz();
            }

            if (raizActual != null) {
                SemanticAnalyzer sem = new SemanticAnalyzer();
                sem.analizar(raizActual);
                if (sem.errores.hayErrores()) {
                    contSem = sem.errores.getErrores().size();
                    for (ErrorCompilacion err : sem.errores.getErrores())
                        modeloErroresSemanticos.addRow(new Object[]{err.getNumero(), err.getLinea(), err.getDescripcion()});
                    panelLateral.setTitleAt(3, "Errores Sem\u00e1nticos (" + contSem + ")");
                    highlightSemanticErrors(sem.errores.getErrores());
                    if (selectedTab == 0) selectedTab = 3;
                }
            }

            if (contLex == 0 && contSin == 0 && contSem == 0) {
                btnVerArbol.setEnabled(true);
            }

            StringBuilder rep = new StringBuilder();
            rep.append(">> REPORTE DE COMPILACI\u00d3N <<\n");
            rep.append("  Fase l\u00e9xica: ").append(contLex == 0 ? "\u2713" : "\u2717 " + contLex + " error(es)").append("\n");
            rep.append("  Fase sint\u00e1ctica: ").append(contSin == 0 ? "\u2713" : "\u2717 " + contSin + " error(es)").append("\n");
            rep.append("  Fase sem\u00e1ntica: ").append(contSem == 0 ? "\u2713" : "\u2717 " + contSem + " error(es)").append("\n");
            if (contLex == 0 && contSin == 0 && contSem == 0) {
                rep.append("\n\u2713 Compilaci\u00f3n exitosa. Sin errores.\n");
                rep.append("  \u2192 Puedes ver el \u00e1rbol sint\u00e1ctico presionando \"\u00c1rbol\".\n");
                txtConsola.setForeground(EXITO);
                lblStatus.setForeground(EXITO);
                lblStatus.setText("  \u2713 Compilaci\u00f3n exitosa");
            } else {
                rep.append("\nRevisa las pesta\u00f1as de errores.");
                txtConsola.setForeground(ERROR_TXT);
                lblStatus.setForeground(ACENTO_CORAL);
                lblStatus.setText("  \u2717 Errores: " + contLex + " l\u00e9xicos, " + contSin + " sint\u00e1cticos, " + contSem + " sem\u00e1nticos");
            }
            txtConsola.setText(rep.toString());
            if (selectedTab > 0) panelLateral.setSelectedIndex(selectedTab);

        } catch (Exception ex) {
            ex.printStackTrace();
            txtConsola.setText("Error: " + ex.getMessage());
            lblStatus.setForeground(ERROR_TXT);
            lblStatus.setText("  Error: " + ex.getMessage());
        }
    }

    private void limpiarInterfaz() {
        txtEntrada.setText("");
        modeloTablaTokens.setRowCount(0);
        modeloErroresLexicos.setRowCount(0);
        modeloErroresSintacticos.setRowCount(0);
        modeloErroresSemanticos.setRowCount(0);
        txtConsola.setText("");
        raizActual = null;
        btnVerArbol.setEnabled(false);
        clearSemanticHighlights();
        
    }

    private void cargarEjemplo() {
        JFileChooser fc = new JFileChooser("ejemplos");
        fc.setDialogTitle("Seleccionar ejemplo SortScript");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos SortScript (*.ss)", "ss"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()))) {
                StringBuilder sb = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) {
                    sb.append(linea).append("\n");
                }
                txtEntrada.setText(sb.toString());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class TokenRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Object tipoVal = table.getModel().getValueAt(row, 1);
            boolean isUnknown = "Desconocido".equals(tipoVal);
            if (isUnknown && !isSelected) {
                setBackground(new Color(255, 70, 70, 40));
                setForeground(new Color(255, 140, 140));
            } else if (!isSelected) {
                setBackground(row % 2 == 0 ? FILA_PAR : FILA_IMPAR);
                setForeground(TEXTO_PRINCIPAL);
            } else {
                setBackground(new Color(60, 60, 90));
                setForeground(Color.WHITE);
            }
            return this;
        }
    }

    private class ErrorRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private final Color tint;
        ErrorRowRenderer(Color tint) { this.tint = tint; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                setBackground(row % 2 == 0
                    ? new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), Math.min(tint.getAlpha(), 30))
                    : new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), Math.min(tint.getAlpha() + 20, 60)));
            }
            return this;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("TabbedPane.contentAreaColor", new Color(26, 26, 36));
            UIManager.put("TabbedPane.unselectedBackground", new Color(26, 26, 36));
            UIManager.put("TabbedPane.selected", new Color(34, 34, 46));
            UIManager.put("TabbedPane.focus", new Color(34, 34, 46));
            UIManager.put("TabbedPane.tabAreaBackground", new Color(26, 26, 36));
            UIManager.put("TabbedPane.selectHighlight", new Color(255, 184, 76));
        } catch (Exception ex) {}
        SwingUtilities.invokeLater(CompiladorUI::new);
    }
}
