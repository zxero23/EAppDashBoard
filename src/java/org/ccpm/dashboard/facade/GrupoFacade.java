/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Menu;

/**
 *
 * @author Alvarenga Angel
 */
@Stateless
public class GrupoFacade extends AbstractFacade<Grupo> implements GrupoFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;
    @EJB
    private MenuFacadeLocal menuFacade;
    @EJB
    private ActionItemFacadeLocal actionFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public GrupoFacade() {
        super(Grupo.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Grupo> findGrupos(List <Long> gruposId) {
        String iDes = gruposId.toString().substring(1, gruposId.toString().length()-1);
        System.out.println("ideeessss "+gruposId.get(0).toString());
        return em.createQuery("select object (m) from Grupo as m where m.id IN (" + iDes +  ")").getResultList();
    }
    
    @Override
    public List <Menu> findAllMenu(Long idGrupo) {
        Query jpqlString = em.createQuery("SELECT OBJECT (m) FROM Grupo AS g, IN (g.menus) m where g.id = :p1 ORDER BY m.orden");
        jpqlString.setParameter("p1", idGrupo);
        return jpqlString.getResultList();
    }   
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Grupo> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Grupo as o WHERE o.estado = :p1");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Grupo> findAll(String nombreGrupo) {
        return em.createQuery("select object(o) FROM Grupo as o WHERE o.estado = true AND o.nombre LIKE '" + nombreGrupo + "%' ORDER BY o.nombre ").getResultList();
    }    
  
    @Override
    public void remove(Long idGrupo) throws Exception {
        try {
            Grupo grupo = this.find(idGrupo);
            grupo.setEstado(false);
            this.edit(grupo);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar el grupo");
        }
    }
        
    @Override
    public void remove(Long idGrupo, List <Long> idAcciones) throws Exception {
        try {
            Grupo grupo = this.find(idGrupo);
             if(!(idAcciones.isEmpty())){
                List <ActionItem> listAction = actionFacade.findAll(idAcciones);
                grupo.setAcciones(null);
                this.edit(grupo);
                grupo.setAcciones(listAction);
                this.edit(grupo);
             }else{
                grupo.setAcciones(null);
                this.edit(grupo);
             }
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar acciones del grupo");
        }
    }

    @Override
    public void create(String nombreGrupo, String descripcionGrupo) throws Exception {
        try {
            Grupo grupo = new Grupo();
            grupo.setNombre(nombreGrupo.toUpperCase());
            grupo.setDescripcion(descripcionGrupo);
            grupo.setEstado(true);
            this.create(grupo);
        } catch (Exception ex) {
            throw new Exception("Error al intentar crear el grupo");
        }
    }

    @Override
    public void edit(Long idGrupo, String nombreGrupo, String descripcionGrupo) throws Exception {
        try {
            Grupo grupo = this.find(idGrupo);
            grupo.setNombre(nombreGrupo.toUpperCase());
            grupo.setDescripcion(descripcionGrupo);
            grupo.setEstado(true);
            this.edit(grupo);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el grupo");
        }
    }
        
    @Override
    public void edit(Long idGrupo, List <Long> idMenu) throws Exception {        
        try {
            Grupo grupo = this.find(idGrupo);
            if(!(idMenu.isEmpty())){
                List <Menu> listaMenu = menuFacade.findAll(idMenu);
                grupo.setAcciones(null);
                grupo.setMenus(null);
                grupo.setMenus(listaMenu);                
                List <ActionItem> listAction = new ArrayList <> ();
                for (Menu menuAux : listaMenu) {
                   listAction.addAll(actionFacade.findAll(menuAux.getId()));
                }
                grupo.setAcciones(listAction);   
            }else{
                grupo.setAcciones(null);
                grupo.setMenus(null);
            }
            this.edit(grupo);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el grupo");
        }
    }
}
