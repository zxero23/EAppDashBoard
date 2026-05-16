package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "mesas")
@PrimaryKeyJoinColumn(name = "id")
public class Mesa extends Iniciador implements Serializable {
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "fecha_alta")
    private Date fechaAlta;
    private String descripcion;
    @ManyToOne
    private Organismo organismo;
    @Column(name = "is_mesa_general")
    private boolean mesaGeneral;
    @Column(name = "esconder_mesa")
    private boolean esconderMesa;
    private boolean iniciador;   

    public Mesa() {
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Organismo getOrganismo() {
        return organismo;
    }

    public void setOrganismo(Organismo organismo) {
        this.organismo = organismo;
    }

   

    public boolean isMesaGeneral() {
        return mesaGeneral;
    }

    public void setMesaGeneral(boolean mesaGeneral) {
        this.mesaGeneral = mesaGeneral;
    }

    public boolean isIniciador() {
        return iniciador;
    }

    public void setIniciador(boolean iniciador) {
        this.iniciador = iniciador;
    }

    public boolean isEsconderMesa() {
        return esconderMesa;
    }

    public void setEsconderMesa(boolean esconderMesa) {
        this.esconderMesa = esconderMesa;
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
        if (!(object instanceof Mesa)) {
            return false;
        }
        Mesa other = (Mesa) object;
        if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.exp.entidades.Mesa[id=" + getId() + "]";
    }
}
