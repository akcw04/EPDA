package servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthFilter - Jakarta EE WebFilter for Role-Based Access Control.
 * Intercepts all requests and enforces authentication and authorization
 * based on the user's role stored in the HTTP session.
 *
 * Roles: Manager, CounterStaff, Technician, Customer
 * Each role can only access its own section (/manager/*, /counterstaff/*, etc.)
 * Unauthenticated users are redirected to /login.xhtml.
 * Authenticated users accessing the wrong section are redirected to their own dashboard.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;
        HttpSession session = httpReq.getSession(false);

        String requestURI = httpReq.getRequestURI();
        String contextPath = httpReq.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // --- Allow public resources (no authentication required) ---

        // Allow login page
        if (path.equals("/login.xhtml") || path.equals("/login.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        // Allow customer registration page
        if (path.equals("/register-customer.jsp") || path.equals("/register-customer.xhtml")) {
            chain.doFilter(request, response);
            return;
        }

        // Allow JSF resource requests (CSS, JS, images)
        if (path.startsWith("/jakarta.faces.resource/") || path.contains("javax.faces.resource")) {
            chain.doFilter(request, response);
            return;
        }

        // Allow static resources (if any)
        if (path.startsWith("/resources/") || path.startsWith("/css/") ||
            path.startsWith("/js/") || path.startsWith("/images/")) {
            chain.doFilter(request, response);
            return;
        }

        // Allow servlet paths (e.g., registration servlet)
        if (path.equals("/RegisterCustomerServlet")) {
            chain.doFilter(request, response);
            return;
        }

        // --- Check authentication ---

        Boolean loggedIn = null;
        String userRole = null;

        if (session != null) {
            loggedIn = (Boolean) session.getAttribute("loggedIn");
            userRole = (String) session.getAttribute("userRole");
        }

        boolean isAuthenticated = (loggedIn != null && loggedIn) && (userRole != null);

        // --- Protect role-specific sections ---

        boolean isProtectedPath = path.startsWith("/manager/") ||
                                  path.startsWith("/counterstaff/") ||
                                  path.startsWith("/technician/") ||
                                  path.startsWith("/customer/");

        if (isProtectedPath) {
            if (!isAuthenticated) {
                // Not logged in: redirect to login page
                httpRes.sendRedirect(contextPath + "/login.xhtml");
                return;
            }

            // Logged in: check role authorization
            String allowedRole = getAllowedRole(path);
            if (allowedRole != null && !allowedRole.equals(userRole)) {
                // Wrong role: redirect to their own dashboard
                String dashboardPath = getDashboardPath(userRole);
                httpRes.sendRedirect(contextPath + dashboardPath);
                return;
            }
        }

        // All checks passed, continue with the request
        chain.doFilter(request, response);
    }

    /**
     * Determine which role is allowed for a given path.
     * @param path the request path (relative to context root)
     * @return the required role, or null if no specific role is required
     */
    private String getAllowedRole(String path) {
        if (path.startsWith("/manager/")) {
            return "Manager";
        } else if (path.startsWith("/counterstaff/")) {
            return "CounterStaff";
        } else if (path.startsWith("/technician/")) {
            return "Technician";
        } else if (path.startsWith("/customer/")) {
            return "Customer";
        }
        return null;
    }

    /**
     * Get the dashboard path for a given user role.
     * @param role the user's role
     * @return the dashboard URL path for that role
     */
    private String getDashboardPath(String role) {
        switch (role) {
            case "Manager":
                return "/manager/dashboard.xhtml";
            case "CounterStaff":
                return "/counterstaff/dashboard.xhtml";
            case "Technician":
                return "/technician/dashboard.xhtml";
            case "Customer":
                return "/customer/dashboard.xhtml";
            default:
                return "/login.xhtml";
        }
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
