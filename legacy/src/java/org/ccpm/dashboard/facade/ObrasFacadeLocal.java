/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.Date;
import java.util.List;
import javax.ejb.Local;
import org.ccpm.dashboard.entity.Obras;
import org.ccpm.dashboard.entity.Situacion;

/**
 *
 * @author Alvarenga Angel
 */
@Local
public interface ObrasFacadeLocal {

    void create(Obras dependencia);

    void edit(Obras dependencia);

    void remove(Obras dependencia);

    Obras find(Object id);

    List<Obras> findAll();
    
    public List <Situacion> findAllSituacion(int id);
    
    public List <Obras> findAllnombre(String leyenda);

    List<Obras> findRange(int[] range);

    int count();

    public List<Obras> findAll(boolean estado, Long idOrg);
    
    public List <Obras> findAll(boolean estado);

    public List<Obras> findAll(String nombre);
    
     public List <Obras> findAll(Long situacion,String leyenda,Long idOrganismo,Long idLocalidad);

    public void create(Long idSituacion, String leyenda,float latitud,float longitud,Long idOrganismo,Date FechaInicio,Date FechaFinal,String adjunto, String[] fotos,Long idLocalidad) throws Exception;

    public void edit(Long idSituacion,Long id, String leyenda,float latitud,float longitud,Long idOrganismo,Date FechaInicio,Date FechaFinal,String adjunto, String[] fotos,Long idLocalidad) throws Exception;

    public void remove(Long id) throws Exception;
    
    public Integer totalObrasPorEstado(Long idOrganismo,int estado);
    
    public List<Obras> findAll(Long idOrganismo,int estado);
    
}
