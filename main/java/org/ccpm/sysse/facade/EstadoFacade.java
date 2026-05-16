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
import org.ccpm.sysse.entity.Estado;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class EstadoFacade extends AbstractFacade<Estado> implements EstadoFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EstadoFacade() {
        super(Estado.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Estado> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Estado as o WHERE o.estado = :p1");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Estado> findAll(String nombre) {
        return em.createQuery("select object(o) FROM Estado as o WHERE o.estado = true AND "+Utilidad.desacentuar("upper(o.nombre)")+" LIKE '%" + nombre.toUpperCase() + "%' ORDER BY o.nombre ").getResultList();
    }
       
    @Override
    public void create(String nombre, String descripcion) throws Exception {
        try {
            Estado objectAux = new Estado();
            objectAux.setNombre(nombre.toUpperCase());
            objectAux.setDescripcion(descripcion.toUpperCase());
            objectAux.setEstado(true);
            this.create(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar crear el estado "+nombre+".");
        }
    }
    
    @Override
    public void remove(Long idEstado) throws Exception {
        try {
            Estado objectAux = this.find(idEstado);
            objectAux.setEstado(false);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar el estado "+ idEstado +".");
        }
    }
    
    @Override
    public void edit(Long idEstado, String nombre, String descripcion) throws Exception {
        try {
            Estado objectAux = this.find(idEstado);
            objectAux.setNombre(nombre.toUpperCase());
            objectAux.setDescripcion(descripcion.toUpperCase());            
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el estado "+idEstado+".");
        }
    }   
    
}
