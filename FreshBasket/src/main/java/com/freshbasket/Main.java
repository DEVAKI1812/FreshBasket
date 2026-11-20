package com.freshbasket;

import com.formdev.flatlaf.FlatLightLaf;
import com.freshbasket.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
                UIManager.put("Button.arc", 10);
                UIManager.put("Component.arc", 10);
                UIManager.put("TextComponent.arc", 10);
                UIManager.put("ScrollBar.thumbArc", 999);
                UIManager.put("ScrollBar.width", 10);
                UIManager.put("ScrollBar.track", new Color(0xF3, 0xF4, 0xF6));
                UIManager.put("ScrollBar.thumb", new Color(0xCB, 0xD5, 0xE1));
            } catch (Exception ignored) {
            }
            new LoginFrame().setVisible(true);
        });
    }
}
