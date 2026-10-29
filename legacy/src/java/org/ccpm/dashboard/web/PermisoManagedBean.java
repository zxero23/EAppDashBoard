/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Menu;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.facade.ActionItemFacadeLocal;
import org.ccpm.dashboard.facade.GrupoFacadeLocal;
import org.ccpm.dashboard.facade.MenuFacadeLocal;
import org.ccpm.dashboard.facade.UsuarioFacadeLocal;

/**
 *
 * @author Alvarenga Angel
 */
@ManagedBean
@SessionScoped
public class PermisoManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private ActionItemFacadeLocal actionFacade;
    @EJB
    private MenuFacadeLocal menuFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    private Long idGrupo;
    private Long idUsuario;
    private List <SelectItem> itemsIzquierda;
    private List <SelectItem> itemsDerecha;

    /** Creates a new instance of PermisoManagedBean */
    public PermisoManagedBean() {
        this.idGrupo = 0L;
        this.idUsuario = 0L;
        this.itemsIzquierda = null;
        this.itemsDerecha = null;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public List<SelectItem> getItemsIzquierda() {
        return itemsIzquierda;
    }

    public void setItemsIzquierda(List<SelectItem> itemsIzquierda) {
        this.itemsIzquierda = itemsIzquierda;
    }

    public List<SelectItem> getItemsDerecha() {
        return itemsDerecha;
    }

    public void setItemsDerecha(List<SelectItem> itemsDerecha) {
        this.itemsDerecha = itemsDerecha;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public List <SelectItem> getSelectItemsGrupo() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Grupo> lstGrupo = grupoFacade.findAll(true);
        if (!(lstGrupo.isEmpty())) {
            for (Grupo grupoAux : lstGrupo) {
                selectItems.add(new SelectItem(grupoAux.getId(), grupoAux.getNombre()));
            }
        }
        return selectItems;
    }
    
    public List <SelectItem> getSelectItemsUsuario() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Usuario> lstUsuario = usuarioFacade.findAll(true);
        if (!(lstUsuario.isEmpty())) {
            for (Usuario usuarioAux : lstUsuario) {
                selectItems.add(new SelectItem(usuarioAux.getId(), usuarioAux.getNombre()));
            }
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        this.setIdGrupo(null);
        this.setIdUsuario(null);
        this.setItemsDerecha(null);
        this.setItemsIzquierda(null);
    }
    
    public void updateListAcctions(ValueChangeEvent event) {
        Grupo grupo = this.grupoFacade.find(Long.parseLong(event.getNewValue().toString()));
        if(grupo!=null){
            this.accionesSeleccionadas(grupo);
            this.accioneDisponibles(grupo);
        }else{
            this.setItemsDerecha(null);
            this.setItemsIzquierda(null);
        }
    }
    
    public void accionesSeleccionadas(Grupo grupo){
        List <ActionItem> listActionItems = grupo.getAcciones(); 
        this.itemsIzquierda = new ArrayList<>();
        if(!(listActionItems.isEmpty())){
            for(ActionItem actionAux : listActionItems){
                this.itemsIzquierda.add(new SelectItem(actionAux.getId(), actionAux.getNombre()));
            }
        }
    }

    public void accioneDisponibles(Grupo grupo){
        List <ActionItem> listActionItemsAll = actionFacade.findAll(true);
        this.itemsDerecha= new ArrayList<>();
        listActionItemsAll.removeAll(grupo.getAcciones());
        if (!(listActionItemsAll.isEmpty())) {
            for (ActionItem actionAux : listActionItemsAll) {
                this.itemsDerecha.add(new SelectItem(actionAux.getId(), actionAux.getNombre()));
            }
        }
    }
    
    public void updateListMenu(ValueChangeEvent event) {
        Grupo grupo = this.grupoFacade.find(Long.parseLong(event.getNewValue().toString()));
        if(grupo!=null){
            this.menuSeleccionados(grupo);
            this.menuDisponibles(grupo);
        }else{
            this.setItemsDerecha(null);
            this.setItemsIzquierda(null);
        }
    }
    
    public void menuSeleccionados(Grupo grupo){
        List <Menu> lstMenu = grupo.getMenus();
        this.itemsIzquierda = new ArrayList<>();
        if(!(lstMenu.isEmpty())){
            for(Menu menuAux : lstMenu){
                this.itemsIzquierda.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            }
        }
    }

    public void menuDisponibles(Grupo grupo){
        List <Menu> lstMenu = menuFacade.findAll(true);
        this.itemsDerecha= new ArrayList<>();
        lstMenu.removeAll(grupo.getMenus());
        if (!(lstMenu.isEmpty())) {
            for (Menu menuAux : lstMenu) {
                this.itemsDerecha.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            }
        }
    }
    
    public String editarGrupoAccion() {
        try {
            List <Long> lstAux = new ArrayList <>();
            for (SelectItem object : this.getItemsIzquierda()) {
                lstAux.add((Long) object.getValue());
            }
            this.grupoFacade.remove(this.getIdGrupo(), lstAux);
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorPermiso");
            this.setMsgSuccessError("La configuración han sido generada con éxito");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorPermiso");
        }
        return this.getResultado();
    }
    
    public String editarGrupoMenu() {        
        try {            
            List <Long> lstAux = new ArrayList <>();
            for (SelectItem object : this.getItemsIzquierda()) {
                lstAux.add((Long) object.getValue());
            }          
            this.grupoFacade.edit(this.getIdGrupo(), lstAux);
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorPermiso");
            this.setMsgSuccessError("La configuración han sido generada con éxito");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorPermiso");
        }
        return this.getResultado();
    }
    
    
    public void updateListMenuUsuario(ValueChangeEvent event) {
        Usuario usuarioAux = this.usuarioFacade.find(Long.parseLong(event.getNewValue().toString()));
        if(usuarioAux!=null){
            this.menuSeleccionados(usuarioAux);
            this.menuDisponibles(usuarioAux);
        }else{
            this.setItemsDerecha(null);
            this.setItemsIzquierda(null);
        }
    }
    
    public void menuDisponibles(Usuario usuario) { 
        List <Menu> listMenuAll = menuFacade.findAll(true);
        this.itemsDerecha = new ArrayList<>();
        listMenuAll.removeAll(usuario.getMenus());
        if (!(listMenuAll.isEmpty())) {
            for (Menu menuAux : listMenuAll) {
                this.itemsDerecha.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            }
        }    
    }
    
    public void menuSeleccionados(Usuario usuario) {
        List <Menu> listMenu = usuario.getMenus();
        this.itemsIzquierda = new ArrayList<>();
        if (!(listMenu.isEmpty())) {
            for (Menu menuAux : listMenu) {
                this.itemsIzquierda.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            }
        }
    }
    
    public String editarUsuarioMenu() {        
        try {            
            List <Long> lstAux = new ArrayList <>();
            for (SelectItem object : this.itemsIzquierda) {
                lstAux.add((Long)object.getValue());
            }            
            this.usuarioFacade.edit(this.getIdUsuario(), lstAux);
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorPermiso");
            this.setMsgSuccessError("La configuración han sido generada con éxito");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorPermiso");
            this.limpiar();
        }
        return this.getResultado();
    }
    
    public void updateListAcctionsUsuario(ValueChangeEvent event) {
        Usuario usuarioAux = this.usuarioFacade.find(Long.parseLong(event.getNewValue().toString()));
        if(usuarioAux!=null){
            this.accionesSeleccionadas(usuarioAux);
            this.accioneDisponibles(usuarioAux);
        }else{
            this.setItemsDerecha(null);
            this.setItemsIzquierda(null);
        }
    }
    
     public void accionesSeleccionadas(Usuario usuario){
        List <ActionItem> listActionItems = usuario.getAcciones(); 
        this.itemsIzquierda = new ArrayList<>();
        if(!(listActionItems.isEmpty())){
            for(ActionItem actionAux : listActionItems){
                this.itemsIzquierda.add(new SelectItem(actionAux.getId(), actionAux.getNombre()));
            }
        }
    }

    public void accioneDisponibles(Usuario usuario){
        List <ActionItem> listActionItemsAll = actionFacade.findAll(true);
        this.itemsDerecha= new ArrayList<>();
        listActionItemsAll.removeAll(usuario.getAcciones());
        if (!(listActionItemsAll.isEmpty())) {
            for (ActionItem actionAux : listActionItemsAll) {
                this.itemsDerecha.add(new SelectItem(actionAux.getId(), actionAux.getNombre()));
            }
        }
    }
    
    public String editarUsuarioAccion() {
        try {
            List <Long> lstAux = new ArrayList <>();
            for (SelectItem object : this.getItemsIzquierda()) {
                lstAux.add((Long) object.getValue());
            }
            this.usuarioFacade.remove(this.getIdUsuario(), lstAux);
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorPermiso");
            this.setMsgSuccessError("La configuración han sido generada con éxito");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorPermiso");
        }
        return this.getResultado();
    }
    
    @Override
    public List<?> getListElements() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SelectItem> getSelectItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String crear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String crearOtro() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void verDetalle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void guardarBorrado() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void prepararParaEditar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String guardarEdicion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    


}
