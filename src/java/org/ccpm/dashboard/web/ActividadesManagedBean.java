/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Actividades;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Metas;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.facade.ActividadesFacadeLocal;
import org.ccpm.dashboard.facade.MetasFacadeLocal;

/**
 *
 * @author Alvarenga Angel
 */
@Named
@RequestScoped
public class ActividadesManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private ActividadesFacadeLocal actividadesFacade;
    @EJB
    private MetasFacadeLocal metasFacade;
    private String leyenda;
    private Long idMetas;
    private int cantidad;
    private List <ActionItem> lstActionItems = new ArrayList <>();

    /** Creates a new instance of ActividadesManagedBean */
    public ActividadesManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevaActividades")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarActividades")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarActividades")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleActividades")) {
                        this.setDetalle(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public int getCantidad() {
        return cantidad;
    }

    

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

   

    
    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public Long getIdMetas() {
        return idMetas;
    }

    public void setIdMetas(Long idMetas) {
        this.idMetas = idMetas;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    @Override
    public List<Actividades> getListElements() {
        if (this.getLeyendaBuscar().equals("")) {
            this.setList(this.actividadesFacade.findAll(true));
        } else {
            this.setList(this.actividadesFacade.findAll(this.getLeyendaBuscar().toUpperCase()));
        }
        return (List<Actividades>) this.getList();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Metas> lstMetas = this.metasFacade.findAll(true);
        if (!(lstMetas.isEmpty())) {
            for (Metas orgAux : lstMetas) {
                selectItems.add(new SelectItem(orgAux.getId(), orgAux.getId()+ "-" + orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        this.setLeyenda(null);
        this.setIdMetas(null);
    }

    @Override
    public String crear() {
        try{
            this.actividadesFacade.create(this.getIdMetas(), this.getLeyenda());
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorActividades");
            this.setMsgSuccessError("La Actividad ha sido generada con éxito");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo ...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorActividades");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.actividadesFacade.create(this.getIdMetas(), this.getLeyenda());
            this.setResultado("nuevaActividades");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorActividades");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Actividades actividadesAux = this.actividadesFacade.find(idAux);
        this.setId(actividadesAux.getId());
        this.setLeyenda(actividadesAux.getLeyenda());        
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            this.actividadesFacade.remove(idAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("actividadesConf");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorActividades");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Actividades actividadesAux = this.actividadesFacade.find(idAux);
        this.setId(actividadesAux.getId());
        this.setLeyenda(actividadesAux.getLeyenda()); 
        System.out.println("Holaaaaaaaaaaaaaaaaaaaaaa!!!!!");
    }

    @Override
    public String guardarEdicion() {
        try {
            this.actividadesFacade.edit(this.getId(), this.getLeyenda());
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorActividades");
            this.setMsgSuccessError("La dependecia ha sido editada con éxito");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorActividades");
        }
        return this.getResultado();
    }

    
}


