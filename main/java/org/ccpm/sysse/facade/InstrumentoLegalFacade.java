package org.ccpm.sysse.facade;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.InstrumentoLegal;
import org.ccpm.sysse.entity.TipoInstrumentoLegal;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class InstrumentoLegalFacade extends AbstractFacade<InstrumentoLegal> implements InstrumentoLegalFacadeLocal {

    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;
    @EJB
    private TipoInstrumentoLegalFacadeLocal tipoIsntrumentoLegalFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public InstrumentoLegalFacade() {
        super(InstrumentoLegal.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<InstrumentoLegal> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from InstrumentoLegal as o WHERE o.estado = :p1");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<InstrumentoLegal> findAll(String descripcion) {
        return em.createQuery("select object(o) FROM InstrumentoLegal as o WHERE o.estado = true AND " + Utilidad.desacentuar("o.descripcion") + " LIKE '%" + descripcion + "%' ORDER BY o.descripcion, o.numero, o.id").getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<InstrumentoLegal> findAll(String descripcion, Long idUsuario) {
        StringBuilder query = new StringBuilder();
        query.append("select * FROM instlegales as o WHERE o.estado = true and o.usuarioCreacion_id =").append(idUsuario);
        if (!descripcion.equals("")) {
            query.append(" and upper(o.descripcion) LIKE '%").append(Utilidad.desacentuar(descripcion).toUpperCase()).append("%'");
        }
        query.append(" ORDER BY o.descripcion, o.numero, o.id");
        Query consulta = em.createNativeQuery(query.toString(), InstrumentoLegal.class);
        return consulta.getResultList();
    }
    
    @Override
    public List<InstrumentoLegal> findAll(String descripcion, Long codigo, Long correlativo, Integer anio){
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM instlegales as i ");
        query.append("INNER JOIN expedientes_instlegales as ei ON ei.listinstrumentoslegales_id=i.id ");
        query.append(" WHERE i.estado = true ");
        
        if (descripcion!=null && !descripcion.equals("")) {
            query.append(" AND upper(i.descripcion) LIKE '%").append(Utilidad.desacentuar(descripcion).toUpperCase()).append("%'");
        }
        if (codigo!=null && correlativo !=null && anio!=null) {
            query.append(" AND ei.cod_organismo=").append(codigo);
            query.append(" AND ei.num_correlativo=").append(correlativo);
            query.append(" AND ei.anio=").append(anio);
        }
        query.append(" ORDER BY i.id ASC");
        Query consulta = em.createNativeQuery(query.toString(), InstrumentoLegal.class);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    //Averiguar donde se usa
    public List<InstrumentoLegal> findAll(Long idUsuario) {
        Query consulta = em.createQuery("select object(o) from InstrumentoLegal as o WHERE o.estado = :p1 and o.usuarioCreacion.id = :p2 ORDER BY o.descripcion, o.numero, o.id");
        consulta.setParameter("p1", true);
        consulta.setParameter("p2", idUsuario);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<InstrumentoLegal> findAllSinAgregar(Long idUsuario) {
        Query consulta = em.createQuery("select object(o) from InstrumentoLegal as o WHERE o.estado = :p1 and o.usuarioCreacion.id = :p2 and o.agregado = :p3 ORDER BY o.descripcion, o.numero, o.id");
        consulta.setParameter("p1", true);
        consulta.setParameter("p2", idUsuario);
        consulta.setParameter("p3", false);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<InstrumentoLegal> findAll(List<String> instLegalId) {
        List<InstrumentoLegal> lstRetorno = new ArrayList<InstrumentoLegal>();
        for (String idAux : instLegalId) {
            InstrumentoLegal instLegalAux = this.find(Long.parseLong(idAux));
            lstRetorno.add(instLegalAux);
        }
        return lstRetorno;
    }

    @Override
    public InstrumentoLegal create(String nro, String descripcion, int fojas,
            Long idTipoInstrumentoLegal, String archivo, Long idUsuario) throws Exception {
        try {
            //verifico que fojas no sea negativo
            if (fojas < 0) {
                throw new Exception("El valor en las fojas no puede ser negativo.");
            }
            TipoInstrumentoLegal tipoIsntrumentoLegalAux = this.tipoIsntrumentoLegalFacade.find(idTipoInstrumentoLegal);
            Usuario usuarioAux = this.usuarioFacade.find(idUsuario);
            InstrumentoLegal instrumentoLegalAux = new InstrumentoLegal();
            instrumentoLegalAux.setTipoInstrumentoLegal(tipoIsntrumentoLegalAux);
            instrumentoLegalAux.setDescripcion(descripcion.toUpperCase());
            instrumentoLegalAux.setNumero(nro);
            instrumentoLegalAux.setFojas(fojas);
            instrumentoLegalAux.setEstado(true);
            instrumentoLegalAux.setAgregado(false);
            if (archivo.isEmpty()) {
                instrumentoLegalAux.setArchivo(null);
            } else {
                instrumentoLegalAux.setArchivo(archivo);
            }
            instrumentoLegalAux.setUsuarioCreacion(usuarioAux);
            //El instrumento legal aun no se envio 
            instrumentoLegalAux.setEnviado(false);
            this.create(instrumentoLegalAux);
            return instrumentoLegalAux;
        } catch (Exception ex) {
            throw new Exception("Error al intentar crear el Instrumento Legal.");
        }
    }

    @Override
    public void edit(Long id, String nro, String descripcion, int foja,
            Long idTipoInstrumentoLegal, String archivo) throws Exception {

        try {
            InstrumentoLegal objectAux = this.find(id);
            TipoInstrumentoLegal instrumentoLegalAux = this.tipoIsntrumentoLegalFacade.find(idTipoInstrumentoLegal);
            objectAux.setNumero(nro);
            objectAux.setDescripcion(descripcion.toUpperCase());
            objectAux.setFojas(foja);
            objectAux.setTipoInstrumentoLegal(instrumentoLegalAux);
            if (archivo.isEmpty()) {
                objectAux.setArchivo(null);
            } else {
                objectAux.setArchivo(archivo);
            }
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el instrumento legal.");
        }
    }

    @Override
    public void remove(Long id) throws Exception {
        try {
            InstrumentoLegal objectAux = this.find(id);
            if (objectAux.isAgregado()) {
                throw new Exception("El instrumento legal no puede borrarse por estar agregado a un expediente.");
            }
            objectAux.setEstado(false);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar el instrumento legal.");
        }
    }
}
