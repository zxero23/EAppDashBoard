package org.ccpm.sysse.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.Resource;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import org.ccpm.sysse.entity.ActionItem;
import org.ccpm.sysse.entity.Expediente;
import org.ccpm.sysse.entity.Grupo;
import org.ccpm.sysse.entity.Iniciador;
import org.ccpm.sysse.entity.Mesa;
import org.ccpm.sysse.entity.TipoAsunto;
import org.ccpm.sysse.entity.TipoIniciador;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.facade.ExpedienteFacadeLocal;
import org.ccpm.sysse.facade.IniciadorFacadeLocal;
import org.ccpm.sysse.facade.UsuarioExternoFacadeLocal;
import org.ccpm.sysse.facade.MesaFacadeLocal;
import org.ccpm.sysse.facade.TipoAsuntoFacadeLocal;
import org.ccpm.sysse.facade.TipoIniciadorFacadeLocal;
import org.ccpm.sysse.utilidad.*;
import org.primefaces.model.FilterMeta;
import org.ccpm.sysse.web.compat.LazyDataModel;
import org.primefaces.model.SortOrder;

@Named
@SessionScoped
public class BusquedaManagedBean extends UtilManagedBean implements Serializable {

    @EJB
    private ExpedienteFacadeLocal expedienteFacade;
    @EJB
    private TipoAsuntoFacadeLocal tipoAsuntoFacade;
    @EJB
    private TipoIniciadorFacadeLocal tipoIniciadorFacade;
    @EJB
    private IniciadorFacadeLocal iniciadorFacade;
    @EJB
    private UsuarioExternoFacadeLocal usuarioExternoFacade;
    @EJB
    private MesaFacadeLocal mesaFacade;
    private Long codOrganismo;
    private Long nroCorrel;
    private Integer anio;
    private Long idIniciador;
    private Long idTipoIniciador;
    private Long idTipoAsunto;
    private Long idMesaOrigen;
    private Long idMesaDestino;
    private String asunto = "";
    private String ordenPagoBsq;
    private String observacionBsq;
    private Integer pedidoFondoBsq;
    private Integer pedidoFondoAnioBsq;
    private Integer ordenEntregaBsq;
    private Integer ordenEntregaAnioBsq;
    private String expMadreBsq = "";
    private Date fechaDesde;
    private Date fechaHasta;
    private boolean consultarHistorial;
    private LazyDataModel<Expediente> expedientes = new LazyDataModel<Expediente>() {
        @Override
        public List<Expediente> load(int i, int i1, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterMeta) {
            return new ArrayList<>();
        }
    };
    private Expediente expediente;
    private Usuario usuario;
    private List<ActionItem> lstActionItems;
    private Boolean perteneceContaduria, deshabilitarIniciador = true, imprimir = false;
    List<SelectItem> selectItemsIniciador = new ArrayList<>();
    private Resource resource;

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            try {
                this.setUsuario(sessionBean.getUsuario());
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
                    if (accionAux.getNombre().equalsIgnoreCase("perteneceContaduria")) { // Contaduría tiene campos extras a la hora de generar expedientes
                        this.setPerteneceContaduria(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("eliminarExpediente")) { // Contaduría tiene campos extras a la hora de generar expedientes
                        this.setBaja(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario.");
        }
    }

    public Boolean getPerteneceContaduria() {
        return perteneceContaduria;
    }

    public void setPerteneceContaduria(Boolean perteneceContaduria) {
        this.perteneceContaduria = perteneceContaduria;
    }

    /**
     * Creates a new instance of BusquedaManagedBean
     */
    public BusquedaManagedBean() {
        this.lstActionItems = new ArrayList<ActionItem>();
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public void setCodOrganismo(Long codOrganismo) {
        this.codOrganismo = codOrganismo;
    }

    public Long getIdTipoIniciador() {
        return idTipoIniciador;
    }

    public void setIdTipoIniciador(Long idTipoIniciador) {
        this.idTipoIniciador = idTipoIniciador;
    }

    public void setIdIniciador(Long idIniciador) {
        this.idIniciador = idIniciador;
    }

    public void setIdTipoAsunto(Long idTipoAsunto) {
        this.idTipoAsunto = idTipoAsunto;
    }

    public void setNroCorrel(Long nroCorrel) {
        this.nroCorrel = nroCorrel;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public void setIdMesaDestino(Long idMesaDestino) {
        this.idMesaDestino = idMesaDestino;
    }

    public void setIdMesaOrigen(Long idMesaOrigen) {
        this.idMesaOrigen = idMesaOrigen;
    }

    public void setExpedientes(LazyDataModel<Expediente> expedientes) {
        this.expedientes = expedientes;
    }

    public void setExpediente(Expediente expediente) {
        this.expediente = expediente;
    }

    public Expediente getExpediente() {
        return expediente;
    }

    public LazyDataModel<Expediente> getExpedientes() {
        return expedientes;
    }

    public Long getIdMesaDestino() {
        return idMesaDestino;
    }

    public Long getIdMesaOrigen() {
        return idMesaOrigen;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public Integer getAnio() {
        return anio;
    }

    public String getAsunto() {
        return asunto;
    }

    public Long getCodOrganismo() {
        return codOrganismo;
    }

    public Long getIdIniciador() {
        return idIniciador;
    }

    public Long getIdTipoAsunto() {
        return idTipoAsunto;
    }

    public Long getNroCorrel() {
        return nroCorrel;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getOrdenPagoBsq() {
        return ordenPagoBsq;
    }

    public void setOrdenPagoBsq(String ordenPagoBsq) {
        this.ordenPagoBsq = ordenPagoBsq;
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

    public Boolean getDeshabilitarIniciador() {
        return deshabilitarIniciador;
    }

    public void setDeshabilitarIniciador(Boolean deshabilitarIniciador) {
        this.deshabilitarIniciador = deshabilitarIniciador;
    }

    public List<SelectItem> getSelectItemsIniciador() {
        return selectItemsIniciador;
    }

    public void setSelectItemsIniciador(List<SelectItem> selectItemsIniciador) {
        this.selectItemsIniciador = selectItemsIniciador;
    }

    public Boolean getImprimir() {
        return imprimir;
    }

    public void setImprimir(Boolean imprimir) {
        this.imprimir = imprimir;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
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

    public List<SelectItem> getSelectItemsTipoAsunto() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<TipoAsunto> lstTipoAsunto = tipoAsuntoFacade.findAll(true);
        if (!(lstTipoAsunto.isEmpty())) {
            for (TipoAsunto tipoAsuntoAux : lstTipoAsunto) {
                selectItems.add(new SelectItem(tipoAsuntoAux.getId(), tipoAsuntoAux.getNombre()));
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

    @Override
    public void aplicarFiltro() {
        Long totalExpedientes = expedienteFacade.countAllAvanzada(getCodOrganismo(),
                getNroCorrel(), getAnio(),
                getFechaDesde(), getFechaHasta(),
                getIdIniciador(),
                getIdMesaOrigen(), getIdMesaDestino(),
                getIdTipoAsunto(), getAsunto(),
                getOrdenPagoBsq(),
                getPedidoFondoBsq(), getPedidoFondoAnioBsq(),
                getOrdenEntregaBsq(), getOrdenEntregaAnioBsq(),
                getExpMadreBsq(), getUsuario().getMesa() != null ? getUsuario().getMesa().getId() : null,
                isConsultarHistorial(), getObservacionBsq());
        LazyDataModel<Expediente> lstExpedientes = new LazyDataModel<Expediente>() {

            @Override
            public List<Expediente> load(int first, int pageSize,
                    final String sortField, final SortOrder sortOrder, final Map<String, FilterMeta> filters) {
                List<Expediente> lstExpedientesAux = new ArrayList<>();
                lstExpedientesAux = expedienteFacade.findAllAvanzada(getCodOrganismo(),
                        getNroCorrel(), getAnio(),
                        getFechaDesde(), getFechaHasta(),
                        getIdIniciador(),
                        getIdMesaOrigen(), getIdMesaDestino(),
                        getIdTipoAsunto(), getAsunto(),
                        getOrdenPagoBsq(),
                        getPedidoFondoBsq(), getPedidoFondoAnioBsq(),
                        getOrdenEntregaBsq(), getOrdenEntregaAnioBsq(),
                        getExpMadreBsq(), getUsuario().getMesa() != null ? getUsuario().getMesa().getId() : null,
                        isConsultarHistorial(), getObservacionBsq(), first, pageSize);
                return lstExpedientesAux;
            }

        };
        lstExpedientes.setRowCount((totalExpedientes).intValue());
        this.setExpedientes(lstExpedientes);
    }


    @Override
    public void actualizar() {
        super.actualizar();
        this.setCodOrganismo(null);
        this.setNroCorrel(null);
        this.setAnio(null);
        this.setFechaDesde(null);
        this.setFechaHasta(null);
        this.setIdIniciador(null);
        this.setIdTipoIniciador(null);
        this.setDeshabilitarIniciador(true);
        this.setIdMesaOrigen(null);
        this.setIdTipoAsunto(null);
        this.setIdMesaDestino(null);
        this.setOrdenPagoBsq("");
        this.setPedidoFondoBsq(null);
        this.setPedidoFondoAnioBsq(null);
        this.setOrdenEntregaBsq(null);
        this.setOrdenEntregaAnioBsq(null);
        this.setObservacionBsq(null);
        this.setExpMadreBsq("");
        this.setAsunto("");
        this.setImprimir(false);
        this.setConsultarHistorial(false);
        this.expedientes = new LazyDataModel<Expediente>() {
            @Override
            public List<Expediente> load(int i, int i1, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterMeta) {
                return new ArrayList<>();
            }
        };
    }

    @Override
    public void limpiar() {
        
    }

    @Override
    public String crear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String crearOtro() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void verDetalle() {

    }

    @Override
    public void guardarBorrado() {

    }

    public String borrarExpediente() {
        try {
            if (this.getExpediente() != null && this.getUsuario().getId().equals(1L)) {
                this.expedienteFacade.eliminarExpediente(getExpediente().getCodOrganismo(),
                        getExpediente().getNumCorrelativo(), getExpediente().getAnio(), this.getUsuario().getId());
            } else if (this.getExpediente() != null && getExpediente().getCodOrganismo().equals(this.getUsuario().getOrganismo().getCodOrganigrama())) {
                this.expedienteFacade.eliminarExpediente(getExpediente().getCodOrganismo(),
                        getExpediente().getNumCorrelativo(), getExpediente().getAnio(), this.getUsuario().getId());

            } else if (getExpediente().isChecked() && !getExpediente().getCodOrganismo().equals(this.getUsuario().getOrganismo().getCodOrganigrama()) && !this.getUsuario().getId().equals(1L)) {
                this.limpiar();
                this.setTitle("Proceso Completo");
                this.setImages("images/symbolError.png");
                this.setResultado("successErrorExpedienteAvanzada");
                this.setMsgSuccessError("El expediente no pertenece a su organismo.");

            }
        } catch (Exception e) {
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolError.png");
            this.setResultado("successErrorExpedienteAvanzada");
            this.setMsgSuccessError("Error al eliminar el expediente: " + e.getMessage());
        }

        return this.getResultado();
    }

    @Override
    public void prepararParaEditar() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String guardarEdicion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<SelectItem> getSelectItems() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<?> getListElements() {
        throw new UnsupportedOperationException("Not supported yet.");
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
        if (this.idTipoIniciador == null || this.idTipoIniciador == 0L) {
            this.selectItemsIniciador.clear();
            this.setIdIniciador(null);
            this.setDeshabilitarIniciador(true);
            return;
        }
        cargarIniciadores(this.idTipoIniciador);
    }

    public void cargarIniciadores(Long idTipoIniciador) {
        this.selectItemsIniciador.clear();
        this.setIdIniciador(null);
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

    /**
     * Este metodo permite mostrar en los detalles del historial el usuario que
     * realizo el movimiento. Se crea un grupo que se llama COLUMNAS ESPECIALES
     * SUPER ADMIN, se asigna solo a los admin que lo requieran.
     *
     * @return
     */
    public boolean mostrarColUsuario() {
        boolean mostrar = false;
        WebManagedBean sessionBean = this.getSessionBean();
        for (Grupo grupo : sessionBean.getUsuario().getGrupos()) {
            if (grupo.getId() == 27L || grupo.getId() == 1L) {
                mostrar = true;
                break;
            }
        }
        return mostrar;
    }

    /**
     * Este metodo permite mostrar en los detalles del historial el ID del
     * movimiento.
     *
     * @return
     */
    public boolean mostrarColSuper() {
        boolean mostrar = false;
        WebManagedBean sessionBean = this.getSessionBean();
        for (Grupo grupo : sessionBean.getUsuario().getGrupos()) {
            if (grupo.getId() == 1L) {
                mostrar = true;
                break;
            }
        }
        return mostrar;
    }

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String arreglarIniciador(Iniciador iniciador) {
        String nombreMostrar = "";
        if (iniciador.getTipoIniciador().getId().equals(1L)) {
            nombreMostrar = iniciador.getNombre();
        } else {
            nombreMostrar = iniciador.getId() + " - " + iniciador.getNombre();
        }
        return nombreMostrar;
    }
}
