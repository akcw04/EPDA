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
 * Technician Dashboard Bean.
 * Requirements: Edit profile, check assigned appointments, update to completed,
 * write feedback, view comments on assigned appointments.
 */
@Named("technicianBean")
@ViewScoped
public class TechnicianBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private FeedbackFacade feedbackFacade;
    @EJB
    private AppointmentCommentFacade commentFacade;
    @EJB
    private UserFacade userFacade;
    @Inject
    private LoginBean loginBean;

    // Sidebar navigation
    private String currentSection = "overview";

    // Dashboard statistics
    private int todayAppointments, pendingAppointments, completedThisMonth;
    private double averageRating;

    // Appointment lists
    private List<Appointment> pendingAppointmentList;
    private List<Appointment> inProgressAppointmentList;
    private List<Appointment> completedAppointmentList;
    private List<Feedback> feedbackList;
    private List<AppointmentComment> commentsList;

    // Profile editing
    private Technician profileData;

    // Feedback creation form
    private String feedbackAppointmentId;
    private String feedbackText;

    // KPI
    private int totalAppointments;
    private double totalRevenue;

    public void init() {
        loadDashboardData();
        loadProfile();
    }

    public void loadDashboardData() {
        try {
            String techId = loginBean != null ? loginBean.getUserId() : null;
            if (techId == null) return;

            List<Appointment> myAppointments = appointmentFacade.getAppointmentsByTechnician(techId);

            pendingAppointmentList = new ArrayList<>();
            inProgressAppointmentList = new ArrayList<>();
            completedAppointmentList = new ArrayList<>();

            LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfToday = startOfToday.plusDays(1);
            LocalDateTime startOfMonth = LocalDateTime.now().toLocalDate().withDayOfMonth(1).atStartOfDay();

            todayAppointments = 0;
            totalRevenue = 0;

            for (Appointment a : myAppointments) {
                String status = a.getStatus();
                if ("Pending".equalsIgnoreCase(status)) {
                    pendingAppointmentList.add(a);
                } else if ("InProgress".equalsIgnoreCase(status)) {
                    inProgressAppointmentList.add(a);
                } else if ("Completed".equalsIgnoreCase(status)) {
                    completedAppointmentList.add(a);
                    totalRevenue += a.getPaymentAmount();
                }

                if (a.getAppointmentDateTime() != null &&
                    a.getAppointmentDateTime().isAfter(startOfToday) &&
                    a.getAppointmentDateTime().isBefore(endOfToday)) {
                    todayAppointments++;
                }
            }

            pendingAppointments = pendingAppointmentList.size();
            totalAppointments = myAppointments.size();

            completedThisMonth = 0;
            for (Appointment a : completedAppointmentList) {
                if (a.getAppointmentDateTime() != null &&
                    a.getAppointmentDateTime().isAfter(startOfMonth)) {
                    completedThisMonth++;
                }
            }

            // Average rating from customer comments
            averageRating = commentFacade.getAverageRatingByTechnician(techId);

            // Load technician's own feedback
            feedbackList = feedbackFacade.getFeedbackByTechnician(techId);

            // Load customer comments on this technician's appointments
            commentsList = commentFacade.getCommentsByTechnician(techId);

        } catch (Exception e) {
            System.err.println("Error loading technician dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadProfile() {
        try {
            if (loginBean != null && loginBean.getUserId() != null) {
                profileData = userFacade.getTechnicianByID(loginBean.getUserId());
            }
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
        }
    }

    // ========== PROFILE EDITING ==========

    public void updateProfile() {
        try {
            if (profileData != null) {
                userFacade.updateTechnician(profileData);
                addInfo("Profile updated successfully.");
            }
        } catch (Exception e) {
            addError("Error updating profile: " + e.getMessage());
        }
    }

    // ========== APPOINTMENT STATUS ==========

    public void startAppointment(String appointmentId) {
        try {
            Appointment a = appointmentFacade.getAppointmentByID(appointmentId);
            if (a != null) {
                a.setStatus(Appointment.STATUS_IN_PROGRESS);
                appointmentFacade.updateAppointment(a);
                addInfo("Appointment started.");
                loadDashboardData();
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    public void completeAppointment(String appointmentId) {
        try {
            Appointment a = appointmentFacade.getAppointmentByID(appointmentId);
            if (a != null) {
                a.setStatus(Appointment.STATUS_COMPLETED);
                appointmentFacade.updateAppointment(a);
                addInfo("Appointment marked as completed.");
                loadDashboardData();
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    // ========== FEEDBACK CREATION ==========

    public void submitFeedback() {
        try {
            if (feedbackAppointmentId == null || feedbackText == null ||
                feedbackAppointmentId.isBlank() || feedbackText.isBlank()) {
                addError("Please select an appointment and enter feedback.");
                return;
            }

            String techId = loginBean.getUserId();
            Feedback feedback = new Feedback();
            feedback.setAppointmentId(feedbackAppointmentId);
            feedback.setTechnicianId(techId);
            feedback.setFeedbackText(feedbackText);

            boolean success = feedbackFacade.createFeedback(feedback);
            if (success) {
                addInfo("Feedback submitted successfully.");
                feedbackAppointmentId = null;
                feedbackText = null;
                loadDashboardData();
            } else {
                addError("Failed to submit feedback.");
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
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

    // ========== GETTERS & SETTERS ==========

    public int getTodayAppointments() { return todayAppointments; }
    public int getPendingAppointments() { return pendingAppointments; }
    public int getCompletedThisMonth() { return completedThisMonth; }
    public double getAverageRating() { return Math.round(averageRating * 10.0) / 10.0; }
    public int getTotalAppointments() { return totalAppointments; }
    public double getTotalRevenue() { return totalRevenue; }

    public List<Appointment> getPendingAppointmentList() { return pendingAppointmentList; }
    public List<Appointment> getInProgressAppointmentList() { return inProgressAppointmentList; }
    public List<Appointment> getCompletedAppointmentList() { return completedAppointmentList; }
    public List<Feedback> getFeedbackList() { return feedbackList; }
    public List<AppointmentComment> getCommentsList() { return commentsList; }

    public Technician getProfileData() { return profileData; }
    public void setProfileData(Technician profileData) { this.profileData = profileData; }

    public String getFeedbackAppointmentId() { return feedbackAppointmentId; }
    public void setFeedbackAppointmentId(String feedbackAppointmentId) { this.feedbackAppointmentId = feedbackAppointmentId; }
    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

    public String getCurrentSection() { return currentSection; }
    public void setCurrentSection(String currentSection) { this.currentSection = currentSection; }
}
