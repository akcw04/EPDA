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
 * Customer Dashboard Bean.
 * Requirements: Edit profile, access service/payment histories,
 * access feedback, comment on individual appointments.
 */
@Named("customerBean")
@ViewScoped
public class CustomerBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private PaymentFacade paymentFacade;
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
    private int totalAppointments, completedAppointmentCount, pendingAppointments;
    private double totalSpent;

    // Data lists
    private List<Service> services;
    private List<Appointment> upcomingAppointments;
    private List<Appointment> completedAppointmentsList;
    private List<Payment> paymentHistory;
    private List<Feedback> feedbackList;
    private List<AppointmentComment> myComments;

    // Profile editing
    private Customer profileData;

    // Comment form
    private String commentAppointmentId;
    private String commentText;
    private Integer commentRating;

    public void init() {
        if (services == null || upcomingAppointments == null ||
            completedAppointmentsList == null || paymentHistory == null ||
            feedbackList == null || myComments == null) {
            loadDashboardData();
        }
        if (profileData == null) {
            loadProfile();
        }
    }

    public void loadDashboardData() {
        try {
            String custId = loginBean != null ? loginBean.getUserId() : null;
            if (custId == null) return;

            services = serviceFacade.getAllServices();

            List<Appointment> allAppts = appointmentFacade.getAppointmentsByCustomer(custId);
            upcomingAppointments = new ArrayList<>();
            completedAppointmentsList = new ArrayList<>();

            for (Appointment a : allAppts) {
                if ("Completed".equalsIgnoreCase(a.getStatus())) {
                    completedAppointmentsList.add(a);
                } else if (!"Cancelled".equalsIgnoreCase(a.getStatus())) {
                    upcomingAppointments.add(a);
                }
            }

            totalAppointments = allAppts.size();
            completedAppointmentCount = completedAppointmentsList.size();
            pendingAppointments = 0;
            for (Appointment a : upcomingAppointments) {
                if ("Pending".equalsIgnoreCase(a.getStatus())) pendingAppointments++;
            }

            paymentHistory = paymentFacade.getPaymentsByCustomer(custId);
            totalSpent = paymentFacade.getTotalPaymentsByCustomer(custId);

            // Feedback written by technicians about this customer's appointments
            feedbackList = new ArrayList<>();
            for (Appointment a : allAppts) {
                try {
                    Feedback fb = feedbackFacade.getFeedbackByAppointment(a.getId());
                    if (fb != null) feedbackList.add(fb);
                } catch (Exception ignore) {}
            }

            // Customer's own comments
            myComments = commentFacade.getCommentsByCustomer(custId);

        } catch (Exception e) {
            System.err.println("Error loading customer dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadProfile() {
        try {
            if (loginBean != null && loginBean.getUserId() != null) {
                profileData = userFacade.getCustomerByID(loginBean.getUserId());
            }
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
        }
    }

    // ========== PROFILE EDITING ==========

    public void updateProfile() {
        try {
            if (profileData != null) {
                userFacade.updateCustomer(profileData);
                addInfo("Profile updated successfully.");
            }
        } catch (Exception e) {
            addError("Error updating profile: " + e.getMessage());
        }
    }

    // ========== CANCEL APPOINTMENT ==========

    public void cancelAppointment(String appointmentId) {
        try {
            Appointment a = appointmentFacade.getAppointmentByID(appointmentId);
            if (a != null) {
                if (!loginBean.getUserId().equals(a.getCustomerId())) {
                    addError("You can only cancel your own appointments.");
                    return;
                }
                if (!Appointment.STATUS_PENDING.equalsIgnoreCase(a.getStatus())) {
                    addError("Only pending appointments can be cancelled.");
                    return;
                }
                a.setStatus(Appointment.STATUS_CANCELLED);
                appointmentFacade.updateAppointment(a);
                addInfo("Appointment cancelled.");
                loadDashboardData();
            } else {
                addError("Appointment not found.");
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    // ========== COMMENT ON APPOINTMENT ==========

    public void submitComment() {
        try {
            if (commentAppointmentId == null || commentText == null ||
                commentAppointmentId.isBlank() || commentText.isBlank()) {
                addError("Please select an appointment and enter your comment.");
                return;
            }

            String custId = loginBean.getUserId();
            Appointment appointment = appointmentFacade.getAppointmentByID(commentAppointmentId);
            if (appointment == null || !custId.equals(appointment.getCustomerId())) {
                addError("Invalid appointment selected.");
                return;
            }
            if (!Appointment.STATUS_COMPLETED.equalsIgnoreCase(appointment.getStatus())) {
                addError("Comments can only be submitted for completed appointments.");
                return;
            }
            if (commentFacade.hasCommentForAppointmentByCustomer(commentAppointmentId, custId)) {
                addError("You have already submitted a comment for this appointment.");
                return;
            }

            AppointmentComment comment = new AppointmentComment();
            comment.setAppointmentId(commentAppointmentId);
            comment.setCustomerId(custId);
            comment.setCommentText(commentText.trim());
            comment.setRating(commentRating);

            boolean success = commentFacade.createComment(comment);
            if (success) {
                addInfo("Comment submitted successfully.");
                commentAppointmentId = null;
                commentText = null;
                commentRating = null;
                loadDashboardData();
            } else {
                addError("Failed to submit comment.");
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

    public int getTotalAppointments() { return totalAppointments; }
    public int getCompletedAppointmentCount() { return completedAppointmentCount; }
    public int getPendingAppointments() { return pendingAppointments; }
    public double getTotalSpent() { return totalSpent; }

    public List<Service> getServices() { if (services == null) loadDashboardData(); return services; }
    public List<Appointment> getUpcomingAppointments() { if (upcomingAppointments == null) loadDashboardData(); return upcomingAppointments; }
    public List<Appointment> getCompletedAppointmentsList() { if (completedAppointmentsList == null) loadDashboardData(); return completedAppointmentsList; }
    public List<Payment> getPaymentHistory() { if (paymentHistory == null) loadDashboardData(); return paymentHistory; }
    public List<Feedback> getFeedbackList() { return feedbackList; }
    public List<AppointmentComment> getMyComments() { return myComments; }

    public Customer getProfileData() { return profileData; }
    public void setProfileData(Customer profileData) { this.profileData = profileData; }

    public String getCommentAppointmentId() { return commentAppointmentId; }
    public void setCommentAppointmentId(String commentAppointmentId) { this.commentAppointmentId = commentAppointmentId; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public Integer getCommentRating() { return commentRating; }
    public void setCommentRating(Integer commentRating) { this.commentRating = commentRating; }

    public String getCurrentSection() { return currentSection; }
    public void setCurrentSection(String currentSection) { this.currentSection = currentSection; }
}
