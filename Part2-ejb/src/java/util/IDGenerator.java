package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ID Generator utility for manual ID generation.
 * Requirement: IDs must be generated in Java before prepareStatement.
 * Format examples: T-001, T-002, C-001, S-001, A-001, M-001
 * (T=Technician, C=Customer, S=Service, A=Appointment, M=Manager)
 */
public class IDGenerator {

    /**
     * Generate next ID for a given entity type.
     * Queries current count and formats ID accordingly.
     *
     * @param connection database connection
     * @param tableName the table to query (e.g., "Technician", "Customer")
     * @param prefix ID prefix (e.g., "T", "C", "S")
     * @return formatted ID string
     * @throws SQLException if database query fails
     */
    public static String generateNextID(Connection connection, String tableName, String prefix) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            
            // Format: prefix-XXX (3-digit zero-padded)
            int nextNumber = count + 1;
            return String.format("%s-%03d", prefix, nextNumber);
        } catch (SQLException e) {
            System.err.println("Failed to generate ID: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Generate ID for Technician
     *
     * @param connection database connection
     * @return new technician ID
     * @throws SQLException if database query fails
     */
    public static String generateTechnicianID(Connection connection) throws SQLException {
        return generateNextID(connection, "Technician", "T");
    }

    /**
     * Generate ID for Customer
     *
     * @param connection database connection
     * @return new customer ID
     * @throws SQLException if database query fails
     */
    public static String generateCustomerID(Connection connection) throws SQLException {
        return generateNextID(connection, "Customer", "C");
    }

    /**
     * Generate ID for Service
     *
     * @param connection database connection
     * @return new service ID
     * @throws SQLException if database query fails
     */
    public static String generateServiceID(Connection connection) throws SQLException {
        return generateNextID(connection, "Service", "S");
    }

    /**
     * Generate ID for Appointment
     *
     * @param connection database connection
     * @return new appointment ID
     * @throws SQLException if database query fails
     */
    public static String generateAppointmentID(Connection connection) throws SQLException {
        return generateNextID(connection, "Appointment", "A");
    }

    /**
     * Generate ID for Manager
     *
     * @param connection database connection
     * @return new manager ID
     * @throws SQLException if database query fails
     */
    public static String generateManagerID(Connection connection) throws SQLException {
        return generateNextID(connection, "Manager", "M");
    }
}
