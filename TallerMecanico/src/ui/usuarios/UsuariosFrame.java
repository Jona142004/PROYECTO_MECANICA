package ui.usuarios;

import ui.components.SelectorPanel;
import ui.components.UIMode;
import ui.empleados.EmpleadoDialog;   // ✅ mejor que ClienteDialog
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UsuariosFrame extends JFrame {

    private UIMode mode;

    // Campos
    private SelectorPanel empleadoRecepcion;
    private JTextField usuario;
    private JPasswordField pass;
    private JComboBox<String> permiso;
    private JComboBox<String> estado;

    // Tabla
    private JTable table;
    private DefaultTableModel model;

    // Botones
    private JButton btnBuscar;   // solo EDIT/DELETE
    private JButton btnGuardar;  // ADD/EDIT
    private JButton btnEliminar; // DELETE

    public UsuariosFrame(UIMode mode) {
        this.mode = mode;

        setTitle("Usuarios - Autos y Motores");
        setSize(1080, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Gestión de Usuarios", "Solo personal de recepción (solo UI)"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
    }

    public UsuariosFrame() {
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

        empleadoRecepcion = new SelectorPanel("(Seleccionar empleado de Recepción)");
        usuario = new JTextField();               // ✅ clave para buscar
        pass = new JPasswordField();
        permiso = new JComboBox<>(new String[]{"ADMINISTRADOR", "EMPLEADO GENERAL"});
        estado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

        empleadoRecepcion.setOnSearch(() -> {
            EmpleadoDialog dialog = new EmpleadoDialog(this, empleadoRecepcion::setText);
            dialog.setVisible(true);
        });

        int r = 0;
        addField(form, g, r++, "Usuario (clave)", usuario);                 // ✅ clave arriba
        addField(form, g, r++, "Empleado (Recepción)", empleadoRecepcion);
        addField(form, g, r++, "Contraseña", pass);
        addField(form, g, r++, "Permiso", permiso);
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
        String[] cols = {"ID", "Empleado", "Usuario", "Permiso", "Estado"};
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
            setKeyState(true, true);     // ✅ solo usuario editable
            setFieldsEnabled(false);     // 🔒 hasta buscar
        }

        if (m == UIMode.DELETE) {
            setKeyState(true, true);     // ✅ solo usuario editable
            setFieldsEnabled(false);     // 🔒 hasta buscar
        }

        revalidate();
        repaint();
    }

    private void setKeyState(boolean enabled, boolean editable) {
        usuario.setEnabled(enabled);
        usuario.setEditable(editable);
    }

    private void setFieldsEnabled(boolean enabled) {
        empleadoRecepcion.setEnabled(enabled);
        pass.setEnabled(enabled);
        permiso.setEnabled(enabled);
        estado.setEnabled(enabled);

        // Para password también controlamos editable
        pass.setEditable(enabled);
    }

    private void onBuscar() {
        if (mode != UIMode.EDIT && mode != UIMode.DELETE) return;

        // Mock: cargar datos del usuario
        empleadoRecepcion.setText("Ana Torres - Recepción");
        pass.setText("1234");
        permiso.setSelectedItem("EMPLEADO GENERAL");
        estado.setSelectedItem("ACTIVO");

        // Bloquear clave
        setKeyState(true, false);

        if (mode == UIMode.EDIT) {
            setFieldsEnabled(true);     // ✅ ahora sí puede editar
        } else {
            setFieldsEnabled(false);    // DELETE: solo lectura
        }

        JOptionPane.showMessageDialog(this,
                "Usuario cargado. Ya puedes " + (mode == UIMode.EDIT ? "editar." : "eliminar."));
    }

    private void onGuardar() {
        if (mode == UIMode.ADD) JOptionPane.showMessageDialog(this, "Guardado (ADD) - Solo UI");
        if (mode == UIMode.EDIT) JOptionPane.showMessageDialog(this, "Guardado (EDIT) - Solo UI");
    }

    private void onEliminar() {
        if (mode == UIMode.DELETE) JOptionPane.showMessageDialog(this, "Eliminado (DELETE) - Solo UI");
    }

    // ===== UI helpers =====
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
