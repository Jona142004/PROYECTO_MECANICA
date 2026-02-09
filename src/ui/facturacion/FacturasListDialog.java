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

    public FacturasListDialog(JFrame parent, Consumer<Integer> onSelect) {
        super(parent, "Buscar Factura", true);
        setSize(850, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // 1. TÍTULO
        JLabel lblTitulo = new JLabel("  Seleccione una factura (Doble clic)");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // 2. TABLA
        String[] cols = {"ID", "Número", "Fecha", "Total ($)"};
        
        // Tabla no editable
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 3. CARGAR DATOS DESDE LA BD
        cargarDatos(model);

        // 4. EVENTO DOBLE CLIC (SELECCIONAR)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    seleccionarFactura(table, onSelect);
                }
            }
        });

        // 5. BOTÓN CANCELAR
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = UITheme.primaryButton("Cancelar");
        JButton btnSeleccionar = UITheme.primaryButton("Seleccionar");
        
        btnCancelar.addActionListener(e -> dispose());
        btnSeleccionar.addActionListener(e -> seleccionarFactura(table, onSelect));
        
        panelBotones.add(btnCancelar);
        panelBotones.add(btnSeleccionar);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarDatos(DefaultTableModel model) {
        FacturaDAO dao = new FacturaDAO();
        List<Factura> lista = dao.listar();
        
        for (Factura f : lista) {
            model.addRow(new Object[]{
                f.getId(),
                f.getNumero(),
                f.getFecha(), // Muestra la fecha (yyyy-mm-dd)
                String.format("%.2f", f.getTotal())
            });
        }
    }

    private void seleccionarFactura(JTable table, Consumer<Integer> onSelect) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila primero.");
            return;
        }
        
        // Obtenemos el ID de la columna 0
        int idFactura = Integer.parseInt(table.getValueAt(row, 0).toString());
        
        // Devolvemos el ID a la ventana principal
        onSelect.accept(idFactura);
        dispose();
    }
}