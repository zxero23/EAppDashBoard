/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Objetivos;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Organismo;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.entity.Situacion;
import org.ccpm.dashboard.facade.ObjetivosFacadeLocal;
import org.ccpm.dashboard.facade.OrganismoFacadeLocal;
import org.ccpm.dashboard.facade.SituacionFacadeLocal;
/**
 *
 * @author Alvarenga Angel
 */
@Named
@ApplicationScoped
public class objetivoPresentacionManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private ObjetivosFacadeLocal objetivoFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    @EJB
    private SituacionFacadeLocal situacionFacade;
    @EJB
    private SituacionFacadeLocal metasFacade;
    private String leyenda;
    private String descripcion;
    private Long idOrganismo=0L;
    private Long idSituacion;
    private Objetivos objetivo;
    private List <ActionItem> lstActionItems = new ArrayList <>();
    private String busquedaPor="0";
    private boolean DisplayBusqueda;
    private int estado;
    private List <Objetivos> lista = new ArrayList <>();
    private OrganismoManagedBean organismoManagedBean;
    

    /** Creates a new instance of ObjetivosManagedBean */
    public objetivoPresentacionManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoObjetivos")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarObjetivos")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarObjetivos")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleObjetivos")) {
                        this.setDetalle(true);
                    }
                    
                }
            }
            this.setIdOrganismo(usuario.getOrganismo().getId());
            System.out.println("objePresentacion "+this.getIdOrganismo());
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public SituacionFacadeLocal getMetasFacade() {
        return metasFacade;
    }

    public void setMetasFacade(SituacionFacadeLocal metasFacade) {
        this.metasFacade = metasFacade;
    }
    
    
    public List<Objetivos> getLista() {
        return lista;
    }

    public void setLista(List<Objetivos> lista) {
        this.lista = lista;
    }

    
    
    
    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    
    
    

    public boolean isDisplayBusqueda() {
        return DisplayBusqueda;
    }

    public void setDisplayBusqueda(boolean DisplayBusqueda) {
        this.DisplayBusqueda = DisplayBusqueda;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    
    
    public String getBusquedaPor() {
        return busquedaPor;
    }

    public void setBusquedaPor(String busquedaPor) {
        this.busquedaPor = busquedaPor;
    }

    
    public Long getIdSituacion() {
        return idSituacion;
    }

    public void setIdSituacion(Long idSituacion) {
        this.idSituacion = idSituacion;
    }
    
    

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }
    
    

    @Override
    public List<Objetivos> getListElements() {   //BUSQUEDA POR LEYENDA,ORGANISMO                    
       
        if (this.getLeyendaBuscar().equals("") && this.getIdOrganismo()==0L) {
            this.setList(this.objetivoFacade.findAll(true));
        } else {
            System.out.println("organismo---"+this.getIdOrganismo()+"leyenda--"+this.getLeyendaBuscar());
            this.setList(this.objetivoFacade.findAll(this.getIdOrganismo(), this.getLeyendaBuscar()));
                    
        }return (List<Objetivos>) this.getList();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Organismo> lstOrganismo = this.organismoFacade.findAll(true);
        if (!(lstOrganismo.isEmpty())) {
            for (Organismo orgAux : lstOrganismo) {
                selectItems.add(new SelectItem(orgAux.getId(),orgAux.getCodOrganigrama()+ "-" +orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
    public List<SelectItem> getSelectItemsSituacion() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Situacion> lstSituacion = this.situacionFacade.findAll(true);
        if (!(lstSituacion.isEmpty())) {
            for (Situacion sitAux : lstSituacion) {
                selectItems.add(new SelectItem(sitAux.getId(),sitAux.getId()+ "-" + sitAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
   
    
     
    
    

    public Objetivos getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivos objetivo) {
        this.objetivo = objetivo;
    }

    @Override
    public void limpiar() {
        this.setLeyenda(null);
       // this.setIdOrganismo(null);
       // this.setIdSituacion(null);
    }

    @Override
    public String crear() {
        try{
            this.objetivoFacade.create(this.getIdOrganismo(),this.getIdSituacion(), this.getLeyenda(), this.getDescripcion()); //para crear se pasa el id de objetivo
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorObjetivos");
            this.setMsgSuccessError("El objetivo ha sido generada con éxito");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo ...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorObjetivos");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.objetivoFacade.create(this.getIdOrganismo(),this.getIdSituacion(), this.getLeyenda(),this.getDescripcion());
            this.setResultado("nuevoObjetivos");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorObjetivos");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        this.setObjetivo(this.objetivoFacade.find(idAux));
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            this.objetivoFacade.remove(idAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("objetivosConf");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorObjetivos");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Objetivos objetivoAux = this.objetivoFacade.find(idAux);
        this.setId(objetivoAux.getId());
        this.setLeyenda(objetivoAux.getLeyenda()); 
        this.setIdOrganismo(objetivoAux.getOrganismo().getId());
        this.setIdSituacion(objetivoAux.getSituacion().getId());
        System.out.println("Holaaaaaaaaaaaaaaaaaaaaaa!!!!!");
    }

    @Override
    public String guardarEdicion() {
        try {
            this.objetivoFacade.edit(this.getIdOrganismo(),this.getIdSituacion(),this.getId(), this.getLeyenda(),this.getDescripcion());
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorObjetivos");
            this.setMsgSuccessError("El Objetivo ha sido editado con éxito");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorObjetivos");
        }
        return this.getResultado();
    }
    
    
     
     
     public void sabe(){
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Organismo organismoAux = this.organismoFacade.find(idAux);
        this.setIdOrganismo(organismoAux.getId());
        System.out.println(this.getIdOrganismo());      
        
    }
     
     
     public int devolverEstado(Long id,int estado){
         return this.objetivoFacade.totalObjetivosPorEstado(id,estado);
     }
     
     public void setearOrgEstado(Long id,int estado){
         this.setIdOrganismo(id);
         this.setEstado(estado);
         this.setLista(this.objetivoFacade.findAll(this.idOrganismo,this.estado));
         
         
         
         
         
     }
     
    
        
        
         
     
     }
      
      
     
      
      
      
      
   
    
    

    


