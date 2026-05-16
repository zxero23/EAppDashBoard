/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.Date;
import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.dashboard.entity.Metas;

/**
 *
 * @author Facundo González
 */
@Local
public interface MetasFacadeLocal {

    void create(Metas meta);

    void edit(Metas meta);

    void remove(Metas meta);

    Metas find(Object id);

    List<Metas> findAll();
    
    List<Metas> findRange(int[] range);

    int count();

    public List<Metas> findAll(boolean estado);

    public List<Metas> findAll(String nombre);
    
    public List <Metas> findAll(Long idObjetivo,Long idOrganismo,String leyenda);

    public void create(Long idObjetivos, Long idSituacion,String nombre,int cantidad,Date fechainicio,Date fechafinal,String descripcion, boolean reduccion) throws Exception;

    public void edit(Long idObjetivos, Long idSituacion,Long id, String leyenda,int cantidad,Date fechainicio,Date fechafinal,String descripcion, boolean reduccion) throws Exception;

    public void remove(Long id) throws Exception;
    
    public Integer totalMetasPorEstado(Long idObjetivo,int estado); //cuenta el total de metas por estado
    
    public List <Metas> findAll(Long idObjetivo,int estado); //Lista las metas por estado, 
    
    public Integer totalMetasPorEstadoOrg(Long idOrg,int estado);
    
    public List <Metas> findAllMetas(Long idOrg,int estado); //cambios domingo 6 de nov, metas por organismo
    
}
