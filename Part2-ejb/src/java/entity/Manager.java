package entity;

import jakarta.persistence.*;

/**
 * Manager entity. Inherits shared fields from BaseEntity.
 * JPA generates MANAGER table with all columns from BaseEntity + manager_id PK.
 */
@Entity
@Table(name = "MANAGER")
public class Manager extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "manager_id", length = 30)
    private String id;

    public Manager() {
        super();
    }

    public Manager(String id, String name, String email) {
        super(name, email, null);
        this.id = id;
    }

    public Manager(String id, String name, String email, String password) {
        super(name, email, password);
        this.id = id;
    }

    public Manager(String id, String name, String email, String password,
                   String gender, String phone, String ic, String address) {
        super(name, email, password, gender, phone, ic, address);
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public String toString() {
        return "Manager{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }
}
