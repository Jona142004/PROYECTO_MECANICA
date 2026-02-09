package ui.servicios;

import dao.ServicioDAO;
import model.Servicio;
import ui.components.UIMode;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ServiciosFrame extends JFrame {

    private UIMode mode;

    // Campos

    private JTextField nombre;
    private JTextField precio;
    private JComboBox<String> gravaIva;
    private JComboBox<String> estado;

    // Tabla
    private JTable table;
    private DefaultTableModel model;
    private JButton btnBuscar, btnGuardar, btnEliminar;
    
    private int idSeleccionado = -1;

    public ServiciosFrame(UIMode mode) {
        this.mode = mode;
        setTitle("Gestión de Servicios");
        setSize(1080, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(header("Servicios", "Mano de obra y precios"), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);

        applyMode(mode);
        cargarTabla();
    }

    public ServiciosFrame() { this(UIMode.ADD); }

    // --- DATOS ---

    private void cargarTabla() {
        model.setRowCount(0);
        ServicioDAO dao = new ServicioDAO();
        List<Servicio> lista = dao.listar();
        for(Servicio s : lista) {
            model.addRow(new Object[]{
                s.getId(), s.getNombre(), String.format("%.2f", s.getPrecio()), 
                s.getIva().equals("S") ? "SÍ" : "NO", 
                s.getEstado().equals("A") ? "ACTIVO" : "INACTIVO"
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
        g.insets = new Insets(6, 6, 6, 6); g.fill = GridBagConstraints.HORIZONTAL;

        nombre = new JTextField();
        precio = new JTextField();
        gravaIva = new JComboBox<>(new String[]{"SÍ", "NO"});
        estado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

        int r = 0;

        addField(form, g, r++, "Nombre Servicio", nombre);
        addField(form, g, r++, "Precio ($)", precio);
        addField(form, g, r++, "¿Grava IVA?", gravaIva);
        addField(form, g, r++, "Estado", estado);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        btnBuscar = UITheme.primaryButton("Buscar");
        btnGuardar = UITheme.primaryButton("Guardar");
        btnEliminar = UITheme.primaryButton("Eliminar");
        actions.add(btnBuscar); actions.add(btnGuardar); actions.add(btnEliminar);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2; form.add(actions, g);

        // Tabla
        JPanel tableCard = UITheme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        String[] cols = {"ID", "Servicio", "Precio", "IVA", "Estado"};
        
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        
        // Selección
        table.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting() && table.getSelectedRow() != -1) cargarSeleccion();
        });

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, tableCard);
        split.setResizeWeight(0.4); split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> onBuscar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnEliminar.addActionListener(e -> onEliminar());

        return root;
    }

    // --- ACCIONES ---

    private void cargarSeleccion() {
        int row = table.getSelectedRow();
        if(row == -1) return;
        
        idSeleccionado = Integer.parseInt(table.getValueAt(row, 0).toString());
        nombre.setText(table.getValueAt(row, 1).toString());
        
        // Convertir precio de texto (ej "35,00") a formato editable
        String precioTexto = table.getValueAt(row, 2).toString().replace(",", ".");
        precio.setText(precioTexto);
        
        gravaIva.setSelectedItem(table.getValueAt(row, 3).toString());
        estado.setSelectedItem(table.getValueAt(row, 4).toString());
        
        if (mode == UIMode.EDIT) setFieldsEditable(true);
    }

    private void onBuscar() {
        String txt = nombre.getText();
        if (txt.isEmpty()) { cargarTabla(); return; }
        
        ServicioDAO dao = new ServicioDAO();
        Servicio s = dao.buscarPorNombre(txt);
        
        if (s != null) {
            model.setRowCount(0);
            model.addRow(new Object[]{
                s.getId(), s.getNombre(), s.getPrecio(), 
                s.getIva().equals("S")?"SÍ":"NO", s.getEstado().equals("A")?"ACTIVO":"INACTIVO"
            });
            table.setRowSelectionInterval(0, 0);
        } else {
            JOptionPane.showMessageDialog(this, "No encontrado");
            cargarTabla();
        }
    }

    private void onGuardar() {
        if(nombre.getText().isEmpty() || precio.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete los campos."); return;
        }
        
        double valorPrecio = 0;
        try {
            valorPrecio = Double.parseDouble(precio.getText().replace(",", "."));
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Precio inválido (use punto o coma)."); return;
        }

        Servicio s = new Servicio();
        s.setNombre(nombre.getText());
        s.setPrecio(valorPrecio);
        s.setIva(gravaIva.getSelectedItem().equals("SÍ") ? "S" : "N");
        
        ServicioDAO dao = new ServicioDAO();
        boolean exito = false;
        
        if (mode == UIMode.ADD) {
            exito = dao.registrar(s);
        } else if (mode == UIMode.EDIT) {
            if(idSeleccionado == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un servicio."); return;
            }
            s.setId(idSeleccionado);
            exito = dao.actualizar(s);
        }
        
        if(exito) {
            JOptionPane.showMessageDialog(this, "Guardado correctamente.");
            cargarTabla();
            nombre.setText(""); precio.setText(""); idSeleccionado = -1;
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar.");
        }
    }

    private void onEliminar() {
        if(idSeleccionado == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this, "¿Desactivar servicio?");
        if(confirm == JOptionPane.YES_OPTION) {
            if(new ServicioDAO().eliminar(idSeleccionado)) {
                JOptionPane.showMessageDialog(this, "Servicio desactivado.");
                cargarTabla();
            }
        }
    }

    // --- HELPERS ---
    private void applyMode(UIMode m) {
        this.mode = m;
        btnBuscar.setVisible(m != UIMode.ADD);
        btnGuardar.setVisible(m != UIMode.DELETE);
        btnEliminar.setVisible(m == UIMode.DELETE);
        
        setFieldsEnabled(m == UIMode.ADD);
        nombre.setEditable(true); // Siempre editable para buscar
    }

    private void setFieldsEnabled(boolean b) {
        // nombre.setEditable(b); // Lo dejamos true para buscar
        precio.setEditable(b);
        gravaIva.setEnabled(b);
    }
    
    private void setFieldsEditable(boolean b) {
        precio.setEditable(b);
        gravaIva.setEnabled(b);
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