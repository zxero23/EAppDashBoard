/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.ccpm.dashboard.entity.Objetivos;
import org.ccpm.dashboard.entity.Metas;
import org.ccpm.dashboard.entity.Situacion;




/**
 *
 * @author Vaio
 */
@Stateless
public class MetasFacade extends AbstractFacade<Metas> implements MetasFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;
    @EJB
    private ObjetivosFacadeLocal ObjetivosFacade;
    @EJB
    private SituacionFacadeLocal situacionFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MetasFacade() {
        super(Metas.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Metas> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Metas as o WHERE o.estado = :p1 ORDER BY o.objetivos.organismo.leyenda");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Metas> findAll(String leyenda) {
        return em.createQuery("select object(o) FROM Metas as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ORDER BY o.leyenda ").getResultList();
    }
    
   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Metas> findAll(Long idObjetivo,Long idOrganismo,String leyenda) {
        StringBuilder query = new StringBuilder();
        
        query.append("select object(o) FROM Metas as o WHERE o.estado = true ");
        
        if (!(leyenda.equals(""))) {
           query.append(" and o.leyenda LIKE '").append(leyenda.toUpperCase()).append("%'");
        } 
        
        if (idObjetivo!=0L) {
            query.append(" AND o.objetivos.id =").append(idObjetivo);
        }
        
        if (idOrganismo !=0L){
            query.append(" AND o.objetivos.organismo.id =").append(idOrganismo);
            
        }
        
            
        
        //System.out.println("consulta---"+query);
        Query consulta = em.createQuery(query.toString());
        return consulta.getResultList();
        
    }
    
    
    
    
    @Override
    public void create(Long idObjetivos, Long idSituacion,String leyenda,int cantidad,Date fechainicio,Date fechafinal, String descripcion, boolean reduccion) throws Exception {
        try {
            
            Objetivos objetivoAux = this.ObjetivosFacade.find(idObjetivos);
            Situacion sitAux = this.situacionFacade.find(idSituacion);
            Metas metaAux = new Metas();
            metaAux.setLeyenda(leyenda.toUpperCase());
            metaAux.setObjetivos(objetivoAux);
            metaAux.setSituacion(sitAux);
            metaAux.setCantidad(cantidad);
            metaAux.setFechaInicio(fechainicio);
            metaAux.setFechaFinal(fechafinal);
            metaAux.setReduccion(reduccion);
            metaAux.setDescripcion(descripcion.toUpperCase());
            metaAux.setEstado(true);
            this.create(metaAux);
            objetivoAux.getMetas().add(metaAux);
            this.ObjetivosFacade.edit(objetivoAux);
        } catch (Exception ex) {
            
            throw new Exception("Error al intentar crear la meta");
        }
    }
    
    @Override
    public void edit(Long idObjetivos, Long idSituacion,Long id, String leyenda,int cantidad,Date fechainicio,Date fechafinal,String descripcion,boolean reduccion) throws Exception {
        try {
            Metas metaAux = this.find(id);
            Objetivos orgAux = metaAux.getObjetivos();
            Objetivos objetivoAux = this.ObjetivosFacade.find(idObjetivos);
            Situacion sitAux = this.situacionFacade.find(idSituacion);
            metaAux.setLeyenda(leyenda.toUpperCase());
            metaAux.setCantidad(cantidad);
            metaAux.setFechaInicio(fechainicio);
            metaAux.setFechaFinal(fechafinal);
            metaAux.setDescripcion(descripcion.toUpperCase());
             metaAux.setReduccion(reduccion);
            metaAux.setObjetivos(objetivoAux);
            metaAux.setSituacion(sitAux);
            this.edit(metaAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la meta");
        }
    }
    
    @Override
    public void remove(Long id) throws Exception {
        try {
            Metas metaAux = this.find(id);
            metaAux.setEstado(false);
            this.edit(metaAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar la meta");
        }
    }
    
    /*obtener la cantidad de objetivos por estado: 
    
        BLANCO=NO VENCIDO, 
        VERDE=EN TERMINO,
        NARANJA=EN ALERTA, 
        ROJO=CANCELADO
    */
    
    @Override
    public Integer totalMetasPorEstado(Long idObjetivo,int estado){ //cuenta el total de metas por estado por objetivo
        //System.out.println("consulta metas");
        return (Integer) em.createQuery("select object(o) FROM Metas as o WHERE o.estado=TRUE AND o.situacion.id="+estado +" AND o.objetivos.id="+idObjetivo).getResultList().size();
         //Situacion sitAux = this.situacionFacade.find(5);
        
    }
    
    @Override
    public List <Metas> findAll(Long idObjetivo,int estado) {
        //System.out.println("Por metas");
        return em.createQuery("select object(o) FROM Metas as o WHERE o.estado =TRUE AND o.situacion.id="+estado +" AND o.objetivos.id="+idObjetivo).getResultList();
    }
    
    @Override
    public Integer totalMetasPorEstadoOrg(Long idOrg,int estado){ //cuenta el total de metas por estado por cada organismo
        //System.out.println("consulta metas");
        return (Integer) em.createQuery("select object(o) FROM Metas as o WHERE o.estado=TRUE AND o.situacion.id="+estado +" AND o.objetivos.organismo.id="+idOrg).getResultList().size();
         //Situacion sitAux = this.situacionFacade.find(5);
        
    }
    
    @Override
    public List <Metas> findAllMetas(Long idOrg,int estado) {
        //System.out.println("Por metas");
        return em.createQuery("select object(o) FROM Metas as o WHERE o.estado=TRUE AND o.situacion.id="+estado +" AND o.objetivos.organismo.id="+idOrg).getResultList();
    }
    
}
    

