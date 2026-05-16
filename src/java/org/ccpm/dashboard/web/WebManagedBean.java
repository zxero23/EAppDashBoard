/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Menu;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.facade.UsuarioFacadeLocal;

/**
 *
 * @author Alvarenga Angel
 */
@Named("webManagedBean")
@SessionScoped
public class WebManagedBean implements Serializable {
    @EJB
    private UsuarioFacadeLocal usuarioFacade;    
    private String nombreUsuario;
    private String claveAcceso;  
    private String resultado;
    private String msgSuccessError;   
    private String title;
    private String images;
    private Usuario usuario; 
    private String nombreGrupo;
        
    /** Creates a new instance of WebManagedBean */
    public WebManagedBean() {
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setMsgSuccessError(String msgSuccessError) {
        this.msgSuccessError = msgSuccessError;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public String getImages() {
        return images;
    }

    public String getMsgSuccessError() {
        return msgSuccessError;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getResultado() {
        return resultado;
    }

    public String getTitle() {
        return title;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String login() {       
        try {
            Usuario usuarioAux = usuarioFacade.find(this.getNombreUsuario().toLowerCase(), this.getClaveAcceso().toLowerCase());
            this.setUsuario(usuarioAux);
            this.setNombreGrupo(usuarioAux.getGrupos().get(0).getNombre());
            this.setMsgSuccessError("");
            // Force full navigation to avoid partial-view update issues on ICEfaces.
            this.setResultado("administracion?faces-redirect=true");
        } catch (Exception ex) {
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorLogin?faces-redirect=true");
        }
        return resultado;
    }
    
    public void logout() {
        this.setNombreUsuario(null);
        this.setClaveAcceso(null);
        this.setUsuario(null);
        this.limpiar();
        this.clearIndicadorBean("indicadorManagedBean");
        
    }
       
    public void limpiar() {
        this.setNombreUsuario(null);
        this.setClaveAcceso(null); 
        
   }
    
     public void clearIndicadorBean (String beanName) {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(beanName);
    }
    
   public List<Menu> getMenu() {        
        List<Menu> listRetorno = new ArrayList<>();        
        List<Grupo> listGrupo = this.getUsuario().getGrupos();
        if (!(listGrupo.isEmpty())) {
            for (Grupo grupoAux : listGrupo) {
                List<Menu> listMenu = grupoAux.getMenus();
                listRetorno.addAll(listMenu);
            }
        }
        listRetorno.addAll(this.getUsuario().getMenus());
        return listRetorno;
    }
   
 

    
       
}

