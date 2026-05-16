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
import org.ccpm.sysse.entity.Leyenda;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class LeyendaFacade extends AbstractFacade<Leyenda> implements LeyendaFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public LeyendaFacade() {
        super(Leyenda.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Leyenda> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Leyenda as o WHERE o.estado = :p1 ORDER BY o.anio ASC");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Leyenda> findAll(Long anio, String leyenda) {
        StringBuilder query = new StringBuilder();
        query.append("select * FROM leyendas as o WHERE o.estado = true");

        if (anio != null) {
            query.append(" and o.anio =").append(anio);
        }
        if (!leyenda.equals("")) {
            query.append(" and upper(o.leyenda) LIKE '%").append(Utilidad.desacentuar(leyenda).toUpperCase()).append("%'");
        }
        query.append(" ORDER BY o.anio ASC");
        Query consulta = em.createNativeQuery(query.toString(), Leyenda.class);
        return consulta.getResultList();
    }

    @Override
    public void create(Long anio, String leyenda) throws Exception {
        try {
            Leyenda objectAux = new Leyenda();
            objectAux.setAnio(anio);
            objectAux.setNombre(leyenda);
            objectAux.setEstado(true);
            this.create(objectAux);            
        } catch (Exception e) {
            throw new Exception("Error al intentar crear la leyenda.");
        }
    }

    @Override
    public void remove(Long id) throws Exception {
        try {
            Leyenda objectAux = this.find(id);            
            this.remove(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar la leyenda.");
        }
    }

    @Override
    public void edit(Long id, Long anioNuevo, String leyenda) throws Exception {
        try {
            Leyenda objectAux = this.find(id);
            objectAux.setAnio(anioNuevo);
            objectAux.setNombre(leyenda);            
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la leyenda.");
        }
    } 
    
}
