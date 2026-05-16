package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.TipoInstrumentoLegal;

@Local
public interface TipoInstrumentoLegalFacadeLocal {

    void create(TipoInstrumentoLegal tipoInstrumentoLegal);

    void edit(TipoInstrumentoLegal tipoInstrumentoLegal);

    void remove(TipoInstrumentoLegal tipoInstrumentoLegal);

    TipoInstrumentoLegal find(Object id);

    List<TipoInstrumentoLegal> findAll();

    List<TipoInstrumentoLegal> findRange(int[] range);

    int count();
    
    public java.util.List<org.ccpm.sysse.entity.TipoInstrumentoLegal> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.TipoInstrumentoLegal> findAll(java.lang.String nombreAccion);

    public void create(java.lang.String nombre, java.lang.String descripcion) throws java.lang.Exception;

    public void remove(java.lang.Long idTipoInstLegal) throws java.lang.Exception;

    public void edit(java.lang.Long idTipoInstLegal, java.lang.String nombre, java.lang.String descripcion) throws java.lang.Exception;
    
}
