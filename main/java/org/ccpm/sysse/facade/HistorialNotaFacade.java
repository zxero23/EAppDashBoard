package org.ccpm.sysse.facade;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.ccpm.sysse.entity.HistorialNota;

@Stateless
public class HistorialNotaFacade extends AbstractFacade<HistorialNota> implements HistorialNotaFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public HistorialNotaFacade() {
        super(HistorialNota.class);
    }
    
}
