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
import org.ccpm.dashboard.entity.Actividades;
import org.ccpm.dashboard.entity.Metas;

/**
 *
 * @author Vaio
 */
@Stateless
public class ActividadesFacade extends AbstractFacade<Actividades> implements ActividadesFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;
    @EJB
    private MetasFacadeLocal metasFacade;
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ActividadesFacade() {
        super(Actividades.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Actividades> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Actividades as o WHERE o.estado = :p1");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Actividades> findAll(String leyenda) {
        return em.createQuery("select object(o) FROM Actividades as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ORDER BY o.leyenda ").getResultList();
    }
    
    @Override
    public void create(Long idMetas, String leyenda) throws Exception {
        try {
           Metas metasAux = this.metasFacade.find(idMetas);
            Actividades objetivoAux = new Actividades();
            objetivoAux.setLeyenda(1 + "-" + leyenda.toUpperCase());
            //objetivoAux.setMetas(1);
            objetivoAux.setEstado(true);
            this.create(objetivoAux);
           // metasAux.getActividadess().add(objetivoAux);
           // this.metasFacade.edit(metasAux);
        } catch (Exception ex) {
            throw new Exception("Error al intentar crear el tipo de objetivo");
        }
    }
    
    @Override
    public void edit(Long id, String nombre) throws Exception {
        try {
            Actividades objetivoAux = this.find(id);
            Metas orgAux = objetivoAux.getMetas();
            objetivoAux.setLeyenda(orgAux.getId()+ "-" + nombre.toUpperCase());
            this.edit(objetivoAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la objetivo");
        }
    }
    
    @Override
    public void remove(Long id) throws Exception {
        try {
            Actividades objetivoAux = this.find(id);
            objetivoAux.setEstado(false);
            this.edit(objetivoAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar la objetivo");
        }
    }
    
}
    

