package org.ccpm.sysse.facade;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.ccpm.sysse.entity.HistorialExpediente;

@Stateless
public class HistorialExpedienteFacade extends AbstractFacade<HistorialExpediente> implements HistorialExpedienteFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public HistorialExpedienteFacade() {
        super(HistorialExpediente.class);
    }
    
}
