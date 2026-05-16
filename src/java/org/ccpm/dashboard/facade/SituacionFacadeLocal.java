/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.dashboard.entity.Situacion;

/**
 *
 * @author Alvarenga Angel
 */
@Local
public interface SituacionFacadeLocal {

    void create(Situacion dependencia);

    void edit(Situacion dependencia);

    void remove(Situacion dependencia);

    Situacion find(Object id);

    List<Situacion> findAll();
    
    public List <Situacion> findAllnombre(String leyenda) ;

    List<Situacion> findRange(int[] range);

    int count();

    public List<Situacion> findAll(boolean estado);

    public List<Situacion> findAll(String nombre);

    public void create(Long idOrganismo, String nombre) throws Exception;

    public void edit(Long id, String nombre) throws Exception;

    public void remove(Long id) throws Exception;
    
}
