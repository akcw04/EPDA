package entity;

import jakarta.persistence.*;

/**
 * CounterStaff entity. Inherits shared fields from BaseEntity.
 * All fields come from BaseEntity (name, email, password, gender, phone, ic, address).
 */
@Entity
@Table(name = "COUNTER_STAFF")
public class CounterStaff extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "counter_staff_id", length = 30)
    private String id;

    public CounterStaff() {
        super();
    }

    public CounterStaff(String id, String name, String email, String password) {
        super(name, email, password);
        this.id = id;
    }

    public CounterStaff(String id, String name, String email, String password,
                        String gender, String phone, String ic, String address) {
        super(name, email, password, gender, phone, ic, address);
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public String toString() {
        return "CounterStaff{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }
}
