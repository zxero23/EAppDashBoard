package org.ccpm.sysse.facade;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.utilidad.Utilidad;
import org.modelmapper.ModelMapper;

@Stateless
public class NotaFacade extends AbstractFacade<Nota> implements NotaFacadeLocal {

    private static final Logger LOGGER = Logger.getLogger(NotaFacade.class.getName());

    @Resource
    private SessionContext sessionContext;

    // ==========================================
    // CONSTANTES DE ESTADO (Reemplaza Magic Numbers)
    // ==========================================
    public static class EstadoNota {
        public static final Long BORRADOR = 1L;
        public static final Long ENVIADO = 2L;
        public static final Long ACEPTADO = 3L;
        public static final Long RECHAZADO = 4L;
        public static final Long ARCHIVADO = 5L;
        public static final Long REENVIADO = 7L;

        private EstadoNota() {
        }
    }

    // ==========================================
    // CONFIGURACIÓN
    // ==========================================
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private TipoAsuntoFacadeLocal tipoAsuntoFacade;
    @EJB
    private EstadoFacadeLocal estadoFacade;
    @EJB
    private IniciadorFacadeLocal iniciadorFacade;
    @EJB
    private MesaFacadeLocal mesaFacade;
    @EJB
    private HistorialNotaFacadeLocal historialFacade;
    @EJB
    private NotaEliminadaFacadeLocal notaEliminadaFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public NotaFacade() {
        super(Nota.class);
    }

    // ==========================================
    // MÉTODOS AUXILIARES TRANSACCIONALES / VALIDACIÓN
    // ==========================================
    private void markRollbackOnly() {
        try {
            if (sessionContext != null) {
                sessionContext.setRollbackOnly();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "No se pudo marcar rollbackOnly", ex);
        }
    }

    private EJBException rollbackBusiness(String message) {
        markRollbackOnly();
        return new EJBException(message);
    }

    private EJBException rollbackBusiness(String message, Throwable cause) {
        markRollbackOnly();
        EJBException exception = new EJBException(message);
        if (cause != null) {
            exception.initCause(cause);
        }
        return exception;
    }

    private <T> T requireEntity(T entity, String message) {
        if (entity == null) {
            throw rollbackBusiness(message);
        }
        return entity;
    }

    private Nota findActiveNotaOrThrow(Long idNota) {
        Nota nota = super.find(idNota);
        if (nota == null || !nota.isEstado()) {
            throw rollbackBusiness("Nota no encontrada: " + idNota);
        }
        return nota;
    }

    private HistorialNota buildHistorial(Nota nota, String observacion, Usuario usuario,
            Iniciador iniciador, Mesa mesaOrigen, Mesa mesaDestino, Estado estado) {
        HistorialNota historial = new HistorialNota();
        historial.setFecha(new Date());
        historial.setObservacion(observacion);
        historial.setUsuario(usuario);
        historial.setIniciador(iniciador != null ? iniciador : nota.getIniciador());
        historial.setMesaOrigen(mesaOrigen);
        historial.setMesaDestino(mesaDestino);
        historial.setEstadoNota(estado);
        historial.setEstado(true);
        return historial;
    }

    private void persistHistorialAndNota(Nota nota, HistorialNota historial) {
        historialFacade.create(historial);
        nota.getListaHistorial().add(historial);
        nota.setUltimoMovimiento(new Date());
        super.edit(nota);
    }

    private void enviarNotaInternal(Nota nota, String obs, Long idMesaOrigen,
            Long idMesaDestino, Long idUsuario) {
        Estado estadoAux = requireEntity(estadoFacade.find(EstadoNota.ENVIADO),
                "Estado ENVIADO no encontrado");
        Mesa mesaDestinoAux = requireEntity(mesaFacade.find(idMesaDestino),
                "Mesa destino no encontrada: " + idMesaDestino);
        Mesa mesaOrigenAux = requireEntity(mesaFacade.find(idMesaOrigen),
                "Mesa origen no encontrada: " + idMesaOrigen);
        Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                "Usuario no encontrado: " + idUsuario);

        nota.setEstadoNota(estadoAux);
        nota.setMesaOrigen(mesaOrigenAux);
        nota.setMesaDestino(mesaDestinoAux);
        nota.setUsuario(usuarioAux);
        nota.setObservacion(obs != null ? obs.toUpperCase() : null);

        HistorialNota historial = buildHistorial(
                nota,
                obs != null ? obs.toUpperCase() : null,
                usuarioAux,
                nota.getIniciador(),
                nota.getMesaOrigen(),
                nota.getMesaDestino(),
                estadoAux);

        persistHistorialAndNota(nota, historial);
    }

    // ==========================================
    // MÉTODOS AUXILIARES SEGUROS (Query Builder)
    // ==========================================

    private static class QueryBuilder {
        private final StringBuilder jpql = new StringBuilder();
        private final Map<String, Object> parameters = new HashMap<>();

        QueryBuilder(String selectClause) {
            jpql.append(selectClause);
        }

        QueryBuilder append(String fragment) {
            jpql.append(fragment);
            return this;
        }

        QueryBuilder addParameter(String name, Object value) {
            parameters.put(name, value);
            return this;
        }

        QueryBuilder addDateRange(String field, Date desde, Date hasta) {
            if (desde != null && hasta != null) {
                Timestamp fDesde = new Timestamp(desde.getTime());
                Timestamp fHasta = new Timestamp(hasta.getTime());

                if (desde.equals(hasta)) {
                Calendar cal = Calendar.getInstance();
                    cal.setTime(hasta);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                fHasta.setTime(cal.getTime().getTime());
            }
                parameters.put("fDesde", fDesde);
                parameters.put("fHasta", fHasta);
                jpql.append(" AND ").append(field).append(" BETWEEN :fDesde AND :fHasta");
            } else if (desde != null) {
                parameters.put("fDesde", new Timestamp(desde.getTime()));
                jpql.append(" AND ").append(field).append(" >= :fDesde");
            } else if (hasta != null) {
                parameters.put("fHasta", new Timestamp(hasta.getTime()));
                jpql.append(" AND ").append(field).append(" <= :fHasta");
            }
            return this;
        }

        QueryBuilder addNotNullFilter(String field, Long value) {
            if (value != null && value != 0) {
                String paramName = field.replace(".", "_");
                parameters.put(paramName, value);
                jpql.append(" AND ").append(field).append(" = :").append(paramName);
            }
            return this;
        }

        QueryBuilder addStringFilter(String field, String value, boolean desacentuar) {
            if (value != null && !value.isEmpty()) {
                String processedValue = desacentuar ? Utilidad.desacentuar(value).toUpperCase() : value.toUpperCase();
                String paramName = field.replace(".", "_");
                parameters.put(paramName, "%" + processedValue + "%");
                jpql.append(" AND UPPER(").append(field).append(") LIKE :").append(paramName);
            }
            return this;
        }

        String getJpql() {
            return jpql.toString();
        }

        Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        void applyParameters(Query query) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    private QueryBuilder buildCommonFilters(QueryBuilder builder, Long idNota, String nroNota,
            String referencia, Date fechaDesde, Date fechaHasta, Long idTipoAsunto,
            Long idIniciador, String observacion, boolean consultarHistorial,
            Long idMesaOrigen, Long idMesaDestino) {

        builder.addNotNullFilter("o.id", idNota)
                .addStringFilter("o.nroNota", nroNota, true)
                .addStringFilter("o.referencia", referencia, true)
                .addDateRange("o.fechaCreacion", fechaDesde, fechaHasta)
                .addNotNullFilter("o.tipoAsunto.id", idTipoAsunto)
                .addNotNullFilter("o.iniciador.id", idIniciador);

        if (observacion != null && !observacion.isEmpty() && consultarHistorial) {
            String obsProcessed = Utilidad.desacentuar(observacion.toUpperCase()).replace(" ", "%");
            builder.addParameter("observacion", "%" + obsProcessed + "%");
            builder.append(" AND UPPER(h.observacion) LIKE :observacion");
        }

        if (consultarHistorial) {
            builder.addNotNullFilter("h.mesaOrigen.id", idMesaOrigen)
                    .addNotNullFilter("h.mesaDestino.id", idMesaDestino);
        } else {
            builder.addNotNullFilter("o.mesaOrigen.id", idMesaOrigen)
                    .addNotNullFilter("o.mesaDestino.id", idMesaDestino);
        }

        return builder;
    }

    private <T> List<T> executeListQuery(String jpql, Map<String, Object> params,
            Integer firstResult, Integer maxResults, Class<T> resultClass) {
        try {
            TypedQuery<T> query = em.createQuery(jpql, resultClass);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            if (firstResult != null) {
                query.setFirstResult(firstResult);
            }
            if (maxResults != null) {
                query.setMaxResults(maxResults);
            }
            return query.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error ejecutando query de lista: " + jpql, e);
            return new ArrayList<>();
        }
    }

    private Long executeCountQuery(String jpql, Map<String, Object> params) {
        try {
            Query query = em.createQuery(jpql);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            return (Long) query.getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error ejecutando query de conteo: " + jpql, e);
            return 0L;
        }
    }

    // ==========================================
    // MÉTODOS PÚBLICOS DE LECTURA
    // ==========================================

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAll(boolean estado) {
        String jpql = "SELECT o FROM Nota o WHERE o.estado = :p1 ORDER BY o.ultimoMovimiento DESC";
        TypedQuery<Nota> consulta = em.createQuery(jpql, Nota.class);
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAll(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesa) {
        List<Nota> lstResultado = new ArrayList<>();
        lstResultado.addAll(this.findAllEnviadas(idNota, nroNota, referencia,
                fechaDesde, fechaHasta, idTipoAsunto, idIniciador, null, idMesa));
        lstResultado.addAll(this.findAllRecibidas(idNota, nroNota, referencia,
                fechaDesde, fechaHasta, idTipoAsunto, idIniciador, null, idMesa));
        lstResultado.addAll(this.findAllAceptadas(idNota, nroNota, referencia,
                fechaDesde, fechaHasta, idTipoAsunto, idIniciador, null, idMesa));
        lstResultado.addAll(this.findAllRechazadas(idNota, nroNota, referencia,
                fechaDesde, fechaHasta, idTipoAsunto, idIniciador, null, idMesa));
        lstResultado.addAll(this.findAllArchivadas(idNota, nroNota, referencia,
                fechaDesde, fechaHasta, idTipoAsunto, idIniciador, null, idMesa));
        return lstResultado;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAll(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, boolean consultarHistorial,
            String observacion) {
        QueryBuilder builder = new QueryBuilder(
                "SELECT DISTINCT o FROM Nota o JOIN FETCH o.listaHistorial h WHERE o.estado = true");
        builder = buildCommonFilters(builder, idNota, nroNota, referencia, fechaDesde,
                fechaHasta, idTipoAsunto, idIniciador, observacion, consultarHistorial,
                idMesaOrigen, idMesaDestino);
        builder.append(" ORDER BY o.ultimoMovimiento DESC, o.id DESC");
        return executeListQuery(builder.getJpql(), builder.getParameters(), null, 1000, Nota.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAll(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, boolean consultarHistorial,
            String observacion, int first, int pageSize) {
        return findAllAvanzada(idNota, nroNota, referencia, fechaDesde, fechaHasta,
                idTipoAsunto, idIniciador, idMesaOrigen, idMesaDestino,
                consultarHistorial, observacion, first, pageSize);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countAll(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, boolean consultarHistorial,
            String observacion) {
        return countAllAvanzada(idNota, nroNota, referencia, fechaDesde, fechaHasta,
                idTipoAsunto, idIniciador, idMesaOrigen, idMesaDestino,
                consultarHistorial, observacion);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAll(Long idMesa) {
        List<Nota> lstResultado = new ArrayList<>();
        lstResultado.addAll(this.findAllEnviadas(idMesa));
        lstResultado.addAll(this.findAllRecibidas(idMesa));
        lstResultado.addAll(this.findAllAceptadas(idMesa));
        lstResultado.addAll(this.findAllRechazadas(idMesa));
        lstResultado.addAll(this.findAllArchivadas(idMesa));
        return lstResultado;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAll(Long idMesa, int first, int pageSize) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT o FROM Nota o WHERE o.estado = true AND (");
        jpql.append("(o.mesaDestino.id = :idMesa AND o.estadoNota.id = :recibido) OR ")
                .append("(o.mesaOrigen.id = :idMesa AND o.estadoNota.id = :enviado) OR ")
                .append("(o.mesaDestino.id = :idMesa AND o.estadoNota.id = :aceptado) OR ")
                .append("(o.mesaOrigen.id = :idMesa AND o.estadoNota.id = :archivado) OR ")
                .append("(o.mesaDestino.id = :idMesa AND o.estadoNota.id = :rechazado)) ")
                .append("ORDER BY o.ultimoMovimiento DESC, o.id DESC");

        TypedQuery<Nota> consulta = em.createQuery(jpql.toString(), Nota.class);
        consulta.setParameter("idMesa", idMesa);
        consulta.setParameter("recibido", EstadoNota.ENVIADO);
        consulta.setParameter("enviado", EstadoNota.ENVIADO);
        consulta.setParameter("aceptado", EstadoNota.ACEPTADO);
        consulta.setParameter("archivado", EstadoNota.ARCHIVADO);
        consulta.setParameter("rechazado", EstadoNota.RECHAZADO);
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        return consulta.getResultList();
    }

    // ==========================================
    // MÉTODOS POR ESTADO
    // ==========================================

    private List<Nota> findNotasByEstado(Long idEstado, Long idMesaOrigen, Long idMesaDestino,
            Integer first, Integer max) {
        QueryBuilder builder = new QueryBuilder("SELECT DISTINCT o FROM Nota o WHERE o.estado = true");
        builder.addNotNullFilter("o.estadoNota.id", idEstado);
        builder.addNotNullFilter("o.mesaOrigen.id", idMesaOrigen);
        builder.addNotNullFilter("o.mesaDestino.id", idMesaDestino);
        builder.append(" ORDER BY o.ultimoMovimiento DESC, o.id DESC");

        return executeListQuery(builder.getJpql(), builder.getParameters(), first, max, Nota.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Nota> findAllEnviadas(Long idMesa) {
        return findNotasByEstado(EstadoNota.ENVIADO, idMesa, null, null, null);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllEnviadas(Long idMesa, int first, int pageSize) {
        return findNotasByEstado(EstadoNota.ENVIADO, idMesa, null, first, pageSize);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllEnviadas(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaDestino, Long idMesaOrigen) {

        QueryBuilder builder = new QueryBuilder("SELECT o FROM Nota o WHERE o.estado = true");
        builder.addNotNullFilter("o.estadoNota.id", EstadoNota.ENVIADO);
        builder = buildCommonFilters(builder, idNota, nroNota, referencia, fechaDesde,
                fechaHasta, idTipoAsunto, idIniciador, null, false, idMesaOrigen, idMesaDestino);
        builder.append(" ORDER BY o.ultimoMovimiento DESC, o.id DESC");

        return executeListQuery(builder.getJpql(), builder.getParameters(), null, 1000, Nota.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllRecibidas(Long idMesa) {
        return findNotasByEstado(EstadoNota.ENVIADO, null, idMesa, null, null);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllRecibidas(Long idMesa, int first, int pageSize) {
        return findNotasByEstado(EstadoNota.ENVIADO, null, idMesa, first, pageSize);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllRecibidas(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino) {

        QueryBuilder builder = new QueryBuilder("SELECT o FROM Nota o WHERE o.estado = true");
        builder.addNotNullFilter("o.estadoNota.id", EstadoNota.ENVIADO);
        builder = buildCommonFilters(builder, idNota, nroNota, referencia, fechaDesde,
                fechaHasta, idTipoAsunto, idIniciador, null, false, idMesaOrigen, idMesaDestino);
        builder.append(" ORDER BY o.ultimoMovimiento DESC, o.id DESC");

        return executeListQuery(builder.getJpql(), builder.getParameters(), null, 1000, Nota.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllAceptadas(Long idMesa) {
        return findNotasByEstado(EstadoNota.ACEPTADO, null, idMesa, null, null);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllAceptadas(Long idMesa, int first, int pageSize) {
        return findNotasByEstado(EstadoNota.ACEPTADO, null, idMesa, first, pageSize);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllAceptadas(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino) {

        QueryBuilder builder = new QueryBuilder("SELECT o FROM Nota o WHERE o.estado = true");
        builder.addNotNullFilter("o.estadoNota.id", EstadoNota.ACEPTADO);
        builder = buildCommonFilters(builder, idNota, nroNota, referencia, fechaDesde,
                fechaHasta, idTipoAsunto, idIniciador, null, false, idMesaOrigen, idMesaDestino);
        builder.append(" ORDER BY o.ultimoMovimiento DESC, o.id DESC");

        return executeListQuery(builder.getJpql(), builder.getParameters(), null, 1000, Nota.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllArchivadas(Long idMesa) {
        return findNotasByEstado(EstadoNota.ARCHIVADO, null, idMesa, null, null);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllArchivadas(Long idMesa, int first, int pageSize) {
        return findNotasByEstado(EstadoNota.ARCHIVADO, null, idMesa, first, pageSize);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllArchivadas(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino) {

        QueryBuilder builder = new QueryBuilder("SELECT o FROM Nota o WHERE o.estado = true");
        builder.addNotNullFilter("o.estadoNota.id", EstadoNota.ARCHIVADO);
        builder = buildCommonFilters(builder, idNota, nroNota, referencia, fechaDesde,
                fechaHasta, idTipoAsunto, idIniciador, null, false, idMesaOrigen, idMesaDestino);
        builder.append(" ORDER BY o.ultimoMovimiento DESC, o.id DESC");

        return executeListQuery(builder.getJpql(), builder.getParameters(), null, 1000, Nota.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllRechazadas(Long idMesa) {
        return findNotasByEstado(EstadoNota.RECHAZADO, idMesa, null, null, null);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllRechazadas(Long idMesa, int first, int pageSize) {
        return findNotasByEstado(EstadoNota.RECHAZADO, idMesa, null, first, pageSize);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllByEstado(Long idMesaDestino, Long idMesaOrigen,
            Long idEstado, int first, int pageSize) {
        return findNotasByEstado(idEstado, idMesaOrigen, idMesaDestino, first, pageSize);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countByEstado(Long idMesaDestino, Long idMesaOrigen, Long idEstado) {
        QueryBuilder builder = new QueryBuilder("SELECT COUNT(DISTINCT o) FROM Nota o WHERE o.estado = true");
        builder.addNotNullFilter("o.estadoNota.id", idEstado);
        builder.addNotNullFilter("o.mesaOrigen.id", idMesaOrigen);
        builder.addNotNullFilter("o.mesaDestino.id", idMesaDestino);
        return executeCountQuery(builder.getJpql(), builder.getParameters());
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllRechazadas(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino) {

        QueryBuilder builder = new QueryBuilder("SELECT o FROM Nota o WHERE o.estado = true");
        builder.addNotNullFilter("o.estadoNota.id", EstadoNota.RECHAZADO);
        builder = buildCommonFilters(builder, idNota, nroNota, referencia, fechaDesde,
                fechaHasta, idTipoAsunto, idIniciador, null, false, idMesaOrigen, idMesaDestino);
        builder.append(" ORDER BY o.ultimoMovimiento DESC, o.id DESC");

        return executeListQuery(builder.getJpql(), builder.getParameters(), null, 1000, Nota.class);
    }

    // ==========================================
    // MÉTODOS DE BÚSQUEDA AVANZADA
    // ==========================================

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Nota> findAllAvanzada(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, boolean consultarHistorial,
            String observacion, int first, int pageSize) {

        String selectClause = consultarHistorial
                ? "SELECT DISTINCT o FROM Nota o INNER JOIN o.listaHistorial h WHERE o.estado = true"
                : "SELECT DISTINCT o FROM Nota o WHERE o.estado = true";

        QueryBuilder builder = new QueryBuilder(selectClause);
        builder = buildCommonFilters(builder, idNota, nroNota, referencia, fechaDesde,
                fechaHasta, idTipoAsunto, idIniciador, observacion, consultarHistorial,
                idMesaOrigen, idMesaDestino);
        builder.append(" ORDER BY o.ultimoMovimiento DESC, o.id DESC");

        return executeListQuery(builder.getJpql(), builder.getParameters(), first, pageSize, Nota.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countAllAvanzada(Long idNota, String nroNota, String referencia,
            Date fechaDesde, Date fechaHasta, Long idTipoAsunto, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, boolean consultarHistorial,
            String observacion) {

        String selectClause = consultarHistorial
                ? "SELECT COUNT(o) FROM Nota o INNER JOIN o.listaHistorial h WHERE o.estado = true"
                : "SELECT COUNT(o) FROM Nota o WHERE o.estado = true";

        QueryBuilder builder = new QueryBuilder(selectClause);
        builder = buildCommonFilters(builder, idNota, nroNota, referencia, fechaDesde,
                fechaHasta, idTipoAsunto, idIniciador, observacion, consultarHistorial,
                idMesaOrigen, idMesaDestino);

        return executeCountQuery(builder.getJpql(), builder.getParameters());
    }

    // ==========================================
    // MÉTODOS DE CONTEO POR ESTADO
    // ==========================================

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countEnvidas(Long idMesa, Long idEstado) {
        QueryBuilder builder = new QueryBuilder("SELECT COUNT(o) FROM Nota o WHERE o.estado = true");
        builder.addNotNullFilter("o.mesaOrigen.id", idMesa)
                .addNotNullFilter("o.estadoNota.id", idEstado);
        return executeCountQuery(builder.getJpql(), builder.getParameters());
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countRecibidas(Long idMesa, Long idEstado) {
        QueryBuilder builder = new QueryBuilder("SELECT COUNT(o) FROM Nota o WHERE o.estado = true");
        builder.addNotNullFilter("o.mesaDestino.id", idMesa)
                .addNotNullFilter("o.estadoNota.id", idEstado);
        return executeCountQuery(builder.getJpql(), builder.getParameters());
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countAceptadas(Long idMesa, Long idEstado) {
        return countRecibidas(idMesa, idEstado);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countArchivada(Long idMesa, Long idEstado) {
        return countRecibidas(idMesa, idEstado);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countRechazada(Long idMesa, Long idEstado) {
        return countEnvidas(idMesa, idEstado);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countNotas(Long idMesa) {
        try {
            return countEnvidas(idMesa, EstadoNota.ENVIADO)
                    + countRecibidas(idMesa, EstadoNota.ENVIADO)
                    + countAceptadas(idMesa, EstadoNota.ACEPTADO)
                    + countRechazada(idMesa, EstadoNota.RECHAZADO)
                    + countArchivada(idMesa, EstadoNota.ARCHIVADO);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error calculando total de notas", e);
            return 0L;
        }
    }

    // ==========================================
    // MÉTODOS DE LECTURA INDIVIDUAL
    // ==========================================

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Nota find(Long idNota) {
        try {
            QueryBuilder builder = new QueryBuilder("SELECT o FROM Nota o WHERE o.estado = true AND o.id = :idNota");
            builder.addParameter("idNota", idNota);
            TypedQuery<Nota> query = em.createQuery(builder.getJpql(), Nota.class);
            builder.applyParameters(query);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error buscando nota con id: " + idNota, e);
            return null;
        }
    }

    // ==========================================
    // MÉTODOS DE REPORTE
    // ==========================================

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public boolean findAll(Date fechaMov, Date hora, Long idMesa, Long idDestino, Long idUsuario) {
        QueryBuilder builder = new QueryBuilder(
                "SELECT o FROM Nota o JOIN o.listaHistorial li WHERE o.estado = true AND li.estadoNota.id = :estadoEnviado AND li.mesaOrigen.id = :idMesa");

        builder.addParameter("estadoEnviado", EstadoNota.ENVIADO)
                .addParameter("idMesa", idMesa);

        if (fechaMov != null) {
            if (hora != null) {
                Calendar calInicio = Calendar.getInstance();
                calInicio.setTime(fechaMov);
                Calendar calHora = Calendar.getInstance();
                calHora.setTime(hora);
                calInicio.set(Calendar.HOUR_OF_DAY, calHora.get(Calendar.HOUR_OF_DAY));
                calInicio.set(Calendar.MINUTE, calHora.get(Calendar.MINUTE));
                calInicio.set(Calendar.SECOND, 0);

                Calendar calFin = Calendar.getInstance();
                calFin.setTime(fechaMov);
                calFin.add(Calendar.DATE, 1);

                builder.addParameter("fechaInicio", new Timestamp(calInicio.getTime().getTime()));
                builder.addParameter("fechaFin", new Timestamp(calFin.getTime().getTime()));
                builder.append(" AND li.fecha BETWEEN :fechaInicio AND :fechaFin");
        } else {
                Calendar calFin = Calendar.getInstance();
                calFin.setTime(fechaMov);
                calFin.add(Calendar.DATE, 1);

                builder.addParameter("fechaInicio", new Timestamp(fechaMov.getTime()));
                builder.addParameter("fechaFin", new Timestamp(calFin.getTime().getTime()));
                builder.append(" AND li.fecha BETWEEN :fechaInicio AND :fechaFin");
            }
        }

        builder.addNotNullFilter("li.mesaDestino.id", idDestino)
                .addNotNullFilter("li.usuario.id", idUsuario)
                .append(" ORDER BY li.fecha DESC");

        List<?> resultados = executeListQuery(builder.getJpql(), builder.getParameters(), null, 1, Nota.class);
        return !resultados.isEmpty();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public boolean findAll(Integer estado, Date fechaDesde, Date fechaHasta,
            Long origen, Long destino) {
        if (estado != null && estado == 7) {
            StringBuilder query = new StringBuilder();
            query.append("select min(h.fecha), n.id FROM notas as n ");
            query.append("inner join notas_historialnotas li on n.id = li.nota_id ");
            query.append("inner join historialnotas h on h.id = li.listahistorial_id ");
            query.append("WHERE n.estado = true ");
            query.append("AND h.estadonota_id = 2");

            if (origen != null && origen != 0) {
                query.append(" and h.mesaorigen_id =").append(origen);
            }
            if (destino != null && destino != 0) {
                query.append(" and h.mesadestino_id =").append(destino);
            }
            query.append(" and h.fecha BETWEEN '").append(fechaDesde).append("' and '").append(fechaHasta).append("' ");
            query.append(" order by h.fecha DESC");

            Query consulta = em.createNativeQuery(query.toString());
            return !consulta.getResultList().isEmpty();
        }

        QueryBuilder builder = new QueryBuilder(
                "SELECT o FROM Nota o JOIN o.listaHistorial li WHERE o.estado = true");

        Long estadoBusqueda = (estado != null && estado != 1) ? estado.longValue() : EstadoNota.ENVIADO;
        builder.addNotNullFilter("li.estadoNota.id", estadoBusqueda)
                .addNotNullFilter("li.mesaOrigen.id", origen)
                .addNotNullFilter("li.mesaDestino.id", destino)
                .addDateRange("li.fecha", fechaDesde, fechaHasta)
                .append(" ORDER BY li.fecha DESC");

        List<?> resultados = executeListQuery(builder.getJpql(), builder.getParameters(), null, 1, Nota.class);
        return !resultados.isEmpty();
    }

    // ==========================================
    // MÉTODOS DE ESCRITURA (TRANSACCIONES REQUIRED)
    // ==========================================

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public Nota create(String nroNota, Date fechaCreacion, String referencia,
            Integer fojas, Long idIniciador, Long idTipoAsunto, String observacion,
            Long idMesaDestino, Long idMesaOrigen, String archivo, Long idUsuario) throws Exception {
        try {
            Nota notaAux = new Nota();
            Mesa mesaOrigenAux = requireEntity(mesaFacade.find(idMesaOrigen),
                    "Mesa origen no encontrada: " + idMesaOrigen);
            Mesa mesaDestinoAux = requireEntity(mesaFacade.find(idMesaDestino),
                    "Mesa destino no encontrada: " + idMesaDestino);
            Iniciador iniciadorAux = requireEntity(iniciadorFacade.find(idIniciador),
                    "Iniciador no encontrado: " + idIniciador);
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);
            TipoAsunto tipoAsuntoAux = requireEntity(tipoAsuntoFacade.find(idTipoAsunto),
                    "Tipo de asunto no encontrado: " + idTipoAsunto);
            Estado estadoAux = requireEntity(estadoFacade.find(EstadoNota.ENVIADO),
                    "Estado ENVIADO no encontrado");

            notaAux.setNroNota(nroNota);
            notaAux.setFechaCreacion(fechaCreacion);
            notaAux.setReferencia(referencia != null ? referencia.toUpperCase() : null);
            notaAux.setFojas(fojas);
            notaAux.setIniciador(iniciadorAux);
            notaAux.setTipoAsunto(tipoAsuntoAux);
            notaAux.setObservacion(observacion != null ? observacion.toUpperCase() : null);
            notaAux.setEstado(true);
            notaAux.setEstadoNota(estadoAux);
            notaAux.setUsuario(usuarioAux);
            notaAux.setMesaOrigen(mesaOrigenAux);
            notaAux.setMesaDestino(mesaDestinoAux);
            notaAux.setUltimoMovimiento(new Date());
            notaAux.setArchivo(archivo != null && !archivo.isEmpty() ? archivo : null);

            super.create(notaAux);

            HistorialNota historial = buildHistorial(
                    notaAux,
                    observacion != null ? observacion.toUpperCase() : null,
                    usuarioAux,
                    iniciadorAux,
                    mesaOrigenAux,
                    mesaDestinoAux,
                    estadoAux);

            persistHistorialAndNota(notaAux, historial);
        return notaAux;

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear nota", e);
            throw rollbackBusiness("Error al intentar crear la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void ingresar(Long idNota, Long idMesaDestinoAux, Long idUsuario) throws Exception {
        try {
            Nota notaAux = findActiveNotaOrThrow(idNota);
                Mesa mesaOrigenAux = notaAux.getMesaDestino();
            Mesa mesaDestinoNueva = requireEntity(mesaFacade.find(idMesaDestinoAux),
                    "Mesa destino no encontrada: " + idMesaDestinoAux);
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);
            Estado estadoAux = requireEntity(estadoFacade.find(EstadoNota.ACEPTADO),
                    "Estado ACEPTADO no encontrado");

                notaAux.setUsuario(usuarioAux);
                notaAux.setMesaOrigen(mesaOrigenAux);
            notaAux.setMesaDestino(mesaDestinoNueva);
                notaAux.setEstadoNota(estadoAux);

            HistorialNota historial = buildHistorial(
                    notaAux,
                    "Ingreso Nota Externo-Obtención.",
                    usuarioAux,
                    notaAux.getIniciador(),
                    mesaOrigenAux,
                    mesaDestinoNueva,
                    estadoAux);

            persistHistorialAndNota(notaAux, historial);

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al ingresar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar ingresar la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void edit(Long idNota, String nro, Date fechaCreacion, String referencia,
            Integer fojas, Long idIniciador, Long idTipoAsunto, String obs,
            Long idMesaDestino, String archivo) throws Exception {
        try {
            TipoAsunto tipoAsuntoAux = requireEntity(tipoAsuntoFacade.find(idTipoAsunto),
                    "Tipo de asunto no encontrado: " + idTipoAsunto);
            Iniciador iniciadorAux = requireEntity(iniciadorFacade.find(idIniciador),
                    "Iniciador no encontrado: " + idIniciador);
            Mesa mesaDestinoAux = requireEntity(mesaFacade.find(idMesaDestino),
                    "Mesa destino no encontrada: " + idMesaDestino);
            Nota notaAux = findActiveNotaOrThrow(idNota);

            Estado estadoAux = notaAux.getEstadoNota();
            notaAux.setNroNota(nro);
            notaAux.setFechaCreacion(fechaCreacion);
            notaAux.setReferencia(referencia != null ? referencia.toUpperCase() : null);
            notaAux.setFojas(fojas);
            notaAux.setIniciador(iniciadorAux);
            notaAux.setTipoAsunto(tipoAsuntoAux);
            notaAux.setObservacion(obs != null ? obs.toUpperCase() : null);
            notaAux.setMesaDestino(mesaDestinoAux);
            notaAux.setArchivo(archivo != null && !archivo.isEmpty() ? archivo : null);

            HistorialNota historial = buildHistorial(
                    notaAux,
                    notaAux.getObservacion(),
                    notaAux.getUsuario(),
                    notaAux.getIniciador(),
                    notaAux.getMesaOrigen(),
                    notaAux.getMesaDestino(),
                    estadoAux);

            persistHistorialAndNota(notaAux, historial);

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al editar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar editar la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void aceptarNota(Long idNota, Long idUsuario, Integer flagBandeja) throws Exception {
        try {
            Nota notaAux = findActiveNotaOrThrow(idNota);
            Estado estadoAux = requireEntity(estadoFacade.find(EstadoNota.ACEPTADO),
                    "Estado ACEPTADO no encontrado");
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);

            notaAux.setEstadoNota(estadoAux);
            if (flagBandeja != null && flagBandeja.equals(1)) {
                Mesa temp = notaAux.getMesaOrigen();
                notaAux.setMesaOrigen(notaAux.getMesaDestino());
                notaAux.setMesaDestino(temp);
            }

            HistorialNota historial = buildHistorial(
                    notaAux,
                    notaAux.getObservacion(),
                    usuarioAux,
                    notaAux.getIniciador(),
                    notaAux.getMesaOrigen(),
                    notaAux.getMesaDestino(),
                    estadoAux);

            persistHistorialAndNota(notaAux, historial);

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al aceptar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar aceptar la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void archivarNota(Long idNota, String obs, String motivo, Long idUsuario) throws Exception {
        try {
            Estado estadoAux = requireEntity(estadoFacade.find(EstadoNota.ARCHIVADO),
                    "Estado ARCHIVADO no encontrado");
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);
            Nota notaAux = findActiveNotaOrThrow(idNota);

            notaAux.setEstadoNota(estadoAux);
            notaAux.setObservacion(obs != null ? obs.toUpperCase() : null);

            String obsHistorial = (obs != null ? obs.toUpperCase() : "")
                    + (motivo != null ? " [" + motivo + "]" : "");

            HistorialNota historial = buildHistorial(
                    notaAux,
                    obsHistorial,
                    usuarioAux,
                    notaAux.getIniciador(),
                    notaAux.getMesaOrigen(),
                    notaAux.getMesaDestino(),
                    estadoAux);

            persistHistorialAndNota(notaAux, historial);

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al archivar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar archivar la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void enviarNota(Long idNota, String obs, Long idMesaOrigen,
            Long idMesaDestino, Long idUsuario) throws Exception {
        try {
            Nota notaAux = findActiveNotaOrThrow(idNota);
            enviarNotaInternal(notaAux, obs, idMesaOrigen, idMesaDestino, idUsuario);
        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al enviar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar enviar la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void enviarNota(Long idNota, String obs, Long idMesaDestino, Long idUsuario) throws Exception {
        try {
            Nota nota = findActiveNotaOrThrow(idNota);
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);
            Mesa mesaUsuario = requireEntity(usuarioAux.getMesa(),
                    "El usuario no tiene mesa asociada");
            enviarNotaInternal(nota, obs, mesaUsuario.getId(), idMesaDestino, idUsuario);
        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al enviar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar enviar la nota " + idNota + ": " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void rechazarNota(Long idNota, String obsRechazo, Long idUsuario) throws Exception {
        try {
            Estado estadoAux = requireEntity(estadoFacade.find(EstadoNota.RECHAZADO),
                    "Estado RECHAZADO no encontrado");
            Nota notaAux = findActiveNotaOrThrow(idNota);
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);

            notaAux.setEstadoNota(estadoAux);

            HistorialNota historial = buildHistorial(
                    notaAux,
                    obsRechazo != null ? obsRechazo.toUpperCase() : null,
                    usuarioAux,
                    notaAux.getIniciador(),
                    notaAux.getMesaDestino(),
                    notaAux.getMesaOrigen(),
                    estadoAux);

            persistHistorialAndNota(notaAux, historial);

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al rechazar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar rechazar la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void reenviarNota(Long idNota, String obs, Long idMesaOrigen,
            Long idMesaDestino, Long idUsuario) throws Exception {
        try {
            Estado estadoEAux = requireEntity(estadoFacade.find(EstadoNota.ENVIADO),
                    "Estado ENVIADO no encontrado");
            Estado estadoRAux = requireEntity(estadoFacade.find(EstadoNota.REENVIADO),
                    "Estado REENVIADO no encontrado");
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);
            Mesa mesaOrigenAux = requireEntity(mesaFacade.find(idMesaOrigen),
                    "Mesa origen no encontrada: " + idMesaOrigen);
            Mesa mesaDestinoAux = requireEntity(mesaFacade.find(idMesaDestino),
                    "Mesa destino no encontrada: " + idMesaDestino);
            Nota notaAux = findActiveNotaOrThrow(idNota);

            notaAux.setEstadoNota(estadoEAux);
            notaAux.setMesaOrigen(mesaOrigenAux);
            notaAux.setMesaDestino(mesaDestinoAux);
            notaAux.setUsuario(usuarioAux);
            notaAux.setObservacion(obs != null ? obs.toUpperCase() : null);

            HistorialNota historial = buildHistorial(
                    notaAux,
                    obs != null ? obs.toUpperCase() : null,
                    usuarioAux,
                    notaAux.getIniciador(),
                    notaAux.getMesaOrigen(),
                    notaAux.getMesaDestino(),
                    estadoRAux);

            persistHistorialAndNota(notaAux, historial);

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al reenviar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar reenviar la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void aceptarReenviarNota(Long idNota, String obs, Long idMesaOrigen,
            Long idMesaDestino, Long idUsuario) throws Exception {
        try {
            Nota notaAux = findActiveNotaOrThrow(idNota);
            Estado estadoAux = requireEntity(estadoFacade.find(EstadoNota.ACEPTADO),
                    "Estado ACEPTADO no encontrado");
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);

            notaAux.setEstadoNota(estadoAux);

            HistorialNota historial = buildHistorial(
                    notaAux,
                    notaAux.getObservacion(),
                    usuarioAux,
                    notaAux.getIniciador(),
                    notaAux.getMesaOrigen(),
                    notaAux.getMesaDestino(),
                    estadoAux);

            persistHistorialAndNota(notaAux, historial);

            // Se evita self-invocation con this.enviarNota(...)
            // y se continúa dentro de la misma transacción activa.
            enviarNotaInternal(notaAux, obs, idMesaOrigen, idMesaDestino, idUsuario);

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al aceptar/reenviar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar aceptar y reenviar la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void edit(Long idNota, String obs, Long idMesaDestino, Long idUsuario) throws Exception {
        try {
            Mesa mesaDestinoAux = requireEntity(mesaFacade.find(idMesaDestino),
                    "Mesa destino no encontrada: " + idMesaDestino);
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);
            Nota notaAux = findActiveNotaOrThrow(idNota);

            Estado estadoAux = notaAux.getEstadoNota();
            notaAux.setObservacion(obs != null ? obs.toUpperCase() : null);
            notaAux.setMesaDestino(mesaDestinoAux);

            HistorialNota historial = buildHistorial(
                    notaAux,
                    "La nota ha sido editada. " + (notaAux.getObservacion() != null ? notaAux.getObservacion() : ""),
                    usuarioAux,
                    notaAux.getIniciador(),
                    notaAux.getMesaOrigen(),
                    notaAux.getMesaDestino(),
                    estadoAux);

            persistHistorialAndNota(notaAux, historial);

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al editar nota " + idNota, e);
            throw rollbackBusiness("Error al intentar editar la nota: " + e.getMessage(), e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void remove(Nota nota, Long idUsuario) {
        try {
            Nota notaManaged = requireEntity(findActiveNotaOrThrow(nota.getId()),
                    "Nota no encontrada: " + nota.getId());
            Usuario usuarioAux = requireEntity(usuarioFacade.find(idUsuario),
                    "Usuario no encontrado: " + idUsuario);

            ModelMapper modelMapper = new ModelMapper();
            NotaEliminada notaEliminada = modelMapper.map(notaManaged, NotaEliminada.class);
            notaEliminada.setUsuarioElimina(usuarioAux);
            notaEliminada.setFechaElimina(new Date());

            notaEliminadaFacade.create(notaEliminada);
            super.remove(notaManaged);

        } catch (EJBException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar nota", e);
            throw rollbackBusiness("Error al eliminar nota", e);
        }
    }
}