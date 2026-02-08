package ui.vehiculos;

import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Consumer;

public class VehiculosDialog extends JDialog {

    public VehiculosDialog(JFrame parent, Consumer<String> onSelect) {
        super(parent, "Buscar Vehiculo", true);
        setSize(720, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JTextField filtro = new JTextField();
        JButton buscar = UITheme.primaryButton("Buscar");

        JPanel top = new JPanel(new BorderLayout(6, 0));
        top.add(new JLabel("Buscar:"), BorderLayout.WEST);
        top.add(filtro, BorderLayout.CENTER);
        top.add(buscar, BorderLayout.EAST);

        String[] cols = {"Codigo", "Placa", "Cliente", "Marca", "Modelo"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        // 👉 Datos mock (solo UI)
        model.addRow(new Object[]{"1", "0102030405", "Juan Pérez", "0999999999"});
        model.addRow(new Object[]{"2", "0101112223", "María López", "0988888888"});

        JTable table = new JTable(model);
        table.setRowHeight(26);

        // Doble clic (se mantiene igual)
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();

                    String nombre = model.getValueAt(row, 2).toString();
                    String cedula = model.getValueAt(row, 1).toString();

                    onSelect.accept(nombre + " - " + cedula);
                    dispose();
                }
            }
        });

        // 🔹 BOTÓN ACEPTAR (abajo a la izquierda)
        JButton aceptar = UITheme.primaryButton("Aceptar");

        aceptar.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Seleccione un Vehiculo",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String nombre = model.getValueAt(row, 2).toString();
            String cedula = model.getValueAt(row, 1).toString();

            onSelect.accept(nombre + " - " + cedula);
            dispose();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(aceptar);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}