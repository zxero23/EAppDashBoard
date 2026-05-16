package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.entity.UsuarioExterno;

@Local
public interface UsuarioExternoFacadeLocal {

    void create(UsuarioExterno usuarioExterno);

    void edit(UsuarioExterno usuarioExterno);

    void remove(UsuarioExterno usuarioExterno);

    UsuarioExterno find(Object id);

    List<UsuarioExterno> findAll();

    List<UsuarioExterno> findRange(int[] range);

    int count();

    public List<UsuarioExterno> findAll(boolean estado);

    public void create(Long idTipoIniciador, String apellido, String nombre, String documento, String matriculaProvincial, String matriculaNacional,
            String email, String uss, String pass, Usuario usuario) throws Exception;

    public void remove(Long idUsuarioExterno) throws Exception;

    public void edit(Long idUsuarioExterno, Long idTipoIniciador, String apellido, String nombre, String documento,
            String matriculaProvincial, String matriculaNacional, String email, String uss, String pass, Usuario usuario) throws Exception;

    public UsuarioExterno findByUsuario(Long idUsuarioSistema);
    
    public UsuarioExterno findUsuarioExterno(Long idUsuario);

    public Long findUltimoID();

    public List<UsuarioExterno> findAll(Long idTipoIniciador);

    public Long countAll(Long id, String nombre, String documento, String matriculaProvincial, String matriculaNacional, Long idTipoIniciador);

    public List<UsuarioExterno> findAll(Long id, String nombre, String documento, String matriculaProvincial, String matriculaNacional, Long idTipoIniciador, int first, int pageSize);

    public Long countByMatriculaProvincial(String matriculaProvincial);

    public Long countByMatriculaNacional(String matriculaNacional);

    public Long countByDocumento(String documento);
}
