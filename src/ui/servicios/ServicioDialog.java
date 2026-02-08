package ui.servicios;

import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Consumer;

public class ServicioDialog extends JDialog {

    public ServicioDialog(JFrame parent, Consumer<ServicioItem> onSelect) {
        super(parent, "Buscar Servicio", true);
        setSize(720, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JTextField filtro = new JTextField();
        JButton buscar = UITheme.primaryButton("Buscar");

        JPanel top = new JPanel(new BorderLayout(6, 0));
        top.add(new JLabel("Buscar (código / nombre):"), BorderLayout.WEST);
        top.add(filtro, BorderLayout.CENTER);
        top.add(buscar, BorderLayout.EAST);

        String[] cols = {"Código", "Servicio", "Precio", "Grava IVA", "Estado"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        // Datos mock (solo UI)
        model.addRow(new Object[]{"SRV-001", "Cambio de aceite", "25.00", "SÍ", "ACTIVO"});
        model.addRow(new Object[]{"SRV-002", "Alineación y balanceo", "30.00", "SÍ", "ACTIVO"});
        model.addRow(new Object[]{"SRV-003", "Lavado motor", "15.00", "NO", "ACTIVO"});

        JTable table = new JTable(model);
        table.setRowHeight(26);

        // Doble clic
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    seleccionarActual(table, model, onSelect);
                }
            }
        });

        JButton aceptar = UITheme.primaryButton("Aceptar");
        aceptar.addActionListener(e -> seleccionarActual(table, model, onSelect));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(aceptar);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // (UI mock) botón buscar no filtra realmente, solo muestra mensaje
        buscar.addActionListener(e -> {
            // Aquí luego filtras en BD; por ahora solo UI
            // JOptionPane.showMessageDialog(this, "Filtro: " + filtro.getText());
        });
    }

    private void seleccionarActual(JTable table, DefaultTableModel model, Consumer<ServicioItem> onSelect) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un servicio", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String codigo = model.getValueAt(row, 0).toString();
        String nombre = model.getValueAt(row, 1).toString();
        double precio = Double.parseDouble(model.getValueAt(row, 2).toString());
        boolean grava = "SÍ".equalsIgnoreCase(model.getValueAt(row, 3).toString());

        onSelect.accept(new ServicioItem(codigo, nombre, precio, grava));
        dispose();
    }


}
