/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.dashboard.facade;

import java.util.List;
import javax.ejb.Local;
import org.ccpm.dashboard.entity.Indicador;

/**
 *
 * @author Alvarenga Angel
 */
@Local
public interface IndicadorFacadeLocal {

    void create(Indicador indicador);

    void edit(Indicador indicador);

    void remove(Indicador indicador);

    Indicador find(Object id);

    List<Indicador> findAll();

    List<Indicador> findRange(int[] range);

    int count();

    public List<Indicador> findAll(boolean estado);
    
    public List <Indicador> findAll(boolean estado, Long metaId);

    public List<Indicador> findAll(String leyenda,Long idOrg);

    public void create(Long idMeta,String Descripcion, String leyenda, String Formula,String[] variableIndicador,String[] serie,String[] valores,int serieSize,String kml) throws Exception;

    public void edit(Long id,String Descripcion, String leyenda, String Formula,String[] variableIndicador,String[] serie,String[] valores,int serieSize,String kml ) throws Exception ;

    public void remove(Long id) throws Exception;
    
}
