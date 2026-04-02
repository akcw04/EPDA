package entity;

/**
 * Manager entity. Inherits from BaseEntity.
 * Represents a manager with no additional attributes beyond id, name, email.
 */
public class Manager extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String password;

    /**
     * Default constructor
     */
    public Manager() {
        super();
    }

    /**
     * Constructor with id, name, and email
     *
     * @param id unique manager identifier
     * @param name manager name
     * @param email manager email address
     */
    public Manager(String id, String name, String email) {
        super(id, name, email);
    }

    /**
     * Constructor with all fields including password
     *
     * @param id unique manager identifier
     * @param name manager name
     * @param email manager email address
     * @param password manager password (plain text; hashed before persistence)
     */
    public Manager(String id, String name, String email, String password) {
        super(id, name, email);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
