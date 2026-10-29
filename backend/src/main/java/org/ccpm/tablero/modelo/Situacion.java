package org.ccpm.tablero.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "situacion")
public class Situacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String leyenda;
    public boolean estado;
}
