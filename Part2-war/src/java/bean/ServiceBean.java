package bean;

import entity.Service;
import facade.ServiceFacade;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.*;

/**
 * ServiceBean - JSF Managed Bean for service management.
 * SessionScoped: maintains service data across pages.
 * Calls ServiceFacade (EJB) for business logic.
 */
@Named("serviceBean")
@SessionScoped
public class ServiceBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private ServiceFacade serviceFacade;

    // Form fields
    private Service currentService;
    private List<Service> services;
    private List<Service> normalServices;
    private List<Service> majorServices;
    private String filterType = "All";  // All, Normal, Major
    private String message;
    private String errorMessage;

    /**
     * Initialize bean
     */
    public void init() {
        loadServices();
        loadNormalServices();
        loadMajorServices();
    }

    /**
     * Load all services
     */
    public void loadServices() {
        try {
            services = serviceFacade.getAllServices();
        } catch (Exception e) {
            errorMessage = "Failed to load services: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Load services by type - Normal (1 hour)
     */
    public void loadNormalServices() {
        try {
            normalServices = serviceFacade.getServicesByType(Service.TYPE_NORMAL);
        } catch (Exception e) {
            errorMessage = "Failed to load normal services: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Load services by type - Major (3 hours)
     */
    public void loadMajorServices() {
        try {
            majorServices = serviceFacade.getServicesByType(Service.TYPE_MAJOR);
        } catch (Exception e) {
            errorMessage = "Failed to load major services: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Create a new service
     */
    public void createService() {
        try {
            if (currentService == null || currentService.getServiceName() == null || 
                currentService.getType() == null || currentService.getBasePrice() <= 0) {
                errorMessage = "Please fill required fields (Service Name, Type, Price > 0)";
                return;
            }
            serviceFacade.createService(currentService);
            message = "Service created successfully: " + currentService.getId();
            errorMessage = null;
            loadServices();
            loadNormalServices();
            loadMajorServices();
            currentService = null;
        } catch (Exception e) {
            errorMessage = "Failed to create service: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Update service
     */
    public void updateService() {
        try {
            if (currentService == null) {
                errorMessage = "No service selected";
                return;
            }
            serviceFacade.updateService(currentService);
            message = "Service updated successfully";
            errorMessage = null;
            loadServices();
            loadNormalServices();
            loadMajorServices();
        } catch (Exception e) {
            errorMessage = "Failed to update service: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Delete service
     */
    public void deleteService(String serviceID) {
        try {
            serviceFacade.deleteService(serviceID);
            message = "Service deleted successfully";
            errorMessage = null;
            loadServices();
            loadNormalServices();
            loadMajorServices();
        } catch (Exception e) {
            errorMessage = "Failed to delete service: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Edit service
     */
    public void editService(Service service) {
        currentService = service;
    }

    /**
     * New service form
     */
    public void newService() {
        currentService = new Service();
    }

    /**
     * Get appropriate service list based on filter
     */
    public List<Service> getFilteredServices() {
        if ("Normal".equals(filterType)) {
            return normalServices;
        } else if ("Major".equals(filterType)) {
            return majorServices;
        }
        return services;
    }

    // ========== Getters & Setters ==========

    public Service getCurrentService() {
        return currentService;
    }

    public void setCurrentService(Service currentService) {
        this.currentService = currentService;
    }

    public List<Service> getServices() {
        return services;
    }

    public List<Service> getNormalServices() {
        return normalServices;
    }

    public List<Service> getMajorServices() {
        return majorServices;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
