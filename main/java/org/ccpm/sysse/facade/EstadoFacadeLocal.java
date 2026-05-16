package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Estado;

@Local
public interface EstadoFacadeLocal {

    void create(Estado estado);

    void edit(Estado estado);

    void remove(Estado estado);

    Estado find(Object id);

    List<Estado> findAll();

    List<Estado> findRange(int[] range);

    int count();

    public java.util.List<org.ccpm.sysse.entity.Estado> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.Estado> findAll(java.lang.String nombre);

    public void create(java.lang.String nombre, java.lang.String descripcion) throws java.lang.Exception;

    public void remove(java.lang.Long idEstado) throws java.lang.Exception;

    public void edit(java.lang.Long idEstado, java.lang.String nombre, java.lang.String descripcion) throws java.lang.Exception;
    
}
