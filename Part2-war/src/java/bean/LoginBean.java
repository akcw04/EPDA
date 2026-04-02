package bean;

import entity.Manager;
import entity.Technician;
import facade.UserFacade;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * LoginBean - JSF Managed Bean for authentication and session management.
 * SessionScoped: maintains logged-in user across all pages.
 * Calls UserFacade.authenticate() which hashes the password and checks all user tables.
 */
@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private UserFacade userFacade;

    // Login form fields
    private String email;
    private String password;

    // Session state
    private boolean loggedIn = false;
    private String userRole;      // "Manager" or "Technician"
    private String userName;
    private String userId;
    private Object currentUser;   // Manager or Technician object

    /**
     * Perform login. Delegates hashing + DB lookup to UserFacade.authenticate().
     * @return navigation outcome
     */
    public String login() {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            addError("Please enter both email and password.");
            return null;
        }

        Object[] result = userFacade.authenticate(email, password);

        if (result != null) {
            currentUser = result[0];
            userRole = (String) result[1];
            loggedIn = true;

            if ("Manager".equals(userRole)) {
                Manager m = (Manager) currentUser;
                userName = m.getName();
                userId = m.getId();
            } else if ("Technician".equals(userRole)) {
                Technician t = (Technician) currentUser;
                userName = t.getName();
                userId = t.getId();
            }

            // Clear sensitive form data
            password = null;
            return "/index.xhtml?faces-redirect=true";
        }

        addError("Invalid email or password.");
        return null;
    }

    /**
     * Logout and invalidate session.
     * @return navigation outcome to login page
     */
    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    /**
     * Check if the current user has a specific role.
     * @param role the role to check
     * @return true if the user's role matches
     */
    public boolean hasRole(String role) {
        return loggedIn && role != null && role.equals(userRole);
    }

    private void addError(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    // ========== Getters & Setters ==========

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public Object getCurrentUser() {
        return currentUser;
    }
}
