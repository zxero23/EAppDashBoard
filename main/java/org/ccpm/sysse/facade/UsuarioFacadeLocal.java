package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Grupo;
import org.ccpm.sysse.entity.Mesa;
import org.ccpm.sysse.entity.Organismo;
import org.ccpm.sysse.entity.Usuario;

@Local
public interface UsuarioFacadeLocal {

    void create(Usuario usuario);

    void edit(Usuario usuario);

    void remove(Usuario usuario);

    Usuario find(Object id);

    List<Usuario> findAll();

    List<Usuario> findRange(int[] range);

    int count();

    public Usuario find(String nombreUsuario, String claveAcceso) throws Exception;

    public List<Usuario> findAll(boolean estado);

    public List<Usuario> findAll(String nombreUsuario);
    
    public List<Usuario> findAll(Long idOrg);
    
    public List<Usuario> findAllMesa(Long idMesa);

    public List<Usuario> findIdentico(String nombreUsuario);

    public void create(String apellido, String nombre, String nombreUss, String correo, List<Grupo> lstGrupo, Long idMesa,Long dni, List<Mesa> mesasDisponibles) throws Exception;

    public void edit(Long id, String apellido, String nombre, String nombreUsuario, String correo, List<Long> listIdGrupos, Long idMesa, Long dni, List<Mesa> mesasDisponibles) throws Exception;

    public void edit(Long id, String claveActual, String claveNueva) throws Exception;

    public void edit(Long idUsuario, List<Long> idMenu) throws Exception;

    public void remove(Long idUsuario, List<Long> idAcciones) throws Exception;

    public void remove(Long idUsuario) throws Exception;

    public boolean isPerteneceContaduria(Long idMesa);

    public Long findUltimoID();

    public Mesa findMesaUsuario(Long idUsuario);

    public Organismo findOrganismoUsuario(Long idUsuario);
    
    public void restorePassword(Long idUsuario) throws Exception;

    public List<Usuario> findAll(String nombreUsuario, String nombre, String apellido, Long idDepto, Long idOrg, Organismo organismoUsuario, int first, int pageSize);

    public Long countAll(String nombreUsuario, String nombre, String apellido, Long idDepto, Long idOrg, Organismo organismoUsuario);

}
