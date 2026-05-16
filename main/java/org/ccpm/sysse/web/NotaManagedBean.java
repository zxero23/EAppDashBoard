package org.ccpm.sysse.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.Resource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.model.SelectItem;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.facade.IniciadorFacadeLocal;
import org.ccpm.sysse.facade.LeyendaFacadeLocal;
import org.ccpm.sysse.facade.MesaFacadeLocal;
import org.ccpm.sysse.facade.NotaFacadeLocal;
import org.ccpm.sysse.facade.TipoAsuntoFacadeLocal;
import org.ccpm.sysse.facade.TipoIniciadorFacadeLocal;
import org.ccpm.sysse.facade.UsuarioExternoFacadeLocal;
import org.ccpm.sysse.facade.UsuarioFacadeLocal;
import org.ccpm.sysse.reporte.ExpedienteReport;
import org.ccpm.sysse.utilidad.ExpResource;
import org.ccpm.sysse.utilidad.ArchivoResource;
import org.ccpm.sysse.utilidad.EstadoEnum;
import org.primefaces.model.FilterMeta;
import org.ccpm.sysse.web.compat.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SortOrder;
import java.util.Map;

@Named
@SessionScoped
public class NotaManagedBean extends UtilManagedBean implements Serializable {

    @EJB
    private TipoIniciadorFacadeLocal tipoIniciadorFacade;
    @EJB
    private IniciadorFacadeLocal iniciadorFacade;
    @EJB
    private TipoAsuntoFacadeLocal tipoAsuntoFacade;
    @EJB
    private UsuarioExternoFacadeLocal usuarioExternoFacade;
    @EJB
    private MesaFacadeLocal mesaFacade;
    @EJB
    private NotaFacadeLocal notaFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private LeyendaFacadeLocal leyendaFacade;

    private boolean ingresar, editar, aceptar, rechazar, archivar, enviar, aceptarReenviar, ingresarEnviarNota = false;
    private boolean reenviar, desarchivar;
    private boolean visibleEditar, visibleAceptar, visibleRechazar;
    private boolean visibleArchivar, visibleEnviar, visibleReenviar, visibleDesarchivar, visibleAceptarReenviar;
    private boolean perteneceContaduria = false, perteneceContencion = false, perteneceLegal = false, perteneceSaludPublica = false;
    private boolean deshabilitarIniciador = true;
    private Long idTipoIniciador;
    private Long idIniciador;
    private Long idTipoAsunto;
    private Long idMesa;
    private Long idMesaOrigen;
    private Long idMesaDestino;
    private Long idUsuario;
    private String titulo;
    private String nroNota;
    private String referencia;
    private Integer fojas = 0;
    private String observacion;
    private String totalEnviado;
    private String totalRecibido;
    private String totalAceptado;
    private String totalRechazado;
    private String totalArchivado;
    private String total;
    private Date fechaCreacion;
    private List<ActionItem> lstActionItems = new ArrayList<>();
    List<SelectItem> selectItemsIniciador = new ArrayList<>();
    private LazyDataModel<Nota> notas;
    private List<Nota> notasSeleccionadas;
    private List<Nota> notasAux;
    // Set para mantener las notas seleccionadas (clave única: id)
    private Set<Long> notasSeleccionadasIds = new HashSet<>();
    private Usuario usuario;
    private Nota nota;
    //atributos para búsqueda//
    private boolean render = false;
    private Long idTipoAsuntoBsq;
    private Long idIniciadorBsq;
    private Long idTipoIniciadorBsq;
    private Long idNotaBsq;
    private String nroNotaBsq = "";
    private String referenciaBsq = "";
    private Long idMesaOrigenBsq;
    private Long idMesaDestinoBsq;
    private Date fechaDesdeBsq;
    private Date fechaHastaBsq;
    private String observacionBsq;
    private Date horaEnvio = quitarTiempo(new Date());
    private Date fechaDesde;
    private Date fechaHasta;
    private Date fechaEnvio;
    //flag
    private Integer flagBandeja;
    private String panelArchivar = "";
    private String panelDesarchivar = "";
    private String msgArchivar = "";
    private String msgDesarchivar = "";
    private String motivoArchivo = "";
    private List<Nota> notasEx;
    //adjuntos
    private String archivo = "";
    private String nombreArchivo = "";
    private Resource resource;
    private final String SistemaOperativo = System.getProperty("os.name");
    //Para successError
    private String etiqueta;
    private String nombreReporte;
    private String recurso;
    private boolean consultarHistorial;

    private Mesa mesaUsuario;//
    private Organismo organismoUsuario;

    public NotaManagedBean() {
        this.nroNota = "S/N";
        this.fechaCreacion = new Date();
        this.flagBandeja = 6;
        this.notas = null;
    }

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            this.setUsuario(sessionBean.getUsuario());
            this.setMesaUsuario(this.usuarioFacade.findMesaUsuario(this.getUsuario().getId()));
            this.setOrganismoUsuario(this.usuarioFacade.findOrganismoUsuario(this.getUsuario().getId()));
            try {
                this.prepararAcciones(this.getUsuario());
                this.prepararEtiquetas();
                // Inicializar la bandeja por defecto (Todos)
                if (this.getFlagBandeja() != null) {
                    this.update(this.getFlagBandeja());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void prepararAcciones(Usuario usuario) throws Exception {
        try {
            List<Grupo> listGrupo = usuario.getGrupos();
            if (!(listGrupo.isEmpty())) {
                listGrupo.stream().forEach((grupoAux) -> {
                    this.lstActionItems.addAll(grupoAux.getAcciones());
                });
            }
            this.lstActionItems.addAll(usuario.getAcciones());
            if (!(lstActionItems.isEmpty())) {
                for (ActionItem accionAux : lstActionItems) {

                    switch (accionAux.getNombre()) {
                        case "nuevaNota":
                            this.setAlta(true);
                            break;
                        case "editarNota":
                            this.setModificacion(true);
                            this.setEditar(true);
                            break;
                        case "borrarNota":
                            this.setBaja(true);
                            break;
                        case "detalleNota":
                            this.setDetalle(true);
                            break;
                        case "obtenerNota":
                            this.setIngresar(true);
                            break;
                        case "aceptarNota":
                            this.setAceptar(true);
                            break;
                        case "aceptarReenviarNota":
                            this.setAceptarReenviar(true);
                            break;
                        case "rechazarNota":
                            this.setRechazar(true);
                            break;
                        case "archivarNota":
                            this.setArchivar(true);
                            break;
                        case "enviarNota":
                            this.setEnviar(true);
                            break;
                        case "reenviarNota":
                            this.setReenviar(true);
                            break;
                        case "desarchivarNota":
                            this.setDesarchivar(true);
                            break;
                        case "perteneceContaduria":
                            this.setPerteneceContaduria(true);
                            break;
                        case "perteneceContencion":
                            this.setPerteneceContencion(true);
                            this.setPerteneceLegal(true);
                            break;
                        case "perteneceSaludPublica":
                            this.setPerteneceSaludPublica(true);
                            quitarTiempo(this.getHoraEnvio());
                            break;
                        case "ingresarEnviar":
                            this.setIngresarEnviarNota(true);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public boolean isIngresar() {
        return ingresar;
    }

    public void setIngresar(boolean ingresar) {
        this.ingresar = ingresar;
    }

    public boolean isEditar() {
        return editar;
    }

    public void setEditar(boolean editar) {
        this.editar = editar;
    }

    public boolean isAceptar() {
        return aceptar;
    }

    public void setAceptar(boolean aceptar) {
        this.aceptar = aceptar;
    }

    public boolean isRechazar() {
        return rechazar;
    }

    public void setRechazar(boolean rechazar) {
        this.rechazar = rechazar;
    }

    public boolean isArchivar() {
        return archivar;
    }

    public void setArchivar(boolean archivar) {
        this.archivar = archivar;
    }

    public boolean isEnviar() {
        return enviar;
    }

    public void setEnviar(boolean enviar) {
        this.enviar = enviar;
    }

    public boolean isReenviar() {
        return reenviar;
    }

    public void setReenviar(boolean reenviar) {
        this.reenviar = reenviar;
    }

    public boolean isDesarchivar() {
        return desarchivar;
    }

    public void setDesarchivar(boolean desarchivar) {
        this.desarchivar = desarchivar;
    }

    public boolean isVisibleEditar() {
        return visibleEditar;
    }

    public void setVisibleEditar(boolean visibleEditar) {
        this.visibleEditar = visibleEditar;
    }

    public boolean isVisibleAceptar() {
        return visibleAceptar;
    }

    public void setVisibleAceptar(boolean visibleAceptar) {
        this.visibleAceptar = visibleAceptar;
    }

    public boolean isVisibleRechazar() {
        return visibleRechazar;
    }

    public void setVisibleRechazar(boolean visibleRechazar) {
        this.visibleRechazar = visibleRechazar;
    }

    public boolean isVisibleArchivar() {
        return visibleArchivar;
    }

    public void setVisibleArchivar(boolean visibleArchivar) {
        this.visibleArchivar = visibleArchivar;
    }

    public boolean isVisibleEnviar() {
        return visibleEnviar;
    }

    public void setVisibleEnviar(boolean visibleEnviar) {
        this.visibleEnviar = visibleEnviar;
    }

    public boolean isVisibleReenviar() {
        return visibleReenviar;
    }

    public void setVisibleReenviar(boolean visibleReenviar) {
        this.visibleReenviar = visibleReenviar;
    }

    public boolean isVisibleDesarchivar() {
        return visibleDesarchivar;
    }

    public void setVisibleDesarchivar(boolean visibleDesarchivar) {
        this.visibleDesarchivar = visibleDesarchivar;
    }

    public boolean isPerteneceContaduria() {
        return perteneceContaduria;
    }

    public void setPerteneceContaduria(boolean perteneceContaduria) {
        this.perteneceContaduria = perteneceContaduria;
    }

    public boolean isPerteneceContencion() {
        return perteneceContencion;
    }

    public void setPerteneceContencion(boolean perteneceContencion) {
        this.perteneceContencion = perteneceContencion;
    }

    public boolean isPerteneceLegal() {
        return perteneceLegal;
    }

    public void setPerteneceLegal(boolean perteneceLegal) {
        this.perteneceLegal = perteneceLegal;
    }

    public boolean isPerteneceSaludPublica() {
        return perteneceSaludPublica;
    }

    public void setPerteneceSaludPublica(boolean perteneceSaludPublica) {
        this.perteneceSaludPublica = perteneceSaludPublica;
    }

    public boolean isDeshabilitarIniciador() {
        return deshabilitarIniciador;
    }

    public void setDeshabilitarIniciador(boolean deshabilitarIniciador) {
        this.deshabilitarIniciador = deshabilitarIniciador;
    }

    public Long getIdTipoIniciador() {
        return idTipoIniciador;
    }

    public void setIdTipoIniciador(Long idTipoIniciador) {
        this.idTipoIniciador = idTipoIniciador;
    }

    public Long getIdIniciador() {
        return idIniciador;
    }

    public void setIdIniciador(Long idIniciador) {
        this.idIniciador = idIniciador;
    }

    public Long getIdTipoAsunto() {
        return idTipoAsunto;
    }

    public void setIdTipoAsunto(Long idTipoAsunto) {
        this.idTipoAsunto = idTipoAsunto;
    }

    public Long getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(Long idMesa) {
        this.idMesa = idMesa;
    }

    public Long getIdMesaOrigen() {
        return idMesaOrigen;
    }

    public void setIdMesaOrigen(Long idMesaOrigen) {
        this.idMesaOrigen = idMesaOrigen;
    }

    public Long getIdMesaDestino() {
        return idMesaDestino;
    }

    public void setIdMesaDestino(Long idMesaDestino) {
        this.idMesaDestino = idMesaDestino;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNroNota() {
        return nroNota;
    }

    public void setNroNota(String nroNota) {
        this.nroNota = nroNota;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Integer getFojas() {
        return fojas;
    }

    public void setFojas(Integer fojas) {
        this.fojas = fojas;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getTotalEnviado() {
        return totalEnviado;
    }

    public void setTotalEnviado(String totalEnviado) {
        this.totalEnviado = totalEnviado;
    }

    public String getTotalRecibido() {
        return totalRecibido;
    }

    public void setTotalRecibido(String totalRecibido) {
        this.totalRecibido = totalRecibido;
    }

    public String getTotalAceptado() {
        return totalAceptado;
    }

    public void setTotalAceptado(String totalAceptado) {
        this.totalAceptado = totalAceptado;
    }

    public String getTotalRechazado() {
        return totalRechazado;
    }

    public void setTotalRechazado(String totalRechazado) {
        this.totalRechazado = totalRechazado;
    }

    public String getTotalArchivado() {
        return totalArchivado;
    }

    public void setTotalArchivado(String totalArchivado) {
        this.totalArchivado = totalArchivado;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<SelectItem> getSelectItemsIniciador() {
        return selectItemsIniciador;
    }

    public void setSelectItemsIniciador(List<SelectItem> selectItemsIniciador) {
        this.selectItemsIniciador = selectItemsIniciador;
    }

    public LazyDataModel<Nota> getNotas() {
        return notas;
    }

    public void setNotas(LazyDataModel<Nota> notas) {
        this.notas = notas;
    }
    
    // Método de compatibilidad para mantener código existente que espera List
    public void setNotasList(List<Nota> notasList) {
        if (notasList != null) {
            this.notas = new LazyDataModel<Nota>() {
                @Override
                public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                    int end = Math.min(first + pageSize, notasList.size());
                    if (first >= notasList.size()) {
                        return new ArrayList<>();
                    }
                    return notasList.subList(first, end);
                }
            };
            this.notas.setRowCount(notasList.size());
        } else {
            this.notas = null;
        }
    }
    
    // Método helper para obtener todas las notas del LazyDataModel como List
    // Úsalo solo cuando necesites iterar sobre todas las notas
    private List<Nota> getNotasAsList() {
        LazyDataModel<Nota> lazyModel = this.getNotas();
        if (lazyModel == null) {
            return new ArrayList<>();
        }
        int total = lazyModel.getRowCount();
        if (total <= 0) {
            return new ArrayList<>();
        }
        // Cargar todas las notas (puede ser costoso si hay muchos)
        List<Nota> lista = lazyModel.load(0, total, null, SortOrder.UNSORTED, null);
        return lista;
    }

    public List<Nota> getNotasAux() {
        return notasAux;
    }

    public void setNotasAux(List<Nota> notasAux) {
        this.notasAux = notasAux;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Nota getNota() {
        return nota;
    }

    public void setNota(Nota nota) {
        this.nota = nota;
    }

    public boolean isRender() {
        return render;
    }

    public void setRender(boolean render) {
        this.render = render;
    }

    public Long getIdTipoAsuntoBsq() {
        return idTipoAsuntoBsq;
    }

    public void setIdTipoAsuntoBsq(Long idTipoAsuntoBsq) {
        this.idTipoAsuntoBsq = idTipoAsuntoBsq;
    }

    public Long getIdIniciadorBsq() {
        return idIniciadorBsq;
    }

    public void setIdIniciadorBsq(Long idIniciadorBsq) {
        this.idIniciadorBsq = idIniciadorBsq;
    }

    public Long getIdTipoIniciadorBsq() {
        return idTipoIniciadorBsq;
    }

    public void setIdTipoIniciadorBsq(Long idTipoIniciadorBsq) {
        this.idTipoIniciadorBsq = idTipoIniciadorBsq;
    }

    public Long getIdNotaBsq() {
        return idNotaBsq;
    }

    public void setIdNotaBsq(Long idNotaBsq) {
        this.idNotaBsq = idNotaBsq;
    }

    public String getNroNotaBsq() {
        return nroNotaBsq;
    }

    public void setNroNotaBsq(String nroNotaBsq) {
        this.nroNotaBsq = nroNotaBsq;
    }

    public String getReferenciaBsq() {
        return referenciaBsq;
    }

    public void setReferenciaBsq(String referenciaBsq) {
        this.referenciaBsq = referenciaBsq;
    }

    public Long getIdMesaOrigenBsq() {
        return idMesaOrigenBsq;
    }

    public void setIdMesaOrigenBsq(Long idMesaOrigenBsq) {
        this.idMesaOrigenBsq = idMesaOrigenBsq;
    }

    public Long getIdMesaDestinoBsq() {
        return idMesaDestinoBsq;
    }

    public void setIdMesaDestinoBsq(Long idMesaDestinoBsq) {
        this.idMesaDestinoBsq = idMesaDestinoBsq;
    }

    public Date getFechaDesdeBsq() {
        return fechaDesdeBsq;
    }

    public void setFechaDesdeBsq(Date fechaDesdeBsq) {
        this.fechaDesdeBsq = fechaDesdeBsq;
    }

    public Date getFechaHastaBsq() {
        return fechaHastaBsq;
    }

    public void setFechaHastaBsq(Date fechaHastaBsq) {
        this.fechaHastaBsq = fechaHastaBsq;
    }

    public Date getHoraEnvio() {
        return horaEnvio;
    }

    public void setHoraEnvio(Date horaEnvio) {
        this.horaEnvio = horaEnvio;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public Date getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Date fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Integer getFlagBandeja() {
        return flagBandeja;
    }

    public void setFlagBandeja(Integer flagBandeja) {
        this.flagBandeja = flagBandeja;
    }

    public String getPanelArchivar() {
        return panelArchivar;
    }

    public void setPanelArchivar(String panelArchivar) {
        this.panelArchivar = panelArchivar;
    }

    public String getPanelDesarchivar() {
        return panelDesarchivar;
    }

    public void setPanelDesarchivar(String panelDesarchivar) {
        this.panelDesarchivar = panelDesarchivar;
    }

    public String getMsgArchivar() {
        return msgArchivar;
    }

    public void setMsgArchivar(String msgArchivar) {
        this.msgArchivar = msgArchivar;
    }

    public String getMsgDesarchivar() {
        return msgDesarchivar;
    }

    public void setMsgDesarchivar(String msgDesarchivar) {
        this.msgDesarchivar = msgDesarchivar;
    }

    public String getMotivoArchivo() {
        return motivoArchivo;
    }

    public void setMotivoArchivo(String motivoArchivo) {
        this.motivoArchivo = motivoArchivo;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public boolean isEsAdjuntoImagen() {
        if (nombreArchivo == null || nombreArchivo.isEmpty()) return false;
        String n = nombreArchivo.toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".gif") || n.endsWith(".webp");
    }

    /**
     * StreamedContent del adjunto de la nota para p:fileDownload (descarga).
     */
    public StreamedContent getAdjuntoStreamedContent() {
        if (resource == null || nombreArchivo == null || nombreArchivo.isEmpty()) {
            return null;
        }
        try {
            final Resource res = resource;
            final String name = nombreArchivo;
            String contentType = "application/octet-stream";
            if (name.toLowerCase().endsWith(".pdf")) contentType = "application/pdf";
            else if (name.toLowerCase().matches(".*\\.(jpg|jpeg)$")) contentType = "image/jpeg";
            else if (name.toLowerCase().endsWith(".png")) contentType = "image/png";
            else if (name.toLowerCase().endsWith(".gif")) contentType = "image/gif";
            final String ct = contentType;
            return DefaultStreamedContent.builder()
                    .stream(() -> {
                        try {
                            return res.getInputStream();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .contentType(ct)
                    .name(name)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getNombreReporte() {
        return nombreReporte;
    }

    public void setNombreReporte(String nombreReporte) {
        this.nombreReporte = nombreReporte;
    }

    public String getRecurso() {
        return recurso;
    }

    public void setRecurso(String recurso) {
        this.recurso = recurso;
    }

    /**
     * Devuelve el reporte actual como StreamedContent para p:fileDownload (PrimeFaces 8).
     */
    public StreamedContent getReporteStreamedContent() {
        if (recurso == null || nombreReporte == null) {
            return null;
        }
        try {
            Resource r = null;
            if ("listadoReciente".equals(recurso)) {
                r = getListadoReciente();
            } else if ("reporteEnviado".equals(recurso)) {
                r = getReporteEnviado();
            } else {
                return null;
            }
            if (r == null) {
                return null;
            }
            final Resource res = r;
            return DefaultStreamedContent.builder()
                    .stream(() -> {
                        try {
                            return res.getInputStream();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .contentType("application/pdf")
                    .name(nombreReporte)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    public Mesa getMesaUsuario() {
        return mesaUsuario;
    }

    public void setMesaUsuario(Mesa mesaUsuario) {
        this.mesaUsuario = mesaUsuario;
    }

    public Organismo getOrganismoUsuario() {
        return organismoUsuario;
    }

    public void setOrganismoUsuario(Organismo organismoUsuario) {
        this.organismoUsuario = organismoUsuario;
    }

    public boolean isAceptarReenviar() {
        return aceptarReenviar;
    }

    public void setAceptarReenviar(boolean aceptarReenviar) {
        this.aceptarReenviar = aceptarReenviar;
    }

    public boolean isVisibleAceptarReenviar() {
        return visibleAceptarReenviar;
    }

    public void setVisibleAceptarReenviar(boolean visibleAceptarReenviar) {
        this.visibleAceptarReenviar = visibleAceptarReenviar;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public boolean isConsultarHistorial() {
        return consultarHistorial;
    }

    public void setConsultarHistorial(boolean consultarHistorial) {
        this.consultarHistorial = consultarHistorial;
    }

    public String getObservacionBsq() {
        return observacionBsq;
    }

    public void setObservacionBsq(String observacionBsq) {
        this.observacionBsq = observacionBsq;
    }

    public List<Nota> getNotasSeleccionadas() {
        return notasSeleccionadas;
    }

    public void setNotasSeleccionadas(List<Nota> notasSeleccionadas) {
        this.notasSeleccionadas = notasSeleccionadas;
    }

    public boolean isIngresarEnviarNota() {
        return ingresarEnviarNota;
    }

    public void setIngresarEnviarNota(boolean ingresarEnviarNota) {
        this.ingresarEnviarNota = ingresarEnviarNota;
    }

    public List<Nota> getNotasEx() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean == null || sessionBean.getUsuario() == null) {
            return new ArrayList<>();
        }
        UsuarioExterno usuarioExAux = this.usuarioExternoFacade.findByUsuario(sessionBean.getUsuario().getId());
        if (usuarioExAux == null || usuarioExAux.getId() == null) {
            return new ArrayList<>();
        }
        String referencia = this.getReferenciaBsq() != null ? desacentuar(this.getReferenciaBsq().toUpperCase()) : "";
        return this.notaFacade.findAll(this.getIdNotaBsq(),
                this.getNroNotaBsq(), referencia,
                this.getFechaDesdeBsq(), this.getFechaHastaBsq(),
                this.getIdTipoAsuntoBsq(), usuarioExAux.getId(), null, null, false, "");
    }

    public void setNotasEx(List<Nota> notasEx) {
        this.notasEx = notasEx;
    }

    public void update(int idBandeja) {
        // Validar que mesaUsuario no sea null antes de continuar
        if (this.getMesaUsuario() == null || this.getMesaUsuario().getId() == null) {
            // Inicializar una lista vacía para evitar NPE
            LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                @Override
                public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                    return new ArrayList<>();
                }
            };
            lstNotas.setRowCount(0);
            this.setNotas(lstNotas);
            return;
        }
        
        // Limpiar selecciones al cambiar de bandeja
        notasSeleccionadasIds.clear();
        
        final NotaFacadeLocal facade = this.notaFacade;
        final Long idMesa = this.getMesaUsuario().getId();
        final Set<Long> notasSeleccionadas = this.notasSeleccionadasIds;
        
        switch (idBandeja) {
            case 1: {
                //Bandeja Enviado
                this.setTitulo("Enviadas");
                this.setFlagBandeja(1);
                this.setPanelArchivar("");
                this.setPanelDesarchivar("");
                this.setMsgArchivar("");
                this.setMsgDesarchivar("");
                Long totalNotas = facade.countEnvidas(idMesa, 2L);
                if (totalNotas == null) {
                    totalNotas = 0L;
                }
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = facade.findAllEnviadas(idMesa, first, pageSize);
                            if (lista != null) {
                                // Sincronizar el estado checked con el Set de seleccionados
                                for (Nota nota : lista) {
                                    nota.setChecked(notasSeleccionadas.contains(nota.getId()));
                                }
                            }
                            return lista != null ? lista : new ArrayList<>();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                if (this.isEditar()) {
                    this.setVisibleEditar(true);
                    if (this.isPerteneceContencion()) {
                        this.setVisibleAceptar(true);
                    } else {
                        this.setVisibleAceptar(false);
                    }
                    this.setVisibleRechazar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleDesarchivar(false);
                }
                this.setVisibleAceptarReenviar(false);
                break;
            }
            case 2: {
                //Bandeja Recibidas
                this.setTitulo("Recibidas");
                this.setFlagBandeja(2);
                this.setPanelArchivar("");
                this.setPanelDesarchivar("");
                this.setMsgArchivar("");
                this.setMsgDesarchivar("");
                Long totalNotas = facade.countRecibidas(idMesa, 2L);
                if (totalNotas == null) {
                    totalNotas = 0L;
                }
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = facade.findAllRecibidas(idMesa, first, pageSize);
                            if (lista != null) {
                                // Sincronizar el estado checked con el Set de seleccionados
                                for (Nota nota : lista) {
                                    nota.setChecked(notasSeleccionadas.contains(nota.getId()));
                                }
                            }
                            return lista != null ? lista : new ArrayList<>();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                if (this.isAceptar()) {
                    this.setVisibleAceptar(true);
                    this.setVisibleEditar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleDesarchivar(false);
                }
                if (this.isRechazar()) {
                    this.setVisibleRechazar(true);
                    this.setVisibleEditar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleDesarchivar(false);
                }
                if (this.isAceptarReenviar()) {
                    this.setVisibleAceptarReenviar(true);
                }
                break;
            }
            case 3: {
                //Bandeja Aceptadas
                this.setTitulo("Aceptadas");
                this.setFlagBandeja(3);
                this.setPanelArchivar("");
                this.setPanelDesarchivar("");
                this.setMsgArchivar("");
                this.setMsgDesarchivar("");
                Long totalNotas = facade.countAceptadas(idMesa, 3L);
                if (totalNotas == null) {
                    totalNotas = 0L;
                }
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = facade.findAllAceptadas(idMesa, first, pageSize);
                            if (lista != null) {
                                // Sincronizar el estado checked con el Set de seleccionados
                                for (Nota nota : lista) {
                                    nota.setChecked(notasSeleccionadas.contains(nota.getId()));
                                }
                            }
                            return lista != null ? lista : new ArrayList<>();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                if (this.isArchivar()) {
                    this.setVisibleArchivar(true);
                    this.setVisibleEditar(false);
                    this.setVisibleRechazar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleAceptar(false);
                }
                if (this.isEnviar()) {
                    this.setVisibleEnviar(true);
                    this.setVisibleEditar(false);
                    this.setVisibleRechazar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleAceptar(false);
                }
                if (this.isEditar()) {
                    this.setVisibleEditar(true);
                }
                this.setVisibleAceptarReenviar(false);
                break;
            }
            case 4: {
                //Bandeja Rechazadas
                this.setTitulo("Rechazadas");
                this.setFlagBandeja(4);
                this.setPanelArchivar("");
                this.setPanelDesarchivar("");
                this.setMsgArchivar("");
                this.setMsgDesarchivar("");
                Long totalNotas = facade.countRechazada(idMesa, 4L);
                if (totalNotas == null) {
                    totalNotas = 0L;
                }
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = facade.findAllRechazadas(idMesa, first, pageSize);
                            if (lista != null) {
                                // Sincronizar el estado checked con el Set de seleccionados
                                for (Nota nota : lista) {
                                    nota.setChecked(notasSeleccionadas.contains(nota.getId()));
                                }
                            }
                            return lista != null ? lista : new ArrayList<>();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                if (this.isReenviar()) {
                    this.setVisibleReenviar(true);
                    this.setVisibleEditar(false);
                    this.setVisibleAceptar(false);
                    this.setVisibleRechazar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleDesarchivar(false);
                }
                this.setVisibleAceptarReenviar(false);
                break;
            }
            case 5: {
                //Bandeja Archivadas
                this.setTitulo("Archivadas");
                this.setFlagBandeja(5);
                this.setPanelArchivar("");
                this.setPanelDesarchivar("");
                this.setMsgArchivar("");
                this.setMsgDesarchivar("");
                Long totalNotas = facade.countArchivada(idMesa, 5L);
                if (totalNotas == null) {
                    totalNotas = 0L;
                }
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = facade.findAllArchivadas(idMesa, first, pageSize);
                            if (lista != null) {
                                // Sincronizar el estado checked con el Set de seleccionados
                                for (Nota nota : lista) {
                                    nota.setChecked(notasSeleccionadas.contains(nota.getId()));
                                }
                            }
                            return lista != null ? lista : new ArrayList<>();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                if (this.isDesarchivar()) {
                    this.setVisibleDesarchivar(true);
                    this.setVisibleEditar(false);
                    this.setVisibleAceptar(false);
                    this.setVisibleRechazar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleReenviar(false);
                }
                this.setVisibleAceptarReenviar(false);
                break;
            }
            case 6: {
                //Bandeja Todos
                this.setFlagBandeja(6);
                this.setPanelArchivar("");
                this.setPanelDesarchivar("");
                this.setMsgArchivar("");
                this.setMsgDesarchivar("");
                Long totalNotas = facade.countNotas(idMesa);
                if (totalNotas == null) {
                    totalNotas = 0L;
                }
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = facade.findAll(idMesa, first, pageSize);
                            if (lista != null) {
                                // Sincronizar el estado checked con el Set de seleccionados
                                for (Nota nota : lista) {
                                    nota.setChecked(notasSeleccionadas.contains(nota.getId()));
                                }
                            }
                            return lista != null ? lista : new ArrayList<>();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                this.setTitulo(null);
                this.setVisibleEditar(false);
                this.setVisibleAceptar(false);
                this.setVisibleRechazar(false);
                this.setVisibleArchivar(false);
                this.setVisibleEnviar(false);
                this.setVisibleReenviar(false);
                this.setVisibleDesarchivar(false);
                this.setPanelArchivar("");
                this.setPanelDesarchivar("");
                this.setVisibleAceptarReenviar(false);
                break;
            }
        }
    }

    public List<SelectItem> getSelectItemsTipoAsunto() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<TipoAsunto> lstTipoAsunto = this.tipoAsuntoFacade.findAll(true);
        if (!(lstTipoAsunto.isEmpty())) {
            lstTipoAsunto.stream().forEach((tipoAsuntoAux) -> {
                selectItems.add(new SelectItem(tipoAsuntoAux.getId(), tipoAsuntoAux.getNombre()));
            });
        }
        return selectItems;
    }

    public List<SelectItem> getSelectItemsDestino() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Mesa> lstMesa = null;
        if (this.isPerteneceContencion() || this.getMesaUsuario().isMesaGeneral()) {
            // Contención y las Mesas de Entradas de los  Organismos solo
            // pueden ver las mesas de entradas de otros organismos.
            if (this.getOrganismoUsuario().getCodOrganigrama().intValue() == 9910) {
                lstMesa = this.mesaFacade.findAllGeneral(this.getOrganismoUsuario().getId(), this.getOrganismoUsuario().getOrganismoPadre().getId());
            } else {
                lstMesa = this.mesaFacade.findAllGeneral(this.getOrganismoUsuario().getId(), 0L);
            }
        } else {
            switch (this.getOrganismoUsuario().getCodOrganigrama().intValue()) {
                case 9900: //Ministerio de Ecología ve la Subsecretaría de Ecología
                    lstMesa = this.mesaFacade.findAll("", this.getOrganismoUsuario().getId(), null);
                    List<Mesa> lstMesaSubEco = this.mesaFacade.findAll("", 69L, null);//69 id Subse de Ecología
                    for (Mesa mesaAux : lstMesaSubEco) {
                        lstMesa.add(mesaAux);
                    }
                    break;
                case 9910: //Subsecretaría de Ecología ve la Ministerio de Ecología
                    lstMesa = this.mesaFacade.findAll("", this.getOrganismoUsuario().getId(), null);
                    List<Mesa> lstMesaMiniEco = this.mesaFacade.findAll("", 80L, null); //80 id Mini de Eco
                    for (Mesa mesaAux : lstMesaMiniEco) {
                        lstMesa.add(mesaAux);
                    }
                    break;
                case 3000: //Ministerio de Hacienda
                    lstMesa = this.mesaFacade.findAll("", this.getOrganismoUsuario().getId(), null);
                    List<Mesa> lstMesaHacienda = this.mesaFacade.findAll("", 1L, null); //1 id Hacienda
                    for (Mesa mesaAux : lstMesaHacienda) {
                        lstMesa.add(mesaAux);
                    }
                    break;
                case 3690: //UEP FIDEPROC
                    lstMesa = this.mesaFacade.findAll("", this.getOrganismoUsuario().getId(), null);
                    List<Mesa> lstMesaUEP = this.mesaFacade.findAll("", 1L, null); //1 id Hacienda
                    for (Mesa mesaAux : lstMesaUEP) {
                        lstMesa.add(mesaAux);
                    }
                    break;
                default: // El resto de las mesas internas solo ven las mesas de su organismo
                    lstMesa = this.mesaFacade.findAll("", this.getOrganismoUsuario().getId(), null);
                    break;
            }
        }
        if (!(lstMesa.isEmpty())) {
            for (Mesa mesaAux : lstMesa) {
                selectItems.add(new SelectItem(mesaAux.getId(), mesaAux.getNombre()));
            }
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        this.setNroNota("S/N");
        this.setIdUsuario(null);
        this.setFechaCreacion(new Date());
        this.setFechaEnvio(new Date());
        this.setHoraEnvio(quitarTiempo(this.getHoraEnvio()));
        this.setReferencia(null);
        this.setFojas(0);
        this.setIdTipoIniciador(null);
        this.setIdIniciador(null);
        this.setIdTipoAsunto(null);
        this.setObservacion(null);
        this.setIdMesa(null);
        this.setId(null);
        this.setNotas(null);
        this.prepararEtiquetas();
        this.update(this.getFlagBandeja());
        this.setDeshabilitarIniciador(true);
        this.setArchivo(null);
        this.setRecurso(null);
        this.setEtiqueta(null);
        this.setNombreReporte(null);
        this.setNotasAux(null);
        this.setConsultarHistorial(false);
    }

    public void clean() {
        this.setIdIniciador(null);
        this.setIdTipoAsunto(null);
        this.setIdMesa(null);
        this.setTitulo(null);
        this.setNroNota(null);
        this.setReferencia(null);
        this.setObservacion(null);
        this.setTotalEnviado(null);
        this.setTotalRecibido(null);
        this.setTotalAceptado(null);
        this.setTotalRechazado(null);
        this.setTotalArchivado(null);
        this.setTotal(null);
        this.setFechaCreacion(null);
        lstActionItems.clear();
        this.setNotas(null);
        this.setUsuario(null);
        this.setNota(null);
        this.setIdTipoAsuntoBsq(null);
        this.setIdIniciadorBsq(null);
        this.setIdNotaBsq(null);
        this.setNroNotaBsq(null);
        this.setReferenciaBsq(null);
        this.setFechaDesdeBsq(null);
        this.setFechaHastaBsq(null);
        this.setFlagBandeja(null);
    }

    @Override
    public String crear() {
        try {
            asegurarArchivoDesdeRequest();
            Date fechaActual = new Date();
            if (this.getFechaCreacion().before(fechaActual)) {
                if (this.getIdIniciador() != 0) {
                    Date fecha = this.getFechaCreacion();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(fecha);
                    this.getFechaCreacion().setHours(cal.get(Calendar.HOUR));
                    this.getFechaCreacion().setMinutes(cal.get(Calendar.MINUTE));
                    this.getFechaCreacion().setSeconds(cal.get(Calendar.SECOND));

                    String archivoParaGuardar = this.getArchivo();
                    if (archivoParaGuardar == null) {
                        archivoParaGuardar = "";
                    } else {
                        archivoParaGuardar = archivoParaGuardar.trim();
                        if (!archivoParaGuardar.isEmpty()) {
                            int lastSep = Math.max(archivoParaGuardar.lastIndexOf('/'), archivoParaGuardar.lastIndexOf('\\'));
                            if (lastSep >= 0 && lastSep < archivoParaGuardar.length() - 1) {
                                archivoParaGuardar = archivoParaGuardar.substring(lastSep + 1);
                            }
                        }
                    }

                    Nota notaAux = this.notaFacade.create(this.getNroNota(), this.getFechaCreacion(),
                            this.getReferencia(), this.getFojas(), this.getIdIniciador(),
                            this.getIdTipoAsunto(), this.getObservacion(),
                            this.getIdMesa(), this.getMesaUsuario().getId(), archivoParaGuardar, this.getUsuario().getId());
                    this.setImages("images/symbolChecks.png");
                    this.setResultado("successErrorNota");
                    this.setMsgSuccessError("La nota " + notaAux.getId() + " ha sido generada con éxito.");
                    this.limpiar();
                    this.prepararEtiquetas();
                    this.actualizar();
                } else {
                    throw new Exception("Elija correctamente el iniciador");
                }

            } else {
                throw new Exception("La fecha ingresada no es correcta.");
            }
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try {
            asegurarArchivoDesdeRequest();
            if (this.getIdIniciador() != 0) {
                Date fecha = this.getFechaCreacion();
                Calendar cal = Calendar.getInstance();
                cal.setTime(fecha);
                this.getFechaCreacion().setHours(cal.get(Calendar.HOUR));
                this.getFechaCreacion().setMinutes(cal.get(Calendar.MINUTE));
                this.getFechaCreacion().setSeconds(cal.get(Calendar.SECOND));

                String archivoParaGuardar = this.getArchivo();
                if (archivoParaGuardar == null) archivoParaGuardar = "";
                else if (!archivoParaGuardar.trim().isEmpty()) {
                    int lastSep = Math.max(archivoParaGuardar.lastIndexOf('/'), archivoParaGuardar.lastIndexOf('\\'));
                    if (lastSep >= 0 && lastSep < archivoParaGuardar.length() - 1) archivoParaGuardar = archivoParaGuardar.substring(lastSep + 1);
                }
                this.notaFacade.create(this.getNroNota(), this.getFechaCreacion(),
                        this.getReferencia(), this.getFojas(), this.getIdIniciador(),
                        this.getIdTipoAsunto(), this.getObservacion(),
                        this.getIdMesa(), this.getMesaUsuario().getId(), archivoParaGuardar, this.getUsuario().getId());
                this.setResultado("nuevaNota");
                this.limpiar();
                this.prepararEtiquetas();
            } else {
                throw new Exception("Elija correctamente el iniciador");
            }

        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    @Override
    public String guardarEdicion() {
        try {
            if (this.getIdIniciador() != 0) {
                String archivoParaGuardar = this.getArchivo();
                if (archivoParaGuardar != null && !archivoParaGuardar.trim().isEmpty()) {
                    int lastSep = Math.max(archivoParaGuardar.lastIndexOf('/'), archivoParaGuardar.lastIndexOf('\\'));
                    if (lastSep >= 0 && lastSep < archivoParaGuardar.length() - 1) {
                        archivoParaGuardar = archivoParaGuardar.substring(lastSep + 1);
                    }
                } else {
                    archivoParaGuardar = null;
                }
                this.notaFacade.edit(this.getId(),
                        this.getNroNota(), this.getFechaCreacion(), this.getReferencia(),
                        this.getFojas(), this.getIdIniciador(), this.getIdTipoAsunto(),
                        this.getObservacion(), this.getIdMesa(), archivoParaGuardar);
            } else {
                throw new Exception("Elija correctamente el iniciador");
            }

            this.limpiar();
            this.prepararEtiquetas();
            this.actualizar();
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError("La nota ha sido editada con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String prepararParaArchivar() {
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount > 0) {
            if (flagCount == 1) {
                Long notaId = notasSeleccionadasIds.iterator().next();
                this.setResultado("archivarNota");
                Nota notaAux = this.notaFacade.find(notaId);

                Collections.reverse(notaAux.getListaHistorial());
                this.setNota(notaAux);

                this.setId(notaAux.getId());
                this.setNroNota(notaAux.getNroNota());
                this.setFechaCreacion(notaAux.getFechaCreacion());
                this.setReferencia(notaAux.getReferencia());
            } else {
                // Para múltiples notas, procesar directamente sin mostrar panel de confirmación individual
                int exitosos = 0;
                int fallidos = 0;
                String mensajeError = "";
                
                for (Long notaId : notasSeleccionadasIds) {
                    try {
                        // Obtener la nota para el motivo de archivo
                        Nota notaAux = this.notaFacade.find(notaId);
                        if (notaAux != null) {
                            // Usar motivo de archivo por defecto si no se especifica
                            String motivo = this.getMotivoArchivo() != null && !this.getMotivoArchivo().isEmpty() 
                                ? this.getMotivoArchivo() : "Archivado múltiple";
                            this.notaFacade.archivarNota(notaId, this.getObservacion(), motivo, this.getUsuario().getId());
                            exitosos++;
                        }
                    } catch (Exception ex) {
                        fallidos++;
                        if (mensajeError.isEmpty()) {
                            mensajeError = ex.getMessage();
                        }
                    }
                }
                
                // Limpiar selecciones después de procesar
                notasSeleccionadasIds.clear();
                
                this.prepararEtiquetas();
                this.update(3);
                
                if (fallidos == 0) {
                    this.setTitle("Proceso Completo");
                    this.setImages("images/symbolChecks.png");
                    this.setMsgSuccessError(exitosos + " notas han sido archivadas con éxito.");
                } else {
                    this.setTitle("Proceso Parcial");
                    this.setImages("images/symbolWarning.png");
                    this.setMsgSuccessError(exitosos + " notas procesadas correctamente. " + fallidos + " notas fallaron: " + mensajeError);
                }
                this.setResultado("successErrorNota");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar al menos una nota de la tabla para realizar esta acción.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String prepararParaEliminar() throws Exception {
        boolean flag = false;
        List<Nota> lstNotas = this.getNotasAsList();
        if (!lstNotas.isEmpty()) {
            for (Nota notaAux : lstNotas) {
                if (notaAux.isChecked()) {
                    flag = true;
                    this.notaFacade.remove(notaAux, this.getUsuario().getId());
                }
            }
        }
        if (flag) {
            this.limpiar();
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorExpedienteAvanzada");
            this.setMsgSuccessError("La nota ha sido eliminado con éxito.");
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar al menos un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpedienteAvanzada");
        }

        return this.getResultado();
    }

    public String prepararParaDetalle() {
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount > 0) {
            if (flagCount == 1) {
                Long notaId = notasSeleccionadasIds.iterator().next();
                this.setResultado("detalleNota");
                Nota notaAux = this.notaFacade.find(notaId);

                Collections.reverse(notaAux.getListaHistorial());
                this.setNota(notaAux);
                if (this.getNota().getArchivo() != null) {
                    this.prepararAdjunto(this.getNota().getArchivo());
                }
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorNota");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public void prepararEtiquetas() {
        if (this.getMesaUsuario() == null || this.getMesaUsuario().getId() == null) {
            this.setTotalEnviado("0");
            this.setTotalRecibido("0");
            this.setTotalAceptado("0");
            this.setTotalArchivado("0");
            this.setTotalRechazado("0");
            this.setTotal("0");
            return;
        }
        Long idMesa = this.getMesaUsuario().getId();
        //Total Bandeja Enviadas
        Long countEnviadas = this.notaFacade.countEnvidas(idMesa, 2L);
        this.setTotalEnviado(countEnviadas != null ? countEnviadas.toString() : "0");
        //Total Bandeja Recibidas
        Long countRecibidas = this.notaFacade.countRecibidas(idMesa, 2L);
        this.setTotalRecibido(countRecibidas != null ? countRecibidas.toString() : "0");
        //Total Bandeja Aceptadas
        Long countAceptadas = this.notaFacade.countAceptadas(idMesa, 3L);
        this.setTotalAceptado(countAceptadas != null ? countAceptadas.toString() : "0");
        //Total Bandeja Archivada
        Long countArchivada = this.notaFacade.countArchivada(idMesa, 5L);
        this.setTotalArchivado(countArchivada != null ? countArchivada.toString() : "0");
        //Total Bandeja Rechazada
        Long countRechazada = this.notaFacade.countRechazada(idMesa, 4L);
        this.setTotalRechazado(countRechazada != null ? countRechazada.toString() : "0");
        //Total Bandejas
        Long countNotas = this.notaFacade.countNotas(idMesa);
        this.setTotal(countNotas != null ? countNotas.toString() : "0");
    }

    public String prepararParEditar() {
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount > 0) {
            if ((this.getFlagBandeja().equals(1) || this.getFlagBandeja().equals(3)) && flagCount == 1) {
                Long notaId = notasSeleccionadasIds.iterator().next();
                Nota notaIte = this.notaFacade.find(notaId);
                this.setResultado("editarNota");
                this.setNota(notaIte);

                this.setId(notaIte.getId());
                this.setNroNota(notaIte.getNroNota());
                this.setFechaCreacion(notaIte.getFechaCreacion());
                this.setReferencia(notaIte.getReferencia());
                this.setFojas(notaIte.getFojas());
                this.setIdTipoIniciador(notaIte.getIniciador().getTipoIniciador().getId());
                this.cargarIniciadores(this.getIdTipoIniciador());
                this.setIdIniciador(notaIte.getIniciador().getId());
                this.setDeshabilitarIniciador(false);
                this.setIdTipoAsunto(notaIte.getTipoAsunto().getId());
                this.setObservacion(notaIte.getObservacion());
                this.setIdMesa(notaIte.getMesaDestino().getId());
                this.setArchivo(notaIte.getArchivo());

            } else if (this.getFlagBandeja().equals(1) && flagCount > 1) {
                List<Nota> notasMultiples = new ArrayList<>();
                for (Long notaId : notasSeleccionadasIds) {
                    notasMultiples.add(this.notaFacade.find(notaId));
                }
                this.setResultado("editarNotaMultiple");
                this.setNotasAux(notasMultiples);
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorNota");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String prepararParaEnviar() {
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount > 0) {
            // Mesa de Contabilidad por defecto tiene el destino la mesa de entradas de Contaduría General
            if (this.getMesaUsuario().getId() == 11189L) {
                this.setIdMesa(204L);
            } else {
                this.setIdMesa(null);
            }
            notasAux = new ArrayList<Nota>();
            Set<Long> clavesAgregadas = new HashSet<>(); // Para evitar duplicados
            
            for (Long notaId : notasSeleccionadasIds) {
                if (!clavesAgregadas.contains(notaId)) {
                    Nota notaAux = this.notaFacade.find(notaId);
                    if (notaAux != null) {
                        notasAux.add(notaAux);
                        clavesAgregadas.add(notaId);
                    }
                }
            }
            if (flagCount == 1) {
                Nota notaIte = notasAux.get(0);
                this.setResultado("enviarNota");
                this.setId(notaIte.getId());
                this.setNota(notaIte);
            } else {
                this.setResultado("enviarNotaMultiple");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar al menos una nota de la tabla para realizar esta acción.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String prepararParaRechazar() {
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount > 0) {
            if (flagCount == 1) {
                Long notaId = notasSeleccionadasIds.iterator().next();
                Nota notaIte = this.notaFacade.find(notaId);
                this.setResultado("rechazarNota");
                this.setId(notaIte.getId());
                this.setNota(notaIte);
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorNota");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String prepararParaReenviar() {
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount > 0) {
            if (flagCount == 1) {
                Long notaId = notasSeleccionadasIds.iterator().next();
                Nota notaIte = this.notaFacade.find(notaId);
                this.setResultado("reenviarNota");
                this.setId(notaIte.getId());
                this.setNota(notaIte);
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorNota");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public Nota buscar() {
        Nota aux = this.notaFacade.find(this.getId());
        return aux;
    }

    public String ingresarNotaPosta() throws Exception {
        try {
            this.notaFacade.ingresar(this.getId(),
                    this.getMesaUsuario().getId(),
                    this.getUsuario().getId());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError("Se ha ingresado la nota con id " + this.getId() + " \"" + this.buscar().getNroNota() + " - " + this.buscar().getReferencia() + "\".");
            this.limpiar();

        } catch (Exception e) {
            this.limpiar();
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError(e.getMessage());
            this.limpiar();
        }

        return this.getResultado();

    }

    public String ingresarNota() throws Exception {
        try {
            Nota notaAux = new Nota();
            notaAux = this.notaFacade.find(this.getId());
            if (!(notaAux == null)) {
                //La nota existe.

                //Verificar que no esté en la bandeja de recibidos del usuario.
                if (notaAux.getMesaDestino().getId() != this.getMesaUsuario().getId()) {
                    //La nota existe.
                    //NO está en alguna bandeja de la mesa del usuario.

                    //Verificar que esté en estado enviada o iniciada
                    if ((notaAux.getEstadoNota().getId() == 1L || notaAux.getEstadoNota().getId() == 2L)) {
                        //La nota existe
                        //NO está en alguna bandeja de la mesa del usuario.
                        //Está enviada o iniciada

                        //Se puede ingresar
                        this.setResultado("ingresarNotaPosta");
                        this.setMsgSuccessError("Está por ingresar la nota con id " + this.getId() + " \"" + this.buscar().getNroNota() + " - " + this.buscar().getReferencia() + "\" ¿Está seguro?");

                    } else {
                        //La nota existe
                        //NO está en alguna bandeja de la mesa del usuario.
                        //NO está enviada o iniciada.
                        throw new Exception("La nota con id " + this.getId() + " \"" + this.buscar().getNroNota() + " - " + this.buscar().getReferencia() + "\" no ha sido enviada.");
                    }
                } else {
                    //La nota existe.
                    //Está en alguna bandeja de la mesa del usuario.
                    if (notaAux.getMesaDestino().getId().equals(139L)) {
                        throw new Exception("Por solicitud del organismo receptor deberá comunicarse con el mismo para obtener la nota.");
                    }
                    String estado = "";
                    switch (notaAux.getEstadoNota().getId().intValue()) {
                        case 3: { //Aceptadas
                            estado = "Aceptadas";
                            break;
                        }
                        case 4: { //Rechazadas
                            estado = "Rechazadas";
                            break;
                        }
                        case 5: { //Archivadas
                            estado = "Archivadas";
                            break;
                        }

                    }
                    throw new Exception("La nota con id " + this.getId() + " \"" + this.buscar().getNroNota() + " - " + this.buscar().getReferencia() + "\" está en su bandeja " + estado + ".");
                }
            } else {
                //La nota no existe
                throw new Exception("No existe una nota con el id " + this.getId() + ".");
            }
        } catch (Exception e) {
            this.limpiar();
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError(e.getMessage());
        }

        return this.getResultado();
    }

    public String ingresarEnviarNotaPosta() throws Exception {
        try {
            for (Nota notaAux : this.getNotasSeleccionadas()) {
                this.notaFacade.ingresar(notaAux.getId(),
                        this.getMesaUsuario().getId(),
                        this.getUsuario().getId());
                this.notaFacade.enviarNota(notaAux.getId(), "", this.getIdMesa(), this.getUsuario().getId());
            }
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError("Se ha ingresado y enviado las notas elegidas.");
            this.limpiar();
        } catch (Exception e) {
            this.limpiar();
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError(e.getMessage());
        }

        return this.getResultado();
    }

    public void prepararParaIngresarEnviarNota() {
        try {
            this.setNotasSeleccionadas(new ArrayList<>());
            this.setIdMesa(null);
        } catch (Exception e) {
        }
    }

    public void validarId(FacesContext context, UIComponent validate, Object value) {
        try {
            Long idAux = (Long) value;
            Nota notaAux = this.notaFacade.find(idAux);
            if (!(EstadoEnum.ENVIADO.getId().equals(notaAux.getEstadoNota().getId()) || EstadoEnum.REENVIADO.getId().equals(notaAux.getEstadoNota().getId()))) {
                final FacesMessage msg = new FacesMessage("La nota no está en estado enviado.");
                context.addMessage(validate.getClientId(context), msg);
                throw new ValidatorException(msg);
            }
        } catch (Exception e) {
        }

    }

    public String getMesaDestinoStr() {
        try {
            Mesa mesaAux = this.mesaFacade.find(this.getIdMesa());
            return mesaAux.getNombre();
        } catch (Exception e) {
        }
        return "Error";
    }

    public void agregarNotas() {
        try {
            Nota notaAux = this.notaFacade.find(this.getId());
            if (!this.getNotasSeleccionadas().contains(notaAux) && (EstadoEnum.ENVIADO.getId().equals(notaAux.getEstadoNota().getId()) || EstadoEnum.REENVIADO.getId().equals(notaAux.getEstadoNota().getId()))) {
                this.notasSeleccionadas.add(notaAux);
            }
        } catch (Exception e) {
            System.out.println("Notas: " + e.getMessage());
        }
        this.setId(null);
    }

    public String aceptarNota() {
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount > 0) {
            int exitosos = 0;
            int fallidos = 0;
            String mensajeError = "";
            
            for (Long notaId : notasSeleccionadasIds) {
                try {
                    this.notaFacade.aceptarNota(notaId, this.getUsuario().getId(), this.getFlagBandeja());
                    exitosos++;
                } catch (Exception ex) {
                    fallidos++;
                    if (mensajeError.isEmpty()) {
                        mensajeError = ex.getMessage();
                    }
                }
            }
            
            // Limpiar selecciones después de procesar
            notasSeleccionadasIds.clear();
            
            this.prepararEtiquetas();
            this.update(this.getFlagBandeja());
            
            if (fallidos == 0) {
                this.setTitle("Proceso Completo");
                this.setImages("images/symbolChecks.png");
                if (exitosos == 1) {
                    this.setMsgSuccessError("La nota ha sido aceptada con éxito.");
                } else {
                    this.setMsgSuccessError(exitosos + " notas han sido aceptadas con éxito.");
                }
            } else {
                this.setTitle("Proceso Parcial");
                this.setImages("images/symbolWarning.png");
                this.setMsgSuccessError(exitosos + " notas procesadas correctamente. " + fallidos + " notas fallaron: " + mensajeError);
            }
            this.setResultado("successErrorNota");
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar al menos una nota de la tabla para realizar esta acción.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String archivarNota() {
        try {
            this.notaFacade.archivarNota(this.getId(), this.getObservacion(), this.getMotivoArchivo(), this.getUsuario().getId());
            this.limpiar();
            this.prepararEtiquetas();
            this.update(3);
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError("La nota ha sido archivada con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorNota");
        }

        return this.getResultado();
    }

    public void definirPanel(ValueChangeEvent event) {
        // Obtener la nota del evento
        Nota notaActual = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            Object itemObj = facesContext.getApplication().evaluateExpressionGet(
                facesContext, "#{item}", Object.class);
            if (itemObj instanceof Nota) {
                notaActual = (Nota) itemObj;
            }
        }
        
        if (notaActual != null) {
            Long notaId = notaActual.getId();
            Boolean nuevoValor = (Boolean) event.getNewValue();
            
            // Agregar o remover del Set según el nuevo valor
            if (Boolean.TRUE.equals(nuevoValor)) {
                // Permitir selección múltiple - NO limpiar el Set
                notasSeleccionadasIds.add(notaId);
                notaActual.setChecked(true);
            } else {
                notasSeleccionadasIds.remove(notaId);
                notaActual.setChecked(false);
            }
        }
        
        // Actualizar el contador basado en el Set
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount == 0) { //nada seleccionado
            this.setPanelArchivar("");
            this.setPanelDesarchivar("");
        } else if (flagCount == 1) { //uno solo seleccionado
            this.setPanelArchivar("archivarNotaConfirmPanel");
            this.setPanelDesarchivar("desarchivarNotaConfirmPanel");
            this.setMsgArchivar("¿Está seguro quiere archivar la nota seleccionada?");
            this.setMsgDesarchivar("¿Está seguro quiere desarchivar la nota seleccionada?");

        } else if (flagCount > 1) { //más de uno seleccionado
            this.setPanelArchivar("archivarNotaConfirmPanel");
            this.setPanelDesarchivar("desarchivarNotaConfirmPanel");
            this.setMsgArchivar("¿Está seguro quiere archivar las notas seleccionadas?");
            this.setMsgDesarchivar("¿Está seguro quiere desarchivar las notas seleccionadas?");
        }
    }

    public String desarchivarNota() {
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount > 0) {
            int exitosos = 0;
            int fallidos = 0;
            String mensajeError = "";
            
            for (Long notaId : notasSeleccionadasIds) {
                try {
                    this.notaFacade.aceptarNota(notaId, this.getUsuario().getId(), this.getFlagBandeja());
                    exitosos++;
                } catch (Exception ex) {
                    fallidos++;
                    if (mensajeError.isEmpty()) {
                        mensajeError = ex.getMessage();
                    }
                }
            }
            
            // Limpiar selecciones después de procesar
            notasSeleccionadasIds.clear();
            
            this.prepararEtiquetas();
            this.update(5);
            
            if (fallidos == 0) {
                this.setTitle("Proceso Completo");
                this.setImages("images/symbolChecks.png");
                if (exitosos == 1) {
                    this.setMsgSuccessError("La nota ha sido desarchivada con éxito.");
                } else {
                    this.setMsgSuccessError(exitosos + " notas han sido desarchivadas con éxito.");
                }
            } else {
                this.setTitle("Proceso Parcial");
                this.setImages("images/symbolWarning.png");
                this.setMsgSuccessError(exitosos + " notas procesadas correctamente. " + fallidos + " notas fallaron: " + mensajeError);
            }
            this.setResultado("successErrorNota");
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar al menos una nota de la tabla para realizar esta acción.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String enviar() {
        try {
            this.notaFacade.enviarNota(this.getId(), this.getObservacion(),
                    this.getMesaUsuario().getId(), this.getIdMesa(),
                    this.getUsuario().getId());
            this.limpiar();
            this.prepararEtiquetas();
            this.update(3);
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError("La nota ha sido enviada con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String enviarNotas() {
        this.setFechaEnvio(new Date());

        for (Nota notaAux : this.getNotasAux()) {
            try {
                this.notaFacade.enviarNota(notaAux.getId(), this.getObservacion(), this.getIdMesa(), this.getUsuario().getId());
            } catch (Exception ex) {

                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError(ex.getMessage());
                this.setResultado("successErrorNota");
            }

        }
        this.setTitle("Proceso Completo");
        this.setImages("images/symbolChecks.png");
        this.setMsgSuccessError("Las notas han sido enviadas con éxito.");
        this.setResultado("successErrorNotaReporte");
        this.setRecurso("listadoReciente");
        this.setEtiqueta("Ver Reporte");
        this.setNombreReporte(this.nombrar("listado", "pdf"));
        this.prepararEtiquetas();
        this.update(3);

        return this.getResultado();
    }

    public String rechazar() {
        try {
            this.notaFacade.rechazarNota(this.getId(), this.getObservacion(), this.getUsuario().getId());
            this.limpiar();
            this.prepararEtiquetas();
            this.update(2);
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError("La nota ha sido rechazada con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String reenviar() {
        try {
            this.notaFacade.reenviarNota(this.getId(), this.getObservacion(),
                    this.getMesaUsuario().getId(), this.getIdMesa(),
                    this.getUsuario().getId());
            this.limpiar();
            this.prepararEtiquetas();
            this.update(4);
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError("La nota ha sido reenvida con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public void validarFecha(FacesContext context, UIComponent validate, Object value) {
        Date hoy = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(hoy);
        c.add(Calendar.DATE, 1);
        hoy = c.getTime();

        if (this.getFechaCreacion().after(hoy)) {
            ((UIInput) validate).setValid(false);
            FacesMessage msg = new FacesMessage("La fecha no puede ser posterior al día de hoy.");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(validate.getClientId(context), msg);

        }
    }

    public void rederPanel() {
        if (this.render) {
            this.setRender(false);
        } else {
            this.setRender(true);
        }
    }

    public void buscarNotasEx() {
        this.getNotasEx();
    }

    public void limpiarExt() {
        this.setIdNotaBsq(null);
        this.setNroNotaBsq("");
        this.setReferenciaBsq("");
        this.setFechaDesdeBsq(null);
        this.setFechaHastaBsq(null);
    }

    public void aplicarFiltro() {
        final NotaFacadeLocal facade = this.notaFacade;
        final Set<Long> notasSeleccionadas = this.notasSeleccionadasIds;
        Long totalNotasTemp = facade.countAll(getIdNotaBsq(),
                getNroNotaBsq(), desacentuar(getReferenciaBsq().toUpperCase()),
                getFechaDesdeBsq(), getFechaHastaBsq(),
                getIdTipoAsuntoBsq(), getIdIniciadorBsq(), getIdMesaOrigenBsq(),
                getIdMesaDestinoBsq(), isConsultarHistorial(), getObservacionBsq());
        final Long totalNotas = (totalNotasTemp == null) ? 0L : totalNotasTemp;
        LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
            @Override
            public List<Nota> load(int first, int pageSize,
                    final String sortField, final SortOrder sortOrder, final Map<String, FilterMeta> filters) {
                try {
                    List<Nota> lstNotasAux = new ArrayList<>();
                    if (totalNotas < 5000L) {
                        lstNotasAux = facade.findAll(getIdNotaBsq(),
                                getNroNotaBsq(), desacentuar(getReferenciaBsq().toUpperCase()),
                                getFechaDesdeBsq(), getFechaHastaBsq(),
                                getIdTipoAsuntoBsq(), getIdIniciadorBsq(), getIdMesaOrigenBsq(),
                                getIdMesaDestinoBsq(), isConsultarHistorial(), getObservacionBsq(), first, pageSize);
                        // Sincronizar el estado checked con el Set de seleccionados
                        if (lstNotasAux != null) {
                            for (Nota nota : lstNotasAux) {
                                nota.setChecked(notasSeleccionadas.contains(nota.getId()));
                            }
                        }
                    }
                    return lstNotasAux != null ? lstNotasAux : new ArrayList<>();
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }
        };
        if (totalNotas < 5000L) {
            lstNotas.setRowCount(totalNotas.intValue());
        } else {
            lstNotas.setRowCount(0);
        }
        this.setNotas(lstNotas);
    }

    public void actualizar() {
        this.setNombreBuscar("");
        this.setIdNotaBsq(null);
        this.setNroNotaBsq("");
        this.setReferenciaBsq("");
        this.setFechaDesdeBsq(null);
        this.setFechaHastaBsq(null);
        this.setIdTipoAsuntoBsq(null);
        this.setIdIniciadorBsq(null);
        this.setIdTipoIniciadorBsq(null);
        this.setDeshabilitarIniciador(true);
        this.setIdMesaOrigenBsq(null);
        this.setIdMesaDestinoBsq(null);
        this.setNotas(null);
        this.prepararEtiquetas();
        switch (this.flagBandeja) {
            case 1: {
                //Bandeja Enviado
                final Long idMesa = this.getMesaUsuario().getId();
                Long totalNotas = this.notaFacade.countEnvidas(idMesa, 2L);
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = notaFacade.findAllEnviadas(idMesa, first, pageSize);
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                break;
            }
            case 2: {
                //Bandeja Recibidos
                final Long idMesa = this.getMesaUsuario().getId();
                Long totalNotas = this.notaFacade.countRecibidas(idMesa, 2L);
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = notaFacade.findAllRecibidas(idMesa, first, pageSize);
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                break;
            }
            case 3: {
                //Bandeja Aceptadas
                final Long idMesa = this.getMesaUsuario().getId();
                Long totalNotas = this.notaFacade.countAceptadas(idMesa, 3L);
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = notaFacade.findAllAceptadas(idMesa, first, pageSize);
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                break;
            }
            case 4: {
                //Bandeja Rechazadas
                final Long idMesa = this.getMesaUsuario().getId();
                Long totalNotas = this.notaFacade.countRechazada(idMesa, 4L);
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = notaFacade.findAllRechazadas(idMesa, first, pageSize);
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                break;
            }
            case 5: {
                //Bandeja Archivadas
                final Long idMesa = this.getMesaUsuario().getId();
                Long totalNotas = this.notaFacade.countArchivada(idMesa, 5L);
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = notaFacade.findAllArchivadas(idMesa, first, pageSize);
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                break;
            }
            case 6: {
                //Bandeja Todos
                final Long idMesa = this.getMesaUsuario().getId();
                Long totalNotas = this.notaFacade.countNotas(idMesa);
                LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {
                    @Override
                    public List<Nota> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Nota> lista = notaFacade.findAll(idMesa, first, pageSize);
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstNotas.setRowCount(totalNotas.intValue());
                this.setNotas(lstNotas);
                break;
            }
            default: {
                this.setNotas(null);
            }
        }
    }

    public void limpiarBusqueda() {
        this.setNombreBuscar("");
        this.setIdNotaBsq(null);
        this.setNroNotaBsq("");
        this.setReferenciaBsq("");
        this.setFechaDesdeBsq(null);
        this.setFechaHastaBsq(null);
        this.setIdTipoAsuntoBsq(null);
        this.setIdTipoIniciadorBsq(null);
        this.setIdIniciadorBsq(null);
        this.setObservacionBsq(null);
        this.setNotas(null);
        this.setDeshabilitarIniciador(true);
        this.setConsultarHistorial(false);
    }

    @Override
    public List<SelectItem> getSelectItems() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        this.setNota(this.notaFacade.find(idAux));
        if (this.getNota().getArchivo() != null) {
            this.prepararAdjunto(this.getNota().getArchivo());
        }
    }

    @Override
    public List<?> getListElements() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String verificarReporte() {
        Boolean flag = false;

        if (this.getFechaCreacion() != null || this.getIdMesa() != null) {
            flag = this.notaFacade.findAll(this.getFechaCreacion(), this.getHoraEnvio(), this.getMesaUsuario().getId(), this.getIdMesa(), this.getIdUsuario());
        } else { //no ingresó fecha o mesa
            flag = false;
        }

        if (flag) {
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setRecurso("reporteEnviado");
            this.setNombreReporte(this.nombrar("listado", "pdf"));
            this.setEtiqueta("Descargar reporte");
            this.setResultado("successErrorReporteNota");
            this.setMsgSuccessError("Reporte generado con éxito.");
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("La consulta realizada no genera ningún reporte.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public Resource getReporteEnviado() throws JRException, FileNotFoundException, IOException {
        Resource miRecurso = null;
        try {
            String nombreReporte = "notaDiario";
            if (this.isPerteneceContaduria()) {
                nombreReporte = "notaFirmaDiario";
            }
            if (this.getIdMesa() == null || this.getIdMesa() == 0) {
                this.setIdMesa(null);
            }

            Timestamp desde = new Timestamp(this.getFechaCreacion().getTime());
            Timestamp hasta = new Timestamp(this.getFechaCreacion().getTime());
            DateFormat formato = new SimpleDateFormat("dd/MM/YYYY");
            DateFormat largo = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
            if (this.getHoraEnvio() != null) {
                Calendar cale = Calendar.getInstance();
                cale.setTime(this.getFechaCreacion());
                cale.set(Calendar.HOUR_OF_DAY, this.getHoraEnvio().getHours());
                cale.set(Calendar.MINUTE, this.getHoraEnvio().getMinutes());
                desde.setTime(cale.getTime().getTime());
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(this.getFechaCreacion());
                desde.setTime(cal.getTime().getTime());
            }
            this.setFechaDesde(desde);

            if (formato.format(this.getFechaCreacion()).equals(formato.format(Calendar.getInstance().getTime()))) {
                //Si la fecha ingresada es hoy, el horario hasta debe ser en el que se genera el reporte
                hasta.setTime(Calendar.getInstance().getTime().getTime());
            } else {
                //Si no es hoy el horario debe ser 23:59:59.999
                Calendar cal = Calendar.getInstance();
                cal.setTime(this.getFechaCreacion());
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                hasta.setTime(cal.getTime().getTime());
            }
            this.setFechaHasta(hasta);
            String nombreOrganismo = "A";
            String nombreOrganismoPadre = "A";

            if ((this.getMesaUsuario().getOrganismo().getOrganismoPadre() == null)) {
                nombreOrganismoPadre = this.getMesaUsuario().getOrganismo().getNombre().toUpperCase();
                nombreOrganismo = "";
            } else {
                nombreOrganismo = this.getMesaUsuario().getOrganismo().getNombre().toUpperCase();
                Organismo aux = this.getMesaUsuario().getOrganismo().getOrganismoPadre();
                while (aux.getOrganismoPadre() != null) {
                    aux = aux.getOrganismoPadre();
                }
                nombreOrganismoPadre = aux.getNombre();
            }
            String mesa = this.getMesaUsuario().getDescripcion();
            JasperPrint jasperResultado = new ExpedienteReport().imprimirReporte(
                    nombreReporte, nombreOrganismoPadre, nombreOrganismo, mesa, "Enviadas", "Destino", 2,
                    this.getFechaDesde(), this.getFechaHasta(), this.getMesaUsuario().getId(), this.getIdMesa(),
                    this.getLeyendaAnual(), "", 0L, this.getIdUsuario());

            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            miRecurso = new ExpResource(bites);

        } catch (Exception ex) {
        }
        return miRecurso;
    }

    public Resource getListadoReciente() throws JRException, FileNotFoundException, IOException {
        Resource miRecurso = null;
        try {
            String nombreReporte = "notaRecienEnviadas";
            if (this.isPerteneceContaduria()) {
                nombreReporte = "notaRecienEnviadasFirma";
            }

            Timestamp desde = new Timestamp(this.getFechaEnvio().getTime());
            Calendar cale = Calendar.getInstance();
            cale.setTime(this.getFechaEnvio());
            cale.set(Calendar.HOUR_OF_DAY, this.getFechaEnvio().getHours());
            cale.set(Calendar.MINUTE, this.getFechaEnvio().getMinutes());
            desde.setTime(cale.getTime().getTime());
            this.setFechaDesde(desde);

            Timestamp hasta = new Timestamp(new Date().getTime());
            hasta.setTime(Calendar.getInstance().getTime().getTime());
            this.setFechaHasta(hasta);

            String nombreOrganismo = "A";
            String nombreOrganismoPadre = "A";

            if ((this.getMesaUsuario().getOrganismo().getOrganismoPadre() == null)) {
                nombreOrganismoPadre = this.getMesaUsuario().getOrganismo().getNombre().toUpperCase();
                nombreOrganismo = "";
            } else {
                nombreOrganismo = this.getMesaUsuario().getOrganismo().getNombre().toUpperCase();
                Organismo aux = this.getMesaUsuario().getOrganismo().getOrganismoPadre();
                while (aux.getOrganismoPadre() != null) {
                    aux = aux.getOrganismoPadre();
                }
                nombreOrganismoPadre = aux.getNombre();
            }
            String mesa = this.getMesaUsuario().getDescripcion();
            if (this.getMesaUsuario().isMesaGeneral() && this.getMesaUsuario().getNombre().contains("MESA DE ENTRADAS Y SALIDAS")) {
                mesa = "MESA DE ENTRADAS Y SALIDAS";
            }

            String listado = "";
            Boolean primero = true;

            for (Nota notaAux : this.getNotasAux()) {
                if (!primero) {
                    listado = listado.concat(", '" + notaAux.getId() + "'");
                    primero = false;
                } else {
                    listado = listado.concat("('" + notaAux.getId() + "' ");
                    primero = false;
                }
            }

            listado = listado.concat(")");

            String mesaExt = "";

            if (this.getIdMesa() != null && this.getIdMesa() != 0) {
                Mesa mesaAux = this.mesaFacade.find(this.getIdMesa());
                mesaExt = mesaAux.getDescripcion();
                if (mesaAux.isMesaGeneral() && mesaAux.getNombre().contains("MESA DE ENTRADAS Y SALIDAS")) {
                    mesaExt = mesaAux.getOrganismo().getNombre() + " - MESA DE ENTRADAS Y SALIDAS";
                }
            }

            JasperPrint jasperResultado = new ExpedienteReport().imprimirReporteRecienEnviados(
                    nombreReporte, nombreOrganismoPadre, nombreOrganismo, mesa,
                    this.getFechaDesde(), this.getFechaHasta(), this.getLeyendaAnual(), mesaExt, listado);

            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            miRecurso = new ExpResource(bites);
        } catch (Exception ex) {
        }
        return miRecurso;
    }

    public List<SelectItem> getSelectItemsTipoIniciador() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<TipoIniciador> lstTipoIniciador = this.tipoIniciadorFacade.findAll(true);
        if (!(lstTipoIniciador.isEmpty())) {
            for (TipoIniciador tipoIniciadorAux : lstTipoIniciador) {
                if (tipoIniciadorAux.isEstado()) {
                    selectItems.add(new SelectItem(tipoIniciadorAux.getId(), tipoIniciadorAux.getNombre()));
                }

            }
        }
        return selectItems;
    }

    public void cargarIniciadores(ValueChangeEvent event) {
        Object value = event.getNewValue();
        Object idComp = event.getComponent().getId();
        this.getSelectItemsIniciador().clear();
        this.selectItemsIniciador.clear();
        this.setIdIniciador(null);
        List<Iniciador> lstIniciadores = this.iniciadorFacade.findAllByTipo(Long.parseLong(value.toString()));
        if (!(lstIniciadores.isEmpty())) {
            for (Iniciador iniciadorAux : lstIniciadores) {
                if (iniciadorAux.isEstado()) {
                    if (iniciadorAux.getTipoIniciador().isExterno() && (iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.COOPERATIVA) || iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.MUTUAL))) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(iniciadorAux.getNombre());
                        sb.append(" - ").append(iniciadorAux.getDocumento());
                        if (iniciadorAux.getMatriculaProvincial() != null && !iniciadorAux.getMatriculaProvincial().isEmpty()) {
                            sb.append(" - Mat. Prov.: ").append(iniciadorAux.getMatriculaProvincial());
                        }
                        if (iniciadorAux.getMatriculaNacional() != null && !iniciadorAux.getMatriculaNacional().isEmpty()) {
                            sb.append(" - Mat. Nac.: ").append(iniciadorAux.getMatriculaNacional());
                        }
                        this.selectItemsIniciador.add(new SelectItem(iniciadorAux.getId(), sb.toString()));
                    } else if (iniciadorAux.getTipoIniciador().isExterno() && !(iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.COOPERATIVA) || iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.MUTUAL))) {
                        this.selectItemsIniciador.add(new SelectItem(iniciadorAux.getId(), iniciadorAux.getNombre() + " - " + iniciadorAux.getDocumento()));
                    } else {
                        this.selectItemsIniciador.add(new SelectItem(iniciadorAux.getId(), iniciadorAux.getNombre()));
                    }
                    if (idComp.equals("tipoIniciador") && iniciadorAux.getNombre().equals(this.getMesaUsuario().getNombre())) {
                        this.setIdIniciador(iniciadorAux.getId());
                    }
                }
            }
            this.setDeshabilitarIniciador(false);
        } else {
            this.setDeshabilitarIniciador(true);
        }
    }

    public void cargarIniciadores(Long idInic) {
        this.selectItemsIniciador.clear();
        List<Iniciador> lstIniciadores = this.iniciadorFacade.findAllByTipo(idInic);
        if (!(lstIniciadores.isEmpty())) {
            for (Iniciador iniciadorAux : lstIniciadores) {
                if (iniciadorAux.isEstado()) {
                    if (iniciadorAux.getTipoIniciador().isExterno() && (iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.COOPERATIVA) || iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.MUTUAL))) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(iniciadorAux.getNombre());
                        sb.append(" - ").append(iniciadorAux.getDocumento());
                        if (iniciadorAux.getMatriculaProvincial() != null && !iniciadorAux.getMatriculaProvincial().isEmpty()) {
                            sb.append(" - Mat. Prov.: ").append(iniciadorAux.getMatriculaProvincial());
                        }
                        if (iniciadorAux.getMatriculaNacional() != null && !iniciadorAux.getMatriculaNacional().isEmpty()) {
                            sb.append(" - Mat. Nac.: ").append(iniciadorAux.getMatriculaNacional());
                        }
                        this.selectItemsIniciador.add(new SelectItem(iniciadorAux.getId(), sb.toString()));
                    } else if (iniciadorAux.getTipoIniciador().isExterno() && !(iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.COOPERATIVA) || iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.MUTUAL))) {
                        this.selectItemsIniciador.add(new SelectItem(iniciadorAux.getId(), iniciadorAux.getNombre() + " - " + iniciadorAux.getDocumento()));
                    } else {
                        this.selectItemsIniciador.add(new SelectItem(iniciadorAux.getId(), iniciadorAux.getNombre()));
                    }
                }
            }
            this.setDeshabilitarIniciador(false);
        } else {
            this.setDeshabilitarIniciador(true);
        }
    }

    public void cargarIniciadoresSeleccionados() {
        if (this.idTipoIniciador == null) {
            this.selectItemsIniciador.clear();
            this.setIdIniciador(null);
            this.setDeshabilitarIniciador(true);
            return;
        }
        cargarIniciadores(this.idTipoIniciador);
    }

    public void cargarIniciadoresBusquedaSeleccionados() {
        if (this.idTipoIniciadorBsq == null) {
            this.selectItemsIniciador.clear();
            this.setIdIniciadorBsq(null);
            this.setDeshabilitarIniciador(true);
            return;
        }
        cargarIniciadores(this.idTipoIniciadorBsq);
    }

    public String mensajear() {
        String mensaje = "";
        if (this.notas != null) {
            int total = this.notas.getRowCount();
            switch (total) {
                case 0:
                    mensaje = "No se encontraron notas.";
                    break;
                case 1:
                    mensaje = "Se encontró una nota.";
                    break;
                default:
                    mensaje = "Se encontraron " + total + " notas.";
                    break;
            }
        } else {
            mensaje = "No se encontraron notas.";
        }

        return mensaje;
    }

    /** Si la petición es multipart y trae un archivo y aún no tenemos path, lo guarda y actualiza archivo. */
    private void asegurarArchivoDesdeRequest() {
        try {
            if (this.getArchivo() != null && !this.getArchivo().trim().isEmpty()) return;
            Object req = FacesContext.getCurrentInstance().getExternalContext().getRequest();
            if (!(req instanceof HttpServletRequest)) return;
            HttpServletRequest request = (HttpServletRequest) req;
            String ct = request.getContentType();
            if (ct == null || !ct.toLowerCase().startsWith("multipart/form-data")) return;
            Collection<Part> parts = request.getParts();
            if (parts == null || parts.isEmpty()) return;
            Part filePart = null;
            for (Part p : parts) {
                if (p.getSize() > 0) {
                    String fn = p.getSubmittedFileName();
                    if (fn != null && !fn.trim().isEmpty()) { filePart = p; break; }
                }
            }
            if (filePart == null) return;
            String fn = filePart.getSubmittedFileName();
            int i = fn.lastIndexOf('.');
            int pIdx = Math.max(fn.lastIndexOf('/'), fn.lastIndexOf('\\'));
            String ext = (i > pIdx && i > 0) ? fn.substring(i + 1) : "";
            String nombreArchivo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + (ext.isEmpty() ? "" : "." + ext);
            File dir = new File(getPathArchivo());
            if (!dir.exists() && !dir.mkdirs()) return;
            File dest = new File(dir, nombreArchivo);
            try (InputStream in = filePart.getInputStream(); java.io.FileOutputStream out = new java.io.FileOutputStream(dest)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }
            this.setArchivo(dest.getAbsolutePath());
        } catch (Exception e) { }
    }

    public void cargarArchivo(org.primefaces.event.FileUploadEvent event) {
        org.primefaces.model.file.UploadedFile file = event.getFile();
        if (file == null) return;
        String tipo = file.getContentType();
        // Tipos MIME válidos (incluyendo variantes comunes)
        String[] tipos = new String[]{
            "application/pdf", "application/x-pdf",
            "image/jpeg", "image/jpg", "image/pjpeg",
            "image/bmp", "image/png", "image/x-png"
        };
        boolean valido = false;
        for (String item : tipos) {
            if (item != null && item.equalsIgnoreCase(tipo)) { valido = true; break; }
        }
        // Si no coincide por tipo MIME, validar por extensión del archivo
        if (!valido) {
            String fn = file.getFileName();
            if (fn != null && !fn.trim().isEmpty()) {
                String ext = fn.substring(fn.lastIndexOf('.') + 1).toLowerCase();
                valido = ext.matches("pdf|jpg|jpeg|bmp|png");
            }
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
            File directorioAdjuntos = new File(this.getPathArchivo());
            if (!directorioAdjuntos.exists() && !directorioAdjuntos.mkdirs()) {
                throw new IOException("No se pudo crear el directorio de adjuntos: " + directorioAdjuntos.getAbsolutePath());
            }
            File miArchivoAux = new File(directorioAdjuntos, nombreArchivo);
            try (java.io.InputStream in = file.getInputStream(); java.io.FileOutputStream out = new java.io.FileOutputStream(miArchivoAux)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }
            this.setArchivo(miArchivoAux.getAbsolutePath());
            this.setNombreArchivo(nombreArchivo);
            // Preparar el recurso para la vista previa
            this.prepararAdjunto(miArchivoAux.getAbsolutePath());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Se adjuntó el archivo.", "Se adjuntó el archivo."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al adjuntar el archivo.", e.getMessage()));
        }
    }

    public void prepararAdjunto(String adjunto) { //devuelve el archivo adjunto segun el path que se le pase
        File file = resolveAdjuntoFile(adjunto);
        this.setNombreArchivo(file != null ? file.getName() : "");
        if (file == null || !file.exists() || !file.isFile()) {
            this.resource = new ArchivoResource(new byte[0]);
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            this.resource = new ArchivoResource(readIntoByteArray(fis));
        } catch (Exception e) {
            this.resource = new ArchivoResource(new byte[0]);
        }
    }

    private File resolveAdjuntoFile(String adjunto) {
        if (adjunto == null || adjunto.trim().isEmpty()) {
            return null;
        }
        File byAbsolutePath = new File(adjunto);
        if (byAbsolutePath.exists() && byAbsolutePath.isFile()) {
            return byAbsolutePath;
        }
        // Fallback: algunos registros guardan rutas antiguas; intentar por nombre dentro del path actual configurado.
        File byCurrentStorage = new File(this.getPathArchivo(), byAbsolutePath.getName());
        if (byCurrentStorage.exists() && byCurrentStorage.isFile()) {
            return byCurrentStorage;
        }
        return byAbsolutePath;
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
        String configuredPath = System.getProperty("sysse.adjuntos.path");
        if (configuredPath != null && !configuredPath.trim().isEmpty()) {
            return configuredPath.endsWith(File.separator) ? configuredPath : configuredPath + File.separator;
        }
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.trim().isEmpty()) {
            userHome = ".";
        }
        return userHome + File.separator + "DatosExpedientes" + File.separator;
    }

    public String getLeyendaAnual() {
        return ("\"" + String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + " - " + this.leyendaFacade.findAll(Long.valueOf(Calendar.getInstance().get(Calendar.YEAR)), "").get(0).getNombre() + "\"");
    }

    public String prepararParaAceptarReenviar() {
        int flagCount = notasSeleccionadasIds.size();
        if (flagCount > 0) {
            // Mesa de Contabilidad por defecto tiene el destino la mesa de entradas de Contaduría General
            if (this.getMesaUsuario().getId() == 11189L) {
                this.setIdMesa(204L);
            } else {
                this.setIdMesa(null);
            }
            
            notasAux = new ArrayList<Nota>();
            Set<Long> clavesAgregadas = new HashSet<>(); // Para evitar duplicados
            
            for (Long notaId : notasSeleccionadasIds) {
                if (!clavesAgregadas.contains(notaId)) {
                    Nota notaAux = this.notaFacade.find(notaId);
                    if (notaAux != null) {
                        notasAux.add(notaAux);
                        clavesAgregadas.add(notaId);
                    }
                }
            }
            
            if (flagCount == 1) {
                Nota notaIte = notasAux.get(0);
                this.setResultado("aceptarReenviarNota");
                this.setNota(notaIte);
                this.setId(notaIte.getId());
            } else {
                this.setResultado("aceptarReenviarNotaMultiple");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar al menos una nota de la tabla para realizar esta acción.");
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String aceptarReenviarNota() {
        try {
            this.notaFacade.aceptarReenviarNota(this.getId(), this.getObservacion(),
                    this.getMesaUsuario().getId(), this.getIdMesa(),
                    this.getUsuario().getId());
            this.limpiar();
            this.prepararEtiquetas();
            this.update(2);
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError("La nota ha sido aceptada y enviado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }

    public String aceptarReenviarNotas() {
        this.setFechaEnvio(new Date());

        for (Nota notaAux : this.getNotasAux()) {
            try {
                this.notaFacade.aceptarReenviarNota(notaAux.getId(), this.getObservacion(),
                        this.getMesaUsuario().getId(), this.getIdMesa(),
                        this.getUsuario().getId());
            } catch (Exception ex) {

                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError(ex.getMessage());
                this.setResultado("successErrorNota");
            }

        }
        this.setTitle("Proceso Completo");
        this.setImages("images/symbolChecks.png");
        this.setMsgSuccessError("Las notas han sido aceptadas y reenviadas con éxito.");
        this.setResultado("successErrorNotaReporte");
        this.setRecurso("listadoReciente");
        this.setEtiqueta("Ver Reporte");
        this.setNombreReporte(this.nombrar("listado", "pdf"));
        this.prepararEtiquetas();
        this.update(2);

        return this.getResultado();
    }

    public List<SelectItem> getSelectItemsUsuarios() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Usuario> lstUsuarios = this.usuarioFacade.findAllMesa(this.getMesaUsuario().getId());
        if (!(lstUsuarios.isEmpty())) {
            lstUsuarios.stream().filter((usuarioAux) -> (usuarioAux.isEstado())).forEach((usuarioAux) -> {
                selectItems.add(new SelectItem(usuarioAux.getId(), usuarioAux.getNombre().concat(", ").concat(usuarioAux.getApellido())));
            });
        }
        return selectItems;
    }

    @Override
    public LazyDataModel<?> getItems() {
        return this.getNotas();
    }

    public String guardarEdicionMultiple() {
        try {
            for (Nota nota : notasAux) {
                this.notaFacade.edit(nota.getId(), this.getObservacion(),
                        this.getIdMesa(), this.getUsuario().getId());
            }

            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorNota");
            this.setMsgSuccessError("Las notas han sido editadas con éxito.");
            this.limpiar();
            this.prepararEtiquetas();
            this.actualizar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorNota");
        }
        return this.getResultado();
    }
}
