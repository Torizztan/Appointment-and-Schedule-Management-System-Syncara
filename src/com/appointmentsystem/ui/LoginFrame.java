package com.appointmentsystem.ui;

import appointmentsystem.dao.UserDAO;
import com.appointmentsystem.model.User;
import com.appointmentsystem.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private static final Color PRIMARY       = new Color(142, 81, 255);
    private static final Color PRIMARY_DARK  = new Color(110, 55, 220);
    private static final Color BG_LIGHT      = new Color(248, 247, 255);
    private static final Color TEXT_DARK     = new Color(22, 22, 30);
    private static final Color TEXT_GRAY     = new Color(120, 120, 135);
    private static final Color BORDER_COLOR  = new Color(220, 215, 240);
    private static final Color ERROR_COLOR   = new Color(220, 38, 38);
    private static final Color SUCCESS_COLOR = new Color(22, 163, 74);

    private CardLayout cardLayout;
    private JPanel formCards;
    private UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        setTitle("Appointment and Schedule Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(940, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_LIGHT);

        JPanel contentPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));
                g2.dispose();
            }
        };
        contentPanel.setOpaque(false);

        contentPanel.add(createSidePanel(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        formCards = new JPanel(cardLayout);
        formCards.setOpaque(false);
        formCards.add(createLoginPanel(),    "login");
        formCards.add(createRegisterPanel(), "register");
        contentPanel.add(formCards, BorderLayout.CENTER);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    // ─────────────────────────── Side panel ────────────────────────────

    private JPanel createSidePanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, getWidth(), getHeight(), PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.CENTER;

        JLabel icon = new JLabel("\uD83D\uDCC5");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        gbc.gridy = 0; gbc.insets = new Insets(0, 30, 8, 30);
        panel.add(icon, gbc);

        JLabel appName = new JLabel("Syncara");
        appName.setFont(new Font("SansSerif", Font.BOLD, 26));
        appName.setForeground(Color.WHITE);
        gbc.gridy = 1; gbc.insets = new Insets(0, 30, 4, 30);
        panel.add(appName, gbc);

        var tagline = new JLabel("<html><center>Appointment and Schedule Management System</center></html>");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tagline.setForeground(new Color(255, 255, 255, 210));
        tagline.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2; gbc.insets = new Insets(0, 30, 24, 30);
        panel.add(tagline, gbc);

        String[] features = {
            "\u2713  Booking appointments",
            "\u2713  Tracking schedule",
            "\u2713  Real-time notifications"
        };
        for (int i = 0; i < features.length; i++) {
            JLabel feat = new JLabel(features[i]);
            feat.setFont(new Font("SansSerif", Font.PLAIN, 12));
            feat.setForeground(new Color(255, 255, 255, 175));
            gbc.gridy = 3 + i;
            gbc.insets = new Insets(3, 40, 3, 40);
            panel.add(feat, gbc);
        }

        return panel;
    }

    // ─────────────────────────── Login panel ────────────────────────────

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 64, 5, 64);

        JLabel titleLabel = new JLabel("Welcome back");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_DARK);
        gbc.gridy = 0; gbc.insets = new Insets(30, 64, 2, 64);
        panel.add(titleLabel, gbc);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_GRAY);
        gbc.gridy = 1; gbc.insets = new Insets(0, 64, 22, 64);
        panel.add(sub, gbc);

        panel.add(fieldLabel("Email address"), fieldLabelGbc(gbc, 2));
        JTextField emailField = createTextField("you@example.com");
        gbc.gridy = 3; gbc.insets = new Insets(0, 64, 14, 64);
        panel.add(emailField, gbc);

        panel.add(fieldLabel("Password"), fieldLabelGbc(gbc, 4));
        JPanel passWrapper = createPasswordWrapper("Enter your password");
        gbc.gridy = 5; gbc.insets = new Insets(0, 64, 6, 64);
        panel.add(passWrapper, gbc);

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        errorLabel.setForeground(ERROR_COLOR);
        gbc.gridy = 6; gbc.insets = new Insets(0, 64, 10, 64);
        panel.add(errorLabel, gbc);

        JButton loginBtn = createPrimaryButton("Sign In");
        loginBtn.addActionListener(e -> {
            String email    = emailField.getText().trim().toLowerCase();
            String password = getPasswordFromWrapper(passWrapper);
            if (email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }
            User user = userDAO.login(email, password);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                dispose();
                new MainFrame().setVisible(true);
            } else {
                errorLabel.setText("Invalid email or password.");
            }
        });
        gbc.gridy = 7; gbc.insets = new Insets(4, 64, 12, 64);
        panel.add(loginBtn, gbc);

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchRow.setOpaque(false);
        JLabel switchText = new JLabel("Don't have an account?");
        switchText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        switchText.setForeground(TEXT_GRAY);
        JButton switchBtn = linkButton("Create one");
        switchBtn.addActionListener(e -> cardLayout.show(formCards, "register"));
        switchRow.add(switchText);
        switchRow.add(switchBtn);
        gbc.gridy = 8; gbc.insets = new Insets(0, 64, 30, 64);
        panel.add(switchRow, gbc);

        return panel;
    }

    // ─────────────────────────── Register panel ───────────────────────────

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 64, 3, 64);

        JLabel titleLabel = new JLabel("Create account");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_DARK);
        gbc.gridy = 0; gbc.insets = new Insets(24, 64, 2, 64);
        panel.add(titleLabel, gbc);

        JLabel sub = new JLabel("Join us \u2014 it's free");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_GRAY);
        gbc.gridy = 1; gbc.insets = new Insets(0, 64, 18, 64);
        panel.add(sub, gbc);

        panel.add(fieldLabel("Full Name"), fieldLabelGbc(gbc, 2));
        JTextField nameField = createTextField("Your full name");
        gbc.gridy = 3; gbc.insets = new Insets(0, 64, 10, 64);
        panel.add(nameField, gbc);

        // ── Email field: accepts any valid email (not just Gmail) ──────
        panel.add(fieldLabel("Email address"), fieldLabelGbc(gbc, 4));
        JTextField emailField = createTextField("your@email.com");
        gbc.gridy = 5; gbc.insets = new Insets(0, 64, 10, 64);
        panel.add(emailField, gbc);

        panel.add(fieldLabel("Password"), fieldLabelGbc(gbc, 6));
        JPanel passWrapper = createPasswordWrapper("Min. 6 characters");
        gbc.gridy = 7; gbc.insets = new Insets(0, 64, 10, 64);
        panel.add(passWrapper, gbc);

        panel.add(fieldLabel("Role"), fieldLabelGbc(gbc, 8));
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"student", "teacher"});
        roleBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roleBox.setPreferredSize(new Dimension(0, 40));
        roleBox.setBackground(Color.WHITE);
        gbc.gridy = 9; gbc.insets = new Insets(0, 64, 12, 64);
        panel.add(roleBox, gbc);

        JLabel msgLabel = new JLabel(" ");
        msgLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        msgLabel.setForeground(ERROR_COLOR);
        gbc.gridy = 10; gbc.insets = new Insets(0, 64, 6, 64);
        panel.add(msgLabel, gbc);

        JButton registerBtn = createPrimaryButton("Create Account");
        registerBtn.addActionListener(e -> {
            String name     = nameField.getText().trim();
            // Normalise email: trim whitespace, lowercase
            String email    = emailField.getText().trim().toLowerCase();
            String password = getPasswordFromWrapper(passWrapper);
            String role     = (String) roleBox.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                msgLabel.setForeground(ERROR_COLOR);
                msgLabel.setText("Please fill in all fields.");
                return;
            }
            // Accept any valid email format (gmail, school .com.ph, .edu, etc.)
            if (!email.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
                msgLabel.setForeground(ERROR_COLOR);
                msgLabel.setText("Please enter a valid email address.");
                return;
            }
            if (password.length() < 6) {
                msgLabel.setForeground(ERROR_COLOR);
                msgLabel.setText("Password must be at least 6 characters.");
                return;
            }
            if (userDAO.register(name, email, password, role)) {
                msgLabel.setForeground(SUCCESS_COLOR);
                msgLabel.setText("Registered! You can now sign in.");
                nameField.setText(""); emailField.setText("");
                setPasswordInWrapper(passWrapper, "");
            } else {
                msgLabel.setForeground(ERROR_COLOR);
                msgLabel.setText("Email already exists.");
            }
        });
        gbc.gridy = 11; gbc.insets = new Insets(4, 64, 10, 64);
        panel.add(registerBtn, gbc);

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchRow.setOpaque(false);
        JLabel switchText = new JLabel("Already have an account?");
        switchText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        switchText.setForeground(TEXT_GRAY);
        JButton switchBtn = linkButton("Sign in");
        switchBtn.addActionListener(e -> cardLayout.show(formCards, "login"));
        switchRow.add(switchText);
        switchRow.add(switchBtn);
        gbc.gridy = 12; gbc.insets = new Insets(0, 64, 24, 64);
        panel.add(switchRow, gbc);

        return panel;
    }

    // ─────────────────────────── Helpers ────────────────────────────────

    private GridBagConstraints fieldLabelGbc(GridBagConstraints base, int gridy) {
        GridBagConstraints g = (GridBagConstraints) base.clone();
        g.gridy = gridy;
        g.insets = new Insets(4, 64, 2, 64);
        return g;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(170, 170, 185));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left + 2,
                            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(0, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.repaint(); }
            @Override public void focusLost (FocusEvent e) { field.repaint(); }
        });
        return field;
    }

    private JPanel createPasswordWrapper(String placeholder) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        wrapper.setPreferredSize(new Dimension(0, 42));

        JPasswordField passField = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(170, 170, 185));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, getInsets().left + 2,
                            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        passField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passField.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 4));
        passField.setOpaque(false);
        passField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { passField.repaint(); }
            @Override public void focusLost (FocusEvent e) { passField.repaint(); }
        });

        wrapper.putClientProperty("passField", passField);

        JButton eyeBtn = new JButton("\uD83D\uDC41") {
            boolean showing = false;
            {
                setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                setForeground(TEXT_GRAY);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(38, 42));
                addActionListener(e -> {
                    showing = !showing;
                    if (showing) {
                        passField.setEchoChar((char) 0);
                        setText("\uD83D\uDE48");
                    } else {
                        passField.setEchoChar('\u2022');
                        setText("\uD83D\uDC41");
                    }
                });
            }
        };

        wrapper.add(passField, BorderLayout.CENTER);
        wrapper.add(eyeBtn,   BorderLayout.EAST);
        return wrapper;
    }

    private String getPasswordFromWrapper(JPanel wrapper) {
        JPasswordField pf = (JPasswordField) wrapper.getClientProperty("passField");
        return pf == null ? "" : new String(pf.getPassword());
    }

    private void setPasswordInWrapper(JPanel wrapper, String text) {
        JPasswordField pf = (JPasswordField) wrapper.getClientProperty("passField");
        if (pf != null) pf.setText(text);
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? PRIMARY_DARK
                        : getModel().isRollover() ? new Color(160, 100, 255) : PRIMARY;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(0, 44));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(PRIMARY);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0));
        return btn;
    }
}
