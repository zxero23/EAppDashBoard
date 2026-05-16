package org.ccpm.sysse.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.*;
import org.ccpm.sysse.utilidad.ExpedienteClave;

@Entity
@Table(name = "expedientes")
@IdClass(ExpedienteClave.class)
public class Expediente implements Serializable {

    @Id
    @Column(name = "cod_organismo")
    private Long codOrganismo;
    @Id
    @Column(name = "num_correlativo")
    private Long numCorrelativo;
    @Id
    private int anio;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "fecha_creacion")
    private Date fechaCreacion;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "fecha_carga")
    private Date fechaCarga;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "ultimo_movimiento")
    private Date ultimoMovimiento;
    private int fojas;
    private int cuerpo;
    @Column(length = 5000)
    private String observacion;
    @Column(length = 5000)
    private String asunto;
    private String nroExp;
    private String ordenPago;
    @Column(name = "pedido_fondo")
    private Integer pedidoFondo;
    @Column(name = "pedido_fondo_anio")
    private Integer pedidoFondoAnio;
    @Column(name = "orden_entrega")
    private Integer ordenEntrega;
    @Column(name = "orden_entrega_anio")
    private Integer ordenEntregaAnio;
    @Column(name = "exp_madre")
    private String expMadre;
    @OneToOne
    private Usuario usuario;
    @OneToMany
    private List<InstrumentoLegal> listInstrumentosLegales;
    @OneToMany
    private List<HistorialExpediente> listHistoriales;
    @OneToOne
    private Estado estadoExpediente;
    @OneToOne
    private TipoAsunto tipoAsunto;
    @OneToOne
    private Mesa mesaOrigen;
    @OneToOne
    private Mesa mesaDestino;
    @OneToMany
    private List<Nota> listNotas;
    @OneToOne
    @JoinColumn(nullable = false)
    private Iniciador iniciador;
    private boolean estado;//borrado lógico
    @Transient
    private boolean checked;

    public Expediente() {
        this.listHistoriales = new ArrayList<HistorialExpediente>();
    }

    public void setEstadoExpediente(Estado estadoExpediente) {
        this.estadoExpediente = estadoExpediente;
    }

    public void setListHistoriales(List<HistorialExpediente> listHistoriales) {
        this.listHistoriales = listHistoriales;
    }

    public void setListInstrumentosLegales(List<InstrumentoLegal> listInstrumentosLegales) {
        this.listInstrumentosLegales = listInstrumentosLegales;
    }

    public void setListNotas(List<Nota> listNotas) {
        this.listNotas = listNotas;
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

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public void setCodOrganismo(Long codOrganismo) {
        this.codOrganismo = codOrganismo;
    }

    public void setCuerpo(int cuerpo) {
        this.cuerpo = cuerpo;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setFojas(int fojas) {
        this.fojas = fojas;
    }

    public void setNumCorrelativo(Long numCorrelativo) {
        this.numCorrelativo = numCorrelativo;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public void setIniciador(Iniciador iniciador) {
        this.iniciador = iniciador;
    }

    public void setNroExp(String nroExp) {
        this.nroExp = nroExp;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public Date getUltimoMovimiento() {
        return ultimoMovimiento;
    }

    public void setUltimoMovimiento(Date ultimoMovimiento) {
        this.ultimoMovimiento = ultimoMovimiento;
    }

    public void setOrdenPago(String ordenPago) {
        this.ordenPago = ordenPago;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public String getOrdenPago() {
        return ordenPago;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public String getNroExp() {
        return nroExp;
    }

    public Iniciador getIniciador() {
        return iniciador;
    }

    public String getAsunto() {
        return asunto;
    }

    public int getAnio() {
        return anio;
    }

    public Long getCodOrganismo() {
        return codOrganismo;
    }

    public int getCuerpo() {
        return cuerpo;
    }

    public boolean isEstado() {
        return estado;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public int getFojas() {
        return fojas;
    }

    public Long getNumCorrelativo() {
        return numCorrelativo;
    }

    public String getObservacion() {
        return observacion;
    }

    public Estado getEstadoExpediente() {
        return estadoExpediente;
    }

    public List<HistorialExpediente> getListHistoriales() {
        return listHistoriales;
    }

    public List<HistorialExpediente> getListHistorialesOrdenados() {
        return listHistoriales.stream()
                .filter(i -> i.isEstado())
                .sorted(Comparator.comparing(HistorialExpediente::getFecha)
                        .reversed())
                .collect(Collectors.toList());
    }

    public List<InstrumentoLegal> getListInstrumentosLegales() {
        return listInstrumentosLegales;
    }

    public List<Nota> getListNotas() {
        return listNotas;
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

    public Integer getPedidoFondo() {
        return pedidoFondo;
    }

    public void setPedidoFondo(Integer pedidoFondo) {
        this.pedidoFondo = pedidoFondo;
    }

    public Integer getPedidoFondoAnio() {
        return pedidoFondoAnio;
    }

    public void setPedidoFondoAnio(Integer pedidoFondoAnio) {
        this.pedidoFondoAnio = pedidoFondoAnio;
    }

    public Integer getOrdenEntrega() {
        return ordenEntrega;
    }

    public void setOrdenEntrega(Integer ordenEntrega) {
        this.ordenEntrega = ordenEntrega;
    }

    public Integer getOrdenEntregaAnio() {
        return ordenEntregaAnio;
    }

    public void setOrdenEntregaAnio(Integer ordenEntregaAnio) {
        this.ordenEntregaAnio = ordenEntregaAnio;
    }

    public String getExpMadre() {
        return expMadre;
    }

    public void setExpMadre(String expMadre) {
        this.expMadre = expMadre;
    }

    @Override
    public String toString() {
        return "Expediente{" + "codOrganismo=" + codOrganismo + ", numCorrelativo=" + numCorrelativo + ", anio=" + anio + '}';
    }

}
