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
import org.ccpm.sysse.facade.ActionItemFacadeLocal;
import org.ccpm.sysse.facade.MenuFacadeLocal;
import org.primefaces.model.LazyDataModel;

@Named
@SessionScoped
public class AccionManagedBean extends UtilManagedBean implements Serializable {

    @EJB
    private ActionItemFacadeLocal actionFacade;
    @EJB
    private MenuFacadeLocal menuFacade;
    private String nombre;
    private List<ActionItem> lstActionItems = new ArrayList<>();

    /**
     * Creates a new instance of AccionManagedBean
     */
    public AccionManagedBean() {
    }

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            try {
                this.prepararAcciones(sessionBean.getUsuario());
            } catch (Exception e) {
            }
        }
    }

    public void prepararAcciones(Usuario usuario) throws Exception {
        try {
            List<Grupo> listGrupo = usuario.getGrupos();
            if (!(listGrupo.isEmpty())) {
                for (Grupo grupoAux : listGrupo) {
                    this.lstActionItems.addAll(grupoAux.getAcciones());
                }
            }
            this.lstActionItems.addAll(usuario.getAcciones());
            if (!(lstActionItems.isEmpty())) {
                for (ActionItem accionAux : lstActionItems) {
                    if (accionAux.getNombre().equalsIgnoreCase("nuevaAccion")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarAccion")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarAccion")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleAccion")) {
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

    public String getNombre() {
        return nombre;
    }

    @Override
    public List<ActionItem> getListElements() {
        if (this.getNombreBuscar().equals("")) {
            this.setList(actionFacade.findAll(true));
        } else {
            this.setList(actionFacade.findAll(desacentuar(this.getNombreBuscar())));
        }
        return (List<ActionItem>) this.getList();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<Menu> listMenu = menuFacade.findAll(true);
        if (!(listMenu.isEmpty())) {
            for (Menu menuAux : listMenu) {
                selectItems.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            }
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        this.setNombre(null);
    }

    @Override
    public String crear() {
        try {
            actionFacade.create(this.getNombre());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorAccion");
            this.setMsgSuccessError("La acción " + this.getNombre() + " ha sido generada con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("La acción " + this.getNombre() + " no se pudo generar.");
            this.setResultado("successErrorAccion");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try {
            actionFacade.create(this.getNombre());
            this.setResultado("nuevaAccion");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("La acción " + this.getNombre() + " no se pudo generar.");
            this.setResultado("successErrorAccion");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAccionAux = Long.parseLong(myRequest.getParameter("id"));
        ActionItem actionItemsAux = this.actionFacade.find(idAccionAux);
        this.setId(actionItemsAux.getId());
        this.setNombre(actionItemsAux.getNombre());
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
            Long idAccionAux = Long.parseLong(myRequest.getParameter("id"));
            this.actionFacade.remove(idAccionAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("accionConf");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("La acción " + this.getNombre() + " no se pudo borrar.");
            this.setResultado("successErrorAccion");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAccionAux = Long.parseLong(myRequest.getParameter("id"));
        ActionItem actionItemsAux = this.actionFacade.find(idAccionAux);
        this.setId(actionItemsAux.getId());
        this.setNombre(actionItemsAux.getNombre());
    }

    @Override
    public String guardarEdicion() {
        try {
            this.actionFacade.edit(this.getId(), this.getNombre());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorAccion");
            this.setMsgSuccessError("La acción " + this.getNombre() + " ha sido editada con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("La acción " + this.getNombre() + " no pudo editarse.");
            this.setResultado("successErrorAccion");
        }
        return this.getResultado();
    }   

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
