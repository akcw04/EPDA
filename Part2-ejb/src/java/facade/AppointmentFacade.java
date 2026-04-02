package facade;

import entity.*;
import util.*;

import jakarta.ejb.Stateless;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AppointmentFacade - Stateless EJB for appointment management.
 * Implements:
 * - Appointment CRUD operations
 * - Concurrency validation (no overlapping technician scheduling)
 * - 5 required reporting methods
 * Uses raw JDBC with try-with-resources.
 */
@Stateless
public class AppointmentFacade {

    /**
     * Create a new appointment.
     * Validates technician availability before scheduling.
     * Generates ID in Java-side before database write.
     *
     * @param appointment the appointment to create
     * @return the appointment with generated ID
     * @throws SQLException if database error or validation fails
     */
    public Appointment createAppointment(Appointment appointment) throws SQLException {
        // Validate technician availability (no overlapping appointments)
        validateTechnicianAvailability(appointment);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Generate ID in Java-side
            String appointmentID = IDGenerator.generateAppointmentID(conn);
            appointment.setId(appointmentID);

            // Insert into Appointment table
            String sql = "INSERT INTO Appointment (" +
                    "appointment_id, customer_id, technician_id, service_id, " +
                    "appointment_datetime, status, payment_amount, comments, rating) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, appointment.getId());
                ps.setString(2, appointment.getCustomer().getId());
                ps.setString(3, appointment.getTechnician().getId());
                ps.setString(4, appointment.getService().getId());
                ps.setTimestamp(5, Timestamp.valueOf(appointment.getAppointmentDateTime()));
                ps.setString(6, appointment.getStatus());
                ps.setDouble(7, appointment.getPaymentAmount());
                ps.setString(8, appointment.getComments());
                ps.setObject(9, appointment.getRating());

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Failed to insert appointment");
                }
            }
            return appointment;
        } catch (SQLException e) {
            System.err.println("Error creating appointment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Retrieve an appointment by ID.
     *
     * @param appointmentID the appointment ID
     * @return Appointment object with nested Customer, Technician, Service
     * @throws SQLException if database error occurs
     */
    public Appointment getAppointmentByID(String appointmentID) throws SQLException {
        String sql = "SELECT a.appointment_id, a.customer_id, a.technician_id, " +
                "a.service_id, a.appointment_datetime, a.status, a.payment_amount, " +
                "a.comments, a.rating FROM Appointment a WHERE a.appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Appointment appointment = mapResultSetToAppointment(rs, conn);
                rs.close();
                return appointment;
            }
            rs.close();
            return null;
        } catch (SQLException e) {
            System.err.println("Error retrieving appointment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Update an appointment.
     *
     * @param appointment the appointment with updated values
     * @throws SQLException if database error occurs
     */
    public void updateAppointment(Appointment appointment) throws SQLException {
        String sql = "UPDATE Appointment SET status = ?, payment_amount = ?, " +
                "comments = ?, rating = ? WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointment.getStatus());
            ps.setDouble(2, appointment.getPaymentAmount());
            ps.setString(3, appointment.getComments());
            ps.setObject(4, appointment.getRating());
            ps.setString(5, appointment.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Appointment not found or not updated");
            }
        } catch (SQLException e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Delete an appointment.
     *
     * @param appointmentID the ID of appointment to delete
     * @throws SQLException if database error occurs
     */
    public void deleteAppointment(String appointmentID) throws SQLException {
        String sql = "DELETE FROM Appointment WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentID);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Appointment not found or not deleted");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get all appointments for a customer.
     *
     * @param customerID the customer ID
     * @return List of appointments
     * @throws SQLException if database error occurs
     */
    public List<Appointment> getAppointmentsByCustomer(String customerID) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.customer_id, a.technician_id, " +
                "a.service_id, a.appointment_datetime, a.status, a.payment_amount, " +
                "a.comments, a.rating FROM Appointment a WHERE a.customer_id = ? ORDER BY a.appointment_datetime DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs, conn));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving appointments by customer: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return appointments;
    }

    // ========== BUSINESS LOGIC: CONCURRENCY VALIDATION ==========

    /**
     * Validate technician availability.
     * Ensure no overlapping appointments for the same technician.
     * Normal service = 1 hour; Major = 3 hours.
     *
     * @param appointment the appointment to validate
     * @throws SQLException if validation fails or database error occurs
     */
    private void validateTechnicianAvailability(Appointment appointment) throws SQLException {
        String technicianID = appointment.getTechnician().getId();
        LocalDateTime startTime = appointment.getAppointmentDateTime();

        // Calculate end time based on service type
        int durationMinutes = appointment.getService().getDurationMinutes();
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        // Derby-compatible: use {fn TIMESTAMPADD(...)} JDBC escape syntax
        String sql = "SELECT COUNT(*) FROM Appointment a " +
                "JOIN Service s ON a.service_id = s.service_id " +
                "WHERE a.technician_id = ? " +
                "AND a.status IN ('Pending', 'InProgress') " +
                "AND a.appointment_datetime < ? " +
                "AND {fn TIMESTAMPADD(SQL_TSI_MINUTE, " +
                "CASE WHEN s.type = 'Normal' THEN 60 ELSE 180 END, " +
                "a.appointment_datetime)} > ?";

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
        } catch (SQLException e) {
            System.err.println("Technician availability validation failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ========== REPORTING METHODS (5 Required) ==========

    /**
     * REPORT 1: Daily Revenue
     * Sum of paymentAmount where date = CURRENT_DATE
     *
     * @return total revenue for today
     * @throws SQLException if database error occurs
     */
    public double getDailyRevenue() throws SQLException {
        String sql = "SELECT SUM(payment_amount) FROM Appointment " +
                "WHERE status = 'Completed' AND CAST(appointment_datetime AS DATE) = CURRENT_DATE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            double revenue = 0.0;
            if (rs.next()) {
                revenue = rs.getDouble(1);
            }
            rs.close();
            return revenue;
        } catch (SQLException e) {
            System.err.println("Error calculating daily revenue: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * REPORT 2: Technician Workload
     * Group by TechnicianID to count active tasks (Pending + InProgress)
     *
     * @return Map with technician IDs and their active task count
     * @throws SQLException if database error occurs
     */
    public Map<String, Integer> getTechnicianWorkload() throws SQLException {
        Map<String, Integer> workload = new HashMap<>();
        String sql = "SELECT technician_id, COUNT(*) as task_count FROM Appointment " +
                "WHERE status IN ('Pending', 'InProgress') " +
                "GROUP BY technician_id ORDER BY task_count DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                workload.put(rs.getString("technician_id"), rs.getInt("task_count"));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error calculating technician workload: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return workload;
    }

    /**
     * REPORT 3: Service Popularity
     * Count distribution of service types (Normal vs Major, etc.)
     *
     * @return Map with service names and count
     * @throws SQLException if database error occurs
     */
    public Map<String, Integer> getServicePopularity() throws SQLException {
        Map<String, Integer> popularity = new LinkedHashMap<>();
        String sql = "SELECT s.service_name, COUNT(a.appointment_id) as service_count " +
                "FROM Service s LEFT JOIN Appointment a ON s.service_id = a.service_id " +
                "GROUP BY s.service_id, s.service_name ORDER BY service_count DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                popularity.put(rs.getString("service_name"), rs.getInt("service_count"));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error calculating service popularity: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return popularity;
    }

    /**
     * REPORT 4: Customer Feedback
     * Extract all non-null comments and ratings
     *
     * @return List of feedback entries (comment, rating, customer name)
     * @throws SQLException if database error occurs
     */
    public List<Map<String, Object>> getCustomerFeedback() throws SQLException {
        List<Map<String, Object>> feedback = new ArrayList<>();
        String sql = "SELECT c.name, a.comments, a.rating FROM Appointment a " +
                "JOIN Customer c ON a.customer_id = c.customer_id " +
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
        } catch (SQLException e) {
            System.err.println("Error retrieving customer feedback: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return feedback;
    }

    /**
     * REPORT 5: Status Analytics
     * Calculate ratio of Pending vs Completed appointments
     *
     * @return Map with status counts and calculated ratios
     * @throws SQLException if database error occurs
     */
    public Map<String, Object> getStatusAnalytics() throws SQLException {
        Map<String, Object> analytics = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as status_count FROM Appointment GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            
            int pendingCount = 0;
            int completedCount = 0;
            int inProgressCount = 0;
            int cancelledCount = 0;
            int totalCount = 0;

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("status_count");
                totalCount += count;

                if ("Pending".equals(status)) {
                    pendingCount = count;
                } else if ("Completed".equals(status)) {
                    completedCount = count;
                } else if ("InProgress".equals(status)) {
                    inProgressCount = count;
                } else if ("Cancelled".equals(status)) {
                    cancelledCount = count;
                }
            }
            rs.close();

            analytics.put("pending", pendingCount);
            analytics.put("completed", completedCount);
            analytics.put("inProgress", inProgressCount);
            analytics.put("cancelled", cancelledCount);
            analytics.put("total", totalCount);

            // Calculate ratios
            if (totalCount > 0) {
                analytics.put("pendingRatio", (double) pendingCount / totalCount);
                analytics.put("completedRatio", (double) completedCount / totalCount);
            } else {
                analytics.put("pendingRatio", 0.0);
                analytics.put("completedRatio", 0.0);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating status analytics: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return analytics;
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Map a ResultSet row to an Appointment object with nested entities.
     * Follows "One ResultRow to One Object" pattern.
     *
     * @param rs the ResultSet
     * @param conn database connection for fetching related objects
     * @return populated Appointment object
     * @throws SQLException if database error occurs
     */
    private Appointment mapResultSetToAppointment(ResultSet rs, Connection conn) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(rs.getString("appointment_id"));
        appointment.setStatus(rs.getString("status"));
        appointment.setPaymentAmount(rs.getDouble("payment_amount"));
        appointment.setComments(rs.getString("comments"));
        appointment.setRating((Integer) rs.getObject("rating"));

        Timestamp ts = rs.getTimestamp("appointment_datetime");
        if (ts != null) {
            appointment.setAppointmentDateTime(ts.toLocalDateTime());
        }

        // Fetch nested Customer object
        Customer customer = fetchCustomerByID(rs.getString("customer_id"), conn);
        appointment.setCustomer(customer);

        // Fetch nested Technician object
        Technician technician = fetchTechnicianByID(rs.getString("technician_id"), conn);
        appointment.setTechnician(technician);

        // Fetch nested Service object
        Service service = fetchServiceByID(rs.getString("service_id"), conn);
        appointment.setService(service);

        return appointment;
    }

    /**
     * Fetch a customer from the database by ID.
     *
     * @param customerID the customer ID
     * @param conn database connection
     * @return Customer object or null if not found
     * @throws SQLException if database error occurs
     */
    private Customer fetchCustomerByID(String customerID, Connection conn) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer c = new Customer(
                        rs.getString("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                );
                rs.close();
                return c;
            }
            rs.close();
        }
        return null;
    }

    /**
     * Fetch a technician from the database by ID.
     *
     * @param technicianID the technician ID
     * @param conn database connection
     * @return Technician object or null if not found
     * @throws SQLException if database error occurs
     */
    private Technician fetchTechnicianByID(String technicianID, Connection conn) throws SQLException {
        String sql = "SELECT * FROM Technician WHERE technician_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Technician t = new Technician(
                        rs.getString("technician_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("specialty"),
                        rs.getBoolean("available")
                );
                rs.close();
                return t;
            }
            rs.close();
        }
        return null;
    }

    /**
     * Fetch a service from the database by ID.
     *
     * @param serviceID the service ID
     * @param conn database connection
     * @return Service object or null if not found
     * @throws SQLException if database error occurs
     */
    private Service fetchServiceByID(String serviceID, Connection conn) throws SQLException {
        String sql = "SELECT * FROM Service WHERE service_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serviceID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Service s = new Service(
                        rs.getString("service_id"),
                        rs.getString("service_name"),
                        rs.getString("type"),
                        rs.getDouble("base_price")
                );
                rs.close();
                return s;
            }
            rs.close();
        }
        return null;
    }
}
