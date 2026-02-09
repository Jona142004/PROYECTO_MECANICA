package ui.servicios;

import dao.ServicioDAO;
import model.Servicio;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class ServicioDialog extends JDialog {

    private Consumer<Servicio> onSelect; 

    public ServicioDialog(JFrame parent, Consumer<Servicio> onSelect) {
        super(parent, "Buscar Servicio", true);
        this.onSelect = onSelect;
        setSize(700, 400);
        setLocationRelativeTo(parent);
        
        // Panel superior
        JTextField filtro = new JTextField();
        JButton btnBuscar = UITheme.primaryButton("Buscar");
        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(new JLabel("Nombre:"), BorderLayout.WEST);
        top.add(filtro, BorderLayout.CENTER);
        top.add(btnBuscar, BorderLayout.EAST);

        // Tabla
        String[] cols = {"ID", "Nombre", "Precio", "IVA"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        
        // Eventos
        btnBuscar.addActionListener(e -> cargarTabla(model, filtro.getText()));
        filtro.addActionListener(e -> cargarTabla(model, filtro.getText()));
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) seleccionar(table);
            }
        });

        // Cargar inicial
        cargarTabla(model, "");

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void cargarTabla(DefaultTableModel model, String busqueda) {
        model.setRowCount(0);
        ServicioDAO dao = new ServicioDAO();
        // Si tienes buscarPorNombre usa ese, si no, listar() y filtra
        List<Servicio> lista = dao.listar(); 
        
        for(Servicio s : lista) {
            if(busqueda.isEmpty() || s.getNombre().toUpperCase().contains(busqueda.toUpperCase())) {
                if(s.getEstado().equals("A")) { // Solo activos
                    model.addRow(new Object[]{s.getId(), s.getNombre(), s.getPrecio(), s.getIva()});
                }
            }
        }
    }

    private void seleccionar(JTable table) {
        int row = table.getSelectedRow();
        if(row == -1) return;
        
        Servicio s = new Servicio();
        s.setId(Integer.parseInt(table.getValueAt(row, 0).toString()));
        s.setNombre(table.getValueAt(row, 1).toString());
        s.setPrecio(Double.parseDouble(table.getValueAt(row, 2).toString()));
        
        String ivaVal = table.getValueAt(row, 3).toString();
        s.setIva(ivaVal.equals("S") || ivaVal.equals("SÍ") ? "S" : "N");
        
        onSelect.accept(s);
        dispose();
    }
}