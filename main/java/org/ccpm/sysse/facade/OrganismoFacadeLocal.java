package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Organismo;

@Local
public interface OrganismoFacadeLocal {

    void create(Organismo organismo);

    void edit(Organismo organismo);

    void remove(Organismo organismo);

    Organismo find(Object id);

    List<Organismo> findAll();

    List<Organismo> findRange(int[] range);

    int count();

    public java.util.List<org.ccpm.sysse.entity.Organismo> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.Organismo> findAll(java.lang.String descripcion);

    public void create(java.lang.Long idOrganismoPadre, java.lang.Long codOrganigrama, java.lang.String nombre, java.lang.String descripcion) throws java.lang.Exception;

    public void remove(java.lang.Long idOrganismo) throws java.lang.Exception;

    public java.util.List<org.ccpm.sysse.entity.Organismo> findAll(java.lang.Long codigo, java.lang.String nombre,Long id);

    public void edit(java.lang.Long idOrganismo, java.lang.String nombre, 
            java.lang.String descripcion, java.util.Date fecha, java.lang.Long nro, java.lang.Long idOrganismoPadre,boolean implementa) throws java.lang.Exception;

    public java.util.List<org.ccpm.sysse.entity.Organismo> findByCodigo(java.lang.Long codOrganismo);

    
    
}
