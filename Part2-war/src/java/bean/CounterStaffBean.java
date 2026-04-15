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
import util.ValidationUtil;

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
        if (customers == null || services == null || technicians == null ||
            appointments == null || payments == null) {
            loadDashboardData();
        }
        if (profileData == null) {
            loadProfile();
        }
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
            if (profileData == null) {
                addError("Profile not found.");
                return;
            }

            CounterStaff existing = userFacade.getCounterStaffByID(profileData.getId());
            if (existing == null) {
                profileData = null;
                addError("Profile no longer exists.");
                return;
            }

            if (!validateUserFields(
                    profileData.getName(),
                    profileData.getEmail(),
                    null,
                    profileData.getGender(),
                    profileData.getPhone(),
                    profileData.getIc(),
                    profileData.getAddress(),
                    existing.getEmail(),
                    existing.getIc(),
                    false)) {
                return;
            }

            profileData.setName(sanitizeText(profileData.getName()));
            profileData.setEmail(ValidationUtil.normalizeEmail(profileData.getEmail()));
            profileData.setGender(sanitizeText(profileData.getGender()));
            profileData.setPhone(ValidationUtil.normalizePhone(profileData.getPhone()));
            profileData.setIc(sanitizeText(profileData.getIc()));
            profileData.setAddress(sanitizeText(profileData.getAddress()));

            userFacade.updateCounterStaff(profileData);
            loadProfile();
            addInfo("Profile updated successfully.");
        } catch (Exception e) {
            addError("Error updating profile: " + e.getMessage());
        }
    }

    // ========== CUSTOMER REGISTRATION ==========

    public void registerCustomer() {
        try {
            if (!validateUserFields(
                    custName,
                    custEmail,
                    custPassword,
                    custGender,
                    custPhone,
                    custIc,
                    custAddress,
                    null,
                    null,
                    true)) {
                return;
            }

            Customer c = new Customer();
            c.setName(sanitizeText(custName));
            c.setEmail(ValidationUtil.normalizeEmail(custEmail));
            c.setPassword(custPassword);
            c.setGender(sanitizeText(custGender));
            c.setPhone(ValidationUtil.normalizePhone(custPhone));
            c.setIc(sanitizeText(custIc));
            c.setAddress(sanitizeText(custAddress));
            boolean success = userFacade.createCustomer(c);
            if (success) {
                addInfo("Customer registered successfully.");
                custName = custEmail = custPassword = custGender = custPhone = custIc = custAddress = null;
                loadDashboardData();
            } else {
                addError("Failed to register customer. A conflicting email or IC may already exist.");
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
            String keyword = searchKeyword != null ? searchKeyword.trim() : null;
            if (keyword != null && !keyword.isBlank()) {
                customers = userFacade.searchCustomers(keyword);
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
            if (editingCustomer == null) {
                addError("Customer not found.");
                return;
            }

            Customer existing = userFacade.getCustomerByID(editingCustomer.getId());
            if (existing == null) {
                editingCustomer = null;
                loadDashboardData();
                addError("Customer no longer exists.");
                return;
            }

            if (!validateUserFields(
                    editingCustomer.getName(),
                    editingCustomer.getEmail(),
                    null,
                    editingCustomer.getGender(),
                    editingCustomer.getPhone(),
                    editingCustomer.getIc(),
                    editingCustomer.getAddress(),
                    existing.getEmail(),
                    existing.getIc(),
                    false)) {
                return;
            }

            editingCustomer.setName(sanitizeText(editingCustomer.getName()));
            editingCustomer.setEmail(ValidationUtil.normalizeEmail(editingCustomer.getEmail()));
            editingCustomer.setGender(sanitizeText(editingCustomer.getGender()));
            editingCustomer.setPhone(ValidationUtil.normalizePhone(editingCustomer.getPhone()));
            editingCustomer.setIc(sanitizeText(editingCustomer.getIc()));
            editingCustomer.setAddress(sanitizeText(editingCustomer.getAddress()));

            userFacade.updateCustomer(editingCustomer);
            editingCustomer = null;
            loadDashboardData();
            addInfo("Customer updated successfully.");
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
            if (!technician.isAvailable()) {
                addError("Selected technician is no longer available.");
                return;
            }
            if (!isTechnicianAvailableForSlot(technician, appointmentDate, service)) {
                addError("Selected technician is not available for the chosen appointment slot.");
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
            if (!Appointment.STATUS_COMPLETED.equalsIgnoreCase(appointment.getStatus())) {
                addError("Payment can only be collected for completed appointments.");
                return;
            }
            if (!paymentFacade.getPaymentsByAppointment(paymentAppointmentId).isEmpty()) {
                addError("Payment has already been collected for this appointment.");
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

    private boolean validateUserFields(String name, String email, String password, String gender,
                                       String phone, String ic, String address,
                                       String existingEmail, String existingIc,
                                       boolean requirePassword) {
        boolean valid = true;

        if (!ValidationUtil.isValidName(name)) {
            addError(ValidationUtil.getErrorMessage("name"));
            valid = false;
        }

        String normalizedEmail = ValidationUtil.normalizeEmail(email);
        if (!ValidationUtil.isValidEmail(email)) {
            addError(ValidationUtil.getErrorMessage("email"));
            valid = false;
        } else if (emailChanged(normalizedEmail, existingEmail) && userFacade.isDuplicateEmail(normalizedEmail)) {
            addError("Email is already registered.");
            valid = false;
        }

        if (requirePassword && !ValidationUtil.isValidPassword(password)) {
            addError(ValidationUtil.getErrorMessage("password"));
            valid = false;
        }

        if (!ValidationUtil.isValidGender(gender)) {
            addError(ValidationUtil.getErrorMessage("gender"));
            valid = false;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            addError(ValidationUtil.getErrorMessage("phone"));
            valid = false;
        }

        String normalizedIc = ValidationUtil.normalizeIC(ic);
        if (!ValidationUtil.isValidIC(ic)) {
            addError(ValidationUtil.getErrorMessage("ic"));
            valid = false;
        } else if (icChanged(normalizedIc, existingIc) && userFacade.isDuplicateIC(ic)) {
            addError("IC number is already registered.");
            valid = false;
        }

        if (!ValidationUtil.isValidAddress(address)) {
            addError(ValidationUtil.getErrorMessage("address"));
            valid = false;
        }

        return valid;
    }

    private boolean emailChanged(String normalizedEmail, String existingEmail) {
        String normalizedExistingEmail = ValidationUtil.normalizeEmail(existingEmail);
        if (normalizedExistingEmail == null) {
            return normalizedEmail != null;
        }
        return normalizedEmail != null && !normalizedExistingEmail.equals(normalizedEmail);
    }

    private boolean icChanged(String normalizedIc, String existingIc) {
        String normalizedExistingIc = ValidationUtil.normalizeIC(existingIc);
        if (normalizedExistingIc == null) {
            return normalizedIc != null;
        }
        return normalizedIc != null && !normalizedExistingIc.equals(normalizedIc);
    }

    private String sanitizeText(String value) {
        return ValidationUtil.trimToNull(value);
    }

    public void onAppointmentDetailsChange() {
        if (selectedTechnicianId == null || selectedTechnicianId.isBlank()) {
            return;
        }

        Technician technician = findTechnicianById(selectedTechnicianId);
        if (!isTechnicianAvailableForSlot(technician, parseAppointmentDate(), findSelectedService())) {
            selectedTechnicianId = null;
        }
    }

    public boolean isBookingSlotReady() {
        return findSelectedService() != null && parseAppointmentDate() != null;
    }

    public String formatCustomerDisplay(Customer customer, String customerId) {
        String resolvedCustomerId = customerId;
        String customerName = null;

        if (customer != null) {
            resolvedCustomerId = customer.getId() != null ? customer.getId() : resolvedCustomerId;
            customerName = ValidationUtil.trimToNull(customer.getName());
        }

        if (customerName != null && resolvedCustomerId != null) {
            return customerName + " (" + resolvedCustomerId + ")";
        }
        if (customerName != null) {
            return customerName;
        }
        return resolvedCustomerId != null ? resolvedCustomerId : "-";
    }

    private boolean hasPaymentForAppointment(String appointmentId) {
        if (payments == null) {
            return false;
        }
        for (Payment payment : payments) {
            if (appointmentId.equals(payment.getAppointmentId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get only completed appointments (for payment collection).
     */
    public List<Appointment> getCompletedAppointments() {
        List<Appointment> list = new ArrayList<>();
        if (appointments != null) {
            for (Appointment a : appointments) {
                if (Appointment.STATUS_COMPLETED.equalsIgnoreCase(a.getStatus()) &&
                    !hasPaymentForAppointment(a.getId())) {
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
    public List<Technician> getAvailableTechnicians() {
        if (technicians == null) {
            loadDashboardData();
        }

        Service selectedService = findSelectedService();
        LocalDateTime appointmentDate = parseAppointmentDate();
        if (selectedService == null || appointmentDate == null) {
            return new ArrayList<>();
        }

        List<Technician> availableTechnicians = new ArrayList<>();
        if (technicians != null) {
            for (Technician technician : technicians) {
                if (isTechnicianAvailableForSlot(technician, appointmentDate, selectedService)) {
                    availableTechnicians.add(technician);
                }
            }
        }
        return availableTechnicians;
    }
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

    private Service findSelectedService() {
        if (selectedServiceId == null || selectedServiceId.isBlank()) {
            return null;
        }

        if (services != null) {
            for (Service service : services) {
                if (service != null && selectedServiceId.equals(service.getId())) {
                    return service;
                }
            }
        }

        return serviceFacade.getServiceByID(selectedServiceId);
    }

    private Technician findTechnicianById(String technicianId) {
        if (technicianId == null || technicianId.isBlank()) {
            return null;
        }

        if (technicians != null) {
            for (Technician technician : technicians) {
                if (technician != null && technicianId.equals(technician.getId())) {
                    return technician;
                }
            }
        }

        return userFacade.getTechnicianByID(technicianId);
    }

    private LocalDateTime parseAppointmentDate() {
        if (appointmentDateStr == null || appointmentDateStr.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(appointmentDateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTechnicianAvailableForSlot(Technician technician,
                                                 LocalDateTime appointmentDate,
                                                 Service service) {
        if (technician == null || !technician.isAvailable() ||
            appointmentDate == null || service == null) {
            return false;
        }

        LocalDateTime requestedEnd = appointmentDate.plusMinutes(getDurationMinutes(service));
        if (appointments == null) {
            loadDashboardData();
        }

        if (appointments != null) {
            for (Appointment appointment : appointments) {
                if (appointment == null || !technician.getId().equals(appointment.getTechnicianId())) {
                    continue;
                }
                if (!Appointment.STATUS_PENDING.equalsIgnoreCase(appointment.getStatus()) &&
                    !Appointment.STATUS_IN_PROGRESS.equalsIgnoreCase(appointment.getStatus())) {
                    continue;
                }

                LocalDateTime existingStart = appointment.getAppointmentDateTime();
                if (existingStart == null) {
                    continue;
                }

                LocalDateTime existingEnd = existingStart.plusMinutes(
                        getDurationMinutes(resolveService(appointment)));
                if (existingStart.isBefore(requestedEnd) && existingEnd.isAfter(appointmentDate)) {
                    return false;
                }
            }
        }

        return true;
    }

    private Service resolveService(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        if (appointment.getService() != null) {
            return appointment.getService();
        }
        if (appointment.getServiceId() == null || appointment.getServiceId().isBlank()) {
            return null;
        }

        if (services != null) {
            for (Service service : services) {
                if (service != null && appointment.getServiceId().equals(service.getId())) {
                    return service;
                }
            }
        }

        return serviceFacade.getServiceByID(appointment.getServiceId());
    }

    private int getDurationMinutes(Service service) {
        return service != null && service.getDurationMinutes() > 0
                ? service.getDurationMinutes()
                : Service.DURATION_NORMAL;
    }
}
