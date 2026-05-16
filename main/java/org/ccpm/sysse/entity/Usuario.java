package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @Column(name = "clave_acceso")
    private String claveAcceso;
    private String apellido;
    private String nombre;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "fecha_alta")
    private Date fechaAlta;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "fecha_modificacion")
    private Date fechaModificacion;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "ultimo_login")
    private Date ultimoLogin;
    private String email;
    @OneToMany
    private List<Grupo> grupos;
    @OneToMany
    private List<Menu> menus;
    @OneToMany
    private List<ActionItem> acciones;
    private String foto;
    @OneToOne
    private Mesa mesa;
    @OneToOne
    private Organismo organismo;
    private Long dni;
    @Column(name = "updated_password")
    private boolean updatedPassword;
    @OneToMany
    private List<Mesa> mesasDisponibles;
    private boolean estado;//borrado lógico

    public Usuario() {
        this.grupos = new ArrayList<Grupo>();
    }

    public void setAcciones(List<ActionItem> acciones) {
        this.acciones = acciones;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setUltimoLogin(Date ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public List<ActionItem> getAcciones() {
        return acciones;
    }

    public String getApellido() {
        return apellido;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEstado() {
        return estado;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public String getFoto() {
        return foto;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public Long getId() {
        return id;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public Date getUltimoLogin() {
        return ultimoLogin;
    }

    public Organismo getOrganismo() {
        return organismo;
    }

    public void setOrganismo(Organismo organismo) {
        this.organismo = organismo;
    }

    public Long getDni() {
        return dni;
    }

    public void setDni(Long dni) {
        this.dni = dni;
    }

    public boolean isUpdatedPassword() {
        return updatedPassword;
    }

    public void setUpdatedPassword(boolean updatedPassword) {
        this.updatedPassword = updatedPassword;
    }

    public String getApellidoNombre() {
        return this.getApellido() + ", " + this.getNombre();
    }

    public List<Mesa> getMesasDisponibles() {
        return mesasDisponibles;
    }

    public void setMesasDisponibles(List<Mesa> mesasDisponibles) {
        this.mesasDisponibles = mesasDisponibles;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Usuario)) {
            return false;
        }
        Usuario other = (Usuario) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.entidades.UsuarioSistema[ id=" + id + " ]";
    }
}
