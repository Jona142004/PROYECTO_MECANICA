package ui.menu;

import ui.citas.CitasFrame;
import ui.clientes.ClientesFrame;
import ui.components.AccordionSection;
import ui.components.UIMode;
import ui.empleados.EmpleadosFrame;
import ui.facturacion.FacturacionFrame;
import ui.login.LoginFrame;
import ui.servicios.ServiciosFrame;
import ui.theme.UITheme;
import ui.usuarios.UsuariosFrame;
import ui.vehiculos.VehiculosFrame;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel sidebar;
    private AccordionSection secEmpleados;
    private AccordionSection secUsuarios;
    private AccordionSection secClientes;
    private AccordionSection secVehiculos;
    private AccordionSection secFact;
    private AccordionSection secServicios;
    private AccordionSection secCitas;

    private JButton btnCliEliminar;
    private JButton btnVehEditar;
    private JButton btnVehEliminar;
    private JButton btnFacBuscar;
    private JButton btnFacAnular;


    private JFrame ventanaActiva = null;
    private AccordionSection expandedSection = null;

    private String permisoSesion;

    public MainFrame(String permiso) {
        this.permisoSesion = permiso;

        setTitle("Autos y Motores - Panel Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildDashboard(), BorderLayout.CENTER);

        applyRoleUI(permisoSesion);
    }

    

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        top.setPreferredSize(new Dimension(0, 56));

        JLabel title = new JLabel("  Autos y Motores");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        right.setOpaque(false);

        JButton logout = UITheme.primaryButton("Cerrar sesión");

        logout.addActionListener(e -> {
            dispose(); // cierra MainFrame
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        right.add(logout);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(UITheme.BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));

        JLabel menuTitle = new JLabel("MENÚ");
        menuTitle.setForeground(new Color(203, 213, 225));
        menuTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(menuTitle);
        sidebar.add(Box.createVerticalStrut(10));

        // ===== SECCIONES (ACORDEÓN) =====

        // CLIENTES (E: Añadir + Editar | A: + Eliminar)
        secClientes = new AccordionSection("Clientes");
        secClientes.setAlignmentX(Component.LEFT_ALIGNMENT);
        secClientes.addItem("Añadir", () -> abrirVentana(new ClientesFrame(UIMode.ADD)));
        secClientes.addItem("Editar", () -> abrirVentana(new ClientesFrame(UIMode.EDIT)));
        btnCliEliminar = secClientes.addItem("Eliminar", () -> abrirVentana(new ClientesFrame(UIMode.DELETE)));

        // VEHÍCULOS (E: solo Añadir | A: + Editar/Eliminar)
        secVehiculos = new AccordionSection("Vehículos");
        secVehiculos.setAlignmentX(Component.LEFT_ALIGNMENT);
        secVehiculos.addItem("Añadir", () -> abrirVentana(new VehiculosFrame(UIMode.ADD)));
        btnVehEditar = secVehiculos.addItem("Editar", () -> abrirVentana(new VehiculosFrame(UIMode.EDIT)));
        btnVehEliminar = secVehiculos.addItem("Eliminar", () -> abrirVentana(new VehiculosFrame(UIMode.DELETE)));

        // CITAS (solo Admin según tu regla)
        secCitas = new AccordionSection("Citas");
        secCitas.setAlignmentX(Component.LEFT_ALIGNMENT);
        secCitas.addItem("Agendar", () -> abrirVentana(new CitasFrame(UIMode.ADD)));
        secCitas.addItem("Cancelar/Eliminar", () -> abrirVentana(new CitasFrame(UIMode.DELETE)));

        // SERVICIOS (solo Admin según tu regla)
        secServicios = new AccordionSection("Servicios");
        secServicios.setAlignmentX(Component.LEFT_ALIGNMENT);
        secServicios.addItem("Añadir", () -> abrirVentana(new ServiciosFrame(UIMode.ADD)));
        secServicios.addItem("Editar", () -> abrirVentana(new ServiciosFrame(UIMode.EDIT)));
        secServicios.addItem("Eliminar", () -> abrirVentana(new ServiciosFrame(UIMode.DELETE)));

        // FACTURACIÓN (E: solo Crear | A: + Buscar/Anular)
        secFact = new AccordionSection("Facturación");
        secFact.setAlignmentX(Component.LEFT_ALIGNMENT);
        secFact.addItem("Crear factura", () -> abrirVentana(new FacturacionFrame(UIMode.ADD)));
        btnFacAnular = secFact.addItem("Anular factura", () -> abrirVentana(new FacturacionFrame(UIMode.DELETE)));

        sidebar.add(secClientes);
        sidebar.add(secVehiculos);
        sidebar.add(secCitas);
        sidebar.add(secServicios);
        sidebar.add(secFact);

        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(10));

        // EMPLEADOS (solo Admin)
        secEmpleados = new AccordionSection("Empleados");
        secEmpleados.setAlignmentX(Component.LEFT_ALIGNMENT);
        secEmpleados.addItem("Añadir", () -> abrirVentana(new EmpleadosFrame(UIMode.ADD)));
        secEmpleados.addItem("Editar", () -> abrirVentana(new EmpleadosFrame(UIMode.EDIT)));
        secEmpleados.addItem("Eliminar", () -> abrirVentana(new EmpleadosFrame(UIMode.DELETE)));
        // USUARIOS (solo Admin)
        secUsuarios = new AccordionSection("Usuarios");
        secUsuarios.setAlignmentX(Component.LEFT_ALIGNMENT);
        secUsuarios.addItem("Añadir", () -> abrirVentana(new UsuariosFrame(UIMode.ADD)));
        secUsuarios.addItem("Eliminar", () -> abrirVentana(new UsuariosFrame(UIMode.DELETE)));

        sidebar.add(secEmpleados);
        sidebar.add(secUsuarios);

        // ===== LOGICA: SOLO 1 SECCIÓN ABIERTA =====
        wireAccordion(secClientes);
        wireAccordion(secVehiculos);
        wireAccordion(secCitas);
        wireAccordion(secServicios);
        wireAccordion(secFact);
        wireAccordion(secEmpleados);
        wireAccordion(secUsuarios);

        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("v1.0 (Solo Interfaz)");
        footer.setForeground(new Color(148, 163, 184));
        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        return sidebar;
    }

    private void wireAccordion(AccordionSection section) {
        section.getHeaderButton().addActionListener(e -> {
            if (expandedSection != null && expandedSection != section) {
                expandedSection.setExpanded(false);
            }
            boolean newState = !section.isExpanded();
            section.setExpanded(newState);
            expandedSection = newState ? section : null;
        });
    }
    
    private JPanel buildDashboard() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel hero = UITheme.cardPanel();
        hero.setLayout(new BorderLayout());

        JLabel h = new JLabel("Panel principal");
        h.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel d = new JLabel("<html>Accede a los módulos del sistema desde el menú lateral.<br/>Este proyecto es solo interfaz (sin BD, sin lógica).</html>");
        d.setForeground(UITheme.MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(h);
        text.add(Box.createVerticalStrut(8));
        text.add(d);

        hero.add(text, BorderLayout.CENTER);

        JPanel quick = new JPanel(new GridLayout(2, 3, 12, 12));
        quick.setOpaque(false);
        quick.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        quick.add(quickCard("Clientes", "Registrar y administrar clientes"));
        quick.add(quickCard("Vehículos", "Registrar vehículos por cliente"));
        quick.add(quickCard("Citas", "Agenda y disponibilidad"));
        quick.add(quickCard("Servicios", "Servicios con IVA/no IVA"));
        quick.add(quickCard("Facturación", "Factura y detalle de factura"));
        quick.add(quickCard("Usuarios", "Acceso y permisos"));

        center.add(hero, BorderLayout.NORTH);
        center.add(quick, BorderLayout.CENTER);

        return center;
    }

    private JPanel quickCard(String title, String desc) {
        JPanel c = UITheme.cardPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel d = new JLabel("<html><span style='color:#6b7280;'>" + desc + "</span></html>");
        c.add(t);
        c.add(Box.createVerticalStrut(6));
        c.add(d);
        return c;
    }

    private void applyRoleUI(String permiso) {
        boolean isAdmin = "A".equalsIgnoreCase(permiso);

        // Secciones completas solo admin
        if (secUsuarios != null) secUsuarios.setVisible(isAdmin);
        if (secEmpleados != null) secEmpleados.setVisible(isAdmin);

        if (secServicios != null) secServicios.setVisible(isAdmin);
        if (secCitas != null) secCitas.setVisible(isAdmin);

        // ===== RESTRICCIÓN DE ITEMS (Empleado E) =====
        // Clientes: no puede eliminar
        if (btnCliEliminar != null) btnCliEliminar.setVisible(isAdmin);

        // Vehículos: empleado NO puede editar/eliminar
        if (btnVehEditar != null) btnVehEditar.setVisible(isAdmin);
        if (btnVehEliminar != null) btnVehEliminar.setVisible(isAdmin);

        // Facturación: empleado NO puede buscar/anular
        if (btnFacBuscar != null) btnFacBuscar.setVisible(isAdmin);
        if (btnFacAnular != null) btnFacAnular.setVisible(isAdmin);

        // Si estaba abierta una sección que se oculta, ciérrala
        if (expandedSection != null && !expandedSection.isVisible()) {
            expandedSection.setExpanded(false);
            expandedSection = null;
        }

        sidebar.revalidate();
        sidebar.repaint();
    }

    private void abrirVentana(JFrame nuevaVentana) {
        if (ventanaActiva != null) {
            ventanaActiva.dispose(); // cierra la ventana anterior
        }
        ventanaActiva = nuevaVentana;
        ventanaActiva.setVisible(true);
    }

}