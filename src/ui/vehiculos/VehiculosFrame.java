package ui.vehiculos;

import ui.clientes.ClienteDialog;
import ui.components.SelectorPanel;
import ui.components.UIMode;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VehiculosFrame extends JFrame {

    private UIMode mode;

    // ===== Campos =====
    private JTextField placa;
    private SelectorPanel cliente;
    private JComboBox<String> marca;
    private JComboBox<String> modelo;

    // ===== Tabla =====
    private JTable table;
    private DefaultTableModel model;

    // ===== Botones =====
    private JButton btnBuscar;   // solo EDIT/DELETE
    private JButton btnGuardar;  // ADD/EDIT
    private JButton btnEliminar; // DELETE

    public VehiculosFrame(UIMode mode) {
        this.mode = mode;

        setTitle("Vehículos - Autos y Motores");
        setSize(1120, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Gestión de Vehículos", "Registro por cliente, marca y modelo (solo UI)"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
    }

    public VehiculosFrame() {
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

        // Campos
        placa = new JTextField();
        cliente = new SelectorPanel("(Seleccionar cliente)");
        marca = new JComboBox<>(new String[]{"(Seleccionar marca)"});
        modelo = new JComboBox<>(new String[]{"(Seleccionar modelo)"});

        cliente.setOnSearch(() -> {
            ClienteDialog dialog = new ClienteDialog(this, cliente::setText);
            dialog.setVisible(true);
        });

        int r = 0;
        addField(form, g, r++, "Placa", placa);          // ✅ clave para buscar
        addField(form, g, r++, "Cliente", cliente);
        addField(form, g, r++, "Marca", marca);
        addField(form, g, r++, "Modelo", modelo);

        // Acciones
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        btnBuscar = UITheme.primaryButton("Buscar");     // EDIT/DELETE
        btnGuardar = UITheme.primaryButton("Guardar");   // ADD/EDIT
        btnEliminar = UITheme.primaryButton("Eliminar"); // DELETE

        actions.add(btnBuscar);
        actions.add(btnGuardar);
        actions.add(btnEliminar);

        g.gridx = 0;
        g.gridy = r;
        g.gridwidth = 2;
        form.add(actions, g);

        // Tabla
        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"ID", "Placa", "Cliente", "Marca", "Modelo"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setRowHeight(26);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.42);
        split.setBorder(null);

        root.add(split, BorderLayout.CENTER);

        // Eventos
        btnBuscar.addActionListener(e -> onBuscar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnEliminar.addActionListener(e -> onEliminar());

        return root;
    }

    // ===================== MODOS (IGUAL QUE CLIENTES, adaptado a PLACA) =====================

    private void applyMode(UIMode m) {
        this.mode = m;

        // Botones visibles
        btnBuscar.setVisible(m == UIMode.EDIT || m == UIMode.DELETE);
        btnGuardar.setVisible(m == UIMode.ADD || m == UIMode.EDIT);
        btnEliminar.setVisible(m == UIMode.DELETE);

        if (m == UIMode.ADD) {
            btnGuardar.setText("Agregar");
            setKeyState(true, true);   // placa editable
            setFieldsEnabled(true);    // resto habilitado
        }

        if (m == UIMode.EDIT) {
            btnGuardar.setText("Guardar");
            setKeyState(true, true);   // ✅ solo placa editable
            setFieldsEnabled(false);   // 🔒 resto deshabilitado hasta buscar
        }

        if (m == UIMode.DELETE) {
            setKeyState(true, true);   // ✅ solo placa editable
            setFieldsEnabled(false);   // 🔒 resto deshabilitado hasta buscar
        }

        revalidate();
        repaint();
    }

    private void setKeyState(boolean enabled, boolean editable) {
        placa.setEnabled(enabled);
        placa.setEditable(editable);
    }

    private void setFieldsEnabled(boolean enabled) {
        cliente.setEnabled(enabled);
        marca.setEnabled(enabled);
        modelo.setEnabled(enabled);
    }

    // ===================== ACCIONES (UI MOCK) =====================

    private void onBuscar() {
        if (mode != UIMode.EDIT && mode != UIMode.DELETE) return;

        // Mock: simula que encontró el vehículo por PLACA
        cliente.setText("Juan Pérez - 0102030405");
        marca.setModel(new DefaultComboBoxModel<>(new String[]{"Chevrolet", "Toyota", "Kia"}));
        modelo.setModel(new DefaultComboBoxModel<>(new String[]{"Silverado", "Hilux", "Rio"}));
        marca.setSelectedItem("Chevrolet");
        modelo.setSelectedItem("Silverado");

        // Bloquear placa
        setKeyState(true, false);

        // Habilitar campos para mostrar
        setFieldsEnabled(true);

        if (mode == UIMode.DELETE) {
            // En eliminar: solo ver, no modificar
            cliente.setEnabled(false);
            marca.setEnabled(false);
            modelo.setEnabled(false);
        }

        JOptionPane.showMessageDialog(this,
                "Vehículo cargado. Ya puedes " + (mode == UIMode.EDIT ? "editar." : "eliminar."));
    }

    private void onGuardar() {
        if (mode == UIMode.ADD) {
            JOptionPane.showMessageDialog(this, "Guardado (Añadir vehículo) - Solo UI");
        } else if (mode == UIMode.EDIT) {
            JOptionPane.showMessageDialog(this, "Guardado (Editar vehículo) - Solo UI");
        }
    }

    private void onEliminar() {
        if (mode == UIMode.DELETE) {
            JOptionPane.showMessageDialog(this, "Eliminado/Desactivado (Vehículo) - Solo UI");
        }
    }

    // ===================== UI HELPERS =====================

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
