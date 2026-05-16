/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Organismo;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.facade.GrupoFacadeLocal;
import org.ccpm.dashboard.facade.OrganismoFacadeLocal;
import org.ccpm.dashboard.facade.UsuarioFacadeLocal;


/**
 *
 * @author Alvarenga Angel
 */
@Named
@SessionScoped
public class UsuarioManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private UsuarioFacadeLocal usuarioFacade; 
    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    private String apellido;
    private String nombre;
    private String uss;
    private String pass;
    private String email;
    private String claveAcceso;
    private String passConfirmar;
    private String nombreP;
    private String passActual;
    private Date fechaAlta;
    private Date fechaModif;
    private Date ultimoLogin;
    private Long idOrganismo=0L;
    private List <Long> idItems = new ArrayList<>();
    private List <ActionItem> lstActionItems = new ArrayList <>();
    private List <Grupo> lstGrupos = new ArrayList <>();
    private List <SelectItem> selecteditemsIzquierda;
    private List <SelectItem> selecteditemsDerecha;

    /** Creates a new instance of UsuarioManagedBean */
    public UsuarioManagedBean() {
        this.selecteditemsDerecha = new ArrayList <>();
        this.selecteditemsIzquierda = new ArrayList <>();
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
    
    
    public List<SelectItem> getSelectItemsOrg() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Organismo> lstOrganismo = this.organismoFacade.findAll(true);
        if (!(lstOrganismo.isEmpty())) {
            for (Organismo orgAux : lstOrganismo) {
                selectItems.add(new SelectItem(orgAux.getId(),orgAux.getCodOrganigrama()+ "-" +orgAux.getLeyenda()));
            }
        }
        return selectItems;
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoUsuario")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarUsuario")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarUsuario")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleUsuario")) {
                        this.setDetalle(true);
                    }
                    System.out.println(accionAux.getNombre());
                }
                
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    
    
    public String getPassConfirmar() {
        return passConfirmar;
    }

    public void setPassConfirmar(String passConfirmar) {
        this.passConfirmar = passConfirmar;
    }

    
    
    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }
    
    

    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }

    public String getNombreP() {
        return nombreP;
    }

    public void setNombreP(String nombreP) {
        this.nombreP = nombreP;
    }

    public String getPassActual() {
        return passActual;
    }

    public void setPassActual(String passActual) {
        this.passActual = passActual;
    }
    

    public List<Long> getIdItems() {
        return idItems;
    }

    public void setIdItems(List<Long> idItems) {
        this.idItems = idItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUss() {
        return uss;
    }

    public void setUss(String uss) {
        this.uss = uss;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public UsuarioFacadeLocal getUsuarioFacade() {
        return usuarioFacade;
    }

    public void setUsuarioFacade(UsuarioFacadeLocal usuarioFacade) {
        this.usuarioFacade = usuarioFacade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getFechaModif() {
        return fechaModif;
    }

    public void setFechaModif(Date fechaModif) {
        this.fechaModif = fechaModif;
    }

    public Date getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(Date ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public GrupoFacadeLocal getGrupoFacade() {
        return grupoFacade;
    }

    public void setGrupoFacade(GrupoFacadeLocal grupoFacade) {
        this.grupoFacade = grupoFacade;
    }

    public List<Grupo> getLstGrupos() {
        return lstGrupos;
    }

    public void setLstGrupos(List<Grupo> lstGrupos) {
        this.lstGrupos = lstGrupos;
    }

    public List<SelectItem> getSelecteditemsIzquierda() {
        return selecteditemsIzquierda;
    }

    public void setSelecteditemsIzquierda(List<SelectItem> selecteditemsIzquierda) {
        this.selecteditemsIzquierda = selecteditemsIzquierda;
    }

    public List<SelectItem> getSelecteditemsDerecha() {
        return selecteditemsDerecha;
    }

    public void setSelecteditemsDerecha(List<SelectItem> selecteditemsDerecha) {
        this.selecteditemsDerecha = selecteditemsDerecha;
    }
    
    @Override
    public List<Usuario> getListElements() {
        if (this.getNombreBuscar().equals("")) {
            this.setList(this.usuarioFacade.findAll(true));
        } else {
            this.setList(this.usuarioFacade.findAll(this.getNombreBuscar().toUpperCase()));
        }
        return (List<Usuario>) this.getList();
    }

    @Override
    public void limpiar() {
       this.setNombre(null);
       this.setApellido(null);
       this.setUss(null);
       this.setPass(null);
       this.getIdItems().clear();
       this.setEmail(null);
       this.setFechaAlta(null);
       this.setFechaModif(null);
       this.setUltimoLogin(null);
    }

    @Override
    public String crear() {
        try{
            List <Grupo> lstAux = this.grupoFacade.findGrupos(idItems);            
            this.usuarioFacade.create(this.getIdOrganismo(),this.getApellido(), this.getNombre(),
            this.getUss(), this.getPass(), this.getEmail(), lstAux);
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorUsuario");
            this.setMsgSuccessError("El usuario ha sido generado con éxito");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo ...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuario");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            List <Grupo> lstAux = this.grupoFacade.findGrupos(idItems);  
            this.usuarioFacade.create(this.getIdOrganismo(),this.getApellido(), this.getNombre(),
            this.getUss(), this.getPass(), this.getEmail(), lstAux);
            this.setResultado("nuevoUsuario");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo...");
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
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Usuario usuarioAux = this.usuarioFacade.find(idAux);
        this.setId(usuarioAux.getId());
        this.setApellido(usuarioAux.getNombre());
        this.setNombre(usuarioAux.getNombre());
        this.setUss(usuarioAux.getNombreUsuario());
        this.setPass(usuarioAux.getClaveAcceso());
        this.setEmail(usuarioAux.getEmail());
        this.setFechaAlta(usuarioAux.getFechaAlta());
        this.setFechaModif(usuarioAux.getFechaModificacion());
        this.setUltimoLogin(usuarioAux.getUltimoLogin());
        this.setLstGrupos(usuarioAux.getGrupos());
        this.setIdOrganismo(usuarioAux.getOrganismo().getId());
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            //TODO
            this.usuarioFacade.remove(idAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("usuarioConf");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuario");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Usuario usuarioAux = this.usuarioFacade.find(idAux);
        this.setId(usuarioAux.getId());
        this.setApellido(usuarioAux.getNombre());
        this.setNombre(usuarioAux.getNombre());
        this.setUss(usuarioAux.getNombreUsuario());
        this.setPass(usuarioAux.getClaveAcceso());
        this.setEmail(usuarioAux.getEmail());
        this.setIdOrganismo(usuarioAux.getOrganismo().getId());
        this.gruposDisponibles(usuarioAux);
        this.gruposSeleccionados(usuarioAux);        
    }
    
    public void gruposDisponibles(Usuario usuario) {     
        this.selecteditemsDerecha.clear();
        List <Grupo> listGruposUsuario = usuario.getGrupos();
        List <Grupo> listGrupos = grupoFacade.findAll(true);
        listGrupos.removeAll(listGruposUsuario);
        if (!(listGrupos.isEmpty())) {
            for (Grupo grupoAux : listGrupos) {
                this.selecteditemsDerecha.add(new SelectItem(grupoAux.getId(), grupoAux.getNombre()));
            }
        }        
    }
    
    public void gruposSeleccionados(Usuario usuario) {
        this.selecteditemsIzquierda.clear();
        List <Grupo> listGrupos = usuario.getGrupos();
        if (!(listGrupos.isEmpty())) {
            for (Grupo grupoAux : listGrupos) {
                this.selecteditemsIzquierda.add(new SelectItem(grupoAux.getId(), grupoAux.getNombre()));
            }
        }
    }
    
    @Override
    public String guardarEdicion() {
        try {
            List <Long> lstAux = new ArrayList <>();
            for (SelectItem object : this.selecteditemsIzquierda) {
                System.out.println("valorr "+object.getValue());
                lstAux.add((Long)object.getValue());
            }            
            usuarioFacade.edit(this.getIdOrganismo(),this.getId(), this.getApellido(), this.getNombre(),
                    this.getUss(), this.getPass(), this.getEmail(), lstAux);
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorUsuario");
            this.setMsgSuccessError("El Usuario ha sido editado con éxito");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorUsuario");
        }
        return this.getResultado();
    }    

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Grupo> listGrupo = grupoFacade.findAll(true);
        if (!(listGrupo.isEmpty())) {
            for (Grupo grupoAux : listGrupo) {
                selectItems.add(new SelectItem(grupoAux.getId(), grupoAux.getNombre()));
            }
        }
        return selectItems;
    }
    
     public void prepararParaEditarContrasena(){
        WebManagedBean sessionBean = this.getSessionBean();
        Usuario usuarioAux = sessionBean.getUsuario();
        this.setId(usuarioAux.getId());
        this.setApellido(usuarioAux.getApellido());
        this.setNombre(usuarioAux.getNombre());
        this.setNombreP(usuarioAux.getNombreUsuario());
        this.setPassActual(usuarioAux.getClaveAcceso());
        this.gruposDisponibles(usuarioAux);
        this.gruposSeleccionados(usuarioAux);
    
    }
     
    public String guardarConfiguracion(){
        try {
            usuarioFacade.edit(this.getId(), this.getPassActual(), this.getClaveAcceso(), this.getPassConfirmar());
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorConfiguracion");
            this.setMsgSuccessError("El usuario ha sido editado con éxito");
            this.limpiar();
        } catch (Exception e) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(e.getMessage());
            this.setResultado("successErrorConfiguracion");
            this.limpiar();
            
        }
        
        return this.getResultado();
    
    }
}

