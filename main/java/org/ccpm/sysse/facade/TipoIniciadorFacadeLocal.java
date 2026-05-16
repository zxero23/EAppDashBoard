package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.TipoIniciador;

@Local
public interface TipoIniciadorFacadeLocal {

    void create(TipoIniciador tipoIniciador);

    void edit(TipoIniciador tipoIniciador);

    void remove(TipoIniciador tipoIniciador);

    TipoIniciador find(Object id);

    List<TipoIniciador> findAll();

    List<TipoIniciador> findRange(int[] range);

    int count();

    public java.util.List<org.ccpm.sysse.entity.TipoIniciador> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.TipoIniciador> findAll(java.lang.String nombreAccion);

    public void create(java.lang.String nombre, java.lang.Boolean externo) throws java.lang.Exception;

    public void remove(java.lang.Long idTipoIniciador) throws java.lang.Exception;

    public void edit(java.lang.Long idTipoIniciador, java.lang.String nombre, java.lang.Boolean externo) throws java.lang.Exception;
    
}
