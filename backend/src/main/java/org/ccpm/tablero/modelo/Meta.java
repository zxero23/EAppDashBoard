package org.ccpm.tablero.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "metas")
public class Meta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String leyenda;
    public String descripcion;
    /** % de variacion buscado respecto del primer valor del indicador (como en la v1), no un valor absoluto. */
    public Integer cantidad;
    /** true: reducir un "cantidad"%; false o null (metas viejas): aumentar un "cantidad"%. */
    public Boolean reduccion;
    public LocalDateTime fechaInicio;
    public LocalDateTime fechaFinal;
    public boolean estado;
    @ManyToOne @JoinColumn(name = "objetivos_id")
    public Objetivo objetivo;
    @ManyToOne @JoinColumn(name = "situacion_id")
    public Situacion situacion;
    @OneToMany(mappedBy = "meta")
    @OrderBy("id")
    public List<Indicador> indicadores;

    /** Avance en % segun el primer indicador activo; null si no hay datos. */
    public Double avance() {
        return indicadores.stream().filter(i -> i.estado).findFirst()
                .map(i -> Indicador.avance(i.valores(), cantidad, esReduccion())).orElse(null);
    }

    public boolean esReduccion() {
        return Boolean.TRUE.equals(reduccion);
    }
}
