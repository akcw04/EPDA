package entity;

import java.io.Serializable;

/**
 * Abstract base class for all entities.
 * Defines shared attributes: id, name, and email.
 * All models must inherit from this base.
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String name;
    protected String email;

    /**
     * Default constructor
     */
    public BaseEntity() {
    }

    /**
     * Constructor with id, name, and email
     *
     * @param id unique identifier
     * @param name entity name
     * @param email entity email address
     */
    public BaseEntity(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // ========== Getters & Setters ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
