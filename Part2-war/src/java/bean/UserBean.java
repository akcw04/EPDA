package bean;

import entity.Customer;
import entity.Technician;
import entity.Manager;
import facade.UserFacade;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.*;

/**
 * UserBean - JSF Managed Bean for user management.
 * SessionScoped: maintains user data across pages.
 * Supports Customer, Technician, and Manager CRUD operations.
 * Calls UserFacade (EJB) for business logic.
 */
@Named("userBean")
@SessionScoped
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private UserFacade userFacade;

    // Form fields
    private String userType = "Customer";  // Customer, Technician, Manager
    private Customer currentCustomer;
    private Technician currentTechnician;
    private Manager currentManager;
    private List<Customer> customers;
    private List<Technician> technicians;
    private List<Manager> managers;
    private String message;
    private String errorMessage;

    /**
     * Initialize bean
     */
    public void init() {
        loadCustomers();
        loadTechnicians();
        loadManagers();
    }

    // ========== CUSTOMER OPERATIONS ==========

    /**
     * Load all customers
     */
    public void loadCustomers() {
        try {
            customers = userFacade.getAllCustomers();
        } catch (Exception e) {
            errorMessage = "Failed to load customers: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Create a new customer
     */
    public void createCustomer() {
        try {
            if (currentCustomer == null || currentCustomer.getName() == null || 
                currentCustomer.getEmail() == null) {
                errorMessage = "Please fill required fields (Name, Email)";
                return;
            }
            userFacade.createCustomer(currentCustomer);
            message = "Customer created successfully: " + currentCustomer.getId();
            errorMessage = null;
            loadCustomers();
            currentCustomer = null;
        } catch (Exception e) {
            errorMessage = "Failed to create customer: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Update customer
     */
    public void updateCustomer() {
        try {
            if (currentCustomer == null) {
                errorMessage = "No customer selected";
                return;
            }
            userFacade.updateCustomer(currentCustomer);
            message = "Customer updated successfully";
            errorMessage = null;
            loadCustomers();
        } catch (Exception e) {
            errorMessage = "Failed to update customer: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Delete customer
     */
    public void deleteCustomer(String customerID) {
        try {
            userFacade.deleteCustomer(customerID);
            message = "Customer deleted successfully";
            errorMessage = null;
            loadCustomers();
        } catch (Exception e) {
            errorMessage = "Failed to delete customer: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Edit customer
     */
    public void editCustomer(Customer customer) {
        currentCustomer = customer;
    }

    /**
     * New customer form
     */
    public void newCustomer() {
        currentCustomer = new Customer();
    }

    // ========== TECHNICIAN OPERATIONS ==========

    /**
     * Load all technicians
     */
    public void loadTechnicians() {
        try {
            technicians = userFacade.getAllTechnicians();
        } catch (Exception e) {
            errorMessage = "Failed to load technicians: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Create a new technician
     */
    public void createTechnician() {
        try {
            if (currentTechnician == null || currentTechnician.getName() == null || 
                currentTechnician.getEmail() == null || currentTechnician.getSpecialty() == null) {
                errorMessage = "Please fill required fields (Name, Email, Specialty)";
                return;
            }
            userFacade.createTechnician(currentTechnician);
            message = "Technician created successfully: " + currentTechnician.getId();
            errorMessage = null;
            loadTechnicians();
            currentTechnician = null;
        } catch (Exception e) {
            errorMessage = "Failed to create technician: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Update technician
     */
    public void updateTechnician() {
        try {
            if (currentTechnician == null) {
                errorMessage = "No technician selected";
                return;
            }
            userFacade.updateTechnician(currentTechnician);
            message = "Technician updated successfully";
            errorMessage = null;
            loadTechnicians();
        } catch (Exception e) {
            errorMessage = "Failed to update technician: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Delete technician
     */
    public void deleteTechnician(String technicianID) {
        try {
            userFacade.deleteTechnician(technicianID);
            message = "Technician deleted successfully";
            errorMessage = null;
            loadTechnicians();
        } catch (Exception e) {
            errorMessage = "Failed to delete technician: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Edit technician
     */
    public void editTechnician(Technician technician) {
        currentTechnician = technician;
    }

    /**
     * New technician form
     */
    public void newTechnician() {
        currentTechnician = new Technician();
    }

    // ========== MANAGER OPERATIONS ==========

    /**
     * Load all managers
     */
    public void loadManagers() {
        try {
            managers = userFacade.getAllManagers();
        } catch (Exception e) {
            errorMessage = "Failed to load managers: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Create a new manager
     */
    public void createManager() {
        try {
            if (currentManager == null || currentManager.getName() == null || 
                currentManager.getEmail() == null) {
                errorMessage = "Please fill required fields (Name, Email)";
                return;
            }
            userFacade.createManager(currentManager);
            message = "Manager created successfully: " + currentManager.getId();
            errorMessage = null;
            loadManagers();
            currentManager = null;
        } catch (Exception e) {
            errorMessage = "Failed to create manager: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Update manager
     */
    public void updateManager() {
        try {
            if (currentManager == null) {
                errorMessage = "No manager selected";
                return;
            }
            userFacade.updateManager(currentManager);
            message = "Manager updated successfully";
            errorMessage = null;
            loadManagers();
        } catch (Exception e) {
            errorMessage = "Failed to update manager: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Delete manager
     */
    public void deleteManager(String managerID) {
        try {
            userFacade.deleteManager(managerID);
            message = "Manager deleted successfully";
            errorMessage = null;
            loadManagers();
        } catch (Exception e) {
            errorMessage = "Failed to delete manager: " + e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Edit manager
     */
    public void editManager(Manager manager) {
        currentManager = manager;
    }

    /**
     * New manager form
     */
    public void newManager() {
        currentManager = new Manager();
    }

    // ========== Getters & Setters ==========

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public void setCurrentCustomer(Customer currentCustomer) {
        this.currentCustomer = currentCustomer;
    }

    public Technician getCurrentTechnician() {
        return currentTechnician;
    }

    public void setCurrentTechnician(Technician currentTechnician) {
        this.currentTechnician = currentTechnician;
    }

    public Manager getCurrentManager() {
        return currentManager;
    }

    public void setCurrentManager(Manager currentManager) {
        this.currentManager = currentManager;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Technician> getTechnicians() {
        return technicians;
    }

    public List<Manager> getManagers() {
        return managers;
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
