package bean;

import facade.AppointmentFacade;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.*;

/**
 * ReportBean - JSF Managed Bean for displaying reports.
 * ViewScoped: maintains report data within a single view.
 * Calls AppointmentFacade (EJB) for the 5 required reporting methods.
 */
@Named("reportBean")
@ViewScoped
public class ReportBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private AppointmentFacade appointmentFacade;

    // Report data
    private double dailyRevenue;
    private Map<String, Integer> technicianWorkload;
    private Map<String, Integer> servicePopularity;
    private List<Map<String, Object>> customerFeedback;
    private Map<String, Object> statusAnalytics;
    private String errorMessage;

    /**
     * Initialize and load all reports
     */
    public void init() {
        loadDailyRevenue();
        loadTechnicianWorkload();
        loadServicePopularity();
        loadCustomerFeedback();
        loadStatusAnalytics();
    }

    /**
     * REPORT 1: Load Daily Revenue
     */
    public void loadDailyRevenue() {
        try {
            dailyRevenue = appointmentFacade.getDailyRevenue();
        } catch (Exception e) {
            errorMessage = "Failed to load daily revenue: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * REPORT 2: Load Technician Workload
     */
    public void loadTechnicianWorkload() {
        try {
            technicianWorkload = appointmentFacade.getTechnicianWorkload();
        } catch (Exception e) {
            errorMessage = "Failed to load technician workload: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * REPORT 3: Load Service Popularity
     */
    public void loadServicePopularity() {
        try {
            servicePopularity = appointmentFacade.getServicePopularity();
        } catch (Exception e) {
            errorMessage = "Failed to load service popularity: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * REPORT 4: Load Customer Feedback
     */
    public void loadCustomerFeedback() {
        try {
            customerFeedback = appointmentFacade.getCustomerFeedback();
        } catch (Exception e) {
            errorMessage = "Failed to load customer feedback: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * REPORT 5: Load Status Analytics
     */
    public void loadStatusAnalytics() {
        try {
            statusAnalytics = appointmentFacade.getStatusAnalytics();
        } catch (Exception e) {
            errorMessage = "Failed to load status analytics: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Refresh all reports
     */
    public void refreshAllReports() {
        init();
    }

    // ========== Getters & Setters ==========

    public double getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(double dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    public Map<String, Integer> getTechnicianWorkload() {
        return technicianWorkload;
    }

    public void setTechnicianWorkload(Map<String, Integer> technicianWorkload) {
        this.technicianWorkload = technicianWorkload;
    }

    public Map<String, Integer> getServicePopularity() {
        return servicePopularity;
    }

    public void setServicePopularity(Map<String, Integer> servicePopularity) {
        this.servicePopularity = servicePopularity;
    }

    public List<Map<String, Object>> getCustomerFeedback() {
        return customerFeedback;
    }

    public void setCustomerFeedback(List<Map<String, Object>> customerFeedback) {
        this.customerFeedback = customerFeedback;
    }

    public Map<String, Object> getStatusAnalytics() {
        return statusAnalytics;
    }

    public void setStatusAnalytics(Map<String, Object> statusAnalytics) {
        this.statusAnalytics = statusAnalytics;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
