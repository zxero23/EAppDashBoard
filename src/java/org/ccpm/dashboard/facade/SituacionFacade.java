/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.ccpm.dashboard.entity.Situacion;
import org.ccpm.dashboard.entity.Objetivos;
import org.ccpm.dashboard.entity.Metas;

/**
 *
 * @author Vaio
 */
@Stateless
public class SituacionFacade extends AbstractFacade<Situacion> implements SituacionFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;
    
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public SituacionFacade() {
        super(Situacion.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Situacion> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Situacion as o WHERE o.estado = :p1");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Situacion> findAll(String leyenda) {
        return em.createQuery("select object(o) FROM Situacion as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ORDER BY o.leyenda ").getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Situacion> findAllnombre(String leyenda) {  //obtiene el id en base al nombre ingresado!
        return em.createQuery("select object(o) FROM Situacion as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ").getResultList();
    }
    
    @Override
    public void create(Long idObjetivos, String leyenda) throws Exception {
        try {
           //Objetivos objetivoAux = this.objetivoFacade.find(idObjetivos);
            Situacion situacionAux = new Situacion();
            //objetivoAux.setLeyenda(1 + "-" + leyenda.toUpperCase());
            //objetivoAux.setObjetivos(1);
            situacionAux.setLeyenda(leyenda);
            situacionAux.setEstado(true);
            this.create(situacionAux);
           // objetivoAux.getSituacions().add(objetivoAux);
           // this.objetivoFacade.edit(objetivoAux);
        } catch (Exception ex) {
            throw new Exception("Error al intentar crear el tipo de situacion");
        }
    }
    
    @Override
    public void edit(Long id, String nombre) throws Exception {
        try {
            Situacion situacionAux = this.find(id);
            //Objetivos orgAux = objetivoAux.getObjetivos();
            situacionAux.setLeyenda(nombre.toUpperCase());
            this.edit(situacionAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la situación");
        }
    }
    
    @Override
    public void remove(Long id) throws Exception {
        try {
            Situacion objetivoAux = this.find(id);
            objetivoAux.setEstado(false);
            this.edit(objetivoAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar la objetivo");
        }
    }
    
}
    

