/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import javax.ejb.Local;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Menu;

/**
 *
 * @author Alvarenga Angel
 */
@Local
public interface GrupoFacadeLocal {

    void create(Grupo grupo);

    void edit(Grupo grupo);

    void remove(Grupo grupo);

    Grupo find(Object id);

    List<Grupo> findAll();

    List<Grupo> findRange(int[] range);

    int count();

    public List<Grupo> findGrupos(List<Long> gruposId);

    public List<Menu> findAllMenu(Long idGrupo);

    public List<Grupo> findAll(boolean estado);

    public List<Grupo> findAll(String nombreGrupo);

    public void remove(Long idGrupo) throws Exception;

    public void remove(Long idGrupo, List<Long> idAcciones) throws Exception;

    public void create(String nombreGrupo, String descripcionGrupo) throws Exception;

    public void edit(Long idGrupo, String nombreGrupo, String descripcionGrupo) throws Exception;

    public void edit(Long idGrupo, List<Long> idMenu) throws Exception;
    
}
