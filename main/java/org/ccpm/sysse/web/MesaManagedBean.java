package org.ccpm.sysse.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.facade.MesaFacadeLocal;
import org.ccpm.sysse.facade.OrganismoFacadeLocal;
import org.primefaces.model.LazyDataModel;

@Named
@SessionScoped
public class MesaManagedBean extends UtilManagedBean implements Serializable {

    @EJB
    private MesaFacadeLocal mesaFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
    private Long idOrganismo;
    private String nombre;
    private String descripcion;
    private boolean checked;
    private Mesa mesa;
    private List<ActionItem> lstActionItems = new ArrayList<>();

    /*Busqueda*/
    private Long idBuscar;
    private Long idOrganismoBuscar;

    /**
     * Creates a new instance of MesaManagedBean
     */
    public MesaManagedBean() {
    }

    @PostConstruct
    private void init() {
        WebManagedBean sessionBean = this.getSessionBean();
        if (sessionBean != null) {
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
                    if (accionAux.getNombre().equalsIgnoreCase("nuevaMesa")) {
                        this.setAlta(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("editarMesa")) {
                        this.setModificacion(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("borrarMesa")) {
                        this.setBaja(true);
                    }
                    if (accionAux.getNombre().equalsIgnoreCase("detalleMesa")) {
                        this.setDetalle(true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Error al recuperar las acciones del usuario.");
        }
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public boolean isChecked() {
        return checked;
    }

    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setLstActionItems(List<ActionItem> lstActionItems) {
        this.lstActionItems = lstActionItems;
    }

    public List<ActionItem> getLstActionItems() {
        return lstActionItems;
    }

    public String getNombre() {
        return nombre;
    }

    public Long getIdOrganismoBuscar() {
        return idOrganismoBuscar;
    }

    public void setIdOrganismoBuscar(Long idOrganismoBuscar) {
        this.idOrganismoBuscar = idOrganismoBuscar;
    }

    public Long getIdBuscar() {
        return idBuscar;
    }

    public void setIdBuscar(Long idBuscar) {
        this.idBuscar = idBuscar;
    }
    
    

    public List<SelectItem> getSelectItemsOrganismo() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Organismo> lstOrganismo = this.organismoFacade.findAll(true);
        if (!(lstOrganismo.isEmpty())) {
            lstOrganismo.stream().forEach((organismoAux) -> {
                selectItems.add(new SelectItem(organismoAux.getId(), organismoAux.getNombre()));
            });
        }
        return selectItems;
    }

    @Override
    public List<Mesa> getListElements() {
        this.setList(this.mesaFacade.findAll(desacentuar(this.getNombreBuscar()), this.getIdOrganismoBuscar(),this.getIdBuscar()));

        return (List<Mesa>) this.getList();
    }

    @Override
    public void limpiar() {
        this.setIdOrganismo(null);
        this.setNombre(null);
        this.setDescripcion(null);
        this.setChecked(false);  
        
    }

    @Override
    public void actualizar() {
        this.setNombreBuscar(null);
        this.setIdOrganismoBuscar(null);
        this.setIdBuscar(null);
        super.actualizar();
    }

    @Override
    public String crear() {
        try {
            this.mesaFacade.create(this.getIdOrganismo(), this.getNombre(),
                    this.getDescripcion(), this.isChecked());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorMesa");
            this.setMsgSuccessError("La mesa ha sido generada con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMesa");
        }
        return this.getResultado();
    }

    @Override
    public String crearOtro() {
        try {
            this.mesaFacade.create(this.getIdOrganismo(), this.getNombre(),
                    this.getDescripcion(), this.isChecked());
            this.setResultado("nuevaMesa");
            this.setNombre(null);
            this.setDescripcion(null);
            this.setChecked(false);
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMesa");
        }
        return this.getResultado();
    }

    @Override
    public void verDetalle() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        this.setMesa(this.mesaFacade.find(idObjectAux));
    }

    @Override
    public void guardarBorrado() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
            Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
            this.mesaFacade.remove(idObjectAux);
            this.setTitle(null);
            this.setImages(null);
            this.setMsgSuccessError(null);
            this.setResultado("mesaConf");
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMesa");
        }
    }

    @Override
    public void prepararParaEditar() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest myRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        Long idObjectAux = Long.parseLong(myRequest.getParameter("id"));
        Mesa mesaAux = this.mesaFacade.find(idObjectAux);
        this.setId(mesaAux.getId());
        this.setIdOrganismo(mesaAux.getOrganismo().getId());
        this.setNombre(mesaAux.getNombre());
        this.setDescripcion(mesaAux.getDescripcion());
        this.setChecked(mesaAux.isMesaGeneral());
    }

    @Override
    public String guardarEdicion() {
        try {
        this.mesaFacade.edit(this.getId(), this.getIdOrganismo(),
                    this.getNombre(), this.getDescripcion(), this.isChecked());
            this.setTitle("Proceso Completo");
            this.setImages("images/symbolChecks.png");
            this.setResultado("successErrorMesa");
            this.setMsgSuccessError("La mesa ha sido editada con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("images/symbolError.png");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorMesa");
        }
        return this.getResultado();
    }

    @Override
    public List<SelectItem> getSelectItems() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Organismo> lstOrganismo = this.organismoFacade.findAll(true);
        if (!(lstOrganismo.isEmpty())) {
            lstOrganismo.stream().forEach((organismoAux) -> {
                selectItems.add(new SelectItem(organismoAux.getId(), organismoAux.getCodOrganigrama().toString().concat(" - ").concat(organismoAux.getNombre()) ));
            });
        }
        return selectItems;
    }

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
