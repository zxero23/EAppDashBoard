package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.ActionItem;

@Local
public interface ActionItemFacadeLocal {

    void create(ActionItem actionItem);

    void edit(ActionItem actionItem);

    void remove(ActionItem actionItem);

    ActionItem find(Object id);

    List<ActionItem> findAll();

    List<ActionItem> findRange(int[] range);

    int count();

    public java.util.List<org.ccpm.sysse.entity.ActionItem> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.ActionItem> findAll(java.lang.String nombreAccion);

    public java.util.List<org.ccpm.sysse.entity.ActionItem> findAll(java.lang.Long idMenu);

    public java.util.List<org.ccpm.sysse.entity.ActionItem> findAll(java.util.List<java.lang.Long> actionsId);

    public void create(java.lang.String nombre) throws java.lang.Exception;

    public void remove(java.lang.Long idAccion) throws java.lang.Exception;

    public void edit(java.lang.Long idAccion, java.lang.String nombreAccion) throws java.lang.Exception;
    
}
