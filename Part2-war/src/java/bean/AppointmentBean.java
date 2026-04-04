package bean;

import entity.*;
import facade.*;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * AppointmentBean - Supports the shared appointments.xhtml page.
 * Role-specific appointment logic is in CounterStaffBean, TechnicianBean, CustomerBean.
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

    private String selectedCustomerID;
    private String selectedTechnicianID;
    private String selectedServiceID;
    private Date appointmentDateTime;

    public List<Appointment> getAppointments() {
        try { return appointmentFacade.getAllAppointments(); }
        catch (Exception e) { return List.of(); }
    }

    public List<Customer> getCustomers() {
        try { return userFacade.getAllCustomers(); }
        catch (Exception e) { return List.of(); }
    }

    public List<Technician> getTechnicians() {
        try { return userFacade.getAllTechnicians(); }
        catch (Exception e) { return List.of(); }
    }

    public List<Service> getServices() {
        try { return serviceFacade.getAllServices(); }
        catch (Exception e) { return List.of(); }
    }

    public void createAppointment() {
        try {
            if (selectedCustomerID == null || selectedTechnicianID == null ||
                selectedServiceID == null || appointmentDateTime == null) return;

            Customer customer = userFacade.getCustomerByID(selectedCustomerID);
            Technician technician = userFacade.getTechnicianByID(selectedTechnicianID);
            Service service = serviceFacade.getServiceByID(selectedServiceID);

            if (customer == null || technician == null || service == null) return;

            Appointment a = new Appointment();
            a.setCustomer(customer);
            a.setTechnician(technician);
            a.setService(service);
            a.setAppointmentDateTime(
                appointmentDateTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            a.setStatus(Appointment.STATUS_PENDING);
            a.setPaymentAmount(service.getBasePrice());
            appointmentFacade.createAppointment(a);

            selectedCustomerID = selectedTechnicianID = selectedServiceID = null;
            appointmentDateTime = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewAppointment(String id) {
        // No-op for shared page
    }

    public void deleteAppointment(String id) {
        try { appointmentFacade.deleteAppointment(id); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // Getters & Setters
    public String getSelectedCustomerID() { return selectedCustomerID; }
    public void setSelectedCustomerID(String selectedCustomerID) { this.selectedCustomerID = selectedCustomerID; }

    public String getSelectedTechnicianID() { return selectedTechnicianID; }
    public void setSelectedTechnicianID(String selectedTechnicianID) { this.selectedTechnicianID = selectedTechnicianID; }

    public String getSelectedServiceID() { return selectedServiceID; }
    public void setSelectedServiceID(String selectedServiceID) { this.selectedServiceID = selectedServiceID; }

    public Date getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(Date appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
}
