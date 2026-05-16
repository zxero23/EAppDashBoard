/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.ExpedienteEliminado;

/**
 *
 * @author Matias
 */
@Local
public interface ExpedienteEliminadoFacadeLocal {

    void create(ExpedienteEliminado expedienteEliminado);

    void edit(ExpedienteEliminado expedienteEliminado);

    void remove(ExpedienteEliminado expedienteEliminado);

    ExpedienteEliminado find(Object id);

    List<ExpedienteEliminado> findAll();

    List<ExpedienteEliminado> findRange(int[] range);

    int count();
    
}
