package ui.facturacion;

import ui.clientes.ClienteDialog;
import ui.components.SelectorPanel;
import ui.servicios.ServicioItem;
import ui.servicios.ServicioDialog; // ajusta si tu clase se llama diferente
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FacturacionFrame extends JFrame {

    private static final double IVA_RATE = 0.12;

    // Detalle (inputs)
    private JTextField codigoServicio;
    private JTextField cantidad;

    // Tabla detalle
    private JTable table;
    private DefaultTableModel model;

    private ServicioItem servicioSeleccionado = null;
    private int editRow = 0;

    // Cabecera (solo lectura)
    private JTextField numFactura;
    private JTextField fecha;
    private JTextField usuario;

    private SelectorPanel cliente;

    // Botón guardar factura
    private JButton btnGuardarFactura;

    public FacturacionFrame() {
        setTitle("Facturación - Autos y Motores");
        setSize(1250, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Facturación", "Cabecera y detalle de factura (solo UI)"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        // Mock: usuario conectado
        setDatosAutomaticos("USUARIO_CONECTADO");
    }

    private void setDatosAutomaticos(String userLogged) {
        numFactura.setText("FAC-" + String.format("%06d", 1)); // mock
        fecha.setText(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        usuario.setText(userLogged);

        // Solo lectura
        numFactura.setEditable(false);
        fecha.setEditable(false);
        usuario.setEditable(false);

        // visibles
        numFactura.setEnabled(true);
        fecha.setEnabled(true);
        usuario.setEnabled(true);
    }

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        root.setBackground(UITheme.BG);

        // ===================== CABECERA =====================
        JPanel cab = UITheme.cardPanel();
        cab.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        numFactura = new JTextField();
        fecha = new JTextField();
        usuario = new JTextField();

        // Solo lectura pero visibles
        numFactura.setEditable(false);
        fecha.setEditable(false);
        usuario.setEditable(false);

        numFactura.setEnabled(true);
        fecha.setEnabled(true);
        usuario.setEnabled(true);

        cliente = new SelectorPanel("(Seleccionar cliente)");
        cliente.setOnSearch(() -> {
            ClienteDialog dialog = new ClienteDialog(this, cliente::setText);
            dialog.setVisible(true);
        });

        int r = 0;
        addField(cab, g, r++, "Número factura", numFactura);
        addField(cab, g, r++, "Fecha emisión", fecha);
        addField(cab, g, r++, "Cliente", cliente);
        addField(cab, g, r++, "Usuario", usuario);

        // ✅ Para que la cabecera NO se coma media pantalla
        cab.setPreferredSize(new Dimension(0, 220));

        // ===================== DETALLE =====================
        JPanel det = UITheme.cardPanel();
        det.setLayout(new BorderLayout(0, 10));
        det.setBorder(BorderFactory.createCompoundBorder(
                det.getBorder(),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        // ---------- TOP (inputs + botones) ----------
        JPanel topCompacto = new JPanel(new GridBagLayout());
        topCompacto.setOpaque(false);

        GridBagConstraints d = new GridBagConstraints();
        d.insets = new Insets(4, 4, 4, 4);
        d.fill = GridBagConstraints.HORIZONTAL;
        d.gridy = 0;

        codigoServicio = new JTextField();
        cantidad = new JTextField("1");

        JButton btnBuscarServicio = UITheme.primaryButton("🔍");
        btnBuscarServicio.setPreferredSize(new Dimension(46, 30));
        btnBuscarServicio.addActionListener(e -> abrirServiciosDialog());

        JPanel codigoPanel = new JPanel(new BorderLayout(6, 0));
        codigoPanel.setOpaque(false);
        codigoPanel.add(codigoServicio, BorderLayout.CENTER);
        codigoPanel.add(btnBuscarServicio, BorderLayout.EAST);

        JButton btnAgregarDetalle = UITheme.primaryButton("Agregar detalle");
        JButton btnQuitarDetalle = UITheme.primaryButton("Quitar detalle");

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBtns.setOpaque(false);
        panelBtns.add(btnAgregarDetalle);
        panelBtns.add(btnQuitarDetalle);

        d.gridx = 0; d.weightx = 0;
        topCompacto.add(new JLabel("Código servicio"), d);

        d.gridx = 1; d.weightx = 0.62;
        topCompacto.add(codigoPanel, d);

        d.gridx = 2; d.weightx = 0;
        topCompacto.add(new JLabel("Cantidad"), d);

        d.gridx = 3; d.weightx = 0.12;
        topCompacto.add(cantidad, d);

        d.gridx = 4; d.weightx = 0.26;
        topCompacto.add(panelBtns, d);

        // ---------- TABLA (que ocupe TODO) ----------
        String[] cols = {"Código", "Servicio", "Cantidad", "Precio Unit.", "Subtotal", "IVA", "Total"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return row == editRow && col == 2;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);

        // ✅ Haz que se vea “grande” y aproveche el ancho
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        // ✅ Esto hace que el scrollpane “jale” espacio en el layout
        scroll.setMinimumSize(new Dimension(0, 380));

        // fila inicial en edición
        editRow = 0;
        model.addRow(new Object[]{"", "", 1, "0.00", "0.00", "0.00", "0.00"});

        // ---------- FOOTER (Guardar Factura) ----------
        JPanel detBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        detBottom.setOpaque(false);

        btnGuardarFactura = UITheme.primaryButton("Guardar Factura");
        btnGuardarFactura.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Factura guardada (Solo UI)")
        );
        detBottom.add(btnGuardarFactura);

        det.add(topCompacto, BorderLayout.NORTH);
        det.add(scroll, BorderLayout.CENTER);
        det.add(detBottom, BorderLayout.SOUTH);

        // eventos
        btnAgregarDetalle.addActionListener(e -> confirmarDetalle());
        btnQuitarDetalle.addActionListener(e -> quitarDetalle());

        // ===================== SPLIT (la tabla domina) =====================
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cab, det);
        split.setBorder(null);
        split.setOneTouchExpandable(false);

        // ✅ Más espacio abajo (tabla). 0.22 = cabecera ~22%, detalle ~78%
        split.setResizeWeight(0.18);
        split.setDividerLocation(200);

        root.add(split, BorderLayout.CENTER);

        return root;
    }

    // ---------- Dialog servicios ----------
    private void abrirServiciosDialog() {
        ServicioDialog dialog = new ServicioDialog(this, item -> {
            servicioSeleccionado = item;

            // Mostrar código en textbox
            codigoServicio.setText(item.getCodigo());

            // Rellenar fila edición en tabla
            model.setValueAt(item.getCodigo(), editRow, 0);
            model.setValueAt(item.getNombre(), editRow, 1);

            int cant = parseIntSafe(cantidad.getText(), 1);
            model.setValueAt(cant, editRow, 2);

            model.setValueAt(String.format("%.2f", item.getPrecio()), editRow, 3);

            recalcularFilaEdicion();
        });
        dialog.setVisible(true);
    }

    private void recalcularFilaEdicion() {
        if (servicioSeleccionado == null) return;

        int cant = parseIntSafe(String.valueOf(model.getValueAt(editRow, 2)), 1);
        double pu = servicioSeleccionado.getPrecio();

        double sub = cant * pu;
        double iva = servicioSeleccionado.isGravaIva() ? (sub * IVA_RATE) : 0.0;
        double total = sub + iva;

        model.setValueAt(String.format("%.2f", sub), editRow, 4);
        model.setValueAt(String.format("%.2f", iva), editRow, 5);
        model.setValueAt(String.format("%.2f", total), editRow, 6);
    }

    private void confirmarDetalle() {
        String cod = String.valueOf(model.getValueAt(editRow, 0)).trim();
        if (cod.isEmpty() || servicioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un servicio (🔍).");
            return;
        }

        int cant = parseIntSafe(String.valueOf(model.getValueAt(editRow, 2)), 0);
        if (cant <= 0) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida (> 0).");
            return;
        }

        // Nueva fila en edición
        model.addRow(new Object[]{"", "", 1, "0.00", "0.00", "0.00", "0.00"});
        editRow = model.getRowCount() - 1;

        servicioSeleccionado = null;
        codigoServicio.setText("");
        cantidad.setText("1");

        table.repaint();
        table.scrollRectToVisible(table.getCellRect(editRow, 0, true));
    }

    private void quitarDetalle() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para quitar.");
            return;
        }

        if (row == editRow) {
            JOptionPane.showMessageDialog(this, "No puedes quitar la fila de edición.");
            return;
        }

        model.removeRow(row);
        if (row < editRow) editRow--;

        if (model.getRowCount() > 0) {
            int showRow = Math.min(editRow, model.getRowCount() - 1);
            table.scrollRectToVisible(table.getCellRect(showRow, 0, true));
        }
    }

    private int parseIntSafe(String txt, int def) {
        try { return Integer.parseInt(txt.trim()); }
        catch (Exception e) { return def; }
    }

    // ---------- UI helpers ----------
    private JPanel header(String title, String subtitle) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        top.setPreferredSize(new Dimension(0, 64));

        JLabel t = new JLabel("  " + title);
        t.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel s = new JLabel("  " + subtitle);
        s.setForeground(UITheme.MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(t);
        text.add(s);

        top.add(text, BorderLayout.WEST);
        return top;
    }

    private void addField(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; g.weightx = 0.35;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 0.65;
        p.add(field, g);
    }
}
