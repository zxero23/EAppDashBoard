/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.dashboard.entity.Objetivos;
import org.ccpm.dashboard.entity.Organismo;
import org.ccpm.dashboard.entity.Situacion;


/**
 *
 * @author Vaio
 */
@Stateless
public class ObjetivosFacade extends AbstractFacade<Objetivos> implements ObjetivosFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    @EJB
    private SituacionFacadeLocal situacionFacade;
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ObjetivosFacade() {
        super(Objetivos.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Objetivos> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Objetivos as o WHERE o.estado = :p1 ORDER BY o.organismo.leyenda");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }
    
    
    public List <Objetivos> findAll(boolean estado, long idOrg) {
        Query consulta = em.createQuery("select object(o) from Objetivos as o WHERE o.estado = :p1 AND o.organismo.id = :p2");
        consulta.setParameter("p1", estado);
        consulta.setParameter("p2", idOrg);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Objetivos> findAll(String leyenda) {
        return em.createQuery("select object(o) FROM Objetivos as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ORDER BY o.leyenda ").getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Objetivos> findAll(Long idOrganismo,String leyenda) {
        StringBuilder query = new StringBuilder();
        query.append("select object(o) FROM Objetivos as o WHERE o.estado = true ");
        
        if (!(leyenda.equals(""))) {
           query.append(" and o.leyenda LIKE '").append(leyenda.toUpperCase()).append("%'");
        } 
        if (idOrganismo!=0L) {
            query.append(" AND o.organismo.id =").append(idOrganismo);
        }
        //System.out.println("consulta---"+query);
        Query consulta = em.createQuery(query.toString());
        return consulta.getResultList();
        
    }
    
    
    
    
    
    
    
    @Override
    public void create(Long idOrganismo,Long idSituacion, String leyenda,String descripcion) throws Exception {
        try {
            Organismo organismoAux = this.organismoFacade.find(idOrganismo);
            Situacion sitAux = this.situacionFacade.find(idSituacion);
            Objetivos objetivoAux = new Objetivos();
            objetivoAux.setLeyenda(leyenda.toUpperCase());
            objetivoAux.setOrganismo(organismoAux);
            objetivoAux.setSituacion(sitAux);
            objetivoAux.setDescripcion(descripcion.toUpperCase());
            objetivoAux.setEstado(true);
            this.create(objetivoAux);
            organismoAux.getObjetivoss().add(objetivoAux);
            this.organismoFacade.edit(organismoAux);
        } catch (Exception ex) {
            throw new Exception("Error al intentar crear el objetivo");
        }
    }
    
    @Override
    public void edit(Long idOrganismo,Long idSituacion,Long id, String leyenda, String descripcion) throws Exception {
        try {
            Objetivos objetivoAux = this.find(id);
            Organismo orgAux = this.organismoFacade.find(idOrganismo);
            Situacion sitAux = this.situacionFacade.find(idSituacion);
            objetivoAux.setLeyenda(leyenda.toUpperCase());
            objetivoAux.setOrganismo(orgAux);
            objetivoAux.setDescripcion(descripcion.toUpperCase());
            objetivoAux.setSituacion(sitAux);
            this.edit(objetivoAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la objetivo");
        }
    }
    
    @Override
    public void remove(Long id) throws Exception {
        try {
            Objetivos objetivoAux = this.find(id);
            objetivoAux.setEstado(false);
            this.edit(objetivoAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar la objetivo");
        }
    }
    
    /*obtener la cantidad de objetivos por estado: 
    
        BLANCO=NO VENCIDO, 
        VERDE=EN TERMINO,
        NARANJA=EN ALERTA, 
        ROJO=CANCELADO
    */
    
    @Override
    public Integer totalObjetivosPorEstado(Long idOrganismo,int estado){
        //System.out.println("consutla");
        return (Integer) em.createQuery("select object(o) FROM Objetivos as o WHERE o.estado=TRUE AND o.situacion.id="+estado +" AND o.organismo.id="+idOrganismo).getResultList().size();
         //Situacion sitAux = this.situacionFacade.find(5);
        
    }
    
    
    @Override
    public List <Objetivos> findAll(Long idOrganismo,int estado) {
        //System.out.println("Por estado");
        return em.createQuery("select object(o) FROM Objetivos as o WHERE o.estado =TRUE AND o.situacion.id="+estado +" AND o.organismo.id="+idOrganismo).getResultList();
    }
}
    

