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

    private JTextField cedula, nombres, apellidos, direccion, telefono, correo;
    private JComboBox<String> rol; // Mecánico / Recepción
    private JComboBox<String> estado; // VISUALIZACIÓN ESTADO

    private JTable table;
    private DefaultTableModel model;

    private JButton btnBuscar, btnGuardar, btnEliminar, btnLimpiar;
    
    private String estadoActualSeleccionado = "";

    public EmpleadosFrame(UIMode mode) {
        this.mode = mode;
        setTitle("Gestión de Empleados");
        setSize(1150, 650); // Un poco más ancho para que entren las columnas
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Empleados", "Administración de personal"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
        cargarTabla();
        
        // Poner foco en cédula al iniciar
        SwingUtilities.invokeLater(() -> cedula.requestFocusInWindow());
    }

    public EmpleadosFrame() {
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

        cedula = new JTextField();
        nombres = new JTextField();
        apellidos = new JTextField();
        direccion = new JTextField();
        telefono = new JTextField();
        correo = new JTextField();
        rol = new JComboBox<>(new String[]{"Mecánico", "Recepción"});
        estado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});
        estado.setEnabled(false); // El estado es automático

        int r = 0;
        addField(form, g, r++, "Cédula", cedula);
        addField(form, g, r++, "Nombres", nombres);
        addField(form, g, r++, "Apellidos", apellidos);
        addField(form, g, r++, "Dirección", direccion); // Campo Dirección
        addField(form, g, r++, "Rol", rol);
        addField(form, g, r++, "Teléfono", telefono);
        addField(form, g, r++, "Correo", correo);
        addField(form, g, r++, "Estado", estado);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        btnBuscar = UITheme.primaryButton("Buscar");
        btnGuardar = UITheme.primaryButton("Guardar");
        btnEliminar = UITheme.primaryButton("Eliminar");
        btnLimpiar = UITheme.primaryButton("Limpiar");

        actions.add(btnBuscar); actions.add(btnGuardar); actions.add(btnEliminar); actions.add(btnLimpiar);
        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        form.add(actions, g);

        // TABLA: AÑADIDA COLUMNA DIRECCIÓN
        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());

        // Definimos las columnas exactas
        String[] cols = {"Cédula", "Nombres", "Apellidos", "Dirección", "Rol", "Teléfono", "Correo", "Estado"};

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);

        // Listener de selección
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                onTableSelect();
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.4);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> onBuscar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        return root;

    } 

    // --- LÓGICA DE NEGOCIO ---

    private void cargarTabla() {
        model.setRowCount(0);
        EmpleadoDAO dao = new EmpleadoDAO();
        List<Empleado> lista = dao.listarTodos(); // Usa listarTodos del DAO nuevo
        for (Empleado e : lista) {
            String rolTexto = "M".equals(e.getRol()) ? "Mecánico" : "Recepción";
            model.addRow(new Object[]{
                e.getCedula(),      // 0
                e.getNombre(),      // 1
                e.getApellido(),    // 2
                e.getDireccion(),   // 3 (Dirección)
                rolTexto,           // 4
                e.getTelefono(),    // 5
                e.getCorreo(),      // 6
                e.getEstado()       // 7
            });
        }
    }

    private void onTableSelect() {
        if (mode == UIMode.ADD) return;

        int row = table.getSelectedRow();
        if (row == -1) return;

        // USAMOS safeStr PARA EVITAR ERRORES SI UN CAMPO ES NULL
        cedula.setText(safeStr(table.getValueAt(row, 0)));
        nombres.setText(safeStr(table.getValueAt(row, 1)));
        apellidos.setText(safeStr(table.getValueAt(row, 2)));
        direccion.setText(safeStr(table.getValueAt(row, 3))); // Carga dirección
        
        String rolTxt = safeStr(table.getValueAt(row, 4));
        rol.setSelectedItem(rolTxt);
        
        telefono.setText(safeStr(table.getValueAt(row, 5)));
        correo.setText(safeStr(table.getValueAt(row, 6)));
        
        String est = safeStr(table.getValueAt(row, 7));
        estado.setSelectedItem("A".equals(est) ? "ACTIVO" : "INACTIVO");
        estadoActualSeleccionado = est;

        if (mode == UIMode.EDIT) {
            setFieldsEditable(true);
            cedula.setEditable(false); // No editar la llave
        }
    }

    // Método de seguridad para evitar NullPointerException
    private String safeStr(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private void onGuardar() {
        if (cedula.getText().isEmpty() || nombres.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Campos obligatorios vacíos."); return;
        }

        Empleado e = new Empleado();
        e.setCedula(cedula.getText());
        e.setNombre(nombres.getText());
        e.setApellido(apellidos.getText());
        e.setDireccion(direccion.getText());
        e.setTelefono(telefono.getText());
        e.setCorreo(correo.getText());
        
        String rolSel = (String) rol.getSelectedItem();
        e.setRol(rolSel.equals("Mecánico") ? "M" : "R");

        EmpleadoDAO dao = new EmpleadoDAO();
        boolean exito = false;

        if (mode == UIMode.ADD) {
            exito = dao.registrar(e);
        } else if (mode == UIMode.EDIT) {
            exito = dao.actualizar(e);
        }

        if (exito) {
            JOptionPane.showMessageDialog(this, "Guardado correctamente.");
            limpiarCampos();
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar. Verifique duplicados.");
        }
    }

    private void onEliminar() {
        String ced = cedula.getText();
        if (ced.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un empleado."); return;
        }
        
        // Si ya está inactivo, avisamos
        if ("I".equals(estadoActualSeleccionado) || "INACTIVO".equals(estado.getSelectedItem())) {
            JOptionPane.showMessageDialog(this, "El empleado tiene facturas registradas.\nNo se puede borrar de la BD.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar empleado?\n", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            EmpleadoDAO dao = new EmpleadoDAO();
            
            // 1=Borrado Total, 2=Desactivado, 0=Error
            int resultado = dao.eliminar(ced);
            
            if (resultado == 1) {
                JOptionPane.showMessageDialog(this, "Empleado ELIMINADO COMPLETAMENTE del sistema.");
                limpiarCampos();
                cargarTabla();
            } else if (resultado == 2) {
                JOptionPane.showMessageDialog(this, "El empleado tiene historial.\nSe ha marcado como INACTIVO.");
                limpiarCampos();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.");
            }
        }
    }

    private void onBuscar() {
        String ced = cedula.getText();
        if (ced.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese cédula."); return; }

        EmpleadoDAO dao = new EmpleadoDAO();
        Empleado e = dao.buscarPorCedula(ced);

        if (e != null) {
            nombres.setText(e.getNombre());
            apellidos.setText(e.getApellido());
            direccion.setText(e.getDireccion());
            telefono.setText(e.getTelefono());
            correo.setText(e.getCorreo());
            rol.setSelectedItem("M".equals(e.getRol()) ? "Mecánico" : "Recepción");
            estado.setSelectedItem("A".equals(e.getEstado()) ? "ACTIVO" : "INACTIVO");
            estadoActualSeleccionado = e.getEstado();

            if (mode == UIMode.EDIT) {
                setFieldsEditable(true);
                cedula.setEditable(false);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No encontrado.");
            limpiarCampos();
        }
    }

    private void limpiarCampos() {
        cedula.setText(""); nombres.setText(""); apellidos.setText("");
        direccion.setText(""); telefono.setText(""); correo.setText("");
        rol.setSelectedIndex(0);
        estado.setSelectedIndex(0);
        estadoActualSeleccionado = "";
        
        table.clearSelection();
        
        if (mode == UIMode.EDIT) {
            cedula.setEditable(true);
            setFieldsEditable(false);
        }
        cedula.requestFocus();
    }
    
    // --- ESTILOS Y HELPERS ---
    
    private void applyMode(UIMode m) {
        this.mode = m;
        btnBuscar.setVisible(m == UIMode.EDIT || m == UIMode.DELETE);
        btnGuardar.setVisible(m == UIMode.ADD || m == UIMode.EDIT);
        btnEliminar.setVisible(m == UIMode.DELETE);
        
        if (m == UIMode.ADD) {
            btnGuardar.setText("Registrar");
            setFieldsEditable(true);
            table.setEnabled(false);
        } else if (m == UIMode.EDIT) {
            btnGuardar.setText("Guardar Cambios");
            setFieldsEditable(false);
            cedula.setEditable(true);
            table.setEnabled(true);
        } else if (m == UIMode.DELETE) {
            setFieldsEditable(false);
            cedula.setEditable(true);
            table.setEnabled(true);
        }
    }

    private void setFieldsEditable(boolean b) {
        nombres.setEditable(b); apellidos.setEditable(b);
        direccion.setEditable(b); telefono.setEditable(b);
        correo.setEditable(b); rol.setEnabled(b);
    }

    private JPanel header(String t, String s) { 
        JPanel top = new JPanel(new BorderLayout()); 
        top.setBackground(Color.WHITE); 
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER)); 
        top.setPreferredSize(new Dimension(0, 64));

        JLabel title = new JLabel("  " + t); 
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        JLabel sub = new JLabel("  " + s); 
        sub.setForeground(UITheme.MUTED);

        JPanel text = new JPanel(); 
        text.setOpaque(false); 
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        
        // CORREGIDO: Agregar componentes, no strings
        text.add(title); 
        text.add(sub);

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