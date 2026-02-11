package ui.usuarios;

import dao.UsuarioDAO;
import model.Usuario;
import ui.components.SelectorPanel; // Usamos el componente con Lupa
import ui.components.UIMode;
import ui.empleados.EmpleadoDialog; // Usamos el buscador de Empleados
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsuariosFrame extends JFrame {

    private UIMode mode;

    // --- CAMPOS ---
    private SelectorPanel selEmpleado; // CAMBIO: Usamos SelectorPanel en vez de JTextField
    private JTextField txtUsuario;
    private JPasswordField txtClave;
    private JComboBox<String> cmbRol;
    
    // Variables de control
   // private int idEmpleadoSeleccionado = -1;
    private int idUsuarioSeleccionado = -1;
    private String cedulaEmpleadoSeleccionada = "";

    // Tabla y Botones
    private JButton btnBuscarUsuario; 
    private JTable table;
    private DefaultTableModel model;
    private JButton btnGuardar;
    private JButton btnEliminar;

    public UsuariosFrame(UIMode mode) {
        this.mode = mode;
        setTitle("Gestión de Usuarios - Autos y Motores");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String titulo = (mode == UIMode.ADD) ? "Crear Nuevo Usuario" : "Eliminar Usuario";
        add(header(titulo, "Gestión de credenciales del sistema"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
        cargarTabla(); 
    }
    
    public UsuariosFrame() { this(UIMode.ADD); }

    // --- CARGA DE DATOS ---
    private void cargarTabla() {
        model.setRowCount(0);
        UsuarioDAO dao = new UsuarioDAO();
        List<Usuario> lista = dao.listar();
        for (Usuario u : lista) {
            model.addRow(new Object[]{
                u.getId(),
                u.getUsuario(),
                u.getRolNombre(), 
                u.getNombreEmpleado(), // "Cédula - Nombre"
                u.getEstado()
            });
        }
    }

    // --- INTERFAZ GRÁFICA ---
    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(UITheme.BG);

        // 1. PANEL FORMULARIO
        JPanel form = UITheme.cardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6); g.fill = GridBagConstraints.HORIZONTAL;

        // CONFIGURACIÓN DEL BUSCADOR DE EMPLEADO
        selEmpleado = new SelectorPanel("(Buscar Empleado)");
        selEmpleado.setOnSearch(() -> {
            // Abrimos el diálogo filtrando por "R" (Recepcionistas)
            new EmpleadoDialog(this, (resultado) -> {
                // 1. Mostramos el texto en el panel
                selEmpleado.setText(resultado);
                
                try {
                    // 2. Dividimos la cadena: "ID - NOMBRE APELLIDO - CEDULA"
                    String[] partes = resultado.split(" - ");
                    
                    if (partes.length == 3) {
                        // El formato es exacto, extraemos por índice fijo
                        // idEmpleadoSeleccionado = Integer.parseInt(partes[0].trim()); // Si necesitas el ID
                        cedulaEmpleadoSeleccionada = partes[2].trim();
                    } else {
                        // Fallback por si el formato cambia o falta alguna parte
                        cedulaEmpleadoSeleccionada = partes[partes.length - 1].trim();
                    }                   
                } catch (Exception e) {
                    System.err.println("Error al parsear selección: " + e.getMessage());
                    cedulaEmpleadoSeleccionada = "";
                }
            }, "R").setVisible(true);
        });

        txtUsuario = new JTextField();
        txtClave = new JPasswordField();
        cmbRol = new JComboBox<>(new String[]{"Administrador", "Empleado"});

        btnBuscarUsuario = UITheme.primaryButton("🔍 Buscar Usuario");
        btnBuscarUsuario.addActionListener(e -> abrirBuscadorUsuarios());

        int r = 0;
        if (mode == UIMode.DELETE) {
            g.gridx = 0; g.gridy = r++; g.gridwidth = 2;
            form.add(btnBuscarUsuario, g);
            g.gridwidth = 1; 
        }
        
        addField(form, g, r++, "Empleado", selEmpleado); 
        addField(form, g, r++, "Usuario", txtUsuario);
        addField(form, g, r++, "Contraseña", txtClave);
        addField(form, g, r++, "Permiso", cmbRol);

        // Botones
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        
        btnGuardar = UITheme.primaryButton("Guardar Usuario");
        btnEliminar = new JButton("ELIMINAR USUARIO");
        btnEliminar.setBackground(new Color(220, 38, 38)); 
        btnEliminar.setForeground(Color.WHITE);
        
        actions.add(btnGuardar); 
        actions.add(btnEliminar);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2; form.add(actions, g);

        // 2. PANEL TABLA
        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"ID Usr", "Usuario", "Rol", "Empleado Asignado", "Estado"};
        
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.4); split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        // Listeners
        btnGuardar.addActionListener(e -> onGuardar());
        btnEliminar.addActionListener(e -> onEliminar());

        return root;
    }

    // --- LÓGICA DE NEGOCIO ---

    private void abrirBuscadorUsuarios() {
        new UsuarioDialog(this, (resultado) -> {
            try {
                // Formato esperado "ID - Usuario"
                int id = Integer.parseInt(resultado.split(" - ")[0]);
                seleccionarEnTablaPorID(id);
            } catch (Exception e) {}
        }).setVisible(true);
    }
    
    private void seleccionarEnTablaPorID(int id) {
        for(int i=0; i<table.getRowCount(); i++) {
            if(Integer.parseInt(table.getValueAt(i, 0).toString()) == id) {
                table.setRowSelectionInterval(i, i);
                idUsuarioSeleccionado = id;
                txtUsuario.setText(table.getValueAt(i, 1).toString());
                selEmpleado.setText(table.getValueAt(i, 3).toString()); // Visual
                break;
            }
        }
    }

    private void onGuardar() {
        // Validamos la variable que llenamos con el diálogo
        if (cedulaEmpleadoSeleccionada.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un empleado usando la lupa."); return;
        }
        if (txtUsuario.getText().isEmpty() || txtClave.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Usuario y contraseña requeridos."); return;
        }

        Usuario u = new Usuario();
        u.setUsuario(txtUsuario.getText());
        u.setClave(new String(txtClave.getPassword()));
        u.setRol(cmbRol.getSelectedItem().equals("Administrador") ? "A" : "E");
        
        UsuarioDAO dao = new UsuarioDAO();
        // Pasamos la cédula capturada desde el diálogo
        if (dao.registrar(u, cedulaEmpleadoSeleccionada)) {
            JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.");
            cargarTabla();
            limpiar();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar.\nPosibles causas:\n- El empleado NO es de Recepción.\n- El usuario ya existe.");
        }
    }

    private void onEliminar() {
        if (idUsuarioSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario para eliminar."); return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (new UsuarioDAO().eliminar(idUsuarioSeleccionado)) {
                JOptionPane.showMessageDialog(this, "Usuario eliminado.");
                cargarTabla();
                limpiar();
            }
        }
    }
    
    private void limpiar() {
        txtUsuario.setText("");
        txtClave.setText("");
        selEmpleado.setText("(Buscar Empleado)");
        cedulaEmpleadoSeleccionada = "";
        idUsuarioSeleccionado = -1;
    }

    private void applyMode(UIMode m) {
        this.mode = m;
        btnGuardar.setVisible(m == UIMode.ADD);
        btnEliminar.setVisible(m == UIMode.DELETE);
        btnBuscarUsuario.setVisible(m == UIMode.DELETE);

        boolean editable = (m == UIMode.ADD);
        selEmpleado.setEnabled(editable); // Habilita/Deshabilita el botón lupa
        txtUsuario.setEditable(editable);
        txtClave.setEditable(editable);
        cmbRol.setEnabled(editable);
        table.setEnabled(m == UIMode.DELETE);
    }
    
    // UI Helpers
    private JPanel header(String t, String s) { 
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(Color.WHITE); top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,UITheme.BORDER)); top.setPreferredSize(new Dimension(0,64));
        JLabel title = new JLabel("  "+t); title.setFont(new Font("SansSerif",Font.BOLD,16));
        JLabel sub = new JLabel("  "+s); sub.setForeground(UITheme.MUTED);
        JPanel txt = new JPanel(); txt.setOpaque(false); txt.setLayout(new BoxLayout(txt,BoxLayout.Y_AXIS)); txt.add(title); txt.add(sub);
        top.add(txt, BorderLayout.WEST); return top;
    }
    private void addField(JPanel p, GridBagConstraints g, int r, String l, JComponent f) { g.gridx=0; g.gridy=r; p.add(new JLabel(l),g); g.gridx=1; p.add(f,g); }
}