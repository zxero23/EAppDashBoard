/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import javax.ejb.Local;
import org.ccpm.dashboard.entity.Objetivos;

/**
 *
 * @author Alvarenga Angel
 */
@Local
public interface ObjetivosFacadeLocal {

    void create(Objetivos dependencia);

    void edit(Objetivos dependencia);

    void remove(Objetivos dependencia);

    Objetivos find(Object id);

    List<Objetivos> findAll();
    
     
     
     public List <Objetivos> findAll(Long idOrganismo,String leyenda); //metodo busqueda por organismo y nombre o por c/u
     
     public List <Objetivos> findAll(boolean estado, long idOrg);

    List<Objetivos> findRange(int[] range);

    int count();

    public List<Objetivos> findAll(boolean estado);

    public List<Objetivos> findAll(String leyenda);
    

    public void create(Long idOrganismo,Long idSituacion, String leyenda, String descripcion) throws Exception;

    public void edit(Long idOrganismo,Long idSituacion,Long id, String leyenda,String descripcion) throws Exception;

    public void remove(Long id) throws Exception;
    
    public Integer totalObjetivosPorEstado(Long idOrganismo,int estado);
    
    public List <Objetivos> findAll(Long idOrganismo,int estado);
    
}
