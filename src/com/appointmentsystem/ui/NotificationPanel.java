package com.appointmentsystem.ui;

import appointmentsystem.dao.NotificationDAO;
import com.appointmentsystem.model.Notification;
import com.appointmentsystem.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.List;

public class NotificationPanel extends JPanel implements MainFrame.Refreshable {
    private NotificationDAO notificationDAO = new NotificationDAO();
    private JPanel listPanel;

    public NotificationPanel() {
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

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Notifications");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(MainFrame.TEXT_DARK);

        int unread = notificationDAO.getUnreadCount(SessionManager.getCurrentUser().getId());
        JLabel badge = new JLabel(unread > 0 ? unread + " unread" : "All read");
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setForeground(unread > 0 ? MainFrame.PRIMARY : MainFrame.SUCCESS);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(badge, BorderLayout.EAST);
        mainContent.add(headerPanel);
        mainContent.add(Box.createVerticalStrut(8));

        JButton markAllBtn = new JButton("Mark all as read");
        markAllBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        markAllBtn.setForeground(MainFrame.PRIMARY);
        markAllBtn.setBorderPainted(false);
        markAllBtn.setContentAreaFilled(false);
        markAllBtn.setFocusPainted(false);
        markAllBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        markAllBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        markAllBtn.addActionListener(e -> {
            notificationDAO.markAllAsRead(SessionManager.getCurrentUser().getId());
            refresh();
        });
        mainContent.add(markAllBtn);
        mainContent.add(Box.createVerticalStrut(16));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        List<Notification> notifications = notificationDAO.getNotifications(SessionManager.getCurrentUser().getId());

        if (notifications.isEmpty()) {
            JPanel emptyCard = createOutlinedBox();
            emptyCard.setOpaque(false);
            emptyCard.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
            emptyCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));

            JLabel emptyIcon = new JLabel("\uD83D\uDD14", SwingConstants.CENTER);
            emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyLabel = new JLabel("No notifications yet");
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            emptyLabel.setForeground(MainFrame.TEXT_GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyCard.add(emptyIcon);
            emptyCard.add(Box.createVerticalStrut(10));
            emptyCard.add(emptyLabel);
            listPanel.add(emptyCard);
        } else {
            SimpleDateFormat dateFmt = new SimpleDateFormat("MMM dd, yyyy  h:mm a");
            for (Notification n : notifications) {
                // Each notification gets its own clearly separated card/box
                listPanel.add(createNotificationCard(n, dateFmt));
                listPanel.add(Box.createVerticalStrut(10));
            }
        }

        mainContent.add(listPanel);

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        MainFrame.styleScrollPane(scroll);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    /**
     * Each notification is wrapped in its own fully-outlined box so they are
     * visually distinct from one another.
     */
    private JPanel createNotificationCard(Notification n, SimpleDateFormat dateFmt) {
        boolean isUnread = !n.isRead();

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background fill
                g2.setColor(isUnread ? new Color(245, 243, 255) : Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                // Full visible outline on every card
                g2.setColor(isUnread ? new Color(190, 170, 245) : new Color(225, 220, 240));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Double(0.6, 0.6, getWidth() - 1.2, getHeight() - 1.2, 12, 12));
                // Unread accent bar
                if (isUnread) {
                    g2.setColor(MainFrame.PRIMARY);
                    g2.fillRect(0, 8, 4, getHeight() - 16);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel msgLabel = new JLabel("<html>" + n.getMessage() + "</html>");
        msgLabel.setFont(new Font("SansSerif", isUnread ? Font.BOLD : Font.PLAIN, 13));
        msgLabel.setForeground(MainFrame.TEXT_DARK);
        msgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel(dateFmt.format(n.getCreatedAt()));
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        timeLabel.setForeground(MainFrame.TEXT_GRAY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(msgLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(timeLabel);

        card.add(textPanel, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionsPanel.setOpaque(false);

        if (isUnread) {
            JButton readBtn = new JButton("Mark read");
            readBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
            readBtn.setForeground(MainFrame.PRIMARY);
            readBtn.setBorderPainted(false);
            readBtn.setContentAreaFilled(false);
            readBtn.setFocusPainted(false);
            readBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            readBtn.addActionListener(e -> {
                notificationDAO.markAsRead(n.getId());
                refresh();
            });
            actionsPanel.add(readBtn);
        }

        JButton deleteBtn = new JButton("X");
        deleteBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        deleteBtn.setForeground(MainFrame.TEXT_GRAY);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> {
            notificationDAO.deleteNotification(n.getId());
            refresh();
        });
        actionsPanel.add(deleteBtn);

        card.add(actionsPanel, BorderLayout.EAST);

        return card;
    }

    /** A plain white outlined panel — used for the "empty" state */
    private JPanel createOutlinedBox() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(225, 220, 240));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
    }

    @Override
    public void refresh() {
        buildUI();
    }
}
