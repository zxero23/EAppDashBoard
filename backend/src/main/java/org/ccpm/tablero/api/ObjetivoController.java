package org.ccpm.tablero.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.ccpm.tablero.modelo.Objetivo;
import org.ccpm.tablero.modelo.Repositorios;
import org.ccpm.tablero.seguridad.Sesion;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.ccpm.tablero.api.Buscar.buscar;

@RestController
@RequestMapping("/api/objetivos")
@Transactional
public class ObjetivoController {
    private final Repositorios.Objetivos objetivos;
    private final Repositorios.Organismos organismos;
    private final Repositorios.Situaciones situaciones;

    public ObjetivoController(Repositorios.Objetivos objetivos, Repositorios.Organismos organismos, Repositorios.Situaciones situaciones) {
        this.objetivos = objetivos;
        this.organismos = organismos;
        this.situaciones = situaciones;
    }

    public record Dto(Long id, String leyenda, String descripcion, Long organismoId, String organismo, Long situacionId, String situacion) {
        static Dto de(Objetivo o) {
            return new Dto(o.id, o.leyenda, o.descripcion, o.organismo.id, o.organismo.leyenda,
                    o.situacion == null ? null : o.situacion.id, o.situacion == null ? null : o.situacion.leyenda);
        }
    }

    public record Datos(@NotBlank String leyenda, String descripcion, @NotNull Long organismoId, @NotNull Long situacionId) {}

    @GetMapping
    @Transactional(readOnly = true)
    public List<Dto> listar(@RequestParam(required = false) Long organismoId) {
        return objetivos.buscar(Sesion.filtro(organismoId)).stream().map(Dto::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('nuevoObjetivos')")
    public Dto crear(@Valid @RequestBody Datos d) {
        Objetivo o = new Objetivo();
        o.estado = true;
        return Dto.de(objetivos.save(aplicar(o, d)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('editarObjetivos')")
    public Dto editar(@PathVariable Long id, @Valid @RequestBody Datos d) {
        return Dto.de(aplicar(cargar(id), d));
    }

    /** Borrado logico, como en la v1. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('borrarObjetivos')")
    public void borrar(@PathVariable Long id) {
        cargar(id).estado = false;
    }

    Objetivo cargar(Long id) {
        Objetivo o = buscar(objetivos, id);
        if (!o.estado) throw Buscar.noEncontrado();
        Sesion.verificar(o.organismo.id);
        return o;
    }

    private Objetivo aplicar(Objetivo o, Datos d) {
        Sesion.verificar(d.organismoId());
        o.leyenda = d.leyenda().trim().toUpperCase();
        o.descripcion = d.descripcion();
        o.organismo = buscar(organismos, d.organismoId());
        o.situacion = buscar(situaciones, d.situacionId());
        return o;
    }
}
