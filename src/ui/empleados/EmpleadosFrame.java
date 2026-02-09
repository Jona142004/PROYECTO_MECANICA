package ui.empleados;

import dao.EmpleadoDAO;
import model.Empleado;
import ui.components.UIMode;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmpleadosFrame extends JFrame {

    private UIMode mode;

    // Campos
    private JTextField cedula, nombres, apellidos, direccion, telefono, correo;
    private JComboBox<String> rol; // Mecánico / Recepción

    // Tabla
    private JTable table;
    private DefaultTableModel model;

    // Botones
    private JButton btnBuscar, btnGuardar, btnEliminar;

    public EmpleadosFrame(UIMode mode) {
        this.mode = mode;
        setTitle("Gestión de Empleados");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Empleados", "Administración de Mecánicos y Recepción"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
        cargarTabla();
    }

    public EmpleadosFrame() {
        this(UIMode.ADD);
    }

    // --- LOGICA DE DATOS ---

    private void cargarTabla() {
        model.setRowCount(0);
        EmpleadoDAO dao = new EmpleadoDAO();
        List<Empleado> lista = dao.listar();
        for (Empleado e : lista) {
            // Convertimos la letra del rol a palabra completa para que se vea bonito
            String rolTexto = e.getRol().equals("M") ? "Mecánico" : "Recepción";
            
            model.addRow(new Object[]{
                e.getId(), e.getCedula(), e.getNombre(), e.getApellido(),
                rolTexto, e.getTelefono(), e.getCorreo()
            });
        }
    }

    private void onBuscar() {
        String ced = cedula.getText();
        if (ced.isEmpty()) return;

        EmpleadoDAO dao = new EmpleadoDAO();
        Empleado e = dao.buscarPorCedula(ced);

        if (e != null) {
            nombres.setText(e.getNombre());
            apellidos.setText(e.getApellido());
            direccion.setText(e.getDireccion());
            telefono.setText(e.getTelefono());
            correo.setText(e.getCorreo());
            
            // Seleccionar Rol
            if (e.getRol().equals("M")) rol.setSelectedItem("Mecánico");
            else rol.setSelectedItem("Recepción");

            setFieldsEnabled(true);
            if (mode == UIMode.EDIT) setFieldsEditable(true);
            else if (mode == UIMode.DELETE) setFieldsEditable(false);
            
            cedula.setEditable(false); // Bloquear cédula al encontrar
        } else {
            JOptionPane.showMessageDialog(this, "Empleado no encontrado.");
            limpiarCampos();
        }
    }

    private void onGuardar() {
        if (cedula.getText().isEmpty() || nombres.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Campos obligatorios vacíos.");
            return;
        }

        Empleado e = new Empleado();
        e.setCedula(cedula.getText());
        e.setNombre(nombres.getText());
        e.setApellido(apellidos.getText());
        e.setDireccion(direccion.getText());
        e.setTelefono(telefono.getText());
        e.setCorreo(correo.getText());
        
        // Convertir selección a 'M' o 'R'
        String rolSeleccionado = (String) rol.getSelectedItem();
        e.setRol(rolSeleccionado.equals("Mecánico") ? "M" : "R");

        EmpleadoDAO dao = new EmpleadoDAO();
        boolean exito = false;

        if (mode == UIMode.ADD) {
            exito = dao.registrar(e);
        } else if (mode == UIMode.EDIT) {
            exito = dao.actualizar(e);
        }

        if (exito) {
            JOptionPane.showMessageDialog(this, "Guardado correctamente.");
            cargarTabla();
            if (mode == UIMode.ADD) limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar.");
        }
    }

    private void onEliminar() {
        String ced = cedula.getText();
        if (ced.isEmpty()) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, "¿Desactivar empleado?");
        if (confirm == JOptionPane.YES_OPTION) {
            EmpleadoDAO dao = new EmpleadoDAO();
            if (dao.eliminar(ced)) {
                JOptionPane.showMessageDialog(this, "Empleado eliminado.");
                cargarTabla();
                limpiarCampos();
            }
        }
    }

    private void limpiarCampos() {
        cedula.setText(""); nombres.setText(""); apellidos.setText("");
        direccion.setText(""); telefono.setText(""); correo.setText("");
        rol.setSelectedIndex(0);
        cedula.setEditable(true);
    }

    // --- UI HELPERS ---

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(UITheme.BG);

        JPanel form = UITheme.cardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        cedula = new JTextField();
        nombres = new JTextField();
        apellidos = new JTextField();
        direccion = new JTextField();
        telefono = new JTextField();
        correo = new JTextField();
        rol = new JComboBox<>(new String[]{"Mecánico", "Recepción"});

        int r = 0;
        addField(form, g, r++, "Cédula", cedula);
        addField(form, g, r++, "Nombres", nombres);
        addField(form, g, r++, "Apellidos", apellidos);
        addField(form, g, r++, "Rol", rol); // Nuevo campo
        addField(form, g, r++, "Dirección", direccion);
        addField(form, g, r++, "Teléfono", telefono);
        addField(form, g, r++, "Correo", correo);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        btnBuscar = UITheme.primaryButton("Buscar");
        btnGuardar = UITheme.primaryButton("Guardar");
        btnEliminar = UITheme.primaryButton("Eliminar");
        actions.add(btnBuscar); actions.add(btnGuardar); actions.add(btnEliminar);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        form.add(actions, g);

        // Tabla
        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"ID", "Cédula", "Nombres", "Apellidos", "Rol", "Teléfono", "Correo"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.4);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> onBuscar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnEliminar.addActionListener(e -> onEliminar());

        return root;
    }

    private void applyMode(UIMode m) {
        this.mode = m;
        btnBuscar.setVisible(m != UIMode.ADD);
        btnGuardar.setVisible(m != UIMode.DELETE);
        btnEliminar.setVisible(m == UIMode.DELETE);
        
        boolean editable = (m == UIMode.ADD);
        setFieldsEnabled(editable);
        cedula.setEditable(true); // Siempre editable al inicio para buscar o escribir nueva
    }

    private void setFieldsEnabled(boolean b) {
        nombres.setEnabled(b); apellidos.setEnabled(b);
        direccion.setEnabled(b); telefono.setEnabled(b);
        correo.setEnabled(b); rol.setEnabled(b);
    }
    
    private void setFieldsEditable(boolean b) {
        // Método auxiliar para habilitar/deshabilitar edición tras buscar
        nombres.setEditable(b); apellidos.setEditable(b);
        direccion.setEditable(b); telefono.setEditable(b);
        correo.setEditable(b); rol.setEnabled(b);
    }

    private JPanel header(String title, String subtitle) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        top.setPreferredSize(new Dimension(0, 64));
        JLabel t = new JLabel("  " + title); t.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel s = new JLabel("  " + subtitle); s.setForeground(UITheme.MUTED);
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(t); text.add(s);
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