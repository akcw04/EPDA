package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Payment entity. Represents payment transactions for appointments.
 * Uses JPA annotations for table generation; accessed via JDBC in PaymentFacade.
 */
@Entity
@Table(name = "PAYMENT")
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "payment_id", length = 30)
    private String id;

    @Column(name = "appointment_id", length = 30, nullable = false)
    private String appointmentId;

    @Column(name = "customer_id", length = 30, nullable = false)
    private String customerId;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "status", length = 20)
    private String status;

    // Transient fields for in-memory use
    @Transient
    private Appointment appointment;
    @Transient
    private Customer customer;

    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.status = "Completed";
    }

    public Payment(String id, String appointmentId, String customerId,
                   double amount, String paymentMethod) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = LocalDateTime.now();
        this.status = "Completed";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    @Override
    public String toString() {
        return "Payment{id='" + id + "', amount=" + amount + ", status='" + status + "'}";
    }
}
