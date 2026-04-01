package entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Appointment entity. Implements Serializable for session storage.
 * Uses composition: contains Customer, Technician, and Service objects.
 * Status: Pending, InProgress, Completed, Cancelled
 */
public class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private Customer customer;
    private Technician technician;
    private Service service;
    private LocalDateTime appointmentDateTime;
    private String status;  // Pending, InProgress, Completed, Cancelled
    private double paymentAmount;
    private String comments;
    private Integer rating;  // 1-5 or null if not rated

    // Status constants
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_IN_PROGRESS = "InProgress";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    /**
     * Default constructor
     */
    public Appointment() {
        this.status = STATUS_PENDING;
    }

    /**
     * Constructor with essential fields
     *
     * @param id appointment identifier
     * @param customer Customer object
     * @param technician Technician object
     * @param service Service object
     * @param appointmentDateTime scheduled appointment date/time
     */
    public Appointment(String id, Customer customer, Technician technician, 
                      Service service, LocalDateTime appointmentDateTime) {
        this.id = id;
        this.customer = customer;
        this.technician = technician;
        this.service = service;
        this.appointmentDateTime = appointmentDateTime;
        this.status = STATUS_PENDING;
        this.paymentAmount = service.getBasePrice();
    }

    // ========== Getters & Setters ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Technician getTechnician() {
        return technician;
    }

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id='" + id + '\'' +
                ", customer=" + (customer != null ? customer.getId() : "null") +
                ", technician=" + (technician != null ? technician.getId() : "null") +
                ", service=" + (service != null ? service.getId() : "null") +
                ", appointmentDateTime=" + appointmentDateTime +
                ", status='" + status + '\'' +
                ", paymentAmount=" + paymentAmount +
                ", rating=" + rating +
                '}';
    }
}
