package ui.clientes;

import ui.components.UIMode;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ClientesFrame extends JFrame {

    private UIMode mode;

    private JTextField cedula, nombres, apellidos, direccion, telefono, correo;
    private JComboBox<String> estado;

    private JTable table;
    private DefaultTableModel model;

    private JButton btnBuscar;    // interno (solo EDIT/DELETE)
    private JButton btnGuardar;   // ADD/EDIT
    private JButton btnEliminar;  // DELETE

    public ClientesFrame(UIMode mode) {
        this.mode = mode;

        setTitle("Clientes - Autos y Motores");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Gestión de Clientes", "Ingreso, búsqueda, actualización y desactivación"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
    }

    public ClientesFrame() {
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
        g.weightx = 1;

        cedula = new JTextField();
        nombres = new JTextField();
        apellidos = new JTextField();
        direccion = new JTextField();
        telefono = new JTextField();
        correo = new JTextField();
        estado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});


        int r = 0;
        addField(form, g, r++, "Cédula/RUC", cedula);
        addField(form, g, r++, "Nombres", nombres);
        addField(form, g, r++, "Apellidos", apellidos);
        addField(form, g, r++, "Dirección", direccion);
        addField(form, g, r++, "Teléfono", telefono);
        addField(form, g, r++, "Correo", correo);
        addField(form, g, r++, "Estado", estado);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        btnBuscar = UITheme.primaryButton("Buscar");     // solo EDIT/DELETE
        btnGuardar = UITheme.primaryButton("Guardar");   // ADD/EDIT
        btnEliminar = UITheme.primaryButton("Eliminar"); // DELETE

        actions.add(btnBuscar);
        actions.add(btnGuardar);
        actions.add(btnEliminar);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        form.add(actions, g);

        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"ID", "Cédula/RUC", "Nombres", "Apellidos", "Teléfono", "Correo", "Estado"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setRowHeight(26);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.38);
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
            setFieldsEditable(true);
        }

        if (m == UIMode.EDIT) {
            btnGuardar.setText("Guardar");
            setKeyState(true, true);     // ✅ solo cédula editable al inicio
            setFieldsEnabled(false);     // 🔒 resto deshabilitado
            setFieldsEditable(false);
        }

        if (m == UIMode.DELETE) {
            setKeyState(true, true);     // ✅ solo cédula editable al inicio
            setFieldsEnabled(false);     // 🔒 resto deshabilitado
            setFieldsEditable(false);
        }

        revalidate();
        repaint();
    }

    private void setKeyState(boolean enabled, boolean editable) {
        cedula.setEnabled(enabled);
        cedula.setEditable(editable);
    }

    private void setFieldsEnabled(boolean enabled) {
        nombres.setEnabled(enabled);
        apellidos.setEnabled(enabled);
        direccion.setEnabled(enabled);
        telefono.setEnabled(enabled);
        correo.setEnabled(enabled);
        estado.setEnabled(enabled);
    }

    private void setFieldsEditable(boolean editable) {
        nombres.setEditable(editable);
        apellidos.setEditable(editable);
        direccion.setEditable(editable);
        telefono.setEditable(editable);
        correo.setEditable(editable);
    }

    private void onBuscar() {
        if (mode != UIMode.EDIT && mode != UIMode.DELETE) return;

        // Mock: cargar datos
        nombres.setText("Juan");
        apellidos.setText("Pérez");
        direccion.setText("Av. 12 de Abril");
        telefono.setText("0999999999");
        correo.setText("juan@mail.com");
        estado.setSelectedItem("ACTIVO");

        // Bloquear clave después de encontrar
        setKeyState(true, false);

        // Habilitar resto
        setFieldsEnabled(true);

        if (mode == UIMode.EDIT) {
            // ✅ editar: campos editables
            setFieldsEditable(true);
        } else {
            // ✅ eliminar: campos solo lectura
            setFieldsEditable(false);
            estado.setEnabled(false);
        }

        JOptionPane.showMessageDialog(this,
                "Cliente cargado. Ya puedes " + (mode == UIMode.EDIT ? "editar." : "eliminar/desactivar."));
    }

    private void onGuardar() {
        if (mode == UIMode.ADD) {
            JOptionPane.showMessageDialog(this, "Guardado (Añadir) - Solo UI");
        } else if (mode == UIMode.EDIT) {
            JOptionPane.showMessageDialog(this, "Guardado (Editar) - Solo UI");
        }
    }

    private void onEliminar() {
        if (mode == UIMode.DELETE) {
            JOptionPane.showMessageDialog(this, "Eliminado/Desactivado - Solo UI");
        }
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
