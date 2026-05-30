package com.appointmentsystem.ui;

import appointmentsystem.dao.AppointmentDAO;
import appointmentsystem.dao.UserDAO;
import appointmentsystem.dao.NotificationDAO;
import com.appointmentsystem.model.Appointment;
import com.appointmentsystem.model.User;
import com.appointmentsystem.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;

public class DashboardPanel extends JPanel implements MainFrame.Refreshable {
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private UserDAO userDAO = new UserDAO();
    private JPanel contentArea;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(MainFrame.BG_LIGHT);
        buildUI();
    }

    private void buildUI() {
        if (contentArea != null) remove(contentArea);

        contentArea = new JPanel();
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setBackground(MainFrame.BG_LIGHT);
        contentArea.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        String role = SessionManager.getRole();
        User user = SessionManager.getCurrentUser();

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(MainFrame.TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(titleLabel);

        JLabel welcomeLabel = new JLabel("Welcome back, " + user.getName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        welcomeLabel.setForeground(MainFrame.TEXT_GRAY);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(welcomeLabel);
        contentArea.add(Box.createVerticalStrut(20));

        if ("admin".equals(role)) {
            buildAdminDashboard();
        } else if ("teacher".equals(role)) {
            buildTeacherDashboard();
        } else {
            buildStudentDashboard();
        }

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(MainFrame.BG_LIGHT);
        MainFrame.styleScrollPane(scroll);
        add(scroll, BorderLayout.CENTER);
    }

    // ─── Admin dashboard ─────────────────────────────────────────────────

    private void buildAdminDashboard() {
        JPanel statsRow = new JPanel(new GridLayout(1, 6, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.add(createStatCard("Total Users",  String.valueOf(userDAO.getUserCount()),                          MainFrame.PRIMARY));
        statsRow.add(createStatCard("Pending",      String.valueOf(appointmentDAO.getCountByStatus("pending")),      MainFrame.WARNING));
        statsRow.add(createStatCard("Approved",     String.valueOf(appointmentDAO.getCountByStatus("approved")),     MainFrame.SUCCESS));
        statsRow.add(createStatCard("Completed",    String.valueOf(appointmentDAO.getCountByStatus("completed")),    new Color(0, 150, 137)));
        statsRow.add(createStatCard("Rejected",     String.valueOf(appointmentDAO.getCountByStatus("rejected")),     MainFrame.ERROR));
        statsRow.add(createStatCard("Cancelled",    String.valueOf(appointmentDAO.getCountByStatus("cancelled")),    MainFrame.TEXT_GRAY));
        contentArea.add(statsRow);
        contentArea.add(Box.createVerticalStrut(20));

        List<Appointment> appointments = appointmentDAO.getAllAppointments();
        SimpleDateFormat dateFmt = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");

        JPanel bookingsCard = createCard("All Bookings");
        String[] cols = {"Student", "Teacher", "Date", "Time", "Status", "Action"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Appointment a : appointments) {
            model.addRow(new Object[]{a.getStudentName(), a.getTeacherName(),
                    dateFmt.format(a.getDate()), timeFmt.format(a.getTime()), a.getStatus(), "Delete"});
        }
        JTable table = createStyledTable(model);
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        addDeleteMouseListener(table, model, appointments);
        bookingsCard.add(styledTableScroll(table, 250));
        contentArea.add(bookingsCard);
        contentArea.add(Box.createVerticalStrut(20));

        List<User> users = userDAO.getAllUsers();
        JPanel usersCard = createCard("User Management");
        String[] userCols = {"Name", "Email", "Role", "Action"};
        DefaultTableModel userModel = new DefaultTableModel(userCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (User u : users) {
            userModel.addRow(new Object[]{u.getName(), u.getEmail(),
                    u.getRole().substring(0, 1).toUpperCase() + u.getRole().substring(1),
                    u.getId() == SessionManager.getCurrentUser().getId() ? "(You)" : "Delete"});
        }
        JTable userTable = createStyledTable(userModel);
        userTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        addUserDeleteMouseListener(userTable, userModel, users);
        usersCard.add(styledTableScroll(userTable, 200));
        contentArea.add(usersCard);
    }

    // ─── Teacher dashboard ────────────────────────────────────────────────

    private void buildTeacherDashboard() {
        int teacherId = SessionManager.getCurrentUser().getId();
        List<Appointment> upcoming = appointmentDAO.getTeacherUpcomingAppointments(teacherId);
        List<Appointment> past     = appointmentDAO.getTeacherPastAppointments(teacherId);
        long pendingCount = upcoming.stream().filter(a -> "pending".equals(a.getStatus())).count();

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.add(createStatCard("Upcoming", String.valueOf(upcoming.size()), MainFrame.PRIMARY));
        statsRow.add(createStatCard("Pending",  String.valueOf(pendingCount),    MainFrame.WARNING));
        statsRow.add(createStatCard("Past",     String.valueOf(past.size()),     MainFrame.TEXT_GRAY));
        contentArea.add(statsRow);
        contentArea.add(Box.createVerticalStrut(20));

        SimpleDateFormat dateFmt = new SimpleDateFormat("MMM dd");
        SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");

        JPanel upcomingCard = createCard("Upcoming Student Requests");

        // Sort control for teacher
        JPanel sortRow = buildSortRow(upcoming, upcomingCard, dateFmt, timeFmt, "teacher");
        upcomingCard.add(sortRow);
        upcomingCard.add(Box.createVerticalStrut(8));

        String[] columns = {"Student", "Date", "Time", "Status", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Appointment a : upcoming) {
            model.addRow(new Object[]{a.getStudentName(),
                    dateFmt.format(a.getDate()), timeFmt.format(a.getTime()), a.getStatus(), "Manage"});
        }
        JTable table = createStyledTable(model);
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        addTeacherManageMouseListener(table, upcoming);
        upcomingCard.add(styledTableScroll(table, 220));
        contentArea.add(upcomingCard);
        contentArea.add(Box.createVerticalStrut(20));

        JPanel pastCard = createCard("Past Appointments");
        String[] pastCols = {"Student", "Date", "Time", "Status", "Remarks"};
        DefaultTableModel pastModel = new DefaultTableModel(pastCols, 0);
        for (Appointment a : past) {
            pastModel.addRow(new Object[]{a.getStudentName(),
                    dateFmt.format(a.getDate()), timeFmt.format(a.getTime()),
                    a.getStatus(), a.getNotes() != null ? a.getNotes() : "No remarks"});
        }
        JTable pastTable = createStyledTable(pastModel);
        pastTable.setEnabled(false);
        pastCard.add(styledTableScroll(pastTable, 180));
        contentArea.add(pastCard);
    }

    // ─── Student dashboard ────────────────────────────────────────────────

    private void buildStudentDashboard() {
        int studentId = SessionManager.getCurrentUser().getId();
        List<Appointment> appointments = appointmentDAO.getStudentAppointments(studentId);
        long pendingCount  = appointments.stream().filter(a -> "pending".equals(a.getStatus())).count();
        long approvedCount = appointments.stream().filter(a -> "approved".equals(a.getStatus())).count();

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.add(createStatCard("Total",    String.valueOf(appointments.size()), MainFrame.PRIMARY));
        statsRow.add(createStatCard("Pending",  String.valueOf(pendingCount),        MainFrame.WARNING));
        statsRow.add(createStatCard("Approved", String.valueOf(approvedCount),       MainFrame.SUCCESS));
        contentArea.add(statsRow);
        contentArea.add(Box.createVerticalStrut(20));

        JPanel card = createCard("My Appointments");

        SimpleDateFormat dateFmt = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");

        // ── Sort control ─────────────────────────────────────────────
        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        sortRow.setOpaque(false);
        sortRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sortLbl = new JLabel("Sort:");
        sortLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortLbl.setForeground(MainFrame.TEXT_GRAY);
        String[] sortOptions = {"Upcoming First", "Latest Added"};
        JComboBox<String> sortBox = new JComboBox<>(sortOptions);
        sortBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortBox.setBackground(Color.WHITE);
        sortRow.add(sortLbl);
        sortRow.add(sortBox);
        card.add(sortRow);
        card.add(Box.createVerticalStrut(8));

        // ── Table model ───────────────────────────────────────────────
        String[] columns = {"Teacher", "Date", "Time", "Status", "Remarks", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        // Use a mutable sorted list
        List<Appointment> sorted = new ArrayList<>(appointments);
        // Default: upcoming first
        sorted.sort(Comparator.comparing(Appointment::getDate));

        Runnable fillModel = () -> {
            model.setRowCount(0);
            for (Appointment a : sorted) {
                model.addRow(new Object[]{
                        a.getTeacherName(),
                        dateFmt.format(a.getDate()),
                        timeFmt.format(a.getTime()),
                        a.getStatus(),
                        a.getNotes() != null ? a.getNotes() : "None",
                        "pending".equals(a.getStatus()) ? "Cancel" : ""
                });
            }
        };
        fillModel.run();

        JTable table = createStyledTable(model);
        // Use CancelButtonRenderer — draws outlined box only when text is "Cancel", nothing when empty
        table.getColumn("Action").setCellRenderer(new CancelButtonRenderer());

        // ── Precise mouse-click handler for Cancel button ─────────────
        final int ACTION_COL = 5;
        final int BTN_W = 70, BTN_H = 26;
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col != ACTION_COL || row < 0 || row >= sorted.size()) return;
                Object val = model.getValueAt(row, ACTION_COL);
                if (!"Cancel".equals(val)) return;

                // Check that the click is inside the visible outlined button area
                Rectangle cellRect = table.getCellRect(row, col, false);
                int bx = cellRect.x + (cellRect.width  - BTN_W) / 2;
                int by = cellRect.y + (cellRect.height - BTN_H) / 2;
                if (!new Rectangle(bx, by, BTN_W, BTN_H).contains(e.getPoint())) return;

                Appointment a = sorted.get(row);
                if (!"pending".equals(a.getStatus())) return;
                int confirm = JOptionPane.showConfirmDialog(DashboardPanel.this,
                        "Cancel this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    appointmentDAO.cancelAppointment(a.getId(), SessionManager.getCurrentUser().getId());
                    refresh();
                }
            }
        });

        // ── Wire sort ComboBox ────────────────────────────────────────
        sortBox.addActionListener(e -> {
            if ("Upcoming First".equals(sortBox.getSelectedItem())) {
                sorted.sort(Comparator.comparing(Appointment::getDate)
                        .thenComparing(Appointment::getTime));
            } else {
                // Latest Added: sort by date descending (most recent date first)
                sorted.sort(Comparator.comparing(Appointment::getDate).reversed()
                        .thenComparing(Comparator.comparing(Appointment::getTime).reversed()));
            }
            fillModel.run();
            table.repaint();
        });

        card.add(styledTableScroll(table, 340));
        contentArea.add(card);
    }

    // ─── Shared helpers ───────────────────────────────────────────────────

    private JPanel buildSortRow(List<Appointment> list, JPanel ownerCard,
                                SimpleDateFormat dateFmt, SimpleDateFormat timeFmt, String role) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel("Sort:");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(MainFrame.TEXT_GRAY);
        JComboBox<String> box = new JComboBox<>(new String[]{"Upcoming First", "Latest Added"});
        box.setFont(new Font("SansSerif", Font.PLAIN, 12));
        box.setBackground(Color.WHITE);
        row.add(lbl);
        row.add(box);
        return row;
    }

    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(225, 220, 245));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.setColor(accentColor);
                g2.fillRect(0, 10, 4, getHeight() - 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 16));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(MainFrame.TEXT_GRAY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 28));
        v.setForeground(MainFrame.TEXT_DARK);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createVerticalStrut(6));
        card.add(v);
        return card;
    }

    JPanel createCard(String title) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(220, 215, 240));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbl.setForeground(MainFrame.PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(12));
        return card;
    }

    JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        
        // Ensure the table paints entirely across the viewport background
        table.setFillsViewportHeight(true); 
        table.setRowHeight(42);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        // Full grid — horizontal + vertical lines
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(230, 225, 250));
        
        table.setSelectionBackground(new Color(245, 243, 255));
        table.setSelectionForeground(MainFrame.TEXT_DARK);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(Color.WHITE);
        header.setForeground(MainFrame.TEXT_GRAY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, MainFrame.PRIMARY));
        header.setPreferredSize(new Dimension(0, 36));

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals("Status")) {
                table.getColumnModel().getColumn(i).setCellRenderer(new StatusCellRenderer());
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                        return this;
                    }
                });
            }
        }
        return table;
    }

    private JScrollPane styledTableScroll(JTable table, int preferredHeight) {
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(0, preferredHeight));
        sp.getVerticalScrollBar().setUnitIncrement(10);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Make sure the background behind the table is crisp white
        sp.getViewport().setBackground(Color.WHITE);
        
        // Call your main styler FIRST
        MainFrame.styleScrollPane(sp);
        
        // CRITICAL FIX: Apply the outer border AFTER styling, so it doesn't get erased!
        // This is what wraps the far right of the table nicely.
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 225, 250), 1));
        
        return sp;
    }

    @Override
    public void refresh() {
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    // ─── Mouse listeners ───────────────────────────────────────────────────

    private void addDeleteMouseListener(JTable table, DefaultTableModel model, List<Appointment> list) {
        int actionCol = model.getColumnCount() - 1;
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col != actionCol || row < 0 || row >= list.size()) return;
                if (!"Delete".equals(model.getValueAt(row, actionCol))) return;
                int c = JOptionPane.showConfirmDialog(DashboardPanel.this,
                        "Delete this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    appointmentDAO.deleteAppointment(list.get(row).getId());
                    refresh();
                }
            }
        });
    }

    private void addUserDeleteMouseListener(JTable table, DefaultTableModel model, List<User> users) {
        int actionCol = model.getColumnCount() - 1;
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col != actionCol || row < 0 || row >= users.size()) return;
                Object val = model.getValueAt(row, actionCol);
                if (!"Delete".equals(val)) return;
                User u = users.get(row);
                if (u.getId() == SessionManager.getCurrentUser().getId()) return;
                int c = JOptionPane.showConfirmDialog(DashboardPanel.this,
                        "Delete user '" + u.getName() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    userDAO.deleteUser(u.getId());
                    refresh();
                }
            }
        });
    }

    private void addTeacherManageMouseListener(JTable table, List<Appointment> list) {
        int actionCol = table.getColumnCount() - 1;
        NotificationDAO notifDAO = new NotificationDAO();
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col != actionCol || row < 0 || row >= list.size()) return;
                Appointment a = list.get(row);
                showTeacherActionDialog(a, (NotificationDAO) notifDAO);
            }
        });
    }

    private void showTeacherActionDialog(Appointment a,
            NotificationDAO notifDAO) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Manage Appointment", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(380, 260);
        dialog.setLocationRelativeTo(this);
        JPanel dp = new JPanel();
        dp.setLayout(new BoxLayout(dp, BoxLayout.Y_AXIS));
        dp.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        dp.setBackground(Color.WHITE);
        JLabel titleLbl = new JLabel("Update: " + a.getStudentName());
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLbl.setForeground(MainFrame.PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        dp.add(titleLbl);
        dp.add(Box.createVerticalStrut(15));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"approved", "rejected", "completed"});
        statusBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        statusBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        dp.add(new JLabel("Status:")); dp.add(statusBox);
        dp.add(Box.createVerticalStrut(10));
        JTextField notesField = new JTextField();
        notesField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        notesField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        notesField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dp.add(new JLabel("Remarks:")); dp.add(notesField);
        dp.add(Box.createVerticalStrut(15));
        JButton saveBtn = new JButton("Update");
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(MainFrame.PRIMARY);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        saveBtn.addActionListener(ev -> {
            String ns = (String) statusBox.getSelectedItem();
            String notes = notesField.getText().trim();
            appointmentDAO.updateStatus(a.getId(), ns, notes);
            String msg = "Your appointment on " + a.getDate() + " has been " + ns
                    + (notes.isEmpty() ? "" : ". Remarks: " + notes);
            notifDAO.addNotification(a.getStudentId(), msg);
            dialog.dispose();
            refresh();
        });
        dp.add(saveBtn);
        dialog.setContentPane(dp);
        dialog.setVisible(true);
    }

    // ─── Cell renderers ───────────────────────────────────────────────────

    static class StatusCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private String text = "";
        private Color badgeBg = Color.WHITE;
        private Color badgeFg = Color.BLACK;

        StatusCellRenderer() { setOpaque(true); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            String val = value != null ? value.toString().toLowerCase() : "";
            
            if (val.contains("pending") || val.contains("awaiting")) {
                text = "Pending / Awaiting Approval";
                badgeBg = new Color(255, 243, 205);
                badgeFg = new Color(133, 100, 4);
            } else if (val.contains("approved") || val.contains("confirmed")) {
                text = "Confirmed";
                badgeBg = new Color(212, 237, 218);
                badgeFg = new Color(21, 87, 36);
            } else if (val.contains("rejected")) {
                text = "Rejected";
                badgeBg = new Color(248, 215, 218);
                badgeFg = new Color(114, 28, 36);
            } else if (val.contains("cancelled")) {
                text = "Cancelled";
                badgeBg = new Color(226, 227, 229);
                badgeFg = new Color(56, 61, 65);
            } else if (val.contains("completed")) {
                text = "Completed";
                badgeBg = new Color(209, 236, 241);
                badgeFg = new Color(12, 84, 96);
            } else {
                text = value != null ? value.toString() : "";
                badgeBg = new Color(240, 240, 240);
                badgeFg = Color.DARK_GRAY;
            }

            setBackground(isSelected ? new Color(245, 243, 255) : Color.WHITE);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (text == null || text.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();

            int paddingX = 12;
            int pillWidth = fm.stringWidth(text) + (paddingX * 2);
            int pillHeight = 24;

            int px = 8;
            int py = (getHeight() - pillHeight) / 2;

            g2.setColor(badgeBg);
            g2.fillRoundRect(px, py, pillWidth, pillHeight, pillHeight, pillHeight);

            g2.setColor(badgeFg);
            g2.drawString(text, px + paddingX, py + (pillHeight - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    static class CancelButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private String text = "";
        private static final int BTN_W = 70, BTN_H = 26;

        CancelButtonRenderer() { 
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            text = value != null ? value.toString() : "";
            setBackground(isSelected ? new Color(245, 243, 255) : Color.WHITE);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (text == null || text.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int px = (getWidth()  - BTN_W) / 2;
            int py = (getHeight() - BTN_H) / 2;
            
            g2.setColor(new Color(255, 240, 240));
            g2.fillRoundRect(px, py, BTN_W, BTN_H, 8, 8);
            
            g2.setColor(MainFrame.ERROR);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(px, py, BTN_W, BTN_H, 8, 8);
            
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(MainFrame.ERROR);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, px + (BTN_W - fm.stringWidth(text)) / 2,
                    py + (BTN_H - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    static class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private String text = "";
        private static final int BTN_W = 70, BTN_H = 26;

        ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            text = value != null ? value.toString() : "";
            setBackground(isSelected ? new Color(245, 243, 255) : Color.WHITE);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (text == null || text.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int px = (getWidth()  - BTN_W) / 2;
            int py = (getHeight() - BTN_H) / 2;
            
            g2.setColor(new Color(240, 240, 250));
            g2.fillRoundRect(px, py, BTN_W, BTN_H, 8, 8);
            
            g2.setColor(MainFrame.PRIMARY);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(px, py, BTN_W, BTN_H, 8, 8);
            
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(MainFrame.PRIMARY);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, px + (BTN_W - fm.stringWidth(text)) / 2,
                    py + (BTN_H - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}