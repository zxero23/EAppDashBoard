package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Leyenda;

@Local
public interface LeyendaFacadeLocal {

    void create(Leyenda leyenda);

    void edit(Leyenda leyenda);

    void remove(Leyenda leyenda);

    Leyenda find(Object id);
    
    public java.util.List<org.ccpm.sysse.entity.Leyenda> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.Leyenda> findAll(java.lang.Long anio, java.lang.String leyenda);

    public void create(java.lang.Long anio, java.lang.String leyenda) throws java.lang.Exception;

    public void remove(java.lang.Long id) throws java.lang.Exception;

    public void edit(java.lang.Long id, java.lang.Long anioNuevo, java.lang.String leyenda) throws java.lang.Exception;

    List<Leyenda> findRange(int[] range);

    int count();
    
}
