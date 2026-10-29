package org.ccpm.tablero.seguridad;

import org.ccpm.tablero.modelo.Organismo;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

/**
 * Usuario logueado. Las authorities son los nombres de las acciones (nuevaMeta, editarObra, ...).
 * El organismo define el alcance: el organismo 0 ve todo, el resto solo lo suyo.
 */
public record Sesion(Long id, String usuario, String nombre, Long organismoId, String organismo,
                     String clave, List<GrantedAuthority> permisos) implements UserDetails {

    public boolean global() {
        return organismoId == null || organismoId == Organismo.GLOBAL;
    }

    public static Sesion actual() {
        return (Sesion) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /** Organismo por el que hay que filtrar: el pedido si es global, el propio si no. */
    public static Long filtro(Long pedido) {
        Sesion s = actual();
        return s.global() ? pedido : s.organismoId;
    }

    public static void verificar(Long organismoId) {
        Sesion s = actual();
        if (!s.global() && !s.organismoId.equals(organismoId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fuera del alcance de su organismo");
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return permisos; }
    @Override public String getPassword() { return clave; }
    @Override public String getUsername() { return usuario; }
}
