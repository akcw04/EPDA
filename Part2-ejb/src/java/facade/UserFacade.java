package facade;

import entity.CounterStaff;
import entity.Customer;
import entity.Manager;
import entity.Technician;
import util.IDGenerator;
import util.SecurityUtil;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.util.List;

/**
 * UserFacade - Stateless EJB for user management.
 * Handles CRUD for Manager, CounterStaff, Technician, Customer.
 * All user types have: name, password, gender, phone, IC, email, address.
 */
@Stateless
public class UserFacade {

    @PersistenceContext(unitName = "Part2-ejbPU")
    private EntityManager em;

    // ========== TECHNICIAN OPERATIONS ==========

    public boolean createTechnician(Technician technician) {
        try {
            technician.setId(IDGenerator.generateTechnicianID(em));
            technician.setPassword(SecurityUtil.hashPassword(technician.getPassword()));
            em.persist(technician);
            return true;
        } catch (PersistenceException e) {
            System.err.println("Error creating technician: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Technician getTechnicianByID(String technicianID) {
        return em.find(Technician.class, technicianID);
    }

    public List<Technician> getAllTechnicians() {
        return em.createQuery("SELECT t FROM Technician t ORDER BY t.name", Technician.class)
                .getResultList();
    }

    public List<Technician> searchTechnicians(String keyword) {
        String kw = "%" + keyword.toLowerCase() + "%";
        return em.createQuery(
                "SELECT t FROM Technician t WHERE LOWER(t.name) LIKE :kw " +
                "OR LOWER(t.email) LIKE :kw OR LOWER(t.specialty) LIKE :kw ORDER BY t.name",
                Technician.class)
                .setParameter("kw", kw)
                .getResultList();
    }

    public void updateTechnician(Technician technician) {
        Technician managed = em.find(Technician.class, technician.getId());
        if (managed == null) return;
        managed.setName(technician.getName());
        managed.setEmail(technician.getEmail());
        managed.setGender(technician.getGender());
        managed.setPhone(technician.getPhone());
        managed.setIc(technician.getIc());
        managed.setAddress(technician.getAddress());
        managed.setSpecialty(technician.getSpecialty());
        managed.setAvailable(technician.isAvailable());
    }

    public void deleteTechnician(String technicianID) {
        Technician t = em.find(Technician.class, technicianID);
        if (t != null) em.remove(t);
    }

    // ========== CUSTOMER OPERATIONS ==========

    public boolean createCustomer(Customer customer) {
        try {
            customer.setId(IDGenerator.generateCustomerID(em));
            customer.setPassword(SecurityUtil.hashPassword(customer.getPassword()));
            em.persist(customer);
            return true;
        } catch (PersistenceException e) {
            System.err.println("Error creating customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Customer getCustomerByID(String customerID) {
        return em.find(Customer.class, customerID);
    }

    public List<Customer> getAllCustomers() {
        return em.createQuery("SELECT c FROM Customer c ORDER BY c.name", Customer.class)
                .getResultList();
    }

    public List<Customer> searchCustomers(String keyword) {
        String kw = "%" + keyword.toLowerCase() + "%";
        return em.createQuery(
                "SELECT c FROM Customer c WHERE LOWER(c.name) LIKE :kw " +
                "OR LOWER(c.email) LIKE :kw OR LOWER(c.phone) LIKE :kw ORDER BY c.name",
                Customer.class)
                .setParameter("kw", kw)
                .getResultList();
    }

    public void updateCustomer(Customer customer) {
        Customer managed = em.find(Customer.class, customer.getId());
        if (managed == null) return;
        managed.setName(customer.getName());
        managed.setEmail(customer.getEmail());
        managed.setGender(customer.getGender());
        managed.setPhone(customer.getPhone());
        managed.setIc(customer.getIc());
        managed.setAddress(customer.getAddress());
    }

    public void deleteCustomer(String customerID) {
        Customer c = em.find(Customer.class, customerID);
        if (c != null) em.remove(c);
    }

    // ========== MANAGER OPERATIONS ==========

    public boolean createManager(Manager manager) {
        try {
            manager.setId(IDGenerator.generateManagerID(em));
            manager.setPassword(SecurityUtil.hashPassword(manager.getPassword()));
            em.persist(manager);
            return true;
        } catch (PersistenceException e) {
            System.err.println("Error creating manager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Manager getManagerByID(String managerID) {
        return em.find(Manager.class, managerID);
    }

    public List<Manager> getAllManagers() {
        return em.createQuery("SELECT m FROM Manager m ORDER BY m.name", Manager.class)
                .getResultList();
    }

    public List<Manager> searchManagers(String keyword) {
        String kw = "%" + keyword.toLowerCase() + "%";
        return em.createQuery(
                "SELECT m FROM Manager m WHERE LOWER(m.name) LIKE :kw " +
                "OR LOWER(m.email) LIKE :kw ORDER BY m.name", Manager.class)
                .setParameter("kw", kw)
                .getResultList();
    }

    public void updateManager(Manager manager) {
        Manager managed = em.find(Manager.class, manager.getId());
        if (managed == null) return;
        managed.setName(manager.getName());
        managed.setEmail(manager.getEmail());
        managed.setGender(manager.getGender());
        managed.setPhone(manager.getPhone());
        managed.setIc(manager.getIc());
        managed.setAddress(manager.getAddress());
    }

    public void deleteManager(String managerID) {
        Manager m = em.find(Manager.class, managerID);
        if (m != null) em.remove(m);
    }

    // ========== COUNTER STAFF OPERATIONS ==========

    public boolean createCounterStaff(CounterStaff counterStaff) {
        try {
            counterStaff.setId(IDGenerator.generateCounterStaffID(em));
            counterStaff.setPassword(SecurityUtil.hashPassword(counterStaff.getPassword()));
            em.persist(counterStaff);
            return true;
        } catch (PersistenceException e) {
            System.err.println("Error creating counter staff: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public CounterStaff getCounterStaffByID(String counterStaffID) {
        return em.find(CounterStaff.class, counterStaffID);
    }

    public List<CounterStaff> getAllCounterStaff() {
        return em.createQuery("SELECT cs FROM CounterStaff cs ORDER BY cs.name", CounterStaff.class)
                .getResultList();
    }

    public List<CounterStaff> searchCounterStaff(String keyword) {
        String kw = "%" + keyword.toLowerCase() + "%";
        return em.createQuery(
                "SELECT cs FROM CounterStaff cs WHERE LOWER(cs.name) LIKE :kw " +
                "OR LOWER(cs.email) LIKE :kw ORDER BY cs.name", CounterStaff.class)
                .setParameter("kw", kw)
                .getResultList();
    }

    public void updateCounterStaff(CounterStaff counterStaff) {
        CounterStaff managed = em.find(CounterStaff.class, counterStaff.getId());
        if (managed == null) return;
        managed.setName(counterStaff.getName());
        managed.setEmail(counterStaff.getEmail());
        managed.setGender(counterStaff.getGender());
        managed.setPhone(counterStaff.getPhone());
        managed.setIc(counterStaff.getIc());
        managed.setAddress(counterStaff.getAddress());
    }

    public void deleteCounterStaff(String counterStaffID) {
        CounterStaff cs = em.find(CounterStaff.class, counterStaffID);
        if (cs != null) em.remove(cs);
    }

    // ========== AUTHENTICATION ==========

    public Object[] authenticate(String email, String plainPassword) {
        String hashed = SecurityUtil.hashPassword(plainPassword);

        Manager m = findManagerByCredentials(email, hashed);
        if (m != null) return new Object[]{m, "Manager"};

        CounterStaff cs = findCounterStaffByCredentials(email, hashed);
        if (cs != null) return new Object[]{cs, "CounterStaff"};

        Technician t = findTechnicianByCredentials(email, hashed);
        if (t != null) return new Object[]{t, "Technician"};

        Customer c = findCustomerByCredentials(email, hashed);
        if (c != null) return new Object[]{c, "Customer"};

        return null;
    }

    private Manager findManagerByCredentials(String email, String hashed) {
        List<Manager> list = em.createQuery(
                "SELECT m FROM Manager m WHERE m.email = :e AND m.password = :p", Manager.class)
                .setParameter("e", email).setParameter("p", hashed).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private CounterStaff findCounterStaffByCredentials(String email, String hashed) {
        List<CounterStaff> list = em.createQuery(
                "SELECT cs FROM CounterStaff cs WHERE cs.email = :e AND cs.password = :p",
                CounterStaff.class)
                .setParameter("e", email).setParameter("p", hashed).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private Technician findTechnicianByCredentials(String email, String hashed) {
        List<Technician> list = em.createQuery(
                "SELECT t FROM Technician t WHERE t.email = :e AND t.password = :p",
                Technician.class)
                .setParameter("e", email).setParameter("p", hashed).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private Customer findCustomerByCredentials(String email, String hashed) {
        List<Customer> list = em.createQuery(
                "SELECT c FROM Customer c WHERE c.email = :e AND c.password = :p",
                Customer.class)
                .setParameter("e", email).setParameter("p", hashed).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    // ========== EMAIL LOOKUP ==========

    public Customer getCustomerByEmail(String email) {
        List<Customer> list = em.createQuery(
                "SELECT c FROM Customer c WHERE c.email = :e", Customer.class)
                .setParameter("e", email).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public Technician getTechnicianByEmail(String email) {
        List<Technician> list = em.createQuery(
                "SELECT t FROM Technician t WHERE t.email = :e", Technician.class)
                .setParameter("e", email).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public Manager getManagerByEmail(String email) {
        List<Manager> list = em.createQuery(
                "SELECT m FROM Manager m WHERE m.email = :e", Manager.class)
                .setParameter("e", email).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public CounterStaff getCounterStaffByEmail(String email) {
        List<CounterStaff> list = em.createQuery(
                "SELECT cs FROM CounterStaff cs WHERE cs.email = :e", CounterStaff.class)
                .setParameter("e", email).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}
