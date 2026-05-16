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
import org.ccpm.sysse.entity.TipoIniciador;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.facade.TipoIniciadorFacadeLocal;
import org.primefaces.model.LazyDataModel;

@Named
@SessionScoped
public class TipoIniciadorManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private TipoIniciadorFacadeLocal tipoIniciadorFacade;
    private String nombre;
    private Boolean externo;
    private List <ActionItem> lstActionItems = new ArrayList <ActionItem>();

    /** Creates a new instance of TipoIniciadorManagedBean */
    public TipoIniciadorManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoTipoIniciador")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarTipoIniciador")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarTipoIniciador")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleTipoIniciador")) {
                        this.setDetalle(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    } 

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getNombre() {
        return nombre;
    }

    public Boolean getExterno() {
        return externo;
    }

    public void setExterno(Boolean externo) {
        this.externo = externo;
    }
    
    @Override
    public List<TipoIniciador> getListElements() {
        if (this.getNombreBuscar().equals("")) {
            this.setList(this.tipoIniciadorFacade.findAll(true));
        } else {
            this.setList(this.tipoIniciadorFacade.findAll(desacentuar(this.getNombreBuscar().toUpperCase())));
        }
        return (List<TipoIniciador>) this.getList();
    }

    @Override
    public void limpiar() {
        this.setNombre(null);
    }

    @Override
    public String crear() {
        try{
            this.tipoIniciadorFacade.create(this.getNombre(),this.getExterno());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorTipoIniciador");
            this.setMsgSuccessError("El tipo de asunto ha sido generado con éxito.");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoIniciador");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.tipoIniciadorFacade.create(this.getNombre(),this.getExterno());
            this.setResultado("nuevoTipoIniciador");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoIniciador");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        TipoIniciador objectAux = this.tipoIniciadorFacade.find(idObjectAux);
        this.setId(objectAux.getId());
        this.setNombre(objectAux.getNombre());
        this.setExterno(objectAux.isExterno());
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
            this.tipoIniciadorFacade.remove(idObjectAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("tipoIniciadorConf");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoIniciador");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        TipoIniciador objectAux = this.tipoIniciadorFacade.find(idObjectAux);
        this.setId(objectAux.getId());
        this.setNombre(objectAux.getNombre());
        this.setExterno(objectAux.isExterno());
    }

    @Override
    public String guardarEdicion() {
        try {
            this.tipoIniciadorFacade.edit(this.getId(), this.getNombre(),this.getExterno());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorTipoIniciador");
            this.setMsgSuccessError("El tipo de asunto ha sido editado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorTipoIniciador");
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


