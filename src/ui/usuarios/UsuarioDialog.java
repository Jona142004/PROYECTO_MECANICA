package ui.usuarios;

import dao.UsuarioDAO;
import model.Usuario;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class UsuarioDialog extends JDialog {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtFiltro;
    
    // Callback para devolver el resultado (Texto con ID)
    private Consumer<String> onSelect; 

    public UsuarioDialog(JFrame parent, Consumer<String> onSelect) {
        super(parent, "Buscar Usuario", true);
        this.onSelect = onSelect;
        
        setSize(800, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // --- PANEL SUPERIOR (BUSCADOR) ---
        JPanel top = new JPanel(new BorderLayout(5, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        txtFiltro = new JTextField();
        JButton btnBuscar = UITheme.primaryButton("Buscar");
        
        // Acción de buscar
        txtFiltro.addActionListener(e -> cargarTabla(txtFiltro.getText()));
        btnBuscar.addActionListener(e -> cargarTabla(txtFiltro.getText()));

        top.add(new JLabel("Usuario o Nombre Empleado: "), BorderLayout.WEST);
        top.add(txtFiltro, BorderLayout.CENTER);
        top.add(btnBuscar, BorderLayout.EAST);

        // --- TABLA CENTRAL ---
        String[] cols = {"ID", "Usuario", "Rol", "Empleado", "Estado"};
        
        // Tabla no editable
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Doble clic para seleccionar
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    seleccionarUsuario();
                }
            }
        });

        // --- PANEL INFERIOR (BOTONES) ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSeleccionar = UITheme.primaryButton("Seleccionar");
        JButton btnCancelar = new JButton("Cancelar");

        btnSeleccionar.addActionListener(e -> seleccionarUsuario());
        btnCancelar.addActionListener(e -> dispose());

        bottom.add(btnCancelar);
        bottom.add(btnSeleccionar);

        // --- ARMADO ---
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // Carga inicial
        cargarTabla(""); 
    }

    private void cargarTabla(String filtro) {
        model.setRowCount(0); // Limpiar
        
        UsuarioDAO dao = new UsuarioDAO();
        List<Usuario> lista = dao.listar(); // Trae todos (activos)
        
        String filtroMin = filtro.toLowerCase();

        for (Usuario u : lista) {
            // Filtro en memoria (Java) porque DAO.listar trae todo
            boolean coincideUsuario = u.getUsuario().toLowerCase().contains(filtroMin);
            boolean coincideNombre = u.getNombreEmpleado().toLowerCase().contains(filtroMin);
            
            if (filtro.isEmpty() || coincideUsuario || coincideNombre) {
                model.addRow(new Object[]{
                    u.getId(),
                    u.getUsuario(),
                    u.getRolNombre(), // "Administrador" o "Empleado"
                    u.getNombreEmpleado(),
                    u.getEstado()
                });
            }
        }
    }

    private void seleccionarUsuario() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la lista.");
            return;
        }

        // Obtener datos de la fila
        String id = table.getValueAt(row, 0).toString();
        String usuario = table.getValueAt(row, 1).toString();
        
        // Formato de retorno: "ID - USUARIO"
        String resultado = id + " - " + usuario;
        
        if (onSelect != null) {
            onSelect.accept(resultado);
        }
        
        dispose();
    }
}