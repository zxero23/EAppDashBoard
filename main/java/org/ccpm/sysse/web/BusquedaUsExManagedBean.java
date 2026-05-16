package org.ccpm.sysse.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.sysse.entity.Expediente;
import org.ccpm.sysse.entity.UsuarioExterno;
import org.ccpm.sysse.facade.ExpedienteFacadeLocal;
import org.ccpm.sysse.facade.UsuarioExternoFacadeLocal;
import org.primefaces.model.LazyDataModel;

@Named
@SessionScoped
public class BusquedaUsExManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private ExpedienteFacadeLocal expedienteFacade;
    @EJB
    private UsuarioExternoFacadeLocal usuarioExFacade;
    private Long codOrganismo;
    private Long nroCorrel;
    private Integer anio;
    
    private Date fechaDesde;
    private Date fechaHasta;
    private List <Expediente> expedientes;
    private Expediente expediente;

    /** Creates a new instance of BusquedaUsExManagedBean */
    public BusquedaUsExManagedBean() {        
        expedientes = new ArrayList <Expediente>();
    }
   
    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public void setCodOrganismo(Long codOrganismo) {
        this.codOrganismo = codOrganismo;
    }

    public void setNroCorrel(Long nroCorrel) {
        this.nroCorrel = nroCorrel;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public void setExpedientes(List<Expediente> expedientes) {
        this.expedientes = expedientes;
    }

    public void setExpediente(Expediente expediente) {
        this.expediente = expediente;
    }

    public Expediente getExpediente() {
        return expediente;
    }

    public List<Expediente> getExpedientes() {
        return expedientes;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public Integer getAnio() {
        return anio;
    }

    public Long getCodOrganismo() {
        return codOrganismo;
    }

    public Long getNroCorrel() {
        return nroCorrel;
    }

    @Override
    public List<Expediente> getListElements() {
        WebManagedBean sessionBean = this.getSessionBean();
        UsuarioExterno usuarioExAux = this.usuarioExFacade.findByUsuario(sessionBean.getUsuario().getId());
        if (usuarioExAux == null) {
            this.setList(new ArrayList<Expediente>());
            return (List<Expediente>) this.getList();
        }
        this.setList(this.expedienteFacade.findAll(this.getCodOrganismo(),
                this.getNroCorrel(), this.getAnio(), this.getFechaDesde(),
                this.getFechaHasta(), usuarioExAux.getId()));
        return (List<Expediente>) this.getList();
    }
    
    public void actualizar() {
        this.setCodOrganismo(null);
        this.setNroCorrel(null);
        this.setAnio(null);
        this.setFechaDesde(null);
        this.setFechaHasta(null);        
    }    
    
    @Override
    public void limpiar() {
        this.setCodOrganismo(null);
        this.setNroCorrel(null);
        this.setAnio(null);
        this.setFechaDesde(null);
        this.setFechaHasta(null);
        this.setExpediente(null);
    }

    @Override
    public String crear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String crearOtro() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long codAux = Long.parseLong(myRequest.getParameter("cod"));
        Long nroCorrelAux = Long.parseLong(myRequest.getParameter("nro"));
        int annioAux = Integer.parseInt(myRequest.getParameter("anio"));
        this.setExpediente(this.expedienteFacade.find(codAux, nroCorrelAux, annioAux));
    }

    @Override
    public void guardarBorrado() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void prepararParaEditar() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String guardarEdicion() {
        throw new UnsupportedOperationException("Not supported yet.");
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

