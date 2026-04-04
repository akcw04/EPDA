package facade;

import entity.Service;
import util.*;

import jakarta.ejb.Stateless;
import java.sql.*;
import java.util.*;

/**
 * ServiceFacade - Stateless EJB for service management.
 * Handles CRUD operations for available services.
 * Services define duration: Normal = 1 hour, Major = 3 hours.
 * Uses raw JDBC with try-with-resources.
 */
@Stateless
public class ServiceFacade {

    /**
     * Create a new service.
     * Generates ID in Java-side before database write.
     *
     * @param service the service to create
     * @return the service with generated ID
     * @throws SQLException if database error occurs
     */
    public Service createService(Service service) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String serviceID = IDGenerator.generateServiceID(conn);
            service.setId(serviceID);

            String sql = "INSERT INTO SERVICE " +
                    "(service_id, service_name, type, duration_minutes, base_price) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, service.getId());
                ps.setString(2, service.getServiceName());
                ps.setString(3, service.getType());
                ps.setInt(4, service.getDurationMinutes());
                ps.setDouble(5, service.getBasePrice());

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Failed to insert service");
                }
            }
            return service;
        } catch (SQLException e) {
            System.err.println("Error creating service: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Retrieve a service by ID.
     *
     * @param serviceID the service ID
     * @return Service object or null if not found
     * @throws SQLException if database error occurs
     */
    public Service getServiceByID(String serviceID) throws SQLException {
        String sql = "SELECT * FROM SERVICE WHERE service_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serviceID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Service service = new Service(
                        rs.getString("service_id"),
                        rs.getString("service_name"),
                        rs.getString("type"),
                        rs.getDouble("base_price")
                );
                rs.close();
                return service;
            }
            rs.close();
            return null;
        } catch (SQLException e) {
            System.err.println("Error retrieving service: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get all services.
     *
     * @return List of all services
     * @throws SQLException if database error occurs
     */
    public List<Service> getAllServices() throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM SERVICE ORDER BY service_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                services.add(new Service(
                        rs.getString("service_id"),
                        rs.getString("service_name"),
                        rs.getString("type"),
                        rs.getDouble("base_price")
                ));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving all services: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return services;
    }

    /**
     * Get services by type (Normal or Major).
     *
     * @param type "Normal" or "Major"
     * @return List of services of the specified type
     * @throws SQLException if database error occurs
     */
    public List<Service> getServicesByType(String type) throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM SERVICE WHERE type = ? ORDER BY service_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                services.add(new Service(
                        rs.getString("service_id"),
                        rs.getString("service_name"),
                        rs.getString("type"),
                        rs.getDouble("base_price")
                ));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving services by type: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return services;
    }

    /**
     * Update a service.
     *
     * @param service the service with updated values
     * @throws SQLException if database error occurs
     */
    public void updateService(Service service) throws SQLException {
        String sql = "UPDATE SERVICE SET service_name = ?, type = ?, duration_minutes = ?, base_price = ? " +
                "WHERE service_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service.getServiceName());
            ps.setString(2, service.getType());
            ps.setInt(3, service.getDurationMinutes());
            ps.setDouble(4, service.getBasePrice());
            ps.setString(5, service.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Service not found or not updated");
            }
        } catch (SQLException e) {
            System.err.println("Error updating service: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Delete a service.
     *
     * @param serviceID the ID of service to delete
     * @throws SQLException if database error occurs
     */
    public void deleteService(String serviceID) throws SQLException {
        String sql = "DELETE FROM SERVICE WHERE service_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serviceID);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Service not found or not deleted");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting service: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
