/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.Date;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.dashboard.entity.Localidad;
import org.ccpm.dashboard.entity.Obras;
import org.ccpm.dashboard.entity.Organismo;
import org.ccpm.dashboard.entity.Situacion;


/**
 *
 * @author Vaio
 */
@Stateless
public class ObrasFacade extends AbstractFacade<Obras> implements ObrasFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;
    @EJB
    private SituacionFacadeLocal situacionFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    @EJB
    private LocalidadFacadeLocal localidadFacade;
   
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ObrasFacade() {
        super(Obras.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Obras> findAll(boolean estado, Long idOrg) {
        Query consulta = em.createQuery("select object(o) from Obras as o WHERE o.estado = :p1 AND o.id = :p2");
        consulta.setParameter("p1", estado);
        consulta.setParameter("p2", idOrg);
        return consulta.getResultList();
    }
    
    public List <Obras> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Obras as o WHERE o.estado = :p1 ORDER BY o.organismo.leyenda ");
        consulta.setParameter("p1", estado);
        
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Obras> findAll(String leyenda) {
        return em.createQuery("select object(o) FROM Obras as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ORDER BY o.leyenda ").getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Situacion> findAllSituacion(int id) {
        return em.createQuery("select object(o) FROM Obras as o WHERE o.estado = true AND o.situacion.id="+id+" ORDER BY o.leyenda ").getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Obras> findAllnombre(String leyenda) {  //obtiene el id en base al nombre ingresado!
        return em.createQuery("select object(o) FROM Obras as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ").getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Obras> findAll(Long situacion,String leyenda,Long idOrganismo,Long idLocalidad) {
        StringBuilder query = new StringBuilder();
        query.append("select object(o) FROM Obras as o WHERE o.estado = true ");
        
        if (!(leyenda.equals(""))) {
           query.append(" and o.leyenda LIKE '").append(leyenda.toUpperCase()).append("%'");
        } 
        
        if (idOrganismo!=0L) {
            query.append(" AND o.organismo.id =").append(idOrganismo);
        }
        
        if (idLocalidad!=0L) {
            query.append(" AND o.localidad.id =").append(idLocalidad);
        }
        
        if (situacion!=0L) {
            query.append(" AND o.situacion.id =").append(situacion);
        }
        //System.out.println("consulta---"+query);
        Query consulta = em.createQuery(query.toString());
        return consulta.getResultList();
        
    }
    
    @Override
    public void create(Long idSituacion, String leyenda,float latitud,float longitud,Long idOrganismo,Date FechaInicio,Date FechaFinal,String adjunto, String[] fotos, Long idLocalidad) throws Exception {
        try {
           Situacion situacionAux = this.situacionFacade.find(idSituacion);
           Organismo organismoAux = this.organismoFacade.find(idOrganismo);
           Localidad localidadAux = this.localidadFacade.find(idLocalidad);
            Obras obraAux = new Obras();
            obraAux.setLeyenda(leyenda.toUpperCase());
            obraAux.setLatitud(latitud);
            obraAux.setLongitud(longitud);
            obraAux.setFechaInicio(FechaInicio);
            obraAux.setFechaFinal(FechaFinal);
            obraAux.setAdjunto(adjunto);
            obraAux.setFotos(fotos);
            obraAux.setSituacion(situacionAux);
            obraAux.setOrganismo(organismoAux);
            obraAux.setEstado(true);
            obraAux.setLocalidad(localidadAux);
            this.create(obraAux);
           // situacionAux.getObrass().add(objetivoAux);
           // this.situacionFacade.edit(situacionAux);
        } catch (Exception ex) {
            throw new Exception("Error al intentar crear el tipo de objetivo");
        }
    }
    
    @Override
    public void edit(Long idSituacion,Long id, String leyenda,float latitud,float longitud,Long idOrganismo,Date FechaInicio,Date FechaFinal, String Adjunto,String[] fotos,Long idLocalidad) throws Exception {
        try {
            Obras obraAux = this.find(id);
            Situacion sitAux = this.situacionFacade.find(idSituacion);
            Organismo organismoAux = this.organismoFacade.find(idOrganismo);
            Localidad localidadAux = this.localidadFacade.find(idLocalidad);
            obraAux.setLeyenda(leyenda.toUpperCase());
            obraAux.setSituacion(sitAux);
            obraAux.setLatitud(latitud);
            obraAux.setLongitud(longitud);
            obraAux.setFechaInicio(FechaInicio);
            obraAux.setFechaFinal(FechaFinal);
            obraAux.setAdjunto(Adjunto);
            obraAux.setFotos(fotos);
            obraAux.setLocalidad(localidadAux);
            obraAux.setOrganismo(organismoAux);            
            this.edit(obraAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la objetivo");
        }
    }
    
    @Override
    public void remove(Long id) throws Exception {
        try {
            Obras objetivoAux = this.find(id);
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
    public Integer totalObrasPorEstado(Long idOrganismo,int estado){
        //System.out.println("consutla");
        return (Integer) em.createQuery("select object(o) FROM Obras as o WHERE o.estado=TRUE AND o.situacion.id="+estado +" AND o.organismo.id="+idOrganismo).getResultList().size();
         //Situacion sitAux = this.situacionFacade.find(5);
        
    }
    
    @Override
    public List<Obras> findAll(Long idOrganismo,int estado){
        //System.out.println("consutla");
        return em.createQuery("select object(o) FROM Obras as o WHERE o.estado=TRUE AND o.situacion.id="+estado +" AND o.organismo.id="+idOrganismo).getResultList();
         //Situacion sitAux = this.situacionFacade.find(5);
        
    }
    
}
    

