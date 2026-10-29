package org.ccpm.tablero.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.ccpm.tablero.modelo.*;
import org.ccpm.tablero.seguridad.Sesion;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.ccpm.tablero.api.Buscar.buscar;

/** Metas y lo que cuelga de ellas: indicadores (series para el grafico) y actividades. */
@RestController
@RequestMapping("/api")
@Transactional
public class MetaController {
    private final Repositorios.Metas metas;
    private final Repositorios.Objetivos objetivos;
    private final Repositorios.Situaciones situaciones;
    private final Repositorios.Indicadores indicadores;
    private final Repositorios.Actividades actividades;

    public MetaController(Repositorios.Metas metas, Repositorios.Objetivos objetivos, Repositorios.Situaciones situaciones,
                          Repositorios.Indicadores indicadores, Repositorios.Actividades actividades) {
        this.metas = metas;
        this.objetivos = objetivos;
        this.situaciones = situaciones;
        this.indicadores = indicadores;
        this.actividades = actividades;
    }

    public record Dto(Long id, String leyenda, String descripcion, Integer cantidad, boolean reduccion,
                      LocalDate fechaInicio, LocalDate fechaFinal, Long objetivoId, String objetivo,
                      Long organismoId, String organismo, Long situacionId, String situacion,
                      Double avance, int indicadores) {
        static Dto de(Meta m) {
            return new Dto(m.id, m.leyenda, m.descripcion, m.cantidad, m.esReduccion(), fecha(m.fechaInicio), fecha(m.fechaFinal),
                    m.objetivo.id, m.objetivo.leyenda, m.objetivo.organismo.id, m.objetivo.organismo.leyenda,
                    m.situacion == null ? null : m.situacion.id, m.situacion == null ? null : m.situacion.leyenda,
                    m.avance(), (int) m.indicadores.stream().filter(i -> i.estado).count());
        }
    }

    public record Datos(@NotBlank String leyenda, String descripcion, @NotNull @PositiveOrZero Integer cantidad,
                        boolean reduccion, @NotNull LocalDate fechaInicio, @NotNull LocalDate fechaFinal,
                        @NotNull Long objetivoId, @NotNull Long situacionId) {}

    /** valorMeta: valor a alcanzar segun la linea de base (primer periodo) y el % de la meta. */
    public record IndicadorDto(Long id, String leyenda, String descripcion, String variable, List<Indicador.Punto> puntos,
                               Double valorMeta, Double avance) {
        static IndicadorDto de(Indicador i) {
            List<Double> v = i.valores();
            return new IndicadorDto(i.id, i.leyenda, i.descripcion, i.variable(), i.puntos(),
                    Indicador.valorMeta(v, i.meta.cantidad, i.meta.esReduccion()),
                    Indicador.avance(v, i.meta.cantidad, i.meta.esReduccion()));
        }
    }

    public record IndicadorDatos(@NotBlank String leyenda, String descripcion, @NotBlank String variable,
                                 @NotEmpty List<@Valid Punto> puntos) {}

    public record Punto(@NotBlank String periodo, @NotNull Double valor) {}

    public record ActividadDto(Long id, String leyenda, Integer cantidad) {}

    public record ActividadDatos(@NotBlank String leyenda, @NotNull @PositiveOrZero Integer cantidad) {}

    // ---- metas

    @GetMapping("/metas")
    @Transactional(readOnly = true)
    public List<Dto> listar(@RequestParam(required = false) Long organismoId, @RequestParam(required = false) Long situacionId,
                            @RequestParam(required = false) Long objetivoId) {
        return metas.buscar(Sesion.filtro(organismoId), situacionId, objetivoId).stream().map(Dto::de).toList();
    }

    @PostMapping("/metas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('nuevaMeta')")
    public Dto crear(@Valid @RequestBody Datos d) {
        Meta m = new Meta();
        m.estado = true;
        m.indicadores = List.of();
        return Dto.de(metas.save(aplicar(m, d)));
    }

    @PutMapping("/metas/{id}")
    @PreAuthorize("hasAuthority('editarMeta')")
    public Dto editar(@PathVariable Long id, @Valid @RequestBody Datos d) {
        return Dto.de(aplicar(cargar(id), d));
    }

    @DeleteMapping("/metas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('borrarMeta')")
    public void borrar(@PathVariable Long id) {
        cargar(id).estado = false;
    }

    // ---- indicadores

    @GetMapping("/metas/{id}/indicadores")
    @Transactional(readOnly = true)
    public List<IndicadorDto> indicadores(@PathVariable Long id) {
        cargar(id);
        return indicadores.findByMetaIdAndEstadoTrueOrderById(id).stream().map(IndicadorDto::de).toList();
    }

    @PostMapping("/metas/{id}/indicadores")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('nuevoIndicador')")
    public IndicadorDto crearIndicador(@PathVariable Long id, @Valid @RequestBody IndicadorDatos d) {
        Indicador i = new Indicador();
        i.meta = cargar(id);
        i.estado = true;
        i.kml = "";
        return IndicadorDto.de(indicadores.save(aplicar(i, d)));
    }

    @PutMapping("/indicadores/{id}")
    @PreAuthorize("hasAuthority('editarIndicador')")
    public IndicadorDto editarIndicador(@PathVariable Long id, @Valid @RequestBody IndicadorDatos d) {
        return IndicadorDto.de(aplicar(cargarIndicador(id), d));
    }

    @DeleteMapping("/indicadores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('borrarIndicador')")
    public void borrarIndicador(@PathVariable Long id) {
        cargarIndicador(id).estado = false;
    }

    // ---- actividades (las edita quien puede editar la meta)

    @GetMapping("/metas/{id}/actividades")
    @Transactional(readOnly = true)
    public List<ActividadDto> actividades(@PathVariable Long id) {
        cargar(id);
        return actividades.findByMetaIdAndEstadoTrueOrderById(id).stream()
                .map(a -> new ActividadDto(a.id, a.leyenda, a.cantidad)).toList();
    }

    @PostMapping("/metas/{id}/actividades")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('editarMeta')")
    public ActividadDto crearActividad(@PathVariable Long id, @Valid @RequestBody ActividadDatos d) {
        Actividad a = new Actividad();
        a.meta = cargar(id);
        a.estado = true;
        a.leyenda = d.leyenda().trim().toUpperCase();
        a.cantidad = d.cantidad();
        a = actividades.save(a);
        return new ActividadDto(a.id, a.leyenda, a.cantidad);
    }

    @DeleteMapping("/actividades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('editarMeta')")
    public void borrarActividad(@PathVariable Long id) {
        Actividad a = buscar(actividades, id);
        cargar(a.meta.id);
        a.estado = false;
    }

    // ----

    private Meta cargar(Long id) {
        Meta m = buscar(metas, id);
        if (!m.estado) throw Buscar.noEncontrado();
        Sesion.verificar(m.objetivo.organismo.id);
        return m;
    }

    private Indicador cargarIndicador(Long id) {
        Indicador i = buscar(indicadores, id);
        if (!i.estado) throw Buscar.noEncontrado();
        cargar(i.meta.id);
        return i;
    }

    private Meta aplicar(Meta m, Datos d) {
        if (d.fechaFinal().isBefore(d.fechaInicio()))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha final es anterior a la inicial");
        Objetivo o = buscar(objetivos, d.objetivoId());
        Sesion.verificar(o.organismo.id);
        m.leyenda = d.leyenda().trim().toUpperCase();
        m.descripcion = d.descripcion();
        m.cantidad = d.cantidad();
        m.reduccion = d.reduccion();
        m.fechaInicio = d.fechaInicio().atStartOfDay();
        m.fechaFinal = d.fechaFinal().atStartOfDay();
        m.objetivo = o;
        m.situacion = buscar(situaciones, d.situacionId());
        return m;
    }

    private static Indicador aplicar(Indicador i, IndicadorDatos d) {
        i.leyenda = d.leyenda().trim().toUpperCase();
        i.descripcion = d.descripcion();
        i.formula = i.formula == null ? "" : i.formula;
        i.setVariable(d.variable().trim().toUpperCase());
        i.setPuntos(d.puntos().stream().map(p -> new Indicador.Punto(p.periodo().trim(), p.valor())).toList());
        return i;
    }

    private static LocalDate fecha(LocalDateTime t) {
        return t == null ? null : t.toLocalDate();
    }
}
