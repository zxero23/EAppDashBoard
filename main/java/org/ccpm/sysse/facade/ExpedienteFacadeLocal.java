package org.ccpm.sysse.facade;

import java.util.Date;
import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Expediente;
import org.ccpm.sysse.entity.InstrumentoLegal;

@Local
public interface ExpedienteFacadeLocal {

    void create(Expediente expediente);

    void edit(Expediente expediente);

    void remove(Expediente expediente);

    Expediente find(Object id);

    List<Expediente> findAll();

    List<Expediente> findRange(int[] range);

    int count();
   
    public List<Expediente> findAll(boolean estado);

    public Expediente findExpedienteByInstrumentoLegal(Long idInstrumento);

    public List<Expediente> findAll(Long idMesa);
    
    public List<Expediente> findAll(Long idMesa, int first, int pageSize);

    public Long countEnvidas(Long idMesa, Long idEstado);

    public Long countRecibidas(Long idMesa, Long idEstado);

    public Long countAceptadas(Long idMesa, Long idEstado);

    public Long countArchivada(Long idMesa, Long idEstado);

    public Long countRechazada(Long idMesa, Long idEstado);

    public Long countNotas(Long idMesa);

    public Long findUltimoNro(Long codOrg);
    
    public Long findUltimoNroPorAnio(Long codOrg, Integer anio);
    
    public Expediente create(Date fecha, int cuerpo,  int fojas, Long idIniciador, Long idTipoAsunto, String asunto, String obs, String ordenPago, Integer  pedidoFondo, Integer  pedidoFondoAnio, Integer  ordenEntrega, Integer ordenEntregaAnio, String expMadre, Long idOrigen, Long idDestino, Long idUsuario) throws Exception;
    
    public Expediente create(Date fecha, Long organismo, Long correl, int anio, int cuerpo,  int fojas, Long idIniciador, Long idTipoAsunto, String asunto, String obs, String ordenPago, Integer  pedidoFondo, Integer  pedidoFondoAnio, Integer  ordenEntrega, Integer ordenEntregaAnio, String expMadre, Long idOrigen, Long idDestino, Long idUsuario) throws Exception;
    
    public Expediente find(Long codOrganismo, Long numCorrelativo, int anio);

    public void edit(Long codOrganismo, Long numCorrelativo, int anio, Date fechaCreacion, Integer cuerpo, Integer fojas, Long idIniciador, Long idTipoAsunto, String asunto, String obs, String ordenPago, Integer  pedidoFondo, Integer  pedidoFondoAnio, Integer  ordenEntrega, Integer ordenEntregaAnio, String expMadre, Long idDestino, Long idUsuario) throws Exception;
    
    public void edit(Long codOrganismo, Long numCorrelativo, int anio, String obs,  Long idDestino, Long idUsuario) throws Exception;

    public void aceptarExpediente(Long codigoOrgani, Long nroCorrel, int annio, Long idUsuario, Integer flagBandeja) throws Exception;

    public void rechazarExpediente(Long codigoOrgani, Long nroCorrel, int annio, String obs, Long idUsuario) throws Exception;

    public void reenviarExpediente(Long codigoOrgani, Long nroCorrel, int annio, int fojas, String obs, Long idMesaOrigen, Long idMesaDestino, Long idUsuario) throws Exception;

    public void archivarExpediente(Long codigoOrgani, Long nroCorrel, int annio, String obs, String motivo, Long idUsuario) throws Exception;

    public void enviarExpediente(Long codigoOrgani, Long nroCorrel, int annio, int fojas, String obs, Long idMesaOrigen, Long idMesaDestino, String ordenPago, Integer  pedidoFondo, Integer  pedidoFondoAnio, Integer  ordenEntrega, Integer ordenEntregaAnio,String expMadre, Long idUsuario) throws Exception;
    
    public void enviarExpediente(Expediente exp, String obs, Long idMesaDestino, Long idUsuario) throws Exception;

    public void agregarInstLeg(Long codigoOrgani, Long nroCorrel, int annio, InstrumentoLegal instLeg, Long idUsuario) throws Exception;

    public void quitarInstLeg(Long codigoOrgani, Long nroCorrel, int annio, InstrumentoLegal instLeg, Long idUsuario) throws Exception;
    
    public void agregarHistorial (Expediente expediente, Long id, int fojas, int fojasOld, String archivo, String archivoOld, Long idUsuario);

   
    public boolean findAll(Date fechaMov, Date hora, Long idMesa, Long idDestino, Long idUsuario);
    
    public boolean findAll(Integer estado, Date fechaDesde, Date fechaHasta, Long origen, Long destino);
    
    public List<Expediente> findAll(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, Long idIniciador);

    public String createExterno(Long codOrganismo, Long nroCorrel, int annio, Long idMesaDestino, int fojas, Long idUsuario, boolean ingresarSiempre) throws Exception;

    public void eliminarExpediente(Long codOrganismo, Long numCorrelativo, int anio, Long id) throws Exception;

    public List<Expediente> findAllByEstado(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, String expMadre, Long idMesaDestino,Long idMesaOrigen, Long idEstado);

    public List<Expediente> findAllByEstado(Long idMesaDestino,Long idMesaOrigen, Long idEstado);

    public List<Expediente> findAllByEstado(Long idMesaDestino, Long idMesaOrigen, Long idEstado, int first, int pageSize);
    
    public Long countByEstado(Long idMesaDestino, Long idMesaOrigen, Long idEstado);

    public void aceptarReenviarExpediente(Long codigoOrgani, Long nroCorrel, int annio, String observacion, Long idMesaDestino, Long idUsuario) throws Exception;
    
    // Buqueda Avanzada
   
    public List<Expediente> findAll(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, Long idTipoAsunto, String asunto, String ordenPago, Integer pedidoFondo, 
            Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, String expMadre, Long idMesaUsuario,
            boolean consultarHistorial,String observacion, int first, int pageSize);

    public Long countAll(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, 
            Long idMesaOrigen, Long idMesaDestino, Long idTipoAsunto, String asunto, String ordenPago, Integer pedidoFondo,
            Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, String expMadre, Long idMesaUsuario,
            boolean consultarHistorial, String observacion);

    // Métodos de conteo nativos para cada bandeja
    public Long countEnviadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen);
    
    public Long countRecibidosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen);
    
    public Long countAceptadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen);
    
    public Long countRechazadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen);
    
    public Long countArchivadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen);

    // Métodos nativos con paginación para cada bandeja
    public List<Expediente> findEnviadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize);
    
    public List<Expediente> findRecibidosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize);
    
    public List<Expediente> findAceptadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize);
    
    public List<Expediente> findRechazadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize);
    
    public List<Expediente> findArchivadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta, 
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, 
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, 
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize);

    public Long countAllAvanzada(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Long idIniciador, Long idMesaOrigen, Long idMesaDestino, Long idTipoAsunto, String asunto, 
            String ordenPago, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega,
            Integer ordenEntregaAnio, String expMadre, Long idMesaUsuario, boolean consultarHistorial, String observacion);

    public List<Expediente> findAllAvanzada(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Long idIniciador, Long idMesaOrigen, Long idMesaDestino, Long idTipoAsunto, String asunto,
            String ordenPago, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaUsuario, boolean consultarHistorial, String observacion, int first, int pageSize);

}
