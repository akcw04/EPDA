package facade;

import entity.*;
import util.*;

import jakarta.ejb.Stateless;
import java.sql.*;
import java.util.*;

/**
 * UserFacade - Stateless EJB for user management using JDBC.
 * Handles CRUD for Manager, CounterStaff, Technician, Customer.
 * All user types have: name, password, gender, phone, IC, email, address.
 */
@Stateless
public class UserFacade {

    // ========== TECHNICIAN OPERATIONS ==========

    public boolean createTechnician(Technician technician) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String id = IDGenerator.generateTechnicianID(conn);
            technician.setId(id);
            String hashedPassword = SecurityUtil.hashPassword(technician.getPassword());

            String sql = "INSERT INTO TECHNICIAN (technician_id, name, email, password, gender, phone, ic, address, specialty, available) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, technician.getId());
                ps.setString(2, technician.getName());
                ps.setString(3, technician.getEmail());
                ps.setString(4, hashedPassword);
                ps.setString(5, technician.getGender());
                ps.setString(6, technician.getPhone());
                ps.setString(7, technician.getIc());
                ps.setString(8, technician.getAddress());
                ps.setString(9, technician.getSpecialty());
                ps.setBoolean(10, technician.isAvailable());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating technician: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Technician getTechnicianByID(String technicianID) throws SQLException {
        String sql = "SELECT * FROM TECHNICIAN WHERE technician_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Technician t = mapTechnician(rs);
                rs.close();
                return t;
            }
            rs.close();
            return null;
        }
    }

    public List<Technician> getAllTechnicians() throws SQLException {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM TECHNICIAN ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTechnician(rs));
            rs.close();
        }
        return list;
    }

    public List<Technician> searchTechnicians(String keyword) throws SQLException {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM TECHNICIAN WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(specialty) LIKE ? ORDER BY name";
        String kw = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTechnician(rs));
            rs.close();
        }
        return list;
    }

    public void updateTechnician(Technician technician) throws SQLException {
        String sql = "UPDATE TECHNICIAN SET name = ?, email = ?, gender = ?, phone = ?, ic = ?, address = ?, " +
                     "specialty = ?, available = ? WHERE technician_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technician.getName());
            ps.setString(2, technician.getEmail());
            ps.setString(3, technician.getGender());
            ps.setString(4, technician.getPhone());
            ps.setString(5, technician.getIc());
            ps.setString(6, technician.getAddress());
            ps.setString(7, technician.getSpecialty());
            ps.setBoolean(8, technician.isAvailable());
            ps.setString(9, technician.getId());
            ps.executeUpdate();
        }
    }

    public void deleteTechnician(String technicianID) throws SQLException {
        String sql = "DELETE FROM TECHNICIAN WHERE technician_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianID);
            ps.executeUpdate();
        }
    }

    // ========== CUSTOMER OPERATIONS ==========

    public boolean createCustomer(Customer customer) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String id = IDGenerator.generateCustomerID(conn);
            customer.setId(id);
            String hashedPassword = SecurityUtil.hashPassword(customer.getPassword());

            String sql = "INSERT INTO CUSTOMER (customer_id, name, email, password, gender, phone, ic, address) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, customer.getId());
                ps.setString(2, customer.getName());
                ps.setString(3, customer.getEmail());
                ps.setString(4, hashedPassword);
                ps.setString(5, customer.getGender());
                ps.setString(6, customer.getPhone());
                ps.setString(7, customer.getIc());
                ps.setString(8, customer.getAddress());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Customer getCustomerByID(String customerID) throws SQLException {
        String sql = "SELECT * FROM CUSTOMER WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer c = mapCustomer(rs);
                rs.close();
                return c;
            }
            rs.close();
            return null;
        }
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM CUSTOMER ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapCustomer(rs));
            rs.close();
        }
        return list;
    }

    public List<Customer> searchCustomers(String keyword) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM CUSTOMER WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(phone) LIKE ? ORDER BY name";
        String kw = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapCustomer(rs));
            rs.close();
        }
        return list;
    }

    public void updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE CUSTOMER SET name = ?, email = ?, gender = ?, phone = ?, ic = ?, address = ? " +
                     "WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getGender());
            ps.setString(4, customer.getPhone());
            ps.setString(5, customer.getIc());
            ps.setString(6, customer.getAddress());
            ps.setString(7, customer.getId());
            ps.executeUpdate();
        }
    }

    public void deleteCustomer(String customerID) throws SQLException {
        String sql = "DELETE FROM CUSTOMER WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            ps.executeUpdate();
        }
    }

    // ========== MANAGER OPERATIONS ==========

    public boolean createManager(Manager manager) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String id = IDGenerator.generateManagerID(conn);
            manager.setId(id);
            String hashedPassword = SecurityUtil.hashPassword(manager.getPassword());

            String sql = "INSERT INTO MANAGER (manager_id, name, email, password, gender, phone, ic, address) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, manager.getId());
                ps.setString(2, manager.getName());
                ps.setString(3, manager.getEmail());
                ps.setString(4, hashedPassword);
                ps.setString(5, manager.getGender());
                ps.setString(6, manager.getPhone());
                ps.setString(7, manager.getIc());
                ps.setString(8, manager.getAddress());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating manager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Manager getManagerByID(String managerID) throws SQLException {
        String sql = "SELECT * FROM MANAGER WHERE manager_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, managerID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Manager m = mapManager(rs);
                rs.close();
                return m;
            }
            rs.close();
            return null;
        }
    }

    public List<Manager> getAllManagers() throws SQLException {
        List<Manager> list = new ArrayList<>();
        String sql = "SELECT * FROM MANAGER ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapManager(rs));
            rs.close();
        }
        return list;
    }

    public List<Manager> searchManagers(String keyword) throws SQLException {
        List<Manager> list = new ArrayList<>();
        String sql = "SELECT * FROM MANAGER WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? ORDER BY name";
        String kw = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapManager(rs));
            rs.close();
        }
        return list;
    }

    public void updateManager(Manager manager) throws SQLException {
        String sql = "UPDATE MANAGER SET name = ?, email = ?, gender = ?, phone = ?, ic = ?, address = ? " +
                     "WHERE manager_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manager.getName());
            ps.setString(2, manager.getEmail());
            ps.setString(3, manager.getGender());
            ps.setString(4, manager.getPhone());
            ps.setString(5, manager.getIc());
            ps.setString(6, manager.getAddress());
            ps.setString(7, manager.getId());
            ps.executeUpdate();
        }
    }

    public void deleteManager(String managerID) throws SQLException {
        String sql = "DELETE FROM MANAGER WHERE manager_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, managerID);
            ps.executeUpdate();
        }
    }

    // ========== COUNTER STAFF OPERATIONS ==========

    public boolean createCounterStaff(CounterStaff counterStaff) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String id = IDGenerator.generateCounterStaffID(conn);
            counterStaff.setId(id);
            String hashedPassword = SecurityUtil.hashPassword(counterStaff.getPassword());

            String sql = "INSERT INTO COUNTER_STAFF (counter_staff_id, name, email, password, gender, phone, ic, address) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, counterStaff.getId());
                ps.setString(2, counterStaff.getName());
                ps.setString(3, counterStaff.getEmail());
                ps.setString(4, hashedPassword);
                ps.setString(5, counterStaff.getGender());
                ps.setString(6, counterStaff.getPhone());
                ps.setString(7, counterStaff.getIc());
                ps.setString(8, counterStaff.getAddress());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating counter staff: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public CounterStaff getCounterStaffByID(String counterStaffID) throws SQLException {
        String sql = "SELECT * FROM COUNTER_STAFF WHERE counter_staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counterStaffID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                CounterStaff cs = mapCounterStaff(rs);
                rs.close();
                return cs;
            }
            rs.close();
            return null;
        }
    }

    public List<CounterStaff> getAllCounterStaff() throws SQLException {
        List<CounterStaff> list = new ArrayList<>();
        String sql = "SELECT * FROM COUNTER_STAFF ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapCounterStaff(rs));
            rs.close();
        }
        return list;
    }

    public List<CounterStaff> searchCounterStaff(String keyword) throws SQLException {
        List<CounterStaff> list = new ArrayList<>();
        String sql = "SELECT * FROM COUNTER_STAFF WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? ORDER BY name";
        String kw = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapCounterStaff(rs));
            rs.close();
        }
        return list;
    }

    public void updateCounterStaff(CounterStaff counterStaff) throws SQLException {
        String sql = "UPDATE COUNTER_STAFF SET name = ?, email = ?, gender = ?, phone = ?, ic = ?, address = ? " +
                     "WHERE counter_staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counterStaff.getName());
            ps.setString(2, counterStaff.getEmail());
            ps.setString(3, counterStaff.getGender());
            ps.setString(4, counterStaff.getPhone());
            ps.setString(5, counterStaff.getIc());
            ps.setString(6, counterStaff.getAddress());
            ps.setString(7, counterStaff.getId());
            ps.executeUpdate();
        }
    }

    public void deleteCounterStaff(String counterStaffID) throws SQLException {
        String sql = "DELETE FROM COUNTER_STAFF WHERE counter_staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counterStaffID);
            ps.executeUpdate();
        }
    }

    // ========== AUTHENTICATION ==========

    public Object[] authenticate(String email, String plainPassword) {
        String hashedPassword = SecurityUtil.hashPassword(plainPassword);

        // Try Manager
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM MANAGER WHERE email = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, hashedPassword);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Manager m = mapManager(rs);
                    rs.close();
                    return new Object[]{m, "Manager"};
                }
                rs.close();
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Try CounterStaff
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM COUNTER_STAFF WHERE email = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, hashedPassword);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    CounterStaff cs = mapCounterStaff(rs);
                    rs.close();
                    return new Object[]{cs, "CounterStaff"};
                }
                rs.close();
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Try Technician
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM TECHNICIAN WHERE email = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, hashedPassword);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Technician t = mapTechnician(rs);
                    rs.close();
                    return new Object[]{t, "Technician"};
                }
                rs.close();
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Try Customer
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM CUSTOMER WHERE email = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, hashedPassword);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Customer c = mapCustomer(rs);
                    rs.close();
                    return new Object[]{c, "Customer"};
                }
                rs.close();
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return null;
    }

    // ========== EMAIL LOOKUP ==========

    public Customer getCustomerByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM CUSTOMER WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { Customer c = mapCustomer(rs); rs.close(); return c; }
            rs.close(); return null;
        }
    }

    public Technician getTechnicianByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM TECHNICIAN WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { Technician t = mapTechnician(rs); rs.close(); return t; }
            rs.close(); return null;
        }
    }

    public Manager getManagerByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM MANAGER WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { Manager m = mapManager(rs); rs.close(); return m; }
            rs.close(); return null;
        }
    }

    public CounterStaff getCounterStaffByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM COUNTER_STAFF WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { CounterStaff cs = mapCounterStaff(rs); rs.close(); return cs; }
            rs.close(); return null;
        }
    }

    // ========== HELPER MAPPERS ==========

    private Technician mapTechnician(ResultSet rs) throws SQLException {
        return new Technician(
                rs.getString("technician_id"), rs.getString("name"),
                rs.getString("email"), rs.getString("password"),
                rs.getString("gender"), rs.getString("phone"),
                rs.getString("ic"), rs.getString("address"),
                rs.getString("specialty"), rs.getBoolean("available"));
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("customer_id"), rs.getString("name"),
                rs.getString("email"), rs.getString("password"),
                rs.getString("gender"), rs.getString("phone"),
                rs.getString("ic"), rs.getString("address"));
    }

    private Manager mapManager(ResultSet rs) throws SQLException {
        return new Manager(
                rs.getString("manager_id"), rs.getString("name"),
                rs.getString("email"), rs.getString("password"),
                rs.getString("gender"), rs.getString("phone"),
                rs.getString("ic"), rs.getString("address"));
    }

    private CounterStaff mapCounterStaff(ResultSet rs) throws SQLException {
        return new CounterStaff(
                rs.getString("counter_staff_id"), rs.getString("name"),
                rs.getString("email"), rs.getString("password"),
                rs.getString("gender"), rs.getString("phone"),
                rs.getString("ic"), rs.getString("address"));
    }
}
