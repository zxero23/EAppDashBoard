/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Menu;

/**
 *
 * @author Alvarenga Angel
 */
@Stateless
public class ActionItemFacade extends AbstractFacade<ActionItem> implements ActionItemFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
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
        Query consulta = em.createQuery("select object(o) from ActionItem as o WHERE o.estado = :p1");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <ActionItem> findAll(String nombreAccion) {
        return em.createQuery("select object(o) FROM ActionItem as o WHERE o.estado = true AND o.nombre LIKE '" + nombreAccion + "%' ORDER BY o.nombre ").getResultList();
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
    public void create(Long idMenuPadre, String nombre) throws Exception {
        try {
            ActionItem actionItems = new ActionItem();
            actionItems.setNombre(nombre);
            actionItems.setEstado(true);
            this.create(actionItems);
            if (idMenuPadre > 0) {
                Menu menuAux = menuFacade.find(idMenuPadre);
                menuAux.getAcciones().add(actionItems);
                menuFacade.edit(menuAux);
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar crear la accion");
        }
    }
    
    @Override
    public void remove(Long idAccion) throws Exception {
        try {
            ActionItem actionItems = this.find(idAccion);
            actionItems.setEstado(false);
            this.edit(actionItems);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar la accion");
        }
    }
    
    @Override
    public void edit(Long idAccion, String nombreAccion) throws Exception {
        try {
            ActionItem actionItems = this.find(idAccion);
            actionItems.setNombre(nombreAccion);
            this.edit(actionItems);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar la accionn");
        }
    }
    
}
