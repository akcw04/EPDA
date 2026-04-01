package entity;

/**
 * Manager entity. Inherits from BaseEntity.
 * Represents a manager with no additional attributes beyond id, name, email.
 */
public class Manager extends BaseEntity {
    private static final long serialVersionUID = 1L;

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

    @Override
    public String toString() {
        return "Manager{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
