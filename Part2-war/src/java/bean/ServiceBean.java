package bean;

import entity.Service;
import facade.ServiceFacade;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceBean - Supports the shared services.xhtml page.
 * Role-specific service management is in ManagerBean.
 */
@Named("serviceBean")
@SessionScoped
public class ServiceBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private ServiceFacade serviceFacade;

    private Service currentService = new Service();
    private String filterType = "All";

    public List<Service> getServices() {
        try { return serviceFacade.getAllServices(); }
        catch (Exception e) { return List.of(); }
    }

    public List<Service> getFilteredServices() {
        try {
            List<Service> all = serviceFacade.getAllServices();
            if ("All".equals(filterType)) return all;
            List<Service> filtered = new ArrayList<>();
            for (Service s : all) {
                if (filterType.equalsIgnoreCase(s.getType())) filtered.add(s);
            }
            return filtered;
        } catch (Exception e) {
            return List.of();
        }
    }

    public void createService() {
        try {
            serviceFacade.createService(currentService);
            currentService = new Service();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void editService(Service s) { this.currentService = s; }

    public void deleteService(String id) {
        try { serviceFacade.deleteService(id); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // Getters & Setters
    public Service getCurrentService() { return currentService; }
    public void setCurrentService(Service currentService) { this.currentService = currentService; }

    public String getFilterType() { return filterType; }
    public void setFilterType(String filterType) { this.filterType = filterType; }
}
