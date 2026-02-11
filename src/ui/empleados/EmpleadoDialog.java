package ui.empleados;

import dao.EmpleadoDAO;
import model.Empleado;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class EmpleadoDialog extends JDialog {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtFiltro;
    private Consumer<String> onSelect;
    private String filtroRol; 

    // Constructor Principal: Este es el que deben usar Citas (con "M"), Usuarios (con "R") y Empleados (con null)
    public EmpleadoDialog(JFrame parent, Consumer<String> onSelect, String filtroRol) {
        super(parent, "Buscar Empleado", true);
        this.onSelect = onSelect;
        this.filtroRol = filtroRol;

        // 1. Configuración de la ventana
        setSize(750, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // 2. Inicializar componentes de UI (Panel Superior)
        JPanel top = new JPanel(new BorderLayout(5, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        txtFiltro = new JTextField();
        JButton btnBuscar = UITheme.primaryButton("Buscar");
        
        txtFiltro.addActionListener(e -> cargarTabla(txtFiltro.getText()));
        btnBuscar.addActionListener(e -> cargarTabla(txtFiltro.getText()));

        top.add(new JLabel("Filtrar por Cédula o Nombre: "), BorderLayout.WEST);
        top.add(txtFiltro, BorderLayout.CENTER);
        top.add(btnBuscar, BorderLayout.EAST);

        // 3. Inicializar el MODELO antes de cargar la tabla para evitar el NullPointerException
        String[] cols = {"ID", "Cédula", "Nombres", "Apellidos", "Rol"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) seleccionarEmpleado();
            }
        });

        // 4. Panel Inferior
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSeleccionar = UITheme.primaryButton("Seleccionar");
        JButton btnCancelar = UITheme.primaryButton("Cancelar");

        btnSeleccionar.addActionListener(e -> seleccionarEmpleado());
        btnCancelar.addActionListener(e -> dispose());

        bottom.add(btnCancelar);
        bottom.add(btnSeleccionar);

        // 5. Agregar al Dialog
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // 6. AHORA SÍ, cargar los datos (el modelo ya no es null)
        cargarTabla(""); 
    }

    // Constructor por compatibilidad (si lo necesitas para otros lados)
    public EmpleadoDialog(JFrame parent, Consumer<String> onSelect) {
        this(parent, onSelect, null);
    }

    private void cargarTabla(String filtro) {
        // Al estar el modelo inicializado arriba, ya no dará error aquí
        model.setRowCount(0);
        EmpleadoDAO dao = new EmpleadoDAO();
        
        // Usamos el método del DAO que adapta la búsqueda al rol recibido
        List<Empleado> lista = dao.buscarPorFiltro(filtro, filtroRol);

        for (Empleado e : lista) {
            String rolStr = e.getRol().equals("M") ? "Mecánico" : "Recepción";
            model.addRow(new Object[]{
                e.getId(), e.getCedula(), e.getNombre(), e.getApellido(), rolStr
            });
        }
    }

    private void seleccionarEmpleado() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un empleado.");
            return;
        }

        String id = table.getValueAt(row, 0).toString();
        String cedula = table.getValueAt(row, 1).toString();
        String nombre = table.getValueAt(row, 2).toString();
        String apellido = table.getValueAt(row, 3).toString();

        String resultado = id + " - " + nombre + " " + apellido + " - " + cedula;
        
        if (onSelect != null) onSelect.accept(resultado);
        dispose();
    }
}