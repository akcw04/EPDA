package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Feedback entity. Represents feedback written by technicians about their appointments.
 * Uses JPA annotations for table generation; accessed via JDBC in FeedbackFacade.
 */
@Entity
@Table(name = "FEEDBACK")
public class Feedback implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "feedback_id", length = 30)
    private String id;

    @Column(name = "appointment_id", length = 30, nullable = false)
    private String appointmentId;

    @Column(name = "technician_id", length = 30, nullable = false)
    private String technicianId;

    @Column(name = "feedback_text", length = 1000)
    private String feedbackText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Transient fields for in-memory use (populated by facade)
    @Transient
    private Appointment appointment;
    @Transient
    private Technician technician;

    public Feedback() {
        this.createdAt = LocalDateTime.now();
    }

    public Feedback(String id, String appointmentId, String technicianId, String feedbackText) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.technicianId = technicianId;
        this.feedbackText = feedbackText;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }

    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public Technician getTechnician() { return technician; }
    public void setTechnician(Technician technician) { this.technician = technician; }

    @Override
    public String toString() {
        return "Feedback{id='" + id + "', feedbackText='" + feedbackText + "', createdAt=" + createdAt + "}";
    }
}
