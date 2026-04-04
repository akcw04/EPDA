package bean;

import entity.*;
import facade.UserFacade;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;

/**
 * UserBean - Supports the shared users.xhtml page.
 * Role-specific user management is in ManagerBean, CounterStaffBean.
 */
@Named("userBean")
@SessionScoped
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private UserFacade userFacade;

    private Customer currentCustomer = new Customer();
    private Technician currentTechnician = new Technician();
    private Manager currentManager = new Manager();

    // Customer CRUD
    public List<Customer> getCustomers() {
        try { return userFacade.getAllCustomers(); }
        catch (Exception e) { return List.of(); }
    }

    public void createCustomer() {
        try { userFacade.createCustomer(currentCustomer); currentCustomer = new Customer(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void editCustomer(Customer c) { this.currentCustomer = c; }

    public void deleteCustomer(String id) {
        try { userFacade.deleteCustomer(id); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // Technician CRUD
    public List<Technician> getTechnicians() {
        try { return userFacade.getAllTechnicians(); }
        catch (Exception e) { return List.of(); }
    }

    public void createTechnician() {
        try { userFacade.createTechnician(currentTechnician); currentTechnician = new Technician(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void editTechnician(Technician t) { this.currentTechnician = t; }

    public void deleteTechnician(String id) {
        try { userFacade.deleteTechnician(id); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // Manager CRUD
    public List<Manager> getManagers() {
        try { return userFacade.getAllManagers(); }
        catch (Exception e) { return List.of(); }
    }

    public void createManager() {
        try { userFacade.createManager(currentManager); currentManager = new Manager(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void editManager(Manager m) { this.currentManager = m; }

    public void deleteManager(String id) {
        try { userFacade.deleteManager(id); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // Getters & Setters
    public Customer getCurrentCustomer() { return currentCustomer; }
    public void setCurrentCustomer(Customer currentCustomer) { this.currentCustomer = currentCustomer; }

    public Technician getCurrentTechnician() { return currentTechnician; }
    public void setCurrentTechnician(Technician currentTechnician) { this.currentTechnician = currentTechnician; }

    public Manager getCurrentManager() { return currentManager; }
    public void setCurrentManager(Manager currentManager) { this.currentManager = currentManager; }
}
