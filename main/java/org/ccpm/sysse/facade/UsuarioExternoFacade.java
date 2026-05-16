package org.ccpm.sysse.facade;

import java.util.Date;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.Grupo;
import org.ccpm.sysse.entity.TipoIniciador;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.entity.UsuarioExterno;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class UsuarioExternoFacade extends AbstractFacade<UsuarioExterno> implements UsuarioExternoFacadeLocal {

    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;
    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private TipoIniciadorFacadeLocal tipoIniciadorFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsuarioExternoFacade() {
        super(UsuarioExterno.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public UsuarioExterno findByUsuario(Long idUsuarioSistema) {
        Query consulta = em.createQuery("select object(o) from UsuarioExterno as o WHERE o.usuarioSistema.estado =:p1 and o.usuarioSistema.id = :p2");
        consulta.setParameter("p1", true);
        consulta.setParameter("p2", idUsuarioSistema);
        List<UsuarioExterno> resultados = consulta.getResultList();
        if (resultados == null || resultados.isEmpty()) {
            return null;
        }
        return resultados.get(0);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public UsuarioExterno findUsuarioExterno(Long idUsuario) {
        StringBuilder query = new StringBuilder();
        query.append("select object(o) FROM UsuarioExterno as o  WHERE o.estado = TRUE ");
        query.append(" and o.id = ").append(idUsuario);
        query.append(" ORDER BY o.id");
        Query consulta = em.createQuery(query.toString(), UsuarioExterno.class);
        UsuarioExterno usuarioAux = (UsuarioExterno) consulta.getSingleResult();
        em.clear();
        return usuarioAux;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<UsuarioExterno> findAll(boolean estado) {
        Query consulta = em.createQuery("select DISTINCT o from UsuarioExterno as o INNER JOIN FETCH o.usuarioSistema as u   WHERE  u.estado=:p1 ORDER BY u.apellido, u.nombre");
        consulta.setParameter("p1", true);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<UsuarioExterno> findAll(Long id, String nombre, String documento, String matriculaProvincial, String matriculaNacional, Long idTipoIniciador, int first, int pageSize) {
        StringBuilder query = new StringBuilder();
        query.append("select object(o) FROM UsuarioExterno as o JOIN FETCH o.usuarioSistema u   WHERE o.estado = true ");

        if (id != null && id != 0L) {
            query.append(" and o.id =").append(id);
        }
        if (!nombre.isEmpty()) {
            query.append(" AND (u.nombre LIKE '%").append(nombre.toUpperCase()).append("%' ");
            query.append(" OR u.apellido LIKE '%").append(nombre.toUpperCase()).append("%') ");
        }

        if (!documento.isEmpty()) {
            query.append(" and o.documento LIKE '%").append(Utilidad.desacentuar(documento).toUpperCase()).append("%' ");
        }
        if (!matriculaProvincial.isEmpty()) {
            query.append(" and o.matriculaProvincial LIKE '%").append(Utilidad.desacentuar(matriculaProvincial).toUpperCase()).append("%' ");
        }
        if (!matriculaNacional.isEmpty()) {
            query.append(" and o.matriculaNacional LIKE '%").append(Utilidad.desacentuar(matriculaNacional).toUpperCase()).append("%' ");
        }
        if (idTipoIniciador != null && idTipoIniciador != 0L) {
            query.append(" and o.tipoIniciador.id =").append(idTipoIniciador);
        }
        query.append(" ORDER BY u.apellido, u.nombre ASC");
        Query consulta = em.createQuery(query.toString(), UsuarioExterno.class);
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        List<UsuarioExterno> lstRetorno = consulta.getResultList();
        em.clear();
        return lstRetorno;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countAll(Long id, String nombre, String documento, String matriculaProvincial, String matriculaNacional, Long idTipoIniciador) {
        StringBuilder query = new StringBuilder();
        query.append("select COUNT(o) FROM UsuarioExterno as o JOIN FETCH o.usuarioSistema u   WHERE o.estado = true ");
        if (id != null && id != 0L) {
            query.append(" and o.id =").append(id);
        }
        if (!nombre.isEmpty()) {
            query.append(" AND (u.nombre LIKE '%").append(nombre.toUpperCase()).append("%' ");
            query.append(" OR u.apellido LIKE '%").append(nombre.toUpperCase()).append("%') ");
        }

        if (!documento.isEmpty()) {
            query.append(" and o.documento LIKE '%").append(Utilidad.desacentuar(documento).toUpperCase()).append("%' ");
        }
        if (!matriculaProvincial.isEmpty()) {
            query.append(" and o.matriculaProvincial LIKE '%").append(Utilidad.desacentuar(matriculaProvincial).toUpperCase()).append("%' ");
        }
        if (!matriculaNacional.isEmpty()) {
            query.append(" and o.matriculaNacional LIKE '%").append(Utilidad.desacentuar(matriculaNacional).toUpperCase()).append("%' ");
        }
        if (idTipoIniciador != null && idTipoIniciador != 0L) {
            query.append(" and o.tipoIniciador.id =").append(idTipoIniciador);
        }
        Query consulta = em.createQuery(query.toString());
        return (Long) consulta.getSingleResult();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<UsuarioExterno> findAll(Long idTipoIniciador) {
        StringBuilder query = new StringBuilder();
        query.append("select object(o) FROM UsuarioExterno as o WHERE o.estado = true ");

        if (idTipoIniciador != null && idTipoIniciador != 0L) {
            query.append(" and o.tipoIniciador.id =").append(idTipoIniciador);
        }
        query.append(" ORDER BY o.nombre ASC");
        Query consulta = em.createQuery(query.toString());
        List<UsuarioExterno> lstRetorno = consulta.getResultList();
        em.clear();
        return lstRetorno;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countByDocumento(String documento) {
        Long retorno = 0L;
        try {
            StringBuilder query = new StringBuilder();
            query.append("select COUNT(o) FROM UsuarioExterno as o WHERE o.estado = true ");
            if (!documento.equals("")) {
                query.append(" and o.documento LIKE '").append((documento).toUpperCase()).append("' ");
            }
            Query consulta = em.createQuery(query.toString(), UsuarioExterno.class);
            retorno = (Long) consulta.getSingleResult();
            em.clear();
        } catch (Exception e) {
            retorno = 0L;
        }
        return retorno;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countByMatriculaProvincial(String matriculaProvincial) {
        Long retorno = 0L;
        try {
            StringBuilder query = new StringBuilder();
            query.append("select COUNT(o) FROM UsuarioExterno as o WHERE o.estado = true ");
            if (!matriculaProvincial.equals("")) {
                query.append(" and o.matriculaProvincial LIKE '").append((matriculaProvincial).toUpperCase()).append("' ");
            }
            Query consulta = em.createQuery(query.toString(), UsuarioExterno.class);
            retorno = (Long) consulta.getSingleResult();
            em.clear();
        } catch (Exception e) {
            retorno = 0L;
        }
        return retorno;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countByMatriculaNacional(String matriculaNacional) {
        Long retorno = 0L;
        try {
            StringBuilder query = new StringBuilder();
            query.append("select COUNT(o) FROM UsuarioExterno as o WHERE o.estado = true ");
            if (!matriculaNacional.equals("")) {
                query.append(" and o.matriculaNacional LIKE '").append((matriculaNacional).toUpperCase()).append("' ");
            }
            Query consulta = em.createQuery(query.toString(), UsuarioExterno.class);
            retorno = (Long) consulta.getSingleResult();
            em.clear();
        } catch (Exception e) {
            retorno = 0L;
        }
        return retorno;
    }

    @Override
    public void create(Long idTipoIniciador, String apellido, String nombre, String documento, String matriculaProvincial, String matriculaNacional, String email,
            String uss, String pass, Usuario user) throws Exception {
        try {
            Grupo grupoAux = this.grupoFacade.find(3L);//3L = grupo usuario externo ver bd
            TipoIniciador tipoIniciadorAux = null;

            Usuario usuarioAux = new Usuario();
            usuarioAux.setEstado(true);
            usuarioAux.setNombreUsuario(uss);
            usuarioAux.setClaveAcceso(pass);
            usuarioAux.setFechaAlta(new Date());
            usuarioAux.setFoto("images/perfil.png");
            if (apellido != null) {
                apellido = apellido.toUpperCase();
            }
            usuarioAux.setApellido(apellido);
            usuarioAux.setNombre(nombre.toUpperCase());
            usuarioAux.setEmail(email);
            usuarioAux.getGrupos().add(grupoAux);
            this.usuarioFacade.create(usuarioAux);
            // ** //
            UsuarioExterno objectAux = new UsuarioExterno();
            if (documento != null && !documento.isEmpty()) {
                objectAux.setDocumento(documento);
            } else {
                objectAux.setDocumento("-");
            }
            if (matriculaNacional != null && !matriculaNacional.isEmpty()) {
                objectAux.setMatriculaNacional(matriculaNacional);
            } else {
                objectAux.setMatriculaNacional("");
            }
            if (matriculaProvincial != null && !matriculaProvincial.isEmpty()) {
                objectAux.setMatriculaProvincial(matriculaProvincial);
            } else {
                objectAux.setMatriculaProvincial("");
            }
            if (apellido != null) {
                apellido = apellido.toUpperCase();
                objectAux.setNombre(apellido + " " + nombre.toUpperCase());
                tipoIniciadorAux = this.tipoIniciadorFacade.find(4L); //Ese es el de persona
            } else {
                objectAux.setNombre(nombre.toUpperCase());
                tipoIniciadorAux = this.tipoIniciadorFacade.find(idTipoIniciador);
            }
            objectAux.setEstado(true);
            objectAux.setUsuarioCreacion(user);
            objectAux.setFechaCreacion(new Date());
            objectAux.setTipoIniciador(tipoIniciadorAux);
            this.create(objectAux);
            objectAux.setUsuarioSistema(usuarioAux);
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar crear el usuario externo." + e.getMessage());
        }
    }

    @Override
    public void remove(Long idUsuarioExterno) throws Exception {
        try {
            UsuarioExterno objectAux = this.find(idUsuarioExterno);
            Usuario usuarioAux = objectAux.getUsuarioSistema();
            usuarioAux.setEstado(false);
            objectAux.setEstado(false);
            this.edit(objectAux);

            this.usuarioFacade.edit(usuarioAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar el usuario externo.");
        }
    }

    @Override
    public Long findUltimoID() {
        Long nro = 1L;
        nro = this.usuarioFacade.findUltimoID();
        return nro;
    }

    @Override
    public void edit(Long id, Long idTipoIniciador, String apellido, String nombre,
            String documento, String matriculaProvincial, String matriculaNacional, String email, String uss, String pass, Usuario user) throws Exception {
        try {
            UsuarioExterno objectAux = this.findByUsuario(id);
            Usuario usuarioAux = this.usuarioFacade.find(objectAux.getUsuarioSistema().getId());
            TipoIniciador tipoIniciadorAux = this.tipoIniciadorFacade.find(idTipoIniciador);
            usuarioAux.setNombreUsuario(uss);
            usuarioAux.setClaveAcceso(pass);

            if (apellido != null) {
                apellido = apellido.toUpperCase();
            }
            usuarioAux.setApellido(apellido);
            usuarioAux.setNombre(nombre.toUpperCase());
            usuarioAux.setEmail(email);

            this.usuarioFacade.edit(usuarioAux);
            // ** //
            if (documento.isEmpty()) {
                objectAux.setDocumento("-");
            } else {
                objectAux.setDocumento(documento);
            }
            if (matriculaNacional != null && !matriculaNacional.isEmpty()) {
                objectAux.setMatriculaNacional(matriculaNacional);
            } else {
                objectAux.setMatriculaNacional("");
            }
            if (matriculaProvincial != null && !matriculaProvincial.isEmpty()) {
                objectAux.setMatriculaProvincial(matriculaProvincial);
            } else {
                objectAux.setMatriculaProvincial("");
            }
            if (apellido != null) {
                apellido = apellido.toUpperCase();
                objectAux.setNombre(apellido + " " + nombre.toUpperCase());
            } else {
                objectAux.setNombre(nombre.toUpperCase());
            }
            objectAux.setTipoIniciador(tipoIniciadorAux);
            objectAux.setUsuarioSistema(usuarioAux);
            objectAux.setUsuarioModificacion(usuarioAux);
            objectAux.setFechaModificacion(new Date());
            this.edit(objectAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el usuario externo.");
        }
    }
}
