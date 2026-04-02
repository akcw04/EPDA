package facade;

import entity.*;
import util.*;

import jakarta.ejb.Stateless;
import java.sql.*;
import java.util.*;

/**
 * UserFacade - Stateless EJB for user management.
 * Handles CRUD operations for Technician, Customer, and Manager.
 * Password hashing via SHA-256 in the Business Tier (EJB).
 * Uses raw JDBC with try-with-resources.
 */
@Stateless
public class UserFacade {

    // ========== TECHNICIAN OPERATIONS ==========

    /**
     * Create a new technician.
     * Generates ID in Java-side, hashes password via SecurityUtil before database write.
     *
     * @param technician the technician to create (password must be set in plain text)
     * @return true if created successfully, false otherwise
     */
    public boolean createTechnician(Technician technician) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String technicianID = IDGenerator.generateTechnicianID(conn);
            technician.setId(technicianID);

            String hashedPassword = SecurityUtil.hashPassword(technician.getPassword());

            String sql = "INSERT INTO Technician " +
                    "(technician_id, name, email, specialty, available, password) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, technician.getId());
                ps.setString(2, technician.getName());
                ps.setString(3, technician.getEmail());
                ps.setString(4, technician.getSpecialty());
                ps.setBoolean(5, technician.isAvailable());
                ps.setString(6, hashedPassword);

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating technician: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieve a technician by ID.
     *
     * @param technicianID the technician ID
     * @return Technician object or null if not found
     * @throws SQLException if database error occurs
     */
    public Technician getTechnicianByID(String technicianID) throws SQLException {
        String sql = "SELECT * FROM Technician WHERE technician_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Technician technician = new Technician(
                        rs.getString("technician_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("specialty"),
                        rs.getBoolean("available")
                );
                rs.close();
                return technician;
            }
            rs.close();
            return null;
        } catch (SQLException e) {
            System.err.println("Error retrieving technician: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get all technicians.
     *
     * @return List of all technicians
     * @throws SQLException if database error occurs
     */
    public List<Technician> getAllTechnicians() throws SQLException {
        List<Technician> technicians = new ArrayList<>();
        String sql = "SELECT * FROM Technician ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                technicians.add(new Technician(
                        rs.getString("technician_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("specialty"),
                        rs.getBoolean("available")
                ));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving all technicians: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return technicians;
    }

    /**
     * Update a technician.
     *
     * @param technician the technician with updated values
     * @throws SQLException if database error occurs
     */
    public void updateTechnician(Technician technician) throws SQLException {
        String sql = "UPDATE Technician SET name = ?, email = ?, specialty = ?, available = ? " +
                "WHERE technician_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technician.getName());
            ps.setString(2, technician.getEmail());
            ps.setString(3, technician.getSpecialty());
            ps.setBoolean(4, technician.isAvailable());
            ps.setString(5, technician.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Technician not found or not updated");
            }
        } catch (SQLException e) {
            System.err.println("Error updating technician: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Delete a technician.
     *
     * @param technicianID the ID of technician to delete
     * @throws SQLException if database error occurs
     */
    public void deleteTechnician(String technicianID) throws SQLException {
        String sql = "DELETE FROM Technician WHERE technician_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianID);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Technician not found or not deleted");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting technician: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ========== CUSTOMER OPERATIONS ==========

    /**
     * Create a new customer.
     * Generates ID in Java-side before database write. No password hashing for customers.
     *
     * @param customer the customer to create
     * @return true if created successfully, false otherwise
     */
    public boolean createCustomer(Customer customer) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String customerID = IDGenerator.generateCustomerID(conn);
            customer.setId(customerID);

            String sql = "INSERT INTO Customer " +
                    "(customer_id, name, email, phone, address) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, customer.getId());
                ps.setString(2, customer.getName());
                ps.setString(3, customer.getEmail());
                ps.setString(4, customer.getPhone());
                ps.setString(5, customer.getAddress());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieve a customer by ID.
     *
     * @param customerID the customer ID
     * @return Customer object or null if not found
     * @throws SQLException if database error occurs
     */
    public Customer getCustomerByID(String customerID) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Customer customer = new Customer(
                        rs.getString("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                );
                rs.close();
                return customer;
            }
            rs.close();
            return null;
        } catch (SQLException e) {
            System.err.println("Error retrieving customer: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get all customers.
     *
     * @return List of all customers
     * @throws SQLException if database error occurs
     */
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customer ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getString("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving all customers: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return customers;
    }

    /**
     * Update a customer.
     *
     * @param customer the customer with updated values
     * @throws SQLException if database error occurs
     */
    public void updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE Customer SET name = ?, email = ?, phone = ?, address = ? " +
                "WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Customer not found or not updated");
            }
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Delete a customer.
     *
     * @param customerID the ID of customer to delete
     * @throws SQLException if database error occurs
     */
    public void deleteCustomer(String customerID) throws SQLException {
        String sql = "DELETE FROM Customer WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Customer not found or not deleted");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ========== AUTHENTICATION ==========

    /**
     * Authenticate a user by email and plain-text password.
     * Hashes the password and checks against all three user tables.
     *
     * @param email the user's email
     * @param plainPassword the plain-text password
     * @return an Object[] { entity, role } where role is "Manager"/"Technician", or null if not found
     */
    public Object[] authenticate(String email, String plainPassword) {
        String hashedPassword = SecurityUtil.hashPassword(plainPassword);

        // Try Manager first
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Manager WHERE email = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, hashedPassword);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Manager m = new Manager(
                            rs.getString("manager_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password")
                    );
                    rs.close();
                    return new Object[]{m, "Manager"};
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error during manager authentication: " + e.getMessage());
            e.printStackTrace();
        }

        // Try Technician
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Technician WHERE email = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, hashedPassword);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Technician t = new Technician(
                            rs.getString("technician_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("specialty"),
                            rs.getBoolean("available"),
                            rs.getString("password")
                    );
                    rs.close();
                    return new Object[]{t, "Technician"};
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error during technician authentication: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Authentication failed
    }

    // ========== MANAGER OPERATIONS ==========

    /**
     * Create a new manager.
     * Generates ID in Java-side, hashes password via SecurityUtil before database write.
     *
     * @param manager the manager to create (password must be set in plain text)
     * @return true if created successfully, false otherwise
     */
    public boolean createManager(Manager manager) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String managerID = IDGenerator.generateManagerID(conn);
            manager.setId(managerID);

            String hashedPassword = SecurityUtil.hashPassword(manager.getPassword());

            String sql = "INSERT INTO Manager (manager_id, name, email, password) VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, manager.getId());
                ps.setString(2, manager.getName());
                ps.setString(3, manager.getEmail());
                ps.setString(4, hashedPassword);

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating manager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieve a manager by ID.
     *
     * @param managerID the manager ID
     * @return Manager object or null if not found
     * @throws SQLException if database error occurs
     */
    public Manager getManagerByID(String managerID) throws SQLException {
        String sql = "SELECT * FROM Manager WHERE manager_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, managerID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Manager manager = new Manager(
                        rs.getString("manager_id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
                rs.close();
                return manager;
            }
            rs.close();
            return null;
        } catch (SQLException e) {
            System.err.println("Error retrieving manager: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get all managers.
     *
     * @return List of all managers
     * @throws SQLException if database error occurs
     */
    public List<Manager> getAllManagers() throws SQLException {
        List<Manager> managers = new ArrayList<>();
        String sql = "SELECT * FROM Manager ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                managers.add(new Manager(
                        rs.getString("manager_id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving all managers: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return managers;
    }

    /**
     * Update a manager.
     *
     * @param manager the manager with updated values
     * @throws SQLException if database error occurs
     */
    public void updateManager(Manager manager) throws SQLException {
        String sql = "UPDATE Manager SET name = ?, email = ? WHERE manager_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manager.getName());
            ps.setString(2, manager.getEmail());
            ps.setString(3, manager.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Manager not found or not updated");
            }
        } catch (SQLException e) {
            System.err.println("Error updating manager: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Delete a manager.
     *
     * @param managerID the ID of manager to delete
     * @throws SQLException if database error occurs
     */
    public void deleteManager(String managerID) throws SQLException {
        String sql = "DELETE FROM Manager WHERE manager_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, managerID);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Manager not found or not deleted");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting manager: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
