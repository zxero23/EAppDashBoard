/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.web;

import java.util.List;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.http.HttpServletRequest;


/**
 *
 * @author Alvarenga Angel
 */
public abstract class UtilManagedBean {
    private String nombreBuscar = "";
    private String leyendaBuscar = "";
    private int anioBuscar=0;
    private boolean alta, baja, modificacion, detalle;
    private List <?> list;
    private Long id;
    private String resultado;
    private String title;
    private String images;
    private String msgSuccessError;
    
    public UtilManagedBean() {        
    }
    
    public abstract List <?> getListElements();
    
    public abstract List <SelectItem> getSelectItems();
    
    public abstract void limpiar();

    public abstract String crear();

    public abstract String crearOtro();

    public abstract void verDetalle();

    public abstract void guardarBorrado();

    public abstract void prepararParaEditar();

    public abstract String guardarEdicion();

    public int getAnioBuscar() {
        return anioBuscar;
    }

    public void setAnioBuscar(int anioBuscar) {
        this.anioBuscar = anioBuscar;
    }
    
    
    
    
    public void setAlta(boolean alta) {
        this.alta = alta;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public void setDetalle(boolean detalle) {
        this.detalle = detalle;
    }

    public void setModificacion(boolean modificacion) {
        this.modificacion = modificacion;
    }

    public void setNombreBuscar(String nombreBuscar) {
        this.nombreBuscar = nombreBuscar;
    }

    public void setLeyendaBuscar(String leyendaBuscar) {
        this.leyendaBuscar = leyendaBuscar;
    }
    
    

    public void setList(List <?> list) {
        this.list = list;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setMsgSuccessError(String msgSuccessError) {
        this.msgSuccessError = msgSuccessError;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImages() {
        return images;
    }

    public String getMsgSuccessError() {
        return msgSuccessError;
    }

    public String getTitle() {
        return title;
    }

    public String getResultado() {
        return resultado;
    }

    public Long getId() {
        return id;
    }

    public List <?> getList() {
        return list;
    }
    
    protected WebManagedBean getSessionBean() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        return (WebManagedBean) myRequest.getSession().getAttribute("webManagedBean");
    }

    public boolean isAlta() {
        return alta;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean isDetalle() {
        return detalle;
    }

    public boolean isModificacion() {
        return modificacion;
    }

    public String getNombreBuscar() {
        return nombreBuscar;
    }
    
    public String getLeyendaBuscar(){
        return leyendaBuscar;
    }
    
    
   
    public void aplicarFiltro() {
        this.getListElements();
    }
    
    public void actualizar() {
          this.setNombreBuscar("");
          this.setLeyendaBuscar("");
          this.setAnioBuscar(0);
          
    }
    
    public void clearIndicadorBean () {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("IndicadorManagedBean");
    }
    
}

