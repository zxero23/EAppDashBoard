/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package org.ccpm.sysse.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.Resource;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.model.SelectItem;
import org.ccpm.sysse.entity.ActionItem;
import org.ccpm.sysse.entity.Grupo;
import org.ccpm.sysse.entity.Iniciador;
import org.ccpm.sysse.entity.Mesa;
import org.ccpm.sysse.entity.Nota;
import org.ccpm.sysse.entity.Organismo;
import org.ccpm.sysse.entity.TipoAsunto;
import org.ccpm.sysse.entity.TipoIniciador;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.facade.IniciadorFacadeLocal;
import org.ccpm.sysse.facade.LeyendaFacadeLocal;
import org.ccpm.sysse.facade.MesaFacadeLocal;
import org.ccpm.sysse.facade.NotaFacadeLocal;
import org.ccpm.sysse.facade.TipoAsuntoFacadeLocal;
import org.ccpm.sysse.facade.TipoIniciadorFacadeLocal;
import org.ccpm.sysse.facade.UsuarioExternoFacadeLocal;
import org.ccpm.sysse.facade.UsuarioFacadeLocal;
import static org.ccpm.sysse.web.UtilManagedBean.desacentuar;
import static org.ccpm.sysse.web.UtilManagedBean.quitarTiempo;
import org.primefaces.model.FilterMeta;
import org.ccpm.sysse.web.compat.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author matia
 */
@Named(value = "busquedaNotaManagedBean")
@SessionScoped
public class BusquedaNotaManagedBean extends UtilManagedBean implements Serializable {

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

    private boolean ingresar, editar, aceptar, rechazar, archivar, enviar, aceptarReenviar = false;
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
    private LazyDataModel<Nota> notas = new LazyDataModel<Nota>() {
        @Override
        public List<Nota> load(int i, int i1, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterMeta) {
            return new ArrayList<>();
        }
    };
    private List<Nota> notasAux;
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

    public BusquedaNotaManagedBean() {

        this.nroNota = "S/N";
        this.fechaCreacion = new Date();
        this.flagBandeja = 6;
    }

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            this.setUsuario(sessionBean.getUsuario());
            try {
                this.prepararAcciones(this.getUsuario());
            } catch (Exception e) {
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
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public TipoIniciadorFacadeLocal getTipoIniciadorFacade() {
        return tipoIniciadorFacade;
    }

    public void setTipoIniciadorFacade(TipoIniciadorFacadeLocal tipoIniciadorFacade) {
        this.tipoIniciadorFacade = tipoIniciadorFacade;
    }

    public IniciadorFacadeLocal getIniciadorFacade() {
        return iniciadorFacade;
    }

    public void setIniciadorFacade(IniciadorFacadeLocal iniciadorFacade) {
        this.iniciadorFacade = iniciadorFacade;
    }

    public TipoAsuntoFacadeLocal getTipoAsuntoFacade() {
        return tipoAsuntoFacade;
    }

    public void setTipoAsuntoFacade(TipoAsuntoFacadeLocal tipoAsuntoFacade) {
        this.tipoAsuntoFacade = tipoAsuntoFacade;
    }

    public UsuarioExternoFacadeLocal getUsuarioExternoFacade() {
        return usuarioExternoFacade;
    }

    public void setUsuarioExternoFacade(UsuarioExternoFacadeLocal usuarioExternoFacade) {
        this.usuarioExternoFacade = usuarioExternoFacade;
    }

    public MesaFacadeLocal getMesaFacade() {
        return mesaFacade;
    }

    public void setMesaFacade(MesaFacadeLocal mesaFacade) {
        this.mesaFacade = mesaFacade;
    }

    public NotaFacadeLocal getNotaFacade() {
        return notaFacade;
    }

    public void setNotaFacade(NotaFacadeLocal notaFacade) {
        this.notaFacade = notaFacade;
    }

    public UsuarioFacadeLocal getUsuarioFacade() {
        return usuarioFacade;
    }

    public void setUsuarioFacade(UsuarioFacadeLocal usuarioFacade) {
        this.usuarioFacade = usuarioFacade;
    }

    public LeyendaFacadeLocal getLeyendaFacade() {
        return leyendaFacade;
    }

    public void setLeyendaFacade(LeyendaFacadeLocal leyendaFacade) {
        this.leyendaFacade = leyendaFacade;
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

    public boolean isAceptarReenviar() {
        return aceptarReenviar;
    }

    public void setAceptarReenviar(boolean aceptarReenviar) {
        this.aceptarReenviar = aceptarReenviar;
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

    public boolean isVisibleAceptarReenviar() {
        return visibleAceptarReenviar;
    }

    public void setVisibleAceptarReenviar(boolean visibleAceptarReenviar) {
        this.visibleAceptarReenviar = visibleAceptarReenviar;
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

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getObservacionBsq() {
        return observacionBsq;
    }

    public void setObservacionBsq(String observacionBsq) {
        this.observacionBsq = observacionBsq;
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

    public List<Nota> getNotasEx() {
        return notasEx;
    }

    public void setNotasEx(List<Nota> notasEx) {
        this.notasEx = notasEx;
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

    public boolean isConsultarHistorial() {
        return consultarHistorial;
    }

    public void setConsultarHistorial(boolean consultarHistorial) {
        this.consultarHistorial = consultarHistorial;
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

    @Override
    public void aplicarFiltro() {
        Long totalNotas = this.notaFacade.countAllAvanzada(getIdNotaBsq(),
                getNroNotaBsq(), desacentuar(getReferenciaBsq().toUpperCase()),
                getFechaDesdeBsq(), getFechaHastaBsq(),
                getIdTipoAsuntoBsq(), getIdIniciadorBsq(), getIdMesaOrigen(),
                getIdMesaDestino(), isConsultarHistorial(), getObservacionBsq());
        LazyDataModel<Nota> lstNotas = new LazyDataModel<Nota>() {

            @Override
            public List<Nota> load(int first, int pageSize,
                    final String sortField, final SortOrder sortOrder, final Map<String, FilterMeta> filters) {
                List<Nota> lstNotasAux = new ArrayList<>();
                lstNotasAux = notaFacade.findAllAvanzada(getIdNotaBsq(),
                        getNroNotaBsq(), desacentuar(getReferenciaBsq().toUpperCase()),
                        getFechaDesdeBsq(), getFechaHastaBsq(),
                        getIdTipoAsuntoBsq(), getIdIniciadorBsq(), getIdMesaOrigen(),
                        getIdMesaDestino(), isConsultarHistorial(), getObservacionBsq(), first, pageSize);
                return lstNotasAux;
            }

        };
        lstNotas.setRowCount((totalNotas).intValue());
        this.setNotas(lstNotas);
    }

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<?> getListElements() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<SelectItem> getSelectItems() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void limpiar() {

    }

    @Override
    public void actualizar() {
        super.actualizar();
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
        this.setDeshabilitarIniciador(true);
        this.setConsultarHistorial(false);
        this.setNotas(new LazyDataModel<Nota>() {
            @Override
            public List<Nota> load(int i, int i1, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterMeta) {
                return new ArrayList<>();
            }
        });
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

    public List<SelectItem> getSelectItemsMesa() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<Mesa> lstMesa = this.mesaFacade.findAll(true);
        if (!(lstMesa.isEmpty())) {
            for (Mesa mesaAux : lstMesa) {
                if (!mesaAux.isEsconderMesa()) {
                    selectItems.add(new SelectItem(mesaAux.getId(), mesaAux.getNombre()));
                }

                if (mesaAux.isEsconderMesa() && this.getUsuario().getMesa() != null && mesaAux.getOrganismo().getId().equals(this.getUsuario().getMesa().getOrganismo().getId())) {
                    selectItems.add(new SelectItem(mesaAux.getId(), mesaAux.getNombre()));
                }

            }
        }
        return selectItems;
    }

    public void cargarIniciadores(ValueChangeEvent event) {
        Object value = event.getNewValue();
        this.selectItemsIniciador.clear();
        this.setIdInic(null);
        this.setIdIniciador(null);
        List<Iniciador> lstIniciadores = this.iniciadorFacade.findAllByTipo(Long.parseLong(value.toString()));
        if (!(lstIniciadores.isEmpty())) {
            for (Iniciador iniciadorAux : lstIniciadores) {
                if (iniciadorAux.isEstado()) {
                    if (iniciadorAux.getTipoIniciador().isExterno() && iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.COOPERATIVA)) {
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
                    } else if (iniciadorAux.getTipoIniciador().isExterno() && !iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.COOPERATIVA)) {
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
        if (this.idTipoIniciadorBsq == null || this.idTipoIniciadorBsq == 0L) {
            this.selectItemsIniciador.clear();
            this.setIdIniciadorBsq(null);
            this.setDeshabilitarIniciador(true);
            return;
        }
        cargarIniciadores(this.idTipoIniciadorBsq);
    }

    public void cargarIniciadores(Long idTipoIniciador) {
        this.selectItemsIniciador.clear();
        this.setIdIniciadorBsq(null);
        if (idTipoIniciador == null || idTipoIniciador == 0L) {
            this.setDeshabilitarIniciador(true);
            return;
        }

        TipoIniciador tipoIniciador = this.tipoIniciadorFacade.find(idTipoIniciador);
        if (tipoIniciador != null && tipoIniciador.isExterno()) {
            List<Iniciador> lstIniciadores = new ArrayList<>(this.usuarioExternoFacade.findAll(tipoIniciador.getId()));
            cargarSelectItemsIniciador(lstIniciadores);
            return;
        }

        if (tipoIniciador != null) {
            List<Iniciador> lstIniciadores = this.iniciadorFacade.findAllByTipo(tipoIniciador.getId());
            cargarSelectItemsIniciador(lstIniciadores);
            return;
        }

        this.setDeshabilitarIniciador(true);
    }

    private void cargarSelectItemsIniciador(List<Iniciador> lstIniciadores) {
        if (lstIniciadores == null || lstIniciadores.isEmpty()) {
            this.setDeshabilitarIniciador(true);
            return;
        }

        for (Iniciador iniciadorAux : lstIniciadores) {
            if (!iniciadorAux.isEstado()) {
                continue;
            }
            if (iniciadorAux.getTipoIniciador().isExterno()
                    && (iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.COOPERATIVA)
                    || iniciadorAux.getTipoIniciador().getId().equals(TipoIniciador.MUTUAL))) {
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
            } else if (iniciadorAux.getTipoIniciador().isExterno()) {
                this.selectItemsIniciador.add(new SelectItem(iniciadorAux.getId(), iniciadorAux.getNombre() + " - " + iniciadorAux.getDocumento()));
            } else {
                this.selectItemsIniciador.add(new SelectItem(iniciadorAux.getId(), iniciadorAux.getNombre()));
            }
        }

        this.setDeshabilitarIniciador(this.selectItemsIniciador.isEmpty());
    }

    public String borrarNota() {
        try {
            if (this.getNota() != null && this.getUsuario().getId().equals(1L)) {
                this.notaFacade.remove(getNota(), this.getUsuario().getId());
            } else {
                this.limpiar();
                this.setTitle("Proceso Completo");
                this.setImages("images/symbolError.png");
                this.setResultado("successErrorNotaAvanzada");
                this.setMsgSuccessError("La nota no pertenece a su organismo.");

            }
        } catch (Exception e) {
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolError.png");
            this.setResultado("successErrorExpedienteAvanzada");
            this.setMsgSuccessError("Error al eliminar la Nota: " + e.getMessage());
        }

        return this.getResultado();
    }

    @Override
    public String crear() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String crearOtro() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void verDetalle() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void guardarBorrado() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void prepararParaEditar() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String guardarEdicion() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
