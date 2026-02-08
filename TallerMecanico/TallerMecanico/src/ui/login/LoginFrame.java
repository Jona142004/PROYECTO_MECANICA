package ui.login;

import ui.menu.MainFrame;
import ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("Autos y Motores - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 650);
        setLocationRelativeTo(null);

        // Fondo completo
        ImagePanel background = new ImagePanel("background.jpg");
        setContentPane(background);

        // Grid para posicionar logo + card
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE;

        // Panel izquierdo (texto + logo)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        background.add(buildBrandPanel(), gbc);

        // Login card
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        background.add(buildLoginCard(), gbc);
    }


    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 40));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("AUTOS Y MOTORES");
        title.setFont(new Font("Arial Black", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("<html>Sistema de gestión<br/>Taller Mecánico</html>");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 15));
        subtitle.setForeground(Color.WHITE);

        JLabel logo = new JLabel(new ImageIcon(
            getClass().getResource("logo.png")
        ));
        logo.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(logo);

        return panel;
    }

    private JPanel buildLoginCard() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(UITheme.BG);

        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 420));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // ancho real del formulario (no del card)
        content.setMaximumSize(new Dimension(320, Integer.MAX_VALUE));
        content.setAlignmentX(Component.CENTER_ALIGNMENT);

        /* ================= HEADER ================= */
        JLabel h = new JLabel("Iniciar sesión");
        h.setFont(new Font("SansSerif", Font.BOLD, 22));
        h.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel d = new JLabel("Ingresa con un usuario autorizado");
        d.setForeground(UITheme.MUTED);
        d.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(h);
        content.add(Box.createVerticalStrut(4));
        content.add(d);
        content.add(Box.createVerticalStrut(22));

        /* ================= FORM ================= */
        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();

        Dimension fieldSize = new Dimension(300, 38);

        txtUser.setMaximumSize(fieldSize);
        txtUser.setPreferredSize(fieldSize);
        txtUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPass.setMaximumSize(fieldSize);
        txtPass.setPreferredSize(fieldSize);
        txtPass.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(labelBold("Usuario"));
        content.add(Box.createVerticalStrut(6));
        content.add(txtUser);
        content.add(Box.createVerticalStrut(16));

        content.add(labelBold("Contraseña"));
        content.add(Box.createVerticalStrut(6));
        content.add(txtPass);
        content.add(Box.createVerticalStrut(28));

        /* ================= ACTIONS ================= */
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        row.setOpaque(false);

        JButton btnLogin = UITheme.primaryButton("INGRESAR");
        JButton btnExit  = UITheme.primaryButton("SALIR");

        Dimension btnSize = new Dimension(140, 40);

        btnLogin.setPreferredSize(btnSize);
        btnLogin.setMaximumSize(btnSize);

        btnExit.setPreferredSize(btnSize);
        btnExit.setMaximumSize(btnSize);

        btnLogin.addActionListener(e -> {
            new MainFrame().setVisible(true);
            dispose();
        });

        btnExit.addActionListener(e -> System.exit(0));

        row.add(btnLogin);
        row.add(btnExit);

        content.add(row);
        content.add(Box.createVerticalStrut(18));

        /* ================= FOOTER ================= */
        JLabel note = new JLabel("Nota: Esta versión es solo interfaz (no funcional).");
        note.setForeground(UITheme.MUTED);
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(note);
        card.add(content);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        wrap.add(card, gbc);

        return wrap;
    }


    private JLabel labelBold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(UITheme.TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return l;
    }

}
