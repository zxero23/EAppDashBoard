package org.ccpm.sysse.utilidad;

import java.io.Serializable;

public class ExpedienteClave implements Serializable {

    private Long codOrganismo;
    private Long numCorrelativo;
    private int anio;

    public ExpedienteClave() {
    }

    public ExpedienteClave(Long codOrganismo, Long numCorrelativo, int anio) {
        this.codOrganismo = codOrganismo;
        this.numCorrelativo = numCorrelativo;
        this.anio = anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public void setCodOrganismo(Long codOrganismo) {
        this.codOrganismo = codOrganismo;
    }

    public void setNumCorrelativo(Long numCorrelativo) {
        this.numCorrelativo = numCorrelativo;
    }

    public int getAnio() {
        return anio;
    }

    public Long getCodOrganismo() {
        return codOrganismo;
    }

    public Long getNumCorrelativo() {
        return numCorrelativo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ExpedienteClave)) {
            return false;
        }
        ExpedienteClave expedientePk = (ExpedienteClave) obj;
        // Usar Objects.equals para comparar Long de forma segura (maneja nulls)
        if (this.codOrganismo == null) {
            if (expedientePk.codOrganismo != null) {
                return false;
            }
        } else if (!this.codOrganismo.equals(expedientePk.codOrganismo)) {
            return false;
        }
        if (this.numCorrelativo == null) {
            if (expedientePk.numCorrelativo != null) {
                return false;
            }
        } else if (!this.numCorrelativo.equals(expedientePk.numCorrelativo)) {
            return false;
        }
        // anio es int primitivo, se compara directamente
        return this.anio == expedientePk.anio;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.codOrganismo != null ? this.codOrganismo.hashCode() : 0);
        hash = 31 * hash + (this.numCorrelativo != null ? this.numCorrelativo.hashCode() : 0);
        hash = 31 * hash + this.anio;
        return hash;
    }


}

