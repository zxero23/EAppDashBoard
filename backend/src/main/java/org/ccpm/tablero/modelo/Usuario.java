package org.ccpm.tablero.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "nombre_usuario")
    public String nombreUsuario;
    /** Texto plano en la v1; BCrypt una vez migrada. */
    @Column(name = "clave_acceso")
    public String claveAcceso;
    public String nombre;
    public String apellido;
    public String email;
    public boolean estado;
    @Column(name = "ultimo_login")
    public LocalDateTime ultimoLogin;
    @ManyToOne @JoinColumn(name = "organismo_id")
    public Organismo organismo;
    @ManyToMany
    @JoinTable(name = "usuarios_grupos", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "grupos_id"))
    public Set<Grupo> grupos;
    @ManyToMany
    @JoinTable(name = "usuarios_acciones", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "acciones_id"))
    public Set<Accion> acciones;
}
