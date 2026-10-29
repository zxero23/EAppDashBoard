package org.ccpm.tablero.seguridad;

import org.ccpm.tablero.modelo.Accion;
import org.ccpm.tablero.modelo.Grupo;
import org.ccpm.tablero.modelo.Repositorios;
import org.ccpm.tablero.modelo.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
public class UsuarioService implements UserDetailsService, UserDetailsPasswordService {
    private final Repositorios.Usuarios usuarios;

    public UsuarioService(Repositorios.Usuarios usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public Sesion loadUserByUsername(String nombre) {
        Usuario u = usuarios.findByNombreUsuarioAndEstadoTrue(nombre.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o clave incorrectos"));
        List<GrantedAuthority> permisos = Stream.concat(
                        u.grupos.stream().filter(g -> g.estado).flatMap((Grupo g) -> g.acciones.stream()),
                        u.acciones.stream())
                .filter(a -> a.estado).map((Accion a) -> (GrantedAuthority) new SimpleGrantedAuthority(a.nombre))
                .distinct().toList();
        String nombreCompleto = String.join(" ", Stream.of(u.nombre, u.apellido).filter(s -> s != null).toList());
        Long orgId = u.organismo == null ? null : u.organismo.id;
        String org = u.organismo == null ? null : u.organismo.leyenda;
        return new Sesion(u.id, u.nombreUsuario, nombreCompleto, orgId, org, u.claveAcceso, permisos);
    }

    /** Lo llama Spring Security cuando ClaveEncoder pide migrar una clave legacy a BCrypt. */
    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String nuevaClave) {
        Sesion s = (Sesion) user;
        usuarios.findById(s.id()).ifPresent(u -> u.claveAcceso = nuevaClave);
        return new Sesion(s.id(), s.usuario(), s.nombre(), s.organismoId(), s.organismo(), nuevaClave, s.permisos());
    }

    @Transactional
    public void registrarLogin(Long id) {
        usuarios.findById(id).ifPresent(u -> u.ultimoLogin = java.time.LocalDateTime.now());
    }
}
