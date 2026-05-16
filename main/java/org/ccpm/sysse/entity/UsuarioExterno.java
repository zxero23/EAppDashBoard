package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "usuariosExternos")
@PrimaryKeyJoinColumn(name = "id")
public class UsuarioExterno extends Iniciador implements Serializable {
    
    @OneToOne
    private Usuario usuarioSistema;
    @Column(name = "fec_cre")
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @OneToOne
    private Usuario usuarioCreacion;
    @Column(name = "fec_mod")
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    private Date fechaModificacion;
    @OneToOne
    private Usuario usuarioModificacion;  
    

    public UsuarioExterno() {
    }

    public void setUsuarioSistema(Usuario usuarioSistema) {
        this.usuarioSistema = usuarioSistema;
    }   

    public Usuario getUsuarioSistema() {
        return usuarioSistema;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(Usuario usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Usuario getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(Usuario usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getId() != null ? getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UsuarioExterno)) {
            return false;
        }
        UsuarioExterno other = (UsuarioExterno) object;
        if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.exp.entidades.UsuarioExterno[id=" + getId() + "]";
    }
}