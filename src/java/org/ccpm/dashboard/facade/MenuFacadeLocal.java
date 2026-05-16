/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import javax.ejb.Local;
import org.ccpm.dashboard.entity.Menu;

/**
 *
 * @author Angel
 */
@Local
public interface MenuFacadeLocal {

    void create(Menu menu);

    void edit(Menu menu);

    void remove(Menu menu);

    Menu find(Object id);

    List<Menu> findAll();

    List<Menu> findRange(int[] range);

    int count();

    public List<Menu> findAll(boolean estado);

    public List<Menu> findAll(String nombreMenu);

    public List<Menu> findAll(List<Long> menuId);

    public List<Menu> findAll(Long id);

    public List<Menu> findAllMenu();

    public void create(Long idMenuPadre, String nombre) throws Exception;

    public void remove(Long idMenu) throws Exception;

    public void edit(Long id, Long idSeleccion, String nombre) throws Exception;
    
}
