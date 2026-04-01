package entity;

/**
 * Technician entity. Inherits from BaseEntity.
 * Represents a technician with specialty skill set.
 */
public class Technician extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String specialty;
    private boolean available;

    /**
     * Default constructor
     */
    public Technician() {
        super();
        this.available = true;
    }

    /**
     * Constructor with all fields
     *
     * @param id unique technician identifier
     * @param name technician name
     * @param email technician email address
     * @param specialty technician specialty skill
     * @param available availability status
     */
    public Technician(String id, String name, String email, String specialty, boolean available) {
        super(id, name, email);
        this.specialty = specialty;
        this.available = available;
    }

    // ========== Getters & Setters ==========

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Technician{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", specialty='" + specialty + '\'' +
                ", available=" + available +
                '}';
    }
}
