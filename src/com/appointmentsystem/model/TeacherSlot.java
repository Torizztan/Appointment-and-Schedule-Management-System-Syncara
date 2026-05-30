package com.appointmentsystem.model;

/**
 * TeacherSlot
 *
 * Represents one 30-minute availability slot from the teacher_availability table.
 *
 * dayOfWeek : 0 = Monday … 4 = Friday  (matches MySQL WEEKDAY())
 * slotTime  : "HH:MM:SS" string, e.g. "08:00:00"
 */
public class TeacherSlot {

    private int     dayOfWeek;
    private String  slotTime;
    private boolean isAvailable;
    private int     maxStudents;

    // ── Constructors ─────────────────────────────────────────────────────

    public TeacherSlot() {}

    public TeacherSlot(int dayOfWeek, String slotTime,
                       boolean isAvailable, int maxStudents) {
        this.dayOfWeek   = dayOfWeek;
        this.slotTime    = slotTime;
        this.isAvailable = isAvailable;
        this.maxStudents = maxStudents;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public int     getDayOfWeek()   { return dayOfWeek;   }
    public String  getSlotTime()    { return slotTime;    }
    public boolean isAvailable()    { return isAvailable; }
    public int     getMaxStudents() { return maxStudents; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setDayOfWeek(int dayOfWeek)       { this.dayOfWeek   = dayOfWeek;   }
    public void setSlotTime(String slotTime)       { this.slotTime    = slotTime;    }
    public void setAvailable(boolean isAvailable)  { this.isAvailable = isAvailable; }
    public void setMaxStudents(int maxStudents)     { this.maxStudents = maxStudents; }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Returns the day name for display, e.g. getDayName(0) → "Monday".
     */
    public static String getDayName(int dayOfWeek) {
        String[] names = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
        if (dayOfWeek >= 0 && dayOfWeek < names.length) return names[dayOfWeek];
        return "Unknown";
    }

    /**
     * Converts "HH:MM:SS" to a display label like "8:00 AM".
     * Safe to call on any slotTime coming from the DB.
     */
    public String getDisplayTime() {
        if (slotTime == null || slotTime.length() < 5) return slotTime;
        try {
            String[] parts = slotTime.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            String ampm = h < 12 ? "AM" : "PM";
            int h12 = h % 12 == 0 ? 12 : h % 12;
            return String.format("%d:%02d %s", h12, m, ampm);
        } catch (Exception e) {
            return slotTime;
        }
    }

    @Override
    public String toString() {
        return String.format("TeacherSlot{day=%d, time=%s, available=%b, max=%d}",
                dayOfWeek, slotTime, isAvailable, maxStudents);
    }
}
