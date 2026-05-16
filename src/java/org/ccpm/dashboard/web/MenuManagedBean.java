/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Menu;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.facade.MenuFacadeLocal;

/**
 *
 * @author Angel
 */
@ManagedBean
@RequestScoped
public class MenuManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private MenuFacadeLocal menuFacade;
    private String nombre;
    private String idMenuPadre;
    private List <ActionItem> lstActionItems = new ArrayList <>();
    
    /** Creates a new instance of MenuManagedBean */
    public MenuManagedBean() {
    }

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            try {
                this.prepararAcciones(sessionBean.getUsuario());
            } catch (Exception e) { }
        }
    }
    
    public void prepararAcciones(Usuario usuario) throws Exception {
        try {
            List <Grupo> listGrupo = usuario.getGrupos();
            if (!(listGrupo.isEmpty())) {
                for (Grupo grupoAux : listGrupo) {
                    this.lstActionItems.addAll(grupoAux.getAcciones());
                }
            }
            this.lstActionItems.addAll(usuario.getAcciones());
            if (!(lstActionItems.isEmpty())) {
                for (ActionItem accionAux : lstActionItems) {
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoMenu")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarMenu")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarMenu")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleMenu")) {
                        this.setDetalle(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public void setIdMenuPadre(String idMenuPadre) {
        this.idMenuPadre = idMenuPadre;
    }

    public String getIdMenuPadre() {
        return idMenuPadre;
    }

    public String getNombre() {
        return nombre;
    }
    
    @Override
    public List <Menu> getListElements() {
        if (this.getNombreBuscar().equals("")) {
            this.setList(menuFacade.findAll(true));
        } else {
            this.setList(menuFacade.findAll(this.getNombreBuscar().toUpperCase()));
        }
        return (List <Menu>) this.getList();
    }

    @Override
    public void limpiar() {
        this.setNombre(null);
    }

    @Override
    public String crear() {
        try {
            menuFacade.create(Long.parseLong(this.getIdMenuPadre()), this.getNombre());
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorMenu");
            this.setMsgSuccessError("El Menu ha sido generado con éxito");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMenu");
            this.limpiar();
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try {
            menuFacade.create(Long.parseLong(this.getIdMenuPadre()), this.getNombre());
            this.setResultado("nuevoMenu");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMenu");
            this.limpiar();
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idMenuAux = Long.parseLong(myRequest.getParameter("id"));
        Menu menuAux = this.menuFacade.find(idMenuAux);
        this.setId(menuAux.getId());
        this.setNombre(menuAux.getNombre());
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idMenuAux = Long.parseLong(myRequest.getParameter("id"));
            this.menuFacade.remove(idMenuAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("menuConf");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMenu");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idMenuAux = Long.parseLong(myRequest.getParameter("id"));
        Menu menuAux = this.menuFacade.find(idMenuAux);
        this.setId(menuAux.getId());
        this.setNombre(menuAux.getNombre());
        if(menuAux.getMenu() != null){
            this.setIdMenuPadre(String.valueOf(menuAux.getMenu().getId()));
        }
    }

    @Override
    public String guardarEdicion() {
        try {
            this.menuFacade.edit(this.getId(), Long.parseLong(this.getIdMenuPadre()), this.getNombre());
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorMenu");
            this.setMsgSuccessError("El menu ha sido editado con éxito");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMenu");
        }
       return this.getResultado();
    }

    @Override
    public List <SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Menu> listMenu = menuFacade.findAll(true);
        if (!(listMenu.isEmpty())) {
            for (Menu menuAux : listMenu) {
                selectItems.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            }
        }
        return selectItems;
    }
    
}
