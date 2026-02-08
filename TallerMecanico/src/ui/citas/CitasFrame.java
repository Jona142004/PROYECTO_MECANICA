package ui.citas;

import ui.components.SelectorPanel;
import ui.components.UIMode;
import ui.empleados.EmpleadoDialog;
import ui.theme.UITheme;
import ui.vehiculos.VehiculosDialog;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CitasFrame extends JFrame {

    private UIMode mode;

    // clave
    private JTextField idCita;

    // campos
    private SelectorPanel vehiculo;
    private SelectorPanel mecanico;
    private JDateChooser fecha;

    // hora (opción 1)
    private JComboBox<String> horaCB;
    private JComboBox<String> minutoCB;
    private JPanel horaPanel;

    
    private JComboBox<String> estado;

    // tabla
    private JTable table;
    private DefaultTableModel model;

    // botones
    private JButton btnBuscar;   // EDIT/DELETE
    private JButton btnGuardar;  // ADD/EDIT
    private JButton btnEliminar; // DELETE

    public CitasFrame(UIMode mode) {
        this.mode = mode;

        setTitle("Citas - Autos y Motores");
        setSize(1180, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Gestión de Citas", "Agenda, asignación de mecánico y cancelación (solo UI)"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
    }

    public CitasFrame() {
        this(UIMode.ADD);
    }

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(UITheme.BG);

        JPanel form = UITheme.cardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        idCita = new JTextField();
        fecha = new JDateChooser();

    // ✅ Hora con combos (más grande y sin "...")
    horaCB = new JComboBox<>();
    for (int h = 0; h < 24; h++) {
        horaCB.addItem(String.format("%02d", h));
    }

    minutoCB = new JComboBox<>(new String[]{
            "00","05","10","15","20","25","30","35","40","45","50","55"
    });

    // ✅ obliga al combo a medir bien (evita que se vea "...")
    horaCB.setPrototypeDisplayValue("00");
    minutoCB.setPrototypeDisplayValue("00");

    // ✅ tamaño mínimo cómodo
    horaCB.setPreferredSize(new Dimension(60, 20));
    minutoCB.setPreferredSize(new Dimension(60, 20));

    // (opcional) fuente un poquito más grande
    Font fHora = new Font("SansSerif", Font.PLAIN, 12);
    horaCB.setFont(fHora);
    minutoCB.setFont(fHora);

    horaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
    horaPanel.setOpaque(false);

    JLabel dosPuntos = new JLabel(":");
    dosPuntos.setFont(new Font("SansSerif", Font.BOLD, 12));

    horaPanel.add(horaCB);
    horaPanel.add(dosPuntos);
    horaPanel.add(minutoCB);

    // ✅ valor inicial visible
    horaCB.setSelectedIndex(0);
    minutoCB.setSelectedIndex(0);

        estado = new JComboBox<>(new String[]{"ACTIVA", "CANCELADA"});
        vehiculo = new SelectorPanel("(Seleccionar vehículo)");
        mecanico = new SelectorPanel("(Seleccionar mecánico)");

        vehiculo.setOnSearch(() -> {
            VehiculosDialog dialog = new VehiculosDialog(this, vehiculo::setText);
            dialog.setVisible(true);
        });

        mecanico.setOnSearch(() -> {
            EmpleadoDialog dialog = new EmpleadoDialog(this, mecanico::setText);
            dialog.setVisible(true);
        });

        int r = 0;
        addField(form, g, r++, "ID Cita", idCita);
        addField(form, g, r++, "Vehículo", vehiculo);
        addField(form, g, r++, "Mecánico", mecanico);
        addField(form, g, r++, "Fecha", fecha);
        addField(form, g, r++, "Hora", horaPanel);
        addField(form, g, r++, "Estado", estado);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        btnBuscar = UITheme.primaryButton("Buscar");
        btnGuardar = UITheme.primaryButton("Guardar");
        btnEliminar = UITheme.primaryButton("Eliminar");

        actions.add(btnBuscar);
        actions.add(btnGuardar);
        actions.add(btnEliminar);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        form.add(actions, g);

        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"ID", "Vehículo", "Mecánico", "Fecha", "Hora", "Estado"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setRowHeight(26);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.42);
        split.setBorder(null);

        root.add(split, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> onBuscar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnEliminar.addActionListener(e -> onEliminar());

        return root;
    }

    private void applyMode(UIMode m) {
        this.mode = m;

        btnBuscar.setVisible(m == UIMode.EDIT || m == UIMode.DELETE);
        btnGuardar.setVisible(m == UIMode.ADD || m == UIMode.EDIT);
        btnEliminar.setVisible(m == UIMode.DELETE);

        if (m == UIMode.ADD) {
            btnGuardar.setText("Agendar");
            setKeyState(true, true);
            setFieldsEnabled(true);
        }

        if (m == UIMode.EDIT) {
            btnGuardar.setText("Guardar");
            setKeyState(true, true);     // solo ID editable
            setFieldsEnabled(false);     // bloqueado hasta buscar
        }

        if (m == UIMode.DELETE) {
            setKeyState(true, true);     // solo ID editable
            setFieldsEnabled(false);     // bloqueado hasta buscar
        }

        revalidate();
        repaint();
    }

    private void setKeyState(boolean enabled, boolean editable) {
        idCita.setEnabled(enabled);
        idCita.setEditable(editable);
    }

    private void setFieldsEnabled(boolean enabled) {
        vehiculo.setEnabled(enabled);
        mecanico.setEnabled(enabled);
        fecha.setEnabled(enabled);

        // ✅ habilitar combos de hora
        horaCB.setEnabled(enabled);
        minutoCB.setEnabled(enabled);

        estado.setEnabled(enabled);
    }

    private void onBuscar() {
        if (mode != UIMode.EDIT && mode != UIMode.DELETE) return;

        // Mock: cargar datos por ID
        vehiculo.setText("ABC-123 - Juan Pérez");
        mecanico.setText("Pedro Mecánico - 0102030405");
        estado.setSelectedItem("ACTIVA");

        // Bloquear clave
        setKeyState(true, false);

        // Mostrar campos
        setFieldsEnabled(true);

        if (mode == UIMode.DELETE) {
            // solo lectura
            setFieldsEnabled(false);
        }

        JOptionPane.showMessageDialog(this,
                "Cita cargada. Ya puedes " + (mode == UIMode.EDIT ? "editar." : "eliminar/cancelar."));
    }

    private void onGuardar() {
        if (mode == UIMode.ADD) {
            JOptionPane.showMessageDialog(this, "Cita agendada - Solo UI");
        } else if (mode == UIMode.EDIT) {
            JOptionPane.showMessageDialog(this, "Cita actualizada - Solo UI");
        }
    }

    private void onEliminar() {
        if (mode == UIMode.DELETE) {
            JOptionPane.showMessageDialog(this, "Cita eliminada/cancelada - Solo UI");
        }
    }

    private JPanel header(String title, String subtitle) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        top.setPreferredSize(new Dimension(0, 64));

        JLabel t = new JLabel("  " + title);
        t.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel s = new JLabel("  " + subtitle);
        s.setForeground(UITheme.MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(t);
        text.add(s);

        top.add(text, BorderLayout.WEST);
        return top;
    }

    private void addField(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; g.weightx = 0.35;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 0.65;
        p.add(field, g);
    }
}
