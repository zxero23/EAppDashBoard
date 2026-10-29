package org.ccpm.tablero.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.ccpm.tablero.modelo.Obra;
import org.ccpm.tablero.modelo.Repositorios;
import org.ccpm.tablero.seguridad.Sesion;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.ccpm.tablero.api.Buscar.buscar;

@RestController
@RequestMapping("/api/obras")
@Transactional
public class ObraController {
    private final Repositorios.Obras obras;
    private final Repositorios.Organismos organismos;
    private final Repositorios.Situaciones situaciones;
    private final Repositorios.Localidades localidades;

    public ObraController(Repositorios.Obras obras, Repositorios.Organismos organismos,
                          Repositorios.Situaciones situaciones, Repositorios.Localidades localidades) {
        this.obras = obras;
        this.organismos = organismos;
        this.situaciones = situaciones;
        this.localidades = localidades;
    }

    public record Dto(Long id, String leyenda, Double latitud, Double longitud, LocalDate fechaInicio, LocalDate fechaFinal,
                      Long organismoId, String organismo, Long situacionId, String situacion, Long localidadId, String localidad) {
        static Dto de(Obra o) {
            return new Dto(o.id, o.leyenda, o.latitud, o.longitud,
                    o.fechaInicio == null ? null : o.fechaInicio.toLocalDate(), o.fechaFinal == null ? null : o.fechaFinal.toLocalDate(),
                    o.organismo.id, o.organismo.leyenda,
                    o.situacion == null ? null : o.situacion.id, o.situacion == null ? null : o.situacion.leyenda,
                    o.localidad == null ? null : o.localidad.id, o.localidad == null ? null : o.localidad.leyenda);
        }
    }

    public record Datos(@NotBlank String leyenda,
                        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitud,
                        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitud,
                        @NotNull LocalDate fechaInicio, @NotNull LocalDate fechaFinal,
                        @NotNull Long organismoId, @NotNull Long situacionId, @NotNull Long localidadId) {}

    @GetMapping
    @Transactional(readOnly = true)
    public List<Dto> listar(@RequestParam(required = false) Long organismoId, @RequestParam(required = false) Long situacionId,
                            @RequestParam(required = false) Long localidadId) {
        return obras.buscar(Sesion.filtro(organismoId), situacionId, localidadId).stream().map(Dto::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('nuevaObra')")
    public Dto crear(@Valid @RequestBody Datos d) {
        Obra o = new Obra();
        o.estado = true;
        return Dto.de(obras.save(aplicar(o, d)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('editarObra')")
    public Dto editar(@PathVariable Long id, @Valid @RequestBody Datos d) {
        return Dto.de(aplicar(cargar(id), d));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('borrarObra')")
    public void borrar(@PathVariable Long id) {
        cargar(id).estado = false;
    }

    private Obra cargar(Long id) {
        Obra o = buscar(obras, id);
        if (!o.estado) throw Buscar.noEncontrado();
        Sesion.verificar(o.organismo.id);
        return o;
    }

    private Obra aplicar(Obra o, Datos d) {
        if (d.fechaFinal().isBefore(d.fechaInicio()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha final es anterior a la inicial");
        Sesion.verificar(d.organismoId());
        o.leyenda = d.leyenda().trim().toUpperCase();
        o.latitud = d.latitud();
        o.longitud = d.longitud();
        o.fechaInicio = d.fechaInicio().atStartOfDay();
        o.fechaFinal = d.fechaFinal().atStartOfDay();
        o.organismo = buscar(organismos, d.organismoId());
        o.situacion = buscar(situaciones, d.situacionId());
        o.localidad = buscar(localidades, d.localidadId());
        return o;
    }
}
