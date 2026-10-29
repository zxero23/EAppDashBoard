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
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
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
import org.icefaces.ace.component.fileentry.FileEntry;
import org.icefaces.ace.component.fileentry.FileEntryEvent;
import org.icefaces.ace.component.fileentry.FileEntryResults;
import org.icefaces.ace.util.IceOutputResource;


/**
 *
 * @author Alvarenga Angel
 */
@ManagedBean
@ViewScoped
public class ObrasPresentacionManagedBean extends UtilManagedBean implements Serializable{
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
    private Long idOrganismo=0L;
    private Long idLocalidad=0L;
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
    private String imas="icon:'images/IMAS.png'";
    private String uscepp="icon:'images/usscpe.png'";
    private Resource resource,resource2;
    private boolean panelObraActiva=false;
    private boolean botonAdjuntoObraTablero=false;
    private boolean verImagen=false;
    private String fotoPath;
    private String path;
    private String[] arrayFotos=new String[5];
    private Resource[] arrayResource=new Resource[5];
    private String reemplazar="DatosTablero";
    private int tamanioFotos=0;
    private IceOutputResource resourceImage;
    private byte[] byteArrayImage;
    private boolean organismoRealizaObras=true;
    private Long idOrgAux=0L;
   
    

    /** Creates a new instance of ObrasManagedBean */
    public ObrasPresentacionManagedBean( ) {
        
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
                    
                }
            }
            this.setIdOrganismo(usuario.getOrganismo().getId()); //setea el organismo del usuario logueado
            this.setIdOrgAux(usuario.getOrganismo().getId());
            System.out.println("id "+usuario.getOrganismo().getId());
            this.setOrganismoRealizaObras(usuario.getOrganismo().isDescentralizado());
            //verifica si el ministerio del usuario realiza obras o si es super
            this.setOrganismoRealizaObras((usuario.getOrganismo().isDescentralizado() || this.idOrganismo==0));
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public Long getIdOrgAux() { // variable para el boton mostrar todas las obras
        return idOrgAux;
    }

    public void setIdOrgAux(Long idOrgAux) {
        this.idOrgAux = idOrgAux;
    }

    
    
    public boolean isOrganismoRealizaObras() {
        return organismoRealizaObras;
    }

    public void setOrganismoRealizaObras(boolean organismoRealizaObras) {
        this.organismoRealizaObras = organismoRealizaObras;
    }
    
    

    public int getTamanioFotos() {
        return tamanioFotos;
    }

    public void setTamanioFotos(int tamanioFotos) {
        this.tamanioFotos = tamanioFotos;
    }
    
    

    public String getUscepp() {
        return uscepp;
    }

    public void setUscepp(String uscepp) {
        this.uscepp = uscepp;
    }

    
    public String getImas() {
        return imas;
    }

    public void setImas(String imas) {
        this.imas = imas;
    }
    
    public Long getIdLocalidad() {
        return idLocalidad;
    }

    public void setIdLocalidad(Long idLocalidad) {
        this.idLocalidad = idLocalidad;
    }
    

    public Resource[] getArrayResource() {
        return arrayResource;
    }

    public void setArrayResource(Resource[] arrayResource) {
        this.arrayResource = arrayResource;
    }
    
    public void setArrayNull() {
      for (int i=0;i<arrayResource.length;i ++)  {
        this.arrayResource[i] = null;
        }
      this.setVerImagen(false);
    }

    public String getReemplazar() {
        return reemplazar;
    }

    public void setReemplazar(String reemplazar) {
        this.reemplazar = reemplazar;
    }
        

    public String[] getArrayFotos() {
        return arrayFotos;
    }

    public void setArrayFotos(String[] arrayFotos) {
        this.arrayFotos = arrayFotos;
    }
    

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        
        this.fotoPath = fotoPath;
    }

    
    
    public IceOutputResource getResourceImage() {
        return resourceImage;
    }

    public void setResourceImage(IceOutputResource resourceImage) {
        this.resourceImage = resourceImage;
    }

    public byte[] getByteArrayImage() {
        return byteArrayImage;
    }

    public void setByteArrayImage(byte[] byteArrayImage) {
        this.byteArrayImage = byteArrayImage;
    }

    
    
    public boolean isVerImagen() {
        return verImagen;
    }

    public void setVerImagen(boolean verImagen) {
        this.verImagen = verImagen;
    }
    
    

    public Resource getResource2() {
        return resource2;
    }

    public void setResource2(Resource resource2) {
        this.resource2 = resource2;
    }
    

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
    
    // cambios 2 de abril, para mostrar todas las obras por organismo y situacion
    public List<Obras> ListarObras(){
        this.setList(this.obrasFacade.findAll(this.getSituacionBuscar(), this.getLeyendaBuscar(),this.getIdOrganismo(),this.getIdLocalidad()));
        return (List<Obras>) this.getList();
    }
    

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        
        List <Situacion> lstSituacions = this.situacionFacade.findAll(true);
        if (!(lstSituacions.isEmpty())) {
            for (Situacion orgAux : lstSituacions) {
                selectItems.add(new SelectItem(orgAux.getId(), orgAux.getId()+ "-" + orgAux.getLeyenda()));
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
        List <Organismo> lstOrganismo = this.organismoFacade.findAll(true,true);
        if (!(lstOrganismo.isEmpty())) {
            for (Organismo orgAux : lstOrganismo) {
                selectItems.add(new SelectItem(orgAux.getId(),orgAux.getCodOrganigrama()+ "-" +orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
    public List<SelectItem> getSelectItemsObras() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Obras> lstObras = this.obrasFacade.findAll(true);
        if (!(lstObras.isEmpty())) {
            for (Obras orgAux : lstObras) {
                selectItems.add(new SelectItem(orgAux.getId(), orgAux.getId()+ "-" + orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        this.setLeyenda(null);
        this.setIdSituacion(null);
        this.setLatitud(0);
        this.setLongitud(0);
        this.setPath("");
    }

  


    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        this.setObras(this.obrasFacade.find(idAux));
        System.out.println(this.getObras().getAdjunto());
        this.initMetaData(); //paso el parametro que trae de la bd al metodo
        
        
        
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
        this.setAdjunto(obrasAux.getAdjunto());
        this.setIdOrganismo(obrasAux.getOrganismo().getId());
        this.setIdSituacion(obrasAux.getSituacion().getId());
       
        
        
    }


    public int devolverEstado(Long id,int estado){
        
         return this.obrasFacade.totalObrasPorEstado(id,estado);
     }
    
     public List<Organismo> getListElementsEnte() {   //devuelve organismos descentralizados
        if (this.getLeyendaBuscar().equals("") && this.getIdOrganismo()==0L) {
            this.setList(this.organismoFacade.findAll(true,true)); //si estado=true y descentralizado=true
        } else {
            this.setList(this.organismoFacade.findAll(this.getLeyendaBuscar().toUpperCase(),this.getIdOrganismo()));
            System.out.println("entrooooooooooooooooooooooo");
        }
        return (List<Organismo>) this.getList();
    }
     
    //devuelve el evento de la carga de archivos, como por ej el path del archivo subido
     public void customValidator(FileEntryEvent entryEvent) { //aaandaaaa
        FileEntryResults results = ((FileEntry) entryEvent.getComponent()).getResults();
        for (FileEntryResults.FileInfo file : results.getFiles()) {
            this.setAdjunto(file.getFile().getAbsolutePath());
            if (file.isSaved()) {
                System.out.println(file.getFileName());
                System.out.println(file.getContentType());
                System.out.println(file.getFile().getAbsolutePath());
                this.setAdjunto(file.getFile().getAbsolutePath());
                
            }
        }
     }
     
    public void initMetaData() { //devuelve el archivo adjunto segun el path que se le pase
        String resourcePath = path;
        File file = new File(resourcePath);
        try {
            this.resource = new ObrasPresentacionManagedBean.MyResource(readIntoByteArray(new FileInputStream(file)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            this.resource = new ObrasPresentacionManagedBean.MyResource(new byte[0]);
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
       
        this.setIdLocalidad(0L);
        
        this.setPanelObraActiva(true);
        this.setIdOrganismo(idOrg);
        this.setSituacionBuscar(estado);
        this.setBotonAdjuntoObraTablero(false);
        this.setVerImagen(false);
        
        
    }

    
    public void verAdjuntoTabla(String ruta){
        this.setResource(null);
        this.setPath(ruta);
        this.setPanelObraActiva(true);
        this.initMetaData();
        this.setBotonAdjuntoObraTablero(true);
    }
    
    public void limpiarFotos(){
       this.setResource2(null);
       this.setArrayNull();
    }
    
     public void verFotoObra(String[] ruta){ //al metodo paso al array de String desde la BD
         this.setArrayNull();
         this.setTamanioFotos(0); //setea el navegador de imagenes a 0
            
         if (ruta != null) {
            this.setResource2(null);
            this.fotoPath="";
            this.setVerImagen(false);
            this.setResource2(null);
            this.setArrayFotos(ruta);
            this.initMetaDataFotos();
            this.setVerImagen(true);   
        }
    }
    
    
      @Override
    public String crear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String crearOtro() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String guardarEdicion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    //lunes 21 de noviembre 2016
    
    public void initMetaDataFotos() { //devuelve el archivo adjunto segun el path que se le pase
        // IceOutputResource image
      for (int i=0;i<this.arrayResource.length;i ++){  
        String resourcePath = this.arrayFotos[i];
        System.out.println("fotoooossss arrayyyyy "+this.arrayFotos[i]);
        File file = new File(resourcePath);
        try {
            //es muy importante colocar Math.random() para que la imagen cambie dentro del la vista!!!!! (un dolor de cabeza)
            this.arrayResource[i] = new IceOutputResource("resourceGraphicImage"+Math.random(), readIntoByteArray2(new FileInputStream(file)), "image/png");
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            this.arrayResource[i] = new IceOutputResource("resourceGraphicImage", new byte[0], "image/png");
        }
      }
       
    
       
        
    }
    
    private static byte[] readIntoByteArray2(InputStream in) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();

        return out.toByteArray();
    }
    
    
    //marzo 22 del 2017, para ver imagenes de las obra tipo carousel, para 5 imagenes
    public void siguiente(){
        if (this.getTamanioFotos()<4){
            this.setTamanioFotos(this.getTamanioFotos()+1);
        }
    }
    public void atras(){
        if (this.getTamanioFotos()>0){
        this.setTamanioFotos(this.getTamanioFotos()-1);
        }
    }
    
   
}
    
  

    




