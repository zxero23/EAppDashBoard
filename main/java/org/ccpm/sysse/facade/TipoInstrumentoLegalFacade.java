package org.ccpm.sysse.facade;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.TipoInstrumentoLegal;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class TipoInstrumentoLegalFacade extends AbstractFacade<TipoInstrumentoLegal> implements TipoInstrumentoLegalFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TipoInstrumentoLegalFacade() {
        super(TipoInstrumentoLegal.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <TipoInstrumentoLegal> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from TipoInstrumentoLegal as o WHERE o.estado = :p1 order by o.nombre");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <TipoInstrumentoLegal> findAll(String nombre) {
        StringBuilder query = new StringBuilder();
        query.append("select * FROM tiposintlegal as o WHERE o.estado = true ");
        if (nombre != null && !nombre.isEmpty()) {            
            query.append(" and upper(o.nombre) LIKE '%").append(Utilidad.desacentuar(nombre).toUpperCase()).append("%'");           
        }
        query.append(" ORDER BY o.nombre ASC");
        Query consulta = em.createNativeQuery(query.toString(), TipoInstrumentoLegal.class);
        return consulta.getResultList();
    }
       
    @Override
    public void create(String nombre, String descripcion) throws Exception {
        try {
            TipoInstrumentoLegal objectAux = new TipoInstrumentoLegal();
            objectAux.setNombre(nombre.toUpperCase());
            objectAux.setDescripcion(descripcion.toUpperCase());
            objectAux.setEstado(true);
            this.create(objectAux);            
        } catch (Exception e) {
            throw new Exception("Error al intentar crear el tipo de instrumento legal.");
        }
    }
    
    @Override
    public void remove(Long idTipoInstLegal) throws Exception {
        try {
            TipoInstrumentoLegal objectAux = this.find(idTipoInstLegal);
            objectAux.setEstado(false);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar el tipo de instrumento legal.");
        }
    }
    
    @Override
    public void edit(Long idTipoInstLegal, String nombre, String descripcion) throws Exception {
        try {
            TipoInstrumentoLegal objectAux = this.find(idTipoInstLegal);
            objectAux.setNombre(nombre.toUpperCase());
            objectAux.setDescripcion(descripcion.toUpperCase());            
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el tipo de instrumento legal.");
        }
    }
}
