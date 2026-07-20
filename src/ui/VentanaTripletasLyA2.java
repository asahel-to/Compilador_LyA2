package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import parser.ParserTAC;

public class VentanaTripletasLyA2 extends JFrame {
    private final JTextArea txtTac = new JTextArea();
    private final JTable tabla = new JTable();
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Índice", "Op", "Arg1", "Arg2"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public VentanaTripletasLyA2(String tacInicial) {
        setTitle("Generador de tres direcciones-LyA2");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(18, 18, 24));
        setLayout(new BorderLayout(10, 10));

        JPanel superior = new JPanel(new BorderLayout(8, 0));
        superior.setBackground(new Color(26, 26, 36));
        superior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtTac.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        txtTac.setBackground(new Color(10, 10, 16));
        txtTac.setForeground(new Color(220, 220, 235));
        txtTac.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scrollTac = new JScrollPane(txtTac);
        scrollTac.setPreferredSize(new Dimension(0, 220));
        superior.add(scrollTac, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setBackground(new Color(26, 26, 36));

        JButton btnClean = new JButton("Clean");
        btnClean.addActionListener(e -> limpiar());
        acciones.add(btnClean);

        JButton btnGenerar = new JButton("Generar txt");
        btnGenerar.addActionListener(e -> generar());
        acciones.add(btnGenerar);

        superior.add(acciones, BorderLayout.EAST);
        add(superior, BorderLayout.NORTH);

        if (tacInicial != null && !tacInicial.trim().isEmpty()) {
            txtTac.setText(tacInicial);
        }

        tabla.setModel(modelo);
        tabla.setBackground(new Color(28, 28, 34));
        tabla.setForeground(new Color(220, 220, 235));
        tabla.setSelectionBackground(new Color(72, 84, 130));
        tabla.setSelectionForeground(new Color(240, 240, 255));
        tabla.setGridColor(new Color(48, 48, 64));
        tabla.setRowHeight(24);
        tabla.setShowGrid(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));

        tabla.getTableHeader().setBackground(new Color(38, 38, 52));
        tabla.getTableHeader().setForeground(new Color(220, 220, 235));
        tabla.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        tabla.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(48, 48, 64)));

        javax.swing.table.DefaultTableCellRenderer renderer = new javax.swing.table.DefaultTableCellRenderer();
        renderer.setBackground(new Color(28, 28, 34));
        renderer.setForeground(new Color(220, 220, 235));
        renderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private void limpiar() {
        txtTac.setText("");
        modelo.setRowCount(0);
    }

    private void generar() {
        String texto = txtTac.getText();
        if (texto == null || texto.trim().isEmpty()) {
            return;
        }

        List<String> lineas = Arrays.asList(texto.split("\\r?\\n"));
        ParserTAC.ResultadoTAC resultado = new ParserTAC().parsear(lineas);
        modelo.setRowCount(0);
        for (ParserTAC.TripletaTAC t : resultado.getTripletas()) {
            modelo.addRow(new Object[]{t.getIndice(), t.getOperador(), t.getOperando1(), t.getOperando2()});
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar tripletas");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        chooser.setSelectedFile(new File("tripletas.txt"));
        if (chooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".txt")) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + ".txt");
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
                for (ParserTAC.TripletaTAC t : resultado.getTripletas()) {
                    pw.println(t.getIndice() + "\t=\t" + t.getOperando1() + "\t" + t.getOperador() + "\t" + t.getOperando2());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
