package ui.clientes;

import dao.ClienteDAO;
import model.Cliente;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class ClienteDialog extends JDialog {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtFiltro;
    private Consumer<String> onSelect;

    public ClienteDialog(JFrame parent, Consumer<String> onSelect) {
        super(parent, "Buscar Cliente", true);
        this.onSelect = onSelect;
        
        setSize(750, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Panel Superior
        JPanel top = new JPanel(new BorderLayout(5, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        txtFiltro = new JTextField();
        JButton btnBuscar = UITheme.primaryButton("Buscar");
        
        txtFiltro.addActionListener(e -> cargarTabla(txtFiltro.getText()));
        btnBuscar.addActionListener(e -> cargarTabla(txtFiltro.getText()));

        top.add(new JLabel("Filtrar por Cédula o Nombre: "), BorderLayout.WEST);
        top.add(txtFiltro, BorderLayout.CENTER);
        top.add(btnBuscar, BorderLayout.EAST);

        // Tabla
        String[] cols = {"ID", "Cédula", "Nombres", "Apellidos", "Teléfono", "Correo"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Doble clic
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) seleccionarCliente();
            }
        });

        // Panel Inferior
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSeleccionar = UITheme.primaryButton("Seleccionar");
        JButton btnCancelar = UITheme.primaryButton("Cancelar");

        btnSeleccionar.addActionListener(e -> seleccionarCliente());
        btnCancelar.addActionListener(e -> dispose());

        bottom.add(btnCancelar);
        bottom.add(btnSeleccionar);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        cargarTabla(""); 
    }

    private void cargarTabla(String filtro) {
        model.setRowCount(0);
        ClienteDAO dao = new ClienteDAO();
        List<Cliente> lista = (filtro == null || filtro.isEmpty()) ? 
                              dao.listarActivos() : dao.buscarPorFiltro(filtro);

        for (Cliente c : lista) {
            model.addRow(new Object[]{
                c.getId(), c.getCedula(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getCorreo()
            });
        }
    }

    private void seleccionarCliente() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente.");
            return;
        }

        // CAMBIO IMPORTANTE: Incluimos el ID en el texto de retorno
        String id = table.getValueAt(row, 0).toString();
        String cedula = table.getValueAt(row, 1).toString();
        String nombre = table.getValueAt(row, 2).toString();
        String apellido = table.getValueAt(row, 3).toString();

        // Formato: "ID - NOMBRE APELLIDO - CEDULA"
        // Ej: "15 - Juan Perez - 0102030405"
        String resultado = id + " - " + nombre + " " + apellido + " - " + cedula;
        
        if (onSelect != null) onSelect.accept(resultado);
        dispose();
    }
}