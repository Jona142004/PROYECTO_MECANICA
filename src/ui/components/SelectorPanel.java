package ui.components;

import ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

public class SelectorPanel extends JPanel {

    private final JTextField field;
    private final JButton lupa;
    private Runnable onSearch;

    public SelectorPanel(String placeholder) {
        setLayout(new BorderLayout(6, 0));
        setOpaque(false);

        field = new JTextField(placeholder);
        field.setEditable(false);
        field.setBackground(Color.WHITE);

        lupa = UITheme.primaryButton("🔍");
        lupa.setPreferredSize(new Dimension(46, 28));

        lupa.addActionListener(e -> {
            // ✅ si está deshabilitado, no hace nada
            if (!isEnabled()) return;

            if (onSearch != null) {
                onSearch.run();
            }
        });

        add(field, BorderLayout.CENTER);
        add(lupa, BorderLayout.EAST);
    }

    public void setOnSearch(Runnable onSearch) {
        this.onSearch = onSearch;
    }

    public void setText(String value) {
        field.setText(value);
    }

    public String getText() {
        return field.getText();
    }

    // ✅ Centralizamos el enable/disable para todo el panel
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        field.setEnabled(enabled);
        lupa.setEnabled(enabled);

        // opcional: look más claro cuando está deshabilitado
        field.setBackground(enabled ? Color.WHITE : new Color(235, 238, 242));
    }
}
