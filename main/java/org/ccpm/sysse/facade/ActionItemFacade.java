package org.ccpm.sysse.facade;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.ActionItem;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class ActionItemFacade extends AbstractFacade<ActionItem> implements ActionItemFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;
    @EJB
    private MenuFacadeLocal menuFacade;
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ActionItemFacade() {
        super(ActionItem.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <ActionItem> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from ActionItem as o WHERE o.estado = :p1 ORDER BY o.nombre ASC");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <ActionItem> findAll(String nombreAccion) {
        return em.createQuery("select object(o) FROM ActionItem as o WHERE o.estado = true AND "+Utilidad.desacentuar("upper(o.nombre)")+" LIKE '%" + nombreAccion.toUpperCase() + "%' ORDER BY o.nombre ASC").getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <ActionItem> findAll(Long idMenu) {
        String jpqlString = "SELECT OBJECT(r) FROM Menu AS m, IN (m.acciones) r where m.id=" + idMenu;
        return em.createQuery(jpqlString).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <ActionItem> findAll(List <Long> actionsId) {
        String iDes = actionsId.toString().substring(1, actionsId.toString().length()-1);
        return em.createQuery("select object (act) from ActionItem as act where act.id in (" + iDes + ")").getResultList();
    }

    @Override
    public void create(String nombre) throws Exception {
        try {
            ActionItem actionItems = new ActionItem();
            actionItems.setNombre(nombre);
            actionItems.setEstado(true);
            this.create(actionItems);
        } catch (Exception e) {
            throw new Exception("Error al intentar crear la acción "+nombre+".");
        }
    }
    
    @Override
    public void remove(Long idAccion) throws Exception {
        try {
            ActionItem actionItems = this.find(idAccion);
            actionItems.setEstado(false);
            this.edit(actionItems);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar la acción "+idAccion+".");
        }
    }
    
    @Override
    public void edit(Long idAccion, String nombreAccion) throws Exception {
        try {
            ActionItem actionItems = this.find(idAccion);
            actionItems.setNombre(nombreAccion);
            this.edit(actionItems);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la acción "+idAccion+"- "+nombreAccion+".");
        }
    }    
}
