package entity;

/**
 * Service entity. Represents a service offered.
 * Services can be Normal (1 hour) or Major (3 hours).
 */
public class Service implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String serviceName;
    private String type;  // "Normal" or "Major"
    private int durationMinutes;  // 60 for Normal, 180 for Major
    private double basePrice;

    // Service type constants
    public static final String TYPE_NORMAL = "Normal";
    public static final String TYPE_MAJOR = "Major";
    public static final int DURATION_NORMAL = 60;   // 1 hour
    public static final int DURATION_MAJOR = 180;   // 3 hours

    /**
     * Default constructor
     */
    public Service() {
    }

    /**
     * Constructor with all required fields
     *
     * @param id unique service identifier
     * @param serviceName name of the service
     * @param type "Normal" or "Major"
     * @param basePrice base price of the service
     */
    public Service(String id, String serviceName, String type, double basePrice) {
        this.id = id;
        this.serviceName = serviceName;
        this.type = type;
        this.basePrice = basePrice;
        this.durationMinutes = type.equalsIgnoreCase(TYPE_NORMAL) ? DURATION_NORMAL : DURATION_MAJOR;
    }

    // ========== Getters & Setters ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        this.durationMinutes = type.equalsIgnoreCase(TYPE_NORMAL) ? DURATION_NORMAL : DURATION_MAJOR;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id='" + id + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", type='" + type + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", basePrice=" + basePrice +
                '}';
    }
}
