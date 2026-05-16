/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.dashboard.entity.Grupo;
import org.ccpm.dashboard.entity.Usuario;

/**
 *
 * @author Alvarenga Angel
 */
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

    public void create(Long idOrganismo,String apellido, String nombre, String nombreUss, String clave, String email, List<Grupo> lstGrupo) throws Exception;

    public void edit(Long idOrganismo,Long id, String apellido, String nombre, String nombreUsuario, String clave, String email, List<Long> listIdGrupos) throws Exception;

    

    public void edit(Long idUsuario, List<Long> idMenu) throws Exception;
    
    public void edit(Long id, String claveActual, String claveNueva, 
    String claveConfirma, String nombreUsuario) throws Exception;

    public void remove(Long idUsuario, List<Long> idAcciones) throws Exception;

    public void remove(Long idUsuario) throws Exception;
    
    public void edit(Long id, String claveActual, String claveNueva, String claveConfirma) throws Exception;
    
}
