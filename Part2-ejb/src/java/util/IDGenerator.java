package util;

import jakarta.persistence.EntityManager;

/**
 * ID Generator utility for manual ID generation before database insert.
 * Format: PREFIX-NNN (3-digit zero-padded)
 */
public class IDGenerator {

    /**
     * Generate the next ID for a given entity, based on the current MAX id.
     *
     * @param em         EntityManager used to run the JPQL query
     * @param entityName JPQL entity name (e.g. "Technician")
     * @param idField    JPQL id field on the entity (e.g. "id")
     * @param prefix     ID prefix (e.g. "T")
     */
    public static String generateNextID(EntityManager em, String entityName,
                                        String idField, String prefix) {
        String jpql = "SELECT MAX(e." + idField + ") FROM " + entityName + " e";
        String maxId = em.createQuery(jpql, String.class).getSingleResult();
        int nextNumber = 1;
        if (maxId != null) {
            String[] parts = maxId.split("-");
            nextNumber = Integer.parseInt(parts[parts.length - 1]) + 1;
        }
        return String.format("%s-%03d", prefix, nextNumber);
    }

    public static String generateTechnicianID(EntityManager em) {
        return generateNextID(em, "Technician", "id", "T");
    }

    public static String generateCustomerID(EntityManager em) {
        return generateNextID(em, "Customer", "id", "C");
    }

    public static String generateServiceID(EntityManager em) {
        return generateNextID(em, "Service", "id", "S");
    }

    public static String generateAppointmentID(EntityManager em) {
        return generateNextID(em, "Appointment", "id", "A");
    }

    public static String generateManagerID(EntityManager em) {
        return generateNextID(em, "Manager", "id", "M");
    }

    public static String generateCounterStaffID(EntityManager em) {
        return generateNextID(em, "CounterStaff", "id", "CS");
    }

    public static String generateFeedbackID(EntityManager em) {
        return generateNextID(em, "Feedback", "id", "FB");
    }

    public static String generatePaymentID(EntityManager em) {
        return generateNextID(em, "Payment", "id", "PY");
    }

    public static String generateCommentID(EntityManager em) {
        return generateNextID(em, "AppointmentComment", "id", "CM");
    }
}
