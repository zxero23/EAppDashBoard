package org.ccpm.tablero.modelo;

import jakarta.persistence.*;

/** Permiso con nombre fijo (nuevaMeta, editarObra, ...); se asigna por grupo o directo al usuario. */
@Entity
@Table(name = "acciones")
public class Accion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String nombre;
    public boolean estado;
}
