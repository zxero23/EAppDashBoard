package org.ccpm.tablero.api;

import org.ccpm.tablero.modelo.*;
import org.ccpm.tablero.seguridad.Sesion;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Datos del semaforo y catalogos para los formularios. */
@RestController
@RequestMapping("/api")
@Transactional(readOnly = true)
public class TableroController {
    private final Repositorios.Organismos organismos;
    private final Repositorios.Situaciones situaciones;
    private final Repositorios.Localidades localidades;
    private final Repositorios.Metas metas;
    private final Repositorios.Obras obras;

    public TableroController(Repositorios.Organismos organismos, Repositorios.Situaciones situaciones,
                             Repositorios.Localidades localidades, Repositorios.Metas metas, Repositorios.Obras obras) {
        this.organismos = organismos;
        this.situaciones = situaciones;
        this.localidades = localidades;
        this.metas = metas;
        this.obras = obras;
    }

    public record Item(Long id, String leyenda) {}
    public record OrganismoItem(Long id, String leyenda, boolean descentralizado) {}
    public record Fila(Long id, String leyenda, Map<Long, Long> conteos, long total) {}
    public record Tablero(List<Fila> ministerios, List<Fila> entes) {}
    /** semaforo: situaciones del tablero en orden verde, amarillo, rojo, azul. */
    public record Catalogos(List<Item> semaforo, List<Item> situaciones, List<Item> localidades, List<OrganismoItem> organismos) {}

    /** Ministerios (no descentralizados) cuentan metas; entes (descentralizados) cuentan obras. */
    @GetMapping("/tablero")
    public Tablero tablero() {
        List<Organismo> visibles = visibles();
        return new Tablero(
                filas(visibles.stream().filter(o -> !o.descentralizado).toList(), metas.contarPorOrganismo(Semaforo.IDS)),
                filas(visibles.stream().filter(o -> o.descentralizado).toList(), obras.contarPorOrganismo(Semaforo.IDS)));
    }

    @GetMapping("/catalogos")
    public Catalogos catalogos() {
        return new Catalogos(
                situaciones.findAllById(Semaforo.IDS).stream().map(s -> new Item(s.id, s.leyenda))
                        .sorted((a, b) -> Long.compare(Semaforo.IDS.indexOf(a.id()), Semaforo.IDS.indexOf(b.id()))).toList(),
                situaciones.findAll().stream().filter(s -> s.estado).map(s -> new Item(s.id, s.leyenda)).toList(),
                localidades.findByEstadoTrueOrderByLeyenda().stream().map(l -> new Item(l.id, l.leyenda)).toList(),
                visibles().stream().map(o -> new OrganismoItem(o.id, o.leyenda, o.descentralizado)).toList());
    }

    private List<Organismo> visibles() {
        Sesion s = Sesion.actual();
        return organismos.findByEstadoTrueAndIdNotOrderByLeyenda(Organismo.GLOBAL).stream()
                .filter(o -> s.global() || o.id.equals(s.organismoId())).toList();
    }

    private static List<Fila> filas(List<Organismo> orgs, List<Repositorios.Conteo> conteos) {
        Map<Long, Map<Long, Long>> por = new HashMap<>();
        for (Repositorios.Conteo c : conteos)
            por.computeIfAbsent(c.getOrganismoId(), k -> new HashMap<>()).put(c.getSituacionId(), c.getCantidad());
        return orgs.stream().map(o -> {
            Map<Long, Long> m = new HashMap<>();
            Semaforo.IDS.forEach(id -> m.put(id, por.getOrDefault(o.id, Map.of()).getOrDefault(id, 0L)));
            return new Fila(o.id, o.leyenda, m, m.values().stream().mapToLong(Long::longValue).sum());
        }).toList();
    }
}
