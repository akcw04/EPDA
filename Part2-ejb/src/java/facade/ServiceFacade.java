package facade;

import entity.Service;
import util.IDGenerator;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * ServiceFacade - Stateless EJB for service management.
 * Handles CRUD operations for available services.
 * Services define duration: Normal = 1 hour, Major = 3 hours.
 */
@Stateless
public class ServiceFacade {

    @PersistenceContext(unitName = "Part2-ejbPU")
    private EntityManager em;

    /**
     * Create a new service. Generates the ID before persist.
     */
    public Service createService(Service service) {
        service.setId(IDGenerator.generateServiceID(em));
        em.persist(service);
        return service;
    }

    /**
     * Retrieve a service by ID.
     */
    public Service getServiceByID(String serviceID) {
        return em.find(Service.class, serviceID);
    }

    /**
     * Get all services ordered by name.
     */
    public List<Service> getAllServices() {
        return em.createQuery(
                "SELECT s FROM Service s ORDER BY s.serviceName", Service.class)
                .getResultList();
    }

    /**
     * Get services by type ("Normal" or "Major").
     */
    public List<Service> getServicesByType(String type) {
        return em.createQuery(
                "SELECT s FROM Service s WHERE s.type = :type ORDER BY s.serviceName", Service.class)
                .setParameter("type", type)
                .getResultList();
    }

    /**
     * Update an existing service.
     */
    public void updateService(Service service) {
        em.merge(service);
    }

    /**
     * Delete a service by ID.
     */
    public void deleteService(String serviceID) {
        Service s = em.find(Service.class, serviceID);
        if (s != null) {
            em.remove(s);
        }
    }
}
