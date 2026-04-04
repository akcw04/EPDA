package facade;

import entity.Payment;
import util.DatabaseConnection;
import util.IDGenerator;

import jakarta.ejb.Stateless;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateless EJB Facade for Payment CRUD operations using JDBC.
 * Manages payment receipts and transaction records.
 */
@Stateless
public class PaymentFacade {

    public boolean createPayment(Payment payment) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String paymentID = IDGenerator.generatePaymentID(conn);
            payment.setId(paymentID);

            if (payment.getReceiptNumber() == null) {
                payment.setReceiptNumber("RCP-" + System.currentTimeMillis());
            }

            String sql = "INSERT INTO PAYMENT (payment_id, appointment_id, customer_id, amount, " +
                         "payment_method, payment_date, receipt_number, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, payment.getId());
                ps.setString(2, payment.getAppointmentId());
                ps.setString(3, payment.getCustomerId());
                ps.setDouble(4, payment.getAmount());
                ps.setString(5, payment.getPaymentMethod());
                ps.setTimestamp(6, Timestamp.valueOf(
                        payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDateTime.now()));
                ps.setString(7, payment.getReceiptNumber());
                ps.setString(8, payment.getStatus() != null ? payment.getStatus() : "Completed");
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Payment getPaymentById(String paymentId) throws SQLException {
        String sql = "SELECT * FROM PAYMENT WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Payment p = mapResultSet(rs);
                rs.close();
                return p;
            }
            rs.close();
            return null;
        }
    }

    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM PAYMENT ORDER BY payment_date DESC";
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

    public List<Payment> getPaymentsByCustomer(String customerId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM PAYMENT WHERE customer_id = ? ORDER BY payment_date DESC";
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

    public List<Payment> getPaymentsByAppointment(String appointmentId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM PAYMENT WHERE appointment_id = ? ORDER BY payment_date DESC";
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

    public double getTotalPaymentsByCustomer(String customerId) throws SQLException {
        String sql = "SELECT SUM(amount) FROM PAYMENT WHERE customer_id = ? AND status = 'Completed'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();
            double total = 0.0;
            if (rs.next()) total = rs.getDouble(1);
            rs.close();
            return total;
        }
    }

    public boolean updatePayment(Payment payment) throws SQLException {
        String sql = "UPDATE PAYMENT SET status = ?, payment_method = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, payment.getStatus());
            ps.setString(2, payment.getPaymentMethod());
            ps.setString(3, payment.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePayment(String paymentId) throws SQLException {
        String sql = "DELETE FROM PAYMENT WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Payment mapResultSet(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getString("payment_id"));
        p.setAppointmentId(rs.getString("appointment_id"));
        p.setCustomerId(rs.getString("customer_id"));
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) p.setPaymentDate(ts.toLocalDateTime());
        p.setReceiptNumber(rs.getString("receipt_number"));
        p.setStatus(rs.getString("status"));
        return p;
    }
}
