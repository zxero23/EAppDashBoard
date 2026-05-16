package org.ccpm.sysse.entity;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name = "tiposIntLegal")
public class TipoInstrumentoLegal implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String descripcion;
    private String nombre;
    private boolean estado; //borrado logico

    public TipoInstrumentoLegal() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public boolean isEstado() {
        return estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNombre() {
        return nombre;
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
        if (!(object instanceof TipoInstrumentoLegal)) {
            return false;
        }
        TipoInstrumentoLegal other = (TipoInstrumentoLegal) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.exp.entidades.TipoInstLegal[id=" + id + "]";
    }
}
