package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.Mesa;
import org.ccpm.sysse.entity.Organismo;

@Local
public interface MesaFacadeLocal {

    void create(Mesa mesa);

    void edit(Mesa mesa);

    void remove(Mesa mesa);

    Mesa find(Object id);

    List<Mesa> findAll();

    List<Mesa> findRange(int[] range);

    int count();
    
    public java.util.List<org.ccpm.sysse.entity.Mesa> findAll(boolean estado);

    public java.util.List<org.ccpm.sysse.entity.Mesa> findAll(java.lang.String nombre, java.lang.Long idOrganismo,Long id);
    
     public List<Mesa> findByOrganismo(Long idOrganismo);
    
    public java.util.List<org.ccpm.sysse.entity.Mesa> findAllGeneral(java.lang.Long idOrganismo , Long idOrganismoPadre);
//    public java.util.List<org.ccpm.sysse.entity.Mesa> findAllGeneralYMiniEco();

    public void create(java.lang.Long idOrganismo, java.lang.String nombre, java.lang.String descripcion, boolean checked) throws java.lang.Exception;

    public void edit(java.lang.Long idMesa, java.lang.Long idOrganismo, java.lang.String nombre, java.lang.String descripcion, boolean checked) throws java.lang.Exception;

    public void remove(java.lang.Long idMesa) throws java.lang.Exception;

    public Organismo findOrganismoMesa(Long idMesa);

    public List<Mesa> findAllGeneral(Long idOrganismo);

    public List<Mesa> findMesas(List<Long> mesasId);
    
}
