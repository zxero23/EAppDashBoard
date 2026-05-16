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
import org.ccpm.sysse.entity.TipoInstrumentoLegal;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.facade.TipoInstrumentoLegalFacadeLocal;
import org.primefaces.model.LazyDataModel;

@Named
@SessionScoped
public class TipoInstrumentoLegalManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private TipoInstrumentoLegalFacadeLocal tipoInstrumentoLegalFacade;
    private String nombre;   
    private String descripcion;
    private List <ActionItem> lstActionItems = new ArrayList <ActionItem>();

    /** Creates a new instance of TipoInstrumentoLegalManagedBean */
    public TipoInstrumentoLegalManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoTipoInstLegal")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarTipoInstLegal")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarTipoInstLegal")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleTipoInstLegal")) {
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
    public List<TipoInstrumentoLegal> getListElements() {
        if (this.getNombreBuscar().equals("")) {
            this.setList(this.tipoInstrumentoLegalFacade.findAll(true));
        } else {
            this.setList(this.tipoInstrumentoLegalFacade.findAll(desacentuar(this.getNombreBuscar().toUpperCase())));
        }
        return (List<TipoInstrumentoLegal>) this.getList();
    }

    @Override
    public void limpiar() {
        this.setNombre(null);
        this.setDescripcion(null);
    }

    @Override
    public String crear() {
        try{
            this.tipoInstrumentoLegalFacade.create(this.getNombre(), this.getDescripcion());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorTipoInstLegal");
            this.setMsgSuccessError("El tipo de instrumento legal ha sido generado con éxito.");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoInstLegal");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.tipoInstrumentoLegalFacade.create(this.getNombre(), this.getDescripcion());
            this.setResultado("nuevoTipoInstLegal");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoInstLegal");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        TipoInstrumentoLegal objectAux = this.tipoInstrumentoLegalFacade.find(idObjectAux);
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
            this.tipoInstrumentoLegalFacade.remove(idObjectAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("tipoInstLegalConf");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoInstLegal");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        TipoInstrumentoLegal objectAux = this.tipoInstrumentoLegalFacade.find(idObjectAux);
        this.setId(objectAux.getId());
        this.setNombre(objectAux.getNombre());
        this.setDescripcion(objectAux.getDescripcion());
    }

    @Override
    public String guardarEdicion() {
        try {
            this.tipoInstrumentoLegalFacade.edit(this.getId(), this.getNombre(), this.getDescripcion());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorTipoInstLegal");
            this.setMsgSuccessError("El tipo de instrumento legal ha sido editado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoInstLegal");
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

