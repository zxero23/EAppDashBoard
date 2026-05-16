package org.ccpm.sysse.facade;

import java.util.Date;
import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Nota;

@Local
public interface NotaFacadeLocal {

    void create(Nota nota);

    void edit(Nota nota);

    void remove(Nota nota);

    Nota find(Object id);

    List<Nota> findAll();

    List<Nota> findRange(int[] range);

    int count();

    public Nota create(String nroNota, Date fechaCreacion, String referencia, Integer fojas, Long idIniciador, Long idTipoAsunto, String observacion, Long idMesaDestino, Long idMesaOrigen, String archivo, Long idUsuario) throws Exception;

    public Long countNotas(Long idMesa);

    public Long countEnvidas(Long idMesa, Long idEstado);

    public Long countRecibidas(Long idMesa, Long idEstado);

    public Long countAceptadas(Long idMesa, Long idEstado);

    public Long countArchivada(Long idMesa, Long idEstado);

    public Long countRechazada(Long idMesa, Long idEstado);

    public List<Nota> findAll(boolean estado);

    public void ingresar(Long idNota, Long idMesaDestinoAux, Long idUsuario) throws Exception;

    public void edit(Long idNota, String nro, Date fechaCreacion, String referencia, Integer fojas, Long idIniciador, Long idTipoAsunto, String obs, Long idMesaDestino, String archivo) throws Exception;
    
     public void edit(Long idNota, String obs, Long idMesaDestino, Long idUsuario) throws Exception;

    public void aceptarNota(Long idNota, Long idUsuario, Integer flagBandeja) throws Exception;

    public void archivarNota(Long idNota, String obs, String motivo, Long idUsuario) throws Exception;

    public void enviarNota(Long idNota, String obs, Long idMesaOrigen, Long idMesaDestino, Long idUsuario) throws Exception;

    public void enviarNota(Long idNota, String obs, Long idMesaDestino, Long idUsuario) throws Exception;

    public void rechazarNota(Long idNota, String obsRechazo, Long idUsuario) throws Exception;

    public void reenviarNota(Long idNota, String obs, Long idMesaOrigen, Long idMesaDestino, Long idUsuario) throws Exception;

    public List<Nota> findAll(Long idNota, String nroNota, String referencia, Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador, Long idMesa);

    public boolean findAll(Integer estado, Date fechaDesde, Date fechaHasta, Long origen, Long destino);

    public List<Nota> findAllEnviadas(Long idNota, String nroNota, String referencia, Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador, Long idMesaDestino, Long idMesaOrigen);

    public List<Nota> findAllRecibidas(Long idNota, String nroNota, String referencia, Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador, Long idMesaOrigen, Long idMesaDestino);

    public List<Nota> findAllAceptadas(Long idNota, String nroNota, String referencia, Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador, Long idMesaOrigen, Long idMesaDestino);

    public List<Nota> findAllArchivadas(Long idNota, String nroNota, String referencia, Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador, Long idMesaOrigen, Long idMesaDestino);

    public List<Nota> findAllRechazadas(Long idNota, String nroNota, String referencia, Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador, Long idMesaDestino, Long idMesaOrigen);

    public List<Nota> findAllEnviadas(Long idMesa);
    
    public List<Nota> findAllEnviadas(Long idMesa, int first, int pageSize);

    public List<Nota> findAllRecibidas(Long idMesa);
    
    public List<Nota> findAllRecibidas(Long idMesa, int first, int pageSize);

    public List<Nota> findAllAceptadas(Long idMesa);
    
    public List<Nota> findAllAceptadas(Long idMesa, int first, int pageSize);

    public List<Nota> findAllArchivadas(Long idMesa);
    
    public List<Nota> findAllArchivadas(Long idMesa, int first, int pageSize);

    public List<Nota> findAllRechazadas(Long idMesa);
    
    public List<Nota> findAllRechazadas(Long idMesa, int first, int pageSize);
    
    public List<Nota> findAllByEstado(Long idMesaDestino, Long idMesaOrigen, Long idEstado, int first, int pageSize);
    
    public Long countByEstado(Long idMesaDestino, Long idMesaOrigen, Long idEstado);

    public List<Nota> findAll(Long idMesa);
    
    public List<Nota> findAll(Long idMesa, int first, int pageSize);

    public boolean findAll(Date fechaMov, Date horaEnvio, Long idMesa, Long idDestino, Long idUsuario);

    public Nota find(Long idNota);

    public void aceptarReenviarNota(Long idNota, String obs, Long idMesaOrigen, Long idMesaDestino, Long idUsuario) throws Exception;

    public void remove(Nota nota, Long idUsuario);
    
    public List<Nota> findAll(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador, 
            Long idMesaOrigen, Long idMesadestino, boolean consultarHistorial,
            String observacion);
    
    public List<Nota> findAll(Long idNota, String nroNota, String referencia, 
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesadestino, boolean consultarHistorial,
            String observacion, int first, int pageSize);

    public Long countAll(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, boolean consultarHistorial,
            String observacion);

    public List<Nota> findAllAvanzada(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, boolean consultarHistorial,
            String observacion, int first, int pageSize);

    public Long countAllAvanzada(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, boolean consultarHistorial, String observacion);

}
