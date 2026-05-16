package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Menu;

@Local
public interface MenuFacadeLocal {

    void create(Menu menu);

    void edit(Menu menu);

    void remove(Menu menu);

    Menu find(Object id);

    List<Menu> findAll();

    List<Menu> findRange(int[] range);

    int count();

    public java.util.List<org.ccpm.sysse.entity.Menu> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.Menu> findAll(java.lang.String nombreMenu);

    public java.util.List<org.ccpm.sysse.entity.Menu> findAll(java.util.List<java.lang.Long> menuId);

    public java.util.List<org.ccpm.sysse.entity.Menu> findAll(java.lang.Long id);

    public java.util.List<org.ccpm.sysse.entity.Menu> findAllMenuPadre();

    public void remove(java.lang.Long idMenu) throws java.lang.Exception;

    public void create(java.lang.Long idMenuPadre, java.lang.String nombre, java.lang.String link) throws java.lang.Exception;

    public void edit(java.lang.Long id, java.lang.Long idSeleccion, java.lang.String nombre, java.lang.String link) throws java.lang.Exception;
    
}
