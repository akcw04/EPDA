package facade;

import entity.Feedback;
import util.IDGenerator;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Stateless EJB Facade for Feedback CRUD operations.
 * Feedback is written by technicians about their appointments.
 */
@Stateless
public class FeedbackFacade {

    @PersistenceContext(unitName = "Part2-ejbPU")
    private EntityManager em;

    public boolean createFeedback(Feedback feedback) {
        try {
            feedback.setId(IDGenerator.generateFeedbackID(em));
            if (feedback.getCreatedAt() == null) {
                feedback.setCreatedAt(LocalDateTime.now());
            }
            em.persist(feedback);
            return true;
        } catch (PersistenceException e) {
            return false;
        }
    }

    public Feedback getFeedbackById(String feedbackId) {
        return em.find(Feedback.class, feedbackId);
    }

    public List<Feedback> getAllFeedback() {
        return em.createQuery(
                "SELECT f FROM Feedback f ORDER BY f.createdAt DESC", Feedback.class)
                .getResultList();
    }

    public List<Feedback> getFeedbackByTechnician(String technicianId) {
        return em.createQuery(
                "SELECT f FROM Feedback f WHERE f.technicianId = :tid ORDER BY f.createdAt DESC",
                Feedback.class)
                .setParameter("tid", technicianId)
                .getResultList();
    }

    public Feedback getFeedbackByAppointment(String appointmentId) {
        List<Feedback> list = em.createQuery(
                "SELECT f FROM Feedback f WHERE f.appointmentId = :aid", Feedback.class)
                .setParameter("aid", appointmentId)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean hasFeedbackForAppointment(String appointmentId) {
        Long count = em.createQuery(
                "SELECT COUNT(f) FROM Feedback f WHERE f.appointmentId = :aid",
                Long.class)
                .setParameter("aid", appointmentId)
                .getSingleResult();
        return count != null && count > 0;
    }

    public boolean updateFeedback(Feedback feedback) {
        try {
            em.merge(feedback);
            return true;
        } catch (PersistenceException e) {
            return false;
        }
    }

    public boolean deleteFeedback(String feedbackId) {
        Feedback f = em.find(Feedback.class, feedbackId);
        if (f == null) return false;
        em.remove(f);
        return true;
    }
}
