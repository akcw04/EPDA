package entity;

/**
 * Customer entity. Inherits from BaseEntity.
 * Represents a customer with phone number and address contact information.
 */
public class Customer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String phone;
    private String address;

    /**
     * Default constructor
     */
    public Customer() {
        super();
    }

    /**
     * Constructor with all fields
     *
     * @param id unique customer identifier
     * @param name customer name
     * @param email customer email address
     * @param phone customer phone number
     * @param address customer street address
     */
    public Customer(String id, String name, String email, String phone, String address) {
        super(id, name, email);
        this.phone = phone;
        this.address = address;
    }

    // ========== Getters & Setters ==========

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
