package org.ccpm.sysse.facade;

import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.utilidad.Utilidad;
import org.modelmapper.ModelMapper;

@Stateless
public class ExpedienteFacade extends AbstractFacade<Expediente> implements ExpedienteFacadeLocal {

    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    private static final Logger logger = Logger.getLogger(ExpedienteFacade.class.getName());

    @EJB
    private EstadoFacadeLocal estadoFacade;
    @EJB
    private IniciadorFacadeLocal iniciadorFacade;
    @EJB
    private TipoAsuntoFacadeLocal tipoAsuntoFacade;
    @EJB
    private MesaFacadeLocal mesaFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private HistorialExpedienteFacadeLocal historialFacade;
    @EJB
    private InstrumentoLegalFacadeLocal instrumentoLegalFacade;
    @EJB
    private ExpedienteEliminadoFacadeLocal expedienteEliminadoFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ExpedienteFacade() {
        super(Expediente.class);
    }

    // ==========================
    //   MÉTODOS DE BÚSQUEDA
    // ==========================

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Expediente find(Long codOrganismo, Long numCorrelativo, int anio) {
        logger.log(Level.INFO, "Ingresando a find() - codOrganismo: {0}, numCorrelativo: {1}, anio: {2}",
                new Object[]{codOrganismo, numCorrelativo, anio});
        try {
            Query consulta = em.createQuery(
                    "SELECT o FROM Expediente o " +
                            "WHERE o.estado = :p1 AND o.codOrganismo = :p2 " +
                            "AND o.numCorrelativo = :p3 AND o.anio = :p4 " +
                            "ORDER BY o.ultimoMovimiento DESC");
            consulta.setParameter("p1", true);
            consulta.setParameter("p2", codOrganismo);
            consulta.setParameter("p3", numCorrelativo);
            consulta.setParameter("p4", anio);
            return (Expediente) consulta.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.FINE, "No se encontró expediente para los parámetros indicados", e);
            return null;
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long findUltimoNro(Long codOrg) {
        logger.log(Level.INFO, "Ingresando a findUltimoNro() - codOrg: {0}", codOrg);
        Calendar c = GregorianCalendar.getInstance();
        Integer annio = c.get(Calendar.YEAR);

        Long nro = 1L;
        try {
            Number result = (Number) em.createQuery(
                            "SELECT MAX(e.numCorrelativo) FROM Expediente e " +
                                    "WHERE e.codOrganismo = :p1 AND e.anio= :p2 AND e.estado = true")
                    .setParameter("p1", codOrg)
                    .setParameter("p2", annio)
                    .getSingleResult();
            if (result != null) {
                nro = result.longValue() + 1;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error en findUltimoNro()", ex);
        }

        return nro;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long findUltimoNroPorAnio(Long codOrg, Integer anio) {
        logger.log(Level.INFO, "Ingresando a findUltimoNroPorAnio() - codOrg: {0}, anio: {1}",
                new Object[]{codOrg, anio});
        try {
            Query consulta = em.createQuery(
                    "SELECT MAX(e.numCorrelativo) FROM Expediente e " +
                            "WHERE e.codOrganismo = :p1 AND e.anio= :p2 AND e.estado = :p3 ");
            consulta.setParameter("p1", codOrg);
            consulta.setParameter("p2", anio);
            consulta.setParameter("p3", true);
            Long ultimo = (Long) consulta.getSingleResult();
            return ultimo != null ? ultimo : 1L;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error en findUltimoNroPorAnio()", ex);
            return 1L;
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findAll(boolean estado) {
        logger.log(Level.INFO, "Ingresando a findAll(boolean estado) - estado: {0}", estado);
        Query consulta = em.createQuery(
                "SELECT o FROM Expediente o WHERE o.estado = :p1 ORDER BY o.ultimoMovimiento DESC");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    /**
     * Bandeja por mesa sin paginación.
     * Antes hacía 5 llamadas a findAllByEstado; ahora una sola query.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findAll(Long idMesa) {
        logger.log(Level.INFO, "Ingresando a findAll(Long idMesa) - idMesa: {0}", idMesa);
        Query consulta = buildBandejaMesaQuery(idMesa);
        return consulta.getResultList();
    }

    /**
     * Bandeja por mesa con paginación.
     */
    @Override
    public List<Expediente> findAll(Long idMesa, int first, int pageSize) {
        logger.log(Level.INFO,
                "Ingresando a findAll(Long idMesa, int first, int pageSize) - idMesa: {0}, first: {1}, pageSize: {2}",
                new Object[]{idMesa, first, pageSize});
        Query consulta = buildBandejaMesaQuery(idMesa);
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        return consulta.getResultList();
    }

    /**
     * Query común para bandeja de una mesa (enviados/recibidos/aceptados/archivados/rechazados/generados).
     * Mantiene lógica equivalente a la que tenías en el findAll(...) con paginación.
     */
    private Query buildBandejaMesaQuery(Long idMesa) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT o FROM Expediente o WHERE o.estado = true");
        query.append(" AND (");
        query.append(" (o.mesaDestino.id = :idMesa AND o.estadoExpediente.id = 2)"); // Recibidos - estado ENVIADO (ID 2)
        query.append(" OR (o.mesaOrigen.id = :idMesa AND o.estadoExpediente.id = 2)"); // Enviados - estado ENVIADO (ID 2)
        query.append(" OR (o.mesaDestino.id = :idMesa AND o.estadoExpediente.id = 3)"); // Aceptados - estado ACEPTADO (ID 3)
        query.append(" OR (o.mesaOrigen.id = :idMesa AND o.estadoExpediente.id = 5)"); // Archivados - estado ARCHIVADO (ID 5)
        query.append(" OR (o.mesaDestino.id = :idMesa AND o.estadoExpediente.id = 4)"); // Rechazados - estado RECHAZADO (ID 4)
        query.append(" OR (o.mesaOrigen.id = :idMesa AND o.estadoExpediente.id = 1)"); // Generados - estado INICIADO (ID 1)
        query.append(" )");
        query.append(" ORDER BY o.ultimoMovimiento DESC");

        Query consulta = em.createQuery(query.toString());
        consulta.setParameter("idMesa", idMesa);
        return consulta;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findAll(Long codOrganismo, Long nro,
                                    Integer annio, Date fechaDesde, Date fechaHasta, Long idIniciador) {
        logger.log(Level.INFO,
                "Ingresando a findAll() - codOrganismo: {0}, nro: {1}, annio: {2}, fechaDesde: {3}, fechaHasta: {4}, idIniciador: {5}",
                new Object[]{codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador});
        StringBuilder query = new StringBuilder();
        query.append("SELECT o FROM Expediente o WHERE o.estado = true");

        if (codOrganismo != null) {
            query.append(" AND o.codOrganismo =").append(codOrganismo);
        }
        if (nro != null) {
            query.append(" AND o.numCorrelativo =").append(nro);
        }
        if (annio != null) {
            query.append(" AND o.anio =").append(annio);
        }
        if (fechaDesde != null && fechaHasta != null) {
            // Normalizar fechaDesde al inicio del día (00:00:00.000)
            Timestamp fDesde = new Timestamp(fechaDesde.getTime());
            Calendar calDesde = Calendar.getInstance();
            calDesde.setTime(fechaDesde);
            calDesde.set(Calendar.HOUR_OF_DAY, 0);
            calDesde.set(Calendar.MINUTE, 0);
            calDesde.set(Calendar.SECOND, 0);
            calDesde.set(Calendar.MILLISECOND, 0);
            fDesde.setTime(calDesde.getTime().getTime());
            
            // Normalizar fechaHasta al final del día (23:59:59.999)
            Timestamp fHasta = new Timestamp(fechaHasta.getTime());
            Calendar calHasta = Calendar.getInstance();
            calHasta.setTime(fechaHasta);
            calHasta.set(Calendar.HOUR_OF_DAY, 23);
            calHasta.set(Calendar.MINUTE, 59);
            calHasta.set(Calendar.SECOND, 59);
            calHasta.set(Calendar.MILLISECOND, 999);
            fHasta.setTime(calHasta.getTime().getTime());
            
            query.append(" AND o.fechaCreacion BETWEEN '").append(fDesde).append("' AND '").append(fHasta).append("' ");
        } else {
            if (fechaDesde != null) {
                // Normalizar fechaDesde al inicio del día (00:00:00.000)
                Timestamp fDesde = new Timestamp(fechaDesde.getTime());
                Calendar calDesde = Calendar.getInstance();
                calDesde.setTime(fechaDesde);
                calDesde.set(Calendar.HOUR_OF_DAY, 0);
                calDesde.set(Calendar.MINUTE, 0);
                calDesde.set(Calendar.SECOND, 0);
                calDesde.set(Calendar.MILLISECOND, 0);
                fDesde.setTime(calDesde.getTime().getTime());
                query.append(" AND o.fechaCreacion >= '").append(fDesde).append("' ");
            }
            if (fechaHasta != null) {
                // Normalizar fechaHasta al final del día (23:59:59.999)
                Timestamp fHasta = new Timestamp(fechaHasta.getTime());
                Calendar calHasta = Calendar.getInstance();
                calHasta.setTime(fechaHasta);
                calHasta.set(Calendar.HOUR_OF_DAY, 23);
                calHasta.set(Calendar.MINUTE, 59);
                calHasta.set(Calendar.SECOND, 59);
                calHasta.set(Calendar.MILLISECOND, 999);
                fHasta.setTime(calHasta.getTime().getTime());
                query.append(" AND o.fechaCreacion <= '").append(fHasta).append("' ");
            }
        }
        if (idIniciador != null && idIniciador != 0) {
            query.append(" AND o.iniciador.id =").append(idIniciador);
        }

        query.append(" ORDER BY o.ultimoMovimiento DESC");
        Query consulta = em.createQuery(query.toString());
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findAllByEstado(Long idMesaDestino, Long idMesaOrigen, Long idEstado) {
        logger.log(Level.INFO,
                "Ingresando a findAllByEstado() - idMesaDestino: {0}, idMesaOrigen: {1}, idEstado: {2}",
                new Object[]{idMesaDestino, idMesaOrigen, idEstado});
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT o FROM Expediente o WHERE o.estado = true");
        if (idEstado != null) {
            query.append(" AND o.estadoExpediente.id =").append(idEstado);
        }
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            query.append(" AND o.mesaOrigen.id =").append(idMesaOrigen);
        }
        if (idMesaDestino != null && idMesaDestino != 0) {
            query.append(" AND o.mesaDestino.id =").append(idMesaDestino);
        }
        query.append(" ORDER BY o.ultimoMovimiento DESC");
        Query consulta = em.createQuery(query.toString());
        return consulta.getResultList();
    }

    @Override
    public List<Expediente> findAllByEstado(Long idMesaDestino, Long idMesaOrigen, Long idEstado, int first, int pageSize) {
        logger.log(Level.INFO,
                "Ingresando a findAllByEstado() con paginación - idMesaDestino: {0}, idMesaOrigen: {1}, idEstado: {2}, first: {3}, pageSize: {4}",
                new Object[]{idMesaDestino, idMesaOrigen, idEstado, first, pageSize});
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT o FROM Expediente o WHERE o.estado = true");
        if (idEstado != null) {
            query.append(" AND o.estadoExpediente.id =").append(idEstado);
        }
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            query.append(" AND o.mesaOrigen.id =").append(idMesaOrigen);
        }
        if (idMesaDestino != null && idMesaDestino != 0) {
            query.append(" AND o.mesaDestino.id =").append(idMesaDestino);
        }
        query.append(" ORDER BY o.ultimoMovimiento DESC");
        Query consulta = em.createQuery(query.toString());
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        return consulta.getResultList();
    }

    @Override
    public Long countByEstado(Long idMesaDestino, Long idMesaOrigen, Long idEstado) {
        logger.log(Level.INFO,
                "Ingresando a countByEstado() - idMesaDestino: {0}, idMesaOrigen: {1}, idEstado: {2}",
                new Object[]{idMesaDestino, idMesaOrigen, idEstado});
        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(DISTINCT o) FROM Expediente o WHERE o.estado = true");
        if (idEstado != null) {
            query.append(" AND o.estadoExpediente.id =").append(idEstado);
        }
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            query.append(" AND o.mesaOrigen.id =").append(idMesaOrigen);
        }
        if (idMesaDestino != null && idMesaDestino != 0) {
            query.append(" AND o.mesaDestino.id =").append(idMesaDestino);
        }
        Query consulta = em.createQuery(query.toString());
        return (Long) consulta.getSingleResult();
    }

    @Override
    public Expediente findExpedienteByInstrumentoLegal(Long idInstrumento) {
        logger.log(Level.INFO, "Ingresando a findExpedienteByInstrumentoLegal() - idInstrumento: {0}", idInstrumento);
        Expediente retorno = null;
        try {
            Query consulta = em.createQuery(
                    "SELECT o FROM Expediente o, IN (o.listInstrumentosLegales) li " +
                            "WHERE li.id = :p2 AND li.estado = :p1");
            consulta.setParameter("p1", true);
            consulta.setParameter("p2", idInstrumento);
            retorno = (Expediente) consulta.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error en findExpedienteByInstrumentoLegal()", e);
        }
        return retorno;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findAllByEstado(Long codOrganismo, Long nro,
                                            Integer annio, Date fechaDesde, Date fechaHasta, Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto, String obs,
                                            Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, String expMadre,
                                            Long idMesaDestino, Long idMesaOrigen, Long idEstado) {
        logger.log(Level.INFO,
                "Ingresando a findAllByEstado() con múltiples parámetros - codOrganismo: {0}, nro: {1}, annio: {2}, idEstado: {3}, idMesaDestino: {4}, idMesaOrigen: {5}, fechaDesdeUltimoMov: {6}, fechaHastaUltimoMov: {7}",
                new Object[]{codOrganismo, nro, annio, idEstado, idMesaDestino, idMesaOrigen, fechaDesdeUltimoMov, fechaHastaUltimoMov});
        StringBuilder query = new StringBuilder();
        
        // Si se busca por fecha de último movimiento, necesitamos hacer JOIN con historiales
        boolean buscarPorUltimoMov = (fechaDesdeUltimoMov != null || fechaHastaUltimoMov != null);
        
        if (buscarPorUltimoMov) {
            // Usar subquery para obtener la fecha del último historial o fecha de creación si no hay historiales
            query.append("SELECT DISTINCT o FROM Expediente o WHERE o.estado = true");
        } else {
            query.append("SELECT DISTINCT o FROM Expediente o WHERE o.estado = true");
        }
        if (idEstado != null && idEstado != 0) {
            query.append(" AND o.estadoExpediente.id =").append(idEstado);
        }
        if (codOrganismo != null) {
            query.append(" AND o.codOrganismo =").append(codOrganismo);
        }
        if (nro != null) {
            query.append(" AND o.numCorrelativo =").append(nro);
        }
        if (annio != null) {
            query.append(" AND o.anio =").append(annio);
        }
        // Normalizar fechas de creación y usar parámetros nombrados
        Timestamp fDesde = null;
        Timestamp fHasta = null;
        if (fechaDesde != null && fechaHasta != null) {
            // Normalizar fechaDesde al inicio del día (00:00:00.000)
            fDesde = new Timestamp(fechaDesde.getTime());
            Calendar calDesde = Calendar.getInstance();
            calDesde.setTime(fechaDesde);
            calDesde.set(Calendar.HOUR_OF_DAY, 0);
            calDesde.set(Calendar.MINUTE, 0);
            calDesde.set(Calendar.SECOND, 0);
            calDesde.set(Calendar.MILLISECOND, 0);
            fDesde.setTime(calDesde.getTime().getTime());
            
            // Normalizar fechaHasta al final del día (23:59:59.999)
            fHasta = new Timestamp(fechaHasta.getTime());
            Calendar calHasta = Calendar.getInstance();
            calHasta.setTime(fechaHasta);
            calHasta.set(Calendar.HOUR_OF_DAY, 23);
            calHasta.set(Calendar.MINUTE, 59);
            calHasta.set(Calendar.SECOND, 59);
            calHasta.set(Calendar.MILLISECOND, 999);
            fHasta.setTime(calHasta.getTime().getTime());
            
            query.append(" AND o.fechaCreacion BETWEEN :fechaDesde AND :fechaHasta ");
        } else {
            if (fechaDesde != null) {
                // Normalizar fechaDesde al inicio del día (00:00:00.000)
                fDesde = new Timestamp(fechaDesde.getTime());
                Calendar calDesde = Calendar.getInstance();
                calDesde.setTime(fechaDesde);
                calDesde.set(Calendar.HOUR_OF_DAY, 0);
                calDesde.set(Calendar.MINUTE, 0);
                calDesde.set(Calendar.SECOND, 0);
                calDesde.set(Calendar.MILLISECOND, 0);
                fDesde.setTime(calDesde.getTime().getTime());
                query.append(" AND o.fechaCreacion >= :fechaDesde ");
            }
            if (fechaHasta != null) {
                // Normalizar fechaHasta al final del día (23:59:59.999)
                fHasta = new Timestamp(fechaHasta.getTime());
                Calendar calHasta = Calendar.getInstance();
                calHasta.setTime(fechaHasta);
                calHasta.set(Calendar.HOUR_OF_DAY, 23);
                calHasta.set(Calendar.MINUTE, 59);
                calHasta.set(Calendar.SECOND, 59);
                calHasta.set(Calendar.MILLISECOND, 999);
                fHasta.setTime(calHasta.getTime().getTime());
                query.append(" AND o.fechaCreacion <= :fechaHasta ");
            }
        }
        if (idIniciador != null && idIniciador != 0) {
            query.append(" AND o.iniciador.id =").append(idIniciador);
        }
        if (idTipoAsunto != null && idTipoAsunto != 0) {
            query.append(" AND o.tipoAsunto.id =").append(idTipoAsunto);
        }
        if (asunto != null && !asunto.equals("")) {
            query.append(" AND UPPER(o.asunto) LIKE '%").append(asunto.toUpperCase()).append("%'");
        }
        if (obs != null && !obs.equals("")) {
            query.append(" AND UPPER(o.observacion) LIKE '%").append(obs.toUpperCase()).append("%'");
        }
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            query.append(" AND o.mesaOrigen.id =").append(idMesaOrigen);
        }
        if (idMesaDestino != null && idMesaDestino != 0) {
            query.append(" AND o.mesaDestino.id =").append(idMesaDestino);
        }
        
        // Filtrar por fecha de último movimiento (fecha del historial más reciente)
        Timestamp fDesdeUltimoMov = null;
        Timestamp fHastaUltimoMov = null;
        if (buscarPorUltimoMov) {
            // Si solo se proporciona una fecha, tratarla como rango completo del día
            if (fechaDesdeUltimoMov != null && fechaHastaUltimoMov == null) {
                fechaHastaUltimoMov = fechaDesdeUltimoMov;
            }
            if (fechaDesdeUltimoMov != null && fechaHastaUltimoMov != null) {
                // Normalizar fechaDesdeUltimoMov al inicio del día (00:00:00.000)
                fDesdeUltimoMov = new Timestamp(fechaDesdeUltimoMov.getTime());
                Calendar calDesdeUltimoMov = Calendar.getInstance();
                calDesdeUltimoMov.setTime(fechaDesdeUltimoMov);
                calDesdeUltimoMov.set(Calendar.HOUR_OF_DAY, 0);
                calDesdeUltimoMov.set(Calendar.MINUTE, 0);
                calDesdeUltimoMov.set(Calendar.SECOND, 0);
                calDesdeUltimoMov.set(Calendar.MILLISECOND, 0);
                fDesdeUltimoMov.setTime(calDesdeUltimoMov.getTime().getTime());
                
                // Normalizar fechaHastaUltimoMov al final del día (23:59:59.999)
                fHastaUltimoMov = new Timestamp(fechaHastaUltimoMov.getTime());
                Calendar calHastaUltimoMov = Calendar.getInstance();
                calHastaUltimoMov.setTime(fechaHastaUltimoMov);
                calHastaUltimoMov.set(Calendar.HOUR_OF_DAY, 23);
                calHastaUltimoMov.set(Calendar.MINUTE, 59);
                calHastaUltimoMov.set(Calendar.SECOND, 59);
                calHastaUltimoMov.set(Calendar.MILLISECOND, 999);
                fHastaUltimoMov.setTime(calHastaUltimoMov.getTime().getTime());
                
                // Si no hay historiales, usar fecha de creación; si hay historiales, usar fecha del historial más reciente
                // Usar una subquery única con CASE WHEN para calcular la fecha efectiva
                query.append(" AND (SELECT CASE WHEN COUNT(li2) = 0 THEN o.fechaCreacion ELSE MAX(li2.fecha) END FROM o.listHistoriales li2 WHERE li2.estado = true) BETWEEN :fechaDesdeUltimoMov AND :fechaHastaUltimoMov ");
            } else {
                if (fechaDesdeUltimoMov != null) {
                    // Normalizar fechaDesdeUltimoMov al inicio del día (00:00:00.000)
                    fDesdeUltimoMov = new Timestamp(fechaDesdeUltimoMov.getTime());
                    Calendar calDesdeUltimoMov = Calendar.getInstance();
                    calDesdeUltimoMov.setTime(fechaDesdeUltimoMov);
                    calDesdeUltimoMov.set(Calendar.HOUR_OF_DAY, 0);
                    calDesdeUltimoMov.set(Calendar.MINUTE, 0);
                    calDesdeUltimoMov.set(Calendar.SECOND, 0);
                    calDesdeUltimoMov.set(Calendar.MILLISECOND, 0);
                    fDesdeUltimoMov.setTime(calDesdeUltimoMov.getTime().getTime());
                    query.append(" AND (SELECT CASE WHEN COUNT(li2) = 0 THEN o.fechaCreacion ELSE MAX(li2.fecha) END FROM o.listHistoriales li2 WHERE li2.estado = true) >= :fechaDesdeUltimoMov ");
                }
                if (fechaHastaUltimoMov != null) {
                    // Normalizar fechaHastaUltimoMov al final del día (23:59:59.999)
                    fHastaUltimoMov = new Timestamp(fechaHastaUltimoMov.getTime());
                    Calendar calHastaUltimoMov = Calendar.getInstance();
                    calHastaUltimoMov.setTime(fechaHastaUltimoMov);
                    calHastaUltimoMov.set(Calendar.HOUR_OF_DAY, 23);
                    calHastaUltimoMov.set(Calendar.MINUTE, 59);
                    calHastaUltimoMov.set(Calendar.SECOND, 59);
                    calHastaUltimoMov.set(Calendar.MILLISECOND, 999);
                    fHastaUltimoMov.setTime(calHastaUltimoMov.getTime().getTime());
                    query.append(" AND (SELECT CASE WHEN COUNT(li2) = 0 THEN o.fechaCreacion ELSE MAX(li2.fecha) END FROM o.listHistoriales li2 WHERE li2.estado = true) <= :fechaHastaUltimoMov ");
                }
            }
        }
        
        query.append(" ORDER BY o.ultimoMovimiento DESC");
        String queryString = query.toString();
        logger.log(Level.INFO, "Query generada en findAllByEstado: {0}", queryString);
        logger.log(Level.INFO, "Parámetros - fDesdeUltimoMov: {0}, fHastaUltimoMov: {1}, idEstado: {2}", 
                new Object[]{fDesdeUltimoMov, fHastaUltimoMov, idEstado});
        Query consulta = em.createQuery(queryString);
        // Establecer parámetros de fecha de creación si existen
        if (fDesde != null) {
            consulta.setParameter("fechaDesde", fDesde);
        }
        if (fHasta != null) {
            consulta.setParameter("fechaHasta", fHasta);
        }
        // Establecer parámetros de fecha de último movimiento si existen
        if (fDesdeUltimoMov != null) {
            consulta.setParameter("fechaDesdeUltimoMov", fDesdeUltimoMov);
        }
        if (fHastaUltimoMov != null) {
            consulta.setParameter("fechaHastaUltimoMov", fHastaUltimoMov);
        }
        List<Expediente> resultado = consulta.getResultList();
        logger.log(Level.INFO, "Resultados encontrados: {0}", resultado.size());
        if (resultado.isEmpty() && buscarPorUltimoMov) {
            // Log adicional para diagnosticar: verificar si hay expedientes sin el filtro de fecha
            logger.log(Level.INFO, "No se encontraron resultados con filtro de fecha de último movimiento. Verificando expedientes sin filtro...");
            StringBuilder querySinFecha = new StringBuilder();
            querySinFecha.append("SELECT COUNT(o) FROM Expediente o WHERE o.estado = true");
            if (idEstado != null && idEstado != 0) {
                querySinFecha.append(" AND o.estadoExpediente.id =").append(idEstado);
            }
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                querySinFecha.append(" AND o.mesaOrigen.id =").append(idMesaOrigen);
            }
            if (idMesaDestino != null && idMesaDestino != 0) {
                querySinFecha.append(" AND o.mesaDestino.id =").append(idMesaDestino);
            }
            Query consultaSinFecha = em.createQuery(querySinFecha.toString());
            Long totalSinFecha = (Long) consultaSinFecha.getSingleResult();
            logger.log(Level.INFO, "Total de expedientes sin filtro de fecha: {0}", totalSinFecha);
        }
        return resultado;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findAll(Long codOrganismo, Long nro,
                                    Integer annio, Date fechaDesde, Date fechaHasta, Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador,
                                    Long idMesaOrigen, Long idMesaDestino, Long idTipoAsunto, String asunto,
                                    String ordenPago,
                                    Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
                                    String expMadre, Long idMesaUsuario, boolean consultarHistorial, String observacion, int first, int pageSize) {
        logger.log(Level.INFO,
                "Ingresando a findAll() con paginación - codOrganismo: {0}, nro: {1}, annio: {2}, consultarHistorial: {3}, first: {4}, pageSize: {5}, fechaDesdeUltimoMov: {6}, fechaHastaUltimoMov: {7}",
                new Object[]{codOrganismo, nro, annio, consultarHistorial, first, pageSize, fechaDesdeUltimoMov, fechaHastaUltimoMov});
        StringBuilder query = new StringBuilder();
        
        // Si se busca por fecha de último movimiento, necesitamos hacer JOIN con historiales
        boolean buscarPorUltimoMov = (fechaDesdeUltimoMov != null || fechaHastaUltimoMov != null);
        
        if (buscarPorUltimoMov) {
            // Usar subquery para obtener la fecha del último historial o fecha de creación si no hay historiales
            query.append("SELECT DISTINCT o FROM Expediente o WHERE o.estado = true");
        } else if (consultarHistorial) {
            query.append("SELECT DISTINCT o FROM Expediente o INNER JOIN o.listHistoriales h WHERE o.estado = true ");
        } else {
            query.append("SELECT DISTINCT o FROM Expediente o WHERE o.estado = true ");
        }

        if (codOrganismo != null) {
            query.append(" AND o.codOrganismo =").append(codOrganismo);
        }
        if (nro != null) {
            query.append(" AND o.numCorrelativo =").append(nro);
        }
        if (annio != null) {
            query.append(" AND o.anio =").append(annio);
        }
        if (codOrganismo != null && nro != null && annio != null) {
            query.append(" OR o.expMadre LIKE '%").append(codOrganismo).append("-").append(nro).append("/").append(annio).append("%' ");
        }

        // Si idMesaUsuario no es null y no se especifican idMesaOrigen ni idMesaDestino, filtrar por todos los estados de la mesa
        // En la bandeja "Todos", mostrar todos los expedientes donde la mesa está involucrada (origen o destino) sin importar el estado
        if (idMesaUsuario != null && (idMesaOrigen == null || idMesaOrigen == 0) && (idMesaDestino == null || idMesaDestino == 0)) {
            query.append(" AND (o.mesaDestino.id = ").append(idMesaUsuario).append(" OR o.mesaOrigen.id = ").append(idMesaUsuario).append(")");
        }

        if (fechaDesde != null && fechaHasta != null) {
            // Normalizar fechaDesde al inicio del día (00:00:00.000)
            Timestamp fDesde = new Timestamp(fechaDesde.getTime());
            Calendar calDesde = Calendar.getInstance();
            calDesde.setTime(fechaDesde);
            calDesde.set(Calendar.HOUR_OF_DAY, 0);
            calDesde.set(Calendar.MINUTE, 0);
            calDesde.set(Calendar.SECOND, 0);
            calDesde.set(Calendar.MILLISECOND, 0);
            fDesde.setTime(calDesde.getTime().getTime());
            
            // Normalizar fechaHasta al final del día (23:59:59.999)
            Timestamp fHasta = new Timestamp(fechaHasta.getTime());
            Calendar calHasta = Calendar.getInstance();
            calHasta.setTime(fechaHasta);
            calHasta.set(Calendar.HOUR_OF_DAY, 23);
            calHasta.set(Calendar.MINUTE, 59);
            calHasta.set(Calendar.SECOND, 59);
            calHasta.set(Calendar.MILLISECOND, 999);
            fHasta.setTime(calHasta.getTime().getTime());
            
            query.append(" AND o.fechaCreacion BETWEEN '").append(fDesde).append("' AND '").append(fHasta).append("' ");
        } else {
            if (fechaDesde != null) {
                // Normalizar fechaDesde al inicio del día (00:00:00.000)
                Timestamp fDesde = new Timestamp(fechaDesde.getTime());
                Calendar calDesde = Calendar.getInstance();
                calDesde.setTime(fechaDesde);
                calDesde.set(Calendar.HOUR_OF_DAY, 0);
                calDesde.set(Calendar.MINUTE, 0);
                calDesde.set(Calendar.SECOND, 0);
                calDesde.set(Calendar.MILLISECOND, 0);
                fDesde.setTime(calDesde.getTime().getTime());
                query.append(" AND o.fechaCreacion >= '").append(fDesde).append("' ");
            }
            if (fechaHasta != null) {
                // Normalizar fechaHasta al final del día (23:59:59.999)
                Timestamp fHasta = new Timestamp(fechaHasta.getTime());
                Calendar calHasta = Calendar.getInstance();
                calHasta.setTime(fechaHasta);
                calHasta.set(Calendar.HOUR_OF_DAY, 23);
                calHasta.set(Calendar.MINUTE, 59);
                calHasta.set(Calendar.SECOND, 59);
                calHasta.set(Calendar.MILLISECOND, 999);
                fHasta.setTime(calHasta.getTime().getTime());
                query.append(" AND o.fechaCreacion <= '").append(fHasta).append("' ");
            }
        }
        if (idIniciador != null && idIniciador != 0) {
            query.append(" AND o.iniciador.id =").append(idIniciador);
        }
        if (consultarHistorial) {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                query.append(" AND h.mesaOrigen.id =").append(idMesaOrigen);
            }
            if (idMesaDestino != null && idMesaDestino != 0) {
                query.append(" AND h.mesaDestino.id =").append(idMesaDestino);
            }
        } else {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                query.append(" AND o.mesaOrigen.id =").append(idMesaOrigen);
            }
            if (idMesaDestino != null && idMesaDestino != 0) {
                query.append(" AND o.mesaDestino.id =").append(idMesaDestino);
            }
        }
        if (observacion != null && !observacion.isEmpty()) {
            if (consultarHistorial) {
                query.append(" AND (UPPER(h.observacion)) LIKE '%")
                        .append(Utilidad.desacentuar(observacion.toUpperCase()).replace(" ", "%"))
                        .append("%'");
            } else {
                query.append(" AND (UPPER(o.observacion)) LIKE '%")
                        .append(Utilidad.desacentuar(observacion.toUpperCase()).replace(" ", "%"))
                        .append("%'");
            }
        }
        if (pedidoFondo != null) {
            query.append(" AND o.pedidoFondo = ").append(pedidoFondo);
        }
        if (pedidoFondoAnio != null) {
            query.append(" AND o.pedidoFondoAnio = ").append(pedidoFondoAnio);
        }
        if (ordenEntrega != null) {
            query.append(" AND o.ordenEntrega = ").append(ordenEntrega);
        }
        if (ordenEntregaAnio != null) {
            query.append(" AND o.ordenEntregaAnio = ").append(ordenEntregaAnio);
        }
        if (expMadre != null && !expMadre.equals("")) {
            query.append(" AND (UPPER(o.expMadre) LIKE '%").append(expMadre.toUpperCase()).append("%'");
            query.append(" OR UPPER(o.nroExp) LIKE '%").append(expMadre.toUpperCase()).append("%')");
        }
        if (idTipoAsunto != null && idTipoAsunto != 0) {
            query.append(" AND o.tipoAsunto.id =").append(idTipoAsunto);
        }
        if (asunto != null && !asunto.equals("")) {
            query.append(" AND UPPER(o.asunto) LIKE '%")
                    .append(Utilidad.desacentuar(asunto).toUpperCase()).append("%'");
        }
        if (ordenPago != null && !ordenPago.equals("")) {
            query.append(" AND UPPER(o.ordenPago) LIKE '%")
                    .append(Utilidad.desacentuar(ordenPago).toUpperCase()).append("%'");
        }
        
        // Filtrar por fecha de último movimiento (fecha del historial más reciente)
        Timestamp fDesdeUltimoMov = null;
        Timestamp fHastaUltimoMov = null;
        if (buscarPorUltimoMov) {
            // Si solo se proporciona una fecha, tratarla como rango completo del día
            if (fechaDesdeUltimoMov != null && fechaHastaUltimoMov == null) {
                fechaHastaUltimoMov = fechaDesdeUltimoMov;
            }
            if (fechaDesdeUltimoMov != null && fechaHastaUltimoMov != null) {
                // Normalizar fechaDesdeUltimoMov al inicio del día (00:00:00.000)
                fDesdeUltimoMov = new Timestamp(fechaDesdeUltimoMov.getTime());
                Calendar calDesdeUltimoMov = Calendar.getInstance();
                calDesdeUltimoMov.setTime(fechaDesdeUltimoMov);
                calDesdeUltimoMov.set(Calendar.HOUR_OF_DAY, 0);
                calDesdeUltimoMov.set(Calendar.MINUTE, 0);
                calDesdeUltimoMov.set(Calendar.SECOND, 0);
                calDesdeUltimoMov.set(Calendar.MILLISECOND, 0);
                fDesdeUltimoMov.setTime(calDesdeUltimoMov.getTime().getTime());
                
                // Normalizar fechaHastaUltimoMov al final del día (23:59:59.999)
                fHastaUltimoMov = new Timestamp(fechaHastaUltimoMov.getTime());
                Calendar calHastaUltimoMov = Calendar.getInstance();
                calHastaUltimoMov.setTime(fechaHastaUltimoMov);
                calHastaUltimoMov.set(Calendar.HOUR_OF_DAY, 23);
                calHastaUltimoMov.set(Calendar.MINUTE, 59);
                calHastaUltimoMov.set(Calendar.SECOND, 59);
                calHastaUltimoMov.set(Calendar.MILLISECOND, 999);
                fHastaUltimoMov.setTime(calHastaUltimoMov.getTime().getTime());
                
                // Si no hay historiales, usar fecha de creación; si hay historiales, usar fecha del historial más reciente
                // Usar una subquery única con CASE WHEN para calcular la fecha efectiva
                query.append(" AND (SELECT CASE WHEN COUNT(li2) = 0 THEN o.fechaCreacion ELSE MAX(li2.fecha) END FROM o.listHistoriales li2 WHERE li2.estado = true) BETWEEN :fechaDesdeUltimoMov AND :fechaHastaUltimoMov ");
            } else {
                if (fechaDesdeUltimoMov != null) {
                    // Normalizar fechaDesdeUltimoMov al inicio del día (00:00:00.000)
                    fDesdeUltimoMov = new Timestamp(fechaDesdeUltimoMov.getTime());
                    Calendar calDesdeUltimoMov = Calendar.getInstance();
                    calDesdeUltimoMov.setTime(fechaDesdeUltimoMov);
                    calDesdeUltimoMov.set(Calendar.HOUR_OF_DAY, 0);
                    calDesdeUltimoMov.set(Calendar.MINUTE, 0);
                    calDesdeUltimoMov.set(Calendar.SECOND, 0);
                    calDesdeUltimoMov.set(Calendar.MILLISECOND, 0);
                    fDesdeUltimoMov.setTime(calDesdeUltimoMov.getTime().getTime());
                    query.append(" AND (SELECT CASE WHEN COUNT(li2) = 0 THEN o.fechaCreacion ELSE MAX(li2.fecha) END FROM o.listHistoriales li2 WHERE li2.estado = true) >= :fechaDesdeUltimoMov ");
                }
                if (fechaHastaUltimoMov != null) {
                    // Normalizar fechaHastaUltimoMov al final del día (23:59:59.999)
                    fHastaUltimoMov = new Timestamp(fechaHastaUltimoMov.getTime());
                    Calendar calHastaUltimoMov = Calendar.getInstance();
                    calHastaUltimoMov.setTime(fechaHastaUltimoMov);
                    calHastaUltimoMov.set(Calendar.HOUR_OF_DAY, 23);
                    calHastaUltimoMov.set(Calendar.MINUTE, 59);
                    calHastaUltimoMov.set(Calendar.SECOND, 59);
                    calHastaUltimoMov.set(Calendar.MILLISECOND, 999);
                    fHastaUltimoMov.setTime(calHastaUltimoMov.getTime().getTime());
                    query.append(" AND (SELECT CASE WHEN COUNT(li2) = 0 THEN o.fechaCreacion ELSE MAX(li2.fecha) END FROM o.listHistoriales li2 WHERE li2.estado = true) <= :fechaHastaUltimoMov ");
                }
            }
        }

        query.append(" ORDER BY o.ultimoMovimiento DESC, o.anio DESC, o.numCorrelativo DESC, o.codOrganismo  ");
        Query consulta = em.createQuery(query.toString());
        // Establecer parámetros de fecha de último movimiento si existen
        if (fDesdeUltimoMov != null) {
            consulta.setParameter("fechaDesdeUltimoMov", fDesdeUltimoMov);
        }
        if (fHastaUltimoMov != null) {
            consulta.setParameter("fechaHastaUltimoMov", fHastaUltimoMov);
        }
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countAll(Long codOrganismo, Long nro,
                         Integer annio, Date fechaDesde, Date fechaHasta, Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador,
                         Long idMesaOrigen, Long idMesaDestino, Long idTipoAsunto, String asunto,
                         String ordenPago,
                         Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
                         String expMadre, Long idMesaUsuario, boolean consultarHistorial, String observacion) {
        logger.log(Level.INFO,
                "Ingresando a countAll() - codOrganismo: {0}, nro: {1}, annio: {2}, consultarHistorial: {3}, fechaDesdeUltimoMov: {4}, fechaHastaUltimoMov: {5}",
                new Object[]{codOrganismo, nro, annio, consultarHistorial, fechaDesdeUltimoMov, fechaHastaUltimoMov});
        StringBuilder query = new StringBuilder();
        
        // Si se busca por fecha de último movimiento, necesitamos hacer JOIN con historiales
        boolean buscarPorUltimoMov = (fechaDesdeUltimoMov != null || fechaHastaUltimoMov != null);
        
        if (buscarPorUltimoMov) {
            // Usar subquery para obtener la fecha del último historial o fecha de creación si no hay historiales
            query.append("SELECT COUNT(DISTINCT o) FROM Expediente o WHERE o.estado = true");
        } else if (consultarHistorial) {
            query.append("SELECT COUNT(DISTINCT o) FROM Expediente o INNER JOIN o.listHistoriales h WHERE o.estado = true ");
        } else {
            query.append("SELECT COUNT(o) FROM Expediente o WHERE o.estado = true ");
        }

        if (codOrganismo != null) {
            query.append(" AND o.codOrganismo =").append(codOrganismo);
        }
        if (nro != null) {
            query.append(" AND o.numCorrelativo =").append(nro);
        }
        if (annio != null) {
            query.append(" AND o.anio =").append(annio);
        }
        if (codOrganismo != null && nro != null && annio != null) {
            query.append(" OR o.expMadre LIKE '%").append(codOrganismo).append("-").append(nro).append("/").append(annio).append("%' ");
        }

        // Si idMesaUsuario no es null y no se especifican idMesaOrigen ni idMesaDestino, filtrar por todos los estados de la mesa
        // En la bandeja "Todos", mostrar todos los expedientes donde la mesa está involucrada (origen o destino) sin importar el estado
        if (idMesaUsuario != null && (idMesaOrigen == null || idMesaOrigen == 0) && (idMesaDestino == null || idMesaDestino == 0)) {
            query.append(" AND (o.mesaDestino.id = ").append(idMesaUsuario).append(" OR o.mesaOrigen.id = ").append(idMesaUsuario).append(")");
        }

        if (fechaDesde != null && fechaHasta != null) {
            // Normalizar fechaDesde al inicio del día (00:00:00.000)
            Timestamp fDesde = new Timestamp(fechaDesde.getTime());
            Calendar calDesde = Calendar.getInstance();
            calDesde.setTime(fechaDesde);
            calDesde.set(Calendar.HOUR_OF_DAY, 0);
            calDesde.set(Calendar.MINUTE, 0);
            calDesde.set(Calendar.SECOND, 0);
            calDesde.set(Calendar.MILLISECOND, 0);
            fDesde.setTime(calDesde.getTime().getTime());
            
            // Normalizar fechaHasta al final del día (23:59:59.999)
            Timestamp fHasta = new Timestamp(fechaHasta.getTime());
            Calendar calHasta = Calendar.getInstance();
            calHasta.setTime(fechaHasta);
            calHasta.set(Calendar.HOUR_OF_DAY, 23);
            calHasta.set(Calendar.MINUTE, 59);
            calHasta.set(Calendar.SECOND, 59);
            calHasta.set(Calendar.MILLISECOND, 999);
            fHasta.setTime(calHasta.getTime().getTime());
            
            query.append(" AND o.fechaCreacion BETWEEN '").append(fDesde).append("' AND '").append(fHasta).append("' ");
        } else {
            if (fechaDesde != null) {
                // Normalizar fechaDesde al inicio del día (00:00:00.000)
                Timestamp fDesde = new Timestamp(fechaDesde.getTime());
                Calendar calDesde = Calendar.getInstance();
                calDesde.setTime(fechaDesde);
                calDesde.set(Calendar.HOUR_OF_DAY, 0);
                calDesde.set(Calendar.MINUTE, 0);
                calDesde.set(Calendar.SECOND, 0);
                calDesde.set(Calendar.MILLISECOND, 0);
                fDesde.setTime(calDesde.getTime().getTime());
                query.append(" AND o.fechaCreacion >= '").append(fDesde).append("' ");
            }
            if (fechaHasta != null) {
                // Normalizar fechaHasta al final del día (23:59:59.999)
                Timestamp fHasta = new Timestamp(fechaHasta.getTime());
                Calendar calHasta = Calendar.getInstance();
                calHasta.setTime(fechaHasta);
                calHasta.set(Calendar.HOUR_OF_DAY, 23);
                calHasta.set(Calendar.MINUTE, 59);
                calHasta.set(Calendar.SECOND, 59);
                calHasta.set(Calendar.MILLISECOND, 999);
                fHasta.setTime(calHasta.getTime().getTime());
                query.append(" AND o.fechaCreacion <= '").append(fHasta).append("' ");
            }
        }
        if (idIniciador != null && idIniciador != 0) {
            query.append(" AND o.iniciador.id =").append(idIniciador);
        }
        if (consultarHistorial) {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                query.append(" AND h.mesaOrigen.id =").append(idMesaOrigen);
            }
            if (idMesaDestino != null && idMesaDestino != 0) {
                query.append(" AND h.mesaDestino.id =").append(idMesaDestino);
            }
        } else {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                query.append(" AND o.mesaOrigen.id =").append(idMesaOrigen);
            }
            if (idMesaDestino != null && idMesaDestino != 0) {
                query.append(" AND o.mesaDestino.id =").append(idMesaDestino);
            }
        }
        if (observacion != null && !observacion.isEmpty()) {
            if (consultarHistorial) {
                query.append(" AND (UPPER(h.observacion)) LIKE '%")
                        .append(Utilidad.desacentuar(observacion.toUpperCase()).replace(" ", "%"))
                        .append("%'");
            } else {
                query.append(" AND (UPPER(o.observacion)) LIKE '%")
                        .append(Utilidad.desacentuar(observacion.toUpperCase()).replace(" ", "%"))
                        .append("%'");
            }
        }
        if (pedidoFondo != null) {
            query.append(" AND o.pedidoFondo = ").append(pedidoFondo);
        }
        if (pedidoFondoAnio != null) {
            query.append(" AND o.pedidoFondoAnio = ").append(pedidoFondoAnio);
        }
        if (ordenEntrega != null) {
            query.append(" AND o.ordenEntrega = ").append(ordenEntrega);
        }
        if (ordenEntregaAnio != null) {
            query.append(" AND o.ordenEntregaAnio = ").append(ordenEntregaAnio);
        }
        if (expMadre != null && !expMadre.equals("")) {
            query.append(" AND (UPPER(o.expMadre) LIKE '%").append(expMadre.toUpperCase()).append("%'");
            query.append(" OR UPPER(o.nroExp) LIKE '%").append(expMadre.toUpperCase()).append("%')");
        }
        if (idTipoAsunto != null && idTipoAsunto != 0) {
            query.append(" AND o.tipoAsunto.id =").append(idTipoAsunto);
        }
        if (asunto != null && !asunto.equals("")) {
            query.append(" AND UPPER(o.asunto) LIKE '%")
                    .append(Utilidad.desacentuar(asunto).toUpperCase()).append("%'");
        }
        if (ordenPago != null && !ordenPago.equals("")) {
            query.append(" AND UPPER(o.ordenPago) LIKE '%")
                    .append(Utilidad.desacentuar(ordenPago).toUpperCase()).append("%'");
        }
        
        // Filtrar por fecha de último movimiento (fecha del historial más reciente)
        Timestamp fDesdeUltimoMov = null;
        Timestamp fHastaUltimoMov = null;
        if (buscarPorUltimoMov) {
            // Si solo se proporciona una fecha, tratarla como rango completo del día
            if (fechaDesdeUltimoMov != null && fechaHastaUltimoMov == null) {
                fechaHastaUltimoMov = fechaDesdeUltimoMov;
            }
            if (fechaDesdeUltimoMov != null && fechaHastaUltimoMov != null) {
                // Normalizar fechaDesdeUltimoMov al inicio del día (00:00:00.000)
                fDesdeUltimoMov = new Timestamp(fechaDesdeUltimoMov.getTime());
                Calendar calDesdeUltimoMov = Calendar.getInstance();
                calDesdeUltimoMov.setTime(fechaDesdeUltimoMov);
                calDesdeUltimoMov.set(Calendar.HOUR_OF_DAY, 0);
                calDesdeUltimoMov.set(Calendar.MINUTE, 0);
                calDesdeUltimoMov.set(Calendar.SECOND, 0);
                calDesdeUltimoMov.set(Calendar.MILLISECOND, 0);
                fDesdeUltimoMov.setTime(calDesdeUltimoMov.getTime().getTime());
                
                // Normalizar fechaHastaUltimoMov al final del día (23:59:59.999)
                fHastaUltimoMov = new Timestamp(fechaHastaUltimoMov.getTime());
                Calendar calHastaUltimoMov = Calendar.getInstance();
                calHastaUltimoMov.setTime(fechaHastaUltimoMov);
                calHastaUltimoMov.set(Calendar.HOUR_OF_DAY, 23);
                calHastaUltimoMov.set(Calendar.MINUTE, 59);
                calHastaUltimoMov.set(Calendar.SECOND, 59);
                calHastaUltimoMov.set(Calendar.MILLISECOND, 999);
                fHastaUltimoMov.setTime(calHastaUltimoMov.getTime().getTime());
                
                // Si no hay historiales, usar fecha de creación; si hay historiales, usar fecha del historial más reciente
                // Usar una subquery única con CASE WHEN para calcular la fecha efectiva
                query.append(" AND (SELECT CASE WHEN COUNT(li2) = 0 THEN o.fechaCreacion ELSE MAX(li2.fecha) END FROM o.listHistoriales li2 WHERE li2.estado = true) BETWEEN :fechaDesdeUltimoMov AND :fechaHastaUltimoMov ");
            } else {
                if (fechaDesdeUltimoMov != null) {
                    // Normalizar fechaDesdeUltimoMov al inicio del día (00:00:00.000)
                    fDesdeUltimoMov = new Timestamp(fechaDesdeUltimoMov.getTime());
                    Calendar calDesdeUltimoMov = Calendar.getInstance();
                    calDesdeUltimoMov.setTime(fechaDesdeUltimoMov);
                    calDesdeUltimoMov.set(Calendar.HOUR_OF_DAY, 0);
                    calDesdeUltimoMov.set(Calendar.MINUTE, 0);
                    calDesdeUltimoMov.set(Calendar.SECOND, 0);
                    calDesdeUltimoMov.set(Calendar.MILLISECOND, 0);
                    fDesdeUltimoMov.setTime(calDesdeUltimoMov.getTime().getTime());
                    query.append(" AND (SELECT CASE WHEN COUNT(li2) = 0 THEN o.fechaCreacion ELSE MAX(li2.fecha) END FROM o.listHistoriales li2 WHERE li2.estado = true) >= :fechaDesdeUltimoMov ");
                }
                if (fechaHastaUltimoMov != null) {
                    // Normalizar fechaHastaUltimoMov al final del día (23:59:59.999)
                    fHastaUltimoMov = new Timestamp(fechaHastaUltimoMov.getTime());
                    Calendar calHastaUltimoMov = Calendar.getInstance();
                    calHastaUltimoMov.setTime(fechaHastaUltimoMov);
                    calHastaUltimoMov.set(Calendar.HOUR_OF_DAY, 23);
                    calHastaUltimoMov.set(Calendar.MINUTE, 59);
                    calHastaUltimoMov.set(Calendar.SECOND, 59);
                    calHastaUltimoMov.set(Calendar.MILLISECOND, 999);
                    fHastaUltimoMov.setTime(calHastaUltimoMov.getTime().getTime());
                    query.append(" AND (SELECT CASE WHEN COUNT(li2) = 0 THEN o.fechaCreacion ELSE MAX(li2.fecha) END FROM o.listHistoriales li2 WHERE li2.estado = true) <= :fechaHastaUltimoMov ");
                }
            }
        }
        
        Query consulta = em.createQuery(query.toString());
        // Establecer parámetros de fecha de último movimiento si existen
        if (fDesdeUltimoMov != null) {
            consulta.setParameter("fechaDesdeUltimoMov", fDesdeUltimoMov);
        }
        if (fHastaUltimoMov != null) {
            consulta.setParameter("fechaHastaUltimoMov", fHastaUltimoMov);
        }
        return (Long) consulta.getSingleResult();
    }

    // ==========================
    //   CREACIÓN / EDICIÓN
    // ==========================

    @Override
    public Expediente create(Date fecha, int cuerpo, int fojas,
                             Long idIniciador, Long idTipoAsunto,
                             String asunto, String obs, String ordenPago,
                             Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, String expMadre,
                             Long idOrigen, Long idDestino,
                             Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a create() - idIniciador: {0}, idTipoAsunto: {1}, idOrigen: {2}, idDestino: {3}, idUsuario: {4}",
                new Object[]{idIniciador, idTipoAsunto, idOrigen, idDestino, idUsuario});
        Expediente expedienteAux = new Expediente();

        try {
            Iniciador iniciadorAux = this.iniciadorFacade.find(idIniciador);
            TipoAsunto tipoAsuntoAux = this.tipoAsuntoFacade.find(idTipoAsunto);
            Mesa mesaOrigenAux = this.mesaFacade.find(idOrigen);
            Mesa mesaDestinoAux = this.mesaFacade.find(idDestino);
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);

            Estado estadoAux;
            Long codOrganismo = usuarioAux.getMesa().getOrganismo().getCodOrganigrama();
            Long nro = this.findUltimoNro(codOrganismo);
            Calendar c = GregorianCalendar.getInstance();
            Integer annio = c.get(Calendar.YEAR);
            String nroExp = codOrganismo.toString() + "-" + nro.toString() + "/" + annio;

            if (idDestino.longValue() == idOrigen.longValue()) {
                estadoAux = this.estadoFacade.find(3L);//Estado Aceptado
            } else {
                estadoAux = this.estadoFacade.find(2L);//Estado Enviado
            }

            expedienteAux.setCodOrganismo(codOrganismo);
            expedienteAux.setNumCorrelativo(nro);
            expedienteAux.setAnio(annio);
            expedienteAux.setCuerpo(cuerpo);
            expedienteAux.setFechaCreacion(fecha);
            expedienteAux.setFojas(fojas);
            expedienteAux.setIniciador(iniciadorAux);
            expedienteAux.setTipoAsunto(tipoAsuntoAux);
            expedienteAux.setAsunto(asunto.toUpperCase());
            expedienteAux.setObservacion(obs.toUpperCase());
            expedienteAux.setOrdenPago(ordenPago);
            expedienteAux.setPedidoFondo(pedidoFondo);
            expedienteAux.setPedidoFondoAnio(pedidoFondoAnio);
            expedienteAux.setOrdenEntrega(ordenEntrega);
            expedienteAux.setOrdenEntregaAnio(ordenEntregaAnio);
            expedienteAux.setExpMadre(expMadre);
            expedienteAux.setMesaDestino(mesaDestinoAux);
            expedienteAux.setMesaOrigen(mesaOrigenAux);
            expedienteAux.setUsuario(usuarioAux);
            expedienteAux.setEstado(true);
            expedienteAux.setEstadoExpediente(estadoAux);
            expedienteAux.setNroExp(nroExp);
            expedienteAux.setFechaCarga(new Date());
            expedienteAux.setUltimoMovimiento(new Date());
            this.create(expedienteAux);

            //historial
            HistorialExpediente historialAux = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historialAux.setFecha(expedienteAux.getFechaCarga());
            }
            historialAux.setFojas(fojas);
            historialAux.setObservacion(obs.toUpperCase());
            historialAux.setUsuario(usuarioAux);
            historialAux.setIniciador(iniciadorAux);
            historialAux.setMesaOrigen(mesaOrigenAux);
            historialAux.setMesaDestino(mesaDestinoAux);
            historialAux.setEstadoHistorial(estadoAux);
            historialAux.setEstado(true);
            this.historialFacade.create(historialAux);
            expedienteAux.getListHistoriales().add(historialAux);
            this.edit(expedienteAux);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en create()", ex);
            throw new Exception("Error al intentar crear el expediente.");
        }
        return expedienteAux;
    }

    @Override
    public Expediente create(Date fecha, Long codOrganismo, Long nro, int anio, int cuerpo, int fojas,
                             Long idIniciador, Long idTipoAsunto,
                             String asunto, String obs, String ordenPago,
                             Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, String expMadre,
                             Long idOrigen, Long idDestino,
                             Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a create(Date, Long, Long, int, ...) - codOrganismo: {0}, nro: {1}, anio: {2}, idOrigen: {3}, idDestino: {4}, idUsuario: {5}",
                new Object[]{codOrganismo, nro, anio, idOrigen, idDestino, idUsuario});
        Expediente expedienteAux = new Expediente();
        try {
            Estado estadoAux;
            Iniciador iniciadorAux = this.iniciadorFacade.find(idIniciador);
            TipoAsunto tipoAsuntoAux = this.tipoAsuntoFacade.find(idTipoAsunto);
            Mesa mesaOrigenAux = this.mesaFacade.find(idOrigen);
            Mesa mesaDestinoAux = this.mesaFacade.find(idDestino);
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            String nroExp = codOrganismo.toString() + "-" + nro.toString() + "/" + anio;

            if (idDestino.longValue() == idOrigen.longValue()) {
                estadoAux = this.estadoFacade.find(3L);//Estado Aceptado
            } else {
                estadoAux = this.estadoFacade.find(2L);//Estado Enviado
            }
            if (pedidoFondo != null && pedidoFondo == 999999) {
                estadoAux = this.estadoFacade.find(5L);//Estado Archivado
                pedidoFondo = null;
            }
            expedienteAux.setCodOrganismo(codOrganismo);
            expedienteAux.setNumCorrelativo(nro);
            expedienteAux.setAnio(anio);
            expedienteAux.setCuerpo(cuerpo);
            expedienteAux.setFechaCreacion(fecha);
            expedienteAux.setFojas(fojas);
            expedienteAux.setIniciador(iniciadorAux);
            expedienteAux.setTipoAsunto(tipoAsuntoAux);
            expedienteAux.setAsunto(asunto.toUpperCase());
            expedienteAux.setObservacion(obs.toUpperCase());
            expedienteAux.setOrdenPago(ordenPago);
            expedienteAux.setPedidoFondo(pedidoFondo);
            expedienteAux.setPedidoFondoAnio(pedidoFondoAnio);
            expedienteAux.setOrdenEntrega(ordenEntrega);
            expedienteAux.setOrdenEntregaAnio(ordenEntregaAnio);
            expedienteAux.setExpMadre(expMadre);
            expedienteAux.setMesaDestino(mesaDestinoAux);
            expedienteAux.setMesaOrigen(mesaOrigenAux);
            expedienteAux.setUsuario(usuarioAux);
            expedienteAux.setEstado(true);
            expedienteAux.setEstadoExpediente(estadoAux);
            expedienteAux.setNroExp(nroExp);
            expedienteAux.setFechaCarga(new Date());
            expedienteAux.setUltimoMovimiento(new Date());
            this.create(expedienteAux);

            //historial
            HistorialExpediente historialAux = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historialAux.setFecha(expedienteAux.getFechaCarga());
            }
            historialAux.setFojas(fojas);
            historialAux.setObservacion(obs.toUpperCase());
            historialAux.setUsuario(usuarioAux);
            historialAux.setIniciador(iniciadorAux);
            historialAux.setMesaOrigen(mesaOrigenAux);
            historialAux.setMesaDestino(mesaDestinoAux);
            historialAux.setEstadoHistorial(estadoAux);
            historialAux.setEstado(true);
            this.historialFacade.create(historialAux);
            expedienteAux.getListHistoriales().add(historialAux);
            this.edit(expedienteAux);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en create(Date, Long, Long, int, ...)", ex);
            throw new Exception("Error al intentar crear el expediente.");
        }
        return expedienteAux;
    }

    @Override
    public void edit(Long codOrganismo, Long numCorrelativo, int anio,
                     Date fechaCreacion, Integer cuerpo, Integer fojas,
                     Long idIniciador, Long idTipoAsunto, String asunto, String obs,
                     String ordenPago, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, String expMadre,
                     Long idDestino, Long idUsuario) throws java.lang.Exception {
        logger.log(Level.INFO,
                "Ingresando a edit() - codOrganismo: {0}, numCorrelativo: {1}, anio: {2}, idDestino: {3}, idUsuario: {4}",
                new Object[]{codOrganismo, numCorrelativo, anio, idDestino, idUsuario});
        try {
            Expediente expedienteAux = this.find(codOrganismo, numCorrelativo, anio);
            TipoAsunto tipoAsuntoAux = this.tipoAsuntoFacade.find(idTipoAsunto);
            Iniciador iniciadorAux = this.iniciadorFacade.find(idIniciador);
            Estado estadoAux = expedienteAux.getEstadoExpediente();
            Mesa mesaDestinoAux = this.mesaFacade.find(idDestino);
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);

            expedienteAux.setFechaCreacion(fechaCreacion);
            expedienteAux.setCuerpo(cuerpo);
            expedienteAux.setFojas(fojas);
            expedienteAux.setIniciador(iniciadorAux);
            expedienteAux.setTipoAsunto(tipoAsuntoAux);
            expedienteAux.setAsunto(asunto.toUpperCase());
            expedienteAux.setObservacion(obs.toUpperCase());
            expedienteAux.setOrdenPago(ordenPago);
            expedienteAux.setPedidoFondo(pedidoFondo);
            expedienteAux.setPedidoFondoAnio(pedidoFondoAnio);
            expedienteAux.setOrdenEntrega(ordenEntrega);
            expedienteAux.setOrdenEntregaAnio(ordenEntregaAnio);
            expedienteAux.setExpMadre(expMadre);
            expedienteAux.setMesaDestino(mesaDestinoAux);

            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(expedienteAux.getFojas());
            historial.setObservacion("El expediente fue editado. " + obs.toUpperCase());
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(expedienteAux.getMesaOrigen());
            historial.setMesaDestino(expedienteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoAux);
            historial.setListIntrumentosLegales(expedienteAux.getListInstrumentosLegales());
            historial.setEstado(true);
            this.historialFacade.create(historial);
            expedienteAux.getListHistoriales().add(historial);
            this.edit(expedienteAux);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en edit()", ex);
            throw new Exception("Error al intentar editar el expediente " + codOrganismo + "-" + numCorrelativo + "/" + anio + ".");
        }
    }

    @Override
    public void edit(Long codOrganismo, Long numCorrelativo, int anio, String obs, Long idDestino, Long idUsuario) throws java.lang.Exception {
        logger.log(Level.INFO,
                "Ingresando a edit(Long, Long, int, String, Long, Long) - codOrganismo: {0}, numCorrelativo: {1}, anio: {2}, idDestino: {3}, idUsuario: {4}",
                new Object[]{codOrganismo, numCorrelativo, anio, idDestino, idUsuario});
        try {
            Expediente expedienteAux = this.find(codOrganismo, numCorrelativo, anio);
            Estado estadoAux = expedienteAux.getEstadoExpediente();
            Mesa mesaDestinoAux = this.mesaFacade.find(idDestino);
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            expedienteAux.setObservacion(obs.toUpperCase());
            expedienteAux.setMesaDestino(mesaDestinoAux);
            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(expedienteAux.getFojas());
            historial.setObservacion("El expediente fue editado. " + obs.toUpperCase());
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(expedienteAux.getMesaOrigen());
            historial.setMesaDestino(expedienteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoAux);
            historial.setListIntrumentosLegales(expedienteAux.getListInstrumentosLegales());
            historial.setEstado(true);
            this.historialFacade.create(historial);
            expedienteAux.getListHistoriales().add(historial);
            expedienteAux.setUltimoMovimiento(new Date());
            this.edit(expedienteAux);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en edit(Long, Long, int, String, Long, Long)", ex);
            throw new Exception("Error al intentar editar el expediente " + codOrganismo + "-" + numCorrelativo + "/" + anio + ".");
        }
    }

    // ==========================
    //   CAMBIOS DE ESTADO
    // ==========================

    @Override
    public void aceptarExpediente(Long codigoOrgani, Long nroCorrel, int annio, Long idUsuario, Integer flagbandeja) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a aceptarExpediente() - codigoOrgani: {0}, nroCorrel: {1}, annio: {2}, idUsuario: {3}, flagbandeja: {4}",
                new Object[]{codigoOrgani, nroCorrel, annio, idUsuario, flagbandeja});
        try {
            Expediente expedienteAux = this.find(codigoOrgani, nroCorrel, annio);
            Estado estadoAux = this.estadoFacade.find(3L);// id Estado Aceptado 3L
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            if (usuarioAux == null || usuarioAux.getMesa() == null) {
                throw new Exception("No se pudo determinar la mesa del usuario que acepta.");
            }
            if (expedienteAux == null || expedienteAux.getMesaDestino() == null
                    || !expedienteAux.getMesaDestino().getId().equals(usuarioAux.getMesa().getId())) {
                throw new Exception("Solo puede aceptar expedientes que se encuentren en su bandeja de recibidos.");
            }
            expedienteAux.setEstadoExpediente(estadoAux);
            expedienteAux.setMesaOrigen(expedienteAux.getMesaOrigen());
            expedienteAux.setMesaDestino(expedienteAux.getMesaDestino());
            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(expedienteAux.getFojas());
            historial.setObservacion("El expediente fue aceptado.");
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(expedienteAux.getMesaOrigen());
            historial.setMesaDestino(expedienteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoAux);
            historial.setListIntrumentosLegales(expedienteAux.getListInstrumentosLegales());
            historial.setEstado(true);
            this.historialFacade.create(historial);
            expedienteAux.getListHistoriales().add(historial);
            //saco enviado al instrumento legal para posibles modificaciones
            for (InstrumentoLegal instrumentoLegal : expedienteAux.getListInstrumentosLegales()) {
                instrumentoLegal.setEnviado(false);
                this.instrumentoLegalFacade.edit(instrumentoLegal);
            }
            expedienteAux.setUltimoMovimiento(new Date());
            this.edit(expedienteAux);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en aceptarExpediente()", ex);
            throw new Exception("Error al intentar aceptar el expediente " + codigoOrgani + "-" + nroCorrel + "/" + annio + ".");
        }
    }

    @Override
    public void aceptarReenviarExpediente(Long codigoOrgani, Long nroCorrel, int annio, String observacion, Long idMesaDestino, Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a aceptarReenviarExpediente() - codigoOrgani: {0}, nroCorrel: {1}, annio: {2}, idMesaDestino: {3}, idUsuario: {4}",
                new Object[]{codigoOrgani, nroCorrel, annio, idMesaDestino, idUsuario});
        try {
            Expediente expedienteAux = this.find(codigoOrgani, nroCorrel, annio);
            Estado estadoAux = this.estadoFacade.find(3L);// id Estado Aceptado 3L
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            expedienteAux.setEstadoExpediente(estadoAux);
            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(expedienteAux.getFojas());
            historial.setObservacion("El expediente fue aceptado.");
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(expedienteAux.getMesaOrigen());
            historial.setMesaDestino(expedienteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoAux);
            historial.setListIntrumentosLegales(expedienteAux.getListInstrumentosLegales());
            historial.setEstado(true);
            this.historialFacade.create(historial);
            expedienteAux.getListHistoriales().add(historial);
            //saco enviado al instrumento legal para posibles modificaciones
            for (InstrumentoLegal instrumentoLegal : expedienteAux.getListInstrumentosLegales()) {
                instrumentoLegal.setEnviado(false);
                this.instrumentoLegalFacade.edit(instrumentoLegal);
            }
            this.edit(expedienteAux);
            this.enviarExpediente(expedienteAux, observacion, idMesaDestino, idUsuario);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en aceptarReenviarExpediente()", ex);
            throw new Exception("Error al intentar aceptar el expediente " + codigoOrgani + "-" + nroCorrel + "/" + annio + ".");
        }
    }

    public void rechazarExpediente(Long codigoOrgani, Long nroCorrel,
                                   int annio, String obs, Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a rechazarExpediente() - codigoOrgani: {0}, nroCorrel: {1}, annio: {2}, idUsuario: {3}",
                new Object[]{codigoOrgani, nroCorrel, annio, idUsuario});
        try {
            Estado estadoAux = this.estadoFacade.find(4L); // Rechazado
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            Expediente expedienteAux = this.find(codigoOrgani, nroCorrel, annio);
            expedienteAux.setEstadoExpediente(estadoAux);
            expedienteAux.setObservacion(obs.toUpperCase());

            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(expedienteAux.getFojas());
            historial.setObservacion(obs.toUpperCase());
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(expedienteAux.getMesaOrigen());
            historial.setMesaDestino(expedienteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoAux);
            historial.setListIntrumentosLegales(expedienteAux.getListInstrumentosLegales());
            historial.setEstado(true);
            this.historialFacade.create(historial);
            expedienteAux.getListHistoriales().add(historial);
            expedienteAux.setUltimoMovimiento(new Date());
            this.edit(expedienteAux);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en rechazarExpediente()", ex);
            throw new Exception("Error al intentar rechazar el expediente " + codigoOrgani + "-" + nroCorrel + "/" + annio + ".");
        }
    }

    @Override
    public void reenviarExpediente(Long codigoOrgani, Long nroCorrel,
                                   int annio, int fojas, String obs, Long idMesaOrigen,
                                   Long idMesaDestino, Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a reenviarExpediente() - codigoOrgani: {0}, nroCorrel: {1}, annio: {2}, idMesaOrigen: {3}, idMesaDestino: {4}, idUsuario: {5}",
                new Object[]{codigoOrgani, nroCorrel, annio, idMesaOrigen, idMesaDestino, idUsuario});
        try {
            Estado estadoAux = this.estadoFacade.find(2L);// Enviado
            Estado estadoDosAux = this.estadoFacade.find(7L);  // Reenviado
            Mesa mesaOrigenAux = this.mesaFacade.find(idMesaOrigen);
            Mesa mesaDestinoAux = this.mesaFacade.find(idMesaDestino);
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            Expediente expedinteAux = this.find(codigoOrgani, nroCorrel, annio);
            expedinteAux.setEstadoExpediente(estadoAux);
            expedinteAux.setMesaOrigen(mesaOrigenAux);
            expedinteAux.setMesaDestino(mesaDestinoAux);
            expedinteAux.setUsuario(usuarioAux);
            expedinteAux.setObservacion(obs.toUpperCase());
            expedinteAux.setFojas(fojas);

            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedinteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(expedinteAux.getFojas());
            historial.setObservacion(obs.toUpperCase());
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedinteAux.getIniciador());
            historial.setMesaOrigen(expedinteAux.getMesaOrigen());
            historial.setMesaDestino(expedinteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoDosAux);
            historial.setListIntrumentosLegales(expedinteAux.getListInstrumentosLegales());
            historial.setEstado(true);
            this.historialFacade.create(historial);
            expedinteAux.getListHistoriales().add(historial);
            expedinteAux.setUltimoMovimiento(new Date());
            this.edit(expedinteAux);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en reenviarExpediente()", ex);
            throw new Exception("Error al intentar reenviar el expediente " + codigoOrgani + "-" + nroCorrel + "/" + annio + " ." + ex.getMessage());
        }
    }

    public void archivarExpediente(Long codigoOrgani, Long nroCorrel,
                                   int annio, String obs, String motivo, Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a archivarExpediente() - codigoOrgani: {0}, nroCorrel: {1}, annio: {2}, idUsuario: {3}",
                new Object[]{codigoOrgani, nroCorrel, annio, idUsuario});
        try {
            Estado estadoAux = this.estadoFacade.find(5L);// Archivado
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            Expediente expedienteAux = this.find(codigoOrgani, nroCorrel, annio);
            expedienteAux.setEstadoExpediente(estadoAux);
            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(expedienteAux.getFojas());
            historial.setObservacion(obs + " [" + motivo + "]");
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(expedienteAux.getMesaOrigen());
            historial.setMesaDestino(expedienteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoAux);
            historial.setListIntrumentosLegales(expedienteAux.getListInstrumentosLegales());
            historial.setEstado(true);
            this.historialFacade.create(historial);
            expedienteAux.getListHistoriales().add(historial);
            expedienteAux.setUltimoMovimiento(new Date());
            this.edit(expedienteAux);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en archivarExpediente()", ex);
            throw new Exception("Error al intentar archivar el expediente " + codigoOrgani + "-" + nroCorrel + "/" + annio + " ." + ex.getMessage());
        }
    }

    @Override
    public void enviarExpediente(Long codigoOrgani, Long nroCorrel,
                                 int annio, int fojas, String obs, Long idMesaOrigen, Long idMesaDestino,
                                 String ordenPago, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio, String expMadre,
                                 Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a enviarExpediente() - codigoOrgani: {0}, nroCorrel: {1}, annio: {2}, idMesaOrigen: {3}, idMesaDestino: {4}, idUsuario: {5}",
                new Object[]{codigoOrgani, nroCorrel, annio, idMesaOrigen, idMesaDestino, idUsuario});
        try {
            Estado estadoAux = this.estadoFacade.find(2L);// Enviado
            Expediente expedienteAux = this.find(codigoOrgani, nroCorrel, annio);
            Mesa mesaOrigenAux = this.mesaFacade.find(idMesaOrigen);
            Mesa mesaDestinoAux = this.mesaFacade.find(idMesaDestino);
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);

            if (expedienteAux.getFojas() <= fojas) {
                expedienteAux.setEstadoExpediente(estadoAux);
                expedienteAux.setMesaOrigen(mesaOrigenAux);
                expedienteAux.setMesaDestino(mesaDestinoAux);
                expedienteAux.setUsuario(usuarioAux);
                expedienteAux.setObservacion(obs.toUpperCase());
                expedienteAux.setFojas(fojas);
                expedienteAux.setPedidoFondo(pedidoFondo);
                expedienteAux.setPedidoFondoAnio(pedidoFondoAnio);
                expedienteAux.setOrdenEntrega(ordenEntrega);
                expedienteAux.setOrdenEntregaAnio(ordenEntregaAnio);
                expedienteAux.setOrdenPago(ordenPago);
                expedienteAux.setExpMadre(expMadre);
                // Historial
                HistorialExpediente historial = new HistorialExpediente();
                if (expedienteAux.getFechaCreacion() != null) {
                    historial.setFecha(new Date());
                }
                historial.setFojas(expedienteAux.getFojas());
                historial.setObservacion(obs.toUpperCase());
                historial.setUsuario(usuarioAux);
                historial.setIniciador(expedienteAux.getIniciador());
                historial.setMesaOrigen(expedienteAux.getMesaOrigen());
                historial.setMesaDestino(expedienteAux.getMesaDestino());
                historial.setEstadoHistorial(estadoAux);
                historial.setListIntrumentosLegales(expedienteAux.getListInstrumentosLegales());
                historial.setEstado(true);
                this.historialFacade.create(historial);
                expedienteAux.getListHistoriales().add(historial);
                //agrego enviado al instrumento legal asi no se modifica mas
                for (InstrumentoLegal instrumentoLegal : expedienteAux.getListInstrumentosLegales()) {
                    instrumentoLegal.setEnviado(true);
                    this.instrumentoLegalFacade.edit(instrumentoLegal);
                }
                expedienteAux.setUltimoMovimiento(new Date());
                this.edit(expedienteAux);
                this.asegurarEstadoEnviado(expedienteAux, "enviarExpediente");

            } else {
                throw new Exception("Las fojas ingresadas son menores a las que tiene actualmente el expediente.");
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en enviarExpediente()", ex);
            throw new Exception("Error al intentar enviar el expediente " + codigoOrgani + "-" + nroCorrel + "/" + annio + " ." + ex.getMessage());
        }
    }

    @Override
    public void enviarExpediente(Expediente exp, String obs, Long idMesaDestino, Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a enviarExpediente(Expediente) - nroExp: {0}, idMesaDestino: {1}, idUsuario: {2}",
                new Object[]{exp != null ? exp.getNroExp() : "null", idMesaDestino, idUsuario});
        try {
            if (exp == null) {
                throw new Exception("El expediente recibido es nulo.");
            }
            String observacion = obs != null ? obs.toUpperCase() : "";
            Expediente expedienteAux = this.find(exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio());
            if (expedienteAux == null) {
                throw new Exception("No se encontró el expediente " + exp.getNroExp() + " para enviar.");
            }
            Estado estadoAux = this.estadoFacade.find(2L);// Enviado
            Mesa mesaDestinoAux = this.mesaFacade.find(idMesaDestino);
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);

            expedienteAux.setEstadoExpediente(estadoAux);
            expedienteAux.setMesaOrigen(usuarioAux.getMesa());
            expedienteAux.setMesaDestino(mesaDestinoAux);
            expedienteAux.setUsuario(usuarioAux);
            expedienteAux.setObservacion(observacion);
            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(expedienteAux.getFojas());
            historial.setObservacion(observacion);
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(expedienteAux.getMesaOrigen());
            historial.setMesaDestino(expedienteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoAux);
            historial.setListIntrumentosLegales(expedienteAux.getListInstrumentosLegales());
            historial.setEstado(true);
            this.historialFacade.create(historial);
            expedienteAux.getListHistoriales().add(historial);
            //agrego enviado al instrumento legal asi no se modifica mas
            for (InstrumentoLegal instrumentoLegal : expedienteAux.getListInstrumentosLegales()) {
                instrumentoLegal.setEnviado(true);
                this.instrumentoLegalFacade.edit(instrumentoLegal);
            }
            expedienteAux.setUltimoMovimiento(new Date());
            this.edit(expedienteAux);
            this.asegurarEstadoEnviado(expedienteAux, "enviarExpediente(Expediente)");

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en enviarExpediente(Expediente)", ex);
            throw new Exception("Error al intentar enviar el expediente " + (exp != null ? exp.getNroExp() : "N/D") + "." + ex.getMessage());
        }
    }

    private void asegurarEstadoEnviado(Expediente expediente, String contexto) {
        if (expediente == null) {
            logger.log(Level.WARNING, "asegurarEstadoEnviado: expediente nulo, contexto={0}", contexto);
            return;
        }
        final String nroExp = expediente.getNroExp();
        try {
            em.flush();
            em.refresh(expediente);

            Long idEstado = expediente.getEstadoExpediente() != null
                    ? expediente.getEstadoExpediente().getId() : null;
            if (expediente.getEstadoExpediente() == null || !Long.valueOf(2L).equals(idEstado)) {
                logger.log(Level.WARNING,
                        "El expediente {0} no quedó en estado ENVIADO tras {1}. Se corrige a 2L.",
                        new Object[]{nroExp, contexto});

                Estado estadoEnviado = this.estadoFacade.find(2L);
                expediente.setEstadoExpediente(estadoEnviado);
                expediente.setUltimoMovimiento(new Date());
                this.edit(expediente);
                em.flush();
                em.refresh(expediente);
                logger.log(Level.INFO,
                        "asegurarEstadoEnviado: aplicada corrección a ENVIADO (2) nroExp={0} contexto={1}",
                        new Object[]{nroExp, contexto});
            } else {
                logger.log(Level.INFO,
                        "asegurarEstadoEnviado: verificación OK, estado ENVIADO (2) nroExp={0} contexto={1}",
                        new Object[]{nroExp, contexto});
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING,
                    "asegurarEstadoEnviado: no se pudo verificar/corregir el estado ENVIADO del expediente {0} en {1}",
                    new Object[]{nroExp, contexto});
            logger.log(Level.FINE, "Detalle de la verificación de estado", ex);
        }
    }

    // ==========================
    //   INSTRUMENTOS LEGALES
    // ==========================

    public void agregarInstLeg(Long codigoOrgani, Long nroCorrel,
                               int annio, InstrumentoLegal instLeg, Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a agregarInstLeg() - codigoOrgani: {0}, nroCorrel: {1}, annio: {2}, idInstrumentoLegal: {3}, idUsuario: {4}",
                new Object[]{codigoOrgani, nroCorrel, annio, instLeg != null ? instLeg.getId() : "null", idUsuario});
        try {
            Expediente expedienteAux = this.find(codigoOrgani, nroCorrel, annio);
            int fojasActual = expedienteAux.getFojas();
            Estado estadoAux = expedienteAux.getEstadoExpediente();
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);

            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(fojasActual + instLeg.getFojas());//fojas nuevas del instrumento legal
            historial.setObservacion("Se vinculó un instrumento legal con " + instLeg.getFojas() + " fojas.");
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(expedienteAux.getMesaOrigen());
            historial.setMesaDestino(expedienteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoAux);
            historial.setListIntrumentosLegales(expedienteAux.getListInstrumentosLegales());
            historial.setEstado(true);
            this.historialFacade.create(historial);

            expedienteAux.setFojas(fojasActual + instLeg.getFojas());
            expedienteAux.getListInstrumentosLegales().add(instLeg);
            expedienteAux.getListHistoriales().add(historial);
            this.edit(expedienteAux);
            instLeg.setAgregado(true);
            this.instrumentoLegalFacade.edit(instLeg);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en agregarInstLeg()", ex);
            throw new Exception("Error al intentar agregar el Instrumento Legal. " + ex.getMessage());
        }
    }

    public void quitarInstLeg(Long codigoOrgani, Long nroCorrel,
                              int annio, InstrumentoLegal instLeg, Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a quitarInstLeg() - codigoOrgani: {0}, nroCorrel: {1}, annio: {2}, idInstrumentoLegal: {3}, idUsuario: {4}",
                new Object[]{codigoOrgani, nroCorrel, annio, instLeg != null ? instLeg.getId() : "null", idUsuario});
        try {
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            Expediente expedienteAux = this.find(codigoOrgani, nroCorrel, annio);
            Estado estadoAux = expedienteAux.getEstadoExpediente();
            int fojasActual = expedienteAux.getFojas();
            // Historial
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(new Date());
            }
            historial.setFojas(fojasActual - instLeg.getFojas());//fojas eliminadas
            historial.setObservacion("Se desvinculó un instrumento legal con " + instLeg.getFojas() + " fojas.");
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(expedienteAux.getMesaOrigen());
            historial.setMesaDestino(expedienteAux.getMesaDestino());
            historial.setEstadoHistorial(estadoAux);
            historial.setEstado(true);
            this.historialFacade.create(historial);

            expedienteAux.setFojas(fojasActual - instLeg.getFojas());
            expedienteAux.getListInstrumentosLegales().remove(instLeg);
            expedienteAux.getListHistoriales().add(historial);
            this.edit(expedienteAux);
            instLeg.setAgregado(false);
            this.instrumentoLegalFacade.edit(instLeg);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en quitarInstLeg()", ex);
            throw new Exception("Error al intentar desvincular el Instrumento Legal. " + ex.getMessage());
        }
    }

    @Override
    public String createExterno(Long codOrganismo, Long nroCorrel,
                                int annio, Long idMesaDestino, int fojas, Long idUsuario, boolean ingresarSiempre) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a createExterno() - codOrganismo: {0}, nroCorrel: {1}, annio: {2}, idMesaDestino: {3}, idUsuario: {4}, ingresarSiempre: {5}",
                new Object[]{codOrganismo, nroCorrel, annio, idMesaDestino, idUsuario, ingresarSiempre});
        Expediente expedienteAux;
        try {
            expedienteAux = this.find(codOrganismo, nroCorrel, annio);
            Mesa mesaOrigenAux = expedienteAux.getMesaDestino();
            Mesa mesaDestinoAux = this.mesaFacade.find(idMesaDestino);
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            Estado estadoAux = this.estadoFacade.find(3L);//Aceptado
            expedienteAux.setUsuario(usuarioAux);
            expedienteAux.setMesaOrigen(mesaOrigenAux);
            expedienteAux.setMesaDestino(mesaDestinoAux);
            expedienteAux.setEstadoExpediente(estadoAux);
            expedienteAux.setFojas(fojas);
            expedienteAux.setFechaCarga(new Date());

            if (ingresarSiempre) {
                HistorialExpediente historial = new HistorialExpediente();
                historial.setFecha(new Date());
                historial.setFojas(fojas);
                historial.setObservacion(expedienteAux.getObservacion());
                historial.setUsuario(usuarioAux);
                historial.setIniciador(expedienteAux.getIniciador());
                historial.setMesaOrigen(mesaOrigenAux);
                historial.setMesaDestino(mesaDestinoAux);
                historial.setEstadoHistorial(this.estadoFacade.find(2L));
                historial.setEstado(true);
                this.historialFacade.create(historial);
                expedienteAux.getListHistoriales().add(historial);
            }

            // Historial Ingreso externo
            HistorialExpediente historial = new HistorialExpediente();
            if (expedienteAux.getFechaCreacion() != null) {
                historial.setFecha(expedienteAux.getFechaCarga());
            }
            historial.setFojas(fojas);
            historial.setObservacion("Ingresó expediente Externo-Obtención.");
            historial.setUsuario(usuarioAux);
            historial.setIniciador(expedienteAux.getIniciador());
            historial.setMesaOrigen(mesaOrigenAux);
            historial.setMesaDestino(mesaDestinoAux);
            historial.setEstadoHistorial(estadoAux);
            historial.setEstado(true);
            this.historialFacade.create(historial);
            expedienteAux.getListHistoriales().add(historial);
            this.edit(expedienteAux);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en createExterno()", ex);
            throw new Exception("Error al intentar crear el expediente externo " + codOrganismo + "-" + nroCorrel + "/" + annio + ". " + ex.getMessage());
        }
        return expedienteAux.getNroExp();
    }

    // ==========================
    //   CONSULTAS AUXILIARES / REPORTES
    // ==========================

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countNotas(Long idMesa) {
        logger.log(Level.INFO, "Ingresando a countNotas() - idMesa: {0}", idMesa);
        Long nro = 0L;
        try {
            Long nroEnviados = this.countEnvidas(idMesa, 2L);
            Long nroRecibidos = this.countRecibidas(idMesa, 2L);
            Long nroAceptados = this.countAceptadas(idMesa, 3L);
            Long nroRechazados = this.countRechazada(idMesa, 4L);
            Long nroArchivados = this.countArchivada(idMesa, 5L);
            nro = nroEnviados + nroRecibidos + nroAceptados + nroRechazados + nroArchivados;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error en countNotas()", e);
            nro = 0L;
        }
        return nro;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countEnvidas(Long idMesa, Long idEstado) {
        logger.log(Level.INFO, "Ingresando a countEnvidas() - idMesa: {0}, idEstado: {1}",
                new Object[]{idMesa, idEstado});
        Long nro = 0L;
        try {
            Query consulta = em.createQuery(
                    "SELECT COUNT(o) FROM Expediente o WHERE o.estado = :p1 AND o.mesaOrigen.id = :p2 AND o.estadoExpediente.id = :p3");
            consulta.setParameter("p1", true);
            consulta.setParameter("p2", idMesa);
            consulta.setParameter("p3", idEstado);
            Number result = (Number) consulta.getSingleResult();
            if (result != null) {
                nro = result.longValue();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error en countEnvidas()", e);
            nro = 0L;
        }
        return nro;
    }

    @Override
    public Long countRecibidas(Long idMesa, Long idEstado) {
        logger.log(Level.INFO, "Ingresando a countRecibidas() - idMesa: {0}, idEstado: {1}",
                new Object[]{idMesa, idEstado});
        Long nro = 0L;
        try {
            Query consulta = em.createQuery(
                    "SELECT COUNT(o) FROM Expediente o WHERE o.estado = :p1 AND o.mesaDestino.id = :p2 AND o.estadoExpediente.id = :p3");
            consulta.setParameter("p1", true);
            consulta.setParameter("p2", idMesa);
            consulta.setParameter("p3", idEstado);
            Number result = (Number) consulta.getSingleResult();
            if (result != null) {
                nro = result.longValue();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error en countRecibidas()", e);
            nro = 0L;
        }
        return nro;
    }

    @Override
    public Long countAceptadas(Long idMesa, Long idEstado) {
        logger.log(Level.INFO, "Ingresando a countAceptadas() - idMesa: {0}, idEstado: {1}",
                new Object[]{idMesa, idEstado});
        Long nro = 0L;
        try {
            Query consulta = em.createQuery(
                    "SELECT COUNT(o) FROM Expediente o WHERE o.estado = :p1 AND o.mesaDestino.id = :p2 AND o.estadoExpediente.id = :p3");
            consulta.setParameter("p1", true);
            consulta.setParameter("p2", idMesa);
            consulta.setParameter("p3", idEstado);
            Number result = (Number) consulta.getSingleResult();
            if (result != null) {
                nro = result.longValue();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error en countAceptadas()", e);
            nro = 0L;
        }
        return nro;
    }

    @Override
    public Long countArchivada(Long idMesa, Long idEstado) {
        logger.log(Level.INFO, "Ingresando a countArchivada() - idMesa: {0}, idEstado: {1}",
                new Object[]{idMesa, idEstado});
        Long nro = 0L;
        try {
            Query consulta = em.createQuery(
                    "SELECT COUNT(o) FROM Expediente o WHERE o.estado = :p1 AND o.mesaDestino.id = :p2 AND o.estadoExpediente.id = :p3");
            consulta.setParameter("p1", true);
            consulta.setParameter("p2", idMesa);
            consulta.setParameter("p3", idEstado);
            Number result = (Number) consulta.getSingleResult();
            if (result != null) {
                nro = result.longValue();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error en countArchivada()", e);
            nro = 0L;
        }
        return nro;
    }

    @Override
    public Long countRechazada(Long idMesa, Long idEstado) {
        logger.log(Level.INFO, "Ingresando a countRechazada() - idMesa: {0}, idEstado: {1}",
                new Object[]{idMesa, idEstado});
        Long nro = 0L;
        try {
            Query consulta = em.createQuery(
                    "SELECT COUNT(o) FROM Expediente o WHERE o.estado = :p1 AND o.mesaOrigen.id = :p2 AND o.estadoExpediente.id = :p3");
            consulta.setParameter("p1", true);
            consulta.setParameter("p2", idMesa);
            consulta.setParameter("p3", idEstado);
            Number result = (Number) consulta.getSingleResult();
            if (result != null) {
                nro = result.longValue();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error en countRechazada()", e);
            nro = 0L;
        }
        return nro;
    }

    @Override
    public void eliminarExpediente(Long codOrganismo, Long numCorrelativo, int anio, Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a eliminarExpediente() - codOrganismo: {0}, numCorrelativo: {1}, anio: {2}, idUsuario: {3}",
                new Object[]{codOrganismo, numCorrelativo, anio, idUsuario});
        try {
            ExpedienteEliminado expedienteEliminado = new ExpedienteEliminado();
            Estado estadoAux = this.estadoFacade.find(8L);  // Eliminado
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            Expediente expedienteAux = this.find(codOrganismo, numCorrelativo, anio);

            //GUARDO EL EXPEDIENTE ELIMINADO
            ModelMapper modelMapper = new ModelMapper();
            expedienteEliminado = modelMapper.map(expedienteAux, ExpedienteEliminado.class);
            expedienteEliminado.setUsuarioElimina(usuarioAux);
            expedienteEliminado.setFechaElimina(new Date());
            this.expedienteEliminadoFacade.create(expedienteEliminado);

            //QUITAR INSTRUMENTOS LEGALES
            List<InstrumentoLegal> listAuxInstLegal = new ArrayList<>(expedienteAux.getListInstrumentosLegales());
            List<InstrumentoLegal> listAuxInstLegales = new ArrayList<>(expedienteAux.getListInstrumentosLegales());

            if (listAuxInstLegal != null) {
                this.quitarTodosInstLeg(codOrganismo,
                        numCorrelativo, anio, listAuxInstLegal, idUsuario);

                //ELIMINAR INSTRUMENTOS LEGALES
                for (InstrumentoLegal insAux : listAuxInstLegales) {
                    this.instrumentoLegalFacade.remove(insAux.getId());
                }
            }

            //Eliminar los Historiales
            expedienteAux.getListHistoriales().clear();

            this.remove(expedienteAux);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en eliminarExpediente()", ex);
            throw new Exception("Error al intentar eliminar el expediente " + codOrganismo + "-" + numCorrelativo + "/" + anio + " .");
        }
    }

    public void quitarTodosInstLeg(Long codigoOrgani, Long nroCorrel,
                                   int annio, List<InstrumentoLegal> instLeg, Long idUsuario) throws Exception {
        logger.log(Level.INFO,
                "Ingresando a quitarTodosInstLeg() - codigoOrgani: {0}, nroCorrel: {1}, annio: {2}, cantidadInstLeg: {3}, idUsuario: {4}",
                new Object[]{codigoOrgani, nroCorrel, annio, instLeg != null ? instLeg.size() : 0, idUsuario});
        try {
            Expediente expedienteAux = this.find(codigoOrgani, nroCorrel, annio);
            expedienteAux.setFojas(0);

            for (InstrumentoLegal instrumentoLegal : instLeg) {
                instrumentoLegal.setAgregado(false);
                this.instrumentoLegalFacade.edit(instrumentoLegal);
            }
            expedienteAux.getListInstrumentosLegales().removeAll(instLeg);
            this.edit(expedienteAux);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error en quitarTodosInstLeg()", ex);
            throw new Exception("Error al intentar desvincular los instrumentos legales. ");
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public boolean findAll(Date fechaMov, Date hora, Long idMesa, Long idDestino, Long idUsuario) {
        logger.log(Level.INFO,
                "Ingresando a findAll(Date, Date, Long, Long, Long) - fechaMov: {0}, hora: {1}, idMesa: {2}, idDestino: {3}, idUsuario: {4}",
                new Object[]{fechaMov, hora, idMesa, idDestino, idUsuario});
        boolean reporte = false;
        StringBuilder query = new StringBuilder();

        query.append("SELECT o FROM Expediente o, IN (o.listHistoriales) li WHERE o.estado = true");
        query.append(" AND li.estadoHistorial.id = 2");
        query.append(" AND li.mesaOrigen.id =").append(idMesa);

        if (fechaMov != null) {
            if (hora != null) {
                Timestamp desde = new Timestamp(fechaMov.getTime());
                Timestamp hasta = new Timestamp(fechaMov.getTime());

                Calendar cale = Calendar.getInstance();
                cale.setTime(fechaMov);
                cale.set(Calendar.HOUR, hora.getHours());
                cale.set(Calendar.MINUTE, hora.getMinutes());
                desde.setTime(cale.getTime().getTime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(fechaMov);
                cal.add(Calendar.DATE, 1);
                hasta.setTime(cal.getTime().getTime());

                query.append(" AND li.fecha BETWEEN '").append(desde).append("' AND '").append(hasta).append("' ");
            } else {
                Timestamp desde = new Timestamp(fechaMov.getTime());
                Timestamp hasta = new Timestamp(fechaMov.getTime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(fechaMov);
                cal.add(Calendar.DATE, 1);
                hasta.setTime(cal.getTime().getTime());
                query.append(" AND li.fecha BETWEEN '").append(desde).append("' AND '").append(hasta).append("' ");
            }
        }
        if (idDestino != null && idDestino != 0) {
            query.append(" AND li.mesaDestino.id =").append(idDestino);
        }

        if (idUsuario != null && idUsuario != 0) {
            query.append(" AND li.usuario.id =").append(idUsuario);
        }
        query.append(" ORDER BY li.fecha DESC");
        Query consulta = em.createQuery(query.toString());

        if (!consulta.getResultList().isEmpty()) {
            reporte = true;
        }
        return reporte;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public boolean findAll(Integer estado, Date fechaDesde, Date fechaHasta, Long origen, Long destino) {
        logger.log(Level.INFO,
                "Ingresando a findAll(Integer, Date, Date, Long, Long) - estado: {0}, fechaDesde: {1}, fechaHasta: {2}, origen: {3}, destino: {4}",
                new Object[]{estado, fechaDesde, fechaHasta, origen, destino});
        boolean reporte = false;
        if (estado != 7) {
            StringBuilder query = new StringBuilder();

            query.append("SELECT DISTINCT o FROM Expediente o, IN (o.listHistoriales) li WHERE o.estado = true");

            if (estado != 1) {
                query.append(" AND li.estadoHistorial.id = ").append(estado);
            } else {
                // Estado 1 = Enviados, buscar en historial con estado 2 (ENVIADO)
                query.append(" AND li.estadoHistorial.id = 2");
            }

            if (origen != null && origen != 0) {
                query.append(" AND li.mesaOrigen.id =").append(origen);
            }

            if (destino != null && destino != 0) {
                query.append(" AND li.mesaDestino.id =").append(destino);
            }

            query.append(" AND li.fecha >= '").append(fechaDesde).append("' AND li.fecha <= '").append(fechaHasta).append("' ");
            query.append(" ORDER BY li.fecha DESC");
            Query consulta = em.createQuery(query.toString());
            
            logger.log(Level.INFO, "Query generada: {0}", query.toString());

            if (!consulta.getResultList().isEmpty()) {
                reporte = true;
            }
        } else {
            StringBuilder query = new StringBuilder();
            query.append("SELECT MIN(h.fecha), e.cod_organismo, e.num_correlativo, e.anio FROM expedientes e ");
            query.append("INNER JOIN expedientes_histexp li ");
            query.append(" ON e.cod_organismo = li.cod_organismo ");
            query.append(" AND e.num_correlativo = li.num_correlativo ");
            query.append(" AND e.anio = li.anio ");
            query.append(" INNER JOIN histexp h ON h.id = li.listhistoriales_id");
            query.append(" WHERE e.estado = true ");
            query.append(" AND h.estadohistorial_id = 2");

            if (origen != null && origen != 0) {
                query.append(" AND h.mesaorigen_id =").append(origen);
            }

            if (destino != null && destino != 0) {
                query.append(" AND h.mesadestino_id =").append(destino);
            }

            query.append(" AND h.fecha BETWEEN '").append(fechaDesde).append("' AND '").append(fechaHasta).append("' ");
            query.append(" ORDER BY h.fecha DESC");

            Query consulta = em.createNativeQuery(query.toString(), Expediente.class);
            if (!consulta.getResultList().isEmpty()) {
                reporte = true;
            }
        }
        return reporte;
    }

    @Override
    public void agregarHistorial(Expediente expediente, Long id, int fojas, int fojasOld, String archivo, String archivoOld, Long idUsuario) {
        logger.log(Level.INFO,
                "Ingresando a agregarHistorial() - nroExp: {0}, idInstrumentoLegal: {1}, fojas: {2}, fojasOld: {3}, idUsuario: {4}",
                new Object[]{expediente != null ? expediente.getNroExp() : "null", id, fojas, fojasOld, idUsuario});
        int fojasActual = expediente.getFojas();
        Estado estadoAux = expediente.getEstadoExpediente();
        Usuario usuarioAux = this.usuarioFacade.find(idUsuario);

        // Historial
        HistorialExpediente historial = new HistorialExpediente();
        if (expediente.getFechaCreacion() != null) {
            historial.setFecha(new Date());
        }
        historial.setFojas(fojasActual - fojasOld + fojas);//fojas nuevas del instrumento legal
        if (fojasOld == fojas && archivo.equals(archivoOld)) {
            historial.setObservacion("Se editó el instrumento legal con ID " + id + ".");
        } else {
            if (fojasOld != fojas) {
                historial.setObservacion("Se editaron las fojas del instrumento legal con ID " + id + ". Tenía " + fojasOld + " y ahora posee " + fojas + ".");
            } else {
                historial.setObservacion("Se editó el archivo del instrumento legal con ID " + id + ".");
            }
        }
        historial.setUsuario(usuarioAux);
        historial.setIniciador(expediente.getIniciador());
        historial.setMesaOrigen(expediente.getMesaOrigen());
        historial.setMesaDestino(expediente.getMesaDestino());
        historial.setEstadoHistorial(estadoAux);
        historial.setListIntrumentosLegales(expediente.getListInstrumentosLegales());
        historial.setEstado(true);
        this.historialFacade.create(historial);
        expediente.setFojas(fojasActual - fojasOld + fojas);
        expediente.getListHistoriales().add(historial);
        this.edit(expediente);
    }

    // ==========================
    //   MÉTODOS NATIVOS SQL POR BANDEJA
    // ==========================

    /**
     * Método helper para construir la parte común de filtros SQL
     */
    private void appendFiltrosComunes(StringBuilder sql, Long codOrganismo, Long nro, Integer annio,
            Date fechaDesde, Date fechaHasta, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega,
            Integer ordenEntregaAnio, String expMadre, List<Object> params) {
        
        logger.log(Level.INFO, "appendFiltrosComunes - fechaDesde: {0}, fechaHasta: {1}", 
                new Object[]{fechaDesde, fechaHasta});
        
        if (codOrganismo != null) {
            sql.append(" AND e.cod_organismo = ?");
            params.add(codOrganismo);
        }
        if (nro != null) {
            sql.append(" AND e.num_correlativo = ?");
            params.add(nro);
        }
        if (annio != null) {
            sql.append(" AND e.anio = ?");
            params.add(annio);
        }
        if (fechaDesde != null && fechaHasta != null) {
            Timestamp fDesde = new Timestamp(fechaDesde.getTime());
            Calendar calDesde = Calendar.getInstance();
            calDesde.setTime(fechaDesde);
            calDesde.set(Calendar.HOUR_OF_DAY, 0);
            calDesde.set(Calendar.MINUTE, 0);
            calDesde.set(Calendar.SECOND, 0);
            calDesde.set(Calendar.MILLISECOND, 0);
            fDesde.setTime(calDesde.getTime().getTime());
            
            Timestamp fHasta = new Timestamp(fechaHasta.getTime());
            Calendar calHasta = Calendar.getInstance();
            calHasta.setTime(fechaHasta);
            calHasta.set(Calendar.HOUR_OF_DAY, 23);
            calHasta.set(Calendar.MINUTE, 59);
            calHasta.set(Calendar.SECOND, 59);
            calHasta.set(Calendar.MILLISECOND, 999);
            fHasta.setTime(calHasta.getTime().getTime());
            
            logger.log(Level.INFO, "Agregando filtro de fecha BETWEEN - fDesde: {0}, fHasta: {1}", 
                    new Object[]{fDesde, fHasta});
            sql.append(" AND e.fecha_creacion BETWEEN ? AND ?");
            params.add(fDesde);
            params.add(fHasta);
        } else {
            if (fechaDesde != null) {
                Timestamp fDesde = new Timestamp(fechaDesde.getTime());
                Calendar calDesde = Calendar.getInstance();
                calDesde.setTime(fechaDesde);
                calDesde.set(Calendar.HOUR_OF_DAY, 0);
                calDesde.set(Calendar.MINUTE, 0);
                calDesde.set(Calendar.SECOND, 0);
                calDesde.set(Calendar.MILLISECOND, 0);
                fDesde.setTime(calDesde.getTime().getTime());
                logger.log(Level.INFO, "Agregando filtro de fecha >= - fDesde: {0}", fDesde);
                sql.append(" AND e.fecha_creacion >= ?");
                params.add(fDesde);
            }
            if (fechaHasta != null) {
                Timestamp fHasta = new Timestamp(fechaHasta.getTime());
                Calendar calHasta = Calendar.getInstance();
                calHasta.setTime(fechaHasta);
                calHasta.set(Calendar.HOUR_OF_DAY, 23);
                calHasta.set(Calendar.MINUTE, 59);
                calHasta.set(Calendar.SECOND, 59);
                calHasta.set(Calendar.MILLISECOND, 999);
                fHasta.setTime(calHasta.getTime().getTime());
                logger.log(Level.INFO, "Agregando filtro de fecha <= - fHasta: {0}", fHasta);
                sql.append(" AND e.fecha_creacion <= ?");
                params.add(fHasta);
            }
        }
        if (idIniciador != null && idIniciador != 0) {
            sql.append(" AND e.iniciador_id = ?");
            params.add(idIniciador);
        }
        if (idTipoAsunto != null && idTipoAsunto != 0) {
            sql.append(" AND e.tipoasunto_id = ?");
            params.add(idTipoAsunto);
        }
        if (asunto != null && !asunto.equals("")) {
            sql.append(" AND UPPER(e.asunto) LIKE ?");
            params.add("%" + asunto.toUpperCase() + "%");
        }
        if (obs != null && !obs.equals("")) {
            sql.append(" AND UPPER(e.observacion) LIKE ?");
            params.add("%" + obs.toUpperCase() + "%");
        }
        if (pedidoFondo != null) {
            sql.append(" AND e.pedido_fondo = ?");
            params.add(pedidoFondo);
        }
        if (pedidoFondoAnio != null) {
            sql.append(" AND e.pedido_fondo_anio = ?");
            params.add(pedidoFondoAnio);
        }
        if (ordenEntrega != null) {
            sql.append(" AND e.orden_entrega = ?");
            params.add(ordenEntrega);
        }
        if (ordenEntregaAnio != null) {
            sql.append(" AND e.orden_entrega_anio = ?");
            params.add(ordenEntregaAnio);
        }
        if (expMadre != null && !expMadre.equals("")) {
            sql.append(" AND (UPPER(e.exp_madre) LIKE ? OR UPPER(e.nro_exp) LIKE ?)");
            params.add("%" + expMadre.toUpperCase() + "%");
            params.add("%" + expMadre.toUpperCase() + "%");
        }
    }

    /**
     * Construye la condición SQL para filtrar por fecha de último movimiento
     */
    private void appendFiltroFechaUltimoMov(StringBuilder sql, Date fechaDesdeUltimoMov, 
            Date fechaHastaUltimoMov, List<Object> params) {
        if (fechaDesdeUltimoMov != null || fechaHastaUltimoMov != null) {
            // Si solo se proporciona una fecha, tratarla como rango completo del día
            if (fechaDesdeUltimoMov != null && fechaHastaUltimoMov == null) {
                fechaHastaUltimoMov = fechaDesdeUltimoMov;
            }
            
            Timestamp fDesdeUltimoMov = null;
            Timestamp fHastaUltimoMov = null;
            
            if (fechaDesdeUltimoMov != null && fechaHastaUltimoMov != null) {
                fDesdeUltimoMov = new Timestamp(fechaDesdeUltimoMov.getTime());
                Calendar calDesdeUltimoMov = Calendar.getInstance();
                calDesdeUltimoMov.setTime(fechaDesdeUltimoMov);
                calDesdeUltimoMov.set(Calendar.HOUR_OF_DAY, 0);
                calDesdeUltimoMov.set(Calendar.MINUTE, 0);
                calDesdeUltimoMov.set(Calendar.SECOND, 0);
                calDesdeUltimoMov.set(Calendar.MILLISECOND, 0);
                fDesdeUltimoMov.setTime(calDesdeUltimoMov.getTime().getTime());
                
                fHastaUltimoMov = new Timestamp(fechaHastaUltimoMov.getTime());
                Calendar calHastaUltimoMov = Calendar.getInstance();
                calHastaUltimoMov.setTime(fechaHastaUltimoMov);
                calHastaUltimoMov.set(Calendar.HOUR_OF_DAY, 23);
                calHastaUltimoMov.set(Calendar.MINUTE, 59);
                calHastaUltimoMov.set(Calendar.SECOND, 59);
                calHastaUltimoMov.set(Calendar.MILLISECOND, 999);
                fHastaUltimoMov.setTime(calHastaUltimoMov.getTime().getTime());
                
                sql.append(" AND (COALESCE((SELECT MAX(h.fecha) FROM histexp h ");
                sql.append("INNER JOIN expedientes_histexp eh ON h.id = eh.listhistoriales_id ");
                sql.append("WHERE eh.cod_organismo = e.cod_organismo ");
                sql.append("AND eh.num_correlativo = e.num_correlativo ");
                sql.append("AND eh.anio = e.anio AND h.estado = true), e.fecha_creacion) ");
                sql.append("BETWEEN ? AND ?)");
                params.add(fDesdeUltimoMov);
                params.add(fHastaUltimoMov);
            } else {
                if (fechaDesdeUltimoMov != null) {
                    fDesdeUltimoMov = new Timestamp(fechaDesdeUltimoMov.getTime());
                    Calendar calDesdeUltimoMov = Calendar.getInstance();
                    calDesdeUltimoMov.setTime(fechaDesdeUltimoMov);
                    calDesdeUltimoMov.set(Calendar.HOUR_OF_DAY, 0);
                    calDesdeUltimoMov.set(Calendar.MINUTE, 0);
                    calDesdeUltimoMov.set(Calendar.SECOND, 0);
                    calDesdeUltimoMov.set(Calendar.MILLISECOND, 0);
                    fDesdeUltimoMov.setTime(calDesdeUltimoMov.getTime().getTime());
                    sql.append(" AND COALESCE((SELECT MAX(h.fecha) FROM histexp h ");
                    sql.append("INNER JOIN expedientes_histexp eh ON h.id = eh.listhistoriales_id ");
                    sql.append("WHERE eh.cod_organismo = e.cod_organismo ");
                    sql.append("AND eh.num_correlativo = e.num_correlativo ");
                    sql.append("AND eh.anio = e.anio AND h.estado = true), e.fecha_creacion) >= ?");
                    params.add(fDesdeUltimoMov);
                }
                if (fechaHastaUltimoMov != null) {
                    fHastaUltimoMov = new Timestamp(fechaHastaUltimoMov.getTime());
                    Calendar calHastaUltimoMov = Calendar.getInstance();
                    calHastaUltimoMov.setTime(fechaHastaUltimoMov);
                    calHastaUltimoMov.set(Calendar.HOUR_OF_DAY, 23);
                    calHastaUltimoMov.set(Calendar.MINUTE, 59);
                    calHastaUltimoMov.set(Calendar.SECOND, 59);
                    calHastaUltimoMov.set(Calendar.MILLISECOND, 999);
                    fHastaUltimoMov.setTime(calHastaUltimoMov.getTime().getTime());
                    sql.append(" AND COALESCE((SELECT MAX(h.fecha) FROM histexp h ");
                    sql.append("INNER JOIN expedientes_histexp eh ON h.id = eh.listhistoriales_id ");
                    sql.append("WHERE eh.cod_organismo = e.cod_organismo ");
                    sql.append("AND eh.num_correlativo = e.num_correlativo ");
                    sql.append("AND eh.anio = e.anio AND h.estado = true), e.fecha_creacion) <= ?");
                    params.add(fHastaUltimoMov);
                }
            }
        }
    }

    // ==========================
    //   MÉTODOS DE CONTEO NATIVOS POR BANDEJA
    // ==========================

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countEnviadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen) {
        logger.log(Level.INFO, "Ingresando a countEnviadosNative() - idMesaOrigen: {0}, idMesaDestino: {1}",
                new Object[]{idMesaOrigen, idMesaDestino});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM (SELECT DISTINCT e.cod_organismo, e.num_correlativo, e.anio FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 2 "); // Estado ENVIADO
        
        List<Object> params = new ArrayList<>();
        
        // Filtrar por mesa del usuario: (mesadestino_id = idMesa OR mesaorigen_id = idMesa)
        // Si solo se proporciona idMesaOrigen (mesa del usuario), usar OR
        // Si también se proporciona idMesaDestino desde filtros, aplicar ambos con AND
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            if (idMesaDestino != null && idMesaDestino != 0) {
                // Ambos filtros: aplicar AND
                sql.append("AND e.mesaorigen_id = ? ");
                sql.append("AND e.mesadestino_id = ? ");
                params.add(idMesaOrigen);
                params.add(idMesaDestino);
            } else {
                // Solo mesa del usuario: usar OR
                sql.append("AND (e.mesadestino_id = ? OR e.mesaorigen_id = ?) ");
                params.add(idMesaOrigen);
                params.add(idMesaOrigen);
            }
        } else if (idMesaDestino != null && idMesaDestino != 0) {
            // Solo mesaDestino desde filtros
            sql.append("AND e.mesadestino_id = ? ");
            params.add(idMesaDestino);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        sql.append(") AS subquery");
        
        Query consulta = em.createNativeQuery(sql.toString());
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
            } else {
                consulta.setParameter(paramIndex, param);
            }
            paramIndex++;
        }
        
        Object result = consulta.getSingleResult();
        return result instanceof Number ? ((Number) result).longValue() : 0L;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countRecibidosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen) {
        logger.log(Level.INFO, "Ingresando a countRecibidosNative() - idMesaDestino: {0}, idMesaOrigen: {1}",
                new Object[]{idMesaDestino, idMesaOrigen});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM (SELECT DISTINCT e.cod_organismo, e.num_correlativo, e.anio FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 2 "); // Estado ENVIADO
        
        List<Object> params = new ArrayList<>();
        
        // Bandeja Recibidos: debe pertenecer a la mesa destino del usuario.
        // Si se informa mesa origen en filtros, se agrega como restricción adicional.
        if (idMesaDestino != null && idMesaDestino != 0) {
            sql.append("AND e.mesadestino_id = ? ");
            params.add(idMesaDestino);
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                sql.append("AND e.mesaorigen_id = ? ");
                params.add(idMesaOrigen);
            }
        } else if (idMesaOrigen != null && idMesaOrigen != 0) {
            // Fallback defensivo si no llega mesa destino.
            sql.append("AND e.mesaorigen_id = ? ");
            params.add(idMesaOrigen);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        sql.append(") AS subquery");
        
        Query consulta = em.createNativeQuery(sql.toString());
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
            } else {
                consulta.setParameter(paramIndex, param);
            }
            paramIndex++;
        }
        
        Object result = consulta.getSingleResult();
        return result instanceof Number ? ((Number) result).longValue() : 0L;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countAceptadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen) {
        logger.log(Level.INFO, "Ingresando a countAceptadosNative() - idMesaDestino: {0}, idMesaOrigen: {1}",
                new Object[]{idMesaDestino, idMesaOrigen});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM (SELECT DISTINCT e.cod_organismo, e.num_correlativo, e.anio FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 3 "); // Estado ACEPTADO
        
        List<Object> params = new ArrayList<>();
        
        // Bandeja Recibidos: debe pertenecer a la mesa destino del usuario.
        // Si se informa mesa origen en filtros, se agrega como restricción adicional.
        if (idMesaDestino != null && idMesaDestino != 0) {
            sql.append("AND e.mesadestino_id = ? ");
            params.add(idMesaDestino);
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                sql.append("AND e.mesaorigen_id = ? ");
                params.add(idMesaOrigen);
            }
        } else if (idMesaOrigen != null && idMesaOrigen != 0) {
            // Fallback defensivo si no llega mesa destino.
            sql.append("AND e.mesaorigen_id = ? ");
            params.add(idMesaOrigen);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        sql.append(") AS subquery");
        
        Query consulta = em.createNativeQuery(sql.toString());
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
            } else {
                consulta.setParameter(paramIndex, param);
            }
            paramIndex++;
        }
        
        Object result = consulta.getSingleResult();
        return result instanceof Number ? ((Number) result).longValue() : 0L;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countRechazadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen) {
        logger.log(Level.INFO, "Ingresando a countRechazadosNative() - idMesaOrigen: {0}, idMesaDestino: {1}",
                new Object[]{idMesaOrigen, idMesaDestino});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM (SELECT DISTINCT e.cod_organismo, e.num_correlativo, e.anio FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 4 "); // Estado RECHAZADO
        
        List<Object> params = new ArrayList<>();
        
        // Filtrar por mesa del usuario: (mesadestino_id = idMesa OR mesaorigen_id = idMesa)
        // Si solo se proporciona idMesaOrigen (mesa del usuario), usar OR
        // Si también se proporciona idMesaDestino desde filtros, aplicar ambos con AND
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            if (idMesaDestino != null && idMesaDestino != 0) {
                // Ambos filtros: aplicar AND
                sql.append("AND e.mesaorigen_id = ? ");
                sql.append("AND e.mesadestino_id = ? ");
                params.add(idMesaOrigen);
                params.add(idMesaDestino);
            } else {
                // Solo mesa del usuario: usar OR
                sql.append("AND (e.mesadestino_id = ? OR e.mesaorigen_id = ?) ");
                params.add(idMesaOrigen);
                params.add(idMesaOrigen);
            }
        } else if (idMesaDestino != null && idMesaDestino != 0) {
            // Solo mesaDestino desde filtros
            sql.append("AND e.mesadestino_id = ? ");
            params.add(idMesaDestino);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        sql.append(") AS subquery");
        
        Query consulta = em.createNativeQuery(sql.toString());
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
            } else {
                consulta.setParameter(paramIndex, param);
            }
            paramIndex++;
        }
        
        Object result = consulta.getSingleResult();
        return result instanceof Number ? ((Number) result).longValue() : 0L;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countArchivadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen) {
        logger.log(Level.INFO, "Ingresando a countArchivadosNative() - idMesaOrigen: {0}, idMesaDestino: {1}, codOrganismo: {2}, nro: {3}, annio: {4}, fechaDesde: {5}, fechaHasta: {6}",
                new Object[]{idMesaOrigen, idMesaDestino, codOrganismo, nro, annio, fechaDesde, fechaHasta});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM (SELECT DISTINCT e.cod_organismo, e.num_correlativo, e.anio FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 5 "); // Estado ARCHIVADO
        
        List<Object> params = new ArrayList<>();
        
        // Para archivados, filtrar por mesa del usuario: (mesadestino_id = idMesa OR mesaorigen_id = idMesa)
        // Si solo se proporciona idMesaOrigen (mesa del usuario), usar OR
        // Si también se proporciona idMesaDestino desde filtros, aplicar ambos con AND
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            if (idMesaDestino != null && idMesaDestino != 0) {
                // Ambos filtros: aplicar AND
                sql.append("AND e.mesaorigen_id = ? ");
                sql.append("AND e.mesadestino_id = ? ");
                params.add(idMesaOrigen);
                params.add(idMesaDestino);
            } else {
                // Solo mesa del usuario: usar OR
                sql.append("AND (e.mesadestino_id = ? OR e.mesaorigen_id = ?) ");
                params.add(idMesaOrigen);
                params.add(idMesaOrigen);
            }
        } else if (idMesaDestino != null && idMesaDestino != 0) {
            // Solo mesaDestino desde filtros
            sql.append("AND e.mesadestino_id = ? ");
            params.add(idMesaDestino);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        sql.append(") AS subquery");
        
        String queryString = sql.toString();
        logger.log(Level.INFO, "Query SQL generada en countArchivadosNative: {0}", queryString);
        logger.log(Level.INFO, "Número de parámetros: {0}", params.size());
        
        Query consulta = em.createNativeQuery(queryString);
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
                logger.log(Level.INFO, "Parámetro {0} (TIMESTAMP): {1}", new Object[]{paramIndex, param});
            } else {
                consulta.setParameter(paramIndex, param);
                logger.log(Level.INFO, "Parámetro {0}: {1}", new Object[]{paramIndex, param});
            }
            paramIndex++;
        }
        
        Object result = consulta.getSingleResult();
        Long count = result instanceof Number ? ((Number) result).longValue() : 0L;
        logger.log(Level.INFO, "Total encontrado en countArchivadosNative: {0}", count);
        
        return count;
    }

    // ==========================
    //   MÉTODOS NATIVOS CON PAGINACIÓN POR BANDEJA
    // ==========================

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findEnviadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize) {
        logger.log(Level.INFO, "Ingresando a findEnviadosNative() con paginación - idMesaOrigen: {0}, idMesaDestino: {1}, first: {2}, pageSize: {3}",
                new Object[]{idMesaOrigen, idMesaDestino, first, pageSize});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT e.* FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 2 "); // Estado ENVIADO
        
        List<Object> params = new ArrayList<>();
        
        // Filtrar por mesa del usuario: (mesadestino_id = idMesa OR mesaorigen_id = idMesa)
        // Si solo se proporciona idMesaOrigen (mesa del usuario), usar OR
        // Si también se proporciona idMesaDestino desde filtros, aplicar ambos con AND
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            if (idMesaDestino != null && idMesaDestino != 0) {
                // Ambos filtros: aplicar AND
                sql.append("AND e.mesaorigen_id = ? ");
                sql.append("AND e.mesadestino_id = ? ");
                params.add(idMesaOrigen);
                params.add(idMesaDestino);
            } else {
                // Solo mesa del usuario: usar OR
                sql.append("AND (e.mesadestino_id = ? OR e.mesaorigen_id = ?) ");
                params.add(idMesaOrigen);
                params.add(idMesaOrigen);
            }
        } else if (idMesaDestino != null && idMesaDestino != 0) {
            // Solo mesaDestino desde filtros
            sql.append("AND e.mesadestino_id = ? ");
            params.add(idMesaDestino);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        
        sql.append(" ORDER BY e.ultimo_movimiento DESC");
        
        Query consulta = em.createNativeQuery(sql.toString(), Expediente.class);
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
            } else {
                consulta.setParameter(paramIndex, param);
            }
            paramIndex++;
        }
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findRecibidosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize) {
        logger.log(Level.INFO, "Ingresando a findRecibidosNative() con paginación - idMesaDestino: {0}, idMesaOrigen: {1}, first: {2}, pageSize: {3}",
                new Object[]{idMesaDestino, idMesaOrigen, first, pageSize});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT e.* FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 2 "); // Estado ENVIADO
        
        List<Object> params = new ArrayList<>();
        
        // Filtrar por mesa del usuario: (mesadestino_id = idMesa OR mesaorigen_id = idMesa)
        // Si solo se proporciona idMesaDestino (mesa del usuario), usar OR
        // Si también se proporciona idMesaOrigen desde filtros, aplicar ambos con AND
        if (idMesaDestino != null && idMesaDestino != 0) {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                // Ambos filtros: aplicar AND
                sql.append("AND e.mesadestino_id = ? ");
                sql.append("AND e.mesaorigen_id = ? ");
                params.add(idMesaDestino);
                params.add(idMesaOrigen);
            } else {
                // Solo mesa del usuario: usar OR
                sql.append("AND (e.mesadestino_id = ? OR e.mesaorigen_id = ?) ");
                params.add(idMesaDestino);
                params.add(idMesaDestino);
            }
        } else if (idMesaOrigen != null && idMesaOrigen != 0) {
            // Solo mesaOrigen desde filtros
            sql.append("AND e.mesaorigen_id = ? ");
            params.add(idMesaOrigen);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        
        sql.append(" ORDER BY e.ultimo_movimiento DESC");
        
        Query consulta = em.createNativeQuery(sql.toString(), Expediente.class);
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
            } else {
                consulta.setParameter(paramIndex, param);
            }
            paramIndex++;
        }
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findAceptadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize) {
        logger.log(Level.INFO, "Ingresando a findAceptadosNative() con paginación - idMesaDestino: {0}, idMesaOrigen: {1}, first: {2}, pageSize: {3}",
                new Object[]{idMesaDestino, idMesaOrigen, first, pageSize});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT e.* FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 3 "); // Estado ACEPTADO
        
        List<Object> params = new ArrayList<>();
        
        // Filtrar por mesa del usuario: (mesadestino_id = idMesa OR mesaorigen_id = idMesa)
        // Si solo se proporciona idMesaDestino (mesa del usuario), usar OR
        // Si también se proporciona idMesaOrigen desde filtros, aplicar ambos con AND
        if (idMesaDestino != null && idMesaDestino != 0) {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                // Ambos filtros: aplicar AND
                sql.append("AND e.mesadestino_id = ? ");
                sql.append("AND e.mesaorigen_id = ? ");
                params.add(idMesaDestino);
                params.add(idMesaOrigen);
            } else {
                // Solo mesa del usuario: usar OR
                sql.append("AND (e.mesadestino_id = ? OR e.mesaorigen_id = ?) ");
                params.add(idMesaDestino);
                params.add(idMesaDestino);
            }
        } else if (idMesaOrigen != null && idMesaOrigen != 0) {
            // Solo mesaOrigen desde filtros
            sql.append("AND e.mesaorigen_id = ? ");
            params.add(idMesaOrigen);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        
        sql.append(" ORDER BY e.ultimo_movimiento DESC");
        
        Query consulta = em.createNativeQuery(sql.toString(), Expediente.class);
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
            } else {
                consulta.setParameter(paramIndex, param);
            }
            paramIndex++;
        }
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findRechazadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize) {
        logger.log(Level.INFO, "Ingresando a findRechazadosNative() con paginación - idMesaOrigen: {0}, idMesaDestino: {1}, first: {2}, pageSize: {3}",
                new Object[]{idMesaOrigen, idMesaDestino, first, pageSize});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT e.* FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 4 "); // Estado RECHAZADO
        
        List<Object> params = new ArrayList<>();
        
        // Filtrar por mesa del usuario: (mesadestino_id = idMesa OR mesaorigen_id = idMesa)
        // Si solo se proporciona idMesaOrigen (mesa del usuario), usar OR
        // Si también se proporciona idMesaDestino desde filtros, aplicar ambos con AND
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            if (idMesaDestino != null && idMesaDestino != 0) {
                // Ambos filtros: aplicar AND
                sql.append("AND e.mesaorigen_id = ? ");
                sql.append("AND e.mesadestino_id = ? ");
                params.add(idMesaOrigen);
                params.add(idMesaDestino);
            } else {
                // Solo mesa del usuario: usar OR
                sql.append("AND (e.mesadestino_id = ? OR e.mesaorigen_id = ?) ");
                params.add(idMesaOrigen);
                params.add(idMesaOrigen);
            }
        } else if (idMesaDestino != null && idMesaDestino != 0) {
            // Solo mesaDestino desde filtros
            sql.append("AND e.mesadestino_id = ? ");
            params.add(idMesaDestino);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        
        sql.append(" ORDER BY e.ultimo_movimiento DESC");
        
        Query consulta = em.createNativeQuery(sql.toString(), Expediente.class);
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
            } else {
                consulta.setParameter(paramIndex, param);
            }
            paramIndex++;
        }
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findArchivadosNative(Long codOrganismo, Long nro, Integer annio, Date fechaDesde, Date fechaHasta,
            Date fechaDesdeUltimoMov, Date fechaHastaUltimoMov, Long idIniciador, Long idTipoAsunto, String asunto,
            String obs, Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaDestino, Long idMesaOrigen, int first, int pageSize) {
        logger.log(Level.INFO, "Ingresando a findArchivadosNative() con paginación - idMesaOrigen: {0}, idMesaDestino: {1}, first: {2}, pageSize: {3}, codOrganismo: {4}, nro: {5}, annio: {6}, fechaDesde: {7}, fechaHasta: {8}",
                new Object[]{idMesaOrigen, idMesaDestino, first, pageSize, codOrganismo, nro, annio, fechaDesde, fechaHasta});
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT e.* FROM expedientes e ");
        sql.append("WHERE e.estado = true ");
        sql.append("AND e.estadoexpediente_id = 5 "); // Estado ARCHIVADO
        
        List<Object> params = new ArrayList<>();
        
        // Para archivados, filtrar por mesa del usuario: (mesadestino_id = idMesa OR mesaorigen_id = idMesa)
        // Si solo se proporciona idMesaOrigen (mesa del usuario), usar OR
        // Si también se proporciona idMesaDestino desde filtros, aplicar ambos con AND
        if (idMesaOrigen != null && idMesaOrigen != 0) {
            if (idMesaDestino != null && idMesaDestino != 0) {
                // Ambos filtros: aplicar AND
                sql.append("AND e.mesaorigen_id = ? ");
                sql.append("AND e.mesadestino_id = ? ");
                params.add(idMesaOrigen);
                params.add(idMesaDestino);
            } else {
                // Solo mesa del usuario: usar OR
                sql.append("AND (e.mesadestino_id = ? OR e.mesaorigen_id = ?) ");
                params.add(idMesaOrigen);
                params.add(idMesaOrigen);
            }
        } else if (idMesaDestino != null && idMesaDestino != 0) {
            // Solo mesaDestino desde filtros
            sql.append("AND e.mesadestino_id = ? ");
            params.add(idMesaDestino);
        }
        
        appendFiltrosComunes(sql, codOrganismo, nro, annio, fechaDesde, fechaHasta, idIniciador, idTipoAsunto,
                asunto, obs, pedidoFondo, pedidoFondoAnio, ordenEntrega, ordenEntregaAnio, expMadre, params);
        appendFiltroFechaUltimoMov(sql, fechaDesdeUltimoMov, fechaHastaUltimoMov, params);
        
        sql.append(" ORDER BY e.ultimo_movimiento DESC");
        
        String queryString = sql.toString();
        logger.log(Level.INFO, "Query SQL generada en findArchivadosNative (con paginación): {0}", queryString);
        logger.log(Level.INFO, "Número de parámetros: {0}", params.size());
        
        Query consulta = em.createNativeQuery(queryString, Expediente.class);
        int paramIndex = 1;
        for (Object param : params) {
            if (param instanceof Timestamp) {
                consulta.setParameter(paramIndex, (Timestamp) param, jakarta.persistence.TemporalType.TIMESTAMP);
                logger.log(Level.INFO, "Parámetro {0} (TIMESTAMP): {1}", new Object[]{paramIndex, param});
            } else {
                consulta.setParameter(paramIndex, param);
                logger.log(Level.INFO, "Parámetro {0}: {1}", new Object[]{paramIndex, param});
            }
            paramIndex++;
        }
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        
        List<Expediente> resultado = consulta.getResultList();
        logger.log(Level.INFO, "Resultados encontrados en findArchivadosNative (con paginación): {0}", resultado.size());
        
        return resultado;
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countAllAvanzada(Long codOrganismo, Long nro,
            Integer annio, Date fechaDesde, Date fechaHasta, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, Long idTipoAsunto, String asunto,
            String ordenPago,
            Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaUsuario, boolean consultarHistorial, String observacion) {
        StringBuilder query = new StringBuilder();
        query.append("select COUNT(o) from Expediente as o ");

        if (consultarHistorial) {
            query.append(" INNER JOIN FETCH o.listHistoriales h WHERE o.estado = true ");
        } else {
            query.append(" WHERE o.estado = true ");
        }

        if (codOrganismo != null) {
            query.append(" and o.codOrganismo =").append(codOrganismo);
        }
        if (nro != null) {
            query.append(" and o.numCorrelativo =").append(nro);
        }
        if (annio != null) {
            query.append(" and o.anio =").append(annio);
        }
        if (codOrganismo != null && nro != null && annio != null) {
            query.append(" OR o.expMadre LIKE '%").append(codOrganismo).append("-").append(nro).append("/").append(annio).append("%' ");
        }
        if (fechaDesde != null && fechaHasta != null) {
            Timestamp fDesde = new Timestamp(fechaDesde.getTime());
            Timestamp fHasta = new Timestamp(fechaHasta.getTime());
            if (fechaDesde.equals(fechaHasta)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fechaHasta);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                fHasta.setTime(cal.getTime().getTime());
            }
            query.append(" and o.fechaCreacion BETWEEN '").append(fDesde).append("' and '").append(fHasta).append("' ");
        } else {
            if (fechaDesde != null) {
                Timestamp fDesde = new Timestamp(fechaDesde.getTime());
                query.append(" and o.fechaCreacion >= '").append(fDesde).append("' ");
            }
            if (fechaHasta != null) {
                Timestamp fHasta = new Timestamp(fechaHasta.getTime());
                query.append(" and o.fechaCreacion <= '").append(fHasta).append("' ");
            }
        }
        if (idIniciador != null && idIniciador != 0) {
            query.append(" and o.iniciador.id =").append(idIniciador);
        }
        if (consultarHistorial) {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                query.append(" and h.mesaOrigen.id =").append(idMesaOrigen);
            }
            if (idMesaDestino != null && idMesaDestino != 0) {
                query.append(" and h.mesaDestino.id =").append(idMesaDestino);
            }
        } else {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                query.append(" and o.mesaOrigen.id =").append(idMesaOrigen);
            }
            if (idMesaDestino != null && idMesaDestino != 0) {
                query.append(" and o.mesaDestino.id =").append(idMesaDestino);
            }
        }
        if (observacion != null && !observacion.isEmpty()) {
            query.append(" and (upper(h.observacion)) like '%").append(Utilidad.desacentuar(observacion.toUpperCase()).replace(" ", "%")).append("%'");
        }
        if (pedidoFondo != null) {
            query.append(" and o.pedidoFondo = ").append(pedidoFondo);
        }
        if (pedidoFondoAnio != null) {
            query.append(" and o.pedidoFondoAnio = ").append(pedidoFondoAnio);
        }
        if (ordenEntrega != null) {
            query.append(" and o.ordenEntrega = ").append(ordenEntrega);
        }
        if (ordenEntregaAnio != null) {
            query.append(" and o.ordenEntregaAnio = ").append(ordenEntregaAnio);
        }
        if (expMadre != null && !expMadre.equals("")) {
            query.append(" and (upper(o.expMadre) LIKE '%").append(expMadre.toUpperCase()).append("%'");
            query.append(" OR upper(o.nroExp) LIKE '%").append(expMadre.toUpperCase()).append("%')");
        }
        if (idTipoAsunto != null && idTipoAsunto != 0) {
            query.append(" and o.tipoAsunto.id =").append(idTipoAsunto);
        }
        if (asunto != null && !asunto.equals("")) {
            query.append(" and upper(o.asunto) LIKE '%").append(Utilidad.desacentuar(asunto).toUpperCase()).append("%'");
        }
        if (ordenPago != null && !ordenPago.equals("")) {
            query.append(" and upper(o.ordenPago) LIKE '%").append(Utilidad.desacentuar(ordenPago).toUpperCase()).append("%'");
        }
        Query consulta = em.createQuery(query.toString());
        Long total = (Long) consulta.getSingleResult();
        return total;
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Expediente> findAllAvanzada(Long codOrganismo, Long nro,
            Integer annio, Date fechaDesde, Date fechaHasta, Long idIniciador,
            Long idMesaOrigen, Long idMesaDestino, Long idTipoAsunto, String asunto,
            String ordenPago,
            Integer pedidoFondo, Integer pedidoFondoAnio, Integer ordenEntrega, Integer ordenEntregaAnio,
            String expMadre, Long idMesaUsuario, boolean consultarHistorial, String observacion, int first, int pageSize) {
        StringBuilder query = new StringBuilder();
        query.append("select  DISTINCT o from Expediente as o ");

        if (consultarHistorial) {
            query.append(" INNER JOIN FETCH o.listHistoriales h WHERE o.estado = true ");
        } else {
            query.append(" WHERE o.estado = true ");
        }

        if (codOrganismo != null) {
            query.append(" and o.codOrganismo =").append(codOrganismo);
        }
        if (nro != null) {
            query.append(" and o.numCorrelativo =").append(nro);
        }
        if (annio != null) {
            query.append(" and o.anio =").append(annio);
        }
//        if (idMesaUsuario != null) {
//            query.append(" and (h.mesaOrigen.id =").append(idMesaUsuario).append(" or ").append(" h.mesaDestino.id = ").append(idMesaUsuario).append(") ");
//        }
        if (codOrganismo != null && nro != null && annio != null) {
            query.append(" OR o.expMadre LIKE '%").append(codOrganismo).append("-").append(nro).append("/").append(annio).append("%' ");
        }
        if (fechaDesde != null && fechaHasta != null) {
            Timestamp fDesde = new Timestamp(fechaDesde.getTime());
            Timestamp fHasta = new Timestamp(fechaHasta.getTime());
            if (fechaDesde.equals(fechaHasta)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fechaHasta);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                fHasta.setTime(cal.getTime().getTime());
            }
            query.append(" and o.fechaCreacion BETWEEN '").append(fDesde).append("' and '").append(fHasta).append("' ");
        } else {
            if (fechaDesde != null) {
                Timestamp fDesde = new Timestamp(fechaDesde.getTime());
                query.append(" and o.fechaCreacion >= '").append(fDesde).append("' ");
            }
            if (fechaHasta != null) {
                Timestamp fHasta = new Timestamp(fechaHasta.getTime());
                query.append(" and o.fechaCreacion <= '").append(fHasta).append("' ");
            }
        }
        if (idIniciador != null && idIniciador != 0) {
            query.append(" and o.iniciador.id =").append(idIniciador);
        }
        if (consultarHistorial) {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                query.append(" and h.mesaOrigen.id =").append(idMesaOrigen);
            }
            if (idMesaDestino != null && idMesaDestino != 0) {
                query.append(" and h.mesaDestino.id =").append(idMesaDestino);
            }
        } else {
            if (idMesaOrigen != null && idMesaOrigen != 0) {
                query.append(" and o.mesaOrigen.id =").append(idMesaOrigen);
            }
            if (idMesaDestino != null && idMesaDestino != 0) {
                query.append(" and o.mesaDestino.id =").append(idMesaDestino);
            }
        }
        if (observacion != null && !observacion.isEmpty()) {
            query.append(" and (upper(h.observacion)) like '%").append(Utilidad.desacentuar(observacion.toUpperCase()).replace(" ", "%")).append("%'");
        }
        if (pedidoFondo != null) {
            query.append(" and o.pedidoFondo = ").append(pedidoFondo);
        }
        if (pedidoFondoAnio != null) {
            query.append(" and o.pedidoFondoAnio = ").append(pedidoFondoAnio);
        }
        if (ordenEntrega != null) {
            query.append(" and o.ordenEntrega = ").append(ordenEntrega);
        }
        if (ordenEntregaAnio != null) {
            query.append(" and o.ordenEntregaAnio = ").append(ordenEntregaAnio);
        }
        if (expMadre != null && !expMadre.equals("")) {
            query.append(" and (upper(o.expMadre) LIKE '%").append(expMadre.toUpperCase()).append("%'");
            query.append(" OR upper(o.nroExp) LIKE '%").append(expMadre.toUpperCase()).append("%')");
        }
        if (idTipoAsunto != null && idTipoAsunto != 0) {
            query.append(" and o.tipoAsunto.id =").append(idTipoAsunto);
        }
        if (asunto != null && !asunto.equals("")) {
            query.append(" and upper(o.asunto) LIKE '%").append(Utilidad.desacentuar(asunto).toUpperCase()).append("%'");
        }
        if (ordenPago != null && !ordenPago.equals("")) {
            query.append(" and upper(o.ordenPago) LIKE '%").append(Utilidad.desacentuar(ordenPago).toUpperCase()).append("%'");
        }

        query.append(" order by o.ultimoMovimiento DESC, o.anio DESC, o.numCorrelativo DESC, o.codOrganismo  ");
        Query consulta = em.createQuery(query.toString());
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        return consulta.getResultList();
    }

}