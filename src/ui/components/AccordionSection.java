package ui.components;

import javax.swing.*;
import java.awt.*;

public class AccordionSection extends JPanel {

    private final JButton headerButton;
    private final JPanel itemsPanel;
    private boolean expanded = false;

    public AccordionSection(String title) {
        setLayout(new BorderLayout());
        setOpaque(false);

        /* ===== HEADER ===== */
        headerButton = new JButton(title + "  ▸");
        headerButton.setFocusPainted(false);
        headerButton.setBorderPainted(false);
        headerButton.setContentAreaFilled(false);
        headerButton.setOpaque(false);
        headerButton.setForeground(Color.WHITE);
        headerButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        headerButton.setHorizontalAlignment(SwingConstants.LEFT);
        headerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerButton.setIcon(null);                 // 🔴 evita iconos fantasmas
        headerButton.setMargin(new Insets(0, 0, 0, 0));
        headerButton.setBorder(
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );

        /* ===== ITEMS ===== */
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        itemsPanel.setVisible(false);
        itemsPanel.setBorder(
                BorderFactory.createEmptyBorder(2, 18, 2, 2) // 🔹 más compacto
        );

        add(headerButton, BorderLayout.NORTH);
        add(itemsPanel, BorderLayout.CENTER);
    }

    public JButton getHeaderButton() {
        return headerButton;
    }

    public JButton addItem(String text, Runnable onClick) {
        JButton item = new JButton(text);

        item.setFocusPainted(false);
        item.setBorderPainted(false);
        item.setContentAreaFilled(false);
        item.setOpaque(false);
        item.setIcon(null);
        item.setForeground(Color.WHITE);
        item.setFont(new Font("SansSerif", Font.PLAIN, 13));
        item.setHorizontalAlignment(SwingConstants.LEFT);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setMargin(new Insets(0, 0, 0, 0));
        item.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        item.addActionListener(e -> onClick.run());
        itemsPanel.add(item);

        return item; 
    }

    public void setExpanded(boolean value) {
        expanded = value;
        itemsPanel.setVisible(expanded);
        headerButton.setText(getTitleWithArrow());
        revalidate();
        repaint();
    }

    public boolean isExpanded() {
        return expanded;
    }

    private String getTitleWithArrow() {
        String txt = headerButton.getText();
        String title = txt.replace("▸", "").replace("▾", "").trim();
        return expanded ? (title + "  ▾") : (title + "  ▸");
    }
}
