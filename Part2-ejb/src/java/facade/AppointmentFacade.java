package facade;

import entity.*;
import util.*;

import jakarta.ejb.Stateless;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AppointmentFacade - Stateless EJB for appointment management using JDBC.
 * Includes CRUD, concurrency validation, and 5 reporting methods.
 */
@Stateless
public class AppointmentFacade {

    // ========== CRUD OPERATIONS ==========

    public Appointment createAppointment(Appointment appointment) throws SQLException {
        validateTechnicianAvailability(appointment);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String appointmentID = IDGenerator.generateAppointmentID(conn);
            appointment.setId(appointmentID);

            String sql = "INSERT INTO APPOINTMENT (appointment_id, customer_id, technician_id, service_id, " +
                         "appointment_datetime, status, payment_amount, comments, rating) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, appointment.getId());
                ps.setString(2, appointment.getCustomerId());
                ps.setString(3, appointment.getTechnicianId());
                ps.setString(4, appointment.getServiceId());
                ps.setTimestamp(5, Timestamp.valueOf(appointment.getAppointmentDateTime()));
                ps.setString(6, appointment.getStatus());
                ps.setDouble(7, appointment.getPaymentAmount());
                ps.setString(8, appointment.getComments());
                ps.setObject(9, appointment.getRating());
                ps.executeUpdate();
            }
            return appointment;
        }
    }

    public Appointment getAppointmentByID(String appointmentID) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENT WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Appointment a = mapResultSetToAppointment(rs, conn);
                rs.close();
                return a;
            }
            rs.close();
            return null;
        }
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM APPOINTMENT ORDER BY appointment_datetime DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs, conn));
            }
            rs.close();
        }
        return appointments;
    }

    public List<Appointment> getAppointmentsByCustomer(String customerID) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM APPOINTMENT WHERE customer_id = ? ORDER BY appointment_datetime DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs, conn));
            }
            rs.close();
        }
        return appointments;
    }

    public List<Appointment> getAppointmentsByTechnician(String technicianID) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM APPOINTMENT WHERE technician_id = ? ORDER BY appointment_datetime DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs, conn));
            }
            rs.close();
        }
        return appointments;
    }

    public void updateAppointment(Appointment appointment) throws SQLException {
        String sql = "UPDATE APPOINTMENT SET status = ?, payment_amount = ?, comments = ?, rating = ? " +
                     "WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointment.getStatus());
            ps.setDouble(2, appointment.getPaymentAmount());
            ps.setString(3, appointment.getComments());
            ps.setObject(4, appointment.getRating());
            ps.setString(5, appointment.getId());
            ps.executeUpdate();
        }
    }

    public void deleteAppointment(String appointmentID) throws SQLException {
        String sql = "DELETE FROM APPOINTMENT WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentID);
            ps.executeUpdate();
        }
    }

    // ========== CONCURRENCY VALIDATION ==========

    private void validateTechnicianAvailability(Appointment appointment) throws SQLException {
        String technicianID = appointment.getTechnicianId();
        LocalDateTime startTime = appointment.getAppointmentDateTime();
        int durationMinutes = appointment.getService() != null ?
                appointment.getService().getDurationMinutes() : 60;
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        String sql = "SELECT COUNT(*) FROM APPOINTMENT a " +
                     "JOIN SERVICE s ON a.service_id = s.service_id " +
                     "WHERE a.technician_id = ? " +
                     "AND a.status IN ('Pending', 'InProgress') " +
                     "AND a.appointment_datetime < ? " +
                     "AND {fn TIMESTAMPADD(SQL_TSI_MINUTE, s.duration_minutes, a.appointment_datetime)} > ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianID);
            ps.setTimestamp(2, Timestamp.valueOf(endTime));
            ps.setTimestamp(3, Timestamp.valueOf(startTime));
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                rs.close();
                throw new SQLException("Technician has overlapping appointment in the requested time slot");
            }
            rs.close();
        }
    }

    // ========== 5 REPORTING METHODS ==========

    public double getDailyRevenue() throws SQLException {
        String sql = "SELECT SUM(payment_amount) FROM APPOINTMENT " +
                     "WHERE status = 'Completed' AND CAST(appointment_datetime AS DATE) = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            double revenue = 0.0;
            if (rs.next()) revenue = rs.getDouble(1);
            rs.close();
            return revenue;
        }
    }

    public Map<String, Integer> getTechnicianWorkload() throws SQLException {
        Map<String, Integer> workload = new HashMap<>();
        String sql = "SELECT t.name, COUNT(a.appointment_id) as task_count FROM APPOINTMENT a " +
                     "JOIN TECHNICIAN t ON a.technician_id = t.technician_id " +
                     "WHERE a.status IN ('Pending', 'InProgress') " +
                     "GROUP BY t.name ORDER BY task_count DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                workload.put(rs.getString("name"), rs.getInt("task_count"));
            }
            rs.close();
        }
        return workload;
    }

    public Map<String, Integer> getServicePopularity() throws SQLException {
        Map<String, Integer> popularity = new LinkedHashMap<>();
        String sql = "SELECT s.service_name, COUNT(a.appointment_id) as service_count " +
                     "FROM SERVICE s LEFT JOIN APPOINTMENT a ON s.service_id = a.service_id " +
                     "GROUP BY s.service_id, s.service_name ORDER BY service_count DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                popularity.put(rs.getString("service_name"), rs.getInt("service_count"));
            }
            rs.close();
        }
        return popularity;
    }

    public List<Map<String, Object>> getCustomerFeedback() throws SQLException {
        List<Map<String, Object>> feedback = new ArrayList<>();
        String sql = "SELECT c.name, a.comments, a.rating FROM APPOINTMENT a " +
                     "JOIN CUSTOMER c ON a.customer_id = c.customer_id " +
                     "WHERE a.comments IS NOT NULL OR a.rating IS NOT NULL " +
                     "ORDER BY a.appointment_datetime DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("customerName", rs.getString("name"));
                entry.put("comments", rs.getString("comments"));
                entry.put("rating", rs.getObject("rating"));
                feedback.add(entry);
            }
            rs.close();
        }
        return feedback;
    }

    public Map<String, Object> getStatusAnalytics() throws SQLException {
        Map<String, Object> analytics = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as status_count FROM APPOINTMENT GROUP BY status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            int pending = 0, completed = 0, inProgress = 0, cancelled = 0, total = 0;
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("status_count");
                total += count;
                if ("Pending".equals(status)) pending = count;
                else if ("Completed".equals(status)) completed = count;
                else if ("InProgress".equals(status)) inProgress = count;
                else if ("Cancelled".equals(status)) cancelled = count;
            }
            rs.close();
            analytics.put("pending", pending);
            analytics.put("completed", completed);
            analytics.put("inProgress", inProgress);
            analytics.put("cancelled", cancelled);
            analytics.put("total", total);
            analytics.put("pendingRatio", total > 0 ? (double) pending / total : 0.0);
            analytics.put("completedRatio", total > 0 ? (double) completed / total : 0.0);
        }
        return analytics;
    }

    // ========== HELPER METHODS ==========

    private Appointment mapResultSetToAppointment(ResultSet rs, Connection conn) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getString("appointment_id"));
        a.setCustomerId(rs.getString("customer_id"));
        a.setTechnicianId(rs.getString("technician_id"));
        a.setServiceId(rs.getString("service_id"));
        a.setStatus(rs.getString("status"));
        a.setPaymentAmount(rs.getDouble("payment_amount"));
        a.setComments(rs.getString("comments"));
        a.setRating((Integer) rs.getObject("rating"));
        Timestamp ts = rs.getTimestamp("appointment_datetime");
        if (ts != null) a.setAppointmentDateTime(ts.toLocalDateTime());

        a.setCustomer(fetchCustomerByID(a.getCustomerId(), conn));
        a.setTechnician(fetchTechnicianByID(a.getTechnicianId(), conn));
        a.setService(fetchServiceByID(a.getServiceId(), conn));

        return a;
    }

    private Customer fetchCustomerByID(String customerID, Connection conn) throws SQLException {
        String sql = "SELECT * FROM CUSTOMER WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer c = new Customer(
                        rs.getString("customer_id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("password"),
                        rs.getString("gender"), rs.getString("phone"),
                        rs.getString("ic"), rs.getString("address"));
                rs.close();
                return c;
            }
            rs.close();
        }
        return null;
    }

    private Technician fetchTechnicianByID(String technicianID, Connection conn) throws SQLException {
        String sql = "SELECT * FROM TECHNICIAN WHERE technician_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Technician t = new Technician(
                        rs.getString("technician_id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("password"),
                        rs.getString("gender"), rs.getString("phone"),
                        rs.getString("ic"), rs.getString("address"),
                        rs.getString("specialty"), rs.getBoolean("available"));
                rs.close();
                return t;
            }
            rs.close();
        }
        return null;
    }

    private Service fetchServiceByID(String serviceID, Connection conn) throws SQLException {
        String sql = "SELECT * FROM SERVICE WHERE service_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serviceID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Service s = new Service(
                        rs.getString("service_id"), rs.getString("service_name"),
                        rs.getString("type"), rs.getDouble("base_price"));
                rs.close();
                return s;
            }
            rs.close();
        }
        return null;
    }
}
