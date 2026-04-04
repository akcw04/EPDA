package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ID Generator utility for manual ID generation before database insert.
 * Format: PREFIX-NNN (3-digit zero-padded)
 */
public class IDGenerator {

    /**
     * Generate next ID for a given table.
     */
    public static String generateNextID(Connection connection, String tableName,
                                         String idColumnName, String prefix) throws SQLException {
        String sql = "SELECT MAX(" + idColumnName + ") FROM " + tableName;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int nextNumber = 1;
            if (rs.next() && rs.getString(1) != null) {
                String maxId = rs.getString(1);
                String[] parts = maxId.split("-");
                nextNumber = Integer.parseInt(parts[parts.length - 1]) + 1;
            }
            return String.format("%s-%03d", prefix, nextNumber);
        }
    }

    public static String generateTechnicianID(Connection conn) throws SQLException {
        return generateNextID(conn, "TECHNICIAN", "technician_id", "T");
    }

    public static String generateCustomerID(Connection conn) throws SQLException {
        return generateNextID(conn, "CUSTOMER", "customer_id", "C");
    }

    public static String generateServiceID(Connection conn) throws SQLException {
        return generateNextID(conn, "SERVICE", "service_id", "S");
    }

    public static String generateAppointmentID(Connection conn) throws SQLException {
        return generateNextID(conn, "APPOINTMENT", "appointment_id", "A");
    }

    public static String generateManagerID(Connection conn) throws SQLException {
        return generateNextID(conn, "MANAGER", "manager_id", "M");
    }

    public static String generateCounterStaffID(Connection conn) throws SQLException {
        return generateNextID(conn, "COUNTER_STAFF", "counter_staff_id", "CS");
    }

    public static String generateFeedbackID(Connection conn) throws SQLException {
        return generateNextID(conn, "FEEDBACK", "feedback_id", "FB");
    }

    public static String generatePaymentID(Connection conn) throws SQLException {
        return generateNextID(conn, "PAYMENT", "payment_id", "PY");
    }

    public static String generateCommentID(Connection conn) throws SQLException {
        return generateNextID(conn, "APPOINTMENT_COMMENT", "comment_id", "CM");
    }
}
