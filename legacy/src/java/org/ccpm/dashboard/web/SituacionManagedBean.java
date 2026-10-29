/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Situacion;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Objetivos;
import org.ccpm.dashboard.entity.Metas;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.facade.SituacionFacadeLocal;
import org.ccpm.dashboard.facade.ObjetivosFacadeLocal;
import org.ccpm.dashboard.facade.MetasFacadeLocal;

/**
 *
 * @author Alvarenga Angel
 */
@ManagedBean
@RequestScoped
public class SituacionManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private SituacionFacadeLocal situacionFacade;
   
    private String leyenda;
    private Long idSituacion;
    private List <ActionItem> lstActionItems = new ArrayList <>();

    /** Creates a new instance of SituacionManagedBean */
    public SituacionManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevaSituacion")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarSituacion")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarSituacion")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleSituacion")) {
                        this.setDetalle(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario");
        }
    }

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public Long getIdSituacion() {
        return idSituacion;
    }

    public void setIdSituacion(Long idSituacion) {
        this.idSituacion = idSituacion;
    }


    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    @Override
    public List<Situacion> getListElements() {
        if (this.getLeyendaBuscar().equals("")) {
            this.setList(this.situacionFacade.findAll(true));
        } else {
            this.setList(this.situacionFacade.findAll(this.getLeyendaBuscar().toUpperCase()));
        }
        return (List<Situacion>) this.getList();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Situacion> lstObjetivos = this.situacionFacade.findAll(true);
        if (!(lstObjetivos.isEmpty())) {
            for (Situacion orgAux : lstObjetivos) {
                selectItems.add(new SelectItem(orgAux.getId(), orgAux.getId()+ "-" + orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        this.setLeyenda(null);
        this.setIdSituacion(null);
    }

    @Override
    public String crear() {
        try{
            this.situacionFacade.create(this.getId(), this.getLeyenda());
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorSituacion");
            this.setMsgSuccessError("El situacion ha sido generada con éxito");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo ...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorSituacion");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.situacionFacade.create(this.getIdSituacion(), this.getLeyenda());
            this.setResultado("nuevaSituacion");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorSituacion");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Situacion situacionAux = this.situacionFacade.find(idAux);
        this.setId(situacionAux.getId());
        this.setLeyenda(situacionAux.getLeyenda());        
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            this.situacionFacade.remove(idAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("situacionConf");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorSituacion");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Situacion situacionAux = this.situacionFacade.find(idAux);
        this.setId(situacionAux.getId());
        System.out.println(this.getId());
        this.setLeyenda(situacionAux.getLeyenda()); 
        System.out.println("Holaaaaaaaaaaaaaaaaaaaaaa!!!!!");
    }

    @Override
    public String guardarEdicion() {
        try {
            this.situacionFacade.edit(this.getId(), this.getLeyenda());
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorSituacion");
            this.setMsgSuccessError("La Situación ha sido editada con éxito");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorSituacion");
        }
        return this.getResultado();
    }

    
}

