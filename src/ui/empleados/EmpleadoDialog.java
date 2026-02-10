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

    private Consumer<String> onSelect;
    private DefaultTableModel model;
    private JTable table;
    private boolean soloMecanicos; // Filtro

    // Constructor Principal (con opción de filtrar)
    public EmpleadoDialog(Window owner, Consumer<String> onSelect, boolean soloMecanicos) {
        super(owner, "Seleccionar Empleado", ModalityType.APPLICATION_MODAL);
        this.onSelect = onSelect;
        this.soloMecanicos = soloMecanicos;
        
        setSize(700, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        add(header(), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);
        
        cargarTabla();
    }

    // Constructor por defecto (trae TODOS, para uso general)
    public EmpleadoDialog(Window owner, Consumer<String> onSelect) {
        this(owner, onSelect, false);
    }

    private JPanel header() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel l = new JLabel(soloMecanicos ? "Seleccionar Mecánico" : "Seleccionar Empleado");
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        p.add(l);
        return p;
    }

    private JPanel content() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UITheme.BG);

        String[] cols = {"ID", "Cédula", "Nombre", "Rol"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(model);
        table.setRowHeight(24);
        
        // Doble clic para seleccionar
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) seleccionar();
            }
        });

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton btn = UITheme.primaryButton("Seleccionar");
        btn.addActionListener(e -> seleccionar());
        
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setOpaque(false);
        bot.add(btn);
        p.add(bot, BorderLayout.SOUTH);

        return p;
    }

    private void cargarTabla() {
        model.setRowCount(0);
        EmpleadoDAO dao = new EmpleadoDAO();
        List<Empleado> lista;
        
        // AQUÍ ESTÁ LA MAGIA: Elegimos qué lista cargar
        if (soloMecanicos) {
            lista = dao.listarMecanicos(); // Solo 'M' y Activos
        } else {
            lista = dao.listar(); // Todos los Activos
        }

        for (Empleado e : lista) {
            String rol = e.getRol().equals("M") ? "Mecánico" : "Recepción";
            model.addRow(new Object[]{
                e.getId(),
                e.getCedula(),
                e.getNombre() + " " + e.getApellido(),
                rol
            });
        }
    }

    private void seleccionar() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un empleado de la lista.");
            return;
        }
        
        // Formato de retorno: "ID - Nombre"
        String id = table.getValueAt(row, 0).toString();
        String nombre = table.getValueAt(row, 2).toString();
        
        onSelect.accept(id + " - " + nombre);
        dispose();
    }
}