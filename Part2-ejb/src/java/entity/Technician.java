package entity;

import jakarta.persistence.*;

/**
 * Technician entity. Inherits shared fields from BaseEntity.
 * Extra fields: specialty, available.
 */
@Entity
@Table(name = "TECHNICIAN")
public class Technician extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "technician_id", length = 30)
    private String id;

    @Column(name = "specialty", length = 100)
    private String specialty;

    @Column(name = "available", nullable = false)
    private boolean available;

    public Technician() {
        super();
        this.available = true;
    }

    public Technician(String id, String name, String email, String specialty, boolean available) {
        super(name, email, null);
        this.id = id;
        this.specialty = specialty;
        this.available = available;
    }

    public Technician(String id, String name, String email, String specialty,
                      boolean available, String password) {
        super(name, email, password);
        this.id = id;
        this.specialty = specialty;
        this.available = available;
    }

    public Technician(String id, String name, String email, String password,
                      String gender, String phone, String ic, String address,
                      String specialty, boolean available) {
        super(name, email, password, gender, phone, ic, address);
        this.id = id;
        this.specialty = specialty;
        this.available = available;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "Technician{id='" + id + "', name='" + name + "', specialty='" + specialty + "'}";
    }
}
