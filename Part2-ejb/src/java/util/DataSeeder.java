package util;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Singleton EJB that seeds initial test data into the Apache Derby database
 * on application deployment. Checks if data already exists before seeding
 * to avoid duplicate inserts on redeployment.
 */
@Singleton
@Startup
public class DataSeeder {

    @PostConstruct
    public void init() {
        try {
            seedData();
        } catch (Exception e) {
            System.err.println("[DataSeeder] FAILED to seed initial data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedData() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            // Check if data already exists
            if (dataExists(conn)) {
                System.out.println("[DataSeeder] Data already exists. Skipping seed.");
                return;
            }

            System.out.println("[DataSeeder] Seeding initial data...");

            seedManagers(conn);
            seedCounterStaff(conn);
            seedTechnicians(conn);
            seedCustomers(conn);
            seedServices(conn);
            seedAppointments(conn);
            seedPayments(conn);
            seedFeedback(conn);
            seedAppointmentComments(conn);

            System.out.println("[DataSeeder] Initial data seeded successfully.");

        } catch (SQLException e) {
            System.err.println("[DataSeeder] SQL error during seeding: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Check if the MANAGER table already has rows.
     * If yes, assume data has been seeded previously.
     */
    private boolean dataExists(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM MANAGER";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // ==================== MANAGERS ====================

    private void seedManagers(Connection conn) throws SQLException {
        String sql = "INSERT INTO MANAGER (manager_id, name, email, password, gender, phone, ic, address) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String hashedPassword = SecurityUtil.hashPassword("admin123");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // M-001
            ps.setString(1, "M-001");
            ps.setString(2, "Admin Manager");
            ps.setString(3, "admin@asc.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "M");
            ps.setString(6, "012-1111111");
            ps.setString(7, "900101-01-1111");
            ps.setString(8, "APU Office");
            ps.executeUpdate();

            // M-002
            ps.setString(1, "M-002");
            ps.setString(2, "Sarah Admin");
            ps.setString(3, "sarah@asc.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "F");
            ps.setString(6, "012-2222222");
            ps.setString(7, "910202-02-2222");
            ps.setString(8, "APU Office");
            ps.executeUpdate();
        }

        System.out.println("[DataSeeder] Managers seeded.");
    }

    // ==================== COUNTER STAFF ====================

    private void seedCounterStaff(Connection conn) throws SQLException {
        String sql = "INSERT INTO COUNTER_STAFF (counter_staff_id, name, email, password, gender, phone, ic, address) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String hashedPassword = SecurityUtil.hashPassword("staff123");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // CS-001
            ps.setString(1, "CS-001");
            ps.setString(2, "John Counter");
            ps.setString(3, "staff@asc.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "M");
            ps.setString(6, "013-1111111");
            ps.setString(7, "920301-01-3333");
            ps.setString(8, "Front Desk");
            ps.executeUpdate();

            // CS-002
            ps.setString(1, "CS-002");
            ps.setString(2, "Mary Counter");
            ps.setString(3, "mary@asc.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "F");
            ps.setString(6, "013-2222222");
            ps.setString(7, "930402-02-4444");
            ps.setString(8, "Front Desk");
            ps.executeUpdate();
        }

        System.out.println("[DataSeeder] Counter Staff seeded.");
    }

    // ==================== TECHNICIANS ====================

    private void seedTechnicians(Connection conn) throws SQLException {
        String sql = "INSERT INTO TECHNICIAN (technician_id, name, email, password, gender, phone, ic, address, specialty, available) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String hashedPassword = SecurityUtil.hashPassword("tech123");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // T-001
            ps.setString(1, "T-001");
            ps.setString(2, "Ali Technician");
            ps.setString(3, "ali@asc.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "M");
            ps.setString(6, "014-1111111");
            ps.setString(7, "880501-01-5555");
            ps.setString(8, "Workshop A");
            ps.setString(9, "Engine Repair");
            ps.setBoolean(10, true);
            ps.executeUpdate();

            // T-002
            ps.setString(1, "T-002");
            ps.setString(2, "Bala Technician");
            ps.setString(3, "bala@asc.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "M");
            ps.setString(6, "014-2222222");
            ps.setString(7, "890602-02-6666");
            ps.setString(8, "Workshop B");
            ps.setString(9, "Brake System");
            ps.setBoolean(10, true);
            ps.executeUpdate();

            // T-003
            ps.setString(1, "T-003");
            ps.setString(2, "Chong Technician");
            ps.setString(3, "chong@asc.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "M");
            ps.setString(6, "014-3333333");
            ps.setString(7, "870703-03-7777");
            ps.setString(8, "Workshop C");
            ps.setString(9, "General Service");
            ps.setBoolean(10, true);
            ps.executeUpdate();
        }

        System.out.println("[DataSeeder] Technicians seeded.");
    }

    // ==================== CUSTOMERS ====================

    private void seedCustomers(Connection conn) throws SQLException {
        String sql = "INSERT INTO CUSTOMER (customer_id, name, email, password, gender, phone, ic, address) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String hashedPassword = SecurityUtil.hashPassword("cust123");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // C-001
            ps.setString(1, "C-001");
            ps.setString(2, "David Customer");
            ps.setString(3, "david@cust.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "M");
            ps.setString(6, "015-1111111");
            ps.setString(7, "950801-01-8888");
            ps.setString(8, "123 Jalan Utama");
            ps.executeUpdate();

            // C-002
            ps.setString(1, "C-002");
            ps.setString(2, "Emily Customer");
            ps.setString(3, "emily@cust.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "F");
            ps.setString(6, "015-2222222");
            ps.setString(7, "960902-02-9999");
            ps.setString(8, "456 Jalan Dua");
            ps.executeUpdate();

            // C-003
            ps.setString(1, "C-003");
            ps.setString(2, "Faisal Customer");
            ps.setString(3, "faisal@cust.com");
            ps.setString(4, hashedPassword);
            ps.setString(5, "M");
            ps.setString(6, "015-3333333");
            ps.setString(7, "971003-03-0000");
            ps.setString(8, "789 Jalan Tiga");
            ps.executeUpdate();
        }

        System.out.println("[DataSeeder] Customers seeded.");
    }

    // ==================== SERVICES ====================

    private void seedServices(Connection conn) throws SQLException {
        String sql = "INSERT INTO SERVICE (service_id, service_name, type, duration_minutes, base_price) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // S-001
            ps.setString(1, "S-001");
            ps.setString(2, "Oil Change");
            ps.setString(3, "Normal");
            ps.setInt(4, 60);
            ps.setDouble(5, 89.90);
            ps.executeUpdate();

            // S-002
            ps.setString(1, "S-002");
            ps.setString(2, "Brake Pad Replacement");
            ps.setString(3, "Normal");
            ps.setInt(4, 60);
            ps.setDouble(5, 149.90);
            ps.executeUpdate();

            // S-003
            ps.setString(1, "S-003");
            ps.setString(2, "Full Engine Service");
            ps.setString(3, "Major");
            ps.setInt(4, 180);
            ps.setDouble(5, 499.90);
            ps.executeUpdate();

            // S-004
            ps.setString(1, "S-004");
            ps.setString(2, "Tire Rotation & Balance");
            ps.setString(3, "Normal");
            ps.setInt(4, 60);
            ps.setDouble(5, 59.90);
            ps.executeUpdate();

            // S-005
            ps.setString(1, "S-005");
            ps.setString(2, "Transmission Service");
            ps.setString(3, "Major");
            ps.setInt(4, 180);
            ps.setDouble(5, 699.90);
            ps.executeUpdate();
        }

        System.out.println("[DataSeeder] Services seeded.");
    }

    // ==================== APPOINTMENTS ====================

    private void seedAppointments(Connection conn) throws SQLException {
        String sql = "INSERT INTO APPOINTMENT (appointment_id, customer_id, technician_id, service_id, "
                   + "appointment_datetime, status, payment_amount) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime twoDaysFromNow = now.plusDays(2);
        LocalDateTime threeDaysAgo = now.minusDays(3);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // A-001: C-001, T-001, S-001, yesterday 10:00, Completed
            ps.setString(1, "A-001");
            ps.setString(2, "C-001");
            ps.setString(3, "T-001");
            ps.setString(4, "S-001");
            ps.setTimestamp(5, Timestamp.valueOf(yesterday.withHour(10).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(6, "Completed");
            ps.setDouble(7, 89.90);
            ps.executeUpdate();

            // A-002: C-001, T-002, S-003, yesterday 14:00, Completed
            ps.setString(1, "A-002");
            ps.setString(2, "C-001");
            ps.setString(3, "T-002");
            ps.setString(4, "S-003");
            ps.setTimestamp(5, Timestamp.valueOf(yesterday.withHour(14).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(6, "Completed");
            ps.setDouble(7, 499.90);
            ps.executeUpdate();

            // A-003: C-002, T-001, S-002, today 09:00, InProgress
            ps.setString(1, "A-003");
            ps.setString(2, "C-002");
            ps.setString(3, "T-001");
            ps.setString(4, "S-002");
            ps.setTimestamp(5, Timestamp.valueOf(now.withHour(9).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(6, "InProgress");
            ps.setDouble(7, 149.90);
            ps.executeUpdate();

            // A-004: C-002, T-003, S-004, today 14:00, Pending
            ps.setString(1, "A-004");
            ps.setString(2, "C-002");
            ps.setString(3, "T-003");
            ps.setString(4, "S-004");
            ps.setTimestamp(5, Timestamp.valueOf(now.withHour(14).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(6, "Pending");
            ps.setDouble(7, 59.90);
            ps.executeUpdate();

            // A-005: C-003, T-002, S-005, tomorrow 10:00, Pending
            ps.setString(1, "A-005");
            ps.setString(2, "C-003");
            ps.setString(3, "T-002");
            ps.setString(4, "S-005");
            ps.setTimestamp(5, Timestamp.valueOf(tomorrow.withHour(10).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(6, "Pending");
            ps.setDouble(7, 699.90);
            ps.executeUpdate();

            // A-006: C-003, T-001, S-001, tomorrow 14:00, Pending
            ps.setString(1, "A-006");
            ps.setString(2, "C-003");
            ps.setString(3, "T-001");
            ps.setString(4, "S-001");
            ps.setTimestamp(5, Timestamp.valueOf(tomorrow.withHour(14).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(6, "Pending");
            ps.setDouble(7, 89.90);
            ps.executeUpdate();

            // A-007: C-001, T-003, S-002, 2 days from now 10:00, Pending
            ps.setString(1, "A-007");
            ps.setString(2, "C-001");
            ps.setString(3, "T-003");
            ps.setString(4, "S-002");
            ps.setTimestamp(5, Timestamp.valueOf(twoDaysFromNow.withHour(10).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(6, "Pending");
            ps.setDouble(7, 149.90);
            ps.executeUpdate();

            // A-008: C-002, T-002, S-003, 3 days ago 10:00, Cancelled
            ps.setString(1, "A-008");
            ps.setString(2, "C-002");
            ps.setString(3, "T-002");
            ps.setString(4, "S-003");
            ps.setTimestamp(5, Timestamp.valueOf(threeDaysAgo.withHour(10).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(6, "Cancelled");
            ps.setDouble(7, 499.90);
            ps.executeUpdate();
        }

        System.out.println("[DataSeeder] Appointments seeded.");
    }

    // ==================== PAYMENTS ====================

    private void seedPayments(Connection conn) throws SQLException {
        String sql = "INSERT INTO PAYMENT (payment_id, appointment_id, customer_id, amount, "
                   + "payment_method, payment_date, receipt_number, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // PY-001: A-001, C-001, 89.90, Cash
            ps.setString(1, "PY-001");
            ps.setString(2, "A-001");
            ps.setString(3, "C-001");
            ps.setDouble(4, 89.90);
            ps.setString(5, "Cash");
            ps.setTimestamp(6, Timestamp.valueOf(yesterday.withHour(11).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(7, "REC-20250401-001");
            ps.setString(8, "Completed");
            ps.executeUpdate();

            // PY-002: A-002, C-001, 499.90, Card
            ps.setString(1, "PY-002");
            ps.setString(2, "A-002");
            ps.setString(3, "C-001");
            ps.setDouble(4, 499.90);
            ps.setString(5, "Card");
            ps.setTimestamp(6, Timestamp.valueOf(yesterday.withHour(17).withMinute(0).withSecond(0).withNano(0)));
            ps.setString(7, "REC-20250401-002");
            ps.setString(8, "Completed");
            ps.executeUpdate();
        }

        System.out.println("[DataSeeder] Payments seeded.");
    }

    // ==================== FEEDBACK ====================

    private void seedFeedback(Connection conn) throws SQLException {
        String sql = "INSERT INTO FEEDBACK (feedback_id, appointment_id, technician_id, feedback_text, created_at) "
                   + "VALUES (?, ?, ?, ?, ?)";

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // FB-001: A-001, T-001
            ps.setString(1, "FB-001");
            ps.setString(2, "A-001");
            ps.setString(3, "T-001");
            ps.setString(4, "Vehicle oil changed successfully. Engine running smoothly.");
            ps.setTimestamp(5, Timestamp.valueOf(yesterday.withHour(11).withMinute(30).withSecond(0).withNano(0)));
            ps.executeUpdate();

            // FB-002: A-002, T-002
            ps.setString(1, "FB-002");
            ps.setString(2, "A-002");
            ps.setString(3, "T-002");
            ps.setString(4, "Complete engine overhaul done. All components checked and replaced.");
            ps.setTimestamp(5, Timestamp.valueOf(yesterday.withHour(17).withMinute(30).withSecond(0).withNano(0)));
            ps.executeUpdate();
        }

        System.out.println("[DataSeeder] Feedback seeded.");
    }

    // ==================== APPOINTMENT COMMENTS ====================

    private void seedAppointmentComments(Connection conn) throws SQLException {
        String sql = "INSERT INTO APPOINTMENT_COMMENT (comment_id, appointment_id, customer_id, "
                   + "comment_text, rating, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // CM-001: A-001, C-001, rating 5
            ps.setString(1, "CM-001");
            ps.setString(2, "A-001");
            ps.setString(3, "C-001");
            ps.setString(4, "Great service! Ali was very professional.");
            ps.setInt(5, 5);
            ps.setTimestamp(6, Timestamp.valueOf(yesterday.withHour(12).withMinute(0).withSecond(0).withNano(0)));
            ps.executeUpdate();

            // CM-002: A-002, C-001, rating 4
            ps.setString(1, "CM-002");
            ps.setString(2, "A-002");
            ps.setString(3, "C-001");
            ps.setString(4, "Excellent work on the engine. Took longer than expected but quality work.");
            ps.setInt(5, 4);
            ps.setTimestamp(6, Timestamp.valueOf(yesterday.withHour(18).withMinute(0).withSecond(0).withNano(0)));
            ps.executeUpdate();
        }

        System.out.println("[DataSeeder] Appointment Comments seeded.");
    }
}
