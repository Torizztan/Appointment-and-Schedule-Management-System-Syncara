package com.appointmentsystem.ui;

import appointmentsystem.dao.AppointmentDAO;
import appointmentsystem.dao.AvailabilityDAO;
import appointmentsystem.dao.NotificationDAO;
import appointmentsystem.dao.UserDAO;
import com.appointmentsystem.model.Appointment;
import com.appointmentsystem.model.TeacherSlot;
import com.appointmentsystem.model.User;
import com.appointmentsystem.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class BookingPanel extends JPanel implements MainFrame.Refreshable {

    private AppointmentDAO  appointmentDAO  = new AppointmentDAO();
    private AvailabilityDAO availabilityDAO = new AvailabilityDAO();
    private UserDAO         userDAO         = new UserDAO();
    private NotificationDAO notificationDAO = new NotificationDAO();

    private String lastSortSelection = "Past First";

    // ── Status helpers ─────────────────────────────────────────────────────

    private static String statusDisplay(String raw) {
        if (raw == null) return "";
        switch (raw.toLowerCase()) {
            case "pending":   return "Pending / Awaiting Approval";
            case "approved":  return "Confirmed";
            case "completed": return "Completed";
            case "rejected":  return "Rejected";
            case "cancelled": return "Cancelled";
            default:          return raw;
        }
    }

    private static Color statusBgColor(String d) {
        if (d.startsWith("Pending")) return new Color(255, 243, 205);
        if (d.equals("Confirmed"))   return new Color(209, 250, 229);
        if (d.equals("Completed"))   return new Color(204, 245, 241);
        if (d.equals("Rejected"))    return new Color(254, 226, 226);
        if (d.equals("Cancelled"))   return new Color(241, 245, 249);
        return new Color(230, 230, 230);
    }

    private static Color statusFgColor(String d) {
        if (d.startsWith("Pending")) return new Color(146, 64,  14);
        if (d.equals("Confirmed"))   return new Color(6,   95,  70);
        if (d.equals("Completed"))   return new Color(19,  78,  74);
        if (d.equals("Rejected"))    return new Color(153, 27,  27);
        if (d.equals("Cancelled"))   return new Color(71,  85, 105);
        return Color.DARK_GRAY;
    }

    // ── Renderers ──────────────────────────────────────────────────────────

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            final String text = value == null ? "" : value.toString();
            JPanel pill = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected ? statusBgColor(text).darker() : statusBgColor(text));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            pill.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            lbl.setForeground(statusFgColor(text));
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            pill.add(lbl);
            JPanel wrap = new JPanel(new GridBagLayout());
            wrap.setOpaque(true);
            wrap.setBackground(isSelected ? table.getSelectionBackground()
                    : (row % 2 == 0 ? Color.WHITE : new Color(252, 251, 255)));
            wrap.add(pill);
            return wrap;
        }
    }

    static class CancelButtonRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JPanel wrap = new JPanel(new GridBagLayout());
            wrap.setOpaque(true);
            wrap.setBackground(isSelected ? table.getSelectionBackground()
                    : (row % 2 == 0 ? Color.WHITE : new Color(252, 251, 255)));
            if ("Cancel".equals(value)) {
                JButton btn = new JButton("Cancel") {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.setColor(new Color(220, 38, 38));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                btn.setFont(new Font("SansSerif", Font.BOLD, 11));
                btn.setForeground(new Color(220, 38, 38));
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setPreferredSize(new Dimension(70, 28));
                wrap.add(btn);
            }
            return wrap;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PANEL SETUP
    // ══════════════════════════════════════════════════════════════════════

    public BookingPanel() {
        setLayout(new BorderLayout());
        setBackground(MainFrame.BG_LIGHT);
        buildUI();
    }

    private void buildUI() {
        removeAll();

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(MainFrame.BG_LIGHT);
        main.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Bookings");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(MainFrame.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(title);

        JLabel sub = new JLabel("Manage your appointments");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(MainFrame.TEXT_GRAY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(sub);
        main.add(Box.createVerticalStrut(20));

        String role = SessionManager.getRole();

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        if ("student".equals(role)) {
            JButton bookBtn = createPrimaryButton("+ Book New Appointment");
            bookBtn.setPreferredSize(new Dimension(230, 42));
            bookBtn.addActionListener(e -> showBookingDialog());
            actionRow.add(bookBtn);
        } else if ("teacher".equals(role)) {
            JButton availBtn = createPrimaryButton("Manage Availability");
            availBtn.setPreferredSize(new Dimension(230, 42));
            availBtn.addActionListener(e -> showManageAvailabilityDialog());
            actionRow.add(availBtn);
        }
        main.add(actionRow);
        main.add(Box.createVerticalStrut(20));

        List<Appointment> appointments;
        String[] columns;
        if ("admin".equals(role)) {
            appointments = appointmentDAO.getAllAppointments();
            columns = new String[]{"Student","Teacher","Date","Time","Status","Notes"};
        } else if ("teacher".equals(role)) {
            int id = SessionManager.getCurrentUser().getId();
            appointments = appointmentDAO.getTeacherUpcomingAppointments(id);
            appointments.addAll(appointmentDAO.getTeacherPastAppointments(id));
            columns = new String[]{"Student","Date","Time","Status","Notes"};
        } else {
            appointments = appointmentDAO.getStudentAppointments(SessionManager.getCurrentUser().getId());
            columns = new String[]{"Teacher","Date","Time","Status","Notes","Action"};
        }

        long totalCount     = appointments.size();
        long pendingCount   = appointments.stream().filter(a -> "pending".equalsIgnoreCase(a.getStatus())).count();
        long approvedCount  = appointments.stream().filter(a -> "approved".equalsIgnoreCase(a.getStatus())).count();
        long completedCount = appointments.stream().filter(a -> "completed".equalsIgnoreCase(a.getStatus())).count();
        long rejectedCount  = appointments.stream().filter(a -> "rejected".equalsIgnoreCase(a.getStatus())).count();
        long cancelledCount = appointments.stream().filter(a -> "cancelled".equalsIgnoreCase(a.getStatus())).count();

        JPanel statsRow;
        if ("admin".equals(role)) {
            statsRow = new JPanel(new GridLayout(1, 6, 12, 0));
            statsRow.setOpaque(false);
            statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            statsRow.add(createStatCard("Total Bookings", String.valueOf(totalCount),     MainFrame.PRIMARY));
            statsRow.add(createStatCard("Pending",        String.valueOf(pendingCount),   MainFrame.WARNING));
            statsRow.add(createStatCard("Approved",       String.valueOf(approvedCount),  MainFrame.SUCCESS));
            statsRow.add(createStatCard("Completed",      String.valueOf(completedCount), new Color(0, 150, 137)));
            statsRow.add(createStatCard("Rejected",       String.valueOf(rejectedCount),  MainFrame.ERROR));
            statsRow.add(createStatCard("Cancelled",      String.valueOf(cancelledCount), MainFrame.TEXT_GRAY));
        } else {
            statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
            statsRow.setOpaque(false);
            statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            statsRow.add(createStatCard("Total",     String.valueOf(totalCount),     MainFrame.PRIMARY));
            statsRow.add(createStatCard("Pending",   String.valueOf(pendingCount),   MainFrame.WARNING));
            statsRow.add(createStatCard("Approved",  String.valueOf(approvedCount),  MainFrame.SUCCESS));
            statsRow.add(createStatCard("Completed", String.valueOf(completedCount), new Color(0, 150, 137)));
        }
        main.add(statsRow);
        main.add(Box.createVerticalStrut(24));
        main.add(createCardWithSort("All Bookings", appointments, columns));

        JScrollPane scroll = new JScrollPane(main);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(50);
        scroll.getVerticalScrollBar().setBlockIncrement(150);
        MainFrame.styleScrollPane(scroll);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ── All Bookings card ─────────────────────────────────────────────────

    private JPanel createCardWithSort(String cardTitle, List<Appointment> appointments, String[] columns) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18));
                g2.setColor(new Color(220, 215, 240));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth()-1, getHeight()-1, 18, 18));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel lbl = new JLabel(cardTitle);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbl.setForeground(MainFrame.PRIMARY);
        headerRow.add(lbl, BorderLayout.WEST);

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        sortPanel.setOpaque(false);
        JLabel sortLbl = new JLabel("Sort:");
        sortLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortLbl.setForeground(MainFrame.TEXT_GRAY);
        JComboBox<String> sortCombo = new JComboBox<>(new String[]{"Past First","Newest First","Status"});
        sortCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortCombo.setBackground(Color.WHITE);
        sortCombo.setPreferredSize(new Dimension(130, 28));
        sortCombo.setSelectedItem(lastSortSelection);
        sortPanel.add(sortLbl);
        sortPanel.add(sortCombo);
        headerRow.add(sortPanel, BorderLayout.EAST);
        card.add(headerRow);
        card.add(Box.createVerticalStrut(14));

        String role = SessionManager.getRole();
        SimpleDateFormat dateFmt = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Appointment a : appointments) {
            String display = statusDisplay(a.getStatus());
            if ("admin".equals(role)) {
                model.addRow(new Object[]{a.getStudentName(), a.getTeacherName(),
                        dateFmt.format(a.getDate()), timeFmt.format(a.getTime()),
                        display, a.getNotes() != null ? a.getNotes() : ""});
            } else if ("teacher".equals(role)) {
                model.addRow(new Object[]{a.getStudentName(),
                        dateFmt.format(a.getDate()), timeFmt.format(a.getTime()),
                        display, a.getNotes() != null ? a.getNotes() : ""});
            } else {
                model.addRow(new Object[]{a.getTeacherName(),
                        dateFmt.format(a.getDate()), timeFmt.format(a.getTime()),
                        display, a.getNotes() != null ? a.getNotes() : "",
                        "pending".equalsIgnoreCase(a.getStatus()) ? "Cancel" : ""});
            }
        }

        JTable table = createStyledTable(model);
        int statusCol = -1;
        for (int c = 0; c < columns.length; c++) if ("Status".equals(columns[c])) { statusCol = c; break; }
        if (statusCol >= 0) table.getColumnModel().getColumn(statusCol).setCellRenderer(new StatusBadgeRenderer());

        if ("student".equals(role)) {
            final int ACTION_COL = columns.length - 1;
            final int BTN_W = 70, BTN_H = 28;
            table.getColumn("Action").setCellRenderer(new CancelButtonRenderer());
            table.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (col != ACTION_COL || row < 0 || row >= appointments.size()) return;
                    if (!"Cancel".equals(model.getValueAt(row, ACTION_COL))) return;
                    Rectangle cr = table.getCellRect(row, col, false);
                    int bx = cr.x + (cr.width - BTN_W) / 2, by = cr.y + (cr.height - BTN_H) / 2;
                    if (!new Rectangle(bx, by, BTN_W, BTN_H).contains(e.getPoint())) return;
                    Appointment a = appointments.get(row);
                    if (!"pending".equalsIgnoreCase(a.getStatus())) return;
                    if (JOptionPane.showConfirmDialog(BookingPanel.this,
                            "Cancel this appointment?", "Confirm",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        appointmentDAO.cancelAppointment(a.getId(), SessionManager.getCurrentUser().getId());
                        refresh();
                    }
                }
            });
        }

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        final int dci = "admin".equals(role) ? 2 : 1;
        final int tci = "admin".equals(role) ? 3 : 2;
        final int sCi = "admin".equals(role) ? 4 : 3;
        Comparator<Object> dateComp = (o1, o2) -> {
            try { return new SimpleDateFormat("MMM dd, yyyy").parse(o1.toString())
                    .compareTo(new SimpleDateFormat("MMM dd, yyyy").parse(o2.toString())); }
            catch (Exception ex) { return 0; }
        };
        Comparator<Object> timeComp = (o1, o2) -> {
            try { return new SimpleDateFormat("h:mm a").parse(o1.toString())
                    .compareTo(new SimpleDateFormat("h:mm a").parse(o2.toString())); }
            catch (Exception ex) { return 0; }
        };
        sorter.setComparator(dci, dateComp);
        sorter.setComparator(tci, timeComp);

        java.util.function.Consumer<SortOrder> applyDateSort = order ->
                sorter.setSortKeys(List.of(new RowSorter.SortKey(dci, order), new RowSorter.SortKey(tci, order)));

        switch (lastSortSelection) {
            case "Newest First": applyDateSort.accept(SortOrder.DESCENDING); break;
            case "Status": sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(sCi, SortOrder.ASCENDING),
                    new RowSorter.SortKey(dci, SortOrder.ASCENDING),
                    new RowSorter.SortKey(tci, SortOrder.ASCENDING))); break;
            default: applyDateSort.accept(SortOrder.ASCENDING);
        }
        sortCombo.addActionListener(e -> {
            String sel = (String) sortCombo.getSelectedItem();
            lastSortSelection = sel;
            if ("Past First".equals(sel)) applyDateSort.accept(SortOrder.ASCENDING);
            else if ("Newest First".equals(sel)) applyDateSort.accept(SortOrder.DESCENDING);
            else sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(sCi, SortOrder.ASCENDING),
                    new RowSorter.SortKey(dci, SortOrder.ASCENDING),
                    new RowSorter.SortKey(tci, SortOrder.ASCENDING)));
        });

        if (statusCol >= 0) {
            table.getColumnModel().getColumn(statusCol).setPreferredWidth(200);
            table.getColumnModel().getColumn(statusCol).setMinWidth(180);
        }
        JScrollPane ts = new JScrollPane(table);
        ts.setBorder(BorderFactory.createLineBorder(new Color(230, 225, 250), 1));
        ts.setPreferredSize(new Dimension(800, 400));
        ts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        ts.setAlignmentX(Component.LEFT_ALIGNMENT);
        ts.getViewport().setBackground(Color.WHITE);
        ts.getVerticalScrollBar().setUnitIncrement(50);
        ts.getVerticalScrollBar().setBlockIncrement(150);
        MainFrame.styleScrollPane(ts);
        card.add(ts);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MANAGE AVAILABILITY DIALOG  (fully rebuilt)
    // ══════════════════════════════════════════════════════════════════════

    private void showManageAvailabilityDialog() {
        int teacherId = SessionManager.getCurrentUser().getId();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Manage Availability", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(860, 640);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(true);

        // ── Week navigation state ─────────────────────────────────────────
        // weekOffset 0 = current week, +1 = next week, -1 = last week
        int[] weekOffset = {0};
        int[] selectedDay = {0}; // 0=Mon … 4=Fri

        String[] DAY_SHORT = {"Mon","Tue","Wed","Thu","Fri"};
        String[] DAY_FULL  = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
        String[][] timeSlots = buildTimeSlots();

        // avail[dayIdx][slotIdx][0] = is_available (1/0)
        // avail[dayIdx][slotIdx][1] = max_students
        int[][][] avail = new int[5][timeSlots.length][2];

        // Load saved data from DB
        Runnable loadFromDB = () -> {
            // defaults first - all slots start as unavailable (greyed out)
            for (int d = 0; d < 5; d++)
                for (int s = 0; s < timeSlots.length; s++) { avail[d][s][0] = 0; avail[d][s][1] = 5; }
            List<TeacherSlot> saved = availabilityDAO.getAvailability(teacherId);
            for (TeacherSlot ts : saved) {
                int d = ts.getDayOfWeek();
                String dbTime = ts.getSlotTime(); // "HH:MM:SS"
                for (int s = 0; s < timeSlots.length; s++) {
                    String slotDB = String.format("%02d:%02d:00",
                            Integer.parseInt(timeSlots[s][0]),
                            Integer.parseInt(timeSlots[s][1]));
                    if (slotDB.equals(dbTime) && d >= 0 && d < 5) {
                        avail[d][s][0] = ts.isAvailable() ? 1 : 0;
                        avail[d][s][1] = ts.getMaxStudents();
                        break;
                    }
                }
            }
        };
        loadFromDB.run();

        // ── Root layout ───────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // ── NORTH: header ─────────────────────────────────────────────────
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(Color.WHITE);
        north.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));

        // Top row: title left, week nav right
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        // Title + month/week label stacked
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        JLabel titleLbl = new JLabel("Manage Availability");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLbl.setForeground(MainFrame.TEXT_DARK);

        // Dynamic month + week context label
        JLabel monthLbl  = new JLabel();
        monthLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        monthLbl.setForeground(MainFrame.PRIMARY);

        JLabel weekLbl = new JLabel();
        weekLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        weekLbl.setForeground(MainFrame.TEXT_GRAY);

        titleStack.add(titleLbl);
        titleStack.add(Box.createVerticalStrut(3));
        titleStack.add(monthLbl);
        titleStack.add(Box.createVerticalStrut(1));
        titleStack.add(weekLbl);

        // Week prev / label / next buttons
        JPanel weekNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        weekNav.setOpaque(false);

        JButton prevWeekBtn = createNavArrowButton("< Prev week");
        JLabel  weekRangeLbl = new JLabel();
        weekRangeLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        weekRangeLbl.setForeground(MainFrame.TEXT_GRAY);
        JButton nextWeekBtn = createNavArrowButton("Next week >");
        JButton todayBtn    = createSmallPillButton("Today");

        weekNav.add(prevWeekBtn);
        weekNav.add(weekRangeLbl);
        weekNav.add(nextWeekBtn);
        weekNav.add(Box.createHorizontalStrut(6));
        weekNav.add(todayBtn);

        topRow.add(titleStack, BorderLayout.WEST);
        topRow.add(weekNav,    BorderLayout.EAST);

        // Hint line
        JLabel hintLbl = new JLabel("Click a slot to toggle availability · use − / + to adjust max students");
        hintLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hintLbl.setForeground(MainFrame.TEXT_GRAY);

        north.add(topRow,  BorderLayout.NORTH);
        north.add(hintLbl, BorderLayout.SOUTH);

        // ── Day tabs row ──────────────────────────────────────────────────
        JPanel dayTabRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        dayTabRow.setBackground(new Color(248, 247, 255));
        dayTabRow.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(230, 225, 248)));

        JButton[] dayBtns = new JButton[5];

        // ── Slot grid area ────────────────────────────────────────────────
        JPanel slotArea = new JPanel(new BorderLayout());
        slotArea.setBackground(new Color(250, 249, 255));

        // Helper: recompute and display month/week labels
        SimpleDateFormat mFmt   = new SimpleDateFormat("MMMM yyyy");
        SimpleDateFormat dFmt   = new SimpleDateFormat("MMM d");
        Runnable updateWeekLabels = () -> {
            Calendar ws = getWeekStart(weekOffset[0]);
            Calendar we = (Calendar) ws.clone();
            we.add(Calendar.DAY_OF_YEAR, 4);

            monthLbl.setText(mFmt.format(ws.getTime()));

            String rangeStr = dFmt.format(ws.getTime()) + " – " + dFmt.format(we.getTime());
            weekRangeLbl.setText(rangeStr);

            String relLabel;
            if (weekOffset[0] == 0)       relLabel = "This week";
            else if (weekOffset[0] == 1)  relLabel = "Next week";
            else if (weekOffset[0] == -1) relLabel = "Last week";
            else if (weekOffset[0] > 0)   relLabel = weekOffset[0] + " weeks ahead";
            else                           relLabel = Math.abs(weekOffset[0]) + " weeks ago";

            weekLbl.setText(relLabel + "  ·  " + rangeStr);

            // Update day tab sub-labels (Mon\nMay 27)
            for (int d = 0; d < 5; d++) {
                Calendar dayCal = (Calendar) ws.clone();
                dayCal.add(Calendar.DAY_OF_YEAR, d);
                SimpleDateFormat dayDateFmt = new SimpleDateFormat("MMM d");
                dayBtns[d].setText("<html><center>" + DAY_SHORT[d]
                        + "<br><span style='font-size:9px'>"
                        + dayDateFmt.format(dayCal.getTime())
                        + "</span></center></html>");
            }
        };

        // Helper: rebuild slot grid for the selected day
        Runnable[] buildSlots = {null};
        buildSlots[0] = () -> {
            slotArea.removeAll();
            int d = selectedDay[0];

            // grid: 4 columns
            JPanel grid = new JPanel(new GridLayout(0, 4, 10, 10));
            grid.setBackground(new Color(250, 249, 255));
            grid.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

            for (int s = 0; s < timeSlots.length; s++) {
                final int si = s;
                final int di = d;
                final String slotLabel = timeSlots[s][2];

                JLabel[] statusRef = {null};
                JPanel[]  cardRef  = {null};
                JPanel[]  maxRef   = {null};
                JLabel[]  countRef = {null};

                // Query real remaining capacity for display
                String slotTimeDB = String.format("%02d:%02d:00",
                        Integer.parseInt(timeSlots[s][0]),
                        Integer.parseInt(timeSlots[s][1]));

                // Show how many are already booked for THIS day in the current week
                Calendar ws = getWeekStart(weekOffset[0]);
                Calendar dayCal = (Calendar) ws.clone();
                dayCal.add(Calendar.DAY_OF_YEAR, di);
                Date slotDate = new Date(dayCal.getTimeInMillis());
                int remaining = (avail[di][si][0] == 1)
                        ? availabilityDAO.getRemainingCapacity(teacherId, slotDate, slotTimeDB)
                        : 0;
                // If -1 (not set up yet) just show max
                if (remaining < 0) remaining = avail[di][si][1];
                final int bookedCount = avail[di][si][1] - remaining;

                JPanel card = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        boolean on = avail[di][si][0] == 1;
                        if (on) {
                            g2.setColor(new Color(237, 233, 255));
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                            g2.setColor(MainFrame.PRIMARY);
                            g2.setStroke(new BasicStroke(1.8f));
                        } else {
                            g2.setColor(new Color(244, 244, 248));
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                            g2.setColor(new Color(210, 208, 222));
                            g2.setStroke(new BasicStroke(1f));
                        }
                        g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                        g2.dispose();
                    }
                };
                cardRef[0] = card;
                card.setOpaque(false);
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.setPreferredSize(new Dimension(0, 115));

                // Time label (large, easy to read)
                JLabel timeLbl = new JLabel(slotLabel, SwingConstants.CENTER);
                timeLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
                timeLbl.setForeground(avail[di][si][0] == 1 ? MainFrame.TEXT_DARK : new Color(180, 178, 195));
                timeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Available / Off status
                boolean initOn = avail[di][si][0] == 1;
                JLabel statusLbl = new JLabel(initOn ? "✓  Available" : "✕  Off", SwingConstants.CENTER);
                statusLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
                statusLbl.setForeground(initOn ? new Color(79, 70, 229) : new Color(160, 160, 175));
                statusLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                statusRef[0] = statusLbl;

                // Booked indicator  e.g.  "2 / 5 booked"
                JLabel bookedLbl = new JLabel(bookedCount > 0
                        ? bookedCount + " / " + avail[di][si][1] + " booked" : " ",
                        SwingConstants.CENTER);
                bookedLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                bookedLbl.setForeground(bookedCount >= avail[di][si][1]
                        ? new Color(220, 38, 38) : new Color(100, 100, 120));
                bookedLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Max students row: big − / count / big +
                JPanel maxRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
                maxRow.setOpaque(false);
                maxRow.setAlignmentX(Component.CENTER_ALIGNMENT);
                maxRef[0] = maxRow;

                JButton minus = makeRoundIconBtn("−");
                JLabel countLbl = new JLabel(String.valueOf(avail[di][si][1]));
                countLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
                countLbl.setForeground(MainFrame.TEXT_DARK);
                countLbl.setPreferredSize(new Dimension(24, 22));
                countLbl.setHorizontalAlignment(SwingConstants.CENTER);
                countRef[0] = countLbl;
                JButton plus = makeRoundIconBtn("+");

                minus.addActionListener(e -> {
                    if (avail[di][si][1] > 1) {
                        avail[di][si][1]--;
                        countRef[0].setText(String.valueOf(avail[di][si][1]));
                    }
                });
                plus.addActionListener(e -> {
                    if (avail[di][si][1] < 30) {
                        avail[di][si][1]++;
                        countRef[0].setText(String.valueOf(avail[di][si][1]));
                    }
                });

                maxRow.add(minus);
                maxRow.add(countLbl);
                maxRow.add(plus);
                maxRow.setVisible(avail[di][si][0] == 1);

                // Toggle on card click
                card.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        Component hit = SwingUtilities.getDeepestComponentAt(card, e.getX(), e.getY());
                        if (hit instanceof JButton) return;
                        avail[di][si][0] = avail[di][si][0] == 1 ? 0 : 1;
                        boolean on = avail[di][si][0] == 1;
                        statusRef[0].setText(on ? "✓  Available" : "✕  Off");
                        statusRef[0].setForeground(on ? new Color(79, 70, 229) : new Color(160, 160, 175));
                        maxRef[0].setVisible(on);
                        timeLbl.setForeground(on ? MainFrame.TEXT_DARK : new Color(180, 178, 195));
                        cardRef[0].repaint();
                    }
                    @Override public void mouseEntered(MouseEvent e) { card.repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { card.repaint(); }
                });

                card.add(Box.createVerticalGlue());
                card.add(timeLbl);
                card.add(Box.createVerticalStrut(3));
                card.add(statusLbl);
                card.add(Box.createVerticalStrut(2));
                card.add(bookedLbl);
                card.add(Box.createVerticalStrut(6));
                card.add(maxRow);
                card.add(Box.createVerticalGlue());

                grid.add(card);
            }

            JScrollPane gs = new JScrollPane(grid);
            gs.setBorder(null);
            gs.getVerticalScrollBar().setUnitIncrement(50);
            gs.getVerticalScrollBar().setBlockIncrement(150);
            MainFrame.styleScrollPane(gs);
            slotArea.add(gs, BorderLayout.CENTER);
            slotArea.revalidate();
            slotArea.repaint();
        };

        // Build day tab buttons
        for (int d = 0; d < 5; d++) {
            final int di = d;
            JButton btn = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean sel = selectedDay[0] == di;
                    g2.setColor(sel ? MainFrame.PRIMARY : Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    if (!sel) {
                        g2.setColor(new Color(210, 205, 235));
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setForeground(d == 0 ? Color.WHITE : MainFrame.TEXT_DARK);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(84, 46));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                selectedDay[0] = di;
                for (int i = 0; i < dayBtns.length; i++) {
                    dayBtns[i].setForeground(selectedDay[0] == i ? Color.WHITE : MainFrame.TEXT_DARK);
                    dayBtns[i].repaint();
                }
                buildSlots[0].run();
            });
            dayBtns[d] = btn;
            dayTabRow.add(btn);
        }

        // Wire week navigation
        prevWeekBtn.addActionListener(e -> {
            weekOffset[0]--;
            updateWeekLabels.run();
            buildSlots[0].run();
        });
        nextWeekBtn.addActionListener(e -> {
            weekOffset[0]++;
            updateWeekLabels.run();
            buildSlots[0].run();
        });
        todayBtn.addActionListener(e -> {
            weekOffset[0] = 0;
            updateWeekLabels.run();
            buildSlots[0].run();
        });

        // Initial draw
        updateWeekLabels.run();
        buildSlots[0].run();

        // ── SOUTH: save button ────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 225, 250)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        // Legend left side
        JPanel legendRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        legendRow.setOpaque(false);
        legendRow.add(makeLegendDot(new Color(79, 70, 229), "Available"));
        legendRow.add(makeLegendDot(new Color(210, 208, 222), "Unavailable"));
        legendRow.add(makeLegendDot(new Color(220, 38, 38),  "Fully booked"));

        JButton saveBtn = createPrimaryButton("Save Availability");
        saveBtn.setPreferredSize(new Dimension(180, 44));
        saveBtn.addActionListener(e -> {
            // Persist every day's slots to DB
            boolean ok = true;
            for (int d = 0; d < 5; d++) {
                String[] slotTimes   = new String[timeSlots.length];
                boolean[] isAvail    = new boolean[timeSlots.length];
                int[]     maxStud    = new int[timeSlots.length];
                for (int s = 0; s < timeSlots.length; s++) {
                    slotTimes[s] = String.format("%02d:%02d:00",
                            Integer.parseInt(timeSlots[s][0]),
                            Integer.parseInt(timeSlots[s][1]));
                    isAvail[s]  = avail[d][s][0] == 1;
                    maxStud[s]  = avail[d][s][1];
                }
                if (!availabilityDAO.saveDayAvailability(teacherId, d, slotTimes, isAvail, maxStud)) ok = false;
            }
            if (ok) {
                JOptionPane.showMessageDialog(dialog,
                        "Availability saved! Students will see your updated slots.",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Some slots could not be saved. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottom.add(legendRow, BorderLayout.WEST);
        bottom.add(saveBtn,   BorderLayout.EAST);

        // ── Assemble ──────────────────────────────────────────────────────
        JPanel northFull = new JPanel(new BorderLayout());
        northFull.setBackground(Color.WHITE);
        northFull.add(north,      BorderLayout.NORTH);
        northFull.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        northFull.add(dayTabRow,  BorderLayout.SOUTH);

        root.add(northFull,  BorderLayout.NORTH);
        root.add(slotArea,   BorderLayout.CENTER);
        root.add(bottom,     BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ── Week navigation helpers ────────────────────────────────────────────

    /** Returns a Calendar set to Monday 00:00:00 of the week at the given offset from today. */
    private Calendar getWeekStart(int weekOffset) {
        Calendar c = Calendar.getInstance();
        int dow = c.get(Calendar.DAY_OF_WEEK);
        // Roll back to Monday
        int daysToMon = (dow == Calendar.SUNDAY) ? -6 : -(dow - Calendar.MONDAY);
        c.add(Calendar.DAY_OF_YEAR, daysToMon + (weekOffset * 7));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private JButton createNavArrowButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(237, 233, 255) : new Color(246, 244, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(210, 205, 235));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(MainFrame.PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSmallPillButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? MainFrame.PRIMARY : new Color(237, 233, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setForeground(MainFrame.PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(60, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(MainFrame.PRIMARY); btn.repaint(); }
        });
        return btn;
    }

    private JPanel makeLegendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 2, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(10, 14));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(MainFrame.TEXT_GRAY);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BOOKING DIALOG  (student)
    // ══════════════════════════════════════════════════════════════════════

    private void showBookingDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Book New Appointment", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(740, 590);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        Date[] pickedDate = {new Date(System.currentTimeMillis())};
        User[] selectedTeacher = {null};
        Runnable[] refreshTeacherList = {null};
        Runnable[] refreshTimeSlots   = {null};

        // ── Left panel ────────────────────────────────────────────────────
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 14));
        left.setPreferredSize(new Dimension(360, 0));

        JLabel dlgTitle = new JLabel("New Appointment");
        dlgTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        dlgTitle.setForeground(MainFrame.TEXT_DARK);
        left.add(dlgTitle);
        JLabel dlgSub = new JLabel("Choose teacher, date and time");
        dlgSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dlgSub.setForeground(MainFrame.TEXT_GRAY);
        left.add(dlgSub);
        left.add(Box.createVerticalStrut(18));

        JLabel teacherHdr = new JLabel("Select Teacher");
        teacherHdr.setFont(new Font("SansSerif", Font.BOLD, 13));
        left.add(teacherHdr);
        left.add(Box.createVerticalStrut(6));

        List<User> teachers = userDAO.getTeachers();
        ButtonGroup teacherGroup = new ButtonGroup();
        JPanel teacherCardsPanel = new JPanel();
        teacherCardsPanel.setLayout(new BoxLayout(teacherCardsPanel, BoxLayout.Y_AXIS));
        teacherCardsPanel.setBackground(Color.WHITE);

        refreshTeacherList[0] = () -> {
            teacherCardsPanel.removeAll();
            teacherGroup.clearSelection();
            selectedTeacher[0] = null;
            for (User t : teachers) {
                boolean isAvail = availabilityDAO.isTeacherAvailableOnDate(t.getId(), pickedDate[0]);
                teacherCardsPanel.add(createTeacherCard(t, teacherGroup, selectedTeacher,
                        isAvail, refreshTimeSlots[0]));
                teacherCardsPanel.add(Box.createVerticalStrut(5));
            }
            teacherCardsPanel.revalidate();
            teacherCardsPanel.repaint();
            if (refreshTimeSlots[0] != null) refreshTimeSlots[0].run();
        };

        JScrollPane teacherScroll = new JScrollPane(teacherCardsPanel);
        teacherScroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        teacherScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        teacherScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 130));
        MainFrame.styleScrollPane(teacherScroll);
        left.add(teacherScroll);
        left.add(Box.createVerticalStrut(18));

        JLabel dateLbl = new JLabel("Select Date");
        dateLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        left.add(dateLbl);
        left.add(Box.createVerticalStrut(6));

        Calendar[] currentMonth = {Calendar.getInstance()};
        JLabel[] selDateDisp = {null};
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM yyyy");
        JLabel monthLabel = new JLabel(monthFmt.format(currentMonth[0].getTime()), SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel[] calGrid = {new JPanel(new GridLayout(7, 7, 3, 3))};
        calGrid[0].setOpaque(false);
        calGrid[0].setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        calGrid[0].setPreferredSize(new Dimension(300, 200));

        Runnable[] buildCal = {null};
        buildCal[0] = () -> {
            calGrid[0].removeAll();
            for (String dw : new String[]{"S","M","T","W","T","F","S"}) {
                JLabel h = new JLabel(dw, SwingConstants.CENTER);
                h.setFont(new Font("SansSerif", Font.BOLD, 10));
                h.setForeground(MainFrame.TEXT_GRAY);
                calGrid[0].add(h);
            }
            Calendar cal = (Calendar) currentMonth[0].clone();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int firstDow    = cal.get(Calendar.DAY_OF_WEEK) - 1;
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int yr = cal.get(Calendar.YEAR), mo = cal.get(Calendar.MONTH);
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0); todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);       todayCal.set(Calendar.MILLISECOND, 0);

            for (int i = 0; i < 42; i++) {
                int dayNum = i - firstDow + 1;
                if (dayNum < 1 || dayNum > daysInMonth) { calGrid[0].add(new JLabel()); continue; }
                Calendar dayCal = Calendar.getInstance();
                dayCal.set(yr, mo, dayNum, 0, 0, 0); dayCal.set(Calendar.MILLISECOND, 0);
                Date dayDate  = new Date(dayCal.getTimeInMillis());
                boolean isToday  = dayNum == todayCal.get(Calendar.DAY_OF_MONTH)
                        && mo == todayCal.get(Calendar.MONTH) && yr == todayCal.get(Calendar.YEAR);
                boolean isPast   = dayCal.getTimeInMillis() < todayCal.getTimeInMillis();
                boolean isPicked = pickedDate[0] != null && pickedDate[0].toString().equals(dayDate.toString());
                final int fd = dayNum;
                JPanel cell = new JPanel() {
                    private boolean hovered = false;
                    {
                        setOpaque(false);
                        setCursor(isPast ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        addMouseListener(new MouseAdapter() {
                            @Override public void mouseEntered(MouseEvent e) { if (!isPast) { hovered = true;  repaint(); } }
                            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                            @Override public void mouseClicked(MouseEvent e) {
                                if (!isPast) {
                                    pickedDate[0] = dayDate;
                                    if (selDateDisp[0] != null)
                                        selDateDisp[0].setText(new SimpleDateFormat("EEE, MMM dd yyyy").format(dayDate));
                                    buildCal[0].run();
                                    refreshTeacherList[0].run();
                                }
                            }
                        });
                    }
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        if (isPicked)               g2.setColor(MainFrame.PRIMARY);
                        else if (isToday && !isPast) g2.setColor(MainFrame.PRIMARY_LIGHT);
                        else if (hovered)            g2.setColor(new Color(235, 230, 255));
                        if (isPicked || (isToday && !isPast) || hovered)
                            g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        FontMetrics fm = g2.getFontMetrics();
                        int nx = (getWidth() - fm.stringWidth(String.valueOf(fd))) / 2;
                        int ny = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                        if (isPast)         g2.setColor(new Color(195, 195, 210));
                        else if (isPicked)  g2.setColor(Color.WHITE);
                        else if (isToday)   g2.setColor(MainFrame.PRIMARY);
                        else                g2.setColor(MainFrame.TEXT_DARK);
                        g2.drawString(String.valueOf(fd), nx, ny);
                        g2.dispose();
                    }
                };
                calGrid[0].add(cell);
            }
            calGrid[0].revalidate(); calGrid[0].repaint();
            monthLabel.setText(monthFmt.format(currentMonth[0].getTime()));
        };

        JPanel monthNav = new JPanel(new BorderLayout(4, 0));
        monthNav.setOpaque(false);
        monthNav.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JButton prevM = createCalArrowButton(false);
        JButton nextM = createCalArrowButton(true);
        prevM.addActionListener(e -> { currentMonth[0].add(Calendar.MONTH, -1); buildCal[0].run(); });
        nextM.addActionListener(e -> { currentMonth[0].add(Calendar.MONTH,  1); buildCal[0].run(); });
        monthNav.add(prevM, BorderLayout.WEST);
        monthNav.add(monthLabel, BorderLayout.CENTER);
        monthNav.add(nextM, BorderLayout.EAST);
        left.add(monthNav);
        left.add(Box.createVerticalStrut(6));
        buildCal[0].run();
        left.add(calGrid[0]);

        // ── Right panel ───────────────────────────────────────────────────
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(new Color(250, 249, 255));
        right.setBorder(BorderFactory.createEmptyBorder(24, 14, 20, 24));

        JLabel timeTitle = new JLabel("Select Time");
        timeTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        right.add(timeTitle);
        right.add(Box.createVerticalStrut(4));

        JLabel selDateLbl = new JLabel(new SimpleDateFormat("EEE, MMM dd yyyy").format(pickedDate[0]));
        selDateLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        selDateLbl.setForeground(MainFrame.TEXT_GRAY);
        selDateDisp[0] = selDateLbl;
        right.add(selDateLbl);
        right.add(Box.createVerticalStrut(14));

        right.add(new JLabel("Purpose of Visit"));
        JComboBox<String> purposeCombo = new JComboBox<>(new String[]{
                "Clinic purposes","Consultation purposes","Academic purposes","Other purposes"});
        purposeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        purposeCombo.setBackground(Color.WHITE);
        right.add(purposeCombo);
        right.add(Box.createVerticalStrut(14));

        JPanel slotGrid = new JPanel(new GridLayout(0, 2, 6, 6));
        slotGrid.setOpaque(false);
        JScrollPane slotScroll = new JScrollPane(slotGrid);
        slotScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 220, 245), 1));
        slotScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        slotScroll.getViewport().setBackground(new Color(250, 249, 255));
        MainFrame.styleScrollPane(slotScroll);

        int[] selH = {-1}, selM = {-1};

        refreshTimeSlots[0] = () -> {
            slotGrid.removeAll();
            selH[0] = -1; selM[0] = -1;
            if (selectedTeacher[0] == null) {
                JLabel ph = new JLabel("  Please select a teacher first.");
                ph.setForeground(MainFrame.TEXT_GRAY);
                ph.setFont(new Font("SansSerif", Font.PLAIN, 12));
                slotGrid.add(ph);
                slotGrid.revalidate(); slotGrid.repaint();
                return;
            }
            String[][] slots = buildTimeSlots();
            JPanel[] slotBtnRefs  = new JPanel[slots.length];
            boolean[] slotSelected = new boolean[slots.length];
            for (int i = 0; i < slots.length; i++) {
                final int idx = i;
                final int h = Integer.parseInt(slots[i][0]);
                final int m = Integer.parseInt(slots[i][1]);
                String slotTimeDB = String.format("%02d:%02d:00", h, m);
                int capacity = availabilityDAO.getRemainingCapacity(
                        selectedTeacher[0].getId(), pickedDate[0], slotTimeDB);
                // capacity -1 means teacher hasn't set up this slot → treat as unavailable
                final boolean isFull = capacity <= 0;
                final String label = isFull
                        ? slots[i][2] + "  (full)"
                        : slots[i][2] + "  (" + capacity + " left)";
                JPanel slotCell = new JPanel() {
                    boolean hovered = false;
                    {
                        setOpaque(false);
                        setCursor(isFull ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        setPreferredSize(new Dimension(0, 36));
                        addMouseListener(new MouseAdapter() {
                            @Override public void mouseEntered(MouseEvent e) { if (!isFull) { hovered = true;  repaint(); } }
                            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                            @Override public void mouseClicked(MouseEvent e) {
                                if (isFull) return;
                                for (int j = 0; j < slotSelected.length; j++) {
                                    slotSelected[j] = false;
                                    if (slotBtnRefs[j] != null) slotBtnRefs[j].repaint();
                                }
                                slotSelected[idx] = true;
                                selH[0] = h; selM[0] = m;
                                repaint();
                            }
                        });
                        slotBtnRefs[idx] = this;
                    }
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        boolean isSel = slotSelected[idx];
                        if (isFull)       g2.setColor(new Color(240, 240, 242));
                        else if (isSel)   g2.setColor(MainFrame.PRIMARY);
                        else if (hovered) g2.setColor(MainFrame.PRIMARY_LIGHT);
                        else              g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.setColor(isSel ? MainFrame.PRIMARY : MainFrame.BORDER);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        g2.setColor(isFull ? Color.LIGHT_GRAY : (isSel ? Color.WHITE : MainFrame.TEXT_DARK));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(label,
                                (getWidth() - fm.stringWidth(label)) / 2,
                                (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                        g2.dispose();
                    }
                };
                slotGrid.add(slotCell);
            }
            slotGrid.revalidate(); slotGrid.repaint();
        };

        right.add(slotScroll);
        right.add(Box.createVerticalStrut(14));

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        errorLbl.setForeground(MainFrame.ERROR);
        right.add(errorLbl);
        right.add(Box.createVerticalStrut(6));

        JButton confirmBtn = createPrimaryButton("Confirm Booking");
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirmBtn.addActionListener(e -> {
            if (selectedTeacher[0] == null) { errorLbl.setText("Please select a teacher."); return; }
            if (selH[0] == -1)              { errorLbl.setText("Please select a time slot."); return; }
            Time time = Time.valueOf(String.format("%02d:%02d:00", selH[0], selM[0]));
            if (appointmentDAO.createAppointment(SessionManager.getCurrentUser().getId(),
                    selectedTeacher[0].getId(), pickedDate[0], time)) {
                JOptionPane.showMessageDialog(dialog, "Appointment booked successfully!");
                dialog.dispose();
                refresh();
            } else {
                errorLbl.setText("Time slot conflict! Choose another time.");
            }
        });
        right.add(confirmBtn);

        refreshTeacherList[0].run();
        root.add(left,  BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel createTeacherCard(User teacher, ButtonGroup group, User[] selected,
            boolean isAvailable, Runnable onSelect) {
        JToggleButton btn = new JToggleButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(!isAvailable ? new Color(245, 245, 250)
                        : isSelected() ? MainFrame.PRIMARY_LIGHT : Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(isSelected() ? MainFrame.PRIMARY : MainFrame.BORDER);
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth()-1, getHeight()-1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setLayout(new BorderLayout(10, 0));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        btn.setEnabled(isAvailable);
        btn.setCursor(isAvailable ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        String init = teacher.getName().length() > 0
                ? String.valueOf(teacher.getName().charAt(0)).toUpperCase() : "?";
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isAvailable ? new Color(220, 205, 255) : Color.LIGHT_GRAY);
                g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                g2.setColor(isAvailable ? MainFrame.PRIMARY : Color.DARK_GRAY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init,
                        (getWidth() - fm.stringWidth(init)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(34, 34));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        JLabel nameLbl  = new JLabel(teacher.getName() + (!isAvailable ? " (Unavailable)" : ""));
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLbl.setForeground(isAvailable ? MainFrame.TEXT_DARK : Color.GRAY);
        JLabel emailLbl = new JLabel(teacher.getEmail());
        emailLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        emailLbl.setForeground(isAvailable ? MainFrame.TEXT_GRAY : Color.LIGHT_GRAY);
        textCol.add(nameLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(emailLbl);

        btn.add(avatar, BorderLayout.WEST);
        btn.add(textCol, BorderLayout.CENTER);
        group.add(btn);
        btn.addActionListener(e -> { selected[0] = teacher; if (onSelect != null) onSelect.run(); });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(btn, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(225, 220, 245));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth()-1, getHeight()-1, 16, 16));
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
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 28));
        v.setForeground(MainFrame.TEXT_DARK);
        card.add(t);
        card.add(Box.createVerticalStrut(6));
        card.add(v);
        return card;
    }

    private JButton makeRoundIconBtn(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(200, 190, 255) : new Color(225, 218, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setForeground(MainFrame.PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createCalArrowButton(boolean pointRight) {
        JButton btn = new JButton() {
            private boolean hovered = false;
            {
                setPreferredSize(new Dimension(30, 30));
                setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) { g2.setColor(MainFrame.PRIMARY_LIGHT); g2.fillRoundRect(3, 3, getWidth()-6, getHeight()-6, 6, 6); }
                int cx = getWidth()/2, cy = getHeight()/2, aw = 6, ah = 5;
                int[] xs = pointRight ? new int[]{cx-aw/2,cx+aw/2,cx-aw/2} : new int[]{cx+aw/2,cx-aw/2,cx+aw/2};
                int[] ys = {cy-ah, cy, cy+ah};
                g2.setColor(hovered ? MainFrame.PRIMARY : MainFrame.TEXT_GRAY);
                g2.fillPolygon(xs, ys, 3);
                g2.dispose();
            }
        };
        return btn;
    }

    private String[][] buildTimeSlots() {
        int total = (18 - 8) * 2;
        String[][] slots = new String[total][3];
        int idx = 0;
        for (int h = 8; h < 18; h++) {
            for (int m : new int[]{0, 30}) {
                int h12 = h % 12 == 0 ? 12 : h % 12;
                slots[idx][0] = String.valueOf(h);
                slots[idx][1] = String.valueOf(m);
                slots[idx][2] = String.format("%d:%02d %s", h12, m, h < 12 ? "AM" : "PM");
                idx++;
            }
        }
        return slots;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(42);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(230, 225, 250));
        table.setSelectionBackground(new Color(245, 243, 255));
        table.setSelectionForeground(MainFrame.TEXT_DARK);
        table.getTableHeader().setReorderingAllowed(false);
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(Color.WHITE);
        header.setForeground(MainFrame.TEXT_GRAY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, MainFrame.PRIMARY));
        return table;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? MainFrame.PRIMARY_DARK
                        : getModel().isRollover() ? new Color(160, 100, 255)
                        : MainFrame.PRIMARY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @Override public void refresh() { buildUI(); }
}