package entity;

import jakarta.persistence.*;

/**
 * Service entity. Represents a service offered by the automotive center.
 * Services can be Normal (1 hour) or Major (3 hours).
 */
@Entity
@Table(name = "SERVICE")
public class Service implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "service_id", length = 30)
    private String id;

    @Column(name = "service_name", length = 100, nullable = false)
    private String serviceName;

    @Column(name = "type", length = 20, nullable = false)
    private String type;  // "Normal" or "Major"

    @Column(name = "duration_minutes")
    private int durationMinutes;  // 60 for Normal, 180 for Major

    @Column(name = "base_price", nullable = false)
    private double basePrice;

    // Service type constants
    public static final String TYPE_NORMAL = "Normal";
    public static final String TYPE_MAJOR = "Major";
    public static final int DURATION_NORMAL = 60;   // 1 hour
    public static final int DURATION_MAJOR = 180;   // 3 hours

    // Minimum ("base") price per service type - the price floor enforced on
    // create/edit. Managers may set a HIGHER price but never below the floor.
    // Changes take effect for FUTURE bookings only; existing appointments
    // keep the paymentAmount that was snapshotted at booking time.
    public static final double BASE_PRICE_NORMAL = 500.0;
    public static final double BASE_PRICE_MAJOR  = 600.0;

    /**
     * Returns the minimum allowable price (the "base price") for the given
     * service type. Returns 0 if the type is null or unrecognised - callers
     * should validate the type separately.
     * @param type "Normal" or "Major"
     * @return floor price in RM
     */
    public static double getMinPriceForType(String type) {
        if (TYPE_NORMAL.equalsIgnoreCase(type)) return BASE_PRICE_NORMAL;
        if (TYPE_MAJOR.equalsIgnoreCase(type))  return BASE_PRICE_MAJOR;
        return 0.0;
    }

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
