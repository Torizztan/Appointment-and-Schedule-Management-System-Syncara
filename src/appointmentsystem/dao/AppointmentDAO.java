package appointmentsystem.dao;

import appointmentsystem.db.DatabaseConnection;
import com.appointmentsystem.model.Appointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public boolean createAppointment(int studentId, int teacherId, Date date, Time time) {
        if (hasConflict(teacherId, date, time)) return false;

        // Check if the slot is available in teacher_availability
        AvailabilityDAO availabilityDAO = new AvailabilityDAO();
        String slotTimeDB = time.toString(); // Time.toString() returns "HH:mm:ss"
        if (!availabilityDAO.isSlotBookable(teacherId, date, slotTimeDB)) {
            return false;
        }

        String sql = "INSERT INTO appointments (student_id, teacher_id, date, time, status) VALUES (?, ?, ?, ?, 'pending')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, teacherId);
            stmt.setDate(3, date);
            stmt.setTime(4, time);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasConflict(int teacherId, Date date, Time time) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE teacher_id = ? AND date = ? AND time = ? AND status NOT IN ('cancelled', 'rejected')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            stmt.setDate(2, date);
            stmt.setTime(3, time);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Appointment> getStudentAppointments(int studentId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name as teacher_name FROM appointments a JOIN users u ON a.teacher_id = u.id WHERE a.student_id = ? ORDER BY a.date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapAppointment(rs, false, true));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getTeacherUpcomingAppointments(int teacherId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name as student_name FROM appointments a JOIN users u ON a.student_id = u.id WHERE a.teacher_id = ? AND a.date >= CURDATE() ORDER BY a.date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapAppointment(rs, true, false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getTeacherPastAppointments(int teacherId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name as student_name FROM appointments a JOIN users u ON a.student_id = u.id WHERE a.teacher_id = ? AND a.date < CURDATE() ORDER BY a.date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapAppointment(rs, true, false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, t.name as teacher_name FROM appointments a JOIN users s ON a.student_id = s.id JOIN users t ON a.teacher_id = t.id ORDER BY a.date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapAppointmentFull(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getAppointmentsByDate(Date date) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, t.name as teacher_name FROM appointments a JOIN users s ON a.student_id = s.id JOIN users t ON a.teacher_id = t.id WHERE a.date = ? ORDER BY a.time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapAppointmentFull(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getAppointmentsByDateAndUser(Date date, int userId, String role) {
        List<Appointment> list = new ArrayList<>();
        String sql;
        if ("student".equals(role)) {
            sql = "SELECT a.*, u.name as teacher_name FROM appointments a JOIN users u ON a.teacher_id = u.id WHERE a.student_id = ? AND a.date = ? ORDER BY a.time ASC";
        } else if ("teacher".equals(role)) {
            sql = "SELECT a.*, u.name as student_name FROM appointments a JOIN users u ON a.student_id = u.id WHERE a.teacher_id = ? AND a.date = ? ORDER BY a.time ASC";
        } else {
            return getAppointmentsByDate(date);
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setStudentId(rs.getInt("student_id"));
                a.setTeacherId(rs.getInt("teacher_id"));
                a.setDate(rs.getDate("date"));
                a.setTime(rs.getTime("time"));
                a.setStatus(rs.getString("status"));
                a.setNotes(rs.getString("notes"));
                a.setCreatedAt(rs.getTimestamp("created_at"));
                if ("student".equals(role)) {
                    a.setTeacherName(rs.getString("teacher_name"));
                } else {
                    a.setStudentName(rs.getString("student_name"));
                }
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(int appointmentId, String status, String notes) {
        String sql = "UPDATE appointments SET status = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, notes);
            stmt.setInt(3, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelAppointment(int appointmentId, int studentId) {
        String sql = "UPDATE appointments SET status = 'cancelled' WHERE id = ? AND student_id = ? AND status = 'pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            stmt.setInt(2, studentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAppointment(int id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Date> getDatesWithAppointments(int year, int month) {
        List<Date> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT date FROM appointments WHERE YEAR(date) = ? AND MONTH(date) = ? AND status NOT IN ('cancelled', 'rejected')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getDate("date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dates;
    }

    public List<Date> getDatesWithAppointmentsForUser(int year, int month, int userId, String role) {
        List<Date> dates = new ArrayList<>();
        String column = "student".equals(role) ? "student_id" : "teacher_id";
        String sql = "SELECT DISTINCT date FROM appointments WHERE YEAR(date) = ? AND MONTH(date) = ? AND " + column + " = ? AND status NOT IN ('cancelled', 'rejected')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getDate("date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dates;
    }

    private Appointment mapAppointment(ResultSet rs, boolean hasStudentName, boolean hasTeacherName) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setTeacherId(rs.getInt("teacher_id"));
        a.setDate(rs.getDate("date"));
        a.setTime(rs.getTime("time"));
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        if (hasStudentName) a.setStudentName(rs.getString("student_name"));
        if (hasTeacherName) a.setTeacherName(rs.getString("teacher_name"));
        return a;
    }

    private Appointment mapAppointmentFull(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setTeacherId(rs.getInt("teacher_id"));
        a.setDate(rs.getDate("date"));
        a.setTime(rs.getTime("time"));
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setStudentName(rs.getString("student_name"));
        a.setTeacherName(rs.getString("teacher_name"));
        return a;
    }
}
