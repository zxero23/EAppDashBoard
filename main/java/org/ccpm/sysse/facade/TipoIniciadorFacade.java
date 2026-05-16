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
import org.ccpm.sysse.entity.TipoIniciador;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class TipoIniciadorFacade extends AbstractFacade<TipoIniciador> implements TipoIniciadorFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TipoIniciadorFacade() {
        super(TipoIniciador.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <TipoIniciador> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from TipoIniciador as o WHERE o.estado = :p1 ORDER BY o.nombre");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <TipoIniciador> findAll(String nombre) {
        StringBuilder query = new StringBuilder();
        query.append("select * FROM tipos_iniciadores as o WHERE o.estado = true ");
        if (nombre != null && !nombre.isEmpty()) {            
            query.append(" and upper(o.nombre) LIKE '%").append(Utilidad.desacentuar(nombre).toUpperCase()).append("%'");           
        }
        query.append(" ORDER BY o.nombre ASC");
        Query consulta = em.createNativeQuery(query.toString(), TipoIniciador.class);
        return consulta.getResultList();
    }
       
    @Override
    public void create(String nombre, Boolean externo) throws Exception {
        try {
            TipoIniciador objectAux = new TipoIniciador();
            objectAux.setNombre(nombre.toUpperCase());
            objectAux.setExterno(externo);
            objectAux.setEstado(true);
            this.create(objectAux);            
        } catch (Exception e) {
            throw new Exception("Error al intentar crear el tipo de iniciador.");
        }
    }
    
    @Override
    public void remove(Long idTipoIniciador) throws Exception {
        try {
            TipoIniciador objectAux = this.find(idTipoIniciador);
            objectAux.setEstado(false);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar el tipo de iniciador.");
        }
    }
    
    @Override
    public void edit(Long idTipoIniciador, String nombre, Boolean externo) throws Exception {
        try {
            TipoIniciador objectAux = this.find(idTipoIniciador);
            objectAux.setNombre(nombre.toUpperCase());
            objectAux.setExterno(externo);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el tipo de iniciador.");
        }
    }
}
