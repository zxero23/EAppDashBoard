package org.ccpm.sysse.entity;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name = "instLegales")
public class InstrumentoLegal implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String numero;
    private String descripcion;
    @OneToOne
    private TipoInstrumentoLegal tipoInstrumentoLegal;
    private boolean agregado;
    private boolean enviado;
    @OneToOne
    private Usuario usuarioCreacion;
    private int fojas;
    private String archivo;
    private boolean estado; //borrado logico

    public InstrumentoLegal() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setTipoInstrumentoLegal(TipoInstrumentoLegal tipoInstrumentoLegal) {
        this.tipoInstrumentoLegal = tipoInstrumentoLegal;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setAgregado(boolean agregado) {
        this.agregado = agregado;
    }

    public void setUsuarioCreacion(Usuario usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public void setFojas(int fojas) {
        this.fojas = fojas;
    }

    public void setEnviado(boolean enviado) {
        this.enviado = enviado;
    }

    public boolean isEnviado() {
        return enviado;
    }

    public int getFojas() {
        return fojas;
    }

    public Usuario getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public boolean isAgregado() {
        return agregado;
    }

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNumero() {
        return numero;
    }

    public boolean isEstado() {
        return estado;
    }

    public TipoInstrumentoLegal getTipoInstrumentoLegal() {
        return tipoInstrumentoLegal;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof InstrumentoLegal)) {
            return false;
        }
        InstrumentoLegal other = (InstrumentoLegal) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.exp.entidades.InstLegal[id=" + id + "]";
    }
}
