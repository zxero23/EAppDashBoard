package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.*;

@Entity
@Table(name = "notas")
public class Nota implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "fecha_creacion")
    private Date fechaCreacion;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "ultimo_movimiento")
    private Date ultimoMovimiento;
    @Column(length = 5000)
    private String referencia;
    @Column(length = 5000)
    private String observacion;
    private String nroNota;
    @OneToOne
    private TipoAsunto tipoAsunto;
    @OneToMany
    private List<HistorialNota> listaHistorial;
    @OneToOne
    private Estado estadoNota;
    @OneToOne
    private Mesa mesaOrigen;
    @OneToOne
    private Mesa mesaDestino;
    @OneToOne
    private Usuario usuario;
    @OneToOne
    @JoinColumn(nullable = false)
    private Iniciador iniciador;
    private String archivo;
    private Integer fojas;
    private boolean estado; //borrado logico
    @Transient
    private boolean checked;

    public Nota() {
        this.listaHistorial = new ArrayList<HistorialNota>();
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setEstadoNota(Estado estadoNota) {
        this.estadoNota = estadoNota;
    }

    public void setListaHistorial(List<HistorialNota> listaHistorial) {
        this.listaHistorial = listaHistorial;
    }

    public void setMesaDestino(Mesa mesaDestino) {
        this.mesaDestino = mesaDestino;
    }

    public void setMesaOrigen(Mesa mesaOrigen) {
        this.mesaOrigen = mesaOrigen;
    }

    public void setTipoAsunto(TipoAsunto tipoAsunto) {
        this.tipoAsunto = tipoAsunto;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getUltimoMovimiento() {
        return ultimoMovimiento;
    }

    public void setUltimoMovimiento(Date ultimoMovimiento) {
        this.ultimoMovimiento = ultimoMovimiento;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public void setIniciador(Iniciador iniciador) {
        this.iniciador = iniciador;
    }

    public void setNroNota(String nroNota) {
        this.nroNota = nroNota;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public String getNroNota() {
        return nroNota;
    }

    public Iniciador getIniciador() {
        return iniciador;
    }

    public Long getId() {
        return id;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public String getObservacion() {
        return observacion;
    }

    public String getReferencia() {
        return referencia;
    }

    public boolean isEstado() {
        return estado;
    }

    public Estado getEstadoNota() {
        return estadoNota;
    }

    public List<HistorialNota> getListaHistorial() {
         return listaHistorial;
    }
    
    public List<HistorialNota> getListaHistorialOrdenados() {
         return listaHistorial.stream()
                .filter(i -> i.isEstado())
                .sorted(Comparator.comparing(HistorialNota::getFecha)
                        .reversed())
                .collect(Collectors.toList());
    }

    public Mesa getMesaDestino() {
        return mesaDestino;
    }

    public Mesa getMesaOrigen() {
        return mesaOrigen;
    }

    public TipoAsunto getTipoAsunto() {
        return tipoAsunto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public Integer getFojas() {
        return fojas;
    }

    public void setFojas(Integer fojas) {
        this.fojas = fojas;
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
        if (!(object instanceof Nota)) {
            return false;
        }
        Nota other = (Nota) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.exp.entidades.Nota[id=" + id + "]";
    }
}
