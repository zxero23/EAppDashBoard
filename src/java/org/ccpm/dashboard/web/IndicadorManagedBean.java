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
import jakarta.enterprise.context.SessionScoped;

import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Indicador;
import org.ccpm.dashboard.entity.Usuario;
import org.ccpm.dashboard.entity.Metas;
import org.ccpm.dashboard.facade.IndicadorFacadeLocal;
import org.ccpm.dashboard.facade.MetasFacadeLocal;

/**
 *
 * @author Alvarenga Angel
 */
@Named
@SessionScoped
public class IndicadorManagedBean extends UtilManagedBean implements Serializable{
    @EJB
    private IndicadorFacadeLocal indicadorFacade;
    @EJB
    private MetasFacadeLocal metasFacade;
    private String formula;
    private String descripcion;
    private String leyenda;
    private Long idIndicador;
    private Long idMeta;
    private Double variable1,variable2;
    private List <ActionItem> lstActionItems = new ArrayList <>();
    private double resultadoIndicador;
    private String[] serie=new String[5],variableIndicador=new String[3],valores=new String[15];
    private int serieSize,variableSize,valoresSize;
    private String [] prueba=new String[serieSize];
    private String kml;
    private Long idOrganismo=0L;
    
    public IndicadorManagedBean() {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevoIndicador")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarIndicador")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarIndicador")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleIndicador")) {
                        this.setDetalle(true);
                    }
                    //System.out.println(accionAux.getNombre());
                    
                }
            }
            System.out.println("id organismo indicador  "+usuario.getOrganismo().getId());
            this.setIdOrganismo(usuario.getOrganismo().getId());
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del indicador");
        }
    }

    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }

    
    
    public String getKml() {
        return kml;
    }

    public void setKml(String kml) {
        this.kml = kml;
    }
    

    public String[] getPrueba() {
        return prueba;
    }

    public void setPrueba(String[] prueba) {
        this.prueba = prueba;
    }

    
    public int getSerieSize() {
        return serieSize;
    }

    public void setSerieSize(int serieSize) {
        this.serieSize = serieSize;
        serie=new String[serieSize];
        valores=new String[serieSize*3];
        
    }

    public int getVariableSize() {
        return variableSize;
    }

    public void setVariableSize(int variableSize) {
        this.variableSize = variableSize;
    }

    public int getValoresSize() {
        return valoresSize;
    }

    public void setValoresSize(int valoresSize) {
        this.valoresSize = valoresSize;
    }

    public String[] getVariableIndicador() {
        return variableIndicador;
    }

    public void setVariableIndicador(String[] variableIndicador) {
        this.variableIndicador = variableIndicador;
    }

    public String[] getSerie() {
        return serie;
    }

    public void setSerie(String[] serie) {
        this.serie = serie;
    }

    public String[] getValores() {
        return valores;
    }

    public void setValores(String[] valores) {
        this.valores = valores;
    }

    public Long getIdMeta() {
        return idMeta;
    }

    public void setIdMeta(Long idMeta) {
        this.idMeta = idMeta;
    }

    
    public Double getVariable1() {
        return variable1;
    }

    public Double getVariable2() {
        return variable2;
    }

    public void setVariable1(Double variable1) {
        this.variable1 = variable1;
    }

    public void setVariable2(Double variable2) {
        this.variable2 = variable2;
    }

    
    
    public double getResultadoIndicador() {
        return resultadoIndicador;
    }

    public void setResultadoIndicador(double resultadoIndicador) {
        this.resultadoIndicador = resultadoIndicador;
    }

    
    
    public String getLeyenda() {
        return leyenda;
    }

    public String getFormula() {
        return formula;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public IndicadorFacadeLocal getIndicadorFacade() {
        return indicadorFacade;
    }

    public void setIndicadorFacade(IndicadorFacadeLocal indicadorFacade) {
        this.indicadorFacade = indicadorFacade;
    }
    

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public Long getIdIndicador() {
        return idIndicador;
    }

    public void setIdIndicador(Long idIndicador) {
        this.idIndicador = idIndicador;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    @Override
    public List<Indicador> getListElements() {
       
        if (this.getLeyendaBuscar().equals("") && this.getIdOrganismo()==0L) {
            this.setList(this.indicadorFacade.findAll(true));
        } else {
            this.setList(this.indicadorFacade.findAll(this.getLeyendaBuscar().toUpperCase(), this.getIdOrganismo()));
        }
       
        return (List<Indicador>) this.getList();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Indicador> lstIndicador = this.indicadorFacade.findAll(true);
        if (!(lstIndicador.isEmpty())) {
            for (Indicador orgAux : lstIndicador) {
                selectItems.add(new SelectItem(orgAux.getId(), orgAux.getId() + "-" + orgAux.getLeyenda()));
            }
        }
        return selectItems;
    }
    
    
    public List<SelectItem> getSelectItemsMetas() {
        List <SelectItem> selectItems = new ArrayList <>();
        List <Metas> lstMetas = this.metasFacade.findAll(true);
        if (!(lstMetas.isEmpty())) {
            for (Metas metaAux : lstMetas) {
                selectItems.add(new SelectItem(metaAux.getId(), metaAux.getId() + "-" + metaAux.getLeyenda()));
            }
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        this.setLeyenda(null);
        this.setDescripcion(null);
        this.setFormula(null);
     
        
    }

    @Override
    public String crear() {
        
        
        try{
            
            this.indicadorFacade.create(this.idMeta,this.getDescripcion(), this.getLeyenda(),"formula",this.variableIndicador,this.serie,this.valores,this.getSerieSize(),"kml");
            this.setTitle("Proceso Completo ...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorIndicador");
            this.setMsgSuccessError("El indicador ha sido generada con éxito");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo ...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorIndicador");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try{
            this.indicadorFacade.create(this.idMeta,this.getDescripcion(), this.getLeyenda(),"formula",this.variableIndicador,this.serie,this.valores,this.getSerieSize(),"kml");
            this.setResultado("nuevoIndicador");
            this.limpiar();
        }catch(Exception ex){
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorIndicador");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Indicador indicadorAux = this.indicadorFacade.find(idAux);
        this.setId(indicadorAux.getId());
        this.setLeyenda(indicadorAux.getLeyenda());  
        this.setDescripcion(indicadorAux.getDescripcion());
        this.setFormula("formula");
        this.setKml("kml");
        this.setIdMeta(indicadorAux.getMetas().getId());
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest)context.getExternalContext().getRequest();
            Long idAux = Long.parseLong(myRequest.getParameter("id"));
            this.indicadorFacade.remove(idAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("IndicadorConf");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorIndicador");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idAux = Long.parseLong(myRequest.getParameter("id"));
        Indicador indicadorAux = this.indicadorFacade.find(idAux);
        this.setId(indicadorAux.getId());
        this.setSerieSize(indicadorAux.getSizeSerie());
        this.setLeyenda(indicadorAux.getLeyenda()); 
        this.setDescripcion(indicadorAux.getDescripcion());
        this.setFormula("formula");
        this.setSerie(indicadorAux.getSerie());
        this.setValores(indicadorAux.getValores());
        this.setVariableIndicador(indicadorAux.getVariableIndicador());
        this.setKml("kml");
        
        //System.out.println("Holaaaaaaaaaaaaaaaaaaaaaa!!!!!");
    }

    @Override
    public String guardarEdicion() {
        try {
            this.indicadorFacade.edit(this.getId(), this.getDescripcion(),this.getLeyenda(),"formula",this.variableIndicador,this.serie,this.valores,this.getSerieSize(),"kml");
            this.setTitle("Proceso Completo...");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorIndicador");
            this.setMsgSuccessError("El indicador ha sido editado con éxito");
        } catch (Exception ex) {
            this.setTitle("Resultado del Chequeo...");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorIndicador");
        }
        return this.getResultado();
    }
    
    
    
    /*public double RealizarIndicador(){
        Indicador indicadorAux = this.indicadorFacade.find(this.idIndicador);
        Expression e = new ExpressionBuilder(indicadorAux.getFormula())
        .variables("HNBI", "HT")
        .build()
        .setVariable("HNBI", this.variable1)
        .setVariable("HT", this.variable2);
        double result = e.evaluate();
        this.setResultadoIndicador(result);
        System.out.println(result);
        return result;
        
        
        
    }*/
    
    
   
    
    

    
}

