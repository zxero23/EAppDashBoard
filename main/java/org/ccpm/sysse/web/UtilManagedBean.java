package org.ccpm.sysse.web;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import org.primefaces.model.LazyDataModel;

public abstract class UtilManagedBean {

    private String nombreBuscar = "";
    private boolean alta, baja, modificacion, detalle;
    private List<?> list;
    private Long id;
    private String resultado;
    private String title;
    private String images;
    private String msgSuccessError;
    private List<Long> selectedItemsIzquierda;
    private List<Long> selectedItemsDerecha;
    private List<SelectItem> itemsIzquierda;
    private List<SelectItem> itemsDerecha;
    private Boolean banderita = true;
    private Long idTipoInic, idInic;
    public abstract LazyDataModel<?> getItems();
    private Integer page = 1;

    public UtilManagedBean() {
        this.itemsDerecha = new ArrayList<>();
        this.itemsIzquierda = new ArrayList<>();
        this.selectedItemsIzquierda = new ArrayList<>();
        this.selectedItemsDerecha = new ArrayList<>();
    }

    public abstract List<?> getListElements();

    public abstract List<SelectItem> getSelectItems();

    public abstract void limpiar();

    public abstract String crear();

    public abstract String crearOtro();

    public abstract void verDetalle();

    public abstract void guardarBorrado();

    public abstract void prepararParaEditar();

    public abstract String guardarEdicion();

    public Boolean getBanderita() {
        return banderita;
    }

    public void setBanderita(Boolean banderita) {
        this.banderita = banderita;
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

    public void setList(List<?> list) {
        this.list = list;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setItemsDerecha(List<SelectItem> itemsDerecha) {
        this.itemsDerecha = itemsDerecha;
    }

    public void setItemsIzquierda(List<SelectItem> itemsIzquierda) {
        this.itemsIzquierda = itemsIzquierda;
    }

    public void setSelectedItemsDerecha(List<Long> selectedItemsDerecha) {
        this.selectedItemsDerecha = selectedItemsDerecha;
    }

    public void setSelectedItemsIzquierda(List<Long> selectedItemsIzquierda) {
        this.selectedItemsIzquierda = selectedItemsIzquierda;
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

    public List<SelectItem> getItemsDerecha() {
        return itemsDerecha;
    }

    public List<SelectItem> getItemsIzquierda() {
        return itemsIzquierda;
    }

    public List<Long> getSelectedItemsDerecha() {
        return selectedItemsDerecha;
    }

    public List<Long> getSelectedItemsIzquierda() {
        return selectedItemsIzquierda;
    }

    public Long getId() {
        return id;
    }

    public List<?> getList() {
        return list;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
    
    

    protected WebManagedBean getSessionBean() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            return (WebManagedBean) context.getApplication()
                    .evaluateExpressionGet(context, "#{webManagedBean}", WebManagedBean.class);

        } catch (Exception ex) {
           //ToDO
        }
        return null;
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

    public Long getIdTipoInic() {
        return idTipoInic;
    }

    public void setIdTipoInic(Long idTipoInic) {
        this.idTipoInic = idTipoInic;
    }

    public Long getIdInic() {
        return idInic;
    }

    public void setIdInic(Long idInic) {
        this.idInic = idInic;
    }

    public void aplicarFiltro() {
        this.setBanderita(true);
        this.getListElements();
    }

    public void actualizar() {
        this.setPage(1);
        this.setNombreBuscar("");
        this.aplicarFiltro();
    }

    public void agregar() {
        Iterator iteratorSelected = getSelectedItemsIzquierda().iterator();
        while (iteratorSelected.hasNext()) {
            SelectItem o;
            Long item = Long.parseLong(iteratorSelected.next().toString());
            Iterator iterator = getItemsIzquierda().iterator();
            while (iterator.hasNext()) {
                o = (SelectItem) iterator.next();
                if (Long.parseLong(o.getValue().toString()) == item) {
                    getItemsIzquierda().remove(o);
                    getItemsDerecha().add(o);
                    break;
                }
            }
        }
        this.selectedItemsIzquierda.clear();
    }

    public void borrar() {
        Iterator iteratorSelected = getSelectedItemsDerecha().iterator();
        while (iteratorSelected.hasNext()) {
            SelectItem o;
            Long item = Long.parseLong(iteratorSelected.next().toString());
            Iterator iterator = getItemsDerecha().iterator();
            while (iterator.hasNext()) {
                o = (SelectItem) iterator.next();
                if (Long.parseLong(o.getValue().toString()) == item) {
                    getItemsDerecha().remove(o);
                    getItemsIzquierda().add(o);
                    break;
                }
            }
        }
        this.selectedItemsDerecha.clear();
    }

    public void agregarTodos() {
        Object object;
        int size = getItemsIzquierda().size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                object = getItemsIzquierda().get(0);
                getItemsIzquierda().remove(object);
                getItemsDerecha().add((SelectItem) object);
            }
            this.selectedItemsIzquierda.clear();
        }
    }

    public void borrarTodos() {
        Object object;
        int size = getItemsDerecha().size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                object = getItemsDerecha().get(0);
                getItemsDerecha().remove(object);
                getItemsIzquierda().add((SelectItem) object);
            }
            this.selectedItemsDerecha.clear();
        }
    }

    public static String desacentuar(String str) {
        String salida = str;
        if (str != null) {
            String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            salida = pattern.matcher(nfdNormalizedString).replaceAll("");
        }
        return salida;
    }

    public String nombrar(String nombre, String extension) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date date = new Date();
        nombre = nombre + dateFormat.format(date) + "." + extension;
        return nombre;
    }

    public String renombrar(String nombre) {
        int i = nombre.lastIndexOf('\\');
        return nombre.substring(i + 1);
    }

    public static Date quitarTiempo(Date fecha) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        } catch (Exception e) {
           return new Date();
        }
    }

}
