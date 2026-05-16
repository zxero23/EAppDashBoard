package org.ccpm.sysse.facade;

import java.util.Date;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.Mesa;
import org.ccpm.sysse.entity.Organismo;
import org.ccpm.sysse.entity.TipoIniciador;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class MesaFacade extends AbstractFacade<Mesa> implements MesaFacadeLocal {

    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    @EJB
    private TipoIniciadorFacadeLocal tipoIniciadorFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MesaFacade() {
        super(Mesa.class);
    }
    
    /**
     *
     * @param mesasId
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Mesa> findMesas(List <Long> mesasId) {
        String iDes = mesasId.toString().substring(1, mesasId.toString().length()-1);
        return em.createQuery("select object (m) from Mesa as m where m.id in (" + iDes +  " ) ").getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Mesa> findAll(boolean estado) {
        Query consulta = em.createQuery("select  object(o) from Mesa as o WHERE  o.estado = :p1 ORDER BY o.nombre");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Organismo findOrganismoMesa(Long idMesa) {
        Query consulta = em.createQuery("select object(o) from Mesa as o WHERE  o.estado = :p1 and o.id = :p2 ORDER BY o.nombre");
        consulta.setParameter("p1", true);
        consulta.setParameter("p2", idMesa);
        Mesa mesaAux = (Mesa) consulta.getSingleResult();
        return mesaAux.getOrganismo();

    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Mesa> findAll(String nombre, Long organismo, Long id) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT object(o) FROM Mesa as o WHERE o.estado = true  ");
        if (id != null && !(id == 0)) {
            query.append(" and o.id = ").append(id);
        }
        if (nombre != null && !nombre.isEmpty()) {
            query.append(" and upper(o.nombre) LIKE '%").append(Utilidad.desacentuar(nombre).toUpperCase()).append("%' ");
        }
        if (organismo != null && !(organismo == 0)) {
            query.append(" and o.organismo.id = ").append(organismo);
        }
        query.append(" ORDER BY o.organismo.codOrganigrama,o.organismo,o.nombre ASC");
        Query consulta = em.createQuery(query.toString());

        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Mesa> findAllGeneral(Long organismo, Long organismoAux) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("EAppSysSE-ejbPU");
        EntityManager entityManager = emf.createEntityManager();
        StringBuilder query = new StringBuilder();
        Long idHacienda = 1L;
        Long idSubse = 412L;
        if (organismoAux != null && organismoAux != 0L) {
            organismo = organismoAux;
        }
        query.append("SELECT * FROM mesas AS m ");
        query.append(" INNER JOIN iniciador AS o ON m.id = o.id  ");
        query.append(" WHERE o.estado = true ");

        query.append(" AND (m.is_mesa_general = TRUE ");
        if (organismo != null && !(organismo == 0)) {
            query.append(" OR m.organismo_id IN (WITH RECURSIVE tbsons as (");
            query.append("  SELECT * FROM organismo WHERE id= ");
            query.append(organismo);
            if (organismo.equals(idHacienda)) {
                query.append(" or id=").append(idSubse);
            } else if (organismo.equals(idSubse)) {
                query.append(" or id=").append(idHacienda);
            }
            query.append("  UNION ALL");
            query.append("  SELECT organismo.* FROM organismo  JOIN tbsons  ON organismo.organismopadre_id=tbsons.id )");
            query.append(" SELECT id FROM tbsons ORDER BY id) ");
        }
        query.append(" ) ");
        query.append(" ORDER BY o.nombre ASC");
        Query consulta = entityManager.createNativeQuery(query.toString(), Mesa.class);
        List<Mesa> retorno = consulta.getResultList();
        entityManager.close();
        return retorno;
    }
    
    @Override
    public void create(Long idOrganismo, String nombre, String descripcion,
            boolean checked) throws Exception {
        try {
            Organismo organismoAux = this.organismoFacade.find(idOrganismo);
            TipoIniciador tipoIniciadorAux = this.tipoIniciadorFacade.find(1L);//Mesa tiene ID 1L
            if (organismoAux.getNombre().equals("MUNICIPALIDAD")) {
                tipoIniciadorAux = this.tipoIniciadorFacade.find(8L);
            }
            if (organismoAux.getNombre().equals("JUZGADO")) {
                tipoIniciadorAux = this.tipoIniciadorFacade.find(7L);
            }
            if (organismoAux.getNombre().equals("HOGAR")) {
                tipoIniciadorAux = this.tipoIniciadorFacade.find(12L);
            }
            Mesa objectAux = new Mesa();
            objectAux.setNombre(organismoAux.getCodOrganigrama() + " - " + nombre.toUpperCase());
            objectAux.setDescripcion(descripcion.toUpperCase());
            objectAux.setOrganismo(organismoAux);
            objectAux.setFechaAlta(new Date());
            objectAux.setMesaGeneral(checked);
            objectAux.setTipoIniciador(tipoIniciadorAux);
            objectAux.setEstado(true);
            this.create(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar crear la mesa.");
        }
    }

    @Override
    public void remove(Long idMesa) throws Exception {
        try {
            Mesa objectAux = this.find(idMesa);
            objectAux.setEstado(false);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar la mesa.");
        }
    }

    @Override
    public void edit(Long idMesa, Long idOrganismo, String nombre,
            String descripcion, boolean checked) throws Exception {
        try {
            Organismo organismoAux = this.organismoFacade.find(idOrganismo);
            TipoIniciador tipoIniciadorAux = this.tipoIniciadorFacade.find(1L);//Mesa tiene ID 1L
            if (organismoAux.getNombre().equals("MUNICIPALIDAD")) {
                tipoIniciadorAux = this.tipoIniciadorFacade.find(8L);
            }
            if (organismoAux.getNombre().equals("JUZGADO")) {
                tipoIniciadorAux = this.tipoIniciadorFacade.find(7L);
            }
            if (organismoAux.getNombre().equals("HOGAR")) {
                tipoIniciadorAux = this.tipoIniciadorFacade.find(12L);
            }
            Mesa objectAux = this.find(idMesa);
            objectAux.setNombre(organismoAux.getCodOrganigrama() + " - " + nombre.toUpperCase());
            objectAux.setDescripcion(descripcion.toUpperCase());
            objectAux.setTipoIniciador(tipoIniciadorAux);
            objectAux.setOrganismo(organismoAux);
            objectAux.setFechaAlta(new Date());
            objectAux.setMesaGeneral(checked);
            objectAux.setEstado(true);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la mesa.");
        }
    }

    @Override
    public List<Mesa> findByOrganismo(Long idOrganismo) {
         Query consulta = em.createQuery("select  object(o) from Mesa as o WHERE  o.estado = true and o.organismo.id = :p1 ORDER BY o.organismo.codOrganigrama");
        consulta.setParameter("p1", idOrganismo);
        return consulta.getResultList();
    }
    
    /**
     *
     * @param idOrganismo
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Mesa> findAllGeneral(Long idOrganismo) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT object(o) FROM Mesa as o WHERE o.estado = true and o.mesaGeneral = true ");
        if (idOrganismo != null && !(idOrganismo == 0)) {
            query.append(" and (o.organismo.id = ").append(idOrganismo);
            query.append(" or o.organismo.organismoPadre.id = ").append(idOrganismo).append(" ) ");
        }
        query.append(" ORDER BY o.organismo.codOrganigrama ASC");
        Query consulta = em.createQuery(query.toString());

        return consulta.getResultList();
    }
}
