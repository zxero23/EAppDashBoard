package org.ccpm.tablero.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/** Repositorios Spring Data. Agrupados porque son todos de una o dos lineas. */
public final class Repositorios {
    private Repositorios() {}

    /** Cantidad de metas u obras de un organismo en una situacion. */
    public interface Conteo {
        Long getOrganismoId();
        Long getSituacionId();
        Long getCantidad();
    }

    public interface Organismos extends JpaRepository<Organismo, Long> {
        List<Organismo> findByEstadoTrueAndIdNotOrderByLeyenda(Long excluido);
    }

    public interface Situaciones extends JpaRepository<Situacion, Long> {}

    public interface Localidades extends JpaRepository<Localidad, Long> {
        List<Localidad> findByEstadoTrueOrderByLeyenda();
    }

    public interface Objetivos extends JpaRepository<Objetivo, Long> {
        @Query("select o from Objetivo o where o.estado = true and (:org is null or o.organismo.id = :org) order by o.organismo.leyenda, o.leyenda")
        List<Objetivo> buscar(Long org);
    }

    public interface Metas extends JpaRepository<Meta, Long> {
        @Query("""
                select m from Meta m where m.estado = true
                and (:org is null or m.objetivo.organismo.id = :org)
                and (:sit is null or m.situacion.id = :sit)
                and (:obj is null or m.objetivo.id = :obj)
                order by m.objetivo.organismo.leyenda, m.objetivo.leyenda, m.leyenda""")
        List<Meta> buscar(Long org, Long sit, Long obj);

        @Query("""
                select m.objetivo.organismo.id as organismoId, m.situacion.id as situacionId, count(m) as cantidad
                from Meta m where m.estado = true and m.situacion.id in :sits
                group by m.objetivo.organismo.id, m.situacion.id""")
        List<Conteo> contarPorOrganismo(List<Long> sits);
    }

    public interface Indicadores extends JpaRepository<Indicador, Long> {
        List<Indicador> findByMetaIdAndEstadoTrueOrderById(Long metaId);
    }

    public interface Actividades extends JpaRepository<Actividad, Long> {
        List<Actividad> findByMetaIdAndEstadoTrueOrderById(Long metaId);
    }

    public interface Obras extends JpaRepository<Obra, Long> {
        @Query("""
                select o from Obra o where o.estado = true
                and (:org is null or o.organismo.id = :org)
                and (:sit is null or o.situacion.id = :sit)
                and (:loc is null or o.localidad.id = :loc)
                order by o.organismo.leyenda, o.leyenda""")
        List<Obra> buscar(Long org, Long sit, Long loc);

        @Query("""
                select o.organismo.id as organismoId, o.situacion.id as situacionId, count(o) as cantidad
                from Obra o where o.estado = true and o.situacion.id in :sits
                group by o.organismo.id, o.situacion.id""")
        List<Conteo> contarPorOrganismo(List<Long> sits);
    }

    public interface Usuarios extends JpaRepository<Usuario, Long> {
        Optional<Usuario> findByNombreUsuarioAndEstadoTrue(String nombreUsuario);
    }
}
