/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;

/**
 *
 * @author Matias
 */
@Entity
@Table(name = "notas_eliminadas")
public class NotaEliminada implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "fecha_creacion")
    private Date fechaCreacion;
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
    private boolean checked;
    @OneToOne
    @JoinColumn(name = "usuario_elimina")
    private Usuario usuarioElimina;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "fecha_elimina")
    private Date fechaElimina;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getNroNota() {
        return nroNota;
    }

    public void setNroNota(String nroNota) {
        this.nroNota = nroNota;
    }

    public TipoAsunto getTipoAsunto() {
        return tipoAsunto;
    }

    public void setTipoAsunto(TipoAsunto tipoAsunto) {
        this.tipoAsunto = tipoAsunto;
    }

    public List<HistorialNota> getListaHistorial() {
        return listaHistorial;
    }

    public void setListaHistorial(List<HistorialNota> listaHistorial) {
        this.listaHistorial = listaHistorial;
    }

    public Estado getEstadoNota() {
        return estadoNota;
    }

    public void setEstadoNota(Estado estadoNota) {
        this.estadoNota = estadoNota;
    }

    public Mesa getMesaOrigen() {
        return mesaOrigen;
    }

    public void setMesaOrigen(Mesa mesaOrigen) {
        this.mesaOrigen = mesaOrigen;
    }

    public Mesa getMesaDestino() {
        return mesaDestino;
    }

    public void setMesaDestino(Mesa mesaDestino) {
        this.mesaDestino = mesaDestino;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Iniciador getIniciador() {
        return iniciador;
    }

    public void setIniciador(Iniciador iniciador) {
        this.iniciador = iniciador;
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

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Usuario getUsuarioElimina() {
        return usuarioElimina;
    }

    public void setUsuarioElimina(Usuario usuarioElimina) {
        this.usuarioElimina = usuarioElimina;
    }

    public Date getFechaElimina() {
        return fechaElimina;
    }

    public void setFechaElimina(Date fechaElimina) {
        this.fechaElimina = fechaElimina;
    }

    
    
    
}
