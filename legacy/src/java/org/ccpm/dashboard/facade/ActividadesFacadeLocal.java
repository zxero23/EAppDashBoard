/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import javax.ejb.Local;
import org.ccpm.dashboard.entity.Actividades;

/**
 *
 * @author Alvarenga Angel
 */
@Local
public interface ActividadesFacadeLocal {

    void create(Actividades dependencia);

    void edit(Actividades dependencia);

    void remove(Actividades dependencia);

    Actividades find(Object id);

    List<Actividades> findAll();

    List<Actividades> findRange(int[] range);

    int count();

    public List<Actividades> findAll(boolean estado);

    public List<Actividades> findAll(String nombre);

    public void create(Long idOrganismo, String nombre) throws Exception;

    public void edit(Long id, String nombre) throws Exception;

    public void remove(Long id) throws Exception;
    
}

