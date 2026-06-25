package ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public class PanelReferenciaErrores extends JPanel {

    private static final Color FONDO_PRINCIPAL = new Color(18, 18, 24);
    private static final Color FONDO_TERCIARIO = new Color(34, 34, 46);
    private static final Color COLOR_BORDE = new Color(48, 48, 64);
    private static final Color TEXTO_PRINCIPAL = new Color(220, 220, 235);
    private static final Color ACENTO_AMBER = new Color(255, 184, 76);

    private static final Color AMBER_TINT = new Color(255, 200, 80, 30);
    private static final Color ORANGE_TINT = new Color(255, 140, 50, 30);
    private static final Color RED_TINT = new Color(255, 80, 80, 30);

    private final List<ErrorReferencia> errores = new ArrayList<>();
    private JTable tblReferencia;
    private JTextPane txtDetalle;
    private DefaultTableModel modelo;

    public PanelReferenciaErrores() {
        cargarDatos();
        construirUI();
    }

    private void cargarDatos() {
        errores.add(new ErrorReferencia(101, "L\u00e9xico",
            "S\u00edmbolo no definido en el alfabeto",
            "El car\u00e1cter ingresado no pertenece al alfabeto del lenguaje SortScript. Por ejemplo, los s\u00edmbolos @, #, $, | no est\u00e1n definidos.",
            "Revisa el c\u00f3digo y elimina o reemplaza el s\u00edmbolo no v\u00e1lido por su equivalente correcto (ej. usar || en lugar de |)."));

        errores.add(new ErrorReferencia(102, "L\u00e9xico",
            "Token no reconocido",
            "El analizador l\u00e9xico encontr\u00f3 una secuencia de caracteres v\u00e1lidos que no corresponde a ning\u00fan token conocido del lenguaje.",
            "Verifica que el token est\u00e9 bien formado. Si es un identificador, aseg\u00farate de que comience con una letra o gui\u00f3n bajo y solo contenga caracteres alfanum\u00e9ricos."));

        errores.add(new ErrorReferencia(103, "L\u00e9xico",
            "Palabra clave incompleta",
            "Se escribi\u00f3 un prefijo de una palabra clave (keyword) que no forma un token v\u00e1lido. Por ejemplo: 'ea' o 'eac' (en lugar de 'each'), 'fo' (en lugar de 'for'), 'sc' (en lugar de 'scan').",
            "Revisa la ortograf\u00eda de la palabra clave. Las palabras clave v\u00e1lidas son: scan, as, for, each, in, if, else, let, done."));

        errores.add(new ErrorReferencia(104, "L\u00e9xico",
            "Nombre de funci\u00f3n incompleto",
            "Se escribi\u00f3 un prefijo de una funci\u00f3n que no forma un token v\u00e1lido. Por ejemplo: 'mov' (en lugar de 'move'), 'cop' (en lugar de 'copy'), 'del' (en lugar de 'delete').",
            "Revisa la ortograf\u00eda de la funci\u00f3n. Las funciones v\u00e1lidas son: move, copy, delete, rename, log."));

        errores.add(new ErrorReferencia(105, "L\u00e9xico",
            "Nombre de atributo incompleto",
            "Se escribi\u00f3 un prefijo de un atributo que no forma un token v\u00e1lido. Por ejemplo: 'ex' (en lugar de 'ext'), 'siz' (en lugar de 'size'), 'nam' (en lugar de 'name').",
            "Revisa la ortograf\u00eda del atributo. Los atributos v\u00e1lidos son: .ext, .size, .name, .date."));

        errores.add(new ErrorReferencia(106, "L\u00e9xico",
            "Palabra clave con caracteres extra",
            "Se escribi\u00f3 una palabra clave completa seguida de caracteres adicionales. Por ejemplo: 'eachh' (each + h), 'forr' (for + r), 'scann' (scan + n).",
            "Revisa la ortograf\u00eda. Si intentabas usar un identificador compuesto, usa gui\u00f3n bajo (_) en lugar de concatenar directamente."));

        errores.add(new ErrorReferencia(107, "L\u00e9xico",
            "Nombre de funci\u00f3n con caracteres extra",
            "Se escribi\u00f3 una funci\u00f3n completa seguida de caracteres adicionales. Por ejemplo: 'movee' (move + e), 'logg' (log + g).",
            "Revisa la ortograf\u00eda de la funci\u00f3n. Las funciones v\u00e1lidas son: move, copy, delete, rename, log."));

        errores.add(new ErrorReferencia(108, "L\u00e9xico",
            "Nombre de atributo con caracteres extra",
            "Se escribi\u00f3 un atributo completo seguido de caracteres adicionales. Por ejemplo: 'exxt' (ext + x), 'sizze' (size + ze).",
            "Revisa la ortograf\u00eda del atributo. Los atributos v\u00e1lidos son: .ext, .size, .name, .date."));

        errores.add(new ErrorReferencia(109, "L\u00e9xico",
            "Literal mal formado",
            "Un literal (n\u00famero, tama\u00f1o, ruta) no cumple con el formato esperado. Por ejemplo: '5XYZ' (tama\u00f1o con unidad inv\u00e1lida), '.abcdefgh' (extensi\u00f3n muy larga).",
            "Revisa el formato del literal. Los tama\u00f1os usan KB, MB o GB. Las extensiones tienen hasta 5 caracteres despu\u00e9s del punto. Las rutas deben contener / o \\."));

        errores.add(new ErrorReferencia(110, "Sint\u00e1ctico",
            "Se espera 'scan' al inicio",
            "El programa debe comenzar con la instrucci\u00f3n 'scan \"ruta\" as identificador'.",
            "Agrega 'scan \"<ruta>\" as <nombre>' como primera l\u00ednea del programa."));

        errores.add(new ErrorReferencia(111, "Sint\u00e1ctico",
            "Instrucci\u00f3n no v\u00e1lida",
            "Se encontr\u00f3 una palabra que no corresponde a ninguna instrucci\u00f3n v\u00e1lida (scan, for, if, let, done) ni a una funci\u00f3n reconocida.",
            "Revisa que la instrucci\u00f3n est\u00e9 bien escrita. Las instrucciones v\u00e1lidas son: scan, for each, if, else, let, done."));

        errores.add(new ErrorReferencia(112, "Sint\u00e1ctico",
            "Error sint\u00e1ctico general",
            "Se esperaba un token espec\u00edfico (como '{', '(', ')', '=', etc.) pero se encontr\u00f3 otro. Esto suele ocurrir por olvidar un delimitador o cerrar incorrectamente un bloque.",
            "Revisa la l\u00ednea indicada: verifica par\u00e9ntesis, llaves y la estructura general de la instrucci\u00f3n."));

        errores.add(new ErrorReferencia(113, "Sint\u00e1ctico",
            "Error en expresi\u00f3n",
            "Se esperaba un literal (n\u00famero, cadena), un identificador o un par\u00e9ntesis de apertura '(' para iniciar una expresi\u00f3n.",
            "Verifica que la expresi\u00f3n est\u00e9 completa. Por ejemplo, en 'if a >' falta el segundo operando; en 'copy(x,)' falta el segundo argumento."));

        errores.add(new ErrorReferencia(114, "Sint\u00e1ctico",
            "Atributo inv\u00e1lido despu\u00e9s de '.'",
            "Despu\u00e9s de un punto (.) se espera uno de los atributos v\u00e1lidos: ext, size, name o date.",
            "Corrige el nombre del atributo. Los \u00fanicos atributos disponibles son: .ext, .size, .name, .date."));

        errores.add(new ErrorReferencia(115, "Sint\u00e1ctico",
            "Funci\u00f3n no reconocida",
            "El nombre de la funci\u00f3n no coincide con ninguna funci\u00f3n incorporada. Las funciones v\u00e1lidas son: move, copy, rename, delete, log.",
            "Verifica la ortograf\u00eda del nombre de la funci\u00f3n. Las funciones disponibles son: move, copy, rename, delete, log (no distinguen may\u00fasculas/min\u00fasculas)."));

        errores.add(new ErrorReferencia(116, "Sem\u00e1ntico",
            "Variable no declarada",
            "Se est\u00e1 usando un identificador que no fue declarado previamente con 'let' o 'scan ... as' o como iterador en un 'for each'.",
            "Declara la variable antes de usarla con 'let <nombre> = <valor>' o verifica que el nombre est\u00e9 escrito correctamente."));

        errores.add(new ErrorReferencia(117, "Sem\u00e1ntico",
            "Variable ya declarada",
            "Se intent\u00f3 declarar una variable con un nombre que ya existe en el mismo alcance (scope).",
            "Usa un nombre diferente para la nueva variable o elimina la declaraci\u00f3n anterior si ya no es necesaria."));

        errores.add(new ErrorReferencia(118, "Sem\u00e1ntico",
            "N\u00famero incorrecto de argumentos",
            "Se llam\u00f3 a una funci\u00f3n con m\u00e1s o menos argumentos de los que espera. Por ejemplo, move necesita 2 argumentos, delete necesita 1.",
            "Revisa la documentaci\u00f3n de la funci\u00f3n: move(rutaOrigen, rutaDestino), copy(rutaOrigen, rutaDestino), rename(ruta, nuevoNombre), delete(ruta), log(mensaje) o log(mensaje, archivo)."));

        errores.add(new ErrorReferencia(119, "Sem\u00e1ntico",
            "Tipo de argumento incorrecto",
            "El tipo del valor pasado como argumento no coincide con el tipo esperado. Por ejemplo, move espera rutas (cadenas con /), pero recibi\u00f3 un n\u00famero.",
            "Aseg\u00farate de que el argumento sea del tipo correcto: las funciones de archivos esperan rutas (cadenas con / o .), log acepta cualquier tipo."));

        errores.add(new ErrorReferencia(120, "Sem\u00e1ntico",
            "Atributo no v\u00e1lido para el tipo",
            "Se intent\u00f3 acceder a un atributo (como .ext o .size) sobre un valor que no es un archivo (por ejemplo, un n\u00famero o una cadena sin ruta de archivo).",
            "Los atributos .ext, .size, .name, .date solo est\u00e1n disponibles para variables de tipo archivo (declaradas en un 'scan ... as' o un 'for each' sobre una colecci\u00f3n de archivos)."));

        errores.add(new ErrorReferencia(121, "Sem\u00e1ntico",
            "Variable no accesible fuera de su alcance",
            "Una variable declarada dentro de un bloque 'for', 'if' o 'else' no puede usarse fuera de ese bloque.",
            "Si necesitas usar la variable fuera, decl\u00e1rala antes del bloque con 'let' o reestructura la l\u00f3gica para no depender de variables internas."));
    }

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(FONDO_PRINCIPAL);

        String[] cols = {"C\u00f3digo", "Tipo", "Causa"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ErrorReferencia e : errores) {
            modelo.addRow(new Object[]{e.codigo, e.tipo, e.causa});
        }

        tblReferencia = new JTable(modelo);
        configurarTabla(tblReferencia);
        configurarCols(tblReferencia);
        tblReferencia.setDefaultRenderer(Object.class, new ReferenciaRowRenderer(errores, AMBER_TINT, ORANGE_TINT, RED_TINT));

        JScrollPane scrollTabla = new JScrollPane(tblReferencia);
        scrollTabla.setBorder(null);
        add(scrollTabla, BorderLayout.CENTER);

        txtDetalle = new JTextPane();
        txtDetalle.setEditable(false);
        txtDetalle.setContentType("text/html");
        txtDetalle.setBackground(FONDO_TERCIARIO);
        txtDetalle.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        txtDetalle.setText("<html><body style='color:#8C8CA5; font-family:Segoe UI, sans-serif; font-size:11px; padding:4px;'>"
            + "Selecciona un error de la lista para ver su descripci\u00f3n y soluci\u00f3n.</body></html>");

        JScrollPane scrollDetalle = new JScrollPane(txtDetalle);
        scrollDetalle.setBorder(null);
        scrollDetalle.setPreferredSize(new Dimension(getWidth(), 110));
        add(scrollDetalle, BorderLayout.SOUTH);

        tblReferencia.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int row = tblReferencia.getSelectedRow();
            if (row >= 0 && row < errores.size()) {
                mostrarDetalle(errores.get(row));
            }
        });
    }

    private void mostrarDetalle(ErrorReferencia err) {
        txtDetalle.setText(String.format(
            "<html><body style='font-family:Segoe UI, sans-serif; font-size:12px; padding:4px;'>"
            + "<p style='color:#DCDCEB; margin:0 0 6px 0;'><b>Error %d</b> &mdash; <span style='color:%s;'>%s</span></p>"
            + "<p style='color:#B8B8CC; margin:0 0 4px 0;'><b>\u00bfPor qu\u00e9 ocurre?</b><br>%s</p>"
            + "<p style='color:#B8B8CC; margin:0;'><b>\u00bfC\u00f3mo solucionarlo?</b><br>%s</p>"
            + "</body></html>",
            err.codigo,
            colorHex(err.tipo),
            err.tipo,
            err.explicacion,
            err.solucion
        ));
        txtDetalle.setCaretPosition(0);
    }

    private static String colorHex(String tipo) {
        if ("L\u00e9xico".equals(tipo)) return "#FFB84C";
        if ("Sint\u00e1ctico".equals(tipo)) return "#FF8C32";
        if ("Sem\u00e1ntico".equals(tipo)) return "#FF5050";
        return "#DCDCEB";
    }

    private void configurarTabla(JTable tabla) {
        tabla.setBackground(FONDO_PRINCIPAL);
        tabla.setForeground(TEXTO_PRINCIPAL);
        tabla.setGridColor(COLOR_BORDE);
        tabla.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        tabla.setRowHeight(28);
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

    private void configurarCols(JTable tabla) {
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        TableColumnModel cm = tabla.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);
        cm.getColumn(0).setMaxWidth(70);
        cm.getColumn(1).setPreferredWidth(80);
        cm.getColumn(1).setMaxWidth(100);
    }

    private static class ErrorReferencia {
        final int codigo;
        final String tipo;
        final String causa;
        final String explicacion;
        final String solucion;

        ErrorReferencia(int codigo, String tipo, String causa, String explicacion, String solucion) {
            this.codigo = codigo;
            this.tipo = tipo;
            this.causa = causa;
            this.explicacion = explicacion;
            this.solucion = solucion;
        }
    }

    private static class ReferenciaRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private final List<ErrorReferencia> errores;
        private final Color amber, orange, red;

        ReferenciaRowRenderer(List<ErrorReferencia> errores, Color amber, Color orange, Color red) {
            this.errores = errores;
            this.amber = amber;
            this.orange = orange;
            this.red = red;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected && row >= 0 && row < errores.size()) {
                String tipo = errores.get(row).tipo;
                if ("L\u00e9xico".equals(tipo)) c.setBackground(amber);
                else if ("Sint\u00e1ctico".equals(tipo)) c.setBackground(orange);
                else if ("Sem\u00e1ntico".equals(tipo)) c.setBackground(red);
            }
            return c;
        }
    }
}
