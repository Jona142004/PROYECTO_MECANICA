package ui.facturacion;

import dao.FacturaDAO;
import model.DetalleFactura;
import model.Factura;
import model.Sesion;
import ui.clientes.ClienteDialog;
import ui.components.SelectorPanel;
import ui.components.UIMode;
import ui.servicios.ServicioDialog;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FacturacionFrame extends JFrame {

    private UIMode mode;
    
    // --- COMPONENTES MODO CREAR (ADD) ---
    private JTextField numFactura, fecha, usuario;
    private SelectorPanel clienteAdd; // Selector para nueva venta
    private JTable tableDetalle;
    private DefaultTableModel modelDetalle;
    private JLabel lblSubtotal, lblIva, lblTotal;
    private List<DetalleFactura> detalles = new ArrayList<>();
    private int idClienteAdd = -1;

    // --- COMPONENTES MODO ANULAR (DELETE) ---
    private JTextField txtBusquedaNro;
    private SelectorPanel filtroClienteAnular; // Filtro para buscar
    private JTable tableAnular;
    private DefaultTableModel modelAnular;

    public FacturacionFrame(UIMode mode) {
        this.mode = mode;
        setTitle("Facturación - Autos y Motores");
        setSize(1250, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // CONSTRUCCIÓN DINÁMICA SEGÚN EL MODO
        if (mode == UIMode.ADD) {
            initModoCrear();
        } else if (mode == UIMode.DELETE) {
            initModoAnular();
        }
    }
    
    public FacturacionFrame() {
        this(UIMode.ADD);
    }

    // ========================================================================
    //                          MODO 1: CREAR FACTURA (ADD)
    // ========================================================================
    private void initModoCrear() {
        add(headerCrear(), BorderLayout.NORTH);
        add(contentCrear(), BorderLayout.CENTER);
        add(footerCrear(), BorderLayout.SOUTH);
        
        cargarDatosInicialesCrear();
    }

    private JPanel headerCrear() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setPreferredSize(new Dimension(0, 80));
        top.setBorder(BorderFactory.createMatteBorder(0,0,1,0,UITheme.BORDER));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Nueva Venta"); 
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        JButton btnLimpiar = UITheme.primaryButton("Limpiar / Nueva");
        btnLimpiar.addActionListener(e -> cargarDatosInicialesCrear());
        
        titlePanel.add(title);
        titlePanel.add(Box.createHorizontalStrut(20));
        titlePanel.add(btnLimpiar);

        top.add(titlePanel, BorderLayout.WEST);
        return top;
    }

    private JPanel contentCrear() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setBackground(UITheme.BG);

        // Formulario
        JPanel form = UITheme.cardPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5); g.fill = GridBagConstraints.HORIZONTAL;

        numFactura = new JTextField(); numFactura.setEditable(false);
        fecha = new JTextField(); fecha.setEditable(false);
        usuario = new JTextField(); usuario.setEditable(false);
        
        clienteAdd = new SelectorPanel("(Seleccionar cliente)");
        clienteAdd.setOnSearch(() -> {
            new ClienteDialog(this, res -> {
                clienteAdd.setText(res);
                try { idClienteAdd = Integer.parseInt(res.split(" - ")[0]); } catch(Exception e){}
            }).setVisible(true);
        });

        addField(form, g, 0, "Nro:", numFactura);
        addField(form, g, 1, "Fecha:", fecha);
        addField(form, g, 2, "Cliente:", clienteAdd);
        addField(form, g, 3, "Vendedor:", usuario);

        // Tabla Detalles
        JPanel det = UITheme.cardPanel();
        det.setLayout(new BorderLayout());
        
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tools.setOpaque(false);
        JButton btnAdd = UITheme.primaryButton("+ Agregar Servicio");
        JButton btnDel = UITheme.primaryButton("- Quitar");
        
        btnAdd.addActionListener(e -> agregarServicio());
        btnDel.addActionListener(e -> quitarServicio());
        
        tools.add(btnAdd); tools.add(btnDel);
        
        modelDetalle = new DefaultTableModel(new String[]{"Cant", "Servicio", "P.Unit", "Total"}, 0);
        tableDetalle = new JTable(modelDetalle);
        tableDetalle.setRowHeight(24);
        
        det.add(tools, BorderLayout.NORTH);
        det.add(new JScrollPane(tableDetalle), BorderLayout.CENTER);

        p.add(form, BorderLayout.NORTH);
        p.add(det, BorderLayout.CENTER);
        return p;
    }

    private JPanel footerCrear() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));

        JPanel montos = new JPanel(new GridLayout(3, 2, 5, 5));
        montos.setOpaque(false);
        lblSubtotal = new JLabel("0.00"); lblIva = new JLabel("0.00"); lblTotal = new JLabel("0.00");
        
        montos.add(new JLabel("Subtotal:")); montos.add(lblSubtotal);
        montos.add(new JLabel("IVA:")); montos.add(lblIva);
        montos.add(new JLabel("TOTAL:")); montos.add(lblTotal);

        JButton btnGuardar = UITheme.primaryButton("GUARDAR VENTA");
        btnGuardar.setPreferredSize(new Dimension(150, 50));
        btnGuardar.addActionListener(e -> guardarFactura());

        p.add(btnGuardar, BorderLayout.WEST);
        p.add(montos, BorderLayout.EAST);
        return p;
    }

    // --- LÓGICA MODO CREAR ---
    private void cargarDatosInicialesCrear() {
        detalles.clear();
        recalcularTablaDetalle();
        numFactura.setText(new FacturaDAO().generarNumeroFactura());
        fecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        usuario.setText(Sesion.get() != null ? Sesion.get().getNombreCompleto() : "Invitado");
        clienteAdd.setText("(Seleccionar cliente)");
        idClienteAdd = -1;
    }

    private void agregarServicio() {
        new ServicioDialog(this, s -> {
            String q = JOptionPane.showInputDialog("Cantidad:");
            if(q != null) {
                try {
                    int cant = Integer.parseInt(q);
                    boolean grava = s.getIva().equals("S");
                    detalles.add(new DetalleFactura(s.getId(), s.getNombre(), cant, s.getPrecio(), grava));
                    recalcularTablaDetalle();
                } catch(Exception e){ JOptionPane.showMessageDialog(this, "Cantidad inválida"); }
            }
        }).setVisible(true);
    }

    private void quitarServicio() {
        int r = tableDetalle.getSelectedRow();
        if(r >= 0) { detalles.remove(r); recalcularTablaDetalle(); }
    }

    private void recalcularTablaDetalle() {
        modelDetalle.setRowCount(0);
        double st = 0, iv = 0;
        for(DetalleFactura d : detalles) {
            d.calcular();
            st += d.getSubtotal();
            iv += d.getValorIva();
            modelDetalle.addRow(new Object[]{d.getCantidad(), d.getNombreServicio(), d.getPrecioUnitario(), d.getTotal()});
        }
        lblSubtotal.setText(String.format("%.2f", st).replace(",","."));
        lblIva.setText(String.format("%.2f", iv).replace(",","."));
        lblTotal.setText(String.format("%.2f", st+iv).replace(",","."));
    }

    private void guardarFactura() {
        if(detalles.isEmpty() || idClienteAdd == -1) {
            JOptionPane.showMessageDialog(this, "Complete cliente y servicios."); return;
        }
        Factura f = new Factura();
        f.setNumero(numFactura.getText());
        f.setFecha(new java.sql.Date(System.currentTimeMillis()));
        f.setIdCliente(idClienteAdd);
        f.setIdUsuario(Sesion.get().getId());
        f.setSubtotal(Double.parseDouble(lblSubtotal.getText()));
        f.setIva(Double.parseDouble(lblIva.getText()));
        f.setTotal(Double.parseDouble(lblTotal.getText()));
        f.setDetalles(detalles);

        if(new FacturaDAO().guardarFactura(f)) {
            JOptionPane.showMessageDialog(this, "Factura Guardada!");
            cargarDatosInicialesCrear();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar.");
        }
    }

// ========================================================================
    //                 MODO 2: BUSCAR Y ANULAR FACTURA (DELETE)
    // ========================================================================
    private void initModoAnular() {
        // HEADER: Panel de búsqueda
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Filtro 1: Por Número
        gbc.gridx = 0; header.add(new JLabel("Nro. Factura:"), gbc);
        txtBusquedaNro = new JTextField(12);
        gbc.gridx = 1; header.add(txtBusquedaNro, gbc);

        // Filtro 2: Por Cliente
        gbc.gridx = 2; header.add(new JLabel("Cliente:"), gbc);
        filtroClienteAnular = new SelectorPanel("(Todos)");
        filtroClienteAnular.setPreferredSize(new Dimension(250, 30));
        filtroClienteAnular.setOnSearch(() -> abrirBuscadorClienteAnular());
        gbc.gridx = 3; header.add(filtroClienteAnular, gbc);

        // Botones de acción
        JButton btnBuscar = UITheme.primaryButton("Buscar");
        gbc.gridx = 4; header.add(btnBuscar, gbc);

        JButton btnRefrescar = new JButton("Limpiar");
        gbc.gridx = 5; header.add(btnRefrescar, gbc);

        // Eventos de búsqueda
        btnBuscar.addActionListener(e -> ejecutarBusqueda());
        txtBusquedaNro.addActionListener(e -> ejecutarBusqueda());
        btnRefrescar.addActionListener(e -> {
            txtBusquedaNro.setText("");
            filtroClienteAnular.setText("(Todos)");
            cargarTablaAnular(new FacturaDAO().listarActivas());
        });

        // CUERPO: Tabla y Botón de Anular
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        centerPanel.setBackground(UITheme.BG);

        String[] cols = {"ID", "Nro Factura", "Fecha", "Cliente", "Total ($)", "Estado"};
        modelAnular = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableAnular = new JTable(modelAnular);
        tableAnular.setRowHeight(30);
        tableAnular.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton btnAnular = UITheme.primaryButton("ANULAR FACTURA SELECCIONADA");
        btnAnular.setBackground(new Color(220, 38, 38)); // Rojo fuerte
        btnAnular.setForeground(Color.WHITE);
        btnAnular.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAnular.setPreferredSize(new Dimension(0, 50));
        btnAnular.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAnular.addActionListener(e -> anularFacturaSeleccionada());

        centerPanel.add(new JScrollPane(tableAnular), BorderLayout.CENTER);
        centerPanel.add(btnAnular, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // Carga inicial
        cargarTablaAnular(new FacturaDAO().listarActivas());
    }

    private void abrirBuscadorClienteAnular() {
        new ClienteDialog(this, res -> {
            filtroClienteAnular.setText(res);
            ejecutarBusqueda(); // Buscar automáticamente al seleccionar
        }).setVisible(true);
    }

    private void ejecutarBusqueda() {
        String nro = txtBusquedaNro.getText().trim();
        String cliText = filtroClienteAnular.getText();
        FacturaDAO dao = new FacturaDAO();
        
        if (!nro.isEmpty()) {
            // Prioridad búsqueda por número
            cargarTablaAnular(dao.listarPorNumero(nro));
        } else if (!cliText.equals("(Todos)")) {
            // Búsqueda por ID de cliente
            try {
                int idCli = Integer.parseInt(cliText.split(" - ")[0]);
                cargarTablaAnular(dao.listarPorCliente(idCli));
            } catch (Exception e) {
                cargarTablaAnular(dao.listarActivas());
            }
        } else {
            cargarTablaAnular(dao.listarActivas());
        }
    }

    private void cargarTablaAnular(List<Factura> lista) {
        modelAnular.setRowCount(0);
        if (lista == null) return;
        for (Factura f : lista) {
            modelAnular.addRow(new Object[]{
                f.getId(), 
                f.getNumero(), 
                f.getFecha(), 
                f.getAuxNombreCliente(), 
                String.format("%.2f", f.getTotal()), 
                "ACTIVA"
            });
        }
    }

    private void anularFacturaSeleccionada() {
        int row = tableAnular.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una factura de la tabla.");
            return;
        }

        int id = (int) tableAnular.getValueAt(row, 0);
        String nro = tableAnular.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de anular la factura Nro: " + nro + "?\nEsta acción no se puede deshacer.", 
            "Confirmar Anulación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (new FacturaDAO().anular(id)) {
                JOptionPane.showMessageDialog(this, "Factura anulada correctamente.");
                ejecutarBusqueda(); // Refrescar con los filtros actuales
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo anular la factura.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- UTILS ---
    private void addField(JPanel p, GridBagConstraints g, int y, String l, JComponent c) {
        g.gridx=0; g.gridy=y; g.weightx=0; p.add(new JLabel(l),g);
        g.gridx=1; g.weightx=1; p.add(c,g);
    }
    
}