/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Alvarenga Angel
 */
@Entity
@Table(name="organismos")
public class Organismo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="cod_organigrama")
    private Long codOrganigrama;
    private String leyenda;
    private String descripcion;
    @OneToMany
    private List<Organismo> organismos;      
    @OneToOne
    private Organismo organismo;
    @OneToMany(mappedBy = "organismo")
    private List<Objetivos> objetivos;
    @OneToMany(mappedBy = "organismo")
    private List<Obras> obras;
    @OneToMany(mappedBy = "organismo")
    private List<Usuario> usuarios;
    private boolean descentralizado;
    private boolean estado;//borrado lógico

    public Organismo() {
        organismos = new ArrayList<>();
        objetivos = new ArrayList<>();
        obras=new ArrayList<>();
        usuarios=new ArrayList<>();
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
    
    
    public boolean isDescentralizado() {
        return descentralizado;
    }

    public void setDescentralizado(boolean descentralizado) {
        this.descentralizado = descentralizado;
    }

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCodOrganigrama() {
        return codOrganigrama;
    }

    public void setCodOrganigrama(Long codOrganigrama) {
        this.codOrganigrama = codOrganigrama;
    }

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Organismo> getOrganismos() {
        return organismos;
    }

    public void setOrganismos(List<Organismo> organismos) {
        this.organismos = organismos;
    }

    public Organismo getOrganismo() {
        return organismo;
    }

    public void setOrganismo(Organismo organismo) {
        this.organismo = organismo;
    }

    public List<Obras> getObras() {
        return obras;
    }

    public void setObras(List<Obras> obras) {
        this.obras = obras;
    }
    
 

    public List<Objetivos> getObjetivoss() {
        return objetivos;
    }

    public void setObjetivos(List<Objetivos> objetivos) {
        this.objetivos = objetivos;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
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
        if (!(object instanceof Organismo)) {
            return false;
        }
        Organismo other = (Organismo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.dashboard.entity.Organismo[ id=" + id + " ]";
    }
    
}
