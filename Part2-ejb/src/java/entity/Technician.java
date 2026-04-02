package entity;

/**
 * Technician entity. Inherits from BaseEntity.
 * Represents a technician with specialty skill set.
 */
public class Technician extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String specialty;
    private boolean available;
    private String password;

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

    /**
     * Constructor with all fields including password
     *
     * @param id unique technician identifier
     * @param name technician name
     * @param email technician email address
     * @param specialty technician specialty skill
     * @param available availability status
     * @param password technician password (plain text; hashed before persistence)
     */
    public Technician(String id, String name, String email, String specialty, boolean available, String password) {
        super(id, name, email);
        this.specialty = specialty;
        this.available = available;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
