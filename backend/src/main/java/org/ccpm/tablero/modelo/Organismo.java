package org.ccpm.tablero.modelo;

import jakarta.persistence.*;

/** Ministerio (descentralizado=false, tiene objetivos y metas) o ente ejecutor de obras (descentralizado=true). Id 0 = vista global. */
@Entity
@Table(name = "organismos")
public class Organismo {
    public static final long GLOBAL = 0L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String leyenda;
    public String descripcion;
    @Column(name = "cod_organigrama")
    public Long codOrganigrama;
    public boolean descentralizado;
    public boolean estado;
}
