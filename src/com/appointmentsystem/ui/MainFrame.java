package com.appointmentsystem.ui;

import com.appointmentsystem.util.SessionManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class MainFrame extends JFrame {
    // ─── Design tokens ─────────────────────────────────────────────────────
    public static final Color PRIMARY       = new Color(142, 81, 255);
    public static final Color PRIMARY_DARK  = new Color(110, 55, 220);
    public static final Color PRIMARY_LIGHT = new Color(237, 230, 255);
    public static final Color BG_LIGHT      = new Color(248, 247, 255);
    public static final Color TEXT_DARK     = new Color(22, 22, 30);
    public static final Color TEXT_GRAY     = new Color(120, 120, 135);
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color SUCCESS       = new Color(22, 163, 74);
    public static final Color ERROR         = new Color(220, 38, 38);
    public static final Color WARNING       = new Color(234, 139, 0);
    public static final Color BORDER        = new Color(225, 220, 245);

    private static final int SIDEBAR_W = 240;

    private CardLayout contentLayout;
    private JPanel     contentPanel;
    private JPanel     sidebarPanel;
    private String     activeItem = "Dashboard";

    public MainFrame() {
        setTitle("Appointment and Schedule Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 660));
        // Start maximized (fullscreen window)
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        applyGlobalScrollbarStyle();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_LIGHT);

        sidebarPanel = createSidebar();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(BG_LIGHT);

        contentPanel.add(new DashboardPanel(),    "Dashboard");
        contentPanel.add(new CalendarPanel(),     "Calendar");
        contentPanel.add(new BookingPanel(),      "Bookings");
        contentPanel.add(new NotificationPanel(), "Notifications");
        contentPanel.add(new SettingsPanel(),     "Settings");

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void applyGlobalScrollbarStyle() {
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("ScrollBar.thumbDarkShadow", new Color(0, 0, 0, 0));
        UIManager.put("ScrollBar.thumb",           new Color(142, 81, 255, 100));
        UIManager.put("ScrollBar.thumbShadow",     new Color(0, 0, 0, 0));
        UIManager.put("ScrollBar.thumbHighlight",  new Color(0, 0, 0, 0));
        UIManager.put("ScrollBar.track",           new Color(237, 234, 255, 60));
        UIManager.put("ScrollBar.trackHighlight",  new Color(237, 234, 255, 60));
    }

    public static void styleScrollPane(JScrollPane sp) {
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(142, 81, 255, 100);
                trackColor = new Color(237, 234, 255, 80);
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setBorder(BorderFactory.createEmptyBorder());
                return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDragging ? new Color(142, 81, 255, 180) : thumbColor);
                g2.fillRoundRect(r.x + 1, r.y + 2, r.width - 2, r.height - 4, 8, 8);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRoundRect(r.x + 2, r.y, r.width - 4, r.height, 6, 6);
                g2.dispose();
            }
        });
        vsb.setPreferredSize(new Dimension(8, Integer.MAX_VALUE));
        vsb.setOpaque(false);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, 0, getHeight(), PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setOpaque(false);

        // ── TOP Panel ────────────────────────────────────────────────────────
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(24, 0, 12, 0)); // No side paddings here

        // Logo Row
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoRow.setOpaque(false);
        logoRow.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        logoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT); // Uniform alignment
        
        JLabel logoIcon = new JLabel("\uD83D\uDCC5");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        logoRow.add(logoIcon);
        JLabel logoText = new JLabel("Syncara");
        logoText.setFont(new Font("SansSerif", Font.BOLD, 18));
        logoText.setForeground(Color.WHITE);
        logoRow.add(logoText);
        
        top.add(logoRow);
        top.add(Box.createVerticalStrut(20));

        // ── User Card Section ────────────────────────────────────────────────
        JPanel userCard = createUserCard();
        top.add(userCard);
        top.add(Box.createVerticalStrut(28));

        // Section Label
        JLabel menuSection = new JLabel("NAVIGATION");
        menuSection.setFont(new Font("SansSerif", Font.BOLD, 10));
        menuSection.setForeground(new Color(255, 255, 255, 100));
        menuSection.setAlignmentX(Component.LEFT_ALIGNMENT); // Uniform alignment
        menuSection.setBorder(BorderFactory.createEmptyBorder(0, 22, 8, 16));
        top.add(menuSection);

        // Navigation Items
        String[][] items = {
            {"Dashboard",     "\u25A3"},
            {"Calendar",      "\uD83D\uDCC5"},
            {"Bookings",      "\uD83D\uDCCB"},
            {"Notifications", "\uD83D\uDD14"},
            {"Settings",      "\u2699"}
        };
        for (String[] item : items) {
            JPanel navItem = createNavItem(item[0], item[1]);
            navItem.setAlignmentX(Component.LEFT_ALIGNMENT); // Uniform alignment
            top.add(navItem);
            top.add(Box.createVerticalStrut(3));
        }

        sidebar.add(top, BorderLayout.NORTH);

        // ── BOTTOM Panel: Logout ─────────────────────────────────────────────
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 16, 24, 16));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 40));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottom.add(sep);
        bottom.add(Box.createVerticalStrut(12));

        JButton logoutBtn = new JButton("\u2190   Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        logoutBtn.setForeground(new Color(255, 190, 190));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setMaximumSize(new Dimension(SIDEBAR_W - 32, 40));
        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        bottom.add(logoutBtn);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createUserCard() {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40)); 
                // Using flat square bounds (fillRect) to fill the width fully with sharp edges
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 74);
            }
            @Override public Dimension getPreferredSize() {
                return new Dimension(SIDEBAR_W, 74);
            }
            @Override public Dimension getMinimumSize() {
                return new Dimension(SIDEBAR_W, 74);
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT); // Match alignment with row items
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16)); // Internal content padding

        String name = SessionManager.getCurrentUser().getName();
        String initials = name.length() >= 1 ? String.valueOf(name.charAt(0)).toUpperCase() : "?";

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials,
                        (getWidth()  - fm.stringWidth(initials)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setMinimumSize(new Dimension(40, 40));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String role = SessionManager.getRole();
        String roleDisplay = role.substring(0, 1).toUpperCase() + role.substring(1);
        JLabel roleLbl = new JLabel(roleDisplay);
        roleLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        roleLbl.setForeground(new Color(255, 255, 255, 165));
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textCol.add(nameLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(roleLbl);

        card.add(avatar,  BorderLayout.WEST);
        card.add(textCol, BorderLayout.CENTER);
        return card;
    }

    private JPanel createNavItem(String name, String icon) {
        boolean isActive = name.equals(activeItem);

        JPanel item = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                if (isActive) {
                    g2.setColor(new Color(255, 255, 255, 45));
                } else if (hover) {
                    g2.setColor(new Color(255, 255, 255, 20));
                }
                if (isActive || hover)
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                if (isActive) {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 8, 3, getHeight() - 16, 3, 3);
                }
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(SIDEBAR_W, 42));
        item.setPreferredSize(new Dimension(SIDEBAR_W, 42));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        iconLbl.setForeground(isActive ? Color.WHITE : new Color(255, 255, 255, 170));
        iconLbl.setPreferredSize(new Dimension(22, 22));

        JLabel textLbl = new JLabel(name);
        textLbl.setFont(new Font("SansSerif", isActive ? Font.BOLD : Font.PLAIN, 13));
        textLbl.setForeground(isActive ? Color.WHITE : new Color(255, 255, 255, 185));

        item.add(iconLbl, BorderLayout.WEST);
        item.add(textLbl, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activeItem = name;
                contentLayout.show(contentPanel, name);
                refreshSidebar();
                refreshActivePanel();
            }
            @Override public void mouseEntered(MouseEvent e) {
                item.putClientProperty("hover", true); item.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                item.putClientProperty("hover", false); item.repaint();
            }
        });

        return item;
    }

    private void refreshSidebar() {
        Container parent = sidebarPanel.getParent();
        parent.remove(sidebarPanel);
        sidebarPanel = createSidebar();
        parent.add(sidebarPanel, BorderLayout.WEST);
        parent.revalidate();
        parent.repaint();
    }

    private void refreshActivePanel() {
        for (Component comp : contentPanel.getComponents()) {
            if (comp.isVisible() && comp instanceof Refreshable) {
                ((Refreshable) comp).refresh();
            }
        }
    }

    public interface Refreshable {
        void refresh();
    }
}