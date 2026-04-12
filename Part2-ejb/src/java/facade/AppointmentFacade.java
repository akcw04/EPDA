package facade;

import entity.Appointment;
import entity.Service;
import util.IDGenerator;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AppointmentFacade - Stateless EJB for appointment management.
 * Includes CRUD, concurrency validation, and 5 reporting methods.
 */
@Stateless
public class AppointmentFacade {

    @PersistenceContext(unitName = "Part2-ejbPU")
    private EntityManager em;

    // ========== CRUD OPERATIONS ==========

    public Appointment createAppointment(Appointment appointment) {
        validateTechnicianAvailability(appointment);
        appointment.setId(IDGenerator.generateAppointmentID(em));
        em.persist(appointment);
        return appointment;
    }

    public Appointment getAppointmentByID(String appointmentID) {
        return em.find(Appointment.class, appointmentID);
    }

    public List<Appointment> getAllAppointments() {
        return em.createQuery(
                "SELECT a FROM Appointment a ORDER BY a.appointmentDateTime DESC",
                Appointment.class)
                .getResultList();
    }

    public List<Appointment> getAppointmentsByCustomer(String customerID) {
        return em.createQuery(
                "SELECT a FROM Appointment a WHERE a.customerId = :cid " +
                "ORDER BY a.appointmentDateTime DESC",
                Appointment.class)
                .setParameter("cid", customerID)
                .getResultList();
    }

    public List<Appointment> getAppointmentsByTechnician(String technicianID) {
        return em.createQuery(
                "SELECT a FROM Appointment a WHERE a.technicianId = :tid " +
                "ORDER BY a.appointmentDateTime DESC",
                Appointment.class)
                .setParameter("tid", technicianID)
                .getResultList();
    }

    public void updateAppointment(Appointment appointment) {
        Appointment managed = em.find(Appointment.class, appointment.getId());
        if (managed == null) return;
        managed.setStatus(appointment.getStatus());
        managed.setPaymentAmount(appointment.getPaymentAmount());
    }

    public void deleteAppointment(String appointmentID) {
        Appointment a = em.find(Appointment.class, appointmentID);
        if (a != null) em.remove(a);
    }

    // ========== CONCURRENCY VALIDATION ==========

    /**
     * Checks whether the technician already has an appointment that overlaps
     * with the requested time slot. Uses a native query because the Derby
     * TIMESTAMPADD ODBC escape syntax cannot be expressed in JPQL.
     */
    private void validateTechnicianAvailability(Appointment appointment) {
        String technicianID = appointment.getTechnicianId();
        LocalDateTime startTime = appointment.getAppointmentDateTime();
        int durationMinutes = appointment.getService() != null
                ? appointment.getService().getDurationMinutes() : 60;
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        String sql = "SELECT COUNT(*) FROM APPOINTMENT a " +
                     "JOIN SERVICE s ON a.service_id = s.service_id " +
                     "WHERE a.technician_id = ? " +
                     "AND a.status IN ('Pending', 'InProgress') " +
                     "AND a.appointment_datetime < ? " +
                     "AND {fn TIMESTAMPADD(SQL_TSI_MINUTE, s.duration_minutes, a.appointment_datetime)} > ?";

        Query q = em.createNativeQuery(sql);
        q.setParameter(1, technicianID);
        q.setParameter(2, Timestamp.valueOf(endTime));
        q.setParameter(3, Timestamp.valueOf(startTime));
        Number count = (Number) q.getSingleResult();
        if (count.intValue() > 0) {
            throw new IllegalStateException(
                    "Technician has overlapping appointment in the requested time slot");
        }
    }

    // ========== 5 REPORTING METHODS ==========

    public double getDailyRevenue() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        Double revenue = em.createQuery(
                "SELECT SUM(a.paymentAmount) FROM Appointment a " +
                "WHERE a.status = 'Completed' " +
                "AND a.appointmentDateTime >= :start AND a.appointmentDateTime < :end",
                Double.class)
                .setParameter("start", dayStart)
                .setParameter("end", dayEnd)
                .getSingleResult();
        return revenue != null ? revenue : 0.0;
    }

    public Map<String, Integer> getTechnicianWorkload() {
        List<Object[]> rows = em.createQuery(
                "SELECT t.name, COUNT(a) FROM Appointment a JOIN a.technician t " +
                "WHERE a.status IN ('Pending', 'InProgress') " +
                "GROUP BY t.name ORDER BY COUNT(a) DESC",
                Object[].class)
                .getResultList();

        Map<String, Integer> workload = new LinkedHashMap<>();
        for (Object[] row : rows) {
            workload.put((String) row[0], ((Number) row[1]).intValue());
        }
        return workload;
    }

    public Map<String, Integer> getServicePopularity() {
        List<Service> services = em.createQuery(
                "SELECT s FROM Service s", Service.class).getResultList();

        List<Object[]> counts = em.createQuery(
                "SELECT a.serviceId, COUNT(a) FROM Appointment a GROUP BY a.serviceId",
                Object[].class).getResultList();

        Map<String, Integer> countsByServiceId = new HashMap<>();
        for (Object[] row : counts) {
            countsByServiceId.put((String) row[0], ((Number) row[1]).intValue());
        }

        services.sort(Comparator.comparingInt(
                (Service s) -> countsByServiceId.getOrDefault(s.getId(), 0)).reversed());

        Map<String, Integer> popularity = new LinkedHashMap<>();
        for (Service s : services) {
            popularity.put(s.getServiceName(), countsByServiceId.getOrDefault(s.getId(), 0));
        }
        return popularity;
    }

    public Map<String, Object> getStatusAnalytics() {
        List<Object[]> rows = em.createQuery(
                "SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status",
                Object[].class)
                .getResultList();

        int pending = 0, completed = 0, inProgress = 0, cancelled = 0, total = 0;
        for (Object[] row : rows) {
            String status = (String) row[0];
            int count = ((Number) row[1]).intValue();
            total += count;
            if ("Pending".equals(status)) pending = count;
            else if ("Completed".equals(status)) completed = count;
            else if ("InProgress".equals(status)) inProgress = count;
            else if ("Cancelled".equals(status)) cancelled = count;
        }

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("pending", pending);
        analytics.put("completed", completed);
        analytics.put("inProgress", inProgress);
        analytics.put("cancelled", cancelled);
        analytics.put("total", total);
        analytics.put("pendingRatio", total > 0 ? (double) pending / total : 0.0);
        analytics.put("completedRatio", total > 0 ? (double) completed / total : 0.0);
        return analytics;
    }
}
