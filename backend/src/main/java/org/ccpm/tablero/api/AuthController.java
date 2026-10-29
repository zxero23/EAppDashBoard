package org.ccpm.tablero.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.ccpm.tablero.seguridad.Sesion;
import org.ccpm.tablero.seguridad.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager auth;
    private final SecurityContextRepository contextos;
    private final UsuarioService usuarios;

    public AuthController(AuthenticationManager auth, SecurityContextRepository contextos, UsuarioService usuarios) {
        this.auth = auth;
        this.contextos = contextos;
        this.usuarios = usuarios;
    }

    public record Login(@NotBlank String usuario, @NotBlank String clave) {}

    public record Yo(Long id, String usuario, String nombre, Long organismoId, String organismo, boolean global, List<String> permisos) {
        static Yo de(Sesion s) {
            return new Yo(s.id(), s.usuario(), s.nombre(), s.organismoId(), s.organismo(), s.global(),
                    s.permisos().stream().map(GrantedAuthority::getAuthority).sorted().toList());
        }
    }

    /** La SPA lo llama al arrancar para recibir la cookie XSRF-TOKEN. */
    @GetMapping("/csrf")
    public void csrf(CsrfToken token) {
        token.getToken();
    }

    @PostMapping("/login")
    public Yo login(@Valid @RequestBody Login login, HttpServletRequest req, HttpServletResponse res) {
        Authentication a;
        try {
            a = auth.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(login.usuario(), login.clave()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o clave incorrectos");
        }
        if (req.getSession(false) != null) req.changeSessionId();
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(a);
        SecurityContextHolder.setContext(ctx);
        contextos.saveContext(ctx, req, res);
        Sesion s = (Sesion) a.getPrincipal();
        usuarios.registrarLogin(s.id());
        return Yo.de(s);
    }

    @GetMapping("/yo")
    public Yo yo() {
        return Yo.de(Sesion.actual());
    }
}
