/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.Resource;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Obras;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Localidad;
import org.ccpm.dashboard.entity.Organismo;
import org.ccpm.dashboard.entity.Situacion;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.facade.LocalidadFacadeLocal;
import org.ccpm.dashboard.facade.ObrasFacadeLocal;
import org.ccpm.dashboard.facade.OrganismoFacadeLocal;
import org.ccpm.dashboard.facade.SituacionFacadeLocal;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author Alvarenga Angel
 */
@Named
@RequestScoped
public class ObrasManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private ObrasFacadeLocal obrasFacade;
    @EJB
    private SituacionFacadeLocal situacionFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    @EJB
    private LocalidadFacadeLocal localidadFacade;
    private String leyenda;
    private Long idSituacion;
    private Long idLocalidad=0L;
    private Long idOrganismo=0L;
    private float Latitud=(float) -26.7;
    private float Longitud=(float) -54.89;
    private Date FechaInicio,FechaFinal;
    private Obras obras;
    private String busquedaPor="0";
    private String adjunto;
    private List <ActionItem> lstActionItems = new ArrayList <>();
    private Long situacionBuscar=0L;
    private Long idObra=0L;
    private String casco="icon:'images/casco1.png'";
    private String iprodha="icon:'images/iprodha1.png'";
    private String vialidad="icon:'images/vialidad1.png'";
    private String logo="icon:'images/globo_verde.png'";
    private Resource resource;
    private boolean panelObraActiva=false;
    private boolean botonAdjuntoObraTablero=false;
    private int sizeFotos;
    private String[] fotos=new String[100];
    private boolean verBoton=false;
    private Long idOrgAux=0L;

    /** Creates a new instance of ObrasManagedBean */
    public ObrasManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevaObra")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarObra")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarObra")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleObra")) {
                        this.setDetalle(true);
                    }
                    
                    //System.out.println("acciones obraaaassss"+accionAux.getNombre());
                    
                }
                this.limpiar();
                //System.out.println("obraConf "+usuario.getOrganismo().getId());
                this.setIdOrgAux(usuario.getOrganismo().getId());
                this.setIdOrganismo(usuario.getOrganismo().getId());
                
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public Long getIdOrgAux() {
        return idOrgAux;
    }

    public void setIdOrgAux(Long idOrgAux) {
        this.idOrgAux = idOrgAux;
    }

    
    
    public Long getIdLocalidad() {
        return idLocalidad;
    }

    public void setIdLocalidad(Long idLocalidad) {
        this.idLocalidad = idLocalidad;
    }

    
    
    public boolean isVerBoton() {
        return verBoton;
    }

    public void setVerBoton(boolean verBoton) {
        this.verBoton = verBoton;
    }
    
    
    
    public int getSizeFotos() {
        return sizeFotos;
    }

    public void setSizeFotos(int sizeFotos) {
        this.sizeFotos = sizeFotos;
        
    }

    
    
    public String[] getFotos() {
        return fotos;
    }

    public void setFotos(String[] fotos) {
         this.fotos = fotos;
         
        
    }
    
    

    public boolean isBotonAdjuntoObraTablero() {
        return botonAdjuntoObraTablero;
    }

    public void setBotonAdjuntoObraTablero(boolean botonAdjuntoObraTablero) {
        this.botonAdjuntoObraTablero = botonAdjuntoObraTablero;
    }

    
    public boolean isPanelObraActiva() {
        return panelObraActiva;
    }

    public void setPanelObraActiva(boolean panelObraActiva) {
        this.panelObraActiva = panelObraActiva;
    }
    

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    
    
    public String getAdjunto() {
        return adjunto;
    }

    public void setAdjunto(String adjunto) {
        this.adjunto = adjunto;
    }
    
    public String getVialidad() {
        return vialidad;
    }

    public void setVialidad(String vialidad) {
        this.vialidad = vialidad;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getIprodha() {
        return iprodha;
    }

    public void setIprodha(String iprodha) {
        this.iprodha = iprodha;
    }

    public String getCasco() {
        return casco;
    }

    public void setCasco(String casco) {
        this.casco = casco;
    }

    
    
    public Date getFechaInicio() {
        return FechaInicio;
    }

    public void setFechaInicio(Date FechaInicio) {
        this.FechaInicio = FechaInicio;
    }

    public Date getFechaFinal() {
        return FechaFinal;
    }

    public void setFechaFinal(Date FechaFinal) {
        this.FechaFinal = FechaFinal;
    }

    
    
    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }

    public Long getIdObra() {
        return idObra;
    }

    public void setIdObra(Long idObra) {
        this.idObra = idObra;
    }

    
    
    public long getSituacionBuscar() {
        return situacionBuscar;
    }

    public void setSituacionBuscar(long situacionBuscar) {
        this.situacionBuscar = situacionBuscar;
    }

   
    public String getBusquedaPor() {
        return busquedaPor;
    }

    public void setBusquedaPor(String busquedaPor) {
        this.busquedaPor = busquedaPor;
    }

    
    
    public Obras getObras() {
        return obras;
    }

    public void setObras(Obras obras) {
        this.obras = obras;
    }

    
    
    public Long getIdSituacion() {
        return idSituacion;
    }

    public void setIdSituacion(Long idSituacion) {
        this.idSituacion = idSituacion;
    }

    
    
    public float getLatitud() {
      
        return Latitud;
    }

    public float getLongitud() {
        return Longitud;
    }

    public void setLatitud(float Latitud) {
        this.Latitud = Latitud;
    }

    public void setLongitud(float Longitud) {
        this.Longitud = Longitud;
    }

  

    
    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    @Override
    public List<Obras> getListElements() {
        //System.out.println(this.situacionBuscar+"---"+this.idOrganismo+"---"+this.idLocalidad);
        if (this.getLeyendaBuscar().equals("") && this.situacionBuscar==0L && this.idOrganismo==0L && this.idLocalidad==0L) {
            this.setList(this.obrasFacade.findAll(true));
            
        } else {
            
           // System.out.println("Situacion---"+this.getSituacionBuscar()+"Leyenda--"+this.getLeyendaBuscar());
            
            this.setList(this.obrasFacade.findAll(this.getSituacionBuscar(), this.getLeyendaBuscar(),this.getIdOrgAux(),this.getIdLocalidad()));
            
        }
       
        return (List<Obras>) this.getList();
        
        
        
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Situacion> lstSituacions = this.situacionFacade.findAll(true);
        if (!(lstSituacions.isEmpty())) {
            for (Situacion orgAux : lstSituacions) {
                selectItems.add(new SelectItem(orgAux.getId(), orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
    
    public List<SelectItem> getSelectItemsLocalidad() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Localidad> lstLocalidad = this.localidadFacade.findAll();
        if (!(lstLocalidad.isEmpty())) {
            for (Localidad locAux : lstLocalidad) {
                selectItems.add(new SelectItem(locAux.getId(),locAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
    public List<SelectItem> getSelectItemsOrg() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Organismo> lstOrganismo = new ArrayList <>();
        if (this.getIdOrganismo() == 0L) {lstOrganismo = this.organismoFacade.findAll(true,true);}
            else {lstOrganismo = this.organismoFacade.findAll(this.getIdOrganismo());}
        if (!(lstOrganismo.isEmpty())) {
            for (Organismo orgAux : lstOrganismo) {
                selectItems.add(new SelectItem(orgAux.getId(),orgAux.getCodOrganigrama()+ "-" +orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
    public List<SelectItem> getSelectItemsObras() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Obras> lstObras = this.obrasFacade.findAll(true,this.getIdOrganismo());
        if (!(lstObras.isEmpty())) {
            for (Obras orgAux : lstObras) {
                selectItems.add(new SelectItem(orgAux.getId(), orgAux.getId()+ "-" + orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        
        this.setIdOrgAux(0L);
        this.setLeyenda("");
        this.setIdSituacion(0L);
        this.setSituacionBuscar(0L);
        this.setLatitud(0);
        this.setLongitud(0);
        this.setIdOrganismo(0L);
        this.setIdLocalidad(0L);
//        this.setFechaInicio(null);
//        this.setFechaFinal(null);
//        this.setAdjunto(null);
//        this.setFotos(null);
//        this.setIdLocalidad(null);
    }

    @Override
    public String crear() {
        try{
            this.obrasFacade.create(this.getIdSituacion(), this.getLeyenda(),this.getLatitud(),this.getLongitud(),this.getIdOrganismo(),this.getFechaInicio(),this.getFechaFinal(),this.getAdjunto(),this.fotos,this.getIdLocalidad());
            System.out.println("guardaaa "+this.fotos[0]);
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorObras");
            this.setMsgSuccessError("La obra ha sido generada con éxito");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo ...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorObras");
        }
        this.limpiar();
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.obrasFacade.create(this.getIdSituacion(), this.getLeyenda(),this.getLatitud(),this.getLongitud(),this.getIdOrganismo(),this.getFechaInicio(),this.getFechaFinal(),this.getAdjunto(),this.getFotos(),this.getIdLocalidad());
            this.setResultado("nuevaObras");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorObras");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        this.setObras(this.obrasFacade.find(idAux));
        System.out.println(this.getObras().getAdjunto());
        this.initMetaData(this.getObras().getAdjunto()); //paso el parametro que trae de la bd al metodo
        
        
        
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            this.obrasFacade.remove(idAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("obrasConf");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorObras");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Obras obrasAux = this.obrasFacade.find(idAux);
        this.setId(obrasAux.getId());
        this.setLeyenda(obrasAux.getLeyenda()); 
        this.setLatitud(obrasAux.getLatitud());
        this.setLongitud(obrasAux.getLongitud());
        this.setFechaInicio(obrasAux.getFechaInicio());
        this.setFechaFinal(obrasAux.getFechaFinal());
        this.setIdLocalidad(obrasAux.getLocalidad().getId());
        this.setIdOrganismo(obrasAux.getOrganismo().getId());
        this.setIdSituacion(obrasAux.getSituacion().getId());
        if (obrasAux.getFotos() != null){this.setFotos(obrasAux.getFotos());}
        if (obrasAux.getAdjunto()!=null) {   
            this.setAdjunto(obrasAux.getAdjunto());
            
            System.out.println("tamaño 2 de abril "+obrasAux.getFotos()[0]); //porq mierda no adnaaa
            this.initMetaData(obrasAux.getAdjunto());
        }else{
            System.out.println("no tiene fotooooo anda por el null del array");
            this.setAdjunto("");
            //this.initMetaData("");
            
        }
        
       
        
        System.out.println("Holaaaaaaaaaaaaaaaaaaaaaa!!!!!");
        
    }

    @Override
    public String guardarEdicion() {
        try {
            System.out.println(this.getLatitud()+"--"+this.getLongitud());
            this.obrasFacade.edit(this.getIdSituacion(),this.getId(), this.getLeyenda(),this.getLatitud(),this.getLongitud(),this.getIdOrganismo(),this.getFechaInicio(),this.getFechaFinal(),this.getAdjunto(),this.getFotos(),this.getIdLocalidad());
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorObras");
            this.setMsgSuccessError("La Obra ha sido editada con éxito");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorObras");
        }
        this.limpiar();
        return this.getResultado();
    }

    public int devolverEstado(Long id,int estado){
         return this.obrasFacade.totalObrasPorEstado(id,estado);
     }
    
     public List<Organismo> getListElementsEnte() {   //devuelve organismos descentralizados
        if (this.getLeyendaBuscar().equals("")) {
            this.setList(this.organismoFacade.findAll(true,true)); //si estado=true y descentralizado=true
        } else {
            this.setList(this.organismoFacade.findAll(this.getLeyendaBuscar().toUpperCase()));
        }
        return (List<Organismo>) this.getList();
    }
     
    //devuelve el evento de la carga de archivos, y el path del archivo subido
     public void customValidator(FileUploadEvent entryEvent) {
        UploadedFile file = entryEvent.getFile();
        if (file != null) {
            this.setAdjunto(file.getFileName());
        }
     }
     
    public void initMetaData(String adjunto) { //devuelve el archivo adjunto segun el path que se le pase
        String resourcePath = adjunto;
        File file = new File(resourcePath);
        try {
            this.resource = new ObrasManagedBean.MyResource(readIntoByteArray(new FileInputStream(file)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            this.resource = new ObrasManagedBean.MyResource(new byte[0]);
        }
    }

    private static byte[] readIntoByteArray(InputStream in) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();

        return out.toByteArray();
    }
    
    public class MyResource extends Resource implements java.io.Serializable {

        private String path = "";
        private HashMap<String, String> headers;
        private byte[] bytes;
        
        public MyResource(byte[] bytes) {
            this.bytes = bytes;
            this.headers = new HashMap<String, String>();
        }
        
        public InputStream getInputStream() {
            return new ByteArrayInputStream(this.bytes);
        }

        public String getRequestPath() {
            return path;
        }
        
        public void setRequestPath(String path) {
            this.path = path;
        }

        public Map<String, String> getResponseHeaders() {
            return headers;
        }

        public URL  getURL() {
            return null;
        }

        public boolean userAgentNeedsUpdate(FacesContext context) {
            return false;
        }
    }
    
    public void setearOrgYestado(long idOrg,long estado){
        this.setPanelObraActiva(true);
        this.setIdOrganismo(idOrg);
        this.setSituacionBuscar(estado);
        
        
    }

    
    public void verAdjuntoTabla(String path){
        this.setPanelObraActiva(true);
        this.initMetaData(path);
        this.setBotonAdjuntoObraTablero(true);
    }
    
    
    //domingo 20 de noviembre 2016 GUARDA PATHS DE TODAS LAS FOTOS SUBIDAS
    public void customValidatorFotos(FileUploadEvent entryEvent) {
        UploadedFile file = entryEvent.getFile();
        if (file != null) {
            String[] ruta = this.getFotos() != null ? this.getFotos() : new String[100];
            for (int i = 0; i < ruta.length; i++) {
                if (ruta[i] == null || ruta[i].isBlank()) {
                    ruta[i] = file.getFileName();
                    break;
                }
            }
            this.setFotos(ruta);
            this.setVerBoton(true);
        }
     }
    
}


