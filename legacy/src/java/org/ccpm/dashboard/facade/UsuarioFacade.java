/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.ccpm.dashboard.entity.ActionItem;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Menu;
import org.ccpm.dashboard.entity.Organismo;
import org.ccpm.dashboard.entity.Usuario;

/**
 *
 * @author Alvarenga Angel
 */
@Stateless
public class UsuarioFacade extends AbstractFacade<Usuario> implements UsuarioFacadeLocal {
    @PersistenceContext(unitName = "EAppDashboard-ejbPU")
    private EntityManager em;
    @EJB
    private GrupoFacadeLocal grupoFacade;
    @EJB
    private ActionItemFacadeLocal actionFacade;
    @EJB
    private MenuFacadeLocal menuFacade;
    @EJB
    private OrganismoFacadeLocal organismoFacade;
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
            usuarioRetorno.setUltimoLogin(new Date());
            this.edit(usuarioRetorno);
        } catch (Exception ex) {
            throw new Exception("Fallo de autenticación, la contraseña no es válida. Por favor, asegúrate de que el bloqueo de mayúsculas no está activado e inténtalo de nuevo.");
        }
        return usuarioRetorno;
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Usuario> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Usuario as o WHERE o.estado = :p1 ORDER BY o.nombre");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Usuario> findAll(String nombreUsuario) {
        return em.createQuery("select object(o) FROM Usuario as o WHERE o.estado = true AND o.nombre LIKE '" + nombreUsuario + "%' ORDER BY o.nombre ").getResultList();
    } 
    
    @Override
    public void create(Long idOrganismo,String apellido, String nombre, String nombreUss, 
    String clave, String email, List <Grupo> lstGrupo) throws Exception {
        Organismo organismoAux = this.organismoFacade.find(idOrganismo);
        try {
            boolean isNameEqual = false;
            List <Usuario> listUsuario = this.findAll(true);
            for (Usuario usuario : listUsuario) {
                if (usuario.getNombreUsuario().equalsIgnoreCase(nombreUss)) {
                    isNameEqual = true;
                    break;
                }
            }
            if (isNameEqual) {
                throw new Exception("El nombre del usuario ya existe");
            }
            Usuario usuario = new Usuario();
            usuario.setNombreUsuario(nombreUss.toLowerCase());
            usuario.setClaveAcceso(clave.toLowerCase());
            usuario.setEmail(email);
            usuario.setEstado(true);
            usuario.setFechaAlta(new Date());
            usuario.setNombre(nombre.toUpperCase());
            usuario.setApellido(apellido.toUpperCase());
            usuario.setFoto("images/perfil.png");
            usuario.setOrganismo(organismoAux);
            this.create(usuario);
            usuario.setGrupos(lstGrupo);
            this.edit(usuario);
        } catch (Exception e) {
            throw new Exception("Error al intentar crear el usuario, " + e.getMessage());
        }
    }
    
    @Override
     public void edit(Long idOrganismo,Long id, String apellido, String nombre, 
     String nombreUsuario, String clave, String email, List <Long> listIdGrupos) throws Exception {
        Organismo organismoAux = this.organismoFacade.find(idOrganismo);
         try {
            Usuario usuarioAux = this.find(id);
            usuarioAux.setNombreUsuario(nombreUsuario.toLowerCase());
            usuarioAux.setClaveAcceso(clave.toLowerCase());
            usuarioAux.setFechaModificacion(new Date());
            usuarioAux.setApellido(apellido.toUpperCase());
            usuarioAux.setNombre(nombre.toUpperCase());
            usuarioAux.setEmail(email);
            usuarioAux.setOrganismo(organismoAux);
            List <Grupo> lstGrupoAux = grupoFacade.findGrupos(listIdGrupos);
            usuarioAux.setGrupos(lstGrupoAux);
            this.edit(usuarioAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el usuario");
        }
    }
    
    
    @Override
    public void edit(Long id, String claveActual, String claveNueva,
            String claveConfirma) throws Exception {
        boolean var = true;
        System.out.println(id + " - " + claveActual + "-" + claveNueva + " - " + claveConfirma);
        Usuario usuarioAux = this.find(id);
        if (usuarioAux.getClaveAcceso().equals(claveActual)) {
            if (claveNueva.equals(claveConfirma)) {
                usuarioAux.setClaveAcceso(claveNueva);
                usuarioAux.setFechaModificacion(new Date());
                this.edit(usuarioAux);
            } else {
                throw new Exception("La clave nueva y la clave a confirmar son distintas");
            }
        } else {
            throw new Exception("La clave actual no es la correcta");
        }
    }
    
    
    @Override
    public void edit(Long id, String claveActual, String claveNueva, 
    String claveConfirma, String nombreUsuario) throws Exception {
        List <Usuario> listUsuario = this.findAll(true);
        Usuario usuarioAux = this.find(id);
        boolean isNameEqual = false;
        if (!(usuarioAux.getNombreUsuario().equalsIgnoreCase(nombreUsuario))) {
            for (Usuario usuario : listUsuario) {
                if (usuario.getNombreUsuario().equalsIgnoreCase(nombreUsuario)) {
                    isNameEqual = true;
                    break;
                }
            }
        }
        if (!isNameEqual) {
            if (usuarioAux.getClaveAcceso().equals(claveActual)) {
                if (claveNueva.equals(claveConfirma)) {
                    usuarioAux.setNombreUsuario(nombreUsuario);
                    usuarioAux.setClaveAcceso(claveNueva);
                    usuarioAux.setFechaModificacion(new Date());
                    this.edit(usuarioAux);
                } else {
                    throw new Exception("La clave no es correcta");
                }
            } else {
                throw new Exception("La clave actual no es la correcta");
            }
        } else {
            throw new Exception("El usuario ya existe. Intente con otro nombre de usuario");
        }
    }    
    
    @Override
    public void edit(Long idUsuario, List <Long> idMenu) throws Exception {
        try {
            Usuario usuario = this.find(idUsuario);
            if(!(idMenu.isEmpty())){
                List <Menu> listaMenu = menuFacade.findAll(idMenu);
                usuario.setMenus(null);
                usuario.setAcciones(null);
                this.edit(usuario);
                if (!(listaMenu.isEmpty())) {
                    usuario.setMenus(listaMenu);
                    this.edit(usuario);
                    List <ActionItem> listAction = new ArrayList <> ();
                    for (Menu menuAux : listaMenu) {
                        List <Menu> listaSubMenuAux = menuAux.getMenus();
                        if (!(listaSubMenuAux.isEmpty())) {
                            for (Menu subMenuAux : listaSubMenuAux) {
                                listAction.addAll(actionFacade.findAll(subMenuAux.getId()));
                            }
                        }
                        listAction.addAll(actionFacade.findAll(menuAux.getId()));
                    }
                    usuario.setAcciones(listAction);
                    this.edit(usuario);
                }
            }else{
                usuario.setMenus(null);
                usuario.setAcciones(null);
                this.edit(usuario);
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el usuario");
        }
    }
    
    @Override
    public void remove(Long idUsuario, List <Long> idAcciones) throws Exception {
        try {
            Usuario usuario = this.find(idUsuario);
            if(!(idAcciones.isEmpty())){
                usuario.getAcciones().clear();
                usuario.setAcciones(this.actionFacade.findAll(idAcciones));
                this.edit(usuario);
            }else{
                usuario.getAcciones().clear();
                this.edit(usuario);
            }
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar acciones del usuario");
        }
    }
    
    @Override
    public void remove(Long idUsuario) throws Exception {
        try {
            Usuario usuarioAux = this.find(idUsuario);
            usuarioAux.setEstado(false);
            this.edit(usuarioAux);
        } catch (Exception e) {
            throw new Exception("Error al intetar borrar el usuario");
        }
    }

    
    
}
