package ui.facturacion;

import dao.FacturaDAO;
import model.Factura;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class FacturasListDialog extends JDialog {

    private Consumer<Integer> onSelect;
    private DefaultTableModel model;
    private JTable table;

    public FacturasListDialog(Window owner, Consumer<Integer> onSelect) {
        super(owner, "Buscar Factura", ModalityType.APPLICATION_MODAL);
        this.onSelect = onSelect;
        setSize(700, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Buscador simple
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Color.WHITE);
        top.add(new JLabel("Facturas Recientes:"));
        add(top, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Nro. Factura", "Cliente", "Total ($)", "Fecha"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) seleccionar();
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        
        cargarTabla();
    }

    private void cargarTabla() {
        model.setRowCount(0);
        List<Factura> lista = new FacturaDAO().listarActivas(); // O listar() todas si prefieres
        for(Factura f : lista) {
            model.addRow(new Object[]{
                f.getId(), f.getNumero(), f.getAuxNombreCliente(), f.getTotal(), f.getFecha()
            });
        }
    }

    private void seleccionar() {
        int row = table.getSelectedRow();
        if(row != -1) {
            int id = Integer.parseInt(table.getValueAt(row, 0).toString());
            onSelect.accept(id);
            dispose();
        }
    }
}