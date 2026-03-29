/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facade;

import entity.CounterStaff;
import entity.Manager;
import entity.Technician;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

/**
 *
 * @author Chan Jia Zhil
 */
public abstract class AbstractFacade<T> {

    private Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }
    
    public T findByIC(String ic) {
        try {
            return getEntityManager()
                    .createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.IC = :ic", entityClass)
                    .setParameter("ic", ic)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<T> findAllManagerStaffs(String id) {
        try {
            return getEntityManager()
                    .createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.createdBy = :id", entityClass)
                    .setParameter("id", id)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<T> findAll() {
        jakarta.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findRange(int[] range) {
        jakarta.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        jakarta.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }
    
    public void updateByID(String id, String newName, String newPassword, String newGender, String newPhoneNumber,
            String newIC, String newEmail, String newAddress) {
        Long idLong = Long.parseLong(id);
        T entity = find(idLong); // Use your existing findByIC method
        if (entity != null) {
            if (entity instanceof Manager) {
                Manager manager = (Manager) entity;
                manager.setName(newName);
                manager.setPassword(newPassword);
                manager.setGender(newGender);
                manager.setPhone(newPhoneNumber);
                manager.setIC(newIC);
                manager.setEmail(newEmail);
                manager.setAddress(newAddress);
                edit(entity); // Use the edit() method to persist changes
            }
            if (entity instanceof CounterStaff) {
                CounterStaff counterStaff = (CounterStaff) entity;
                counterStaff.setName(newName);
                counterStaff.setPassword(newPassword);
                counterStaff.setGender(newGender);
                counterStaff.setPhone(newPhoneNumber);
                counterStaff.setIC(newIC);
                counterStaff.setEmail(newEmail);
                counterStaff.setAddress(newAddress);
                edit(entity); // Use the edit() method to persist changes
            }
            if (entity instanceof Technician) {
                Technician technician = (Technician) entity;
                technician.setName(newName);
                technician.setPassword(newPassword);
                technician.setGender(newGender);
                technician.setPhone(newPhoneNumber);
                technician.setIC(newIC);
                technician.setEmail(newEmail);
                technician.setAddress(newAddress);
                edit(entity); // Use the edit() method to persist changes
            }
        }
    }

    public int count() {
        jakarta.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        jakarta.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        jakarta.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
}
