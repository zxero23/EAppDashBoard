package org.ccpm.tablero.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "actividades")
public class Actividad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String leyenda;
    public Integer cantidad;
    public boolean estado;
    @ManyToOne @JoinColumn(name = "metas_id")
    public Meta meta;
}
