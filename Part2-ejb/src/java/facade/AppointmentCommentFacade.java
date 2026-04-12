package facade;

import entity.AppointmentComment;
import util.IDGenerator;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateless EJB Facade for AppointmentComment CRUD operations.
 * Comments are written by customers about their appointments.
 */
@Stateless
public class AppointmentCommentFacade {

    @PersistenceContext(unitName = "Part2-ejbPU")
    private EntityManager em;

    public boolean createComment(AppointmentComment comment) {
        try {
            comment.setId(IDGenerator.generateCommentID(em));
            if (comment.getCreatedAt() == null) {
                comment.setCreatedAt(LocalDateTime.now());
            }
            em.persist(comment);
            return true;
        } catch (PersistenceException e) {
            System.err.println("Error creating comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public AppointmentComment getCommentById(String commentId) {
        return em.find(AppointmentComment.class, commentId);
    }

    public List<AppointmentComment> getAllComments() {
        return em.createQuery(
                "SELECT c FROM AppointmentComment c ORDER BY c.createdAt DESC",
                AppointmentComment.class)
                .getResultList();
    }

    public List<AppointmentComment> getCommentsByAppointment(String appointmentId) {
        return em.createQuery(
                "SELECT c FROM AppointmentComment c WHERE c.appointmentId = :aid ORDER BY c.createdAt DESC",
                AppointmentComment.class)
                .setParameter("aid", appointmentId)
                .getResultList();
    }

    public List<AppointmentComment> getCommentsByCustomer(String customerId) {
        return em.createQuery(
                "SELECT c FROM AppointmentComment c WHERE c.customerId = :cid ORDER BY c.createdAt DESC",
                AppointmentComment.class)
                .setParameter("cid", customerId)
                .getResultList();
    }

    /**
     * Build a per-customer summary for customers who have submitted comments.
     */
    public List<Map<String, Object>> getCustomerFeedbackSummary() {
        List<AppointmentComment> comments = getAllComments();
        Map<String, Map<String, Object>> summaryByCustomer = new LinkedHashMap<>();

        for (AppointmentComment comment : comments) {
            String customerId = comment.getCustomerId();
            if (customerId == null || summaryByCustomer.containsKey(customerId)) {
                continue;
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            String customerName = comment.getCustomer() != null
                    ? comment.getCustomer().getName()
                    : customerId;
            entry.put("customerName", customerName);
            entry.put("totalAppointments", 0);
            entry.put("completedAppointments", 0);
            entry.put("totalSpent", 0.0);
            summaryByCustomer.put(customerId, entry);
        }

        if (summaryByCustomer.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> customerIds = new ArrayList<>(summaryByCustomer.keySet());
        List<Object[]> appointmentRows = em.createQuery(
                "SELECT a.customerId, COUNT(a), " +
                "SUM(CASE WHEN a.status = 'Completed' THEN 1 ELSE 0 END), " +
                "SUM(CASE WHEN a.status = 'Completed' THEN a.paymentAmount ELSE 0.0 END) " +
                "FROM Appointment a " +
                "WHERE a.customerId IN :customerIds " +
                "GROUP BY a.customerId",
                Object[].class)
                .setParameter("customerIds", customerIds)
                .getResultList();

        for (Object[] row : appointmentRows) {
            Map<String, Object> entry = summaryByCustomer.get((String) row[0]);
            if (entry == null) {
                continue;
            }

            entry.put("totalAppointments", row[1] != null ? ((Number) row[1]).intValue() : 0);
            entry.put("completedAppointments", row[2] != null ? ((Number) row[2]).intValue() : 0);
            entry.put("totalSpent", row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
        }

        return new ArrayList<>(summaryByCustomer.values());
    }

    /**
     * Get comments for all appointments of a specific technician.
     */
    public List<AppointmentComment> getCommentsByTechnician(String technicianId) {
        return em.createQuery(
                "SELECT c FROM AppointmentComment c WHERE c.appointment.technicianId = :tid " +
                "ORDER BY c.createdAt DESC",
                AppointmentComment.class)
                .setParameter("tid", technicianId)
                .getResultList();
    }

    /**
     * Get average rating from customer comments for a technician.
     */
    public double getAverageRatingByTechnician(String technicianId) {
        Double avg = em.createQuery(
                "SELECT AVG(c.rating) FROM AppointmentComment c " +
                "WHERE c.appointment.technicianId = :tid AND c.rating IS NOT NULL",
                Double.class)
                .setParameter("tid", technicianId)
                .getSingleResult();
        return avg != null ? avg : 0.0;
    }

    public boolean updateComment(AppointmentComment comment) {
        try {
            em.merge(comment);
            return true;
        } catch (PersistenceException e) {
            System.err.println("Error updating comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteComment(String commentId) {
        AppointmentComment c = em.find(AppointmentComment.class, commentId);
        if (c == null) return false;
        em.remove(c);
        return true;
    }
}
