package org.ccpm.sysse.facade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.*;
import org.ccpm.sysse.utilidad.MailSenderThread;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class UsuarioFacade extends AbstractFacade<Usuario> implements UsuarioFacadeLocal {

    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;
    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private ActionItemFacadeLocal actionFacade;
    @EJB
    private MenuFacadeLocal menuFacade;
    @EJB
    private MesaFacadeLocal mesaFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsuarioFacade() {
        super(Usuario.class);
    }

    @Override
    public Usuario find(String nombreUsuario, String claveAcceso) throws Exception {
        Usuario usuarioRetorno = null;
        try {
            boolean flag = true;//para controlar el borrado logico;
            Query consulta = em.createQuery("select u from Usuario u where u.nombreUsuario = :p1 and u.claveAcceso = :p2 and u.estado = :p3");
            consulta.setParameter("p1", nombreUsuario);
            consulta.setParameter("p2", claveAcceso);
            consulta.setParameter("p3", flag);
            usuarioRetorno = (Usuario) consulta.getSingleResult();
            if (usuarioRetorno.getUltimoLogin() != null) {
                usuarioRetorno.setUltimoLogin(new Date());
            }
            this.edit(usuarioRetorno);
        } catch (Exception ex) {
            throw new Exception("Fallo inicio de sesión: " + ex.getMessage());
        }
        return usuarioRetorno;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Usuario> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Usuario as o WHERE o.estado = :p1 ORDER BY o.apellido ASC");
        consulta.setParameter("p1", true);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Mesa findMesaUsuario(Long idUsuario) {
        Query consulta = em.createQuery("select object(o) from Usuario as o WHERE o.estado = :p1 and o.id = :p2 ");
        consulta.setParameter("p1", true);
        consulta.setParameter("p2", idUsuario);
        Usuario usuarioAux = (Usuario) consulta.getSingleResult();
        return usuarioAux.getMesa();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Organismo findOrganismoUsuario(Long idUsuario) {
        Query consulta = em.createQuery("select object(o) from Usuario as o WHERE o.estado = :p1 and o.id = :p2 ");
        consulta.setParameter("p1", true);
        consulta.setParameter("p2", idUsuario);
        Usuario usuarioAux = (Usuario) consulta.getSingleResult();
        return usuarioAux.getOrganismo();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Usuario> findAll(String nombreUsuario) {
        return em.createQuery("select object(o) FROM Usuario as o WHERE o.estado = true AND o.nombreUsuario LIKE '" + nombreUsuario + "%' ORDER BY o.nombreUsuario ").getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Usuario> findAll(String nombreUsuario, String nombre, String apellido, Long idDepto, Long idOrg, Organismo organismoUsuario, int first, int pageSize) {
        StringBuilder query = new StringBuilder();
        query.append("select * FROM usuarios as o "
                + " INNER JOIN iniciador on o.mesa_id = iniciador.id "
                + " INNER JOIN mesas on iniciador.id = mesas.id "
                + " INNER JOIN organismo as org on mesas.organismo_id = org.id ");
        if (organismoUsuario != null && organismoUsuario.isAdministraSubOrganismos()) {
            query.append(" LEFT JOIN organismo as orgPadre on org.organismopadre_id = orgPadre.id "
                    + " LEFT JOIN organismo as orgPadrePadre on orgPadre.organismopadre_id = orgPadrePadre.id ");
        }
        query.append(" WHERE o.estado = true ");
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            query.append(" and o.nombre_usuario LIKE '%").append(nombreUsuario).append("%'");
        }
        if (nombre != null && !nombre.isEmpty()) {
            query.append(" and upper(o.nombre) LIKE '%").append(Utilidad.desacentuar(nombre).toUpperCase()).append("%'");
        }
        if (apellido != null && !apellido.isEmpty()) {
            query.append(" and upper(o.apellido) LIKE '%").append(Utilidad.desacentuar(apellido).toUpperCase()).append("%'");
        }
        if (idDepto != null && idDepto != 0L) {
            query.append(" and o.mesa_id = ").append(idDepto);
        }
        if(idOrg!=null && idOrg!=0L){
             query.append(" and org.id = ").append(idOrg);
        }else if(organismoUsuario!=null){
            query.append(" and org.id = ").append(organismoUsuario.getId());
        }
        if (organismoUsuario != null && organismoUsuario.isAdministraSubOrganismos()) {
            query.append(" and (org.id = ").append(organismoUsuario.getId());
            query.append(" or orgPadre.id = ").append(organismoUsuario.getId());
            query.append(" or orgPadrePadre.id = ").append(organismoUsuario.getId()).append(")");
        }
        query.append(" ORDER BY o.apellido ASC");
        Query consulta = em.createNativeQuery(query.toString(), Usuario.class);
        consulta.setFirstResult(first);
        consulta.setMaxResults(pageSize);
        return consulta.getResultList();

    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long countAll(String nombreUsuario, String nombre, String apellido, Long idDepto, Long idOrg, Organismo organismoUsuario) {
        StringBuilder query = new StringBuilder();
        query.append("select COUNT(o.id) FROM usuarios as o "
                + " INNER JOIN iniciador on o.mesa_id = iniciador.id "
                + " INNER JOIN mesas on iniciador.id = mesas.id "
                + " INNER JOIN organismo as org on mesas.organismo_id = org.id ");
        if (organismoUsuario != null && organismoUsuario.isAdministraSubOrganismos()) {
            query.append(" LEFT JOIN organismo as orgPadre on org.organismopadre_id = orgPadre.id "
                    + " LEFT JOIN organismo as orgPadrePadre on orgPadre.organismopadre_id = orgPadrePadre.id ");
        }
        query.append(" WHERE o.estado = true ");
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            query.append(" and o.nombre_usuario LIKE '%").append(nombreUsuario).append("%'");
        }
        if (nombre != null && !nombre.isEmpty()) {
            query.append(" and upper(o.nombre) LIKE '%").append(Utilidad.desacentuar(nombre).toUpperCase()).append("%'");
        }
        if (apellido != null && !apellido.isEmpty()) {
            query.append(" and upper(o.apellido) LIKE '%").append(Utilidad.desacentuar(apellido).toUpperCase()).append("%'");
        }
        if (idDepto != null && idDepto != 0L) {
            query.append(" and o.mesa_id = ").append(idDepto);
        }
        if(idOrg!=null && idOrg!=0L){
             query.append(" and org.id = ").append(idOrg);
        }else if(organismoUsuario!=null){
            query.append(" and org.id = ").append(organismoUsuario.getId());
        }
        if (organismoUsuario != null && organismoUsuario.isAdministraSubOrganismos()) {
            query.append(" and (org.id = ").append(organismoUsuario.getId());
            query.append(" or orgPadre.id = ").append(organismoUsuario.getId());
            query.append(" or orgPadrePadre.id = ").append(organismoUsuario.getId()).append(")");
        }
        Query consulta = em.createNativeQuery(query.toString());
        return (Long) consulta.getSingleResult();

    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<Usuario> findIdentico(String nombreUsuario) {
        StringBuilder query = new StringBuilder();
        query.append("select object(o) FROM Usuario as o WHERE o.estado = true");

        if (!nombreUsuario.equals("")) {
            query.append(" and o.nombreUsuario LIKE '").append(nombreUsuario).append("'");
        }
        query.append(" order by o.nombre ASC");
        Query consulta = em.createQuery(query.toString());

        return consulta.getResultList();
    }

    @Override
    public void create(String apellido, String nombre, String nombreUss, String correo, List<Grupo> lstGrupo, Long idMesa, Long dni, List<Mesa> mesasDisponibles) throws Exception {
        try {

            boolean isNameEqual = false;
            List<Usuario> listUsuario = this.findAll(true);
            for (Usuario usuario : listUsuario) {
                if (usuario.getNombreUsuario().equalsIgnoreCase(nombreUss)) {
                    isNameEqual = true;
                    throw new Exception("Usuario ya empleado.");
                }
            }
            if (isNameEqual) {
                throw new Exception("El nombre del usuario ya existe.");
            }
            Mesa mesaAux = this.mesaFacade.find(idMesa);
            Usuario usuario = new Usuario();
            usuario.setNombreUsuario(nombreUss);
            usuario.setDni(dni);
            String randomPassword = Utilidad.generateRandomPassword(10);
            usuario.setClaveAcceso(randomPassword);
            usuario.setUpdatedPassword(true);
            usuario.setEmail(correo);
            usuario.setEstado(true);
            usuario.setFechaAlta(new Date());
            usuario.setNombre(nombre.toUpperCase());
            usuario.setApellido(apellido.toUpperCase());
            usuario.setMesa(mesaAux);
            if(!mesasDisponibles.contains(mesaAux) && !mesasDisponibles.isEmpty()){
                mesasDisponibles.add(mesaAux);
            }
            usuario.setMesasDisponibles(mesasDisponibles);
            usuario.setOrganismo(mesaAux.getOrganismo());
            usuario.setFoto("images/perfil.png");
            if (usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
                //Mandar mail nueva clave
                String asunto = "Credenciales de acceso para el Sistema de Gestión de Expedientes";
                StringBuilder mensaje = new StringBuilder();
                mensaje.append("Estimado/a ")
                        .append(usuario.getApellidoNombre())
                        .append(" le envíamos en el presente mail las credenciales para acceder al Sistema de Gestión de Expedientes.")
                        .append("\n")
                        .append("\n")
                        .append("Deberá ingresar la primera vez con los siguientes datos: ")
                        .append("\n")
                        .append("\n")
                        .append("Usuario: ").append(usuario.getNombreUsuario())
                        .append("\n")
                        .append("Contraseña: ").append(randomPassword)
                        .append("\n")
                        .append("\n")
                        .append("Tenga en cuenta que la contraseña proporcionada es provisoria, deberá cambiarla al ingresar.")
                        .append("\n")
                        .append("\n")
                        .append("Link para ingresar: ").append("https://www.sysse.misiones.gov.ar/")
                        .append("\n")
                        .append("\n")
                        .append("Centro de Cómputos de la Provincia de Misiones\n")
                        .append("25 de Mayo 1460 - CP 3300\n")
                        .append("Teléfono: 4447479 Centtrex 7479");
                MailSenderThread mailSender = new MailSenderThread(usuario.getEmail(), asunto, mensaje.toString(), new ArrayList(), false);
                mailSender.start();
            }
            this.create(usuario);
            usuario.setGrupos(lstGrupo);
            this.edit(usuario);
        } catch (Exception e) {
            throw new Exception("Error al intentar crear el usuario.");
        }
    }

    @Override
    public void edit(Long id, String apellido, String nombre,
            String nombreUsuario, String correo, List<Long> listIdGrupos,
            Long idMesa, Long dni, List<Mesa> mesasDisponibles) throws Exception {
        try {
            Usuario usuarioAux = this.find(id);
            usuarioAux.setNombreUsuario(nombreUsuario);
            Mesa mesaAux = this.mesaFacade.find(idMesa);
            usuarioAux.setClaveAcceso(usuarioAux.getClaveAcceso());
            usuarioAux.setEmail(correo);
            usuarioAux.setDni(dni);
            usuarioAux.setFechaModificacion(new Date());
            usuarioAux.setApellido(apellido.toUpperCase());
            usuarioAux.setNombre(nombre.toUpperCase());
            List<Grupo> lstGrupoAux = grupoFacade.finGrupos(listIdGrupos);
            usuarioAux.setGrupos(lstGrupoAux);
            usuarioAux.setMesa(mesaAux);
            if(!mesasDisponibles.contains(mesaAux) && !mesasDisponibles.isEmpty()){
                mesasDisponibles.add(mesaAux);
            }
            usuarioAux.setMesasDisponibles(mesasDisponibles);
            usuarioAux.setOrganismo(mesaAux.getOrganismo());
            this.edit(usuarioAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el usuario.");
        }
    }

    @Override
    public void edit(Long id, String claveActual, String claveNueva) throws Exception {
        Usuario usuarioAux = this.find(id);
        if (usuarioAux.getClaveAcceso().equalsIgnoreCase(claveActual)) {
            usuarioAux.setClaveAcceso(claveNueva);
            usuarioAux.setUpdatedPassword(false);
            usuarioAux.setFechaModificacion(new Date());
            usuarioAux.setUltimoLogin(new Date());
            this.edit(usuarioAux);
        } else {
            throw new Exception("La clave actual no es la correcta.");
        }
    }

    @Override
    public void edit(Long idUsuario, List<Long> idMenu) throws Exception {
        try {
            Usuario usuario = this.find(idUsuario);
            List<Menu> listaMenu = menuFacade.findAll(idMenu);
            usuario.setMenus(listaMenu);
            this.edit(usuario);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el usuario.");
        }
    }

    @Override
    public void remove(Long idUsuario, List<Long> idAcciones) throws Exception {
        try {
            Usuario usuario = this.find(idUsuario);
            usuario.setAcciones(this.actionFacade.findAll(idAcciones));
            this.edit(usuario);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar acciones del usuario.");
        }
    }

    @Override
    public void remove(Long idUsuario) throws Exception {
        try {
            Usuario usuarioAux = this.find(idUsuario);
            usuarioAux.setEstado(false);
            this.edit(usuarioAux);
        } catch (Exception e) {
            throw new Exception("Error al intetar borrar el usuario.");
        }
    }

    @Override
    public boolean isPerteneceContaduria(Long idMesa) {
        boolean pertenece = false;
        this.mesaFacade.find(idMesa);

        Mesa mesaAux = this.mesaFacade.find(idMesa);
        Organismo orgAux = mesaAux.getOrganismo();

        while (orgAux.getOrganismoPadre() != null) {
            orgAux = orgAux.getOrganismoPadre();
        }
        if (orgAux.getId().intValue() == 97) { //97 es el id de Contaduría
            pertenece = true;
        }
        return pertenece;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public Long findUltimoID() {
        Long nro = 1L;
        try {
            Number result = (Number) em.createQuery("SELECT MAX(u.id) FROM Usuario u").getSingleResult();

            if (result != null) {
                nro = result.longValue() + 1;
            }
        } catch (Exception ex) {
        }
        return nro;
    }

    @Override
    public List<Usuario> findAll(Long idOrg) {
        StringBuilder query = new StringBuilder();
        query.append("select * FROM usuarios as o "
                + " INNER JOIN iniciador on o.mesa_id = iniciador.id"
                + " INNER JOIN mesas on iniciador.id = mesas.id"
                + " INNER JOIN organismo as org on mesas.organismo_id = org.id"
                + " WHERE o.estado = true ");
        if (idOrg != null && idOrg != 0) {
            query.append(" and (org.id = " + idOrg);
            query.append(" OR org.organismopadre_id = " + idOrg + ") ");

        }
        query.append(" ORDER BY o.apellido ASC, o.nombre ASC");
        Query consulta = em.createNativeQuery(query.toString(), Usuario.class);
        return consulta.getResultList();
    }

    @Override
    public List<Usuario> findAllMesa(Long idMesa) {
        Query consulta = em.createQuery("select object(o) from Usuario as o WHERE o.estado = :p1 and o.mesa.id = :p2 ORDER BY o.apellido ASC");
        consulta.setParameter("p1", true);
        consulta.setParameter("p2", idMesa);
        return consulta.getResultList();
    }

    @Override
    public void restorePassword(Long idUsuario) throws Exception {
        try {
            Usuario usuarioAux = this.find(idUsuario);
            String randomPassword = Utilidad.generateRandomPassword(10);
            usuarioAux.setClaveAcceso(randomPassword);
            usuarioAux.setUpdatedPassword(true);
            this.edit(usuarioAux);
            //Mandar mail nueva clave
            String asunto = "Contraseña de usuario cambiada en Sistema de Gestión de Expedientes";
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Estimado/a ")
                    .append(usuarioAux.getApellidoNombre())
                    .append(" le informamos que se ha cambiado la contraseña de su usuario de Sistema de Gestión de Expedientes.")
                    .append("\n")
                    .append("\n")
                    .append("Deberá ingresar nuevamente con los siguientes datos. Tenga en cuenta que la contraseña proporcionada es provisoria, deberá cambiarla al ingresar.")
                    .append("\n")
                    .append("\n")
                    .append("Usuario: ").append(usuarioAux.getNombreUsuario())
                    .append("\n")
                    .append("Contraseña: ").append(randomPassword)
                    .append("\n")
                    .append("Link para ingresar: ").append("https://www.sysse.misiones.gov.ar/")
                    .append("\n")
                    .append("Por favor no responder este email.")
                    .append("\n")
                    .append("\n")
                    .append("Centro de Cómputos de la Provincia de Misiones\n")
                    .append("25 de Mayo 1460 - CP 3300\n")
                    .append("Teléfono: 4447479 Centtrex 7479");
            MailSenderThread mailSender = new MailSenderThread(usuarioAux.getEmail(), asunto, mensaje.toString(), new ArrayList(), false);
            mailSender.start();
        } catch (Exception e) {
            throw new Exception("Error al intetar borrar el usuario");
        }
    }
}
