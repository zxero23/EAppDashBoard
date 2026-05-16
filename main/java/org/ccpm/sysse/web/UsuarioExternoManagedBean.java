package org.ccpm.sysse.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.sysse.entity.ActionItem;
import org.ccpm.sysse.entity.Grupo;
import org.ccpm.sysse.entity.TipoIniciador;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.entity.UsuarioExterno;
import org.ccpm.sysse.facade.GrupoFacadeLocal;
import org.ccpm.sysse.facade.TipoIniciadorFacadeLocal;
import org.ccpm.sysse.facade.UsuarioExternoFacadeLocal;
import org.ccpm.sysse.facade.UsuarioFacadeLocal;
import static org.ccpm.sysse.web.UtilManagedBean.desacentuar;
import org.primefaces.model.FilterMeta;
import org.ccpm.sysse.web.compat.LazyDataModel;
import org.primefaces.model.SortOrder;

@Named
@SessionScoped
public class UsuarioExternoManagedBean extends UtilManagedBean implements Serializable {

    @EJB
    private UsuarioExternoFacadeLocal usuarioExternoFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private TipoIniciadorFacadeLocal tipoIniciadorFacade;
    private String apellido;
    private String nombre;
    private String documento;
    private String matriculaProvincial;
    private String matriculaNacional;
    private String documentoViejo;
    private String matriculaProvincialViejo;
    private String matriculaNacionalViejo;
    private String email;
    private String uss;
    private String ussViejo;
    private String pass;
    private UsuarioExterno usuarioExterno;
    private List<ActionItem> lstActionItems = new ArrayList<ActionItem>();
    private Usuario usuario;
    private String documentoBuscar = "";
    private String matriculaProvincialBuscar = "";
    private String matriculaNacionalBuscar = "";
    private Long idTipoIniciador;
    private Long idTipoIniciadorBuscar;
    private Long idBuscar;

    /**
     * Nuevo usuario renderización
     */
    private String etiquetaCuit = "";
    private String etiquetaApellido = "";
    private Boolean visibleFisica = true;

    /**
     * Creates a new instance of UsuarioExternoManagedBean
     */
    public UsuarioExternoManagedBean() {
    }

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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoUsuarioExterno")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarUsuarioExterno")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarUsuarioExterno")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleUsuarioExterno")) {
                        this.setDetalle(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setUss(String uss) {
        this.uss = uss;
    }

    public void setUsuarioExterno(UsuarioExterno usuarioExterno) {
        this.usuarioExterno = usuarioExterno;
    }

    public UsuarioExterno getUsuarioExterno() {
        return usuarioExterno;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }

    public String getUss() {
        return uss;
    }

    public String getPass() {
        return pass;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEtiquetaCuit() {
        return etiquetaCuit;
    }

    public void setEtiquetaCuit(String etiquetaCuit) {
        this.etiquetaCuit = etiquetaCuit;
    }

    public String getEtiquetaApellido() {
        return etiquetaApellido;
    }

    public void setEtiquetaApellido(String etiquetaApellido) {
        this.etiquetaApellido = etiquetaApellido;
    }

    public Boolean getVisibleFisica() {
        return visibleFisica;
    }

    public void setVisibleFisica(Boolean visibleFisica) {
        this.visibleFisica = visibleFisica;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getDocumentoBuscar() {
        return documentoBuscar;
    }

    public void setDocumentoBuscar(String documentoBuscar) {
        this.documentoBuscar = documentoBuscar;
    }

    public String getDocumentoViejo() {
        return documentoViejo;
    }

    public void setDocumentoViejo(String documentoViejo) {
        this.documentoViejo = documentoViejo;
    }

    public String getUssViejo() {
        return ussViejo;
    }

    public void setUssViejo(String ussViejo) {
        this.ussViejo = ussViejo;
    }

    public Long getIdTipoIniciador() {
        return idTipoIniciador;
    }

    public void setIdTipoIniciador(Long idTipoIniciador) {
        this.idTipoIniciador = idTipoIniciador;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getMatriculaProvincial() {
        return matriculaProvincial;
    }

    public void setMatriculaProvincial(String matriculaProvincial) {
        this.matriculaProvincial = matriculaProvincial;
    }

    public String getMatriculaNacional() {
        return matriculaNacional;
    }

    public void setMatriculaNacional(String matriculaNacional) {
        this.matriculaNacional = matriculaNacional;
    }

    public String getMatriculaProvincialBuscar() {
        return matriculaProvincialBuscar;
    }

    public void setMatriculaProvincialBuscar(String matriculaProvincialBuscar) {
        this.matriculaProvincialBuscar = matriculaProvincialBuscar;
    }

    public String getMatriculaNacionalBuscar() {
        return matriculaNacionalBuscar;
    }

    public void setMatriculaNacionalBuscar(String matriculaNacionalBuscar) {
        this.matriculaNacionalBuscar = matriculaNacionalBuscar;
    }

    public String getMatriculaProvincialViejo() {
        return matriculaProvincialViejo;
    }

    public void setMatriculaProvincialViejo(String matriculaProvincialViejo) {
        this.matriculaProvincialViejo = matriculaProvincialViejo;
    }

    public String getMatriculaNacionalViejo() {
        return matriculaNacionalViejo;
    }

    public void setMatriculaNacionalViejo(String matriculaNacionalViejo) {
        this.matriculaNacionalViejo = matriculaNacionalViejo;
    }

    public Long getIdTipoIniciadorBuscar() {
        return idTipoIniciadorBuscar;
    }

    public void setIdTipoIniciadorBuscar(Long idTipoIniciadorBuscar) {
        this.idTipoIniciadorBuscar = idTipoIniciadorBuscar;
    }

    public Long getIdBuscar() {
        return idBuscar;
    }

    public void setIdBuscar(Long idBuscar) {
        this.idBuscar = idBuscar;
    }

    @Override
    public void limpiar() {
        this.setApellido(null);
        this.setNombre(null);
        this.setDocumento(null);
        this.setMatriculaProvincial(null);
        this.setMatriculaNacional(null);
        this.setMatriculaNacionalViejo(null);
        this.setMatriculaProvincialViejo(null);
        this.setDocumentoViejo(null);
        this.setEmail(null);
        this.setUss(null);
        this.setUssViejo(null);
        this.setPass(null);
        this.setIdTipoIniciador(null);
    }

    @Override
    public String crear() {
        try {
            if ((this.getIdTipoIniciador().equals(TipoIniciador.COOPERATIVA) || this.getIdTipoIniciador().equals(TipoIniciador.MUTUAL))
                    && !(this.getUsuario().getGrupos().contains(this.grupoFacade.find(1L))
                    || this.getUsuario().getGrupos().contains(this.grupoFacade.find(22L)))) {
                throw new Exception("El Ministerio de Acción Cooperativa es el único organismo competente para crear cooperativas. Si no está cargada debe comunicarse con ellos.");
            }
            this.usuarioExternoFacade.create(this.getIdTipoIniciador(), this.getApellido(),
                    this.getNombre().toUpperCase(), this.getDocumento(), this.getMatriculaProvincial(), this.getMatriculaNacional(),
                    this.getEmail(), this.getUss(), this.getPass(), this.getSessionBean().getUsuario());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorUsuarioExterno");
            this.limpiar();
            this.setMsgSuccessError("El usuario externo ha sido generado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuarioExterno");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try {
            if (this.getIdTipoIniciador().equals(TipoIniciador.COOPERATIVA)
                    && !(this.getUsuario().getGrupos().contains(this.grupoFacade.find(1L))
                    || this.getUsuario().getGrupos().contains(this.grupoFacade.find(22L)))) {
                throw new Exception("El Ministerio de Acción Cooperativa es el único organismo competente para crear cooperativas. Si no está cargada debe comunicarse con ellos.");
            }
            this.usuarioExternoFacade.create(this.getIdTipoIniciador(), this.getApellido(),
                    this.getNombre().toUpperCase(), this.getDocumento(), this.getMatriculaProvincial(), this.getMatriculaNacional(),
                    this.getEmail(), this.getUss(), this.getPass(), this.getSessionBean().getUsuario());
            this.setResultado("nuevoUsuarioExterno");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuarioExterno");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        this.setUsuarioExterno(this.usuarioExternoFacade.find(idObjectAux));
    }

    public String detallar() {
        String pagina = "detalleUsuarioExternoPJ";
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        UsuarioExterno usuarioExternoAux = this.usuarioExternoFacade.find(idObjectAux);
        this.setUsuarioExterno(this.usuarioExternoFacade.find(idObjectAux));
        this.setId(usuarioExternoAux.getId());
        this.setApellido(usuarioExternoAux.getUsuarioSistema().getApellido());
        this.setNombre(usuarioExternoAux.getUsuarioSistema().getNombre());
        this.setDocumento(usuarioExternoAux.getDocumento());
        this.setMatriculaProvincial(usuarioExternoAux.getMatriculaProvincial());
        this.setMatriculaNacional(usuarioExternoAux.getMatriculaNacional());
        this.setEmail(usuarioExternoAux.getUsuarioSistema().getEmail());
        this.setUss(usuarioExternoAux.getUsuarioSistema().getNombreUsuario());
        this.setPass(usuarioExternoAux.getUsuarioSistema().getClaveAcceso());
        if (this.getApellido() == null) {
            pagina = "detalleUsuarioExternoPJ";
        } else {
            pagina = "detalleUsuarioExternoPF";
        }
        return pagina;
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
            Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
            this.usuarioExternoFacade.remove(idObjectAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.aplicarFiltro();
            this.setResultado("usuarioExternoConf");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuarioExterno");
        }
    }

    public String prepararParaEdicion() {
        String pagina = "editarUsuarioExternoPJ";
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        UsuarioExterno usuarioExternoAux = this.usuarioExternoFacade.find(idObjectAux);
        this.setId(usuarioExternoAux.getUsuarioSistema().getId());
        this.setApellido(usuarioExternoAux.getUsuarioSistema().getApellido());
        this.setNombre(usuarioExternoAux.getUsuarioSistema().getNombre());
        this.setDocumento(usuarioExternoAux.getDocumento());
        this.setMatriculaProvincial(usuarioExternoAux.getMatriculaProvincial());
        this.setMatriculaNacional(usuarioExternoAux.getMatriculaNacional());
        this.setDocumentoViejo(usuarioExternoAux.getDocumento());
        this.setMatriculaNacionalViejo(usuarioExternoAux.getMatriculaNacional());
        this.setMatriculaProvincialViejo(usuarioExternoAux.getMatriculaProvincial());
        this.setEmail(usuarioExternoAux.getUsuarioSistema().getEmail());
        this.setUss(usuarioExternoAux.getUsuarioSistema().getNombreUsuario());
        this.setUssViejo(usuarioExternoAux.getUsuarioSistema().getNombreUsuario());
        this.setPass(usuarioExternoAux.getUsuarioSistema().getClaveAcceso());

        if (usuarioExternoAux.getTipoIniciador() != null) {
            this.setIdTipoIniciador(usuarioExternoAux.getTipoIniciador().getId());
        }
        if (this.getApellido() == null) {
            pagina = "editarUsuarioExternoPJ";
        } else {
            pagina = "editarUsuarioExternoPF";
        }
        return pagina;
    }

    public String prepararParaExpe() {

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        UsuarioExterno usuarioExternoAux = this.usuarioExternoFacade.find(idObjectAux);

        this.setIdTipoInic(usuarioExternoAux.getTipoIniciador().getId());
        this.setIdInic(usuarioExternoAux.getId());

        return "nuevoExpedienteInterno";
    }

    @Override
    public String guardarEdicion() {
        try {
            if ((this.getIdTipoIniciador().equals(TipoIniciador.COOPERATIVA) || this.getIdTipoIniciador().equals(TipoIniciador.MUTUAL))
                    && !(this.getUsuario().getGrupos().contains(this.grupoFacade.find(1L))
                    || this.getUsuario().getGrupos().contains(this.grupoFacade.find(22L)))) {
                throw new Exception("El Ministerio de Acción Cooperativa es el único organismo competente para editar cooperativas o mutuales. Si no está bien cargada debe comunicarse con ellos.");
            }
            this.usuarioExternoFacade.edit(this.getId(), this.getIdTipoIniciador(), this.getApellido(),
                    this.getNombre().toUpperCase(), this.getDocumento(), this.getMatriculaProvincial(), this.getMatriculaNacional(),
                    this.getEmail(), this.getUss(), this.getPass(), this.getUsuario());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorUsuarioExterno");
            this.setMsgSuccessError("El usuario externo ha sido editado con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuarioExterno");
        }
        return this.getResultado();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String nuevoUsuarioExtPF() {
        String usuarioExt = "externo" + this.usuarioExternoFacade.findUltimoID().toString();
        this.limpiar();
        this.setIdTipoIniciador(4L);
        this.setUss(usuarioExt);
        this.setPass(usuarioExt);
        return "nuevoUsuarioExternoPF";
    }

    public String nuevoUsuarioExtPJ() {
        String usuarioExt = "externo" + this.usuarioExternoFacade.findUltimoID().toString();
        this.limpiar();
        this.setUss(usuarioExt);
        this.setPass(usuarioExt);
        return "nuevoUsuarioExternoPJ";
    }

    public void validarDocumento(FacesContext context, UIComponent validate, Object value) {
        String documentoNuevo = (String) value;
        if (!(documentoNuevo.equals(this.getDocumentoViejo()))) {
            Long items = this.usuarioExternoFacade.countByDocumento(documentoNuevo);

            if (!items.equals(0L)) {
                FacesMessage msg = new FacesMessage("El valor ingresado ya se encuentra en el sistema.");
                context.addMessage(validate.getClientId(context), msg);
                throw new ValidatorException(msg);
            }
        }
    }

    public void validarMatriculaProvincial(FacesContext context, UIComponent validate, Object value) {
        String matriculaProvincialNueva = (String) value;
        if (!(matriculaProvincialNueva.equals(this.getMatriculaProvincialViejo()))) {
            Long items = this.usuarioExternoFacade.countByMatriculaProvincial(matriculaProvincialNueva);

            if (!items.equals(0L)) {
                FacesMessage msg = new FacesMessage("El valor ingresado ya se encuentra en el sistema.");
                context.addMessage(validate.getClientId(context), msg);
                throw new ValidatorException(msg);
            }
        }
    }

    public void validarMatriculaNacional(FacesContext context, UIComponent validate, Object value) {
        String matriculaNacionalNueva = (String) value;
        if (!(matriculaNacionalNueva.equals(this.getMatriculaNacionalViejo()))) {
            Long items = this.usuarioExternoFacade.countByMatriculaNacional(matriculaNacionalNueva);

            if (!items.equals(0L)) {
                FacesMessage msg = new FacesMessage("El valor ingresado ya se encuentra en el sistema.");
                context.addMessage(validate.getClientId(context), msg);
                throw new ValidatorException(msg);
            }
        }
    }

    public void coincidenciaNombre(FacesContext context, UIComponent validate, Object value) {
        String nombreUss = (String) value;
        if (!(nombreUss.equals(this.getUssViejo()))) {
            List<Usuario> lista = this.usuarioFacade.findIdentico(nombreUss);

            if (!lista.isEmpty()) {
                FacesMessage msg = new FacesMessage("Ese nombre de ususario ya se encuentra en el sistema.");
                context.addMessage(validate.getClientId(context), msg);
                throw new ValidatorException(msg);
            }
        }
    }

    @Override
    public void prepararParaEditar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<UsuarioExterno> getListElements() {
        return (List<UsuarioExterno>) this.getList();
    }

    @Override
    public void actualizar() {
        this.setDocumentoBuscar("");
        this.setMatriculaProvincialBuscar("");
        this.setMatriculaNacionalBuscar("");
        this.setNombreBuscar("");
        this.setIdTipoIniciadorBuscar(null);
        this.setIdBuscar(null);
        this.aplicarFiltro();
    }

    public List<SelectItem> getSelectTipoIniciador() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<TipoIniciador> lstTipoIniciador = this.tipoIniciadorFacade.findAll(true);
        if (!(lstTipoIniciador.isEmpty())) {
            for (TipoIniciador tipoIniciadorAux : lstTipoIniciador) {
                if (tipoIniciadorAux.isEstado() && tipoIniciadorAux.isExterno() && !tipoIniciadorAux.getNombre().equals("PERSONA")) {
                    selectItems.add(new SelectItem(tipoIniciadorAux.getId(), tipoIniciadorAux.getNombre()));
                }

            }
        }
        return selectItems;
    }

    public List<SelectItem> getSelectTipoIniciadorBusqueda() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<TipoIniciador> lstTipoIniciador = this.tipoIniciadorFacade.findAll(true);
        if (!(lstTipoIniciador.isEmpty())) {
            for (TipoIniciador tipoIniciadorAux : lstTipoIniciador) {
                if (tipoIniciadorAux.isEstado() && tipoIniciadorAux.isExterno()) {
                    selectItems.add(new SelectItem(tipoIniciadorAux.getId(), tipoIniciadorAux.getNombre()));
                }

            }
        }
        return selectItems;
    }

    @Override
    public LazyDataModel<UsuarioExterno> getItems() {
        LazyDataModel<UsuarioExterno> usuariosExternos = new LazyDataModel<UsuarioExterno>() {

            @Override
            public List<UsuarioExterno> load(int first, int pageSize,
                    final String sortField, final SortOrder sortOrder, final Map<String, FilterMeta> filters) {

                List<UsuarioExterno> usuariosExternos = usuarioExternoFacade.findAll(getIdBuscar(), desacentuar(getNombreBuscar().toUpperCase()),
                        getDocumentoBuscar(), getMatriculaProvincialBuscar(), getMatriculaNacionalBuscar(), getIdTipoIniciadorBuscar(), first, pageSize);

                return usuariosExternos;
            }

        };
        usuariosExternos.setRowCount(usuarioExternoFacade.countAll(getIdBuscar(), desacentuar(getNombreBuscar().toUpperCase()),
                getDocumentoBuscar(), getMatriculaProvincialBuscar(), getMatriculaNacionalBuscar(), getIdTipoIniciadorBuscar()).intValue());
        return usuariosExternos;
    }

}
