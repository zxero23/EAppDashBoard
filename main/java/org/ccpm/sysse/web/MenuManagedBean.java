package org.ccpm.sysse.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.sysse.entity.ActionItem;
import org.ccpm.sysse.entity.Grupo;
import org.ccpm.sysse.entity.Menu;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.facade.MenuFacadeLocal;
import org.primefaces.model.LazyDataModel;


@Named
@SessionScoped
public class MenuManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private MenuFacadeLocal menuFacade;
    private String nombre;
    private Long idMenuPadre;
    private String link;
    private String menuPadre;
    private List <ActionItem> lstActionItems = new ArrayList <ActionItem>();
    
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
            throw new Exception("Error al recuperar las acciones del usuario.");
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

    public void setIdMenuPadre(Long idMenuPadre) {
        this.idMenuPadre = idMenuPadre;
    }

    public Long getIdMenuPadre() {
        return idMenuPadre;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMenuPadre() {
        return menuPadre;
    }

    public void setMenuPadre(String menuPadre) {
        this.menuPadre = menuPadre;
    }
    
    @Override
    public List <Menu> getListElements() {
        if (this.getNombreBuscar().equals("")) {
            this.setList(menuFacade.findAll(true));
        } else {
            this.setList(menuFacade.findAll(desacentuar(this.getNombreBuscar().toUpperCase())));
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
            this.menuFacade.create(this.getIdMenuPadre(), this.getNombre(), this.getLink());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorMenu");
            this.setMsgSuccessError("El menú ha sido generado con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
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
            this.menuFacade.create(this.getIdMenuPadre(), this.getNombre(), this.getLink());
            this.setResultado("nuevoMenu");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
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
        this.setLink(menuAux.getLink());
        if (menuAux.getMenuPadre() != null){
            this.setMenuPadre(menuAux.getMenuPadre().getNombre());
        }else{
            this.setMenuPadre("-");
        }
        
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
            this.setTitle("¡Error!");
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
        this.setLink(menuAux.getLink());
        if(menuAux.getMenuPadre() != null){
            this.setIdMenuPadre(menuAux.getMenuPadre().getId());
        }        
    }

    @Override
    public String guardarEdicion() {
        try {
            this.menuFacade.edit(this.getId(), this.getIdMenuPadre(), this.getNombre(), this.getLink());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorMenu");
            this.setMsgSuccessError("El menú ha sido editado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMenu");
        }
       return this.getResultado();
    }

    @Override
    public List <SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <SelectItem>();
        List <Menu> listMenu = menuFacade.findAll(true);
        if (!(listMenu.isEmpty())) {
            for (Menu menuAux : listMenu) {
                selectItems.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            }
        }
        return selectItems;
    }       

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}