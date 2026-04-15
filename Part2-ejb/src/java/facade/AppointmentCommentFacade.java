package facade;

import entity.AppointmentComment;
import util.IDGenerator;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

    public boolean hasCommentForAppointmentByCustomer(String appointmentId, String customerId) {
        Long count = em.createQuery(
                "SELECT COUNT(c) FROM AppointmentComment c " +
                "WHERE c.appointmentId = :aid AND c.customerId = :cid",
                Long.class)
                .setParameter("aid", appointmentId)
                .setParameter("cid", customerId)
                .getSingleResult();
        return count != null && count > 0;
    }

    /**
     * Build a per-customer feedback summary from actual customer comments.
     */
    public List<Map<String, Object>> getCustomerFeedbackSummary() {
        List<AppointmentComment> comments = getAllComments();
        Map<String, Map<String, Object>> summaryByCustomer = new LinkedHashMap<>();
        Map<String, Integer> ratedCounts = new HashMap<>();
        Map<String, Integer> ratingTotals = new HashMap<>();

        for (AppointmentComment comment : comments) {
            String customerId = comment.getCustomerId();
            if (customerId == null || customerId.isBlank()) {
                continue;
            }

            Map<String, Object> entry = summaryByCustomer.get(customerId);
            if (entry == null) {
                entry = new LinkedHashMap<>();
                String customerName = comment.getCustomer() != null
                        ? comment.getCustomer().getName()
                        : customerId;
                entry.put("customerName", customerName);
                entry.put("totalComments", 0);
                entry.put("averageRating", null);
                entry.put("latestComment", comment.getCommentText());
                entry.put("latestCommentDate", comment.getCreatedAt());
                summaryByCustomer.put(customerId, entry);
            }

            entry.put("totalComments", ((Number) entry.get("totalComments")).intValue() + 1);

            if (comment.getRating() != null) {
                ratedCounts.put(customerId, ratedCounts.getOrDefault(customerId, 0) + 1);
                ratingTotals.put(customerId, ratingTotals.getOrDefault(customerId, 0) + comment.getRating());
            }
        }

        for (Map.Entry<String, Map<String, Object>> summaryEntry : summaryByCustomer.entrySet()) {
            String customerId = summaryEntry.getKey();
            Integer ratedCount = ratedCounts.get(customerId);
            if (ratedCount != null && ratedCount > 0) {
                double avg = (double) ratingTotals.get(customerId) / ratedCount;
                summaryEntry.getValue().put("averageRating", avg);
            } else {
                summaryEntry.getValue().put("averageRating", "N/A");
            }
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
