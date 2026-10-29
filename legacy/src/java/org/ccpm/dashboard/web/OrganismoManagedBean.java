/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Objetivos;
import org.ccpm.dashboard.entity.Organismo;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.facade.ObjetivosFacadeLocal;
import org.ccpm.dashboard.facade.OrganismoFacadeLocal;
import org.ccpm.dashboard.facade.SituacionFacadeLocal;


/**
 *
 * @author Alvarenga Angel
 */
@ManagedBean
@RequestScoped
public class OrganismoManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private OrganismoFacadeLocal organismoFacade; 
    @EJB
    private ObjetivosFacadeLocal objetivosFacade;
    @EJB
    private SituacionFacadeLocal situacionFacade;
    private Long codOrganigrama;
    private String leyenda;
    private String descripcion;
    private Long idOrganismo;
    private List <ActionItem> lstActionItems = new ArrayList <>();
    private String resultadoOrganismo;
    private Organismo organismo;
    private Objetivos objetivo;
    private boolean descentralizado;
    private boolean mostrarPanelMeta=true; 
    private boolean mostrarPanelObra=true; //panel con obras por estado y organismo.
    private boolean mostrarIndicador=false;

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
            this.setIdOrganismo(usuario.getOrganismo().getId());
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public boolean isMostrarIndicador() {
        return mostrarIndicador;
    }

    public void setMostrarIndicador(boolean mostrarIndicador) {
        this.mostrarIndicador = mostrarIndicador;
    }
    
    

    public boolean isMostrarPanelMeta() {
        return mostrarPanelMeta;
    }

    public void setMostrarPanelMeta(boolean mostrarPanelMeta) {
        this.mostrarPanelMeta = mostrarPanelMeta;
    }

    
    public boolean isMostrarPanelObra() {
        return mostrarPanelObra;
    }

    public void setMostrarPanelObra(boolean mostrarPanelObra) {
        this.mostrarPanelObra = mostrarPanelObra;
    }
    
    public boolean isDescentralizado() {
        return descentralizado;
    }

    public void setDescentralizado(boolean descentralizado) {
        this.descentralizado = descentralizado;
    }
 
    
    
    public String getResultadoOrganismo() {
        return resultadoOrganismo;
    }

    public void setResultadoOrganismo(String resultadoOrganismo) {
        this.resultadoOrganismo = resultadoOrganismo;
    }

    public Objetivos getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivos objetivo) {
        this.objetivo = objetivo;
    }

    public Organismo getOrganismo() {
        return organismo;
    }

    public void setOrganismo(Organismo organismo) {
        this.organismo = organismo;
    }

    public OrganismoFacadeLocal getOrganismoFacade() {
        return organismoFacade;
    }

    public void setOrganismoFacade(OrganismoFacadeLocal organismoFacade) {
        this.organismoFacade = organismoFacade;
    }

    public Long getCodOrganigrama() {
        return codOrganigrama;
    }

    public void setCodOrganigrama(Long codOrganigrama) {
        this.codOrganigrama = codOrganigrama;
    }

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    @Override
    public List<Organismo> getListElements() {
        if (this.getLeyendaBuscar().equals("")) {
            this.setList(this.organismoFacade.findAll(true));
        } else {
            this.setList(this.organismoFacade.findAll(this.getLeyendaBuscar().toUpperCase()));
        }
        return (List<Organismo>) this.getList();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Organismo> lstOrganismo = this.organismoFacade.findAll(true);
        if (!(lstOrganismo.isEmpty())) {
            for (Organismo orgAux : lstOrganismo) {
                selectItems.add(new SelectItem(orgAux.getId(), orgAux.getCodOrganigrama() + "-" + orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
    public List<Objetivos> getListElementsObj() {
       if (this.getId() != null){
            this.setList(this.objetivosFacade.findAll(this.getId(), ""));}
            return (List<Objetivos>) this.getList();
    }

    @Override
    public void limpiar() {
        this.setIdOrganismo(null);
        this.setCodOrganigrama(null);
        this.setLeyenda(null);
        this.setDescripcion(null);
    }

    @Override
    public String crear() {
        try{
            this.organismoFacade.create(this.getIdOrganismo(), this.getCodOrganigrama(), this.getLeyenda(), this.getDescripcion(),this.isDescentralizado());
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorOrganismo");
            this.setMsgSuccessError("El organismo ha sido generado con éxito");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo ...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorOrganismo");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.organismoFacade.create(this.getIdOrganismo(), this.getCodOrganigrama(), this.getLeyenda(), this.getDescripcion(), this.isDescentralizado());            
            this.setResultado("nuevoOrganismo");            
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo ...");
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
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Organismo organismoAux = this.organismoFacade.find(idAux);
        this.setId(organismoAux.getId());
        this.setCodOrganigrama(organismoAux.getCodOrganigrama());
        this.setLeyenda(organismoAux.getLeyenda());
        this.setDescripcion(organismoAux.getDescripcion());
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            this.organismoFacade.remove(idAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("organismoConf");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorOrganismo");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Organismo organismoAux = this.organismoFacade.find(idAux);
        this.setId(organismoAux.getId());
        this.setCodOrganigrama(organismoAux.getCodOrganigrama());
        this.setLeyenda(organismoAux.getLeyenda());
        this.setDescripcion(organismoAux.getDescripcion());        
    }

    @Override
    public String guardarEdicion() {
        try {
            this.organismoFacade.edit(this.getId(), this.getCodOrganigrama(), this.getLeyenda(), this.getDescripcion(),this.isDescentralizado());      
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorOrganismo");
            this.setMsgSuccessError("El organismo ha sido editado con éxito");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorOrganismo");
        }
        return this.getResultado();
    }
     public List<Organismo> getListElementsOrg() {
        if (this.getLeyendaBuscar().equals("") && this.getIdOrganismo()==0L) {
            this.setList(this.organismoFacade.findAll(true,false));
        } else {
            this.setList(this.organismoFacade.findAll(this.getLeyendaBuscar().toUpperCase(), this.getIdOrganismo()));
        }
        return (List<Organismo>) this.getList();
    }
     
   public void verPanelMeta(){
       this.setMostrarPanelMeta(true);
       this.setMostrarPanelObra(false);
       this.setMostrarIndicador(false);
       
   }
   
   public void verPanelObra(){
       this.setMostrarPanelMeta(false);
       this.setMostrarPanelObra(true);
       this.setMostrarIndicador(false);
       
   }
   
   public void verPanelMetaIndicador(){
       this.setMostrarPanelMeta(true);
       this.setMostrarPanelObra(false);
       this.setMostrarIndicador(true);
   }
   
     
    
} 
    