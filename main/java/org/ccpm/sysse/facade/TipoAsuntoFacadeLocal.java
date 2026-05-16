package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.TipoAsunto;

@Local
public interface TipoAsuntoFacadeLocal {

    void create(TipoAsunto tipoAsunto);

    void edit(TipoAsunto tipoAsunto);

    void remove(TipoAsunto tipoAsunto);

    TipoAsunto find(Object id);

    List<TipoAsunto> findAll();

    List<TipoAsunto> findRange(int[] range);

    int count();

    public java.util.List<org.ccpm.sysse.entity.TipoAsunto> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.TipoAsunto> findAll(java.lang.String nombreAccion);

    public void create(java.lang.String nombre, java.lang.String descripcion) throws java.lang.Exception;

    public void remove(java.lang.Long idTipoInstLegal) throws java.lang.Exception;

    public void edit(java.lang.Long idTipoInstLegal, java.lang.String nombre, java.lang.String descripcion) throws java.lang.Exception;
    
}
