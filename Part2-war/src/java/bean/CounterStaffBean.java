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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Counter Staff Dashboard Bean.
 * Requirements: Edit profile, register/delete/search/update customers,
 * book appointments (next 5 days), assign to technicians,
 * generate receipt and collect payment.
 */
@Named("counterStaffBean")
@ViewScoped
public class CounterStaffBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private UserFacade userFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private PaymentFacade paymentFacade;
    @Inject
    private LoginBean loginBean;

    // Sidebar navigation
    private String currentSection = "overview";

    // Dashboard statistics
    private int totalCustomers, totalServices, todayAppointments, pendingBookings;

    // Data lists
    private List<Customer> customers;
    private List<Service> services;
    private List<Technician> technicians;
    private List<Appointment> appointments;
    private List<Payment> payments;

    // Customer registration form
    private String custName, custEmail, custPassword, custGender, custPhone, custIc, custAddress;

    // Appointment booking form
    private String selectedCustomerId, selectedServiceId, selectedTechnicianId;
    private String appointmentDateStr;

    // Payment form
    private String paymentAppointmentId, paymentMethod;

    // Profile editing
    private CounterStaff profileData;

    // Editing selected customer
    private Customer editingCustomer;

    // Search
    private String searchKeyword;

    public void init() {
        loadDashboardData();
        loadProfile();
    }

    public void loadDashboardData() {
        try {
            customers = userFacade.getAllCustomers();
            totalCustomers = customers != null ? customers.size() : 0;

            services = serviceFacade.getAllServices();
            totalServices = services != null ? services.size() : 0;

            technicians = userFacade.getAllTechnicians();

            appointments = appointmentFacade.getAllAppointments();
            todayAppointments = 0;
            pendingBookings = 0;
            if (appointments != null) {
                LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
                LocalDateTime endOfToday = startOfToday.plusDays(1);
                for (Appointment a : appointments) {
                    if (a.getAppointmentDateTime() != null &&
                        a.getAppointmentDateTime().isAfter(startOfToday) &&
                        a.getAppointmentDateTime().isBefore(endOfToday)) {
                        todayAppointments++;
                    }
                    if ("Pending".equalsIgnoreCase(a.getStatus())) pendingBookings++;
                }
            }

            payments = paymentFacade.getAllPayments();
        } catch (Exception e) {
            System.err.println("Error loading counter staff dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadProfile() {
        try {
            if (loginBean != null && loginBean.getUserId() != null) {
                profileData = userFacade.getCounterStaffByID(loginBean.getUserId());
            }
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
        }
    }

    // ========== PROFILE EDITING ==========

    public void updateProfile() {
        try {
            if (profileData != null) {
                userFacade.updateCounterStaff(profileData);
                addInfo("Profile updated successfully.");
            }
        } catch (Exception e) {
            addError("Error updating profile: " + e.getMessage());
        }
    }

    // ========== CUSTOMER REGISTRATION ==========

    public void registerCustomer() {
        try {
            if (custName == null || custEmail == null || custPassword == null ||
                custName.isBlank() || custEmail.isBlank() || custPassword.isBlank()) {
                addError("Name, email, and password are required.");
                return;
            }
            Customer c = new Customer();
            c.setName(custName); c.setEmail(custEmail); c.setPassword(custPassword);
            c.setGender(custGender); c.setPhone(custPhone); c.setIc(custIc); c.setAddress(custAddress);
            boolean success = userFacade.createCustomer(c);
            if (success) {
                addInfo("Customer registered successfully.");
                custName = custEmail = custPassword = custGender = custPhone = custIc = custAddress = null;
                loadDashboardData();
            } else {
                addError("Failed to register customer. Email may already exist.");
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    public void deleteCustomer(String id) {
        try { userFacade.deleteCustomer(id); loadDashboardData(); addInfo("Customer deleted."); }
        catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void searchCustomers() {
        try {
            if (searchKeyword != null && !searchKeyword.isBlank()) {
                customers = userFacade.searchCustomers(searchKeyword);
            } else {
                customers = userFacade.getAllCustomers();
            }
            totalCustomers = customers != null ? customers.size() : 0;
        } catch (Exception e) {
            addError("Search error: " + e.getMessage());
        }
    }

    public void clearSearch() {
        searchKeyword = null;
        loadDashboardData();
    }

    // ========== EDIT / UPDATE CUSTOMER ==========

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

    // ========== APPOINTMENT BOOKING (next 5 days) ==========

    public void bookAppointment() {
        try {
            if (selectedCustomerId == null || selectedServiceId == null ||
                selectedTechnicianId == null || appointmentDateStr == null ||
                appointmentDateStr.isBlank()) {
                addError("All booking fields are required.");
                return;
            }

            LocalDateTime appointmentDate = LocalDateTime.parse(appointmentDateStr);

            // Validate: appointment must be within the next 5 days
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime maxDate = now.plusDays(5);
            if (appointmentDate.isBefore(now) || appointmentDate.isAfter(maxDate)) {
                addError("Appointment must be within the next 5 days.");
                return;
            }

            Customer customer = userFacade.getCustomerByID(selectedCustomerId);
            Service service = serviceFacade.getServiceByID(selectedServiceId);
            Technician technician = userFacade.getTechnicianByID(selectedTechnicianId);

            if (customer == null || service == null || technician == null) {
                addError("Invalid selection.");
                return;
            }

            Appointment appointment = new Appointment();
            appointment.setCustomer(customer);
            appointment.setService(service);
            appointment.setTechnician(technician);
            appointment.setAppointmentDateTime(appointmentDate);
            appointment.setStatus(Appointment.STATUS_PENDING);
            appointment.setPaymentAmount(service.getBasePrice());

            appointmentFacade.createAppointment(appointment);
            addInfo("Appointment booked successfully.");

            selectedCustomerId = selectedServiceId = selectedTechnicianId = null;
            appointmentDateStr = null;
            loadDashboardData();
        } catch (Exception e) {
            addError("Booking error: " + e.getMessage());
        }
    }

    // ========== PAYMENT & RECEIPT ==========

    public void collectPayment() {
        try {
            if (paymentAppointmentId == null || paymentMethod == null ||
                paymentAppointmentId.isBlank() || paymentMethod.isBlank()) {
                addError("Appointment and payment method are required.");
                return;
            }

            Appointment appointment = appointmentFacade.getAppointmentByID(paymentAppointmentId);
            if (appointment == null) {
                addError("Appointment not found.");
                return;
            }

            Payment payment = new Payment();
            payment.setAppointmentId(paymentAppointmentId);
            payment.setCustomerId(appointment.getCustomerId());
            payment.setAmount(appointment.getPaymentAmount());
            payment.setPaymentMethod(paymentMethod);
            payment.setStatus("Completed");

            boolean success = paymentFacade.createPayment(payment);
            if (success) {
                // Update appointment status
                appointment.setStatus(Appointment.STATUS_COMPLETED);
                appointmentFacade.updateAppointment(appointment);
                addInfo("Payment collected. Receipt: " + payment.getReceiptNumber());
                paymentAppointmentId = null;
                paymentMethod = null;
                loadDashboardData();
            } else {
                addError("Failed to process payment.");
            }
        } catch (Exception e) {
            addError("Payment error: " + e.getMessage());
        }
    }

    // ========== SIDEBAR NAVIGATION ==========

    public void navigateTo(String section) {
        this.currentSection = section;
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

    /**
     * Get only completed appointments (for payment collection).
     */
    public List<Appointment> getCompletedAppointments() {
        List<Appointment> list = new ArrayList<>();
        if (appointments != null) {
            for (Appointment a : appointments) {
                if ("Completed".equalsIgnoreCase(a.getStatus()) || "InProgress".equalsIgnoreCase(a.getStatus())) {
                    list.add(a);
                }
            }
        }
        return list;
    }

    // ========== GETTERS & SETTERS ==========

    public int getTotalCustomers() { if (customers == null) loadDashboardData(); return totalCustomers; }
    public int getTotalServices() { if (services == null) loadDashboardData(); return totalServices; }
    public int getTodayAppointments() { return todayAppointments; }
    public int getPendingBookings() { return pendingBookings; }

    public List<Customer> getCustomers() { if (customers == null) loadDashboardData(); return customers; }
    public List<Service> getServices() { if (services == null) loadDashboardData(); return services; }
    public List<Technician> getTechnicians() { if (technicians == null) loadDashboardData(); return technicians; }
    public List<Appointment> getAppointments() { if (appointments == null) loadDashboardData(); return appointments; }
    public List<Payment> getPayments() { if (payments == null) loadDashboardData(); return payments; }

    public CounterStaff getProfileData() { return profileData; }
    public void setProfileData(CounterStaff profileData) { this.profileData = profileData; }

    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; }
    public String getCustEmail() { return custEmail; }
    public void setCustEmail(String custEmail) { this.custEmail = custEmail; }
    public String getCustPassword() { return custPassword; }
    public void setCustPassword(String custPassword) { this.custPassword = custPassword; }
    public String getCustGender() { return custGender; }
    public void setCustGender(String custGender) { this.custGender = custGender; }
    public String getCustPhone() { return custPhone; }
    public void setCustPhone(String custPhone) { this.custPhone = custPhone; }
    public String getCustIc() { return custIc; }
    public void setCustIc(String custIc) { this.custIc = custIc; }
    public String getCustAddress() { return custAddress; }
    public void setCustAddress(String custAddress) { this.custAddress = custAddress; }

    public String getSelectedCustomerId() { return selectedCustomerId; }
    public void setSelectedCustomerId(String selectedCustomerId) { this.selectedCustomerId = selectedCustomerId; }
    public String getSelectedServiceId() { return selectedServiceId; }
    public void setSelectedServiceId(String selectedServiceId) { this.selectedServiceId = selectedServiceId; }
    public String getSelectedTechnicianId() { return selectedTechnicianId; }
    public void setSelectedTechnicianId(String selectedTechnicianId) { this.selectedTechnicianId = selectedTechnicianId; }
    public String getAppointmentDateStr() { return appointmentDateStr; }
    public void setAppointmentDateStr(String appointmentDateStr) { this.appointmentDateStr = appointmentDateStr; }

    public String getPaymentAppointmentId() { return paymentAppointmentId; }
    public void setPaymentAppointmentId(String paymentAppointmentId) { this.paymentAppointmentId = paymentAppointmentId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }

    public Customer getEditingCustomer() { return editingCustomer; }
    public void setEditingCustomer(Customer editingCustomer) { this.editingCustomer = editingCustomer; }

    public String getCurrentSection() { return currentSection; }
    public void setCurrentSection(String currentSection) { this.currentSection = currentSection; }
}
