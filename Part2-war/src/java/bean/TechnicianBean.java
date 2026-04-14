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
        if (pendingAppointmentList == null || inProgressAppointmentList == null ||
            completedAppointmentList == null || feedbackList == null ||
            commentsList == null) {
            loadDashboardData();
        }
        if (profileData == null) {
            loadProfile();
        }
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
            if (profileData == null) {
                addError("Profile not found.");
                return;
            }

            Technician existing = userFacade.getTechnicianByID(profileData.getId());
            if (existing == null) {
                profileData = null;
                addError("Profile no longer exists.");
                return;
            }

            boolean valid = validateUserFields(
                    profileData.getName(),
                    profileData.getEmail(),
                    profileData.getGender(),
                    profileData.getPhone(),
                    profileData.getIc(),
                    profileData.getAddress(),
                    existing.getEmail(),
                    existing.getIc());

            if (!validateOptionalSpecialty(profileData.getSpecialty())) {
                valid = false;
            }

            if (!valid) {
                return;
            }

            profileData.setName(sanitizeText(profileData.getName()));
            profileData.setEmail(ValidationUtil.normalizeEmail(profileData.getEmail()));
            profileData.setGender(sanitizeText(profileData.getGender()));
            profileData.setPhone(sanitizeText(profileData.getPhone()));
            profileData.setIc(sanitizeText(profileData.getIc()));
            profileData.setAddress(sanitizeText(profileData.getAddress()));
            profileData.setSpecialty(resolveSpecialty(profileData.getSpecialty()));

            userFacade.updateTechnician(profileData);
            loadProfile();
            addInfo("Profile updated successfully.");
        } catch (Exception e) {
            addError("Error updating profile: " + e.getMessage());
        }
    }

    // ========== APPOINTMENT STATUS ==========

    public void startAppointment(String appointmentId) {
        try {
            Appointment a = appointmentFacade.getAppointmentByID(appointmentId);
            if (a != null) {
                if (!loginBean.getUserId().equals(a.getTechnicianId())) {
                    addError("You can only update your own appointments.");
                    return;
                }
                if (!Appointment.STATUS_PENDING.equalsIgnoreCase(a.getStatus())) {
                    addError("Only pending appointments can be started.");
                    return;
                }
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
                if (!loginBean.getUserId().equals(a.getTechnicianId())) {
                    addError("You can only update your own appointments.");
                    return;
                }
                if (!Appointment.STATUS_IN_PROGRESS.equalsIgnoreCase(a.getStatus())) {
                    addError("Only appointments in progress can be completed.");
                    return;
                }
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
            Appointment appointment = appointmentFacade.getAppointmentByID(feedbackAppointmentId);
            if (appointment == null || !techId.equals(appointment.getTechnicianId())) {
                addError("Invalid appointment selected.");
                return;
            }
            if (!Appointment.STATUS_COMPLETED.equalsIgnoreCase(appointment.getStatus())) {
                addError("Feedback can only be submitted for completed appointments.");
                return;
            }
            if (feedbackFacade.hasFeedbackForAppointment(feedbackAppointmentId)) {
                addError("Feedback has already been submitted for this appointment.");
                return;
            }

            Feedback feedback = new Feedback();
            feedback.setAppointmentId(feedbackAppointmentId);
            feedback.setTechnicianId(techId);
            feedback.setFeedbackText(feedbackText.trim());

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

    private boolean validateUserFields(String name, String email, String gender,
                                       String phone, String ic, String address,
                                       String existingEmail, String existingIc) {
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

    private boolean validateOptionalSpecialty(String specialty) {
        String trimmedSpecialty = sanitizeText(specialty);
        if (trimmedSpecialty == null) {
            return true;
        }
        if (!ValidationUtil.isValidSpecialty(trimmedSpecialty)) {
            addError(ValidationUtil.getErrorMessage("specialty"));
            return false;
        }
        return true;
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

    private String resolveSpecialty(String specialty) {
        String trimmedSpecialty = sanitizeText(specialty);
        return trimmedSpecialty == null ? "General" : trimmedSpecialty;
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
