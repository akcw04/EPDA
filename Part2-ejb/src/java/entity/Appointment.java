package entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * Appointment entity. Uses JPA for table generation, JDBC for data access.
 * Status: Pending, InProgress, Completed, Cancelled
 */
@Entity
@Table(name = "APPOINTMENT")
public class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "appointment_id", length = 30)
    private String id;

    @Column(name = "customer_id", length = 30, nullable = false)
    private String customerId;

    @Column(name = "technician_id", length = 30, nullable = false)
    private String technicianId;

    @Column(name = "service_id", length = 30, nullable = false)
    private String serviceId;

    @Column(name = "appointment_datetime", nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "payment_amount")
    private double paymentAmount;

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "rating")
    private Integer rating;

    // Status constants
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_IN_PROGRESS = "InProgress";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    // Transient fields for in-memory use (populated by facade)
    @Transient
    private Customer customer;
    @Transient
    private Technician technician;
    @Transient
    private Service service;

    public Appointment() {
        this.status = STATUS_PENDING;
    }

    public Appointment(String id, Customer customer, Technician technician,
                       Service service, LocalDateTime appointmentDateTime) {
        this.id = id;
        this.customer = customer;
        this.technician = technician;
        this.service = service;
        this.customerId = customer.getId();
        this.technicianId = technician.getId();
        this.serviceId = service.getId();
        this.appointmentDateTime = appointmentDateTime;
        this.status = STATUS_PENDING;
        this.paymentAmount = service.getBasePrice();
    }

    // Getters & Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) this.customerId = customer.getId();
    }

    public Technician getTechnician() { return technician; }
    public void setTechnician(Technician technician) {
        this.technician = technician;
        if (technician != null) this.technicianId = technician.getId();
    }

    public Service getService() { return service; }
    public void setService(Service service) {
        this.service = service;
        if (service != null) this.serviceId = service.getId();
    }

    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(double paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    @Override
    public String toString() {
        return "Appointment{id='" + id + "', status='" + status + "', dateTime=" + appointmentDateTime + "}";
    }
}
