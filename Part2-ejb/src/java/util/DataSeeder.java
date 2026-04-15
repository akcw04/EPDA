package util;

import entity.Appointment;
import entity.AppointmentComment;
import entity.CounterStaff;
import entity.Customer;
import entity.Feedback;
import entity.Manager;
import entity.Payment;
import entity.Service;
import entity.Technician;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;

/**
 * Singleton EJB that seeds initial demo data into the database on
 * application deployment. Relies on persistence.xml drop-and-create
 * so the schema is fresh every deploy and records never collide.
 */
@Singleton
@Startup
public class DataSeeder {

    @PersistenceContext(unitName = "Part2-ejbPU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        try {
            seedData();
        } catch (Exception e) {
            System.err.println("[DataSeeder] FAILED to seed initial data: " + e.getMessage());
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void seedData() {
        // persistence.xml uses drop-and-create, so the schema is fresh on every deploy.
        System.out.println("[DataSeeder] Seeding initial data...");

        seedManagers();
        seedCounterStaff();
        seedTechnicians();
        seedCustomers();
        seedServices();
        seedAppointments();
        seedPayments();
        seedFeedback();
        seedAppointmentComments();

        System.out.println("[DataSeeder] Initial data seeded successfully.");
    }

    // ==================== MANAGERS ====================

    private void seedManagers() {
        createManager("M-001", "Admin Manager", "admin@asc.com", "admin123",
                "M", "012-1111111", "900101-01-1111", "APU Office");
        createManager("M-002", "Sarah Admin", "sarah@asc.com", "admin123",
                "F", "012-2222222", "910202-02-2222", "APU Office");

        System.out.println("[DataSeeder] Managers seeded.");
    }

    // ==================== COUNTER STAFF ====================

    private void seedCounterStaff() {
        createCounterStaff("CS-001", "John Counter", "staff@asc.com", "staff123",
                "M", "013-1111111", "920301-01-3333", "Front Desk");
        createCounterStaff("CS-002", "Mary Counter", "mary@asc.com", "staff123",
                "F", "013-2222222", "930402-02-4444", "Front Desk");

        System.out.println("[DataSeeder] Counter Staff seeded.");
    }

    // ==================== TECHNICIANS ====================

    private void seedTechnicians() {
        createTechnician("T-001", "Ali Technician", "ali@asc.com", "tech123",
                "M", "014-1111111", "880501-01-5555", "Workshop A", "Engine Repair", true);
        createTechnician("T-002", "Bala Technician", "bala@asc.com", "tech123",
                "M", "014-2222222", "890602-02-6666", "Workshop B", "Brake System", true);
        createTechnician("T-003", "Chong Technician", "chong@asc.com", "tech123",
                "M", "014-3333333", "870703-03-7777", "Workshop C", "General Service", true);
        createTechnician("T-004", "Devi Technician", "devi@asc.com", "tech123",
                "F", "014-4444444", "910804-04-8888", "Workshop D", "Transmission", true);
        createTechnician("T-005", "Ethan Technician", "ethan@asc.com", "tech123",
                "M", "014-5555555", "920905-05-9999", "Workshop E", "Electrical System", true);

        System.out.println("[DataSeeder] Technicians seeded.");
    }

    // ==================== CUSTOMERS ====================

    private void seedCustomers() {
        createCustomer("C-001", "David Customer", "david@cust.com", "cust123",
                "M", "015-1111111", "950801-01-8888", "123 Jalan Utama");
        createCustomer("C-002", "Emily Customer", "emily@cust.com", "cust123",
                "F", "015-2222222", "960902-02-9999", "456 Jalan Dua");
        createCustomer("C-003", "Faisal Customer", "faisal@cust.com", "cust123",
                "M", "015-3333333", "971003-03-0000", "789 Jalan Tiga");
        createCustomer("C-004", "Grace Customer", "grace@cust.com", "cust123",
                "F", "015-4444444", "981104-04-1111", "12 Jalan Empat");
        createCustomer("C-005", "Hakim Customer", "hakim@cust.com", "cust123",
                "M", "015-5555555", "991205-05-2222", "34 Jalan Lima");
        createCustomer("C-006", "Irene Customer", "irene@cust.com", "cust123",
                "F", "015-6666666", "000106-06-3333", "56 Jalan Enam");
        createCustomer("C-007", "Jason Customer", "jason@cust.com", "cust123",
                "M", "015-7777777", "010207-07-4444", "78 Jalan Tujuh");
        createCustomer("C-008", "Kiran Customer", "kiran@cust.com", "cust123",
                "F", "015-8888888", "020308-08-5555", "90 Jalan Lapan");
        createCustomer("C-009", "Liam Customer", "liam@cust.com", "cust123",
                "M", "015-9999999", "030409-09-6666", "11 Jalan Sembilan");
        createCustomer("C-010", "Mira Customer", "mira@cust.com", "cust123",
                "F", "016-1010101", "040510-10-7777", "22 Jalan Sepuluh");

        System.out.println("[DataSeeder] Customers seeded.");
    }

    private void createManager(String id, String name, String email, String plainPassword,
                               String gender, String phone, String ic, String address) {
        Manager manager = new Manager();
        manager.setId(id);
        manager.setName(name);
        manager.setEmail(email);
        manager.setPassword(SecurityUtil.hashPassword(plainPassword));
        manager.setGender(gender);
        manager.setPhone(ValidationUtil.normalizePhone(phone));
        manager.setIc(ic);
        manager.setAddress(address);
        em.persist(manager);
    }

    private void createCounterStaff(String id, String name, String email, String plainPassword,
                                    String gender, String phone, String ic, String address) {
        CounterStaff counterStaff = new CounterStaff();
        counterStaff.setId(id);
        counterStaff.setName(name);
        counterStaff.setEmail(email);
        counterStaff.setPassword(SecurityUtil.hashPassword(plainPassword));
        counterStaff.setGender(gender);
        counterStaff.setPhone(ValidationUtil.normalizePhone(phone));
        counterStaff.setIc(ic);
        counterStaff.setAddress(address);
        em.persist(counterStaff);
    }

    private void createTechnician(String id, String name, String email, String plainPassword,
                                  String gender, String phone, String ic, String address,
                                  String specialty, boolean available) {
        Technician technician = new Technician();
        technician.setId(id);
        technician.setName(name);
        technician.setEmail(email);
        technician.setPassword(SecurityUtil.hashPassword(plainPassword));
        technician.setGender(gender);
        technician.setPhone(ValidationUtil.normalizePhone(phone));
        technician.setIc(ic);
        technician.setAddress(address);
        technician.setSpecialty(specialty);
        technician.setAvailable(available);
        em.persist(technician);
    }

    private void createCustomer(String id, String name, String email, String plainPassword,
                                String gender, String phone, String ic, String address) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setEmail(email);
        customer.setPassword(SecurityUtil.hashPassword(plainPassword));
        customer.setGender(gender);
        customer.setPhone(ValidationUtil.normalizePhone(phone));
        customer.setIc(ic);
        customer.setAddress(address);
        em.persist(customer);
    }

    // ==================== SERVICES ====================

    private void seedServices() {
        Service s1 = new Service("S-001", "Oil Change", "Normal", 89.90);
        em.persist(s1);

        Service s2 = new Service("S-002", "Brake Pad Replacement", "Normal", 149.90);
        em.persist(s2);

        Service s3 = new Service("S-003", "Full Engine Service", "Major", 499.90);
        em.persist(s3);

        Service s4 = new Service("S-004", "Tire Rotation & Balance", "Normal", 59.90);
        em.persist(s4);

        Service s5 = new Service("S-005", "Transmission Service", "Major", 699.90);
        em.persist(s5);

        System.out.println("[DataSeeder] Services seeded.");
    }

    // ==================== APPOINTMENTS ====================

    private void seedAppointments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime twoDaysAgo = now.minusDays(2);
        LocalDateTime threeDaysAgo = now.minusDays(3);
        LocalDateTime fourDaysAgo = now.minusDays(4);
        LocalDateTime fiveDaysAgo = now.minusDays(5);
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime twoDaysFromNow = now.plusDays(2);

        // ----- Completed appointments (5) -----
        persistAppointment("A-001", "C-001", "T-001", "S-001",
                yesterday.withHour(10).withMinute(0).withSecond(0).withNano(0),
                "Completed", 89.90);

        persistAppointment("A-002", "C-001", "T-002", "S-003",
                yesterday.withHour(14).withMinute(0).withSecond(0).withNano(0),
                "Completed", 499.90);

        persistAppointment("A-009", "C-004", "T-003", "S-002",
                twoDaysAgo.withHour(9).withMinute(0).withSecond(0).withNano(0),
                "Completed", 149.90);

        persistAppointment("A-010", "C-005", "T-004", "S-005",
                fourDaysAgo.withHour(11).withMinute(0).withSecond(0).withNano(0),
                "Completed", 699.90);

        persistAppointment("A-011", "C-006", "T-005", "S-004",
                fiveDaysAgo.withHour(15).withMinute(0).withSecond(0).withNano(0),
                "Completed", 59.90);

        // ----- Ongoing (InProgress) appointments (2) -----
        persistAppointment("A-003", "C-002", "T-001", "S-002",
                now.withHour(9).withMinute(0).withSecond(0).withNano(0),
                "InProgress", 149.90);

        persistAppointment("A-012", "C-007", "T-004", "S-003",
                now.withHour(11).withMinute(0).withSecond(0).withNano(0),
                "InProgress", 499.90);

        // ----- Pending appointments -----
        persistAppointment("A-004", "C-002", "T-003", "S-004",
                now.withHour(14).withMinute(0).withSecond(0).withNano(0),
                "Pending", 59.90);

        persistAppointment("A-005", "C-003", "T-002", "S-005",
                tomorrow.withHour(10).withMinute(0).withSecond(0).withNano(0),
                "Pending", 699.90);

        persistAppointment("A-006", "C-003", "T-001", "S-001",
                tomorrow.withHour(14).withMinute(0).withSecond(0).withNano(0),
                "Pending", 89.90);

        persistAppointment("A-007", "C-001", "T-003", "S-002",
                twoDaysFromNow.withHour(10).withMinute(0).withSecond(0).withNano(0),
                "Pending", 149.90);

        persistAppointment("A-013", "C-008", "T-005", "S-001",
                tomorrow.withHour(16).withMinute(0).withSecond(0).withNano(0),
                "Pending", 89.90);

        // ----- Cancelled appointment -----
        persistAppointment("A-008", "C-002", "T-002", "S-003",
                threeDaysAgo.withHour(10).withMinute(0).withSecond(0).withNano(0),
                "Cancelled", 499.90);

        System.out.println("[DataSeeder] Appointments seeded.");
    }

    private void persistAppointment(String id, String customerId, String technicianId,
                                    String serviceId, LocalDateTime dateTime,
                                    String status, double paymentAmount) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setCustomerId(customerId);
        a.setTechnicianId(technicianId);
        a.setServiceId(serviceId);
        a.setAppointmentDateTime(dateTime);
        a.setStatus(status);
        a.setPaymentAmount(paymentAmount);
        em.persist(a);
    }

    // ==================== PAYMENTS ====================

    private void seedPayments() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        LocalDateTime fourDaysAgo = LocalDateTime.now().minusDays(4);
        LocalDateTime fiveDaysAgo = LocalDateTime.now().minusDays(5);

        Payment p1 = new Payment("PY-001", "A-001", "C-001", 89.90, "Cash");
        p1.setPaymentDate(yesterday.withHour(11).withMinute(0).withSecond(0).withNano(0));
        p1.setReceiptNumber("REC-20250401-001");
        p1.setStatus("Completed");
        em.persist(p1);

        Payment p2 = new Payment("PY-002", "A-002", "C-001", 499.90, "Card");
        p2.setPaymentDate(yesterday.withHour(17).withMinute(0).withSecond(0).withNano(0));
        p2.setReceiptNumber("REC-20250401-002");
        p2.setStatus("Completed");
        em.persist(p2);

        Payment p3 = new Payment("PY-003", "A-009", "C-004", 149.90, "Card");
        p3.setPaymentDate(twoDaysAgo.withHour(10).withMinute(0).withSecond(0).withNano(0));
        p3.setReceiptNumber("REC-20250401-003");
        p3.setStatus("Completed");
        em.persist(p3);

        Payment p4 = new Payment("PY-004", "A-010", "C-005", 699.90, "Card");
        p4.setPaymentDate(fourDaysAgo.withHour(12).withMinute(0).withSecond(0).withNano(0));
        p4.setReceiptNumber("REC-20250401-004");
        p4.setStatus("Completed");
        em.persist(p4);

        Payment p5 = new Payment("PY-005", "A-011", "C-006", 59.90, "Cash");
        p5.setPaymentDate(fiveDaysAgo.withHour(16).withMinute(0).withSecond(0).withNano(0));
        p5.setReceiptNumber("REC-20250401-005");
        p5.setStatus("Completed");
        em.persist(p5);

        System.out.println("[DataSeeder] Payments seeded.");
    }

    // ==================== FEEDBACK ====================

    private void seedFeedback() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        Feedback f1 = new Feedback("FB-001", "A-001", "T-001",
                "Vehicle oil changed successfully. Engine running smoothly.");
        f1.setCreatedAt(yesterday.withHour(11).withMinute(30).withSecond(0).withNano(0));
        em.persist(f1);

        Feedback f2 = new Feedback("FB-002", "A-002", "T-002",
                "Complete engine overhaul done. All components checked and replaced.");
        f2.setCreatedAt(yesterday.withHour(17).withMinute(30).withSecond(0).withNano(0));
        em.persist(f2);

        System.out.println("[DataSeeder] Feedback seeded.");
    }

    // ==================== APPOINTMENT COMMENTS ====================

    private void seedAppointmentComments() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        AppointmentComment cm1 = new AppointmentComment("CM-001", "A-001", "C-001",
                "Great service! Ali was very professional.", 5);
        cm1.setCreatedAt(yesterday.withHour(12).withMinute(0).withSecond(0).withNano(0));
        em.persist(cm1);

        AppointmentComment cm2 = new AppointmentComment("CM-002", "A-002", "C-001",
                "Excellent work on the engine. Took longer than expected but quality work.", 4);
        cm2.setCreatedAt(yesterday.withHour(18).withMinute(0).withSecond(0).withNano(0));
        em.persist(cm2);

        System.out.println("[DataSeeder] Appointment Comments seeded.");
    }
}
