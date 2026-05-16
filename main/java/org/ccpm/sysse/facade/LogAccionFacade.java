package org.ccpm.sysse.facade;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.ccpm.sysse.entity.LogAccion;

@Stateless
public class LogAccionFacade extends AbstractFacade<LogAccion> implements LogAccionFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public LogAccionFacade() {
        super(LogAccion.class);
    }
    
}
