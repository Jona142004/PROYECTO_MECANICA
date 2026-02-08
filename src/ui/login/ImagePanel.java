package ui.login;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {

    private final Image background;

    public ImagePanel(String imageName) {
        background = new ImageIcon(
            getClass().getResource(imageName)
        ).getImage();

        setLayout(new GridBagLayout()); // clave para centrar contenido
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
