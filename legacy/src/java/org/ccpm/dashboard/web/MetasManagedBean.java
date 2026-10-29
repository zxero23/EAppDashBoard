/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Metas;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Objetivos;
import org.ccpm.dashboard.entity.Situacion;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.facade.MetasFacadeLocal;
import org.ccpm.dashboard.facade.ObjetivosFacadeLocal;
import org.ccpm.dashboard.facade.SituacionFacadeLocal;
import org.ccpm.dashboard.facade.IndicadorFacadeLocal;
import org.ccpm.dashboard.facade.OrganismoFacadeLocal;

/**
 *
 * @author Alvarenga Angel
 */
@ManagedBean
@RequestScoped
public class MetasManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private MetasFacadeLocal metasFacade;
    @EJB
    private ObjetivosFacadeLocal objetivoFacade;
    @EJB
    private SituacionFacadeLocal situacionFacade;
    @EJB
    private IndicadorFacadeLocal indicadorFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    
    private String leyenda;
    private Long idObjetivos=0L;
    private Long idOrganismo=0L;
    private Long idSituacion;
    private Long idIndicador;
    private int cantidad;
    private Date fechaInicio;
    private Date fechaFinal;
    private List <ActionItem> lstActionItems = new ArrayList <>();
    private Metas metas;
    private String busquedaPor="0";
    private String descripcion;
    private Objetivos objetivo;
    private boolean mostrarMeta=false; //oculta panel de las metas
    private boolean reduccion=false;
    private boolean aumentar=false;
    private int estado;
    private List<Metas> lista = new ArrayList <>();
    private Long idOrg;
    private Long idOrgUser=0L;
    
    /** Creates a new instance of MetasManagedBean */
    public MetasManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevaMeta")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarMeta")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarMeta")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleMeta")) {
                        this.setDetalle(true);
                    }
                    //System.out.println("acciones metas--->"+accionAux.getNombre());
                }
            }
            System.out.println("organismo q pertenece el usuario" +usuario.getNombre()+" " +usuario.getOrganismo().getLeyenda());
            this.setIdOrganismo(usuario.getOrganismo().getId());
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public Long getIdOrgUser() {
        return idOrgUser;
    }

    public void setIdOrgUser(Long idOrgUser) {
        this.idOrgUser = idOrgUser;
    }

    
    
    public boolean isAumentar() {
        
        return aumentar;
    }

    public void setAumentar(boolean aumentar) {
        if(aumentar)this.setReduccion(false);
        this.aumentar = aumentar;
    }

    
    
    public boolean isReduccion() {
        
        return reduccion;
    }

    public void setReduccion(boolean reduccion) {
        if(reduccion)this.setAumentar(false);
        this.reduccion = reduccion;
    }
    
    

    public Long getIdOrg() {
        return idOrg;
    }

    public void setIdOrg(Long idOrg) {
        this.idOrg = idOrg;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    

    public List<Metas> getLista() {
        return lista;
    }

    public void setLista(List<Metas> lista) {
        this.lista = lista;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    
    
    public boolean isMostrarMeta() {
        return mostrarMeta;
    }

    public void setMostrarMeta(boolean mostrarMeta) {
        this.mostrarMeta = mostrarMeta;
    }

    
    
    public Objetivos getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivos objetivo) {
        this.objetivo = objetivo;
    }

   
    
    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    
    
    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }
    
    

    public String getBusquedaPor() {
        return busquedaPor;
    }

    public void setBusquedaPor(String busquedaPor) {
        this.busquedaPor = busquedaPor;
    }

    
    
    public Metas getMetas() {
        return metas;
    }

    public void setMetas(Metas metas) {
        this.metas = metas;
    }
    
    

    public Long getIdIndicador() {
        return idIndicador;
    }

    public void setIdIndicador(Long idIndicador) {
        this.idIndicador = idIndicador;
    }
    
    
    
    public Long getIdSituacion() {
        return idSituacion;
    }

    public void setIdSituacion(Long idSituacion) {
        this.idSituacion = idSituacion;
    }

    
    
    public int getCantidad() {
        return cantidad;
    }

    

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public Long getIdObjetivos() {
        return idObjetivos;
    }

    public void setIdObjetivos(Long idObjetivos) {
        this.idObjetivos = idObjetivos;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

  @Override
    public List<Metas> getListElements() {   //BUSQUEDA POR LEYENDA,OBJETIVOS                    
       
        if (this.getLeyendaBuscar().equals("") && this.getIdObjetivos()==0L && this.getIdOrganismo()==0L) {
            this.setList(this.metasFacade.findAll(true));
        } else {
            
            this.setList(this.metasFacade.findAll(this.getIdObjetivos(),this.getIdOrganismo(),this.getLeyendaBuscar()));
                    
        }return (List<Metas>) this.getList();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Objetivos> lstObjetivos = new ArrayList <>();
        if (this.getIdOrganismo()==0L){lstObjetivos = this.objetivoFacade.findAll(true);}
            else {lstObjetivos = this.objetivoFacade.findAll(true,this.getIdOrganismo());}
        if (!(lstObjetivos.isEmpty())) {
            for (Objetivos orgAux : lstObjetivos) {
                selectItems.add(new SelectItem(orgAux.getId(), orgAux.getId()+ "-" + orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
    
    public List<SelectItem> getSelectItemsSituacion() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Situacion> lstSituacion = this.situacionFacade.findAll(true);
        if (!(lstSituacion.isEmpty())) {
            for (Situacion sitAux : lstSituacion) {
                selectItems.add(new SelectItem(sitAux.getId(), sitAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
  

    @Override
    public void limpiar() {
        this.setLeyenda(null);
       
    }

    @Override
    public String crear() {
        try{
            System.out.println();
            this.metasFacade.create(this.getIdObjetivos(),this.getIdSituacion(), this.getLeyenda(), this.getCantidad(),this.fechaInicio,this.fechaFinal,this.getDescripcion(),this.reduccion);
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorMetas");
            this.setMsgSuccessError("La meta ha sido generada con éxito");
            this.limpiar();
        }catch(Exception ex){
            
            this.setTitle("Resultado del Chequeo ...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMetas");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.metasFacade.create(this.getIdObjetivos(),this.getIdSituacion(),this.getLeyenda(),this.getCantidad(),this.fechaInicio,this.fechaFinal,this.getDescripcion(),this.reduccion);
            this.setResultado("nuevaMetas");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMetas");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        this.setMetas(this.metasFacade.find(idAux)); //la forma de crear detalle por angel ***xx****xxx
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            this.metasFacade.remove(idAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("metasConf");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMetas");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Metas metasAux = this.metasFacade.find(idAux);
        this.setId(metasAux.getId());
        this.setLeyenda(metasAux.getLeyenda()); 
        this.setCantidad(metasAux.getCantidad());
        this.setFechaInicio(metasAux.getFechaInicio());
        this.setFechaFinal(metasAux.getFechaFinal());
        this.setDescripcion(metasAux.getDescripcion());
        this.setIdObjetivos(metasAux.getObjetivos().getId());
        this.setIdSituacion(metasAux.getSituacion().getId());
        this.setReduccion(metasAux.isReduccion());
        System.out.println("Holaaaaaaaaaaaaaaaaaaaaaa!!!!!");
    }

    @Override
    public String guardarEdicion() {
        try {
            this.metasFacade.edit(this.getIdObjetivos(),this.getIdSituacion(),this.getId(), this.getLeyenda(),this.getCantidad(),this.fechaInicio,this.fechaFinal,this.getDescripcion(),this.reduccion);
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorMetas");
            this.setMsgSuccessError("La meta ha sido editada con éxito");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMetas");
        }
        return this.getResultado();
    }
    
    
    
    
    
  
    
     public int devolverEstado(Long id,int estado){
         return this.metasFacade.totalMetasPorEstado(id,estado);
     }
     
     public void setearObjEstado(Long id,int estado){
        
         this.setIdObjetivos(id);
         this.setEstado(estado);
         this.setLista(this.metasFacade.findAll(this.idObjetivos, this.estado));
        // System.out.println("-----------id-->"+this.idObjetivos+"---estado-->"+this.estado);
         
     }
     
     public List<Metas> devolverLista(){
         
         this.setLista(this.metasFacade.findAll(this.getIdOrg(), this.getEstado()));
         return (List<Metas>) this.metasFacade.findAll(this.getIdOrg(), this.getEstado());
     }

    
}

