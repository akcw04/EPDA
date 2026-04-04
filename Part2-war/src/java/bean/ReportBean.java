package bean;

import facade.AppointmentFacade;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportBean - Supports the shared reports.xhtml page.
 * Role-specific reporting is also in ManagerBean.
 */
@Named("reportBean")
@ViewScoped
public class ReportBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private AppointmentFacade appointmentFacade;

    private double dailyRevenue;
    private Map<String, Integer> technicianWorkload = new HashMap<>();
    private Map<String, Integer> servicePopularity = new HashMap<>();
    private List<Map<String, Object>> customerFeedback = List.of();
    private Map<String, Object> statusAnalytics = new HashMap<>();

    public void init() {
        refreshAllReports();
    }

    public void refreshAllReports() {
        try {
            dailyRevenue = appointmentFacade.getDailyRevenue();
            technicianWorkload = appointmentFacade.getTechnicianWorkload();
            servicePopularity = appointmentFacade.getServicePopularity();
            customerFeedback = appointmentFacade.getCustomerFeedback();
            statusAnalytics = appointmentFacade.getStatusAnalytics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getters
    public double getDailyRevenue() { return dailyRevenue; }
    public Map<String, Integer> getTechnicianWorkload() { return technicianWorkload; }
    public Map<String, Integer> getServicePopularity() { return servicePopularity; }
    public List<Map<String, Object>> getCustomerFeedback() { return customerFeedback; }
    public Map<String, Object> getStatusAnalytics() { return statusAnalytics; }
}
