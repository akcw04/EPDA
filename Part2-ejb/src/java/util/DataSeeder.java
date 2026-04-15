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
import java.util.List;

/**
 * Singleton EJB that seeds initial test data into the database on
 * application deployment. Skips seeding if data already exists.
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
        if (dataExists()) {
            System.out.println("[DataSeeder] Data already exists. Checking for missing data...");
            seedMissing();
            return;
        }

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

    private boolean dataExists() {
        Long count = em.createQuery("SELECT COUNT(m) FROM Manager m", Long.class)
                .getSingleResult();
        return count != null && count > 0;
    }

    private void seedMissing() {
        // Always sync the documented demo accounts so login test credentials remain valid.
        seedManagers();
        seedCounterStaff();
        seedTechnicians();
        seedCustomers();

        Long svcCount = em.createQuery("SELECT COUNT(s) FROM Service s", Long.class).getSingleResult();
        if (svcCount == null || svcCount == 0) {
            System.out.println("[DataSeeder] Services missing. Re-seeding...");
            seedServices();
        }
    }

    // ==================== MANAGERS ====================

    private void seedManagers() {
        upsertManager("M-001", "Admin Manager", "admin@asc.com", "admin123",
                "M", "012-1111111", "900101-01-1111", "APU Office");
        upsertManager("M-002", "Sarah Admin", "sarah@asc.com", "admin123",
                "F", "012-2222222", "910202-02-2222", "APU Office");

        System.out.println("[DataSeeder] Managers seeded.");
    }

    // ==================== COUNTER STAFF ====================

    private void seedCounterStaff() {
        upsertCounterStaff("CS-001", "John Counter", "staff@asc.com", "staff123",
                "M", "013-1111111", "920301-01-3333", "Front Desk");
        upsertCounterStaff("CS-002", "Mary Counter", "mary@asc.com", "staff123",
                "F", "013-2222222", "930402-02-4444", "Front Desk");

        System.out.println("[DataSeeder] Counter Staff seeded.");
    }

    // ==================== TECHNICIANS ====================

    private void seedTechnicians() {
        upsertTechnician("T-001", "Ali Technician", "ali@asc.com", "tech123",
                "M", "014-1111111", "880501-01-5555", "Workshop A", "Engine Repair", true);
        upsertTechnician("T-002", "Bala Technician", "bala@asc.com", "tech123",
                "M", "014-2222222", "890602-02-6666", "Workshop B", "Brake System", true);
        upsertTechnician("T-003", "Chong Technician", "chong@asc.com", "tech123",
                "M", "014-3333333", "870703-03-7777", "Workshop C", "General Service", true);
        upsertTechnician("T-004", "Devi Technician", "devi@asc.com", "tech123",
                "F", "014-4444444", "910804-04-8888", "Workshop D", "Transmission", true);
        upsertTechnician("T-005", "Ethan Technician", "ethan@asc.com", "tech123",
                "M", "014-5555555", "920905-05-9999", "Workshop E", "Electrical System", true);

        System.out.println("[DataSeeder] Technicians seeded.");
    }

    // ==================== CUSTOMERS ====================

    private void seedCustomers() {
        upsertCustomer("C-001", "David Customer", "david@cust.com", "cust123",
                "M", "015-1111111", "950801-01-8888", "123 Jalan Utama");
        upsertCustomer("C-002", "Emily Customer", "emily@cust.com", "cust123",
                "F", "015-2222222", "960902-02-9999", "456 Jalan Dua");
        upsertCustomer("C-003", "Faisal Customer", "faisal@cust.com", "cust123",
                "M", "015-3333333", "971003-03-0000", "789 Jalan Tiga");
        upsertCustomer("C-004", "Grace Customer", "grace@cust.com", "cust123",
                "F", "015-4444444", "981104-04-1111", "12 Jalan Empat");
        upsertCustomer("C-005", "Hakim Customer", "hakim@cust.com", "cust123",
                "M", "015-5555555", "991205-05-2222", "34 Jalan Lima");
        upsertCustomer("C-006", "Irene Customer", "irene@cust.com", "cust123",
                "F", "015-6666666", "000106-06-3333", "56 Jalan Enam");
        upsertCustomer("C-007", "Jason Customer", "jason@cust.com", "cust123",
                "M", "015-7777777", "010207-07-4444", "78 Jalan Tujuh");
        upsertCustomer("C-008", "Kiran Customer", "kiran@cust.com", "cust123",
                "F", "015-8888888", "020308-08-5555", "90 Jalan Lapan");
        upsertCustomer("C-009", "Liam Customer", "liam@cust.com", "cust123",
                "M", "015-9999999", "030409-09-6666", "11 Jalan Sembilan");
        upsertCustomer("C-010", "Mira Customer", "mira@cust.com", "cust123",
                "F", "016-1010101", "040510-10-7777", "22 Jalan Sepuluh");

        System.out.println("[DataSeeder] Customers seeded.");
    }

    private void upsertManager(String id, String name, String email, String plainPassword,
                               String gender, String phone, String ic, String address) {
        String hashed = SecurityUtil.hashPassword(plainPassword);
        Manager manager = findManagerByIdOrEmail(id, email);
        if (manager == null) {
            manager = new Manager();
            manager.setId(id);
            em.persist(manager);
        }
        manager.setName(name);
        manager.setEmail(email);
        manager.setPassword(hashed);
        manager.setGender(gender);
        manager.setPhone(ValidationUtil.normalizePhone(phone));
        manager.setIc(ic);
        manager.setAddress(address);
    }

    private void upsertCounterStaff(String id, String name, String email, String plainPassword,
                                    String gender, String phone, String ic, String address) {
        String hashed = SecurityUtil.hashPassword(plainPassword);
        CounterStaff counterStaff = findCounterStaffByIdOrEmail(id, email);
        if (counterStaff == null) {
            counterStaff = new CounterStaff();
            counterStaff.setId(id);
            em.persist(counterStaff);
        }
        counterStaff.setName(name);
        counterStaff.setEmail(email);
        counterStaff.setPassword(hashed);
        counterStaff.setGender(gender);
        counterStaff.setPhone(ValidationUtil.normalizePhone(phone));
        counterStaff.setIc(ic);
        counterStaff.setAddress(address);
    }

    private void upsertTechnician(String id, String name, String email, String plainPassword,
                                  String gender, String phone, String ic, String address,
                                  String specialty, boolean available) {
        String hashed = SecurityUtil.hashPassword(plainPassword);
        Technician technician = findTechnicianByIdOrEmail(id, email);
        if (technician == null) {
            technician = new Technician();
            technician.setId(id);
            em.persist(technician);
        }
        technician.setName(name);
        technician.setEmail(email);
        technician.setPassword(hashed);
        technician.setGender(gender);
        technician.setPhone(ValidationUtil.normalizePhone(phone));
        technician.setIc(ic);
        technician.setAddress(address);
        technician.setSpecialty(specialty);
        technician.setAvailable(available);
    }

    private void upsertCustomer(String id, String name, String email, String plainPassword,
                                String gender, String phone, String ic, String address) {
        String hashed = SecurityUtil.hashPassword(plainPassword);
        Customer customer = findCustomerByIdOrEmail(id, email);
        if (customer == null) {
            customer = new Customer();
            customer.setId(id);
            em.persist(customer);
        }
        customer.setName(name);
        customer.setEmail(email);
        customer.setPassword(hashed);
        customer.setGender(gender);
        customer.setPhone(ValidationUtil.normalizePhone(phone));
        customer.setIc(ic);
        customer.setAddress(address);
    }

    private Manager findManagerByIdOrEmail(String id, String email) {
        Manager manager = em.find(Manager.class, id);
        if (manager != null) {
            return manager;
        }
        List<Manager> matches = em.createQuery(
                "SELECT m FROM Manager m WHERE m.email = :email", Manager.class)
                .setParameter("email", email)
                .getResultList();
        return matches.isEmpty() ? null : matches.get(0);
    }

    private CounterStaff findCounterStaffByIdOrEmail(String id, String email) {
        CounterStaff counterStaff = em.find(CounterStaff.class, id);
        if (counterStaff != null) {
            return counterStaff;
        }
        List<CounterStaff> matches = em.createQuery(
                "SELECT cs FROM CounterStaff cs WHERE cs.email = :email", CounterStaff.class)
                .setParameter("email", email)
                .getResultList();
        return matches.isEmpty() ? null : matches.get(0);
    }

    private Technician findTechnicianByIdOrEmail(String id, String email) {
        Technician technician = em.find(Technician.class, id);
        if (technician != null) {
            return technician;
        }
        List<Technician> matches = em.createQuery(
                "SELECT t FROM Technician t WHERE t.email = :email", Technician.class)
                .setParameter("email", email)
                .getResultList();
        return matches.isEmpty() ? null : matches.get(0);
    }

    private Customer findCustomerByIdOrEmail(String id, String email) {
        Customer customer = em.find(Customer.class, id);
        if (customer != null) {
            return customer;
        }
        List<Customer> matches = em.createQuery(
                "SELECT c FROM Customer c WHERE c.email = :email", Customer.class)
                .setParameter("email", email)
                .getResultList();
        return matches.isEmpty() ? null : matches.get(0);
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
