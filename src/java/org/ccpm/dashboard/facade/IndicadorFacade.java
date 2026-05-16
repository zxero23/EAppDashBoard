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
import org.ccpm.dashboard.entity.Indicador;
import org.ccpm.dashboard.entity.Metas;



/**
 *
 * @author Vaio
 */
@Stateless
public class IndicadorFacade extends AbstractFacade<Indicador> implements IndicadorFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;
    @EJB
    private MetasFacadeLocal metasFacade;
   
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public IndicadorFacade() {
        super(Indicador.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Indicador> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Indicador as o WHERE o.estado = :p1");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Indicador> findAll(boolean estado, Long metaId) {
        Query consulta = em.createQuery("select object(o) from Indicador as o WHERE o.estado = :p1 AND o.metas.id = :p2 ");
        consulta.setParameter("p1", estado);
        consulta.setParameter("p2", metaId);
        
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Indicador> findAll(String leyenda, Long idOrg) {
        if (idOrg==0L){ return em.createQuery("select object(o) FROM Indicador as o WHERE o.estado = true AND o.leyenda LIKE '" + leyenda + "%' ORDER BY o.leyenda").getResultList();}
        else {
              return em.createQuery("select object(o) FROM Indicador as o WHERE o.estado = true  AND o.metas.objetivos.organismo.id = " +idOrg+ " AND o.leyenda LIKE '" + leyenda + "%' ORDER BY o.leyenda").getResultList();}
    }
    
    @Override
    public void create(Long idMeta,String Descripcion, String leyenda, String Formula,String[] variableIndicador,String[] serie,String[] valores,int serieSize,String kml) throws Exception {
        
        try {            
            Metas metaAux = this.metasFacade.find(idMeta);
            Indicador indicadorAux = new Indicador();
            indicadorAux.setLeyenda(leyenda.toUpperCase());
            indicadorAux.setDescripcion(Descripcion.toUpperCase());
            indicadorAux.setFormula(Formula.toUpperCase());
            indicadorAux.setEstado(true);
            indicadorAux.setMetas(metaAux);
            indicadorAux.setVariableIndicador(variableIndicador);
            indicadorAux.setSerie(serie);
            indicadorAux.setValores(valores);
            indicadorAux.setSizeSerie(serieSize);
            indicadorAux.setKml(kml);
            this.create(indicadorAux);
            metaAux.getIndicador().add(indicadorAux);
            this.metasFacade.edit(metaAux);
            
            
        } catch (Exception ex) {
            throw new Exception("Error al intentar crear el indicador");
        }
    }
    
    @Override
    public void edit(Long id,String Descripcion, String leyenda, String Formula,String[] variableIndicador,String[] serie,String[] valores,int serieSize, String kml) throws Exception {
        try {
            Indicador indicadorAux = this.find(id);
            indicadorAux.setLeyenda(leyenda.toUpperCase());
            indicadorAux.setDescripcion(Descripcion.toUpperCase());
            indicadorAux.setFormula(Formula.toUpperCase());
            indicadorAux.setVariableIndicador(variableIndicador);
            indicadorAux.setSerie(serie);
            indicadorAux.setValores(valores);
            indicadorAux.setSizeSerie(serieSize);
            indicadorAux.setKml(kml);
            this.edit(indicadorAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la indicador");
        }
    }
    
    @Override
    public void remove(Long id) throws Exception {
        try {
            Indicador indicadorAux = this.find(id);
            indicadorAux.setEstado(false);
            this.edit(indicadorAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar la indicador");
        }
    }
    
}
    


