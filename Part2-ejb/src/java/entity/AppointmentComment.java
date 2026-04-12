package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AppointmentComment entity. Represents comments written by customers on their appointments.
 */
@Entity
@Table(name = "APPOINTMENT_COMMENT")
public class AppointmentComment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "comment_id", length = 30)
    private String id;

    @Column(name = "appointment_id", length = 30, nullable = false)
    private String appointmentId;

    @Column(name = "customer_id", length = 30, nullable = false)
    private String customerId;

    @Column(name = "comment_text", length = 500)
    private String commentText;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    public AppointmentComment() {
        this.createdAt = LocalDateTime.now();
    }

    public AppointmentComment(String id, String appointmentId, String customerId,
                              String commentText, Integer rating) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.commentText = commentText;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    @Override
    public String toString() {
        return "AppointmentComment{id='" + id + "', commentText='" + commentText + "', rating=" + rating + "}";
    }
}
