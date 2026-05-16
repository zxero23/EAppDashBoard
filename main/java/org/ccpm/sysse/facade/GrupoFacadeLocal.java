package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Grupo;

@Local
public interface GrupoFacadeLocal {

    void create(Grupo grupo);

    void edit(Grupo grupo);

    void remove(Grupo grupo);

    Grupo find(Object id);

    List<Grupo> findAll();

    List<Grupo> findRange(int[] range);

    int count();

    public java.util.List<org.ccpm.sysse.entity.Grupo> finGrupos(java.util.List<java.lang.Long> gruposId);

    public java.util.List<org.ccpm.sysse.entity.Menu> findAllMenu(java.lang.Long idGrupo);

    public java.util.List<org.ccpm.sysse.entity.Grupo> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.Grupo> findAll(java.lang.String nombreGrupo);

    public java.util.List<org.ccpm.sysse.entity.Grupo> findAll(java.util.List<java.lang.String> gruposId);

    public void remove(java.lang.Long idGrupo) throws java.lang.Exception;

    public void remove(java.lang.Long idGrupo, java.util.List<java.lang.Long> idAcciones) throws java.lang.Exception;

    public void create(java.lang.String nombreGrupo, java.lang.String descripcionGrupo) throws java.lang.Exception;

    public void edit(java.lang.Long idGrupo, java.lang.String nombreGrupo, java.lang.String descripcionGrupo) throws java.lang.Exception;

    public void edit(java.lang.Long idGrupo, java.util.List<java.lang.Long> idMenu) throws java.lang.Exception;
    
}
