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
import javax.faces.application.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
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
import org.ccpm.reporte.*;
import org.ccpm.util.*;

/**
 *
 * @author Alvarenga Angel
 */
@ManagedBean
@ViewScoped
public class MetaPresentacionManagedBean extends UtilManagedBean implements Serializable{
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
    private int estado;
    private List<Metas> lista = new ArrayList <>();
    private Long idOrg;
    private String nombreOrg;
    private String nombreEstado;
    private boolean mostrarPanelMeta=false; 
    private boolean mostrarPanelObra=false; //panel con obras por estado y organismo.
    private boolean mostrarIndicador=false;
    private boolean reduccion=false;
    public String focus="";
    public String grafico="";
    private boolean organismoRealizaObras=true;
    
    /** Creates a new instance of MetasManagedBean */
    public MetaPresentacionManagedBean() {
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
                    //System.out.println("acciones--->"+accionAux.getNombre());
                }
            }
            this.setIdOrganismo(usuario.getOrganismo().getId());
            this.setOrganismoRealizaObras(usuario.getOrganismo().isDescentralizado());
            
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public boolean isOrganismoRealizaObras() {
        return organismoRealizaObras;
    }

    public void setOrganismoRealizaObras(boolean organismoRealizaObras) {
        this.organismoRealizaObras = organismoRealizaObras;
    }

  
    
    
    public String getGrafico() {
        return grafico;
    }

    public void setGrafico(String grafico) {
        this.grafico = grafico;
    }
    
    

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        System.out.println(focus);
        this.focus = focus;
        
    }

    
    public boolean isReduccion() {
        return reduccion;
    }

    public void setReduccion(boolean reduccion) {
        this.reduccion = reduccion;
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
    
    

    public boolean isMostrarIndicador() {
        return mostrarIndicador;
    }

    public void setMostrarIndicador(boolean mostrarIndicador) {
        this.mostrarIndicador = mostrarIndicador;
    }
    
    

    public String getNombreEstado() {
        return nombreEstado;
    }

    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }

    
    public String getNombreOrg() {
        return nombreOrg;
    }

    public void setNombreOrg(String nombreOrg) {
        this.nombreOrg = nombreOrg;
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
            System.out.println("organismo---"+this.getIdObjetivos()+"leyenda--"+this.getLeyendaBuscar());
            this.setList(this.metasFacade.findAll(this.getIdObjetivos(),this.getIdOrganismo(),this.getLeyendaBuscar()));
                    
        }return (List<Metas>) this.getList();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Objetivos> lstObjetivos = this.objetivoFacade.findAll(true);
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
                selectItems.add(new SelectItem(sitAux.getId(), sitAux.getId()+ "-" + sitAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
  

    @Override
    public void limpiar() {
        this.setLeyenda(null);
        this.setLista(null);
       
    }

    @Override
    public String crear() {
        try{
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
        this.setIdObjetivos(metasAux.getObjetivos().getId());
        this.setIdSituacion(metasAux.getSituacion().getId());
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
         this.setMostrarMeta(false);
         
     }
     
     //cambios domingo 06 de noviembre metas por organismo
     public int devolverEstadoOrg(Long id,int estado){
         return this.metasFacade.totalMetasPorEstadoOrg(id, estado);
     }
     
     public void setearOrgEstado(Long idOrg,int estado,String nombreOrg){
         this.setMostrarMeta(true);
         this.setMostrarIndicador(false);
         this.setNombreOrg(nombreOrg);
         this.setNombreEstado((estado==7)?"EN TERMINO":(estado==8)?"EN ALERTA":(estado==10)?"FUERA DE TÉRMINO":"TERMINADO");
         this.setEstado(estado);
         this.setLista(this.metasFacade.findAllMetas(idOrg, estado));
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
       this.setFocus("grafico");
       this.setMostrarPanelMeta(true);
       this.setMostrarPanelObra(false);
       this.setMostrarIndicador(true);
   }
   
    public Resource impObj() {
        //yteArrayOutputStream baos = new ByteArrayOutputStream();
        Resource miRecurso = new MyResource(new byte[0]);
        try {
            JasperPrint jasperResultado = new Reporte().impObjetivos();
            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            miRecurso = new MyResource(bites);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return miRecurso;
        //return new ByteArrayResource(baos.toByteArray());
    }
    
    public Resource impMet() {
        //yteArrayOutputStream baos = new ByteArrayOutputStream();
        Resource miRecurso = new MyResource(new byte[0]);
        try {
            JasperPrint jasperResultado = new Reporte().impMetas();
            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            miRecurso = new MyResource(bites);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return miRecurso;
        //return new ByteArrayResource(baos.toByteArray());
    }
    
    
    public int  totalIndicadores(Long id){  //devuelve el total de indicadores por Meta
       Metas lstMeta=this.metasFacade.find(id);
       return lstMeta.getIndicador().size()>0?1:0;
       
        
    }

 
    
   
     
       
}


