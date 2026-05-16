/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.ccpm.dashboard.entity.Organismo;

/**
 *
 * @author Alvarenga Angel
 */
@Stateless
public class OrganismoFacade extends AbstractFacade<Organismo> implements OrganismoFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public OrganismoFacade() {
        super(Organismo.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Organismo> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Organismo as o WHERE o.estado = :p1");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Organismo> findAll(boolean estado,boolean descentralizado) {
        Query consulta = em.createQuery("select object(o) from Organismo as o WHERE o.estado = :p1 AND o.descentralizado = :p2 ORDER BY o.leyenda");
        consulta.setParameter("p1", estado);
        consulta.setParameter("p2", descentralizado);
       
        
        return consulta.getResultList();
    }
    
    
    public List <Organismo> findAll(Long idOrganismo) {
        return em.createQuery("select object(o) FROM Organismo as o WHERE o.estado = true AND o.descentralizado = true AND o.id ="+ idOrganismo ).getResultList();
    }
     
    @Override
    public List <Organismo> findAll(String leyenda,Long idOrganismo) {
        return em.createQuery("select object(o) FROM Organismo as o WHERE o.estado = true AND o.id ="+ idOrganismo +" AND o.leyenda LIKE '" + leyenda + "%' ORDER BY o.leyenda ").getResultList();
    }
    
    public List <Organismo> findAll(boolean estado,Long idOrganismo) {
        return em.createQuery("select object(o) FROM Organismo as o WHERE o.estado = true AND o.id ="+ idOrganismo).getResultList();
    }
     
   

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Organismo> findAll(String leyenda) {
        return em.createQuery("select object(o) FROM Organismo as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ORDER BY o.leyenda ").getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Organismo> findAllnombre(String leyenda) {  //obtiene el id en base al nombre ingresado!
        return em.createQuery("select object(o) FROM Organismo as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ").getResultList();
    }
    
    @Override
    public void create(Long idOrganismoPadre, Long codOrganismo, String leyenda,
            String descripcion,boolean descentralizado) throws Exception {
        boolean var = true;
        try {
            Organismo orgAux = new Organismo();
            orgAux.setCodOrganigrama(codOrganismo);
            orgAux.setLeyenda(leyenda.toUpperCase());
            orgAux.setDescripcion(descripcion);
            orgAux.setDescentralizado(descentralizado);
            orgAux.setEstado(true);
            this.create(orgAux);
            if (idOrganismoPadre > 0) {
                Organismo orgPadreAux = this.find(idOrganismoPadre);
                orgPadreAux.getOrganismos().add(orgAux);
                orgAux.setOrganismo(orgPadreAux);
                this.edit(orgAux);
                this.edit(orgPadreAux);
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar crear el organismo");
        }
    }
    
    @Override
    public void edit(Long id, Long codOrganismo, String leyenda,
            String descripcion,boolean descentralizado) throws Exception {
        try {
            Organismo orgAux = this.find(id);
            orgAux.setCodOrganigrama(codOrganismo);
            orgAux.setLeyenda(leyenda.toUpperCase());
            orgAux.setDescripcion(descripcion);
            orgAux.setDescentralizado(descentralizado);
            this.edit(orgAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el tipo de bien");
        }
    }
    
    @Override
    public void remove(Long id) throws Exception {
        try {
            Organismo organismoAux = this.find(id);
            organismoAux.setEstado(false);
            this.edit(organismoAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar el organismo");
        }
    }
    
    
}
