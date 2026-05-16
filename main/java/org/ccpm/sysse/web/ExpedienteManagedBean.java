package org.ccpm.sysse.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import jakarta.faces.application.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.model.SelectItem;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.facade.*;
import org.ccpm.sysse.utilidad.*;
import org.ccpm.sysse.reporte.ExpedienteReport;
import static org.ccpm.sysse.web.UtilManagedBean.desacentuar;
import org.primefaces.model.FilterMeta;
import org.ccpm.sysse.web.compat.LazyDataModel;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.DefaultStreamedContent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;

@Named
@SessionScoped
public class ExpedienteManagedBean extends UtilManagedBean implements Serializable {
    private static final Logger logger = Logger.getLogger(ExpedienteManagedBean.class.getName());

    @EJB
    private ExpedienteFacadeLocal expedienteFacade;
    @EJB
    private TipoIniciadorFacadeLocal tipoIniciadorFacade;
    @EJB
    private IniciadorFacadeLocal iniciadorFacade;
    @EJB
    private UsuarioExternoFacadeLocal usuarioExternoFacade;
    @EJB
    private TipoAsuntoFacadeLocal tipoAsuntoFacade;
    @EJB
    private MesaFacadeLocal mesaFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private InstrumentoLegalFacadeLocal instrumentoLegalFacade;
    @EJB
    private TipoInstrumentoLegalFacadeLocal tipoInstrumentoLegalFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    @EJB
    private LeyendaFacadeLocal leyendaFacade;

    private String totalEnviado;
    private String totalRecibido;
    private String totalAceptado;
    private String totalRechazado;
    private String totalArchivado;
    private String total;
    private String titulo;
    private Integer flagBandeja = 6;
    private Usuario usuario;
    private List<ActionItem> lstActionItems;
    private List<Organismo> lstOrganismo;
    private LazyDataModel<Expediente> expedientes;
    private DataTable tablaExpedientes;
    private List<Expediente> expedientesAux;
    private List<HistorialExpediente> historiales;
    // Set para mantener los expedientes seleccionados (clave única: codOrganismo-numCorrelativo-anio)
    private Set<org.ccpm.sysse.utilidad.ExpedienteClave> expedientesSeleccionados = new HashSet<>();
    /** Lista precargada al hacer clic en Exportar; evita depender del contexto en la petición del stream. */
    private List<Expediente> listaExpedientesParaExportar;
    List<SelectItem> selectItemsIniciador = new ArrayList<>();
    List<SelectItem> selectItemsIniciadorBsq = new ArrayList<>();
    private boolean ultimoNro, expedienteExterno, listado;
    private String mensajeUltimoNro;
    private Expediente expediente;
    //para nuevo expedientes
    private Long idTipoIniciador;
    private Long idIniciador;
    private Long idTipoAsunto;
    private Long idMesa;
    private Mesa mesaUsuario;//
    private Organismo organismoUsuario;
    private Long idOrganismo;
    private Long codOrganismo;
    private Long codOrganismoAgro;
    private Long nroCorrel;
    private String asunto;
    private String observacion;
    private String ordenPago;
    private Integer pedidoFondo;
    private Integer pedidoFondoAnio;
    private Integer ordenEntrega;
    private Integer ordenEntregaAnio;
    private String expMadre;
    private String motivoArchivo;
    private Integer cuerpo;
    private Integer fojas;
    private Integer annio;
    private Date fechaCreacion;
    private Date fechaEnvio;
    private Date fechaActual = new Date();
    private Date horaEnvio = quitarTiempo(new Date());
    private Date fechaMovimiento;
    private boolean visibleEditar, visibleAceptar, visibleRechazar, visibleReenviar, visibleEnviar, visibleAceptarReenviar;
    private boolean perteneceContaduria = false, perteneceEntSalAgro = false, perteneceContencion = false,
            perteneceLegal = false, perteneceCooperativa = false, perteneceSaludPublica = false, aceptarReenviar = false, ingresarSiempre = false;
    private boolean aceptar, rechazar, enviar, reenviar, desarchivar, ingresar;
    private boolean archivar, agregarInstrumento, desvincularInstrumento, reporte;
    private boolean visibleArchivar, visibleAgregarInst, visibleDesvincularInst, visibleReporte;
    private boolean visibleDesarchivar, visibleReporteEnviados, visibleReporteRecibidos, visibleReporteTodos;
    private boolean visibleReporteAceptados, visibleReporteRechazados, visibleReporteArchivados, deshabilitarIniciador = true;
    private List<String> lstInstLegalSeleccionado;
    private String tipoListado;
    private String tipoFecha;
    private String tipoOrganismo;
    private Expediente expedienteGen;
    private Organismo organismoAsoc;
    private String nroExpedienteAux;
    //atributos para búsqueda//
    private boolean render = false;
    private Long idTipoAsuntoBsq;
    private Long idIniciadorBsq;
    private Long idTipoIniciadorBsq;
    private Long codBsq;
    private Long nroBsq;
    private Long idUsuario;
    private Integer annioBsq;
    private Date fechaDesdeBsq;
    private Date fechaHastaBsq;
    private Date fechaDesdeUltimoMovBsq;
    private Date fechaHastaUltimoMovBsq;
    private String asuntoBsq = "";
    private List<Long> idItems = new ArrayList<>();
    private Integer pedidoFondoBsq;
    private Integer pedidoFondoAnioBsq;
    private Integer ordenEntregaBsq;
    private Integer ordenEntregaAnioBsq;
    private String expMadreBsq;
    private Long idMesaOrigenBsq;
    private Long idMesaDestinoBsq;
    private Date fechaDesde;
    private Date fechaHasta;
    //atributos para ingreso expediente externo
    private Long codOrganismoIng;
    private Long nroCorrelIng;
    private Integer fojasIng;
    private Integer annioIng;
    //Paneles
    private String desarchivarPanel = "";
    private String aceptarPanel = "";
    private String msgDesarchivar = "";
    private String msgAceptar = "";
    private String type = "PDF";
    private Resource resource;
    //Instrumentos Legales
    private String descripcionInstrumentoBuscar;
    private Long idTipoInstrumentoLegal;
    private String numero;
    private String descripcion;
    private String tipoInstrumento;
    private int fojasInst;
    private int fojasInstOld;
    private InstrumentoLegal instAux;
    private String archivo = "";
    private String archivoOld = "";
    private String nombreArchivo = "";
    private boolean altaInstrumentoLegal, detalleInstrumentoLegal, bajaInstrumentoLegal, editarInstrumentoLegal;
    private final String SistemaOperativo = System.getProperty("os.name");
    //Para successError
    private String etiqueta;
    private String nombreReporte;
    private String recurso;

    /**
     * Creates a new instance of ExpedienteManagedBean
     */
    public ExpedienteManagedBean() {
        this.lstActionItems = new ArrayList<>();
        this.expedientes = null;
        this.cuerpo = 1;
        this.fojas = 1;
        this.fechaCreacion = new Date();

        if (this.getIdTipoInic() != null) {
            this.setIdTipoIniciador(this.getIdTipoInic());
            this.setDeshabilitarIniciador(false);
        }
    }

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            this.setUsuario(sessionBean.getUsuario());
            this.setMesaUsuario(this.usuarioFacade.findMesaUsuario(this.getUsuario().getId()));
            this.setOrganismoUsuario(this.usuarioFacade.findOrganismoUsuario(this.getUsuario().getId()));
            try {
                this.prepararAcciones(sessionBean.getUsuario());
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

    public MesaFacadeLocal getMesaFacade() {
        return mesaFacade;
    }

    public void setMesaFacade(MesaFacadeLocal mesaFacade) {
        this.mesaFacade = mesaFacade;
    }

    public UsuarioFacadeLocal getUsuarioFacade() {
        return usuarioFacade;
    }

    public void setUsuarioFacade(UsuarioFacadeLocal usuarioFacade) {
        this.usuarioFacade = usuarioFacade;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getFlagBandeja() {
        return flagBandeja;
    }

    public void setFlagBandeja(Integer flagBandeja) {
        this.flagBandeja = flagBandeja;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public LazyDataModel<Expediente> getExpedientes() {
        return expedientes;
    }

    public void setExpedientes(LazyDataModel<Expediente> expedientes) {
        this.expedientes = expedientes;
    }

    public DataTable getTablaExpedientes() {
        return tablaExpedientes;
    }

    public void setTablaExpedientes(DataTable tablaExpedientes) {
        this.tablaExpedientes = tablaExpedientes;
    }

    public void prepararExportacionExpedientes() {
        // Cargar todos los expedientes de la bandeja actual (total, no solo primera página)
        // para que el stream de descarga use esta lista aunque se abra en otra petición.
        this.listaExpedientesParaExportar = getExpedientesParaExportar();
        if (this.tablaExpedientes != null) {
            this.tablaExpedientes.setFirst(0);
        }
    }
    
    // Método de compatibilidad para mantener código existente que espera List
    public void setExpedientesList(List<Expediente> expedientesList) {
        if (expedientesList != null) {
            this.expedientes = new LazyDataModel<Expediente>() {
                @Override
                public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                    int end = Math.min(first + pageSize, expedientesList.size());
                    if (first >= expedientesList.size()) {
                        return new ArrayList<>();
                    }
                    return expedientesList.subList(first, end);
                }
            };
            this.expedientes.setRowCount(expedientesList.size());
        } else {
            this.expedientes = null;
        }
    }
    
    // Método helper para obtener todos los expedientes del LazyDataModel como List
    // Úsalo solo cuando necesites iterar sobre todos los expedientes
    private List<Expediente> getExpedientesAsList() {
        LazyDataModel<Expediente> lazyModel = this.getExpedientes();
        if (lazyModel == null) {
            return new ArrayList<>();
        }
        int total = lazyModel.getRowCount();
        if (total <= 0) {
            return new ArrayList<>();
        }
        // Cargar todos los expedientes (puede ser costoso si hay muchos)
        List<Expediente> lista = lazyModel.load(0, total, null, SortOrder.UNSORTED, null);
        // Sincronizar el estado checked con el Set de seleccionados
        for (Expediente exp : lista) {
            org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
            );
            exp.setChecked(expedientesSeleccionados.contains(clave));
        }
        return lista;
    }

    /**
     * Obtiene todos los expedientes de la bandeja actual para exportar, usando el facade
     * con el total real (no solo la primera página del paginador).
     */
    private List<Expediente> getExpedientesParaExportar() {
        if (getMesaUsuario() == null || getMesaUsuario().getId() == null) {
            return new ArrayList<>();
        }
        Long idMesa = getMesaUsuario().getId();
        Long totalLong;
        List<Expediente> lista;
        switch (flagBandeja) {
            case 1: // Enviados
                totalLong = expedienteFacade.countByEstado(null, idMesa, 2L);
                lista = totalLong != null && totalLong > 0
                        ? expedienteFacade.findAllByEstado(null, idMesa, 2L, 0, totalLong.intValue())
                        : new ArrayList<>();
                break;
            case 2: // Recibidos
                totalLong = expedienteFacade.countByEstado(idMesa, null, 2L);
                lista = totalLong != null && totalLong > 0
                        ? expedienteFacade.findAllByEstado(idMesa, null, 2L, 0, totalLong.intValue())
                        : new ArrayList<>();
                break;
            case 3: // Aceptados
                totalLong = expedienteFacade.countByEstado(idMesa, null, 3L);
                lista = totalLong != null && totalLong > 0
                        ? expedienteFacade.findAllByEstado(idMesa, null, 3L, 0, totalLong.intValue())
                        : new ArrayList<>();
                break;
            case 4: // Rechazados
                totalLong = expedienteFacade.countByEstado(null, idMesa, 4L);
                lista = totalLong != null && totalLong > 0
                        ? expedienteFacade.findAllByEstado(null, idMesa, 4L, 0, totalLong.intValue())
                        : new ArrayList<>();
                break;
            case 5: // Archivados
                totalLong = expedienteFacade.countByEstado(idMesa, null, 5L);
                lista = totalLong != null && totalLong > 0
                        ? expedienteFacade.findAllByEstado(idMesa, null, 5L, 0, totalLong.intValue())
                        : new ArrayList<>();
                break;
            case 6: // Todos
                totalLong = expedienteFacade.countNotas(idMesa);
                lista = totalLong != null && totalLong > 0
                        ? expedienteFacade.findAll(idMesa, 0, totalLong.intValue())
                        : new ArrayList<>();
                break;
            default:
                return getExpedientesAsList();
        }
        for (Expediente exp : lista) {
            org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                    exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio());
            exp.setChecked(expedientesSeleccionados.contains(clave));
        }
        return lista;
    }
    
    // Método para verificar si un expediente está seleccionado
    public boolean isExpedienteSeleccionado(Expediente expediente) {
        if (expediente == null) {
            return false;
        }
        org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
            expediente.getCodOrganismo(),
            expediente.getNumCorrelativo(),
            expediente.getAnio()
        );
        return expedientesSeleccionados.contains(clave);
    }

    public List<Expediente> getExpedientesAux() {
        return expedientesAux;
    }

    public void setExpedientesAux(List<Expediente> expedientesAux) {
        this.expedientesAux = expedientesAux;
    }

    public List<HistorialExpediente> getHistoriales() {
        return historiales;
    }

    public void setHistoriales(List<HistorialExpediente> historiales) {
        this.historiales = historiales;
    }

    public List<SelectItem> getSelectItemsIniciadorBsq() {
        return selectItemsIniciadorBsq;
    }

    public void setSelectItemsIniciadorBsq(List<SelectItem> selectItemsIniciadorBsq) {
        this.selectItemsIniciadorBsq = selectItemsIniciadorBsq;
    }

    public boolean isUltimoNro() {
        return ultimoNro;
    }

    public void setUltimoNro(boolean ultimoNro) {
        this.ultimoNro = ultimoNro;
    }

    public boolean isExpedienteExterno() {
        return expedienteExterno;
    }

    public void setExpedienteExterno(boolean expedienteExterno) {
        this.expedienteExterno = expedienteExterno;
    }

    public boolean isListado() {
        return listado;
    }

    public void setListado(boolean listado) {
        this.listado = listado;
    }

    public Expediente getExpediente() {
        return expediente;
    }

    public void setExpediente(Expediente expediente) {
        this.expediente = expediente;
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

    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }

    public Long getCodOrganismo() {
        return codOrganismo;
    }

    public void setCodOrganismo(Long codOrganismo) {
        this.codOrganismo = codOrganismo;
    }

    public Long getCodOrganismoAgro() {
        return codOrganismoAgro;
    }

    public void setCodOrganismoAgro(Long codOrganismoAgro) {
        this.codOrganismoAgro = codOrganismoAgro;
    }

    public Long getNroCorrel() {
        return nroCorrel;
    }

    public void setNroCorrel(Long nroCorrel) {
        this.nroCorrel = nroCorrel;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getOrdenPago() {
        return ordenPago;
    }

    public void setOrdenPago(String ordenPago) {
        this.ordenPago = ordenPago;
    }

    public Integer getPedidoFondo() {
        return pedidoFondo;
    }

    public void setPedidoFondo(Integer pedidoFondo) {
        this.pedidoFondo = pedidoFondo;
    }

    public Integer getPedidoFondoAnio() {
        return pedidoFondoAnio;
    }

    public void setPedidoFondoAnio(Integer pedidoFondoAnio) {
        this.pedidoFondoAnio = pedidoFondoAnio;
    }

    public Integer getOrdenEntrega() {
        return ordenEntrega;
    }

    public void setOrdenEntrega(Integer ordenEntrega) {
        this.ordenEntrega = ordenEntrega;
    }

    public Integer getOrdenEntregaAnio() {
        return ordenEntregaAnio;
    }

    public void setOrdenEntregaAnio(Integer ordenEntregaAnio) {
        this.ordenEntregaAnio = ordenEntregaAnio;
    }

    public String getExpMadre() {
        return expMadre;
    }

    public void setExpMadre(String expMadre) {
        this.expMadre = expMadre;
    }

    public String getMotivoArchivo() {
        return motivoArchivo;
    }

    public void setMotivoArchivo(String motivoArchivo) {
        this.motivoArchivo = motivoArchivo;
    }

    public Integer getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(Integer cuerpo) {
        this.cuerpo = cuerpo;
    }

    public Integer getFojas() {
        return fojas;
    }

    public void setFojas(Integer fojas) {
        this.fojas = fojas;
    }

    public Integer getAnnio() {
        return annio;
    }

    public void setAnnio(Integer annio) {
        this.annio = annio;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Date fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Date getFechaActual() {
        return fechaActual;
    }

    public void setFechaActual(Date fechaActual) {
        this.fechaActual = fechaActual;
    }

    public Date getHoraEnvio() {
        return horaEnvio;
    }

    public void setHoraEnvio(Date horaEnvio) {
        this.horaEnvio = horaEnvio;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Date fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
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

    public boolean isVisibleReenviar() {
        return visibleReenviar;
    }

    public void setVisibleReenviar(boolean visibleReenviar) {
        this.visibleReenviar = visibleReenviar;
    }

    public boolean isVisibleEnviar() {
        return visibleEnviar;
    }

    public void setVisibleEnviar(boolean visibleEnviar) {
        this.visibleEnviar = visibleEnviar;
    }

    public boolean isPerteneceContaduria() {
        return perteneceContaduria;
    }

    public void setPerteneceContaduria(boolean perteneceContaduria) {
        this.perteneceContaduria = perteneceContaduria;
    }

    public boolean isPerteneceEntSalAgro() {
        return perteneceEntSalAgro;
    }

    public void setPerteneceEntSalAgro(boolean perteneceEntSalAgro) {
        this.perteneceEntSalAgro = perteneceEntSalAgro;
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

    public boolean isPerteneceCooperativa() {
        return perteneceCooperativa;
    }

    public void setPerteneceCooperativa(boolean perteneceCooperativa) {
        this.perteneceCooperativa = perteneceCooperativa;
    }

    public boolean isPerteneceSaludPublica() {
        return perteneceSaludPublica;
    }

    public void setPerteneceSaludPublica(boolean perteneceSaludPublica) {
        this.perteneceSaludPublica = perteneceSaludPublica;
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

    public boolean isIngresar() {
        return ingresar;
    }

    public void setIngresar(boolean ingresar) {
        this.ingresar = ingresar;
    }

    public boolean isArchivar() {
        return archivar;
    }

    public void setArchivar(boolean archivar) {
        this.archivar = archivar;
    }

    public boolean isAgregarInstrumento() {
        return agregarInstrumento;
    }

    public void setAgregarInstrumento(boolean agregarInstrumento) {
        this.agregarInstrumento = agregarInstrumento;
    }

    public boolean isDesvincularInstrumento() {
        return desvincularInstrumento;
    }

    public void setDesvincularInstrumento(boolean desvincularInstrumento) {
        this.desvincularInstrumento = desvincularInstrumento;
    }

    public boolean isReporte() {
        return reporte;
    }

    public void setReporte(boolean reporte) {
        this.reporte = reporte;
    }

    public boolean isVisibleArchivar() {
        return visibleArchivar;
    }

    public void setVisibleArchivar(boolean visibleArchivar) {
        this.visibleArchivar = visibleArchivar;
    }

    public boolean isVisibleAgregarInst() {
        return visibleAgregarInst;
    }

    public void setVisibleAgregarInst(boolean visibleAgregarInst) {
        this.visibleAgregarInst = visibleAgregarInst;
    }

    public boolean isVisibleDesvincularInst() {
        return visibleDesvincularInst;
    }

    public void setVisibleDesvincularInst(boolean visibleDesvincularInst) {
        this.visibleDesvincularInst = visibleDesvincularInst;
    }

    public boolean isVisibleReporte() {
        return visibleReporte;
    }

    public void setVisibleReporte(boolean visibleReporte) {
        this.visibleReporte = visibleReporte;
    }

    public boolean isVisibleDesarchivar() {
        return visibleDesarchivar;
    }

    public void setVisibleDesarchivar(boolean visibleDesarchivar) {
        this.visibleDesarchivar = visibleDesarchivar;
    }

    public boolean isVisibleReporteEnviados() {
        return visibleReporteEnviados;
    }

    public void setVisibleReporteEnviados(boolean visibleReporteEnviados) {
        this.visibleReporteEnviados = visibleReporteEnviados;
    }

    public boolean isVisibleReporteRecibidos() {
        return visibleReporteRecibidos;
    }

    public void setVisibleReporteRecibidos(boolean visibleReporteRecibidos) {
        this.visibleReporteRecibidos = visibleReporteRecibidos;
    }

    public boolean isVisibleReporteTodos() {
        return visibleReporteTodos;
    }

    public void setVisibleReporteTodos(boolean visibleReporteTodos) {
        this.visibleReporteTodos = visibleReporteTodos;
    }

    public boolean isVisibleReporteAceptados() {
        return visibleReporteAceptados;
    }

    public void setVisibleReporteAceptados(boolean visibleReporteAceptados) {
        this.visibleReporteAceptados = visibleReporteAceptados;
    }

    public boolean isVisibleReporteRechazados() {
        return visibleReporteRechazados;
    }

    public void setVisibleReporteRechazados(boolean visibleReporteRechazados) {
        this.visibleReporteRechazados = visibleReporteRechazados;
    }

    public boolean isVisibleReporteArchivados() {
        return visibleReporteArchivados;
    }

    public void setVisibleReporteArchivados(boolean visibleReporteArchivados) {
        this.visibleReporteArchivados = visibleReporteArchivados;
    }

    public boolean isDeshabilitarIniciador() {
        return deshabilitarIniciador;
    }

    public void setDeshabilitarIniciador(boolean deshabilitarIniciador) {
        this.deshabilitarIniciador = deshabilitarIniciador;
    }

    public List<String> getLstInstLegalSeleccionado() {
        return lstInstLegalSeleccionado;
    }

    public void setLstInstLegalSeleccionado(List<String> lstInstLegalSeleccionado) {
        this.lstInstLegalSeleccionado = lstInstLegalSeleccionado;
    }

    public String getTipoListado() {
        return tipoListado;
    }

    public void setTipoListado(String tipoListado) {
        this.tipoListado = tipoListado;
    }

    public String getTipoFecha() {
        return tipoFecha;
    }

    public void setTipoFecha(String tipoFecha) {
        this.tipoFecha = tipoFecha;
    }

    public String getTipoOrganismo() {
        return tipoOrganismo;
    }

    public void setTipoOrganismo(String tipoOrganismo) {
        this.tipoOrganismo = tipoOrganismo;
    }

    public Expediente getExpedienteGen() {
        return expedienteGen;
    }

    public void setExpedienteGen(Expediente expedienteGen) {
        this.expedienteGen = expedienteGen;
    }

    public Organismo getOrganismoAsoc() {
        return organismoAsoc;
    }

    public void setOrganismoAsoc(Organismo organismoAsoc) {
        this.organismoAsoc = organismoAsoc;
    }

    public String getNroExpedienteAux() {
        return nroExpedienteAux;
    }

    public void setNroExpedienteAux(String nroExpedienteAux) {
        this.nroExpedienteAux = nroExpedienteAux;
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

    public Long getCodBsq() {
        return codBsq;
    }

    public void setCodBsq(Long codBsq) {
        this.codBsq = codBsq;
    }

    public Long getNroBsq() {
        return nroBsq;
    }

    public void setNroBsq(Long nroBsq) {
        this.nroBsq = nroBsq;
    }

    public Integer getAnnioBsq() {
        return annioBsq;
    }

    public void setAnnioBsq(Integer annioBsq) {
        this.annioBsq = annioBsq;
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

    public Date getFechaDesdeUltimoMovBsq() {
        return fechaDesdeUltimoMovBsq;
    }

    public void setFechaDesdeUltimoMovBsq(Date fechaDesdeUltimoMovBsq) {
        this.fechaDesdeUltimoMovBsq = fechaDesdeUltimoMovBsq;
    }

    public Date getFechaHastaUltimoMovBsq() {
        return fechaHastaUltimoMovBsq;
    }

    public void setFechaHastaUltimoMovBsq(Date fechaHastaUltimoMovBsq) {
        this.fechaHastaUltimoMovBsq = fechaHastaUltimoMovBsq;
    }

    public String getAsuntoBsq() {
        return asuntoBsq;
    }

    public void setAsuntoBsq(String asuntoBsq) {
        this.asuntoBsq = asuntoBsq;
    }

    public List<Long> getIdItems() {
        return idItems;
    }

    public void setIdItems(List<Long> idItems) {
        this.idItems = idItems;
    }

    public Integer getPedidoFondoBsq() {
        return pedidoFondoBsq;
    }

    public void setPedidoFondoBsq(Integer pedidoFondoBsq) {
        this.pedidoFondoBsq = pedidoFondoBsq;
    }

    public Integer getPedidoFondoAnioBsq() {
        return pedidoFondoAnioBsq;
    }

    public void setPedidoFondoAnioBsq(Integer pedidoFondoAnioBsq) {
        this.pedidoFondoAnioBsq = pedidoFondoAnioBsq;
    }

    public Integer getOrdenEntregaBsq() {
        return ordenEntregaBsq;
    }

    public void setOrdenEntregaBsq(Integer ordenEntregaBsq) {
        this.ordenEntregaBsq = ordenEntregaBsq;
    }

    public Integer getOrdenEntregaAnioBsq() {
        return ordenEntregaAnioBsq;
    }

    public void setOrdenEntregaAnioBsq(Integer ordenEntregaAnioBsq) {
        this.ordenEntregaAnioBsq = ordenEntregaAnioBsq;
    }

    public String getExpMadreBsq() {
        return expMadreBsq;
    }

    public void setExpMadreBsq(String expMadreBsq) {
        this.expMadreBsq = expMadreBsq;
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

    public Long getCodOrganismoIng() {
        return codOrganismoIng;
    }

    public void setCodOrganismoIng(Long codOrganismoIng) {
        this.codOrganismoIng = codOrganismoIng;
    }

    public Long getNroCorrelIng() {
        return nroCorrelIng;
    }

    public void setNroCorrelIng(Long nroCorrelIng) {
        this.nroCorrelIng = nroCorrelIng;
    }

    public Integer getFojasIng() {
        return fojasIng;
    }

    public void setFojasIng(Integer fojasIng) {
        this.fojasIng = fojasIng;
    }

    public Integer getAnnioIng() {
        return annioIng;
    }

    public void setAnnioIng(Integer annioIng) {
        this.annioIng = annioIng;
    }

    public String getDesarchivarPanel() {
        return desarchivarPanel;
    }

    public void setDesarchivarPanel(String desarchivarPanel) {
        this.desarchivarPanel = desarchivarPanel;
    }

    public String getAceptarPanel() {
        return aceptarPanel;
    }

    public void setAceptarPanel(String aceptarPanel) {
        this.aceptarPanel = aceptarPanel;
    }

    public String getMsgDesarchivar() {
        return msgDesarchivar;
    }

    public void setMsgDesarchivar(String msgDesarchivar) {
        this.msgDesarchivar = msgDesarchivar;
    }

    public String getMsgAceptar() {
        return msgAceptar;
    }

    public void setMsgAceptar(String msgAceptar) {
        this.msgAceptar = msgAceptar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getDescripcionInstrumentoBuscar() {
        return descripcionInstrumentoBuscar;
    }

    public void setDescripcionInstrumentoBuscar(String descripcionInstrumentoBuscar) {
        this.descripcionInstrumentoBuscar = descripcionInstrumentoBuscar;
    }

    public Long getIdTipoInstrumentoLegal() {
        return idTipoInstrumentoLegal;
    }

    public void setIdTipoInstrumentoLegal(Long idTipoInstrumentoLegal) {
        this.idTipoInstrumentoLegal = idTipoInstrumentoLegal;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoInstrumento() {
        return tipoInstrumento;
    }

    public void setTipoInstrumento(String tipoInstrumento) {
        this.tipoInstrumento = tipoInstrumento;
    }

    public int getFojasInst() {
        return fojasInst;
    }

    public void setFojasInst(int fojasInst) {
        this.fojasInst = fojasInst;
    }

    public int getFojasInstOld() {
        return fojasInstOld;
    }

    public void setFojasInstOld(int fojasInstOld) {
        this.fojasInstOld = fojasInstOld;
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

    public String getArchivoOld() {
        return archivoOld;
    }

    public void setArchivoOld(String archivoOld) {
        this.archivoOld = archivoOld;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public boolean isAltaInstrumentoLegal() {
        return altaInstrumentoLegal;
    }

    public void setAltaInstrumentoLegal(boolean altaInstrumentoLegal) {
        this.altaInstrumentoLegal = altaInstrumentoLegal;
    }

    public boolean isDetalleInstrumentoLegal() {
        return detalleInstrumentoLegal;
    }

    public void setDetalleInstrumentoLegal(boolean detalleInstrumentoLegal) {
        this.detalleInstrumentoLegal = detalleInstrumentoLegal;
    }

    public boolean isBajaInstrumentoLegal() {
        return bajaInstrumentoLegal;
    }

    public void setBajaInstrumentoLegal(boolean bajaInstrumentoLegal) {
        this.bajaInstrumentoLegal = bajaInstrumentoLegal;
    }

    public boolean isEditarInstrumentoLegal() {
        return editarInstrumentoLegal;
    }

    public void setEditarInstrumentoLegal(boolean editarInstrumentoLegal) {
        this.editarInstrumentoLegal = editarInstrumentoLegal;
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
     * Se usa en la página de éxito tras generar carátula/listado/historial.
     */
    public StreamedContent getReporteStreamedContent() {
        if (recurso == null || nombreReporte == null) {
            return null;
        }
        try {
            Resource r = null;
            switch (recurso) {
                case "reImprimirCaratula":
                    r = getReImprimirCaratula();
                    break;
                case "imprimirCaratula":
                    r = getImprimirCaratula();
                    break;
                case "imprimirCaratulaAgro":
                    r = getImprimirCaratulaAgro();
                    break;
                case "reporteEnviado":
                    r = getReporteEnviado();
                    break;
                case "listadoReciente":
                    r = getListadoReciente();
                    break;
                case "historial":
                    r = getHistorial();
                    break;
                default:
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
            logger.log(Level.WARNING, "Error al obtener reporte para descarga", e);
            return null;
        }
    }

    /**
     * StreamedContent para exportar la lista actual de expedientes a Excel (XLSX).
     * Carga todos los registros una sola vez en servidor y evita el bucle del DataExporter con LazyDataModel.
     */
    public StreamedContent getExportacionExpedientesExcelStream() {
        return DefaultStreamedContent.builder()
                .stream(this::buildExportacionExpedientesExcelStream)
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .name("resultados.xlsx")
                .build();
    }

    private ByteArrayInputStream buildExportacionExpedientesExcelStream() {
        // Usar la lista precargada en prepararExportacionExpedientes(); si no hay (p. ej. segunda petición), cargar ahora.
        List<Expediente> lista = listaExpedientesParaExportar != null
                ? listaExpedientesParaExportar
                : getExpedientesParaExportar();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.forLanguageTag("es-AR"));
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Expedientes");
            int rowNum = 0;
            // Encabezados (mismas columnas que la tabla, sin la de checkbox)
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Nº", "Fecha Creación", "Fecha Último Movimiento", "Iniciador", "Asunto"};
            int col = 0;
            for (String h : headers) {
                headerRow.createCell(col++).setCellValue(h);
            }
            if (isPerteneceContaduria()) {
                headerRow.createCell(col++).setCellValue("Orden de Pago");
                headerRow.createCell(col++).setCellValue("Pedido Fondo");
                headerRow.createCell(col++).setCellValue("Orden Entrega");
            }
            if (flagBandeja == 2 || flagBandeja == 3 || flagBandeja == 5 || flagBandeja == 6) {
                headerRow.createCell(col++).setCellValue("Origen");
            }
            if (flagBandeja == 1 || flagBandeja == 4 || flagBandeja == 6) {
                headerRow.createCell(col++).setCellValue("Destino");
            }
            headerRow.createCell(col).setCellValue("Fojas");
            // Datos
            for (Expediente item : lista) {
                Row dataRow = sheet.createRow(rowNum++);
                int c = 0;
                dataRow.createCell(c++).setCellValue(item.getNroExp() != null ? item.getNroExp() : "");
                dataRow.createCell(c++).setCellValue(item.getFechaCreacion() != null ? sdf.format(item.getFechaCreacion()) : "");
                dataRow.createCell(c++).setCellValue(item.getUltimoMovimiento() != null ? sdf.format(item.getUltimoMovimiento()) : "");
                dataRow.createCell(c++).setCellValue(item.getIniciador() != null ? arreglarIniciador(item.getIniciador().getNombre()) : "");
                dataRow.createCell(c++).setCellValue(item.getAsunto() != null ? item.getAsunto() : "");
                if (isPerteneceContaduria()) {
                    dataRow.createCell(c++).setCellValue(item.getOrdenPago() != null ? item.getOrdenPago() : "-");
                    dataRow.createCell(c++).setCellValue(item.getPedidoFondo() != null && item.getPedidoFondoAnio() != null
                            ? item.getPedidoFondo().toString().concat("/").concat(item.getPedidoFondoAnio().toString()) : "-");
                    dataRow.createCell(c++).setCellValue(item.getOrdenEntrega() != null && item.getOrdenEntregaAnio() != null
                            ? item.getOrdenEntrega().toString().concat("/").concat(item.getOrdenEntregaAnio().toString()) : "-");
                }
                if (flagBandeja == 2 || flagBandeja == 3 || flagBandeja == 5 || flagBandeja == 6) {
                    dataRow.createCell(c++).setCellValue(item.getMesaOrigen() != null ? filtrarMesa(item.getMesaOrigen()) : "");
                }
                if (flagBandeja == 1 || flagBandeja == 4 || flagBandeja == 6) {
                    dataRow.createCell(c++).setCellValue(item.getMesaDestino() != null ? filtrarMesa(item.getMesaDestino()) : "");
                }
                dataRow.createCell(c).setCellValue(item.getFojas());
            }
            wb.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al generar Excel de expedientes", e);
            return new ByteArrayInputStream(new byte[0]);
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

    public boolean isIngresarSiempre() {
        return ingresarSiempre;
    }

    public void setIngresarSiempre(boolean ingresarSiempre) {
        this.ingresarSiempre = ingresarSiempre;
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
                        case "nuevoExpedienteInterno":
                            this.setAlta(true);
                            break;
                        case "nuevoExpedienteExterno":
                            this.setExpedienteExterno(true);
                            break;
                        case "editarExpediente":
                            this.setModificacion(true);
                            break;
                        case "aceptarExpediente":
                            this.setAceptar(true);
                            break;
                        case "aceptarReenviarExpediente":
                            this.setAceptarReenviar(true);
                            break;
                        case "rechazarExpediente":
                            this.setRechazar(true);
                            break;
                        case "reenviarExpediente":
                            this.setReenviar(true);
                            break;
                        case "archivarExpediente":
                            this.setArchivar(true);
                            break;
                        case "enviarExpediente":
                            this.setEnviar(true);
                            break;
                        case "agregarInstLegal":
                            this.setAgregarInstrumento(true);
                            break;
                        case "desvincularInstLegal":
                            this.setDesvincularInstrumento(true);
                            break;
                        case "verReporte":
                            this.setReporte(true);
                            break;
                        case "verNumeroExpediente":
                            this.setUltimoNro(true);
                            break;
                        case "desarchivarExpediente":
                            this.setDesarchivar(true);
                            break;
                        case "ingresarExpediente":
                            this.setIngresar(true);
                            break;
                        case "listado":
                            this.setListado(true);
                            break;
                        case "eliminarExpediente":
                            this.setBaja(true);
                            break;
                        case "perteneceEntSalAgro":
                            // La mesa de entrada del Agro al generar expediente selecciona organismo
                            this.setPerteneceEntSalAgro(true);
                            break;
                        case "perteneceContaduria":
                            // Contaduría tiene campos extras para generar expedientes: pedido fondo, orden de entrega, expediente superior
                            this.setPerteneceContaduria(true);
                            break;
                        case "perteneceContencion":
                            // Cualquier otro usuario del sistema no debe saber en que parte de contención está el expediente
                            this.setPerteneceContencion(true);
                            this.setPerteneceLegal(true);
                            break;
                        case "perteneceCooperativa":
                            // Para ver "Ingresar Expe de Coop"
                            this.setPerteneceCooperativa(true);
                            break;
                        case "perteneceSaludPublica":
                            // Cuando genera expediente no ven el mensaje en orden de pago
                            this.setPerteneceSaludPublica(true);
                            this.quitarTiempo(this.getHoraEnvio());
                            break;
                        case "nuevoInstrumentoLegal":
                            this.setAltaInstrumentoLegal(true);
                            break;
                        case "borrarInstrumentoLegal":
                            this.setBajaInstrumentoLegal(true);
                            break;
                        case "detalleInstrumentoLegal":
                            this.setDetalleInstrumentoLegal(true);
                            break;
                        case "editarInstrumentoLegal":
                            this.setEditarInstrumentoLegal(true);
                            break;
                        case "ingresarSiempre":
                            this.setIngresarSiempre(true);
                            break;

                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario.");
        }
    }

    public String getMensajeUltimoNro() {
        Long nroExp;
        this.mensajeUltimoNro = "El último expediente generado por la mesa es: ";
        try {
            nroExp = this.expedienteFacade.findUltimoNro(this.getOrganismoUsuario().getCodOrganigrama());
            if (this.getMesaUsuario().getId() == 180L) { //Si el usuario pertenece a la mesa de entrada del agro
                Long[] exp = findUltimoAgro();
                nroExp = exp[0] + 1;
            }

            this.mensajeUltimoNro = this.mensajeUltimoNro + (nroExp - 1);
        } catch (Exception ex) {
            this.mensajeUltimoNro = "Error al recuperar el último expediente.";
        }
        return mensajeUltimoNro;
    }

    public void prepararEtiquetas() {
        // Validar que mesaUsuario no sea null
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
        Long countEnvidas = this.expedienteFacade.countEnvidas(idMesa, 2L);
        this.setTotalEnviado(countEnvidas != null ? countEnvidas.toString() : "0");
        
        //Total Bandeja Recibidas
        Long countRecibidas = this.expedienteFacade.countRecibidas(idMesa, 2L);
        this.setTotalRecibido(countRecibidas != null ? countRecibidas.toString() : "0");
        
        //Total Bandeja Aceptadas
        Long countAceptadas = this.expedienteFacade.countAceptadas(idMesa, 3L);
        this.setTotalAceptado(countAceptadas != null ? countAceptadas.toString() : "0");
        
        //Total Bandeja Archivada
        Long countArchivada = this.expedienteFacade.countArchivada(idMesa, 5L);
        this.setTotalArchivado(countArchivada != null ? countArchivada.toString() : "0");
        
        //Total Bandeja Rechazada
        Long countRechazada = this.expedienteFacade.countRechazada(idMesa, 4L);
        this.setTotalRechazado(countRechazada != null ? countRechazada.toString() : "0");
        
        //Total Bandejas
        Long countNotas = this.expedienteFacade.countNotas(idMesa);
        this.setTotal(countNotas != null ? countNotas.toString() : "0");
    }

    public void update(int idBandeja) {
        // Validar que mesaUsuario no sea null antes de continuar
        if (this.getMesaUsuario() == null || this.getMesaUsuario().getId() == null) {
            logger.log(Level.WARNING, "Mesa de usuario es null en update. No se puede cargar la bandeja.");
            // Inicializar una lista vacía para evitar NPE
            LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                @Override
                public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                    return new ArrayList<>();
                }
            };
            lstExpedientes.setRowCount(0);
            this.setExpedientes(lstExpedientes);
            return;
        }
        
        // Limpiar selecciones al cambiar de bandeja
        expedientesSeleccionados.clear();
        
        switch (idBandeja) {
            case 1: {
                //Bandeja Enviado
                this.setTitulo("Enviados");
                this.setFlagBandeja(1);
                this.setDesarchivarPanel("");
                this.setAceptarPanel("");
                this.setMsgAceptar("");
                this.setMsgDesarchivar("");
                final Long idMesaDestino = null;
                final Long idMesaOrigen = this.getMesaUsuario().getId();
                final Long idEstado = 2L;
                Long totalExpedientes = this.expedienteFacade.countByEstado(idMesaDestino, idMesaOrigen, idEstado);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        List<Expediente> lista = expedienteFacade.findAllByEstado(idMesaDestino, idMesaOrigen, idEstado, first, pageSize);
                        // Sincronizar el estado checked con el Set de seleccionados
                        for (Expediente exp : lista) {
                            org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                            );
                            exp.setChecked(expedientesSeleccionados.contains(clave));
                        }
                        return lista;
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes.intValue());
                this.setExpedientes(lstExpedientes);
                if (this.isModificacion()) {
                    this.setVisibleEditar(true);
                    if (this.isPerteneceContencion()) {
                        this.setVisibleAceptar(true);
                    } else {
                        this.setVisibleAceptar(false);
                    }
                    this.setVisibleRechazar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleAgregarInst(false);
                    this.setVisibleDesvincularInst(false);
                    this.setVisibleReporte(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleReporteEnviados(true);
                    this.setVisibleReporteRecibidos(false);
                    this.setVisibleReporteAceptados(false);
                    this.setVisibleReporteRechazados(false);
                    this.setVisibleReporteArchivados(false);
                    this.setVisibleReporteTodos(false);
                }
                this.setVisibleAceptarReenviar(false);
                break;
            }
            case 2: {
                //Bandeja Recibidas
                this.setTitulo("Recibidos");
                this.setFlagBandeja(2);
                this.setDesarchivarPanel("");
                this.setAceptarPanel("");
                this.setMsgAceptar("");
                this.setMsgDesarchivar("");
                final Long idMesaDestino = this.getMesaUsuario().getId();
                final Long idMesaOrigen = null;
                final Long idEstado = 2L;
                Long totalExpedientes = this.expedienteFacade.countByEstado(idMesaDestino, idMesaOrigen, idEstado);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        List<Expediente> lista = expedienteFacade.findAllByEstado(idMesaDestino, idMesaOrigen, idEstado, first, pageSize);
                        // Sincronizar el estado checked con el Set de seleccionados
                        for (Expediente exp : lista) {
                            org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                            );
                            exp.setChecked(expedientesSeleccionados.contains(clave));
                        }
                        return lista;
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes.intValue());
                this.setExpedientes(lstExpedientes);
                if (this.isAceptar()) {
                    this.setVisibleAceptar(true);
                    this.setVisibleEditar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleAgregarInst(false);
                    this.setVisibleDesvincularInst(false);
                    this.setVisibleReporte(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleReporteEnviados(false);
                    this.setVisibleReporteRecibidos(true);
                    this.setVisibleReporteAceptados(false);
                    this.setVisibleReporteRechazados(false);
                    this.setVisibleReporteArchivados(false);
                    this.setVisibleReporteTodos(false);
                }
                if (this.isRechazar()) {
                    this.setVisibleRechazar(true);
                    this.setVisibleEditar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleAgregarInst(false);
                    this.setVisibleDesvincularInst(false);
                    this.setVisibleReporte(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleReporteEnviados(false);
                    this.setVisibleReporteRecibidos(true);
                    this.setVisibleReporteAceptados(false);
                    this.setVisibleReporteRechazados(false);
                    this.setVisibleReporteArchivados(false);
                    this.setVisibleReporteTodos(false);
                }
                if (this.isAceptarReenviar()) {
                    this.setVisibleAceptarReenviar(true);
                }
                break;
            }
            case 3: {
                //Bandeja Aceptadas
                this.setTitulo("Aceptados");
                this.setFlagBandeja(3);
                this.setDesarchivarPanel("");
                this.setAceptarPanel("");
                this.setMsgAceptar("");
                this.setMsgDesarchivar("");
                final Long idMesaDestino = this.getMesaUsuario().getId();
                final Long idMesaOrigen = null;
                final Long idEstado = 3L;
                Long totalExpedientes = this.expedienteFacade.countByEstado(idMesaDestino, idMesaOrigen, idEstado);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        List<Expediente> lista = expedienteFacade.findAllByEstado(idMesaDestino, idMesaOrigen, idEstado, first, pageSize);
                        // Sincronizar el estado checked con el Set de seleccionados
                        for (Expediente exp : lista) {
                            org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                            );
                            exp.setChecked(expedientesSeleccionados.contains(clave));
                        }
                        return lista;
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes.intValue());
                this.setExpedientes(lstExpedientes);
                if (this.isArchivar()) {
                    this.setVisibleArchivar(true);
                    // this.setVisibleEditar(true);
                    this.setVisibleRechazar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleAceptar(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleDesvincularInst(true);
                    this.setVisibleReporte(true);
                    this.setVisibleReporteEnviados(false);
                    this.setVisibleReporteRecibidos(false);
                    this.setVisibleReporteAceptados(true);
                    this.setVisibleReporteRechazados(false);
                    this.setVisibleReporteArchivados(false);
                    this.setVisibleReporteTodos(false);
                }
                if (this.isEnviar()) {
                    this.setVisibleEnviar(true);
                    this.setVisibleEditar(true);
                    this.setVisibleRechazar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleAceptar(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleReporteEnviados(false);
                    this.setVisibleReporteRecibidos(false);
                    this.setVisibleReporteAceptados(true);
                    this.setVisibleReporteRechazados(false);
                    this.setVisibleReporteArchivados(false);
                    this.setVisibleReporteTodos(false);
                }
                if (this.isAgregarInstrumento()) {
                    this.setVisibleAgregarInst(true);
                    this.setVisibleEditar(true);
                    this.setVisibleRechazar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleAceptar(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleReporteEnviados(false);
                    this.setVisibleReporteRecibidos(false);
                    this.setVisibleReporteAceptados(true);
                    this.setVisibleReporteRechazados(false);
                    this.setVisibleReporteArchivados(false);
                    this.setVisibleReporteTodos(false);
                }
                if (this.isDesvincularInstrumento()) {
                    this.setVisibleDesvincularInst(true);
                    this.setVisibleEditar(true);
                    this.setVisibleRechazar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleAceptar(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleReporteEnviados(false);
                    this.setVisibleReporteRecibidos(false);
                    this.setVisibleReporteAceptados(true);
                    this.setVisibleReporteRechazados(false);
                    this.setVisibleReporteArchivados(false);
                    this.setVisibleReporteTodos(false);
                }
                if (this.isReporte()) {
                    this.setVisibleReporte(true);
                    this.setVisibleEditar(true);
                    this.setVisibleRechazar(false);
                    this.setVisibleReenviar(false);
                    this.setVisibleAceptar(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleReporteEnviados(false);
                    this.setVisibleReporteRecibidos(false);
                    this.setVisibleReporteAceptados(true);
                    this.setVisibleReporteRechazados(false);
                    this.setVisibleReporteArchivados(false);
                    this.setVisibleReporteTodos(false);
                }
                this.setVisibleAceptarReenviar(false);
                break;
            }
            case 4: {
                //Bandeja Rechazadas
                this.setTitulo("Rechazados");
                this.setFlagBandeja(4);
                this.setDesarchivarPanel("");
                this.setAceptarPanel("");
                this.setMsgAceptar("");
                this.setMsgDesarchivar("");
                final Long idMesaDestino = null;
                final Long idMesaOrigen = this.getMesaUsuario().getId();
                final Long idEstado = 4L;
                Long totalExpedientes = this.expedienteFacade.countByEstado(idMesaDestino, idMesaOrigen, idEstado);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        List<Expediente> lista = expedienteFacade.findAllByEstado(idMesaDestino, idMesaOrigen, idEstado, first, pageSize);
                        // Sincronizar el estado checked con el Set de seleccionados
                        for (Expediente exp : lista) {
                            org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                            );
                            exp.setChecked(expedientesSeleccionados.contains(clave));
                        }
                        return lista;
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes.intValue());
                this.setExpedientes(lstExpedientes);
                if (this.isReenviar()) {
                    this.setVisibleReenviar(true);
                    this.setVisibleEditar(false);
                    this.setVisibleAceptar(false);
                    this.setVisibleRechazar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleAgregarInst(false);
                    this.setVisibleDesvincularInst(false);
                    this.setVisibleReporte(false);
                    this.setVisibleDesarchivar(false);
                    this.setVisibleReporteEnviados(false);
                    this.setVisibleReporteRecibidos(false);
                    this.setVisibleReporteAceptados(false);
                    this.setVisibleReporteRechazados(true);
                    this.setVisibleReporteArchivados(false);
                    this.setVisibleReporteTodos(false);
                }
                this.setVisibleAceptarReenviar(false);
                break;
            }
            case 5: {
                //Bandeja Archivadas
                this.setTitulo("Archivados");
                this.setFlagBandeja(5);
                this.setDesarchivarPanel("");
                this.setAceptarPanel("");
                this.setMsgAceptar("");
                this.setMsgDesarchivar("");
                final Long idMesaDestino = this.getMesaUsuario().getId();
                final Long idMesaOrigen = null;
                final Long idEstado = 5L;
                Long totalExpedientes = this.expedienteFacade.countByEstado(idMesaDestino, idMesaOrigen, idEstado);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        List<Expediente> lista = expedienteFacade.findAllByEstado(idMesaDestino, idMesaOrigen, idEstado, first, pageSize);
                        // Sincronizar el estado checked con el Set de seleccionados
                        for (Expediente exp : lista) {
                            org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                            );
                            exp.setChecked(expedientesSeleccionados.contains(clave));
                        }
                        return lista;
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes.intValue());
                this.setExpedientes(lstExpedientes);
                if (this.isDesarchivar()) {
                    this.setVisibleDesarchivar(true);
                    this.setVisibleReenviar(false);
                    this.setVisibleEditar(false);
                    this.setVisibleAceptar(false);
                    this.setVisibleRechazar(false);
                    this.setVisibleEnviar(false);
                    this.setVisibleArchivar(false);
                    this.setVisibleAgregarInst(false);
                    this.setVisibleDesvincularInst(false);
                    this.setVisibleReporte(false);
                    this.setVisibleReporteEnviados(false);
                    this.setVisibleReporteRecibidos(false);
                    this.setVisibleReporteAceptados(false);
                    this.setVisibleReporteRechazados(false);
                    this.setVisibleReporteArchivados(true);
                    this.setVisibleReporteTodos(false);
                }
                this.setVisibleAceptarReenviar(false);
                break;
            }
            case 6: {
                //Bandeja Todos
                this.setVisibleReenviar(false);
                this.setVisibleEditar(false);
                this.setVisibleAceptar(false);
                this.setVisibleRechazar(false);
                this.setVisibleEnviar(false);
                this.setVisibleArchivar(false);
                this.setVisibleAgregarInst(false);
                this.setVisibleDesvincularInst(false);
                this.setVisibleReporte(false);
                this.setVisibleDesarchivar(false);
                this.setVisibleReporteEnviados(false);
                this.setVisibleReporteRecibidos(false);
                this.setVisibleReporteAceptados(false);
                this.setVisibleReporteRechazados(false);
                this.setVisibleReporteArchivados(false);
                this.setVisibleAceptarReenviar(false);
                this.setVisibleReporteTodos(true);
                this.setTitulo(null);
                this.setFlagBandeja(6);
                this.setDesarchivarPanel("");
                this.setAceptarPanel("");
                this.setMsgAceptar("");
                this.setMsgDesarchivar("");
                // Para "Todos" cargamos todos los expedientes de la mesa con paginación lazy optimizada
                final Long idMesa = this.getMesaUsuario().getId();
                // Contamos todos los expedientes de la mesa (sin filtro de estado)
                Long totalExpedientes = this.expedienteFacade.countNotas(idMesa);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            // Usamos el método optimizado con paginación real en la base de datos
                            List<Expediente> lista = expedienteFacade.findAll(idMesa, first, pageSize);
                            // Sincronizar el estado checked con el Set de seleccionados
                            for (Expediente exp : lista) {
                                org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                    exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                                );
                                exp.setChecked(expedientesSeleccionados.contains(clave));
                            }
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes.intValue());
                this.setExpedientes(lstExpedientes);
                break;
            }
        }
    }

    public List<SelectItem> getSelectItemsIniciador() {
        return selectItemsIniciador;
    }

    public void setSelectItemsIniciador(List<SelectItem> selectItemsIniciador) {
        this.selectItemsIniciador = selectItemsIniciador;
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

    public List<SelectItem> getSelectItemsInstLegal() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<InstrumentoLegal> lstInstLegal = this.instrumentoLegalFacade.findAllSinAgregar(this.getUsuario().getId());
        if (!(lstInstLegal.isEmpty())) {
            lstInstLegal.stream().forEach((instLegalAux) -> {
                selectItems.add(new SelectItem(instLegalAux.getId(), instLegalAux.getNumero() + "-" + instLegalAux.getDescripcion()));
            });
        }
        return selectItems;
    }

    public List<SelectItem> getSelectItemsInstLegalAgregados() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<InstrumentoLegal> lstInstLegal = this.expediente.getListInstrumentosLegales();
        if (!(lstInstLegal.isEmpty())) {
            lstInstLegal.stream().forEach((instLegalAux) -> {
                selectItems.add(new SelectItem(instLegalAux.getId(), instLegalAux.getNumero() + "-" + instLegalAux.getDescripcion()));
            });
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        this.setFechaCreacion(new Date());
        this.setHoraEnvio(quitarTiempo(new Date()));
        this.setCodOrganismo(null);
        this.setNroCorrel(null);
        this.setAnnio(null);
        this.setFojas(1);
        this.setIdIniciador(null);
        this.setIdTipoIniciador(null);
        this.setDeshabilitarIniciador(true);
        this.setIdTipoAsunto(null);
        this.setAsunto(null);
        this.setObservacion(null);
        this.setOrdenPago(null);
        this.setIdMesa(null);
        this.setExpediente(null);
        this.setCodOrganismoAgro(null);
        this.idItems.clear();
        this.setExpedientesAux(null);
        this.setExpedientes(null);
        this.setPedidoFondo(null);
        this.setPedidoFondoAnio(null);
        this.setOrdenEntrega(null);
        this.setOrdenEntregaAnio(null);
        this.setExpMadre(null);
        this.setPedidoFondoBsq(null);
        this.setOrdenEntregaBsq(null);
        this.setExpMadreBsq(null);
        this.setIdMesaOrigenBsq(null);
        this.setIdMesaDestinoBsq(null);
        if (this.getFlagBandeja() == null) {
            this.setFlagBandeja(6);
        }
        this.update(this.getFlagBandeja());
        this.setRecurso(null);
        this.setEtiqueta(null);
        this.setNombreReporte(null);
        this.setIdUsuario(null);

    }

    public void clean() {
        this.setFojas(1);
        this.setIdIniciador(null);
        this.setIdTipoAsunto(null);
        this.setAsunto(null);
        this.setObservacion(null);
        this.setOrdenPago(null);
        this.setIdMesa(null);
        this.lstActionItems = new ArrayList<>();
        this.expedientes = null;
        this.expedientesAux = new ArrayList<>();
    }

    @Override
    public String crear() {
        try {
            Calendar cal = Calendar.getInstance();
            if (this.getFechaCreacion().before(this.getFechaActual())) {
                if (this.getCodOrganismoAgro() != null) {
                    //El agro siempre particular
                    this.setCodOrganismo(this.organismoFacade.find(this.getCodOrganismoAgro()).getCodOrganigrama());
                    this.setNroCorrel(findUltimoAgro()[0] + 1);
                    Date fecha = this.getFechaCreacion();

                    cal.setTime(fecha);
                    int anio = cal.get(Calendar.YEAR);
                    this.setAnnio(anio);
                    this.getFechaCreacion().setHours(cal.get(Calendar.HOUR));
                    this.getFechaCreacion().setMinutes(cal.get(Calendar.MINUTE));
                    this.getFechaCreacion().setSeconds(cal.get(Calendar.SECOND));
                    if (this.getIdIniciador() >= 0) {
                        this.setExpedienteGen(this.expedienteFacade.create(this.getFechaCreacion(),
                                this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio(),
                                this.getCuerpo(), this.getFojas(), this.getIdIniciador(),
                                this.getIdTipoAsunto(), this.getAsunto(), this.getObservacion(),
                                this.getOrdenPago(),
                                this.getPedidoFondo(), this.getPedidoFondoAnio(),
                                this.getOrdenEntrega(), this.getOrdenEntregaAnio(),
                                this.getExpMadre(),
                                this.getMesaUsuario().getId(),
                                this.getIdMesa(), this.getUsuario().getId())
                        );
                        this.setOrganismoAsoc(this.organismoFacade.find(this.getCodOrganismoAgro()));
                        this.setTitle("Proceso Completo");
                        this.setImages("images/symbolChecks.png");
                        this.setRecurso("imprimirCaratulaAgro");
                        this.setEtiqueta("Ver Carátula");
                        this.setNombreReporte(this.nombrar("caratula", "pdf"));
                        this.setResultado("successErrorExpedienteReporte");
                        this.setMsgSuccessError("El expediente " + this.getExpedienteGen().getNroExp() + " ha sido generado con éxito.");
                        this.prepararEtiquetas();
                    } else {
                        throw new Exception("Elija correctamente el iniciador");
                    }

                } else {
                    //Los demás organismos van más tranquis
                    if (this.getIdIniciador() >= 0) {
                        this.getFechaCreacion().setHours(cal.get(Calendar.HOUR));
                        this.getFechaCreacion().setMinutes(cal.get(Calendar.MINUTE));
                        this.getFechaCreacion().setSeconds(cal.get(Calendar.SECOND));
                        if (this.isPerteneceSaludPublica() && this.getOrdenPago().isEmpty()) {
                            this.setOrdenPago(" ");
                        }
                        if ((this.getPedidoFondo() != null && this.getPedidoFondoAnio() != null) || this.getPedidoFondo() == null) {
                            if ((this.getOrdenEntrega() != null && this.getOrdenEntregaAnio() != null) || this.getOrdenEntrega() == null) {
                                this.setExpedienteGen(this.expedienteFacade.create(this.getFechaCreacion(),
                                        this.getCuerpo(), this.getFojas(), this.getIdIniciador(),
                                        this.getIdTipoAsunto(), this.getAsunto(), this.getObservacion(),
                                        this.getOrdenPago(),
                                        this.getPedidoFondo(), this.getPedidoFondoAnio(),
                                        this.getOrdenEntrega(), this.getOrdenEntregaAnio(),
                                        this.getExpMadre(), this.getMesaUsuario().getId(),
                                        this.getIdMesa(), this.getUsuario().getId()));

                                this.setTitle("Proceso Completo");
                                this.setImages("images/symbolChecks.png");
                                this.setRecurso("imprimirCaratula");
                                this.setEtiqueta("Ver Carátula");
                                this.setNombreReporte(this.nombrar("caratula", "pdf"));
                                this.setResultado("successErrorExpedienteReporte");
                                this.setMsgSuccessError("El expediente " + this.getExpedienteGen().getNroExp() + " ha sido generado con éxito.");
                                this.prepararEtiquetas();
                            } else {
                                throw new Exception("Si completa la orden de entrega debe completar el año.");
                            }
                        } else {
                            throw new Exception("Si completa el pedido de fondo debe completar el año.");
                        }
                    } else {
                        throw new Exception("Elija correctamente el iniciador");
                    }

                }
            } else {
                throw new Exception("La fecha ingresada no es correcta.");
            }
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("El expediente no pudo generarse. " + ex.getMessage());
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String crearExterno() {
        try {
            if (this.getFechaCreacion().before(this.getFechaActual())) {

                if (this.getIdIniciador() != 0) {
                    Calendar c = GregorianCalendar.getInstance();
                    this.getFechaCreacion().setHours(c.get(Calendar.HOUR));
                    this.getFechaCreacion().setMinutes(c.get(Calendar.MINUTE));
                    this.getFechaCreacion().setSeconds(c.get(Calendar.SECOND));
                    if ((this.getPedidoFondo() != null && this.getPedidoFondoAnio() != null) || this.getPedidoFondo() == null) {
                        if ((this.getOrdenEntrega() != null && this.getOrdenEntregaAnio() != null) || this.getOrdenEntrega() == null) {
                            this.setExpedienteGen(this.expedienteFacade.create(this.getFechaCreacion(),
                                    this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio(),
                                    this.getCuerpo(), this.getFojas(), this.getIdIniciador(),
                                    this.getIdTipoAsunto(), this.getAsunto(), this.getObservacion(),
                                    this.getOrdenPago(),
                                    this.getPedidoFondo(), this.getPedidoFondoAnio(),
                                    this.getOrdenEntrega(), this.getOrdenEntregaAnio(),
                                    this.getExpMadre(),
                                    this.getMesaUsuario().getId(),
                                    this.getMesaUsuario().getId(), this.getUsuario().getId()));
                            this.setTitle("Proceso Completo");
                            this.setImages("images/symbolChecks.png");
                            this.setResultado("successErrorExpediente");
                            this.setMsgSuccessError("El expediente " + this.getExpedienteGen().getNroExp() + " ha sido generado con éxito.");
                            this.limpiar();
                            this.prepararEtiquetas();
                        } else {
                            throw new Exception("Si completa la orden de entrega debe completar el año.");
                        }
                    } else {
                        throw new Exception("Si completa el pedido de fondo debe completar el año.");
                    }
                } else {
                    throw new Exception("Elija correctamente el iniciador");
                }

            } else {
                throw new Exception("La fecha ingresada no es correcta.");
            }
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("El expediente no pudo generarse.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String prepararParaDetalle() {
        // Usar el Set de expedientes seleccionados en lugar de iterar sobre todos
        int flagCount = expedientesSeleccionados.size();
        logger.log(Level.INFO, "prepararParaDetalle llamado - Total seleccionados: {0}", flagCount);
        
        // Log de todos los elementos seleccionados para depuración
        if (flagCount > 0) {
            StringBuilder sb = new StringBuilder("Expedientes seleccionados: ");
            for (org.ccpm.sysse.utilidad.ExpedienteClave clave : expedientesSeleccionados) {
                sb.append(clave.getCodOrganismo()).append("-")
                  .append(clave.getNumCorrelativo()).append("-")
                  .append(clave.getAnio()).append("; ");
            }
            logger.log(Level.INFO, sb.toString());
        }
        
        if (flagCount == 0) {
            logger.log(Level.WARNING, "No hay expedientes seleccionados");
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        } else if (flagCount == 1) {
            // Obtener el único expediente seleccionado
            org.ccpm.sysse.utilidad.ExpedienteClave clave = expedientesSeleccionados.iterator().next();
            logger.log(Level.INFO, "Buscando expediente: {0}-{1}-{2}", 
                new Object[]{clave.getCodOrganismo(), clave.getNumCorrelativo(), clave.getAnio()});
            
            Expediente expedienteAux = this.expedienteFacade.find(
                clave.getCodOrganismo(),
                clave.getNumCorrelativo(),
                clave.getAnio()
            );
            
            if (expedienteAux != null) {
                logger.log(Level.INFO, "Expediente encontrado, navegando a detalle");
                this.setResultado("detalleExpediente");
                Collections.sort(expedienteAux.getListHistoriales(), new Comparator<HistorialExpediente>() {
                    @Override
                    public int compare(HistorialExpediente o1, HistorialExpediente o2) {
                        return o1.getFecha().compareTo(o2.getFecha());
                    }
                });
                Collections.reverse(expedienteAux.getListHistoriales());
                this.setExpediente(expedienteAux);
            } else {
                logger.log(Level.WARNING, "No se pudo encontrar el expediente en la base de datos");
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("No se pudo encontrar el expediente seleccionado.");
                this.setResultado("successErrorExpediente");
            }
        } else {
            logger.log(Level.WARNING, "Múltiples expedientes seleccionados: {0}", flagCount);
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        }
        logger.log(Level.INFO, "prepararParaDetalle retornando: {0}", this.getResultado());
        return this.getResultado();
    }

    public String prepararParEditar() {
        int flagCount = expedientesSeleccionados.size();
        Expediente expedienteIte = null;
        List<Expediente> expedientesMultiples = new ArrayList<>();
        
        if (flagCount > 0) {
            // Obtener los expedientes seleccionados del Set
            for (org.ccpm.sysse.utilidad.ExpedienteClave clave : expedientesSeleccionados) {
                Expediente expedienteAux = this.expedienteFacade.find(
                    clave.getCodOrganismo(),
                    clave.getNumCorrelativo(),
                    clave.getAnio()
                );
                if (expedienteAux != null) {
                    if (expedienteIte == null) {
                        expedienteIte = expedienteAux;
                    }
                    expedientesMultiples.add(expedienteAux);
                }
            }
        }
        
        if (flagCount > 0) {
            if ((this.getFlagBandeja().equals(1) || this.getFlagBandeja().equals(3)) && !(flagCount > 1)) {
                this.setResultado("editarExpediente");
                Expediente expedienteAux = this.expedienteFacade.find(expedienteIte.getCodOrganismo(),
                        expedienteIte.getNumCorrelativo(), expedienteIte.getAnio());
                this.setExpediente(expedienteAux);
                this.setCodOrganismo(expedienteAux.getCodOrganismo());
                this.setNroCorrel(expedienteAux.getNumCorrelativo());
                this.setAnnio(expedienteAux.getAnio());
                this.setFechaCreacion(expedienteAux.getFechaCreacion());
                this.setCuerpo(expedienteAux.getCuerpo());
                this.setFojas(expedienteAux.getFojas());
                if (expedienteAux.getIniciador() != null) {
                    this.setIdTipoIniciador(expedienteAux.getIniciador().getTipoIniciador().getId());
                    this.setIdIniciador(expedienteAux.getIniciador().getId());
                }
                this.cargarIniciadores(this.getIdTipoIniciador());

                this.setDeshabilitarIniciador(false);
                this.setIdTipoAsunto(expedienteAux.getTipoAsunto().getId());
                this.setAsunto(expedienteAux.getAsunto());
                this.setObservacion(expedienteAux.getObservacion());
                if (expedienteAux.getOrdenPago() != null) {
                    if (expedienteAux.getOrdenPago().equals(" ")) {
                        this.setOrdenPago(null);
                    } else {
                        this.setOrdenPago(expedienteAux.getOrdenPago());
                    }
                }
                this.setPedidoFondo(expedienteAux.getPedidoFondo());
                this.setPedidoFondoAnio(expedienteAux.getPedidoFondoAnio());
                this.setOrdenEntrega(expedienteAux.getOrdenEntrega());
                this.setOrdenEntregaAnio(expedienteAux.getOrdenEntregaAnio());
                this.setExpMadre(expedienteAux.getExpMadre());
                this.setIdMesa(expedienteAux.getMesaDestino().getId());
                this.setNroExpedienteAux(expedienteAux.getNroExp());
            } else if (this.getFlagBandeja().equals(1) && (flagCount > 1)) {
                this.setResultado("editarExpedienteMultiple");
                this.setExpedientesAux(expedientesMultiples);
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorExpediente");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String prepararParaArchivar() {
        int flagCount = expedientesSeleccionados.size();
        Expediente expedienteIte = null;
        
        if (flagCount > 0) {
            // Obtener el expediente seleccionado del Set
            org.ccpm.sysse.utilidad.ExpedienteClave clave = expedientesSeleccionados.iterator().next();
            expedienteIte = this.expedienteFacade.find(
                clave.getCodOrganismo(),
                clave.getNumCorrelativo(),
                clave.getAnio()
            );
        }
        
        if (flagCount > 0) {
            if (!(flagCount > 1)) {
                this.setResultado("archivarExpediente");
                Expediente expedienteAux = this.expedienteFacade.find(expedienteIte.getCodOrganismo(),
                        expedienteIte.getNumCorrelativo(), expedienteIte.getAnio());
                this.setExpediente(expedienteAux);
                this.setCodOrganismo(expedienteAux.getCodOrganismo());
                this.setNroCorrel(expedienteAux.getNumCorrelativo());
                this.setAnnio(expedienteAux.getAnio());
                this.setFechaCreacion(expedienteAux.getFechaCreacion());
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorExpediente");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    @Override
    public String guardarEdicion() {
        try {
            if ((this.getPedidoFondo() != null && this.getPedidoFondoAnio() != null) || this.getPedidoFondo() == null) {
                if ((this.getOrdenEntrega() != null && this.getOrdenEntregaAnio() != null) || this.getOrdenEntrega() == null) {
                    this.expedienteFacade.edit(this.getCodOrganismo(),
                            this.getNroCorrel(), this.getAnnio(), this.getFechaCreacion(),
                            this.getCuerpo(), this.getFojas(), this.getIdIniciador(),
                            this.getIdTipoAsunto(), this.getAsunto(),
                            this.getObservacion(), this.getOrdenPago(),
                            this.getPedidoFondo(), this.getPedidoFondoAnio(),
                            this.getOrdenEntrega(), this.getOrdenEntregaAnio(),
                            this.getExpMadre(),
                            this.getIdMesa(), this.getUsuario().getId());

                    this.setTitle("Proceso Completo");
                    this.setImages("images/symbolChecks.png");
                    this.setResultado("successErrorExpediente");
                    this.setMsgSuccessError("El exediente ha sido editado con éxito.");
                    this.limpiar();
                    this.prepararEtiquetas();
                    this.actualizar();
                } else {
                    throw new Exception("Si completa la orden de entrega debe completar el año.");
                }
            } else {
                throw new Exception("Si completa el pedido de fondo debe completar el año.");
            }
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String guardarEdicionMultiple() {
        try {
            for (Expediente expediente : expedientesAux) {
                this.expedienteFacade.edit(expediente.getCodOrganismo(),
                        expediente.getNumCorrelativo(), expediente.getAnio(), this.getObservacion(),
                        this.getIdMesa(), this.getUsuario().getId());
            }

            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorExpediente");
            this.setMsgSuccessError("Los exedientes ha sido editados con éxito.");
            this.limpiar();
            this.prepararEtiquetas();
            this.actualizar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String aceptarExpediente() {
        int flagCount = expedientesSeleccionados.size();
        
        if (flagCount == 0) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar al menos un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
            return this.getResultado();
        }
        
        // Procesar todos los expedientes seleccionados
        int exitosos = 0;
        int fallidos = 0;
        StringBuilder errores = new StringBuilder();
        
        for (org.ccpm.sysse.utilidad.ExpedienteClave clave : expedientesSeleccionados) {
            try {
                Expediente expteIte = this.expedienteFacade.find(
                    clave.getCodOrganismo(),
                    clave.getNumCorrelativo(),
                    clave.getAnio()
                );
                
                if (expteIte != null) {
                    this.expedienteFacade.aceptarExpediente(
                        expteIte.getCodOrganismo(),
                        expteIte.getNumCorrelativo(), 
                        expteIte.getAnio(), 
                        this.getUsuario().getId(), 
                        this.getFlagBandeja()
                    );
                    exitosos++;
                } else {
                    fallidos++;
                    errores.append("Expediente ").append(clave.getCodOrganismo())
                           .append("-").append(clave.getNumCorrelativo())
                           .append("/").append(clave.getAnio())
                           .append(" no encontrado. ");
                }
            } catch (Exception ex) {
                fallidos++;
                errores.append("Error en expediente ").append(clave.getCodOrganismo())
                       .append("-").append(clave.getNumCorrelativo())
                       .append("/").append(clave.getAnio())
                       .append(": ").append(ex.getMessage()).append(". ");
            }
        }
        
        // Limpiar las selecciones después de procesar
        expedientesSeleccionados.clear();
        
        // Mostrar resultado
        if (fallidos == 0) {
            this.setResultado("expedienteConf");
            this.prepararEtiquetas();
            this.update(this.getFlagBandeja());
        } else {
            this.setTitle("¡Atención!");
            this.setImages("images/symbolWarning.png");
            String mensaje = "Se procesaron " + exitosos + " expediente(s) correctamente. ";
            if (fallidos > 0) {
                mensaje += fallidos + " expediente(s) fallaron: " + errores.toString();
            }
            this.setMsgSuccessError(mensaje);
            this.setResultado("successErrorExpediente");
            this.prepararEtiquetas();
            this.update(this.getFlagBandeja());
        }
        
        return this.getResultado();
    }

    public String prepararParaRechazar() {
        int flagCount = expedientesSeleccionados.size();
        Expediente expeteIte = null;
        
        if (flagCount > 0) {
            // Obtener el expediente seleccionado del Set
            org.ccpm.sysse.utilidad.ExpedienteClave clave = expedientesSeleccionados.iterator().next();
            expeteIte = this.expedienteFacade.find(
                clave.getCodOrganismo(),
                clave.getNumCorrelativo(),
                clave.getAnio()
            );
        }
        
        if (flagCount > 0) {
            if (!(flagCount > 1)) {
                this.setResultado("rechazarExpediente");
                Expediente expedienteAux = this.expedienteFacade.find(expeteIte.getCodOrganismo(),
                        expeteIte.getNumCorrelativo(), expeteIte.getAnio());
                this.setExpediente(expedienteAux);
                this.setCodOrganismo(expedienteAux.getCodOrganismo());
                this.setNroCorrel(expedienteAux.getNumCorrelativo());
                this.setAnnio(expedienteAux.getAnio());
                this.setObservacion(expedienteAux.getObservacion());
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorExpediente");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String prepararParaAceptarReenviar() {
        int flagCount = expedientesSeleccionados.size();
        Expediente expeteIte = null;
        this.setExpedientesAux(new ArrayList<>());
        // Usar un Set para evitar duplicados basado en la clave única
        Set<org.ccpm.sysse.utilidad.ExpedienteClave> clavesAgregadas = new HashSet<>();
        
        if (flagCount > 0) {
            // Obtener los expedientes seleccionados del Set
            for (org.ccpm.sysse.utilidad.ExpedienteClave clave : expedientesSeleccionados) {
                // Verificar que no se haya agregado ya este expediente
                if (!clavesAgregadas.contains(clave)) {
                    Expediente expteAux = this.expedienteFacade.find(
                        clave.getCodOrganismo(),
                        clave.getNumCorrelativo(),
                        clave.getAnio()
                    );
                    if (expteAux != null) {
                        if (expeteIte == null) {
                            expeteIte = expteAux;
                        }
                        this.expedientesAux.add(expteAux);
                        clavesAgregadas.add(clave);
                    }
                }
            }
        }
        
        if (flagCount > 0) {
            if (!(flagCount > 1)) {
                this.setResultado("aceptarReenviarExpediente");
                Expediente expedienteAux = this.expedienteFacade.find(expeteIte.getCodOrganismo(),
                        expeteIte.getNumCorrelativo(), expeteIte.getAnio());
                this.setExpediente(expedienteAux);
                this.setCodOrganismo(expedienteAux.getCodOrganismo());
                this.setNroCorrel(expedienteAux.getNumCorrelativo());
                this.setAnnio(expedienteAux.getAnio());
                this.setObservacion(expedienteAux.getObservacion());
            } else {
                this.setResultado("aceptarReenviarExpedienteMultiple");
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String rechazarExpediente() {
        try {
            this.expedienteFacade.rechazarExpediente(this.getCodOrganismo(),
                    this.getNroCorrel(), this.getAnnio(), this.getObservacion(), this.getUsuario().getId());
            Long mesaID = this.expedienteFacade.find(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio()).getMesaOrigen().getId();
            //this.avisarUsaurios(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio(), "Rechazado", mesaID);
            this.limpiar();
            this.prepararEtiquetas();
            this.update(2);
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorExpediente");
            this.setMsgSuccessError("El expediente ha sido rechazado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String prepararParaReenviar() {
        int flagCount = expedientesSeleccionados.size();
        Expediente expedienteIte = null;
        
        if (flagCount > 0) {
            // Obtener el expediente seleccionado del Set
            org.ccpm.sysse.utilidad.ExpedienteClave clave = expedientesSeleccionados.iterator().next();
            expedienteIte = this.expedienteFacade.find(
                clave.getCodOrganismo(),
                clave.getNumCorrelativo(),
                clave.getAnio()
            );
        }
        
        if (flagCount > 0) {
            if (!(flagCount > 1)) {
                this.setResultado("reenviarExpediente");
                Expediente expedienteAux = this.expedienteFacade.find(expedienteIte.getCodOrganismo(),
                        expedienteIte.getNumCorrelativo(), expedienteIte.getAnio());
                this.setExpediente(expedienteAux);
                this.setFojas(expedienteAux.getFojas());
                this.setCodOrganismo(expedienteAux.getCodOrganismo());
                this.setNroCorrel(expedienteAux.getNumCorrelativo());
                this.setAnnio(expedienteAux.getAnio());
                this.setObservacion(expedienteAux.getObservacion());
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorExpediente");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String reenviarExpediente() {
        try {
            this.expedienteFacade.reenviarExpediente(this.getCodOrganismo(),
                    this.getNroCorrel(), this.getAnnio(), this.getFojas(),
                    this.getObservacion(), this.getMesaUsuario().getId(),
                    this.getIdMesa(), this.getUsuario().getId());
            Long mesaID = this.expedienteFacade.find(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio()).getMesaDestino().getId();
            // this.avisarUsaurios(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio(), "Recibido", mesaID);
            this.limpiar();
            this.prepararEtiquetas();
            this.update(4);
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorExpediente");
            this.setMsgSuccessError("El expediente ha sido enviado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String archivarExpediente() {
        try {
            this.expedienteFacade.archivarExpediente(this.getCodOrganismo(),
                    this.getNroCorrel(), this.getAnnio(), this.getObservacion(),
                    this.getMotivoArchivo(), this.getUsuario().getId());
            this.limpiar();
            this.prepararEtiquetas();
            this.update(3);
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorExpediente");
            this.setMsgSuccessError("El expediente ha sido archivado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String desarchivarExpediente() {
        int flagCount = expedientesSeleccionados.size();
        
        if (flagCount == 0) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar al menos un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
            return this.getResultado();
        }
        
        // Procesar todos los expedientes seleccionados
        int exitosos = 0;
        int fallidos = 0;
        StringBuilder errores = new StringBuilder();
        
        for (org.ccpm.sysse.utilidad.ExpedienteClave clave : expedientesSeleccionados) {
            try {
                Expediente expteIte = this.expedienteFacade.find(
                    clave.getCodOrganismo(),
                    clave.getNumCorrelativo(),
                    clave.getAnio()
                );
                
                if (expteIte != null) {
                    this.expedienteFacade.aceptarExpediente(
                        expteIte.getCodOrganismo(),
                        expteIte.getNumCorrelativo(), 
                        expteIte.getAnio(), 
                        this.getUsuario().getId(), 
                        this.getFlagBandeja()
                    );
                    exitosos++;
                } else {
                    fallidos++;
                    errores.append("Expediente ").append(clave.getCodOrganismo())
                           .append("-").append(clave.getNumCorrelativo())
                           .append("/").append(clave.getAnio())
                           .append(" no encontrado. ");
                }
            } catch (Exception ex) {
                fallidos++;
                errores.append("Error en expediente ").append(clave.getCodOrganismo())
                       .append("-").append(clave.getNumCorrelativo())
                       .append("/").append(clave.getAnio())
                       .append(": ").append(ex.getMessage()).append(". ");
            }
        }
        
        // Limpiar las selecciones después de procesar
        expedientesSeleccionados.clear();
        
        // Mostrar resultado
        if (fallidos == 0) {
            this.setResultado("expedienteConf");
            this.prepararEtiquetas();
            this.update(5);
        } else {
            this.setTitle("¡Atención!");
            this.setImages("images/symbolWarning.png");
            String mensaje = "Se desarchivaron " + exitosos + " expediente(s) correctamente. ";
            if (fallidos > 0) {
                mensaje += fallidos + " expediente(s) fallaron: " + errores.toString();
            }
            this.setMsgSuccessError(mensaje);
            this.setResultado("successErrorExpediente");
            this.prepararEtiquetas();
            this.update(5);
        }
        
        return this.getResultado();
    }

    //@SuppressWarnings("empty-statement")
    public String prepararParaEnviar() {
        int flagCount = expedientesSeleccionados.size();
        Expediente expeteIte = null;
        expedientesAux = new ArrayList<>();
        // Usar un Set para evitar duplicados basado en la clave única
        Set<org.ccpm.sysse.utilidad.ExpedienteClave> clavesAgregadas = new HashSet<>();
        
        if (flagCount > 0) {
            // Obtener los expedientes seleccionados del Set
            for (org.ccpm.sysse.utilidad.ExpedienteClave clave : expedientesSeleccionados) {
                // Verificar que no se haya agregado ya este expediente
                if (!clavesAgregadas.contains(clave)) {
                    Expediente expteAux = this.expedienteFacade.find(
                        clave.getCodOrganismo(),
                        clave.getNumCorrelativo(),
                        clave.getAnio()
                    );
                    if (expteAux != null) {
                        if (expeteIte == null) {
                            expeteIte = expteAux;
                        }
                        expedientesAux.add(expteAux);
                        clavesAgregadas.add(clave);
                    }
                }
            }
        }
        
        if (flagCount > 0) {
            if (this.getMesaUsuario().getId() == 11189L) {
                this.setIdMesa(204L);
            } else {
                this.setIdMesa(null);
            }
            if (!(flagCount > 1)) {
                this.setResultado("enviarExpediente");
                Expediente expedienteAux = this.expedienteFacade.find(expeteIte.getCodOrganismo(),
                        expeteIte.getNumCorrelativo(), expeteIte.getAnio());
                this.setExpediente(expedienteAux);
                this.setCodOrganismo(expedienteAux.getCodOrganismo());
                this.setNroCorrel(expedienteAux.getNumCorrelativo());
                this.setAnnio(expedienteAux.getAnio());
                this.setObservacion(expedienteAux.getObservacion());
                this.setFojas(expedienteAux.getFojas());
                if (expedienteAux.getOrdenPago() != null) {
                    if (expedienteAux.getOrdenPago().equals(" ")) {
                        this.setOrdenPago(null);
                    } else {
                        this.setOrdenPago(expedienteAux.getOrdenPago());
                    }
                }
                this.setPedidoFondo(expedienteAux.getPedidoFondo());
                this.setPedidoFondoAnio(expedienteAux.getPedidoFondoAnio());
                this.setOrdenEntrega(expedienteAux.getOrdenEntrega());
                this.setOrdenEntregaAnio(expedienteAux.getOrdenEntregaAnio());
                this.setExpMadre(expedienteAux.getExpMadre());
            } else {
                this.setResultado("enviarExpedienteMultiple");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar al menos un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String enviarExpediente() {
        try {
            if ((this.getPedidoFondo() != null && this.getPedidoFondoAnio() != null) || this.getPedidoFondo() == null) {
                if ((this.getOrdenEntrega() != null && this.getOrdenEntregaAnio() != null) || this.getOrdenEntrega() == null) {
                    this.expedienteFacade.enviarExpediente(this.getCodOrganismo(),
                            this.getNroCorrel(), this.getAnnio(), this.getFojas(), this.getObservacion(),
                            this.getMesaUsuario().getId(), this.getIdMesa(),
                            this.getOrdenPago(),
                            this.getPedidoFondo(), this.getPedidoFondoAnio(),
                            this.getOrdenEntrega(), this.getOrdenEntregaAnio(),
                            this.getExpMadre(),
                            this.getUsuario().getId());
                    /*Long mesaID = this.expedienteFacade.find(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio()).getMesaDestino().getId();
                     this.avisarUsaurios(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio(), "Recibido", mesaID);*/
                    this.limpiar();
                    this.prepararEtiquetas();
                    this.update(3);
                    this.setTitle("Proceso Completo");
                    this.setImages("images/symbolChecks.png");
                    this.setResultado("successErrorExpediente");
                    this.setMsgSuccessError("El expediente ha sido enviado con éxito.");
                } else {
                    throw new Exception("Si completa la orden de entrega debe completar el año.");
                }
            } else {
                throw new Exception("Si completa el pedido de fondo debe completar el año.");
            }
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String enviarExpedientes() {
        this.setFechaEnvio(new Date());

        for (Expediente expteAux : this.getExpedientesAux()) {
            try {
                this.expedienteFacade.enviarExpediente(expteAux, this.getObservacion(), this.getIdMesa(), this.getUsuario().getId());
            } catch (Exception ex) {

                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError(ex.getMessage());
                this.setResultado("successErrorExpediente");
            }

        }
        this.setTitle("Proceso Completo");
        this.setImages("images/symbolChecks.png");
        this.setMsgSuccessError("Los expedientes han sido enviados con éxito.");
        this.setResultado("successErrorExpedienteReporte");
        this.setRecurso("listadoReciente");
        this.setEtiqueta("Ver Reporte");
        this.setNombreReporte(this.nombrar("listado", "pdf"));
        this.prepararEtiquetas();
        this.update(3);

        return this.getResultado();
    }

    public String aceptarReenviarExpediente() {
        try {
            this.expedienteFacade.aceptarReenviarExpediente(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio(), this.getObservacion(), this.getIdMesa(), this.getUsuario().getId());
            /*Long mesaID = this.expedienteFacade.find(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio()).getMesaDestino().getId();
                     this.avisarUsaurios(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio(), "Recibido", mesaID);*/
            this.limpiar();
            this.prepararEtiquetas();
            this.update(2);
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorExpediente");
            this.setMsgSuccessError("El expediente ha sido aceptado y enviado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public String aceptarReenviarExpedientes() {
        this.setFechaEnvio(new Date());
        for (Expediente expteAux : this.getExpedientesAux()) {
            try {
                this.expedienteFacade.aceptarReenviarExpediente(expteAux.getCodOrganismo(), expteAux.getNumCorrelativo(), expteAux.getAnio(), this.getObservacion(), this.getIdMesa(), this.getUsuario().getId());
            } catch (Exception ex) {

                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError(ex.getMessage());
                this.setResultado("successErrorExpediente");
            }

        }
        this.setTitle("Proceso Completo");
        this.setImages("images/symbolChecks.png");
        this.setMsgSuccessError("Los expedientes han sido aceptados y enviados con éxito.");
        this.setResultado("successErrorExpedienteReporte");
        this.setRecurso("listadoReciente");
        this.setEtiqueta("Ver Reporte");
        this.setNombreReporte(this.nombrar("listado", "pdf"));
        this.prepararEtiquetas();
        this.update(2);

        return this.getResultado();
    }

    public String prepararParaReeImprimir() {
        int flagCount = expedientesSeleccionados.size();
        Expediente expedienteIte = null;
        
        if (flagCount > 0) {
            // Obtener el expediente seleccionado del Set
            org.ccpm.sysse.utilidad.ExpedienteClave clave = expedientesSeleccionados.iterator().next();
            expedienteIte = this.expedienteFacade.find(
                clave.getCodOrganismo(),
                clave.getNumCorrelativo(),
                clave.getAnio()
            );
        }
        
        if (flagCount > 0) {
            if (!(flagCount > 1)) {
                this.setResultado("reeImprimirCaratula");
                Expediente expedienteAux = this.expedienteFacade.find(expedienteIte.getCodOrganismo(),
                        expedienteIte.getNumCorrelativo(), expedienteIte.getAnio());
                this.setExpediente(expedienteAux);
                this.setCodOrganismo(expedienteAux.getCodOrganismo());
                this.setNroCorrel(expedienteAux.getNumCorrelativo());
                this.setAnnio(expedienteAux.getAnio());
                this.setFechaCreacion(expedienteAux.getFechaCreacion());
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorExpediente");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public void rederPanel() {
        if (this.render) {
            this.setRender(false);
        } else {
            this.setRender(true);
        }
    }

    @Override
    public void aplicarFiltro() {
        // Limpiar selecciones al aplicar un nuevo filtro
        expedientesSeleccionados.clear();
        
        switch (this.flagBandeja) {
            case 1: {
                //Bandeja Enviado - estado ENVIADO (ID 2), mesaOrigen = mesaUsuario, mesaDestino = filtro opcional
                final Long idMesaDestino = this.getIdMesaDestinoBsq();
                final Long idMesaOrigen = this.getMesaUsuario().getId();
                Long totalExpedientes = this.expedienteFacade.countEnviadosNative(this.getCodBsq(),
                        this.getNroBsq(), this.getAnnioBsq(),
                        this.getFechaDesdeBsq(), this.getFechaHastaBsq(),
                        this.getFechaDesdeUltimoMovBsq(), this.getFechaHastaUltimoMovBsq(),
                        this.getIdIniciadorBsq(),
                        this.getIdTipoAsuntoBsq(), desacentuar(this.getAsuntoBsq()), desacentuar(this.getObservacion()),
                        this.getPedidoFondoBsq(), this.getPedidoFondoAnioBsq(),
                        this.getOrdenEntregaBsq(), this.getOrdenEntregaAnioBsq(),
                        desacentuar(this.getExpMadreBsq()), 
                        idMesaDestino, idMesaOrigen);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Expediente> lista = expedienteFacade.findEnviadosNative(
                                    getCodBsq(), getNroBsq(), getAnnioBsq(),
                                    getFechaDesdeBsq(), getFechaHastaBsq(),
                                    getFechaDesdeUltimoMovBsq(), getFechaHastaUltimoMovBsq(),
                                    getIdIniciadorBsq(), getIdTipoAsuntoBsq(),
                                    desacentuar(getAsuntoBsq()), desacentuar(getObservacion()),
                                    getPedidoFondoBsq(), getPedidoFondoAnioBsq(),
                                    getOrdenEntregaBsq(), getOrdenEntregaAnioBsq(),
                                    desacentuar(getExpMadreBsq()),
                                    idMesaDestino, idMesaOrigen, first, pageSize);
                            for (Expediente exp : lista) {
                                org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                    exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                                );
                                exp.setChecked(expedientesSeleccionados.contains(clave));
                            }
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes != null ? totalExpedientes.intValue() : 0);
                this.setExpedientes(lstExpedientes);
                break;
            }
            case 2: {
                //Bandeja Recibidos - estado ENVIADO (ID 2), mesaDestino = mesaUsuario, mesaOrigen = filtro opcional
                final Long idMesaDestino = this.getMesaUsuario().getId();
                final Long idMesaOrigen = this.getIdMesaOrigenBsq();
                Long totalExpedientes = this.expedienteFacade.countRecibidosNative(this.getCodBsq(),
                        this.getNroBsq(), this.getAnnioBsq(),
                        this.getFechaDesdeBsq(), this.getFechaHastaBsq(),
                        this.getFechaDesdeUltimoMovBsq(), this.getFechaHastaUltimoMovBsq(),
                        this.getIdIniciadorBsq(), this.getIdTipoAsuntoBsq(),
                        desacentuar(this.getAsuntoBsq()), desacentuar(this.getObservacion()),
                        this.getPedidoFondoBsq(), this.getPedidoFondoAnioBsq(),
                        this.getOrdenEntregaBsq(), this.getOrdenEntregaAnioBsq(),
                        desacentuar(this.getExpMadreBsq()), 
                        idMesaDestino, idMesaOrigen);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Expediente> lista = expedienteFacade.findRecibidosNative(
                                    getCodBsq(), getNroBsq(), getAnnioBsq(),
                                    getFechaDesdeBsq(), getFechaHastaBsq(),
                                    getFechaDesdeUltimoMovBsq(), getFechaHastaUltimoMovBsq(),
                                    getIdIniciadorBsq(), getIdTipoAsuntoBsq(),
                                    desacentuar(getAsuntoBsq()), desacentuar(getObservacion()),
                                    getPedidoFondoBsq(), getPedidoFondoAnioBsq(),
                                    getOrdenEntregaBsq(), getOrdenEntregaAnioBsq(),
                                    desacentuar(getExpMadreBsq()),
                                    idMesaDestino, idMesaOrigen, first, pageSize);
                            for (Expediente exp : lista) {
                                org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                    exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                                );
                                exp.setChecked(expedientesSeleccionados.contains(clave));
                            }
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes != null ? totalExpedientes.intValue() : 0);
                this.setExpedientes(lstExpedientes);
                break;
            }
            case 3: {
                //Bandeja Aceptadas - estado ACEPTADO (ID 3), mesaDestino = mesaUsuario, mesaOrigen = filtro opcional
                final Long idMesaDestino = this.getMesaUsuario().getId();
                final Long idMesaOrigen = this.getIdMesaOrigenBsq();
                Long totalExpedientes = this.expedienteFacade.countAceptadosNative(this.getCodBsq(),
                        this.getNroBsq(), this.getAnnioBsq(),
                        this.getFechaDesdeBsq(), this.getFechaHastaBsq(),
                        this.getFechaDesdeUltimoMovBsq(), this.getFechaHastaUltimoMovBsq(),
                        this.getIdIniciadorBsq(), this.getIdTipoAsuntoBsq(),
                        desacentuar(this.getAsuntoBsq()),
                        desacentuar(this.getObservacion()),
                        this.getPedidoFondoBsq(), this.getPedidoFondoAnioBsq(),
                        this.getOrdenEntregaBsq(), this.getOrdenEntregaAnioBsq(),
                        desacentuar(this.getExpMadreBsq()), 
                        idMesaDestino, idMesaOrigen);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Expediente> lista = expedienteFacade.findAceptadosNative(
                                    getCodBsq(), getNroBsq(), getAnnioBsq(),
                                    getFechaDesdeBsq(), getFechaHastaBsq(),
                                    getFechaDesdeUltimoMovBsq(), getFechaHastaUltimoMovBsq(),
                                    getIdIniciadorBsq(), getIdTipoAsuntoBsq(),
                                    desacentuar(getAsuntoBsq()), desacentuar(getObservacion()),
                                    getPedidoFondoBsq(), getPedidoFondoAnioBsq(),
                                    getOrdenEntregaBsq(), getOrdenEntregaAnioBsq(),
                                    desacentuar(getExpMadreBsq()),
                                    idMesaDestino, idMesaOrigen, first, pageSize);
                            for (Expediente exp : lista) {
                                org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                    exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                                );
                                exp.setChecked(expedientesSeleccionados.contains(clave));
                            }
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes != null ? totalExpedientes.intValue() : 0);
                this.setExpedientes(lstExpedientes);
                break;
            }
            case 4: {
                //Bandeja Rechazadas - estado RECHAZADO (ID 4), mesaOrigen = mesaUsuario, mesaDestino = filtro opcional
                final Long idMesaDestino = this.getIdMesaDestinoBsq();
                final Long idMesaOrigen = this.getMesaUsuario().getId();
                Long totalExpedientes = this.expedienteFacade.countRechazadosNative(this.getCodBsq(),
                        this.getNroBsq(), this.getAnnioBsq(),
                        this.getFechaDesdeBsq(), this.getFechaHastaBsq(),
                        this.getFechaDesdeUltimoMovBsq(), this.getFechaHastaUltimoMovBsq(),
                        this.getIdIniciadorBsq(),
                        this.getIdTipoAsuntoBsq(),
                        desacentuar(this.getAsuntoBsq()), desacentuar(this.getObservacion()),
                        this.getPedidoFondoBsq(), this.getPedidoFondoAnioBsq(),
                        this.getOrdenEntregaBsq(), this.getOrdenEntregaAnioBsq(),
                        desacentuar(this.getExpMadreBsq()), 
                        idMesaDestino, idMesaOrigen);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Expediente> lista = expedienteFacade.findRechazadosNative(
                                    getCodBsq(), getNroBsq(), getAnnioBsq(),
                                    getFechaDesdeBsq(), getFechaHastaBsq(),
                                    getFechaDesdeUltimoMovBsq(), getFechaHastaUltimoMovBsq(),
                                    getIdIniciadorBsq(), getIdTipoAsuntoBsq(),
                                    desacentuar(getAsuntoBsq()), desacentuar(getObservacion()),
                                    getPedidoFondoBsq(), getPedidoFondoAnioBsq(),
                                    getOrdenEntregaBsq(), getOrdenEntregaAnioBsq(),
                                    desacentuar(getExpMadreBsq()),
                                    idMesaDestino, idMesaOrigen, first, pageSize);
                            for (Expediente exp : lista) {
                                org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                    exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                                );
                                exp.setChecked(expedientesSeleccionados.contains(clave));
                            }
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes != null ? totalExpedientes.intValue() : 0);
                this.setExpedientes(lstExpedientes);
                break;
            }
            case 5: {
                //Bandeja Archivadas - estado ARCHIVADO (ID 5), mesaOrigen = mesaUsuario, mesaDestino = filtro opcional
                final Long idMesaDestino = null; // No aplica para archivados
                final Long idMesaOrigen = this.getMesaUsuario().getId();
                Long totalExpedientes = this.expedienteFacade.countArchivadosNative(this.getCodBsq(),
                        this.getNroBsq(), this.getAnnioBsq(),
                        this.getFechaDesdeBsq(), this.getFechaHastaBsq(),
                        this.getFechaDesdeUltimoMovBsq(), this.getFechaHastaUltimoMovBsq(),
                        this.getIdIniciadorBsq(),
                        this.getIdTipoAsuntoBsq(),
                        desacentuar(this.getAsuntoBsq()), desacentuar(this.getObservacion()),
                        this.getPedidoFondoBsq(), this.getPedidoFondoAnioBsq(),
                        this.getOrdenEntregaBsq(), this.getOrdenEntregaAnioBsq(),
                        desacentuar(this.getExpMadreBsq()), 
                        idMesaDestino, idMesaOrigen);
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            List<Expediente> lista = expedienteFacade.findArchivadosNative(
                                    getCodBsq(), getNroBsq(), getAnnioBsq(),
                                    getFechaDesdeBsq(), getFechaHastaBsq(),
                                    getFechaDesdeUltimoMovBsq(), getFechaHastaUltimoMovBsq(),
                                    getIdIniciadorBsq(), getIdTipoAsuntoBsq(),
                                    desacentuar(getAsuntoBsq()), desacentuar(getObservacion()),
                                    getPedidoFondoBsq(), getPedidoFondoAnioBsq(),
                                    getOrdenEntregaBsq(), getOrdenEntregaAnioBsq(),
                                    desacentuar(getExpMadreBsq()),
                                    idMesaDestino, idMesaOrigen, first, pageSize);
                            for (Expediente exp : lista) {
                                org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                    exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                                );
                                exp.setChecked(expedientesSeleccionados.contains(clave));
                            }
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes != null ? totalExpedientes.intValue() : 0);
                this.setExpedientes(lstExpedientes);
                break;
            }
            case 6: {
                //Bandeja Todos
                // Usar el método con paginación que acepta idMesaUsuario y todos los parámetros de búsqueda
                final Long idMesa = this.getMesaUsuario().getId();
                // Contar el total de expedientes con los filtros aplicados
                Long totalExpedientes = this.expedienteFacade.countAll(this.getCodBsq(),
                        this.getNroBsq(), this.getAnnioBsq(),
                        this.getFechaDesdeBsq(), this.getFechaHastaBsq(),
                        this.getFechaDesdeUltimoMovBsq(), this.getFechaHastaUltimoMovBsq(),
                        this.getIdIniciadorBsq(),
                        this.getIdMesaOrigenBsq(), this.getIdMesaDestinoBsq(),
                        this.getIdTipoAsuntoBsq(),
                        desacentuar(this.getAsuntoBsq()),
                        null, // ordenPago
                        this.getPedidoFondoBsq(), this.getPedidoFondoAnioBsq(),
                        this.getOrdenEntregaBsq(), this.getOrdenEntregaAnioBsq(),
                        desacentuar(this.getExpMadreBsq()),
                        idMesa, // idMesaUsuario - para filtrar por todos los estados de la mesa
                        false, // consultarHistorial
                        desacentuar(this.getObservacion()));
                LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {
                    @Override
                    public List<Expediente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filters) {
                        try {
                            // Usar el método con paginación que acepta idMesaUsuario y todos los parámetros de búsqueda
                            List<Expediente> lista = expedienteFacade.findAll(
                                    getCodBsq(),
                                    getNroBsq(), getAnnioBsq(),
                                    getFechaDesdeBsq(), getFechaHastaBsq(),
                                    getFechaDesdeUltimoMovBsq(), getFechaHastaUltimoMovBsq(),
                                    getIdIniciadorBsq(),
                                    getIdMesaOrigenBsq(), getIdMesaDestinoBsq(),
                                    getIdTipoAsuntoBsq(),
                                    desacentuar(getAsuntoBsq()),
                                    null, // ordenPago
                                    getPedidoFondoBsq(), getPedidoFondoAnioBsq(),
                                    getOrdenEntregaBsq(), getOrdenEntregaAnioBsq(),
                                    desacentuar(getExpMadreBsq()),
                                    idMesa, // idMesaUsuario - para filtrar por todos los estados de la mesa
                                    false, // consultarHistorial
                                    desacentuar(getObservacion()),
                                    first, pageSize);
                            // Sincronizar el estado checked con el Set de seleccionados
                            for (Expediente exp : lista) {
                                org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                                    exp.getCodOrganismo(), exp.getNumCorrelativo(), exp.getAnio()
                                );
                                exp.setChecked(expedientesSeleccionados.contains(clave));
                            }
                            return lista;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                };
                lstExpedientes.setRowCount(totalExpedientes != null ? totalExpedientes.intValue() : 0);
                this.setExpedientes(lstExpedientes);
                break;
            }
        }
    }

    @Override
    public void actualizar() {
        this.setCodBsq(null);
        this.setNroBsq(null);
        this.setAnnioBsq(null);
        this.setFechaDesdeBsq(null);
        this.setFechaHastaBsq(null);
        this.setFechaDesdeUltimoMovBsq(null);
        this.setFechaHastaUltimoMovBsq(null);
        this.setIdTipoIniciadorBsq(null);
        this.setIdIniciadorBsq(null);
        this.setDeshabilitarIniciador(true);
        this.setIdMesaOrigenBsq(null);
        this.setIdMesaDestinoBsq(null);
        this.setIdTipoAsuntoBsq(null);
        this.setAsuntoBsq("");
        this.setPedidoFondoBsq(null);
        this.setPedidoFondoAnioBsq(null);
        this.setOrdenEntregaBsq(null);
        this.setOrdenEntregaAnioBsq(null);
        this.setExpMadreBsq(null);
        this.setExpedientes(null);
        this.prepararEtiquetas();
        switch (this.flagBandeja) {
            case 1: {
                //Bandeja Enviado
                this.setExpedientesList(this.expedienteFacade.findAllByEstado(null, this.getMesaUsuario().getId(), 2L));
                break;
            }
            case 2: {
                //Bandeja Recibidos
                this.setExpedientesList(this.expedienteFacade.findAllByEstado(this.getMesaUsuario().getId(), null, 2L));
                break;
            }
            case 3: {
                //Bandeja Aceptadas                
                this.setExpedientesList(this.expedienteFacade.findAllByEstado(this.getMesaUsuario().getId(), null, 3L));
                break;
            }
            case 4: {
                //Bandeja Rechazadas                
                this.setExpedientesList(this.expedienteFacade.findAllByEstado(null, this.getMesaUsuario().getId(), 4L));
                break;
            }
            case 5: {
                //Bandeja Archivadas                
                this.setExpedientesList(this.expedienteFacade.findAllByEstado(this.getMesaUsuario().getId(), null, 5L));
                break;
            }
            case 6: {
                //Bandeja Todos                
                this.setExpedientesList(this.expedienteFacade.findAll(this.getMesaUsuario().getId()));
                break;
            }
        }
    }

    public String generarCaratula() {
        try {
            this.setExpedienteGen(this.expedienteFacade.find(this.getCodOrganismo(),
                    this.getNroCorrel(), this.getAnnio()));
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setRecurso("imprimirCaratula");
            this.setEtiqueta("Ver Carátula");
            this.setNombreReporte(this.nombrar("caratula", "pdf"));
            this.setResultado("successErrorExpedienteReporte");
            this.setMsgSuccessError("El expediente ha sido generado con éxito.");
            this.prepararEtiquetas();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    private Organismo getOrganismoPadre(Organismo org) {
        return org.getOrganismoPadre();
    }

    public String getNombreOrganismoPadre() {
        String nombreOrganismoPadre = null;
        Organismo organismoPadre = this.getOrganismoUsuario().getOrganismoPadre();
        nombreOrganismoPadre = organismoPadre.getNombre();
        boolean b = true;
        while (b) {
            organismoPadre = this.getOrganismoPadre(organismoPadre);
            if (organismoPadre.getNombre().equalsIgnoreCase(nombreOrganismoPadre)) {
                b = false;
            } else {
                nombreOrganismoPadre = organismoPadre.getNombre();
            }
        }
        return nombreOrganismoPadre;
    }

    public Resource getImprimirCaratulaAgro() throws JRException, FileNotFoundException, IOException {
        Resource miRecurso = null;
        String nombreOrganismo = "A";
        String nombreOrganismoPadre = "A";
        Organismo organismoPadre = this.getOrganismoAsoc();

        nombreOrganismo = this.getOrganismoAsoc().getNombre();
        nombreOrganismoPadre = this.getOrganismoAsoc().getNombre();
        boolean b = true;
        while (b) {
            if (organismoPadre.getOrganismoPadre() == null) {
                b = false;
            } else {
                organismoPadre = this.getOrganismoPadre(organismoPadre);
                nombreOrganismoPadre = organismoPadre.getNombre();
            }
        }

        if (nombreOrganismo.equalsIgnoreCase(nombreOrganismoPadre)) {
            nombreOrganismo = "UNIDAD SUPERIOR";
        }

        try {
            JasperPrint jasperResultado = new ExpedienteReport().imprimirCaratula(this.getExpedienteGen().getCodOrganismo(),
                    this.getExpedienteGen().getNumCorrelativo(), this.getExpedienteGen().getAnio(),
                    nombreOrganismoPadre, nombreOrganismo, this.getCuerpo(),
                    this.ingresarIniciador(this.getExpedienteGen().getIniciador().getId()));
            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            miRecurso = new ExpResource(bites);
        } catch (Exception ex) {
        }
        return miRecurso;
    }

    public Resource getImprimirCaratula() throws JRException, FileNotFoundException, IOException {
        Resource miRecurso = null;
        String nombreOrganismo = "A";
        String nombreOrganismoPadre = "A";

        if ((this.getOrganismoUsuario().getOrganismoPadre() == null)) {
            nombreOrganismoPadre = this.getOrganismoUsuario().getNombre().toUpperCase();
            nombreOrganismo = "";
        } else {
            nombreOrganismo = this.getOrganismoUsuario().getNombre().toUpperCase();
            Organismo aux = this.getOrganismoUsuario().getOrganismoPadre();
            while (aux.getOrganismoPadre() != null) {

                aux = aux.getOrganismoPadre();
            }

            nombreOrganismoPadre = aux.getNombre();

        }

        try {

            JasperPrint jasperResultado = new ExpedienteReport().imprimirCaratula(this.getExpedienteGen().getCodOrganismo(),
                    this.getExpedienteGen().getNumCorrelativo(), this.getExpedienteGen().getAnio(),
                    nombreOrganismoPadre, nombreOrganismo, this.getCuerpo(),
                    this.ingresarIniciador(this.getExpedienteGen().getIniciador().getId()));
            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            miRecurso = new ExpResource(bites);
        } catch (Exception ex) {
        }
        return miRecurso;
    }

    public String generarCaratulaRe() {

        try {
            this.setExpedienteGen(this.expedienteFacade.find(this.getExpediente().getCodOrganismo(),
                    this.getExpediente().getNumCorrelativo(), this.getExpediente().getAnio()));
            if (this.isPerteneceEntSalAgro()) {
                if (this.organismoFacade.findByCodigo(this.getExpediente().getCodOrganismo()).size() > 1) {
                    this.setCodOrganismoIng(this.getExpediente().getCodOrganismo());
                    this.setResultado("elegirOrganismo");
                } else {
                    this.setOrganismoAsoc(this.organismoFacade.findByCodigo(this.getExpediente().getCodOrganismo()).get(0));
                    this.setTitle("Proceso Completo");
                    this.setImages("images/symbolChecks.png");
                    this.setRecurso("imprimirCaratulaAgro");
                    this.setEtiqueta("Ver Carátula");
                    this.setNombreReporte(this.nombrar("caratula", "pdf"));
                    this.setResultado("successErrorExpedienteReporte");
                    this.setMsgSuccessError("La carátula ha sido generada con éxito.");
                    this.prepararEtiquetas();
                }

            } else {
                this.setTitle("Proceso Completo");
                this.setImages("images/symbolChecks.png");
                this.setRecurso("reImprimirCaratula");
                this.setEtiqueta("Ver Carátula");
                this.setNombreReporte(this.nombrar("caratula", "pdf"));
                this.setResultado("successErrorExpedienteReporte");
                this.setMsgSuccessError("La carátula ha sido generada con éxito.");
                this.prepararEtiquetas();
            }
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("No se puede imprimir la carátula.");
            this.setResultado("successErrorExpediente");
            this.limpiar();
        }
        return this.getResultado();
    }

    public String generarCaratulaReAgro() {
        try {
            this.setOrganismoAsoc(this.organismoFacade.find(this.getIdOrganismo()));
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setRecurso("imprimirCaratulaAgro");
            this.setEtiqueta("Ver Carátula");
            this.setNombreReporte(this.nombrar("caratula", "pdf"));
            this.setResultado("successErrorExpedienteReporte");
            this.setMsgSuccessError("La carátula ha sido generada con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setResultado("successErrorExpediente");
        }

        return this.getResultado();
    }

    public Resource getReImprimirCaratula() throws JRException, FileNotFoundException, IOException {
        Resource miRecurso = null;
        String nombreOrganismo = "B";
        String nombreOrganismoPadre = "B";

        if ((this.getOrganismoUsuario().getOrganismoPadre() == null)) {
            nombreOrganismoPadre = this.getOrganismoUsuario().getNombre().toUpperCase();
            nombreOrganismo = "";
        } else {
            nombreOrganismo = this.getOrganismoUsuario().getNombre();
            Organismo aux = this.getOrganismoUsuario().getOrganismoPadre();
            while (aux.getOrganismoPadre() != null) {
                aux = aux.getOrganismoPadre();
            }

            nombreOrganismoPadre = aux.getNombre();

        }
        try {

            JasperPrint jasperResultado = new ExpedienteReport().imprimirCaratula(this.getExpedienteGen().getCodOrganismo(),
                    this.getExpedienteGen().getNumCorrelativo(), this.getExpedienteGen().getAnio(),
                    nombreOrganismoPadre,
                    nombreOrganismo,
                    this.getCuerpo(),
                    this.ingresarIniciador(this.getExpedienteGen().getIniciador().getId()));
            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            miRecurso = new ExpResource(bites);
        } catch (Exception ex) {
        }
        this.cuerpo = null;
        return miRecurso;
    }

    public String verificarReporte() {
        Boolean flag = false;

        if (this.getFechaCreacion() != null || this.getIdMesa() != null) {
            flag = this.expedienteFacade.findAll(this.getFechaCreacion(), this.getHoraEnvio(), this.getMesaUsuario().getId(), this.getIdMesa(), this.getIdUsuario());
        } else { //no ingresó fecha o mesa
            flag = false;
        }

        if (flag) {
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorReporte");
            this.setMsgSuccessError("Reporte generado con éxito.");
            this.setRecurso("reporteEnviado");
            this.setNombreReporte(nombrar("listado", "pdf"));
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("La consulta realizada no genera ningún reporte.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public Resource getReporteEnviado() throws JRException, FileNotFoundException, IOException {
        Resource miRecurso = null;
        try {
            String nombreReporte = "expDiario";
            if (this.isPerteneceContaduria()) {
                nombreReporte = "expFirmaDiario";
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

            if ((this.getOrganismoUsuario().getOrganismoPadre() == null)) {
                nombreOrganismoPadre = this.getOrganismoUsuario().getNombre().toUpperCase();
                nombreOrganismo = "";
            } else {
                nombreOrganismo = this.getOrganismoUsuario().getNombre().toUpperCase();
                Organismo aux = this.getOrganismoUsuario().getOrganismoPadre();
                while (aux.getOrganismoPadre() != null) {
                    aux = aux.getOrganismoPadre();
                }
                nombreOrganismoPadre = aux.getNombre();
            }
            String mesa = this.getMesaUsuario().getDescripcion();
            if (this.getMesaUsuario().isMesaGeneral() && this.getMesaUsuario().getNombre().contains("MESA DE ENTRADAS Y SALIDAS")) {
                mesa = "MESA DE ENTRADAS Y SALIDAS";
            }

            JasperPrint jasperResultado = new ExpedienteReport().imprimirReporte(
                    nombreReporte, nombreOrganismoPadre, nombreOrganismo, mesa, "Enviados", "Destino", 2,
                    this.getFechaDesde(), this.getFechaHasta(), this.getMesaUsuario().getId(), this.getIdMesa(),
                    this.getLeyendaAnual(), "", 0L, this.getIdUsuario());

            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            miRecurso = new ExpResource(bites);
        } catch (Exception ex) {
        }
        this.cuerpo = null;
        return miRecurso;
    }

    public Resource getListadoReciente() throws JRException, FileNotFoundException, IOException {
        Resource miRecurso = null;
        try {
            String nombreReporte = "expRecienEnviados";
            if (this.isPerteneceContaduria()) {
                nombreReporte = "expRecienEnviadosFirma";
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

            if ((this.getOrganismoUsuario().getOrganismoPadre() == null)) {
                nombreOrganismoPadre = this.getOrganismoUsuario().getNombre().toUpperCase();
                nombreOrganismo = "";
            } else {
                nombreOrganismo = this.getOrganismoUsuario().getNombre().toUpperCase();
                Organismo aux = this.getOrganismoUsuario().getOrganismoPadre();
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

            for (Expediente expteAux : this.getExpedientesAux()) {
                if (!primero) {
                    listado = listado.concat(", '" + expteAux.getCodOrganismo().toString() + expteAux.getNumCorrelativo().toString() + expteAux.getAnio() + "'");
                    primero = false;
                } else {
                    listado = listado.concat("('" + expteAux.getCodOrganismo().toString() + expteAux.getNumCorrelativo().toString() + expteAux.getAnio() + "' ");
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
        this.cuerpo = null;
        return miRecurso;
    }

    public String ingresarExpediente() {
        Expediente expedienteAux = new Expediente();
        expedienteAux = this.expedienteFacade.find(this.getCodOrganismoIng(),
                this.getNroCorrelIng(), this.getAnnioIng());
        if (expedienteAux == null) {
            //No existe el expediente

            //No se encuentra el expediente tengo que poder crear como externo
            //siempre y cuando el organismo esté cargado en el sistema
            //Verificar que el organismo está en el sistema
            List<Organismo> listadoOrganismos = this.organismoFacade.findByCodigo(this.getCodOrganismoIng());
            if (listadoOrganismos.isEmpty()) {
                //No existe el expediente
                //Organismo NO está en el sistema

                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("El organismo no está cargado en el sistema. Pida a la persona administradora del sistema para agregarlo.");
                this.setResultado("successErrorExpediente");
            } else {
                //No existe el expediente
                //Organismo está en el sistema
                Organismo organismoAux = listadoOrganismos.get(0);

                //Verificar que el organismo implemente el sistema.
                if (organismoAux.getFechaInicio() == null) {
                    //No existe el expediente
                    //Organismo está en el sistema
                    //Organismo es único con el código
                    //Organismo NO implementa el sistema

                    //Se tiene que crear un nuevo expediente externo
                    this.setNroExpedienteAux(this.getCodOrganismoIng() + "-" + this.getNroCorrelIng() + "/" + this.getAnnioIng());
                    this.setFojas(this.getFojasIng());
                    this.setCodOrganismo(this.getCodOrganismoIng());
                    this.setNroCorrel(this.getNroCorrelIng());
                    this.setAnnio(this.getAnnioIng());
                    this.setResultado("nuevoExpedienteExterno");
                } else {
                    //No existe el expediente
                    //Organismo está en el sistema
                    //Organismo es único con el código
                    //Organismo implementa el sistema

                    //Verificar que el expediente que se quiere ingresar es posterior al último
                    Long ultimo = this.expedienteFacade.findUltimoNroPorAnio(this.getCodOrganismoIng(), this.getAnnioIng());
                    if ((this.getNroCorrelIng() - 1) == ultimo) {
                        //No existe el expediente
                        //Organismo está en el sistema
                        //Organismo implementa el sistema
                        //Se intenta ingresar un expediente inmediatamente posterior al ultimo
                        this.setNroExpedienteAux(this.getCodOrganismoIng() + "-" + this.getNroCorrelIng() + "/" + this.getAnnioIng());
                        this.setFojas(this.getFojasIng());
                        this.setResultado("nuevoExpedienteExterno");
                    } else {
                        if ((this.getNroCorrelIng() - 1) < ultimo) {
                            //No existe el expediente
                            //Organismo está en el sistema
                            //Organismo implementa el sistema
                            //Organismo es único con el código
                            //Se intenta ingresar anterior al último
                            //-*-*-*No debería llegar acá dado que existe el expediente.*-*-*-
                            this.setMsgSuccessError("Está intentando ingresar el expediente " + this.getCodOrganismoIng() + "-" + this.getNroCorrelIng() + "/" + this.getAnnioIng() + " el cual ya está en el sistema.");

                        } else {
                            //No existe el expediente
                            //Organismo está en el sistema
                            //Organismo implementa el sistema
                            //Organismo es único con el código
                            //Se intenta ingresar un expediente muy posterior al último
                            Long diferencia = this.getNroCorrelIng() - ultimo;
                            this.setMsgSuccessError("Está intentando ingresar el expediente " + this.getCodOrganismoIng() + "-" + this.getNroCorrelIng() + "/" + this.getAnnioIng() + " sin embargo el último generado es " + this.getCodOrganismoIng() + "-" + ultimo + "/" + this.getAnnioIng() + ". Faltan cargar " + diferencia + " expedientes.");
                        }
                        this.setTitle("¡Error!");
                        this.setImages("images/symbolError.png");
                        this.setResultado("successErrorExpediente");
                    }
                }
            }
        } else {
            try {
                if(expedienteAux.getMesaDestino().getId().equals(139L)){
                    throw new Exception("Por solicitud del organismo receptor deberá comunicarse con el mismo para obtener el expediente.");
                }
                //Existe el expediente.
                //Está en la subsecretaria 1760
                if (!this.getUsuario().getMesa().getOrganismo().getCodOrganigrama().equals(1760L)) {
                    if (expedienteAux.getMesaOrigen().getOrganismo().getCodOrganigrama().equals(1760L) && expedienteAux.getMesaDestino().getOrganismo().getCodOrganigrama().equals(1760L)) {
                        throw new Exception("El expediente no está autorizado para ser ingresado.");
                    }
                }
                
                //Verificar que el expediente no se encuentre la bandeja de recibido 
                //de la mesa. En tal caso se le comunica de la situación
                if (expedienteAux.getMesaDestino().getId() != this.getMesaUsuario().getId()) {
                    //Existe el expediente.
                    //NO está en alguna bandeja de la mesa del usuario.

                    //Verificar que este en estado enviado o iniciado.
                    if (!(expedienteAux.getEstadoExpediente().getId() == 1L || expedienteAux.getEstadoExpediente().getId() == 2L)) {
                        //Existe el expediente.
                        //NO está en alguna bandeja de la mesa del usuario.
                        //No está enviado o iniciado.
                        System.out.println("Mesa destino:" + expedienteAux.getMesaDestino().getOrganismo().getId());
                        System.out.println("Organismo del tipo:" + this.getUsuario().getOrganismo().getId());
                        System.out.println("Ingresar siempre: " + this.isIngresarSiempre());
                        if (expedienteAux.getMesaDestino().getOrganismo().getId().equals(this.getUsuario().getOrganismo().getId())
                                && this.isIngresarSiempre()) {
                            this.setResultado("ingresarExpedientePosta");
                            this.setMsgSuccessError("Está por ingresar el expediente " + expedienteAux.getNroExp() + " con asunto \"" + expedienteAux.getAsunto() + "\" ¿Está seguro?");
                            this.setCodOrganismo(this.getCodOrganismoIng());
                            this.setNroCorrel(this.getNroCorrelIng());
                            this.setAnnio(this.getAnnioIng());
                            this.setFojas(this.getFojasIng());
                        } else {
                            throw new Exception("No es posible ingresar el expediente ya que no se encuentra en estado enviado.");
                        }
                    } else {
                        //Existe el expediente.
                        //NO está en alguna bandeja de la mesa del usuario.
                        //Está enviado o iniciado.

                        //Verificar que las fojas ingresadas no sean inferiores a la actual
                        if (this.getFojasIng() < expedienteAux.getFojas()) {
                            //Existe el expediente.
                            //NO está en alguna bandeja de la mesa del usuario.
                            //Está enviado o iniciado.
                            //La cantidad de fojas es menor

                            throw new Exception("La cantidad de fojas (" + expedienteAux.getFojas() + ") del expediente a ingresar es "
                                    + "mayor que el nº de fojas ingresadas (" + this.getFojasIng() + ")");
                        } else {
                            //Existe el expediente.
                            //NO está en alguna bandeja de la mesa del usuario.
                            //Está enviado o iniciado.
                            //La cantidad de fojas es igual o mayor

                            //Se puede ingresar el expediente
                            this.setResultado("ingresarExpedientePosta");
                            this.setMsgSuccessError("Está por ingresar el expediente " + expedienteAux.getNroExp() + " con asunto \"" + expedienteAux.getAsunto() + "\" ¿Está seguro?");
                            this.setCodOrganismo(this.getCodOrganismoIng());
                            this.setNroCorrel(this.getNroCorrelIng());
                            this.setAnnio(this.getAnnioIng());
                            this.setFojas(this.getFojasIng());
                        }
                    }
                } else {
                    //Existe el expediente.
                    //Está en alguna bandeja del usuario

                    throw new Exception("El expediente se encuentra en alguna de sus bandejas.");
                }
            } catch (Exception ex) {
                this.limpiarIngreso();
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setResultado("successErrorExpediente");
                this.setMsgSuccessError(ex.getMessage());

            }
        }
        this.limpiarIngreso();
        return this.getResultado();
    }

    public String ingresarExpedientePosta() throws Exception {
        try {
            Long idUsuario = this.getUsuario().getId();
            Long idMesaDestinoAux = this.getMesaUsuario().getId();

            this.expedienteFacade.createExterno(this.getCodOrganismo(),
                    this.getNroCorrel(), this.getAnnio(),
                    idMesaDestinoAux, this.getFojas(), idUsuario, this.isIngresarSiempre());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorExpediente");
            this.setMsgSuccessError("El expediente " + this.getCodOrganismo() + "-" + this.getNroCorrel() + "/" + this.getAnnio() + " se ingresó correctamente.");
            this.prepararEtiquetas();
            this.update(2);
        } catch (Exception e) {
            this.limpiar();
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setResultado("successErrorExpediente");
            this.setMsgSuccessError(e.getMessage());
            this.limpiar();
        }

        return this.getResultado();

    }

    @Override
    public List<SelectItem> getSelectItems() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String crearOtro() {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void verDetalle() {
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

    public void limpiarIngreso() {
        this.setCodOrganismoIng(null);
        this.setNroCorrelIng(null);
        this.setAnnioIng(null);
        this.setFojasIng(null);
    }

    public String ingresarExt(int caso) {
        String retorno = "expedienteConf";
        this.limpiarIngreso();
        switch (caso) {
            case 1: {//Cancelar en la ventana ingresarExpediente
                retorno = "expedienteConf";
                break;
            }
            case 2: { //Ingresar expediente en la ventana expedienteConf
                retorno = "ingresarExpediente";
                break;
            }
        }
        return retorno;
    }

    public String prepararIngresarCoop() {
        String retorno = "nuevoExpedienteCoop";
        this.setCodOrganismo(3330L);
        this.setCuerpo(1);
        this.setFojas(0);
        this.setIdMesa(168L);
        this.setFechaCreacion(null);
        return retorno;
    }

    public String ingresarCoop(int crearOtro) {
        try {
            List<Expediente> lstExpes = this.expedienteFacade.findAll(this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio(), null, null, null);

            Calendar cal = Calendar.getInstance();
            if (this.getFechaCreacion().before(this.getFechaActual())) {

                if (lstExpes.isEmpty()) {
                    this.getFechaCreacion().setHours(cal.get(Calendar.HOUR));
                    this.getFechaCreacion().setMinutes(cal.get(Calendar.MINUTE));
                    this.getFechaCreacion().setSeconds(cal.get(Calendar.SECOND));

                    this.setExpedienteGen(this.expedienteFacade.create(this.getFechaCreacion(),
                            this.getCodOrganismo(), this.getNroCorrel(), this.getAnnio(),
                            1, 0, this.getIdIniciador(),
                            this.getIdTipoAsunto(), this.getAsunto(), this.getObservacion(),
                            null,
                            999999, null,
                            null, null,
                            null,
                            this.getMesaUsuario().getId(),
                            this.getIdMesa(), this.getUsuario().getId()));

                    if (crearOtro == 1) {
                        this.setTitle("Proceso Completo");
                        this.setImages("images/symbolChecks.png");
                        this.setRecurso("imprimirCaratula");
                        this.setEtiqueta("Ver Carátula");
                        this.setNombreReporte(this.nombrar("caratula", "pdf"));
                        this.setResultado("successErrorExpedienteReporte");
                        this.setMsgSuccessError("El expediente " + this.getExpedienteGen().getNroExp() + " ha sido generado con éxito.");

                        this.prepararEtiquetas();
                    } else {
                        this.setResultado("nuevoExpedienteCoop");
                        this.prepararIngresarCoop();
                        this.setIdTipoAsunto(null);
                        this.setAsunto("");
                        this.setObservacion("");
                        this.setNroCorrel(null);
                        this.setAnnio(null);
                    }

                } else {
                    this.setTitle("¡Error!");
                    this.setImages("images/symbolError.png");
                    this.setMsgSuccessError("El expediente ya existe en el sistema.");
                    this.setResultado("successErrorExpediente");
                }
            } else {
                throw new Exception("La fecha ingresada no es correcta.");
            }
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("El expediente no pudo generarse.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();

    }

    public String nuevoExpInt() {
        Long nroCorrelativo = this.expedienteFacade.findUltimoNro(this.getOrganismoUsuario().getCodOrganigrama());
        Long codOrganismo = this.getOrganismoUsuario().getCodOrganigrama();
        Calendar c = GregorianCalendar.getInstance();
        Integer annio = c.get(Calendar.YEAR);
        this.setFechaCreacion(c.getTime());
        this.setNroExpedienteAux(codOrganismo + "-" + nroCorrelativo + "/" + annio);
        this.setFojas(0);

        if (this.getMesaUsuario().getId() == 180L) { //Si el usuario pertenece a la mesa de entrada del agro
            Long[] exp = findUltimoAgro();
            nroCorrelativo = exp[0] + 1;
            codOrganismo = exp[1];
            this.setNroExpedienteAux(nroCorrelativo + "/" + annio);
        }

        return "nuevoExpedienteInterno";
    }

    public Long ingresarUltimo() {
        Long nroExp = 0L;
        nroExp = this.expedienteFacade.findUltimoNro(this.getOrganismoUsuario().getCodOrganigrama());
        if (nroExp == null) {
            nroExp = 0L;
        }
        return nroExp;
    }

    public void definirPanel(ValueChangeEvent event) {
        logger.log(Level.INFO, "definirPanel llamado - OldValue: {0}, NewValue: {1}", 
            new Object[]{event.getOldValue(), event.getNewValue()});
        
        // Obtener el expediente del atributo del componente
        Expediente expedienteActual = null;
        if (event.getComponent() != null) {
            Object expedienteObj = event.getComponent().getAttributes().get("expediente");
            logger.log(Level.INFO, "Expediente del atributo: {0}", expedienteObj);
            if (expedienteObj instanceof Expediente) {
                expedienteActual = (Expediente) expedienteObj;
            }
        }
        
        // Si no está en el atributo, intentar obtenerlo del RequestMap (var="item" del dataTable)
        if (expedienteActual == null) {
            expedienteActual = (Expediente) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("item");
            logger.log(Level.INFO, "Expediente del RequestMap: {0}", expedienteActual);
        }
        
        // Si aún no está, intentar obtenerlo del ELContext
        if (expedienteActual == null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                Object itemObj = facesContext.getApplication().evaluateExpressionGet(
                    facesContext, "#{item}", Object.class);
                if (itemObj instanceof Expediente) {
                    expedienteActual = (Expediente) itemObj;
                    logger.log(Level.INFO, "Expediente del ELContext: {0}", expedienteActual);
                }
            }
        }
        
        if (expedienteActual != null) {
            // Crear la clave única del expediente
            org.ccpm.sysse.utilidad.ExpedienteClave clave = new org.ccpm.sysse.utilidad.ExpedienteClave(
                expedienteActual.getCodOrganismo(),
                expedienteActual.getNumCorrelativo(),
                expedienteActual.getAnio()
            );
            
            // Obtener el nuevo valor del checkbox
            Boolean nuevoValor = (Boolean) event.getNewValue();
            logger.log(Level.INFO, "Nuevo valor del checkbox: {0}, Expediente: {1}-{2}-{3}", 
                new Object[]{nuevoValor, expedienteActual.getCodOrganismo(), 
                    expedienteActual.getNumCorrelativo(), expedienteActual.getAnio()});
            
            // Agregar o remover del Set según el nuevo valor
            if (Boolean.TRUE.equals(nuevoValor)) {
                // Agregar el expediente al Set (permitir múltiples selecciones)
                expedientesSeleccionados.add(clave);
                // Sincronizar el estado checked del objeto
                expedienteActual.setChecked(true);
                logger.log(Level.INFO, "Expediente agregado al Set. Total seleccionados: {0}", expedientesSeleccionados.size());
            } else {
                expedientesSeleccionados.remove(clave);
                // Sincronizar el estado checked del objeto
                expedienteActual.setChecked(false);
                logger.log(Level.INFO, "Expediente removido del Set. Total seleccionados: {0}", expedientesSeleccionados.size());
            }
        } else {
            logger.log(Level.WARNING, "No se pudo obtener el expediente del evento");
        }
        
        // Actualizar el contador basado en el Set
        int flagCount = expedientesSeleccionados.size();
        logger.log(Level.INFO, "Total de expedientes seleccionados: {0}", flagCount);
        
        if (flagCount == 0) { //nada seleccionado
            this.setDesarchivarPanel("");
            this.setAceptarPanel("");

        } else if (flagCount == 1) { //uno solo seleccionado
            this.setDesarchivarPanel("desarchivarExpedienteConfirmPanel");
            this.setAceptarPanel("aceptarExpedienteConfirmPanel");
            this.setMsgDesarchivar("¿Está seguro que quiere desarchivar el expediente seleccionado?");
            this.setMsgAceptar("¿Está seguro que quiere aceptar el expediente seleccionado?");
        } else if (flagCount > 1) { //más de uno seleccionado
            this.setDesarchivarPanel("desarchivarExpedienteConfirmPanel");
            this.setAceptarPanel("aceptarExpedienteConfirmPanel");
            this.setMsgDesarchivar("¿Está seguro que quiere desarchivar los expedientes seleccionados?");
            this.setMsgAceptar("¿Está seguro que quiere aceptar los expedientes seleccionados?");
        }
    }

    public String ingresarIniciador(Long id) {
        String resultado = "";
        Iniciador iniAux = this.iniciadorFacade.find(id);
        if (iniAux.getTipoIniciador().getNombre().equals("MESA") || iniAux.getTipoIniciador().getNombre().equals("JUZGADO") || iniAux.getTipoIniciador().getNombre().equals("MUNICIPALIDAD")) {
            Mesa mesaAux = this.mesaFacade.find(id);
            resultado = mesaAux.getDescripcion();

        } else {
            resultado = iniAux.getNombre();

        }
        return resultado;
    }

    public String arreglarIniciador(String nombre) {
        String resultado = nombre;
        if (nombre.startsWith("PF ") || nombre.startsWith("PJ ")) {
            resultado = resultado.substring(3);
        }
        return resultado;
    }

    public String prepararParaImprimirListado(int idBandeja) {
        switch (idBandeja) {
            case 1: { //Bandeja Enviado
                this.setTipoListado("Expedientes Enviados");
                this.setTipoFecha("Fecha de envío");
                this.setTipoOrganismo("Enviados al organismo");
                break;
            }
            case 2: { //Bandeja Recibidos
                this.setTipoListado("Expedientes Recibidos");
                this.setTipoFecha("Fecha de recepción");
                this.setTipoOrganismo("Recibidos del organismo");
                break;
            }
            case 3: { //Bandeja Aceptadas  
                this.setTipoListado("Expedientes Aceptados");
                this.setTipoFecha("Fecha de aceptación");
                this.setTipoOrganismo("Aceptados del organismo");
                break;
            }
            case 4: { //Bandeja Rechazadas  
                this.setTipoListado("Expedientes Rechazados");
                this.setTipoFecha("Fecha de rechazo");
                this.setTipoOrganismo("Organismo que rechazó");
                break;
            }
            case 5: { //Bandeja Archivadas     
                this.setTipoListado("Expedientes Archivados");
                this.setTipoFecha("Fecha de archivado");
                this.setTipoOrganismo("Archivados al organismo");
                break;
            }
            case 6: { //Bandeja Todos 
                this.setTipoListado("Todos los Expedientes");
                this.setTipoFecha("Fecha de Movimiento");
                this.setTipoOrganismo("Organismo");
                break;
            }
        }
        return "listado";

    }

    public Long[] findUltimoAgro() {
        Long[] salida = new Long[2];
        salida[0] = this.expedienteFacade.findUltimoNro(7000L) - 1;
        salida[1] = 7000L;

        if (this.expedienteFacade.findUltimoNro(7001L) - 1 > salida[0]) { //Dirección general de asuntos Jurídicos - Agro
            salida[0] = this.expedienteFacade.findUltimoNro(7001L) - 1;
            salida[1] = 7001L;
        }
        if (this.expedienteFacade.findUltimoNro(7020L) - 1 > salida[0]) { //Dirección general administrativa - Agro
            salida[0] = this.expedienteFacade.findUltimoNro(7020L) - 1;
            salida[1] = 7020L;
        }
        if (this.expedienteFacade.findUltimoNro(7100L) - 1 > salida[0]) { //Subsecretaria de desarrollo y producción animal
            salida[0] = this.expedienteFacade.findUltimoNro(7100L) - 1;
            salida[1] = 7100L;
        }
        if (this.expedienteFacade.findUltimoNro(7120L) - 1 > salida[0]) { //Dirección general producción animal
            salida[0] = this.expedienteFacade.findUltimoNro(7120L) - 1;
            salida[1] = 7120L;
        }
        if (this.expedienteFacade.findUltimoNro(7105L) - 1 > salida[0]) { //Dirección general de Yerba mate y té
            salida[0] = this.expedienteFacade.findUltimoNro(7105L) - 1;
            salida[1] = 7105L;
        }
        if (this.expedienteFacade.findUltimoNro(7120L) - 1 > salida[0]) { //EPDA - prosap
            salida[0] = this.expedienteFacade.findUltimoNro(7120L) - 1;
            salida[1] = 7120L;
        }
        if (this.expedienteFacade.findUltimoNro(7211L) - 1 > salida[0]) { //EPDA - prosap
            salida[0] = this.expedienteFacade.findUltimoNro(7211L) - 1;
            salida[1] = 7211L;
        }
        if (this.expedienteFacade.findUltimoNro(7306L) - 1 > salida[0]) { //Dirección general economía agraria y sistemas de información
            salida[0] = this.expedienteFacade.findUltimoNro(7306L);
            salida[1] = 7306L;
        }
        if (this.expedienteFacade.findUltimoNro(7400L) - 1 > salida[0]) { //Subsecretaria de desarrollo forestal
            salida[0] = this.expedienteFacade.findUltimoNro(7400L) - 1;
            salida[1] = 7400L;
        }
        return salida;
    }

    public List<SelectItem> getLstOrganismosAgro() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();

        selectItems.add(new SelectItem(73L, "7000 - Ministerio del Agro y la Producción"));
        selectItems.add(new SelectItem(280L, "7000 - Subsecretaría de Coordinación Investigación y Desarrollo Pesquero"));
        selectItems.add(new SelectItem(281L, "7000 - Subsecretaría de Coordinación Provincial de Apicultura"));
        selectItems.add(new SelectItem(278L, "7000 - Subsecretaría de Desarrollo y Producción Vegetal"));
        selectItems.add(new SelectItem(276L, "7000 - Subsecretaría de Planificación y Extensión y Programas de Financiamiento Rural"));
        selectItems.add(new SelectItem(282L, "7000 - Subsecretaría de Plan Provincial de Desarrollo Ganadero"));
        selectItems.add(new SelectItem(277L, "7000 - Subsecretaría de Tabaco"));
        selectItems.add(new SelectItem(284L, "7000 - Dirección de Difusión y Capacitación"));
        selectItems.add(new SelectItem(279L, "7000 - Dirección General de Coordinación e Integración Informática"));
        selectItems.add(new SelectItem(283L, "7000 - Gerente General Unidad Provincia Subejecución (U.E.P.)"));
        selectItems.add(new SelectItem(183L, "7001 - Dirección General de Asuntos Jurídicos - Ministerio del Agro y la Producción"));
        selectItems.add(new SelectItem(184L, "7020 - Dirección General Administrativa - Ministerio del Agro y la Producción"));
        selectItems.add(new SelectItem(187L, "7100 - Subsecretaría de Desarrollo y Producción Animal"));
        selectItems.add(new SelectItem(185L, "7105 - Dirección General de Yerba Mate y Té"));
        selectItems.add(new SelectItem(194L, "7120 - Dirección General de Producción Animal Especies Tradicionales y No Tradicionales"));
        selectItems.add(new SelectItem(173L, "7211 - Subsecretaría de Coordinación EPDA PROSAP"));
        selectItems.add(new SelectItem(151L, "7306 - Dirección General de Economía Agraria y Sistema de Información"));
        selectItems.add(new SelectItem(74L, "7400 - Subsecretaría de Desarrollo Forestal"));

        return selectItems;
    }

    public List<SelectItem> getSelectItemsOrganismoCod() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Organismo> lstOrganismo = this.organismoFacade.findByCodigo(this.getCodOrganismoIng());
        if (!(lstOrganismo.isEmpty())) {
            lstOrganismo.stream().forEach((organismoAux) -> {
                selectItems.add(new SelectItem(organismoAux.getId(), organismoAux.getNombre() + " (" + organismoAux.getCodOrganigrama() + ")"));
            });
        }
        return selectItems;
    }

    public String filtrarMesa(Mesa mesa) {
        String mesaModif = "";
        if (getUsuario().getMesa() != null && mesa != null) {
            mesaModif = mesa.getNombre();
            if (!isPerteneceContencion() && !isPerteneceLegal()
                    && getMesaUsuario() != null && mesa.isEsconderMesa() && !getMesaUsuario().getOrganismo().equals(mesa.getOrganismo())) {
                mesaModif = mesa.getOrganismo().getCodOrganigrama() + " - " + mesa.getOrganismo().getNombre();
            }
        } else {
            mesaModif = mesa.getOrganismo().getCodOrganigrama() + " - " + mesa.getOrganismo().getNombre();
        }
        if (getUsuario().getGrupos().stream().anyMatch(entry -> entry.getId().equals(Long.valueOf(1L)))) {
            mesaModif = mesa.getId() + " - " + mesa.getNombre();
        }
        return mesaModif.toUpperCase();
    }

    public void enviarMail(String email, String asunto, String textoMensaje) throws Exception {
        try {
            // Propiedades de la conexión
            Properties props = new Properties();
            props.setProperty("mail.smtp.host", "smtp.gmail.com");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.port", "587");
            props.setProperty("mail.smtp.user", "ccpmisiones@gmail.com");
            props.setProperty("mail.smtp.auth", "true");

            // Preparamos la sesion
            Session session = Session.getDefaultInstance(props);

            // Construimos el mensaje
            MimeMessage message = new MimeMessage(session);
            message.addHeader("Disposition-Notification-To", "ccpmisiones@gmail.com");
            message.setFrom(new InternetAddress("ccpmisiones@gmail.com"));
            message.addRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(email));
            message.setSubject(asunto);
            message.setText(textoMensaje);

            Transport t = session.getTransport("smtp");

            // Lo enviamos.
            t.connect("ccpmisiones@gmail.com", "aguantejavaypostgres1");
            if (t.isConnected()) {
                t.sendMessage(message, message.getAllRecipients());
            } else {

            }

            // Cierre.
            t.close();

        } catch (Exception e) {
            throw new Exception("No se pudo envial el mail. ");
        }
    }

    @Override
    public List<?> getListElements() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Resource getHistorial() throws JRException, FileNotFoundException, IOException {
        Resource miRecurso = null;
        try {
            JasperPrint jasperResultado = new ExpedienteReport().imprimirHistorial(this.expediente.getCodOrganismo(), this.expediente.getNumCorrelativo(), this.expediente.getAnio(), this.getLeyendaAnual());
            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            miRecurso = new ExpResource(bites);
        } catch (Exception ex) {
        }
        this.cuerpo = null;
        return miRecurso;
    }

    public String imprimirHistorial() {
        this.setTitle("Proceso Completo");
        this.setImages("images/symbolChecks.png");
        this.setRecurso("historial");
        this.setEtiqueta("Ver Historial");
        this.setNombreReporte(this.nombrar("historial", "pdf"));
        this.setResultado("successErrorExpedienteReporte");
        this.setMsgSuccessError("Historial generado con éxito.");
        return this.getResultado();
    }

    public void validadorAnio(FacesContext context, UIComponent validate, Object value) {
        Integer anio = (Integer) value;

        if (anio != null && anio < 1920) {
            FacesMessage msg = new FacesMessage("El anio ingresado no es válido.");
            context.addMessage(validate.getClientId(context), msg);
            throw new ValidatorException(msg);
        }
    }

    public void validadorCorrelativo(FacesContext context, UIComponent validate, Object value) {
        Long numCorre = (Long) value;
        if (this.getCodOrganismoIng() != null && this.getNroCorrelIng() != null && this.getAnnioIng() != null) {
            Long ultimoNum = this.expedienteFacade.findUltimoNroPorAnio(this.getCodOrganismoIng(), this.getAnnioIng());
            List<Organismo> organismos = this.organismoFacade.findByCodigo(this.getCodOrganismoIng());
            Calendar calendar = Calendar.getInstance();
            if (ultimoNum != null && ultimoNum < numCorre && !organismos.isEmpty() && organismos.get(0).isImplementa() && calendar.get(Calendar.YEAR) <= this.getAnnioIng()) {
                boolean verificacionSaludPublica = true;
                if (this.getMesaUsuario().getId().equals(308L) && this.getCodOrganismoIng() >= 6000L && this.getCodOrganismoIng() < 7000L) { //solo mesa de salud publica
                    verificacionSaludPublica = false;
                }
                if (verificacionSaludPublica) {
                    FacesMessage msg = new FacesMessage((FacesMessage.Severity) FacesMessage.VALUES.get(1), "El número ingresado es superior al ultimo número generado: " + ultimoNum + ".", "El número ingresado es superior al ultimo número generado: " + ultimoNum + ".");
                    context.addMessage(validate.getClientId(context), msg);
                    throw new ValidatorException(msg);
                }
            }
        }
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

    public List<SelectItem> getSelectItemsTipoIniciador() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<TipoIniciador> lstTipoIniciador = this.tipoIniciadorFacade.findAll(true);
        if (!(lstTipoIniciador.isEmpty())) {
            lstTipoIniciador.stream().filter((tipoIniciadorAux) -> (tipoIniciadorAux.isEstado())).forEach((tipoIniciadorAux) -> {
                selectItems.add(new SelectItem(tipoIniciadorAux.getId(), tipoIniciadorAux.getNombre()));
            });
        }
        return selectItems;
    }

    public void cargarIniciadores(Long idInic) {
        this.selectItemsIniciador.clear();
        this.setIdIniciadorBsq(null);
        if (idInic == null || idInic.equals(0L)) {
            this.setDeshabilitarIniciador(true);
            return;
        }
        TipoIniciador tipoIniciador = this.tipoIniciadorFacade.find(idInic);
        if (tipoIniciador == null) {
            this.setDeshabilitarIniciador(true);
            return;
        }
        if (tipoIniciador.isExterno()) {
            List<UsuarioExterno> lstIniciadores = this.usuarioExternoFacade.findAll(tipoIniciador.getId());
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
        } else if (tipoIniciador != null) {
            List<Iniciador> lstIniciadores = this.iniciadorFacade.findAllByTipo(tipoIniciador.getId());
            if (!(lstIniciadores.isEmpty())) {
                lstIniciadores.stream().filter((iniciadorAux) -> (iniciadorAux.isEstado())).forEach((iniciadorAux) -> {
                    this.selectItemsIniciador.add(new SelectItem(iniciadorAux.getId(), iniciadorAux.getNombre()));
                });
                this.setDeshabilitarIniciador(false);
            } else {
                this.setDeshabilitarIniciador(true);
            }
        }

    }

    public void cargarIniciadoresBusqueda() {
        cargarIniciadores(this.getIdTipoIniciadorBsq());
    }

    public void cargarIniciadores(ValueChangeEvent event) {
        Object value = event.getNewValue();
        Object idComp = event.getComponent().getId();
        this.selectItemsIniciador.clear();
        this.setIdIniciador(null);
        TipoIniciador tipoIniciador = this.tipoIniciadorFacade.find(Long.parseLong(value.toString()));
        if (tipoIniciador != null && tipoIniciador.isExterno()) {
            List<UsuarioExterno> lstIniciadores = this.usuarioExternoFacade.findAll(tipoIniciador.getId());
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
                    if (idComp.equals("tipoIniciador") && iniciadorAux.getNombre().equals(this.getMesaUsuario().getNombre())) {
                        this.setIdIniciador(iniciadorAux.getId());
                    }
                }
                this.setDeshabilitarIniciador(false);
            } else {
                this.setDeshabilitarIniciador(true);
            }
        } else if (tipoIniciador != null) {
            List<Iniciador> lstIniciadores = this.iniciadorFacade.findAllByTipo(tipoIniciador.getId());
            if (!(lstIniciadores.isEmpty())) {
                for (Iniciador iniciadorAux : lstIniciadores) {
                    if (iniciadorAux.isEstado()) {
                        if (iniciadorAux.getTipoIniciador().isExterno() && (iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.COOPERATIVA) || iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.MUTUAL))) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(iniciadorAux.getNombre());
                            sb.append(" - ").append(iniciadorAux.getDocumento());
                            if (iniciadorAux.getMatriculaProvincial() != null && iniciadorAux.getMatriculaProvincial().isEmpty()) {
                                sb.append(" - Mat. Prov.: ").append(iniciadorAux.getMatriculaProvincial());
                            }
                            if (iniciadorAux.getMatriculaNacional() != null && iniciadorAux.getMatriculaNacional().isEmpty()) {
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
                if (idComp.equals("tipoIniciadorExt") && Long.parseLong(value.toString()) == 1L) {
                    Mesa mesaAux = this.iniciadorFacade.findMesaEntrada(this.getCodOrganismo());
                    if (mesaAux != null) {
                        this.setIdIniciador(mesaAux.getId());
                    }
                }
                this.setDeshabilitarIniciador(false);
            } else {
                this.setDeshabilitarIniciador(true);
            }
        }
    }

    //INICIO GESTIÓN INSTRUMENTO LEGAL
    public String gestionarInstLegal() {
        int flagCount = expedientesSeleccionados.size();
        Expediente expedienteIte = null;
        
        if (flagCount > 0) {
            // Obtener el expediente seleccionado del Set
            org.ccpm.sysse.utilidad.ExpedienteClave clave = expedientesSeleccionados.iterator().next();
            expedienteIte = this.expedienteFacade.find(
                clave.getCodOrganismo(),
                clave.getNumCorrelativo(),
                clave.getAnio()
            );
        }
        
        if (flagCount > 0) {
            if (!(flagCount > 1)) {
                this.setResultado("instrumentoLegalConf");
                this.setExpediente(this.expedienteFacade.find(expedienteIte.getCodOrganismo(),
                        expedienteIte.getNumCorrelativo(), expedienteIte.getAnio()));
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("Debe seleccionar solo un elemento de la tabla para realizar esta acción.");
                this.setResultado("successErrorExpediente");
            }
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("Debe seleccionar un elemento de la tabla para realizar esta acción.");
            this.setResultado("successErrorExpediente");
        }
        return this.getResultado();
    }

    public void limpiarInstrumentoLegal() {
        this.setNumero(null);
        this.setDescripcion(null);
        this.setFojasInst(0);
        this.setFojasInstOld(0);
        this.setIdTipoInstrumentoLegal(null);
        this.setArchivo(null);
        this.setArchivoOld(null);
    }

    public List<InstrumentoLegal> getListInstrumentoLegal() {
        this.setList(this.instrumentoLegalFacade.findAll(this.getDescripcionInstrumentoBuscar(), this.getExpediente().getCodOrganismo(), this.getExpediente().getNumCorrelativo(), this.getExpediente().getAnio()));
        return (List<InstrumentoLegal>) this.getList();
    }

    public void aplicarFiltroInstrumentoLegal() {
        if (this.getBanderita()) {
            this.setBanderita(false);
            this.setList(this.instrumentoLegalFacade.findAll(this.getDescripcionInstrumentoBuscar(), this.getExpediente().getCodOrganismo(), this.getExpediente().getNumCorrelativo(), this.getExpediente().getAnio()));
        }
    }

    public void actualizarInstrumentoLegal() {
        this.setDescripcionInstrumentoBuscar("");
        this.setBanderita(true);
        this.aplicarFiltroInstrumentoLegal();
    }

    public List<SelectItem> getSelectItemsTipoInstrumentoLegal() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<TipoInstrumentoLegal> lstTipoInstrumentoLegal = tipoInstrumentoLegalFacade.findAll(true);
        if (!(lstTipoInstrumentoLegal.isEmpty())) {
            lstTipoInstrumentoLegal.stream().forEach((tipoInstrumentoLegalAux) -> {
                selectItems.add(new SelectItem(tipoInstrumentoLegalAux.getId(), tipoInstrumentoLegalAux.getNombre()));
            });
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
            if (item != null && item.equals(tipo)) {
                valido = true;
                break;
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
            String nombreArchivo = dateFormat.format(date) + " - " + file.getFileName();
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

    public Resource prepararAdjunto(String adjunto) { //devuelve el archivo adjunto segun el path que se le pase
        String resourcePath = adjunto;
        File file = new File(resourcePath);
        try {
            this.resource = new ArchivoResource(readIntoByteArray(new FileInputStream(file)));
        } catch (Exception e) {
            e.printStackTrace();
            this.resource = new ArchivoResource(new byte[0]);
        }
        return resource;
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

    public String crearInstrumentoLegal() {
        try {
            WebManagedBean sessionBean = this.getSessionBean();
            Usuario usuarioAux = sessionBean.getUsuario();

            InstrumentoLegal insAux = this.instrumentoLegalFacade.create(this.getNumero(),
                    this.getDescripcion(), this.getFojasInst(),
                    this.getIdTipoInstrumentoLegal(), this.getArchivo(), usuarioAux.getId());
            this.expedienteFacade.agregarInstLeg(this.getExpediente().getCodOrganismo(), this.getExpediente().getNumCorrelativo(), this.getExpediente().getAnio(), insAux, usuarioAux.getId());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorInstrumentoLegal");
            this.setMsgSuccessError("El instrumento legal ha sido generado con éxito.");
            this.limpiarInstrumentoLegal();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorInstrumentoLegal");
        }
        return this.getResultado();
    }

    public String crearOtroInstrumentoLegal() {
        try {
            WebManagedBean sessionBean = this.getSessionBean();
            Usuario usuarioAux = sessionBean.getUsuario();
            InstrumentoLegal insAux = this.instrumentoLegalFacade.create(this.getNumero(),
                    this.getDescripcion(), this.getFojasInst(),
                    this.getIdTipoInstrumentoLegal(), this.getArchivo(), usuarioAux.getId());
            this.expedienteFacade.agregarInstLeg(this.getExpediente().getCodOrganismo(), this.getExpediente().getNumCorrelativo(), this.getExpediente().getAnio(), insAux, usuarioAux.getId());
            this.setResultado("nuevoInstrumentoLegal");
            this.limpiarInstrumentoLegal();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorInstrumentoLegal");
        }
        return this.getResultado();
    }

    public void verDetalleInstrumentoLegal() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        InstrumentoLegal insAux = this.instrumentoLegalFacade.find(idAux);
        this.setId(insAux.getId());
        this.setNumero(insAux.getNumero());
        this.setDescripcion(insAux.getDescripcion());
        this.setFojasInst(insAux.getFojas());
        this.setTipoInstrumento(insAux.getTipoInstrumentoLegal().getNombre());
        this.setArchivo(insAux.getArchivo());

    }

    public void prepararParaEditarInstrumentoLegal() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        InstrumentoLegal insAux = this.instrumentoLegalFacade.find(idAux);
        this.setId(insAux.getId());
        this.setNumero(insAux.getNumero());
        this.setDescripcion(insAux.getDescripcion());
        this.setFojasInst(insAux.getFojas());
        this.setFojasInstOld(insAux.getFojas());
        this.setIdTipoInstrumentoLegal(insAux.getTipoInstrumentoLegal().getId());
        this.setArchivo(insAux.getArchivo());
        this.setArchivoOld(insAux.getArchivo());
    }

    public String guardarEdicionInstrumentoLegal() {
        try {
            WebManagedBean sessionBean = this.getSessionBean();
            Usuario usuarioAux = sessionBean.getUsuario();
            this.instrumentoLegalFacade.edit(this.getId(), this.getNumero(),
                    this.getDescripcion(), this.getFojasInst(),
                    this.getIdTipoInstrumentoLegal(), this.getArchivo());
            this.expedienteFacade.agregarHistorial(this.getExpediente(), this.getId(), this.getFojasInst(), this.getFojasInstOld(), this.getArchivo(), this.getArchivoOld(), usuarioAux.getId());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorInstrumentoLegal");
            this.setMsgSuccessError("El instrumento legal ha sido editado con éxito.");
            this.limpiarInstrumentoLegal();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorInstrumentoLegal");
        }
        return this.getResultado();
    }

    public void guardarBorradoInstrumentoLegal() {
        try {
            WebManagedBean sessionBean = this.getSessionBean();
            Usuario usuarioAux = sessionBean.getUsuario();
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
            Long idInstrumento = Long.parseLong(myRequest.getParameter("id"));
            InstrumentoLegal instAux = this.instrumentoLegalFacade.find(idInstrumento);
            this.expedienteFacade.quitarInstLeg(this.getExpediente().getCodOrganismo(), this.getExpediente().getNumCorrelativo(), this.getExpediente().getAnio(), instAux, usuarioAux.getId());
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

    // Fin Instrumento Legal
    public void cargarFojas(ValueChangeEvent event) {
        Object value = event.getNewValue();

        Expediente expAux = this.expedienteFacade.find(this.getCodOrganismoIng(), this.getNroCorrelIng(), Integer.parseInt(value.toString()));
        if (expAux != null) {
            this.setFojasIng(expAux.getFojas());
        } else {
            this.setFojasIng(0);
        }

    }

    public void agregarAnio(ValueChangeEvent event) {
        Object idComp = event.getComponent().getId();
        Object value = event.getNewValue();
        if (value != null) {
            Calendar hoy = Calendar.getInstance();

            if (idComp.equals("ordenEntrega") && this.getOrdenEntregaAnio() == null) {
                this.setOrdenEntregaAnio(hoy.get(Calendar.YEAR));
            }
            if (idComp.equals("pedidoFondo") && this.getPedidoFondoAnio() == null) {
                this.setPedidoFondoAnio(hoy.get(Calendar.YEAR));
            }
        } else {
            this.setOrdenEntregaAnio(null);
            this.setPedidoFondoAnio(null);
        }

    }

    public String getLeyendaAnual() {
        return ("\"" + String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + " - " + this.leyendaFacade.findAll(Long.valueOf(Calendar.getInstance().get(Calendar.YEAR)), "").get(0).getNombre() + "\"");
    }

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
