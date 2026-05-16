/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.sysse.facade;

import java.util.List;
import jakarta.ejb.Local;
import org.ccpm.sysse.entity.NotaEliminada;

/**
 *
 * @author Matias
 */
@Local
public interface NotaEliminadaFacadeLocal {

    void create(NotaEliminada notaEliminada);

    void edit(NotaEliminada notaEliminada);

    void remove(NotaEliminada notaEliminada);

    NotaEliminada find(Object id);

    List<NotaEliminada> findAll();

    List<NotaEliminada> findRange(int[] range);

    int count();
    
}
