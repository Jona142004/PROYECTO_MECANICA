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
    private JLabel lblAcumuladorVentas;

    // --- COMPONENTES MODO ANULAR (DELETE) ---
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
        
        JButton btnLimpiar = new JButton("Limpiar / Nueva");
        btnLimpiar.addActionListener(e -> cargarDatosInicialesCrear());
        
        titlePanel.add(title);
        titlePanel.add(Box.createHorizontalStrut(20));
        titlePanel.add(btnLimpiar);

        // ACUMULADOR
        lblAcumuladorVentas = new JLabel(" CARGANDO... ");
        lblAcumuladorVentas.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblAcumuladorVentas.setForeground(new Color(0, 100, 0));
        lblAcumuladorVentas.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        actualizarAcumulador();

        top.add(titlePanel, BorderLayout.WEST);
        top.add(lblAcumuladorVentas, BorderLayout.EAST);
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
        actualizarAcumulador();
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
    
    private void actualizarAcumulador() {
        double total = new FacturaDAO().obtenerTotalVentas();
        lblAcumuladorVentas.setText(String.format(" TOTAL VENTAS HISTÓRICO: $ %.2f ", total));
    }

    // ========================================================================
    //                          MODO 2: ANULAR FACTURA (DELETE)
    // ========================================================================
    private void initModoAnular() {
        add(headerAnular(), BorderLayout.NORTH);
        add(contentAnular(), BorderLayout.CENTER);
        cargarTablaAnular(new FacturaDAO().listarActivas());
    }

    private JPanel headerAnular() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(0,0,1,0, UITheme.BORDER));

        JLabel title = new JLabel("Anulación de Facturas");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        p.add(title);
        p.add(Box.createHorizontalStrut(20));
        p.add(new JLabel("Filtrar por Cliente:"));

        filtroClienteAnular = new SelectorPanel("(Todos los clientes)");
        filtroClienteAnular.setPreferredSize(new Dimension(300, 30));
        filtroClienteAnular.setOnSearch(() -> {
            new ClienteDialog(this, res -> {
                filtroClienteAnular.setText(res);
                try {
                    int idCli = Integer.parseInt(res.split(" - ")[0]);
                    cargarTablaAnular(new FacturaDAO().listarPorCliente(idCli));
                } catch(Exception e){}
            }).setVisible(true);
        });

        JButton btnRefrescar = new JButton("Ver Todas");
        btnRefrescar.addActionListener(e -> {
            filtroClienteAnular.setText("(Todos los clientes)");
            cargarTablaAnular(new FacturaDAO().listarActivas());
        });

        p.add(filtroClienteAnular);
        p.add(btnRefrescar);
        return p;
    }

    private JPanel contentAnular() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        p.setBackground(UITheme.BG);

        // Tabla simplificada para anular
        String[] cols = {"ID", "Nro Factura", "Fecha", "Cliente", "Total ($)", "Estado"};
        modelAnular = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableAnular = new JTable(modelAnular);
        tableAnular.setRowHeight(28);

        // Botón Anular
        JButton btnAnular = new JButton("ANULAR FACTURA SELECCIONADA");
        btnAnular.setBackground(new Color(200, 50, 50));
        btnAnular.setForeground(Color.WHITE);
        btnAnular.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAnular.setPreferredSize(new Dimension(0, 50));
        btnAnular.addActionListener(e -> anularFacturaSeleccionada());

        p.add(new JScrollPane(tableAnular), BorderLayout.CENTER);
        p.add(btnAnular, BorderLayout.SOUTH);
        return p;
    }

    private void cargarTablaAnular(List<Factura> lista) {
        modelAnular.setRowCount(0);
        for(Factura f : lista) {
            modelAnular.addRow(new Object[]{
                f.getId(), f.getNumero(), f.getFecha(), f.getAuxNombreCliente(), f.getTotal(), "ACTIVA"
            });
        }
    }

    private void anularFacturaSeleccionada() {
        int row = tableAnular.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura."); return;
        }

        int id = Integer.parseInt(tableAnular.getValueAt(row, 0).toString());
        String nro = tableAnular.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Anular factura " + nro + "?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if(confirm == JOptionPane.YES_OPTION) {
            if(new FacturaDAO().anular(id)) {
                JOptionPane.showMessageDialog(this, "Factura Anulada.");
                // Refrescar tabla actual
                if(filtroClienteAnular.getText().contains("(Todos")) {
                    cargarTablaAnular(new FacturaDAO().listarActivas());
                } else {
                    // Mantener filtro si estaba activo (re-parsear ID es complejo aqui, mejor refrescar todo o guardar ID filtro)
                    cargarTablaAnular(new FacturaDAO().listarActivas()); 
                    filtroClienteAnular.setText("(Todos los clientes)");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error al anular.");
            }
        }
    }

    // --- UTILS ---
    private void addField(JPanel p, GridBagConstraints g, int y, String l, JComponent c) {
        g.gridx=0; g.gridy=y; g.weightx=0; p.add(new JLabel(l),g);
        g.gridx=1; g.weightx=1; p.add(c,g);
    }
}