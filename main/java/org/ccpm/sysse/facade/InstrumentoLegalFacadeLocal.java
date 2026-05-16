package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.InstrumentoLegal;

@Local
public interface InstrumentoLegalFacadeLocal {

    void create(InstrumentoLegal instrumentoLegal);

    void edit(InstrumentoLegal instrumentoLegal);

    void remove(InstrumentoLegal instrumentoLegal);

    InstrumentoLegal find(Object id);

    List<InstrumentoLegal> findAll();

    List<InstrumentoLegal> findRange(int[] range);

    int count();

    public java.util.List<org.ccpm.sysse.entity.InstrumentoLegal> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.InstrumentoLegal> findAll(java.lang.String descripcion);
    
    public java.util.List<org.ccpm.sysse.entity.InstrumentoLegal> findAll(java.lang.String descripcion, java.lang.Long codigo, java.lang.Long correlativo, java.lang.Integer anio);

    public InstrumentoLegal create(java.lang.String nro, java.lang.String descripcion, int fojas, java.lang.Long idTipoInstrumentoLegal, java.lang.String archivo, java.lang.Long idUsuario) throws java.lang.Exception;

    public void edit(java.lang.Long id, java.lang.String nro, java.lang.String descripcion, int foja, java.lang.Long idTipoInstrumentoLegal, java.lang.String archivo) throws java.lang.Exception;

    public void remove(java.lang.Long id) throws java.lang.Exception;

    public java.util.List<org.ccpm.sysse.entity.InstrumentoLegal> findAll(java.lang.String descripcion, java.lang.Long idUsuario);

    public java.util.List<org.ccpm.sysse.entity.InstrumentoLegal> findAll(java.lang.Long idUsuario);
    
    public java.util.List<org.ccpm.sysse.entity.InstrumentoLegal> findAllSinAgregar(java.lang.Long idUsuario);

    public java.util.List<org.ccpm.sysse.entity.InstrumentoLegal> findAll(java.util.List<java.lang.String> instLegalId);

      
    
}
