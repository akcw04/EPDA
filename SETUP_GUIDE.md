# EPDA - Enterprise Programming Dynamic Appointment System

## System Architecture

**3-Tier MVC Architecture:**
- **View Layer:** JSF 4.0 with PrimeFaces 13.0.0
- **Business Logic:** Jakarta EE 10 Stateless EJBs
- **Data Access:** Raw JDBC with PreparedStatement & try-with-resources

**Technology Stack:**
- Java 21
- Jakarta EE 10
- GlassFish 8.0.0
- NetBeans 23
- Apache Derby (Database)

---

## Project Structure

```
Part2-ejb/
  └─ src/java/
      ├─ entity/          (Models: BaseEntity, Customer, Technician, Manager, Service, Appointment)
      ├─ facade/          (EJBs: UserFacade, AppointmentFacade, ServiceFacade)
      └─ util/            (Utilities: DatabaseConnection, IDGenerator, SecurityUtil)

Part2-war/
  ├─ src/java/bean/       (JSF Managed Beans: AppointmentBean, UserBean, ServiceBean, ReportBean)
  └─ web/
      ├─ index.xhtml      (Home page)
      ├─ appointments.xhtml (Appointment management)
      ├─ users.xhtml      (Customer/Technician/Manager management)
      ├─ services.xhtml   (Service management)
      ├─ reports.xhtml    (Analytics & reporting)
      └─ WEB-INF/         (beans.xml, web.xml, glassfish-web.xml)
```

---

## Key Features

### 1. Strict OOP & Modeling
- **Inheritance:** BaseEntity (abstract) for shared attributes (id, name, email)
- **Encapsulation:** All fields private with getters/setters
- **Serializable:** All models implement `java.io.Serializable` for session storage
- **Composition:** Appointment contains Customer, Technician, Service objects (not just IDs)
- **Polymorphism:** Facade interfaces for interchangeable business logic

### 2. Business Logic Rules

#### ID Generation (Java-Side)
- No IDENTITY or AUTO_INCREMENT in database
- IDs generated in EJB before `prepareStatement`
- Format: `PREFIX-NNN` (e.g., `T-001`, `C-002`, `S-003`)
  - `T-` Technician
  - `C-` Customer
  - `M-` Manager
  - `S-` Service
  - `A-` Appointment

#### Password Security
- SHA-256 hashing in Business Tier (EJB)
- Implemented in `SecurityUtil.hashPassword()`
- Hashed before reaching JDBC layer

#### Concurrency & Validation
- **Technician Availability:** No overlapping appointments
- **Service Duration:** Normal = 1 hour, Major = 3 hours
- **Prevention:** Query for time slot conflicts before insert
- Validation occurs in AppointmentFacade before JDBC write

### 3. Reporting (5 Required Methods in AppointmentFacade)

1. **Daily Revenue**
   - Sum of `paymentAmount` where status='Completed' and date=TODAY
   - Method: `getDailyRevenue()`

2. **Technician Workload**
   - Count active tasks (Pending + InProgress) grouped by Technician
   - Method: `getTechnicianWorkload()`

3. **Service Popularity**
   - Count distribution of service types across all appointments
   - Method: `getServicePopularity()`

4. **Customer Feedback**
   - Extract all non-null comments and ratings
   - Method: `getCustomerFeedback()`

5. **Status Analytics**
   - Calculate ratio of Pending vs Completed appointments
   - Method: `getStatusAnalytics()`

### 4. Error Handling
- Every catch block includes `e.printStackTrace()` for GlassFish logging
- Errors logged to `System.err` for visibility in server logs
- Update in-memory state only after successful database commit

---

## Setup Instructions

### 1. Database Setup

**Create Database in Apache Derby:**
```sql
-- Connect to Derby and create database "EPDA"
create database EPDA;
```

**Run Schema Script:**
1. Locate: `database_schema.sql` in project root
2. Execute in Derby SQL client or NetBeans Database Console
3. Creates all tables with sample data

### 2. GlassFish Configuration

**Configure JDBC Resource (optional for connection pooling):**
```xml
In GlassFish Admin Console:
1. Resources > JDBC Resources
2. New JDBC Resource: jdbc/EPDA
3. Configure connection details
4. Reference in EJBs via @Resource injection
```

Currently configured to direct `DriverManager` connection in `DatabaseConnection.java`.

### 3. Deployment

**From NetBeans:**
1. Right-click project → Clean & Build
2. Deploy to GlassFish 8.0.0
3. Access at: `http://localhost:8080/Part2-war/`

**Manual Deployment:**
```bash
asadmin deploy Part2-ear.ear
```

---

## Tier Responsibilities

### View Layer (JSF/PrimeFaces)
- `index.xhtml` - Home/navigation
- `appointments.xhtml` - Schedule and manage appointments
- `users.xhtml` - Create/update customers, technicians, managers
- `services.xhtml` - Manage available services
- `reports.xhtml` - View analytics dashboards
- Regex validators and `required="true"` attributes on forms

### Controller Layer (Managed Beans)
- `AppointmentBean` - SessionScoped, handles appointment operations
- `UserBean` - SessionScoped, manages all user types
- `ServiceBean` - SessionScoped, manages services
- `ReportBean` - ViewScoped, displays reports
- Use `@Named` and `@SessionScoped`/`@ViewScoped`
- Call EJBs via `@EJB` injection

### Business Tier (EJBs)
- `AppointmentFacade` - Appointment CRUD, availability validation, 5 reports
- `UserFacade` - Technician/Customer/Manager CRUD
- `ServiceFacade` - Service CRUD
- All marked `@Stateless`
- ID generation before SQL
- Password hashing before storage
- Concurrency checks

### Data Tier (JDBC)
- `DatabaseConnection` - Connection management
- `IDGenerator` - ID generation utilities
- `SecurityUtil` - Password hashing
- Raw SQL with `PreparedStatement`
- Try-with-resources for resource cleanup

---

## Database Schema

### Tables

| Table | Key Fields | Description |
|-------|-----------|-------------|
| Manager | manager_id (PK), name, email | System managers |
| Technician | technician_id (PK), name, email, specialty, available | Service technicians |
| Customer | customer_id (PK), name, email, phone, address | Service customers |
| Service | service_id (PK), service_name, type, base_price | Available services (Normal/Major) |
| Appointment | appointment_id (PK), customer_id (FK), technician_id (FK), service_id (FK), appointment_datetime, status, payment_amount, comments, rating | Scheduled appointments |

### Constraints
- Foreign keys enforce referential integrity
- Unique constraints on email fields
- Status values: 'Pending', 'InProgress', 'Completed', 'Cancelled'
- Service types: 'Normal' (60 min), 'Major' (180 min)

---

## Development Guidelines

### Code Style
- Follow standard Java naming conventions
- Encapsulation: all fields private
- Comments for public methods
- Getters/Setters for all properties

### EJB Best Practices
- Use `@Stateless` for facadesbjects (no state between calls)
- Inject dependencies via `@EJB`
- Throw `SQLException` from facade methods
- Let ManagedBeans catch and handle exceptions

### JSF Best Practices
- Use PrimeFaces for UI components
- SessionScoped for cross-page data
- ViewScoped for single-page data
- Always include `rendered="#{bean != null}"` checks
- Use `f:viewAction` to initialize reports

### JDBC Best Practices
- Always use try-with-resources
- Always use `PreparedStatement` (prevent SQL injection)
- Close ResultSet explicitly
- Log errors with `e.printStackTrace()`

---

## Common Issues & Solutions

### Issue: "Table not found"
**Solution:** Run `database_schema.sql` to create tables and sample data

### Issue: "No EJB found"
**Solution:** 
- Ensure EJB JAR is in WAR's classpath
- Check `package-appclient.xml` manifest
- Verify `@EJB` injection in managed bean

### Issue: "Connection refused"
**Solution:**
- Verify Derby is running
- Check datasource configuration in GlassFish
- Verify database URL in `DatabaseConnection.java`

### Issue: "Generated ID already exists"
**Solution:**
- Reset ID counters in database
- Run `database_schema.sql` to recreate tables

---

## Future Enhancements

- [ ] JPA/Hibernate integration
- [ ] Connection pooling via GlassFish JNDI
- [ ] Authentication & Authorization (Roles)
- [ ] Audit logging
- [ ] Email notifications
- [ ] REST API endpoints
- [ ] Advanced reporting (PDF export)
- [ ] Scheduler for automated tasks

---

## Contact & Support

For issues or questions, refer to:
- GlassFish 8.0.0 documentation
- Jakarta EE 10 specification
- Apache Derby SQL documentation

---

**Last Updated:** March 24, 2026
**Version:** 1.0
**Status:** Initial Implementation
