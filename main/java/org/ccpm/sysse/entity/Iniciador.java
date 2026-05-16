package org.ccpm.sysse.entity;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name = "iniciador")
@Inheritance(strategy = InheritanceType.JOINED)
public class Iniciador implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre")
    private String nombre;
    @JoinColumn(name = "tipoiniciador_id")
    @OneToOne
    private TipoIniciador tipoIniciador;
    @Column(name = "matricula_provincial")
    private String matriculaProvincial; /*Atributo de Cooperativas */
    @Column(name = "matricula_nacional")
    private String matriculaNacional;
    @Column(name = "documento")
    private String documento;
    private boolean estado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }   

    public TipoIniciador getTipoIniciador() {
        return tipoIniciador;
    }

    public void setTipoIniciador(TipoIniciador tipoIniciador) {
        this.tipoIniciador = tipoIniciador;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getMatriculaNacional() {
        return matriculaNacional;
    }

    public void setMatriculaNacional(String matriculaNacional) {
        this.matriculaNacional = matriculaNacional;
    }

    public String getMatriculaProvincial() {
        return matriculaProvincial;
    }

    public void setMatriculaProvincial(String matriculaProvincial) {
        this.matriculaProvincial = matriculaProvincial;
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
        if (!(object instanceof Iniciador)) {
            return false;
        }
        Iniciador other = (Iniciador) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.exp.entidades.Iniciador[id=" + id + "]";
    }
}

