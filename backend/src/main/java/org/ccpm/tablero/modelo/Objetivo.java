package org.ccpm.tablero.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "objetivos")
public class Objetivo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String leyenda;
    public String descripcion;
    public boolean estado;
    @ManyToOne @JoinColumn(name = "organismo_id")
    public Organismo organismo;
    @ManyToOne @JoinColumn(name = "situacion_id")
    public Situacion situacion;
}
