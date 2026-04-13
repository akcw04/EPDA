package bean;

import entity.*;
import facade.*;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import util.ValidationUtil;
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

/**
 * Manager Dashboard Bean. Requirements: Register/delete/search/update all staff
 * (3 types + managers), set prices, view all feedback and comments, 5 reports.
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
    @Inject
    private LoginBean loginBean;

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
    private double newServicePrice;

    // Search
    private String searchKeyword;

    // Editing selected staff
    private Technician editingTechnician;
    private CounterStaff editingCounterStaff;
    private Manager editingManager;
    private Customer editingCustomer;
    
    // Editing selected staff original values
    private String originalEditingTechnicianEmailValue;
    private String originalEditingTechnicianIcValue;
    private String originalEditingCounterStaffEmailValue;
    private String originalEditingCounterStaffIcValue;
    private String originalEditingManagerEmailValue;
    private String originalEditingManagerIcValue;
    private String originalEditingCustomerEmailValue;
    private String originalEditingCustomerIcValue;

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
        if (services == null || technicians == null || counterStaffList == null
                || customers == null || managers == null || appointments == null
                || allFeedback == null || allComments == null) {
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
                    if ("Pending".equalsIgnoreCase(a.getStatus())) {
                        pendingAppointments++;
                    }
                }
            }

            allFeedback = feedbackFacade.getAllFeedback();
            allComments = commentFacade.getAllComments();
        } catch (Exception e) {
            System.err.println("Error loading manager dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== STAFF REGISTRATION ==========
    public void registerStaff() {
        try {
            boolean valuesCorrect = false;

            // Name validation 
            if (!ValidationUtil.isValidName(regName)) {
                addError(ValidationUtil.getErrorMessage("name"));
                return;
            }

            // Email validation
            if (!ValidationUtil.isValidEmail(regEmail)) {
                addError(ValidationUtil.getErrorMessage("email"));
                return;
            } else {
                // Email duplication validation
                boolean duplicateEmailFound = userFacade.isDuplicateEmail(regEmail);
                if (duplicateEmailFound) {
                    addError("Email may have been registered.");
                    return;
                }
            }

            // Gender validation
            if (!ValidationUtil.isValidGender(regGender)) {
                addError(ValidationUtil.getErrorMessage("gender"));
                return;
            }

            // Phone validation
            if (!ValidationUtil.isValidPhone(regPhone)) {
                addError(ValidationUtil.getErrorMessage("phone"));
                return;
            }

            // IC validation
            if (!ValidationUtil.isValidIC(regIc)) {
                addError(ValidationUtil.getErrorMessage("ic"));
                return;
            } else {
                // IC duplication validation
                boolean duplicateIcFound = userFacade.isDuplicateIC(regIc);
                if (duplicateIcFound) {
                    addError("IC number may have been registered.");
                    return;
                }
            }

            // Address validation
            if (!ValidationUtil.isValidAddress(regAddress)) {
                addError(ValidationUtil.getErrorMessage("address"));
                return;
            }

            // Password validation
            if (!ValidationUtil.isValidPassword(regPassword)) {
                addError(ValidationUtil.getErrorMessage("password"));
                return;
            } else {
                valuesCorrect = true;
            }

            if (valuesCorrect) {
                boolean success = false;

                switch (regUserType) {
                    case "Technician":
                        Technician t = new Technician();
                        t.setName(regName);
                        t.setEmail(regEmail);
                        t.setPassword(regPassword);
                        t.setGender(regGender);
                        t.setPhone(regPhone);
                        t.setIc(regIc);
                        t.setAddress(regAddress);
                        t.setSpecialty(regSpecialty != null ? regSpecialty : "General");
                        t.setAvailable(true);
                        success = userFacade.createTechnician(t);
                        break;
                    case "CounterStaff":
                        CounterStaff cs = new CounterStaff();
                        cs.setName(regName);
                        cs.setEmail(regEmail);
                        cs.setPassword(regPassword);
                        cs.setGender(regGender);
                        cs.setPhone(regPhone);
                        cs.setIc(regIc);
                        cs.setAddress(regAddress);
                        success = userFacade.createCounterStaff(cs);
                        break;
                    case "Manager":
                        Manager m = new Manager();
                        m.setName(regName);
                        m.setEmail(regEmail);
                        m.setPassword(regPassword);
                        m.setGender(regGender);
                        m.setPhone(regPhone);
                        m.setIc(regIc);
                        m.setAddress(regAddress);
                        success = userFacade.createManager(m);
                        break;
                }
                if (success) {
                    addInfo(regUserType + " registered successfully.");
                    clearRegistrationForm();
                    loadDashboardData();
                } else {
                    addError("Failed to register " + regUserType);
                }
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
        try {
            userFacade.deleteTechnician(id);
            loadDashboardData();
            addInfo("Technician deleted.");
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    public void deleteCounterStaff(String id) {
        try {
            userFacade.deleteCounterStaff(id);
            loadDashboardData();
            addInfo("Counter staff deleted.");
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    public void deleteManager(String id) {
        try {
            userFacade.deleteManager(id);
            loadDashboardData();
            addInfo("Manager deleted.");
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    public void deleteCustomer(String id) {
        try {
            userFacade.deleteCustomer(id);
            loadDashboardData();
            addInfo("Customer deleted.");
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
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
        try {
            editingTechnician = userFacade.getTechnicianByID(id);
            setOriginalEditingTechnicianEmailValue(editingTechnician.getEmail());
            setOriginalEditingTechnicianIcValue(editingTechnician.getIc());
        } catch (Exception e) {
            addError("Error loading technician: " + e.getMessage());
        }
    }

    public void saveEditTechnician() {
        try {
            if (editingTechnician != null) {
                boolean valuesCorrect = false;
                
                // Name validation 
                if (!ValidationUtil.isValidName(editingTechnician.getName())) {
                    addError(ValidationUtil.getErrorMessage("name"));
                    return;
                }
                
                // Email validation
                if (!ValidationUtil.isValidEmail(editingTechnician.getEmail())) {
                    addError(ValidationUtil.getErrorMessage("email"));
                    return;
                } else {
                    // Email duplication validation
                    boolean duplicateEmailFound = userFacade.isDuplicateEmail(editingTechnician.getEmail());
                    if (duplicateEmailFound && (!editingTechnician.getEmail().equals(getOriginalEditingTechnicianEmailValue()))) {
                        addError("Email may have been registered.");
                        return;
                    }
                }
                
                // Gender validation
                if (!ValidationUtil.isValidGender(editingTechnician.getGender())) {
                    addError(ValidationUtil.getErrorMessage("gender"));
                    return;
                }

                // Phone validation
                if (!ValidationUtil.isValidPhone(editingTechnician.getPhone())) {
                    addError(ValidationUtil.getErrorMessage("phone"));
                    return;
                }

                // IC validation
                if (!ValidationUtil.isValidIC(editingTechnician.getIc())) {
                    addError(ValidationUtil.getErrorMessage("ic"));
                    return;
                } else {
                    // IC duplication validation
                    boolean duplicateIcFound = userFacade.isDuplicateIC(editingTechnician.getIc());
                    if (duplicateIcFound && (!editingTechnician.getIc().equals(getOriginalEditingTechnicianIcValue()))) {
                        addError("IC number may have been registered.");
                        return;
                    }
                }
                
                // Address validation
                if (!ValidationUtil.isValidAddress(editingTechnician.getAddress())) {
                    addError(ValidationUtil.getErrorMessage("address"));
                    return;
                } else {
                    valuesCorrect = true;
                }
                
                if (valuesCorrect) {
                    userFacade.updateTechnician(editingTechnician);
                    editingTechnician = null;
                    loadDashboardData();
                    addInfo("Technician updated successfully.");
                }
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    public void loadEditCounterStaff(String id) {
        try {
            editingCounterStaff = userFacade.getCounterStaffByID(id);
            setOriginalEditingCounterStaffEmailValue(editingCounterStaff.getEmail());
            setOriginalEditingCounterStaffIcValue(editingCounterStaff.getIc());
        } catch (Exception e) {
            addError("Error loading counter staff: " + e.getMessage());
        }
    }

    public void saveEditCounterStaff() {
        try {
            if (editingCounterStaff != null) {
                boolean valuesCorrect = false;
                
                // Name validation 
                if (!ValidationUtil.isValidName(editingCounterStaff.getName())) {
                    addError(ValidationUtil.getErrorMessage("name"));
                    return;
                }
                
                // Email validation
                if (!ValidationUtil.isValidEmail(editingCounterStaff.getEmail())) {
                    addError(ValidationUtil.getErrorMessage("email"));
                    return;
                } else {
                    // Email duplication validation
                    boolean duplicateEmailFound = userFacade.isDuplicateEmail(editingCounterStaff.getEmail());
                    if (duplicateEmailFound && (!editingCounterStaff.getEmail().equals(getOriginalEditingCounterStaffEmailValue()))) {
                        addError("Email may have been registered.");
                        return;
                    }
                }
                
                // Gender validation
                if (!ValidationUtil.isValidGender(editingCounterStaff.getGender())) {
                    addError(ValidationUtil.getErrorMessage("gender"));
                    return;
                }

                // Phone validation
                if (!ValidationUtil.isValidPhone(editingCounterStaff.getPhone())) {
                    addError(ValidationUtil.getErrorMessage("phone"));
                    return;
                }

                // IC validation
                if (!ValidationUtil.isValidIC(editingCounterStaff.getIc())) {
                    addError(ValidationUtil.getErrorMessage("ic"));
                    return;
                } else {
                    // IC duplication validation
                    boolean duplicateIcFound = userFacade.isDuplicateIC(editingCounterStaff.getIc());
                    if (duplicateIcFound && (!editingCounterStaff.getIc().equals(getOriginalEditingCounterStaffIcValue()))) {
                        addError("IC number may have been registered.");
                        return;
                    }
                }
                
                // Address validation
                if (!ValidationUtil.isValidAddress(editingCounterStaff.getAddress())) {
                    addError(ValidationUtil.getErrorMessage("address"));
                    return;
                } else {
                    valuesCorrect = true;
                }
                
                if (valuesCorrect) {
                    userFacade.updateCounterStaff(editingCounterStaff);
                    editingCounterStaff = null;
                    loadDashboardData();
                    addInfo("Counter staff updated successfully.");
                }
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    public void loadEditManager(String id) {
        try {
            editingManager = userFacade.getManagerByID(id);
            setOriginalEditingManagerEmailValue(editingManager.getEmail());
            setOriginalEditingManagerIcValue(editingManager.getIc());
        } catch (Exception e) {
            addError("Error loading manager: " + e.getMessage());
        }
    }

    public void saveEditManager() {
        try {
            if (editingManager != null) {
                boolean valuesCorrect = false;
                
                // Name validation 
                if (!ValidationUtil.isValidName(editingManager.getName())) {
                    addError(ValidationUtil.getErrorMessage("name"));
                    return;
                }
                
                // Email validation
                if (!ValidationUtil.isValidEmail(editingManager.getEmail())) {
                    addError(ValidationUtil.getErrorMessage("email"));
                    return;
                } else {
                    // Email duplication validation
                    boolean duplicateEmailFound = userFacade.isDuplicateEmail(editingManager.getEmail());
                    if (duplicateEmailFound && (!editingManager.getEmail().equals(getOriginalEditingManagerEmailValue()))) {
                        addError("Email may have been registered.");
                        return;
                    }
                }
                
                // Gender validation
                if (!ValidationUtil.isValidGender(editingManager.getGender())) {
                    addError(ValidationUtil.getErrorMessage("gender"));
                    return;
                }

                // Phone validation
                if (!ValidationUtil.isValidPhone(editingManager.getPhone())) {
                    addError(ValidationUtil.getErrorMessage("phone"));
                    return;
                }

                // IC validation
                if (!ValidationUtil.isValidIC(editingManager.getIc())) {
                    addError(ValidationUtil.getErrorMessage("ic"));
                    return;
                } else {
                    // IC duplication validation
                    boolean duplicateIcFound = userFacade.isDuplicateIC(editingManager.getIc());
                    if (duplicateIcFound && (!editingManager.getIc().equals(getOriginalEditingManagerIcValue()))) {
                        addError("IC number may have been registered.");
                        return;
                    }
                }
                
                // Address validation
                if (!ValidationUtil.isValidAddress(editingManager.getAddress())) {
                    addError(ValidationUtil.getErrorMessage("address"));
                    return;
                } else {
                    valuesCorrect = true;
                }
                
                if (valuesCorrect) {
                    userFacade.updateManager(editingManager);
                    editingManager = null;
                    loadDashboardData();
                    addInfo("Manager updated successfully.");
                }
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    public void loadEditCustomer(String id) {
        try {
            editingCustomer = userFacade.getCustomerByID(id);
            setOriginalEditingCustomerEmailValue(editingCustomer.getEmail());
            setOriginalEditingCustomerIcValue(editingCustomer.getIc());
        } catch (Exception e) {
            addError("Error loading customer: " + e.getMessage());
        }
    }

    public void saveEditCustomer() {
        try {
            if (editingCustomer != null) {
                boolean valuesCorrect = false;
                
                // Name validation 
                if (!ValidationUtil.isValidName(editingCustomer.getName())) {
                    addError(ValidationUtil.getErrorMessage("name"));
                    return;
                }
                
                // Email validation
                if (!ValidationUtil.isValidEmail(editingCustomer.getEmail())) {
                    addError(ValidationUtil.getErrorMessage("email"));
                    return;
                } else {
                    // Email duplication validation
                    boolean duplicateEmailFound = userFacade.isDuplicateEmail(editingCustomer.getEmail());
                    if (duplicateEmailFound && (!editingCustomer.getEmail().equals(getOriginalEditingCustomerEmailValue()))) {
                        addError("Email may have been registered.");
                        return;
                    }
                }
                
                // Gender validation
                if (!ValidationUtil.isValidGender(editingCustomer.getGender())) {
                    addError(ValidationUtil.getErrorMessage("gender"));
                    return;
                }

                // Phone validation
                if (!ValidationUtil.isValidPhone(editingCustomer.getPhone())) {
                    addError(ValidationUtil.getErrorMessage("phone"));
                    return;
                }

                // IC validation
                if (!ValidationUtil.isValidIC(editingCustomer.getIc())) {
                    addError(ValidationUtil.getErrorMessage("ic"));
                    return;
                } else {
                    // IC duplication validation
                    boolean duplicateIcFound = userFacade.isDuplicateIC(editingCustomer.getIc());
                    if (duplicateIcFound && (!editingCustomer.getIc().equals(getOriginalEditingCustomerIcValue()))) {
                        addError("IC number may have been registered.");
                        return;
                    }
                }
                
                // Address validation
                if (!ValidationUtil.isValidAddress(editingCustomer.getAddress())) {
                    addError(ValidationUtil.getErrorMessage("address"));
                    return;
                } else {
                    valuesCorrect = true;
                }
                
                if (valuesCorrect) {
                    userFacade.updateCustomer(editingCustomer);
                    editingCustomer = null;
                    loadDashboardData();
                    addInfo("Customer updated successfully.");
                }
            }
        } catch (Exception e) {
            addError("Error: " + e.getMessage());
        }
    }

    // ========== SERVICE MANAGEMENT (SET PRICES) ==========
    public void createService() {
        try {
            boolean valuesCorrect = false;

            if (!ValidationUtil.isValidName(newServiceName)) {
                addError(ValidationUtil.getErrorMessage("serviceName"));
                return;
            }

            if (ValidationUtil.isInputEmpty(newServiceType)) {
                addError(ValidationUtil.getErrorMessage("serviceType"));
                return;
            }

            if (!ValidationUtil.isValidServicePrice(newServicePrice)) {
                addError(ValidationUtil.getErrorMessage("servicePrice"));
                return;
            } else {
                valuesCorrect = true;
            }

            if (valuesCorrect) {
                Service s = new Service(null, newServiceName, newServiceType, newServicePrice);
                serviceFacade.createService(s);
                newServiceName = null;
                newServiceType = null;
                newServicePrice = 0;
                loadDashboardData();
                addInfo("Service created.");
            }
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
            if (service.getServiceName() == null || service.getServiceName().trim().isBlank()
                    || service.getType() == null || service.getType().trim().isBlank()
                    || service.getBasePrice() <= 0) {
                addError("Service name, type, and price are required.");
                return;
            }

            service.setServiceName(service.getServiceName().trim());
            service.setType(service.getType().trim());
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

            if (pending != null) {
                labels.add("Pending");
                values.add((Number) pending);
                colors.add("rgb(245, 158, 11)");
            }
            if (inProgress != null) {
                labels.add("In Progress");
                values.add((Number) inProgress);
                colors.add("rgb(59, 130, 246)");
            }
            if (completed != null) {
                labels.add("Completed");
                values.add((Number) completed);
                colors.add("rgb(16, 185, 129)");
            }
            if (cancelled != null) {
                labels.add("Cancelled");
                values.add((Number) cancelled);
                colors.add("rgb(239, 68, 68)");
            }
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

    // ========== GETTERS & SETTERS ==========
    public int getTotalServices() {
        if (services == null) {
            loadDashboardData();
        }
        return totalServices;
    }

    public int getTotalTechnicians() {
        if (technicians == null) {
            loadDashboardData();
        }
        return totalTechnicians;
    }

    public int getTotalCustomers() {
        if (customers == null) {
            loadDashboardData();
        }
        return totalCustomers;
    }

    public int getTotalCounterStaff() {
        if (counterStaffList == null) {
            loadDashboardData();
        }
        return totalCounterStaff;
    }

    public int getPendingAppointments() {
        if (appointments == null) {
            loadDashboardData();
        }
        return pendingAppointments;
    }

    public List<Service> getServices() {
        if (services == null) {
            loadDashboardData();
        }
        return services;
    }

    public List<Technician> getTechnicians() {
        if (technicians == null) {
            loadDashboardData();
        }
        return technicians;
    }

    public List<CounterStaff> getCounterStaffList() {
        if (counterStaffList == null) {
            loadDashboardData();
        }
        return counterStaffList;
    }

    public List<Customer> getCustomers() {
        if (customers == null) {
            loadDashboardData();
        }
        return customers;
    }

    public List<Manager> getManagers() {
        if (managers == null) {
            loadDashboardData();
        }
        return managers;
    }

    public List<Appointment> getAppointments() {
        if (appointments == null) {
            loadDashboardData();
        }
        return appointments;
    }

    public List<Feedback> getAllFeedback() {
        if (allFeedback == null) {
            loadDashboardData();
        }
        return allFeedback;
    }

    public List<AppointmentComment> getAllComments() {
        if (allComments == null) {
            loadDashboardData();
        }
        return allComments;
    }

    public String getRegName() {
        return regName;
    }

    public void setRegName(String regName) {
        this.regName = regName;
    }

    public String getRegEmail() {
        return regEmail;
    }

    public void setRegEmail(String regEmail) {
        this.regEmail = regEmail;
    }

    public String getRegPassword() {
        return regPassword;
    }

    public void setRegPassword(String regPassword) {
        this.regPassword = regPassword;
    }

    public String getRegGender() {
        return regGender;
    }

    public void setRegGender(String regGender) {
        this.regGender = regGender;
    }

    public String getRegPhone() {
        return regPhone;
    }

    public void setRegPhone(String regPhone) {
        this.regPhone = regPhone;
    }

    public String getRegIc() {
        return regIc;
    }

    public void setRegIc(String regIc) {
        this.regIc = regIc;
    }

    public String getRegAddress() {
        return regAddress;
    }

    public void setRegAddress(String regAddress) {
        this.regAddress = regAddress;
    }

    public String getRegSpecialty() {
        return regSpecialty;
    }

    public void setRegSpecialty(String regSpecialty) {
        this.regSpecialty = regSpecialty;
    }

    public String getRegUserType() {
        return regUserType;
    }

    public void setRegUserType(String regUserType) {
        this.regUserType = regUserType;
    }

    public String getNewServiceName() {
        return newServiceName;
    }

    public void setNewServiceName(String newServiceName) {
        this.newServiceName = newServiceName;
    }

    public String getNewServiceType() {
        return newServiceType;
    }

    public void setNewServiceType(String newServiceType) {
        this.newServiceType = newServiceType;
    }

    public double getNewServicePrice() {
        return newServicePrice;
    }

    public void setNewServicePrice(double newServicePrice) {
        this.newServicePrice = newServicePrice;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public double getDailyRevenue() {
        return dailyRevenue;
    }

    public String getRevenuePeriod() {
        return revenuePeriod;
    }

    public void setRevenuePeriod(String revenuePeriod) {
        this.revenuePeriod = revenuePeriod;
    }

    public String getRevenuePeriodDescription() {
        LocalDate today = LocalDate.now();
        switch (revenuePeriod != null ? revenuePeriod : "daily") {
            case "monthly":
                return "Completed payments recorded in "
                        + today.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            case "yearly":
                return "Completed payments recorded in " + today.getYear();
            case "daily":
            default:
                return "Completed payments recorded on "
                        + today.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        }
    }

    public Map<String, Integer> getTechnicianWorkload() {
        return technicianWorkload;
    }

    public Map<String, Integer> getServicePopularity() {
        return servicePopularity;
    }

    public List<Map<String, Object>> getCustomerFeedback() {
        return customerFeedback;
    }

    public Map<String, Object> getStatusAnalytics() {
        return statusAnalytics;
    }

    public BarChartModel getWorkloadChartModel() {
        return workloadChartModel;
    }

    public PieChartModel getStatusChartModel() {
        return statusChartModel;
    }

    public BarChartModel getPopularityChartModel() {
        return popularityChartModel;
    }

    public Technician getEditingTechnician() {
        return editingTechnician;
    }

    public void setEditingTechnician(Technician editingTechnician) {
        this.editingTechnician = editingTechnician;
    }
    
    public String getOriginalEditingTechnicianEmailValue() {
        return originalEditingTechnicianEmailValue;
    }

    public void setOriginalEditingTechnicianEmailValue(String originalEditingTechnicianEmailValue) {
        this.originalEditingTechnicianEmailValue = originalEditingTechnicianEmailValue;
    }

    public String getOriginalEditingTechnicianIcValue() {
        return originalEditingTechnicianIcValue;
    }

    public void setOriginalEditingTechnicianIcValue(String originalEditingTechnicianIcValue) {
        this.originalEditingTechnicianIcValue = originalEditingTechnicianIcValue;
    }
    
    public CounterStaff getEditingCounterStaff() {
        return editingCounterStaff;
    }

    public void setEditingCounterStaff(CounterStaff editingCounterStaff) {
        this.editingCounterStaff = editingCounterStaff;
    }

    public String getOriginalEditingCounterStaffEmailValue() {
        return originalEditingCounterStaffEmailValue;
    }

    public void setOriginalEditingCounterStaffEmailValue(String originalEditingCounterStaffEmailValue) {
        this.originalEditingCounterStaffEmailValue = originalEditingCounterStaffEmailValue;
    }

    public String getOriginalEditingCounterStaffIcValue() {
        return originalEditingCounterStaffIcValue;
    }

    public void setOriginalEditingCounterStaffIcValue(String originalEditingCounterStaffIcValue) {
        this.originalEditingCounterStaffIcValue = originalEditingCounterStaffIcValue;
    }

    public Manager getEditingManager() {
        return editingManager;
    }

    public void setEditingManager(Manager editingManager) {
        this.editingManager = editingManager;
    }

    public String getOriginalEditingManagerEmailValue() {
        return originalEditingManagerEmailValue;
    }

    public void setOriginalEditingManagerEmailValue(String originalEditingManagerEmailValue) {
        this.originalEditingManagerEmailValue = originalEditingManagerEmailValue;
    }

    public String getOriginalEditingManagerIcValue() {
        return originalEditingManagerIcValue;
    }

    public void setOriginalEditingManagerIcValue(String originalEditingManagerIcValue) {
        this.originalEditingManagerIcValue = originalEditingManagerIcValue;
    }
    
    public Customer getEditingCustomer() {
        return editingCustomer;
    }

    public void setEditingCustomer(Customer editingCustomer) {
        this.editingCustomer = editingCustomer;
    }

    public String getOriginalEditingCustomerEmailValue() {
        return originalEditingCustomerEmailValue;
    }

    public void setOriginalEditingCustomerEmailValue(String originalEditingCustomerEmailValue) {
        this.originalEditingCustomerEmailValue = originalEditingCustomerEmailValue;
    }

    public String getOriginalEditingCustomerIcValue() {
        return originalEditingCustomerIcValue;
    }

    public void setOriginalEditingCustomerIcValue(String originalEditingCustomerIcValue) {
        this.originalEditingCustomerIcValue = originalEditingCustomerIcValue;
    }

    public Service getEditingService() {
        return editingService;
    }

    public void setEditingService(Service editingService) {
        this.editingService = editingService;
    }

    public String getCurrentSection() {
        return currentSection;
    }

    public void setCurrentSection(String currentSection) {
        this.currentSection = currentSection;
    }
}
