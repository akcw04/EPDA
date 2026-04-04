package servlet;

import entity.*;
import facade.UserFacade;
import util.ValidationUtil;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * RegistrationServlet - Handles user registration form submissions.
 * Validates input fields and creates new users (Customer, Manager, CounterStaff, Technician).
 * Sends JSON responses for AJAX requests or redirects for form submissions.
 */
@WebServlet(name = "RegistrationServlet", urlPatterns = {"/register"})
public class RegistrationServlet extends HttpServlet {

    @EJB
    private UserFacade userFacade;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            String userType = trimParam(request.getParameter("userType"));
            String name = trimParam(request.getParameter("name"));
            String email = trimParam(request.getParameter("email"));
            String password = request.getParameter("password"); // Don't trim password
            String passwordConfirm = request.getParameter("passwordConfirm");
            String gender = trimParam(request.getParameter("gender"));
            String phone = trimParam(request.getParameter("phone"));
            String ic = trimParam(request.getParameter("ic"));
            String address = trimParam(request.getParameter("address"));

            // ========== VALIDATION ==========

            // Validate required fields
            if (userType == null || userType.isBlank()) {
                sendJsonResponse(out, false, "User type is required");
                return;
            }

            if (name == null || name.isBlank()) {
                sendJsonResponse(out, false, "Name is required");
                return;
            }

            if (!ValidationUtil.isValidName(name)) {
                sendJsonResponse(out, false, ValidationUtil.getErrorMessage("name"));
                return;
            }

            if (email == null || email.isBlank()) {
                sendJsonResponse(out, false, "Email is required");
                return;
            }

            if (!ValidationUtil.isValidEmail(email)) {
                sendJsonResponse(out, false, ValidationUtil.getErrorMessage("email"));
                return;
            }

            // Check for duplicate email
            try {
                if (isEmailExists(userType, email)) {
                    sendJsonResponse(out, false, "Email already registered");
                    return;
                }
            } catch (SQLException e) {
                sendJsonResponse(out, false, "Database error checking email");
                return;
            }

            if (password == null || password.isBlank()) {
                sendJsonResponse(out, false, "Password is required");
                return;
            }

            if (!ValidationUtil.isValidPassword(password)) {
                sendJsonResponse(out, false, ValidationUtil.getErrorMessage("password"));
                return;
            }

            if (passwordConfirm == null || passwordConfirm.isBlank()) {
                sendJsonResponse(out, false, "Password confirmation is required");
                return;
            }

            if (!ValidationUtil.passwordsMatch(password, passwordConfirm)) {
                sendJsonResponse(out, false, ValidationUtil.getErrorMessage("passwordMatch"));
                return;
            }

            if (gender == null || gender.isBlank()) {
                sendJsonResponse(out, false, "Gender is required");
                return;
            }

            if (!ValidationUtil.isValidGender(gender)) {
                sendJsonResponse(out, false, ValidationUtil.getErrorMessage("gender"));
                return;
            }

            if (phone == null || phone.isBlank()) {
                sendJsonResponse(out, false, "Phone is required");
                return;
            }

            if (!ValidationUtil.isValidPhone(phone)) {
                sendJsonResponse(out, false, ValidationUtil.getErrorMessage("phone"));
                return;
            }

            if (ic == null || ic.isBlank()) {
                sendJsonResponse(out, false, "IC is required");
                return;
            }

            if (!ValidationUtil.isValidIC(ic)) {
                sendJsonResponse(out, false, ValidationUtil.getErrorMessage("ic"));
                return;
            }

            if (address == null || address.isBlank()) {
                sendJsonResponse(out, false, "Address is required");
                return;
            }

            if (!ValidationUtil.isValidAddress(address)) {
                sendJsonResponse(out, false, ValidationUtil.getErrorMessage("address"));
                return;
            }

            // ========== CREATE USER ==========
            boolean created = false;

            switch (userType.toUpperCase()) {
                case "CUSTOMER":
                    Customer customer = new Customer();
                    customer.setName(name);
                    customer.setEmail(email);
                    customer.setPassword(password);
                    customer.setGender(gender);
                    customer.setPhone(phone);
                    customer.setIc(ic);
                    customer.setAddress(address);
                    created = userFacade.createCustomer(customer);
                    break;

                case "COUNTERSTAFF":
                    CounterStaff counterStaff = new CounterStaff();
                    counterStaff.setName(name);
                    counterStaff.setEmail(email);
                    counterStaff.setPassword(password);
                    counterStaff.setGender(gender);
                    counterStaff.setPhone(phone);
                    counterStaff.setIc(ic);
                    counterStaff.setAddress(address);
                    created = userFacade.createCounterStaff(counterStaff);
                    break;

                case "TECHNICIAN":
                    Technician technician = new Technician();
                    technician.setName(name);
                    technician.setEmail(email);
                    technician.setPassword(password);
                    technician.setGender(gender);
                    technician.setPhone(phone);
                    technician.setIc(ic);
                    technician.setAddress(address);
                    technician.setAvailable(true);
                    created = userFacade.createTechnician(technician);
                    break;

                case "MANAGER":
                    Manager manager = new Manager();
                    manager.setName(name);
                    manager.setEmail(email);
                    manager.setPassword(password);
                    manager.setGender(gender);
                    manager.setPhone(phone);
                    manager.setIc(ic);
                    manager.setAddress(address);
                    created = userFacade.createManager(manager);
                    break;

                default:
                    sendJsonResponse(out, false, "Invalid user type");
                    return;
            }

            if (created) {
                sendJsonResponse(out, true, "Registration successful! Redirecting...", "/EPDA/login.xhtml");
            } else {
                sendJsonResponse(out, false, "Registration failed. Please try again.");
            }

        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            try (PrintWriter out = response.getWriter()) {
                sendJsonResponse(out, false, "Server error: " + e.getMessage());
            }
        }
    }

    /**
     * Check if email already exists in the database.
     * @param userType the type of user to check
     * @param email the email to check
     * @return true if email exists, false otherwise
     * @throws SQLException if database error occurs
     */
    private boolean isEmailExists(String userType, String email) throws SQLException {
        try {
            switch (userType.toUpperCase()) {
                case "CUSTOMER":
                    return userFacade.getCustomerByEmail(email) != null;
                case "COUNTERSTAFF":
                    return userFacade.getCounterStaffByEmail(email) != null;
                case "TECHNICIAN":
                    return userFacade.getTechnicianByEmail(email) != null;
                case "MANAGER":
                    return userFacade.getManagerByEmail(email) != null;
                default:
                    return false;
            }
        } catch (SQLException e) {
            throw new SQLException("Error checking email existence: " + e.getMessage());
        }
    }

    /**
     * Safely trim a parameter value (null-safe).
     * @param param the parameter value
     * @return trimmed value or null
     */
    private String trimParam(String param) {
        return param == null ? null : param.trim();
    }

    /**
     * Send JSON response without redirect URL.
     */
    private void sendJsonResponse(PrintWriter out, boolean success, String message) {
        out.print("{\"success\":" + success + ",\"message\":\"" + escapeJson(message) + "\"}");
        out.flush();
    }

    /**
     * Send JSON response with redirect URL.
     */
    private void sendJsonResponse(PrintWriter out, boolean success, String message, String redirectUrl) {
        out.print("{\"success\":" + success + ",\"message\":\"" + escapeJson(message) 
                + "\",\"redirectUrl\":\"" + escapeJson(redirectUrl) + "\"}");
        out.flush();
    }

    /**
     * Escape special characters for JSON.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
