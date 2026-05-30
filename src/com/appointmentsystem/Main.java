package com.appointmentsystem;

import com.appointmentsystem.ui.LoginFrame;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.focusColor", new java.awt.Color(142, 81, 255, 80));

            // ── Aesthetic scrollbar styling ──────────────────────────
            // Slim width
            UIManager.put("ScrollBar.width", 8);
            // Suppress default arrows and shadows
            UIManager.put("ScrollBar.thumbDarkShadow", new Color(0, 0, 0, 0));
            UIManager.put("ScrollBar.thumbShadow",     new Color(0, 0, 0, 0));
            UIManager.put("ScrollBar.thumbHighlight",  new Color(0, 0, 0, 0));
            // Soft purple thumb and near-invisible track
            UIManager.put("ScrollBar.thumb",  new Color(142, 81, 255, 110));
            UIManager.put("ScrollBar.track",  new Color(237, 234, 255, 70));
            UIManager.put("ScrollBar.trackHighlight", new Color(237, 234, 255, 70));

        } catch (Exception e) {
            // ignore if look-and-feel doesn't support these keys
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}