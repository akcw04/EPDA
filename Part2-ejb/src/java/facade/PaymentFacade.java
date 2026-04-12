package facade;

import entity.Payment;
import util.IDGenerator;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Stateless EJB Facade for Payment CRUD operations.
 * Manages payment receipts and transaction records.
 */
@Stateless
public class PaymentFacade {

    @PersistenceContext(unitName = "Part2-ejbPU")
    private EntityManager em;

    public boolean createPayment(Payment payment) {
        try {
            payment.setId(IDGenerator.generatePaymentID(em));
            if (payment.getReceiptNumber() == null) {
                payment.setReceiptNumber("RCP-" + System.currentTimeMillis());
            }
            if (payment.getPaymentDate() == null) {
                payment.setPaymentDate(LocalDateTime.now());
            }
            if (payment.getStatus() == null) {
                payment.setStatus("Completed");
            }
            em.persist(payment);
            return true;
        } catch (PersistenceException e) {
            System.err.println("Error creating payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Payment getPaymentById(String paymentId) {
        return em.find(Payment.class, paymentId);
    }

    public List<Payment> getAllPayments() {
        return em.createQuery(
                "SELECT p FROM Payment p ORDER BY p.paymentDate DESC", Payment.class)
                .getResultList();
    }

    public List<Payment> getPaymentsByCustomer(String customerId) {
        return em.createQuery(
                "SELECT p FROM Payment p WHERE p.customerId = :cid ORDER BY p.paymentDate DESC",
                Payment.class)
                .setParameter("cid", customerId)
                .getResultList();
    }

    public List<Payment> getPaymentsByAppointment(String appointmentId) {
        return em.createQuery(
                "SELECT p FROM Payment p WHERE p.appointmentId = :aid ORDER BY p.paymentDate DESC",
                Payment.class)
                .setParameter("aid", appointmentId)
                .getResultList();
    }

    public double getTotalPaymentsByCustomer(String customerId) {
        Double total = em.createQuery(
                "SELECT SUM(p.amount) FROM Payment p WHERE p.customerId = :cid AND p.status = 'Completed'",
                Double.class)
                .setParameter("cid", customerId)
                .getSingleResult();
        return total != null ? total : 0.0;
    }

    public boolean updatePayment(Payment payment) {
        try {
            em.merge(payment);
            return true;
        } catch (PersistenceException e) {
            System.err.println("Error updating payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePayment(String paymentId) {
        Payment p = em.find(Payment.class, paymentId);
        if (p == null) return false;
        em.remove(p);
        return true;
    }
}
