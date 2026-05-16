package org.ccpm.sysse.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import org.ccpm.sysse.entity.ActionItem;
import org.ccpm.sysse.entity.Grupo;
import org.ccpm.sysse.entity.Menu;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.facade.ActionItemFacadeLocal;
import org.ccpm.sysse.facade.GrupoFacadeLocal;
import org.ccpm.sysse.facade.MenuFacadeLocal;
import org.ccpm.sysse.facade.UsuarioFacadeLocal;
import org.primefaces.model.LazyDataModel;

@Named
@SessionScoped
public class PermisoManagedBean extends UtilManagedBean implements Serializable {

    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private ActionItemFacadeLocal actionFacade;
    @EJB
    private MenuFacadeLocal menuFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    private Long idGrupo;
    private Long idUsuario;
    private List<SelectItem> itemsIzquierda;
    private List<SelectItem> itemsDerecha;


    public PermisoManagedBean() {
        this.idGrupo = 0L;
        this.idUsuario = 0L;
        this.itemsIzquierda = null;
        this.itemsDerecha = null;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public List<SelectItem> getItemsIzquierda() {
        return itemsIzquierda;
    }

    public void setItemsIzquierda(List<SelectItem> itemsIzquierda) {
        this.itemsIzquierda = itemsIzquierda;
    }

    public List<SelectItem> getItemsDerecha() {
        return itemsDerecha;
    }

    public void setItemsDerecha(List<SelectItem> itemsDerecha) {
        this.itemsDerecha = itemsDerecha;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public List<SelectItem> getSelectItemsGrupo() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Grupo> lstGrupo = grupoFacade.findAll(true);
        if (!(lstGrupo.isEmpty())) {
            lstGrupo.stream().sorted((g1, g2) -> (g1.getNombre().compareTo(g2.getNombre()))).forEach((grupoAux) -> {
                selectItems.add(new SelectItem(grupoAux.getId(), grupoAux.getNombre()));
            });
        }
        return selectItems;
    }

    public List<SelectItem> getSelectItemsUsuario() {
        List<SelectItem> selectItems = new ArrayList<>();
        List<Usuario> lstUsuario = usuarioFacade.findAll(true);
        if (!(lstUsuario.isEmpty())) {
            lstUsuario.stream().forEach((usuarioAux) -> {
                selectItems.add(new SelectItem(usuarioAux.getId(), usuarioAux.getApellidoNombre() + "-" + usuarioAux.getNombreUsuario()));
            });
        }
        return selectItems;
    }

    @Override
    public void limpiar() {
        this.setIdGrupo(null);
        this.setIdUsuario(null);
        this.setItemsDerecha(null);
        this.setItemsIzquierda(null);
    }

    public void updateListAcctions(ValueChangeEvent event) {
        Grupo grupo = this.grupoFacade.find(Long.parseLong(event.getNewValue().toString()));
        if (grupo != null) {
            this.accionesSeleccionadas(grupo);
            this.accioneDisponibles(grupo);
        } else {
            this.setItemsDerecha(null);
            this.setItemsIzquierda(null);
        }
    }

    public void accionesSeleccionadas(Grupo grupo) {
        List<ActionItem> listActionItems = grupo.getAcciones();
        this.itemsIzquierda = new ArrayList<>();
        if (!(listActionItems.isEmpty())) {
            listActionItems.stream().filter((g1) -> (g1.isEstado())).forEach((actionAux) -> {
                this.itemsIzquierda.add(new SelectItem(actionAux.getId(), actionAux.getNombre()));
            });
        }
    }

    public void accioneDisponibles(Grupo grupo) {
        List<ActionItem> listActionItemsAll = actionFacade.findAll(true);
        this.itemsDerecha = new ArrayList<>();
        listActionItemsAll.removeAll(grupo.getAcciones());
        if (!(listActionItemsAll.isEmpty())) {
            listActionItemsAll.stream().forEach((actionAux) -> {
                this.itemsDerecha.add(new SelectItem(actionAux.getId(), actionAux.getNombre()));
            });
        }
    }

    public void updateListMenu(ValueChangeEvent event) {
        Grupo grupo = this.grupoFacade.find(Long.parseLong(event.getNewValue().toString()));
        if (grupo != null) {
            this.menuSeleccionados(grupo);
            this.menuDisponibles(grupo);
        } else {
            this.setItemsDerecha(null);
            this.setItemsIzquierda(null);
        }
    }

    public void menuSeleccionados(Grupo grupo) {
        List<Menu> lstMenu = grupo.getMenus();
        this.itemsIzquierda = new ArrayList<>();
        if (!(lstMenu.isEmpty())) {
            lstMenu.stream().sorted((m1, m2) -> (m1.getNombre().compareToIgnoreCase(m2.getNombre()))).forEach((menuAux) -> {
                this.itemsIzquierda.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            });
        }
    }

    public void menuDisponibles(Grupo grupo) {
        List<Menu> lstMenu = menuFacade.findAll(true);
        this.itemsDerecha = new ArrayList<>();
        lstMenu.removeAll(grupo.getMenus());
        if (!(lstMenu.isEmpty())) {
            lstMenu.stream().sorted((m1, m2) -> (m1.getNombre().compareToIgnoreCase(m2.getNombre()))).forEach((menuAux) -> {
                this.itemsDerecha.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            });
        }
    }

    public String editarGrupoAccion() {
        try {
            List<Long> lstAux = new ArrayList<>();
            this.getItemsIzquierda().stream().forEach((object) -> {
                lstAux.add((Long) object.getValue());
            });
            this.grupoFacade.remove(this.getIdGrupo(), lstAux);
            this.setTitle("Proceso completo...");
            this.setImages("fa fa-check-circle-o");
            this.setResultado("successErrorPermiso");
            this.setMsgSuccessError("La configuración han sido generada con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("fa fa-times-circle-o");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorPermiso");
        }
        return this.getResultado();
    }

    public String editarGrupoMenu() {
        try {
            List<Long> lstAux = new ArrayList<>();
            this.getItemsIzquierda().stream().forEach((object) -> {
                lstAux.add((Long) object.getValue());
            });
            this.grupoFacade.edit(this.getIdGrupo(), lstAux);
            this.setTitle("Proceso completo...");
            this.setImages("fa fa-check-circle-o");
            this.setResultado("successErrorPermiso");
            this.setMsgSuccessError("La configuración han sido generada con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("fa fa-times-circle-o");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorPermiso");
        }
        return this.getResultado();
    }

    public void updateListMenuUsuario(ValueChangeEvent event) {
        Usuario usuarioAux = this.usuarioFacade.find(Long.parseLong(event.getNewValue().toString()));
        if (usuarioAux != null) {
            this.menuSeleccionados(usuarioAux);
            this.menuDisponibles(usuarioAux);
        } else {
            this.setItemsDerecha(null);
            this.setItemsIzquierda(null);
        }
    }

    public void menuDisponibles(Usuario usuario) {
        List<Menu> listMenuAll = menuFacade.findAll(true);
        this.itemsDerecha = new ArrayList<>();
        listMenuAll.removeAll(usuario.getMenus());
        if (!(listMenuAll.isEmpty())) {
            listMenuAll.stream().forEach((menuAux) -> {
                this.itemsDerecha.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            });
        }
    }

    public void menuSeleccionados(Usuario usuario) {
        List<Menu> listMenu = usuario.getMenus();
        this.itemsIzquierda = new ArrayList<>();
        if (!(listMenu.isEmpty())) {
            listMenu.stream().forEach((menuAux) -> {
                this.itemsIzquierda.add(new SelectItem(menuAux.getId(), menuAux.getNombre()));
            });
        }
    }

    public String editarUsuarioMenu() {
        try {
            List<Long> lstAux = new ArrayList<>();
            this.itemsIzquierda.stream().forEach((object) -> {
                lstAux.add((Long) object.getValue());
            });
            this.usuarioFacade.edit(this.getIdUsuario(), lstAux);
            this.setTitle("Proceso completo...");
            this.setImages("fa fa-check-circle-o");
            this.setResultado("successErrorPermiso");
            this.setMsgSuccessError("La configuración han sido generada con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("fa fa-times-circle-o");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorPermiso");
            this.limpiar();
        }
        return this.getResultado();
    }

    public void updateListAcctionsUsuario(ValueChangeEvent event) {
        Usuario usuarioAux = this.usuarioFacade.find(Long.parseLong(event.getNewValue().toString()));
        if (usuarioAux != null) {
            this.accionesSeleccionadas(usuarioAux);
            this.accioneDisponibles(usuarioAux);
        } else {
            this.setItemsDerecha(null);
            this.setItemsIzquierda(null);
        }
    }

    public void accionesSeleccionadas(Usuario usuario) {
        List<ActionItem> listActionItems = usuario.getAcciones();
        this.itemsIzquierda = new ArrayList<>();
        if (!(listActionItems.isEmpty())) {
            listActionItems.stream().forEach((actionAux) -> {
                this.itemsIzquierda.add(new SelectItem(actionAux.getId(), actionAux.getNombre()));
            });
        }
    }

    public void accioneDisponibles(Usuario usuario) {
        List<ActionItem> listActionItemsAll = actionFacade.findAll(true);
        this.itemsDerecha = new ArrayList<>();
        listActionItemsAll.removeAll(usuario.getAcciones());
        if (!(listActionItemsAll.isEmpty())) {
            listActionItemsAll.stream().forEach((actionAux) -> {
                this.itemsDerecha.add(new SelectItem(actionAux.getId(), actionAux.getNombre()));
            });
        }
    }

    public String editarUsuarioAccion() {
        try {
            List<Long> lstAux = new ArrayList<>();
            this.getItemsIzquierda().stream().forEach((object) -> {
                lstAux.add((Long) object.getValue());
            });
            this.usuarioFacade.remove(this.getIdUsuario(), lstAux);
            this.setTitle("Proceso completo...");
            this.setImages("fa fa-check-circle-o");
            this.setResultado("successErrorPermiso");
            this.setMsgSuccessError("La configuración han sido generada con éxito.");
            this.limpiar();
        } catch (Exception ex) {
            this.setTitle("¡Error!");
            this.setImages("fa fa-times-circle-o");
            this.setMsgSuccessError(ex.getMessage());
            this.setResultado("successErrorPermiso");
        }
        return this.getResultado();
    }

    @Override
    public List<?> getListElements() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SelectItem> getSelectItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String crear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String crearOtro() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void verDetalle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void prepararParaEditar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String guardarEdicion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void guardarBorrado() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LazyDataModel<?> getItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
