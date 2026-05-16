package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.Iniciador;
import org.ccpm.sysse.entity.Mesa;

@Stateless
public class IniciadorFacade extends AbstractFacade<Iniciador> implements IniciadorFacadeLocal {

    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public IniciadorFacade() {
        super(Iniciador.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Iniciador> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Iniciador as o WHERE  o.estado = :p1 order by o.nombre");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
        
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Iniciador> findAllByTipo(Long idTipoInciador) {
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("EAppSysSE-ejbPU");
            EntityManager entityManager = emf.createEntityManager();
            StringBuilder query = new StringBuilder();
            query.append("SELECT object(i) FROM Iniciador as i WHERE i.estado = true and i.tipoIniciador.id = :p1");
            query.append(" ORDER BY i.nombre");//Trae los iniciadores
            Query consulta = entityManager.createQuery(query.toString(), Iniciador.class);
            consulta.setParameter("p1", idTipoInciador);
            List<Iniciador> retorno = consulta.getResultList();
            entityManager.close();
            return retorno;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Mesa findMesaEntrada(Long codOrganismo) {
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("EAppSysSE-ejbPU");
            EntityManager entityManager = emf.createEntityManager();
            StringBuilder query = new StringBuilder();
            query.append("SELECT object(m) FROM Mesa as m  WHERE  m.estado = true ");
            query.append(" and m.organismo.codOrganigrama=").append(codOrganismo);
            query.append(" and m.nombre like '%").append("MESA DE ENTRADAS Y SALIDAS").append("%'");
            Query consulta = entityManager.createQuery(query.toString(), Mesa.class);
            List<Mesa> mesas = consulta.getResultList();
            entityManager.close();
            return mesas.isEmpty() ? null : mesas.get(0);

        } catch (Exception e) {
            return null;
        }
    }
}
