package org.ccpm.sysse.web;

import java.io.Serializable;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.Grupo;
import org.ccpm.sysse.entity.Menu;
import org.ccpm.sysse.entity.Mesa;
import org.ccpm.sysse.entity.Organismo;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.facade.GrupoFacadeLocal;
import org.ccpm.sysse.facade.MesaFacadeLocal;
import org.ccpm.sysse.facade.UsuarioFacadeLocal;
import org.ccpm.sysse.utilidad.UserActivityLogger;

@Named
@SessionScoped
public class WebManagedBean implements Serializable {

    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private MesaFacadeLocal mesaFacade;

    @Inject
    BeanManager beanManager;

    private String nombreUsuario;
    private String claveAcceso;
    private String resultado;
    private String msgSuccessError;
    private String title;
    private String images;
    private Usuario usuario;
    private Organismo organismo;
    private Mesa mesa;
    private List<Menu> menu;
    private String nombreGrupo;
    //* Atributos para cambio de contraseña */
    private String claveNueva;
    private String claveConfirma;
    private String claveRepite;
    private Long idUsuario;
    private String year;

    /* fin atributos cambio de contraseña */
    /**
     * Creates a new instance of WebManagedBean
     */
    public WebManagedBean() {

    }

    @PostConstruct
    public void init() {
        Date date = new Date();

        ZoneId timeZone = ZoneId.systemDefault();
        LocalDate getLocalDate = date.toInstant().atZone(timeZone).toLocalDate();
        this.setYear(String.valueOf(getLocalDate.getYear()));

    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setMsgSuccessError(String msgSuccessError) {
        this.msgSuccessError = msgSuccessError;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setNombreGrupo(String nombreGrupo) {
        this.nombreGrupo = nombreGrupo;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public String getImages() {
        return images;
    }

    public String getMsgSuccessError() {
        return msgSuccessError;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getResultado() {
        return resultado;
    }

    public String getTitle() {
        return title;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Organismo getOrganismo() {
        return organismo;
    }

    public void setOrganismo(Organismo organismo) {
        this.organismo = organismo;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public String getClaveNueva() {
        return claveNueva;
    }

    public void setClaveNueva(String claveNueva) {
        this.claveNueva = claveNueva;
    }

    public String getClaveConfirma() {
        return claveConfirma;
    }

    public void setClaveConfirma(String claveConfirma) {
        this.claveConfirma = claveConfirma;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getClaveRepite() {
        return claveRepite;
    }

    public void setClaveRepite(String claveRepite) {
        this.claveRepite = claveRepite;
    }

    public List<Menu> getMenu() {
        return menu;
    }

    public void setMenu(List<Menu> menu) {
        this.menu = menu;
    }

    /**
     * Devuelve el outcome de navegación para un ítem del menú.
     * Si el link viene vacío o null desde la BD, infiere el outcome por el menú padre (sin tocar la BD).
     */
    public String getMenuOutcome(Menu subItem, Menu parent) {
        if (subItem != null && !isLinkEmptyOrPlaceholder(subItem.getLink())) {
            String link = subItem.getLink().trim();
            if ("seguridad".equalsIgnoreCase(link)) {
                return "configuracionConf";
            }
            if ("configuracion".equalsIgnoreCase(link)) {
                return "configuracionConf";
            }
            return link;
        }
        if (parent != null && parent.getNombre() != null) {
            String parentNombre = parent.getNombre().trim().toUpperCase();
            if ("EXPEDIENTES".equals(parentNombre)) {
                return "expedienteConf";
            }
            if ("NOTAS".equals(parentNombre)) {
                return "busquedaNotas";
            }
            if ("BÚSQUEDA AVANZADA".equals(parentNombre) || "BUSQUEDA AVANZADA".equals(parentNombre)) {
                return "busquedaConf";
            }
            if ("SEGURIDAD".equals(parentNombre)) {
                return "configuracionConf";
            }
        }
        return "";
    }

    /** Considera vacío o placeholder (ej. "-") un link que no debe usarse como outcome. */
    private static boolean isLinkEmptyOrPlaceholder(String link) {
        if (link == null) {
            return true;
        }
        String t = link.trim();
        return t.isEmpty() || "-".equals(t);
    }

    /**
     * Devuelve el outcome para hacer del menú padre un link (primera opción en cada submenú).
     * Si el padre tiene link en BD lo usa; si no, infiere por nombre.
     */
    public String getParentMenuOutcome(Menu parent) {
        if (parent == null) {
            return "";
        }
        if (!isLinkEmptyOrPlaceholder(parent.getLink())) {
            String link = parent.getLink().trim();
            if ("seguridad".equalsIgnoreCase(link)) {
                return "configuracionConf";
            }
            if ("configuracion".equalsIgnoreCase(link)) {
                return "configuracionConf";
            }
            return link;
        }
        if (parent.getNombre() == null) {
            return "";
        }
        String n = parent.getNombre().trim().toUpperCase();
        switch (n) {
            case "EXPEDIENTES":
                return "expedienteConf";
            case "NOTAS":
                return "busquedaNotas";
            case "BÚSQUEDA AVANZADA":
            case "BUSQUEDA AVANZADA":
                return "busquedaConf";
            case "REPORTES":
                return "reporteConf";
            case "MANUAL DE USUARIO":
                return "manualConf";
            case "USUARIO EXTERNO":
                return "usuarioExternoConf";
            case "ORGANISMOS":
                return "organismoConf";
            case "MESAS":
                return "mesaConf";
            case "CONFIGURACIÓN INTERNA":
            case "CONFIGURACION INTERNA":
                return "configuracionConf";
            case "SEGURIDAD":
                return "configuracionConf";
            default:
                return "";
        }
    }

    public boolean shouldRenderSubMenuItem(Menu subItem, Menu parent) {
        if (subItem == null) {
            return false;
        }
        if (parent == null) {
            return true;
        }
        String subName = normalizeMenuName(subItem.getNombre());
        String parentName = normalizeMenuName(parent.getNombre());
        return !subName.equals(parentName);
    }

    public boolean hasVisibleSubMenuItems(Menu parent) {
        if (parent == null || parent.getMenus() == null || parent.getMenus().isEmpty()) {
            return false;
        }
        for (Menu subItem : parent.getMenus()) {
            if (shouldRenderSubMenuItem(subItem, parent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Devuelve la clase del icono PrimeIcons para un ítem del menú según su nombre.
     */
    public String getMenuIcon(String nombre) {
        if (nombre == null) {
            return "pi pi-circle";
        }
        String n = normalizeMenuName(nombre);
        switch (n) {
            case "BÚSQUEDA AVANZADA":
            case "BUSQUEDA AVANZADA":
                return "pi pi-search";
            case "REPORTES":
                return "pi pi-chart-bar";
            case "MANUAL DE USUARIO":
                return "pi pi-book";
            case "USUARIO EXTERNO":
                return "pi pi-users";
            case "ORGANISMOS":
                return "pi pi-building";
            case "MESAS":
                return "pi pi-sitemap";
            case "NOTAS":
                return "pi pi-file-edit";
            case "EXPEDIENTES":
                return "pi pi-briefcase";
            case "CONFIGURACIÓN INTERNA":
            case "CONFIGURACION INTERNA":
            case "SEGURIDAD":
                return "pi pi-cog";
            default:
                return "pi pi-circle";
        }
    }

    private static String normalizeMenuName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.trim().toUpperCase();
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String login() {
        try {
            this.removeSessionBean(false);
            Usuario usuarioAux = usuarioFacade.find(this.getNombreUsuario(), this.getClaveAcceso());
            this.setUsuario(usuarioAux);
            this.cargarMenu(usuarioAux);
            if (usuarioAux.getMesa() != null) {
                this.setMesa(this.usuarioFacade.findMesaUsuario(this.getUsuario().getId()));
                this.setOrganismo(this.usuarioFacade.findOrganismoUsuario(this.getUsuario().getId()));
            }
            
            // Registrar login exitoso
            String usuarioInfo = usuarioAux.getNombreUsuario() + " (" + 
                                (usuarioAux.getNombre() != null ? usuarioAux.getNombre() : "") + " " +
                                (usuarioAux.getApellido() != null ? usuarioAux.getApellido() : "") + ")";
            
            String organismoInfo = null;
            if (this.getOrganismo() != null) {
                organismoInfo = (this.getOrganismo().getCodOrganigrama() != null ? 
                               this.getOrganismo().getCodOrganigrama() + " - " : "") +
                               (this.getOrganismo().getNombre() != null ? 
                               this.getOrganismo().getNombre() : "Sin nombre");
            } else if (usuarioAux.getOrganismo() != null) {
                organismoInfo = (usuarioAux.getOrganismo().getCodOrganigrama() != null ? 
                               usuarioAux.getOrganismo().getCodOrganigrama() + " - " : "") +
                               (usuarioAux.getOrganismo().getNombre() != null ? 
                               usuarioAux.getOrganismo().getNombre() : "Sin nombre");
            }
            
            UserActivityLogger.logActivity(usuarioInfo, "LOGIN EXITOSO - Inicio de sesión", organismoInfo);
            
            if (usuarioAux.isUpdatedPassword()) {
                this.setIdUsuario(usuarioAux.getId());
                this.setResultado("cambiarContrasenia");
            } else {
                this.setResultado("administracion");
            }
        } catch (Exception ex) {
            this.testConnection();
            System.out.println("Salida: " + ex.getMessage());
            
            // Registrar intento de login fallido
            UserActivityLogger.logActivity(this.getNombreUsuario(), "LOGIN FALLIDO - Error: " + ex.getMessage());
            
            this.setResultado("successErrorLogin");
        }
        return resultado;
    }

    public void logout() {
        // Registrar logout antes de limpiar la sesión
        if (this.getUsuario() != null) {
            String usuarioInfo = this.getUsuario().getNombreUsuario() + " (" + 
                                (this.getUsuario().getNombre() != null ? this.getUsuario().getNombre() : "") + " " +
                                (this.getUsuario().getApellido() != null ? this.getUsuario().getApellido() : "") + ")";
            
            String organismoInfo = null;
            if (this.getOrganismo() != null) {
                organismoInfo = (this.getOrganismo().getCodOrganigrama() != null ? 
                               this.getOrganismo().getCodOrganigrama() + " - " : "") +
                               (this.getOrganismo().getNombre() != null ? 
                               this.getOrganismo().getNombre() : "Sin nombre");
            } else if (this.getUsuario().getOrganismo() != null) {
                organismoInfo = (this.getUsuario().getOrganismo().getCodOrganigrama() != null ? 
                               this.getUsuario().getOrganismo().getCodOrganigrama() + " - " : "") +
                               (this.getUsuario().getOrganismo().getNombre() != null ? 
                               this.getUsuario().getOrganismo().getNombre() : "Sin nombre");
            }
            
            UserActivityLogger.logActivity(usuarioInfo, "LOGOUT - Cierre de sesión", organismoInfo);
        }
        
        this.limpiar();
        this.removeSessionBean(true);
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    }

    public void removeSessionBean(boolean isLogout) {
        {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("expedienteManagedBean");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("mesaManagedBean");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("organismoManagedBean");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("usuarioExternoManagedBean");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("busquedaUsExManagedBean");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("notaManagedBean");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("accionManagedBean");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("menuManagedBean");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("grupoManagedBean");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("usuarioManagedBean");

            if (isLogout) { //Si es logout se limpia el managedBean sino tira error
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("webManagedBean");
            }
        }
    }

    public void cargarMenu(Usuario usuario) {
        this.setMenu(new ArrayList<>());
        List<Grupo> listGrupo = usuario.getGrupos();
        listGrupo.stream().map((grupoAux) -> grupoAux.getMenus().stream().sorted(Comparator.comparing(Menu::getId)).collect(Collectors.toList())).forEach((listMenu) -> {
            this.menu.addAll(listMenu);
        });
        menu.addAll(usuario.getMenus());
    }

    public void limpiar() {
        this.setNombreUsuario(null);
        this.setClaveAcceso(null);
        this.setUsuario(null);
        this.setOrganismo(null);
        this.setMesa(null);
    }

    public String usuarioPass() {
        try {
            if (this.getClaveConfirma().equals(this.getUsuario().getClaveAcceso())) {
                if (!this.getClaveNueva().equals(this.getClaveRepite())) {
                    this.setTitle("¡Error!");
                    this.setImages("images/symbolError.png");
                    this.setMsgSuccessError("La nueva clave no coincide con la repetición.");
                    this.setResultado("successErrorCambio");
                    this.limpiar();
                    throw new Exception("La nueva clave no coincide con la repetición.");
                } else {
                    this.usuarioFacade.edit(this.getIdUsuario(), this.getClaveConfirma(),
                            this.getClaveNueva());
                    this.setResultado("successErrorCambio");
                    this.setMsgSuccessError("La contraseña ha sido editada con éxito.");
                    this.setTitle("Proceso Completo");
                    this.setImages("images/symbolChecks.png");
                    this.limpiarContrasenias();
                }
            } else {
                this.setTitle("¡Error!");
                this.setImages("images/symbolError.png");
                this.setMsgSuccessError("La clave actual no es correcta.");
                this.setResultado("successErrorConfiguracionConf");
                this.limpiarContrasenias();
                throw new Exception("La clave actual no es correcta.");

            }
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorConfiguracionConf");
            this.limpiarContrasenias();
        }
        return this.getResultado();
    }

    public void pass() {
        this.setIdUsuario(this.usuario.getId());
        this.setNombreUsuario(this.usuario.getNombreUsuario());
    }

    public void limpiarContrasenias() {
        this.setClaveConfirma(null);
        this.setClaveNueva(null);
        this.setClaveRepite(null);
    }

    public void coincidenciaClaveVieja(FacesContext context, UIComponent validate, Object value) {
        String claveVieja = (String) value;

        if (!this.getUsuario().getClaveAcceso().equals(claveVieja)) {
            FacesMessage msg = new FacesMessage("La clave ingresada no coincide con la clave actual.");
            context.addMessage(validate.getClientId(context), msg);
            throw new ValidatorException(msg);
        }
    }

    public void coincidenciaClaveNueva(FacesContext context, UIComponent validate, Object value) {
        String repite = (String) value;

        if (!this.claveNueva.equals(repite)) {
            FacesMessage msg = new FacesMessage("La repetición de la clave no coincide.");
            context.addMessage(validate.getClientId(context), msg);
            throw new ValidatorException(msg);
        }
    }

    public List<Mesa> getMesasDisponibles() {
        List<Mesa> mesas = new ArrayList<>();
        if (this.getUsuario().getMesasDisponibles() != null && !this.getUsuario().getMesasDisponibles().isEmpty()) {
            for (Mesa mesaux : this.getUsuario().getMesasDisponibles()) {
                mesas.add(mesaux);
            }
        }
        return mesas;
    }

    public void updateMesaUsuario(Long idMesa) {
        try {
            this.getUsuario().setMesa(this.mesaFacade.find(idMesa));
            this.getUsuario().setOrganismo(this.getUsuario().getMesa().getOrganismo());
            this.usuarioFacade.edit(this.getUsuario());
            this.setMesa(this.usuarioFacade.findMesaUsuario(this.getUsuario().getId()));
            this.setOrganismo(this.usuarioFacade.findOrganismoUsuario(this.getUsuario().getId()));
            this.setUsuario(this.usuarioFacade.find(this.getUsuario().getId()));
            System.out.println("Mesa de usuario: " + this.getUsuario().getMesa().getNombre());
            AlterableContext ctxSession = (AlterableContext) beanManager.getContext(SessionScoped.class);
            for (Bean<?> bean : beanManager.getBeans(ExpedienteManagedBean.class)) {
                Object instance = ctxSession.get(bean);
                if (instance != null) {
                    ctxSession.destroy(bean);
                }
            }
            for (Bean<?> bean : beanManager.getBeans(NotaManagedBean.class)) {
                Object instance = ctxSession.get(bean);
                if (instance != null) {
                    ctxSession.destroy(bean);
                }
            }
        } catch (Exception e) {
        }
    }

    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    public void testConnection() {
        Query query = em.createNativeQuery("SELECT 1");
        query.getSingleResult();
        System.out.println("✅ Conexión exitosa");
    }

}
