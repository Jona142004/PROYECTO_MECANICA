package ui;

import ui.login.LoginFrame;
import ui.theme.UITheme;
import javax.swing.SwingUtilities;

public class AppLauncher {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {            
            UITheme.apply();
            new LoginFrame().setVisible(true);
        });
    }
}
