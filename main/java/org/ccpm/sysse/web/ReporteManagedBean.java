package org.ccpm.sysse.web;

import jakarta.faces.application.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.facade.*;
import org.ccpm.sysse.utilidad.*;
import org.ccpm.sysse.reporte.ExpedienteReport;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.DefaultStreamedContent;
import java.io.ByteArrayInputStream;

@Named
@SessionScoped
public class ReporteManagedBean extends UtilManagedBean implements Serializable {

    private static final Logger logger = Logger.getLogger(ReporteManagedBean.class.getName());

    @EJB
    private ExpedienteFacadeLocal expedienteFacade;
    @EJB
    private NotaFacadeLocal notaFacade;
    @EJB
    private MesaFacadeLocal mesaFacade;
    @EJB
    private LeyendaFacadeLocal leyendaFacade;

    private Usuario usuario;

    private int respecto;
    private int estado;
    private int tiempo;
    private Long idMesa;
    private Date fecha = Calendar.getInstance().getTime();
    private Date hora = new Date();
    private Integer anio = Calendar.getInstance().get(Calendar.YEAR) - 1;
    private Integer anioMes = Calendar.getInstance().get(Calendar.YEAR);
    private Integer mes;
    private String reporteEstado;
    private String orgDest;

    private boolean diario = true;
    private boolean mensual;
    private boolean anual;
    private boolean flagReporte;
    private byte[] reportePdfBytes;

    private Long origen;
    private Long destino;
    private Date fechaDesde;
    private Date fechaHasta;

    private List<Expediente> expedientes;
    private List<Nota> notas;

    private List<ActionItem> lstActionItems;
    private boolean perteneceContencion = false, perteneceLegal = false, perteneceContaduria = false, perteneceSaludPublica = false;

    public ReporteManagedBean() {
        this.lstActionItems = new ArrayList<ActionItem>();
    }

    @PostConstruct
    private void init() {
        this.setHora(quitarTiempo(new Date()));
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            this.setUsuario(sessionBean.getUsuario());            
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
                    switch (accionAux.getNombre()) {
                        case "perteneceContencion":
                            // Para que aparezca por defecto la hora actual
                            this.setPerteneceContencion(true);
                            this.setPerteneceLegal(true);
                            break;
                        case "perteneceContaduria":
                            this.setPerteneceContaduria(true);
                            break;
                        case "perteneceSaludPublica":
                            this.setPerteneceSaludPublica(true);                            
                            break;

                    }
                }

            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public int getRespecto() {
        return respecto;
    }

    public void setRespecto(int respecto) {
        this.respecto = respecto;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getTiempo() {
        return tiempo;
    }

    public void setTiempo(int tiempo) {
        this.tiempo = tiempo;
    }

    public Long getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(Long idMesa) {
        this.idMesa = idMesa;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getHora() {
        return hora;
    }

    public void setHora(Date hora) {
        this.hora = hora;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getAnioMes() {
        return anioMes;
    }

    public void setAnioMes(Integer anioMes) {
        this.anioMes = anioMes;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public boolean isDiario() {
        return diario;
    }

    public void setDiario(boolean diario) {
        this.diario = diario;
    }

    public boolean isMensual() {
        return mensual;
    }

    public void setMensual(boolean mensual) {
        this.mensual = mensual;
    }

    public boolean isAnual() {
        return anual;
    }

    public void setAnual(boolean anual) {
        this.anual = anual;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getOrigen() {
        return origen;
    }

    public void setOrigen(Long origen) {
        this.origen = origen;
    }

    public Long getDestino() {
        return destino;
    }

    public void setDestino(Long destino) {
        this.destino = destino;
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

    public boolean isFlagReporte() {
        return flagReporte;
    }

    public void setFlagReporte(boolean flagReporte) {
        this.flagReporte = flagReporte;
    }

    public List<Expediente> getExpedientes() {
        return expedientes;
    }

    public void setExpedientes(List<Expediente> expedientes) {
        this.expedientes = expedientes;
    }

    public List<Nota> getNotas() {
        return notas;
    }

    public void setNotas(List<Nota> notas) {
        this.notas = notas;
    }

    public String getReporteEstado() {
        return reporteEstado;
    }

    public void setReporteEstado(String reporteEstado) {
        this.reporteEstado = reporteEstado;
    }

    public String getOrgDest() {
        return orgDest;
    }

    public void setOrgDest(String orgDest) {
        this.orgDest = orgDest;
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

    public boolean isPerteneceContaduria() {
        return perteneceContaduria;
    }

    public void setPerteneceContaduria(boolean perteneceContaduria) {
        this.perteneceContaduria = perteneceContaduria;
    }

    public boolean isPerteneceSaludPublica() {
        return perteneceSaludPublica;
    }

    public void setPerteneceSaludPublica(boolean perteneceSaludPublica) {
        this.perteneceSaludPublica = perteneceSaludPublica;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    @Override
    public void limpiar() {
        this.setRespecto(1);
        this.setEstado(1);
        this.setIdMesa(null);
        this.setTiempo(1);
        this.setDiario(true);
        this.setMensual(false);
        this.setAnual(false);
        this.setAnio(this.getAnioActual() - 1);
        this.setAnioMes(this.getAnioActual());
        this.setMes(1);
        this.setFecha(Calendar.getInstance().getTime());
        quitarTiempo(this.getHora());

        if (this.perteneceContencion) {
            this.setHora(Calendar.getInstance().getTime());
        } else {
            this.setHora(null);
        }
        this.setOrigen(null);
        this.setDestino(null);
        this.setFechaDesde(null);
        this.setFechaHasta(null);
        this.setFlagReporte(false);
        this.setExpedientes(null);
        this.setNotas(null);
        this.setReporteEstado(null);
        this.reportePdfBytes = null;
    }

    public String cancelar() {
        this.limpiar();
        return "reporteConf";
    }

    public List<SelectItem> getSelectItemsMesa() {    
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<Mesa> lstMesa = null;
        if (this.isPerteneceContencion() || this.getUsuario().getMesa().isMesaGeneral()) {
            // Contención y las Mesas de Entradas de los  Organismos solo
            // pueden ver las mesas de entradas de otros organismos.
           if (this.getUsuario().getOrganismo().getCodOrganigrama().intValue() == 9910) {
                lstMesa = this.mesaFacade.findAllGeneral(this.getUsuario().getOrganismo().getId(),this.getUsuario().getOrganismo().getOrganismoPadre().getId());       
            }else{
                lstMesa = this.mesaFacade.findAllGeneral(this.getUsuario().getOrganismo().getId(),0L);
            }
        } else {
            switch (this.getUsuario().getMesa().getOrganismo().getCodOrganigrama().intValue()) {
                case 9900: //Ministerio de Ecología ve la Subsecretaría de Ecología
                    lstMesa = this.mesaFacade.findAll("", this.getUsuario().getMesa().getOrganismo().getId(),null);
                    List<Mesa> lstMesaSubEco = this.mesaFacade.findAll("", 69L,null);//69 id Subse de Ecología
                    for (Mesa mesaAux : lstMesaSubEco) {
                        lstMesa.add(mesaAux);
                    }
                    break;
                case 9910: //Subsecretaría de Ecología ve la Ministerio de Ecología
                    lstMesa = this.mesaFacade.findAll("", this.getUsuario().getMesa().getOrganismo().getId(),null);
                    List<Mesa> lstMesaMiniEco = this.mesaFacade.findAll("", 80L,null); //80 id Mini de Eco
                    for (Mesa mesaAux : lstMesaMiniEco) {
                        lstMesa.add(mesaAux);
                    }
                    break;
                default: // El resto de las mesas internas solo ven las mesas de su organismo
                    lstMesa = this.mesaFacade.findAll("", this.getUsuario().getMesa().getOrganismo().getId(),null);
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
    public List<?> getListElements() {
        throw new UnsupportedOperationException("Sin implementar");
    }

    @Override
    public List<SelectItem> getSelectItems() {
        throw new UnsupportedOperationException("Sin implementar");
    }

    @Override
    public String crear() {
        throw new UnsupportedOperationException("Sin implementar");
    }

    @Override
    public String crearOtro() {
        throw new UnsupportedOperationException("Sin implementar");
    }

    @Override
    public void verDetalle() {
        throw new UnsupportedOperationException("Sin implementar");
    }

    @Override
    public void guardarBorrado() {
        throw new UnsupportedOperationException("Sin implementar");
    }

    @Override
    public void prepararParaEditar() {
        throw new UnsupportedOperationException("Sin implementar");
    }

    @Override
    public String guardarEdicion() {
        throw new UnsupportedOperationException("Sin implementar");
    }

    public void rangoAnio(FacesContext context, UIComponent validate, Object value) {
        if (this.getAnio() > this.getAnioActual()) {
            FacesMessage msg = new FacesMessage("El año tiene que ser menor o igual al actual: " + this.getAnioActual() + ".");
            context.addMessage(validate.getClientId(context), msg);
            throw new ValidatorException(msg);
        }
    }

    public void rangoAnioMes(FacesContext context, UIComponent validate, Object value) {
        if (this.getAnioMes() > this.getAnioActual()) {
            FacesMessage msg = new FacesMessage("El año tiene que ser menor o igual al actual: " + this.getAnioActual() + ".");
            context.addMessage(validate.getClientId(context), msg);
            throw new ValidatorException(msg);
        }
    }

    public Integer getAnioActual() {
        Integer anioActual = Calendar.getInstance().get(Calendar.YEAR);
        return anioActual;
    }

    public List<SelectItem> getSelectItemsMeses() {
        List<SelectItem> mesesAux = new ArrayList<>();
        Calendar c = new GregorianCalendar();

        Integer anioActual = c.get(Calendar.YEAR);
        Integer mesActual = c.get(Calendar.MONTH);
        if (this.getAnioMes() != null && this.getAnioMes() == anioActual.longValue()) {
            for (Integer i = 0; i <= (mesActual - 1); i++) {
                mesesAux.add(new SelectItem(i.longValue() + 1, this.obtenerNombreMes(i.longValue() + 1)));
            }
            this.setMes(mesActual);
        } else {
            for (Integer i = 0; i < 12; i++) {
                mesesAux.add(new SelectItem(i.longValue() + 1, this.obtenerNombreMes(i.longValue() + 1)));
            }
            this.setMes(1);
        }

        return mesesAux;
    }

    public String obtenerNombreMes(Long mes) {
        String nombre = "";
        switch (mes.intValue()) {

            case 1:
                nombre = "Enero";
                break;
            case 2:
                nombre = "Febrero";
                break;
            case 3:
                nombre = "Marzo";
                break;
            case 4:
                nombre = "Abril";
                break;
            case 5:
                nombre = "Mayo";
                break;
            case 6:
                nombre = "Junio";
                break;
            case 7:
                nombre = "Julio";
                break;
            case 8:
                nombre = "Agosto";
                break;
            case 9:
                nombre = "Septiembre";
                break;
            case 10:
                nombre = "Octubre";
                break;
            case 11:
                nombre = "Noviembre";
                break;
            case 12:
                nombre = "Diciembre";
                break;
            default:
                nombre = "Error";
                break;
        }
        return nombre;
    }

    public void renderizar(ValueChangeEvent event) {
        this.setTiempo(Integer.parseInt(event.getNewValue().toString()));
        switch (this.getTiempo()) {

            case 1:
                this.setDiario(true);
                this.setMensual(false);
                this.setAnual(false);
                break;
            case 2:
                this.setDiario(false);
                this.setMensual(true);
                this.setAnual(false);
                break;
            case 3:
                this.setDiario(false);
                this.setMensual(false);
                this.setAnual(true);
                break;
            default:
                this.setDiario(false);
                this.setMensual(false);
                this.setAnual(false);
                break;
        }
    }

    public String verificarReporte() {
        Boolean flagExp = false;
        Boolean flagNotas = false;

        // Validaciones mínimas por criterio temporal para evitar NPE y reportes vacíos.
        if (this.getTiempo() == 1 && this.getFecha() == null) {
            FacesContext.getCurrentInstance().addMessage("formu:fecha",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe ingresar una fecha."));
            return "reporteConf";
        }
        if (this.getTiempo() == 2 && (this.getAnioMes() == null || this.getMes() == null || this.getMes() < 1 || this.getMes() > 12)) {
            FacesContext.getCurrentInstance().addMessage("formu:mes",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe ingresar año y mes válidos."));
            return "reporteConf";
        }
        if (this.getTiempo() == 3 && this.getAnio() == null) {
            FacesContext.getCurrentInstance().addMessage("formu:anio",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe ingresar un año válido."));
            return "reporteConf";
        }

        switch (this.getEstado()) { //Según el estado la mesa del usuario puede ser origen o destino
            case 1: //Enviados
            case 4: //Rechazados
            case 5: //Archivados
            case 6: //Generados
                this.setOrigen(this.getUsuario().getMesa().getId());
                if (this.getIdMesa() != null && this.getIdMesa() != 0) {
                    this.setDestino(this.getIdMesa());
                } else {
                    // Si no se selecciona una mesa específica, limpiar destino para mostrar todos
                    this.setDestino(null);
                }
                logger.log(Level.INFO, "Estado {0} - Origen: {1}, Destino: {2}, IdMesa seleccionada: {3}",
                    new Object[]{this.getEstado(), this.getOrigen(), this.getDestino(), this.getIdMesa()});
                break;
            case 2://Recibidos
            case 3://Aceptados
                this.setDestino(this.getUsuario().getMesa().getId());
                if (this.getIdMesa() != null && this.getIdMesa() != 0) {
                    this.setOrigen(this.getIdMesa());
                } else {
                    // Si no se selecciona una mesa específica, limpiar origen para mostrar todos
                    this.setOrigen(null);
                }
                logger.log(Level.INFO, "Estado {0} - Origen: {1}, Destino: {2}, IdMesa seleccionada: {3}",
                    new Object[]{this.getEstado(), this.getOrigen(), this.getDestino(), this.getIdMesa()});
                break;
        }

        switch (this.getTiempo()) {
            case 1: { //Diario
                Timestamp desde = new Timestamp(this.getFecha().getTime());
                Timestamp hasta = new Timestamp(this.getFecha().getTime());
                DateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                
                // Establecer fechaDesde al inicio del día (00:00:00.000)
                Calendar calDesde = Calendar.getInstance();
                calDesde.setTime(this.getFecha());
                calDesde.set(Calendar.HOUR_OF_DAY, 0);
                calDesde.set(Calendar.MINUTE, 0);
                calDesde.set(Calendar.SECOND, 0);
                calDesde.set(Calendar.MILLISECOND, 0);
                desde.setTime(calDesde.getTime().getTime());
                
                if (this.getHora() != null) {
                    // Si hay hora específica, usar esa hora para desde
                    Calendar cale = Calendar.getInstance();
                    cale.setTime(this.getFecha());
                    Calendar horaSel = Calendar.getInstance();
                    horaSel.setTime(this.getHora());
                    cale.set(Calendar.HOUR_OF_DAY, horaSel.get(Calendar.HOUR_OF_DAY));
                    cale.set(Calendar.MINUTE, horaSel.get(Calendar.MINUTE));
                    cale.set(Calendar.SECOND, 0);
                    cale.set(Calendar.MILLISECOND, 0);
                    desde.setTime(cale.getTime().getTime());
                }
                this.setFechaDesde(desde);

                // Establecer fechaHasta al final del día (23:59:59.999)
                Calendar calHasta = Calendar.getInstance();
                calHasta.setTime(this.getFecha());
                calHasta.set(Calendar.HOUR_OF_DAY, 23);
                calHasta.set(Calendar.MINUTE, 59);
                calHasta.set(Calendar.SECOND, 59);
                calHasta.set(Calendar.MILLISECOND, 999);
                hasta.setTime(calHasta.getTime().getTime());
                
                // Si la fecha es hoy y no hay hora específica, usar la hora actual como hasta
                if (formato.format(this.getFecha()).equals(formato.format(Calendar.getInstance().getTime())) && this.getHora() == null) {
                    hasta.setTime(Calendar.getInstance().getTime().getTime());
                }
                
                this.setFechaHasta(hasta);
                
                logger.log(Level.INFO, "Fechas diario - Desde: {0}, Hasta: {1}", 
                    new Object[]{desde, hasta});

                break;
            }
            case 2: {//Mensual
                Calendar cale = Calendar.getInstance();
                cale.set(Calendar.MONTH, this.getMes() - 1);
                cale.set(Calendar.YEAR, this.getAnioMes());
                cale.set(Calendar.DAY_OF_MONTH, 1);
                cale.set(Calendar.HOUR_OF_DAY, 0);
                cale.set(Calendar.MINUTE, 0);
                cale.set(Calendar.SECOND, 0);
                cale.set(Calendar.MILLISECOND, 0);
                cale.getTime();

                Timestamp desde = new Timestamp(cale.getTime().getTime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(desde);
                cal.set(Calendar.DAY_OF_MONTH, cale.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Timestamp hasta = new Timestamp(cal.getTime().getTime());
                this.setFechaDesde(desde);
                this.setFechaHasta(hasta);
                break;
            }
            case 3: {//Anual
                Calendar cale = Calendar.getInstance();
                cale.set(Calendar.DAY_OF_YEAR, 1);
                cale.set(Calendar.YEAR, this.getAnio());
                cale.set(Calendar.HOUR_OF_DAY, 0);
                cale.set(Calendar.MINUTE, 0);
                cale.set(Calendar.SECOND, 0);
                cale.set(Calendar.MILLISECOND, 0);
                cale.getTime();

                Timestamp desde = new Timestamp(cale.getTime().getTime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(desde);
                cal.set(Calendar.DAY_OF_YEAR, cale.getActualMaximum(Calendar.DAY_OF_YEAR));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Timestamp hasta = new Timestamp(cal.getTime().getTime());
                this.setFechaDesde(desde);
                this.setFechaHasta(hasta);
                break;
            }
        }

        switch (this.getRespecto()) {

            case 1: // Expedientes
                flagExp = this.expedienteFacade.findAll(this.getEstado(), this.getFechaDesde(), this.getFechaHasta(), this.getOrigen(), this.getDestino());
                break;
            case 2: // Notas
                flagNotas = this.notaFacade.findAll(this.getEstado(), this.getFechaDesde(), this.getFechaHasta(), this.getOrigen(), this.getDestino());
                break;
            /*case 3: // Todas
                
             break;*/
            default:
                flagExp = false;
                flagNotas = false;
                break;
        }

        if (flagExp || flagNotas) {
            this.setFlagReporte(true);
        }

        if (this.isFlagReporte()) {
            try {
                getReporte();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error al pregenerar reporte para descarga", e);
            }
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("reporteVista");
            this.setMsgSuccessError("Reporte generado con éxito.");
        } else {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError("La consulta realizada no genera ningún reporte.");
            this.setResultado("successErrorInforme");
        }
        return this.getResultado();
    }

    public Resource getReporte() throws JRException, FileNotFoundException, IOException {
        Resource miRecurso = null;
        try {
            // Validaciones iniciales
            if (this.getUsuario() == null || this.getUsuario().getMesa() == null) {
                logger.log(Level.SEVERE, "Error: Usuario o Mesa es null al generar reporte");
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede generar el reporte: Usuario o Mesa no disponible."));
                return null;
            }

            String nombreReporte;
            String reporteRespecto = "exp";
            String reporteCuando = "Diario";
            String mesaExt = "";
            this.setReporteEstado("Enviados");
            this.setOrgDest("Mesa");

            switch (this.getRespecto()) {
                case 1: { // Expedientes
                    reporteRespecto = "exp";
                    switch (this.getEstado()) {
                        case 1:
                            this.setReporteEstado("Enviados");
                            break;
                        case 2:
                            this.setReporteEstado("Recibidos");
                            break;
                        case 3:
                            this.setReporteEstado("Aceptados");
                            break;
                        case 4:
                            this.setReporteEstado("Rechazados");
                            break;
                        case 5:
                            this.setReporteEstado("Archivados");
                            break;
                        case 6:
                            this.setReporteEstado("Generados");
                            reporteRespecto = "expGen";
                            break;
                        default:
                            this.setReporteEstado("");
                    }
                    break;
                }
                case 2: { // Notas
                    reporteRespecto = "nota";
                    switch (this.getEstado()) {
                        case 1:
                            this.setReporteEstado("Enviadas");
                            break;
                        case 2:
                            this.setReporteEstado("Recibidas");
                            break;
                        case 3:
                            this.setReporteEstado("Aceptadas");
                            break;
                        case 4:
                            this.setReporteEstado("Rechazadas");
                            break;
                        case 5:
                            this.setReporteEstado("Archivadas");
                            break;
                        case 6:
                            this.setReporteEstado("Generados");
                            reporteRespecto = "notaGen";
                            break;
                        default:
                            this.setReporteEstado("");
                    }
                    break;
                }
                /*case 3: { // Todos
                 reporteCuando = "Anual";
                 break;
                 }*/
            }

            switch (this.getTiempo()) {
                case 1: { // diario
                    reporteCuando = "Diario";
                    break;
                }
                case 2: { // mensual
                    reporteCuando = "Mensual";
                    break;
                }
                case 3: { // anual
                    reporteCuando = "Anual";
                    break;
                }
            }

            nombreReporte = reporteRespecto.concat(reporteCuando);
            if (reporteRespecto.equals("exp") && reporteCuando.equals("Diario") && this.isPerteneceContaduria()) {
                nombreReporte = "expFirmaDiario";
            }

            switch (this.getEstado()) {
                case 1: //Enviados
                case 4: //Rechazados
                case 5: //Archivados
                case 6: //Generados
                    this.setOrgDest("Destino");
                    if (this.getIdMesa() != null && this.getIdMesa() != 0) {
                        Mesa mesaAux = this.mesaFacade.find(this.getIdMesa());
                        if (mesaAux != null) {
                            mesaExt = mesaAux.getDescripcion();
                            if (mesaAux.isMesaGeneral() && mesaAux.getNombre() != null && mesaAux.getNombre().contains("MESA DE ENTRADAS Y SALIDAS")) {
                                if (mesaAux.getOrganismo() != null) {
                                    mesaExt = mesaAux.getOrganismo().getDescripcion() + " - MESA DE ENTRADAS Y SALIDAS";
                                }
                            }
                        }
                    }
                    break;
                case 2://Recibidos
                case 3://Aceptados
                    this.setOrgDest("Origen");
                    if (this.getIdMesa() != null && this.getIdMesa() != 0) {
                        Mesa mesaAux = this.mesaFacade.find(this.getIdMesa());
                        if (mesaAux != null) {
                            mesaExt = mesaAux.getDescripcion();
                            if (mesaAux.isMesaGeneral() && mesaAux.getNombre() != null && mesaAux.getNombre().contains("MESA DE ENTRADAS Y SALIDAS")) {
                                if (mesaAux.getOrganismo() != null) {
                                    mesaExt = mesaAux.getOrganismo().getDescripcion() + " - MESA DE ENTRADAS Y SALIDAS";
                                }
                            }
                        }
                    }
                    break;
            }

            String nombreOrganismo = "A";
            String nombreOrganismoPadre = "A";

            if (this.getUsuario().getMesa().getOrganismo() == null) {
                logger.log(Level.SEVERE, "Error: Organismo es null al generar reporte");
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede generar el reporte: Organismo no disponible."));
                return null;
            }

            if ((this.getUsuario().getMesa().getOrganismo().getOrganismoPadre() == null)) {
                nombreOrganismoPadre = this.getUsuario().getMesa().getOrganismo().getNombre() != null 
                    ? this.getUsuario().getMesa().getOrganismo().getNombre().toUpperCase() : "";
                nombreOrganismo = "";
            } else {
                nombreOrganismo = this.getUsuario().getMesa().getOrganismo().getNombre() != null 
                    ? this.getUsuario().getMesa().getOrganismo().getNombre().toUpperCase() : "";
                Organismo aux = this.getUsuario().getMesa().getOrganismo().getOrganismoPadre();
                while (aux != null && aux.getOrganismoPadre() != null) {
                    aux = aux.getOrganismoPadre();
                }
                if (aux != null) {
                    nombreOrganismoPadre = aux.getNombre() != null ? aux.getNombre() : "";
                }
            }

            String mesa = this.getUsuario().getMesa().getDescripcion() != null 
                ? this.getUsuario().getMesa().getDescripcion() : "";
            if (this.getUsuario().getMesa().isMesaGeneral() && this.getUsuario().getMesa().getNombre() != null 
                && this.getUsuario().getMesa().getNombre().contains("MESA DE ENTRADAS Y SALIDAS")) {
                mesa = "MESA DE ENTRADAS Y SALIDAS";
            }

            String leyendaAnual = this.getLeyendaAnual();
            if (leyendaAnual == null) {
                leyendaAnual = "";
            }

            // Convertir estado para el reporte: estado=1 (Enviados) debe ser estado=2 en el historial
            int estadoReporte = this.getEstado();
            if (estadoReporte == 1) {
                estadoReporte = 2; // Estado 1 (Enviados) se busca como estado 2 (ENVIADO) en el historial
            }
            
            logger.log(Level.INFO, "Generando reporte - Estado original: {0}, Estado para reporte: {1}, Origen: {2}, Destino: {3}, FechaDesde: {4}, FechaHasta: {5}",
                new Object[]{this.getEstado(), estadoReporte, this.getOrigen(), this.getDestino(), this.getFechaDesde(), this.getFechaHasta()});
            
            JasperPrint jasperResultado = new ExpedienteReport().imprimirReporte(
                    nombreReporte, nombreOrganismoPadre, nombreOrganismo, mesa, this.getReporteEstado(), this.getOrgDest(),
                    estadoReporte,
                    this.getFechaDesde(),
                    this.getFechaHasta(),
                    this.getOrigen(),
                    this.getDestino(),
                    leyendaAnual,
                    mesaExt,
                    0L,0L);

            if (jasperResultado == null) {
                logger.log(Level.SEVERE, "Error: JasperPrint es null después de imprimirReporte");
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo generar el reporte. Por favor, verifique los datos."));
                return null;
            }

            byte[] bites = JasperExportManager.exportReportToPdf(jasperResultado);
            if (bites == null || bites.length == 0) {
                logger.log(Level.SEVERE, "Error: El array de bytes del PDF es null o vacío");
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo exportar el reporte a PDF."));
                return null;
            }

            this.reportePdfBytes = bites;
            miRecurso = new ExpResource(bites);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al generar el reporte", ex);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al generar el reporte: " + ex.getMessage()));
            ex.printStackTrace();
        }
        return miRecurso;
    }

    public StreamedContent getReporteStreamedContent() {
        if (reportePdfBytes == null || reportePdfBytes.length == 0) {
            return null;
        }
        return DefaultStreamedContent.builder()
            .stream(() -> new ByteArrayInputStream(reportePdfBytes))
            .contentType("application/pdf")
            .name(nombrar("listado", "pdf"))
            .build();
    }
    
    public String getLeyendaAnual() {
        try {
            Integer anioActual = Calendar.getInstance().get(Calendar.YEAR);
            List<Leyenda> leyendas = this.leyendaFacade.findAll(Long.valueOf(anioActual), "");
            if (leyendas != null && !leyendas.isEmpty() && leyendas.get(0) != null) {
                String nombreLeyenda = leyendas.get(0).getNombre() != null ? leyendas.get(0).getNombre() : "";
                return ("\"" + String.valueOf(anioActual) + " - " + nombreLeyenda + "\"");
            } else {
                logger.log(Level.WARNING, "No se encontró leyenda para el año: " + anioActual);
                return ("\"" + String.valueOf(anioActual) + " - \"");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al obtener leyenda anual", ex);
            Integer anioActual = Calendar.getInstance().get(Calendar.YEAR);
            return ("\"" + String.valueOf(anioActual) + " - \"");
        }
    }

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}