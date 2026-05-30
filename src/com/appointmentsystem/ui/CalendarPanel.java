package com.appointmentsystem.ui;

import appointmentsystem.dao.AppointmentDAO;
import com.appointmentsystem.model.Appointment;
import com.appointmentsystem.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;
import java.util.List;

public class CalendarPanel extends JPanel implements MainFrame.Refreshable {
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private Calendar currentMonth;
    private JPanel calendarGrid;
    private JLabel monthLabel;
    private JPanel appointmentListPanel;
    private Date selectedDate;

    /**
     * Max bookable slots per day — teachers/admins can change this at runtime.
     * Stored as a static field so it persists across CalendarPanel rebuilds within a session.
     */
    private static int maxSlotsPerDay = 20;

    public CalendarPanel() {
        currentMonth = Calendar.getInstance();
        selectedDate = new Date(System.currentTimeMillis());
        setLayout(new BorderLayout());
        setBackground(MainFrame.BG_LIGHT);
        buildUI();
    }

    private void buildUI() {
        removeAll();

        JPanel mainContent = new JPanel(new BorderLayout(16, 0));
        mainContent.setBackground(MainFrame.BG_LIGHT);
        mainContent.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // ── Left panel (calendar) ─────────────────────────────────────
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(480, 0));

        // Header row: title + optional "Set Max Slots" button for teachers/admins
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Calendar");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(MainFrame.TEXT_DARK);
        headerRow.add(titleLabel, BorderLayout.WEST);

        String role = SessionManager.getRole();
        if ("teacher".equals(role) || "admin".equals(role)) {
            JButton setMaxBtn = new JButton("Set Max Slots (" + maxSlotsPerDay + "/day)") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover()
                            ? MainFrame.PRIMARY_LIGHT : new Color(242, 240, 255));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(MainFrame.PRIMARY);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            setMaxBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            setMaxBtn.setForeground(MainFrame.PRIMARY);
            setMaxBtn.setContentAreaFilled(false);
            setMaxBtn.setBorderPainted(false);
            setMaxBtn.setFocusPainted(false);
            setMaxBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaxBtn.setPreferredSize(new Dimension(190, 30));
            setMaxBtn.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(
                        this,
                        "Enter maximum appointments per day (1 – 100):",
                        "Set Max Slots",
                        JOptionPane.PLAIN_MESSAGE);
                if (input != null) {
                    try {
                        int n = Integer.parseInt(input.trim());
                        if (n >= 1 && n <= 100) {
                            maxSlotsPerDay = n;
                            setMaxBtn.setText("Set Max Slots (" + maxSlotsPerDay + "/day)");
                            refresh();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Please enter a number between 1 and 100.", "Invalid", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (NumberFormatException ignored) {
                        JOptionPane.showMessageDialog(this, "Invalid number.", "Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
            headerRow.add(setMaxBtn, BorderLayout.EAST);
        }

        leftPanel.add(headerRow);
        leftPanel.add(Box.createVerticalStrut(4));

        JLabel subtitleLabel = new JLabel("Click on a date to view appointments");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(MainFrame.TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(subtitleLabel);
        leftPanel.add(Box.createVerticalStrut(16));

        JPanel calendarCard = createCalendarCard();
        leftPanel.add(calendarCard);

        mainContent.add(leftPanel, BorderLayout.CENTER);

        // ── Right panel (appointment list) ────────────────────────────
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(380, 0));

        appointmentListPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        appointmentListPanel.setOpaque(false);
        appointmentListPanel.setLayout(new BoxLayout(appointmentListPanel, BoxLayout.Y_AXIS));
        appointmentListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        updateAppointmentList();

        JScrollPane rightScroll = new JScrollPane(appointmentListPanel);
        rightScroll.setBorder(null);
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightScroll.setOpaque(false);
        rightScroll.getViewport().setOpaque(false);
        applyScrollbarStyle(rightScroll);
        rightPanel.add(rightScroll);

        mainContent.add(rightPanel, BorderLayout.EAST);

        JScrollPane outerScroll = new JScrollPane(mainContent);
        outerScroll.setBorder(null);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        outerScroll.setOpaque(false);
        outerScroll.getViewport().setOpaque(false);
        applyScrollbarStyle(outerScroll);
        add(outerScroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    static void applyScrollbarStyle(JScrollPane sp) {
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(142, 81, 255, 90);
                trackColor = new Color(237, 234, 255, 60);
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0));
                b.setBorder(BorderFactory.createEmptyBorder()); return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDragging ? new Color(142, 81, 255, 160) : thumbColor);
                g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRoundRect(r.x + 3, r.y, r.width - 6, r.height, 6, 6);
                g2.dispose();
            }
        });
        vsb.setPreferredSize(new Dimension(8, Integer.MAX_VALUE));
        vsb.setOpaque(false);
    }

    private JPanel createCalendarCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Month navigation ──────────────────────────────────────────
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setOpaque(false);
        navPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton prevBtn = createArrowButton(false);
        prevBtn.addActionListener(e -> { currentMonth.add(Calendar.MONTH, -1); refreshCalendar(); });

        SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM yyyy");
        monthLabel = new JLabel(monthFmt.format(currentMonth.getTime()), SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        monthLabel.setForeground(MainFrame.TEXT_DARK);

        JButton nextBtn = createArrowButton(true);
        nextBtn.addActionListener(e -> { currentMonth.add(Calendar.MONTH, 1); refreshCalendar(); });

        navPanel.add(prevBtn, BorderLayout.WEST);
        navPanel.add(monthLabel, BorderLayout.CENTER);
        navPanel.add(nextBtn, BorderLayout.EAST);
        card.add(navPanel);
        card.add(Box.createVerticalStrut(12));

        // ── Day-of-week headers ───────────────────────────────────────
        JPanel dayHeaders = new JPanel(new GridLayout(1, 7, 4, 0));
        dayHeaders.setOpaque(false);
        dayHeaders.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        dayHeaders.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String d : new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            lbl.setForeground(MainFrame.TEXT_GRAY);
            dayHeaders.add(lbl);
        }
        card.add(dayHeaders);
        card.add(Box.createVerticalStrut(8));

        calendarGrid = new JPanel(new GridLayout(6, 7, 4, 4));
        calendarGrid.setOpaque(false);
        calendarGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        fillCalendarGrid();
        card.add(calendarGrid);

        return card;
    }

    private JButton createArrowButton(boolean right) {
        JButton btn = new JButton() {
            private boolean hover = false;
            {
                setPreferredSize(new Dimension(36, 36));
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover) {
                    g2.setColor(MainFrame.PRIMARY_LIGHT);
                    g2.fillRoundRect(4, 4, getWidth()-8, getHeight()-8, 8, 8);
                }
                int cx = getWidth() / 2, cy = getHeight() / 2, aw = 7, ah = 6;
                int[] xs = right ? new int[]{cx-aw/2, cx+aw/2, cx-aw/2}
                                 : new int[]{cx+aw/2, cx-aw/2, cx+aw/2};
                int[] ys = {cy-ah, cy, cy+ah};
                g2.setColor(hover ? MainFrame.PRIMARY : MainFrame.TEXT_DARK);
                g2.fillPolygon(xs, ys, 3);
                g2.dispose();
            }
        };
        return btn;
    }

    private void fillCalendarGrid() {
        calendarGrid.removeAll();

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow   = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        // ── Fetch dates that have appointments, then build a per-day count map ──
        String role   = SessionManager.getRole();
        int    userId = SessionManager.getCurrentUser().getId();
        List<Date> datesWithAppts;
        if ("admin".equals(role)) {
            datesWithAppts = appointmentDAO.getDatesWithAppointments(year, month);
        } else {
            datesWithAppts = appointmentDAO.getDatesWithAppointmentsForUser(year, month, userId, role);
        }

        Map<String, Integer> dayCountMap = new HashMap<>();
        for (Date d : datesWithAppts) {
            if (!dayCountMap.containsKey(d.toString())) {
                List<Appointment> dayAppts;
                if ("admin".equals(role)) {
                    dayAppts = appointmentDAO.getAppointmentsByDate(d);
                } else {
                    dayAppts = appointmentDAO.getAppointmentsByDateAndUser(d, userId, role);
                }
                dayCountMap.put(d.toString(), dayAppts.size());
            }
        }

        Calendar today = Calendar.getInstance();
        Calendar selectedCal = Calendar.getInstance();
        if (selectedDate != null) selectedCal.setTime(selectedDate);
        
        // Create a calendar instance strictly for midnight today to accurately check for past days
        Calendar todayMidnight = Calendar.getInstance();
        todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
        todayMidnight.set(Calendar.MINUTE, 0);
        todayMidnight.set(Calendar.SECOND, 0);
        todayMidnight.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < 42; i++) {
            int dayNum = i - firstDow + 1;
            if (dayNum < 1 || dayNum > daysInMonth) {
                JLabel empty = new JLabel("");
                empty.setPreferredSize(new Dimension(54, 46));
                calendarGrid.add(empty);
            } else {
                final int day = dayNum;

                Calendar dayCal = Calendar.getInstance();
                dayCal.set(year, month - 1, day, 0, 0, 0);
                dayCal.set(Calendar.MILLISECOND, 0);
                Date dayDate = new Date(dayCal.getTimeInMillis());

                boolean isToday = (day == today.get(Calendar.DAY_OF_MONTH)
                        && (month - 1) == today.get(Calendar.MONTH)
                        && year == today.get(Calendar.YEAR));

                boolean isSelected = (selectedDate != null
                        && selectedCal.get(Calendar.DAY_OF_MONTH) == day
                        && selectedCal.get(Calendar.MONTH) == (month - 1)
                        && selectedCal.get(Calendar.YEAR) == year);
                        
                // Check if the current calendar day is before today
                final boolean isPast = dayCal.before(todayMidnight);

                final int bookedCount = dayCountMap.getOrDefault(dayDate.toString(), 0);
                final int available   = Math.max(0, maxSlotsPerDay - bookedCount);
                final boolean hasAppts = bookedCount > 0;

                JButton dayBtn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        // Background
                        if (isSelected) {
                            g2.setColor(isPast ? new Color(170, 170, 170) : MainFrame.PRIMARY);
                            g2.fill(new RoundRectangle2D.Double(3, 2, getWidth()-6, getHeight()-4, 10, 10));
                        } else if (getModel().isRollover()) {
                            g2.setColor(new Color(245, 243, 255));
                            g2.fill(new RoundRectangle2D.Double(3, 2, getWidth()-6, getHeight()-4, 10, 10));
                        }

                        // Slot count (Moved to TOP)
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
                        String slotTxt = available + "/" + maxSlotsPerDay;
                        FontMetrics sfm = g2.getFontMetrics();
                        int sx = (getWidth() - sfm.stringWidth(slotTxt)) / 2;
                        int sy = 12; // Rendered at the top now
                        
                        if (isSelected) {
                            g2.setColor(new Color(255, 255, 255, 190));
                        } else if (isPast) {
                            g2.setColor(new Color(200, 200, 200)); // Greyed out for past days
                        } else if (available == 0) {
                            g2.setColor(MainFrame.ERROR);
                        } else {
                            g2.setColor(new Color(100, 100, 110));
                        }
                        g2.drawString(slotTxt, sx, sy);

                        // Day number (Shifted down slightly to center below the top text)
                        g2.setFont(new Font("SansSerif", isToday && !isSelected ? Font.BOLD : Font.PLAIN, 13));
                        
                        if (isPast && !isSelected) {
                            g2.setColor(new Color(180, 180, 180)); // Greyed out for past days
                        } else {
                            g2.setColor(isSelected ? Color.WHITE : isToday ? MainFrame.PRIMARY : MainFrame.TEXT_DARK);
                        }
                        
                        String dayStr = String.valueOf(day);
                        FontMetrics fm = g2.getFontMetrics();
                        int numX = (getWidth() - fm.stringWidth(dayStr)) / 2;
                        int numY = getHeight() / 2 + 6; 
                        g2.drawString(dayStr, numX, numY);

                        // Appointment indicator dot (Moved to bottom)
                        if (hasAppts && !isSelected) {
                            g2.setColor(isPast ? new Color(200, 200, 200) : MainFrame.PRIMARY);
                            g2.fillOval(getWidth() / 2 - 3, getHeight() - 8, 6, 6);
                        }

                        g2.dispose();
                    }
                };
                dayBtn.setContentAreaFilled(false);
                dayBtn.setBorderPainted(false);
                dayBtn.setFocusPainted(false);
                dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                dayBtn.setPreferredSize(new Dimension(54, 46));
                dayBtn.setToolTipText(available + " of " + maxSlotsPerDay + " slots available on "
                        + String.format("%04d-%02d-%02d", year, month, day));
                dayBtn.addActionListener(e -> {
                    selectedDate = dayDate;
                    refreshCalendar();
                    updateAppointmentList();
                });
                calendarGrid.add(dayBtn);
            }
        }
    }

    private void refreshCalendar() {
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM yyyy");
        monthLabel.setText(monthFmt.format(currentMonth.getTime()));
        fillCalendarGrid();
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private void updateAppointmentList() {
        appointmentListPanel.removeAll();

        SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
        JLabel dateTitle = new JLabel(dateFmt.format(selectedDate));
        dateTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        dateTitle.setForeground(MainFrame.PRIMARY);
        dateTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appointmentListPanel.add(dateTitle);
        appointmentListPanel.add(Box.createVerticalStrut(6));

        String role   = SessionManager.getRole();
        int    userId = SessionManager.getCurrentUser().getId();
        List<Appointment> appts;
        if ("admin".equals(role)) {
            appts = appointmentDAO.getAppointmentsByDate(selectedDate);
        } else {
            appts = appointmentDAO.getAppointmentsByDateAndUser(selectedDate, userId, role);
        }

        // Use the same maxSlotsPerDay so the count here always matches the calendar badges
        int bookedCount = appts.size();
        int available   = Math.max(0, maxSlotsPerDay - bookedCount);

        // Slots line — plain text, no colored background
        JLabel slotsLbl = new JLabel(available + " of " + maxSlotsPerDay + " slots available");
        slotsLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        slotsLbl.setForeground(available == 0 ? MainFrame.ERROR : MainFrame.SUCCESS);
        slotsLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        appointmentListPanel.add(slotsLbl);
        appointmentListPanel.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Appointments for this day");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MainFrame.TEXT_GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appointmentListPanel.add(subtitle);
        appointmentListPanel.add(Box.createVerticalStrut(16));

        if (appts.isEmpty()) {
            JLabel noAppts = new JLabel("No appointments on this day.");
            noAppts.setFont(new Font("SansSerif", Font.ITALIC, 13));
            noAppts.setForeground(MainFrame.TEXT_GRAY);
            noAppts.setAlignmentX(Component.LEFT_ALIGNMENT);
            appointmentListPanel.add(noAppts);
        } else {
            SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");
            for (Appointment a : appts) {
                appointmentListPanel.add(createAppointmentCard(a, timeFmt, role));
                appointmentListPanel.add(Box.createVerticalStrut(8));
            }
        }

        appointmentListPanel.revalidate();
        appointmentListPanel.repaint();
    }

    private JPanel createAppointmentCard(Appointment a, SimpleDateFormat timeFmt, String role) {
        Color statusColor;
        switch (a.getStatus()) {
            case "approved":  statusColor = MainFrame.SUCCESS; break;
            case "pending":   statusColor = MainFrame.WARNING; break;
            case "rejected":  statusColor = MainFrame.ERROR;   break;
            case "completed": statusColor = new Color(0, 150, 137); break;
            default:          statusColor = MainFrame.TEXT_GRAY;
        }
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(244, 244, 245));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(statusColor);
                g2.fillRect(0, 6, 4, getHeight() - 12);
                g2.setColor(new Color(220, 220, 230));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth()-1, getHeight()-1, 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        String personLabel;
        if ("student".equals(role)) {
            personLabel = "Teacher: " + (a.getTeacherName() != null ? a.getTeacherName() : "N/A");
        } else if ("teacher".equals(role)) {
            personLabel = "Student: " + (a.getStudentName() != null ? a.getStudentName() : "N/A");
        } else {
            personLabel = (a.getStudentName() != null ? a.getStudentName() : "?")
                    + " \u2192 " + (a.getTeacherName() != null ? a.getTeacherName() : "?");
        }
        JLabel nameLabel = new JLabel(personLabel);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLabel.setForeground(MainFrame.TEXT_DARK);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel(timeFmt.format(a.getTime()) + "  \u2022  " + a.getStatus().toUpperCase());
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        timeLabel.setForeground(statusColor);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(timeLabel);
        return card;
    }

    @Override
    public void refresh() {
        buildUI();
    }
}