package org.ccpm.sysse.entity;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name = "movimientos")
public class Movimiento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cod_org_anterior")
    private Long codOrganismoAnterior;
    @Column(name = "cod_org_nuevo")
    private Long codOrganismoNuevo;
    @Column(name = "version_mov")
    private int versionMovimiento;
    @Column(name = "id_padre_anterior")
    private Long idPadreAnterior;
    private String observacion;

    public Movimiento() {
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCodOrganismoAnterior(Long codOrganismoAnterior) {
        this.codOrganismoAnterior = codOrganismoAnterior;
    }

    public void setCodOrganismoNuevo(Long codOrganismoNuevo) {
        this.codOrganismoNuevo = codOrganismoNuevo;
    }

    public void setIdPadreAnterior(Long idPadreAnterior) {
        this.idPadreAnterior = idPadreAnterior;
    }

    public void setVersionMovimiento(int versionMovimiento) {
        this.versionMovimiento = versionMovimiento;
    }

    public String getObservacion() {
        return observacion;
    }

    public Long getId() {
        return id;
    }

    public Long getCodOrganismoAnterior() {
        return codOrganismoAnterior;
    }

    public Long getCodOrganismoNuevo() {
        return codOrganismoNuevo;
    }

    public Long getIdPadreAnterior() {
        return idPadreAnterior;
    }

    public int getVersionMovimiento() {
        return versionMovimiento;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Movimiento)) {
            return false;
        }
        Movimiento other = (Movimiento) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ccpm.entity.Movimiento[id=" + id + "]";
    }
}
