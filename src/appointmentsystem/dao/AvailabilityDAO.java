package appointmentsystem.dao;

import appointmentsystem.db.DatabaseConnection;
import com.appointmentsystem.model.TeacherSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AvailabilityDAO
 *
 * Handles all DB operations for the teacher_availability table.
 *
 * Table expected (run schema SQL first):
 *   teacher_availability(id, teacher_id, day_of_week, slot_time,
 *                        is_available, max_students)
 *   day_of_week: 0=Monday … 4=Friday  (matches WEEKDAY() in MySQL)
 */
public class AvailabilityDAO {

    // ── Save / upsert ────────────────────────────────────────────────────

    /**
     * Saves (inserts or updates) a single slot for a teacher.
     * Safe to call in a loop for every slot in the availability dialog.
     *
     * @param teacherId    the teacher's user id
     * @param dayOfWeek    0 = Monday … 4 = Friday
     * @param slotTime     "HH:MM:SS" format, e.g. "08:00:00"
     * @param isAvailable  true = open, false = blocked
     * @param maxStudents  cap for this slot (1–30)
     * @return true if the DB write succeeded
     */
    public boolean saveSlot(int teacherId, int dayOfWeek, String slotTime,
                            boolean isAvailable, int maxStudents) {
        String sql = "INSERT INTO teacher_availability " +
                     "(teacher_id, day_of_week, slot_time, is_available, max_students) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "is_available = VALUES(is_available), " +
                     "max_students = VALUES(max_students)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            ps.setInt(2, dayOfWeek);
            ps.setString(3, slotTime);
            ps.setInt(4, isAvailable ? 1 : 0);
            ps.setInt(5, maxStudents);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[AvailabilityDAO] saveSlot failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convenience: saves every slot for one day in a single call.
     * Pass parallel arrays — index must match across all three.
     *
     * @param teacherId    the teacher's user id
     * @param dayOfWeek    0 = Monday … 4 = Friday
     * @param slotTimes    array of "HH:MM:SS" strings for every 30-min slot
     * @param isAvailable  availability flag per slot
     * @param maxStudents  max-student cap per slot
     * @return true if all writes succeeded
     */
    public boolean saveDayAvailability(int teacherId, int dayOfWeek,
                                       String[] slotTimes,
                                       boolean[] isAvailable,
                                       int[] maxStudents) {
        String sql = "INSERT INTO teacher_availability " +
                     "(teacher_id, day_of_week, slot_time, is_available, max_students) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "is_available = VALUES(is_available), " +
                     "max_students = VALUES(max_students)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            for (int i = 0; i < slotTimes.length; i++) {
                ps.setInt(1, teacherId);
                ps.setInt(2, dayOfWeek);
                ps.setString(3, slotTimes[i]);
                ps.setInt(4, isAvailable[i] ? 1 : 0);
                ps.setInt(5, maxStudents[i]);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.err.println("[AvailabilityDAO] saveDayAvailability failed: " + e.getMessage());
            return false;
        }
    }

    // ── Load ─────────────────────────────────────────────────────────────

    /**
     * Returns all saved availability rows for a teacher, ordered by
     * day_of_week then slot_time.  Returns an empty list (never null)
     * if nothing has been saved yet.
     *
     * @param teacherId the teacher's user id
     */
    public List<TeacherSlot> getAvailability(int teacherId) {
        String sql = "SELECT day_of_week, slot_time, is_available, max_students " +
                     "FROM teacher_availability " +
                     "WHERE teacher_id = ? " +
                     "ORDER BY day_of_week, slot_time";
        List<TeacherSlot> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new TeacherSlot(
                        rs.getInt("day_of_week"),
                        rs.getString("slot_time"),
                        rs.getInt("is_available") == 1,
                        rs.getInt("max_students")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[AvailabilityDAO] getAvailability failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns only the slots that are marked available for a specific day.
     * Useful when building the student booking dialog's time-slot grid.
     *
     * @param teacherId  the teacher's user id
     * @param dayOfWeek  0 = Monday … 4 = Friday
     */
    public List<TeacherSlot> getAvailableSlotsForDay(int teacherId, int dayOfWeek) {
        String sql = "SELECT day_of_week, slot_time, is_available, max_students " +
                     "FROM teacher_availability " +
                     "WHERE teacher_id = ? " +
                     "AND day_of_week = ? " +
                     "AND is_available = 1 " +
                     "ORDER BY slot_time";
        List<TeacherSlot> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            ps.setInt(2, dayOfWeek);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new TeacherSlot(
                        rs.getInt("day_of_week"),
                        rs.getString("slot_time"),
                        true,
                        rs.getInt("max_students")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[AvailabilityDAO] getAvailableSlotsForDay failed: " + e.getMessage());
        }
        return list;
    }

    // ── Capacity check ───────────────────────────────────────────────────

    /**
     * Returns how many seats are still open for a specific teacher + date + time.
     *
     * Logic:  max_students  −  COUNT(pending or approved appointments)
     *
     * Returns 0 if the slot is full, -1 if the slot doesn't exist in
     * teacher_availability (teacher never set it up), or -2 on DB error.
     *
     * @param teacherId   the teacher's user id
     * @param date        the appointment date (java.sql.Date)
     * @param slotTime    "HH:MM:SS", must match what's stored in the table
     */
    public int getRemainingCapacity(int teacherId, Date date, String slotTime) {
        // WEEKDAY(date) returns 0=Monday … 6=Sunday in MySQL — matches our day_of_week convention.
        String sql = "SELECT ta.max_students " +
                     "- COALESCE(( " +
                     "SELECT COUNT(*) " +
                     "FROM appointments a " +
                     "WHERE a.teacher_id = ta.teacher_id " +
                     "AND a.date = ? " +
                     "AND a.time = ? " +
                     "AND a.status IN ('pending','approved') " +
                     "), 0) AS remaining " +
                     "FROM teacher_availability ta " +
                     "WHERE ta.teacher_id = ? " +
                     "AND ta.slot_time = ? " +
                     "AND ta.day_of_week = WEEKDAY(?) " +
                     "AND ta.is_available = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, date);
            ps.setString(2, slotTime);
            ps.setInt(3, teacherId);
            ps.setString(4, slotTime);
            ps.setDate(5, date);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Math.max(0, rs.getInt("remaining"));
            }
            return -1; // slot not found in availability table

        } catch (SQLException e) {
            System.err.println("[AvailabilityDAO] getRemainingCapacity failed: " + e.getMessage());
            return -2;
        }
    }

    /**
     * Quick boolean — is this slot still bookable?
     * Saves you calling getRemainingCapacity() and comparing to 0 everywhere.
     */
    public boolean isSlotBookable(int teacherId, Date date, String slotTime) {
        return getRemainingCapacity(teacherId, date, slotTime) > 0;
    }

    // ── Teacher availability check (replaces the weekday-only stub) ──────

    /**
     * Returns true if the teacher has at least one available slot on
     * the given date's day-of-week.  Replaces the old stub in BookingPanel
     * that only checked for weekdays.
     *
     * @param teacherId the teacher's user id
     * @param date      the date to check
     */
    public boolean isTeacherAvailableOnDate(int teacherId, Date date) {
        String sql = "SELECT COUNT(*) AS cnt " +
                     "FROM teacher_availability " +
                     "WHERE teacher_id = ? " +
                     "AND day_of_week = WEEKDAY(?) " +
                     "AND is_available = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            ps.setDate(2, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("cnt") > 0;

        } catch (SQLException e) {
            System.err.println("[AvailabilityDAO] isTeacherAvailableOnDate failed: " + e.getMessage());
        }
        return false;
    }

    // ── Delete ───────────────────────────────────────────────────────────

    /**
     * Removes all availability rows for a teacher on a specific day.
     * Handy if you want to fully reset a day before re-saving.
     */
    public boolean clearDay(int teacherId, int dayOfWeek) {
        String sql = "DELETE FROM teacher_availability WHERE teacher_id = ? AND day_of_week = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            ps.setInt(2, dayOfWeek);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[AvailabilityDAO] clearDay failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes ALL availability rows for a teacher across all days.
     * Use with care — typically only on account deletion.
     */
    public boolean clearAll(int teacherId) {
        String sql = "DELETE FROM teacher_availability WHERE teacher_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[AvailabilityDAO] clearAll failed: " + e.getMessage());
            return false;
        }
    }
}