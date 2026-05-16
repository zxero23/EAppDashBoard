/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.dashboard.entity.Menu;
import org.ccpm.dashboard.utilidad.Utilidad;

/**
 *
 * @author Angel
 */
@Stateless
public class MenuFacade extends AbstractFacade<Menu> implements MenuFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MenuFacade() {
        super(Menu.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Menu> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Menu as o WHERE o.estado = :p1");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Menu> findAll(String nombreMenu) {
        return em.createQuery("select object(o) FROM Menu as o WHERE o.estado = true AND o.nombre LIKE '" + nombreMenu + "%' ORDER BY o.nombre ").getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Menu> findAll(List <Long> menuId) {
        String iDes = menuId.toString().substring(1, menuId.toString().length()-1);
        return em.createQuery("select object (m) from Menu as m where m.id in (" + iDes + ")").getResultList();
    }
    
    @Override
    public List <Menu> findAll(Long id) {
        Query jpqlString = em.createQuery("SELECT OBJECT (subm) FROM Menu AS m, IN (m.menus) subm where m.id = :p1 ORDER BY subm.orden");
        jpqlString.setParameter("p1", id);
        return jpqlString.getResultList();
    }  
    
    @Override
    public List<Menu> findAllMenu() {
        Query consulta = em.createQuery("select object(o) from Menu as o WHERE o.estado = :p1 and o.menu is null");
        consulta.setParameter("p1", true);
        return consulta.getResultList();
    }
    
    @Override
    public void create(Long idMenuPadre, String nombre) throws Exception {
        try {
            Menu menuAux = new Menu();
            String link = Utilidad.textoSinBlancos(nombre.toLowerCase());
            menuAux.setNombre(nombre.toUpperCase());
            menuAux.setEstado(true);
            menuAux.setLink(link);
            em.persist(menuAux);
            if (idMenuPadre > 0) {
                Menu menuPadreAux = this.find(idMenuPadre);
                menuPadreAux.getMenus().add(menuAux);
                menuAux.setMenu(menuPadreAux);
                this.edit(menuAux);
                this.edit(menuPadreAux);
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar crear el menu");
        }
    }
    
    @Override
    public void remove(Long idMenu) throws Exception {
        try {
            Menu menuAux = this.find(idMenu);
            menuAux.setEstado(false);
            em.merge(menuAux);
        } catch (Exception e) {
            throw new Exception("Error al intetar borrar el menu");
        }
    }
    
    @Override
    public void edit(Long id, Long idSeleccion, String nombre) throws Exception {
        try {
            String link = Utilidad.textoSinBlancos(nombre.toLowerCase());
            Menu menuPadreSeleccion = this.find(idSeleccion);
            Menu menuAux = this.find(id);
            Menu menuPadreActual = menuAux.getMenu();
            menuAux.setNombre(nombre.toUpperCase());
            menuAux.setEstado(true);
            menuAux.setLink(link);
            if (menuPadreActual != null) {
                if (!(menuPadreActual.equals(menuPadreSeleccion))) {
                    if (menuPadreSeleccion != null) {
                        menuPadreActual.getMenus().remove(menuAux);
                        this.edit(menuPadreActual);
                        menuPadreSeleccion.getMenus().add(menuAux);
                        this.edit(menuPadreSeleccion);
                        menuAux.setMenu(menuPadreSeleccion);
                    } else {
                        menuPadreActual.getMenus().remove(menuAux);
                        this.edit(menuPadreActual);
                        menuAux.setMenu(menuPadreSeleccion);
                    }
                }
            } else {
                if (menuPadreSeleccion != null) {
                    menuPadreSeleccion.getMenus().add(menuAux);
                    this.edit(menuPadreSeleccion);
                    menuAux.setMenu(menuPadreSeleccion);
                }
            }
            this.edit(menuAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el menu");
        }
    }    
    
}
