package ui.citas;

import dao.CitaDAO;
import model.Cita;
import ui.components.SelectorPanel;
import ui.components.UIMode;
import ui.empleados.EmpleadoDialog;
import ui.theme.UITheme;
import ui.vehiculos.VehiculosDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class CitasFrame extends JFrame {

    private UIMode mode;

    // Campos
    private int idCitaSeleccionada = -1; // Control interno del ID

    private SelectorPanel vehiculo;
    private SelectorPanel mecanico;
    private JDateChooser fecha;

    // Hora
    private JComboBox<String> horaCB;
    private JComboBox<String> minutoCB;
    private JPanel horaPanel;

    private JComboBox<String> estado;

    // Tabla
    private JTable table;
    private DefaultTableModel model;

    // Botones
    private JButton btnGuardar; // ADD
    private JButton btnCancelarCita; // DELETE logic

    // Variables para guardar IDs seleccionados de los diálogos
    private int idVehiculoSel = -1;
    private int idMecanicoSel = -1;

    public CitasFrame(UIMode mode) {
        this.mode = mode;
        setTitle("Gestión de Citas");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Agenda de Citas", "Programación de servicios y mantenimiento"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
        cargarTabla();
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

        // --- COMPONENTES ---

        // 1. Vehículo
        vehiculo = new SelectorPanel("(Seleccionar vehículo)");
        vehiculo.setOnSearch(() -> {
            VehiculosDialog dialog = new VehiculosDialog(this, (res) -> {
                vehiculo.setText(res); // Ej: "ABC-123 - Kia Rio"
                try {

                    String[] partes = res.split(" - ");
                    if (partes.length > 0 && partes[0].matches("\\d+")) {
                        idVehiculoSel = Integer.parseInt(partes[0]);
                    }
                } catch (Exception e) {
                }

                // REQUEST FOCUS: Al volver, saltar al siguiente
                mecanico.requestFocusInWindow();
            });
            dialog.setVisible(true);
        });

        // EN CitasFrame.java (dentro de content)
        // 2. Mecánico
        mecanico = new SelectorPanel("(Seleccionar mecánico)");
        mecanico.setOnSearch(() -> {
            // Pasamos "M" para filtrar solo mecánicos
            EmpleadoDialog dialog = new EmpleadoDialog(this, (res) -> {
                mecanico.setText(res);
                String[] partes = res.split(" - ");
                if (partes.length > 0)
                    idMecanicoSel = Integer.parseInt(partes[0]);
                // ... resto de tu lógica de parseo ID ...
            }, "M");
            dialog.setVisible(true);
        });

        // 3. Fecha y Hora
        fecha = new JDateChooser();
        fecha.setDate(new Date()); // Hoy por defecto

        horaCB = new JComboBox<>();
        for (int h = 8; h < 19; h++)
            horaCB.addItem(String.format("%02d", h)); // De 8am a 6pm

        minutoCB = new JComboBox<>(
                new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" });

        horaCB.setPrototypeDisplayValue("00");
        minutoCB.setPrototypeDisplayValue("00");

        // 2. Tamaño fijo más grande (Ancho: 70px, Alto: 30px)
        Dimension dimCombo = new Dimension(70, 30);
        horaCB.setPreferredSize(dimCombo);
        minutoCB.setPreferredSize(dimCombo);

        horaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        horaPanel.setOpaque(false);
        horaPanel.add(horaCB);
        horaPanel.add(new JLabel(" : "));
        horaPanel.add(minutoCB);

        estado = new JComboBox<>(new String[] { "ACTIVA", "CANCELADA" });
        estado.setEnabled(false); // Automático

        // Construcción Formulario
        int r = 0;
        // NO AGREGAMOS EL CAMPO ID (Es secuencia)
        addField(form, g, r++, "Vehículo", vehiculo);
        addField(form, g, r++, "Mecánico", mecanico);
        addField(form, g, r++, "Fecha", fecha);
        addField(form, g, r++, "Hora", horaPanel);
        addField(form, g, r++, "Estado", estado);

        // Botones
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        btnGuardar = UITheme.primaryButton("Agendar Cita");
        btnCancelarCita = UITheme.primaryButton("CANCELAR CITA");
        btnCancelarCita.setBackground(new Color(220, 38, 38));
        btnCancelarCita.setForeground(Color.WHITE);

        actions.add(btnGuardar);
        actions.add(btnCancelarCita);

        g.gridx = 0;
        g.gridy = r;
        g.gridwidth = 2;
        form.add(actions, g);

        // Tabla
        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = { "ID", "Fecha", "Hora", "Placa", "Cliente", "Mecánico", "Estado" };

        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(26);

        // Listener Selección
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                onTableSelect();
            }
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.4);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        // Listeners
        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelarCita.addActionListener(e -> onCancelar());

        return root;
    }

    // --- LÓGICA ---

    private void cargarTabla() {
        model.setRowCount(0);
        CitaDAO dao = new CitaDAO();
        List<Cita> lista = dao.listar();
        for (Cita c : lista) {
            // Formatear hora visualmente
            String horaStr = String.format("%tT", c.getHora());
            model.addRow(new Object[] {
                    c.getId(),
                    c.getFecha(),
                    horaStr,
                    c.getPlaca(),
                    c.getCliente(),
                    c.getNombreMecanico(),
                    c.getEstado(),
                    // Columnas ocultas para IDs (truco para recuperarlos al clickear)
                    c.getIdVehiculo(), // 7
                    c.getIdMecanico() // 8
            });
        }
    }

    private void onTableSelect() {
        int row = table.getSelectedRow();
        if (row == -1)
            return;

        // Recuperar datos visuales
        idCitaSeleccionada = Integer.parseInt(table.getValueAt(row, 0).toString());
        Date date = (Date) table.getValueAt(row, 1); // java.sql.Date
        fecha.setDate(date);

        // Parsear hora para los combos
        String horaStr = table.getValueAt(row, 2).toString(); // HH:mm:ss
        try {
            String[] parts = horaStr.split(":");
            horaCB.setSelectedItem(parts[0]);
            // Ajustar minutos al más cercano (00, 15, 30, 45)
            int min = Integer.parseInt(parts[1]);
            if (min < 15)
                minutoCB.setSelectedIndex(0);
            else if (min < 30)
                minutoCB.setSelectedIndex(1);
            else if (min < 45)
                minutoCB.setSelectedIndex(2);
            else
                minutoCB.setSelectedIndex(3);
        } catch (Exception e) {
        }

        vehiculo.setText(table.getValueAt(row, 3).toString()); // Muestra placa
        mecanico.setText(table.getValueAt(row, 5).toString()); // Muestra nombre mec
        estado.setSelectedItem(table.getValueAt(row, 6).toString());

        // IMPORTANTE: Recuperar IDs internos (que no mostramos en columnas visibles
        // pero existen en el modelo si los agregamos al DAO list)
        // Para simplificar, aquí asumimos que el usuario debe volver a seleccionar si
        // edita,
        // O mejor: usar columnas ocultas en el modelo.
        // Por ahora, habilitar cancelación.

        if (mode == UIMode.DELETE) {
            btnCancelarCita.setEnabled(true);
        }
    }

    private void onGuardar() {
        if (idVehiculoSel == -1 || idMecanicoSel == -1 || fecha.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.");
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha.getDate());
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt((String) horaCB.getSelectedItem()));
        cal.set(Calendar.MINUTE, Integer.parseInt((String) minutoCB.getSelectedItem()));
        cal.set(Calendar.SECOND, 0);

        if (cal.getTime().before(new Date())) {
            JOptionPane.showMessageDialog(this, "No puede agendar en el pasado.");
            return;
        }

        Cita c = new Cita();
        c.setIdVehiculo(idVehiculoSel);
        c.setIdMecanico(idMecanicoSel);
        c.setFecha(new java.sql.Date(fecha.getDate().getTime()));
        c.setHora(new java.sql.Timestamp(cal.getTimeInMillis()));

        if (new CitaDAO().agendar(c)) {
            JOptionPane.showMessageDialog(this, "Cita agendada.");
            cargarTabla();
            limpiar();
        } else {
            JOptionPane.showMessageDialog(this, "Mecánico ocupado en ese horario.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancelar() {
        if (idCitaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una cita.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Cancelar esta cita?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (new CitaDAO().cancelar(idCitaSeleccionada)) {
                JOptionPane.showMessageDialog(this, "Cita cancelada.");
                cargarTabla();
                limpiar();
            }
        }
    }

    private void limpiar() {
        vehiculo.setText("(Seleccionar vehículo)");
        mecanico.setText("(Seleccionar mecánico)");
        fecha.setDate(new Date());
        horaCB.setSelectedIndex(0);
        minutoCB.setSelectedIndex(0);
        estado.setSelectedIndex(0);
        idCitaSeleccionada = -1;
        idVehiculoSel = -1;
        idMecanicoSel = -1;
    }

    private void applyMode(UIMode m) {
        this.mode = m;
        btnGuardar.setVisible(m == UIMode.ADD);
        btnCancelarCita.setVisible(m == UIMode.DELETE);

        boolean editable = (m == UIMode.ADD);
        vehiculo.setEnabled(editable);
        mecanico.setEnabled(editable);
        fecha.setEnabled(editable);
        horaCB.setEnabled(editable);
        minutoCB.setEnabled(editable);
        table.setEnabled(m == UIMode.DELETE); // Solo se selecciona tabla para borrar/ver
    }

    // Header y UI Helpers
    private JPanel header(String t, String s) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        top.setPreferredSize(new Dimension(0, 64));
        JLabel title = new JLabel("  " + t);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel sub = new JLabel("  " + s);
        sub.setForeground(UITheme.MUTED);
        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(title);
        txt.add(sub);
        top.add(txt, BorderLayout.WEST);
        return top;
    }

    private void addField(JPanel p, GridBagConstraints g, int r, String l, JComponent f) {
        g.gridx = 0;
        g.gridy = r;
        p.add(new JLabel(l), g);
        g.gridx = 1;
        p.add(f, g);
    }
}