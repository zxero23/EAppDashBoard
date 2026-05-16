package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name="registros")
public class Registro implements Serializable {    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    private Date fecha;
    private String nombre;
    private String telefono;
    private String email;
    private String organismo;
    private String observacion;
    private boolean estado;//borrado lógico    

    public Registro() {
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setOrganismo(String organismo) {
        this.organismo = organismo;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public String getEmail() {
        return email;
    }

    public Date getFecha() {
        return fecha;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getOrganismo() {
        return organismo;
    }

    public String getTelefono() {
        return telefono;
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
        if (!(object instanceof Registro)) {
            return false;
        }
        Registro other = (Registro) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.sysse.entity.Registro[ id=" + id + " ]";
    }
    
}
