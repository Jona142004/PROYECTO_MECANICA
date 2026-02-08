package ui.clientes;

import dao.ClienteDAO;
import model.Cliente;
import ui.components.UIMode;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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
        cargarTabla();
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
    // Solo buscamos si estamos en modo EDITAR o ELIMINAR
    if (mode != UIMode.EDIT && mode != UIMode.DELETE) return;

    String ced = cedula.getText();
    if (ced.isEmpty()) {
        cargarTabla();
        JOptionPane.showMessageDialog(this, "Ingrese una cédula para buscar.");
        return;
    }

    ClienteDAO dao = new ClienteDAO();
    Cliente c = dao.buscarPorCedula(ced);

    if (c != null) {
        // ¡Encontrado! Llenamos las cajas de texto
        nombres.setText(c.getNombre());
        apellidos.setText(c.getApellido());
        direccion.setText(c.getDireccion());
        telefono.setText(c.getTelefono());
        correo.setText(c.getCorreo());
        estado.setSelectedItem("ACTIVO"); // Tu DAO solo trae activos por ahora

        // Lógica de la interfaz (bloquear cédula, habilitar resto)
        setKeyState(true, false); // Cédula visible pero no editable
        setFieldsEnabled(true);   // Habilitar campos

        if (mode == UIMode.EDIT) {
            setFieldsEditable(true);
        } else {
            // En modo eliminar, mostramos los datos pero no dejamos editar
            setFieldsEditable(false);
            estado.setEnabled(false);
        }
    } else {
        JOptionPane.showMessageDialog(this, "Cliente no encontrado o inactivo.");
        // Opcional: limpiar campos si no encuentra nada
        limpiarCampos();
        cargarTabla(); // (Tendrías que crear este método auxiliar)
    }
}
private void onGuardar() {
    // 1. Recoger datos
    String ced = cedula.getText();
    String nom = nombres.getText();
    String ape = apellidos.getText();
    String dir = direccion.getText();
    String tel = telefono.getText();
    String cor = correo.getText();

    // 2. Validar
    if (ced.isEmpty() || nom.isEmpty() || ape.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Cédula, Nombres y Apellidos son obligatorios.");
        return;
    }

    // 3. Crear objeto Cliente
    Cliente c = new Cliente();
    c.setCedula(ced);
    c.setNombre(nom);
    c.setApellido(ape);
    c.setDireccion(dir);
    c.setTelefono(tel);
    c.setCorreo(cor);
    
    // 4. Llamar al DAO
    ClienteDAO dao = new ClienteDAO();
    boolean exito = false;

    if (mode == UIMode.ADD) {
        exito = dao.registrar(c);
    } else if (mode == UIMode.EDIT) {
        exito = dao.actualizar(c);
    }

    // 5. Resultado
    if (exito) {
    JOptionPane.showMessageDialog(this, "Operación exitosa.");
    limpiarCampos(); // <--- AQUÍ
    
    } else {
        JOptionPane.showMessageDialog(this, "Error al guardar. Verifica los datos.");
    }
    cargarTabla();
}

 private void onEliminar() {
    if (mode == UIMode.DELETE) {
        String ced = cedula.getText();
        if (ced.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Seguro que deseas eliminar este cliente?", 
            "Confirmar", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            ClienteDAO dao = new ClienteDAO();
            
            // CORRECCIÓN: Llamamos al método eliminar y guardamos el resultado
            boolean exito = dao.eliminar(ced); 

            if (exito) {
                JOptionPane.showMessageDialog(this, "Cliente eliminado correctamente.");
                limpiarCampos(); 
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar. Verifique la conexión.");
            }
        }
    }
    cargarTabla();
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
    private void limpiarCampos() {
        cedula.setText("");
        nombres.setText("");
        apellidos.setText("");
        direccion.setText("");
        telefono.setText("");
        correo.setText("");
        estado.setSelectedItem("ACTIVO"); // O setSelectedIndex(0);
        
        // Opcional: Poner el cursor de nuevo en la cédula para seguir escribiendo
        cedula.requestFocus(); 
    }
    private void cargarTabla() {
        // 1. Limpiar la tabla actual
        model.setRowCount(0);
        
        // 2. Pedir datos al DAO
        ClienteDAO dao = new ClienteDAO();
        List<Cliente> lista = dao.listarActivos();
        
        // 3. Llenar la tabla fila por fila
        for (Cliente c : lista) {
            model.addRow(new Object[]{
                c.getId(),
                c.getCedula(),
                c.getNombre(),
                c.getApellido(),
                c.getTelefono(),
                c.getCorreo(),
                c.getEstado()
            });
        }
    }
}
