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

    private UIMode mode; // El modo actual (ADD, VIEW, DELETE)
    private int idFacturaCargada = -1; // ID de la factura que estamos viendo/anulando

    // Cabecera
    private JTextField numFactura;
    private JTextField fecha;
    private JTextField usuario;
    private SelectorPanel cliente;

    // Detalle
    private JTable table;
    private DefaultTableModel model;
    
    // Botones Detalle
    private JButton btnAddService, btnDelService;

    // Totales
    private JLabel lblSubtotal, lblIva, lblTotal;
    
    // Botones Acción
    private JButton btnGuardar;
    private JButton btnAnular; // Nuevo botón rojo

    // Lógica de Negocio
    private List<DetalleFactura> detalles = new ArrayList<>();
    private int idClienteSeleccionado = -1;

    public FacturacionFrame(UIMode mode) {
        this.mode = mode;
        setTitle("Facturación - Autos y Motores");
        setSize(1250, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String titulo = (mode == UIMode.ADD) ? "Nueva Venta" : (mode == UIMode.DELETE ? "Anular Factura" : "Consultar Factura");
        add(header("Facturación", titulo), BorderLayout.NORTH);
        add(content(), BorderLayout.CENTER);
        add(footer(), BorderLayout.SOUTH);

        // LÓGICA DE INICIO SEGÚN MODO
        if (mode == UIMode.ADD) {
            cargarDatosIniciales();
            aplicarPermisos(true); // Habilitar todo
        } else {
            // Si es Buscar o Anular, abrimos el buscador automáticamente
            // Usamos invokeLater para que la ventana cargue primero
            SwingUtilities.invokeLater(this::abrirBuscador);
            aplicarPermisos(false); // Bloquear todo por defecto
        }
    }
    
    public FacturacionFrame() { this(UIMode.ADD); }

    // --- MÉTODOS DE CARGA ---

    private void cargarDatosIniciales() {
        numFactura.setText(new FacturaDAO().generarNumeroFactura());
        fecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        if (Sesion.get() != null) {
            usuario.setText(Sesion.get().getNombreCompleto());
        } else {
            usuario.setText("Invitado (Sin Sesión)");
        }
    }

    private void abrirBuscador() {
        new FacturasListDialog(this, (id) -> {
            cargarFacturaDesdeBD(id);
        }).setVisible(true);
    }

    private void cargarFacturaDesdeBD(int id) {
        FacturaDAO dao = new FacturaDAO();
        
        // 1. Cargar Detalles
        detalles = dao.listarDetalles(id);
        idFacturaCargada = id;
        
        // 2. Reflejar en Tabla
        recalcularTabla();
        
        // 3. Simular datos cabecera (Idealmente deberías tener un método 'buscarPorId' en DAO que traiga todo)
        // Por ahora ponemos datos visuales
        numFactura.setText("FACT-" + id); 
        fecha.setText("(Fecha guardada)"); 
        usuario.setText("(Vendedor original)");
        cliente.setText("(Cliente original)");

        // 4. Activar botón Anular si es modo DELETE
        if (mode == UIMode.DELETE) {
            btnAnular.setVisible(true);
        }
    }

    private void aplicarPermisos(boolean editable) {
        cliente.setEnabled(editable);
        btnAddService.setEnabled(editable);
        btnDelService.setEnabled(editable);
        btnGuardar.setVisible(editable);
        btnAnular.setVisible(false); // Se activa solo al cargar factura en modo DELETE
    }

    // ================= UI PRINCIPAL =================

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        root.setBackground(UITheme.BG);

        // --- SECCIÓN CABECERA ---
        JPanel cab = UITheme.cardPanel();
        cab.setLayout(new GridBagLayout());
        cab.setPreferredSize(new Dimension(0, 180));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6); g.fill = GridBagConstraints.HORIZONTAL;

        numFactura = new JTextField(); numFactura.setEditable(false);
        fecha = new JTextField();      fecha.setEditable(false);
        usuario = new JTextField();    usuario.setEditable(false);

        cliente = new SelectorPanel("(Seleccionar cliente)");
        cliente.setOnSearch(() -> {
            new ClienteDialog(this, (res) -> {
                cliente.setText(res);
                try {
                    String[] parts = res.split(" - ");
                    if (parts.length > 0) idClienteSeleccionado = Integer.parseInt(parts[0]);
                } catch (Exception e) { idClienteSeleccionado = -1; }
            }).setVisible(true);
        });

        int r = 0;
        addField(cab, g, r++, "Nro. Factura", numFactura);
        addField(cab, g, r++, "Fecha Emisión", fecha);
        addField(cab, g, r++, "Cliente", cliente);
        addField(cab, g, r++, "Vendedor", usuario);

        // --- SECCIÓN DETALLE (TABLA) ---
        JPanel det = UITheme.cardPanel();
        det.setLayout(new BorderLayout(0, 10));
        det.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Toolbar botones
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tools.setOpaque(false);
        btnAddService = UITheme.primaryButton("+ Agregar Servicio");
        btnDelService = UITheme.primaryButton("- Quitar Seleccionado");
        
        btnAddService.addActionListener(e -> agregarServicio());
        btnDelService.addActionListener(e -> quitarServicio());
        
        tools.add(btnAddService); tools.add(btnDelService);

        String[] cols = {"Cant", "Descripción", "P. Unit ($)", "Subtotal", "IVA", "Total"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        
        det.add(tools, BorderLayout.NORTH);
        det.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cab, det);
        split.setBorder(null); split.setResizeWeight(0.25);
        
        root.add(split, BorderLayout.CENTER);
        return root;
    }
    
    // ================= SECCIÓN TOTALES Y BOTONES =================
    
    private JPanel footer() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        p.setBackground(UITheme.BG);
        
        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        totalsPanel.setOpaque(false);
        
        lblSubtotal = new JLabel("0.00"); lblSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblIva = new JLabel("0.00");      lblIva.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotal = new JLabel("0.00");    lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        totalsPanel.add(new JLabel("Subtotal:")); totalsPanel.add(lblSubtotal);
        totalsPanel.add(new JLabel("IVA Total:")); totalsPanel.add(lblIva);
        totalsPanel.add(new JLabel("TOTAL A PAGAR:")); totalsPanel.add(lblTotal);
        
        // BOTONES DE ACCIÓN
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        
        btnGuardar = UITheme.primaryButton("GUARDAR FACTURA");
        btnGuardar.setPreferredSize(new Dimension(180, 45));
        btnGuardar.addActionListener(e -> guardarFacturaBD());
        
        btnAnular = new JButton("ANULAR FACTURA");
        btnAnular.setBackground(new Color(220, 38, 38)); // Rojo
        btnAnular.setForeground(Color.WHITE);
        btnAnular.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAnular.setPreferredSize(new Dimension(180, 45));
        btnAnular.setVisible(false); // Oculto por defecto
        btnAnular.addActionListener(e -> anularFactura());

        actions.add(btnGuardar);
        actions.add(btnAnular);
        
        p.add(totalsPanel, BorderLayout.EAST);
        p.add(actions, BorderLayout.WEST);
        
        return p;
    }

    // ================= LÓGICA DE NEGOCIO =================

    private void agregarServicio() {
        new ServicioDialog(this, (servicio) -> {
            String input = JOptionPane.showInputDialog(this, "Cantidad para " + servicio.getNombre() + ":", "1");
            if(input == null) return;
            try {
                int cant = Integer.parseInt(input);
                if(cant <= 0) throw new Exception();
                boolean grava = servicio.getIva().equals("S");
                DetalleFactura d = new DetalleFactura(servicio.getId(), servicio.getNombre(), cant, servicio.getPrecio(), grava);
                detalles.add(d);
                recalcularTabla();
            } catch(Exception e) {
                JOptionPane.showMessageDialog(this, "Cantidad inválida.");
            }
        }).setVisible(true);
    }
    
    private void quitarServicio() {
        int row = table.getSelectedRow();
        if(row == -1) { JOptionPane.showMessageDialog(this, "Seleccione una fila."); return; }
        detalles.remove(row);
        recalcularTabla();
    }
    
    private void recalcularTabla() {
        model.setRowCount(0);
        double subtotalGral = 0;
        double ivaGral = 0;
        
        for(DetalleFactura d : detalles) {
            d.calcular();
            subtotalGral += d.getSubtotal();
            ivaGral += d.getValorIva();
            model.addRow(new Object[]{d.getCantidad(), d.getNombreServicio(), String.format("%.2f", d.getPrecioUnitario()), String.format("%.2f", d.getSubtotal()), String.format("%.2f", d.getValorIva()), String.format("%.2f", d.getTotal())});
        }
        lblSubtotal.setText(String.format("%.2f", subtotalGral));
        lblIva.setText(String.format("%.2f", ivaGral));
        lblTotal.setText(String.format("%.2f", subtotalGral + ivaGral));
    }

    private void guardarFacturaBD() {
        if(idClienteSeleccionado == -1) { JOptionPane.showMessageDialog(this, "Seleccione un cliente."); return; }
        if(detalles.isEmpty()) { JOptionPane.showMessageDialog(this, "La factura está vacía."); return; }
        if(Sesion.get() == null) { JOptionPane.showMessageDialog(this, "Error de sesión."); return; }

        Factura f = new Factura();
        f.setNumero(numFactura.getText());
        f.setFecha(new java.sql.Date(System.currentTimeMillis()));
        f.setIdCliente(idClienteSeleccionado);
        f.setIdUsuario(Sesion.get().getId());
        try {
            f.setSubtotal(Double.parseDouble(lblSubtotal.getText().replace(",", ".")));
            f.setIva(Double.parseDouble(lblIva.getText().replace(",", ".")));
            f.setTotal(Double.parseDouble(lblTotal.getText().replace(",", ".")));
        } catch(Exception e) { return; }
        f.setDetalles(detalles);

        if(new FacturaDAO().guardarFactura(f)) {
            JOptionPane.showMessageDialog(this, "¡Factura guardada correctamente!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar en BD.");
        }
    }
    
    private void anularFactura() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de ANULAR esta factura?\nEsta acción es irreversible.", "Confirmar Anulación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            FacturaDAO dao = new FacturaDAO();
            if (dao.anular(idFacturaCargada)) {
                JOptionPane.showMessageDialog(this, "Factura anulada correctamente.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al anular.");
            }
        }
    }

    // ================= UI HELPERS =================

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