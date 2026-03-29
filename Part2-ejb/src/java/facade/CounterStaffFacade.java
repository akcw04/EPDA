/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facade;

import entity.CounterStaff;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author Chan Jia Zhil
 */
@Stateless
public class CounterStaffFacade extends AbstractFacade<CounterStaff> {

    @PersistenceContext(unitName = "Part2-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CounterStaffFacade() {
        super(CounterStaff.class);
    }
    
}
