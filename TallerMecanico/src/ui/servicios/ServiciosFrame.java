package ui.servicios;

import ui.components.UIMode;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ServiciosFrame extends JFrame {

    private UIMode mode;

    // Clave
    private JTextField idServicio;

    // Campos
    private JTextField nombre;
    private JTextField precio;
    private JComboBox<String> gravaIva;
    private JComboBox<String> estado;

    // Tabla
    private JTable table;
    private DefaultTableModel model;

    // Botones
    private JButton btnBuscar;
    private JButton btnGuardar;
    private JButton btnEliminar;

    public ServiciosFrame(UIMode mode) {
        this.mode = mode;

        setTitle("Servicios - Autos y Motores");
        setSize(1080, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Gestión de Servicios", "Servicios con IVA o sin IVA (solo UI)"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
    }

    public ServiciosFrame() {
        this(UIMode.ADD);
    }

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(UITheme.BG);

        JPanel form = UITheme.cardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        idServicio = new JTextField();

        nombre = new JTextField();
        precio = new JTextField();
        gravaIva = new JComboBox<>(new String[]{"SÍ", "NO"});
        estado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

        int r = 0;
        addField(form, g, r++, "ID Servicio", idServicio); // clave
        addField(form, g, r++, "Nombre del servicio", nombre);
        addField(form, g, r++, "Precio mano de obra", precio);
        addField(form, g, r++, "¿Grava IVA?", gravaIva);
        addField(form, g, r++, "Estado", estado);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        btnBuscar = UITheme.primaryButton("Buscar");
        btnGuardar = UITheme.primaryButton("Guardar");
        btnEliminar = UITheme.primaryButton("Eliminar");

        actions.add(btnBuscar);
        actions.add(btnGuardar);
        actions.add(btnEliminar);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        form.add(actions, g);

        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"ID", "Servicio", "Precio", "Grava IVA", "Estado"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setRowHeight(26);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.42);
        split.setBorder(null);

        root.add(split, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> onBuscar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnEliminar.addActionListener(e -> onEliminar());

        return root;
    }

    private void applyMode(UIMode m) {
        this.mode = m;

        btnBuscar.setVisible(m == UIMode.EDIT || m == UIMode.DELETE);
        btnGuardar.setVisible(m == UIMode.ADD || m == UIMode.EDIT);
        btnEliminar.setVisible(m == UIMode.DELETE);

        if (m == UIMode.ADD) {
            btnGuardar.setText("Agregar");
            setKeyState(true, true);
            setFieldsEnabled(true);
        }

        if (m == UIMode.EDIT) {
            btnGuardar.setText("Guardar");
            setKeyState(true, true);
            setFieldsEnabled(false);
        }

        if (m == UIMode.DELETE) {
            setKeyState(true, true);
            setFieldsEnabled(false);
        }

        revalidate();
        repaint();
    }

    private void setKeyState(boolean enabled, boolean editable) {
        idServicio.setEnabled(enabled);
        idServicio.setEditable(editable);
    }

    private void setFieldsEnabled(boolean enabled) {
        nombre.setEnabled(enabled); nombre.setEditable(enabled);
        precio.setEnabled(enabled); precio.setEditable(enabled);
        gravaIva.setEnabled(enabled);
        estado.setEnabled(enabled);
    }

    private void onBuscar() {
        if (mode != UIMode.EDIT && mode != UIMode.DELETE) return;

        // Mock
        nombre.setText("Cambio de aceite");
        precio.setText("35.00");
        gravaIva.setSelectedItem("SÍ");
        estado.setSelectedItem("ACTIVO");

        setKeyState(true, false);
        setFieldsEnabled(true);

        if (mode == UIMode.DELETE) {
            setFieldsEnabled(false);
        }

        JOptionPane.showMessageDialog(this,
                "Servicio cargado. Ya puedes " + (mode == UIMode.EDIT ? "editar." : "eliminar/desactivar."));
    }

    private void onGuardar() {
        if (mode == UIMode.ADD) JOptionPane.showMessageDialog(this, "Guardado (ADD) - Solo UI");
        if (mode == UIMode.EDIT) JOptionPane.showMessageDialog(this, "Guardado (EDIT) - Solo UI");
    }

    private void onEliminar() {
        if (mode == UIMode.DELETE) JOptionPane.showMessageDialog(this, "Eliminado (DELETE) - Solo UI");
    }

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
