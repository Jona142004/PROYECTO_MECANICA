package ui.clientes;

import dao.ClienteDAO;
import model.Cliente;
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
    private JButton btnBuscar, btnGuardar, btnEliminar, btnLimpiar;
    private String estadoActualSeleccionado = "";

    public ClientesFrame(UIMode mode) {
        this.mode = mode;
        setTitle("Clientes - Autos y Motores");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(header("Gestión de Clientes", "Administración de base de datos"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);
        applyMode(mode);
        cargarTabla();
        SwingUtilities.invokeLater(() -> cedula.requestFocusInWindow());
    }

    public ClientesFrame() { this(UIMode.ADD); }

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(UITheme.BG);

        JPanel form = UITheme.cardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

        cedula = new JTextField();
        nombres = new JTextField();
        apellidos = new JTextField();
        direccion = new JTextField();
        telefono = new JTextField();
        correo = new JTextField();
        estado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});
        estado.setEnabled(false);

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
        btnBuscar = UITheme.primaryButton("Buscar");
        btnGuardar = UITheme.primaryButton("Guardar");
        btnEliminar = UITheme.primaryButton("Eliminar");
        btnLimpiar = UITheme.primaryButton("Limpiar");
        
        actions.add(btnBuscar); actions.add(btnGuardar); actions.add(btnEliminar); actions.add(btnLimpiar);
        g.gridx = 0; g.gridy = r; g.gridwidth = 2; form.add(actions, g);

        // TABLA
        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"ID", "Cédula", "Nombres", "Apellidos", "Dirección", "Teléfono", "Correo", "Estado"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) onTableSelect();
        });
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.38); split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> onBuscar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        return root;
    }

    private void onTableSelect() {
        if (mode == UIMode.ADD) return;
        int row = table.getSelectedRow();
        if (row == -1) return;

        cedula.setText(safeStr(table.getValueAt(row, 1)));
        nombres.setText(safeStr(table.getValueAt(row, 2)));
        apellidos.setText(safeStr(table.getValueAt(row, 3)));
        direccion.setText(safeStr(table.getValueAt(row, 4))); 
        telefono.setText(safeStr(table.getValueAt(row, 5)));
        correo.setText(safeStr(table.getValueAt(row, 6)));
        
        String est = safeStr(table.getValueAt(row, 7));
        estado.setSelectedItem(est.equals("A") ? "ACTIVO" : "INACTIVO");
        estadoActualSeleccionado = est;

        if (mode == UIMode.EDIT) {
            setFieldsEditable(true);
            cedula.setEditable(false);
        }
    }

    private void onEliminar() {
        String ced = cedula.getText();
        if (ced.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente."); return;
        }

        // Si ya está inactivo, avisamos
        if ("I".equals(estadoActualSeleccionado)) {
            JOptionPane.showMessageDialog(this, "El cliente ya es histórico (Inactivo).\nNo se puede borrar de la BD.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar cliente?\nSi tiene historial (vehículos/facturas) pasará a INACTIVO.", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ClienteDAO dao = new ClienteDAO();
            
            // Lógica inteligente: 1=Borrado Físico, 2=Borrado Lógico
            int resultado = dao.eliminar(ced);
            
            if (resultado == 1) {
                JOptionPane.showMessageDialog(this, "Cliente ELIMINADO COMPLETAMENTE de la base de datos.");
                limpiarCampos();
                cargarTabla();
            } else if (resultado == 2) {
                JOptionPane.showMessageDialog(this, "El cliente tiene historial (vehículos o facturas).\nSe ha marcado como INACTIVO.");
                limpiarCampos();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.");
            }
        }
    }

    private void onGuardar() {
        if (cedula.getText().isEmpty() || nombres.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Campos obligatorios vacíos."); return;
        }
        Cliente c = new Cliente();
        c.setCedula(cedula.getText());
        c.setNombre(nombres.getText());
        c.setApellido(apellidos.getText());
        c.setDireccion(direccion.getText());
        c.setTelefono(telefono.getText());
        c.setCorreo(correo.getText());
        
        // Leemos el estado del combo (para poder reactivar si es necesario)
        String estSel = (String) estado.getSelectedItem();
        c.setEstado(estSel.equals("ACTIVO") ? "A" : "I");

        ClienteDAO dao = new ClienteDAO();
        boolean exito = (mode == UIMode.ADD) ? dao.registrar(c) : dao.actualizar(c);

        if (exito) {
            JOptionPane.showMessageDialog(this, "Operación exitosa.");
            limpiarCampos();
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar.");
        }
    }

    private void onBuscar() {
        String ced = cedula.getText();
        if (ced.isEmpty()) return;
        Cliente c = new ClienteDAO().buscarPorCedula(ced);
        if (c != null) {
            nombres.setText(c.getNombre());
            apellidos.setText(c.getApellido());
            direccion.setText(c.getDireccion());
            telefono.setText(c.getTelefono());
            correo.setText(c.getCorreo());
            estado.setSelectedItem(c.getEstado().equals("A") ? "ACTIVO" : "INACTIVO");
            estadoActualSeleccionado = c.getEstado();
            if (mode == UIMode.EDIT) {
                setFieldsEditable(true);
                cedula.setEditable(false);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Cliente no encontrado.");
        }
    }

    private void cargarTabla() {
        model.setRowCount(0);
        for (Cliente c : new ClienteDAO().listarTodos()) {
            model.addRow(new Object[]{ c.getId(), c.getCedula(), c.getNombre(), c.getApellido(), c.getDireccion(), c.getTelefono(), c.getCorreo(), c.getEstado() });
        }
    }

    private void limpiarCampos() {
        cedula.setText(""); nombres.setText(""); apellidos.setText("");
        direccion.setText(""); telefono.setText(""); correo.setText("");
        estado.setSelectedIndex(0);
        estadoActualSeleccionado = "";
        table.clearSelection();
        if (mode == UIMode.EDIT) { cedula.setEditable(true); setFieldsEditable(false); }
        cedula.requestFocus();
    }

    private void applyMode(UIMode m) {
        this.mode = m;
        btnBuscar.setVisible(m == UIMode.EDIT || m == UIMode.DELETE);
        btnGuardar.setVisible(m == UIMode.ADD || m == UIMode.EDIT);
        btnEliminar.setVisible(m == UIMode.DELETE);
        if (m == UIMode.ADD) {
            btnGuardar.setText("Registrar");
            setFieldsEditable(true); table.setEnabled(false); estado.setEnabled(false);
        } else if (m == UIMode.EDIT) {
            btnGuardar.setText("Guardar Cambios");
            setFieldsEditable(false); cedula.setEditable(true); table.setEnabled(true); estado.setEnabled(true);
        } else {
            setFieldsEditable(false); cedula.setEditable(true); table.setEnabled(true); estado.setEnabled(false);
        }
    }

    private void setFieldsEditable(boolean b) {
        nombres.setEditable(b); apellidos.setEditable(b); direccion.setEditable(b); telefono.setEditable(b); correo.setEditable(b);
    }
    
    private String safeStr(Object o) { return o == null ? "" : o.toString(); }

    private JPanel header(String t, String s) { 
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(Color.WHITE); 
        top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,UITheme.BORDER)); top.setPreferredSize(new Dimension(0,64));
        JLabel title = new JLabel("  "+t); title.setFont(new Font("SansSerif",Font.BOLD,16));
        JLabel sub = new JLabel("  "+s); sub.setForeground(UITheme.MUTED);
        JPanel txt = new JPanel(); txt.setOpaque(false); txt.setLayout(new BoxLayout(txt,BoxLayout.Y_AXIS)); 
        txt.add(title); txt.add(sub);
        top.add(txt, BorderLayout.WEST); return top;
    }
    private void addField(JPanel p, GridBagConstraints g, int r, String l, JComponent f) { g.gridx=0; g.gridy=r; p.add(new JLabel(l),g); g.gridx=1; p.add(f,g); }
    
}