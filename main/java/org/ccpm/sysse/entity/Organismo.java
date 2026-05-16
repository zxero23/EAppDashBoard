package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "Organismo")
public class Organismo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long codOrganigrama;
    private String nombre;
    private String descripcion;
    @OneToMany(mappedBy = "organismoPadre")
    private List<Organismo> organismos;
    @OneToMany(mappedBy = "organismo")
    private List<Usuario> listUsuarios;
    private boolean implementa;
    private boolean estado;//borrado lógico
    private int version;
    @ManyToOne
    private Organismo organismoPadre;
    @OneToMany
    private List <Movimiento> listaMovimientos;
    @OneToMany(mappedBy = "organismo")
    private List<Mesa> mesas;
    //Datos de implementación del sistema
    @Temporal(jakarta.persistence.TemporalType.DATE)
    @Column(name="fecha_inicio")
    private Date fechaInicio;
    @Column(name = "num_correlativo")
    private Long numCorrelativo;
    @Column(name = "administra_suborganismos")
    private boolean administraSubOrganismos;

    public Organismo() {
        this.organismos = new ArrayList<Organismo>();
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public List<Usuario> getListUsuarios() {
        return listUsuarios;
    }

    public void setListUsuarios(List<Usuario> listUsuarios) {
        this.listUsuarios = listUsuarios;
    }

    public boolean isImplementa() {
        return implementa;
    }

    public void setImplementa(boolean implementa) {
        this.implementa = implementa;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Organismo getOrganismoPadre() {
        return organismoPadre;
    }

    public void setOrganismoPadre(Organismo organismoPadre) {
        this.organismoPadre = organismoPadre;
    }

    public List<Movimiento> getListaMovimientos() {
        return listaMovimientos;
    }

    public void setListaMovimientos(List<Movimiento> listaMovimientos) {
        this.listaMovimientos = listaMovimientos;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Long getNumCorrelativo() {
        return numCorrelativo;
    }

    public void setNumCorrelativo(Long numCorrelativo) {
        this.numCorrelativo = numCorrelativo;
    }

    public List<Mesa> getMesas() {
        return mesas;
    }

    public void setMesas(List<Mesa> mesas) {
        this.mesas = mesas;
    }

    public boolean isAdministraSubOrganismos() {
        return administraSubOrganismos;
    }

    public void setAdministraSubOrganismos(boolean administraSubOrganismos) {
        this.administraSubOrganismos = administraSubOrganismos;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
     
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
        return "org.ccpm.entity.Organismo[id=" + id + "]";
    }

}
