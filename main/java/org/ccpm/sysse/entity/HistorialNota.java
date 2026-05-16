package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "historialNotas")
public class HistorialNota implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    private Date fecha;
    @Column(length = 5000)
    private String observacion;   
    @OneToOne
    private Mesa mesaOrigen;
    @OneToOne
    private Mesa mesaDestino;
    @OneToOne
    private Usuario usuario;
    @OneToOne
    private Estado estadoNota;
     @OneToOne
    private Iniciador iniciador;
    private boolean estado;//borrado lógico

    public HistorialNota() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setEstadoNota(Estado estadoNota) {
        this.estadoNota = estadoNota;
    }

    public void setMesaDestino(Mesa mesaDestino) {
        this.mesaDestino = mesaDestino;
    }

    public void setMesaOrigen(Mesa mesaOrigen) {
        this.mesaOrigen = mesaOrigen;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setIniciador(Iniciador iniciador) {
        this.iniciador = iniciador;
    }

    public Iniciador getIniciador() {
        return iniciador;
    }

    public Long getId() {
        return id;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getObservacion() {
        return observacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public Estado getEstadoNota() {
        return estadoNota;
    }

    public Mesa getMesaDestino() {
        return mesaDestino;
    }

    public Mesa getMesaOrigen() {
        return mesaOrigen;
    }

    public Usuario getUsuario() {
        return usuario;
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
        if (!(object instanceof HistorialNota)) {
            return false;
        }
        HistorialNota other = (HistorialNota) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.exp.entidades.HistorialNota[id=" + id + "]";
    }
}
