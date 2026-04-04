package facade;

import entity.AppointmentComment;
import util.DatabaseConnection;
import util.IDGenerator;

import jakarta.ejb.Stateless;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateless EJB Facade for AppointmentComment CRUD operations using JDBC.
 * Comments are written by customers about their appointments.
 */
@Stateless
public class AppointmentCommentFacade {

    public boolean createComment(AppointmentComment comment) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String commentID = IDGenerator.generateCommentID(conn);
            comment.setId(commentID);

            String sql = "INSERT INTO APPOINTMENT_COMMENT (comment_id, appointment_id, customer_id, " +
                         "comment_text, rating, created_at) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, comment.getId());
                ps.setString(2, comment.getAppointmentId());
                ps.setString(3, comment.getCustomerId());
                ps.setString(4, comment.getCommentText());
                ps.setObject(5, comment.getRating());
                ps.setTimestamp(6, Timestamp.valueOf(
                        comment.getCreatedAt() != null ? comment.getCreatedAt() : LocalDateTime.now()));
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public AppointmentComment getCommentById(String commentId) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENT_COMMENT WHERE comment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, commentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                AppointmentComment c = mapResultSet(rs);
                rs.close();
                return c;
            }
            rs.close();
            return null;
        }
    }

    public List<AppointmentComment> getAllComments() throws SQLException {
        List<AppointmentComment> list = new ArrayList<>();
        String sql = "SELECT * FROM APPOINTMENT_COMMENT ORDER BY created_at DESC";
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

    public List<AppointmentComment> getCommentsByAppointment(String appointmentId) throws SQLException {
        List<AppointmentComment> list = new ArrayList<>();
        String sql = "SELECT * FROM APPOINTMENT_COMMENT WHERE appointment_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            rs.close();
        }
        return list;
    }

    public List<AppointmentComment> getCommentsByCustomer(String customerId) throws SQLException {
        List<AppointmentComment> list = new ArrayList<>();
        String sql = "SELECT * FROM APPOINTMENT_COMMENT WHERE customer_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            rs.close();
        }
        return list;
    }

    /**
     * Get comments for all appointments of a specific technician.
     */
    public List<AppointmentComment> getCommentsByTechnician(String technicianId) throws SQLException {
        List<AppointmentComment> list = new ArrayList<>();
        String sql = "SELECT c.* FROM APPOINTMENT_COMMENT c " +
                     "JOIN APPOINTMENT a ON c.appointment_id = a.appointment_id " +
                     "WHERE a.technician_id = ? ORDER BY c.created_at DESC";
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

    /**
     * Get average rating from customer comments for a technician.
     */
    public double getAverageRatingByTechnician(String technicianId) throws SQLException {
        String sql = "SELECT AVG(c.rating) FROM APPOINTMENT_COMMENT c " +
                     "JOIN APPOINTMENT a ON c.appointment_id = a.appointment_id " +
                     "WHERE a.technician_id = ? AND c.rating IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianId);
            ResultSet rs = ps.executeQuery();
            double avg = 0.0;
            if (rs.next()) avg = rs.getDouble(1);
            rs.close();
            return avg;
        }
    }

    public boolean updateComment(AppointmentComment comment) throws SQLException {
        String sql = "UPDATE APPOINTMENT_COMMENT SET comment_text = ?, rating = ? WHERE comment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, comment.getCommentText());
            ps.setObject(2, comment.getRating());
            ps.setString(3, comment.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteComment(String commentId) throws SQLException {
        String sql = "DELETE FROM APPOINTMENT_COMMENT WHERE comment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, commentId);
            return ps.executeUpdate() > 0;
        }
    }

    private AppointmentComment mapResultSet(ResultSet rs) throws SQLException {
        AppointmentComment c = new AppointmentComment();
        c.setId(rs.getString("comment_id"));
        c.setAppointmentId(rs.getString("appointment_id"));
        c.setCustomerId(rs.getString("customer_id"));
        c.setCommentText(rs.getString("comment_text"));
        c.setRating((Integer) rs.getObject("rating"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
