package ui.vehiculos;

import dao.VehiculoDAO;
import model.Vehiculo;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class VehiculosDialog extends JDialog {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtFiltro;
    private Consumer<String> onSelect;

    public VehiculosDialog(JFrame parent, Consumer<String> onSelect) {
        super(parent, "Buscar Vehículo", true);
        this.onSelect = onSelect;
        setSize(750, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Panel Superior
        JPanel top = new JPanel(new BorderLayout(5, 0));
        txtFiltro = new JTextField();
        JButton btnBuscar = UITheme.primaryButton("Buscar");
        
        btnBuscar.addActionListener(e -> cargarTabla(txtFiltro.getText()));
        txtFiltro.addActionListener(e -> cargarTabla(txtFiltro.getText()));

        top.add(new JLabel("Placa o Cliente: "), BorderLayout.WEST);
        top.add(txtFiltro, BorderLayout.CENTER);
        top.add(btnBuscar, BorderLayout.EAST);

        // Tabla
        String[] cols = {"ID", "Placa", "Marca/Modelo", "Cliente"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) seleccionar();
            }
        });

        // Botones
        JButton btnOk = UITheme.primaryButton("Seleccionar");
        btnOk.addActionListener(e -> seleccionar());
        
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnOk, BorderLayout.SOUTH);

        cargarTabla("");
    }

    private void cargarTabla(String filtro) {
        model.setRowCount(0);
        VehiculoDAO dao = new VehiculoDAO();
        List<Vehiculo> lista = (filtro.isEmpty()) ? dao.listar() : dao.buscarPorFiltro(filtro);
        
        for(Vehiculo v : lista) {
            model.addRow(new Object[]{
                v.getId(), v.getPlaca(), v.getNombreMarca() + " " + v.getNombreModelo(), v.getNombreCliente()
            });
        }
    }

    private void seleccionar() {
        int row = table.getSelectedRow();
        if(row == -1) return;
        
        String id = table.getValueAt(row, 0).toString();
        String placa = table.getValueAt(row, 1).toString();
        String cliente = table.getValueAt(row, 3).toString();
        
        // Retornamos formato: "ID - PLACA - CLIENTE"
        if(onSelect != null) onSelect.accept(id + " - " + placa + " - " + cliente);
        dispose();
    }
}