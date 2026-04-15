package bean;

import entity.*;
import facade.*;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.pie.PieChartOptions;
import util.ValidationUtil;

/**
 * Manager Dashboard Bean.
 * Requirements: Register/delete/search/update all staff (3 types + managers),
 * set prices, view all feedback and comments, 5 reports.
 */
@Named("managerBean")
@ViewScoped
public class ManagerBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private UserFacade userFacade;
    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private PaymentFacade paymentFacade;
    @EJB
    private FeedbackFacade feedbackFacade;
    @EJB
    private AppointmentCommentFacade commentFacade;

    // Sidebar navigation
    private String currentSection = "overview";

    // Dashboard statistics
    private int totalServices;
    private int totalTechnicians;
    private int totalCustomers;
    private int totalCounterStaff;
    private int pendingAppointments;

    // Data lists
    private List<Service> services;
    private List<Technician> technicians;
    private List<CounterStaff> counterStaffList;
    private List<Customer> customers;
    private List<Manager> managers;
    private List<Appointment> appointments;
    private List<Feedback> allFeedback;
    private List<AppointmentComment> allComments;

    // Staff registration form
    private String regName, regEmail, regPassword, regGender, regPhone, regIc, regAddress;
    private String regSpecialty;
    private String regUserType = "Technician";

    // Service form
    private Service editingService;
    private String newServiceName, newServiceType;
    private Double newServicePrice;

    // Search
    private String searchKeyword;

    // Editing selected staff
    private Technician editingTechnician;
    private CounterStaff editingCounterStaff;
    private Manager editingManager;
    private Customer editingCustomer;

    // Reports
    private double dailyRevenue;
    private String revenuePeriod = "daily";
    private Map<String, Integer> technicianWorkload;
    private Map<String, Integer> servicePopularity;
    private List<Map<String, Object>> customerFeedback;
    private Map<String, Object> statusAnalytics;

    // Chart models
    private BarChartModel workloadChartModel;
    private PieChartModel statusChartModel;
    private BarChartModel popularityChartModel;

    public void init() {
        if (services == null || technicians == null || counterStaffList == null ||
            customers == null || managers == null || appointments == null ||
            allFeedback == null || allComments == null) {
            loadDashboardData();
        }
    }

    public void loadDashboardData() {
        try {
            services = serviceFacade.getAllServices();
            totalServices = services != null ? services.size() : 0;

            technicians = userFacade.getAllTechnicians();
            totalTechnicians = technicians != null ? technicians.size() : 0;

            customers = userFacade.getAllCustomers();
            totalCustomers = customers != null ? customers.size() : 0;

            counterStaffList = userFacade.getAllCounterStaff();
            totalCounterStaff = counterStaffList != null ? counterStaffList.size() : 0;

            managers = userFacade.getAllManagers();

            appointments = appointmentFacade.getAllAppointments();
            pendingAppointments = 0;
            if (appointments != null) {
                for (Appointment a : appointments) {
                    if ("Pending".equalsIgnoreCase(a.getStatus())) pendingAppointments++;
                }
            }

            allFeedback = feedbackFacade.getAllFeedback();
            allComments = commentFacade.getAllComments();
        } catch (Exception e) {
            addError("Error loading dashboard: " + e.getMessage());
        }
    }

    // ========== STAFF REGISTRATION ==========

    public void registerStaff() {
        try {
            boolean valid = true;

            if (!isValidStaffUserType(regUserType)) {
                addError("Please select a valid staff type.");
                valid = false;
            }
            if (!validateStaffFields(regName, regEmail, regPassword, regGender, regPhone, regIc, regAddress, null, null, true)) {
                valid = false;
            }
            if ("Technician".equals(regUserType) && !validateOptionalSpecialty(regSpecialty)) {
                valid = false;
            }

            if (!valid) {
                return;
            }

            boolean success = false;
            switch (regUserType) {
                case "Technician":
                    Technician t = new Technician();
                    t.setName(sanitizeText(regName));
                    t.setEmail(ValidationUtil.normalizeEmail(regEmail));
                    t.setPassword(regPassword);
                    t.setGender(sanitizeText(regGender));
                    t.setPhone(ValidationUtil.normalizePhone(regPhone));
                    t.setIc(sanitizeText(regIc));
                    t.setAddress(sanitizeText(regAddress));
                    t.setSpecialty(resolveSpecialty(regSpecialty));
                    t.setAvailable(true);
                    success = userFacade.createTechnician(t);
                    break;
                case "CounterStaff":
                    CounterStaff cs = new CounterStaff();
                    cs.setName(sanitizeText(regName));
                    cs.setEmail(ValidationUtil.normalizeEmail(regEmail));
                    cs.setPassword(regPassword);
                    cs.setGender(sanitizeText(regGender));
                    cs.setPhone(ValidationUtil.normalizePhone(regPhone));
                    cs.setIc(sanitizeText(regIc));
                    cs.setAddress(sanitizeText(regAddress));
                    success = userFacade.createCounterStaff(cs);
                    break;
                case "Manager":
                    Manager m = new Manager();
                    m.setName(sanitizeText(regName));
                    m.setEmail(ValidationUtil.normalizeEmail(regEmail));
                    m.setPassword(regPassword);
                    m.setGender(sanitizeText(regGender));
                    m.setPhone(ValidationUtil.normalizePhone(regPhone));
                    m.setIc(sanitizeText(regIc));
                    m.setAddress(sanitizeText(regAddress));
                    success = userFacade.createManager(m);
                    break;
                default:
                    addError("Please select a valid staff type.");
                    return;
            }

            if (success) {
                addInfo(regUserType + " registered successfully.");
                clearRegistrationForm();
                loadDashboardData();
            } else {
                addError("Failed to register " + regUserType + ". A conflicting email or IC may already exist.");
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    private void clearRegistrationForm() {
        regName = regEmail = regPassword = regGender = regPhone = regIc = regAddress = regSpecialty = null;
        regUserType = "Technician";
    }

    // ========== STAFF DELETE ==========

    public void deleteTechnician(String id) {
        try { userFacade.deleteTechnician(id); loadDashboardData(); addInfo("Technician deleted."); }
        catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void deleteCounterStaff(String id) {
        try { userFacade.deleteCounterStaff(id); loadDashboardData(); addInfo("Counter staff deleted."); }
        catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void deleteManager(String id) {
        try { userFacade.deleteManager(id); loadDashboardData(); addInfo("Manager deleted."); }
        catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void deleteCustomer(String id) {
        try { userFacade.deleteCustomer(id); loadDashboardData(); addInfo("Customer deleted."); }
        catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    // ========== SEARCH ==========

    public void searchStaff() {
        try {
            String keyword = searchKeyword != null ? searchKeyword.trim() : null;
            if (keyword != null && !keyword.isBlank()) {
                switch (currentSection) {
                    case "technicians":
                        technicians = userFacade.searchTechnicians(keyword);
                        break;
                    case "counter-staff":
                        counterStaffList = userFacade.searchCounterStaff(keyword);
                        break;
                    case "managers":
                        managers = userFacade.searchManagers(keyword);
                        break;
                    case "customers":
                        customers = userFacade.searchCustomers(keyword);
                        break;
                    default:
                        loadDashboardData();
                        break;
                }
            } else {
                loadDashboardData();
            }
        } catch (Exception e) {
            addError("Search error: " + e.getMessage());
        }
    }

    public void clearSearch() {
        searchKeyword = null;
        loadDashboardData();
    }

    // ========== EDIT / UPDATE STAFF ==========

    public void loadEditTechnician(String id) {
        try { editingTechnician = userFacade.getTechnicianByID(id); }
        catch (Exception e) { addError("Error loading technician: " + e.getMessage()); }
    }

    public void saveEditTechnician() {
        try {
            if (editingTechnician == null) {
                addError("Technician not found.");
                return;
            }

            Technician existing = userFacade.getTechnicianByID(editingTechnician.getId());
            if (existing == null) {
                editingTechnician = null;
                loadDashboardData();
                addError("Technician no longer exists.");
                return;
            }

            boolean valid = validateStaffFields(
                    editingTechnician.getName(),
                    editingTechnician.getEmail(),
                    null,
                    editingTechnician.getGender(),
                    editingTechnician.getPhone(),
                    editingTechnician.getIc(),
                    editingTechnician.getAddress(),
                    existing.getEmail(),
                    existing.getIc(),
                    false);

            if (!validateOptionalSpecialty(editingTechnician.getSpecialty())) {
                valid = false;
            }

            if (!valid) {
                return;
            }

            editingTechnician.setName(sanitizeText(editingTechnician.getName()));
            editingTechnician.setEmail(ValidationUtil.normalizeEmail(editingTechnician.getEmail()));
            editingTechnician.setGender(sanitizeText(editingTechnician.getGender()));
            editingTechnician.setPhone(ValidationUtil.normalizePhone(editingTechnician.getPhone()));
            editingTechnician.setIc(sanitizeText(editingTechnician.getIc()));
            editingTechnician.setAddress(sanitizeText(editingTechnician.getAddress()));
            editingTechnician.setSpecialty(resolveSpecialty(editingTechnician.getSpecialty()));

            userFacade.updateTechnician(editingTechnician);
            editingTechnician = null;
            loadDashboardData();
            addInfo("Technician updated successfully.");
        } catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void loadEditCounterStaff(String id) {
        try { editingCounterStaff = userFacade.getCounterStaffByID(id); }
        catch (Exception e) { addError("Error loading counter staff: " + e.getMessage()); }
    }

    public void saveEditCounterStaff() {
        try {
            if (editingCounterStaff == null) {
                addError("Counter staff not found.");
                return;
            }

            CounterStaff existing = userFacade.getCounterStaffByID(editingCounterStaff.getId());
            if (existing == null) {
                editingCounterStaff = null;
                loadDashboardData();
                addError("Counter staff no longer exists.");
                return;
            }

            if (!validateStaffFields(
                    editingCounterStaff.getName(),
                    editingCounterStaff.getEmail(),
                    null,
                    editingCounterStaff.getGender(),
                    editingCounterStaff.getPhone(),
                    editingCounterStaff.getIc(),
                    editingCounterStaff.getAddress(),
                    existing.getEmail(),
                    existing.getIc(),
                    false)) {
                return;
            }

            editingCounterStaff.setName(sanitizeText(editingCounterStaff.getName()));
            editingCounterStaff.setEmail(ValidationUtil.normalizeEmail(editingCounterStaff.getEmail()));
            editingCounterStaff.setGender(sanitizeText(editingCounterStaff.getGender()));
            editingCounterStaff.setPhone(ValidationUtil.normalizePhone(editingCounterStaff.getPhone()));
            editingCounterStaff.setIc(sanitizeText(editingCounterStaff.getIc()));
            editingCounterStaff.setAddress(sanitizeText(editingCounterStaff.getAddress()));

            userFacade.updateCounterStaff(editingCounterStaff);
            editingCounterStaff = null;
            loadDashboardData();
            addInfo("Counter staff updated successfully.");
        } catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void loadEditManager(String id) {
        try { editingManager = userFacade.getManagerByID(id); }
        catch (Exception e) { addError("Error loading manager: " + e.getMessage()); }
    }

    public void saveEditManager() {
        try {
            if (editingManager == null) {
                addError("Manager not found.");
                return;
            }

            Manager existing = userFacade.getManagerByID(editingManager.getId());
            if (existing == null) {
                editingManager = null;
                loadDashboardData();
                addError("Manager no longer exists.");
                return;
            }

            if (!validateStaffFields(
                    editingManager.getName(),
                    editingManager.getEmail(),
                    null,
                    editingManager.getGender(),
                    editingManager.getPhone(),
                    editingManager.getIc(),
                    editingManager.getAddress(),
                    existing.getEmail(),
                    existing.getIc(),
                    false)) {
                return;
            }

            editingManager.setName(sanitizeText(editingManager.getName()));
            editingManager.setEmail(ValidationUtil.normalizeEmail(editingManager.getEmail()));
            editingManager.setGender(sanitizeText(editingManager.getGender()));
            editingManager.setPhone(ValidationUtil.normalizePhone(editingManager.getPhone()));
            editingManager.setIc(sanitizeText(editingManager.getIc()));
            editingManager.setAddress(sanitizeText(editingManager.getAddress()));

            userFacade.updateManager(editingManager);
            editingManager = null;
            loadDashboardData();
            addInfo("Manager updated successfully.");
        } catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    public void loadEditCustomer(String id) {
        try { editingCustomer = userFacade.getCustomerByID(id); }
        catch (Exception e) { addError("Error loading customer: " + e.getMessage()); }
    }

    public void saveEditCustomer() {
        try {
            if (editingCustomer == null) {
                addError("Customer not found.");
                return;
            }

            Customer existing = userFacade.getCustomerByID(editingCustomer.getId());
            if (existing == null) {
                editingCustomer = null;
                loadDashboardData();
                addError("Customer no longer exists.");
                return;
            }

            if (!validateStaffFields(
                    editingCustomer.getName(),
                    editingCustomer.getEmail(),
                    null,
                    editingCustomer.getGender(),
                    editingCustomer.getPhone(),
                    editingCustomer.getIc(),
                    editingCustomer.getAddress(),
                    existing.getEmail(),
                    existing.getIc(),
                    false)) {
                return;
            }

            editingCustomer.setName(sanitizeText(editingCustomer.getName()));
            editingCustomer.setEmail(ValidationUtil.normalizeEmail(editingCustomer.getEmail()));
            editingCustomer.setGender(sanitizeText(editingCustomer.getGender()));
            editingCustomer.setPhone(ValidationUtil.normalizePhone(editingCustomer.getPhone()));
            editingCustomer.setIc(sanitizeText(editingCustomer.getIc()));
            editingCustomer.setAddress(sanitizeText(editingCustomer.getAddress()));

            userFacade.updateCustomer(editingCustomer);
            editingCustomer = null;
            loadDashboardData();
            addInfo("Customer updated successfully.");
        } catch (Exception e) { addError("Error: " + e.getMessage()); }
    }

    // ========== SERVICE MANAGEMENT (SET PRICES) ==========

    public void createService() {
        try {
            if (!validateServiceFields(newServiceName, newServiceType, newServicePrice, null)) {
                return;
            }
            String serviceName = sanitizeText(newServiceName);
            String serviceType = sanitizeText(newServiceType);
            Service s = new Service(null, serviceName, serviceType, newServicePrice);
            serviceFacade.createService(s);
            newServiceName = null;
            newServiceType = null;
            newServicePrice = null;
            loadDashboardData();
            addInfo("Service created.");
        } catch (Exception e) {
            addError("Error creating service: " + e.getMessage());
        }
    }

    public void loadEditService(String serviceId) {
        try {
            Service service = serviceFacade.getServiceByID(serviceId);
            if (service == null) {
                addError("Service not found.");
                return;
            }

            editingService = new Service();
            editingService.setId(service.getId());
            editingService.setServiceName(service.getServiceName());
            editingService.setType(service.getType());
            editingService.setBasePrice(service.getBasePrice());
        } catch (Exception e) {
            addError("Error loading service: " + e.getMessage());
        }
    }

    public void updateService(Service service) {
        try {
            if (service == null) {
                addError("Service not found.");
                return;
            }
            if (serviceFacade.getServiceByID(service.getId()) == null) {
                editingService = null;
                addError("Service no longer exists.");
                loadDashboardData();
                return;
            }
            if (!validateServiceFields(service.getServiceName(), service.getType(), service.getBasePrice(), service.getId())) {
                return;
            }

            service.setServiceName(sanitizeText(service.getServiceName()));
            service.setType(sanitizeText(service.getType()));
            serviceFacade.updateService(service);
            editingService = null;
            loadDashboardData();
            addInfo("Service updated.");
        } catch (Exception e) {
            addError("Error updating service: " + e.getMessage());
        }
    }

    public void saveEditService() {
        updateService(editingService);
    }

    /**
     * Auto-fill canonical price when the Add-New-Service type dropdown changes.
     * Normal -> RM 500, Major -> RM 600, cleared selection -> empty.
     * Price input remains editable after auto-fill.
     */
    public void onNewServiceTypeChange() {
        if (Service.TYPE_NORMAL.equalsIgnoreCase(newServiceType)) {
            newServicePrice = 500.0;
        } else if (Service.TYPE_MAJOR.equalsIgnoreCase(newServiceType)) {
            newServicePrice = 600.0;
        } else {
            newServicePrice = null;
        }
    }

    /**
     * Auto-fill canonical price when the Edit-Service type dropdown changes.
     * Mirrors {@link #onNewServiceTypeChange()} but targets the editing entity.
     */
    public void onEditServiceTypeChange() {
        if (editingService == null) return;
        String type = editingService.getType();
        if (Service.TYPE_NORMAL.equalsIgnoreCase(type)) {
            editingService.setBasePrice(500.0);
        } else if (Service.TYPE_MAJOR.equalsIgnoreCase(type)) {
            editingService.setBasePrice(600.0);
        }
    }

    public void cancelEditService() {
        editingService = null;
    }

    public void deleteService(String serviceId) {
        try {
            serviceFacade.deleteService(serviceId);
            if (editingService != null && serviceId.equals(editingService.getId())) {
                editingService = null;
            }
            loadDashboardData();
            addInfo("Service deleted.");
        } catch (Exception e) {
            addError("Error deleting service: " + e.getMessage());
        }
    }

    // ========== REPORTS ==========

    public void loadReports() {
        try {
            dailyRevenue = paymentFacade.getRevenueForPeriod(revenuePeriod);
            technicianWorkload = appointmentFacade.getTechnicianWorkload();
            servicePopularity = appointmentFacade.getServicePopularity();
            customerFeedback = commentFacade.getCustomerFeedbackSummary();
            statusAnalytics = appointmentFacade.getStatusAnalytics();
            buildWorkloadChart();
            buildStatusChart();
            buildPopularityChart();
        } catch (Exception e) {
            addError("Error loading reports: " + e.getMessage());
        }
    }

    // ========== CHART BUILDERS ==========

    private void buildWorkloadChart() {
        workloadChartModel = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Active Tasks");
        dataSet.setBackgroundColor("rgba(67, 97, 238, 0.7)");
        dataSet.setBorderColor("rgb(67, 97, 238)");
        dataSet.setBorderWidth(1);

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (technicianWorkload != null) {
            for (Map.Entry<String, Integer> entry : technicianWorkload.entrySet()) {
                labels.add(entry.getKey());
                values.add(entry.getValue());
            }
        }

        dataSet.setData(values);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        workloadChartModel.setData(data);

        BarChartOptions options = new BarChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Technician Workload");
        options.setTitle(title);
        workloadChartModel.setOptions(options);
    }

    private void buildStatusChart() {
        statusChartModel = new PieChartModel();
        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        if (statusAnalytics != null) {
            Object pending = statusAnalytics.get("pending");
            Object inProgress = statusAnalytics.get("inProgress");
            Object completed = statusAnalytics.get("completed");
            Object cancelled = statusAnalytics.get("cancelled");

            if (pending != null) { labels.add("Pending"); values.add((Number) pending); colors.add("rgb(245, 158, 11)"); }
            if (inProgress != null) { labels.add("In Progress"); values.add((Number) inProgress); colors.add("rgb(59, 130, 246)"); }
            if (completed != null) { labels.add("Completed"); values.add((Number) completed); colors.add("rgb(16, 185, 129)"); }
            if (cancelled != null) { labels.add("Cancelled"); values.add((Number) cancelled); colors.add("rgb(239, 68, 68)"); }
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(colors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        statusChartModel.setData(data);

        PieChartOptions options = new PieChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Appointment Status Distribution");
        options.setTitle(title);
        statusChartModel.setOptions(options);
    }

    private void buildPopularityChart() {
        popularityChartModel = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Bookings");
        dataSet.setBackgroundColor("rgba(16, 185, 129, 0.7)");
        dataSet.setBorderColor("rgb(16, 185, 129)");
        dataSet.setBorderWidth(1);

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (servicePopularity != null) {
            for (Map.Entry<String, Integer> entry : servicePopularity.entrySet()) {
                labels.add(entry.getKey());
                values.add(entry.getValue());
            }
        }

        dataSet.setData(values);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        popularityChartModel.setData(data);

        BarChartOptions options = new BarChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Service Popularity");
        options.setTitle(title);
        popularityChartModel.setOptions(options);
    }

    // ========== SIDEBAR NAVIGATION ==========

    public void navigateTo(String section) {
        this.currentSection = section;
        if ("register-staff".equals(section)) {
            clearRegistrationForm();
        }
        if ("reports".equals(section)) {
            loadReports();
        }
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

    private boolean validateStaffFields(String name, String email, String password, String gender,
                                        String phone, String ic, String address,
                                        String existingEmail, String existingIc,
                                        boolean requirePassword) {
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

        if (requirePassword && !ValidationUtil.isValidPassword(password)) {
            addError(ValidationUtil.getErrorMessage("password"));
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

    private boolean validateServiceFields(String serviceName, String serviceType, Double servicePrice,
                                          String existingServiceId) {
        boolean valid = true;

        if (!ValidationUtil.isValidServiceName(serviceName)) {
            addError(ValidationUtil.getErrorMessage("serviceName"));
            valid = false;
        } else if (serviceFacade.isDuplicateServiceName(serviceName, existingServiceId)) {
            addError("Service name already exists.");
            valid = false;
        }

        if (!ValidationUtil.isValidServiceType(serviceType)) {
            addError(ValidationUtil.getErrorMessage("serviceType"));
            valid = false;
        }

        if (!ValidationUtil.isValidServicePrice(servicePrice)) {
            addError(ValidationUtil.getErrorMessage("servicePrice"));
            valid = false;
        }

        return valid;
    }

    private boolean isValidStaffUserType(String staffUserType) {
        return "Technician".equals(staffUserType)
                || "CounterStaff".equals(staffUserType)
                || "Manager".equals(staffUserType);
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

    public int getTotalServices() { if (services == null) loadDashboardData(); return totalServices; }
    public int getTotalTechnicians() { if (technicians == null) loadDashboardData(); return totalTechnicians; }
    public int getTotalCustomers() { if (customers == null) loadDashboardData(); return totalCustomers; }
    public int getTotalCounterStaff() { if (counterStaffList == null) loadDashboardData(); return totalCounterStaff; }
    public int getPendingAppointments() { if (appointments == null) loadDashboardData(); return pendingAppointments; }

    public List<Service> getServices() { if (services == null) loadDashboardData(); return services; }
    public List<Technician> getTechnicians() { if (technicians == null) loadDashboardData(); return technicians; }
    public List<CounterStaff> getCounterStaffList() { if (counterStaffList == null) loadDashboardData(); return counterStaffList; }
    public List<Customer> getCustomers() { if (customers == null) loadDashboardData(); return customers; }
    public List<Manager> getManagers() { if (managers == null) loadDashboardData(); return managers; }
    public List<Appointment> getAppointments() { if (appointments == null) loadDashboardData(); return appointments; }
    public List<Feedback> getAllFeedback() { if (allFeedback == null) loadDashboardData(); return allFeedback; }
    public List<AppointmentComment> getAllComments() { if (allComments == null) loadDashboardData(); return allComments; }

    public String getRegName() { return regName; }
    public void setRegName(String regName) { this.regName = regName; }
    public String getRegEmail() { return regEmail; }
    public void setRegEmail(String regEmail) { this.regEmail = regEmail; }
    public String getRegPassword() { return regPassword; }
    public void setRegPassword(String regPassword) { this.regPassword = regPassword; }
    public String getRegGender() { return regGender; }
    public void setRegGender(String regGender) { this.regGender = regGender; }
    public String getRegPhone() { return regPhone; }
    public void setRegPhone(String regPhone) { this.regPhone = regPhone; }
    public String getRegIc() { return regIc; }
    public void setRegIc(String regIc) { this.regIc = regIc; }
    public String getRegAddress() { return regAddress; }
    public void setRegAddress(String regAddress) { this.regAddress = regAddress; }
    public String getRegSpecialty() { return regSpecialty; }
    public void setRegSpecialty(String regSpecialty) { this.regSpecialty = regSpecialty; }
    public String getRegUserType() { return regUserType; }
    public void setRegUserType(String regUserType) { this.regUserType = regUserType; }

    public String getNewServiceName() { return newServiceName; }
    public void setNewServiceName(String newServiceName) { this.newServiceName = newServiceName; }
    public String getNewServiceType() { return newServiceType; }
    public void setNewServiceType(String newServiceType) { this.newServiceType = newServiceType; }
    public Double getNewServicePrice() { return newServicePrice; }
    public void setNewServicePrice(Double newServicePrice) { this.newServicePrice = newServicePrice; }

    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }

    public double getDailyRevenue() { return dailyRevenue; }
    public String getRevenuePeriod() { return revenuePeriod; }
    public void setRevenuePeriod(String revenuePeriod) { this.revenuePeriod = revenuePeriod; }

    public String getRevenuePeriodDescription() {
        LocalDate today = LocalDate.now();
        switch (revenuePeriod != null ? revenuePeriod : "daily") {
            case "monthly":
                return "Completed payments recorded in " +
                        today.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            case "yearly":
                return "Completed payments recorded in " + today.getYear();
            case "daily":
            default:
                return "Completed payments recorded on " +
                        today.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        }
    }

    public Map<String, Integer> getTechnicianWorkload() { return technicianWorkload; }
    public Map<String, Integer> getServicePopularity() { return servicePopularity; }
    public List<Map<String, Object>> getCustomerFeedback() { return customerFeedback; }
    public Map<String, Object> getStatusAnalytics() { return statusAnalytics; }

    public BarChartModel getWorkloadChartModel() { return workloadChartModel; }
    public PieChartModel getStatusChartModel() { return statusChartModel; }
    public BarChartModel getPopularityChartModel() { return popularityChartModel; }

    public Technician getEditingTechnician() { return editingTechnician; }
    public void setEditingTechnician(Technician editingTechnician) { this.editingTechnician = editingTechnician; }

    public CounterStaff getEditingCounterStaff() { return editingCounterStaff; }
    public void setEditingCounterStaff(CounterStaff editingCounterStaff) { this.editingCounterStaff = editingCounterStaff; }

    public Manager getEditingManager() { return editingManager; }
    public void setEditingManager(Manager editingManager) { this.editingManager = editingManager; }

    public Customer getEditingCustomer() { return editingCustomer; }
    public void setEditingCustomer(Customer editingCustomer) { this.editingCustomer = editingCustomer; }

    public Service getEditingService() { return editingService; }
    public void setEditingService(Service editingService) { this.editingService = editingService; }

    public String getCurrentSection() { return currentSection; }
    public void setCurrentSection(String currentSection) { this.currentSection = currentSection; }
}
