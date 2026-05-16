package org.ccpm.sysse.facade;

import java.util.Date;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.Organismo;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class OrganismoFacade extends AbstractFacade<Organismo> implements OrganismoFacadeLocal {

    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public OrganismoFacade() {
        super(Organismo.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Organismo> findByCodigo(Long codOrganismo) {
        try {
            Query consulta = em.createQuery("select object(o) from Organismo as o WHERE o.estado = :p1 and o.codOrganigrama = :p2 ORDER BY o.codOrganigrama ASC");
            consulta.setParameter("p1", true);
            consulta.setParameter("p2", codOrganismo);
            return consulta.getResultList();
        } catch (Exception ex) {
            return null;
        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Organismo> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Organismo as o WHERE o.estado = :p1 ORDER BY o.codOrganigrama ASC");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Organismo> findAll(String nombre) {
        return em.createQuery("select object(o) FROM Organismo as o WHERE  o.estado = true AND " + Utilidad.desacentuar("o.nombre") + " LIKE '%" + nombre + "%' ORDER BY o.nombre ").getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Organismo> findAll(Long codigo, String nombre,Long id) {
        StringBuilder query = new StringBuilder();
        query.append("select object(o) FROM Organismo as o WHERE o.estado = true ");
        if (id != null && !(id == 0)) {
            query.append(" and o.id = ").append(id);
        }
        if (codigo != null) {
            query.append(" and o.codOrganigrama =").append(codigo);
        }
        if (!nombre.equals("")) {
            query.append(" and o.nombre LIKE '%").append(Utilidad.desacentuar(nombre).toUpperCase()).append("%'");
        }
        query.append(" ORDER BY o.codOrganigrama ASC");
        Query consulta = em.createQuery(query.toString());
        return consulta.getResultList();
    }

    @Override
    public void create(Long idOrganismoPadre, Long codOrganigrama,
            String nombre, String descripcion) throws Exception {
        try {
            Organismo objectAux = new Organismo();
            objectAux.setCodOrganigrama(codOrganigrama);
            objectAux.setNombre(nombre.toUpperCase());
            objectAux.setDescripcion(descripcion.toUpperCase());
            objectAux.setEstado(true);
            this.create(objectAux);
            if (idOrganismoPadre != 0L) {
                Organismo organismoPadre = this.find(idOrganismoPadre);
                objectAux.setOrganismoPadre(organismoPadre);
                this.edit(objectAux);
                organismoPadre.getOrganismos().add(objectAux);
                this.edit(organismoPadre);
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar crear el organismo.");
        }
    }

    @Override
    public void remove(Long idOrganismo) throws Exception {
        try {
            Organismo objectAux = this.find(idOrganismo);
            objectAux.setEstado(false);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar el organismo.");
        }
    }

    @Override
    public void edit(Long idOrganismo, String nombre,
            String descripcion, Date fecha, Long nro, Long idOrganismoPadre, boolean implementa) throws Exception {
        try {
            Organismo objectAux = this.find(idOrganismo);
            objectAux.setNombre(nombre.toUpperCase());
            objectAux.setDescripcion(descripcion.toUpperCase());
            if (fecha != null) {
                objectAux.setFechaInicio(fecha);
            }
            if (idOrganismoPadre != null && idOrganismoPadre != 0L) {
                objectAux.setOrganismoPadre(this.find(idOrganismoPadre));
            }
            objectAux.setImplementa(implementa);
            objectAux.setCodOrganigrama(nro);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el organismo.");
        }
    }
}
