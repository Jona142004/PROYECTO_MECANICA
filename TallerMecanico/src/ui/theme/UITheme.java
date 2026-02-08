package ui.theme;

import javax.swing.*;
import java.awt.*;

public class UITheme {

    public static final Color BG = new Color(15, 23, 42);
    public static final Color TEXT = new Color(33, 43, 54);
    public static final Color MUTED = new Color(120, 130, 140);
    public static final Color BORDER = new Color(220, 225, 230);

    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.gridColor", Color.WHITE);
        UIManager.put("Table.selectionBackground", new Color(220, 235, 255));
        UIManager.put("TableHeader.background", new Color(235, 238, 242));
        UIManager.put("TableHeader.foreground", TEXT);
        UIManager.put("Label.foreground", TEXT);
    }

   public static JButton primaryButton(String text) {
        JButton b = new JButton(text);

        b.setFocusPainted(false);
        b.setBorderPainted(false);          // opcional: evita borde del L&F
        b.setContentAreaFilled(true);       // IMPORTANTÍSIMO: pinta el fondo
        b.setOpaque(true);                  // IMPORTANTÍSIMO: respeta background

        b.setForeground(Color.WHITE);
        b.setBackground(BG);

        b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return b;
    }

    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);

        b.setFocusPainted(false);
        b.setContentAreaFilled(true);       // para que el blanco se pinte bien
        b.setOpaque(true);

        b.setForeground(TEXT);
        b.setBackground(Color.gray);

        b.setBorder(BorderFactory.createLineBorder(BORDER));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return b;
    }


    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(26, new Color(220, 225, 230)), // radio suave
            BorderFactory.createEmptyBorder(26, 26, 26, 26)
        ));
        return p;
    }

}
