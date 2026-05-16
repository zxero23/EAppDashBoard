package org.ccpm.sysse.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.facade.OrganismoFacadeLocal;
import org.primefaces.model.LazyDataModel;


@Named
@SessionScoped
public class OrganismoManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private OrganismoFacadeLocal organismoFacade;    
    private Long idOrganismoPadre;
    private Long codOrganigrama;
    private String nombre;
    private boolean implementa;
    private String descripcion;
    private String organismoPadre;
    private List <ActionItem> lstActionItems = new ArrayList <>();
    private List <Organismo> lstOrganismos = new ArrayList <>();
    //para busqueda
    private Long codOrganigramaBsq;
    private Long idBuscar;
    //para organismo implementado
    private Date fechaImple;
    private Long numImple;

    /** Creates a new instance of OrganismoManagedBean */
    public OrganismoManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoOrganismo")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarOrganismo")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarOrganismo")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleOrganismo")) {
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

    public void setIdOrganismoPadre(Long idOrganismoPadre) {
        this.idOrganismoPadre = idOrganismoPadre;
    }

    public void setCodOrganigrama(Long codOrganigrama) {
        this.codOrganigrama = codOrganigrama;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setOrganismoPadre(String organismoPadre) {
        this.organismoPadre = organismoPadre;
    }

    public void setCodOrganigramaBsq(Long codOrganigramaBsq) {
        this.codOrganigramaBsq = codOrganigramaBsq;
    }

    public void setFechaImple(Date fechaImple) {
        this.fechaImple = fechaImple;
    }

    public void setNumImple(Long numImple) {
        this.numImple = numImple;
    }

    public Date getFechaImple() {
        return fechaImple;
    }

    public Long getNumImple() {
        return numImple;
    }

    public Long getCodOrganigramaBsq() {
        return codOrganigramaBsq;
    }

    public String getOrganismoPadre() {
        return organismoPadre;
    }

    public Long getIdOrganismoPadre() {
        return idOrganismoPadre;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public Long getCodOrganigrama() {
        return codOrganigrama;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Organismo> getLstOrganismos() {
        return lstOrganismos;
    }

    public void setLstOrganismos(List<Organismo> lstOrganismos) {
        this.lstOrganismos = lstOrganismos;
    }

    public Long getIdBuscar() {
        return idBuscar;
    }

    public void setIdBuscar(Long idBuscar) {
        this.idBuscar = idBuscar;
    }

    public boolean isImplementa() {
        return implementa;
    }

    public void setImplementa(boolean implementa) {
        this.implementa = implementa;
    }
    
    
    
 
    @Override
    public List<Organismo> getListElements() {
        this.setList(this.organismoFacade.findAll(this.getCodOrganigramaBsq(), desacentuar(this.getNombreBuscar().toUpperCase()),this.getIdBuscar()));
        return (List<Organismo>) this.getList();
    }

    @Override
    public void limpiar() {
        this.setIdOrganismoPadre(null);
        this.setCodOrganigrama(null);
        this.setNombre(null);
        this.setDescripcion(null);
        this.setLstOrganismos(new ArrayList());
    }

    @Override
    public String crear() {
        try{
            this.organismoFacade.create(this.getIdOrganismoPadre(), 
                    this.getCodOrganigrama(), this.getNombre(), 
                    this.getDescripcion());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorOrganismo");
            this.setMsgSuccessError("El organismo ha sido generado con éxito.");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorOrganismo");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.organismoFacade.create(this.getIdOrganismoPadre(), 
                    this.getCodOrganigrama(), this.getNombre(), 
                    this.getDescripcion());
            this.setResultado("nuevoOrganismo");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorOrganismo");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        Organismo organismoAux = this.organismoFacade.find(idObjectAux);
        this.setId(organismoAux.getId());
        this.setCodOrganigrama(organismoAux.getCodOrganigrama());
        this.setNombre(organismoAux.getNombre());
        this.setDescripcion(organismoAux.getDescripcion());
        this.setFechaImple(organismoAux.getFechaInicio());
        if (organismoAux.getOrganismoPadre() != null){
            this.setOrganismoPadre(organismoAux.getOrganismoPadre().getNombre());
        }else{
            this.setOrganismoPadre("Ninguno");
        } 
        this.setImplementa(organismoAux.isImplementa());
        this.setLstOrganismos(organismoAux.getOrganismos());
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
            this.organismoFacade.remove(idObjectAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("organismoConf");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorOrganismo");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        Organismo organismoAux = this.organismoFacade.find(idObjectAux);
        this.setId(organismoAux.getId());
        if (organismoAux.getOrganismoPadre() != null){
          this.setIdOrganismoPadre(organismoAux.getOrganismoPadre().getId());  
        }
        
        this.setCodOrganigrama(organismoAux.getCodOrganigrama());
        this.setNombre(organismoAux.getNombre());
        this.setDescripcion(organismoAux.getDescripcion());
        this.setFechaImple(organismoAux.getFechaInicio());
        this.setNumImple(organismoAux.getNumCorrelativo()); 
        this.setImplementa(organismoAux.isImplementa());
    }

    @Override
    public String guardarEdicion() {
        try {
            this.organismoFacade.edit(this.getId(), this.getNombre(),this.getDescripcion(), this.getFechaImple(),this.getCodOrganigrama(), this.getIdOrganismoPadre(),this.isImplementa());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorOrganismo");
            this.setMsgSuccessError("El organismo ha sido editado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorOrganismo");
        }
        return this.getResultado();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <SelectItem>();
        List <Organismo> lstOrganismo = this.organismoFacade.findAll(true);
        if (!(lstOrganismo.isEmpty())) {
            for (Organismo organismoAux : lstOrganismo) {
                selectItems.add(new SelectItem(organismoAux.getId(), organismoAux.getCodOrganigrama() + " - " + organismoAux.getNombre()));
            }
        }
        return selectItems;
    }
    
    @Override
    public void actualizar(){
        this.setNombreBuscar("");
        this.setCodOrganigramaBsq(null);
        this.setIdBuscar(null);
        super.actualizar();
    }      

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
