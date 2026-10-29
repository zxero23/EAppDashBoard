package org.ccpm.tablero.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "localidad")
public class Localidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String leyenda;
    public boolean estado;
}
