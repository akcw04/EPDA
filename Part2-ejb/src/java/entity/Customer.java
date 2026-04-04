package entity;

import jakarta.persistence.*;

/**
 * Customer entity. Inherits shared fields from BaseEntity.
 * All fields come from BaseEntity (name, email, password, gender, phone, ic, address).
 */
@Entity
@Table(name = "CUSTOMER")
public class Customer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "customer_id", length = 30)
    private String id;

    public Customer() {
        super();
    }

    public Customer(String id, String name, String email, String phone, String address) {
        super(name, email, null);
        this.id = id;
        this.phone = phone;
        this.address = address;
    }

    public Customer(String id, String name, String email, String password,
                    String phone, String address) {
        super(name, email, password);
        this.id = id;
        this.phone = phone;
        this.address = address;
    }

    public Customer(String id, String name, String email, String password,
                    String gender, String phone, String ic, String address) {
        super(name, email, password, gender, phone, ic, address);
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public String toString() {
        return "Customer{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }
}
