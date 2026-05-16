package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "menus")
public class Menu implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String link;
    private Integer orden;
    @ManyToOne
    private Menu menuPadre;
    @OneToMany(mappedBy = "menuPadre")
    private List<Menu> menus;
    private boolean estado; //borrado logico

    public Menu() {
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setMenuPadre(Menu menuPadre) {
        this.menuPadre = menuPadre;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }
    public boolean isEstado() {
        return estado;
    }

    public Long getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public Menu getMenuPadre() {
        return menuPadre;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getOrden() {
        return orden;
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
        if (!(object instanceof Menu)) {
            return false;
        }
        Menu other = (Menu) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.entidades.MenuSistema[ id=" + id + " ]";
    }
}
