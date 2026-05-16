package org.ccpm.sysse.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.ccpm.sysse.entity.Mesa;
import org.ccpm.sysse.entity.Organismo;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.facade.GrupoFacadeLocal;
import org.ccpm.sysse.facade.MesaFacadeLocal;
import org.ccpm.sysse.facade.OrganismoFacadeLocal;
import org.ccpm.sysse.facade.UsuarioFacadeLocal;
import org.ccpm.sysse.utilidad.GrupoEnum;
import org.ccpm.sysse.utilidad.OrganismoEnum;
import org.primefaces.model.FilterMeta;
import org.ccpm.sysse.web.compat.LazyDataModel;
import org.primefaces.model.SortOrder;

@Named
@SessionScoped
public class UsuarioManagedBean extends UtilManagedBean implements Serializable {

    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private MesaFacadeLocal mesaFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    private Usuario usuario;
    private Usuario usuarioAux;
    private String nombre;
    private String claveAcceso;
    private String apellido;
    private String nombreUsuario;
    private String ussViejo;
    private String correo;
    private Long dni;
    private Long idMesa;
    private Long idOrganismo;
    private boolean restorePassword;
    private boolean asignarMesasDisponibles;
    private List<String> lstGrupoSeleccionado;
    private List<ActionItem> lstActionItems = new ArrayList<>();
    private LazyDataModel<Usuario> usuarios = new LazyDataModel<Usuario>() {
        @Override
        public List<Usuario> load(int i, int i1, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterMeta) {
            return new ArrayList<>();
        }
    };
    private List<Long> idItems = new ArrayList<>();
    private List<Long> idMesasDisponibles = new ArrayList<>();

    /*
     * Búsqueda
     */
    private String nomBusq;
    private String apellidoBusq;
    private Long idMesaBsq;
    private Long idOrganismoBsq;

    /**
     * Creates a new instance of UsuarioManagedBean
     */
    public UsuarioManagedBean() {

    }

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
            this.setUsuario(sessionBean.getUsuario());
            try {
                this.prepararAcciones(sessionBean.getUsuario());
            } catch (Exception e) {
            }
        }
        this.aplicarFiltro();
    }

    public List<Long> getIdItems() {
        return idItems;
    }

    public void setIdItems(List<Long> idItems) {
        this.idItems = idItems;
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
                        case "nuevoUsuario":
                            this.setAlta(true);
                            break;
                        case "editarUsuario":
                            this.setModificacion(true);
                            break;
                        case "borrarUsuario":
                            this.setBaja(true);
                            break;
                        case "detalleUsuario":
                            this.setDetalle(true);
                            break;
                        case "restorePassword":
                            this.setRestorePassword(true);
                            break;
                        case "asignarMesasDisponibles":
                            this.setAsignarMesasDisponibles(true);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public void setLstGrupoSeleccionado(List<String> lstGrupoSeleccionado) {
        this.lstGrupoSeleccionado = lstGrupoSeleccionado;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setIdMesa(Long idMesa) {
        this.idMesa = idMesa;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }

    public Long getIdMesa() {
        return idMesa;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<String> getLstGrupoSeleccionado() {
        return lstGrupoSeleccionado;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNomBusq() {
        return nomBusq;
    }

    public void setNomBusq(String nomBusq) {
        this.nomBusq = nomBusq;
    }

    public String getApellidoBusq() {
        return apellidoBusq;
    }

    public void setApellidoBusq(String apellidoBusq) {
        this.apellidoBusq = apellidoBusq;
    }

    public String getUssViejo() {
        return ussViejo;
    }

    public void setUssViejo(String ussViejo) {
        this.ussViejo = ussViejo;
    }

    public Long getDni() {
        return dni;
    }

    public void setDni(Long dni) {
        this.dni = dni;
    }

    public Usuario getUsuarioAux() {
        return usuarioAux;
    }

    public void setUsuarioAux(Usuario usuarioAux) {
        this.usuarioAux = usuarioAux;
    }

    public boolean isRestorePassword() {
        return restorePassword;
    }

    public void setRestorePassword(boolean restorePassword) {
        this.restorePassword = restorePassword;
    }

    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }

    public Long getIdMesaBsq() {
        return idMesaBsq;
    }

    public void setIdMesaBsq(Long idMesaBsq) {
        this.idMesaBsq = idMesaBsq;
    }

    public Long getIdOrganismoBsq() {
        return idOrganismoBsq;
    }

    public void setIdOrganismoBsq(Long idOrganismoBsq) {
        this.idOrganismoBsq = idOrganismoBsq;
    }

    public LazyDataModel<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(LazyDataModel<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public List<Long> getIdMesasDisponibles() {
        return idMesasDisponibles;
    }

    public void setIdMesasDisponibles(List<Long> idMesasDisponibles) {
        this.idMesasDisponibles = idMesasDisponibles;
    }

    public boolean isAsignarMesasDisponibles() {
        return asignarMesasDisponibles;
    }

    public void setAsignarMesasDisponibles(boolean asignarMesasDisponibles) {
        this.asignarMesasDisponibles = asignarMesasDisponibles;
    }

    public List<SelectItem> getSelectItemsOrganismo() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Organismo> lstOrganismo = new ArrayList<>();
        if (usuario.getGrupos().contains(grupoFacade.find(GrupoEnum.SUPER_ADMIN.getId()))) {
            lstOrganismo = this.organismoFacade.findAll(true);
        } else {
            lstOrganismo.add(this.getUsuario().getOrganismo());
            if (this.getUsuario().getOrganismo().isAdministraSubOrganismos()) {
                lstOrganismo.addAll(this.getUsuario().getOrganismo().getOrganismos());
                for (Organismo organismoAux : this.getUsuario().getOrganismo().getOrganismos()) {
                    lstOrganismo.addAll(organismoAux.getOrganismos());
                }
            }
        }
        if (!(lstOrganismo.isEmpty())) {
            lstOrganismo.stream().forEach((organismoAux) -> {
                selectItems.add(new SelectItem(organismoAux.getId(), organismoAux.getCodOrganigrama() + " - " + organismoAux.getNombre()));
            });
        }
        return selectItems;
    }

    public List<SelectItem> getSelectItemsOrganismoBsq() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Organismo> lstOrganismo = new ArrayList<>();
        if (usuario.getGrupos().contains(grupoFacade.find(GrupoEnum.SUPER_ADMIN.getId()))) {
            lstOrganismo = this.organismoFacade.findAll(true);
        } else {
            lstOrganismo.add(this.getUsuario().getOrganismo());
            if (this.getUsuario().getOrganismo().isAdministraSubOrganismos()) {
                lstOrganismo.addAll(this.getUsuario().getOrganismo().getOrganismos());
                for (Organismo organismoAux : this.getUsuario().getOrganismo().getOrganismos()) {
                    lstOrganismo.addAll(organismoAux.getOrganismos());
                }
            }
        }
        if (!(lstOrganismo.isEmpty())) {
            lstOrganismo.stream().forEach((organismoAux) -> {
                selectItems.add(new SelectItem(organismoAux.getId(), organismoAux.getCodOrganigrama() + " - " + organismoAux.getNombre()));
            });
        }
        return selectItems;
    }

    public List<SelectItem> getSelectItemsMesa() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Mesa> lstMesa = new ArrayList<>();
        if (this.getIdOrganismo() != null && this.getIdOrganismo() != 0L) {
            lstMesa = this.mesaFacade.findByOrganismo(this.getIdOrganismo());
        }
        if (this.getIdOrganismo() != null && this.getIdOrganismo() != 0L) {
            lstMesa = this.mesaFacade.findByOrganismo(this.getIdOrganismo());
        }
        if (!(lstMesa.isEmpty())) {
            for (Mesa mesaAux : lstMesa) {
                selectItems.add(new SelectItem(mesaAux.getId(), mesaAux.getNombre()));
            }
        }
        return selectItems;
    }

    public List<SelectItem> getSelectItemsMesaBsq() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Mesa> lstMesa = new ArrayList<>();
        if (this.getIdOrganismoBsq() != null && this.getIdOrganismoBsq() != 0L) {
            lstMesa = this.mesaFacade.findByOrganismo(this.getIdOrganismoBsq());
        }
        if (this.getIdOrganismo() != null && this.getIdOrganismo() != 0L) {
            lstMesa = this.mesaFacade.findByOrganismo(this.getIdOrganismo());
        }
        if (!(lstMesa.isEmpty())) {
            for (Mesa mesaAux : lstMesa) {
                selectItems.add(new SelectItem(mesaAux.getId(), mesaAux.getNombre()));
            }
        }
        return selectItems;
    }

    public boolean perteneceContaduria() {
        return usuarioFacade.isPerteneceContaduria(this.getIdMesa());
    }

    @Override
    public List<Usuario> getListElements() {
        return (List<Usuario>) this.getList();
    }

    @Override
    public void actualizar() {
        this.setNomBusq(null);
        this.setApellidoBusq(null);
        this.setNombreBuscar(null);
        this.setIdOrganismoBsq(null);
        this.setIdMesaBsq(null);
        this.setDni(null);
        super.actualizar();
    }

    @Override
    public void limpiar() {
        this.setUsuarioAux(null);
        this.setNombre(null);
        this.setClaveAcceso(null);
        this.setApellido(null);
        this.setDni(null);
        this.setNombreUsuario(null);
        this.setUssViejo(null);
        this.setCorreo(null);
        this.setIdMesa(null);
        this.setIdOrganismo(null);
        this.setLstGrupoSeleccionado(null);
        this.setLstActionItems(null);
        this.setIdMesasDisponibles(new ArrayList<>());
    }

    @Override
    public String crear() {
        try {
            List<Grupo> lstAux = this.grupoFacade.finGrupos(idItems);
            List<Mesa> lstMesasAux = new ArrayList<>();
            if (this.getIdMesasDisponibles() != null && !this.getIdMesasDisponibles().isEmpty()) {
                lstMesasAux = this.mesaFacade.findMesas(this.getIdMesasDisponibles());
            }
            this.usuarioFacade.create(this.getApellido(), this.getNombre(),
                    this.getNombreUsuario(), this.getCorreo(), lstAux, this.getIdMesa(), this.getDni(), lstMesasAux);
            this.limpiar();
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorUsuario");
            this.setMsgSuccessError("El usuario ha sido generado con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuario");
            this.limpiar();
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try {
            List<Grupo> lstAux = this.grupoFacade.finGrupos(idItems);
            List<Mesa> lstMesasAux = new ArrayList<>();
            if (this.getIdMesasDisponibles() != null && !this.getIdMesasDisponibles().isEmpty()) {
                lstMesasAux = this.mesaFacade.findMesas(this.getIdMesasDisponibles());
            }
            this.usuarioFacade.create(this.getApellido(), this.getNombre(),
                    this.getNombreUsuario(), this.getCorreo(),
                    lstAux,
                    this.getIdMesa(), this.getDni(), lstMesasAux);
            this.setResultado("nuevoUsuario");
            this.setNombre(null);
            this.setClaveAcceso(null);
            this.setApellido(null);
            this.setNombreUsuario(null);
            this.setUssViejo(null);
            this.setCorreo("-");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuario");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idUsuarioAux = Long.parseLong(myRequest.getParameter("id"));
        Usuario usuarioDetalle = this.usuarioFacade.find(idUsuarioAux);
        this.setUsuarioAux(usuarioDetalle);
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
            Long idUsuarioAux = Long.parseLong(myRequest.getParameter("id"));
            this.usuarioFacade.remove(idUsuarioAux);
            this.limpiar();
            this.setResultado("usuarioConf");
        } catch (Exception ex) {
            this.limpiar();
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuario");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idUsuarioAux = Long.parseLong(myRequest.getParameter("id"));
        Usuario usuarioEditar = this.usuarioFacade.find(idUsuarioAux);
        this.setId(usuarioEditar.getId());
        this.setNombreUsuario(usuarioEditar.getNombreUsuario());
        this.setUssViejo(usuarioEditar.getNombreUsuario());
        this.setClaveAcceso(usuarioEditar.getClaveAcceso());
        this.setCorreo(usuarioEditar.getEmail());
        this.setApellido(usuarioEditar.getApellido());
        this.setNombre(usuarioEditar.getNombre());
        this.gruposDisponibles(usuarioEditar);
        this.gruposSeleccionados(usuarioEditar);
        this.setDni(usuarioEditar.getDni());
        this.setIdOrganismo(usuarioEditar.getMesa().getOrganismo().getId());
        this.setIdMesa(usuarioEditar.getMesa().getId());
        this.setIdMesasDisponibles(new ArrayList<>());
        for (Mesa mesaAux : usuarioEditar.getMesasDisponibles()) {
            this.getIdMesasDisponibles().add(mesaAux.getId());
        }
    }

    public void gruposSeleccionados(Usuario usuario) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<Grupo> listGrupos = null;
        if (usuario != null) {
            listGrupos = usuario.getGrupos();

            if (!(listGrupos.isEmpty())) {
                for (Grupo grupoAux : listGrupos) {
                    selectItems.add(new SelectItem(grupoAux.getId(), grupoAux.getNombre()));
                }
            }
        }
        this.setItemsDerecha(selectItems);
    }

    public void gruposDisponibles(Usuario usuario) {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Grupo> listGruposUsuario = null;
        List<Grupo> listGrupos = new ArrayList<>();
        if (usuario.getGrupos().contains(grupoFacade.find(GrupoEnum.SUPER_ADMIN.getId()))) {
            listGrupos = this.grupoFacade.findAll(true);
        } else {
            listGrupos = this.grupoFacade.finGrupos(Arrays.asList(GrupoEnum.BUSQUEDA_AVANZADA.getId(), GrupoEnum.MESA_GENERAL.getId(), GrupoEnum.MESA_INTERNA.getId(),GrupoEnum.MESA_INTERNA_BASICA.getId(), GrupoEnum.USUARIO_EXTERNO.getId(),GrupoEnum.MESA_GENERAL_SIN_INGRESO_EXTERNO.getId()));
        }
        if (usuario != null) {
            listGruposUsuario = usuario.getGrupos();
            listGrupos.removeAll(listGruposUsuario);
        }
        if (!(listGrupos.isEmpty())) {
            for (Grupo grupoAux : listGrupos) {
                selectItems.add(new SelectItem(grupoAux.getId(), grupoAux.getNombre()));
            }
        }
        this.setItemsIzquierda(selectItems);
    }

    @Override
    public String guardarEdicion() {
        try {
            List<Long> lstAux = new ArrayList<>();
            for (SelectItem object : this.getItemsDerecha()) {
                lstAux.add((Long) object.getValue());
            }
            List<Mesa> lstMesasAux = new ArrayList<>();
            if (this.getIdMesasDisponibles() != null && !this.getIdMesasDisponibles().isEmpty()) {
                lstMesasAux = this.mesaFacade.findMesas(this.getIdMesasDisponibles());
            }
            usuarioFacade.edit(this.getId(), this.getApellido(), this.getNombre(),
                    this.getNombreUsuario(), this.getCorreo(), lstAux,
                    this.getIdMesa(), this.getDni(), lstMesasAux);
            this.limpiar();
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorUsuario");
            this.setMsgSuccessError("El usuario ha sido editado con éxito.");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuario");
        }
        return this.getResultado();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<Grupo> listGrupo = new ArrayList<>();
        if (usuario.getGrupos().contains(grupoFacade.find(GrupoEnum.SUPER_ADMIN.getId()))) {
            listGrupo = this.grupoFacade.findAll(true);
        } else {
            listGrupo = this.grupoFacade.finGrupos(Arrays.asList(GrupoEnum.BUSQUEDA_AVANZADA.getId(),
                    GrupoEnum.MESA_GENERAL.getId(), GrupoEnum.MESA_INTERNA.getId(), GrupoEnum.USUARIO_EXTERNO.getId(),
                    GrupoEnum.USUARIO_EXTERNO_ALM.getId(), GrupoEnum.MESA_INTERNA_INGRESO.getId(),GrupoEnum.MESA_GENERAL_SIN_INGRESO_EXTERNO.getId()));
        }
        if (usuario.getMesa() != null && usuario.getMesa().getOrganismo().getCodOrganigrama().equals(OrganismoEnum.CONTADURIA.getCodigo())) {
            listGrupo.add(this.grupoFacade.find(GrupoEnum.CONTADURIA.getId()));
        }
        if (!(listGrupo.isEmpty())) {
            for (Grupo grupoAux : listGrupo) {
                selectItems.add(new SelectItem(grupoAux.getId(), grupoAux.getNombre()));
            }
        }
        return selectItems;
    }

    public List<SelectItem> getSelectItemsMesasDisponibles() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<Mesa> mesas = new ArrayList<>();
        if (this.getUsuario().getMesa() != null) {
            mesas = this.mesaFacade.findAllGeneral(this.getUsuario().getMesa().getOrganismo().getId());
        } else {
            mesas = this.mesaFacade.findAllGeneral(null);
        }
        for (Mesa mesa : mesas) {
            selectItems.add(new SelectItem(mesa.getId(), mesa.getNombre()));
        }

        return selectItems;
    }

    public void validarEmail(FacesContext context, UIComponent validate, Object value) {
        String email = (String) value;

        if (email == null || email.isEmpty()) {
            final FacesMessage msg = new FacesMessage("Email es requerido.");
            context.addMessage(validate.getClientId(context), msg);
            throw new ValidatorException(msg);
        }
        // Patrón para validar el email
        Pattern pattern = Pattern
                .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher mather = pattern.matcher(email);
        if (!mather.find() == true) {
            final FacesMessage msg = new FacesMessage("Email no tiene formato correcto.");
            context.addMessage(validate.getClientId(context), msg);
            throw new ValidatorException(msg);
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

    public String retorePassword() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            this.usuarioFacade.restorePassword(idAux);
            Usuario usuarioAux = this.usuarioFacade.find(idAux);
            this.setTitle("Proceso completo...");
            this.setImages("images/symbolChecks.png");
            this.setMsgSuccessError("Se ha restaurado la contraseña con éxito. Se le ha enviado a su correo electrónico la información para iniciar sesión.");
            this.setResultado("successErrorUsuario");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuario");
        }
        return this.getResultado();
    }

    @Override
    public void aplicarFiltro() {
        LazyDataModel<Usuario> usuarios = new LazyDataModel<Usuario>() {

            @Override
            public List<Usuario> load(int first, int pageSize,
                    final String sortField, final SortOrder sortOrder, final Map<String, FilterMeta> filters) {

                List<Usuario> usuarios = usuarioFacade.findAll(desacentuar(getNombreBuscar()),
                        desacentuar(getNomBusq()), desacentuar(getApellidoBusq()),
                        getIdMesaBsq(), getIdOrganismoBsq(),
                        usuario.getGrupos().contains(grupoFacade.find(GrupoEnum.SUPER_ADMIN.getId())) ? null : getUsuario().getMesa().getOrganismo(), first, pageSize);

                return usuarios;
            }

        };
        usuarios.setRowCount(usuarioFacade.countAll(desacentuar(getNombreBuscar()),
                desacentuar(getNomBusq()), desacentuar(getApellidoBusq()),
                getIdMesaBsq(), getIdOrganismoBsq(),
                usuario.getGrupos().contains(grupoFacade.find(GrupoEnum.SUPER_ADMIN.getId())) ? null : getUsuario().getMesa().getOrganismo()).intValue());
        this.setUsuarios(usuarios);
    }

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
