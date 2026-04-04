package bean;

import entity.*;
import facade.*;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manager Dashboard Bean.
 * Requirements: Register/delete/search/update all staff (3 types + managers),
 * set prices, view all feedback and comments, 5 reports.
 */
@Named("managerBean")
@ViewScoped
public class ManagerBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private UserFacade userFacade;
    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private FeedbackFacade feedbackFacade;
    @EJB
    private AppointmentCommentFacade commentFacade;
    @Inject
    private LoginBean loginBean;

    // Dashboard statistics
    private int totalServices;
    private int totalTechnicians;
    private int totalCustomers;
    private int totalCounterStaff;
    private int pendingAppointments;

    // Data lists
    private List<Service> services;
    private List<Technician> technicians;
    private List<CounterStaff> counterStaffList;
    private List<Customer> customers;
    private List<Manager> managers;
    private List<Appointment> appointments;
    private List<Feedback> allFeedback;
    private List<AppointmentComment> allComments;

    // Staff registration form
    private String regName, regEmail, regPassword, regGender, regPhone, regIc, regAddress;
    private String regSpecialty;
    private String regUserType = "Technician";

    // Service form
    private Service editingService;
    private String newServiceName, newServiceType;
    private double newServicePrice;

    // Search
    private String searchKeyword;

    // Editing selected staff
    private Technician editingTechnician;
    private CounterStaff editingCounterStaff;
    private Manager editingManager;
    private Customer editingCustomer;

    // Reports
    private double dailyRevenue;
    private Map<String, Integer> technicianWorkload;
    private Map<String, Integer> servicePopularity;
    private List<Map<String, Object>> customerFeedback;
    private Map<String, Object> statusAnalytics;

    public void init() {
        loadDashboardData();
    }

    public void loadDashboardData() {
        try {
            services = serviceFacade.getAllServices();
            totalServices = services != null ? services.size() : 0;

            technicians = userFacade.getAllTechnicians();
            totalTechnicians = technicians != null ? technicians.size() : 0;

            customers = userFacade.getAllCustomers();
            totalCustomers = customers != null ? customers.size() : 0;

            counterStaffList = userFacade.getAllCounterStaff();
            totalCounterStaff = counterStaffList != null ? counterStaffList.size() : 0;

            managers = userFacade.getAllManagers();

            appointments = appointmentFacade.getAllAppointments();
            pendingAppointments = 0;
            if (appointments != null) {
                for (Appointment a : appointments) {
                    if ("Pending".equalsIgnoreCase(a.getStatus())) pendingAppointments++;
                }
            }

            allFeedback = feedbackFacade.getAllFeedback();
            allComments = commentFacade.getAllComments();
        } catch (Exception e) {
            System.err.println("Error loading manager dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== STAFF REGISTRATION ==========

    public void registerStaff() {
        try {
            if (regName == null || regEmail == null || regPassword == null ||
                regName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                addError("Name, email, and password are required.");
                return;
            }

            boolean success = false;
            switch (regUserType) {
                case "Technician":
                    Technician t = new Technician();
                    t.setName(regName); t.setEmail(regEmail); t.setPassword(regPassword);
                    t.setGender(regGender); t.setPhone(regPhone); t.setIc(regIc); t.setAddress(regAddress);
                    t.setSpecialty(regSpecialty != null ? regSpecialty : "General");
                    t.setAvailable(true);
                    success = userFacade.createTechnician(t);
                    break;
                case "CounterStaff":
                    CounterStaff cs = new CounterStaff();
                    cs.setName(regName); cs.setEmail(regEmail); cs.setPassword(regPassword);
                    cs.setGender(regGender); cs.setPhone(regPhone); cs.setIc(regIc); cs.setAddress(regAddress);
                    success = userFacade.createCounterStaff(cs);
                    break;
                case "Manager":
                    Manager m = new Manager();
                    m.setName(regName); m.setEmail(regEmail); m.setPassword(regPassword);
                    m.setGender(regGender); m.setPhone(regPhone); m.setIc(regIc); m.setAddress(regAddress);
                    success = userFacade.createManager(m);
                    break;
            }

            if (success) {
                addInfo(regUserType + " registered successfully.");
                clearRegistrationForm();
                loadDashboardData();
            } else {
                addError("Failed to register " + regUserType + ". Email may already exist.");
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    private void clearRegistrationForm() {
        regName = regEmail = regPassword = regGender = regPhone = regIc = regAddress = regSpecialty = null;
    }

    // ========== STAFF DELETE ==========

    public void deleteTechnician(String id) {
        try { userFacade.deleteTechnician(id); loadDashboardData(); addInfo("Technician deleted."); }
        catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void deleteCounterStaff(String id) {
        try { userFacade.deleteCounterStaff(id); loadDashboardData(); addInfo("Counter staff deleted."); }
        catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void deleteManager(String id) {
        try { userFacade.deleteManager(id); loadDashboardData(); addInfo("Manager deleted."); }
        catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void deleteCustomer(String id) {
        try { userFacade.deleteCustomer(id); loadDashboardData(); addInfo("Customer deleted."); }
        catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    // ========== SEARCH ==========

    public void searchStaff() {
        try {
            if (searchKeyword != null && !searchKeyword.isBlank()) {
                technicians = userFacade.searchTechnicians(searchKeyword);
                counterStaffList = userFacade.searchCounterStaff(searchKeyword);
                managers = userFacade.searchManagers(searchKeyword);
                customers = userFacade.searchCustomers(searchKeyword);
            } else {
                loadDashboardData();
            }
        } catch (Exception e) {
            addError("Search error: " + e.getMessage());
        }
    }

    public void clearSearch() {
        searchKeyword = null;
        loadDashboardData();
    }

    // ========== EDIT / UPDATE STAFF ==========

    public void loadEditTechnician(String id) {
        try { editingTechnician = userFacade.getTechnicianByID(id); }
        catch (Exception e) { addError("Error loading technician: " + e.getMessage()); }
    }

    public void saveEditTechnician() {
        try {
            if (editingTechnician != null) {
                userFacade.updateTechnician(editingTechnician);
                editingTechnician = null;
                loadDashboardData();
                addInfo("Technician updated successfully.");
            }
        } catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void loadEditCounterStaff(String id) {
        try { editingCounterStaff = userFacade.getCounterStaffByID(id); }
        catch (Exception e) { addError("Error loading counter staff: " + e.getMessage()); }
    }

    public void saveEditCounterStaff() {
        try {
            if (editingCounterStaff != null) {
                userFacade.updateCounterStaff(editingCounterStaff);
                editingCounterStaff = null;
                loadDashboardData();
                addInfo("Counter staff updated successfully.");
            }
        } catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void loadEditManager(String id) {
        try { editingManager = userFacade.getManagerByID(id); }
        catch (Exception e) { addError("Error loading manager: " + e.getMessage()); }
    }

    public void saveEditManager() {
        try {
            if (editingManager != null) {
                userFacade.updateManager(editingManager);
                editingManager = null;
                loadDashboardData();
                addInfo("Manager updated successfully.");
            }
        } catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void loadEditCustomer(String id) {
        try { editingCustomer = userFacade.getCustomerByID(id); }
        catch (Exception e) { addError("Error loading customer: " + e.getMessage()); }
    }

    public void saveEditCustomer() {
        try {
            if (editingCustomer != null) {
                userFacade.updateCustomer(editingCustomer);
                editingCustomer = null;
                loadDashboardData();
                addInfo("Customer updated successfully.");
            }
        } catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    // ========== SERVICE MANAGEMENT (SET PRICES) ==========

    public void createService() {
        try {
            if (newServiceName == null || newServiceType == null || newServicePrice <= 0) {
                addError("Service name, type, and price are required.");
                return;
            }
            Service s = new Service(null, newServiceName, newServiceType, newServicePrice);
            serviceFacade.createService(s);
            newServiceName = null; newServiceType = null; newServicePrice = 0;
            loadDashboardData();
            addInfo("Service created.");
        } catch (Exception e) {
            addError("Error creating service: " + e.getMessage());
        }
    }

    public void updateService(Service service) {
        try {
            serviceFacade.updateService(service);
            loadDashboardData();
            addInfo("Service updated.");
        } catch (Exception e) {
            addError("Error updating service: " + e.getMessage());
        }
    }

    public void deleteService(String serviceId) {
        try {
            serviceFacade.deleteService(serviceId);
            loadDashboardData();
            addInfo("Service deleted.");
        } catch (Exception e) {
            addError("Error deleting service: " + e.getMessage());
        }
    }

    // ========== REPORTS ==========

    public void loadReports() {
        try {
            dailyRevenue = appointmentFacade.getDailyRevenue();
            technicianWorkload = appointmentFacade.getTechnicianWorkload();
            servicePopularity = appointmentFacade.getServicePopularity();
            customerFeedback = appointmentFacade.getCustomerFeedback();
            statusAnalytics = appointmentFacade.getStatusAnalytics();
        } catch (Exception e) {
            addError("Error loading reports: " + e.getMessage());
        }
    }

    // ========== HELPERS ==========

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    // ========== GETTERS & SETTERS ==========

    public int getTotalServices() { if (services == null) loadDashboardData(); return totalServices; }
    public int getTotalTechnicians() { if (technicians == null) loadDashboardData(); return totalTechnicians; }
    public int getTotalCustomers() { if (customers == null) loadDashboardData(); return totalCustomers; }
    public int getTotalCounterStaff() { if (counterStaffList == null) loadDashboardData(); return totalCounterStaff; }
    public int getPendingAppointments() { if (appointments == null) loadDashboardData(); return pendingAppointments; }

    public List<Service> getServices() { if (services == null) loadDashboardData(); return services; }
    public List<Technician> getTechnicians() { if (technicians == null) loadDashboardData(); return technicians; }
    public List<CounterStaff> getCounterStaffList() { if (counterStaffList == null) loadDashboardData(); return counterStaffList; }
    public List<Customer> getCustomers() { if (customers == null) loadDashboardData(); return customers; }
    public List<Manager> getManagers() { if (managers == null) loadDashboardData(); return managers; }
    public List<Appointment> getAppointments() { if (appointments == null) loadDashboardData(); return appointments; }
    public List<Feedback> getAllFeedback() { if (allFeedback == null) loadDashboardData(); return allFeedback; }
    public List<AppointmentComment> getAllComments() { if (allComments == null) loadDashboardData(); return allComments; }

    public String getRegName() { return regName; }
    public void setRegName(String regName) { this.regName = regName; }
    public String getRegEmail() { return regEmail; }
    public void setRegEmail(String regEmail) { this.regEmail = regEmail; }
    public String getRegPassword() { return regPassword; }
    public void setRegPassword(String regPassword) { this.regPassword = regPassword; }
    public String getRegGender() { return regGender; }
    public void setRegGender(String regGender) { this.regGender = regGender; }
    public String getRegPhone() { return regPhone; }
    public void setRegPhone(String regPhone) { this.regPhone = regPhone; }
    public String getRegIc() { return regIc; }
    public void setRegIc(String regIc) { this.regIc = regIc; }
    public String getRegAddress() { return regAddress; }
    public void setRegAddress(String regAddress) { this.regAddress = regAddress; }
    public String getRegSpecialty() { return regSpecialty; }
    public void setRegSpecialty(String regSpecialty) { this.regSpecialty = regSpecialty; }
    public String getRegUserType() { return regUserType; }
    public void setRegUserType(String regUserType) { this.regUserType = regUserType; }

    public String getNewServiceName() { return newServiceName; }
    public void setNewServiceName(String newServiceName) { this.newServiceName = newServiceName; }
    public String getNewServiceType() { return newServiceType; }
    public void setNewServiceType(String newServiceType) { this.newServiceType = newServiceType; }
    public double getNewServicePrice() { return newServicePrice; }
    public void setNewServicePrice(double newServicePrice) { this.newServicePrice = newServicePrice; }

    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }

    public double getDailyRevenue() { return dailyRevenue; }
    public Map<String, Integer> getTechnicianWorkload() { return technicianWorkload; }
    public Map<String, Integer> getServicePopularity() { return servicePopularity; }
    public List<Map<String, Object>> getCustomerFeedback() { return customerFeedback; }
    public Map<String, Object> getStatusAnalytics() { return statusAnalytics; }

    public Technician getEditingTechnician() { return editingTechnician; }
    public void setEditingTechnician(Technician editingTechnician) { this.editingTechnician = editingTechnician; }

    public CounterStaff getEditingCounterStaff() { return editingCounterStaff; }
    public void setEditingCounterStaff(CounterStaff editingCounterStaff) { this.editingCounterStaff = editingCounterStaff; }

    public Manager getEditingManager() { return editingManager; }
    public void setEditingManager(Manager editingManager) { this.editingManager = editingManager; }

    public Customer getEditingCustomer() { return editingCustomer; }
    public void setEditingCustomer(Customer editingCustomer) { this.editingCustomer = editingCustomer; }
}
