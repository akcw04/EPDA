/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facade;

import entity.Technician;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author Chan Jia Zhil
 */
@Stateless
public class TechnicianFacade extends AbstractFacade<Technician> {

    @PersistenceContext(unitName = "Part2-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TechnicianFacade() {
        super(Technician.class);
    }
    
}
