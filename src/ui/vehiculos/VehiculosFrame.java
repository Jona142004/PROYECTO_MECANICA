package ui.vehiculos;

import dao.VehiculoDAO;
import dao.VehiculoDAO.OpcionCombo;
import model.Vehiculo;
import ui.clientes.ClienteDialog;
import ui.components.SelectorPanel;
import ui.components.UIMode;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VehiculosFrame extends JFrame {

    private UIMode mode;

    // Campos
    private JTextField placa;
    private SelectorPanel cliente;
    private JComboBox<OpcionCombo> marca;
    private JComboBox<OpcionCombo> modelo;

    // Tabla
    private JTable table;
    private DefaultTableModel model;

    // Botones
    private JButton btnBuscar, btnGuardar, btnEliminar;
    
    // IDs para control
    private int idClienteSeleccionado = -1;
    private int idVehiculoSeleccionado = -1; // NUEVO: Para saber cuál editar/borrar

    public VehiculosFrame(UIMode mode) {
        this.mode = mode;
        setTitle("Vehículos - Autos y Motores");
        setSize(1120, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Gestión de Vehículos", "Registro por cliente, marca y modelo"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
        
        cargarCombos();
        cargarTabla();
    }

    public VehiculosFrame() {
        this(UIMode.ADD);
    }

    // --- CARGA DE DATOS ---

    private void cargarCombos() {
        marca.removeAllItems();
        modelo.removeAllItems();
        VehiculoDAO dao = new VehiculoDAO();
        for (OpcionCombo item : dao.obtenerMarcas()) marca.addItem(item);
        for (OpcionCombo item : dao.obtenerModelos()) modelo.addItem(item);
    }

    private void cargarTabla() {
        model.setRowCount(0);
        VehiculoDAO dao = new VehiculoDAO();
        List<Vehiculo> lista = dao.listar();
        for (Vehiculo v : lista) {
            model.addRow(new Object[]{
                v.getId(),
                v.getPlaca(),
                v.getNombreCliente(),
                v.getNombreMarca(),
                v.getNombreModelo()
            });
        }
    }

    // --- UI ---

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(UITheme.BG);

        JPanel form = UITheme.cardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        placa = new JTextField();
        cliente = new SelectorPanel("(Seleccionar cliente)");
        marca = new JComboBox<>();
        modelo = new JComboBox<>();

        cliente.setOnSearch(() -> {
            ClienteDialog dialog = new ClienteDialog(this, (texto) -> {
                cliente.setText(texto);
                try {
                    String[] partes = texto.split(" - ");
                    if (partes.length > 0) {
                        idClienteSeleccionado = Integer.parseInt(partes[0]);
                    }
                } catch (Exception e) {
                    idClienteSeleccionado = -1;
                }
            });
            dialog.setVisible(true);
        });

        int r = 0;
        addField(form, g, r++, "Placa", placa);
        addField(form, g, r++, "Cliente", cliente);
        addField(form, g, r++, "Marca", marca);
        addField(form, g, r++, "Modelo", modelo);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        btnBuscar = UITheme.primaryButton("Buscar");
        btnGuardar = UITheme.primaryButton("Guardar");
        btnEliminar = UITheme.primaryButton("Eliminar");
        actions.add(btnBuscar); actions.add(btnGuardar); actions.add(btnEliminar);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        form.add(actions, g);

        // Tabla
        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"ID", "Placa", "Cliente", "Marca", "Modelo"};
        
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(model);
        table.setRowHeight(26);
        
        // Evento Selección
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                cargarDatosDeFilaSeleccionada();
            }
        });

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

    // --- LÓGICA DE SELECCIÓN Y BÚSQUEDA ---
    
    private void cargarDatosDeFilaSeleccionada() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        // 1. CAPTURAR ID DEL VEHÍCULO SELECCIONADO
        idVehiculoSeleccionado = Integer.parseInt(table.getValueAt(row, 0).toString());
        String placaVal = table.getValueAt(row, 1).toString();
        
        VehiculoDAO dao = new VehiculoDAO();
        Vehiculo v = dao.buscarPorPlaca(placaVal);
        
        if (v != null) {
            placa.setText(v.getPlaca());
            cliente.setText(v.getIdCliente() + " - " + v.getNombreCliente()); // Mostramos ID para consistencia
            idClienteSeleccionado = v.getIdCliente();
            
            // Seleccionar Combos
            seleccionarEnCombo(marca, v.getIdMarca());
            seleccionarEnCombo(modelo, v.getIdModelo());
            
            if (mode == UIMode.EDIT) setFieldsEditable(true);
        }
    }
    
    private void seleccionarEnCombo(JComboBox<OpcionCombo> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void onBuscar() {
        String texto = placa.getText().trim();
        if (texto.isEmpty()) {
            cargarTabla();
            return;
        }
        VehiculoDAO dao = new VehiculoDAO();
        Vehiculo v = dao.buscarPorPlaca(texto);

        if (v != null) {
            model.setRowCount(0);
            model.addRow(new Object[]{v.getId(), v.getPlaca(), v.getNombreCliente(), v.getNombreMarca(), v.getNombreModelo()});
            table.setRowSelectionInterval(0, 0); // Esto disparará cargarDatosDeFilaSeleccionada
        } else {
            JOptionPane.showMessageDialog(this, "No encontrado");
            cargarTabla();
        }
    }

    // --- ACCIONES PRINCIPALES CORREGIDAS ---

    private void onGuardar() {
        // Validaciones
        if (placa.getText().isEmpty() || marca.getSelectedItem() == null || modelo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.");
            return;
        }
        if (idClienteSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente válido.");
            return;
        }

        OpcionCombo m = (OpcionCombo) marca.getSelectedItem();
        OpcionCombo mo = (OpcionCombo) modelo.getSelectedItem();
        
        Vehiculo v = new Vehiculo();
        v.setPlaca(placa.getText());
        v.setIdMarca(m.getId());
        v.setIdModelo(mo.getId());
        v.setIdCliente(idClienteSeleccionado);
        
        VehiculoDAO dao = new VehiculoDAO();
        boolean exito = false;

        // LÓGICA DE ACTUALIZAR VS GUARDAR
        if (mode == UIMode.EDIT) {
            if (idVehiculoSeleccionado == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un vehículo de la tabla para editar.");
                return;
            }
            v.setId(idVehiculoSeleccionado); // Pasamos el ID para el WHERE del SQL
            exito = dao.actualizar(v);
        } else {
            // MODO AGREGAR
            exito = dao.registrar(v);
        }
        
        if (exito) {
            JOptionPane.showMessageDialog(this, "Operación exitosa.");
            cargarTabla();
            limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar (Verifica duplicados o conexión).");
        }
    }
    
    private void onEliminar() {
        if (idVehiculoSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un vehículo de la tabla para eliminar.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar vehículo seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            VehiculoDAO dao = new VehiculoDAO();
            if (dao.eliminar(idVehiculoSeleccionado)) {
                JOptionPane.showMessageDialog(this, "Vehículo eliminado.");
                cargarTabla();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar (Puede tener citas asociadas).");
            }
        }
    }
    
    private void limpiarCampos() {
        placa.setText("");
        cliente.setText("(Seleccionar cliente)");
        idClienteSeleccionado = -1;
        idVehiculoSeleccionado = -1;
        if (marca.getItemCount() > 0) marca.setSelectedIndex(0);
        if (modelo.getItemCount() > 0) modelo.setSelectedIndex(0);
    }

    // --- HELPERS UI ---

    private void applyMode(UIMode m) {
        this.mode = m;
        btnBuscar.setVisible(m != UIMode.ADD);
        btnGuardar.setVisible(m != UIMode.DELETE);
        btnEliminar.setVisible(m == UIMode.DELETE);
        
        if(m == UIMode.ADD) {
            placa.setEditable(true);
            setFieldsEnabled(true);
        } else {
            placa.setEditable(true);
            setFieldsEnabled(false);
        }
    }
    
    private void setFieldsEnabled(boolean b) {
        cliente.setEnabled(b);
        marca.setEnabled(b);
        modelo.setEnabled(b);
    }
    
    private void setFieldsEditable(boolean b) {
        marca.setEnabled(b);
        modelo.setEnabled(b);
    }

    private JPanel header(String title, String subtitle) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        top.setPreferredSize(new Dimension(0, 64));
        JLabel t = new JLabel("  " + title); t.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel s = new JLabel("  " + subtitle); s.setForeground(UITheme.MUTED);
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(t); text.add(s);
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