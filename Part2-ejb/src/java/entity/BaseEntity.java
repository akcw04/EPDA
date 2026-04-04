package entity;

import java.io.Serializable;
import jakarta.persistence.*;

/**
 * Abstract base class for all user entities.
 * MappedSuperclass: JPA generates shared columns (name, email, password, etc.) in each child table.
 * Each child entity declares its own @Id with a specific column name.
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "name", length = 100, nullable = false)
    protected String name;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    protected String email;

    @Column(name = "password", length = 255, nullable = false)
    protected String password;

    @Column(name = "gender", length = 10)
    protected String gender;

    @Column(name = "phone", length = 20)
    protected String phone;

    @Column(name = "ic", length = 20)
    protected String ic;

    @Column(name = "address", length = 255)
    protected String address;

    public BaseEntity() {
    }

    public BaseEntity(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public BaseEntity(String name, String email, String password,
                      String gender, String phone, String ic, String address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.phone = phone;
        this.ic = ic;
        this.address = address;
    }

    // Getters & Setters

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getIc() { return ic; }
    public void setIc(String ic) { this.ic = ic; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
