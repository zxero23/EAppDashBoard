/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.sysse.facade;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.ccpm.sysse.entity.ExpedienteEliminado;

/**
 *
 * @author Matias
 */
@Stateless
public class ExpedienteEliminadoFacade extends AbstractFacade<ExpedienteEliminado> implements ExpedienteEliminadoFacadeLocal {

    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ExpedienteEliminadoFacade() {
        super(ExpedienteEliminado.class);
    }
    
}
