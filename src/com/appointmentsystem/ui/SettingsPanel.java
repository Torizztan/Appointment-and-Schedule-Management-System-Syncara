package com.appointmentsystem.ui;

import appointmentsystem.dao.UserDAO;
import com.appointmentsystem.model.User;
import com.appointmentsystem.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SettingsPanel extends JPanel implements MainFrame.Refreshable {
    private UserDAO userDAO = new UserDAO();

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(MainFrame.BG_LIGHT);
        buildUI();
    }

    private void buildUI() {
        removeAll();

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(MainFrame.BG_LIGHT);
        mainContent.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(MainFrame.TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Manage your account settings");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(MainFrame.TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(subtitleLabel);
        mainContent.add(Box.createVerticalStrut(24));

        User currentUser = SessionManager.getCurrentUser();
        User freshUser = userDAO.getUserById(currentUser.getId());
        if (freshUser != null) {
            currentUser = freshUser;
            SessionManager.setCurrentUser(freshUser);
        }

        JPanel profileCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        profileCard.setOpaque(false);
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.setMaximumSize(new Dimension(500, 500));

        JLabel cardTitle = new JLabel("Account Settings");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardTitle.setForeground(MainFrame.PRIMARY);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.add(cardTitle);
        profileCard.add(Box.createVerticalStrut(20));

        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLabel.setForeground(MainFrame.TEXT_DARK);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.add(nameLabel);
        profileCard.add(Box.createVerticalStrut(4));

        JTextField nameField = new JTextField(currentUser.getName());
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        profileCard.add(nameField);
        profileCard.add(Box.createVerticalStrut(16));

        JLabel emailLabel = new JLabel("Email (Cannot be changed)");
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        emailLabel.setForeground(MainFrame.TEXT_DARK);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.add(emailLabel);
        profileCard.add(Box.createVerticalStrut(4));

        JTextField emailField = new JTextField(currentUser.getEmail());
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setEnabled(false);
        emailField.setBackground(new Color(245, 245, 245));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        profileCard.add(emailField);
        profileCard.add(Box.createVerticalStrut(16));

        JLabel roleLabel = new JLabel("Role");
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        roleLabel.setForeground(MainFrame.TEXT_DARK);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.add(roleLabel);
        profileCard.add(Box.createVerticalStrut(4));

        String roleText = currentUser.getRole().substring(0, 1).toUpperCase() + currentUser.getRole().substring(1);
        JTextField roleField = new JTextField(roleText);
        roleField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        roleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleField.setEnabled(false);
        roleField.setBackground(new Color(245, 245, 245));
        roleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        profileCard.add(roleField);
        profileCard.add(Box.createVerticalStrut(16));

        JLabel passLabel = new JLabel("New Password (Leave blank to keep current)");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLabel.setForeground(MainFrame.TEXT_DARK);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.add(passLabel);
        profileCard.add(Box.createVerticalStrut(4));

        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        profileCard.add(passField);
        profileCard.add(Box.createVerticalStrut(8));

        JLabel msgLabel = new JLabel(" ");
        msgLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        msgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.add(msgLabel);
        profileCard.add(Box.createVerticalStrut(8));

        JButton saveBtn = new JButton("Save Changes") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(MainFrame.PRIMARY.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(MainFrame.PRIMARY.brighter());
                } else {
                    g2.setColor(MainFrame.PRIMARY);
                }
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setBorderPainted(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newPass = new String(passField.getPassword());
            if (newName.isEmpty()) {
                msgLabel.setForeground(MainFrame.ERROR);
                msgLabel.setText("Name cannot be empty.");
                return;
            }
            boolean success = userDAO.updateUser(SessionManager.getCurrentUser().getId(), newName, newPass);
            if (success) {
                User updatedUser = userDAO.getUserById(SessionManager.getCurrentUser().getId());
                if (updatedUser != null) {
                    SessionManager.setCurrentUser(updatedUser);
                }
                msgLabel.setForeground(MainFrame.SUCCESS);
                msgLabel.setText("Profile updated successfully!");
                passField.setText("");
            } else {
                msgLabel.setForeground(MainFrame.ERROR);
                msgLabel.setText("Error updating profile.");
            }
        });
        profileCard.add(saveBtn);

        mainContent.add(profileCard);

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    @Override
    public void refresh() {
        buildUI();
    }
}
