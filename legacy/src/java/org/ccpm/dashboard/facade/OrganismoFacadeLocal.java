/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import javax.ejb.Local;
import org.ccpm.dashboard.entity.Organismo;

/**
 *
 * @author Alvarenga Angel
 */
@Local
public interface OrganismoFacadeLocal {

    void create(Organismo organismo);

    void edit(Organismo organismo);

    void remove(Organismo organismo);

    Organismo find(Object id);

    List<Organismo> findAll();

    List<Organismo> findRange(int[] range);

    int count();

    public List<Organismo> findAll(boolean estado);
    
    public List <Organismo> findAll(boolean estado,boolean descentralizado);
    
    public List <Organismo> findAll(String leyenda,Long idOrganismo); //metodo busqueda ver tablero, por organismo de usuario

    public List<Organismo> findAll(String leyenda);
    
    public List <Organismo> findAll(boolean estado,Long idOrganismo);
    
    public List <Organismo> findAll(Long idOrganismo);
    
    public List<Organismo> findAllnombre(String leyenda) ;

    public void create(Long idOrganismoPadre, Long codOrganismo, String leyenda, String descripcion, boolean descentralizado) throws Exception;

    public void edit(Long id, Long codOrganismo, String leyenda, String descripcion, boolean descentralizado) throws Exception;

    public void remove(Long id) throws Exception;
    
}
