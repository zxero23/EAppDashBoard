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
import org.ccpm.sysse.entity.TipoAsunto;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.facade.TipoAsuntoFacadeLocal;
import org.primefaces.model.LazyDataModel;

@Named
@SessionScoped
public class TipoAsuntoManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private TipoAsuntoFacadeLocal tipoAsuntoFacade;
    private String nombre;   
    private String descripcion;
    private List <ActionItem> lstActionItems = new ArrayList <ActionItem>();

    /** Creates a new instance of TipoAsuntoManagedBean */
    public TipoAsuntoManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoTipoAsunto")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarTipoAsunto")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarTipoAsunto")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleTipoAsunto")) {
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

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
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
    public List<TipoAsunto> getListElements() {
        if (this.getNombreBuscar().equals("")) {
            this.setList(this.tipoAsuntoFacade.findAll(true));
        } else {
            this.setList(this.tipoAsuntoFacade.findAll(desacentuar(this.getNombreBuscar().toUpperCase())));
        }
        return (List<TipoAsunto>) this.getList();
    }

    @Override
    public void limpiar() {
        this.setNombre(null);
        this.setDescripcion(null);
    }

    @Override
    public String crear() {
        try{
            this.tipoAsuntoFacade.create(this.getNombre(), this.getDescripcion());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorTipoAsunto");
            this.setMsgSuccessError("El tipo de asunto ha sido generado con éxito.");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoAsunto");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.tipoAsuntoFacade.create(this.getNombre(), this.getDescripcion());
            this.setResultado("nuevoTipoAsunto");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoAsunto");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        TipoAsunto objectAux = this.tipoAsuntoFacade.find(idObjectAux);
        this.setId(objectAux.getId());
        this.setNombre(objectAux.getNombre());
        this.setDescripcion(objectAux.getDescripcion());
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
            this.tipoAsuntoFacade.remove(idObjectAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("tipoAsuntoConf");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoAsunto");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        TipoAsunto objectAux = this.tipoAsuntoFacade.find(idObjectAux);
        this.setId(objectAux.getId());
        this.setNombre(objectAux.getNombre());
        this.setDescripcion(objectAux.getDescripcion());
    }

    @Override
    public String guardarEdicion() {
        try {
            this.tipoAsuntoFacade.edit(this.getId(), this.getNombre(), this.getDescripcion());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorTipoAsunto");
            this.setMsgSuccessError("El tipo de asunto ha sido editado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoAsunto");
        }
        return this.getResultado();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}


