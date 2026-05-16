package org.ccpm.sysse.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.Resource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.facade.ExpedienteFacadeLocal;
import org.ccpm.sysse.facade.InstrumentoLegalFacadeLocal;
import org.ccpm.sysse.facade.TipoInstrumentoLegalFacadeLocal;
import org.ccpm.sysse.utilidad.ArchivoResource;
import org.primefaces.model.LazyDataModel;

@Named
@SessionScoped
public class InstrumentoLegalManagedBean extends UtilManagedBean implements Serializable {

    @EJB
    private InstrumentoLegalFacadeLocal instrumentoLegalFacade;
    @EJB
    private TipoInstrumentoLegalFacadeLocal tipoInstrumentoLegalFacade;
    @EJB
    private ExpedienteFacadeLocal expedienteFacade;
    private Long idTipoInstrumentoLegal;
    private String numero;
    private String descripcion;
    private String expediente;
    private String tipoInstrumento;
    private int fojas;
    private List<ActionItem> lstActionItems = new ArrayList<ActionItem>();
    private InstrumentoLegal instAux;
    private String archivo = "";
    private String nombreArchivo = "";
    private Resource resource;
    private final String SistemaOperativo = System.getProperty("os.name");

    /**
     * Creates a new instance of InstrumentoLegalManagedBean
     */
    public InstrumentoLegalManagedBean() {
    }

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            try {
                this.prepararAcciones(sessionBean.getUsuario());
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoInstrumentoLegal")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarInstrumentoLegal")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarInstrumentoLegal")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleInstrumentoLegal")) {
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

    public void setIdTipoInstrumentoLegal(Long idTipoInstrumentoLegal) {
        this.idTipoInstrumentoLegal = idTipoInstrumentoLegal;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFojas(int fojas) {
        this.fojas = fojas;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setExpediente(String expediente) {
        this.expediente = expediente;
    }

    public void setTipoInstrumento(String tipoInstrumento) {
        this.tipoInstrumento = tipoInstrumento;
    }

    public String getExpediente() {
        return expediente;
    }

    public String getTipoInstrumento() {
        return tipoInstrumento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getFojas() {
        return fojas;
    }

    public String getNumero() {
        return numero;
    }

    public Long getIdTipoInstrumentoLegal() {
        return idTipoInstrumentoLegal;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public InstrumentoLegal getInstAux() {
        return instAux;
    }

    public void setInstAux(InstrumentoLegal instAux) {
        this.instAux = instAux;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    @Override
    public List<InstrumentoLegal> getListElements() {

        WebManagedBean sessionBean = this.getSessionBean();
        Usuario usuarioAux = sessionBean.getUsuario();
        this.setList(this.instrumentoLegalFacade.findAll(desacentuar(this.getNombreBuscar()), usuarioAux.getId()));
        return (List<InstrumentoLegal>) this.getList();
    }

    public String getSistemaOperativo() {
        return SistemaOperativo;
    }

    @Override
    public void limpiar() {
        this.setNumero(null);
        this.setDescripcion(null);
        this.setFojas(0);
        this.setIdTipoInstrumentoLegal(null);
        this.setArchivo(null);
    }

    @Override
    public String crear() {
        try {
            WebManagedBean sessionBean = this.getSessionBean();
            Usuario usuarioAux = sessionBean.getUsuario();
            this.instrumentoLegalFacade.create(this.getNumero(),
                    this.getDescripcion(), this.getFojas(),
                    this.getIdTipoInstrumentoLegal(), this.getArchivo(), usuarioAux.getId());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorInstrumentoLegal");
            this.setMsgSuccessError("El instrumento legal ha sido generado con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorInstrumentoLegal");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try {
            WebManagedBean sessionBean = this.getSessionBean();
            Usuario usuarioAux = sessionBean.getUsuario();
            this.instrumentoLegalFacade.create(this.getNumero(),
                    this.getDescripcion(), this.getFojas(),
                    this.getIdTipoInstrumentoLegal(), this.getArchivo(), usuarioAux.getId());
            this.setResultado("nuevoInstrumentoLegal");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorInstrumentoLegal");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        this.setInstAux(this.instrumentoLegalFacade.find(idObjectAux));
        this.setId(this.getInstAux().getId());
        this.setNumero(this.getInstAux().getNumero());
        this.setDescripcion(this.getInstAux().getDescripcion());
        this.setFojas(this.getInstAux().getFojas());
        Expediente expedienteAux = this.expedienteFacade.findExpedienteByInstrumentoLegal(idObjectAux);
        if (expedienteAux != null) {
            this.setExpediente(expedienteAux.getCodOrganismo() + "-" + expedienteAux.getNumCorrelativo() + "/" + expedienteAux.getAnio());
        } else {
            this.setExpediente("NINGUNO");
        }
        this.setTipoInstrumento(this.getInstAux().getTipoInstrumentoLegal().getNombre());
        if (this.getInstAux().getArchivo() != null) {
            this.prepararAdjunto(this.getInstAux().getArchivo());
        }
    }

    public String verAgregado(Long id) {
        String agregado = "NINGUNO";
        Expediente expedienteAux = this.expedienteFacade.findExpedienteByInstrumentoLegal(id);
        if (expedienteAux != null) {
            agregado = expedienteAux.getNroExp();
        }
        return agregado;

    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
            Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
            this.instrumentoLegalFacade.remove(idObjectAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("instrumentoLegalConf");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorInstrumentoLegal");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        InstrumentoLegal objectAux = this.instrumentoLegalFacade.find(idObjectAux);
        this.setId(objectAux.getId());
        this.setNumero(objectAux.getNumero());
        this.setDescripcion(objectAux.getDescripcion());
        this.setFojas(objectAux.getFojas());
        this.setIdTipoInstrumentoLegal(objectAux.getTipoInstrumentoLegal().getId());
        this.setArchivo(objectAux.getArchivo());
    }

    @Override
    public String guardarEdicion() {
        try {
            this.instrumentoLegalFacade.edit(this.getId(), this.getNumero(),
                    this.getDescripcion(), this.getFojas(),
                    this.getIdTipoInstrumentoLegal(), this.getArchivo());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorInstrumentoLegal");
            this.setMsgSuccessError("El instrumento legal ha sido editado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorInstrumentoLegal");
        }
        return this.getResultado();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<TipoInstrumentoLegal> lstTipoInstrumentoLegal = tipoInstrumentoLegalFacade.findAll(true);
        if (!(lstTipoInstrumentoLegal.isEmpty())) {
            for (TipoInstrumentoLegal tipoInstrumentoLegalAux : lstTipoInstrumentoLegal) {
                selectItems.add(new SelectItem(tipoInstrumentoLegalAux.getId(), tipoInstrumentoLegalAux.getNombre()));
            }
        }
        return selectItems;
    }

    public void cargarArchivo(org.primefaces.event.FileUploadEvent event) {
        org.primefaces.model.file.UploadedFile file = event.getFile();
        if (file == null) return;
        String tipo = file.getContentType();
        String[] tipos = new String[]{"application/pdf", "image/jpg", "image/jpeg", "image/bmp", "image/png"};
        boolean valido = false;
        for (String item : tipos) {
            if (item != null && item.equals(tipo)) { valido = true; break; }
        }
        if (!valido) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Solo se pueden adjuntar imágenes o archivos pdf.",
                    "Solo se pueden adjuntar imágenes o archivos pdf."));
            return;
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Date date = new Date();
            String fn = file.getFileName();
            int i = fn.lastIndexOf('.');
            int p = Math.max(fn.lastIndexOf('/'), fn.lastIndexOf('\\'));
            String extension = (i > p && i > 0) ? fn.substring(i + 1) : "";
            String nombreArchivo = dateFormat.format(date) + (extension.isEmpty() ? "" : "." + extension);
            File miArchivoAux = new File(this.getPathArchivo() + nombreArchivo);
            try (java.io.InputStream in = file.getInputStream(); java.io.FileOutputStream out = new java.io.FileOutputStream(miArchivoAux)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }
            this.setArchivo(miArchivoAux.getAbsolutePath());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Se adjuntó el archivo.", "Se adjuntó el archivo."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al adjuntar el archivo.", e.getMessage()));
        }
    }

    public void prepararAdjunto(String adjunto) { //devuelve el archivo adjunto segun el path que se le pase
        String resourcePath = adjunto;
        File file = new File(resourcePath);
        try {
            this.resource = new ArchivoResource(readIntoByteArray(new FileInputStream(file)));
            this.setNombreArchivo(file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            this.resource = new ArchivoResource(new byte[0]);
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

    public String getPathArchivo() {
        return "/home/DatosExpedientes/";
    }

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
