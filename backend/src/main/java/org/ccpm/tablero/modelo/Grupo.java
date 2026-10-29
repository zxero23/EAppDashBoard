package org.ccpm.tablero.modelo;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "grupos")
public class Grupo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String nombre;
    public boolean estado;
    @ManyToMany
    @JoinTable(name = "grupos_acciones", joinColumns = @JoinColumn(name = "grupo_id"), inverseJoinColumns = @JoinColumn(name = "acciones_id"))
    public Set<Accion> acciones;
}
