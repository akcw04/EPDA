package facade;

import entity.Feedback;
import util.DatabaseConnection;
import util.IDGenerator;

import jakarta.ejb.Stateless;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateless EJB Facade for Feedback CRUD operations using JDBC.
 * Feedback is written by technicians about their appointments.
 */
@Stateless
public class FeedbackFacade {

    public boolean createFeedback(Feedback feedback) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String feedbackID = IDGenerator.generateFeedbackID(conn);
            feedback.setId(feedbackID);

            String sql = "INSERT INTO FEEDBACK (feedback_id, appointment_id, technician_id, feedback_text, created_at) " +
                         "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, feedback.getId());
                ps.setString(2, feedback.getAppointmentId());
                ps.setString(3, feedback.getTechnicianId());
                ps.setString(4, feedback.getFeedbackText());
                ps.setTimestamp(5, Timestamp.valueOf(
                        feedback.getCreatedAt() != null ? feedback.getCreatedAt() : LocalDateTime.now()));
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating feedback: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Feedback getFeedbackById(String feedbackId) throws SQLException {
        String sql = "SELECT * FROM FEEDBACK WHERE feedback_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, feedbackId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Feedback f = mapResultSet(rs);
                rs.close();
                return f;
            }
            rs.close();
            return null;
        }
    }

    public List<Feedback> getAllFeedback() throws SQLException {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT * FROM FEEDBACK ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            rs.close();
        }
        return list;
    }

    public List<Feedback> getFeedbackByTechnician(String technicianId) throws SQLException {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT * FROM FEEDBACK WHERE technician_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            rs.close();
        }
        return list;
    }

    public Feedback getFeedbackByAppointment(String appointmentId) throws SQLException {
        String sql = "SELECT * FROM FEEDBACK WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Feedback f = mapResultSet(rs);
                rs.close();
                return f;
            }
            rs.close();
            return null;
        }
    }

    public boolean updateFeedback(Feedback feedback) throws SQLException {
        String sql = "UPDATE FEEDBACK SET feedback_text = ? WHERE feedback_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, feedback.getFeedbackText());
            ps.setString(2, feedback.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteFeedback(String feedbackId) throws SQLException {
        String sql = "DELETE FROM FEEDBACK WHERE feedback_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, feedbackId);
            return ps.executeUpdate() > 0;
        }
    }

    private Feedback mapResultSet(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setId(rs.getString("feedback_id"));
        f.setAppointmentId(rs.getString("appointment_id"));
        f.setTechnicianId(rs.getString("technician_id"));
        f.setFeedbackText(rs.getString("feedback_text"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) f.setCreatedAt(ts.toLocalDateTime());
        return f;
    }
}
