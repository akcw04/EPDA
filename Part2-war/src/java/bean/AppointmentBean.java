package bean;

import entity.*;
import facade.*;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AppointmentBean - JSF Managed Bean for appointment operations.
 * SessionScoped: maintains appointment data across multiple pages.
 * Calls AppointmentFacade (EJB) for business logic.
 */
@Named("appointmentBean")
@SessionScoped
public class AppointmentBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private AppointmentFacade appointmentFacade;

    @EJB
    private UserFacade userFacade;

    @EJB
    private ServiceFacade serviceFacade;

    // Form fields
    private Appointment currentAppointment;
    private String selectedCustomerID;
    private String selectedTechnicianID;
    private String selectedServiceID;
    private LocalDateTime appointmentDateTime;
    private List<Appointment> appointments;
    private List<Customer> customers;
    private List<Technician> technicians;
    private List<Service> services;
    private String message;
    private String errorMessage;

    /**
     * Initialize bean
     */
    public void init() {
        currentAppointment = null;
        loadCustomers();
        loadTechnicians();
        loadServices();
    }

    /**
     * Load all customers
     */
    private void loadCustomers() {
        try {
            customers = userFacade.getAllCustomers();
        } catch (Exception e) {
            errorMessage = "Failed to load customers: " + e.getMessage();
        }
    }

    /**
     * Load all technicians
     */
    private void loadTechnicians() {
        try {
            technicians = userFacade.getAllTechnicians();
        } catch (Exception e) {
            errorMessage = "Failed to load technicians: " + e.getMessage();
        }
    }

    /**
     * Load all services
     */
    private void loadServices() {
        try {
            services = serviceFacade.getAllServices();
        } catch (Exception e) {
            errorMessage = "Failed to load services: " + e.getMessage();
        }
    }

    /**
     * Create a new appointment
     */
    public void createAppointment() {
        try {
            if (selectedCustomerID == null || selectedTechnicianID == null || 
                selectedServiceID == null || appointmentDateTime == null) {
                errorMessage = "Please fill all required fields";
                return;
            }

            // Fetch related entities
            Customer customer = userFacade.getCustomerByID(selectedCustomerID);
            Technician technician = userFacade.getTechnicianByID(selectedTechnicianID);
            Service service = serviceFacade.getServiceByID(selectedServiceID);

            // Create appointment
            Appointment appointment = new Appointment(
                    null,  // ID will be generated
                    customer,
                    technician,
                    service,
                    appointmentDateTime
            );

            appointmentFacade.createAppointment(appointment);
            message = "Appointment created successfully: " + appointment.getId();
            errorMessage = null;
            resetForm();
        } catch (Exception e) {
            errorMessage = "Failed to create appointment: " + e.getMessage();
            System.err.println("Error: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Update appointment status and feedback
     */
    public void updateAppointment() {
        try {
            if (currentAppointment == null) {
                errorMessage = "No appointment selected";
                return;
            }
            appointmentFacade.updateAppointment(currentAppointment);
            message = "Appointment updated successfully";
            errorMessage = null;
        } catch (Exception e) {
            errorMessage = "Failed to update appointment: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Delete an appointment
     */
    public void deleteAppointment(String appointmentID) {
        try {
            appointmentFacade.deleteAppointment(appointmentID);
            message = "Appointment deleted successfully";
            errorMessage = null;
            appointments.removeIf(a -> a.getId().equals(appointmentID));
        } catch (Exception e) {
            errorMessage = "Failed to delete appointment: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Load appointments for current customer (if logged in)
     */
    public void loadMyAppointments(String customerID) {
        try {
            appointments = appointmentFacade.getAppointmentsByCustomer(customerID);
        } catch (Exception e) {
            errorMessage = "Failed to load appointments: " + e.getMessage();
        }
    }

    /**
     * View appointment details
     */
    public void viewAppointment(String appointmentID) {
        try {
            currentAppointment = appointmentFacade.getAppointmentByID(appointmentID);
            if (currentAppointment == null) {
                errorMessage = "Appointment not found";
            }
        } catch (Exception e) {
            errorMessage = "Failed to load appointment: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Reset form fields
     */
    private void resetForm() {
        selectedCustomerID = null;
        selectedTechnicianID = null;
        selectedServiceID = null;
        appointmentDateTime = null;
    }

    // ========== Getters & Setters ==========

    public Appointment getCurrentAppointment() {
        return currentAppointment;
    }

    public void setCurrentAppointment(Appointment currentAppointment) {
        this.currentAppointment = currentAppointment;
    }

    public String getSelectedCustomerID() {
        return selectedCustomerID;
    }

    public void setSelectedCustomerID(String selectedCustomerID) {
        this.selectedCustomerID = selectedCustomerID;
    }

    public String getSelectedTechnicianID() {
        return selectedTechnicianID;
    }

    public void setSelectedTechnicianID(String selectedTechnicianID) {
        this.selectedTechnicianID = selectedTechnicianID;
    }

    public String getSelectedServiceID() {
        return selectedServiceID;
    }

    public void setSelectedServiceID(String selectedServiceID) {
        this.selectedServiceID = selectedServiceID;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Technician> getTechnicians() {
        return technicians;
    }

    public List<Service> getServices() {
        return services;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
