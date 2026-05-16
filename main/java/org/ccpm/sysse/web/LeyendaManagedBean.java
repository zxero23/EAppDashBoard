package org.ccpm.sysse.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.facade.LeyendaFacadeLocal;
import org.primefaces.model.LazyDataModel;

@Named
@SessionScoped
public class LeyendaManagedBean extends UtilManagedBean implements Serializable {

    @EJB
    private LeyendaFacadeLocal leyendaFacade;
    private Long anio;
    private String leyenda;
    private List<ActionItem> lstActionItems = new ArrayList<ActionItem>();
    private List<Leyenda> leyendas;
    //para busqueda
    private Long anioBsq;
    private String leyendaBsq;

    public LeyendaManagedBean() {
    }

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            try {
                this.prepararAcciones(sessionBean.getUsuario());
                this.setLeyendas(this.leyendaFacade.findAll(true));
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevaLeyenda")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarLeyenda")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarLeyenda")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleLeyenda")) {
                        this.setDetalle(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario.");
        }
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public Long getAnio() {
        return anio;
    }

    public void setAnio(Long anio) {
        this.anio = anio;
    }

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public Long getAnioBsq() {
        return anioBsq;
    }

    public void setAnioBsq(Long anioBsq) {
        this.anioBsq = anioBsq;
    }

    public String getLeyendaBsq() {
        return leyendaBsq;
    }

    public void setLeyendaBsq(String leyendaBsq) {
        this.leyendaBsq = leyendaBsq;
    }

    public List<Leyenda> getLeyendas() {
        return leyendas;
    }

    public void setLeyendas(List<Leyenda> leyendas) {
        this.leyendas = leyendas;
    }

    
    @Override
    public List<Leyenda> getListElements() {
        this.setList(this.leyendaFacade.findAll(this.getAnioBsq(), desacentuar(this.getLeyendaBsq().toUpperCase())));
        return (List<Leyenda>) this.getList();
    }

    @Override
    public void aplicarFiltro() {
        this.setLeyendas(this.leyendaFacade.findAll(this.getAnioBsq(), desacentuar(this.getLeyendaBsq().toUpperCase())));
    }

    @Override
    public void limpiar() {
        this.setAnio(null);
        this.setLeyenda(null);
        this.setAnioBsq(null);
        this.setLeyendaBsq(null);
        this.setLeyendas(this.leyendaFacade.findAll(true));
    }

    public void actualizar() {
        this.limpiar();        
    }

    @Override
    public String crear() {
        try {
            this.leyendaFacade.create(this.getAnio(),
                    this.getLeyenda());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorLeyenda");
            this.setMsgSuccessError("El leyenda ha sido generada con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorLeyenda");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try {
            this.leyendaFacade.create(this.getAnio(),
                    this.getLeyenda());
            this.setResultado("nuevaLeyenda");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorLeyenda");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Leyenda leyendaAux = this.leyendaFacade.find(idAux);
        this.setAnio(leyendaAux.getAnio());
        this.setLeyenda(leyendaAux.getNombre());
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            this.leyendaFacade.remove(idAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("leyendaConf");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorLeyenda");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Leyenda leyendaAux = this.leyendaFacade.find(idAux);
        this.setAnio(leyendaAux.getAnio());
        this.setLeyenda(leyendaAux.getNombre());
    }

    @Override
    public String guardarEdicion() {
        try {
            this.leyendaFacade.edit(this.getId(), this.getAnio(), this.getLeyenda());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorLeyenda");
            this.setMsgSuccessError("El leyenda ha sido editada con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorLeyenda");
        }
        return this.getResultado();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<Leyenda> lstLeyenda = this.leyendaFacade.findAll(true);
        if (!(lstLeyenda.isEmpty())) {
            for (Leyenda leyendaAux : lstLeyenda) {
                selectItems.add(new SelectItem(leyendaAux.getAnio(), leyendaAux.getNombre()));
            }
        }
        return selectItems;
    }                

    public void verificarAnios(FacesContext context, UIComponent validate, Object value) {
        Long anioNuevo = (Long) value;

        List<Leyenda> lstLeyenda = this.leyendaFacade.findAll(true);
        if (!(lstLeyenda.isEmpty())) {
            for (Leyenda leyendaAux : lstLeyenda) {
                if (anioNuevo.equals(leyendaAux.getAnio())) {
                    FacesMessage msg = new FacesMessage(anioNuevo+" tiene asignada una leyenda en el sistema.");
                    context.addMessage(validate.getClientId(context), msg);
                    throw new ValidatorException(msg);
                }
            }            
        }
    } 

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
