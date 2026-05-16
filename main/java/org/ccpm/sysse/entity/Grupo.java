package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "grupos")
public class Grupo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    @OneToMany
    private List<Menu> menus;
    @OneToMany
    private List<ActionItem> acciones;
    private boolean estado;

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setAcciones(List<ActionItem> acciones) {
        this.acciones = acciones;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    public String getNombre() {
        return nombre;
    }  
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public List<ActionItem> getAcciones() {
        return acciones;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (!(object instanceof Grupo)) {
            return false;
        }
        Grupo other = (Grupo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.entidades.GrupoSistema[ id=" + id + " ]";
    }
    
}
